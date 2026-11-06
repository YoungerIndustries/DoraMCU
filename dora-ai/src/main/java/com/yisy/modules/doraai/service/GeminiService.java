package com.yisy.modules.doraai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraai.config.GeminiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Service for communicating with the Google Gemini API.
 * <p>
 * This service provides methods for chat and streaming responses via the Gemini API.
 * It implements the {@link AiProvider} interface to allow switching between AI backends.
 * </p>
 */
@Service
@ConditionalOnProperty(prefix = "gemini", name = "enabled", havingValue = "true")
public class GeminiService implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final GeminiConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiService(GeminiConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofMillis(config.getConnectTimeoutMs()))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderId() {
        return "gemini";
    }

    /**
     * Sends a chat request to Gemini and returns the complete response.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @return the AI response text
     */
    @Override
    public String chat(String model, List<Message> messages) {
        String modelName = (model != null && !model.isBlank()) ? model : config.getModel();
        log.debug("Sending chat request to Gemini, model: {}, messages: {}", modelName, messages.size());

        try {
            String jsonBody = buildChatRequest(modelName, messages, false);
            String url = config.getBaseUrl() + "/v1beta/models/" + modelName + ":generateContent?key=" + config.getApiKey();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini chat request failed with status: {}", response.statusCode());
                throw new GeminiException("Gemini API error: " + response.statusCode() + " - " + response.body());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            String content = extractContent(jsonNode);
            return ResponseTextFilter.normalize(content);

        } catch (IOException e) {
            log.error("IO error communicating with Gemini", e);
            throw new GeminiException("Failed to communicate with Gemini: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for Gemini response", e);
            throw new GeminiException("Interrupted while waiting for Gemini response", e);
        }
    }

    /**
     * Sends a chat request to Gemini and streams the response back via SSE.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @return SseEmitter for streaming the response
     */
    @Override
    public SseEmitter streamChat(String model, List<Message> messages) {
        SseEmitter emitter = new SseEmitter(0L);
        streamChat(model, messages, emitter, null);
        return emitter;
    }

    /**
     * Sends a chat request to Gemini and streams the response to the given emitter.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @param emitter the SseEmitter to write events to
     * @param collector optional StringBuilder to collect message content
     */
    @Override
    public void streamChat(String model, List<Message> messages, SseEmitter emitter, StringBuilder collector) {
        String modelName = (model != null && !model.isBlank()) ? model : config.getModel();
        log.debug("Starting streaming chat with Gemini, model: {}", modelName);

        String jsonBody;
        try {
            jsonBody = buildChatRequest(modelName, messages, true);
        } catch (IOException e) {
            log.error("Failed to serialize request body", e);
            emitter.completeWithError(e);
            return;
        }

        String url = config.getBaseUrl() + "/v1beta/models/" + modelName + ":streamGenerateContent?key=" + config.getApiKey();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Stream the response in a separate thread
        Thread streamThread = new Thread(() -> {
            // Send "processing" status before waiting for Gemini
            try {
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data("processing"));
            } catch (IOException e) {
                log.error("Failed to send processing status", e);
                emitter.completeWithError(e);
                return;
            }

            boolean[] firstToken = {true};
            try {
                HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
                response.body().forEach(line -> {
                    try {
                        // Gemini streaming returns JSON lines
                        if (line.trim().isEmpty()) {
                            return;
                        }

                        JsonNode jsonNode = objectMapper.readTree(line);
                        String content = extractStreamContent(jsonNode);

                        if (content != null && !content.isEmpty()) {
                            // Normalize text to ensure proper spacing and newlines
                            String normalizedContent = ResponseTextFilter.normalize(content);

                            // Send "generating" status on first token
                            if (firstToken[0]) {
                                firstToken[0] = false;
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("status")
                                            .data("generating"));
                                } catch (IOException e) {
                                    log.error("Failed to send generating status", e);
                                }
                            }
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(normalizedContent));
                                // Collect for conversation history
                                if (collector != null) {
                                    collector.append(normalizedContent);
                                }
                            } catch (IOException e) {
                                log.error("Failed to send message chunk", e);
                                emitter.completeWithError(e);
                                return;
                            }
                        }
                    } catch (IOException e) {
                        log.error("Failed to parse Gemini streaming response", e);
                        emitter.completeWithError(e);
                    }
                });

                // Send done status
                try {
                    emitter.send(SseEmitter.event()
                            .name("status")
                            .data("done"));
                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data(""));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("Failed to send done status", e);
                }
            } catch (IOException e) {
                log.error("IO error during streaming from Gemini", e);
                emitter.completeWithError(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted during streaming from Gemini", e);
                emitter.completeWithError(e);
            }
        });
        streamThread.setDaemon(true);
        streamThread.start();
    }

    /**
     * Checks if the Gemini service is available.
     *
     * @return true if Gemini is reachable, false otherwise
     */
    @Override
    public boolean isAvailable() {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            log.warn("Gemini API key is not configured");
            return false;
        }

        try {
            // Use a simple model info request to check availability
            String url = config.getBaseUrl() + "/v1beta/models/" + config.getModel() + "?key=" + config.getApiKey();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofMillis(config.getConnectTimeoutMs()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Gemini is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Builds the JSON request body for Gemini API.
     */
    private String buildChatRequest(String modelName, List<Message> messages, boolean stream) throws IOException {
        // Extract system message if present
        String systemInstruction = null;
        List<Message> nonSystemMessages = new ArrayList<>();

        for (Message msg : messages) {
            if ("system".equals(msg.role())) {
                systemInstruction = msg.content();
            } else {
                nonSystemMessages.add(msg);
            }
        }

        // Build the request body
        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();

        // Add system instruction if present
        if (systemInstruction != null && !systemInstruction.isBlank()) {
            java.util.Map<String, Object> systemInst = new java.util.HashMap<>();
            systemInst.put("parts", java.util.List.of(Map.of("text", systemInstruction)));
            requestBody.put("systemInstruction", systemInst);
        }

        // Build contents array
        List<Object> contents = new ArrayList<>();
        for (Message msg : nonSystemMessages) {
            String role = "user".equals(msg.role()) ? "user" : "model";
            java.util.Map<String, Object> content = new java.util.HashMap<>();
            content.put("role", role);
            content.put("parts", java.util.List.of(Map.of("text", msg.content())));
            contents.add(content);
        }
        requestBody.put("contents", contents);

        // Generation config
        java.util.Map<String, Object> generationConfig = new java.util.HashMap<>();
        generationConfig.put("temperature", config.getTemperature());
        generationConfig.put("maxOutputTokens", config.getMaxTokens());
        requestBody.put("generationConfig", generationConfig);

        return objectMapper.writeValueAsString(requestBody);
    }

    /**
     * Extracts content from a non-streaming Gemini response.
     */
    private String extractContent(JsonNode jsonNode) {
        JsonNode candidates = jsonNode.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            if (parts.isArray() && parts.size() > 0) {
                return parts.get(0).path("text").asText("");
            }
        }
        return "";
    }

    /**
     * Extracts content from a streaming Gemini response chunk.
     */
    private String extractStreamContent(JsonNode jsonNode) {
        JsonNode candidates = jsonNode.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            if (parts.isArray() && parts.size() > 0) {
                return parts.get(0).path("text").asText("");
            }
        }
        return "";
    }

    /**
     * Exception thrown when communication with Gemini fails.
     */
    public static class GeminiException extends RuntimeException {
        public GeminiException(String message) {
            super(message);
        }

        public GeminiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
