package com.yisy.modules.doraai.controller;

import com.yisy.modules.doraai.dto.GenerationRequest;
import com.yisy.modules.doraai.service.CodeGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST controller for AI-assisted code generation.
 * <p>
 * Provides endpoints for generating code based on descriptions and context.
 * </p>
 */
@RestController
@RequestMapping("/api/ai/generate")
public class CodeGenerationController {

    private static final Logger log = LoggerFactory.getLogger(CodeGenerationController.class);

    private final CodeGenerationService codeGenerationService;

    public CodeGenerationController(CodeGenerationService codeGenerationService) {
        this.codeGenerationService = codeGenerationService;
    }

    /**
     * Generate code based on a description.
     *
     * @param request the generation request
     * @return SSE emitter streaming the generated code
     */
    @PostMapping
    public SseEmitter generateCode(@RequestBody GenerationRequest request) {
        log.info("Code generation request: projectId={}, language={}",
                request.getProjectId(), request.getLanguage());
        return codeGenerationService.generateCode(request);
    }
}
