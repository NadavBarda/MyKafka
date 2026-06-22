package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class IncAgent implements Agent {
    private String topicSub;
    private String topicPub;

    public IncAgent(String[] subs, String[] pubs) {
        validateDependencies(subs, pubs);

        TopicManager tm = TopicManagerSingleton.get();

        this.topicSub = subs[0];
        this.topicPub = pubs[0];

        tm.getTopic(topicSub).subscribe(this);
        tm.getTopic(topicPub).addPublisher(this);

    }

    private void validateDependencies(String[] subs, String[] pubs) {
        if (subs == null || pubs == null) {
            throw new IllegalArgumentException("Subs and pubs cannot be null");
        }
        if (subs.length < 1 || pubs.length < 1) {
            throw new IllegalArgumentException("Subs and pubs arrays must have at least 1 and 1 elements respectively");
        }
        if (subs[0] == null || pubs[0] == null) {
            throw new IllegalArgumentException("Elements in subs and pubs arrays cannot be null");
        }
    }

    @Override
    public String getName() {
        return "IncAgent";
    }

    @Override
    public void reset() {
    }

    @Override
    public void callback(String topic, Message msg) {
        if (!Double.isNaN(msg.asDouble) && topic.equals(topicSub)) {
            if (topicPub != null) {
                TopicManagerSingleton.get().getTopic(topicPub).publish(new Message(msg.asDouble + 1));
            }
        }
    }

    @Override
    public void close() {
    }
}
