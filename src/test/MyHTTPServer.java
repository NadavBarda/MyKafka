package test;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import test.RequestParser.RequestInfo;

public class MyHTTPServer extends Thread implements HTTPServer {

    private final int port;

    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    // 3 distinct thread-safe maps for HTTP methods as required by the assignment
    private final Map<String, Servlet> getServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> postServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> deleteServlets = new ConcurrentHashMap<>();

    // Map holding the method maps
    private final Map<String, Map<String, Servlet>> methodMaps = new ConcurrentHashMap<>();

    public MyHTTPServer(int port, int numThreads) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(numThreads);

        // Initialize the methodMaps registry
        methodMaps.put("GET", getServlets);
        methodMaps.put("POST", postServlets);
        methodMaps.put("DELETE", deleteServlets);
    }

    private Map<String, Servlet> getMapForMethod(String httpCommand) {
        if (httpCommand == null) {
            return null;
        }
        return methodMaps.get(httpCommand.toUpperCase());
    }

    @Override
    public void addServlet(String httpCommanmd, String uri, Servlet s) {
        Map<String, Servlet> map = getMapForMethod(httpCommanmd);
        if (map != null && uri != null && s != null) {
            map.put(uri, s);
        }
    }

    @Override
    public void removeServlet(String httpCommanmd, String uri) {
        Map<String, Servlet> map = getMapForMethod(httpCommanmd);
        if (map != null && uri != null) {
            map.remove(uri);
        }
    }

    @Override
    public void start() {
        this.running = true;
        super.start();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            serverListener();
        } catch (IOException e) {
        } finally {
            close();
        }
    }

    private void serverListener() {
        while (this.running) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                try {
                    threadPool.submit(() -> handleClient(clientSocket));
                } catch (java.util.concurrent.RejectedExecutionException e) {
                    try {
                        clientSocket.close();
                    } catch (IOException ex) {
                        // Ignore
                    }
                }
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                if (!this.running) {
                    break;
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream os = socket.getOutputStream()) {

            RequestInfo ri = RequestParser.parseRequest(reader);
            if (ri == null)
                return;

            String method = ri.getHttpCommand();
            String path = ri.getPath();

            Servlet servlet = findBestMatchingServlet(method, path);
            if (servlet != null) {
                servlet.handle(ri, os);
            } else {
                send404(os);
            }

        } catch (Exception e) {
            // Suppress exception to keep the worker thread healthy
        }
    }

    private Servlet findBestMatchingServlet(String httpCommand, String path) {
        Map<String, Servlet> map = getMapForMethod(httpCommand);
        if (map == null || path == null) {
            return null;
        }

        Servlet bestMatch = null;
        int longestMatchLength = -1;

        for (Map.Entry<String, Servlet> entry : map.entrySet()) {
            String registeredUri = entry.getKey();
            if (path.startsWith(registeredUri)) {
                if (registeredUri.length() > longestMatchLength) {
                    longestMatchLength = registeredUri.length();
                    bestMatch = entry.getValue();
                }
            }
        }
        return bestMatch;
    }

    private void send404(OutputStream os) throws IOException {
        String body = "<html><body><h1>404 Not Found</h1><p>No matching servlet was found for this request.</p></body></html>";
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + body.getBytes().length + "\r\n" +
                "Connection: close\r\n\r\n" +
                body;
        os.write(response.getBytes());
        os.flush();
    }

    @Override
    public void close() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Ignore close errors
        }

        threadPool.shutdown();

        // Close all servlets in all maps safely
        closeAllServlets(getServlets);
        closeAllServlets(postServlets);
        closeAllServlets(deleteServlets);
    }

    private void closeAllServlets(Map<String, Servlet> map) {
        for (Servlet servlet : map.values()) {
            try {
                servlet.close();
            } catch (IOException e) {
                // Ignore servlet closing errors
            }
        }
    }
}
