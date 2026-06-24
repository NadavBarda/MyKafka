package configs;

import graph.Agent;
import graph.Message;

public class LogAgent implements Agent {

    @Override
    public String getName() {
        return "LogAgent";
    }

    @Override
    public void reset() {

    }

    @Override
    public void callback(String topic, Message msg) {
        System.out.println("LogAgent: " + topic + " " + msg.asText);
    }

    @Override
    public void close() {
    }

}
