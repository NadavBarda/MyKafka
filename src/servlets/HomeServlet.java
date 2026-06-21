package servlets;

import java.io.IOException;
import java.io.OutputStream;

import server.RequestParser.RequestInfo;

public class HomeServlet implements Servlet {
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String body = "<html>" +
                "<head><title>My HTTPServer</title>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f172a; color: #f8fafc; padding: 40px; text-align: center; }"
                +
                "h1 { color: #38bdf8; font-size: 2.5em; margin-bottom: 20px; }" +
                "p { font-size: 1.2em; color: #94a3b8; }" +
                "a { color: #38bdf8; text-decoration: none; font-weight: bold; border-bottom: 2px solid #38bdf8; transition: all 0.3s; }"
                +
                "a:hover { color: #7dd3fc; border-color: #7dd3fc; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>🚀 My Java HTTP Server is Running!</h1>" +
                "<p>Welcome! The server is up and listening on port 8080.</p>" +
                "</body></html>";

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + body.getBytes().length + "\r\n" +
                "Connection: close\r\n\r\n" +
                body;
        toClient.write(response.getBytes());
        toClient.flush();
    }

    @Override
    public void close() {
        // No resources to close
    }
}
