package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import server.RequestParser.RequestInfo;

public class Default404Servlet implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String body = "<html>" +
                "<head><title>404 Not Found</title>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f172a; color: #f8fafc; text-align: center; padding: 50px; }" +
                "h1 { color: #f87171; font-size: 3em; margin-bottom: 10px; }" +
                "p { color: #94a3b8; font-size: 1.2em; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>404 Not Found</h1>" +
                "<p>No matching servlet was found for this request.</p>" +
                "</body>" +
                "</html>";

        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                body;

        toClient.write(response.getBytes(StandardCharsets.UTF_8));
        toClient.flush();
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}
