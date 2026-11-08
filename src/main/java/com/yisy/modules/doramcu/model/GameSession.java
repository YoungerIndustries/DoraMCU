package com.yisy.modules.doramcu.model;

import com.yisy.modules.doramcu.unified.GameEngine;
import com.yisy.modules.doramcu.unified.GlobalClock;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Wrapper for an active game session, holding the game engine instance,
 * player list, SSE emitters, and concurrency lock.
 * <p>
 * Generic over {@link GameEngine} so that different game types (4X, Traveller,
 * Captain's Log) can each use their own game engine implementation.
 * </p>
 */
public class GameSession {

    private final String sessionId;
    private final String name;
    private final GameEngine game;
    private final Map<Long, SseEmitter> emitters;
    private final Map<Long, ScheduledFuture<?>> heartbeatFutures;
    private final Set<Long> playerIds;
    private final Map<Long, String> aiPlayerDifficulties;
    private final ReentrantReadWriteLock lock;
    /** Global clock tracking in-game time for this session. */
    private GlobalClock globalClock;

    public GameSession(String sessionId, String name, GameEngine game) {
        this.sessionId = sessionId;
        this.name = name;
        this.game = game;
        this.emitters = new ConcurrentHashMap<>();
        this.heartbeatFutures = new ConcurrentHashMap<>();
        this.playerIds = Collections.synchronizedSet(new LinkedHashSet<>());
        this.aiPlayerDifficulties = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the game engine for this session.
     * <p>
     * Callers that need game-specific operations should cast to the concrete
     * game type (e.g., {@code (com.yisy.modules.doramcu.hex.Game) session.getGame()}).
     * </p>
     *
     * @return the game engine instance
     */
    public GameEngine getGame() {
        return game;
    }

    public Map<Long, SseEmitter> getEmitters() {
        return emitters;
    }

    public Set<Long> getPlayerIds() {
        return playerIds;
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    public void addPlayer(Long playerId) {
        playerIds.add(playerId);
    }

    public void removePlayer(Long playerId) {
        playerIds.remove(playerId);
        SseEmitter emitter = emitters.remove(playerId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    public void registerEmitter(Long playerId, SseEmitter emitter) {
        emitters.put(playerId, emitter);
    }

    /**
     * Registers a heartbeat scheduled future for a player.
     * Used so it can be cancelled when the emitter is unregistered.
     */
    public void registerHeartbeatFuture(Long playerId, ScheduledFuture<?> future) {
        heartbeatFutures.put(playerId, future);
    }

    public void unregisterEmitter(Long playerId) {
        // Cancel the heartbeat task to prevent it from running after emitter is gone
        ScheduledFuture<?> future = heartbeatFutures.remove(playerId);
        if (future != null) {
            future.cancel(false);
        }
        SseEmitter emitter = emitters.remove(playerId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    public void registerAiPlayer(Long playerId, String difficulty) {
        aiPlayerDifficulties.put(playerId, difficulty);
    }

    public Map<Long, String> getAiPlayerDifficulties() {
        return aiPlayerDifficulties;
    }

    public boolean isAiPlayer(Long playerId) {
        return aiPlayerDifficulties.containsKey(playerId);
    }

    public String getAiPlayerDifficulty(Long playerId) {
        return aiPlayerDifficulties.get(playerId);
    }

    public GlobalClock getGlobalClock() {
        return globalClock;
    }

    public void setGlobalClock(GlobalClock globalClock) {
        this.globalClock = globalClock;
    }
}
