package server.ratelimiter;

/**
 * Context class in the Strategy Pattern.
 * Delegates rate limiting checks to the currently active strategy.
 */
public class RateLimiter {
    private volatile RateLimitingStrategy strategy;

    /**
     * Constructs a RateLimiter with the specified initial strategy.
     *
     * @param strategy the initial rate limiting strategy
     */
    public RateLimiter(RateLimitingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    /**
     * Dynamically changes the active rate limiting strategy.
     *
     * @param strategy the new strategy to use
     */
    public void setStrategy(RateLimitingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    /**
     * Checks if the request from the given client for the specified URI is allowed.
     *
     * @param clientId the identifier of the client making the request
     * @param uri the URI requested
     * @return true if the request is allowed; false otherwise
     */
    public boolean allowRequest(String clientId, String uri) {
        return strategy.allowRequest(clientId, uri);
    }
}
