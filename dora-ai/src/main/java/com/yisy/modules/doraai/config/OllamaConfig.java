package com.yisy.modules.doraai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Ollama integration.
 * <p>
 * These properties control how the application connects to the local Ollama
 * instance and which model to use for AI responses.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {

    /** Base URL of the Ollama API (default: http://localhost:11434) */
    private String baseUrl = "http://localhost:11434";

    /** Default model name to use for chat (default: llama3) */
    private String model = "llama3";

    /** Temperature for generation (0.0 to 1.0, default: 0.7) */
    private double temperature = 0.7;

    /** Maximum number of tokens in the response (default: 2048) */
    private int maxTokens = 2048;

    /** Connection timeout in milliseconds (default: 5000) */
    private int connectTimeoutMs = 5000;

    /** Read timeout in milliseconds (default: 120000) */
    private int readTimeoutMs = 120000;

    /** Whisper model name for speech-to-text (default: whisper) */
    private String whisperModel = "whisper";

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

    public String getWhisperModel() {
        return whisperModel;
    }

    public void setWhisperModel(String whisperModel) {
        this.whisperModel = whisperModel;
    }
}
