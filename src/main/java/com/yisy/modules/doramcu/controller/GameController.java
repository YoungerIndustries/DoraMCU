package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.util.PlayerIdExtractor;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameSessionService gameSessionService;
    private final PlayerIdExtractor playerIdExtractor;

    public GameController(GameSessionService gameSessionService, PlayerIdExtractor playerIdExtractor) {
        this.gameSessionService = gameSessionService;
        this.playerIdExtractor = playerIdExtractor;
    }

    @GetMapping("/state")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable String sessionId,
                                                          Authentication authentication) {
        log.debug("User {} requesting game state for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.getGameState(playerId, sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/move-fleet")
    public ResponseEntity<GameStateResponse> moveFleet(@PathVariable String sessionId,
                                                       @Valid @RequestBody MoveFleetRequest request,
                                                       Authentication authentication) {
        log.debug("User {} moving fleet in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.moveFleet(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/build-ship")
    public ResponseEntity<GameStateResponse> buildShip(@PathVariable String sessionId,
                                                       @Valid @RequestBody BuildShipRequest request,
                                                       Authentication authentication) {
        log.debug("User {} building ship in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buildShip(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/engage-combat")
    public ResponseEntity<GameStateResponse> engageCombat(@PathVariable String sessionId,
                                                          @Valid @RequestBody EngageCombatRequest request,
                                                          Authentication authentication) {
        log.debug("User {} engaging combat in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.engageCombat(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/advance-turn")
    public ResponseEntity<GameStateResponse> advanceTurn(@PathVariable String sessionId,
                                                         Authentication authentication) {
        log.debug("User {} advancing turn in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.advanceTurn(playerId, sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pipeline")
    public ResponseEntity<ResourcePipelineSummary> getPipelineStatus(@PathVariable String sessionId,
                                                                     Authentication authentication) {
        log.debug("User {} requesting pipeline status for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        ResourcePipelineSummary summary = gameSessionService.getPipelineSummary(playerId, sessionId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/pipeline/details")
    public ResponseEntity<PipelineDetailsDto> getPipelineDetails(@PathVariable String sessionId,
                                                                  Authentication authentication) {
        log.debug("User {} requesting pipeline details for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        PipelineDetailsDto details = gameSessionService.getPipelineDetails(playerId, sessionId);
        if (details == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(details);
    }
}
