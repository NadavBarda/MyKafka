package views;

import config.Graph;
import config.Node;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.Message;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * HtmlGraphWriter generates the HTML/JS representation of a computational Graph.
 * It uses a layered layout algorithm to automatically position nodes on a canvas.
 */
public class HtmlGraphWriter {

    /**
     * Generates a list of HTML source code lines representing the computational graph.
     *
     * @param graph the computational Graph instance.
     * @return a List of strings containing the complete HTML response.
     */
    public static List<String> getGraphHTML(Graph graph) {
        List<String> templateLines = new ArrayList<>();
        File templateFile = new File("html_files/graph.html");

        if (templateFile.exists() && templateFile.isFile()) {
            try {
                templateLines = Files.readAllLines(templateFile.toPath(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("Error reading graph.html template: " + e.getMessage());
                templateLines = getFallbackTemplate();
            }
        } else {
            templateLines = getFallbackTemplate();
        }

        // Generate the dynamic JS arrays containing nodes and edges
        String jsData = generateJsData(graph);

        // Replace the placeholder comment in the template
        List<String> outputLines = new ArrayList<>();
        for (String line : templateLines) {
            if (line.contains("// DATA_PLACEHOLDER") || line.contains("DATA_PLACEHOLDER")) {
                outputLines.add(jsData);
            } else {
                outputLines.add(line);
            }
        }

        return outputLines;
    }

    private static String generateJsData(Graph graph) {
        if (graph == null || graph.isEmpty()) {
            return "const topics = [];\nconst agents = [];\nconst edges = [];\n";
        }

        // 1. Separate Topic nodes and Agent nodes
        List<Node> topicNodes = new ArrayList<>();
        List<Node> agentNodes = new ArrayList<>();
        for (Node node : graph) {
            if (node.getName().startsWith("T")) {
                topicNodes.add(node);
            } else if (node.getName().startsWith("A")) {
                agentNodes.add(node);
            }
        }

        // 2. Perform a Layered/Rank Layout Algorithm to compute X and Y coordinates
        Map<Node, Integer> ranks = assignRanks(graph);

        // Group nodes by rank to layout vertically in columns
        Map<Integer, List<Node>> nodesByRank = new TreeMap<>();
        for (Node node : graph) {
            int r = ranks.getOrDefault(node, 0);
            nodesByRank.computeIfAbsent(r, k -> new ArrayList<>()).add(node);
        }

        int maxRank = nodesByRank.isEmpty() ? 0 : ((TreeMap<Integer, List<Node>>) nodesByRank).lastKey();

        // Canvas size: 600 width, 400 height
        Map<String, int[]> positions = new HashMap<>(); // nodeName -> [x, y]
        int width = 600;
        int height = 400;
        int horizontalMargin = 80;
        int verticalMargin = 50;

        for (Map.Entry<Integer, List<Node>> entry : nodesByRank.entrySet()) {
            int rank = entry.getKey();
            List<Node> colNodes = entry.getValue();
            int nodesInCol = colNodes.size();

            // Compute X coordinate for this column
            int x = (maxRank == 0) 
                    ? width / 2 
                    : horizontalMargin + rank * ((width - 2 * horizontalMargin) / maxRank);

            // Compute Y coordinates for nodes in this column
            for (int i = 0; i < nodesInCol; i++) {
                int y;
                if (nodesInCol == 1) {
                    y = height / 2;
                } else {
                    y = verticalMargin + i * ((height - 2 * verticalMargin) / (nodesInCol - 1));
                }
                positions.put(colNodes.get(i).getName(), new int[]{x, y});
            }
        }

        // 3. Serialize topics
        StringBuilder sb = new StringBuilder();
        sb.append("window.topics = [\n");
        for (int i = 0; i < topicNodes.size(); i++) {
            Node node = topicNodes.get(i);
            String rawName = node.getName().substring(1); // Strip prefix 'T'
            
            // Retrieve last message value from TopicManager
            String value = "N/A";
            Topic t = TopicManagerSingleton.get().getTopic(rawName);
            if (t != null) {
                Message lastMsg = t.getLastMessage();
                if (lastMsg != null) {
                    value = lastMsg.asText;
                }
            }

            int[] pos = positions.getOrDefault(node.getName(), new int[]{300, 200});
            sb.append(String.format("    { id: '%s', value: '%s', x: %d, y: %d }", 
                    escapeJs(rawName), escapeJs(value), pos[0], pos[1]));
            if (i < topicNodes.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("];\n\n");

        // 4. Serialize agents
        sb.append("window.agents = [\n");
        for (int i = 0; i < agentNodes.size(); i++) {
            Node node = agentNodes.get(i);
            String rawName = node.getName().substring(1); // Strip prefix 'A'
            int[] pos = positions.getOrDefault(node.getName(), new int[]{300, 200});
            sb.append(String.format("    { id: '%s', x: %d, y: %d }", 
                    escapeJs(rawName), pos[0], pos[1]));
            if (i < agentNodes.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("];\n\n");

        // 5. Serialize edges
        sb.append("window.edges = [\n");
        List<String> edgeList = new ArrayList<>();
        for (Node srcNode : graph) {
            for (Node destNode : srcNode.getEdges()) {
                String srcName = srcNode.getName().substring(1);
                String destName = destNode.getName().substring(1);
                boolean isAgent = srcNode.getName().startsWith("T");
                edgeList.add(String.format("    { from: '%s', to: '%s', isAgent: %b }", 
                        escapeJs(srcName), escapeJs(destName), isAgent));
            }
        }
        sb.append(String.join(",\n", edgeList));
        sb.append("\n];\n");

        return sb.toString();
    }

    /**
     * Computes ranks (layers) for each node using topological relaxation.
     */
    private static Map<Node, Integer> assignRanks(Graph graph) {
        Map<Node, Integer> ranks = new HashMap<>();
        Map<Node, Integer> inDegree = new HashMap<>();

        // Compute in-degrees
        for (Node node : graph) {
            inDegree.putIfAbsent(node, 0);
            for (Node neighbor : node.getEdges()) {
                inDegree.put(neighbor, inDegree.getOrDefault(neighbor, 0) + 1);
            }
        }

        // Queue for BFS relaxation
        Queue<Node> queue = new LinkedList<>();
        for (Node node : graph) {
            if (inDegree.getOrDefault(node, 0) == 0) {
                ranks.put(node, 0);
                queue.add(node);
            }
        }

        // Fallback: If there are cycles and queue is empty, initialize all to 0
        if (queue.isEmpty()) {
            for (Node node : graph) {
                ranks.put(node, 0);
                queue.add(node);
            }
        }

        // Relax ranks
        while (!queue.isEmpty()) {
            Node u = queue.poll();
            int uRank = ranks.getOrDefault(u, 0);
            for (Node v : u.getEdges()) {
                int vRank = ranks.getOrDefault(v, 0);
                if (uRank + 1 > vRank) {
                    ranks.put(v, uRank + 1);
                    queue.add(v);
                }
            }
        }

        return ranks;
    }

    private static String escapeJs(String input) {
        if (input == null) return "";
        return input.replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static List<String> getFallbackTemplate() {
        return Arrays.asList(
            "<!DOCTYPE html>",
            "<html lang=\"en\">",
            "<head>",
            "    <meta charset=\"UTF-8\">",
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">",
            "    <title>Graph View</title>",
            "    <link rel=\"stylesheet\" href=\"/app/graph.css\">",
            "</head>",
            "<body>",
            "    <canvas id=\"graphCanvas\" width=\"600\" height=\"400\"></canvas>",
            "    <script>",
            "        // DATA_PLACEHOLDER",
            "    </script>",
            "    <script src=\"/app/graph.js\"></script>",
            "</body>",
            "</html>"
        );
    }
}
