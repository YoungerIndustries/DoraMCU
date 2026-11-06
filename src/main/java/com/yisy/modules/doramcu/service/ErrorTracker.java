package com.yisy.modules.doramcu.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Error tracking service that counts errors by type and endpoint.
 * <p>
 * Features:
 * <ul>
 *   <li>Counts errors by exception type and endpoint</li>
 *   <li>Exposes error counts via Prometheus metrics</li>
 *   <li>Logs error summaries periodically</li>
 *   <li>Provides programmatic access to error statistics</li>
 * </ul>
 * </p>
 * <p>
 * Integrates with GlobalExceptionHandler to track all handled exceptions.
 * Error counts are exposed as Prometheus counters for monitoring dashboards.
 * </p>
 */
@Service
public class ErrorTracker {

    private static final Logger log = LoggerFactory.getLogger(ErrorTracker.class);

    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicLong> errorByType = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorByEndpoint = new ConcurrentHashMap<>();
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong lastSummaryTime = new AtomicLong(System.currentTimeMillis());

    /**
     * Creates a new ErrorTracker with the given MeterRegistry.
     *
     * @param meterRegistry the Micrometer registry for Prometheus metrics
     */
    public ErrorTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Records an error with the exception type and endpoint.
     *
     * @param exceptionType the simple class name of the exception
     * @param endpoint      the HTTP endpoint that failed (e.g., "/api/v1/game")
     */
    public void recordError(String exceptionType, String endpoint) {
        totalErrors.incrementAndGet();
        errorByType.computeIfAbsent(exceptionType, k -> new AtomicLong(0)).incrementAndGet();
        if (endpoint != null && !endpoint.isEmpty()) {
            errorByEndpoint.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        }

        // Increment Prometheus counter
        Counter.builder("doramcu.errors.total")
                .tag("type", exceptionType)
                .tag("endpoint", endpoint != null ? endpoint : "unknown")
                .register(meterRegistry)
                .increment();

        // Log error summary periodically (every 5 minutes)
        maybeLogSummary();
    }

    /**
     * Returns the total number of errors recorded.
     */
    public long getTotalErrors() {
        return totalErrors.get();
    }

    /**
     * Returns a map of error counts by exception type.
     */
    public Map<String, Long> getErrorsByType() {
        return errorByType.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    /**
     * Returns a map of error counts by endpoint.
     */
    public Map<String, Long> getErrorsByEndpoint() {
        return errorByEndpoint.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    /**
     * Returns the top N error types by count.
     *
     * @param n the number of top entries to return
     * @return a list of error type entries sorted by count descending
     */
    public List<Map.Entry<String, Long>> getTopErrorTypes(int n) {
        return errorByType.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .map(e -> Map.entry(e.getKey(), e.getValue().get()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the top N endpoints by error count.
     *
     * @param n the number of top entries to return
     * @return a list of endpoint entries sorted by count descending
     */
    public List<Map.Entry<String, Long>> getTopErrorEndpoints(int n) {
        return errorByEndpoint.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .map(e -> Map.entry(e.getKey(), e.getValue().get()))
                .collect(Collectors.toList());
    }

    /**
     * Resets all error counts.
     * <p>
     * Useful for testing or periodic cleanup.
     * </p>
     */
    public void reset() {
        totalErrors.set(0);
        errorByType.clear();
        errorByEndpoint.clear();
        log.info("ErrorTracker reset");
    }

    private void maybeLogSummary() {
        long now = System.currentTimeMillis();
        if (now - lastSummaryTime.get() > 5 * 60 * 1000) { // 5 minutes
            if (lastSummaryTime.compareAndSet(lastSummaryTime.get(), now)) {
                logSummary();
            }
        }
    }

    private void logSummary() {
        long total = totalErrors.get();
        if (total == 0) {
            return;
        }

        log.info("=== Error Summary (last 5 minutes) ===");
        log.info("Total errors: {}", total);

        errorByType.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(5)
                .forEach(entry -> log.info("  Type {}: {}", entry.getKey(), entry.getValue().get()));

        errorByEndpoint.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(5)
                .forEach(entry -> log.info("  Endpoint {}: {}", entry.getKey(), entry.getValue().get()));

        log.info("===================================");
    }
}
