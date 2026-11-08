package com.yisy.modules.doraai.controller;

import com.yisy.modules.doraai.dto.TranscribeRequest;
import com.yisy.modules.doraai.dto.TranscribeResponse;
import com.yisy.modules.doraai.dto.TranslateRequest;
import com.yisy.modules.doraai.dto.TranslateResponse;
import com.yisy.modules.doraai.service.TranslationService;
import com.yisy.modules.doraai.service.VoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * REST controller for voice and translation features.
 * <p>
 * Provides endpoints for speech-to-text transcription, text translation,
 * and language-aware chat.
 * </p>
 */
@RestController
@RequestMapping("/api/ai/voice")
public class VoiceController {

    private static final Logger log = LoggerFactory.getLogger(VoiceController.class);

    private final VoiceService voiceService;
    private final TranslationService translationService;

    public VoiceController(VoiceService voiceService, TranslationService translationService) {
        this.voiceService = voiceService;
        this.translationService = translationService;
    }

    /**
     * Transcribe audio to text.
     *
     * @param request the transcription request with base64-encoded audio
     * @return the transcription result
     */
    @PostMapping("/transcribe")
    public ResponseEntity<?> transcribe(@RequestBody TranscribeRequest request) {
        try {
            TranscribeResponse response = voiceService.transcribe(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Transcription failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Transcription failed: " + e.getMessage()));
        }
    }

    /**
     * Translate text to a target language.
     *
     * @param request the translation request with text and target language
     * @return SSE emitter streaming the translated text
     */
    @PostMapping("/translate")
    public SseEmitter translate(@RequestBody TranslateRequest request) {
        log.info("Translation request: target={}, source={}, text length={}", 
                request.getTargetLanguage(), request.getSourceLanguage(), 
                request.getText() != null ? request.getText().length() : 0);
        return translationService.translate(request.getText(), request.getSourceLanguage(), request.getTargetLanguage());
    }

    /**
     * Get the list of supported languages.
     */
    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getLanguages() {
        return ResponseEntity.ok(Map.of(
                "languages", translationService.getSupportedLanguages()
        ));
    }
}
