package servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import server.RequestParser.RequestInfo;
import config.GenericConfig;
import config.ConfigSingleton;

public class ConfLoader implements Servlet {
   
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        if (ri == null) {
            sendError(toClient, 400, "Bad Request: Null RequestInfo");
            return;
        }

        try {
            String fileName = saveFileData(ri);
            if (fileName != null && !fileName.isEmpty()) {
                GenericConfig conf = new GenericConfig();
                conf.setConfFile("assets/config_files/" + fileName);
                conf.create();

                ConfigSingleton.get().set(conf);
                sendSuccess(toClient, fileName);
            } else {
                sendError(toClient, 400, "Bad Request: Missing or invalid file name/content");
            }
        } catch (Exception e) {
            sendError(toClient, 500, "Internal Server Error: " + e.getMessage());
        }
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

    private void sendSuccess(OutputStream toClient, String fileName) throws IOException {
        String body = "<html>" +
                "<head><title>Success</title>" +
                "<style>" +
                "body { font-family: 'Segoe UI', sans-serif; background-color: #0f172a; color: #f8fafc; text-align: center; padding: 50px; }"
                +
                "h1 { color: #38bdf8; }" +
                "p { color: #94a3b8; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>✅ File Uploaded Successfully</h1>" +
                "<p>Saved config as: <strong>" + fileName + "</strong></p>" +
                "</body>" +
                "</html>";

        String response = "HTTP/1.1 200 OK\r\n" +
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
        System.out.println("close");
    }

}
