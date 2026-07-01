package configs;


public class ConfigSingleton {

    public static class ConfigHolder {
        private static final ConfigHolder instance = new ConfigHolder();
        private volatile Config config = null;

        private ConfigHolder() {}

        
        public synchronized Config get() {
            return this.config;
        }

        // Replaces the active configuration, closing the old one if it exists.
        public synchronized void set(Config config) {
            if (this.config != null) {
                this.config.close();
            }
            this.config = config;
        }

        // Closes the current config and clears the reference.
        public synchronized void close() {
            if (this.config != null) {
                this.config.close();
                this.config = null;
            }
        }
    }

    public static ConfigHolder get() {
        return ConfigHolder.instance;
    }
}
