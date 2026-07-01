package server.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed Window implementation of RateLimitingStrategy.
 * Limits requests using a fixed-time window per (clientId, uri) pair.
 */
public class FixedWindowStrategy implements RateLimitingStrategy {
    private final long windowSizeMillis;
    private final int maxRequests;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public FixedWindowStrategy(long windowSizeMillis, int maxRequests) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxRequests = maxRequests;
    }

    @Override
    public boolean allowRequest(String clientId, String uri) {
        String key = clientId + ":" + uri;
        Window window = windows.computeIfAbsent(key, k -> new Window());
        return window.allow(maxRequests, windowSizeMillis);
    }

    private static class Window {
        private long windowStart;
        private int requestCount;

        public Window() {
            this.windowStart = System.currentTimeMillis();
            this.requestCount = 0;
        }

        public synchronized boolean allow(int maxRequests, long windowSizeMillis) {
            long now = System.currentTimeMillis();
            if (now - windowStart >= windowSizeMillis) {
                windowStart = now;
                requestCount = 0;
            }

            if (requestCount < maxRequests) {
                requestCount++;
                return true;
            }
            return false;
        }
    }
}
