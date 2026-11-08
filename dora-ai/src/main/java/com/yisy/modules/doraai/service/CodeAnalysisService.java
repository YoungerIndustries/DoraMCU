package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.controller.ProjectController;
import com.yisy.modules.doraai.dto.AnalysisRequest;
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
 * Service for AI-assisted code analysis (bug detection and code review).
 * <p>
 * Builds prompts that include project context and code snippets,
 * then uses the AI provider to analyze for bugs, anti-patterns, and improvements.
 * </p>
 */
@Service
public class CodeAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CodeAnalysisService.class);

    private final AiProviderFactory providerFactory;
    private final ProjectController projectController;

    public CodeAnalysisService(AiProviderFactory providerFactory, ProjectController projectController) {
        this.providerFactory = providerFactory;
        this.projectController = projectController;
    }

    /**
     * Analyze code for potential bugs.
     *
     * @param request the analysis request with code and/or file paths
     * @return SSE emitter streaming the analysis result
     */
    public SseEmitter detectBugs(AnalysisRequest request) {
        String systemPrompt = buildBugDetectionPrompt(request);
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", buildAnalysisContent(request)));

        SseEmitter emitter = new SseEmitter();
        AiProvider provider = providerFactory.getProviderWithFallback(null);

        try {
            emitter.send(SseEmitter.event().name("status").data("analyzing"));
        } catch (IOException e) {
            log.error("Failed to send status", e);
        }

        StringBuilder fullResponse = new StringBuilder();
        emitter.onCompletion(() -> {
            log.debug("Bug detection complete: {} chars", fullResponse.length());
        });

        try {
            provider.streamChat(null, messages, emitter, fullResponse);
        } catch (Exception e) {
            log.error("Bug detection failed", e);
            try {
                emitter.send(SseEmitter.event().name("error").data("Analysis failed: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioEx) {
                log.error("Failed to send error", ioEx);
            }
        }

        return emitter;
    }

    /**
     * Perform a general code review.
     *
     * @param request the analysis request
     * @return SSE emitter streaming the review result
     */
    public SseEmitter reviewCode(AnalysisRequest request) {
        String systemPrompt = buildCodeReviewPrompt(request);
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", buildAnalysisContent(request)));

        SseEmitter emitter = new SseEmitter();
        AiProvider provider = providerFactory.getProviderWithFallback(null);

        try {
            emitter.send(SseEmitter.event().name("status").data("reviewing"));
        } catch (IOException e) {
            log.error("Failed to send status", e);
        }

        StringBuilder fullResponse = new StringBuilder();
        emitter.onCompletion(() -> {
            log.debug("Code review complete: {} chars", fullResponse.length());
        });

        try {
            provider.streamChat(null, messages, emitter, fullResponse);
        } catch (Exception e) {
            log.error("Code review failed", e);
            try {
                emitter.send(SseEmitter.event().name("error").data("Review failed: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioEx) {
                log.error("Failed to send error", ioEx);
            }
        }

        return emitter;
    }

    private String buildBugDetectionPrompt(AnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert code analyst specializing in bug detection.\n\n");
        sb.append("Analyze the provided code for potential bugs, including:\n");
        sb.append("- Null pointer exceptions and uninitialized variables\n");
        sb.append("- Resource leaks (unclosed streams, connections, etc.)\n");
        sb.append("- Thread safety issues and race conditions\n");
        sb.append("- Logic errors and off-by-one bugs\n");
        sb.append("- Exception handling issues\n");
        sb.append("- Security vulnerabilities\n\n");
        sb.append("For each issue found, provide:\n");
        sb.append("1. The specific line or code section\n");
        sb.append("2. A description of the bug\n");
        sb.append("3. Severity (Critical/High/Medium/Low)\n");
        sb.append("4. A suggested fix\n\n");

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            sb.append("Focus area: ").append(request.getDescription()).append("\n\n");
        }

        return sb.toString();
    }

    private String buildCodeReviewPrompt(AnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an experienced software engineer performing a code review.\n\n");
        sb.append("Review the provided code for:\n");
        sb.append("- Code quality and readability\n");
        sb.append("- Design patterns and best practices\n");
        sb.append("- Performance considerations\n");
        sb.append("- Maintainability and testability\n");
        sb.append("- Potential improvements and refactoring opportunities\n\n");
        sb.append("Provide constructive feedback with specific examples.\n\n");

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            sb.append("Focus area: ").append(request.getDescription()).append("\n\n");
        }

        return sb.toString();
    }

    private String buildAnalysisContent(AnalysisRequest request) {
        StringBuilder content = new StringBuilder();

        // Add project context if available
        if (request.getProjectId() != null && !request.getProjectId().isBlank()) {
            CodeIndex index = projectController.getIndex(request.getProjectId());
            if (index != null) {
                // Add relevant file contents
                if (request.getFilePaths() != null && !request.getFilePaths().isEmpty()) {
                    for (String filePath : request.getFilePaths()) {
                        String fileContent = index.getFileContent(filePath);
                        if (fileContent != null) {
                            content.append("File: ").append(filePath).append("\n");
                            content.append("```\n").append(fileContent).append("\n```\n\n");
                        }
                    }
                } else {
                    // Add project overview
                    content.append(index.getProjectOverview()).append("\n\n");
                }
            }
        }

        // Add inline code if provided
        if (request.getCode() != null && !request.getCode().isBlank()) {
            content.append("Code to analyze:\n```\n").append(request.getCode()).append("\n```\n");
        }

        return content.toString();
    }
}
