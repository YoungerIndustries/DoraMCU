package com.yisy.modules.doramcu.config;

import io.micrometer.core.instrument.Counter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimitFilterTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private MetricsConfig metricsConfig;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() throws IOException {
        rateLimitFilter = new RateLimitFilter(rateLimiterService, metricsConfig);
        // Mock the response writer — must return a real PrintWriter
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    // ---- General endpoint - allowed ----

    @Test
    @DisplayName("doFilterInternal - should allow general endpoint when under limit")
    void doFilterInternal_shouldAllowGeneralEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/devices");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimiterService.isAllowed("192.168.1.1")).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // ---- General endpoint - rate limited ----

    @Test
    @DisplayName("doFilterInternal - should reject general endpoint when over limit")
    void doFilterInternal_shouldRejectGeneralEndpoint() throws ServletException, IOException {
        when(rateLimiterService.isAllowed("192.168.1.1")).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/api/devices");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(metricsConfig.getRateLimitRejectedCounter()).thenReturn(mock(Counter.class));

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429); // TOO_MANY_REQUESTS
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(any(), any());
    }

    // ---- Login endpoint - allowed ----

    @Test
    @DisplayName("doFilterInternal - should allow login endpoint when under limit")
    void doFilterInternal_shouldAllowLoginEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimiterService.isAllowedForLogin("192.168.1.1")).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // ---- Login endpoint - rate limited ----

    @Test
    @DisplayName("doFilterInternal - should reject login endpoint when over limit")
    void doFilterInternal_shouldRejectLoginEndpoint() throws ServletException, IOException {
        when(rateLimiterService.isAllowedForLogin("192.168.1.1")).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(metricsConfig.getRateLimitRejectedCounter()).thenReturn(mock(Counter.class));

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429); // TOO_MANY_REQUESTS
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(any(), any());
    }

    // ---- IP extraction - X-Forwarded-For ----

    @Test
    @DisplayName("doFilterInternal - should use X-Forwarded-For header for IP")
    void doFilterInternal_shouldUseXForwardedFor() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/devices");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
        when(rateLimiterService.isAllowed("10.0.0.1")).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimiterService).isAllowed("10.0.0.1");
    }

    // ---- IP extraction - X-Real-IP ----

    @Test
    @DisplayName("doFilterInternal - should use X-Real-IP when X-Forwarded-For is missing")
    void doFilterInternal_shouldUseXRealIP() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/devices");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
        when(rateLimiterService.isAllowed("10.0.0.2")).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimiterService).isAllowed("10.0.0.2");
    }

    // ---- IP extraction - remote addr ----

    @Test
    @DisplayName("doFilterInternal - should use remoteAddr as fallback")
    void doFilterInternal_shouldUseRemoteAddr() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/devices");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(rateLimiterService.isAllowed("192.168.1.100")).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimiterService).isAllowed("192.168.1.100");
    }

    // ---- IP extraction - unknown header ----

    @Test
    @DisplayName("doFilterInternal - should skip unknown X-Forwarded-For value")
    void doFilterInternal_shouldSkipUnknownForwardedFor() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/devices");
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.200");
        when(rateLimiterService.isAllowed("192.168.1.200")).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimiterService).isAllowed("192.168.1.200");
    }

    // ---- IP extraction - IPv6 with comma ----

    @Test
    @DisplayName("doFilterInternal - should handle comma-separated X-Forwarded-For")
    void doFilterInternal_shouldHandleCommaSeparatedIP() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/devices");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(rateLimiterService.isAllowed("10.0.0.1")).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimiterService).isAllowed("10.0.0.1");
    }
}
