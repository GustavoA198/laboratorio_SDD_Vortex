package com.clinica.config;

import com.clinica.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter that enforces rate limiting per user.
 * Uses a sliding window algorithm with ConcurrentHashMap for thread safety.
 * Limits to 10 requests per minute per user.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final Map<String, List<Long>> requests = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String user = getCurrentUser();

        // Skip rate limiting for unauthenticated requests
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        List<Long> timestamps = requests.computeIfAbsent(user, k -> new ArrayList<>());
        long now = System.currentTimeMillis();

        // Remove expired timestamps
        timestamps.removeIf(t -> now - t > WINDOW_MS);

        // Check rate limit
        if (timestamps.size() >= MAX_REQUESTS) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Rate limit exceeded. Maximum 10 requests per minute allowed.\"}"
            );
            return;
        }

        // Add current request timestamp
        timestamps.add(now);

        filterChain.doFilter(request, response);
    }

    /**
     * Get the current authenticated username from SecurityContext.
     *
     * @return the username or null if not authenticated
     */
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip rate limiting for auth endpoints
        return request.getRequestURI().startsWith("/api/v1/auth/");
    }
}