package com.yisy.modules.doraai.controller;

import com.yisy.modules.doraai.dto.ProjectRequest;
import com.yisy.modules.doraai.model.CodeIndex;
import com.yisy.modules.doraai.model.CodeSymbol;
import com.yisy.modules.doraai.service.ProjectScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for project registration and code indexing.
 * <p>
 * Allows users to register project directories, scan them for source files,
 * and query the code index for symbols and project structure.
 * </p>
 */
@RestController
@RequestMapping("/api/ai/projects")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectScanner projectScanner;

    /** Map of project IDs to their code indices. */
    private final Map<String, CodeIndex> projectIndices = new HashMap<>();

    public ProjectController(ProjectScanner projectScanner) {
        this.projectScanner = projectScanner;
    }

    /**
     * Register and scan a new project.
     *
     * @param request the project name and path
     * @return the project ID and scan statistics
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerProject(@RequestBody ProjectRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Project name is required"));
        }
        if (request.getPath() == null || request.getPath().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Project path is required"));
        }

        String projectId = request.getName().toLowerCase().replace(" ", "-");
        CodeIndex index = new CodeIndex();

        try {
            int fileCount = projectScanner.scanDirectory(request.getPath(), index);
            projectIndices.put(projectId, index);

            log.info("Registered project '{}' with {} files and {} symbols",
                    projectId, index.getFileCount(), index.getSymbolCount());

            return ResponseEntity.ok(Map.of(
                    "projectId", projectId,
                    "name", request.getName(),
                    "path", request.getPath(),
                    "fileCount", index.getFileCount(),
                    "symbolCount", index.getSymbolCount()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to scan project: {}", request.getPath(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to scan project: " + e.getMessage()));
        }
    }

    /**
     * List all registered projects.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listProjects() {
        List<Map<String, Object>> projects = new ArrayList<>();
        projectIndices.forEach((id, index) -> {
            Map<String, Object> info = new HashMap<>();
            info.put("projectId", id);
            info.put("fileCount", index.getFileCount());
            info.put("symbolCount", index.getSymbolCount());
            projects.add(info);
        });
        return ResponseEntity.ok(projects);
    }

    /**
     * Get project overview (structure summary).
     */
    @GetMapping("/{projectId}/overview")
    public ResponseEntity<Map<String, Object>> getProjectOverview(@PathVariable String projectId) {
        CodeIndex index = projectIndices.get(projectId);
        if (index == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "projectId", projectId,
                "overview", index.getProjectOverview(),
                "fileCount", index.getFileCount(),
                "symbolCount", index.getSymbolCount()
        ));
    }

    /**
     * Search symbols in a project.
     */
    @GetMapping("/{projectId}/search")
    public ResponseEntity<List<Map<String, Object>>> searchSymbols(
            @PathVariable String projectId,
            @RequestParam String query) {
        CodeIndex index = projectIndices.get(projectId);
        if (index == null) {
            return ResponseEntity.notFound().build();
        }
        List<CodeSymbol> symbols = index.searchSymbols(query);
        List<Map<String, Object>> results = new ArrayList<>();
        for (CodeSymbol symbol : symbols) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", symbol.getType().name());
            map.put("name", symbol.getName());
            map.put("signature", symbol.getSignature());
            map.put("filePath", symbol.getFilePath());
            map.put("startLine", symbol.getStartLine());
            map.put("parentClass", symbol.getParentClass());
            results.add(map);
        }
        return ResponseEntity.ok(results);
    }

    /**
     * Get file content from the index.
     */
    @GetMapping("/{projectId}/file/{filePath:.+}")
    public ResponseEntity<Map<String, Object>> getFileContent(
            @PathVariable String projectId,
            @PathVariable String filePath) {
        CodeIndex index = projectIndices.get(projectId);
        if (index == null) {
            return ResponseEntity.notFound().build();
        }
        String content = index.getFileContent(filePath);
        if (content == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "path", filePath,
                "content", content
        ));
    }

    /**
     * Remove a registered project.
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Map<String, String>> removeProject(@PathVariable String projectId) {
        CodeIndex removed = projectIndices.remove(projectId);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Project '" + projectId + "' removed"));
    }

    /**
     * Get the code index for a project (used internally by other services).
     */
    public CodeIndex getIndex(String projectId) {
        return projectIndices.get(projectId);
    }
}
