package config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;

public class Graph extends ArrayList<Node> {

    public boolean hasCycles() {
        for (Node node : this) {
            if (node != null && node.hasCycles()) {
                return true;
            }
        }
        return false;
    }

    public void createFromTopics() {
        this.clear();
        Map<String, Node> nodeMap = new HashMap<>();

        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        if (tm.getTopics() == null) {
            return;
        }

        for (Topic topic : tm.getTopics()) {
            if (topic == null || topic.name == null) {
                continue;
            }
            String topicNodeName = "T" + topic.name;
            Node topicNode = nodeMap.computeIfAbsent(topicNodeName, k -> new Node(k));

            List<Agent> subs = topic.getSubs();
            List<Agent> pubs = topic.getPubs();

            if (subs != null) {
                for (Agent agent : subs) {
                    if (agent == null || agent.getName() == null) {
                        continue;
                    }
                    String agentNodeName = "A" + agent.getName();
                    Node agentNode = nodeMap.computeIfAbsent(agentNodeName, k -> new Node(k));
                    topicNode.addEdge(agentNode);
                }
            }

            if (pubs != null) {
                for (Agent agent : pubs) {
                    if (agent == null || agent.getName() == null) {
                        continue;
                    }
                    String agentNodeName = "A" + agent.getName();
                    Node agentNode = nodeMap.computeIfAbsent(agentNodeName, k -> new Node(k));
                    agentNode.addEdge(topicNode);
                }
            }
        }

        this.addAll(nodeMap.values());
    }
}
