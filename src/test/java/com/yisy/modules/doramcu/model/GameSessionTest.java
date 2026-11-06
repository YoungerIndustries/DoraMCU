package com.yisy.modules.doramcu.model;

import com.yisy.modules.doramcu.hex.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameSession Tests")
class GameSessionTest {

    private GameSession session;

    @BeforeEach
    void setUp() {
        Game game = new Game("Test Game");
        session = new GameSession("sess-1", "Test Game", game);
    }

    // ---- Constructor & Getters ----

    @Test
    @DisplayName("Constructor - should initialize all fields")
    void constructor_shouldInitializeFields() {
        assertEquals("sess-1", session.getSessionId());
        assertEquals("Test Game", session.getName());
        assertNotNull(session.getGame());
        assertNotNull(session.getEmitters());
        assertNotNull(session.getPlayerIds());
        assertNotNull(session.getLock());
    }

    @Test
    @DisplayName("Constructor - should start with empty players")
    void constructor_shouldStartEmpty() {
        Set<Long> players = session.getPlayerIds();
        assertTrue(players.isEmpty());
    }

    @Test
    @DisplayName("Constructor - should start with empty emitters")
    void constructor_shouldStartWithEmptyEmitters() {
        assertTrue(session.getEmitters().isEmpty());
    }

    // ---- Player Management ----

    @Test
    @DisplayName("addPlayer - should add player ID")
    void addPlayer_shouldAddPlayer() {
        session.addPlayer(1L);
        assertTrue(session.getPlayerIds().contains(1L));
        assertEquals(1, session.getPlayerIds().size());
    }

    @Test
    @DisplayName("addPlayer - should add multiple players")
    void addPlayer_shouldAddMultiplePlayers() {
        session.addPlayer(1L);
        session.addPlayer(2L);
        session.addPlayer(3L);

        assertEquals(3, session.getPlayerIds().size());
        assertTrue(session.getPlayerIds().contains(1L));
        assertTrue(session.getPlayerIds().contains(2L));
        assertTrue(session.getPlayerIds().contains(3L));
    }

    @Test
    @DisplayName("removePlayer - should remove player ID")
    void removePlayer_shouldRemovePlayer() {
        session.addPlayer(1L);
        session.addPlayer(2L);

        session.removePlayer(1L);

        assertFalse(session.getPlayerIds().contains(1L));
        assertTrue(session.getPlayerIds().contains(2L));
        assertEquals(1, session.getPlayerIds().size());
    }

    @Test
    @DisplayName("removePlayer - should remove associated emitter")
    void removePlayer_shouldRemoveEmitter() {
        SseEmitter emitter = new SseEmitter();
        session.registerEmitter(1L, emitter);
        session.addPlayer(1L);

        session.removePlayer(1L);

        assertFalse(session.getEmitters().containsKey(1L));
    }

    @Test
    @DisplayName("removePlayer - should do nothing for non-existent player")
    void removePlayer_shouldDoNothingForNonExistent() {
        // Should not throw
        session.removePlayer(999L);
        assertTrue(session.getPlayerIds().isEmpty());
    }

    // ---- Emitter Management ----

    @Test
    @DisplayName("registerEmitter - should register emitter for player")
    void registerEmitter_shouldRegister() {
        SseEmitter emitter = new SseEmitter();
        session.registerEmitter(1L, emitter);

        assertTrue(session.getEmitters().containsKey(1L));
        assertSame(emitter, session.getEmitters().get(1L));
    }

    @Test
    @DisplayName("registerEmitter - should overwrite existing emitter")
    void registerEmitter_shouldOverwrite() {
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();

        session.registerEmitter(1L, emitter1);
        session.registerEmitter(1L, emitter2);

        assertEquals(1, session.getEmitters().size());
        assertSame(emitter2, session.getEmitters().get(1L));
    }

    @Test
    @DisplayName("unregisterEmitter - should remove emitter")
    void unregisterEmitter_shouldRemove() {
        SseEmitter emitter = new SseEmitter();
        session.registerEmitter(1L, emitter);

        session.unregisterEmitter(1L);

        assertFalse(session.getEmitters().containsKey(1L));
    }

    @Test
    @DisplayName("unregisterEmitter - should do nothing for non-existent player")
    void unregisterEmitter_shouldDoNothingForNonExistent() {
        // Should not throw
        session.unregisterEmitter(999L);
    }

    // ---- Game Access ----

    @Test
    @DisplayName("getGame - should return the game instance")
    void getGame_shouldReturnGame() {
        Game game = session.getGame();
        assertNotNull(game);
        assertEquals("Test Game", game.getName());
    }

    // ---- Lock ----

    @Test
    @DisplayName("getLock - should return a non-null lock")
    void getLock_shouldReturnLock() {
        assertNotNull(session.getLock());
    }
}
