package com.yisy.modules.doraai.controller;

import com.yisy.modules.doraai.dto.AnalysisRequest;
import com.yisy.modules.doraai.service.CodeAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST controller for AI-assisted code analysis.
 * <p>
 * Provides endpoints for bug detection and code review.
 * </p>
 */
@RestController
@RequestMapping("/api/ai/analyze")
public class CodeAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(CodeAnalysisController.class);

    private final CodeAnalysisService codeAnalysisService;

    public CodeAnalysisController(CodeAnalysisService codeAnalysisService) {
        this.codeAnalysisService = codeAnalysisService;
    }

    /**
     * Analyze code for potential bugs.
     *
     * @param request the analysis request
     * @return SSE emitter streaming the analysis result
     */
    @PostMapping("/bugs")
    public SseEmitter detectBugs(@RequestBody AnalysisRequest request) {
        log.info("Bug detection request: projectId={}, files={}",
                request.getProjectId(),
                request.getFilePaths() != null ? request.getFilePaths().size() : 0);
        return codeAnalysisService.detectBugs(request);
    }

    /**
     * Perform a general code review.
     *
     * @param request the analysis request
     * @return SSE emitter streaming the review result
     */
    @PostMapping("/review")
    public SseEmitter reviewCode(@RequestBody AnalysisRequest request) {
        log.info("Code review request: projectId={}, files={}",
                request.getProjectId(),
                request.getFilePaths() != null ? request.getFilePaths().size() : 0);
        return codeAnalysisService.reviewCode(request);
    }
}
