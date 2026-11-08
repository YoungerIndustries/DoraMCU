package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.service.AiProvider.Message;
import com.yisy.modules.doraai.util.LanguageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for language-aware translation and multilingual chat.
 * <p>
 * Builds language-aware system prompts and routes translation requests
 * through the AI provider.
 * </p>
 */
@Service
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    private final AiProviderFactory providerFactory;

    /** Supported languages for translation. */
    public static final List<String> SUPPORTED_LANGUAGES = LanguageUtil.SUPPORTED_LANGUAGES;

    public TranslationService(AiProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    /**
     * Translate text to a target language.
     *
     * @param text the text to translate
     * @param targetLanguage the target language code
     * @return SSE emitter streaming the translated text
     */
    public SseEmitter translate(String text, String targetLanguage) {
        return translate(text, null, targetLanguage);
    }

    /**
     * Translate text to a target language with optional source language hint.
     *
     * @param text the text to translate
     * @param sourceLanguage optional source language code for better translation quality
     * @param targetLanguage the target language code
     * @return SSE emitter streaming the translated text
     */
    public SseEmitter translate(String text, String sourceLanguage, String targetLanguage) {
        if (text == null || text.isBlank()) {
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send(SseEmitter.event().name("error").data("Text is required"));
            } catch (IOException e) {
                log.error("Failed to send error", e);
            }
            emitter.complete();
            return emitter;
        }

        String systemPrompt = buildTranslationPrompt(sourceLanguage, targetLanguage);
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", text));

        SseEmitter emitter = new SseEmitter();
        AiProvider provider = providerFactory.getProviderWithFallback(null);

        try {
            emitter.send(SseEmitter.event().name("status").data("translating"));
        } catch (IOException e) {
            log.error("Failed to send status", e);
        }

        StringBuilder fullResponse = new StringBuilder();
        emitter.onCompletion(() -> {
            log.debug("Translation complete: {} chars", fullResponse.length());
        });

        try {
            provider.streamChat(null, messages, emitter, fullResponse);
        } catch (Exception e) {
            log.error("Translation failed", e);
            try {
                emitter.send(SseEmitter.event().name("error").data("Translation failed: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioEx) {
                log.error("Failed to send error", ioEx);
            }
        }

        return emitter;
    }

    /**
     * Chat with language awareness — the AI responds in the specified language.
     *
     * @param message the user message
     * @param sourceLanguage the source language code
     * @return SSE emitter streaming the response
     */
    public SseEmitter chatInLanguage(String message, String sourceLanguage) {
        String systemPrompt = buildLanguageAwarePrompt(sourceLanguage);
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", message));

        SseEmitter emitter = new SseEmitter();
        AiProvider provider = providerFactory.getProviderWithFallback(null);

        try {
            emitter.send(SseEmitter.event().name("status").data("responding"));
        } catch (IOException e) {
            log.error("Failed to send status", e);
        }

        StringBuilder fullResponse = new StringBuilder();
        emitter.onCompletion(() -> {
            log.debug("Language-aware chat complete: {} chars", fullResponse.length());
        });

        try {
            provider.streamChat(null, messages, emitter, fullResponse);
        } catch (Exception e) {
            log.error("Language-aware chat failed", e);
            try {
                emitter.send(SseEmitter.event().name("error").data("Chat failed: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioEx) {
                log.error("Failed to send error", ioEx);
            }
        }

        return emitter;
    }

    private String buildTranslationPrompt(String sourceLanguage, String targetLanguage) {
        String targetName = LanguageUtil.toLanguageName(targetLanguage);
        StringBuilder sb = new StringBuilder();
        sb.append("You are a professional translator. Translate the following text to " + targetName);
        if (sourceLanguage != null && !sourceLanguage.isBlank()) {
            String sourceName = LanguageUtil.toLanguageName(sourceLanguage);
            sb.append(" from " + sourceName);
        }
        sb.append(". Provide only the translated text, without explanations or notes. ");
        sb.append("Preserve the original formatting and tone.");
        return sb.toString();
    }

    private String buildLanguageAwarePrompt(String sourceLanguage) {
        String langName = LanguageUtil.toLanguageName(sourceLanguage);
        return "You are DoraAI, a helpful assistant. Respond in " + langName +
                ". Be concise but thorough. Use examples when helpful.";
    }

    /**
     * Get the list of supported languages.
     */
    public List<String> getSupportedLanguages() {
        return List.copyOf(SUPPORTED_LANGUAGES);
    }
}
