package com.yisy.modules.doramcu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that generates a unique correlation ID for each request.
 * <p>
 * The correlation ID is stored in the MDC (Mapped Diagnostic Context) so that
 * all log messages within the request include the ID, making it easy to trace
 * a request across controllers, services, and repositories.
 * </p>
 * <p>
 * The ID is also added to the response header {@code X-Correlation-Id} so that
 * clients can include it in error reports.
 * </p>
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    /** Header name for the correlation ID. */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    /** MDC key for the correlation ID. */
    public static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CORRELATION_ID_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}
