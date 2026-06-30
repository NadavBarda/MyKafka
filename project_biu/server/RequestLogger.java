package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class RequestLogger {
    private static final String LOG_FILE_PATH = "server.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ConcurrentLinkedQueue<LogEntry> logQueue = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static Thread loggerThread;
    private static PrintWriter fileWriter;

    public static class LogEntry {
        final LocalDateTime timestamp;
        final String clientAddress;
        final String method;
        final String uri;
        final String level;
        final String message;
        final Throwable throwable;

        public LogEntry(String clientAddress, String method, String uri, String level, String message, Throwable throwable) {
            this.timestamp = LocalDateTime.now();
            this.clientAddress = clientAddress;
            this.method = method;
            this.uri = uri;
            this.level = level;
            this.message = message;
            this.throwable = throwable;
        }
    }

    public static synchronized void start() {
        if (running.compareAndSet(false, true)) {
            try {
                // Initialize writer in append mode with auto-flushing
                fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(LOG_FILE_PATH, true)), true);
            } catch (IOException e) {
                System.err.println("Failed to initialize log file: " + e.getMessage());
            }
            loggerThread = new Thread(RequestLogger::runLoggerLoop, "Request-Logger-Thread");
            loggerThread.setDaemon(true);
            loggerThread.start();
        }
    }

    private static void runLoggerLoop() {
        while (running.get() || !logQueue.isEmpty()) {
            try {
                LogEntry entry = logQueue.poll();
                if (entry != null) {
                    processLogEntry(entry);
                } else {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Logger error: " + e.getMessage());
            }
        }
        
        // Ensure log file writer is safely closed when the thread exits
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

    private static void processLogEntry(LogEntry entry) {
        String formattedTime = entry.timestamp.format(formatter);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] [%s] Client: %s | Method: %s | URI: %s",
                formattedTime, entry.level, entry.clientAddress, entry.method, entry.uri));
        
        if (entry.message != null && !entry.message.isEmpty()) {
            sb.append(" | Message: ").append(entry.message);
        }
        if (entry.throwable != null) {
            sb.append(" | Exception: ").append(entry.throwable.toString());
        }

        String logMessage = sb.toString();
        if (fileWriter != null) {
            fileWriter.println(logMessage);
            if (entry.throwable != null) {
                entry.throwable.printStackTrace(fileWriter);
            }
        } else {
            System.err.println("Fallback - " + logMessage);
            if (entry.throwable != null) {
                entry.throwable.printStackTrace(System.err);
            }
        }
    }

    public static void log(String clientAddress, String method, String uri) {
        log(clientAddress, method, uri, "INFO", null, null);
    }

    public static void logError(String clientAddress, String method, String uri, String message, Throwable throwable) {
        log(clientAddress, method, uri, "ERROR", message, throwable);
    }

    public static void log(String clientAddress, String method, String uri, String level, String message, Throwable throwable) {
        if (!running.get()) {
            start();
        }
        logQueue.offer(new LogEntry(clientAddress, method, uri, level, message, throwable));
    }

    public static synchronized void shutdown() {
        if (running.compareAndSet(true, false)) {
            if (loggerThread != null) {
                try {
                    loggerThread.join(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
