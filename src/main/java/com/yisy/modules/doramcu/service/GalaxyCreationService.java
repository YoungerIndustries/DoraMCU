package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.CreateSessionResponse;
import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that orchestrates the complete galaxy creation sequence.
 * <p>
 * This service provides a single entry point for creating a new galaxy with AI players
 * and allowing human players to join dynamically with empire creation as they're added.
 * </p>
 * <p>
 * The sequence is:
 * <ol>
 *   <li>Create galaxy with AI-controlled empires for all player slots</li>
 *   <li>Create game session</li>
 *   <li>Add 3 AI players with different difficulty levels</li>
 *   <li>Allow new human players to join with a new empire</li>
 * </ol>
 * </p>
 * <p>
 * When a human player joins, a new empire is created for them with a unique empire ID.
 * The player receives a home planet with starting facilities.
 * </p>
 */
@Service
public class GalaxyCreationService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyCreationService.class);

    private final GalaxyService galaxyService;
    private final GalaxyProgressService galaxyProgressService;
    private final GameSessionService gameSessionService;
    private final AiPlayerService aiPlayerService;
    private final PlayerRepository playerRepository;

    public GalaxyCreationService(GalaxyService galaxyService,
                                  GalaxyProgressService galaxyProgressService,
                                  GameSessionService gameSessionService,
                                  AiPlayerService aiPlayerService,
                                  PlayerRepository playerRepository) {
        this.galaxyService = galaxyService;
        this.galaxyProgressService = galaxyProgressService;
        this.gameSessionService = gameSessionService;
        this.aiPlayerService = aiPlayerService;
        this.playerRepository = playerRepository;
    }

    /**
     * Creates a new galaxy with 3 AI players and returns the session ID.
     * <p>
     * This method performs the complete initialization sequence:
     * <ol>
     *   <li>Creates AI-controlled empires for all player slots</li>
     *   <li>Generates and persists the galaxy</li>
     *   <li>Creates a game session</li>
     *   <li>Adds 3 AI players with different difficulty levels</li>
     * </ol>
     * </p>
     *
     * @param sessionName the name of the game session
     * @return the creation result containing session ID and player information
     */
    public GalaxyCreationResult createGalaxyWithAiPlayers(String sessionName) {
        log.info("Starting galaxy creation sequence with session name: {}", sessionName);

        // Step 1: Create AI-controlled empires for all player slots
        Map<Integer, Empire> empires = createAiControlledEmpires();

        // Step 2: Generate and persist the galaxy
        generateAndPersistGalaxy(empires);

        // Step 3: Create the game session
        CreateSessionResponse sessionResponse = gameSessionService.createDefaultSession(sessionName);
        String sessionId = sessionResponse.getSessionId();
        log.info("Game session created: {} ({})", sessionId, sessionName);

        // Step 3.5: Register empires in the Game for component lookup
        gameSessionService.registerEmpires(empires);

        // Step 4: Add 3 AI players with different difficulty levels
        List<AiPlayerInfo> aiPlayers = addAiPlayers(sessionId);

        log.info("Galaxy creation sequence complete: session={}, aiPlayers={}", sessionId, aiPlayers.size());

        return new GalaxyCreationResult(sessionId, sessionName, aiPlayers);
    }

    /**
     * Allows a new human player to join an existing galaxy session with a new empire.
     * <p>
     * When a player joins, a new empire is created for them with a unique empire ID.
     * The player receives a home planet with starting facilities.
     * </p>
     *
     * @param playerId the unique identifier for the player
     * @param playerName the name of the player
     * @param sessionId the session to join
     * @return the join response with player information
     */
    public JoinSessionResponse joinPlayerWithEmpire(Long playerId, String playerName, String sessionId) {
        log.info("Player {} ({}) joining session {} with empire creation", playerName, playerId, sessionId);

        // Join the session, which creates the player's empire and home planet
        JoinSessionResponse response = gameSessionService.joinSession(playerId, playerName, sessionId);

        log.info("Player {} joined session {} with empire id={}", playerName, sessionId, playerId);

        return response;
    }

    /**
     * Creates AI-controlled empires for all registered player slots.
     * <p>
     * Each empire is initially AI-controlled (rulerPlayerId = -1).
     * When a human player joins, a new empire is created for them independently.
     * </p>
     *
     * @return map of empire ID to Empire instance
     */
    private Map<Integer, Empire> createAiControlledEmpires() {
        List<Player> players = playerRepository.findAll();
        log.info("Creating AI-controlled empires for {} player slots...", players.size());

        Map<Integer, Empire> empires = new HashMap<>();
        for (Player player : players) {
            int empireId = player.getId().intValue();
            String empireName = player.getEmpireName();
            int aiRulerId = -1; // AI-controlled initially

            Empire empire = new Empire(empireId, empireName, aiRulerId);
            empires.put(empireId, empire);
            log.info("Created AI-controlled empire: {} (id={}, ruler=AI)", empireName, empireId);
        }

        return empires;
    }

    /**
     * Generates and persists the galaxy with the given empires.
     *
     * @param empires the map of empire ID to Empire instance
     */
    private void generateAndPersistGalaxy(Map<Integer, Empire> empires) {
        if (galaxyService.hasGalaxies()) {
            log.info("Galaxy already exists. Skipping generation.");
            return;
        }

        log.info("Generating galaxy with {} AI-controlled empires...", empires.size());

        // Set progress listener for SSE events
        galaxyService.setProgressListener(galaxyProgressService);

        // Generate and save galaxy with empires
        galaxyService.generateAndSaveGalaxyWithEmpires(
                GalaxyFactory.GAME_GALAXY_NAME,
                empires,
                GalaxyFactory.gameGalaxyConfig().getSystemCount(),
                GalaxyFactory.gameGalaxyConfig().getPlanetsPerSystem(),
                GalaxyFactory.gameGalaxyConfig().getMoonChance(),
                GalaxyFactory.gameGalaxyConfig().getAsteroidBeltChance(),
                GalaxyFactory.gameGalaxyConfig().getGasGiantChance(),
                GalaxyFactory.gameGalaxyConfig().getNebulaChance(),
                GalaxyFactory.gameGalaxyConfig().getBlackHoleChance(),
                GalaxyFactory.gameGalaxyConfig().getCometChance(),
                GalaxyFactory.gameGalaxyConfig().getSpaceStationChance(),
                GalaxyFactory.gameGalaxyConfig().getHyperlaneDensity(),
                GalaxyFactory.GAME_GALAXY_SEED
        );

        log.info("Galaxy '{}' generated and persisted with {} AI-controlled empires",
                GalaxyFactory.GAME_GALAXY_NAME, empires.size());
    }

    /**
     * Adds 3 AI players to the session with different difficulty levels.
     *
     * @param sessionId the session to add AI players to
     * @return list of AI player information
     */
    private List<AiPlayerInfo> addAiPlayers(String sessionId) {
        log.info("Adding 3 AI players to session {}", sessionId);

        // Add AI Vanguard (EASY)
        JoinSessionResponse aiVanguard = aiPlayerService.createAiPlayer(
                sessionId, "AI Vanguard", AiPlayerService.Difficulty.EASY);
        log.info("AI player 'AI Vanguard' (EASY) added to session");

        // Add AI Strategist (MEDIUM)
        JoinSessionResponse aiStrategist = aiPlayerService.createAiPlayer(
                sessionId, "AI Strategist", AiPlayerService.Difficulty.MEDIUM);
        log.info("AI player 'AI Strategist' (MEDIUM) added to session");

        // Add AI Overlord (HARD)
        JoinSessionResponse aiOverlord = aiPlayerService.createAiPlayer(
                sessionId, "AI Overlord", AiPlayerService.Difficulty.HARD);
        log.info("AI player 'AI Overlord' (HARD) added to session");

        return List.of(
                new AiPlayerInfo(aiVanguard.getPlayerId(), aiVanguard.getPlayerName(), "EASY"),
                new AiPlayerInfo(aiStrategist.getPlayerId(), aiStrategist.getPlayerName(), "MEDIUM"),
                new AiPlayerInfo(aiOverlord.getPlayerId(), aiOverlord.getPlayerName(), "HARD")
        );
    }

    /**
     * Result of galaxy creation containing session information and AI player details.
     */
    public static class GalaxyCreationResult {
        private final String sessionId;
        private final String sessionName;
        private final List<AiPlayerInfo> aiPlayers;

        public GalaxyCreationResult(String sessionId, String sessionName, List<AiPlayerInfo> aiPlayers) {
            this.sessionId = sessionId;
            this.sessionName = sessionName;
            this.aiPlayers = aiPlayers;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getSessionName() {
            return sessionName;
        }

        public List<AiPlayerInfo> getAiPlayers() {
            return aiPlayers;
        }
    }

    /**
     * Information about an AI player.
     */
    public static class AiPlayerInfo {
        private final Long playerId;
        private final String playerName;
        private final String difficulty;

        public AiPlayerInfo(Long playerId, String playerName, String difficulty) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.difficulty = difficulty;
        }

        public Long getPlayerId() {
            return playerId;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getDifficulty() {
            return difficulty;
        }
    }
}
