import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.TopicDisplayer;
import servlets.HtmlLoader;
import servlets.GraphDataServlet;
import server.HTTPServer;
import configs.SystemResetService;

public class Main {

    private static void initailServelt(HTTPServer server) {
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("GET", "/api/graph", new GraphDataServlet());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
    }

    private static void closeServer(HTTPServer server) {

        server.close();
        SystemResetService.cleanAll();
    }

    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);

        initailServelt(server);

        server.start();
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeServer(server);
    }
}