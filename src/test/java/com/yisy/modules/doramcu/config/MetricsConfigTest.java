package com.yisy.modules.doramcu.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MetricsConfig Tests")
class MetricsConfigTest {

    private MetricsConfig metricsConfig;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsConfig = new MetricsConfig(meterRegistry);
    }

    // ---- Device counters ----

    @Test
    @DisplayName("getDeviceCreateCounter - should return non-null counter")
    void getDeviceCreateCounter_shouldReturnCounter() {
        Counter counter = metricsConfig.getDeviceCreateCounter();
        assertNotNull(counter);
    }

    @Test
    @DisplayName("getDeviceUpdateCounter - should return non-null counter")
    void getDeviceUpdateCounter_shouldReturnCounter() {
        Counter counter = metricsConfig.getDeviceUpdateCounter();
        assertNotNull(counter);
    }

    @Test
    @DisplayName("getDeviceDeleteCounter - should return non-null counter")
    void getDeviceDeleteCounter_shouldReturnCounter() {
        Counter counter = metricsConfig.getDeviceDeleteCounter();
        assertNotNull(counter);
    }

    @Test
    @DisplayName("getDeviceFetchCounter - should return non-null counter")
    void getDeviceFetchCounter_shouldReturnCounter() {
        Counter counter = metricsConfig.getDeviceFetchCounter();
        assertNotNull(counter);
    }

    // ---- Auth counters ----

    @Test
    @DisplayName("getLoginSuccessCounter - should return non-null counter")
    void getLoginSuccessCounter_shouldReturnCounter() {
        Counter counter = metricsConfig.getLoginSuccessCounter();
        assertNotNull(counter);
    }

    @Test
    @DisplayName("getLoginFailureCounter - should return non-null counter")
    void getLoginFailureCounter_shouldReturnCounter() {
        Counter counter = metricsConfig.getLoginFailureCounter();
        assertNotNull(counter);
    }

    // ---- Rate limit counter ----

    @Test
    @DisplayName("getRateLimitRejectedCounter - should return non-null counter")
    void getRateLimitRejectedCounter_shouldReturnCounter() {
        Counter counter = metricsConfig.getRateLimitRejectedCounter();
        assertNotNull(counter);
    }

    // ---- Timer ----

    @Test
    @DisplayName("getDeviceOperationTimer - should return non-null timer")
    void getDeviceOperationTimer_shouldReturnTimer() {
        Timer timer = metricsConfig.getDeviceOperationTimer();
        assertNotNull(timer);
    }

    // ---- Counter increment ----

    @Test
    @DisplayName("Device counters - should increment correctly")
    void deviceCounters_shouldIncrement() {
        Counter createCounter = metricsConfig.getDeviceCreateCounter();
        assertEquals(0, createCounter.count());
        createCounter.increment();
        assertEquals(1, createCounter.count());
    }

    @Test
    @DisplayName("Auth counters - should increment correctly")
    void authCounters_shouldIncrement() {
        Counter successCounter = metricsConfig.getLoginSuccessCounter();
        assertEquals(0, successCounter.count());
        successCounter.increment();
        assertEquals(1, successCounter.count());
    }

    @Test
    @DisplayName("Rate limit counter - should increment correctly")
    void rateLimitCounter_shouldIncrement() {
        Counter rejectedCounter = metricsConfig.getRateLimitRejectedCounter();
        assertEquals(0, rejectedCounter.count());
        rejectedCounter.increment();
        assertEquals(1, rejectedCounter.count());
    }

    // ---- Meter registry verification ----

    @Test
    @DisplayName("All metrics - should be registered in meter registry")
    void allMetrics_shouldBeRegistered() {
        // Verify all counters are registered
        assertTrue(meterRegistry.find("device.operations.create").counter() != null);
        assertTrue(meterRegistry.find("device.operations.update").counter() != null);
        assertTrue(meterRegistry.find("device.operations.delete").counter() != null);
        assertTrue(meterRegistry.find("device.operations.fetch").counter() != null);
        assertTrue(meterRegistry.find("auth.login.success").counter() != null);
        assertTrue(meterRegistry.find("auth.login.failure").counter() != null);
        assertTrue(meterRegistry.find("ratelimit.requests.rejected").counter() != null);
        assertTrue(meterRegistry.find("device.operations.duration").timer() != null);
    }
}
