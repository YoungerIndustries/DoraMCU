package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.model.Conversation;
import com.yisy.modules.doraai.service.AiProvider.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

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
        Conversation conversation = conversations.computeIfAbsent(sessionId, k -> new Conversation(k));

        // Add user message to history (with file context if provided)
        String enrichedMessage = message;
        if (fileContext != null && !fileContext.isBlank()) {
            enrichedMessage = message + "\n\n" + fileContext;
        }
        conversation.addUserMessage(enrichedMessage);

        // Build system prompt
        String systemPrompt = buildSystemPrompt(mode);

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

        // Create the SSE emitter that Spring will manage
        SseEmitter emitter = new SseEmitter(0L);

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
     * Builds the system prompt based on the chat mode.
     */
    private String buildSystemPrompt(String mode) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are DoraAI, a helpful assistant for the DoraMCU space strategy game. ");
        sb.append("You can answer questions about the game, its mechanics, and provide strategic advice.\n\n");

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
        return conversations.get(sessionId);
    }

    /**
     * Removes a conversation.
     */
    public void removeConversation(String sessionId) {
        conversations.remove(sessionId);
        log.info("Removed conversation: {}", sessionId);
    }

}
