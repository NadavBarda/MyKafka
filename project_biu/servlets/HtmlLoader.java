package servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import server.RequestParser.RequestInfo;

/**
 * HtmlLoader is a servlet that serves static resources (like HTML, CSS, and JS files)
 * from a directory configured at runtime.
 */
public class HtmlLoader implements Servlet {

    private final File directory;

    /**
     * Constructs the HtmlLoader pointing to the specified directory.
     *
     * @param directoryPath the root directory path where static files are located.
     */
    public class HtmlLoaderConstructor {
        // Just for documentation
    }

    public HtmlLoader(String directoryPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Directory path cannot be null or empty.");
        }
        this.directory = new File(directoryPath);
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        if (ri == null) {
            sendError(toClient, 400, "Bad Request", "Null RequestInfo");
            return;
        }

        String[] segments = ri.getUriSegments();
        if (segments == null || segments.length == 0 || !segments[0].equals("app")) {
            sendError(toClient, 404, "Not Found", "Invalid request path.");
            return;
        }

        // Reconstruct the subpath from the URI segments after "/app"
        StringBuilder pathBuilder = new StringBuilder();
        if (segments.length == 1) {
            pathBuilder.append("index.html");
        } else {
            for (int i = 1; i < segments.length; i++) {
                if (i > 1) {
                    pathBuilder.append(File.separator);
                }
                // URL-decode the segment to handle spaces or special characters in filenames
                String decodedSegment = java.net.URLDecoder.decode(segments[i], StandardCharsets.UTF_8);
                pathBuilder.append(decodedSegment);
            }
        }

        File targetFile = new File(directory, pathBuilder.toString());

        // Security check: Prevent directory traversal attacks.
        // Ensure root canonical path ends with a separator to avoid partial name match vulnerabilities.
        String rootCanonical = directory.getCanonicalPath();
        if (!rootCanonical.endsWith(File.separator)) {
            rootCanonical += File.separator;
        }
        String fileCanonical = targetFile.getCanonicalPath();
        if (!fileCanonical.startsWith(rootCanonical)) {
            sendError(toClient, 403, "Forbidden", "Access denied.");
            return;
        }

        if (!targetFile.exists() || !targetFile.isFile()) {
            sendError(toClient, 404, "Not Found", "The requested file <strong>" 
                    + HtmlUtil.escapeHtml(pathBuilder.toString()) + "</strong> was not found.");
            return;
        }

        // Success response
        byte[] data = Files.readAllBytes(targetFile.toPath());
        String mimeType = getMimeType(targetFile.getName());

        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "Connection: close\r\n\r\n";

        toClient.write(header.getBytes(StandardCharsets.UTF_8));
        toClient.write(data);
        toClient.flush();
    }

    private String getMimeType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        } else if (lower.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        } else if (lower.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".ico")) {
            return "image/x-icon";
        } else {
            return "application/octet-stream";
        }
    }

    private void sendError(OutputStream toClient, int statusCode, String statusText, String message) throws IOException {
        String body = "<html>" +
                "<head><title>" + statusCode + " " + statusText + "</title>" +
                "<style>" +
                "body { font-family: 'Segoe UI', sans-serif; background-color: #0f172a; color: #f8fafc; text-align: center; padding: 50px; }" +
                "h1 { color: #f87171; font-size: 3em; margin-bottom: 20px; }" +
                "p { color: #94a3b8; font-size: 1.2em; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>" + statusCode + " " + statusText + "</h1>" +
                "<p>" + message + "</p>" +
                "</body>" +
                "</html>";

        String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
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
