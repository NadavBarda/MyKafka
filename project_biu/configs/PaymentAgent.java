package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class PaymentAgent implements Agent {
    private final String subTopic;
    private final String pubTopic;

    public PaymentAgent(String[] subs, String[] pubs) {
        if (subs == null || pubs == null || subs.length < 1 || pubs.length < 1) {
            throw new IllegalArgumentException("Invalid subs or pubs");
        }
        this.subTopic = subs[0];
        this.pubTopic = pubs[0];
        
        TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(subTopic).subscribe(this);
        tm.getTopic(pubTopic).addPublisher(this);
    }

    @Override
    public String getName() {
        return "PaymentAgent";
    }

    @Override
    public void reset() {
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(subTopic)) {
            double tickets = msg.asDouble;
            if (!Double.isNaN(tickets) && tickets > 0) {
                double amount = tickets * 50.0;
                System.out.println("PaymentAgent: Processed payment of $" + amount + " for " + (int)tickets + " tickets.");
                // We pass the ticket quantity forward to payment_status if payment is "processed/successful"
                TopicManagerSingleton.get().getTopic(pubTopic).publish(new Message(tickets));
            }
        }
    }

    @Override
    public void close() {
    }
}
