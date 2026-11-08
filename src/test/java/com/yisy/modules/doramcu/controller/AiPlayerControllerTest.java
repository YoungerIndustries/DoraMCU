package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.ai.AiDelegationService;
import com.yisy.modules.doramcu.ai.GameChatService;
import com.yisy.modules.doramcu.config.AiDifficultyConfig;
import com.yisy.modules.doradl.dl.service.DlGameAiService;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.model.SolarSystem;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PersistedGameSessionRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.service.GameStateBuilder;
import com.yisy.modules.doramcu.service.SessionActionHandler;
import com.yisy.modules.doramcu.service.TurnScheduler;
import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.unified.GlobalClock;
import com.yisy.modules.doramcu.unified.GlobalClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("AiPlayerController Tests")
class AiPlayerControllerTest {

    private GameSessionService gameSessionService;
    private DlGameAiService dlGameAiService;
    private AiPlayerService aiPlayerService;
    private AiPlayerController controller;

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
        AiDelegationService aiDelegationService = Mockito.mock(AiDelegationService.class);
        controller = new AiPlayerController(aiPlayerService, aiDelegationService);
    }

    // ---- Create AI Player ----

    @Test
    @DisplayName("createAiPlayer - should return 200 OK on success")
    void createAiPlayer_shouldReturnOk() {
        // Create a session first
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        ResponseEntity<JoinSessionResponse> response = controller.createAiPlayer(
                sessionId, "AI Opponent", AiPlayerService.Difficulty.MEDIUM);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AI Opponent", response.getBody().getPlayerName());
    }

    @Test
    @DisplayName("createAiPlayer - should return 404 when session not found")
    void createAiPlayer_shouldReturnNotFound() {
        ResponseEntity<JoinSessionResponse> response = controller.createAiPlayer(
                "nonexistent", "AI", AiPlayerService.Difficulty.EASY);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("createAiPlayer - should support EASY difficulty")
    void createAiPlayer_shouldSupportEasy() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        ResponseEntity<JoinSessionResponse> response = controller.createAiPlayer(
                sessionId, "Easy AI", AiPlayerService.Difficulty.EASY);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("createAiPlayer - should support HARD difficulty")
    void createAiPlayer_shouldSupportHard() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        ResponseEntity<JoinSessionResponse> response = controller.createAiPlayer(
                sessionId, "Hard AI", AiPlayerService.Difficulty.HARD);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ---- Execute AI Turn ----

    @Test
    @DisplayName("executeAiTurn - should return 200 OK on success")
    void executeAiTurn_shouldReturnOk() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        // Create AI player
        JoinSessionResponse aiResponse = aiPlayerService.createAiPlayer(
                sessionId, "AI Opponent", AiPlayerService.Difficulty.MEDIUM);
        long aiPlayerId = aiResponse.getPlayerId();

        // Simultaneous turns: AI can execute at any time
        ResponseEntity<GameStateResponse> response = controller.executeAiTurn(
                sessionId, aiPlayerId, AiPlayerService.Difficulty.MEDIUM);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("executeAiTurn - should return 404 when session not found")
    void executeAiTurn_shouldReturnNotFound() {
        ResponseEntity<GameStateResponse> response = controller.executeAiTurn(
                "nonexistent", 1_000_001L, AiPlayerService.Difficulty.MEDIUM);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("executeAiTurn - should return 200 OK for AI at any time (simultaneous turns)")
    void executeAiTurn_shouldReturnOkAtAnyTime() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        // Create AI player
        JoinSessionResponse aiResponse = aiPlayerService.createAiPlayer(
                sessionId, "AI Opponent", AiPlayerService.Difficulty.MEDIUM);
        long aiPlayerId = aiResponse.getPlayerId();

        // Simultaneous turns: AI can execute immediately, no need to wait for turn
        ResponseEntity<GameStateResponse> response = controller.executeAiTurn(
                sessionId, aiPlayerId, AiPlayerService.Difficulty.MEDIUM);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ---- Integration Tests ----

    @Test
    @DisplayName("Integration - Full AI player workflow")
    void integration_fullWorkflow() {
        // 1. Create session
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        // 2. Create AI player via controller
        ResponseEntity<JoinSessionResponse> createResponse2 = controller.createAiPlayer(
                sessionId, "AI Opponent", AiPlayerService.Difficulty.MEDIUM);
        assertEquals(HttpStatus.OK, createResponse2.getStatusCode());
        long aiPlayerId = createResponse2.getBody().getPlayerId();

        // 3. Verify session has 2 players
        assertEquals(2, ((Game) gameSessionService.getGameSession(sessionId).getGame()).getPlayerCount());

        // 4. Execute AI turn - simultaneous turns, no need to advance
        ResponseEntity<GameStateResponse> turnResponse = controller.executeAiTurn(
                sessionId, aiPlayerId, AiPlayerService.Difficulty.MEDIUM);
        assertEquals(HttpStatus.OK, turnResponse.getStatusCode());
    }

    @Test
    @DisplayName("Integration - Multiple AI players via controller")
    void integration_multipleAiPlayers() {
        CreateSessionResponse createResponse = gameSessionService.createSession(
                1L, "HumanPlayer", "Test Game");
        String sessionId = createResponse.getSessionId();

        // Create multiple AI players
        controller.createAiPlayer(sessionId, "AI 1", AiPlayerService.Difficulty.EASY);
        controller.createAiPlayer(sessionId, "AI 2", AiPlayerService.Difficulty.MEDIUM);
        controller.createAiPlayer(sessionId, "AI 3", AiPlayerService.Difficulty.HARD);

        // Verify all players are in session
        assertEquals(4, ((Game) gameSessionService.getGameSession(sessionId).getGame()).getPlayerCount());
    }
}
