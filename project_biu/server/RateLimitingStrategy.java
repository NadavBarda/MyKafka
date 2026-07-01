package server;

/**
 * Strategy interface for rate limiting algorithms.
 * Follows the Strategy Pattern to decouple the rate limiter decision logic.
 */
public interface RateLimitingStrategy {
    /**
     * Determines whether the given request should be allowed.
     *
     * @param clientId the identifier of the client making the request
     * @param uri      the URI requested
     * @return true if the request is allowed under the rate limit; false otherwise
     */
    boolean allowRequest(String clientId, String uri);
}
