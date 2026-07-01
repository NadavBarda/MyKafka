package configs;

// Singleton container for the active computational graph.
public class GraphSingleton {

    // Holder class implementing the Singleton pattern for the graph.
    public static class GraphHolder {
        private static final GraphHolder instance = new GraphHolder();
        private volatile Graph graph = null;

        private GraphHolder() {}

        // Retrieves the current graph instance.
        public synchronized Graph get() {
            return this.graph;
        }

        // Replaces the current graph instance.
        public synchronized void set(Graph graph) {
            this.graph = graph;
        }

        // Clears the active graph reference.
        public synchronized void clear() {
            this.graph = null;
        }
    }

    // Accesses the global graph holder singleton instance.
    public static GraphHolder get() {
        return GraphHolder.instance;
    }
}
