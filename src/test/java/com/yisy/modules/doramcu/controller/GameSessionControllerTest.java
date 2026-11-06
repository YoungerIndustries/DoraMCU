package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.CreateSessionRequest;
import com.yisy.modules.doramcu.dto.CreateSessionResponse;
import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.util.PlayerIdExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameSessionController Tests")
class GameSessionControllerTest {

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private PlayerIdExtractor playerIdExtractor;

    @Mock
    private Authentication authentication;

    private GameSessionController controller;

    @BeforeEach
    void setUp() {
        controller = new GameSessionController(gameSessionService, playerIdExtractor);
    }

    // ---- Create Session ----

    @Test
    @DisplayName("createSession - should return 201 Created with valid request")
    void createSession_shouldReturnCreated() {
        CreateSessionRequest request = new CreateSessionRequest("My Game");
        CreateSessionResponse expected = new CreateSessionResponse("sess-1", "My Game", 1L, "Player1");

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.createSession(eq(1L), eq("1"), eq("My Game")))
                .thenReturn(expected);

        ResponseEntity<CreateSessionResponse> response = controller.createSession(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(gameSessionService).createSession(1L, "1", "My Game");
    }

    @Test
    @DisplayName("createSession - should return 500 on service error")
    void createSession_shouldReturn500OnError() {
        CreateSessionRequest request = new CreateSessionRequest("My Game");

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.createSession(eq(1L), eq("1"), eq("My Game")))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<CreateSessionResponse> response = controller.createSession(request, authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ---- Join Session ----

    @Test
    @DisplayName("joinSession - should return 200 OK on successful join")
    void joinSession_shouldReturnOk() {
        JoinSessionResponse expected = new JoinSessionResponse("sess-1", "My Game", 2L, "Player2");

        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.joinSession(eq(2L), eq("2"), eq("sess-1")))
                .thenReturn(expected);

        ResponseEntity<JoinSessionResponse> response = controller.joinSession("sess-1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    @DisplayName("joinSession - should return 404 when session not found")
    void joinSession_shouldReturn404() {
        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.joinSession(eq(2L), eq("2"), eq("nonexistent")))
                .thenThrow(new NoSuchElementException("Session not found"));

        ResponseEntity<JoinSessionResponse> response = controller.joinSession("nonexistent", authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("joinSession - should return 409 when player already joined")
    void joinSession_shouldReturn409() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.joinSession(eq(1L), eq("1"), eq("sess-1")))
                .thenThrow(new IllegalStateException("Player already in session"));

        ResponseEntity<JoinSessionResponse> response = controller.joinSession("sess-1", authentication);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // ---- Leave Session ----

    @Test
    @DisplayName("leaveSession - should return 200 OK")
    void leaveSession_shouldReturnOk() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        doNothing().when(gameSessionService).leaveSession(1L, "sess-1");

        ResponseEntity<Void> response = controller.leaveSession("sess-1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gameSessionService).leaveSession(1L, "sess-1");
    }

    // ---- List Sessions ----

    @Test
    @DisplayName("listSessions - should return empty list when no sessions")
    void listSessions_shouldReturnEmpty() {
        when(gameSessionService.listSessions()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Map<String, Object>>> response = controller.listSessions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("listSessions - should return all sessions")
    void listSessions_shouldReturnAll() {
        Map<String, Object> session1 = Map.of(
                "sessionId", "sess-1",
                "name", "Game 1",
                "playerCount", 2
        );
        when(gameSessionService.listSessions()).thenReturn(List.of(session1));

        ResponseEntity<List<Map<String, Object>>> response = controller.listSessions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ---- Player ID Extraction ----

    @Test
    @DisplayName("createSession - should use numeric username as player ID")
    void createSession_shouldUseNumericUsername() {
        CreateSessionRequest request = new CreateSessionRequest("My Game");
        CreateSessionResponse expected = new CreateSessionResponse("sess-1", "My Game", 12345L, "12345");

        when(authentication.getName()).thenReturn("12345");
        when(playerIdExtractor.extract(authentication)).thenReturn(12345L);
        when(gameSessionService.createSession(eq(12345L), eq("12345"), eq("My Game")))
                .thenReturn(expected);

        controller.createSession(request, authentication);

        verify(gameSessionService).createSession(12345L, "12345", "My Game");
    }
}
