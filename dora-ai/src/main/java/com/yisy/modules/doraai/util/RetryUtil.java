package com.yisy.modules.doraai.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Simple retry utility with exponential backoff for external API calls.
 * <p>
 * Retries on transient failures (IOException, InterruptedException, 5xx errors)
 * with exponential backoff and jitter to avoid thundering herd problems.
 * </p>
 */
public class RetryUtil {

    private static final Logger log = LoggerFactory.getLogger(RetryUtil.class);

    private RetryUtil() {
        // Utility class
    }

    /**
     * Executes a supplier with retry logic.
     *
     * @param <T> the return type
     * @param maxAttempts maximum number of attempts (default 3)
     * @param baseDelayMs base delay in milliseconds between retries (default 1000)
     * @param operation the operation to execute
     * @return the result of the operation
     * @throws RuntimeException if all attempts fail
     */
    public static <T> T withRetry(int maxAttempts, long baseDelayMs, Supplier<T> operation) {
        Throwable lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T result = operation.get();
                if (attempt > 1) {
                    log.debug("Retry succeeded on attempt {}/{}", attempt, maxAttempts);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                if (!isRetryable(e) || attempt >= maxAttempts) {
                    // Preserve the original exception if it's already a RuntimeException
                    if (e instanceof RuntimeException re) {
                        throw re;
                    }
                    throw new RuntimeException("Operation failed after " + attempt + " attempt(s)", e);
                }
                long delay = calculateDelay(baseDelayMs, attempt);
                log.warn("Retryable failure (attempt {}/{}): {}. Retrying in {}ms...",
                        attempt, maxAttempts, e.getMessage(), delay);
                sleep(delay);
            }
        }

        throw new RuntimeException("Operation failed after " + maxAttempts + " attempts", lastException);
    }

    /**
     * Executes a runnable with retry logic (no return value).
     */
    public static void withRetry(int maxAttempts, long baseDelayMs, Runnable operation) {
        withRetry(maxAttempts, baseDelayMs, () -> {
            operation.run();
            return null;
        });
    }

    /**
     * Default retry with 3 attempts and 1-second base delay.
     */
    public static <T> T withRetry(Supplier<T> operation) {
        return withRetry(3, 1000, operation);
    }

    /**
     * Determines if an exception is retryable, checking both the exception
     * and its cause (for lambdas that wrap checked exceptions in RuntimeException).
     */
    private static boolean isRetryable(Throwable e) {
        return isRetryableInternal(e) || (e.getCause() != null && isRetryableInternal(e.getCause()));
    }

    private static boolean isRetryableInternal(Throwable e) {
        return e instanceof java.io.IOException
                || e instanceof InterruptedException
                || e instanceof java.net.ConnectException
                || e instanceof java.net.SocketTimeoutException
                || (e.getMessage() != null && (
                        e.getMessage().contains("500")
                                || e.getMessage().contains("502")
                                || e.getMessage().contains("503")
                                || e.getMessage().contains("504")
                                || e.getMessage().contains("Connection refused")
                                || e.getMessage().contains("Connection timed out")
                                || e.getMessage().contains("timeout")
                ));
    }

    /**
     * Calculates delay with exponential backoff and jitter.
     */
    private static long calculateDelay(long baseDelayMs, int attempt) {
        long exponentialDelay = baseDelayMs * (1L << (attempt - 1));
        long jitter = (long) (Math.random() * baseDelayMs);
        return Math.min(exponentialDelay + jitter, 30000); // Cap at 30 seconds
    }

    /**
     * Sleeps for the specified duration, handling interrupts.
     */
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during retry delay", e);
        }
    }
}
