package server;

import server.ratelimiter.RateLimiter;

// Interface for servers supporting rate limiting.
public interface RateLimitedServer extends HTTPServer {
    // Gets the active rate limiter.
    RateLimiter getRateLimiter();

    // Sets the active rate limiter.
    void setRateLimiter(RateLimiter rateLimiter);

    // Checks if request is allowed by rate limiting.
    boolean allow(String clientId, String uri);
}
