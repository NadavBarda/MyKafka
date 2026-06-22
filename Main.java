import config.MathExampleConfig;
import configs.LogAgent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

class Main {
    public static void main(String[] args) {

        MathExampleConfig config = new MathExampleConfig();
        TopicManager tm = TopicManagerSingleton.get();
        LogAgent logger = new LogAgent();

        config.create();

        Topic topic_A =  tm.getTopic("A");
        Topic topic_B =  tm.getTopic("B");
    
        Topic topic_R3 = tm.getTopic("R3");
        topic_R3.subscribe(logger);  

        topic_A.publish(new Message(10));
        topic_B.publish(new Message(5));

              
    }
}
