package com.yisy.modules.doraai.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Common interface for AI providers (Ollama, Gemini, etc.).
 * <p>
 * Allows the application to switch between different LLM backends
 * without changing the service layer code.
 * </p>
 */
public interface AiProvider {

    /**
     * Represents a message in a chat conversation.
     */
    record Message(String role, String content) {
        public Message {
            if (role == null || role.isBlank()) {
                throw new IllegalArgumentException("Role must not be null or blank");
            }
            if (content == null) {
                content = "";
            }
        }
    }

    /**
     * Returns the provider identifier (e.g., "ollama", "gemini").
     */
    String getProviderId();

    /**
     * Sends a chat request and returns the complete response.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @return the AI response text
     */
    String chat(String model, List<Message> messages);

    /**
     * Sends a chat request and streams the response back via SSE.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @return SseEmitter for streaming the response
     */
    SseEmitter streamChat(String model, List<Message> messages);

    /**
     * Sends a chat request and streams the response to the given emitter.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @param emitter the SseEmitter to write events to
     * @param collector optional StringBuilder to collect message content
     */
    void streamChat(String model, List<Message> messages, SseEmitter emitter, StringBuilder collector);

    /**
     * Checks if the AI provider service is available.
     *
     * @return true if the provider is reachable, false otherwise
     */
    boolean isAvailable();
}
