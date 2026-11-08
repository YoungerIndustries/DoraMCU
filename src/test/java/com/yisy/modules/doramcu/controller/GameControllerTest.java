package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.exception.NotYourTurnException;
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
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameController Tests")
class GameControllerTest {

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private PlayerIdExtractor playerIdExtractor;

    @Mock
    private Authentication authentication;

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController(gameSessionService, playerIdExtractor);
    }

    // ---- Get Game State ----

    @Test
    @DisplayName("getGameState - should return 200 OK with game state")
    void getGameState_shouldReturnOk() {
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getGameState(eq(1L), eq("sess-1"), eq(false)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.getGameState("sess-1", false, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    @DisplayName("getGameState - should throw NoSuchElementException when session not found")
    void getGameState_shouldThrow404() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getGameState(eq(1L), eq("nonexistent"), eq(false)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.getGameState("nonexistent", false, authentication));
    }

    // ---- Move Fleet ----

    @Test
    @DisplayName("moveFleet - should return 200 OK on success")
    void moveFleet_shouldReturnOk() {
        MoveFleetRequest request = new MoveFleetRequest(0, 1, 1);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.moveFleet(eq(1L), eq("sess-1"), any(MoveFleetRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.moveFleet("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("moveFleet - should throw NoSuchElementException when session not found")
    void moveFleet_shouldThrow404() {
        MoveFleetRequest request = new MoveFleetRequest(0, 1, 1);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.moveFleet(eq(1L), eq("nonexistent"), any(MoveFleetRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.moveFleet("nonexistent", request, authentication));
    }

    @Test
    @DisplayName("moveFleet - should throw NotYourTurnException when not player's turn")
    void moveFleet_shouldThrow403() {
        MoveFleetRequest request = new MoveFleetRequest(0, 1, 1);

        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.moveFleet(eq(2L), eq("sess-1"), any(MoveFleetRequest.class)))
                .thenThrow(new NotYourTurnException("Not your turn"));

        assertThrows(NotYourTurnException.class, () -> controller.moveFleet("sess-1", request, authentication));
    }

    @Test
    @DisplayName("moveFleet - should throw IllegalStateException when fleet not found")
    void moveFleet_shouldThrow400() {
        MoveFleetRequest request = new MoveFleetRequest(999, 1, 1);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.moveFleet(eq(1L), eq("sess-1"), any(MoveFleetRequest.class)))
                .thenThrow(new IllegalStateException("Fleet not found or not owned"));

        assertThrows(IllegalStateException.class, () -> controller.moveFleet("sess-1", request, authentication));
    }

    // ---- Build Ship ----

    @Test
    @DisplayName("buildShip - should return 200 OK on success")
    void buildShip_shouldReturnOk() {
        BuildShipRequest request = new BuildShipRequest("FIGHTER", null);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildShip(eq(1L), eq("sess-1"), any(BuildShipRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.buildShip("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("buildShip - should throw NoSuchElementException when session not found")
    void buildShip_shouldThrow404() {
        BuildShipRequest request = new BuildShipRequest("FIGHTER", null);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildShip(eq(1L), eq("nonexistent"), any(BuildShipRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.buildShip("nonexistent", request, authentication));
    }

    @Test
    @DisplayName("buildShip - should throw NotYourTurnException when not player's turn")
    void buildShip_shouldThrow403() {
        BuildShipRequest request = new BuildShipRequest("FIGHTER", null);

        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.buildShip(eq(2L), eq("sess-1"), any(BuildShipRequest.class)))
                .thenThrow(new NotYourTurnException("Not your turn"));

        assertThrows(NotYourTurnException.class, () -> controller.buildShip("sess-1", request, authentication));
    }

    @Test
    @DisplayName("buildShip - should throw IllegalArgumentException on invalid ship type")
    void buildShip_shouldThrow400() {
        BuildShipRequest request = new BuildShipRequest("INVALID", null);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildShip(eq(1L), eq("sess-1"), any(BuildShipRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid ship type"));

        assertThrows(IllegalArgumentException.class, () -> controller.buildShip("sess-1", request, authentication));
    }

    // ---- Engage Combat ----

    @Test
    @DisplayName("engageCombat - should return 200 OK on success")
    void engageCombat_shouldReturnOk() {
        EngageCombatRequest request = new EngageCombatRequest(0, 1);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.engageCombat(eq(1L), eq("sess-1"), any(EngageCombatRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.engageCombat("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("engageCombat - should throw NoSuchElementException when session not found")
    void engageCombat_shouldThrow404() {
        EngageCombatRequest request = new EngageCombatRequest(0, 1);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.engageCombat(eq(1L), eq("nonexistent"), any(EngageCombatRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.engageCombat("nonexistent", request, authentication));
    }

    @Test
    @DisplayName("engageCombat - should throw NotYourTurnException when not player's turn")
    void engageCombat_shouldThrow403() {
        EngageCombatRequest request = new EngageCombatRequest(0, 1);

        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.engageCombat(eq(2L), eq("sess-1"), any(EngageCombatRequest.class)))
                .thenThrow(new NotYourTurnException("Not your turn"));

        assertThrows(NotYourTurnException.class, () -> controller.engageCombat("sess-1", request, authentication));
    }

    @Test
    @DisplayName("engageCombat - should throw IllegalStateException when fleet not found")
    void engageCombat_shouldThrow400() {
        EngageCombatRequest request = new EngageCombatRequest(999, 1);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.engageCombat(eq(1L), eq("sess-1"), any(EngageCombatRequest.class)))
                .thenThrow(new IllegalStateException("Attacker fleet not found or not owned"));

        assertThrows(IllegalStateException.class, () -> controller.engageCombat("sess-1", request, authentication));
    }

    // ---- Advance Turn ----

    @Test
    @DisplayName("advanceTurn - should return 200 OK on success")
    void advanceTurn_shouldReturnOk() {
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 1, 2L, "Player2", Collections.emptyList(), false);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.advanceTurn(eq(1L), eq("sess-1")))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.advanceTurn("sess-1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getCurrentTurn());
        assertEquals(2L, response.getBody().getCurrentPlayerId());
    }

    @Test
    @DisplayName("advanceTurn - should throw NoSuchElementException when session not found")
    void advanceTurn_shouldThrow404() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.advanceTurn(eq(1L), eq("nonexistent")))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.advanceTurn("nonexistent", authentication));
    }

    @Test
    @DisplayName("advanceTurn - should throw NotYourTurnException when not player's turn")
    void advanceTurn_shouldThrow403() {
        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.advanceTurn(eq(2L), eq("sess-1")))
                .thenThrow(new NotYourTurnException("Not your turn"));

        assertThrows(NotYourTurnException.class, () -> controller.advanceTurn("sess-1", authentication));
    }
}

