package com.yisy.modules.doramcu.model;

import com.yisy.modules.doramcu.hex.Game;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameSession {

    private final String sessionId;
    private final String name;
    private final Game game;
    private final Map<Long, SseEmitter> emitters;
    private final Set<Long> playerIds;
    private final Map<Long, String> aiPlayerDifficulties;
    private final ReentrantReadWriteLock lock;

    public GameSession(String sessionId, String name, Game game) {
        this.sessionId = sessionId;
        this.name = name;
        this.game = game;
        this.emitters = new ConcurrentHashMap<>();
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

    public Game getGame() {
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

    public void unregisterEmitter(Long playerId) {
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
}
