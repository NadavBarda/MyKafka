package server.ratelimiter;

// RateLimiter context that delegates requests to a RateLimitingStrategy.
public class RateLimiter {
    private volatile RateLimitingStrategy strategy;

    // Initialize with a rate limiting strategy.
    public RateLimiter(RateLimitingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    // Changes the active rate limiting strategy.
    public void setStrategy(RateLimitingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    // Checks if the request is allowed.
    public boolean allowRequest(String clientId, String uri) {
        return strategy.allowRequest(clientId, uri);
    }
}
