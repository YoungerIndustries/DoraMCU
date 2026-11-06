package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import com.yisy.modules.doramcu.dto.GameStateResponse;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.AiPlayerService.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * REST controller for managing AI players in game sessions.
 * <p>
 * Provides endpoints to create AI players with different difficulty levels
 * and execute AI turns automatically.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/ai")
public class AiPlayerController {

    private static final Logger log = LoggerFactory.getLogger(AiPlayerController.class);

    private final AiPlayerService aiPlayerService;

    public AiPlayerController(AiPlayerService aiPlayerService) {
        this.aiPlayerService = aiPlayerService;
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
}
