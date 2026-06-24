package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import graph.TopicManagerSingleton;
import graph.Topic;
import graph.Message;
import server.RequestParser.RequestInfo;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        if (ri == null) {
            sendError(toClient, 400, "Bad Request: Null RequestInfo");
            return;
        }

        publishMessage(ri.getParams());

        String body = generateHtml();
        sendSuccess(toClient, body);
    }

    private void publishMessage(Map<String, String> params) {
        if (params == null) {
            return;
        }
        String topicName = params.get("topic");
        String messageVal = params.get("message");

        if (topicName != null && !topicName.trim().isEmpty() && messageVal != null) {
            TopicManagerSingleton.get().getTopic(topicName).publish(new Message(messageVal));
        }
    }

    private String generateHtml() {
        // Generate HTML table of all topics and their last values
        StringBuilder sb = new StringBuilder();
        sb.append("<html>")
                .append("<head><title>Topics Status</title>")
                .append("<style>")
                .append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f172a; color: #f8fafc; padding: 20px; }")
                .append("table { border-collapse: collapse; width: 100%; max-width: 600px; margin: 20px 0; background-color: #1e293b; border-radius: 8px; overflow: hidden; }")
                .append("th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #334155; }")
                .append("th { background-color: #38bdf8; color: #0f172a; font-weight: bold; }")
                .append("tr:hover { background-color: #334155; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<h2>Topic Values Dashboard</h2>")
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
        System.out.println("close");
    }

}
