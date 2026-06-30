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
}
