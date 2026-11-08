package com.yisy.modules.doraai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraai.config.OllamaConfig;
import com.yisy.modules.doraai.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Service for communicating with the Ollama LLM API.
 * <p>
 * This service provides methods for chat, code generation, and streaming responses
 * via the Ollama HTTP API. It handles both synchronous and streaming (SSE) modes.
 * Implements {@link AiProvider} to allow switching between AI backends.
 * </p>
 */
@Service
public class OllamaService implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    private final OllamaConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaService(OllamaConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderId() {
        return "ollama";
    }

    /**
     * Sends a chat request to Ollama and returns the complete response.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @return the AI response text
     */
    public String chat(String model, List<Message> messages) {
        String modelName = (model != null && !model.isBlank()) ? model : config.getModel();
        log.debug("Sending chat request to Ollama, model: {}, messages: {}", modelName, messages.size());

        return RetryUtil.withRetry(3, 1000L, () -> {
            try {
                Map<String, Object> requestBody = Map.of(
                        "model", modelName,
                        "messages", messages,
                        "stream", false,
                        "options", Map.of(
                                "temperature", config.getTemperature(),
                                "num_predict", config.getMaxTokens()
                        )
                );

                String jsonBody = objectMapper.writeValueAsString(requestBody);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.getBaseUrl() + "/api/chat"))
                        .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    log.error("Ollama chat request failed with status: {}", response.statusCode());
                    throw new OllamaException("Ollama API error: " + response.statusCode());
                }

                JsonNode jsonNode = objectMapper.readTree(response.body());
                String content = jsonNode.path("message").path("content").asText();
                return ResponseTextFilter.normalize(content);

            } catch (IOException e) {
                throw new OllamaException("Failed to communicate with Ollama: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OllamaException("Interrupted while waiting for Ollama response", e);
            }
        });
    }

    /**
     * Sends a chat request to Ollama and streams the response back via SSE.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @return SseEmitter for streaming the response
     */
    public SseEmitter streamChat(String model, List<Message> messages) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 minute timeout
        streamChat(model, messages, emitter, null);
        return emitter;
    }

    /**
     * Sends a chat request to Ollama and streams the response to the given emitter.
     *
     * @param model the model name to use (overrides config if provided)
     * @param messages the conversation messages
     * @param emitter the SseEmitter to write events to
     * @param collector optional StringBuilder to collect message content
     */
    public void streamChat(String model, List<Message> messages, SseEmitter emitter, StringBuilder collector) {
        String modelName = (model != null && !model.isBlank()) ? model : config.getModel();
        log.debug("Starting streaming chat with Ollama, model: {}", modelName);

        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", messages,
                "stream", true,
                "options", Map.of(
                        "temperature", config.getTemperature(),
                        "num_predict", config.getMaxTokens()
                )
        );

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (IOException e) {
            log.error("Failed to serialize request body", e);
            emitter.completeWithError(e);
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + "/api/chat"))
                .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Stream the response in a separate thread
        Thread streamThread = new Thread(() -> {
            // Send "processing" status before waiting for Ollama
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
                        JsonNode jsonNode = objectMapper.readTree(line);
                        String partialContent = jsonNode.path("message").path("content").asText("");
                        boolean done = jsonNode.path("done").asBoolean(false);
                        if (!partialContent.isEmpty()) {
                            // Normalize text to ensure proper spacing and newlines
                            String normalizedContent = ResponseTextFilter.normalize(partialContent);

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

                        // Check if this is the last message
                        if (done) {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("status")
                                        .data("done"));
                            } catch (IOException e) {
                                log.error("Failed to send done status", e);
                            }
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data(""));
                            emitter.complete();
                        }
                    } catch (IOException e) {
                        log.error("Failed to send SSE event", e);
                        emitter.completeWithError(e);
                    }
                });
            } catch (IOException e) {
                log.error("IO error during streaming from Ollama", e);
                emitter.completeWithError(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted during streaming from Ollama", e);
                emitter.completeWithError(e);
            }
        });
        streamThread.setDaemon(true);
        streamThread.start();
    }

    /**
     * Generates text using Ollama's generate endpoint (non-chat mode).
     *
     * @param model the model name to use
     * @param prompt the prompt text
     * @return the generated text
     */
    public String generate(String model, String prompt) {
        String modelName = (model != null && !model.isBlank()) ? model : config.getModel();
        log.debug("Sending generate request to Ollama, model: {}", modelName);

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", modelName,
                    "prompt", prompt,
                    "stream", false,
                    "options", Map.of(
                            "temperature", config.getTemperature(),
                            "num_predict", config.getMaxTokens()
                    )
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/api/generate"))
                    .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Ollama generate request failed with status: {}", response.statusCode());
                throw new OllamaException("Ollama API error: " + response.statusCode());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            String content = jsonNode.path("response").asText();
            return ResponseTextFilter.normalize(content);

        } catch (IOException e) {
            log.error("IO error communicating with Ollama", e);
            throw new OllamaException("Failed to communicate with Ollama: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for Ollama response", e);
            throw new OllamaException("Interrupted while waiting for Ollama response", e);
        }
    }

    /**
     * Transcribes audio using Ollama's Whisper model.
     * <p>
     * Sends audio as base64 WAV in the {@code images} field to Ollama {@code /api/generate}
     * with the Whisper model. Parses response for transcribed text and detected language.
     * </p>
     *
     * @param model the Whisper model name (e.g., "whisper")
     * @param audioBase64 base64-encoded WAV audio data
     * @param languageHint optional language hint (e.g., "en", "zh")
     * @return a TranscriptionResult containing text, detected language, and confidence
     */
    public TranscriptionResult transcribe(String model, String audioBase64, String languageHint) {
        String modelName = (model != null && !model.isBlank()) ? model : "whisper";
        log.debug("Sending transcription request to Ollama Whisper, model: {}", modelName);

        try {
            // Build the request body with audio in the "images" field
            java.util.Map<String, Object> options = new java.util.HashMap<>();
            if (languageHint != null && !languageHint.isBlank()) {
                options.put("language", languageHint);
            }

            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("prompt", "");
            requestBody.put("stream", false);
            requestBody.put("images", java.util.List.of(audioBase64));
            requestBody.put("options", options);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/api/generate"))
                    .timeout(java.time.Duration.ofMillis(config.getReadTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Ollama transcribe request failed with status: {}", response.statusCode());
                throw new OllamaException("Ollama API error: " + response.statusCode());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            String text = jsonNode.path("response").asText("");

            // Whisper may return detected language in the context or metadata
            String detectedLanguage = languageHint != null && !languageHint.isBlank() 
                    ? languageHint : "en";
            double confidence = 0.9; // Whisper doesn't always expose confidence via this API

            return new TranscriptionResult(text, detectedLanguage, confidence);

        } catch (IOException e) {
            log.error("IO error during transcription", e);
            throw new OllamaException("Failed to transcribe audio: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted during transcription", e);
            throw new OllamaException("Interrupted during transcription", e);
        }
    }

    /**
     * Result of a transcription request.
     */
    public record TranscriptionResult(String text, String language, double confidence) {
    }

    /**
     * Gets the ObjectMapper used for JSON serialization.
     */
    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Gets the HttpClient used for HTTP requests.
     */
    HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Gets the base URL of the Ollama API.
     */
    String getBaseUrl() {
        return config.getBaseUrl();
    }

    /**
     * Gets the read timeout in milliseconds.
     */
    long getReadTimeoutMs() {
        return config.getReadTimeoutMs();
    }

    /**
     * Checks if the Ollama service is available.
     *
     * @return true if Ollama is reachable, false otherwise
     */
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/api/tags"))
                    .timeout(java.time.Duration.ofMillis(config.getConnectTimeoutMs()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Ollama is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Exception thrown when communication with Ollama fails.
     */
    public static class OllamaException extends RuntimeException {
        public OllamaException(String message) {
            super(message);
        }

        public OllamaException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
