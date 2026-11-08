package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.ai.AiDelegationService;
import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.exception.NotYourTurnException;
import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.model.PersistedGameSession;
import com.yisy.modules.doramcu.model.SolarSystem;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PersistedGameSessionRepository;
import com.yisy.modules.doramcu.unified.GameCalendarFormatter;
import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.unified.GlobalClock;
import com.yisy.modules.doramcu.unified.GlobalClockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central game session service that owns the session map and delegates to
 * specialized handlers for actions and state queries.
 * <p>
 * This class was refactored from a 1,579-line monolith into a thin facade
 * that coordinates between:
 * <ul>
 *   <li>{@code SessionActionHandler} — all mutable game actions (move, build, combat, etc.)</li>
 *   <li>{@code GameStateBuilder} — all response DTO building</li>
 * </ul>
 * Session lifecycle operations (create, join, leave, persist) remain here
 * as they are tightly coupled to the session map ownership.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class GameSessionService {

    private static final Logger log = LoggerFactory.getLogger(GameSessionService.class);

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PersistedGameSessionRepository persistedGameSessionRepository;
    private final GalaxyRepository galaxyRepository;
    private final GameInitializationService gameInitializationService;
    private final TurnScheduler turnScheduler;
    private final GameStateBuilder gameStateBuilder;
    private final CharacterService characterService;
    private final AiDelegationService aiDelegationService;
    private final SessionActionHandler sessionActionHandler;
    private final GlobalClockService globalClockService;

    public GameSessionService(PersistedGameSessionRepository persistedGameSessionRepository,
                              GalaxyRepository galaxyRepository,
                              GameInitializationService gameInitializationService,
                              @Lazy TurnScheduler turnScheduler,
                              GameStateBuilder gameStateBuilder,
                              CharacterService characterService,
                              @Lazy AiDelegationService aiDelegationService,
                              SessionActionHandler sessionActionHandler,
                              GlobalClockService globalClockService) {
        this.persistedGameSessionRepository = persistedGameSessionRepository;
        this.galaxyRepository = galaxyRepository;
        this.gameInitializationService = gameInitializationService;
        this.turnScheduler = turnScheduler;
        this.gameStateBuilder = gameStateBuilder;
        this.characterService = characterService;
        this.aiDelegationService = aiDelegationService;
        this.sessionActionHandler = sessionActionHandler;
        this.globalClockService = globalClockService;
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
    @Transactional
    public CreateSessionResponse createDefaultSession(String sessionName) {
        String sessionId = UUID.randomUUID().toString();
        log.debug("Creating default game session: {}", sessionName);

        Game game = new Game(sessionName);
        loadGalaxyToGame(game, sessionName);

        GameSession session = new GameSession(sessionId, sessionName, game);
        // Initialize global clock for this session
        GlobalClock clock = globalClockService.getOrCreateClock(sessionId, GameType.DORAMCU_4X);
        session.setGlobalClock(clock);
        sessions.put(sessionId, session);

        // Persist session metadata to database
        persistSession(sessionId, sessionName, session.getPlayerIds());

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.SESSION_CREATED, sessionId,
                Map.of("sessionName", sessionName)));

        return new CreateSessionResponse(sessionId, sessionName, null, null);
    }

    @Transactional
    public CreateSessionResponse createSession(Long playerId, String playerName, String sessionName) {
        String sessionId = UUID.randomUUID().toString();
        log.debug("Creating new game session: {} for player: {} ({})", sessionId, playerName, playerId);

        Game game = new Game(sessionName);
        loadGalaxyToGame(game, sessionName);

        // Set up the complete player: player, empire, home system, fleets, navy sync
        Game.PlayerSetupResult setup = game.setupPlayer(playerId.intValue(), playerName);
        log.info("Player {} (id={}) created with empire id={}", playerName, playerId, setup.getEmpire().getId());

        GameSession session = new GameSession(sessionId, sessionName, game);
        // Initialize global clock for this session
        GlobalClock clock = globalClockService.getOrCreateClock(sessionId, GameType.DORAMCU_4X);
        session.setGlobalClock(clock);
        session.addPlayer(playerId);
        sessions.put(sessionId, session);

        // Persist session metadata to database
        persistSession(sessionId, sessionName, session.getPlayerIds());

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.PLAYER_JOINED, sessionId,
                Map.of("playerId", playerId, "playerName", playerName)));

        return new CreateSessionResponse(sessionId, sessionName, playerId, playerName);
    }

    @Transactional
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

        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            // Set up the complete player: player, empire, home system, fleets, navy sync
            Game.PlayerSetupResult setup = game.setupPlayer(playerId.intValue(), playerName);
            log.info("Player {} (id={}) joined with empire id={}", playerName, playerId, setup.getEmpire().getId());
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

    @Transactional
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
            // Clean up global clock when session is removed
            globalClockService.removeClock(sessionId);
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
        Game game = (Game) session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            return null;
        }
        ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
        if (pipeline == null) {
            return null;
        }
        return gameStateBuilder.buildPipelineSummary(pipeline);
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
        Game game = (Game) session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            return null;
        }
        ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
        if (pipeline == null) {
            return null;
        }
        return gameStateBuilder.buildPipelineDetails(pipeline);
    }

    public GameStateResponse getGameState(Long playerId, String sessionId) {
        return getGameState(playerId, sessionId, false);
    }

    public GameStateResponse getGameState(Long playerId, String sessionId, boolean summary) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        updateSessionActivity(sessionId);
        session.getLock().readLock().lock();
        try {
            return gameStateBuilder.buildGameStateResponse(session, playerId, summary);
        } finally {
            session.getLock().readLock().unlock();
        }
    }

    // ---- Delegated action methods ----

    @Transactional
    public GameStateResponse moveFleet(Long playerId, String sessionId, MoveFleetRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.moveFleet(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "MOVE_FLEET", "playerId", playerId, "fleetId", request.getFleetId(),
                        "targetRow", request.getTargetRow(), "targetCol", request.getTargetCol(),
                        "solarSystemId", request.getSolarSystemId() != null ? request.getSolarSystemId() : "current")));
        return response;
    }

    @Transactional
    public GameStateResponse buildShip(Long playerId, String sessionId, BuildShipRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buildShip(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUILD_SHIP", "playerId", playerId, "shipType", request.getShipType(),
                        "targetFleetId", request.getTargetFleetId())));
        return response;
    }

    @Transactional
    public GameStateResponse engageCombat(Long playerId, String sessionId, EngageCombatRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.engageCombat(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "ENGAGE_COMBAT", "playerId", playerId,
                        "attackerFleetId", request.getAttackerFleetId(),
                        "defenderFleetId", request.getDefenderFleetId())));
        return response;
    }

    @Transactional
    public GameStateResponse advanceTurn(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.advanceTurn(session, playerId);
        Game game = (Game) session.getGame();
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.TURN_CHANGED, sessionId,
                Map.of("turn", game.getCurrentTurn(), "autoAdvanced", false)));
        return response;
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
    @Transactional
    private void persistSession(String sessionId, String sessionName, Set<Long> playerIds) {
        if (persistedGameSessionRepository == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        final String[] gameStateJson = new String[1];
        GameSession session = sessions.get(sessionId);
        if (session != null) {
            try {
                Map<String, Object> gameState = ((Game) session.getGame()).serializeGameState();
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
    @Transactional
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

    /**
     * Loads the first available galaxy from the database into the given Game instance.
     * Shared by createDefaultSession() and createSession() to avoid duplication.
     */
    private void loadGalaxyToGame(Game game, String sessionName) {
        List<com.yisy.modules.doramcu.model.Galaxy> dbGalaxies = galaxyRepository.findAll();
        if (!dbGalaxies.isEmpty()) {
            com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy =
                    gameInitializationService.convertToInMemoryGalaxy(dbGalaxies.get(0));
            if (inMemoryGalaxy != null) {
                game.addGalaxy(inMemoryGalaxy);
                log.info("Loaded galaxy '{}' into game session with {} systems",
                        inMemoryGalaxy.getName(), inMemoryGalaxy.getAllSystems().size());
            }
        } else {
            log.warn("No galaxy found in database for session {}", sessionName);
        }
    }

    @Transactional
    public Optional<PersistedGameSession> findPersistedSession() {
        if (persistedGameSessionRepository == null) {
            return Optional.empty();
        }
        List<PersistedGameSession> all = persistedGameSessionRepository.findAll();
        if (all.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(all.get(0));
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
                ((Game) defaultSession.getGame()).registerEmpire(empire);
            }
            log.info("Registered {} empires in game session {}", empires.size(), defaultSession.getSessionId());
        }
    }

    /**
     * Updates a sector name in all in-memory game sessions that have a matching galaxy.
     * This keeps the in-memory state consistent after a database rename.
     *
     * @param galaxyName the galaxy name to match
     * @param row the sector row
     * @param col the sector column
     * @param name the new name
     */
    public void updateSectorNameInMemory(String galaxyName, int row, int col, String name) {
        for (GameSession session : sessions.values()) {
            Game game = (Game) session.getGame();
            if (game == null) continue;
            Galaxy galaxy = game.getGalaxy(0);
            if (galaxy == null || !galaxyName.equals(galaxy.getName())) continue;
            Galaxy.Sector sector = galaxy.getSector(row, col);
            if (sector != null) {
                sector.setName(name);
                log.info("Updated in-memory sector name at ({}, {}) to '{}' in galaxy '{}'", row, col, name, galaxyName);
            }
        }
    }

    /**
     * Updates a subsector name in all in-memory game sessions that have a matching galaxy.
     * This keeps the in-memory state consistent after a database rename.
     *
     * @param galaxyName the galaxy name to match
     * @param row the sector row
     * @param col the sector column
     * @param subRow the subsector row
     * @param subCol the subsector column
     * @param name the new name
     */
    public void updateSubSectorNameInMemory(String galaxyName, int row, int col, int subRow, int subCol, String name) {
        for (GameSession session : sessions.values()) {
            Game game = (Game) session.getGame();
            if (game == null) continue;
            Galaxy galaxy = game.getGalaxy(0);
            if (galaxy == null || !galaxyName.equals(galaxy.getName())) continue;
            Galaxy.Sector sector = galaxy.getSector(row, col);
            if (sector != null) {
                com.yisy.modules.doramcu.hex.SubSector subSector = sector.getSubSector(subRow, subCol);
                if (subSector != null) {
                    subSector.setName(name);
                    log.info("Updated in-memory subsector name at ({},{},{},{}) to '{}' in galaxy '{}'",
                            row, col, subRow, subCol, name, galaxyName);
                }
            }
        }
    }

    // ---- Delegated Empire Action Methods ----

    @Transactional
    public GameStateResponse buildStation(Long playerId, String sessionId, BuildStationRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buildStation(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUILD_STATION", "playerId", playerId, "stationType", request.getStationType())));
        return response;
    }

    @Transactional
    public GameStateResponse buildFacility(Long playerId, String sessionId, BuildFacilityRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buildFacility(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUILD_FACILITY", "playerId", playerId, "facilityType", request.getFacilityType(),
                        "planetId", request.getPlanetId())));
        return response;
    }

    @Transactional
    public GameStateResponse createFleet(Long playerId, String sessionId, CreateFleetRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.createFleet(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "CREATE_FLEET", "playerId", playerId, "fleetName", request.getFleetName())));
        return response;
    }

    @Transactional
    public GameStateResponse colonizePlanet(Long playerId, String sessionId, ColonizeRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.colonizePlanet(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "COLONIZE", "playerId", playerId, "planetId", request.getPlanetId())));
        return response;
    }

    public List<BattleSummaryDto> getActiveBattles(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return new ArrayList<>();
        }
        return gameStateBuilder.buildActiveBattles(session);
    }

    @Transactional
    public GameStateResponse reinforceBattle(Long playerId, String sessionId, int battleId, ReinforceBattleRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.reinforceBattle(session, playerId, battleId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "REINFORCE_BATTLE", "playerId", playerId, "battleId", battleId,
                        "fleetId", request.getFleetId())));
        return response;
    }

    @Transactional
    public GameStateResponse retreatBattle(Long playerId, String sessionId, int battleId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.retreatBattle(session, playerId, battleId);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "RETREAT_BATTLE", "playerId", playerId, "battleId", battleId)));
        return response;
    }

    /**
     * Fetch full system details from the database for the system detail view.
     * Returns all celestial bodies with names, positions, and ownership info.
     */
    public java.util.Map<String, Object> getSystemDetails(Long playerId, String sessionId, int systemId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }

        return gameStateBuilder.buildSystemDetails(systemId, (Game) session.getGame());
    }

    /**
     * Periodically persists all active game sessions to the database.
     * Runs every 5 minutes to prevent data loss on server restart.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRateString = "${gamesession.persist-interval-ms:300000}")
    public void persistAllSessions() {
        if (sessions.isEmpty()) {
            return;
        }

        int persisted = 0;
        for (GameSession session : sessions.values()) {
            try {
                persistSession(session.getSessionId(), session.getName(), session.getPlayerIds());
                persisted++;
            } catch (Exception e) {
                log.warn("Failed to persist session {}: {}", session.getSessionId(), e.getMessage(), e);
            }
        }

        if (persisted > 0) {
            log.info("Periodically persisted {} game sessions", persisted);
        }
    }

    // ---- Delegated Traveller RPG Integration Methods ----

    @Transactional
    public GameStateResponse buildShipFromDesign(Long playerId, String sessionId, ShipDesignRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buildShipFromDesign(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUILD_FROM_DESIGN", "playerId", playerId, "shipName", request.getName())));
        return response;
    }

    @Transactional
    public GameStateResponse buyCommodity(Long playerId, String sessionId, TradeRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buyCommodity(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUY_COMMODITY", "playerId", playerId, "commodity", request.getCommodity(),
                        "quantity", request.getQuantity())));
        return response;
    }

    @Transactional
    public GameStateResponse sellCommodity(Long playerId, String sessionId, TradeRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.sellCommodity(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "SELL_COMMODITY", "playerId", playerId, "commodity", request.getCommodity(),
                        "quantity", request.getQuantity())));
        return response;
    }

    public List<TradeOpportunityDto> getTradeMarket(Long playerId, String sessionId, int systemId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return List.of();
        }

        return gameStateBuilder.buildTradeOpportunities(systemId, (Game) session.getGame());
    }

    @Transactional
    public GameStateResponse assignCommander(Long playerId, String sessionId, int characterId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.assignCommander(session, playerId, characterId);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "ASSIGN_COMMANDER", "playerId", playerId, "characterId", characterId)));
        return response;
    }
}
