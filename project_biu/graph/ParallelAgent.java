package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent {

    private final Agent agent;
    private final BlockingQueue<Message> queue;
    private final Thread thread;
    private volatile boolean stop;

    private static class TopicMessage extends Message {
        public final String topic;
        public final Message message;

        public TopicMessage(String topic, Message message) {
            super(message != null ? message.data : new byte[0]);
            this.topic = topic;
            this.message = message;
        }
    }

    public ParallelAgent(Agent agent, int capacity) {
        if (agent == null) {
            throw new IllegalArgumentException("Agent cannot be null");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.stop = false;

        this.thread = new Thread(this::run, "ParallelAgent-" + agent.getName());
        this.thread.start();
    }

    private void run() {
        while (!stop && !Thread.currentThread().isInterrupted()) {
            try {
                Message m = queue.take();
                if (m instanceof TopicMessage) {
                    TopicMessage tm = (TopicMessage) m;
                    this.agent.callback(tm.topic, tm.message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Exception in agent callback: " + e.getMessage());
            }
        }
    }

    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void reset() {
        agent.reset();
    }

    @Override
    public void callback(String topic, Message msg) {
        if (stop) {
            return;
        }
        try {
            queue.put(new TopicMessage(topic, msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        if (stop) {
            return;
        }
        stop = true;
        thread.interrupt();
        queue.clear();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        agent.close();
    }
}
