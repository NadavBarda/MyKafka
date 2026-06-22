package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartRequestParser extends RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            throw new IOException("Invalid HTTP request line");
        }
        String httpCommand = parts[0];
        String uri = parts[1];

        Map<String, String> params = new HashMap<>();
        String[] uriParts = parseUriPartsAndQueryParams(uri, params);

        Map<String, String> headers = new HashMap<>();
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

        String contentType = headers.get("Content-Type");
        if (contentType == null) contentType = headers.get("content-type");

        byte[] content = null;
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            content = readExactBytes(reader, contentLength);
            content = extractMultipartParams(content, contentType, params);
        } else if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
            content = readExactBytes(reader, contentLength);
            if (content != null) {
                parseUrlEncodedParams(new String(content, StandardCharsets.UTF_8), params);
            }
        } else {
            content = readExactBytes(reader, contentLength);
        }

        return new RequestInfo(httpCommand, uri, uriParts, params, headers, content);
    }

    private static String[] parseUriPartsAndQueryParams(String uri, Map<String, String> params) {
        String path = uri;
        if (uri.contains("?")) {
            int qMark = uri.indexOf('?');
            path = uri.substring(0, qMark);
            String query = uri.substring(qMark + 1);
            parseUrlEncodedParams(query, params);
        }

        String[] rawParts = path.split("/");
        List<String> cleaned = new ArrayList<>();
        for (String part : rawParts) {
            if (!part.isEmpty()) {
                cleaned.add(part);
            }
        }
        return cleaned.toArray(new String[0]);
    }

    private static void parseUrlEncodedParams(String query, Map<String, String> params) {
        if (query == null || query.isEmpty()) return;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            } else if (kv.length == 1) {
                params.put(kv[0], "");
            }
        }
    }

    private static byte[] readExactBytes(BufferedReader reader, int length) throws IOException {
        if (length <= 0) return null;
        
        char[] cbuf = new char[length];
        int read = 0;
        while (read < length) {
            int res = reader.read(cbuf, read, length - read);
            if (res == -1) break;
            read += res;
        }
        if (read == 0) return null;
        
        byte[] bytes = new byte[read];
        for (int i = 0; i < read; i++) {
            bytes[i] = (byte) cbuf[i];
        }
        return bytes;
    }

    private static byte[] extractMultipartParams(byte[] content, String contentType, Map<String, String> params) {
        if (content == null) return null;
        
        String body = new String(content, StandardCharsets.ISO_8859_1);
        String boundary = null;
        int bIdx = contentType.indexOf("boundary=");
        if (bIdx != -1) {
            boundary = contentType.substring(bIdx + 9).trim();
        }
        if (boundary == null) return content;

        byte[] fileContent = null;
        String dashBoundary = "--" + boundary;
        String[] parts = body.split(dashBoundary);
        
        for (String part : parts) {
            if (part == null || part.trim().isEmpty() || part.trim().equals("--") || part.trim().equals("--\r\n")) continue;

            int headerEnd = part.indexOf("\r\n\r\n");
            int headerLen = 4;
            if (headerEnd == -1) {
                headerEnd = part.indexOf("\n\n");
                headerLen = 2;
            }
            
            if (headerEnd != -1) {
                String headersPart = part.substring(0, headerEnd);
                
                String name = null;
                String filename = null;
                
                String[] headerLines = headersPart.split("\n");
                for (String hl : headerLines) {
                    hl = hl.trim();
                    if (hl.toLowerCase().startsWith("content-disposition:")) {
                        int nIdx = hl.indexOf("name=\"");
                        if (nIdx != -1) {
                            int nEnd = hl.indexOf("\"", nIdx + 6);
                            if (nEnd != -1) name = hl.substring(nIdx + 6, nEnd);
                        }
                        int fIdx = hl.indexOf("filename=\"");
                        if (fIdx != -1) {
                            int fEnd = hl.indexOf("\"", fIdx + 10);
                            if (fEnd != -1) filename = hl.substring(fIdx + 10, fEnd);
                        }
                    }
                }
                
                if (name != null) {
                    int bodyStart = headerEnd + headerLen;
                    int bodyEnd = part.length();
                    if (part.endsWith("\r\n")) {
                        bodyEnd -= 2;
                    } else if (part.endsWith("\n")) {
                        bodyEnd -= 1;
                    }

                    if (bodyStart <= bodyEnd) {
                        if (filename != null) {
                            params.put("file", filename);
                            String bodyPart = part.substring(bodyStart, bodyEnd);
                            fileContent = bodyPart.getBytes(StandardCharsets.ISO_8859_1);
                        } else {
                            String bodyPart = part.substring(bodyStart, bodyEnd);
                            params.put(name, bodyPart.trim());
                        }
                    }
                }
            }
        }
        
        return fileContent != null ? fileContent : content;
    }
}
