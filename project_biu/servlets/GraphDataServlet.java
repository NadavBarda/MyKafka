package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

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

    private final Gson gson = new Gson();

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            Collection<Topic> topics = TopicManagerSingleton.get().getTopics();
            Set<String> uniqueAgentNames = new HashSet<>();
            
            List<Map<String, String>> topicsData = new ArrayList<>();
            List<Map<String, String>> edgesData = new ArrayList<>();
            
            for (Topic t : topics) {
                Message msg = t.getLastMessage();
                String val = msg != null ? msg.asText : "";
                
                Map<String, String> topicMap = new HashMap<>();
                topicMap.put("id", t.getName());
                topicMap.put("value", val);
                topicsData.add(topicMap);
                
                for (Agent sub : t.getSubs()) {
                    uniqueAgentNames.add(sub.getName());
                    Map<String, String> edgeMap = new HashMap<>();
                    edgeMap.put("from", t.getName());
                    edgeMap.put("to", sub.getName());
                    edgesData.add(edgeMap);
                }
                
                for (Agent pub : t.getPubs()) {
                    uniqueAgentNames.add(pub.getName());
                    Map<String, String> edgeMap = new HashMap<>();
                    edgeMap.put("from", pub.getName());
                    edgeMap.put("to", t.getName());
                    edgesData.add(edgeMap);
                }
            }
            
            List<Map<String, String>> agentsData = new ArrayList<>();
            for (String agentName : uniqueAgentNames) {
                Map<String, String> agentMap = new HashMap<>();
                agentMap.put("id", agentName);
                agentsData.add(agentMap);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("topics", topicsData);
            responseData.put("agents", agentsData);
            responseData.put("edges", edgesData);
            
            String body = gson.toJson(responseData);
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "Connection: close\r\n\r\n" +
                    body;

            toClient.write(response.getBytes(StandardCharsets.UTF_8));
            toClient.flush();
        } catch (Exception e) {
            if (ri != null) {
                server.RequestLogger.logError(ri.getClientAddress(), ri.getHttpCommand(), ri.getUri(), "Exception in GraphDataServlet", e);
            }
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}
