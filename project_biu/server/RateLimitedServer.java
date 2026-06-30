package server;

import server.ratelimiter.RateLimiter;

/**
 * Interface representing a server that supports rate limiting.
 * Follows the Interface Segregation Principle (ISP) by separating
 * rate limiting configuration from the core HTTP server operations.
 */
public interface RateLimitedServer extends HTTPServer {
    /**
     * Gets the active rate limiter config context.
     *
     * @return the active RateLimiter instance
     */
    RateLimiter getRateLimiter();

    /**
     * Sets or changes the rate limiter context dynamically.
     *
     * @param rateLimiter the new RateLimiter instance to apply
     */
    void setRateLimiter(RateLimiter rateLimiter);

    /**
     * Checks if a request from the given client for the specified URI is allowed under rate limits.
     *
     * @param clientId the client identifier
     * @param uri the requested URI
     * @return true if the request is allowed (or no rate limiting is active); false otherwise
     */
    boolean allow(String clientId, String uri);
}
