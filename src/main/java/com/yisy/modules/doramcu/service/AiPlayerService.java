package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.ai.*;
import com.yisy.modules.doramcu.config.AiDifficultyConfig;
import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doradl.dl.service.DlGameAiService;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.model.GameSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service that provides AI-controlled players for the 4X space strategy game.
 * <p>
 * The AI player can join game sessions and make autonomous decisions including:
 * building ships, moving fleets, engaging in combat, and advancing turns.
 * The AI uses a combination of neural network predictions and heuristic-based
 * decision making with configurable difficulty levels.
 * </p>
 * <p>
 * Difficulty levels affect the AI's strategic decisions:
 * <ul>
 *   <li>EASY — Random actions, poor resource management</li>
 *   <li>MEDIUM — Neural network + heuristic blend, balanced strategy</li>
 *   <li>HARD — Neural network + heuristic blend, aggressive expansion</li>
 * </ul>
 * </p>
 */
@Service
public class AiPlayerService {

    private static final Logger log = LoggerFactory.getLogger(AiPlayerService.class);

    private final GameSessionService gameSessionService;
    private final DlGameAiService dlGameAiService;
    private final DoraAiLearningService doraAiLearningService;
    private final GameChatService gameChatService;
    private final Random random;

    /**
     * Difficulty levels for AI players.
     */
    public enum Difficulty {
        /** Random actions, poor resource management */
        EASY,
        /** Balanced approach, moderate strategy */
        MEDIUM,
        /** Aggressive expansion, optimal resource usage */
        HARD
    }

    private final AiDifficultyConfig aiDifficultyConfig;

    /** Map from session player ID to their EmpireAiCore instance. */
    private final Map<Long, EmpireAiCore> aiCoreMap = new HashMap<>();

    public AiPlayerService(GameSessionService gameSessionService, DlGameAiService dlGameAiService,
                           AiDifficultyConfig aiDifficultyConfig, GameChatService gameChatService,
                           DoraAiLearningService doraAiLearningService) {
        this.gameSessionService = gameSessionService;
        this.dlGameAiService = dlGameAiService;
        this.aiDifficultyConfig = aiDifficultyConfig;
        this.gameChatService = gameChatService;
        this.doraAiLearningService = doraAiLearningService;
        this.random = new Random();
    }

    /**
     * Creates a new AI player and adds it to an existing game session.
     *
     * @param sessionId the session to join
     * @param name the AI player's name
     * @param difficulty the AI difficulty level
     * @return the response with the AI player's ID (Game's internal ID)
     */
    public JoinSessionResponse createAiPlayer(String sessionId, String name, Difficulty difficulty) {
        log.debug("Creating AI player: {} with difficulty: {} in session: {}", name, difficulty, sessionId);

        // Join the session first
        Long aiPlayerId = generateAiPlayerId();
        JoinSessionResponse response = gameSessionService.joinSession(aiPlayerId, name, sessionId);

        // Register this player as an AI player with the given difficulty
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session != null) {
            session.registerAiPlayer(aiPlayerId, difficulty.name());
        }

        // Get the actual Game player (already set up with empire, home system, and fleets by joinSession)
        session = gameSessionService.getGameSession(sessionId);
        if (session != null) {
            session.getLock().writeLock().lock();
            try {
                Game game = (Game) session.getGame();
                // The last player added is our AI
                List<Player> players = game.getPlayers();
                Player aiPlayer = players.get(players.size() - 1);

                log.debug("AI player {} created with Game ID: {} and empire ID: {}",
                        name, aiPlayer.getId(), aiPlayer.getEmpireId());

                // Create EmpireAiCore for this AI player
                int researchLevel = switch (difficulty) {
                    case EASY -> 1;
                    case MEDIUM -> 4;
                    case HARD -> 7;
                };
                EmpireAiCore aiCore = new EmpireAiCore(aiPlayer.getId(), aiPlayer, researchLevel);
                aiCoreMap.put(aiPlayerId, aiCore);
                gameChatService.registerEmpireAiCore(aiPlayerId, aiCore);
                log.info("Created EmpireAiCore for AI player {}: {}", name, aiCore);
            } finally {
                session.getLock().writeLock().unlock();
            }
        }

