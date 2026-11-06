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

        // Get the actual Game player ID (which is players.size() - 1)
        session = gameSessionService.getGameSession(sessionId);
        if (session != null) {
            session.getLock().writeLock().lock();
            try {
                Game game = session.getGame();
                // The last player added is our AI
                List<Player> players = game.getPlayers();
                Player aiPlayer = players.get(players.size() - 1);

                // Create starting fleets
                game.createStartingFleets(aiPlayer);
                log.debug("AI player {} created starting fleets with Game ID: {}", name, aiPlayer.getId());

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
        if (index >= 0 && index < session.getGame().getPlayers().size()) {
            return session.getGame().getPlayers().get(index).getId();
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

        GameStateResponse state = gameSessionService.getGameState(null, sessionId);

        // Simultaneous turns: execute actions for all AI players
        for (Map.Entry<Long, Difficulty> entry : aiDifficulties.entrySet()) {
            Long sessionPlayerId = entry.getKey();
            Difficulty difficulty = entry.getValue();

            log.debug("AI player (session ID: {}, difficulty: {}) taking turn in session: {}",
                    sessionPlayerId, difficulty, sessionId);

            // Find the game player ID for this session player
            List<Long> sessionPlayers = new ArrayList<>(session.getPlayerIds());
            int index = sessionPlayers.indexOf(sessionPlayerId);
            if (index < 0 || index >= session.getGame().getPlayers().size()) {
                continue;
            }
            int gamePlayerId = session.getGame().getPlayers().get(index).getId();

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
            Game game = session.getGame();
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

        // Resources are managed by ResourcePipelineManager; use defaults for AI decisions
        // The AI core uses these values to determine build affordability
        return AiDecisionContext.builder()
                .player(aiPlayer)
                .game(game)
                .ownedFleets(ownedFleets)
                .enemyFleets(enemyFleets)
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();
    }

    /**
     * Executes an AiDecision by calling the appropriate GameSessionService method.
     * Records the action with the Dora AI learning service for neural network training.
     */
    private void executeDecision(String sessionId, Long sessionPlayerId, AiDecision decision,
                                  Game game, Player aiPlayer) {
        // Capture game state before action for learning
        GameStateResponse stateBefore = gameSessionService.getGameState(sessionPlayerId, sessionId);

        ActionReward.Action dlAction = mapActionToDlAction(decision.getActionType());

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

        // Capture game state after action and record for learning
        GameStateResponse stateAfter = gameSessionService.getGameState(sessionPlayerId, sessionId);
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
     * Easy AI: makes random decisions with limited strategy.
     */
    private void performEasyActions(Game game, Player aiPlayer,
                                     String sessionId, Long playerId) {
        // Randomly decide to build a ship or move a fleet
        int action = random.nextInt(3);

        switch (action) {
            case 0 -> {
                // Build a random ship type
                ShipType[] types = {ShipType.FRIGATE, ShipType.DESTROYER};
                ShipType type = types[random.nextInt(types.length)];
                Integer fleetId = getRandomFleetId(aiPlayer);
                if (fleetId != null) {
                    BuildShipRequest request = new BuildShipRequest(type.name(), fleetId);
                    gameSessionService.buildShip(playerId, sessionId, request);
                    log.debug("AI Easy: built {} in fleet {}", type, fleetId);
                }
            }
            case 1 -> {
                // Move a random fleet to a random position
                Integer fleetId = getRandomFleetId(aiPlayer);
                if (fleetId != null) {
                    int row = random.nextInt(10) - 5;
                    int col = random.nextInt(10) - 5;
                    MoveFleetRequest request = new MoveFleetRequest(fleetId, row, col);
                    gameSessionService.moveFleet(playerId, sessionId, request);
                    log.debug("AI Easy: moved fleet {} to ({}, {})", fleetId, row, col);
                }
            }
            default -> {
                // Do nothing this turn
                log.debug("AI Easy: skipped turn");
            }
        }
    }

    /**
     * Medium AI: uses DL AI predictions blended with heuristics for balanced strategy.
     */
    private void performMediumActions(Game game, Player aiPlayer,
                                       String sessionId, Long playerId) {
        // Get DL AI predicted action
        ActionReward.Action predictedAction = getDlPredictedAction(sessionId, playerId);
        log.debug("AI Medium: DL predicted action: {}", predictedAction);

        switch (predictedAction) {
            case BUILD_SHIP -> {
                // Flat resources removed – use pipeline modules
                }
            case MOVE_FLEET, EXPAND_COLONY -> {
                Fleet target = findNearestEnemyFleet(game, aiPlayer);
                Integer fleetId = getBestFleetId(aiPlayer, game);
                if (fleetId != null) {
                    if (target != null && target.getPosition() != null) {
                        // Move toward enemy
                        Hex3D targetPos = target.getPosition();
                        MoveFleetRequest request = new MoveFleetRequest(
                                fleetId, targetPos.getX(), targetPos.getY());
                        gameSessionService.moveFleet(playerId, sessionId, request);
                        log.debug("AI Medium: moved fleet {} toward enemy at ({}, {})",
                                fleetId, targetPos.getX(), targetPos.getY());
                    } else {
                        // Expand to random position near center
                        int row = random.nextInt(10) - 5;
                        int col = random.nextInt(10) - 5;
                        MoveFleetRequest request = new MoveFleetRequest(fleetId, row, col);
                        gameSessionService.moveFleet(playerId, sessionId, request);
                        log.debug("AI Medium: moved fleet {} to ({}, {}) for expansion", fleetId, row, col);
                    }
                }
            }
            case ENGAGE_COMBAT -> {
                Fleet target = findNearestEnemyFleet(game, aiPlayer);
                if (target != null) {
                    Integer attackerFleetId = getBestFleetId(aiPlayer, game);
                    if (attackerFleetId != null) {
                        Fleet attacker = game.getFleet(attackerFleetId);
                        if (attacker != null && target.getPosition() != null) {
                            if (isAdjacent(attacker.getPosition(), target.getPosition())) {
                                EngageCombatRequest request = new EngageCombatRequest(
                                        attackerFleetId, target.getId());
                                gameSessionService.engageCombat(playerId, sessionId, request);
                                log.debug("AI Medium: engaged combat with fleet {}", target.getId());
                            } else {
                                // Move toward enemy first
                                Hex3D targetPos = target.getPosition();
                                MoveFleetRequest request = new MoveFleetRequest(
                                        attackerFleetId, targetPos.getX(), targetPos.getY());
                                gameSessionService.moveFleet(playerId, sessionId, request);
                                log.debug("AI Medium: moved fleet {} toward enemy at ({}, {})",
                                        attackerFleetId, targetPos.getX(), targetPos.getY());
                            }
                        }
                    }
                }
            }
            case WAIT -> {
                log.debug("AI Medium: waiting this turn");
            }
        }
    }

    /**
     * Hard AI: uses DL AI predictions for aggressive expansion with optimal resource usage.
     */
    private void performHardActions(Game game, Player aiPlayer,
                                     String sessionId, Long playerId) {
        // Get DL AI predicted action
        ActionReward.Action predictedAction = getDlPredictedAction(sessionId, playerId);
        log.debug("AI Hard: DL predicted action: {}", predictedAction);

        // Create new fleet if no fleets exist
        if (aiPlayer.getOwnedFleetIds().isEmpty()) {
            game.createStartingFleets(aiPlayer);
            log.debug("AI Hard: created starting fleets");
            return;
        }

        switch (predictedAction) {
            case BUILD_SHIP -> {
                // Flat resources removed – use pipeline modules
            }
            case MOVE_FLEET, EXPAND_COLONY -> {
                Fleet target = findNearestEnemyFleet(game, aiPlayer);
                Integer fleetId = getBestFleetId(aiPlayer, game);
                if (fleetId != null) {
                    if (target != null && target.getPosition() != null) {
                        // Move toward enemy
                        Hex3D targetPos = target.getPosition();
                        MoveFleetRequest request = new MoveFleetRequest(
                                fleetId, targetPos.getX(), targetPos.getY());
                        gameSessionService.moveFleet(playerId, sessionId, request);
                        log.debug("AI Hard: moved fleet {} toward enemy at ({}, {})",
                                fleetId, targetPos.getX(), targetPos.getY());
                    } else {
                        // Expand to random position
                        int row = random.nextInt(10) - 5;
                        int col = random.nextInt(10) - 5;
                        MoveFleetRequest request = new MoveFleetRequest(fleetId, row, col);
                        gameSessionService.moveFleet(playerId, sessionId, request);
                        log.debug("AI Hard: moved fleet {} to ({}, {}) for expansion", fleetId, row, col);
                    }
                }
            }
            case ENGAGE_COMBAT -> {
                Fleet target = findNearestEnemyFleet(game, aiPlayer);
                if (target != null) {
                    Integer attackerFleetId = getBestFleetId(aiPlayer, game);
                    if (attackerFleetId != null) {
                        Fleet attacker = game.getFleet(attackerFleetId);
                        if (attacker != null && target.getPosition() != null) {
                            if (isAdjacent(attacker.getPosition(), target.getPosition())) {
                                EngageCombatRequest request = new EngageCombatRequest(
                                        attackerFleetId, target.getId());
                                gameSessionService.engageCombat(playerId, sessionId, request);
                                log.debug("AI Hard: engaged combat with fleet {}", target.getId());
                            } else {
                                // Move toward enemy first
                                Hex3D targetPos = target.getPosition();
                                MoveFleetRequest request = new MoveFleetRequest(
                                        attackerFleetId, targetPos.getX(), targetPos.getY());
                                gameSessionService.moveFleet(playerId, sessionId, request);
                                log.debug("AI Hard: moved fleet {} toward enemy at ({}, {})",
                                        attackerFleetId, targetPos.getX(), targetPos.getY());
                            }
                        }
                    }
                }
            }
            case WAIT -> {
                log.debug("AI Hard: waiting this turn");
            }
        }
    }

    /**
     * Chooses a ship type based on available resources (Medium difficulty).
     */
    private ShipType chooseShipTypeMedium(int minerals, int energy) {
        AiDifficultyConfig.Medium medium = aiDifficultyConfig.getMedium();
        if (minerals >= medium.getCruiserMinerals() && energy >= medium.getCruiserEnergy()) {
            return ShipType.CRUISER;
        } else if (minerals >= medium.getDestroyerMinerals() && energy >= medium.getDestroyerEnergy()) {
            return ShipType.DESTROYER;
        } else {
            return ShipType.FRIGATE;
        }
    }

    /**
     * Chooses the optimal ship type based on resources and game state (Hard difficulty).
     */
    private ShipType chooseShipTypeHard(int minerals, int energy, int food) {
        AiDifficultyConfig.Hard hard = aiDifficultyConfig.getHard();
        // Prefer larger ships when resources allow
        if (minerals >= hard.getBattleshipMinerals() && energy >= hard.getBattleshipEnergy()) {
            return ShipType.BATTLESHIP;
        } else if (minerals >= hard.getCruiserMinerals() && energy >= hard.getCruiserEnergy()) {
            return ShipType.CRUISER;
        } else if (minerals >= hard.getDestroyerMinerals() && energy >= hard.getDestroyerEnergy()) {
            return ShipType.DESTROYER;
        } else {
            return ShipType.FRIGATE;
        }
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
