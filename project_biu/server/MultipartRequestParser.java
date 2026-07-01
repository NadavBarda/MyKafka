package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// We could also use here adapter pattern

// Parses HTTP requests, handling query parameters, headers, and multipart/form-data.
public class MultipartRequestParser extends RequestParser {

    private static final String BOUNDARY = "boundary=";
    private static final String CONTENT_DISPOSITION = "content-disposition:";
    private static final String NAME = "name=\"";
    private static final String FILENAME = "filename=\"";
    private static final String FILE_PARAM = "file";

    // Parses the incoming HTTP request.
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
        int contentLength = parseHeaders(reader, headers);

        byte[] content = parseBody(reader, contentLength, headers, params);

        return new RequestInfo(httpCommand, uri, uriParts, params, headers, content);
    }

    // Reads HTTP headers and extracts Content-Length if present.
    private static int parseHeaders(BufferedReader reader, Map<String, String> headers) throws IOException {
        int contentLength = 0;
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(":", 2);
            if (headerParts.length != 2)
                continue;

            String key = headerParts[0].trim();
            String value = headerParts[1].trim();
            headers.put(key, value);
            if ("content-length".equalsIgnoreCase(key)) {
                contentLength = Integer.parseInt(value);
            }

        }
        return contentLength;
    }

    // Reads and parses request body, handling multipart/form-data content.
    private static byte[] parseBody(BufferedReader reader, int contentLength, Map<String, String> headers,
            Map<String, String> params) throws IOException {

        byte[] content = readExactBytes(reader, contentLength);
        String contentType = headers.getOrDefault("Content-Type", headers.get("content-type"));

        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            return extractMultipartParams(content, contentType, params);
        }

        return content;
    }

    // Parses URI path segments and query parameters.
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

    // Parses a url-encoded query string into params map.
    private static void parseUrlEncodedParams(String query, Map<String, String> params) {
        if (query == null || query.isEmpty())
            return;
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

    // Reads exact number of bytes from the reader.
    private static byte[] readExactBytes(BufferedReader reader, int length) throws IOException {
        if (length <= 0)
            return null;

        char[] cbuf = new char[length];
        int read = 0;
        while (read < length) {
            int res = reader.read(cbuf, read, length - read);
            if (res == -1)
                break;
            read += res;
        }
        if (read == 0)
            return null;

        byte[] bytes = new byte[read];
        for (int i = 0; i < read; i++) {
            bytes[i] = (byte) cbuf[i];
        }
        return bytes;
    }

    // Parses a multipart request body to extract parameters and file content.
    private static byte[] extractMultipartParams(byte[] content, String contentType, Map<String, String> params) {
        if (content == null) {
            return null;
        }

        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            return content;
        }

        String body = new String(content, StandardCharsets.ISO_8859_1);
        String dashBoundary = "--" + boundary;
        String[] parts = body.split(dashBoundary);
        byte[] fileContent = null;

        for (String part : parts) {
            if (isDelimiterOrEmpty(part)) {
                continue;
            }

            int headerEnd = part.indexOf("\r\n\r\n");
            int headerLen = 4;
            if (headerEnd == -1) {
                headerEnd = part.indexOf("\n\n");
                headerLen = 2;
            }

            if (headerEnd != -1) {
                String headersPart = part.substring(0, headerEnd);
                MultipartHeaderInfo info = parsePartHeaders(headersPart);

                if (info.name != null) {
                    byte[] extracted = extractPartBody(part, headerEnd, headerLen, info, params);
                    if (extracted != null) {
                        fileContent = extracted;
                    }
                }
            }
        }

        return fileContent != null ? fileContent : content;
    }

    private static String extractBoundary(String contentType) {
        int bIdx = contentType.indexOf(BOUNDARY);
        if (bIdx != -1) {
            return contentType.substring(bIdx + BOUNDARY.length()).trim();
        }
        return null;
    }

    private static boolean isDelimiterOrEmpty(String part) {
        if (part == null) {
            return true;
        }
        String trimmed = part.trim();
        return trimmed.isEmpty() || trimmed.equals("--") || trimmed.equals("--\r\n");
    }

    // Parses headers of a single multipart part.
    private static MultipartHeaderInfo parsePartHeaders(String headersPart) {
        String name = null;
        String filename = null;
        String[] headerLines = headersPart.split("\n");
        for (String hl : headerLines) {
            hl = hl.trim();
            if (hl.toLowerCase().startsWith(CONTENT_DISPOSITION)) {
                name = extractHeaderDirective(hl, NAME);
                filename = extractHeaderDirective(hl, FILENAME);
            }
        }
        return new MultipartHeaderInfo(name, filename);
    }

    private static String extractHeaderDirective(String headerLine, String directive) {
        int idx = headerLine.indexOf(directive);
        if (idx != -1) {
            int end = headerLine.indexOf("\"", idx + directive.length());
            if (end != -1) {
                return headerLine.substring(idx + directive.length(), end);
            }
        }
        return null;
    }

    // Extracts the body of a single multipart part.
    private static byte[] extractPartBody(String part, int headerEnd, int headerLen, MultipartHeaderInfo info,
            Map<String, String> params) {
        int bodyStart = headerEnd + headerLen;
        int bodyEnd = part.length();
        if (part.endsWith("\r\n")) {
            bodyEnd -= 2;
        } else if (part.endsWith("\n")) {
            bodyEnd -= 1;
        }

        if (bodyStart <= bodyEnd) {
            String bodyPart = part.substring(bodyStart, bodyEnd);
            if (info.filename != null) {
                params.put(FILE_PARAM, info.filename);
                return bodyPart.getBytes(StandardCharsets.ISO_8859_1);
            } else {
                params.put(info.name, bodyPart.trim());
            }
        }
        return null;
    }

    private static class MultipartHeaderInfo {
        final String name;
        final String filename;

        MultipartHeaderInfo(String name, String filename) {
            this.name = name;
            this.filename = filename;
        }
    }

}
