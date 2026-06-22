package config;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import server.HTTPServer;

public class ConfigUpdateAgent implements Agent {
    private final String name = "ConfigUpdateAgent";
    private final HTTPServer server;

    public ConfigUpdateAgent(HTTPServer server) {
        this.server = server;
        TopicManagerSingleton.get().getTopic("Configuration").subscribe(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
    }

    @Override
    public void callback(String topic, Message msg) {
        if (msg != null && msg.asText != null && !msg.asText.isEmpty()) {
            String fileName = msg.asText;

            GenericConfig conf = new GenericConfig();
            conf.setConfFile("assets/config_files/" + fileName);
            conf.create();

            if (this.server != null) {
                this.server.setConfig(conf);
                System.out.println("Config updated");
            }
        }
    }

    @Override
    public void close() {
    }
}
