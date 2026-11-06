package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Game - Empire ID and Player Creation Tests")
class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game("Test Galaxy");
    }

    // ==================== Empire ID Counter ====================

    @Test
    @DisplayName("getNextEmpireIdAndIncrement - should start at 1")
    void getNextEmpireIdAndIncrement_shouldStartAtOne() {
        assertEquals(1, game.getNextEmpireIdAndIncrement());
    }

    @Test
    @DisplayName("getNextEmpireIdAndIncrement - should increment sequentially")
    void getNextEmpireIdAndIncrement_shouldIncrementSequentially() {
        int first = game.getNextEmpireIdAndIncrement();
        int second = game.getNextEmpireIdAndIncrement();
        int third = game.getNextEmpireIdAndIncrement();

        assertEquals(1, first);
        assertEquals(2, second);
        assertEquals(3, third);
    }

    @Test
    @DisplayName("getNextEmpireIdAndIncrement - should produce unique IDs")
    void getNextEmpireIdAndIncrement_shouldProduceUniqueIds() {
        int id1 = game.getNextEmpireIdAndIncrement();
        int id2 = game.getNextEmpireIdAndIncrement();

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("getNextEmpireIdAndIncrement - should continue incrementing after many calls")
    void getNextEmpireIdAndIncrement_shouldContinueAfterManyCalls() {
        for (int i = 1; i <= 10; i++) {
            assertEquals(i, game.getNextEmpireIdAndIncrement());
        }
    }

    // ==================== Player Creation with Empire ID ====================

    @Test
    @DisplayName("createPlayer and setEmpireId - should assign unique empire ID to new player")
    void createPlayer_shouldAssignUniqueEmpireId() {
        com.yisy.modules.doramcu.hex.Player player = game.createPlayer(100, "New Player");
        int empireId = game.getNextEmpireIdAndIncrement();
        player.setEmpireId(empireId);

        assertEquals(1, player.getEmpireId());
    }

    @Test
    @DisplayName("createPlayer and setEmpireId - should assign different empire IDs to multiple players")
    void createPlayer_shouldAssignDifferentEmpireIds() {
        com.yisy.modules.doramcu.hex.Player player1 = game.createPlayer(100, "Player One");
        int empireId1 = game.getNextEmpireIdAndIncrement();
        player1.setEmpireId(empireId1);

        com.yisy.modules.doramcu.hex.Player player2 = game.createPlayer(101, "Player Two");
        int empireId2 = game.getNextEmpireIdAndIncrement();
        player2.setEmpireId(empireId2);

        assertNotEquals(player1.getEmpireId(), player2.getEmpireId());
        assertEquals(1, player1.getEmpireId());
        assertEquals(2, player2.getEmpireId());
    }

    @Test
    @DisplayName("createPlayer and setEmpireId - empire ID should not conflict with player ID")
    void createPlayer_empireIdShouldNotConflictWithPlayerId() {
        // Player ID is 100, empire ID should start at 1
        com.yisy.modules.doramcu.hex.Player player = game.createPlayer(100, "New Player");
        int empireId = game.getNextEmpireIdAndIncrement();
        player.setEmpireId(empireId);

        assertEquals(100, player.getId());
        assertEquals(1, player.getEmpireId());
        assertNotEquals(player.getId(), player.getEmpireId());
    }

    @Test
    @DisplayName("createPlayer and setEmpireId - player should have null empireId before assignment")
    void createPlayer_empireIdShouldBeNullBeforeAssignment() {
        com.yisy.modules.doramcu.hex.Player player = game.createPlayer(100, "New Player");

        assertNull(player.getEmpireId());
    }

    @Test
    @DisplayName("createPlayer and setEmpireId - multiple players should have sequential empire IDs")
    void createPlayer_multiplePlayersSequentialEmpireIds() {
        com.yisy.modules.doramcu.hex.Player player1 = game.createPlayer(100, "Player One");
        player1.setEmpireId(game.getNextEmpireIdAndIncrement());

        com.yisy.modules.doramcu.hex.Player player2 = game.createPlayer(101, "Player Two");
        player2.setEmpireId(game.getNextEmpireIdAndIncrement());

        com.yisy.modules.doramcu.hex.Player player3 = game.createPlayer(102, "Player Three");
        player3.setEmpireId(game.getNextEmpireIdAndIncrement());

        assertEquals(1, player1.getEmpireId());
        assertEquals(2, player2.getEmpireId());
        assertEquals(3, player3.getEmpireId());
    }

    // ==================== Player Creation ====================

    @Test
    @DisplayName("createPlayer with ID - should create player with specified ID")
    void createPlayerWithId_shouldCreatePlayerWithSpecifiedId() {
        com.yisy.modules.doramcu.hex.Player player = game.createPlayer(42, "Test Player");

        assertEquals(42, player.getId());
        assertEquals("Test Player", player.getName());
    }

    @Test
    @DisplayName("createPlayer with auto ID - should create player with auto-generated ID")
    void createPlayerAutoId_shouldCreatePlayerWithAutoId() {
        com.yisy.modules.doramcu.hex.Player player = game.createPlayer("Auto Player");

        assertEquals("Auto Player", player.getName());
        assertNotNull(player.getId());
    }

    @Test
    @DisplayName("createPlayer - should add player to game's player list")
    void createPlayer_shouldAddPlayerToList() {
        com.yisy.modules.doramcu.hex.Player player = game.createPlayer(100, "New Player");

        assertEquals(1, game.getPlayerCount());
        assertTrue(game.getPlayerList().contains(player));
    }

    @Test
    @DisplayName("createPlayer - should set empire name from player name")
    void createPlayer_shouldSetEmpireName() {
        com.yisy.modules.doramcu.hex.Player player = game.createPlayer(100, "Terran Republic");

        assertEquals("Terran Republic", player.getEmpireName());
    }

    // ==================== Planet Creation for Player ====================

    @Test
    @DisplayName("createPlanet - should create planet for player with empire")
    void createPlanet_shouldCreatePlanetForPlayer() {
        com.yisy.modules.doramcu.hex.Player player = game.createPlayer(100, "New Player");
        int empireId = game.getNextEmpireIdAndIncrement();
        player.setEmpireId(empireId);

        game.createPlanet("Home-World", PlanetSize.MEDIUM, MineralQuality.RICH, player);

        assertEquals(1, game.getPlanetsMap().size());
        assertTrue(player.getOwnedPlanetIds().containsAll(game.getPlanetsMap().keySet()));
    }

    @Test
    @DisplayName("createPlanet - should assign planet to player's owned planets")
    void createPlanet_shouldAssignToPlayerOwnedPlanets() {
        com.yisy.modules.doramcu.hex.Player player = game.createPlayer(100, "New Player");
        player.setEmpireId(game.getNextEmpireIdAndIncrement());

        game.createPlanet("Home-World", PlanetSize.MEDIUM, MineralQuality.RICH, player);

        assertEquals(1, player.getOwnedPlanetIds().size());
    }

    // ==================== Game Constructor ====================

    @Test
    @DisplayName("Game constructor - should throw on null name")
    void constructor_shouldThrowOnNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Game(null));
    }

    @Test
    @DisplayName("Game constructor - should throw on blank name")
    void constructor_shouldThrowOnBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Game("   "));
    }

    @Test
    @DisplayName("Game constructor - should throw on empty name")
    void constructor_shouldThrowOnEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> new Game(""));
    }

    @Test
    @DisplayName("Game constructor - should initialize with no players")
    void constructor_shouldInitializeWithNoPlayers() {
        assertEquals(0, game.getPlayerCount());
    }

    @Test
    @DisplayName("Game constructor - should initialize with empty planets map")
    void constructor_shouldInitializeWithEmptyPlanets() {
        assertTrue(game.getPlanetsMap().isEmpty());
    }
}
