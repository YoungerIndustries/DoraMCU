package com.yisy.modules.doramcu.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameEvent Tests")
class GameEventTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        GameEvent event = new GameEvent(
                GameEvent.EventType.TURN_CHANGED,
                "sess-1",
                Map.of("turn", 1)
        );

        assertEquals(GameEvent.EventType.TURN_CHANGED, event.getType());
        assertEquals("sess-1", event.getSessionId());
        assertEquals(Map.of("turn", 1), event.getPayload());
    }

    @Test
    @DisplayName("Default constructor - should create empty event")
    void defaultConstructor_shouldCreateEmpty() {
        GameEvent event = new GameEvent();
        assertNull(event.getType());
        assertNull(event.getSessionId());
        assertNull(event.getPayload());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        GameEvent event = new GameEvent();
        event.setType(GameEvent.EventType.PLAYER_JOINED);
        event.setSessionId("sess-2");
        event.setPayload("data");

        assertEquals(GameEvent.EventType.PLAYER_JOINED, event.getType());
        assertEquals("sess-2", event.getSessionId());
        assertEquals("data", event.getPayload());
    }

    @Test
    @DisplayName("EventType enum - should contain all expected values")
    void eventType_shouldContainAllValues() {
        GameEvent.EventType[] types = GameEvent.EventType.values();
        assertEquals(6, types.length);
    }
}

@DisplayName("CreateSessionRequest Tests")
class CreateSessionRequestTest {

    @Test
    @DisplayName("Constructor - should initialize session name")
    void constructor_shouldInitializeName() {
        CreateSessionRequest request = new CreateSessionRequest("My Game");
        assertEquals("My Game", request.getSessionName());
    }

    @Test
    @DisplayName("Default constructor - should create empty request")
    void defaultConstructor_shouldCreateEmpty() {
        CreateSessionRequest request = new CreateSessionRequest();
        assertNull(request.getSessionName());
    }

    @Test
    @DisplayName("Setter - should update session name")
    void setter_shouldUpdateName() {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setSessionName("New Game");
        assertEquals("New Game", request.getSessionName());
    }
}

@DisplayName("CreateSessionResponse Tests")
class CreateSessionResponseTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        CreateSessionResponse response = new CreateSessionResponse("sess-1", "My Game", 1L, "Player1");

        assertEquals("sess-1", response.getSessionId());
        assertEquals("My Game", response.getSessionName());
        assertEquals(1L, response.getPlayerId());
        assertEquals("Player1", response.getPlayerName());
    }

    @Test
    @DisplayName("Default constructor - should create empty response")
    void defaultConstructor_shouldCreateEmpty() {
        CreateSessionResponse response = new CreateSessionResponse();
        assertNull(response.getSessionId());
        assertNull(response.getSessionName());
        assertNull(response.getPlayerId());
        assertNull(response.getPlayerName());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        CreateSessionResponse response = new CreateSessionResponse();
        response.setSessionId("sess-2");
        response.setSessionName("New Game");
        response.setPlayerId(2L);
        response.setPlayerName("Player2");

        assertEquals("sess-2", response.getSessionId());
        assertEquals("New Game", response.getSessionName());
        assertEquals(2L, response.getPlayerId());
        assertEquals("Player2", response.getPlayerName());
    }
}

@DisplayName("JoinSessionRequest Tests")
class JoinSessionRequestTest {

    @Test
    @DisplayName("Constructor - should initialize session ID")
    void constructor_shouldInitializeSessionId() {
        JoinSessionRequest request = new JoinSessionRequest("sess-1");
        assertEquals("sess-1", request.getSessionId());
    }

    @Test
    @DisplayName("Default constructor - should create empty request")
    void defaultConstructor_shouldCreateEmpty() {
        JoinSessionRequest request = new JoinSessionRequest();
        assertNull(request.getSessionId());
    }
}

@DisplayName("JoinSessionResponse Tests")
class JoinSessionResponseTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        JoinSessionResponse response = new JoinSessionResponse("sess-1", "My Game", 2L, "Player2");

        assertEquals("sess-1", response.getSessionId());
        assertEquals("My Game", response.getSessionName());
        assertEquals(2L, response.getPlayerId());
        assertEquals("Player2", response.getPlayerName());
    }

    @Test
    @DisplayName("Default constructor - should create empty response")
    void defaultConstructor_shouldCreateEmpty() {
        JoinSessionResponse response = new JoinSessionResponse();
        assertNull(response.getSessionId());
    }
}

