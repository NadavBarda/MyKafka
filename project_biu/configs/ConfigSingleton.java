package configs;

/**
 * Singleton container for the active system configuration.
 * Provides thread-safe global access to the current {@link Config} instance.
 */
public class ConfigSingleton {

    /**
     * Holder class implementing the Singleton pattern for the configuration.
     * Thread-safety is achieved using synchronized accessors.
     */
    public static class ConfigHolder {
        private static final ConfigHolder instance = new ConfigHolder();
        private volatile Config config = null;

        private ConfigHolder() {}

        /**
         * Retrieves the current configuration instance.
         *
         * @return the active Config instance, or null if no configuration is loaded.
         */
        public synchronized Config get() {
            return this.config;
        }

        /**
         * Safely replaces the current configuration. 
         * Automatically closes the previous configuration if it exists to release resources (e.g., active agents).
         *
         * @param config the new Config instance to set.
         */
        public synchronized void set(Config config) {
            if (this.config != null) {
                this.config.close();
            }
            this.config = config;
        }

        /**
         * Closes the active configuration (stopping all background agent threads) and removes the reference.
         */
        public synchronized void close() {
            if (this.config != null) {
                this.config.close();
                this.config = null;
            }
        }
    }

    /**
     * Accesses the global configuration holder singleton instance.
     *
     * @return the ConfigHolder instance.
     */
    public static ConfigHolder get() {
        return ConfigHolder.instance;
    }
}
