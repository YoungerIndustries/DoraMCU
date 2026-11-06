package com.yisy.modules.doramcu.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MetricsConfig {

    private static final Logger log = LoggerFactory.getLogger(MetricsConfig.class);

    private final Counter deviceCreateCounter;
    private final Counter deviceUpdateCounter;
    private final Counter deviceDeleteCounter;
    private final Counter deviceFetchCounter;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter rateLimitRejectedCounter;
    private final Timer deviceOperationTimer;

    public MetricsConfig(MeterRegistry meterRegistry) {
        log.info("Initializing custom Micrometer metrics");

        // Device operation counters
        deviceCreateCounter = Counter.builder("device.operations.create")
                .description("Number of device creation operations")
                .tag("type", "device")
                .register(meterRegistry);

        deviceUpdateCounter = Counter.builder("device.operations.update")
                .description("Number of device update operations")
                .tag("type", "device")
                .register(meterRegistry);

        deviceDeleteCounter = Counter.builder("device.operations.delete")
                .description("Number of device deletion operations")
                .tag("type", "device")
                .register(meterRegistry);

        deviceFetchCounter = Counter.builder("device.operations.fetch")
                .description("Number of device fetch operations")
                .tag("type", "device")
                .register(meterRegistry);

        // Authentication counters
        loginSuccessCounter = Counter.builder("auth.login.success")
                .description("Number of successful login attempts")
                .tag("type", "authentication")
                .register(meterRegistry);

        loginFailureCounter = Counter.builder("auth.login.failure")
                .description("Number of failed login attempts")
                .tag("type", "authentication")
                .register(meterRegistry);

        // Rate limiting counter
        rateLimitRejectedCounter = Counter.builder("ratelimit.requests.rejected")
                .description("Number of requests rejected by rate limiter")
                .tag("type", "rate-limiting")
                .register(meterRegistry);

        // Device operation timer
        deviceOperationTimer = Timer.builder("device.operations.duration")
                .description("Duration of device operations")
                .tag("type", "device")
                .register(meterRegistry);

        log.info("Custom metrics registered successfully");
    }

    public Counter getDeviceCreateCounter() {
        return deviceCreateCounter;
    }

    public Counter getDeviceUpdateCounter() {
        return deviceUpdateCounter;
    }

    public Counter getDeviceDeleteCounter() {
        return deviceDeleteCounter;
    }

    public Counter getDeviceFetchCounter() {
        return deviceFetchCounter;
    }

    public Counter getLoginSuccessCounter() {
        return loginSuccessCounter;
    }

    public Counter getLoginFailureCounter() {
        return loginFailureCounter;
    }

    public Counter getRateLimitRejectedCounter() {
        return rateLimitRejectedCounter;
    }

    public Timer getDeviceOperationTimer() {
        return deviceOperationTimer;
    }
}
