package graph;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    public final String name;
    private final List<Agent> subs = new ArrayList<>();
    private final List<Agent> pubs = new ArrayList<>();

    private volatile Message lastMessage = null;

    Topic(String name) {
        this.name = name;
    }

    public Message getLastMessage() {
        return this.lastMessage;
    }

    public void subscribe(Agent a) {
        if (a != null && !subs.contains(a)) {
            subs.add(a);
        }
    }

    public String getName() {
        return name;
    }

    public void unsubscribe(Agent a) {
        subs.remove(a);
    }

    public void publish(Message m) {
        if (m != null) {
            this.lastMessage = m;
            for (Agent a : subs) {
                a.callback(this.name, m);
            }
        }
    }

    public void addPublisher(Agent a) {
        if (a != null && !pubs.contains(a)) {
            pubs.add(a);
        }
    }

    public void removePublisher(Agent a) {
        pubs.remove(a);
    }

    public List<Agent> getSubs() {
        return subs;
    }

    public List<Agent> getPubs() {
        return pubs;
    }
}
