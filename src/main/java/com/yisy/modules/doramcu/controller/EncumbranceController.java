package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.traveller.*;
import com.yisy.modules.doramcu.service.EncumbranceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for encumbrance management.
 * <p>
 * Provides endpoints for managing character inventories,
 * checking encumbrance levels, and presetting gear.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/encumbrance}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/encumbrance")
public class EncumbranceController {

    private static final Logger log = LoggerFactory.getLogger(EncumbranceController.class);

    private final EncumbranceService encumbranceService;

    public EncumbranceController(EncumbranceService encumbranceService) {
        this.encumbranceService = encumbranceService;
    }

    /**
     * Gets the current encumbrance state for a character.
     */
    @GetMapping("/{characterId}")
    public ResponseEntity<Map<String, Object>> getEncumbrance(@PathVariable int characterId) {
        log.debug("GET /api/v1/traveller/encumbrance/{}", characterId);

        try {
            Encumbrance enc = encumbranceService.getEncumbrance(characterId);

            Map<String, Object> result = new HashMap<>();
            result.put("characterId", characterId);
            result.put("characterName", enc.getCharacter().getName());
            result.put("carryingCapacity", enc.getCarryingCapacity());
            result.put("currentWeight", enc.getCurrentWeight());
            result.put("remainingCapacity", enc.getRemainingCapacity());
            result.put("encumbranceLevel", enc.getEncumbranceLevel().getName());
            result.put("encumbranceDescription", enc.getEncumbranceLevel().getDescription());
            result.put("movementPenalty", enc.getMovementPenalty());
            result.put("combatPenalty", enc.getCombatPenalty());
            result.put("baseMovement", enc.getBaseMovement());
            result.put("effectiveMovement", enc.getEffectiveMovement());
            result.put("isOverloaded", enc.isOverloaded());

            // Inventory
            List<Map<String, Object>> inventory = new ArrayList<>();
            for (Encumbrance.Item item : enc.getInventory()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("name", item.getName());
                itemMap.put("weight", item.getWeight());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("totalWeight", item.getTotalWeight());
                itemMap.put("category", item.getCategory().getName());
                inventory.add(itemMap);
            }
            result.put("inventory", inventory);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Adds an item to a character's inventory.
     */
    @PostMapping("/{characterId}/items")
    public ResponseEntity<Map<String, Object>> addItem(
            @PathVariable int characterId,
            @RequestParam String name,
            @RequestParam double weight,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam String category) {
        log.debug("POST /api/v1/traveller/encumbrance/{}/items — {}", characterId, name);

        Encumbrance.Item.Category cat;
        try {
            cat = Encumbrance.Item.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            cat = Encumbrance.Item.Category.OTHER;
        }

        boolean added = encumbranceService.addItem(characterId, name, weight, quantity, cat);

        Map<String, Object> result = new HashMap<>();
        result.put("success", added);
        result.put("summary", encumbranceService.getSummary(characterId));

        return added ? ResponseEntity.ok(result) : ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }

    /**
     * Removes an item from a character's inventory.
     */
    @DeleteMapping("/{characterId}/items")
    public ResponseEntity<Map<String, Object>> removeItem(
            @PathVariable int characterId,
            @RequestParam String name,
            @RequestParam(defaultValue = "1") int quantity) {
        log.debug("DELETE /api/v1/traveller/encumbrance/{}/items — {}", characterId, name);

        boolean removed = encumbranceService.removeItem(characterId, name, quantity);

        Map<String, Object> result = new HashMap<>();
        result.put("success", removed);
        result.put("summary", encumbranceService.getSummary(characterId));

        return removed ? ResponseEntity.ok(result) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * Removes all items of a given name from a character's inventory.
     */
    @DeleteMapping("/{characterId}/items/all")
    public ResponseEntity<Map<String, Object>> removeAllItem(
            @PathVariable int characterId,
            @RequestParam String name) {
        log.debug("DELETE /api/v1/traveller/encumbrance/{}/items/all — {}", characterId, name);

        encumbranceService.removeAllItem(characterId, name);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("summary", encumbranceService.getSummary(characterId));

        return ResponseEntity.ok(result);
    }

    /**
     * Presets gear for a character based on their career.
     */
    @PostMapping("/{characterId}/preset")
    public ResponseEntity<Map<String, Object>> presetGear(
            @PathVariable int characterId,
            @RequestParam String career) {
        log.debug("POST /api/v1/traveller/encumbrance/{}/preset — {}", characterId, career);

        try {
            Career car = Career.valueOf(career.toUpperCase());
            encumbranceService.presetGear(characterId, car);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("summary", encumbranceService.getSummary(characterId));

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Unknown career: " + career);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Gets a text summary of a character's encumbrance state.
     */
    @GetMapping("/{characterId}/summary")
    public ResponseEntity<Map<String, String>> getSummary(@PathVariable int characterId) {
        log.debug("GET /api/v1/traveller/encumbrance/{}/summary", characterId);

        try {
            String summary = encumbranceService.getSummary(characterId);
            Map<String, String> result = new HashMap<>();
            result.put("summary", summary);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Lists all tracked characters.
     */
    @GetMapping
    public ResponseEntity<Set<Integer>> listTracked() {
        log.debug("GET /api/v1/traveller/encumbrance");
        return ResponseEntity.ok(encumbranceService.listTrackedCharacters());
    }

    /**
     * Lists all available item categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> listCategories() {
        log.debug("GET /api/v1/traveller/encumbrance/categories");

        List<String> categories = new ArrayList<>();
        for (Encumbrance.Item.Category category : Encumbrance.Item.Category.values()) {
            categories.add(category.getName());
        }

        return ResponseEntity.ok(categories);
    }
}
