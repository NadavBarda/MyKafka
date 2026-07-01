package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import server.RequestParser.RequestInfo;

public class Default404Servlet implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String body = "";
        java.io.File htmlFile = new java.io.File("html_files/404.html");
        if (htmlFile.exists() && htmlFile.isFile()) {
            body = new String(java.nio.file.Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);
        }

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
