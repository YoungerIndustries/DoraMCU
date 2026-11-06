package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages time-based automatic turn advancement for active game sessions.
 * <p>
 * Advances game time by 1 second on each tick. When accumulated time reaches
 * the configured turn duration, a full turn is processed for the session.
 * The scheduler can be enabled/disabled and the turn interval can be changed
 * at runtime via the AdminController.
 * </p>
 */
@Service
public class TurnScheduler {

    private static final Logger log = LoggerFactory.getLogger(TurnScheduler.class);

    /** Default turn interval: 5 seconds per turn. */
    private static final long DEFAULT_TURN_INTERVAL_SECONDS = 5;

    /** Whether the scheduler is currently running. */
    private volatile boolean enabled = true;

    /** Turn interval in seconds (configurable at runtime). */
    private volatile long turnIntervalSeconds = DEFAULT_TURN_INTERVAL_SECONDS;

    /** Accumulated seconds since last turn advance per session. */
    private final Map<String, Long> accumulatedSeconds = new ConcurrentHashMap<>();

    private final GameSessionService gameSessionService;
    private final AiPlayerService aiPlayerService;
    private final DoraAiLearningService doraAiLearningService;

    @Value("${doramcu.turn.interval.seconds:5}")
    public void setDefaultTurnInterval(long interval) {
        this.turnIntervalSeconds = interval;
        log.info("Turn scheduler initialized with interval: {} seconds", interval);
    }

    @Autowired
    public TurnScheduler(GameSessionService gameSessionService,
                         @Lazy AiPlayerService aiPlayerService,
                         DoraAiLearningService doraAiLearningService) {
        this.gameSessionService = gameSessionService;
        this.aiPlayerService = aiPlayerService;
        this.doraAiLearningService = doraAiLearningService;
    }

    /**
     * Scheduled tick: runs every 1 second to advance game time.
     * When accumulated time reaches the turn interval, processes a full turn.
     */
    @Scheduled(fixedRate = 1000)
    public void tick() {
        if (!enabled) {
            return;
        }

        for (Map.Entry<String, GameSession> entry : gameSessionService.getAllSessions().entrySet()) {
            String sessionId = entry.getKey();
            GameSession session = entry.getValue();
            Game game = session.getGame();

            // Skip sessions with no players
            if (game.getPlayers().isEmpty()) {
                continue;
            }

            accumulatedSeconds.merge(sessionId, 1L, Long::sum);
            long accumulated = accumulatedSeconds.getOrDefault(sessionId, 0L);

            if (accumulated >= turnIntervalSeconds) {
                // Calculate how many full turns to advance
                long turnsToAdvance = accumulated / turnIntervalSeconds;
                accumulatedSeconds.put(sessionId, accumulated % turnIntervalSeconds);

                for (long i = 0; i < turnsToAdvance; i++) {
                    try {
                        session.getLock().writeLock().lock();
                        try {
                            game.nextTurn();
                            // Simultaneous turns: all players act concurrently, no player cycling
                        } finally {
                            session.getLock().writeLock().unlock();
                        }

                        log.debug("Auto-advanced turn {} in session {} (simultaneous turns)",
                                game.getCurrentTurn(), sessionId);

                        // Execute AI player actions for this turn
                        executeAiActionsForSession(sessionId, session);

                        // Broadcast turn change event - all players can act simultaneously
                        gameSessionService.broadcastEvent(sessionId,
                                new com.yisy.modules.doramcu.dto.GameEvent(
                                        com.yisy.modules.doramcu.dto.GameEvent.EventType.TURN_CHANGED,
                                        sessionId,
                                        Map.of(
                                                "turn", game.getCurrentTurn(),
                                                "autoAdvanced", true
                                        )));
                    } catch (Exception e) {
                        log.warn("Failed to auto-advance turn in session: {}", sessionId, e);
                    }
                }
            }
        }
    }

    /**
     * Returns whether the scheduler is currently enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the scheduler.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("Turn scheduler {}d", enabled ? "enab" : "disab");
    }

    /**
     * Returns the current turn interval in seconds.
     */
    public long getTurnIntervalSeconds() {
        return turnIntervalSeconds;
    }

    /**
     * Sets the turn interval in seconds.
     *
     * @param seconds the new turn interval (must be >= 1)
     */
    public void setTurnIntervalSeconds(long seconds) {
        if (seconds < 1) {
            throw new IllegalArgumentException("Turn interval must be at least 1 second");
        }
        this.turnIntervalSeconds = seconds;
        // Reset accumulated time when interval changes
        accumulatedSeconds.replaceAll((k, v) -> 0L);
        log.info("Turn interval changed to {} seconds", seconds);
    }

    /**
     * Returns the accumulated seconds for a session since last turn.
     */
    public long getAccumulatedSeconds(String sessionId) {
        return accumulatedSeconds.getOrDefault(sessionId, 0L);
    }

