package configs;

import java.util.function.BinaryOperator;

import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;

public class BinOpAgent implements Agent {
    private final String name;
    private final BinaryOperator<Double> op;
    private double input1Value = 0.0;
    private double input2Value = 0.0;
    private boolean hasInput1 = false;
    private boolean hasInput2 = false;
    private Topic topic1;
    private Topic topic2;
    private Topic topicOut;

    public BinOpAgent(String name, String inputTopic1, String inputTopic2, String outputTopic,
            BinaryOperator<Double> op) {
        this.name = name != null ? name : "";
        this.op = op;

        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();

        this.topic1 = inputTopic1 != null ? tm.getTopic(inputTopic1) : null;
        this.topic2 = inputTopic2 != null ? tm.getTopic(inputTopic2) : null;
        this.topicOut = outputTopic != null ? tm.getTopic(outputTopic) : null;

        if (topic1 != null) {
            topic1.subscribe(this);
        }
        if (topic2 != null) {
            topic2.subscribe(this);
        }
        if (topicOut != null) {
            topicOut.addPublisher(this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        input1Value = 0.0;
        input2Value = 0.0;
        hasInput1 = false;
        hasInput2 = false;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic == null || msg == null || Double.isNaN(msg.asDouble)) {
            return;
        }

        if (topic1 != null && topic.equals(topic1.getName())) {
            input1Value = msg.asDouble;
            hasInput1 = true;
        } else if (topic2 != null && topic.equals(topic2.getName())) {
            input2Value = msg.asDouble;
            hasInput2 = true;
        }

        if (hasInput1 && hasInput2 && op != null && topicOut != null) {
            double result = op.apply(input1Value, input2Value);
            topicOut.publish(new Message(result));
        }
    }

    @Override
    public void close() {
    }
}
