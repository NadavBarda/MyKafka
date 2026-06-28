package servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import server.RequestParser.RequestInfo;
import configs.GenericConfig;
import configs.ConfigSingleton;
import configs.Graph;
import configs.GraphSingleton;
import configs.SystemResetService;
import graph.TopicManagerSingleton;
import views.HtmlGraphWriter;
import java.util.List;

public class ConfLoader implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        if (ri == null) {
            sendError(toClient, 400, "Bad Request: Null RequestInfo");
            return;
        }

        try {
            String fileName = saveFileData(ri);
            if (fileName == null || fileName.isEmpty()) {
                sendError(toClient, 400, "Bad Request: Missing or invalid file name/content");
                return;
            }

            deployConfig(fileName);

            // Generate computational graph from currently active topics
            Graph graph = new Graph();
            graph.createFromTopics();

            if (graph.hasCycles()) {
                // Rollback: remove all generated data because of the invalid file
                cleanData();
                // Delete the uploaded invalid configuration file
                File uploadedFile = new File("assets/config_files/" + fileName);
                if (uploadedFile.exists()) {
                    uploadedFile.delete();
                }

                sendError(toClient, 400,
                        "The computational graph could not be created because the configuration contains cycles.");
                return;
            }

            // Set the new graph in the singleton
            GraphSingleton.get().set(graph);

            List<String> htmlLines = HtmlGraphWriter.getGraphHTML(graph);
            String fullHtml = String.join("\n", htmlLines);

            sendResponse(toClient, 200, "OK", fullHtml);
        } catch (Exception e) {
            sendError(toClient, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void cleanData() {
        SystemResetService.cleanAll();
    }

    private void deployConfig(String fileName) {
        // 1. Close and clean up the current configuration (stops old running agents)
        cleanData();

        // 2. Create the new config
        GenericConfig conf = new GenericConfig();
        conf.setConfFile("assets/config_files/" + fileName);
        conf.create();

        // 5. Set the new config in the singleton
        ConfigSingleton.get().set(conf);
    }

    private String saveFileData(RequestInfo ri) {
        if (ri == null || ri.getParams() == null) {
            return null;
        }

        String fileName = ri.getParams().get("file");

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "uploaded_config.conf"; // default if missing
        }

        // Sanitize filename to prevent directory traversal
        fileName = new File(fileName).getName();
        if (fileName.isEmpty() || fileName.equals(".") || fileName.equals("..")) {
            return null;
        }

        byte[] data = ri.getContent();
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            File dir = new File("assets/config_files");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.err.println("Failed to create directory assets/config_files");
                    return null;
                }
            }

            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
            return fileName;
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            return null;
        }
    }

    private void sendResponse(OutputStream toClient, int statusCode, String statusText, String body)
            throws IOException {
        String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                body;

        toClient.write(response.getBytes(StandardCharsets.UTF_8));
        toClient.flush();
    }

    private void sendError(OutputStream toClient, int statusCode, String message) throws IOException {
        String statusText = (statusCode == 400) ? "Bad Request" : "Internal Server Error";
        String body = "<html>" +
                "<head><title>" + statusText + "</title>" +
                "<style>" +
                "body { font-family: 'Segoe UI', sans-serif; background-color: #0f172a; color: #f8fafc; text-align: center; padding: 50px; }"
                +
                "h1 { color: #f87171; }" +
                "p { color: #94a3b8; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>❌ " + statusText + "</h1>" +
                "<p>" + HtmlUtil.escapeHtml(message) + "</p>" +
                "</body>" +
                "</html>";

        sendResponse(toClient, statusCode, statusText, body);
    }

    
    @Override
    public void close() throws IOException {
        System.out.println("Closing ConfLoader...");
        cleanData();
    }
}
