package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.ai.GameChatService;
import com.yisy.modules.doramcu.config.AiDifficultyConfig;
import com.yisy.modules.doradl.dl.service.DlGameAiService;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.model.SolarSystem;
import com.yisy.modules.doramcu.repository.PersistedGameSessionRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.service.TurnScheduler;
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
        TurnScheduler turnScheduler = Mockito.mock(TurnScheduler.class);
        when(turnScheduler.getTurnIntervalSeconds()).thenReturn(5L);
        when(turnScheduler.getAccumulatedSeconds(anyString())).thenReturn(0L);
        when(turnScheduler.isEnabled()).thenReturn(true);
        gameSessionService = new GameSessionService(solarSystemRepository, persistedGameSessionRepository, turnScheduler);
        dlGameAiService = Mockito.mock(DlGameAiService.class);
        AiDifficultyConfig aiDifficultyConfig = new AiDifficultyConfig();
        GameChatService gameChatService = Mockito.mock(GameChatService.class);
        DoraAiLearningService doraAiLearningService = Mockito.mock(DoraAiLearningService.class);
        aiPlayerService = new AiPlayerService(gameSessionService, dlGameAiService, aiDifficultyConfig, gameChatService, doraAiLearningService);
        controller = new AiPlayerController(aiPlayerService);
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
        assertEquals(2, gameSessionService.getGameSession(sessionId).getGame().getPlayerCount());

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
        assertEquals(4, gameSessionService.getGameSession(sessionId).getGame().getPlayerCount());
    }
}
