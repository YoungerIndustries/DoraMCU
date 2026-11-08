package com.yisy.modules.doraai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraai.config.OpenHandsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Service for communicating with the DoraAI2 OpenHands agent server.
 * <p>
 * This service provides autonomous coding capabilities similar to Junie by
 * calling the DoraAI2 Python server's REST API. It supports multiple interaction
 * modes (chat, code, fast_code, advanced_chat, run_verify, setup) and both
 * synchronous and streaming (SSE) response modes.
 * </p>
 */
@Service
@ConditionalOnProperty(name = "openhands.enabled", havingValue = "true", matchIfMissing = true)
public class OpenHandsServiceProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenHandsServiceProvider.class);

    private final OpenHandsConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenHandsServiceProvider(OpenHandsConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderId() {
        return "openhands";
    }

    @Override
    public String chat(String model, List<Message> messages) {
        // Build a single message string from the conversation history
        String messageText = buildMessageText(messages);
        log.debug("Sending chat request to OpenHands, mode: {}, messages: {}", config.getMode(), messages.size());

        try {
            Map<String, Object> requestBody = Map.of(
                    "message", messageText,
                    "mode", config.getMode()
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/api/chat/blocking"))
                    .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("OpenHands chat request failed with status: {}", response.statusCode());
                throw new OpenHandsException("OpenHands API error: " + response.statusCode());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            String content = jsonNode.path("content").asText();
            return ResponseTextFilter.normalize(content);

        } catch (IOException e) {
            log.error("IO error communicating with OpenHands", e);
            throw new OpenHandsException("Failed to communicate with OpenHands: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for OpenHands response", e);
            throw new OpenHandsException("Interrupted while waiting for OpenHands response", e);
        }
    }

    @Override
    public SseEmitter streamChat(String model, List<Message> messages) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 minute timeout
        streamChat(model, messages, emitter, null);
        return emitter;
    }

    @Override
    public void streamChat(String model, List<Message> messages, SseEmitter emitter, StringBuilder collector) {
        String messageText = buildMessageText(messages);
        log.debug("Sending streaming chat request to OpenHands, mode: {}", config.getMode());

        try {
            Map<String, Object> requestBody = Map.of(
                    "message", messageText,
                    "mode", config.getMode()
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/api/chat"))
                    .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Send the request and process the streaming response
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenApply(HttpResponse::body)
                    .thenAccept(inputStream -> {
                        try (InputStream is = inputStream;
                             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                            String line;
                            StringBuilder dataBuffer = new StringBuilder();

                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("event:")) {
                                    // Event type line, skip for now
                                    continue;
                                } else if (line.startsWith("data:")) {
                                    String data = line.substring(5).trim();
                                    if (data.isEmpty()) {
                                        // Empty data means end of event
                                        if (dataBuffer.length() > 0 && collector != null) {
                                            collector.append(dataBuffer);
                                        }
                                        try {
                                            if (dataBuffer.length() > 0) {
                                                emitter.send(SseEmitter.event()
                                                        .name("message")
                                                        .data(dataBuffer.toString()));
                                            }
                                            dataBuffer.setLength(0);
                                        } catch (IOException e) {
                                            log.error("Failed to send SSE event", e);
                                            break;
                                        }
                                    } else {
                                        dataBuffer.append(data);
                                    }
                                } else if (line.isEmpty()) {
                                    // End of SSE event block
                                    continue;
                                }
                            }

                            // Send any remaining data
                            if (dataBuffer.length() > 0) {
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(dataBuffer.toString()));
                                } catch (IOException e) {
                                    log.error("Failed to send final SSE event", e);
                                }
                            }

                            emitter.complete();

                        } catch (IOException e) {
                            log.error("Error reading streaming response from OpenHands", e);
                            try {
                                emitter.completeWithError(e);
                            } catch (Exception ex) {
                                log.error("Failed to complete emitter with error", ex);
                            }
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Error in streaming request to OpenHands", ex);
                        try {
                            emitter.completeWithError(
                                    new OpenHandsException("Streaming error: " + ex.getMessage()));
                        } catch (Exception ex2) {
                            log.error("Failed to complete emitter with error", ex2);
                        }
                        return null;
                    });

        } catch (IOException e) {
            log.error("IO error building streaming request to OpenHands", e);
            emitter.completeWithError(
                    new OpenHandsException("Failed to build request: " + e.getMessage(), e));
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/health"))
                    .timeout(java.time.Duration.ofMillis(config.getConnectTimeoutMs()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            log.debug("OpenHands server is not available: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    /**
     * Builds a single message text from the conversation message list.
     * <p>
     * OpenHands expects a single message string, so we concatenate all messages
     * with their roles as prefixes.
     * </p>
     *
     * @param messages the conversation messages
     * @return a formatted message string
     */
    private String buildMessageText(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        if (messages.size() == 1) {
            return messages.get(0).content();
        }

        StringBuilder sb = new StringBuilder();
        for (Message msg : messages) {
            String role = msg.role();
            if ("system".equals(role)) {
                sb.append("System: ").append(msg.content()).append("\n\n");
            } else if ("user".equals(role)) {
                sb.append("User: ").append(msg.content()).append("\n\n");
            } else if ("assistant".equals(role)) {
                sb.append("Assistant: ").append(msg.content()).append("\n\n");
            } else {
                sb.append(role).append(": ").append(msg.content()).append("\n\n");
            }
        }

        // Remove trailing newlines
        String result = sb.toString().trim();
        return result;
    }

    /**
     * Exception thrown when communication with OpenHands fails.
     */
    public static class OpenHandsException extends RuntimeException {
        public OpenHandsException(String message) {
            super(message);
        }

        public OpenHandsException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
