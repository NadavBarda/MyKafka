package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class PlusAgent implements Agent {
    private String topicX;
    private String topicY;
    private String topicPub;
    private double x = 0;
    private double y = 0;

    public PlusAgent(String[] subs, String[] pubs) {
        validateDependencies(subs, pubs);

        TopicManager tm = TopicManagerSingleton.get();

        this.topicX = subs[0];
        this.topicY = subs[1];
        this.topicPub = pubs[0];

        tm.getTopic(topicX).subscribe(this);
        tm.getTopic(topicY).subscribe(this);
        tm.getTopic(topicPub).addPublisher(this);

    }

    private void validateDependencies(String[] subs, String[] pubs) {
        if (subs == null || pubs == null) {
            throw new IllegalArgumentException("Subs and pubs cannot be null");
        }
        if (subs.length < 2 || pubs.length < 1) {
            throw new IllegalArgumentException("Subs and pubs arrays must have at least 2 and 1 elements respectively");
        }
        if (subs[0] == null || subs[1] == null || pubs[0] == null) {
            throw new IllegalArgumentException("Elements in subs and pubs arrays cannot be null");
        }
    }

    @Override
    public String getName() {
        return "PlusAgent";
    }

    @Override
    public void reset() {
        x = 0;
        y = 0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) {
            return;
        }

        if (topic.equals(topicX)) {
            x = msg.asDouble;
        } else if (topic.equals(topicY)) {
            y = msg.asDouble;
        } else {
            return;
        }

        if (!Double.isNaN(x) && !Double.isNaN(y)) {
            if (topicPub != null) {
                TopicManagerSingleton.get().getTopic(topicPub).publish(new Message(x + y));
            }
        }
    }

    @Override
    public void close() {
    }
}
