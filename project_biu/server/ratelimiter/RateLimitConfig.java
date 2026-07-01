package server.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Maps URIs to their rate limiting rules.
public class RateLimitConfig {
    public static class LimitRule {
        private final long capacity;
        private final double refillRatePerSecond;

        public LimitRule(long capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
        }

        public long getCapacity() {
            return capacity;
        }

        public double getRefillRatePerSecond() {
            return refillRatePerSecond;
        }
    }

    private final LimitRule defaultRule;
    private final Map<String, LimitRule> uriRules = new ConcurrentHashMap<>();

    // Set default capacity and refill rate.
    public RateLimitConfig(long defaultCapacity, double defaultRefillRate) {
        this.defaultRule = new LimitRule(defaultCapacity, defaultRefillRate);
    }

    // Adds or overrides a rule for a specific URI.
    public void addRule(String uri, long capacity, double refillRate) {
        uriRules.put(uri, new LimitRule(capacity, refillRate));
    }

    // Gets the rule for a URI, or defaultRule if none exists.
    public LimitRule getRule(String uri) {
        return uriRules.getOrDefault(uri, defaultRule);
    }
}
