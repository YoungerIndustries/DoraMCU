package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.util.PlayerIdExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/ws/v1/game")
public class GameWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(GameWebSocketController.class);
    private static final long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    private static final long HEARTBEAT_INTERVAL = 15000L; // 15 seconds

    private final GameSessionService gameSessionService;
    private final PlayerIdExtractor playerIdExtractor;
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(4);

    public GameWebSocketController(GameSessionService gameSessionService, PlayerIdExtractor playerIdExtractor) {
        this.gameSessionService = gameSessionService;
        this.playerIdExtractor = playerIdExtractor;
    }

    @GetMapping(value = "/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable String sessionId, Authentication authentication) {
        log.debug("User {} connecting to SSE stream for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);

        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        try {
            GameSession session = gameSessionService.getGameSession(sessionId);
            if (session == null) {
                log.warn("Session not found: {}", sessionId);
                emitter.complete();
                return emitter;
            }

            session.registerEmitter(playerId, emitter);

            emitter.onCompletion(() -> {
                log.debug("SSE stream completed for player {} in session: {}", playerId, sessionId);
                session.unregisterEmitter(playerId);
            });
            emitter.onTimeout(() -> {
                log.debug("SSE stream timed out for player {} in session: {}", playerId, sessionId);
                session.unregisterEmitter(playerId);
            });
            emitter.onError((e) -> {
                log.warn("SSE stream error for player {} in session: {}", playerId, sessionId, e);
                session.unregisterEmitter(playerId);
            });

            // Send initial connection event
            try {
                emitter.send(SseEmitter.event()
                        .name("connected")
                        .data("Connected to game session: " + sessionId));
            } catch (IOException e) {
                log.warn("Failed to send initial connection event", e);
            }

            // Start heartbeat to keep the connection alive
            heartbeatExecutor.scheduleAtFixedRate(() -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data(System.currentTimeMillis()));
                } catch (IOException e) {
                    log.debug("Heartbeat failed for player {} in session {}: {}", playerId, sessionId, e.getMessage());
                    session.unregisterEmitter(playerId);
                }
            }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            log.error("Failed to register SSE emitter", e);
            emitter.complete();
        }

        return emitter;
    }
}
