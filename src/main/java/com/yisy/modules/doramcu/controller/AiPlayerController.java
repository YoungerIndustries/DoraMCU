package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.ai.AiDelegationArea;
import com.yisy.modules.doramcu.ai.AiDelegationSettings;
import com.yisy.modules.doramcu.ai.AiDelegationService;
import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import com.yisy.modules.doramcu.dto.GameStateResponse;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.AiPlayerService.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for managing AI players in game sessions.
 * <p>
 * Provides endpoints to create AI players with different difficulty levels,
 * execute AI turns automatically, and manage AI delegation for human players.
 * </p>
 * <p>
 * AI Delegation allows a human player to delegate empire management responsibilities
 * to the AI. The player can enable the empire-level AI switch and then fine-tune
 * by toggling individual delegation areas (fleet management, combat, construction, etc.).
 * </p>
 */
@RestController
@RequestMapping("/api/v1/4x/sessions/{sessionId}/ai")
public class AiPlayerController {

    private static final Logger log = LoggerFactory.getLogger(AiPlayerController.class);

    private final AiPlayerService aiPlayerService;
    private final AiDelegationService aiDelegationService;

    public AiPlayerController(AiPlayerService aiPlayerService, AiDelegationService aiDelegationService) {
        this.aiPlayerService = aiPlayerService;
        this.aiDelegationService = aiDelegationService;
    }

    /**
     * Creates a new AI player and adds it to the specified game session.
     *
     * @param sessionId the game session ID
     * @param name the AI player's name
     * @param difficulty the AI difficulty level (EASY, MEDIUM, HARD)
     * @return the join response with the AI player's ID
     */
    @PostMapping
    public ResponseEntity<JoinSessionResponse> createAiPlayer(
            @PathVariable String sessionId,
            @RequestParam String name,
            @RequestParam(defaultValue = "MEDIUM") Difficulty difficulty) {
        
        log.debug("Creating AI player: {} with difficulty: {} in session: {}", name, difficulty, sessionId);

        try {
            JoinSessionResponse response = aiPlayerService.createAiPlayer(sessionId, name, difficulty);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Session not found: {}", sessionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("Failed to create AI player: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Executes one turn for the current AI player in the session.
     * <p>
     * This endpoint checks if the current player is an AI, and if so,
     * performs actions based on the AI's difficulty level and advances the turn.
     * </p>
     *
     * @param sessionId the game session ID
     * @param aiPlayerId the AI player's ID
     * @param difficulty the AI difficulty level
     * @return the game state after AI actions
     */
    @PostMapping("/turn")
    public ResponseEntity<GameStateResponse> executeAiTurn(
            @PathVariable String sessionId,
            @RequestParam Long aiPlayerId,
            @RequestParam(defaultValue = "MEDIUM") Difficulty difficulty) {
        
        log.debug("Executing AI turn for player: {} in session: {}", aiPlayerId, sessionId);

        try {
            GameStateResponse response = aiPlayerService.executeAiTurn(
                    sessionId, java.util.Map.of(aiPlayerId, difficulty));
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Session not found: {}", sessionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("turn")) {
                log.warn("Not AI player's turn in session: {}", sessionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ========================================================================
    // AI Delegation Endpoints
    // ========================================================================

    /**
     * Gets the current AI delegation status for a player.
     * <p>
     * Returns the empire-level AI switch state and the status of each delegation area.
     * </p>
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @return the delegation status map
     */
    @GetMapping("/delegation")
    public ResponseEntity<Map<String, Object>> getDelegationStatus(
            @PathVariable String sessionId,
            @RequestParam Long playerId) {
        
        log.debug("Getting delegation status for player: {} in session: {}", playerId, sessionId);

        try {
            Map<String, Object> status = aiDelegationService.getDelegationStatus(sessionId, playerId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.warn("Failed to get delegation status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Enables or disables the empire-level AI switch for a player.
     * <p>
     * When enabled, the AI takes over all delegation areas by default.
     * The player can then fine-tune by toggling individual areas.
     * </p>
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @param enabled true to enable AI delegation, false to disable
     * @return the updated delegation settings as a status map
     */
    @PostMapping("/delegation/empire")
    public ResponseEntity<Map<String, Object>> setEmpireAiEnabled(
            @PathVariable String sessionId,
            @RequestParam Long playerId,
            @RequestParam(defaultValue = "true") boolean enabled) {
        
        log.debug("Setting empire AI to {} for player: {} in session: {}", enabled, playerId, sessionId);

        try {
            AiDelegationSettings settings = aiDelegationService.setEmpireAiEnabled(sessionId, playerId, enabled);
            Map<String, Object> status = aiDelegationService.getDelegationStatus(sessionId, playerId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.warn("Failed to set empire AI: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Toggles a specific delegation area for a player.
     * <p>
     * The empire-level AI switch must be enabled for area toggles to take effect.
     * </p>
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @param area the delegation area name (e.g., "FLEET_MANAGEMENT", "COMBAT")
     * @param enabled true to enable, false to disable
     * @return the updated delegation settings as a status map
     */
    @PostMapping("/delegation/area/{area}")
    public ResponseEntity<Map<String, Object>> toggleDelegationArea(
            @PathVariable String sessionId,
            @RequestParam Long playerId,
            @PathVariable String area,
            @RequestParam(defaultValue = "true") boolean enabled) {
        
        log.debug("Toggling delegation area {} to {} for player: {} in session: {}",
                area, enabled, playerId, sessionId);

        try {
            AiDelegationArea delegationArea = AiDelegationArea.valueOf(area.toUpperCase());
            aiDelegationService.toggleArea(sessionId, playerId, delegationArea, enabled);
            Map<String, Object> status = aiDelegationService.getDelegationStatus(sessionId, playerId);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid delegation area: {}", area);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.warn("Failed to toggle delegation area: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Executes an AI turn for a player with delegation enabled.
     * <p>
     * Triggers the AI to make decisions and execute actions for the player's empire
     * based on the current delegation settings.
     * </p>
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @return the game state after AI actions
     */
    @PostMapping("/delegation/turn")
    public ResponseEntity<GameStateResponse> executeDelegatedAiTurn(
            @PathVariable String sessionId,
            @RequestParam Long playerId) {
        
        log.debug("Executing delegated AI turn for player: {} in session: {}", playerId, sessionId);

        try {
            GameStateResponse response = aiDelegationService.executeAiTurn(sessionId, playerId);
            if (response == null) {
                log.warn("AI turn execution returned null for player: {} in session: {}", playerId, sessionId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Failed to execute delegated AI turn: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets the list of all available delegation areas with their descriptions.
     *
     * @return a map of area names to their display names and descriptions
     */
    @GetMapping("/delegation/areas")
    public ResponseEntity<Map<String, Object>> getDelegationAreas() {
        Map<String, Object> areas = new java.util.LinkedHashMap<>();
        for (AiDelegationArea area : AiDelegationArea.values()) {
            Map<String, String> areaInfo = new java.util.LinkedHashMap<>();
            areaInfo.put("name", area.name());
            areaInfo.put("displayName", area.getDisplayName());
            areaInfo.put("description", area.getDescription());
            areas.put(area.name(), areaInfo);
        }
        return ResponseEntity.ok(areas);
    }
}
