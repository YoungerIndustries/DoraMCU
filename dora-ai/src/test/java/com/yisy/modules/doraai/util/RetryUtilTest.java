package com.yisy.modules.doraai.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryUtilTest {

    @Test
    void withRetry_succeeds_on_first_attempt() {
        int[] callCount = {0};
        String result = RetryUtil.withRetry(() -> {
            callCount[0]++;
            return "success";
        });
        assertEquals("success", result);
        assertEquals(1, callCount[0]);
    }

    @Test
    void withRetry_retries_on_io_exception() {
        int[] callCount = {0};
        String result = RetryUtil.withRetry(3, 10L, () -> {
            callCount[0]++;
            if (callCount[0] < 2) {
                throw new RuntimeException(new java.io.IOException("transient failure"));
            }
            return "success";
        });
        assertEquals("success", result);
        assertEquals(2, callCount[0]);
    }

    @Test
    void withRetry_fails_after_max_attempts() {
        int[] callCount = {0};
        assertThrows(RuntimeException.class, () ->
            RetryUtil.withRetry(3, 10L, () -> {
                callCount[0]++;
                throw new RuntimeException(new java.io.IOException("persistent failure"));
            })
        );
        assertEquals(3, callCount[0]);
    }

    @Test
    void withRetry_does_not_retry_non_retryable_exception() {
        assertThrows(RuntimeException.class, () ->
            RetryUtil.withRetry(3, 10L, () -> {
                throw new IllegalArgumentException("not retryable");
            })
        );
    }

    @Test
    void withRetry_runnable() {
        int[] callCount = {0};
        RetryUtil.withRetry(3, 10L, () -> {
            callCount[0]++;
            if (callCount[0] < 2) {
                throw new RuntimeException(new java.io.IOException("transient"));
            }
        });
        assertEquals(2, callCount[0]);
    }

    @Test
    void withRetry_default_params() {
        int[] callCount = {0};
        RetryUtil.withRetry(3, 10L, () -> {
            callCount[0]++;
            if (callCount[0] < 2) {
                throw new RuntimeException(new java.net.ConnectException("connection refused"));
            }
            return "ok";
        });
        assertEquals(2, callCount[0]);
    }
}
