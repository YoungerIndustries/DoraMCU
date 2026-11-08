package com.yisy.modules.doramcu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Adds API deprecation headers to v1 endpoints.
 * <p>
 * When v2 APIs are introduced, these headers will inform clients that v1 is deprecated
 * and provide the sunset date for planning migrations.
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ApiDeprecationFilter extends OncePerRequestFilter {

    private static final String DEPRECATION_HEADER = "Deprecation";
    private static final String SUNSET_HEADER = "Sunset";
    private static final String LINK_HEADER = "Link";

    /**
     * Sunset date for v1 APIs: December 31, 2027 00:00:00 UTC
     * Update this date when setting a new deprecation timeline.
     */
    private static final String SUNSET_DATE = "2027-12-31";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/api/v1/")) {
            // Format sunset date as HTTP-date (RFC 7231)
            Instant sunsetInstant = Instant.parse(SUNSET_DATE + "T00:00:00Z");
            String sunsetFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
                    .withZone(ZoneOffset.UTC)
                    .format(sunsetInstant);

            response.setHeader(DEPRECATION_HEADER, "true");
            response.setHeader(SUNSET_HEADER, sunsetFormatted);
            response.setHeader(LINK_HEADER, "</api/v2/>; rel=\"successor-version\"");
        }

        filterChain.doFilter(request, response);
    }
}
