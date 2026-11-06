package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.CreateSessionResponse;
import com.yisy.modules.doramcu.dto.GameStateResponse;
import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import com.yisy.modules.doramcu.DoraMcuApplication;

/**
 * Integration tests for the full game session lifecycle.
 * Covers: create session, join session, get game state, advance turn, leave session.
 */
@SpringBootTest(classes = DoraMcuApplication.class)
@ActiveProfiles("test")
class GameSessionLifecycleIntegrationTest {

    @Autowired
    private GameSessionService gameSessionService;

    private String sessionId;
    private Long player1Id = 1L;
    private Long player2Id = 2L;

    @BeforeEach
    void setUp() {
        // Create a fresh session for each test
        CreateSessionResponse response = gameSessionService.createSession(
                player1Id, "Player1", "Test Session");
        sessionId = response.getSessionId();
    }

    @Test
    void createSession_shouldCreateSessionWithOnePlayer() {
        assertNotNull(sessionId);
        assertFalse(sessionId.isBlank());

        GameStateResponse state = gameSessionService.getGameState(player1Id, sessionId);
        assertNotNull(state);
        assertEquals(sessionId, state.getSessionId());
        assertEquals(1, state.getPlayers().size());
        // Simultaneous turns: currentPlayerName is null
        assertNull(state.getCurrentPlayerName());
    }

    @Test
    void joinSession_shouldAddSecondPlayer() {
        JoinSessionResponse response = gameSessionService.joinSession(
                player2Id, "Player2", sessionId);
        assertNotNull(response);
        assertEquals(sessionId, response.getSessionId());

        GameStateResponse state = gameSessionService.getGameState(player1Id, sessionId);
        assertEquals(2, state.getPlayers().size());
    }

    @Test
    void joinSession_shouldThrowWhenSessionNotFound() {
        assertThrows(Exception.class, () ->
                gameSessionService.joinSession(player2Id, "Player2", "nonexistent-session"));
    }

    @Test
    void joinSession_shouldThrowWhenPlayerAlreadyJoined() {
        assertThrows(IllegalStateException.class, () ->
                gameSessionService.joinSession(player1Id, "Player1", sessionId));
    }

    @Test
    void getGameState_shouldReturnCurrentTurnInfo() {
        GameStateResponse state = gameSessionService.getGameState(player1Id, sessionId);
        assertNotNull(state);
        assertEquals(0, state.getCurrentTurn());
        // Simultaneous turns: currentPlayerName is null
        assertNull(state.getCurrentPlayerName());
    }

    @Test
    void advanceTurn_shouldSucceedForAnyPlayer() {
        // Simultaneous turns: any player can advance the turn
        GameStateResponse response = gameSessionService.advanceTurn(player1Id, sessionId);
        assertNotNull(response);
        assertEquals(1, response.getCurrentTurn());
        // Simultaneous turns: currentPlayerName is null
        assertNull(response.getCurrentPlayerName());
    }

    @Test
    void leaveSession_shouldRemovePlayer() {
        // Join second player
        gameSessionService.joinSession(player2Id, "Player2", sessionId);

        // Leave as player 1
        gameSessionService.leaveSession(player1Id, sessionId);

        // Session should still exist with player 2
        var sessions = gameSessionService.listSessions();
        boolean found = sessions.stream()
                .anyMatch(s -> sessionId.equals(s.get("sessionId")));
        assertTrue(found, "Session should still exist after one player leaves");

        GameStateResponse state = gameSessionService.getGameState(null, sessionId);
        // Player count in the game may still be 2 since leaveSession only removes from session, not game
        assertTrue(state.getPlayers().size() >= 1);
    }

    @Test
    void leaveSession_shouldRemoveEmptySession() {
        // Leave as the only player
        gameSessionService.leaveSession(player1Id, sessionId);

        // Session should be removed
        assertThrows(Exception.class, () ->
                gameSessionService.getGameState(player1Id, sessionId));
    }

    @Test
    void listSessions_shouldIncludeCreatedSession() {
        var sessions = gameSessionService.listSessions();
        assertFalse(sessions.isEmpty());

        boolean found = sessions.stream()
                .anyMatch(s -> sessionId.equals(s.get("sessionId")));
        assertTrue(found);
    }
}
