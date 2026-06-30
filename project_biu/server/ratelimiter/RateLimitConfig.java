package server.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration class that maps URIs to their respective rate limiting rules.
 * Follows SRP by handling configuration lookup separate from execution logic.
 */
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

    /**
     * Constructs the config with a default rate limit rule.
     *
     * @param defaultCapacity the default token capacity
     * @param defaultRefillRate the default refill rate per second
     */
    public RateLimitConfig(long defaultCapacity, double defaultRefillRate) {
        this.defaultRule = new LimitRule(defaultCapacity, defaultRefillRate);
    }

    /**
     * Adds or overrides a rate limiting rule for a specific URI.
     *
     * @param uri the URI pattern to match
     * @param capacity the token capacity for this URI
     * @param refillRate the refill rate per second for this URI
     */
    public void addRule(String uri, long capacity, double refillRate) {
        uriRules.put(uri, new LimitRule(capacity, refillRate));
    }

    /**
     * Retrieves the rate limiting rule for a given URI, falling back to the default rule.
     *
     * @param uri the requested URI
     * @return the applicable LimitRule
     */
    public LimitRule getRule(String uri) {
        return uriRules.getOrDefault(uri, defaultRule);
    }
}
