package configs;

import graph.TopicManagerSingleton;

// Handles system-wide cleanup and reset.
public class SystemResetService {

    private static final Object lock = new Object();

    // Resets configuration, running agents, topics, and graphs.
    public static void cleanAll() {
        synchronized (lock) {
            // Clean up config, topics, and graph references
            ConfigSingleton.get().close();
            TopicManagerSingleton.get().clear();
            GraphSingleton.get().clear();
        }
    }
}
