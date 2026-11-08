package com.yisy.modules.doraai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration for shared HTTP client used by AI provider services.
 * <p>
 * Creates a single HttpClient instance with connection pooling and shared
 * executor to avoid resource leaks from multiple independent instances.
 * Each service previously created its own HttpClient, which meant separate
 * connection pools and thread pools — this configuration consolidates them.
 * </p>
 */
@Configuration
public class HttpClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    @Bean
    public HttpClient aiHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }
}
