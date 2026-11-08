package com.yisy.modules.doraai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraai.config.DeepSeekConfig;
import com.yisy.modules.doraai.util.RetryUtil;
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
 * Service for communicating with the DeepSeek API.
 * <p>
 * This service provides methods for chat and streaming responses via the DeepSeek API.
 * The API is OpenAI-compatible, using the /v1/chat/completions endpoint.
 * It implements the {@link AiProvider} interface to allow switching between AI backends.
 * </p>
 */
@Service
@ConditionalOnProperty(prefix = "deepseek", name = "enabled", havingValue = "true")
public class DeepSeekService implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);

    private final DeepSeekConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeepSeekService(DeepSeekConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderId() {
        return "deepseek";
    }

    /**
     * Sends a chat request to DeepSeek and returns the complete response.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @return the AI response text
     */
    @Override
    public String chat(String model, List<Message> messages) {
        String modelName = (model != null && !model.isBlank()) ? model : config.getModel();
        log.debug("Sending chat request to DeepSeek, model: {}, messages: {}", modelName, messages.size());

        return RetryUtil.withRetry(3, 1000L, () -> {
            try {
                String jsonBody = buildChatRequest(modelName, messages, false);
                String url = config.getBaseUrl() + "/v1/chat/completions";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + config.getApiKey())
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    log.error("DeepSeek chat request failed with status: {}", response.statusCode());
                    throw new DeepSeekException("DeepSeek API error: " + response.statusCode() + " - " + response.body());
                }

                JsonNode jsonNode = objectMapper.readTree(response.body());
                String content = extractContent(jsonNode);
                return ResponseTextFilter.normalize(content);

            } catch (IOException e) {
                throw new DeepSeekException("Failed to communicate with DeepSeek: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DeepSeekException("Interrupted while waiting for DeepSeek response", e);
            }
        });
    }

    /**
     * Sends a chat request to DeepSeek and streams the response back via SSE.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @return SseEmitter for streaming the response
     */
    @Override
    public SseEmitter streamChat(String model, List<Message> messages) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 minute timeout
        streamChat(model, messages, emitter, null);
        return emitter;
    }

    /**
     * Sends a chat request to DeepSeek and streams the response to the given emitter.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @param emitter the SseEmitter to write events to
     * @param collector optional StringBuilder to collect message content
     */
    @Override
    public void streamChat(String model, List<Message> messages, SseEmitter emitter, StringBuilder collector) {
        String modelName = (model != null && !model.isBlank()) ? model : config.getModel();
        log.debug("Starting streaming chat with DeepSeek, model: {}", modelName);

        String jsonBody;
        try {
            jsonBody = buildChatRequest(modelName, messages, true);
        } catch (IOException e) {
            log.error("Failed to serialize request body", e);
            emitter.completeWithError(e);
            return;
        }

        String url = config.getBaseUrl() + "/v1/chat/completions";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Stream the response in a separate thread
        Thread streamThread = new Thread(() -> {
            // Send "processing" status before waiting for DeepSeek
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
                        // DeepSeek streaming returns SSE format: data: {...}
                        if (line.trim().isEmpty()) {
                            return;
                        }

                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if (data.equals("[DONE]")) {
                                return;
                            }

                            JsonNode jsonNode = objectMapper.readTree(data);
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
                        }
                    } catch (IOException e) {
                        log.error("Failed to parse DeepSeek streaming response", e);
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
                log.error("IO error during streaming from DeepSeek", e);
                emitter.completeWithError(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted during streaming from DeepSeek", e);
                emitter.completeWithError(e);
            }
        });
        streamThread.setDaemon(true);
        streamThread.start();
    }

    /**
     * Checks if the DeepSeek service is available.
     *
     * @return true if DeepSeek is reachable, false otherwise
     */
    @Override
    public boolean isAvailable() {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            log.warn("DeepSeek API key is not configured");
            return false;
        }

        try {
            // Use a simple models list request to check availability
            String url = config.getBaseUrl() + "/v1/models";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofMillis(config.getConnectTimeoutMs()))
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.warn("DeepSeek is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Builds the JSON request body for DeepSeek API (OpenAI-compatible format).
     */
    private String buildChatRequest(String modelName, List<Message> messages, boolean stream) throws IOException {
        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("model", modelName);

        // Build messages array (DeepSeek uses OpenAI-compatible format)
        List<Object> msgList = new ArrayList<>();
        for (Message msg : messages) {
            java.util.Map<String, String> m = new java.util.HashMap<>();
            m.put("role", msg.role());
            m.put("content", msg.content());
            msgList.add(m);
        }
        requestBody.put("messages", msgList);

        // Generation parameters
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens());
        requestBody.put("stream", stream);

        return objectMapper.writeValueAsString(requestBody);
    }

    /**
     * Extracts content from a non-streaming DeepSeek response.
     */
    private String extractContent(JsonNode jsonNode) {
        JsonNode choices = jsonNode.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode message = choices.get(0).path("message");
            return message.path("content").asText("");
        }
        return "";
    }

    /**
     * Extracts content from a streaming DeepSeek response chunk.
     */
    private String extractStreamContent(JsonNode jsonNode) {
        JsonNode choices = jsonNode.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode delta = choices.get(0).path("delta");
            return delta.path("content").asText("");
        }
        return "";
    }

    /**
     * Exception thrown when communication with DeepSeek fails.
     */
    public static class DeepSeekException extends RuntimeException {
        public DeepSeekException(String message) {
            super(message);
        }

        public DeepSeekException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
