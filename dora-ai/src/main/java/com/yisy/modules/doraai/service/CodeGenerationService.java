package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.controller.ProjectController;
import com.yisy.modules.doraai.dto.GenerationRequest;
import com.yisy.modules.doraai.model.CodeIndex;
import com.yisy.modules.doraai.service.AiProvider.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for AI-assisted code generation.
 * <p>
 * Builds prompts that include project context and generates code
 * based on user descriptions, using the AI provider.
 * </p>
 */
@Service
public class CodeGenerationService {

    private static final Logger log = LoggerFactory.getLogger(CodeGenerationService.class);

    private final AiProviderFactory providerFactory;
    private final ProjectController projectController;

    public CodeGenerationService(AiProviderFactory providerFactory, ProjectController projectController) {
        this.providerFactory = providerFactory;
        this.projectController = projectController;
    }

    /**
     * Generate code based on a description.
     *
     * @param request the generation request with description and optional context
     * @return SSE emitter streaming the generated code
     */
    public SseEmitter generateCode(GenerationRequest request) {
        String systemPrompt = buildGenerationPrompt(request);
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", buildGenerationContent(request)));

        SseEmitter emitter = new SseEmitter();
        AiProvider provider = providerFactory.getProviderWithFallback(null);

        try {
            emitter.send(SseEmitter.event().name("status").data("generating"));
        } catch (IOException e) {
            log.error("Failed to send status", e);
        }

        StringBuilder fullResponse = new StringBuilder();
        emitter.onCompletion(() -> {
            log.debug("Code generation complete: {} chars", fullResponse.length());
        });

        try {
            provider.streamChat(null, messages, emitter, fullResponse);
        } catch (Exception e) {
            log.error("Code generation failed", e);
            try {
                emitter.send(SseEmitter.event().name("error").data("Generation failed: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioEx) {
                log.error("Failed to send error", ioEx);
            }
        }

        return emitter;
    }

    private String buildGenerationPrompt(GenerationRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert software developer generating high-quality code.\n\n");
        sb.append("Generate complete, working code that follows best practices:\n");
        sb.append("- Use proper naming conventions\n");
        sb.append("- Include error handling\n");
        sb.append("- Add meaningful comments\n");
        sb.append("- Follow SOLID principles\n");
        sb.append("- Write clean, readable code\n\n");

        if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
            sb.append("Target language: ").append(request.getLanguage()).append("\n\n");
        }

        sb.append("Provide the code in markdown code blocks with the language specified.\n");
        return sb.toString();
    }

    private String buildGenerationContent(GenerationRequest request) {
        StringBuilder content = new StringBuilder();

        // Add project context if available
        if (request.getProjectId() != null && !request.getProjectId().isBlank()) {
            CodeIndex index = projectController.getIndex(request.getProjectId());
            if (index != null) {
                content.append("Project context:\n");
                content.append(index.getProjectOverview()).append("\n\n");

                // Add relevant context files
                if (request.getContext() != null && !request.getContext().isBlank()) {
                    String relevant = index.getRelevantContext(request.getContext(), 3);
                    if (!relevant.isBlank()) {
                        content.append("Relevant files:\n").append(relevant).append("\n");
                    }
                }
            }
        }

        // Add the description
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            content.append("Task: ").append(request.getDescription()).append("\n");
        }

        // Add additional context
        if (request.getContext() != null && !request.getContext().isBlank()) {
            content.append("Additional context: ").append(request.getContext()).append("\n");
        }

        return content.toString();
    }
}
