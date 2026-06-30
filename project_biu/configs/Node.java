package configs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.Message;

public class Node {
    private String name;
    private List<Node> edges;
    private Message message;

    public Node(String name) {
        this.name = name != null ? name : "";
        this.edges = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public List<Node> getEdges() {
        if (edges == null) {
            edges = new ArrayList<>();
        }
        return edges;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges != null ? edges : new ArrayList<>();
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void addEdge(Node node) {
        if (node == null || node == this) {
            return;
        }

        if (this.edges == null) {
            this.edges = new ArrayList<>();
        }
        this.edges.add(node);
    }

    public boolean hasCycles() {
        return hasCyclesHelper(this, new HashSet<>(), new HashSet<>());
    }

    private boolean hasCyclesHelper(Node current, Set<Node> visited, Set<Node> recursionStack) {
        if (current == null) {
            return false;
        }
        if (recursionStack.contains(current)) {
            return true;
        }
        if (visited.contains(current)) {
            return false;
        }

        visited.add(current);
        recursionStack.add(current);

        List<Node> neighbors = current.getEdges();
        if (neighbors != null) {
            for (Node neighbor : neighbors) {
                if (neighbor != null) {
                    if (hasCyclesHelper(neighbor, visited, recursionStack)) {
                        return true;
                    }
                }
            }
        }

        recursionStack.remove(current);
        return false;
    }
}