package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import server.RequestParser.RequestInfo;

/**
 * GraphDataServlet serializes the current state of the Topic/Agent graph
 * and returns it as a JSON payload for the client to render dynamically.
 */
public class GraphDataServlet implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        Collection<Topic> topics = TopicManagerSingleton.get().getTopics();
        Set<String> uniqueAgentNames = new HashSet<>();
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Serialize Topics
        json.append("\"topics\": [");
        boolean firstTopic = true;
        for (Topic t : topics) {
            if (!firstTopic) json.append(",");
            firstTopic = false;
            
            Message msg = t.getLastMessage();
            String val = msg != null ? msg.asText : "null";
            
            json.append("{")
                .append("\"id\": \"").append(escapeJson(t.getName())).append("\",")
                .append("\"value\": \"").append(escapeJson(val)).append("\"")
                .append("}");
                
            for (Agent sub : t.getSubs()) uniqueAgentNames.add(sub.getName());
            for (Agent pub : t.getPubs()) uniqueAgentNames.add(pub.getName());
        }
        json.append("],");
        
        // Serialize Agents
        json.append("\"agents\": [");
        boolean firstAgent = true;
        for (String agentName : uniqueAgentNames) {
            if (!firstAgent) json.append(",");
            firstAgent = false;
            
            json.append("{")
                .append("\"id\": \"").append(escapeJson(agentName)).append("\"")
                .append("}");
        }
        json.append("],");
        
        // Serialize Edges
        json.append("\"edges\": [");
        boolean firstEdge = true;
        for (Topic t : topics) {
            for (Agent sub : t.getSubs()) {
                if (!firstEdge) json.append(",");
                firstEdge = false;
                json.append("{")
                    .append("\"from\": \"").append(escapeJson(t.getName())).append("\",")
                    .append("\"to\": \"").append(escapeJson(sub.getName())).append("\"")
                    .append("}");
            }
            for (Agent pub : t.getPubs()) {
                if (!firstEdge) json.append(",");
                firstEdge = false;
                json.append("{")
                    .append("\"from\": \"").append(escapeJson(pub.getName())).append("\",")
                    .append("\"to\": \"").append(escapeJson(t.getName())).append("\"")
                    .append("}");
            }
        }
        json.append("]");
        json.append("}");
        
        String body = json.toString();
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                body;

        toClient.write(response.getBytes(StandardCharsets.UTF_8));
        toClient.flush();
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
