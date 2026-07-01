package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import graph.Topic;
import graph.Message;
import server.RequestParser.RequestInfo;

// Servlet to display all topics and their last message.
public class TopicDisplayer implements Servlet {

    // Outcomes of publishing a message.
    public enum PublishStatus {
        SUCCESS,
        TOPIC_NOT_FOUND,
        MISSING_PARAMS,
        NO_PARAMS
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        if (ri == null) {
            sendError(toClient, 400, "Bad Request: Null RequestInfo");
            return;
        }

        PublishStatus status = publishMessage(ri.getParams());

        // Map status to alert message if needed
        String alertMessage = switch (status) {
            case TOPIC_NOT_FOUND -> "The requested topic does not exist";
            default -> null; // SUCCESS, NO_PARAMS, and MISSING_PARAMS render the default dashboard
        };

        // Return HTML dashboard
        sendSuccess(toClient, generateHtml(alertMessage));
    }

    // Publishes message to topic if it exists.
    private PublishStatus publishMessage(Map<String, String> params) {
        if (params == null) {
            return PublishStatus.NO_PARAMS;
        }

        String topicName = params.get("topic");
        String messageVal = params.get("message");

        if (topicName != null && !topicName.trim().isEmpty() && messageVal != null) {
            TopicManager topicManager = TopicManagerSingleton.get();

            // Ensure topic exists to avoid creating non-configured topics.
            if (topicManager.topicExists(topicName)) {
                topicManager.getTopic(topicName).publish(new Message(messageVal));
                return PublishStatus.SUCCESS;
            } else {
                return PublishStatus.TOPIC_NOT_FOUND;
            }
        }
        return PublishStatus.MISSING_PARAMS;
    }

    private String generateHtml(String alertMessage) {
        String template = "";
        java.io.File htmlFile = new java.io.File("html_files/topics.html");
        if (htmlFile.exists() && htmlFile.isFile()) {
            try {
                template = new String(java.nio.file.Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                // Fallback to inline template if reading fails
            }
        }

        String alertHtml = "";
        if (alertMessage != null) {
            alertHtml = "<div class=\"alert\">⚠️ " + HtmlUtil.escapeHtml(alertMessage) + "</div>";
        }

        StringBuilder rowsBuilder = new StringBuilder();
        Collection<Topic> topics = TopicManagerSingleton.get().getTopics();
        for (Topic t : topics) {
            Message lastMsg = t.getLastMessage();
            String val = (lastMsg != null) ? HtmlUtil.escapeHtml(lastMsg.asText) : "<em>No message yet</em>";
            rowsBuilder.append("<tr>")
                    .append("<td>").append(HtmlUtil.escapeHtml(t.getName())).append("</td>")
                    .append("<td>").append(val).append("</td>")
                    .append("</tr>\n");
        }

        return template.replace("{{ALERT}}", alertHtml)
                .replace("{{ROWS}}", rowsBuilder.toString());
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

    private void sendSuccess(OutputStream toClient, String body) throws IOException {
        sendResponse(toClient, 200, "OK", body);
    }

    private void sendError(OutputStream toClient, int statusCode, String message) throws IOException {
        String statusText = (statusCode == 400) ? "Bad Request" : "Internal Server Error";
        String body = "<html><body><h1>" + statusCode + " " + statusText + "</h1><p>" + HtmlUtil.escapeHtml(message)
                + "</p></body></html>";
        sendResponse(toClient, statusCode, statusText, body);
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}