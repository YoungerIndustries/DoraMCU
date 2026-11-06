package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link PersistedGameSession}.
 */
class PersistedGameSessionTest {

    // ---- Constructor Tests ----

    @Test
    @DisplayName("Default constructor creates empty session")
    void testDefaultConstructor() {
        PersistedGameSession session = new PersistedGameSession();

        assertNull(session.getId());
        assertNull(session.getSessionId());
        assertNull(session.getSessionName());
        assertNull(session.getGameState());
        assertNull(session.getPlayerIds());
        assertNull(session.getCreatedAt());
        assertNull(session.getUpdatedAt());
        assertNull(session.getLastActivityAt());
    }

    // ---- ID Tests ----

    @Test
    @DisplayName("Should set and get ID")
    void testId() {
        PersistedGameSession session = new PersistedGameSession();
        session.setId(1L);

        assertEquals(1L, session.getId());
    }

    // ---- Session ID Tests ----

    @Test
    @DisplayName("Should set and get session ID")
    void testSessionId() {
        PersistedGameSession session = new PersistedGameSession();
        session.setSessionId("abc-123");

        assertEquals("abc-123", session.getSessionId());
    }

    @Test
    @DisplayName("Should set session ID to null")
    void testSessionIdNull() {
        PersistedGameSession session = new PersistedGameSession();
        session.setSessionId("abc-123");
        session.setSessionId(null);

        assertNull(session.getSessionId());
    }

    // ---- Session Name Tests ----

    @Test
    @DisplayName("Should set and get session name")
    void testSessionName() {
        PersistedGameSession session = new PersistedGameSession();
        session.setSessionName("My Game Session");

        assertEquals("My Game Session", session.getSessionName());
    }

    // ---- Game State Tests ----

    @Test
    @DisplayName("Should set and get game state")
    void testGameState() {
        PersistedGameSession session = new PersistedGameSession();
        String json = "{\"turn\":1,\"players\":[1,2]}";
        session.setGameState(json);

        assertEquals(json, session.getGameState());
    }

    // ---- Player IDs Tests ----

    @Test
    @DisplayName("Should set and get player IDs")
    void testPlayerIds() {
        PersistedGameSession session = new PersistedGameSession();
        session.setPlayerIds("1,2,3");

        assertEquals("1,2,3", session.getPlayerIds());
    }

    // ---- Timestamp Tests ----

    @Test
    @DisplayName("Should set and get created at")
    void testCreatedAt() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        PersistedGameSession session = new PersistedGameSession();
        session.setCreatedAt(now);

        assertEquals(now, session.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get updated at")
    void testUpdatedAt() {
        LocalDateTime now = LocalDateTime.of(2024, 6, 20, 14, 45, 0);
        PersistedGameSession session = new PersistedGameSession();
        session.setUpdatedAt(now);

        assertEquals(now, session.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set and get last activity at")
    void testLastActivityAt() {
        LocalDateTime now = LocalDateTime.of(2024, 12, 25, 0, 0, 0);
        PersistedGameSession session = new PersistedGameSession();
        session.setLastActivityAt(now);

        assertEquals(now, session.getLastActivityAt());
    }

    // ---- Full Session Tests ----

    @Test
    @DisplayName("Should set all fields and retrieve them")
    void testFullSession() {
        PersistedGameSession session = new PersistedGameSession();
        session.setId(42L);
        session.setSessionId("session-42");
        session.setSessionName("Galactic War");
        session.setGameState("{\"turn\":10}");
        session.setPlayerIds("1,2,3,4");
        session.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
        session.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 0, 0, 0));
        session.setLastActivityAt(LocalDateTime.of(2024, 1, 15, 12, 0, 0));

        assertEquals(42L, session.getId());
        assertEquals("session-42", session.getSessionId());
        assertEquals("Galactic War", session.getSessionName());
        assertEquals("{\"turn\":10}", session.getGameState());
        assertEquals("1,2,3,4", session.getPlayerIds());
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0, 0), session.getCreatedAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 0, 0, 0), session.getUpdatedAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 12, 0, 0), session.getLastActivityAt());
    }

    @Test
    @DisplayName("Should allow updating fields multiple times")
    void testUpdateFields() {
        PersistedGameSession session = new PersistedGameSession();
        session.setSessionName("Old Name");
        assertEquals("Old Name", session.getSessionName());

        session.setSessionName("New Name");
        assertEquals("New Name", session.getSessionName());
    }
}
