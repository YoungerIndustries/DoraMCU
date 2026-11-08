package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.ai.GameChatService;
import com.yisy.modules.doramcu.config.AiDifficultyConfig;
import com.yisy.modules.doradl.dl.service.DlGameAiService;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PersistedGameSessionRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import com.yisy.modules.doramcu.service.GameStateBuilder;
import com.yisy.modules.doramcu.service.SessionActionHandler;
import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.unified.GlobalClock;
import com.yisy.modules.doramcu.unified.GlobalClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("AiPlayerService Tests")
class AiPlayerServiceTest {

    private GameSessionService gameSessionService;
    private DlGameAiService dlGameAiService;
    private AiPlayerService aiPlayerService;

    @BeforeEach
    void setUp() {
        SolarSystemRepository solarSystemRepository = Mockito.mock(SolarSystemRepository.class);
        PersistedGameSessionRepository persistedGameSessionRepository = Mockito.mock(PersistedGameSessionRepository.class);
        GalaxyRepository galaxyRepository = Mockito.mock(GalaxyRepository.class);
        Mockito.when(galaxyRepository.findAll()).thenReturn(new java.util.ArrayList<>());
        TurnScheduler turnScheduler = Mockito.mock(TurnScheduler.class);
        when(turnScheduler.getTurnIntervalSeconds()).thenReturn(5L);
        when(turnScheduler.getAccumulatedSeconds(anyString())).thenReturn(0L);
        when(turnScheduler.isEnabled()).thenReturn(true);
        GameStateBuilder gameStateBuilder = Mockito.mock(GameStateBuilder.class);
        Mockito.when(gameStateBuilder.buildPlayerSummaries(Mockito.any(), Mockito.any())).thenReturn(new java.util.ArrayList<>());
        Mockito.when(gameStateBuilder.buildSubSectorInfo(Mockito.any(), Mockito.anyBoolean())).thenReturn(new java.util.ArrayList<>());
        Mockito.when(gameStateBuilder.buildGameStateResponse(Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenAnswer(invocation -> {
            com.yisy.modules.doramcu.dto.GameStateResponse resp = new com.yisy.modules.doramcu.dto.GameStateResponse();
            com.yisy.modules.doramcu.model.GameSession session = invocation.getArgument(0);
            if (session != null) {
                resp.setSessionId(session.getSessionId());
            }
            resp.setSubSectors(new java.util.ArrayList<>());
            return resp;
        });
        SessionActionHandler sessionActionHandler = Mockito.mock(SessionActionHandler.class);
        GlobalClockService globalClockService = Mockito.mock(GlobalClockService.class);
        when(globalClockService.getOrCreateClock(anyString(), any())).thenAnswer(invocation -> GlobalClock.forGameType(GameType.DORAMCU_4X));
        gameSessionService = new GameSessionService(persistedGameSessionRepository, galaxyRepository, null, turnScheduler, gameStateBuilder, null, null, sessionActionHandler, globalClockService);
        dlGameAiService = Mockito.mock(DlGameAiService.class);
        AiDifficultyConfig aiDifficultyConfig = new AiDifficultyConfig();
        GameChatService gameChatService = Mockito.mock(GameChatService.class);
        DoraAiLearningService doraAiLearningService = Mockito.mock(DoraAiLearningService.class);
        aiPlayerService = new AiPlayerService(gameSessionService, dlGameAiService, aiDifficultyConfig, gameChatService, doraAiLearningService);
    }

    // ---- Create AI Player ----

    @Test
    @DisplayName("createAiPlayer - should create AI player and join session")
    void createAiPlayer_shouldJoinSession() {
        // Create a session with a human player first
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        // Create AI player
        JoinSessionResponse response = aiPlayerService.createAiPlayer(
                sessionId, "AI Opponent", AiPlayerService.Difficulty.MEDIUM);

        assertNotNull(response);
        assertEquals(sessionId, response.getSessionId());
        assertEquals("AI Opponent", response.getPlayerName());
        assertTrue(response.getPlayerId() >= 1000L); // AI player IDs start from 1000
    }

    @Test
    @DisplayName("createAiPlayer - should add AI player to session")
    void createAiPlayer_shouldAddToSession() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        aiPlayerService.createAiPlayer(sessionId, "AI Opponent", AiPlayerService.Difficulty.EASY);

        GameSession session = gameSessionService.getGameSession(sessionId);
        assertNotNull(session);
        assertEquals(2, ((Game) session.getGame()).getPlayerCount());
        assertTrue(session.getPlayerIds().contains(1L));
    }

    @Test
    @DisplayName("createAiPlayer - should create starting fleets for AI")
    void createAiPlayer_shouldCreateStartingFleets() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        aiPlayerService.createAiPlayer(
                sessionId, "AI Opponent", AiPlayerService.Difficulty.MEDIUM);

        GameSession session = gameSessionService.getGameSession(sessionId);
        Game game = (Game) session.getGame();
        
        // The AI is the last player added
        List<Player> players = game.getPlayers();
        Player aiPlayer = players.get(players.size() - 1);
        
        assertNotNull(aiPlayer);
        assertEquals("AI Opponent", aiPlayer.getName());
        assertFalse(aiPlayer.getOwnedFleetIds().isEmpty());
    }