@DisplayName("PlayerSummary Tests")
class PlayerSummaryTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        Map<String, Integer> resources = Map.of("MINERALS", 100, "ENERGY", 50);
        PlayerSummary summary = new PlayerSummary(1L, "Player1", resources, 3, 2);

        assertEquals(1L, summary.getId());
        assertEquals("Player1", summary.getName());
        assertEquals(resources, summary.getResources());
        assertEquals(3, summary.getPlanetCount());
        assertEquals(2, summary.getFleetCount());
    }

    @Test
    @DisplayName("Default constructor - should create empty summary")
    void defaultConstructor_shouldCreateEmpty() {
        PlayerSummary summary = new PlayerSummary();
        assertNull(summary.getId());
        assertNull(summary.getName());
        assertNull(summary.getResources());
        assertEquals(0, summary.getPlanetCount());
        assertEquals(0, summary.getFleetCount());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        PlayerSummary summary = new PlayerSummary();
        summary.setId(2L);
        summary.setName("Player2");
        summary.setResources(Map.of("MINERALS", 200));
        summary.setPlanetCount(5);
        summary.setFleetCount(3);

        assertEquals(2L, summary.getId());
        assertEquals("Player2", summary.getName());
        assertEquals(5, summary.getPlanetCount());
        assertEquals(3, summary.getFleetCount());
    }
}

@DisplayName("GameStateResponse Tests")
class GameStateResponseTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        List<PlayerSummary> players = List.of(new PlayerSummary(1L, "Player1", Map.of(), 1, 0));
        GameStateResponse response = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", players, true);

        assertEquals("sess-1", response.getSessionId());
        assertEquals(0, response.getCurrentTurn());
        assertEquals(1L, response.getCurrentPlayerId());
        assertEquals("Player1", response.getCurrentPlayerName());
        assertEquals(players, response.getPlayers());
        assertTrue(response.isMyTurn());
    }

    @Test
    @DisplayName("Default constructor - should create empty response")
    void defaultConstructor_shouldCreateEmpty() {
        GameStateResponse response = new GameStateResponse();
        assertNull(response.getSessionId());
        assertEquals(0, response.getCurrentTurn());
        assertNull(response.getCurrentPlayerId());
        assertNull(response.getCurrentPlayerName());
        assertNull(response.getPlayers());
        assertFalse(response.isMyTurn());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        GameStateResponse response = new GameStateResponse();
        response.setSessionId("sess-2");
        response.setCurrentTurn(5);
        response.setCurrentPlayerId(2L);
        response.setCurrentPlayerName("Player2");
        response.setPlayers(List.of());
        response.setMyTurn(false);

        assertEquals("sess-2", response.getSessionId());
        assertEquals(5, response.getCurrentTurn());
        assertEquals(2L, response.getCurrentPlayerId());
        assertEquals("Player2", response.getCurrentPlayerName());
        assertFalse(response.isMyTurn());
    }
}

@DisplayName("MoveFleetRequest Tests")
class MoveFleetRequestTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        MoveFleetRequest request = new MoveFleetRequest(0, 5, 3);

        assertEquals(0, request.getFleetId());
        assertEquals(5, request.getTargetRow());
        assertEquals(3, request.getTargetCol());
    }

    @Test
    @DisplayName("Default constructor - should create empty request")
    void defaultConstructor_shouldCreateEmpty() {
        MoveFleetRequest request = new MoveFleetRequest();
        assertNull(request.getFleetId());
        assertNull(request.getTargetRow());
        assertNull(request.getTargetCol());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        MoveFleetRequest request = new MoveFleetRequest();
        request.setFleetId(1);
        request.setTargetRow(10);
        request.setTargetCol(20);

        assertEquals(1, request.getFleetId());
        assertEquals(10, request.getTargetRow());
        assertEquals(20, request.getTargetCol());
    }
}

@DisplayName("BuildShipRequest Tests")
class BuildShipRequestTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        BuildShipRequest request = new BuildShipRequest("FIGHTER", 0);

        assertEquals("FIGHTER", request.getShipType());
        assertEquals(0, request.getTargetFleetId());
    }

    @Test
    @DisplayName("Default constructor - should create empty request")
    void defaultConstructor_shouldCreateEmpty() {
        BuildShipRequest request = new BuildShipRequest();
        assertNull(request.getShipType());
        assertNull(request.getTargetFleetId());
        assertNull(request.getHullShape());
        assertNull(request.getExteriorMaterial());
        assertNull(request.getInteriorMaterial());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        BuildShipRequest request = new BuildShipRequest();
        request.setShipType("CRUISER");
        request.setTargetFleetId(1);
        request.setHullShape("WEDGE");
        request.setExteriorMaterial(2);
        request.setInteriorMaterial(0);

        assertEquals("CRUISER", request.getShipType());
        assertEquals(1, request.getTargetFleetId());
        assertEquals("WEDGE", request.getHullShape());
        assertEquals(2, request.getExteriorMaterial());
        assertEquals(0, request.getInteriorMaterial());
    }
}

