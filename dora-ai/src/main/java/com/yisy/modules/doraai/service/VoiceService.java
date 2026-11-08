package com.yisy.modules.doraai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraai.config.OllamaConfig;
import com.yisy.modules.doraai.dto.TranscribeRequest;
import com.yisy.modules.doraai.dto.TranscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service for speech-to-text transcription using Ollama Whisper model.
 * <p>
 * Sends audio data to Ollama's Whisper model for transcription.
 * </p>
 */
@Service
public class VoiceService {

    private static final Logger log = LoggerFactory.getLogger(VoiceService.class);

    private final OllamaConfig ollamaConfig;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public VoiceService(OllamaConfig ollamaConfig, HttpClient httpClient) {
        this.ollamaConfig = ollamaConfig;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Transcribe audio to text using Ollama Whisper.
     *
     * @param request the transcription request with base64-encoded audio
     * @return the transcription result
     */
    public TranscribeResponse transcribe(TranscribeRequest request) {
        if (request.getAudioBase64() == null || request.getAudioBase64().isBlank()) {
            throw new IllegalArgumentException("Audio data is required");
        }

        log.info("Transcribing audio: {} bytes, language hint: {}",
                request.getAudioBase64().length(),
                request.getLanguageHint());

        try {
            // Build the request body for Ollama Whisper
            Map<String, Object> body = Map.of(
                    "model", ollamaConfig.getWhisperModel(),
                    "prompt", request.getLanguageHint() != null ?
                            "Transcribe in " + request.getLanguageHint() : "",
                    "images", java.util.List.of(request.getAudioBase64())
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaConfig.getBaseUrl() + "/api/generate"))
                    .timeout(java.time.Duration.ofMillis(ollamaConfig.getReadTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Ollama transcription failed: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Transcription failed with status " + response.statusCode());
            }

            String text = extractTextFromResponse(response.body());
            String language = detectLanguage(text, request.getLanguageHint());
            return new TranscribeResponse(text, language);

        } catch (Exception e) {
            log.error("Transcription failed", e);
            throw new RuntimeException("Transcription failed: " + e.getMessage(), e);
        }
    }

    private String extractTextFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.has("response")) {
                return root.get("response").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to parse Ollama response: {}", e.getMessage());
        }
        return response;
    }

    private String detectLanguage(String text, String hint) {
        if (hint != null && !hint.isBlank()) {
            return hint;
        }
        // Simple language detection based on common characters
        if (text.matches(".*[\\u4e00-\\u9fff].*")) return "zh";
        if (text.matches(".*[\\u3040-\\u309f\\u30a0-\\u30ff].*")) return "ja";
        if (text.matches(".*[\\uac00-\\ud7af].*")) return "ko";
        if (text.matches(".*[\\u0400-\\u04ff].*")) return "ru";
        if (text.matches(".*[\\u0600-\\u06ff].*")) return "ar";
        return "en";
    }
}
