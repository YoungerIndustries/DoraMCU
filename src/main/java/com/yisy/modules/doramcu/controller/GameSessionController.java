package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.CreateSessionRequest;
import com.yisy.modules.doramcu.dto.CreateSessionResponse;
import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.util.PlayerIdExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/sessions")
public class GameSessionController {

    private static final Logger log = LoggerFactory.getLogger(GameSessionController.class);

    private final GameSessionService gameSessionService;
    private final PlayerIdExtractor playerIdExtractor;

    public GameSessionController(GameSessionService gameSessionService, PlayerIdExtractor playerIdExtractor) {
        this.gameSessionService = gameSessionService;
        this.playerIdExtractor = playerIdExtractor;
    }

    @PostMapping
    public ResponseEntity<CreateSessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request,
                                                               Authentication authentication) {
        log.debug("Creating session: {} for user: {}", request.getSessionName(), authentication.getName());

        Long playerId = playerIdExtractor.extract(authentication);
        String playerName = authentication.getName();

        try {
            CreateSessionResponse response = gameSessionService.createSession(playerId, playerName,
                    request.getSessionName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to create session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<JoinSessionResponse> joinSession(@PathVariable String sessionId,
                                                           Authentication authentication) {
        log.debug("User {} joining session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        String playerName = authentication.getName();

        try {
            JoinSessionResponse response = gameSessionService.joinSession(playerId, playerName, sessionId);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Session not found: {}", sessionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("Player already in session: {}", sessionId);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{sessionId}/leave")
    public ResponseEntity<Void> leaveSession(@PathVariable String sessionId,
                                             Authentication authentication) {
        log.debug("User {} leaving session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        gameSessionService.leaveSession(playerId, sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listSessions() {
        List<Map<String, Object>> sessions = gameSessionService.listSessions();
        return ResponseEntity.ok(sessions);
    }
}
