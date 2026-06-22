package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    // Inner class to hold request data
    public static class RequestInfo {
        private final String httpCommand; 
        private final String uri; 
        private final String path; 
        private final String[] uriParts; 
        private final Map<String, String> params; 
        private final Map<String, String> headers; 
        private final byte[] content; 

        public RequestInfo(String httpCommand, String uri, String[] uriParts, Map<String, String> params,
                Map<String, String> headers, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            
            String tempPath = uri;
            if (uri != null && uri.contains("?")) {
                tempPath = uri.substring(0, uri.indexOf("?"));
            }
            this.path = tempPath;
            
            this.uriParts = uriParts;
            this.params = params;
            this.headers = headers;
            this.content = content;
        }

        // Getters for external use
        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String getPath() {
            return path;
        }

        public String[] getUriParts() {
            return uriParts;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public byte[] getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "Command: " + httpCommand + "\n" +
                    "URI: " + uri + "\n" +
                    "Parts: " + java.util.Arrays.toString(uriParts) + "\n" +
                    "Params: " + params + "\n" +
                    "Headers: " + headers + "\n" +
                    "Content Length: " + (content != null ? content.length : 0) + " bytes\n" +
                    "Content: \n" + (content != null ? new String(content) : "null");
        }

        public String[] getUriSegments() {
            return this.uriParts;
        }

        public Map<String, String> getParameters() {
            return this.params;
        }
    }

    
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] requestLineParts = splitRequestLine(requestLine);
        String httpCommand = requestLineParts[0];
        String uri = requestLineParts[1];

        Map<String, String> params = parseQueryParams(uri);
        String[] uriParts = parseUriParts(uri);

        Map<String, String> headers = new HashMap<>();
        int contentLength = parseHeaders(reader, headers);

        // Parse intermediate body parameters
        parseBodyParams(reader, params);

        byte[] content = readContent(reader, contentLength);

        return new RequestInfo(httpCommand, uri, uriParts, params, headers, content);
    }

    private static void parseBodyParams(BufferedReader reader, Map<String, String> params) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] keyValue = line.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0].trim(), keyValue[1].trim());
            } else if (keyValue.length == 1) {
                params.put(keyValue[0].trim(), "");
            }
        }
    }

    private static String[] splitRequestLine(String requestLine) throws IOException {
        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            throw new IOException("Invalid HTTP request line");
        }
        return parts;
    }

    private static Map<String, String> parseQueryParams(String uri) {
        Map<String, String> params = new HashMap<>();
        if (!uri.contains("?")) {
            return params;
        }

        int questionMarkIndex = uri.indexOf('?');
        String queryString = uri.substring(questionMarkIndex + 1);

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                params.put(keyValue[0], "");
            }
        }

        return params;
    }

    private static String[] parseUriParts(String uri) {
        String path = uri;
        if (uri.contains("?")) {
            int questionMarkIndex = uri.indexOf('?');
            path = uri.substring(0, questionMarkIndex);
        }

        String[] rawParts = path.split("/");
        List<String> cleanedParts = new ArrayList<>();
        for (String part : rawParts) {
            if (!part.isEmpty()) {
                cleanedParts.add(part);
            }
        }
        return cleanedParts.toArray(new String[0]);
    }

    private static int parseHeaders(BufferedReader reader, Map<String, String> headers) throws IOException {
        int contentLength = 0;
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(":", 2);
            if (headerParts.length == 2) {
                String key = headerParts[0].trim();
                String value = headerParts[1].trim();
                headers.put(key, value);
                if ("content-length".equalsIgnoreCase(key)) {
                    contentLength = Integer.parseInt(value);
                }
            }
        }
        return contentLength;
    }

    private static byte[] readContent(BufferedReader reader, int contentLength) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        String line;
        
        // Read until an empty line or reader.ready() returns false
        while (reader.ready() && (line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }
            buffer.write(line.getBytes());
            buffer.write('\n');
        }
        
        byte[] contentBytes = buffer.toByteArray();
        return contentBytes.length > 0 ? contentBytes : null;
    }
}