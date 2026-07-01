import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.TopicDisplayer;
import servlets.HtmlLoader;
import servlets.GraphDataServlet;
import server.HTTPServer;
import server.RateLimitedServer;
import server.ratelimiter.RateLimitConfig;
import server.ratelimiter.RateLimiter;
import server.ratelimiter.TokenBucketStrategy;
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
        RateLimitedServer server = new MyHTTPServer(8080, 5);

        RateLimitConfig rateLimitConfig = new RateLimitConfig(5, 1.0);
        rateLimitConfig.addRule("/api/graph", 2, 1.2);
        rateLimitConfig.addRule("/upload", 2, 0.4);
        RateLimiter rateLimiter = new RateLimiter(new TokenBucketStrategy(rateLimitConfig));
        server.setRateLimiter(rateLimiter);

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