@DisplayName("EngageCombatRequest Tests")
class EngageCombatRequestTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        EngageCombatRequest request = new EngageCombatRequest(0, 1);

        assertEquals(0, request.getAttackerFleetId());
        assertEquals(1, request.getDefenderFleetId());
    }

    @Test
    @DisplayName("Default constructor - should create empty request")
    void defaultConstructor_shouldCreateEmpty() {
        EngageCombatRequest request = new EngageCombatRequest();
        assertNull(request.getAttackerFleetId());
        assertNull(request.getDefenderFleetId());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        EngageCombatRequest request = new EngageCombatRequest();
        request.setAttackerFleetId(2);
        request.setDefenderFleetId(3);

        assertEquals(2, request.getAttackerFleetId());
        assertEquals(3, request.getDefenderFleetId());
    }
}

@DisplayName("LoginRequest Tests")
class LoginRequestTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        LoginRequest request = new LoginRequest("admin", "secret");

        assertEquals("admin", request.getUsername());
        assertEquals("secret", request.getPassword());
    }

    @Test
    @DisplayName("Default constructor - should create empty request")
    void defaultConstructor_shouldCreateEmpty() {
        LoginRequest request = new LoginRequest();
        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        LoginRequest request = new LoginRequest();
        request.setUsername("player1");
        request.setPassword("pass123");

        assertEquals("player1", request.getUsername());
        assertEquals("pass123", request.getPassword());
    }
}

@DisplayName("LoginResponse Tests")
class LoginResponseTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        LoginResponse response = new LoginResponse("jwt-token", "admin");

        assertEquals("jwt-token", response.getToken());
        assertEquals("admin", response.getUsername());
    }

    @Test
    @DisplayName("Default constructor - should create empty response")
    void defaultConstructor_shouldCreateEmpty() {
        LoginResponse response = new LoginResponse();
        assertNull(response.getToken());
        assertNull(response.getUsername());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        LoginResponse response = new LoginResponse();
        response.setToken("new-token");
        response.setUsername("player1");

        assertEquals("new-token", response.getToken());
        assertEquals("player1", response.getUsername());
    }
}

@DisplayName("SubSectorInfo Tests")
class SubSectorInfoTest {

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        SubSectorInfo info = new SubSectorInfo(1, "Alpha-1", 0, 0, 0, 0, true, 2, List.of());

        assertEquals(1, info.getId());
        assertEquals("Alpha-1", info.getName());
        assertEquals(0, info.getSectorRow());
        assertEquals(0, info.getSectorCol());
        assertEquals(0, info.getSubRow());
        assertEquals(0, info.getSubCol());
        assertTrue(info.isGenerated());
        assertEquals(2, info.getSystemCount());
        assertEquals(List.of(), info.getSystems());
    }

    @Test
    @DisplayName("Default constructor - should create empty info")
    void defaultConstructor_shouldCreateEmpty() {
        SubSectorInfo info = new SubSectorInfo();
        assertEquals(0, info.getId());
        assertNull(info.getName());
        assertFalse(info.isGenerated());
        assertEquals(0, info.getSystemCount());
        assertNull(info.getSystems());
    }

    @Test
    @DisplayName("Setters - should update all fields")
    void setters_shouldUpdateFields() {
        SubSectorInfo info = new SubSectorInfo();
        info.setId(5);
        info.setName("Beta-2");
        info.setSectorRow(1);
        info.setSectorCol(2);
        info.setSubRow(3);
        info.setSubCol(4);
        info.setGenerated(true);
        info.setSystemCount(3);
        info.setSystems(List.of(Map.of("name", "Sol")));

        assertEquals(5, info.getId());
        assertEquals("Beta-2", info.getName());
        assertEquals(1, info.getSectorRow());
        assertEquals(2, info.getSectorCol());
        assertEquals(3, info.getSubRow());
        assertEquals(4, info.getSubCol());
        assertTrue(info.isGenerated());
        assertEquals(3, info.getSystemCount());
        assertEquals(List.of(Map.of("name", "Sol")), info.getSystems());
    }
}
