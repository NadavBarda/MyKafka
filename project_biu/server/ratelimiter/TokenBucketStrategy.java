package server.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Token Bucket implementation of RateLimitingStrategy.
 * Limits requests using a token bucket algorithm per (clientId, uri) pair.
 */
public class TokenBucketStrategy implements RateLimitingStrategy {
    private final RateLimitConfig config;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketStrategy(RateLimitConfig config) {
        this.config = config;
    }

    @Override
    public boolean allowRequest(String clientId, String uri) {
        String key = clientId + ":" + uri;
        RateLimitConfig.LimitRule rule = config.getRule(uri);
        Bucket bucket = buckets.computeIfAbsent(key,
                k -> new Bucket(rule.getCapacity(), rule.getRefillRatePerSecond()));
        return bucket.consume();
    }

    private class Bucket {
        private final long capacity;
        private final double refillRatePerSecond;
        private double tokens;
        private long lastRefillTimestamp;

        public Bucket(long capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
            this.tokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean consume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsedMillis = now - lastRefillTimestamp;
            if (elapsedMillis > 0) {
                double tokensToAdd = (elapsedMillis / 1000.0) * refillRatePerSecond;
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTimestamp = now;
            }
        }
    }
}
