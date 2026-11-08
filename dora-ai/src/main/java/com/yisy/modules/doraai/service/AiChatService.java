package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.model.Conversation;
import com.yisy.modules.doraai.service.AiProvider.Message;
import com.yisy.modules.doraai.util.LanguageUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Service that orchestrates chat interactions between the user and AI providers.
 * <p>
 * Maintains conversation history per session and routes queries to AI providers
 * (Ollama, DeepSeek, Gemini) via AiProviderFactory.
 * </p>
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final AiProviderFactory providerFactory;
    private final Cache<String, Conversation> conversations = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();

    /** SSE emitter timeout in seconds (5 minutes). */
    private static final long SSE_TIMEOUT_SECONDS = 300;

    public AiChatService(AiProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    /**
     * Sends a chat message and returns a streaming response.
     *
     * @param sessionId the conversation session ID (generated if null)
     * @param message the user message
     * @param mode the chat mode (chat, code-gen, bug-detect)
     * @param projectId optional project ID for code context
     * @param sourceLanguage optional source language for language-aware responses
     * @param fileContext optional pre-fetched file contents for additional context
     * @param providerId optional AI provider ID (e.g., "ollama", "gemini"); defaults to configured default
     * @return SseEmitter for streaming the AI response
     */
    public SseEmitter chat(String sessionId, String message, String mode, String projectId, String sourceLanguage, String fileContext, String providerId) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        log.info("Chat request: sessionId={}, mode={}, hasFileContext={}, provider={}", sessionId, mode,
                fileContext != null && !fileContext.isBlank(), providerId);

        // Get or create conversation
        Conversation conversation = conversations.get(sessionId, k -> new Conversation(k));

        // Add user message to history (with file context if provided)
        String enrichedMessage = message;
        if (fileContext != null && !fileContext.isBlank()) {
            enrichedMessage = message + "\n\n" + fileContext;
        }
        conversation.addUserMessage(enrichedMessage);

        // Build system prompt (with language awareness if sourceLanguage is provided)
        String systemPrompt = buildSystemPrompt(mode, sourceLanguage);

        // Build message list
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));

        // Add conversation history (last 10 messages to avoid context overflow)
        List<Conversation.Message> history = conversation.getMessages();
        int startIdx = Math.max(0, history.size() - 10);
        for (int i = startIdx; i < history.size(); i++) {
            Conversation.Message m = history.get(i);
            messages.add(new Message(m.getRole(), m.getContent()));
        }

        // Create the SSE emitter with a timeout
        SseEmitter emitter = new SseEmitter(TimeUnit.SECONDS.toMillis(SSE_TIMEOUT_SECONDS));

        // Collect the full response to save to conversation history
        StringBuilder fullResponse = new StringBuilder();
        emitter.onCompletion(() -> {
            if (fullResponse.length() > 0) {
                conversation.addAiMessage(fullResponse.toString());
                log.debug("Saved AI response to conversation history: {} messages", conversation.getMessageCount());
            }
        });

        // Send "receiving" status immediately
        try {
            emitter.send(SseEmitter.event()
                    .name("status")
                    .data("receiving"));
        } catch (IOException e) {
            log.error("Failed to send receiving status", e);
        }

        // Get the AI provider with automatic fallback if the requested one is unavailable
        AiProvider provider = providerFactory.getProviderWithFallback(providerId);
        log.info("Routing chat for session {} to provider: {}", sessionId, provider.getProviderId());
        try {
            provider.streamChat(null, messages, emitter, fullResponse);
        } catch (Exception e) {
            log.error("Failed to stream chat using provider: " + provider.getProviderId(), e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("Error communicating with AI provider (" + provider.getProviderId() + "): " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioException) {
                log.error("Failed to send error event to SSE emitter", ioException);
            }
        }

        return emitter;
    }

    /**
     * Sends a chat message and returns a streaming response (simple version).
     *
     * @param sessionId the conversation session ID (generated if null)
     * @param message the user message
     * @param mode the chat mode (chat, code-gen, bug-detect)
     * @return SseEmitter for streaming the AI response
     */
    public SseEmitter chat(String sessionId, String message, String mode) {
        return chat(sessionId, message, mode, null, null, null, null);
    }

    /**
     * Sends a chat message with a custom system prompt and returns a streaming response.
     *
     * @param sessionId the conversation session ID (generated if null)
     * @param message the user message
     * @param mode the chat mode (chat, code-gen, bug-detect)
     * @param systemPrompt custom system prompt (overrides the default)
     * @return SseEmitter for streaming the AI response
     */
    public SseEmitter chatWithSystemPrompt(String sessionId, String message, String mode, String systemPrompt) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        log.info("Chat with system prompt: sessionId={}, mode={}", sessionId, mode);

        // Get or create conversation
        Conversation conversation = conversations.get(sessionId, k -> new Conversation(k));
        conversation.addUserMessage(message);

        // Build message list with custom system prompt
        List<Message> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new Message("system", systemPrompt));
        } else {
            messages.add(new Message("system", buildSystemPrompt(mode, null)));
        }

        // Add conversation history (last 10 messages to avoid context overflow)
        List<Conversation.Message> history = conversation.getMessages();
        int startIdx = Math.max(0, history.size() - 10);
        for (int i = startIdx; i < history.size(); i++) {
            Conversation.Message m = history.get(i);
            messages.add(new Message(m.getRole(), m.getContent()));
        }

        // Create the SSE emitter with a timeout
        SseEmitter emitter = new SseEmitter(TimeUnit.SECONDS.toMillis(SSE_TIMEOUT_SECONDS));

        // Collect the full response to save to conversation history
        StringBuilder fullResponse = new StringBuilder();
        emitter.onCompletion(() -> {
            if (fullResponse.length() > 0) {
                conversation.addAiMessage(fullResponse.toString());
                log.debug("Saved AI response to conversation history: {} messages", conversation.getMessageCount());
            }
        });

        // Send "receiving" status immediately
        try {
            emitter.send(SseEmitter.event()
                    .name("status")
                    .data("receiving"));
        } catch (IOException e) {
            log.error("Failed to send receiving status", e);
        }

        // Get the AI provider with automatic fallback
        AiProvider provider = providerFactory.getProviderWithFallback(null);
        log.info("Routing chat for session {} to provider: {}", sessionId, provider.getProviderId());
        try {
            provider.streamChat(null, messages, emitter, fullResponse);
        } catch (Exception e) {
            log.error("Failed to stream chat using provider: " + provider.getProviderId(), e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("Error communicating with AI provider (" + provider.getProviderId() + "): " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioException) {
                log.error("Failed to send error event to SSE emitter", ioException);
            }
        }

        return emitter;
    }

    /**
     * Builds the system prompt based on the chat mode and optional source language.
     */
    private String buildSystemPrompt(String mode, String sourceLanguage) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are DoraAI, a helpful assistant for the DoraMCU space strategy game. ");
        sb.append("You can answer questions about the game, its mechanics, and provide strategic advice.\n\n");

        // Language-aware instruction
        if (sourceLanguage != null && !sourceLanguage.isBlank()) {
            String langName = LanguageUtil.toLanguageName(sourceLanguage);
            sb.append("## Language\n");
            sb.append("Respond in " + langName + ". The user's input is in " + langName + ".\n\n");
        }

        // Mode-specific instructions
        if ("code-gen".equals(mode)) {
            sb.append("## Task\n");
            sb.append("The user wants you to generate code. Provide complete, working code with proper formatting and comments.\n");
        } else if ("bug-detect".equals(mode)) {
            sb.append("## Task\n");
            sb.append("The user wants you to analyze code for potential bugs. Look for common issues like null pointer exceptions, ");
            sb.append("resource leaks, thread safety issues, and logic errors. Provide specific line references and suggested fixes.\n");
        } else {
            sb.append("## Task\n");
            sb.append("Answer the user's question. Be concise but thorough. Use examples when helpful.\n");
        }

        return sb.toString();
    }


    /**
     * Gets the conversation for a session ID.
     */
    public Conversation getConversation(String sessionId) {
        return conversations.getIfPresent(sessionId);
    }

    /**
     * Removes a conversation.
     */
    public void removeConversation(String sessionId) {
        conversations.invalidate(sessionId);
        log.info("Removed conversation: {}", sessionId);
    }

    /**
     * Caffeine cache handles eviction automatically via expireAfterAccess.
     * This method is retained for manual cleanup if needed.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRateString = "${ai.chat.cleanup-interval-ms:600000}")
    public void evictIdleConversations() {
        conversations.cleanUp();
        log.debug("Conversation cache cleanup: {} active conversations", conversations.estimatedSize());
    }

    /**
     * Returns the number of active conversations.
     */
    public int getConversationCount() {
        return (int) conversations.estimatedSize();
    }

}
