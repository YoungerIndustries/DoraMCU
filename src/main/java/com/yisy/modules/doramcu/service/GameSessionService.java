package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.exception.NotYourTurnException;
import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.model.PersistedGameSession;
import com.yisy.modules.doramcu.model.SolarSystem;
import com.yisy.modules.doramcu.repository.PersistedGameSessionRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GameSessionService {

    private static final Logger log = LoggerFactory.getLogger(GameSessionService.class);

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SolarSystemRepository solarSystemRepository;
    private final PersistedGameSessionRepository persistedGameSessionRepository;
    private final TurnScheduler turnScheduler;

    public GameSessionService(SolarSystemRepository solarSystemRepository,
                              PersistedGameSessionRepository persistedGameSessionRepository,
                              @Lazy TurnScheduler turnScheduler) {
        this.solarSystemRepository = solarSystemRepository;
        this.persistedGameSessionRepository = persistedGameSessionRepository;
        this.turnScheduler = turnScheduler;
    }

    /**
     * Creates a new game session without an initial human player.
     * <p>
     * Used by {@link GalaxyCreationService} to create a session that AI players can join first,
     * before human players join dynamically.
     * </p>
     *
     * @param sessionName the name of the session
     * @return the session response with a generated session ID
     */
    public CreateSessionResponse createDefaultSession(String sessionName) {
        String sessionId = UUID.randomUUID().toString();
        log.debug("Creating default game session: {}", sessionName);

        Game game = new Game(sessionName);
        GameSession session = new GameSession(sessionId, sessionName, game);
        sessions.put(sessionId, session);

        // Persist session metadata to database
        persistSession(sessionId, sessionName, session.getPlayerIds());

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.SESSION_CREATED, sessionId,
                Map.of("sessionName", sessionName)));

        return new CreateSessionResponse(sessionId, sessionName, null, null);
    }

    public CreateSessionResponse createSession(Long playerId, String playerName, String sessionName) {
        String sessionId = UUID.randomUUID().toString();
        log.debug("Creating new game session: {} for player: {} ({})", sessionId, playerName, playerId);

        Game game = new Game(sessionName);
        // Create player with the session player ID so it matches the hex.Player identity
        Player gamePlayer = game.createPlayer(playerId.intValue(), playerName);
        // Create a new unique empire for this player
        int newEmpireId = game.getNextEmpireIdAndIncrement();
        gamePlayer.setEmpireId(newEmpireId);
        log.info("Player {} (id={}) created new empire id={}", playerName, playerId, newEmpireId);
        game.createPlanet(
                "Home-" + playerName, PlanetSize.MEDIUM, MineralQuality.RICH, gamePlayer);

        GameSession session = new GameSession(sessionId, sessionName, game);
        session.addPlayer(playerId);
        sessions.put(sessionId, session);

        // Persist session metadata to database
        persistSession(sessionId, sessionName, session.getPlayerIds());

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.PLAYER_JOINED, sessionId,
                Map.of("playerId", playerId, "playerName", playerName)));

        return new CreateSessionResponse(sessionId, sessionName, playerId, playerName);
    }

    public JoinSessionResponse joinSession(Long playerId, String playerName, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        log.debug("Player {} ({}) joining session: {}", playerName, playerId, sessionId);

        // If player is already in the session, throw an exception
        if (session.getPlayerIds().contains(playerId)) {
            log.warn("Player {} is already in session: {}", playerId, sessionId);
            throw new IllegalStateException("Player is already in this session");
        }

        Game game = session.getGame();
        session.getLock().writeLock().lock();
        try {
            // Create player with the session player ID so it matches the hex.Player identity
            Player gamePlayer = game.createPlayer(playerId.intValue(), playerName);
            // Create a new unique empire for this player
            int newEmpireId = game.getNextEmpireIdAndIncrement();
            gamePlayer.setEmpireId(newEmpireId);
            log.info("Player {} (id={}) created new empire id={}", playerName, playerId, newEmpireId);
            game.createPlanet("Home-" + playerName, PlanetSize.MEDIUM, MineralQuality.RICH, gamePlayer);
        } finally {
            session.getLock().writeLock().unlock();
        }

        session.addPlayer(playerId);

        // Update persisted session with new player
        persistSession(sessionId, session.getName(), session.getPlayerIds());

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.PLAYER_JOINED, sessionId,
                Map.of("playerId", playerId, "playerName", playerName)));

        return new JoinSessionResponse(sessionId, session.getName(), playerId, playerName);
    }

    public void leaveSession(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            return;
        }

        log.debug("Player {} leaving session: {}", playerId, sessionId);
        session.removePlayer(playerId);

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.PLAYER_LEFT, sessionId,
                Map.of("playerId", playerId)));

        if (session.getPlayerIds().isEmpty()) {
            sessions.remove(sessionId);
            // Remove persisted session when last player leaves
            if (persistedGameSessionRepository != null) {
                persistedGameSessionRepository.findBySessionId(sessionId).ifPresent(persistedGameSessionRepository::delete);
            }
            log.debug("Removed empty session: {}", sessionId);
        } else {
            // Update player list in persisted session
            persistSession(sessionId, session.getName(), session.getPlayerIds());
        }
    }

    public List<Map<String, Object>> listSessions() {
        return sessions.values().stream().map(session -> {
            Map<String, Object> info = new HashMap<>();
            info.put("sessionId", session.getSessionId());
            info.put("name", session.getName());
            info.put("playerCount", session.getPlayerIds().size());
            info.put("players", session.getPlayerIds());
            return info;
        }).collect(Collectors.toList());
    }

    /**
     * Returns the first active game session, or null if no sessions exist.
     * <p>
     * Used to provide a default session for auto-login in development mode.
     * </p>
     *
     * @return the first active session, or null if none exist
     */
    public GameSession getDefaultSession() {
        if (sessions.isEmpty()) {
            return null;
        }
        return sessions.values().iterator().next();
    }

    /**
     * Returns the session with the given name, or null if not found.
     *
     * @param name the session name
     * @return the matching session, or null
     */
    public GameSession getSessionByName(String name) {
        return sessions.values().stream()
                .filter(s -> name.equals(s.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the resource pipeline summary for the given player.
     * Returns null if the session or pipeline is not found.
     */
    public ResourcePipelineSummary getPipelineSummary(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        Game game = session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            return null;
        }
        ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
        if (pipeline == null) {
            return null;
        }
        return buildPipelineSummary(pipeline);
    }

    /**
     * Returns detailed pipeline information for the given player.
     * Returns null if the session or pipeline is not found.
     */
    public PipelineDetailsDto getPipelineDetails(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        Game game = session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            return null;
        }
        ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
        if (pipeline == null) {
            return null;
        }
        return buildPipelineDetails(pipeline);
    }

    /**
     * Builds a comprehensive PipelineDetailsDto from the given ResourcePipelineManager.
     */
    private PipelineDetailsDto buildPipelineDetails(ResourcePipelineManager pipeline) {
        PipelineDetailsDto details = new PipelineDetailsDto();
        details.setSummary(buildPipelineSummary(pipeline));

        // Mining operations
        List<MiningOperationDto> miningDtos = new ArrayList<>();
        for (MiningOperation op : pipeline.getMiningOperations().values()) {
            CelestialBody body = op.getCelestialBody();
            MiningOperationDto dto = new MiningOperationDto(
                    body.getId(),
                    body.getName(),
                    body.getClass().getSimpleName(),
                    op.getOwnerEmpireId(),
                    op.getMiningBonus(),
                    enumMapToStringMap(op.getProductionRate()),
                    enumMapToStringMap(op.getStorage().getInventory()),
                    op.getStorage().getTotalCapacity()
            );
            miningDtos.add(dto);
        }
        details.setMiningOperations(miningDtos);

        // Transport orders
        List<TransportOrderDto> transportDtos = new ArrayList<>();
        for (TransportOrder order : pipeline.getActiveTransportOrders()) {
            TransportOrderDto dto = new TransportOrderDto(
                    order.getId(),
                    order.getOwnerEmpireId(),
                    order.getSourceBodyId(),
                    order.getDestinationBodyId(),
                    order.getResourceType().name(),
                    order.getTotalAmount(),
                    order.getAmountPerTurn(),
                    order.getTurnsTotal(),
                    order.getTurnsRemaining(),
                    order.getAmountLoaded(),
                    order.getProgress(),
                    order.isCompleted(),
                    order.isCancelled(),
                    order.isInProgress()
            );
            transportDtos.add(dto);
        }
        details.setTransportOrders(transportDtos);

        // Construction orders
        List<ConstructionOrderDto> constructionDtos = new ArrayList<>();
        for (ConstructionOrder order : pipeline.getConstructionQueue()) {
            ConstructionOrderDto.ConstructionType type;
            String stationType = null;
            String planetFacility = null;
            Integer targetPlanetId = null;
            Integer targetSystemId = null;

            if (order.getConstructionType() == ConstructionOrder.ConstructionType.SPACE_STATION) {
                type = ConstructionOrderDto.ConstructionType.SPACE_STATION;
                stationType = order.getStationType().name();
                targetSystemId = order.getTargetSystem() != null ? order.getTargetSystem().getId() : null;
            } else {
                type = ConstructionOrderDto.ConstructionType.PLANET_FACILITY;
                planetFacility = order.getPlanetFacility().name();
                targetPlanetId = order.getTargetPlanetId();
            }

            ConstructionOrderDto dto = new ConstructionOrderDto(
                    order.getOrderId(),
                    order.getOwnerEmpireId(),
                    type,
                    stationType,
                    planetFacility,
                    targetPlanetId,
                    targetSystemId,
                    enumMapToStringMap(order.getModuleRequirements()),
                    order.getTotalTurns(),
                    order.getTurnsCompleted(),
                    order.getProgress(),
                    order.isCompleted(),
                    order.isCancelled()
            );
            constructionDtos.add(dto);
        }
        details.setConstructionOrders(constructionDtos);

        // Processing facilities
        List<ProcessingFacilityDto> processingDtos = new ArrayList<>();
        for (ProcessingFacility facility : pipeline.getProcessingFacilities()) {
            ProcessingFacilityDto dto = new ProcessingFacilityDto();
            ProcessingRecipe recipe = facility.getCurrentRecipe();
            if (recipe != null) {
                // Get the first input resource type for display
                Map<RawResourceType, Integer> inputs = recipe.getInputs();
                if (!inputs.isEmpty()) {
                    dto.setInputResource(inputs.keySet().iterator().next().name());
                    dto.setInputPerTurn(inputs.values().iterator().next());
                }
                dto.setOutputResource(recipe.getOutput().name());
                dto.setOutputPerTurn(recipe.getOutputAmount());
            }
            dto.setProcessing(facility.isProcessing());
            dto.setTurnsRemaining(facility.getTurnsRemaining());
            processingDtos.add(dto);
        }
        details.setProcessingFacilities(processingDtos);

        // Module assemblers
        List<ModuleAssemblerDto> assemblerDtos = new ArrayList<>();
        for (ModuleAssembler assembler : pipeline.getAssemblers()) {
            ModuleAssemblerDto dto = new ModuleAssemblerDto();
            dto.setModuleType(assembler.getCurrentModule() != null ? assembler.getCurrentModule().name() : null);
            dto.setAssembling(assembler.isAssembling());
            dto.setTurnsRemaining(assembler.getTurnsRemaining());
            assemblerDtos.add(dto);
        }
        details.setAssemblers(assemblerDtos);

        return details;
    }

    public GameStateResponse getGameState(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        updateSessionActivity(sessionId);

        Game game = session.getGame();
        session.getLock().readLock().lock();
        try {
            // Simultaneous turns: all players can act at any time
            List<PlayerSummary> players = buildPlayerSummaries(game, session);
            List<SubSectorInfo> subSectors = buildSubSectorInfo(game);

            GameStateResponse response = new GameStateResponse(
                    sessionId,
                    game.getCurrentTurn(),
                    null,  // No current player in simultaneous turns
                    null,  // No current player name
                    players,
                    true   // All players can act simultaneously
            );
            response.setSubSectors(subSectors);

            // Build resource pipeline summary, fleet list, and empire summary for the requesting player
            final Integer[] playerEmpireIdHolder = new Integer[1];
            if (playerId != null) {
                Player player = findPlayerBySessionId(game, session, playerId);
                if (player != null) {
                    playerEmpireIdHolder[0] = player.getEmpireId();
                    ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
                    if (pipeline != null) {
                        response.setPipelineSummary(buildPipelineSummary(pipeline));
                    }
                    response.setFleets(buildFleetList(player, game));
                    response.setEmpireSummary(buildEmpireSummary(player));
                }
            }

            // Mark contested subsectors (any empire other than the requesting player owns planets here)
            final Integer playerEmpireId = playerEmpireIdHolder[0];
            if (playerEmpireId != null) {
                for (SubSectorInfo ss : subSectors) {
                    if (ss.getOwnerEmpireIds() != null && !ss.getOwnerEmpireIds().isEmpty()) {
                        boolean hasOtherEmpire = ss.getOwnerEmpireIds().stream()
                                .anyMatch(id -> !id.equals(playerEmpireId));
                        ss.setContested(hasOtherEmpire);
                    }
                }
            }

            // Populate turn timing information for timed turns UI
            response.setTurnIntervalSeconds(turnScheduler.getTurnIntervalSeconds());
            response.setAccumulatedSeconds(turnScheduler.getAccumulatedSeconds(sessionId));
            response.setSchedulerEnabled(turnScheduler.isEnabled());

            return response;
        } finally {
            session.getLock().readLock().unlock();
        }
    }

    public GameStateResponse moveFleet(Long playerId, String sessionId, MoveFleetRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        Game game = session.getGame();
        session.getLock().writeLock().lock();
        try {
            // Simultaneous turns: any player can move their own fleets at any time
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, sessionId);
                throw new IllegalStateException("Player not found in session");
            }

            Integer fleetId = request.getFleetId();
            if (!player.getOwnedFleetIds().contains(fleetId)) {
                log.warn("Player {} tried to move fleet {} which they don't own", playerId, fleetId);
                throw new IllegalStateException("Fleet not found or not owned");
            }

            Fleet fleet = game.getFleets().get(fleetId);
            if (fleet == null) {
                throw new IllegalStateException("Fleet not found");
            }

            Hex3D target = new Hex3D(request.getTargetRow(), request.getTargetCol(), 0);
            Integer targetSystemId = request.getSolarSystemId();

            // Determine movement type: within-system (MDrive) or between-system (JDrive)
            if (targetSystemId == null) {
                // Within-system movement using MDrive
                Hex3D currentPos = fleet.getPosition();
                if (currentPos != null) {
                    int distance = currentPos.distance(target);
                    int mDriveRange = fleet.getEffectiveFleetMDriveSpeed();
                    if (distance > mDriveRange) {
                        log.warn("Player {} tried to move fleet {} beyond MDrive range: distance={}, range={}",
                                playerId, fleetId, distance, mDriveRange);
                        throw new IllegalStateException(
                                "Target is out of MDrive range: distance " + distance + " exceeds range " + mDriveRange);
                    }
                }
                fleet.setPosition(target);
                log.debug("Player {} moved fleet {} within system to ({}, {}) using MDrive",
                        playerId, fleetId, request.getTargetRow(), request.getTargetCol());
            } else {
                // Between-system movement using JDrive
                if (!game.getSolarSystems().containsKey(targetSystemId)) {
                    log.warn("Player {} tried to jump fleet {} to non-existent system {}",
                            playerId, fleetId, targetSystemId);
                    throw new IllegalStateException("Target solar system not found: " + targetSystemId);
                }

                int jDriveRange = fleet.getEffectiveFleetJDriveSpeed();
                if (jDriveRange <= 0) {
                    log.warn("Player {} tried to jump fleet {} but JDrive is not operational",
                            playerId, fleetId);
                    throw new IllegalStateException("JDrive is not operational — fleet health too low");
                }

                // Verify hyperlane connection exists
                boolean hasHyperlane = false;
                for (Galaxy galaxy : game.getGalaxies().values()) {
                    for (Hyperlane hyperlane : galaxy.getAllHyperlanes()) {
                        if (hyperlane.connectsTo(targetSystemId)) {
                            hasHyperlane = true;
                            break;
                        }
                    }
                    if (hasHyperlane) break;
                }

                if (!hasHyperlane) {
                    log.warn("Player {} tried to jump fleet {} but no hyperlane connects to system {}",
                            playerId, fleetId, targetSystemId);
                    throw new IllegalStateException("No hyperlane connection to target system");
                }

                // Update fleet's system and position
                fleet.setPosition(target);
                log.debug("Player {} jumped fleet {} to system {} at ({}, {}) using JDrive",
                        playerId, fleetId, targetSystemId, request.getTargetRow(), request.getTargetCol());
            }

            broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                    Map.of("action", "MOVE_FLEET", "playerId", playerId, "fleetId", fleetId,
                            "targetRow", request.getTargetRow(), "targetCol", request.getTargetCol(),
                            "solarSystemId", targetSystemId != null ? targetSystemId : "current")));

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buildShip(Long playerId, String sessionId, BuildShipRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        Game game = session.getGame();
        session.getLock().writeLock().lock();
        try {
            // Simultaneous turns: any player can build ships at any time
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, sessionId);
                throw new IllegalStateException("Player not found in session");
            }

            ShipType shipType;
            try {
                shipType = ShipType.valueOf(request.getShipType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid ship type: " + request.getShipType());
            }

            Ship ship = new Ship(0, shipType);
            Integer targetFleetId = request.getTargetFleetId();
            if (targetFleetId != null) {
                if (!player.getOwnedFleetIds().contains(targetFleetId)) {
                    throw new IllegalStateException("Fleet not found or not owned");
                }
            }

            // Get the resource pipeline manager for this player's empire
            ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
            if (pipeline == null) {
                log.warn("Player {} has no resource pipeline initialized", player.getName());
                throw new IllegalStateException("Resource pipeline not initialized for player");
            }

            // Queue the ship build using pipeline modules
            ShipOrder order = game.queueShipBuild(player, ship,
                    targetFleetId != null ? targetFleetId : 0, pipeline);
            if (order == null) {
                log.warn("Player {} cannot afford ship {} with current modules",
                        player.getName(), request.getShipType());
                throw new IllegalStateException("Insufficient construction modules for ship build");
            }

            log.info("Ship build queued for {} (pipeline modules): {}", player.getName(), request.getShipType());

            log.debug("Player {} queued build for ship type: {} in fleet: {}", playerId, request.getShipType(), targetFleetId);

            broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                    Map.of("action", "BUILD_SHIP", "playerId", playerId, "shipType", request.getShipType(),
                            "targetFleetId", targetFleetId)));

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse engageCombat(Long playerId, String sessionId, EngageCombatRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        Game game = session.getGame();
        session.getLock().writeLock().lock();
        try {
            // Simultaneous turns: any player can engage combat at any time
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, sessionId);
                throw new IllegalStateException("Player not found in session");
            }

            Integer attackerFleetId = request.getAttackerFleetId();
            if (!player.getOwnedFleetIds().contains(attackerFleetId)) {
                throw new IllegalStateException("Attacker fleet not found or not owned");
            }

            Fleet attacker = game.getFleets().get(attackerFleetId);
            Fleet defender = game.getFleets().get(request.getDefenderFleetId());
            if (attacker == null || defender == null) {
                throw new IllegalStateException("Fleet not found");
            }

            Battle battle = game.createBattle(attacker, defender, attacker.getPosition());
            Battle.BattleResult result = battle.fightToCompletion();

            log.debug("Player {} engaged combat: fleet {} vs fleet {}, result: {}",
                    playerId, attackerFleetId, request.getDefenderFleetId(),
                    result != null ? result.toString() : "null");

            broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                    Map.of("action", "ENGAGE_COMBAT", "playerId", playerId,
                            "attackerFleetId", attackerFleetId,
                            "defenderFleetId", request.getDefenderFleetId())));

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse advanceTurn(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        Game game = session.getGame();
        session.getLock().writeLock().lock();
        try {
            // Simultaneous turns: any player can advance the turn
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, sessionId);
                throw new IllegalStateException("Player not found in session");
            }

            game.nextTurn();
            // Simultaneous turns: no nextPlayer() cycling - all players act concurrently

            log.debug("Turn advanced to {} in session: {} (simultaneous turns)",
                    game.getCurrentTurn(), sessionId);

            broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.TURN_CHANGED, sessionId,
                    Map.of("turn", game.getCurrentTurn(),
                            "autoAdvanced", false)));

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public void broadcastEvent(String sessionId, GameEvent event) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return;
        }

        for (SseEmitter emitter : session.getEmitters().values()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("game-event")
                        .data(event));
            } catch (IOException e) {
                log.warn("Failed to send event to emitter in session: {}", sessionId, e);
            }
        }
    }

    public GameSession getGameSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Finds the game Player corresponding to a session player ID.
     * Maps session player IDs to game players by index position.
     *
     * @param game the game instance
     * @param session the game session
     * @param playerId the session player ID
     * @return the corresponding game player, or null if not found
     */
    private Player findPlayerBySessionId(Game game, GameSession session, Long playerId) {
        List<Long> sessionPlayers = new java.util.ArrayList<>(session.getPlayerIds());
        int index = sessionPlayers.indexOf(playerId);
        if (index < 0 || index >= game.getPlayers().size()) {
            return null;
        }
        return game.getPlayers().get(index);
    }

    /**
     * Returns all active game sessions.
     */
    public Map<String, GameSession> getAllSessions() {
        return sessions;
    }

    /**
     * Persist session metadata and full game state to the database.
     * Creates or updates the persisted session record.
     * No-op if the repository is not available (e.g., in unit tests).
     */
    private void persistSession(String sessionId, String sessionName, Set<Long> playerIds) {
        if (persistedGameSessionRepository == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        // Serialize the full game state to JSON
        final String[] gameStateJson = new String[1];
        GameSession session = sessions.get(sessionId);
        if (session != null) {
            try {
                Map<String, Object> gameState = session.getGame().serializeGameState();
                gameStateJson[0] = objectMapper.writeValueAsString(gameState);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize game state for session: {}", sessionId, e);
            }
        }

        persistedGameSessionRepository.findBySessionId(sessionId).ifPresentOrElse(
                persisted -> {
                    persisted.setSessionName(sessionName);
                    persisted.setPlayerIds(playerIds.toString());
                    persisted.setGameState(gameStateJson[0]);
                    persisted.setUpdatedAt(now);
                    persisted.setLastActivityAt(now);
                    persistedGameSessionRepository.save(persisted);
                },
                () -> {
                    PersistedGameSession persisted = new PersistedGameSession();
                    persisted.setSessionId(sessionId);
                    persisted.setSessionName(sessionName);
                    persisted.setPlayerIds(playerIds.toString());
                    persisted.setGameState(gameStateJson[0]);
                    persisted.setCreatedAt(now);
                    persisted.setUpdatedAt(now);
                    persisted.setLastActivityAt(now);
                    persistedGameSessionRepository.save(persisted);
                }
        );
    }

    /**
     * Update the last activity timestamp for a session in the database.
     * No-op if the repository is not available (e.g., in unit tests).
     */
    private void updateSessionActivity(String sessionId) {
        if (persistedGameSessionRepository == null) {
            return;
        }
        persistedGameSessionRepository.findBySessionId(sessionId).ifPresent(persisted -> {
            persisted.setLastActivityAt(LocalDateTime.now());
            persisted.setUpdatedAt(LocalDateTime.now());
            persistedGameSessionRepository.save(persisted);
        });
    }

    private GameStateResponse getGameStateInternal(GameSession session, Long playerId) {
        Game game = session.getGame();
        List<PlayerSummary> players = buildPlayerSummaries(game, session);
        List<SubSectorInfo> subSectors = buildSubSectorInfo(game);

        // Simultaneous turns: all players can act at any time, so isMyTurn is always true
        boolean isMyTurn = true;

        GameStateResponse response = new GameStateResponse(
                session.getSessionId(),
                game.getCurrentTurn(),
                null,  // No current player in simultaneous turns
                null,  // No current player name
                players,
                isMyTurn
        );
        response.setSubSectors(subSectors);

        // Populate turn timing information for timed turns UI
        response.setTurnIntervalSeconds(turnScheduler.getTurnIntervalSeconds());
        response.setAccumulatedSeconds(turnScheduler.getAccumulatedSeconds(session.getSessionId()));
        response.setSchedulerEnabled(turnScheduler.isEnabled());

        // Build resource pipeline summary, fleet list, and empire summary for the requesting player
        final Integer[] playerEmpireIdHolder = new Integer[1];
        Player player = findPlayerBySessionId(game, session, playerId);
        if (player != null) {
            playerEmpireIdHolder[0] = player.getEmpireId();
            ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
            if (pipeline != null) {
                response.setPipelineSummary(buildPipelineSummary(pipeline));
            }
            // Build fleet list for the requesting player
            response.setFleets(buildFleetList(player, game));
            // Build empire summary for the requesting player
            response.setEmpireSummary(buildEmpireSummary(player));
        }

        // Mark contested subsectors (any empire other than the requesting player owns planets here)
        final Integer playerEmpireId = playerEmpireIdHolder[0];
        if (playerEmpireId != null) {
            for (SubSectorInfo ss : subSectors) {
                if (ss.getOwnerEmpireIds() != null && !ss.getOwnerEmpireIds().isEmpty()) {
                    boolean hasOtherEmpire = ss.getOwnerEmpireIds().stream()
                            .anyMatch(id -> !id.equals(playerEmpireId));
                    ss.setContested(hasOtherEmpire);
                }
            }
        }
        return response;
    }

    /**
     * Builds a list of FleetDto for the current player's fleets.
     */
    private List<FleetDto> buildFleetList(Player currentPlayer, Game game) {
        List<FleetDto> fleetDtos = new ArrayList<>();
        // Build a map from system ID to subsector coordinates for fleet positioning
        Map<Integer, int[]> systemToSubSector = new HashMap<>();
        Galaxy galaxy = game.getGalaxy(0);
        if (galaxy != null) {
            for (int r = 0; r < Galaxy.GRID_SIZE; r++) {
                for (int c = 0; c < Galaxy.GRID_SIZE; c++) {
                    Galaxy.Sector sector = galaxy.getSector(r, c);
                    if (sector != null) {
                        for (SubSector subSector : sector.getAllSubSectors()) {
                            for (GalacticEntry entry : subSector.getAllEntries()) {
                                systemToSubSector.put(entry.getSystem().getId(),
                                        new int[]{subSector.getSubRow(), subSector.getSubCol()});
                            }
                        }
                    }
                }
            }
        }
        for (Integer fleetId : currentPlayer.getOwnedFleetIds()) {
            Fleet fleet = game.getFleets().get(fleetId);
            if (fleet == null || fleet.isEmpty()) {
                continue;
            }
            Hex3D pos = fleet.getPosition();
            FleetDto dto = new FleetDto();
            dto.setId(fleet.getId());
            dto.setName(fleet.getName());
            dto.setPositionX(pos != null ? pos.getX() : 0);
            dto.setPositionY(pos != null ? pos.getY() : 0);
            dto.setPositionZ(pos != null ? pos.getZ() : 0);
            // Resolve subsector coordinates from fleet's home system
            int[] subCoords = resolveFleetSubSector(fleet, systemToSubSector);
            dto.setSubSectorRow(subCoords[0]);
            dto.setSubSectorCol(subCoords[1]);
            dto.setMDriveSpeed(fleet.getFleetMDriveSpeed());
            dto.setJDriveSpeed(fleet.getFleetJDriveSpeed());
            dto.setEffectiveMDriveSpeed(fleet.getEffectiveFleetMDriveSpeed());
            dto.setEffectiveJDriveSpeed(fleet.getEffectiveFleetJDriveSpeed());
            dto.setHealthRatio(fleet.getFleetHealthRatio());
            dto.setShipCount(fleet.getActiveShips().size());
            dto.setTotalDamage(fleet.getTotalDamage());
            dto.setCurrentShieldPoints(fleet.getCurrentShieldPoints());
            dto.setTotalShieldPoints(fleet.getTotalShieldPoints());
            dto.setCurrentHullPoints(fleet.getCurrentHullPoints());
            dto.setTotalHullPoints(fleet.getTotalHullPoints());
            // Build ship list
            List<ShipDto> shipDtos = new ArrayList<>();
            for (Ship ship : fleet.getActiveShips()) {
                ShipDto shipDto = new ShipDto();
                shipDto.setId(ship.getId());
                shipDto.setName(ship.getName());
                shipDto.setShipType(ship.getShipType().name());
                shipDto.setMDriveSpeed(ship.getMDriveSpeed());
                shipDto.setJDriveSpeed(ship.getJDriveSpeed());
                shipDto.setHullHealthRatio(ship.getHullHealthRatio());
                shipDto.setCurrentHullPoints(ship.getCurrentHullPoints());
                shipDto.setMaxHullPoints(ship.getMaxHullPoints());
                shipDto.setCurrentShieldPoints(ship.getCurrentShieldPoints());
                shipDto.setMaxShieldPoints(ship.getMaxShieldPoints());
                shipDto.setTotalDamage(ship.getTotalDamage());
                shipDto.setDestroyed(ship.isDestroyed());
                shipDtos.add(shipDto);
            }
            dto.setShips(shipDtos);
            fleetDtos.add(dto);
        }
        return fleetDtos;
    }

    /**
     * Builds an EmpireSummary DTO from the current player's hex Player data.
     */
    private EmpireSummary buildEmpireSummary(Player player) {
        EmpireSummary summary = new EmpireSummary();
        int empireId = player.getEmpireId() != null ? player.getEmpireId() : 0;
        summary.setEmpireId(empireId);
        summary.setEmpireName(player.getEmpireName() != null ? player.getEmpireName() : "Unnamed Empire");
        summary.setGovernmentType(player.getGovernmentType() != null ? player.getGovernmentType().name() : "DEMOCRACY");
        summary.setEthos(player.getEthos() != null ? player.getEthos().name() : "MATERIALIST");
        summary.setPlanetCount(player.getOwnedPlanetIds().size());
        summary.setCapitalPlanetId(player.getHomePlanetId());
        summary.setFleetCount(player.getOwnedFleetIds().size());

        // Look up the hex Empire for component data
        Empire hexEmpire = findEmpireByPlayer(player);
        if (hexEmpire != null) {
            // Army component
            EmpireArmy army = hexEmpire.getArmy();
            summary.setArmyUnitCount(army.getUnitCount());
            summary.setArmyTotalOffense(army.getTotalOffense());
            summary.setArmyTotalDefense(army.getTotalDefense());
            summary.setArmySupplyCost(army.getTotalSupplyCost());

            // Navy component
            EmpireNavy navy = hexEmpire.getNavy();
            summary.setNavyFleetCount(navy.getFleetCount());
            summary.setNavyShipCount(navy.getTotalShipCount());
            summary.setNavyTotalDamage(navy.getTotalDamage());
            summary.setNavyTotalShieldPoints(navy.getTotalShieldPoints());
            summary.setNavyTotalHullPoints(navy.getTotalHullPoints());

            // Facilities component
            EmpireFacilities facilities = hexEmpire.getFacilities();
            summary.setFacilityCount(facilities.getTotalFacilityCount());
            summary.setSpaceStationCount(facilities.getSpaceStationCount());
            summary.setMineralProduction(facilities.getTotalMineralProduction());
            summary.setEnergyProduction(facilities.getTotalEnergyProduction());
            summary.setFoodProduction(facilities.getTotalFoodProduction());
            summary.setResearchOutput(facilities.getTotalResearchOutput());
            summary.setFacilityDefenseBonus(facilities.getTotalDefenseBonus());
            summary.setCreditIncome(facilities.getTotalCreditIncome());

            // Industrial capacity from facilities
            summary.setIndustrialCapacity(facilities.getTotalIndustrialCapacity());
            summary.setIndustrialPlanetCount(facilities.getIndustrialPlanetCount());
        }

        return summary;
    }

    /**
     * Finds the hex Empire for a given Player by searching the Game's empire registry.
     */
    private Empire findEmpireByPlayer(Player player) {
        int empireId = player.getEmpireId() != null ? player.getEmpireId() : -1;
        if (empireId < 0) return null;

        for (GameSession session : sessions.values()) {
            Game game = session.getGame();
            if (game == null) continue;
            Empire found = game.getEmpire(empireId);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Registers empires in the default game session's Game.
     * Called after galaxy creation to make empires available for component lookup.
     *
     * @param empires the map of empire ID to Empire instance
     */
    public void registerEmpires(Map<Integer, Empire> empires) {
        GameSession defaultSession = getDefaultSession();
        if (defaultSession != null && defaultSession.getGame() != null) {
            for (Empire empire : empires.values()) {
                defaultSession.getGame().registerEmpire(empire);
            }
            log.info("Registered {} empires in game session {}", empires.size(), defaultSession.getSessionId());
        }
    }

    /**
     * Builds a ResourcePipelineSummary DTO from the given ResourcePipelineManager.
     * Converts enum-keyed maps to String-keyed maps for JSON serialization.
     */
    private ResourcePipelineSummary buildPipelineSummary(ResourcePipelineManager pipeline) {
        Map<String, Integer> rawResources = enumMapToStringMap(pipeline.getRawStorage().getInventory());
        Map<String, Integer> refinedResources = enumMapToStringMap(pipeline.getRefinedStorage().getInventory());
        Map<String, Integer> modules = enumMapToStringMap(pipeline.getModuleStorage().getInventory());
        Map<String, Integer> miningRate = enumMapToStringMap(pipeline.getAggregateMiningRate());

        return new ResourcePipelineSummary(
                rawResources,
                refinedResources,
                modules,
                miningRate,
                pipeline.getMiningOperationCount(),
                pipeline.getActiveProcessingCount(),
                pipeline.getActiveAssemblerCount(),
                pipeline.getConstructionQueueSize()
        );
    }

    /**
     * Converts an enum-keyed map to a String-keyed map for JSON serialization.
     */
    private <T extends Enum<T>> Map<String, Integer> enumMapToStringMap(Map<T, Integer> enumMap) {
        Map<String, Integer> stringMap = new HashMap<>();
        for (Map.Entry<T, Integer> entry : enumMap.entrySet()) {
            stringMap.put(entry.getKey().name(), entry.getValue());
        }
        return stringMap;
    }

    private List<PlayerSummary> buildPlayerSummaries(Game game, GameSession session) {
        return game.getPlayers().stream().map(p -> {
            // Include pipeline module inventory in resources for the current player
            Map<String, Integer> resources = new HashMap<>();
            ResourcePipelineManager pipeline = game.getPipelineManager(p.getEmpireId());
            if (pipeline != null) {
                resources.putAll(enumMapToStringMap(pipeline.getModuleStorage().getInventory()));
                resources.putAll(enumMapToStringMap(pipeline.getRawStorage().getInventory()));
                resources.putAll(enumMapToStringMap(pipeline.getRefinedStorage().getInventory()));
            }
            PlayerSummary summary = new PlayerSummary(
                    (long) p.getId(),
                    p.getName(),
                    resources,
                    p.getOwnedPlanetIds().size(),
                    p.getOwnedFleetIds().size()
            );
            // Check if this player is an AI player
            for (Long sessionPlayerId : session.getPlayerIds()) {
                if (sessionPlayerId >= 1000 && session.isAiPlayer(sessionPlayerId)) {
                    List<Long> sessionPlayers = new ArrayList<>(session.getPlayerIds());
                    int gamePlayerIndex = sessionPlayers.indexOf(sessionPlayerId);
                    if (gamePlayerIndex == p.getId()) {
                        summary.setAi(true);
                        summary.setDifficulty(session.getAiPlayerDifficulty(sessionPlayerId));
                        break;
                    }
                }
            }
            return summary;
        }).collect(Collectors.toList());
    }

    /**
     * Resolves the subsector coordinates for a fleet by looking up its home system
     * in the system-to-subsector map. Falls back to the fleet's Hex3D position
     * mapped to subsector coordinates, or defaults to (5, 5) (center).
     */
    private int[] resolveFleetSubSector(Fleet fleet, Map<Integer, int[]> systemToSubSector) {
        // Try home system first
        if (fleet.getHomeSystem() != null && systemToSubSector.containsKey(fleet.getHomeSystem().getId())) {
            return systemToSubSector.get(fleet.getHomeSystem().getId());
        }
        // Try home station
        if (fleet.getHomeStation() != null && systemToSubSector.containsKey(fleet.getHomeStation().getId())) {
            return systemToSubSector.get(fleet.getHomeStation().getId());
        }
        // Fall back to center subsector
        return new int[]{5, 5};
    }

    private List<SubSectorInfo> buildSubSectorInfo(Game game) {
        List<SubSectorInfo> result = new ArrayList<>();

        // Fallback: try in-memory Galaxy first
        Galaxy galaxy = game.getGalaxy(0);
        if (galaxy != null) {
            Galaxy.Sector centralSector = galaxy.getSector(5, 5);
            if (centralSector != null) {
                for (SubSector subSector : centralSector.getAllSubSectors()) {
                    if (!subSector.isGenerated()) {
                        continue;
                    }
                    List<Map<String, Object>> systems = new ArrayList<>();
                    Set<Integer> ownerEmpireIds = new HashSet<>();
                    for (Map.Entry<Integer, com.yisy.modules.doramcu.hex.SolarSystem> entry : subSector.getAllSystems().entrySet()) {
                        com.yisy.modules.doramcu.hex.SolarSystem system = entry.getValue();
                        Map<String, Object> sysInfo = new HashMap<>();
                        sysInfo.put("id", system.getId());
                        sysInfo.put("starCount", system.getTotalStarCount());
                        sysInfo.put("planetCount", system.getTotalPlanetCount());
                        sysInfo.put("bodyCount", system.getTotalBodyCount());
                        // Extract ownership from planets in this system
                        for (com.yisy.modules.doramcu.hex.SolarSystemEntity entity : system.getAllSystems().values()) {
                            for (com.yisy.modules.doramcu.hex.Planet planet : entity.getAllPlanets().values()) {
                                Integer ownerId = planet.getOwnerEmpireId();
                                if (ownerId != null) {
                                    ownerEmpireIds.add(ownerId);
                                }
                            }
                        }
                        systems.add(sysInfo);
                    }
                    SubSectorInfo info = new SubSectorInfo(
                            subSector.getId(),
                            subSector.getName(),
                            5, 5,
                            subSector.getSubRow(),
                            subSector.getSubCol(),
                            subSector.isGenerated(),
                            subSector.size(),
                            systems
                    );
                    info.setOwnerEmpireIds(ownerEmpireIds);
                    info.setTradeType(centralSector.getTradeType());
                    result.add(info);
                }
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        // Fallback: query from database directly using projection to avoid lazy loading issues
        if (solarSystemRepository == null) {
            return result;
        }
        List<Object[]> rows = solarSystemRepository.findBySectorRowAndSectorColCounts(5, 5);
        if (rows.isEmpty()) {
            return result;
        }

        // Group solar systems by subsector
        Map<String, List<Object[]>> subSectorMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            // row: [id, subSectorRow, subSectorCol, starCount, planetCount, moonCount, asteroidBeltCount, gasGiantCount, nebulaCount, blackHoleCount, cometCount, spaceStationCount, asteroidCount]
            int subRow = ((Number) row[1]).intValue();
            int subCol = ((Number) row[2]).intValue();
            String key = subRow + "," + subCol;
            subSectorMap.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        // Build SubSectorInfo for each subsector
        for (Map.Entry<String, List<Object[]>> entry : subSectorMap.entrySet()) {
            String[] coords = entry.getKey().split(",");
            int subRow = Integer.parseInt(coords[0]);
            int subCol = Integer.parseInt(coords[1]);
            List<Object[]> systems = entry.getValue();

            List<Map<String, Object>> systemInfos = new ArrayList<>();
            for (Object[] row : systems) {
                Map<String, Object> sysInfo = new HashMap<>();
                sysInfo.put("id", row[0]);
                int starCount = ((Number) row[3]).intValue();
                int planetCount = ((Number) row[4]).intValue();
                int moonCount = ((Number) row[5]).intValue();
                int asteroidBeltCount = ((Number) row[6]).intValue();
                int gasGiantCount = ((Number) row[7]).intValue();
                int nebulaCount = ((Number) row[8]).intValue();
                int blackHoleCount = ((Number) row[9]).intValue();
                int cometCount = ((Number) row[10]).intValue();
                int spaceStationCount = ((Number) row[11]).intValue();
                int asteroidCount = ((Number) row[12]).intValue();
                sysInfo.put("starCount", starCount);
                sysInfo.put("planetCount", planetCount);
                int bodyCount = starCount + planetCount + moonCount + asteroidBeltCount
                        + gasGiantCount + nebulaCount + blackHoleCount + cometCount
                        + spaceStationCount + asteroidCount;
                sysInfo.put("bodyCount", bodyCount);
                systemInfos.add(sysInfo);
            }

            int subSectorId = subRow * 10 + subCol;
            SubSectorInfo info = new SubSectorInfo(
                    subSectorId,
                    "SubSector [" + subRow + "," + subCol + "]",
                    5, 5,
                    subRow, subCol,
                    true,
                    systems.size(),
                    systemInfos
            );
            result.add(info);
        }
        return result;
    }
}
