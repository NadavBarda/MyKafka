import server.MyHTTPServer;
import servlets.ConfLoader;
import server.HTTPServer;

public class Main {
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);
        server.addServlet("POST", "/upload", new ConfLoader());
        server.start();
        System.in.read();
        server.close();
        System.out.println("done");
    }
}