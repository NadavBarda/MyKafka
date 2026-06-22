import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.TopicDisplayer;
import server.HTTPServer;

public class Main {
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/display", new TopicDisplayer());
        server.start();
        System.in.read();
        server.close();
        System.out.println("done");
    }
}