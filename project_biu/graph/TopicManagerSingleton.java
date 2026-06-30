package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {

    public static class TopicManager {
        private static final TopicManager instance = new TopicManager();
        private final ConcurrentHashMap<String, Topic> topics = new ConcurrentHashMap<>();

        private TopicManager() {
        }

        public Topic getTopic(String name) {
            String trimmedName = null;
            if (name == null || (trimmedName = name.trim()).isEmpty()) {
                return null;
            }
            return topics.computeIfAbsent(trimmedName, k -> new Topic(k));
        }

        public Collection<Topic> getTopics() {
            return topics.values();
        }

        public boolean topicExists(String topicName) {
            String trimmedName = null;
            if (topicName == null || (trimmedName = topicName.trim()).isEmpty()) {
                return false;
            }
            return topics.containsKey(trimmedName);
        }

        public void clear() {
            topics.clear();
        }
    }

    public static TopicManager get() {
        return TopicManager.instance;
    }
}
