package servlets;

public class FallbackHtml {

    public static String get429Html() {
        return "<html>" +
                "<head><title>429 Too Many Requests</title>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f172a; color: #f8fafc; text-align: center; padding: 50px; }" +
                "h1 { color: #f87171; font-size: 3em; margin-bottom: 10px; }" +
                "p { color: #94a3b8; font-size: 1.2em; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>429 Too Many Requests</h1>" +
                "<p>You are making too many requests. Please slow down.</p>" +
                "</body>" +
                "</html>";
    }

    public static String get404Html() {
        return "<html>" +
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
    }

    public static String getTopicsHtml() {
        return "<html>" +
                "<head><title>Topics Status</title>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f172a; color: #f8fafc; padding: 20px; }" +
                "table { border-collapse: collapse; width: 100%; max-width: 600px; margin: 20px 0; background-color: #1e293b; border-radius: 8px; overflow: hidden; }" +
                "th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #334155; word-break: break-word; }" +
                "th { background-color: #38bdf8; color: #0f172a; font-weight: bold; }" +
                "tr:hover { background-color: #334155; }" +
                ".alert { padding: 12px; background-color: #f87171; color: #0f172a; border-radius: 6px; margin-bottom: 15px; max-width: 600px; font-weight: 600; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "{{ALERT}}" +
                "<h2>Topic Values Dashboard</h2>" +
                "<table>" +
                "<thead><tr><th>Topic Name</th><th>Last Message Value</th></tr></thead>" +
                "<tbody>" +
                "{{ROWS}}" +
                "</tbody>" +
                "</table>" +
                "</body>" +
                "</html>";
    }
}
