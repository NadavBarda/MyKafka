package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class InventoryAgent implements Agent {
    private final String subTopic;
    private final String pubInventoryTopic;
    private final String pubConfirmationTopic;
    private volatile int inventory = 50;

    public InventoryAgent(String[] subs, String[] pubs) {
        if (subs == null || pubs == null || subs.length < 1 || pubs.length < 2) {
            throw new IllegalArgumentException("InventoryAgent requires at least 1 subscriber topic and 2 publisher topics");
        }
        this.subTopic = subs[0];
        this.pubInventoryTopic = pubs[0];
        this.pubConfirmationTopic = pubs[1];
        
        TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(subTopic).subscribe(this);
        tm.getTopic(pubInventoryTopic).addPublisher(this);
        tm.getTopic(pubConfirmationTopic).addPublisher(this);
        
        // Publish initial inventory
        publishInventory();
    }

    private void publishInventory() {
        TopicManagerSingleton.get().getTopic(pubInventoryTopic).publish(new Message(inventory));
    }

    @Override
    public String getName() {
        return "InventoryAgent";
    }

    @Override
    public void reset() {
        inventory = 50;
        publishInventory();
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(subTopic)) {
            double ticketsDouble = msg.asDouble;
            if (Double.isNaN(ticketsDouble)) {
                return;
            }
            int ticketsNeeded = (int) ticketsDouble;
            if (inventory >= ticketsNeeded) {
                inventory -= ticketsNeeded;
                publishInventory();
                TopicManagerSingleton.get().getTopic(pubConfirmationTopic).publish(
                    new Message("SUCCESS: " + ticketsNeeded + " tickets booked. Remaining: " + inventory)
                );
                System.out.println("InventoryAgent: Booked " + ticketsNeeded + " tickets. Remaining: " + inventory);
            } else {
                TopicManagerSingleton.get().getTopic(pubConfirmationTopic).publish(
                    new Message("FAILURE: Not enough tickets. Requested: " + ticketsNeeded + ", Remaining: " + inventory)
                );
                System.err.println("InventoryAgent: Insufficient inventory to book " + ticketsNeeded + " tickets. Current stock: " + inventory);
            }
        }
    }

    @Override
    public void close() {
    }
}
