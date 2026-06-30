package configs;

import graph.TopicManagerSingleton;

/**
 * Thread-safe centralized utility service to handle system-wide cleanup and reset operations.
 * Coordinates closing/clearing all global single sources of truth.
 */
public class SystemResetService {

    private static final Object lock = new Object();

    /**
     * Resets and cleans all active configuration, running agent threads, 
     * topic listings, and calculated computational graphs in a thread-safe manner.
     */
    public static void cleanAll() {
        synchronized (lock) {
            // 1. Close and clean up the current configuration (stops old running agents)
            ConfigSingleton.get().close();

            // 2. Clear out all active topics from the TopicManager singleton
            TopicManagerSingleton.get().clear();

            // 3. Clear out the global computational graph reference
            GraphSingleton.get().clear();
        }
    }
}
