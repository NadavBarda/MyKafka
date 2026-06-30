package configs;

/**
 * Singleton container for the active computational graph.
 * Provides thread-safe global access to the current {@link Graph} instance.
 */
public class GraphSingleton {

    /**
     * Holder class implementing the Singleton pattern for the graph.
     * Thread-safety is achieved using synchronized accessors.
     */
    public static class GraphHolder {
        private static final GraphHolder instance = new GraphHolder();
        private volatile Graph graph = null;

        private GraphHolder() {}

        /**
         * Retrieves the current graph instance.
         *
         * @return the active Graph instance, or null if no graph is loaded.
         */
        public synchronized Graph get() {
            return this.graph;
        }

        /**
         * Safely replaces the current graph instance.
         *
         * @param graph the new Graph instance to set.
         */
        public synchronized void set(Graph graph) {
            this.graph = graph;
        }

        /**
         * Clears the active graph reference.
         */
        public synchronized void clear() {
            this.graph = null;
        }
    }

    /**
     * Accesses the global graph holder singleton instance.
     *
     * @return the GraphHolder instance.
     */
    public static GraphHolder get() {
        return GraphHolder.instance;
    }
}
