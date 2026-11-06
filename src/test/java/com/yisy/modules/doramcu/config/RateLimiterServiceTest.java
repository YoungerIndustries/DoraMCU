package com.yisy.modules.doramcu.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RateLimiterService Tests")
class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() throws Exception {
        rateLimiterService = new RateLimiterService();
        // Set @Value fields using reflection since Spring isn't injecting them
        setField(rateLimiterService, "generalRequestsPerMinute", 100);
        setField(rateLimiterService, "loginRequestsPerMinute", 5);
        setField(rateLimiterService, "windowSeconds", 60);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ---- isAllowed (general) ----

    @Test
    @DisplayName("isAllowed - should allow first request")
    void isAllowed_shouldAllowFirstRequest() {
        assertTrue(rateLimiterService.isAllowed("192.168.1.1"));
    }

    @Test
    @DisplayName("isAllowed - should allow multiple requests under limit")
    void isAllowed_shouldAllowMultipleRequests() {
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiterService.isAllowed("192.168.1.1"), "Request " + i + " should be allowed");
        }
    }

    @Test
    @DisplayName("isAllowed - should use different limits for different keys")
    void isAllowed_shouldUseDifferentLimitsForDifferentKeys() {
        // Each IP has its own counter
        assertTrue(rateLimiterService.isAllowed("192.168.1.1"));
        assertTrue(rateLimiterService.isAllowed("192.168.1.2"));
    }

    // ---- isAllowedForLogin ----

    @Test
    @DisplayName("isAllowedForLogin - should allow first login request")
    void isAllowedForLogin_shouldAllowFirstRequest() {
        assertTrue(rateLimiterService.isAllowedForLogin("192.168.1.1"));
    }

    @Test
    @DisplayName("isAllowedForLogin - should allow up to login limit")
    void isAllowedForLogin_shouldAllowUpToLimit() {
        // Default login limit is 5 requests per minute
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.isAllowedForLogin("192.168.1.10"), "Login request " + i + " should be allowed");
        }
    }

    @Test
    @DisplayName("isAllowedForLogin - should reject after exceeding limit")
    void isAllowedForLogin_shouldRejectAfterLimit() {
        String key = "192.168.1.20";
        // Exhaust the login limit (default 5)
        for (int i = 0; i < 5; i++) {
            rateLimiterService.isAllowedForLogin(key);
        }
        assertFalse(rateLimiterService.isAllowedForLogin(key), "Should be rejected after limit");
    }

    // ---- Getters ----

    @Test
    @DisplayName("getGeneralRequestsPerMinute - should return default value")
    void getGeneralRequestsPerMinute_shouldReturnDefault() {
        // Default is 100
        assertEquals(100, rateLimiterService.getGeneralRequestsPerMinute());
    }

    @Test
    @DisplayName("getLoginRequestsPerMinute - should return default value")
    void getLoginRequestsPerMinute_shouldReturnDefault() {
        // Default is 5
        assertEquals(5, rateLimiterService.getLoginRequestsPerMinute());
    }

    @Test
    @DisplayName("getWindowSeconds - should return default value")
    void getWindowSeconds_shouldReturnDefault() {
        // Default is 60
        assertEquals(60, rateLimiterService.getWindowSeconds());
    }
}