        return response;
    }

    /**
     * Returns the Game's internal player ID for a given session player ID.
     */
    private int getGamePlayerId(GameSession session, long playerId) {
        // Find the index in session's player list
        List<Long> sessionPlayers = new ArrayList<>(session.getPlayerIds());
        int index = sessionPlayers.indexOf(playerId);
        Game game = (Game) session.getGame();
        if (index >= 0 && index < game.getPlayers().size()) {
            return game.getPlayers().get(index).getId();
        }
        return -1;
    }

    /**
     * Executes one turn for all AI players in a session.
     * <p>
     * In simultaneous turns mode, all AI players can act at any time.
     * This method performs actions for all configured AI players.
     * </p>
     *
     * @param sessionId the game session ID
     * @param aiDifficulties map of AI player IDs (session IDs) to their difficulty levels
     * @return the game state after AI actions
     */
    public GameStateResponse executeAiTurn(String sessionId, Map<Long, Difficulty> aiDifficulties) {
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        GameStateResponse state = gameSessionService.getGameState(null, sessionId, true);

        // Simultaneous turns: execute actions for all AI players
        for (Map.Entry<Long, Difficulty> entry : aiDifficulties.entrySet()) {
            Long sessionPlayerId = entry.getKey();
            Difficulty difficulty = entry.getValue();

            log.debug("AI player (session ID: {}, difficulty: {}) taking turn in session: {}",
                    sessionPlayerId, difficulty, sessionId);

            // Find the game player ID for this session player
            List<Long> sessionPlayers = new ArrayList<>(session.getPlayerIds());
            int index = sessionPlayers.indexOf(sessionPlayerId);
            Game game = (Game) session.getGame();
            if (index < 0 || index >= game.getPlayers().size()) {
                continue;
            }
            int gamePlayerId = game.getPlayers().get(index).getId();

            // Perform AI actions
            performAiActions(sessionId, sessionPlayerId, gamePlayerId, difficulty);
        }

        return state;
    }

    /**
     * Performs a series of actions for an AI player based on its difficulty.
     * Uses neural network predictions for MEDIUM and HARD difficulty.
     *
     * @param sessionId the session ID
     * @param sessionPlayerId the AI player's session ID (for GameSessionService calls)
     * @param gamePlayerId the AI player's Game internal ID (for finding the player)
     * @param difficulty the AI's difficulty level
     */
    private void performAiActions(String sessionId, Long sessionPlayerId, int gamePlayerId, Difficulty difficulty) {
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null) {
            return;
        }

        session.getLock().writeLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player aiPlayer = findPlayerById(game, gamePlayerId);
            if (aiPlayer == null || aiPlayer.isEliminated()) {
                return;
            }

            // Get or create EmpireAiCore for this player
            EmpireAiCore aiCore = aiCoreMap.get(sessionPlayerId);
            if (aiCore == null) {
                // Fallback: create a default AI core
                int researchLevel = switch (difficulty) {
                    case EASY -> 1;
                    case MEDIUM -> 4;
                    case HARD -> 7;
                };
                aiCore = new EmpireAiCore(aiPlayer.getId(), aiPlayer, researchLevel);
                aiCoreMap.put(sessionPlayerId, aiCore);
                gameChatService.registerEmpireAiCore(sessionPlayerId, aiCore);
            }

            // Build decision context
            AiDecisionContext context = buildDecisionContext(game, aiPlayer);

            // Update GameChatService with current context
            gameChatService.updateEmpireContext(sessionPlayerId, context);

            // Use EmpireAiCore to decide
            AiDecision decision = aiCore.decide(context);
            log.debug("AI player {} (personality: {}) decision: {}",
                    aiPlayer.getEmpireName(), aiCore.getPersonality().getLabel(), decision);

            // Execute the decision
            executeDecision(sessionId, sessionPlayerId, decision, game, aiPlayer);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    /**
     * Builds an AiDecisionContext from the current game state.
     */
    private AiDecisionContext buildDecisionContext(Game game, Player aiPlayer) {
        // Gather owned fleets
        List<Fleet> ownedFleets = new ArrayList<>();
        for (Integer fleetId : aiPlayer.getOwnedFleetIds()) {
            Fleet fleet = game.getFleet(fleetId);
            if (fleet != null) {
                ownedFleets.add(fleet);
            }
        }

        // Gather enemy fleets
        List<Fleet> enemyFleets = new ArrayList<>();
        for (Map.Entry<Integer, Fleet> entry : game.getFleets().entrySet()) {
            if (!aiPlayer.getOwnedFleetIds().contains(entry.getKey())) {
                enemyFleets.add(entry.getValue());
            }
        }

        // Gather stations
        List<com.yisy.modules.doramcu.hex.SpaceStation> stations = new ArrayList<>();
        for (com.yisy.modules.doramcu.hex.SpaceStation station : game.getSpaceStations()) {
            if (station.getOwner() != null && station.getOwner().getId() == aiPlayer.getId()) {
                stations.add(station);
            }
        }

        // Get actual resources from the player's resource pipeline
        java.util.Map<String, Integer> resources = getActualResources(aiPlayer, game);
        int minerals = resources.getOrDefault("minerals", 100);
        int energy = resources.getOrDefault("energy", 50);
        int food = resources.getOrDefault("food", 30);
        int credits = resources.getOrDefault("credits", 50);

        return AiDecisionContext.builder()
                .player(aiPlayer)
                .game(game)
                .ownedFleets(ownedFleets)
                .enemyFleets(enemyFleets)
                .stations(stations)
                .resources(resources)
                .minerals(minerals)
                .energy(energy)
                .food(food)
                .credits(credits)
                .build();
    }

    /**
     * Gets the actual resources for a player from their resource pipeline.
     * Falls back to default values if the pipeline is not available.
     */
    private java.util.Map<String, Integer> getActualResources(Player aiPlayer, Game game) {
        // Try to get resources from the player's resource pipeline
        try {
            com.yisy.modules.doramcu.hex.ResourcePipelineManager pipeline =
                    game.getPipelineManager(aiPlayer.getEmpireId());
            if (pipeline != null) {
                java.util.Map<String, Integer> resources = new java.util.HashMap<>();

                // Get raw resources
                com.yisy.modules.doramcu.hex.ResourceStorage<com.yisy.modules.doramcu.hex.RawResourceType> rawStorage =
                        pipeline.getRawStorage();
                if (rawStorage != null) {
                    for (com.yisy.modules.doramcu.hex.RawResourceType type : com.yisy.modules.doramcu.hex.RawResourceType.values()) {
                        resources.put(type.name().toLowerCase(), rawStorage.getAmount(type));
                    }
                }

                // Get refined resources
                com.yisy.modules.doramcu.hex.ResourceStorage<com.yisy.modules.doramcu.hex.RefinedResourceType> refinedStorage =
                        pipeline.getRefinedStorage();
                if (refinedStorage != null) {
                    for (com.yisy.modules.doramcu.hex.RefinedResourceType type : com.yisy.modules.doramcu.hex.RefinedResourceType.values()) {
                        resources.put(type.name().toLowerCase(), refinedStorage.getAmount(type));
                    }
                }

                // Map common resource names for AI decision making
                int minerals = resources.getOrDefault("minerals", 0) +
                               resources.getOrDefault("iron", 0) +
                               resources.getOrDefault("aluminum", 0);
                int energy = resources.getOrDefault("energy", 0) +
                             resources.getOrDefault("gas", 0);

                resources.put("minerals", minerals);
                resources.put("energy", Math.max(energy, resources.getOrDefault("energy", 50)));
                resources.put("food", resources.getOrDefault("food", 30));
                resources.put("credits", resources.getOrDefault("credits", 50));

                return resources;
            }
        } catch (Exception e) {
            log.debug("Failed to get actual resources for player {}: {}", aiPlayer.getName(), e.getMessage());
        }

        // Fallback to default values
        return Map.of("minerals", 100, "energy", 50, "food", 30, "credits", 50);
    }

    /**
     * Executes an AiDecision by calling the appropriate GameSessionService method.
     * Records the action with the Dora AI learning service for neural network training.
     */
    private void executeDecision(String sessionId, Long sessionPlayerId, AiDecision decision,
                                  Game game, Player aiPlayer) {
        // Capture game state before action for learning (summary mode to avoid full subsector iteration)
        GameStateResponse stateBefore = gameSessionService.getGameState(sessionPlayerId, sessionId, true);

        ActionReward.Action dlAction = mapActionToDlAction(decision.getActionType());

        try {
            switch (decision.getActionType()) {
                case MOVE_FLEET -> {
                    if (decision.getFleetId() != null && decision.getTargetPosition() != null) {
                        MoveFleetRequest request = new MoveFleetRequest(
                                decision.getFleetId(),
                                decision.getTargetPosition().getX(),
                                decision.getTargetPosition().getY());
                        gameSessionService.moveFleet(sessionPlayerId, sessionId, request);
                        log.debug("AI executed MOVE_FLEET: {}", decision.getReasoning());
                    }
                }
                case ENGAGE_COMBAT -> {
                    if (decision.getFleetId() != null && decision.getTargetEnemyFleetId() != null) {
                        Fleet attacker = game.getFleet(decision.getFleetId());
                        Fleet target = game.getFleet(decision.getTargetEnemyFleetId());
                        if (attacker != null && target != null &&
                                attacker.getPosition() != null && target.getPosition() != null) {
                            if (isAdjacent(attacker.getPosition(), target.getPosition())) {
                                EngageCombatRequest request = new EngageCombatRequest(
                                        decision.getFleetId(),
                                        decision.getTargetEnemyFleetId());
                                gameSessionService.engageCombat(sessionPlayerId, sessionId, request);
                                log.debug("AI executed ENGAGE_COMBAT: {}", decision.getReasoning());
                            } else {
                                // Move toward enemy first
                                Hex3D targetPos = target.getPosition();
                                MoveFleetRequest request = new MoveFleetRequest(
                                        decision.getFleetId(),
                                        targetPos.getX(),
                                        targetPos.getY());
                                gameSessionService.moveFleet(sessionPlayerId, sessionId, request);
                                log.debug("AI moved toward enemy: {}", decision.getReasoning());
                            }
                        }
                    }
                }
                case BUILD_SHIP -> {
                    Integer fleetId = getRandomFleetId(aiPlayer);
                    if (fleetId != null && decision.getShipTypeToBuild() != null) {
                        BuildShipRequest request = new BuildShipRequest(
                                decision.getShipTypeToBuild().name(),
                                fleetId);
                        gameSessionService.buildShip(sessionPlayerId, sessionId, request);
                        log.debug("AI executed BUILD_SHIP: {}", decision.getReasoning());
                    }
                }
                case SCOUT -> {
                    if (decision.getFleetId() != null && decision.getTargetPosition() != null) {
                        MoveFleetRequest request = new MoveFleetRequest(
                                decision.getFleetId(),
                                decision.getTargetPosition().getX(),
                                decision.getTargetPosition().getY());
                        gameSessionService.moveFleet(sessionPlayerId, sessionId, request);
                        log.debug("AI executed SCOUT: {}", decision.getReasoning());
                    }
                }
                case RETREAT, FORTIFY, WAIT -> {
                    log.debug("AI executed {}: {}", decision.getActionType(), decision.getReasoning());
                }
            }
        } catch (IllegalStateException e) {
            // AI action failed (e.g., cannot afford ship, out of range) — log and continue
            log.debug("AI action {} failed for player {}: {}", decision.getActionType(), aiPlayer.getName(), e.getMessage());
        } catch (Exception e) {
            log.warn("Unexpected error executing AI action {} for player {}: {}",
                    decision.getActionType(), aiPlayer.getName(), e.getMessage(), e);
        }

        // Capture game state after action and record for learning (summary mode to avoid full subsector iteration)
        GameStateResponse stateAfter = gameSessionService.getGameState(sessionPlayerId, sessionId, true);
        recordActionForLearning(sessionId, sessionPlayerId, stateBefore, stateAfter, dlAction);
    }

    /**
     * Maps an AiDecision action type to a Dora AI ActionReward.Action.
     */
    private ActionReward.Action mapActionToDlAction(AiDecision.ActionType actionType) {
        return switch (actionType) {
            case MOVE_FLEET -> ActionReward.Action.MOVE_FLEET;
            case ENGAGE_COMBAT -> ActionReward.Action.ENGAGE_COMBAT;
            case BUILD_SHIP -> ActionReward.Action.BUILD_SHIP;
            case SCOUT -> ActionReward.Action.SCOUT_AREA;
            case RETREAT -> ActionReward.Action.RETREAT;
            case FORTIFY -> ActionReward.Action.FORTIFY_PLANET;
            case WAIT -> ActionReward.Action.WAIT;
            default -> ActionReward.Action.WAIT;
        };
    }

    /**
     * Records an AI action with the Dora AI learning service.
     * Computes the reward based on the game state change and stores it for training.
     */
    private void recordActionForLearning(String sessionId, Long sessionPlayerId,
                                          GameStateResponse stateBefore, GameStateResponse stateAfter,
                                          ActionReward.Action action) {
        try {
            // Convert to DL module game states
            com.yisy.modules.doradl.dto.GameStateResponse dlStateBefore = convertToDlGameState(stateBefore);
            com.yisy.modules.doradl.dto.GameStateResponse dlStateAfter = convertToDlGameState(stateAfter);

            // Compute reward based on outcome
            double reward = doraAiLearningService.computeReward(dlStateBefore, dlStateAfter, sessionPlayerId);

            // Record the action for learning
            doraAiLearningService.recordAction(dlStateBefore, sessionPlayerId, action, reward);

            log.debug("Recorded AI action for learning: {} (reward: {:.3f})", action, reward);
        } catch (Exception e) {
            log.warn("Failed to record AI action for learning: {}", e.getMessage());
        }
    }

    /**
     * Gets the DL AI predicted action for a player.
     */
    private ActionReward.Action getDlPredictedAction(String sessionId, Long playerId) {
        try {
            com.yisy.modules.doramcu.dto.GameStateResponse gameState = gameSessionService.getGameState(playerId, sessionId);
            if (gameState == null) {
                return ActionReward.Action.WAIT;
            }
            // Convert to DL module's GameStateResponse
            com.yisy.modules.doradl.dto.GameStateResponse dlGameState = convertToDlGameState(gameState);
            Map<String, Object> prediction = dlGameAiService.predictAction(dlGameState, playerId);
            String actionName = (String) prediction.get("action");
            return ActionReward.Action.valueOf(actionName);
        } catch (Exception e) {
            log.warn("DL AI prediction failed for player {}: {}", playerId, e.getMessage());
            return ActionReward.Action.WAIT;
        }
    }

    /**
     * Converts main module GameStateResponse to DL module GameStateResponse.
     */
    private com.yisy.modules.doradl.dto.GameStateResponse convertToDlGameState(
            com.yisy.modules.doramcu.dto.GameStateResponse source) {
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

    /**
     * Finds the nearest enemy fleet to the AI player's fleets.
     */
    private Fleet findNearestEnemyFleet(Game game, Player aiPlayer) {
        Fleet nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<Integer, Fleet> entry : game.getFleets().entrySet()) {
            Fleet fleet = entry.getValue();
            // Skip own fleets
            if (aiPlayer.getOwnedFleetIds().contains(entry.getKey())) {
                continue;
            }

            // Check if the fleet has ships (is active)
            if (fleet.getShips().isEmpty()) {
                continue;
            }

            // Calculate distance
            Hex3D aiPos = null;
            for (Integer fleetId : aiPlayer.getOwnedFleetIds()) {
                Fleet f = game.getFleet(fleetId);
                if (f != null && f.getPosition() != null) {
                    aiPos = f.getPosition();
                    break;
                }
            }

            if (aiPos != null && fleet.getPosition() != null) {
                int distance = hexDistance(aiPos, fleet.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = fleet;
                }
            }
        }

        return nearest;
    }

    /**
     * Calculates the distance between two hex positions using cubic coordinates.
     */
    private int hexDistance(Hex3D a, Hex3D b) {
        return (Math.abs(a.getX() - b.getX()) +
                Math.abs(a.getY() - b.getY()) +
                Math.abs(a.getZ() - b.getZ())) / 2;
    }

    /**
     * Checks if two hex positions are adjacent.
     */
    private boolean isAdjacent(Hex3D a, Hex3D b) {
        if (a == null || b == null) {
            return false;
        }
        return hexDistance(a, b) <= 1;
    }

    /**
     * Gets a random fleet ID from the player's owned fleets.
     */
    private Integer getRandomFleetId(Player player) {
        Set<Integer> fleetIds = player.getOwnedFleetIds();
        if (fleetIds.isEmpty()) {
            return null;
        }
        List<Integer> list = new ArrayList<>(fleetIds);
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Gets the best fleet ID for the player (largest fleet by ship count).
     */
    private Integer getBestFleetId(Player player, Game game) {
        Set<Integer> fleetIds = player.getOwnedFleetIds();
        if (fleetIds.isEmpty()) {
            return null;
        }

        Integer bestId = null;
        int maxShips = -1;

        for (Integer fleetId : fleetIds) {
            Fleet fleet = game.getFleet(fleetId);
            if (fleet != null) {
                int shipCount = fleet.getShips().size();
                if (shipCount > maxShips) {
                    maxShips = shipCount;
                    bestId = fleetId;
                }
            }
        }

        return bestId;
    }

    /**
     * Finds a player by ID in the game.
     */
    private Player findPlayerById(Game game, int playerId) {
        return game.getPlayers().stream()
                .filter(p -> p.getId() == playerId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the number of active AI cores.
     */
    public int getAiCoreCount() {
        return aiCoreMap.size();
    }

    /**
     * Finds a player by long ID in the game (for AI players with IDs > Integer.MAX_VALUE).
     */
    private Player findPlayerById(Game game, long playerId) {
        return game.getPlayers().stream()
                .filter(p -> (long) p.getId() == playerId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Generates a unique player ID for an AI player.
     * Uses IDs starting from 1000 to avoid conflicts with human players (0-999).
     */
    private long generateAiPlayerId() {
        // AI player IDs start from 1000 to avoid conflicts with human players
        return 1000 + random.nextInt(9000);
    }
}
