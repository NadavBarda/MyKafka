package server.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

// Fixed window rate limiting strategy.
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
