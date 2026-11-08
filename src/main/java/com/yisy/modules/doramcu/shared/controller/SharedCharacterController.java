package com.yisy.modules.doramcu.shared.controller;

import com.yisy.modules.doramcu.shared.dto.CreateSharedCharacterRequest;
import com.yisy.modules.doramcu.shared.dto.TravelCharacterRequest;
import com.yisy.modules.doramcu.shared.model.SharedCharacter;
import com.yisy.modules.doramcu.shared.model.SharedCharacterSkill;
import com.yisy.modules.doramcu.shared.model.SharedCharacterTravelLog;
import com.yisy.modules.doramcu.shared.service.SharedCharacterService;
import com.yisy.modules.doramcu.unified.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for shared characters that can travel between
 * any game systems in the DoraMCU universe (Traveller, Captain's Log, DoraMCU 4X).
 * <p>
 * Provides endpoints for creating shared characters, importing from
 * existing systems, traveling between systems, and managing skills.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/shared-characters}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/shared-characters")
public class SharedCharacterController {

    private static final Logger log = LoggerFactory.getLogger(SharedCharacterController.class);

    private final SharedCharacterService sharedCharacterService;

    public SharedCharacterController(SharedCharacterService sharedCharacterService) {
        this.sharedCharacterService = sharedCharacterService;
    }

    /**
     * Creates a new shared character with the given attributes.
     * If attributes are not provided, they are auto-rolled (2D6).
     *
     * @param request the character creation request
     * @return the created shared character
     */
    @PostMapping
    public ResponseEntity<SharedCharacter> createSharedCharacter(
            @RequestBody CreateSharedCharacterRequest request) {
        log.debug("POST /api/v1/shared-characters - creating shared character: {}", request.getName());
        SharedCharacter character = sharedCharacterService.createSharedCharacter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(character);
    }

    /**
     * Imports an existing Captain's Log character into the shared database.
     *
     * @param clCharacterId the Captain's Log character ID
     * @return the created shared character
     */
    @PostMapping("/import/captains-log")
    public ResponseEntity<SharedCharacter> importFromCaptainLog(
            @RequestParam Long clCharacterId) {
        log.debug("POST /api/v1/shared-characters/import/captains-log?clCharacterId={}", clCharacterId);
        try {
            SharedCharacter character = sharedCharacterService.importFromCaptainLog(clCharacterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(character);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to import Captain's Log character: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Imports an existing Traveller character into the shared database.
     *
     * @param travellerCharacterId the Traveller character ID
     * @return the created shared character
     */
    @PostMapping("/import/traveller")
    public ResponseEntity<SharedCharacter> importFromTraveller(
            @RequestParam int travellerCharacterId) {
        log.debug("POST /api/v1/shared-characters/import/traveller?travellerCharacterId={}", travellerCharacterId);
        try {
            SharedCharacter character = sharedCharacterService.importFromTraveller(travellerCharacterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(character);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to import Traveller character: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Imports an existing 4X Player (Empire ruler) into the shared database.
     *
     * @param playerId the 4X Player ID
     * @return the created shared character
     */
    @PostMapping("/import/4x")
    public ResponseEntity<SharedCharacter> importFrom4X(
            @RequestParam Long playerId) {
        log.debug("POST /api/v1/shared-characters/import/4x?playerId={}", playerId);
        try {
            SharedCharacter character = sharedCharacterService.importFrom4X(playerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(character);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to import 4X Player: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Travels a character from its current system to the target system.
     *
     * @param id the shared character ID
     * @param request the travel request with target system
     * @return the updated shared character
     */
    @PostMapping("/{id}/travel")
    public ResponseEntity<SharedCharacter> travelCharacter(
            @PathVariable Long id,
            @RequestBody TravelCharacterRequest request) {
        log.debug("POST /api/v1/shared-characters/{}/travel - traveling to {}", id, request.getTargetSystem());
        try {
            SharedCharacter character = sharedCharacterService.travelCharacter(id, request);
            return ResponseEntity.ok(character);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to travel character: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Retrieves a shared character by ID.
     *
     * @param id the shared character ID
     * @return the character, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<SharedCharacter> getSharedCharacter(@PathVariable Long id) {
        log.debug("GET /api/v1/shared-characters/{}", id);
        return sharedCharacterService.getSharedCharacter(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Returns all shared characters.
     *
     * @return list of all shared characters
     */
    @GetMapping
    public ResponseEntity<List<SharedCharacter>> getAllSharedCharacters() {
        log.debug("GET /api/v1/shared-characters - listing all shared characters");
        List<SharedCharacter> characters = sharedCharacterService.listSharedCharacters();
        return ResponseEntity.ok(characters);
    }

    /**
     * Returns shared characters filtered by game type.
     *
     * @param gameType the game type (TRAVELLER, CAPTAINS_LOG, or DORAMCU_4X)
     * @return list of shared characters in that game
     */
    @GetMapping("/by-system")
    public ResponseEntity<List<SharedCharacter>> getCharactersBySystem(
            @RequestParam GameType gameType) {
        log.debug("GET /api/v1/shared-characters/by-system?gameType={}", gameType);
        List<SharedCharacter> characters = sharedCharacterService.listBySystem(gameType);
        return ResponseEntity.ok(characters);
    }

    /**
     * Searches shared characters by name.
     *
     * @param name the name to search for
     * @return list of matching shared characters
     */
    @GetMapping("/search")
    public ResponseEntity<List<SharedCharacter>> searchCharacters(@RequestParam String name) {
        log.debug("GET /api/v1/shared-characters/search?name={}", name);
        List<SharedCharacter> characters = sharedCharacterService.searchByName(name);
        return ResponseEntity.ok(characters);
    }

    /**
     * Gets the travel history for a shared character.
     *
     * @param id the shared character ID
     * @return list of travel log entries
     */
    @GetMapping("/{id}/travel-history")
    public ResponseEntity<List<SharedCharacterTravelLog>> getTravelHistory(@PathVariable Long id) {
        log.debug("GET /api/v1/shared-characters/{}/travel-history", id);
        try {
            List<SharedCharacterTravelLog> history = sharedCharacterService.getTravelHistory(id);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Adds a skill to a shared character.
     *
     * @param id the shared character ID
     * @param params map with skillName, skillLevel (optional), and systemSource (optional)
     * @return the created skill
     */
    @PostMapping("/{id}/skills")
    public ResponseEntity<SharedCharacterSkill> addSkill(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        log.debug("POST /api/v1/shared-characters/{}/skills - adding skill", id);
        try {
            String skillName = (String) params.get("skillName");
            int skillLevel = params.get("skillLevel") != null
                    ? ((Number) params.get("skillLevel")).intValue()
                    : 1;
            String systemSource = (String) params.get("systemSource");

            if (skillName == null || skillName.isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            SharedCharacterSkill skill = sharedCharacterService.addSkill(id, skillName, skillLevel, systemSource);
            return ResponseEntity.status(HttpStatus.CREATED).body(skill);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
