package server;

import servlets.Servlet;
import servlets.Default404Servlet;
import server.RequestParser.RequestInfo;

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

public class MyHTTPServer extends Thread implements HTTPServer {

    private final int port;

    private final ExecutorService threadPool;
    private final Thread ownerThread;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private volatile boolean closed = false;

    private final Map<String, Servlet> getServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> postServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> deleteServlets = new ConcurrentHashMap<>();

    private final Map<String, Map<String, Servlet>> methodMaps = new ConcurrentHashMap<>();
    private final Servlet default404Servlet = new Default404Servlet();

    public MyHTTPServer(int port, int numThreads) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(numThreads);
        this.ownerThread = Thread.currentThread();

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

            RequestInfo ri = MultipartRequestParser.parseRequest(reader);
            if (ri == null)
                return;

            String method = ri.getHttpCommand();
            String path = ri.getPath();

            Servlet servlet = findBestMatchingServlet(method, path);
            if (servlet != null) {
                servlet.handle(ri, os);
            } else {
                default404Servlet.handle(ri, os);
            }

        } catch (Exception e) {
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

    @Override
    public void close() {
        if (Thread.currentThread() != ownerThread || closed) {
            return;
        }
        closed = true;
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

        for (Map<String, Servlet> map : methodMaps.values()) {
            closeAllServlets(map);
        }
    }

    private void closeAllServlets(Map<String, Servlet> map) {
        for (Servlet servlet : map.values()) {
            try {
                servlet.close();
            } catch (IOException e) {
                // Ignore servlet closing errors
            }
        }
        map.clear();
    }
}
