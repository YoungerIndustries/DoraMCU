package com.yisy.modules.doraai.controller;

import com.yisy.modules.doraai.dto.ChatRequest;
import com.yisy.modules.doraai.service.AiChatService;
import com.yisy.modules.doraai.service.AiProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for AI chat interactions.
 * <p>
 * Provides endpoints for sending chat messages and receiving streaming AI responses.
 * Supports multiple AI providers (Ollama, Gemini).
 * </p>
 */
@RestController
@RequestMapping("/api/ai/chat")
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private final AiChatService aiChatService;
    private final AiProviderFactory providerFactory;

    public AiChatController(AiChatService aiChatService, AiProviderFactory providerFactory) {
        this.aiChatService = aiChatService;
        this.providerFactory = providerFactory;
    }

    /**
     * Sends a chat message and receives a streaming response.
     *
     * @param request the chat request containing the message and optional session ID
     * @return SseEmitter for streaming the AI response
     */
    @PostMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        log.info("Chat request: sessionId={}, mode={}, language={}, inputType={}, hasFileContext={}, provider={}",
                request.getSessionId(), request.getMode(), request.getSourceLanguage(), request.getInputType(),
                request.getFileContext() != null && !request.getFileContext().isBlank(), request.getProviderId());

        String mode = request.getMode() != null ? request.getMode() : "chat";
        String fileContext = request.getFileContext();
        if (fileContext != null && fileContext.isBlank()) {
            fileContext = null;
        }
        return aiChatService.chat(request.getSessionId(), request.getMessage(), mode, request.getProjectId(),
                request.getSourceLanguage(), fileContext, request.getProviderId());
    }

    /**
     * Checks the status of all AI providers.
     *
     * @return status information for all providers
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new HashMap<>();
        List<String> providers = providerFactory.getAvailableProviders();
        status.put("providers", providers);

        for (String providerId : providers) {
            boolean available = providerFactory.isProviderAvailable(providerId);
            status.put(providerId + "Available", available);
        }

        // Keep backward compatibility
        status.put("ollamaAvailable", providerFactory.isProviderAvailable("ollama"));
        status.put("geminiAvailable", providerFactory.isProviderAvailable("gemini"));
        status.put("deepseekAvailable", providerFactory.isProviderAvailable("deepseek"));
        status.put("application", "DoraAI");
        return ResponseEntity.ok(status);
    }

    /**
     * Lists all available AI providers.
     *
     * @return list of available provider IDs
     */
    @GetMapping("/providers")
    public ResponseEntity<List<String>> getProviders() {
        return ResponseEntity.ok(providerFactory.getAvailableProviders());
    }

    /**
     * Clears the conversation history for a session.
     *
     * @param sessionId the session ID to clear
     * @return 200 OK if successful
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> clearConversation(@PathVariable String sessionId) {
        aiChatService.removeConversation(sessionId);
        return ResponseEntity.ok().build();
    }
}
