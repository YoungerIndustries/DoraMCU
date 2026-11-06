package com.yisy.modules.doramcu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimiterService rateLimiterService;
    private final MetricsConfig metricsConfig;

    public RateLimitFilter(RateLimiterService rateLimiterService, MetricsConfig metricsConfig) {
        this.rateLimiterService = rateLimiterService;
        this.metricsConfig = metricsConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String uri = request.getRequestURI();

        // Apply stricter rate limiting for login endpoint
        if (uri.startsWith("/api/v1/auth/login") || uri.equals("/auth/login")) {
            if (!rateLimiterService.isAllowedForLogin(clientIp)) {
                log.warn("Rate limit exceeded for login from IP: {}", clientIp);
                metricsConfig.getRateLimitRejectedCounter().increment();
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Too many login attempts. Please try again later.\"}"
                );
                return;
            }
        } else {
            // Apply general rate limiting for all other endpoints
            if (!rateLimiterService.isAllowed(clientIp)) {
                log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, uri);
                metricsConfig.getRateLimitRejectedCounter().increment();
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Rate limit exceeded. Please try again later.\"}"
                );
                return;
            }
        }

        log.debug("Rate limit check passed for IP: {} on endpoint: {}", clientIp, uri);
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle IPv6 format
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
