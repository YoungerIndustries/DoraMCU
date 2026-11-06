package com.yisy.modules.doraai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Google Gemini integration.
 * <p>
 * These properties control how the application connects to the Google Gemini API
 * and which model to use for AI responses.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiConfig {

    /** Whether Gemini is enabled (default: false) */
    private boolean enabled = false;

    /** Google API key for Gemini access */
    private String apiKey;

    /** Base URL for the Gemini API (default: https://generativelanguage.googleapis.com) */
    private String baseUrl = "https://generativelanguage.googleapis.com";

    /** Default model name to use (default: gemini-2.0-flash) */
    private String model = "gemini-2.0-flash";

    /** Temperature for generation (0.0 to 1.0, default: 0.7) */
    private double temperature = 0.7;

    /** Maximum number of tokens in the response (default: 2048) */
    private int maxTokens = 2048;

    /** Connection timeout in milliseconds (default: 5000) */
    private int connectTimeoutMs = 5000;

    /** Read timeout in milliseconds (default: 120000) */
    private int readTimeoutMs = 120000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
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
