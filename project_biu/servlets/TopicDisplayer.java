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

/**
 * TopicDisplayer displays a dashboard table of all topics and their last message values.
 * It handles GET requests to inspect topic states and allows publishing new messages.
 */
public class TopicDisplayer implements Servlet {

    /**
     * Enumerates the possible outcomes when attempting to publish a message.
     */
    public enum PublishStatus {
        SUCCESS,          // Message published successfully
        TOPIC_NOT_FOUND,  // Target topic name doesn't exist in the active graph
        MISSING_PARAMS,   // Request query parameters are missing or incomplete
        NO_PARAMS         // Default initial page request with no query parameters
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        if (ri == null) {
            sendError(toClient, 400, "Bad Request: Null RequestInfo");
            return;
        }

        // 1. Process the message publishing step
        PublishStatus status = publishMessage(ri.getParams());

        // 2. Dispatch layout flow based on status.
        // We use a switch expression instead of an EnumMap because it's simpler and more readable
        // for lightweight request routing where we only need to map status to an optional alert message.
        String alertMessage = switch (status) {
            case TOPIC_NOT_FOUND -> "The requested topic does not exist";
            default -> null; // SUCCESS, NO_PARAMS, and MISSING_PARAMS render the default dashboard
        };

        // 3. Serve the HTML response containing the topics dashboard
        sendSuccess(toClient, generateHtml(alertMessage));
    }

    /**
     * Attempts to extract 'topic' and 'message' parameters and publish a new message.
     * Checks if the topic exists prior to publishing to avoid creating orphaned topics.
     */
    private PublishStatus publishMessage(Map<String, String> params) {
        if (params == null) {
            return PublishStatus.NO_PARAMS;
        }

        String topicName = params.get("topic");
        String messageVal = params.get("message");

        if (topicName != null && !topicName.trim().isEmpty() && messageVal != null) {
            TopicManager topicManager = TopicManagerSingleton.get();

            // We perform an O(1) containsKey check using topicExists() to prevent users from
            // dynamically instantiating non-existent topics that are not in the configuration graph.
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
        StringBuilder sb = new StringBuilder();
        sb.append("<html>")
                .append("<head><title>Topics Status</title>")
                .append("<style>")
                .append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f172a; color: #f8fafc; padding: 20px; }")
                .append("table { border-collapse: collapse; width: 100%; max-width: 600px; margin: 20px 0; background-color: #1e293b; border-radius: 8px; overflow: hidden; }")
                .append("th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #334155; word-break: break-word; }")
                .append("th { background-color: #38bdf8; color: #0f172a; font-weight: bold; }")
                .append("tr:hover { background-color: #334155; }")
                .append(".alert { padding: 12px; background-color: #f87171; color: #0f172a; border-radius: 6px; margin-bottom: 15px; max-width: 600px; font-weight: 600; }")
                .append("</style>")
                .append("</head>")
                .append("<body>");

        if (alertMessage != null) {
            sb.append("<div class=\"alert\">⚠️ ").append(HtmlUtil.escapeHtml(alertMessage)).append("</div>");
        }

        sb.append("<h2>Topic Values Dashboard</h2>")
                .append("<table>")
                .append("<thead><tr><th>Topic Name</th><th>Last Message Value</th></tr></thead>")
                .append("<tbody>");

        Collection<Topic> topics = TopicManagerSingleton.get().getTopics();
        for (Topic t : topics) {
            Message lastMsg = t.getLastMessage();
            String val = (lastMsg != null) ? HtmlUtil.escapeHtml(lastMsg.asText) : "<em>No message yet</em>";
            sb.append("<tr>")
                    .append("<td>").append(HtmlUtil.escapeHtml(t.getName())).append("</td>")
                    .append("<td>").append(val).append("</td>")
                    .append("</tr>");
        }

        sb.append("</tbody>")
                .append("</table>")
                .append("</body>")
                .append("</html>");

        return sb.toString();
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