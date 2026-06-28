package views;

import configs.Graph;
import configs.Node;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.Message;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * HtmlGraphWriter generates the HTML/JS representation of a computational
 * Graph.
 * It uses a layered layout algorithm to automatically position nodes on a
 * canvas.
 */
public class HtmlGraphWriter {

    /**
     * Generates a list of HTML source code lines representing the computational
     * graph.
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
            return "window.topics = [];\nwindow.agents = [];\nwindow.edges = [];\n";
        }

        List<Node> topicNodes = new ArrayList<>();
        List<Node> agentNodes = new ArrayList<>();
        separateNodes(graph, topicNodes, agentNodes);

        Map<Node, Integer> ranks = assignRanks(graph);
        Map<String, int[]> positions = computePositions(graph, ranks);

        StringBuilder sb = new StringBuilder();
        sb.append(serializeTopics(topicNodes, positions));
        sb.append(serializeAgents(agentNodes, positions));
        sb.append(serializeEdges(graph));

        return sb.toString();
    }

    private static void separateNodes(Graph graph, List<Node> topicNodes, List<Node> agentNodes) {
        for (Node node : graph) {
            if (node.getName().startsWith("T")) {
                topicNodes.add(node);
            } else if (node.getName().startsWith("A")) {
                agentNodes.add(node);
            }
        }
    }

    private static Map<String, int[]> computePositions(Graph graph, Map<Node, Integer> ranks) {
        Map<Integer, List<Node>> nodesByRank = new TreeMap<>();
        for (Node node : graph) {
            int r = ranks.getOrDefault(node, 0);
            nodesByRank.computeIfAbsent(r, k -> new ArrayList<>()).add(node);
        }

        int maxRank = nodesByRank.isEmpty() ? 0 : ((TreeMap<Integer, List<Node>>) nodesByRank).lastKey();
        Map<String, int[]> positions = new HashMap<>();
        int width = 600;
        int height = 400;
        int horizontalMargin = 80;
        int verticalMargin = 50;

        for (Map.Entry<Integer, List<Node>> entry : nodesByRank.entrySet()) {
            int rank = entry.getKey();
            List<Node> colNodes = entry.getValue();
            int nodesInCol = colNodes.size();

            int x = (maxRank == 0)
                    ? width / 2
                    : horizontalMargin + rank * ((width - 2 * horizontalMargin) / maxRank);

            for (int i = 0; i < nodesInCol; i++) {
                int y = (nodesInCol == 1)
                        ? height / 2
                        : verticalMargin + i * ((height - 2 * verticalMargin) / (nodesInCol - 1));
                positions.put(colNodes.get(i).getName(), new int[] { x, y });
            }
        }
        return positions;
    }

    private static String serializeTopics(List<Node> topicNodes, Map<String, int[]> positions) {
        StringBuilder sb = new StringBuilder();
        sb.append("window.topics = [\n");
        for (int i = 0; i < topicNodes.size(); i++) {
            Node node = topicNodes.get(i);
            String rawName = node.getName().substring(1);

            String value = "N/A";
            Topic t = TopicManagerSingleton.get().getTopic(rawName);
            if (t != null) {
                Message lastMsg = t.getLastMessage();
                if (lastMsg != null) {
                    value = lastMsg.asText;
                }
            }

            int[] pos = positions.getOrDefault(node.getName(), new int[] { 300, 200 });
            sb.append(String.format("    { id: '%s', value: '%s', x: %d, y: %d }",
                    escapeJs(rawName), escapeJs(value), pos[0], pos[1]));
            if (i < topicNodes.size() - 1)
                sb.append(",");
            sb.append("\n");
        }
        sb.append("];\n\n");
        return sb.toString();
    }

    private static String serializeAgents(List<Node> agentNodes, Map<String, int[]> positions) {
        StringBuilder sb = new StringBuilder();
        sb.append("window.agents = [\n");
        for (int i = 0; i < agentNodes.size(); i++) {
            Node node = agentNodes.get(i);
            String rawName = node.getName().substring(1);
            int[] pos = positions.getOrDefault(node.getName(), new int[] { 300, 200 });
            sb.append(String.format("    { id: '%s', x: %d, y: %d }",
                    escapeJs(rawName), pos[0], pos[1]));
            if (i < agentNodes.size() - 1)
                sb.append(",");
            sb.append("\n");
        }
        sb.append("];\n\n");
        return sb.toString();
    }

    private static String serializeEdges(Graph graph) {
        StringBuilder sb = new StringBuilder();
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

        int maxRelaxations = graph.size() * graph.size();
        int relaxations = 0;

        // Relax ranks
        while (!queue.isEmpty()) {
            if (relaxations++ > maxRelaxations) {
                break; // Prevent infinite loop in case of cycles
            }
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
        if (input == null)
            return "";
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
                "</html>");
    }
}
