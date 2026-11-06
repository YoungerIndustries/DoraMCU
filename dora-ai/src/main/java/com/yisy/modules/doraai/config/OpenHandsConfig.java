package com.yisy.modules.doraai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for OpenHands/DoraAI2 integration.
 * <p>
 * These properties control how the application connects to the DoraAI2
 * Python agent server which provides autonomous coding capabilities
 * similar to Junie.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "openhands")
public class OpenHandsConfig {

    /** Whether the OpenHands provider is enabled (default: true) */
    private boolean enabled = true;

    /** Base URL of the DoraAI2 agent server (default: http://localhost:8082) */
    private String baseUrl = "http://localhost:8082";

    /** Agent mode: chat, code, fast_code, advanced_chat, run_verify, setup (default: chat) */
    private String mode = "chat";

    /** Connection timeout in milliseconds (default: 5000) */
    private int connectTimeoutMs = 5000;

    /** Read timeout in milliseconds (default: 300000, 5 minutes for agent tasks) */
    private int readTimeoutMs = 300000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
}
