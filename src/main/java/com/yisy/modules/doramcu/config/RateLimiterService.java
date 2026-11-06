package com.yisy.modules.doramcu.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    @Value("${ratelimit.general.requests-per-minute:100}")
    private int generalRequestsPerMinute;

    @Value("${ratelimit.login.requests-per-minute:5}")
    private int loginRequestsPerMinute;

    @Value("${ratelimit.window-seconds:60}")
    private int windowSeconds;

    /**
     * Lock-free sliding window rate limiter.
     * Each key maps to a counter and a window start timestamp.
     * Uses ConcurrentHashMap for thread safety without synchronized blocks.
     */
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    private final Cache<String, Void> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private static class WindowCounter {
        final long windowStart;
        final AtomicInteger count;

        WindowCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(count);
        }
    }

    public boolean isAllowed(String key) {
        return isAllowed(key, generalRequestsPerMinute);
    }

    public boolean isAllowedForLogin(String key) {
        return isAllowed(key, loginRequestsPerMinute);
    }

    /**
     * Lock-free rate limit check using sliding window counters.
     * Keeps a cache entry alive so expired keys are eventually garbage collected.
     */
    private boolean isAllowed(String key, int maxRequests) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        // Ensure the key exists in the cache for TTL-based eviction
        cache.get(key, k -> null);

        WindowCounter counter = counters.get(key);
        if (counter == null || counter.windowStart < windowStart) {
            // Window expired or first request — reset counter
            WindowCounter newCounter = new WindowCounter(now, 1);
            WindowCounter existing = counters.putIfAbsent(key, newCounter);
            if (existing != null && existing.windowStart >= windowStart) {
                // Another thread won the race and the window is still valid
                int newCount = existing.count.incrementAndGet();
                if (newCount > maxRequests) {
                    existing.count.decrementAndGet();
                    log.warn("Rate limit exceeded for key: {} ({} requests in {} seconds)", key, newCount - 1, windowSeconds);
                    return false;
                }
            }
            return true;
        } else {
            // Window still valid — increment atomically
            int newCount = counter.count.incrementAndGet();
            if (newCount > maxRequests) {
                counter.count.decrementAndGet();
                log.warn("Rate limit exceeded for key: {} ({} requests in {} seconds)", key, newCount - 1, windowSeconds);
                return false;
            }
            return true;
        }
    }

    public int getGeneralRequestsPerMinute() {
        return generalRequestsPerMinute;
    }

    public int getLoginRequestsPerMinute() {
        return loginRequestsPerMinute;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }
}