    @Test
    @DisplayName("createAiPlayer - should throw when session not found")
    void createAiPlayer_shouldThrowWhenNotFound() {
        assertThrows(NoSuchElementException.class, () ->
                aiPlayerService.createAiPlayer("nonexistent", "AI", AiPlayerService.Difficulty.EASY)
        );
    }

    @Test
    @DisplayName("createAiPlayer - should support all difficulty levels")
    void createAiPlayer_shouldSupportAllDifficulties() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        // Test EASY
        JoinSessionResponse easyResponse = aiPlayerService.createAiPlayer(
                sessionId, "Easy AI", AiPlayerService.Difficulty.EASY);
        assertNotNull(easyResponse);

        // Test MEDIUM
        JoinSessionResponse mediumResponse = aiPlayerService.createAiPlayer(
                sessionId, "Medium AI", AiPlayerService.Difficulty.MEDIUM);
        assertNotNull(mediumResponse);

        // Test HARD
        JoinSessionResponse hardResponse = aiPlayerService.createAiPlayer(
                sessionId, "Hard AI", AiPlayerService.Difficulty.HARD);
        assertNotNull(hardResponse);

        // Verify all players are in the session
        GameSession session = gameSessionService.getGameSession(sessionId);
        assertEquals(4, ((Game) session.getGame()).getPlayerCount()); // Human + 3 AIs
    }

    // ---- Execute AI Turn ----

    @Test
    @DisplayName("executeAiTurn - should return state when no AI players configured")
    void executeAiTurn_shouldReturnStateWhenNoAiPlayers() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        // No AI players configured - should return state without error
        Map<Long, AiPlayerService.Difficulty> aiMap = Map.of();
        GameStateResponse response = aiPlayerService.executeAiTurn(sessionId, aiMap);
        assertNotNull(response);
    }

    @Test
    @DisplayName("executeAiTurn - should throw when session not found")
    void executeAiTurn_shouldThrowWhenNotFound() {
        Map<Long, AiPlayerService.Difficulty> aiMap = Map.of();
        assertThrows(NoSuchElementException.class, () ->
                aiPlayerService.executeAiTurn("nonexistent", aiMap)
        );
    }

    @Test
    @DisplayName("Integration - AI player should have fleets created")
    void integration_aiPlayerShouldHaveFleetsCreated() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                0L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        JoinSessionResponse aiResponse = aiPlayerService.createAiPlayer(sessionId, "AI Builder", AiPlayerService.Difficulty.MEDIUM);

        GameSession session = gameSessionService.getGameSession(sessionId);
        Game game = (Game) session.getGame();

        List<Player> players = game.getPlayers();
        Player aiPlayer = players.get(players.size() - 1);
        assertNotNull(aiPlayer);
        // AI player game ID should match the session player ID
        assertEquals(aiResponse.getPlayerId().intValue(), aiPlayer.getId());

        // AI should have starting fleets
        assertFalse(aiPlayer.getOwnedFleetIds().isEmpty());
        assertEquals(2, aiPlayer.getOwnedFleetIds().size()); // Military + Mining
    }

    @Test
    @DisplayName("Integration - AI player should have fleets")
    void integration_aiPlayerShouldHaveFleets() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                0L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        aiPlayerService.createAiPlayer(sessionId, "AI Mover", AiPlayerService.Difficulty.EASY);

        GameSession session = gameSessionService.getGameSession(sessionId);
        Game game = (Game) session.getGame();

        List<Player> players = game.getPlayers();
        Player aiPlayer = players.get(players.size() - 1);
        assertNotNull(aiPlayer);

        // AI should have fleets
        assertFalse(aiPlayer.getOwnedFleetIds().isEmpty());
    }

    @Test
    @DisplayName("executeAiTurn - should execute for all AI players (simultaneous turns)")
    void executeAiTurn_shouldExecuteForAllAiPlayers() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                0L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        JoinSessionResponse aiResponse = aiPlayerService.createAiPlayer(
                sessionId, "AI Opponent", AiPlayerService.Difficulty.MEDIUM);

        // Simultaneous turns: AI can execute at any time without needing to be "current"
        Map<Long, AiPlayerService.Difficulty> aiMap = Map.of(aiResponse.getPlayerId(), AiPlayerService.Difficulty.MEDIUM);
        GameStateResponse response = aiPlayerService.executeAiTurn(sessionId, aiMap);

        assertNotNull(response);
        assertEquals(sessionId, response.getSessionId());
    }

    // ---- Difficulty Levels ----

    @Test
    @DisplayName("Difficulty enum - should have EASY, MEDIUM, HARD values")
    void difficultyEnum_shouldHaveValues() {
        AiPlayerService.Difficulty[] values = AiPlayerService.Difficulty.values();
        assertEquals(3, values.length);
        assertEquals(AiPlayerService.Difficulty.EASY, values[0]);
        assertEquals(AiPlayerService.Difficulty.MEDIUM, values[1]);
        assertEquals(AiPlayerService.Difficulty.HARD, values[2]);
    }

    // ---- AI Player ID Generation ----

    @Test
    @DisplayName("createAiPlayer - should generate unique player IDs")
    void createAiPlayer_shouldGenerateUniqueIds() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        JoinSessionResponse response1 = aiPlayerService.createAiPlayer(
                sessionId, "AI 1", AiPlayerService.Difficulty.EASY);
        JoinSessionResponse response2 = aiPlayerService.createAiPlayer(
                sessionId, "AI 2", AiPlayerService.Difficulty.EASY);

        assertNotEquals(response1.getPlayerId(), response2.getPlayerId());
    }

    // ---- Integration Tests ----

    @Test
    @DisplayName("Integration - Multiple AI players should work together")
    void integration_multipleAiPlayers() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        // Create multiple AI players
        aiPlayerService.createAiPlayer(sessionId, "AI Easy", AiPlayerService.Difficulty.EASY);
        aiPlayerService.createAiPlayer(sessionId, "AI Medium", AiPlayerService.Difficulty.MEDIUM);
        aiPlayerService.createAiPlayer(sessionId, "AI Hard", AiPlayerService.Difficulty.HARD);

        GameSession session = gameSessionService.getGameSession(sessionId);
        assertEquals(4, ((Game) session.getGame()).getPlayerCount());

        // All AI players should have fleets
        Game game = (Game) session.getGame();
        List<Player> players = game.getPlayers();
        for (int i = 1; i < players.size(); i++) { // Skip human player at index 0
            Player player = players.get(i);
            assertFalse(player.getOwnedFleetIds().isEmpty());
        }
    }
}
