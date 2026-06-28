import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.TopicDisplayer;
import servlets.HtmlLoader;
import servlets.GraphDataServlet;
import server.HTTPServer;
import configs.SystemResetService;

public class Main {
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);
        
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("GET", "/api/graph", new GraphDataServlet());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        server.start();
        System.in.read();
        server.close();
        SystemResetService.cleanAll();
    }
}