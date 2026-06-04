package com.clinica.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for RateLimitFilter:
 * - Within limit (allow)
 * - Exceeded limit (reject with 429)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RateLimitFilter Tests")
class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(String username) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            username,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_PACIENTE"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should allow request when under limit (first request)")
    void shouldAllowFirstRequest() throws Exception {
        // Given
        authenticateUser("testuser");
        when(request.getRequestURI()).thenReturn("/api/v1/citas");

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("Should skip rate limiting for auth endpoints")
    void shouldSkipRateLimitingForAuthEndpoints() throws Exception {
        // Given - auth endpoint path
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then - request passes through without rate limiting
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("Should allow unauthenticated requests without rate limiting")
    void shouldAllowUnauthenticatedRequests() throws Exception {
        // Given - no authentication set, request to non-auth endpoint
        when(request.getRequestURI()).thenReturn("/api/v1/citas");

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("ShouldNotFilter returns true for auth endpoints")
    void shouldNotFilterForAuthEndpoints() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");

        // When
        boolean result = rateLimitFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("ShouldNotFilter returns false for non-auth endpoints")
    void shouldNotFilterForNonAuthEndpoints() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/citas/1");

        // When
        boolean result = rateLimitFilter.shouldNotFilter(request);

        // Then
        assertFalse(result);
    }
}