package com.yisy.modules.doramcu.traveller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for browsing and searching the Classic Traveller rules database.
 * <p>
 * Provides endpoints for querying rule entries and reference tables from 12 Classic Traveller
 * source books: The Traveller Book, B01-B07, S03, S07, S09, and A05.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/rules")
public class TravellerRulesController {

    private static final Logger log = LoggerFactory.getLogger(TravellerRulesController.class);

    private final TravellerRulesService travellerRulesService;

    public TravellerRulesController(TravellerRulesService travellerRulesService) {
        this.travellerRulesService = travellerRulesService;
    }

    // ==================== Summary ====================

    /**
     * Gets a summary of all rules and tables in the Traveller rules database.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getRulesSummary() {
        log.debug("GET /api/v1/traveller/rules/summary");
        Map<String, Object> summary = new HashMap<>();

        // Count total entries by summing all categories
        long totalEntries = 0;
        for (String category : travellerRulesService.getCategories()) {
            totalEntries += travellerRulesService.getRulesByCategory(category).size();
        }

        summary.put("totalEntries", totalEntries);
        summary.put("categories", travellerRulesService.getCategories());
        summary.put("sources", travellerRulesService.getSources());
        
        // Dynamically get all table categories from the database
        List<String> tableCategories = travellerRulesService.getAllTableCategories();
        summary.put("tableCategories", tableCategories);

        return ResponseEntity.ok(summary);
    }

    // ==================== Rule Entries ====================

    /**
     * Gets all rule entries for a given category.
     */
    @GetMapping("/category")
    public ResponseEntity<List<TravellerRulesEntry>> getRulesByCategory(
            @RequestParam String category) {
        log.debug("GET /api/v1/traveller/rules/category?category={}", category);
        List<TravellerRulesEntry> rules = travellerRulesService.getRulesByCategory(category);
        return ResponseEntity.ok(rules);
    }

    /**
     * Gets all rule entries from a given source book.
     */
    @GetMapping("/source")
    public ResponseEntity<List<TravellerRulesEntry>> getRulesBySource(
            @RequestParam String source) {
        log.debug("GET /api/v1/traveller/rules/source?source={}", source);
        List<TravellerRulesEntry> rules = travellerRulesService.getRulesBySource(source);
        return ResponseEntity.ok(rules);
    }

    /**
     * Searches rule entries by keyword.
     */
    @GetMapping("/search")
    public ResponseEntity<List<TravellerRulesEntry>> searchRules(
            @RequestParam String keyword) {
        log.debug("GET /api/v1/traveller/rules/search?keyword={}", keyword);
        List<TravellerRulesEntry> rules = travellerRulesService.searchRules(keyword);
        return ResponseEntity.ok(rules);
    }

    /**
     * Gets all available rule categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        log.debug("GET /api/v1/traveller/rules/categories");
        return ResponseEntity.ok(travellerRulesService.getCategories());
    }

    /**
     * Gets all available source books.
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        log.debug("GET /api/v1/traveller/rules/sources");
        return ResponseEntity.ok(travellerRulesService.getSources());
    }

    // ==================== Reference Tables ====================

    /**
     * Gets all table entries for a given table category (e.g., TABLE_SAVING_THROW).
     * If no category is specified, returns all table entries.
     */
    @GetMapping("/tables")
    public ResponseEntity<List<TravellerRulesTable>> getTableEntries(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            log.debug("GET /api/v1/traveller/rules/tables?category={}", category);
            List<TravellerRulesTable> entries = travellerRulesService.getTableEntriesByCategory(category);
            return ResponseEntity.ok(entries);
        } else {
            log.debug("GET /api/v1/traveller/rules/tables (all)");
            List<TravellerRulesTable> allEntries = travellerRulesService.getAllTableEntries();
            return ResponseEntity.ok(allEntries);
        }
    }

    /**
     * Gets all table entries for a given category (alias for /tables?category=).
     */
    @GetMapping("/tables/category")
    public ResponseEntity<List<TravellerRulesTable>> getTableEntriesByCategory(
            @RequestParam String category) {
        log.debug("GET /api/v1/traveller/rules/tables/category?category={}", category);
        List<TravellerRulesTable> entries = travellerRulesService.getTableEntriesByCategory(category);
        return ResponseEntity.ok(entries);
    }
}