    /**
     * Executes AI player actions for all registered AI players in a session.
     * <p>
     * After each turn advance, this method triggers all AI players to take actions.
     * Each AI player's action is recorded with the Dora AI learning service so the
     * neural network can learn from the outcomes.
     * </p>
     */
    private void executeAiActionsForSession(String sessionId, GameSession session) {
        Map<Long, String> aiPlayerDifficulties = session.getAiPlayerDifficulties();
        if (aiPlayerDifficulties.isEmpty()) {
            return;
        }

        log.debug("Executing AI actions for {} AI players in session: {}",
                aiPlayerDifficulties.size(), sessionId);

        for (Map.Entry<Long, String> entry : aiPlayerDifficulties.entrySet()) {
            Long aiPlayerId = entry.getKey();
            String difficultyName = entry.getValue();

            try {
                AiPlayerService.Difficulty difficulty = AiPlayerService.Difficulty.valueOf(difficultyName);
                Map<Long, AiPlayerService.Difficulty> aiMap = Map.of(aiPlayerId, difficulty);

                // Capture game state before AI action for learning
                com.yisy.modules.doramcu.dto.GameStateResponse stateBefore =
                        gameSessionService.getGameState(aiPlayerId, sessionId);

                // Execute AI turn
                aiPlayerService.executeAiTurn(sessionId, aiMap);

                // Capture game state after AI action for learning
                com.yisy.modules.doramcu.dto.GameStateResponse stateAfter =
                        gameSessionService.getGameState(aiPlayerId, sessionId);

                // Compute reward and record for learning
                double reward = doraAiLearningService.computeReward(
                        convertToDlGameState(stateBefore),
                        convertToDlGameState(stateAfter),
                        aiPlayerId);

                log.debug("AI player {} (difficulty: {}) action completed with reward: {:.2f}",
                        aiPlayerId, difficulty, reward);

            } catch (Exception e) {
                log.warn("AI action failed for player {} in session {}: {}",
                        aiPlayerId, sessionId, e.getMessage());
            }
        }
    }

    /**
     * Converts main module GameStateResponse to DL module GameStateResponse.
     */
    private com.yisy.modules.doradl.dto.GameStateResponse convertToDlGameState(
            com.yisy.modules.doramcu.dto.GameStateResponse source) {
        if (source == null) {
            return null;
        }
        com.yisy.modules.doradl.dto.GameStateResponse target = new com.yisy.modules.doradl.dto.GameStateResponse();
        target.setSessionId(source.getSessionId());
        target.setCurrentTurn(source.getCurrentTurn());
        target.setCurrentPlayerId(source.getCurrentPlayerId());
        target.setCurrentPlayerName(source.getCurrentPlayerName());
        target.setMyTurn(source.isMyTurn());

        // Convert players
        java.util.List<com.yisy.modules.doradl.dto.PlayerSummary> dlPlayers = new java.util.ArrayList<>();
        if (source.getPlayers() != null) {
            for (com.yisy.modules.doramcu.dto.PlayerSummary player : source.getPlayers()) {
                com.yisy.modules.doradl.dto.PlayerSummary dlPlayer = new com.yisy.modules.doradl.dto.PlayerSummary();
                dlPlayer.setId(player.getId());
                dlPlayer.setName(player.getName());
                dlPlayer.setResources(player.getResources());
                dlPlayer.setPlanetCount(player.getPlanetCount());
                dlPlayer.setFleetCount(player.getFleetCount());
                dlPlayer.setAi(player.isAi());
                dlPlayer.setDifficulty(player.getDifficulty());
                dlPlayers.add(dlPlayer);
            }
        }
        target.setPlayers(dlPlayers);

        // Convert subSectors
        java.util.List<com.yisy.modules.doradl.dto.SubSectorInfo> dlSubSectors = new java.util.ArrayList<>();
        if (source.getSubSectors() != null) {
            for (com.yisy.modules.doramcu.dto.SubSectorInfo sub : source.getSubSectors()) {
                com.yisy.modules.doradl.dto.SubSectorInfo dlSub = new com.yisy.modules.doradl.dto.SubSectorInfo();
                dlSub.setId(sub.getId());
                dlSub.setName(sub.getName());
                dlSub.setSectorRow(sub.getSectorRow());
                dlSub.setSectorCol(sub.getSectorCol());
                dlSub.setSubRow(sub.getSubRow());
                dlSub.setSubCol(sub.getSubCol());
                dlSub.setGenerated(sub.isGenerated());
                dlSub.setSystemCount(sub.getSystemCount());
                dlSub.setSystems(sub.getSystems());
                dlSubSectors.add(dlSub);
            }
        }
        target.setSubSectors(dlSubSectors);

        return target;
    }
}
