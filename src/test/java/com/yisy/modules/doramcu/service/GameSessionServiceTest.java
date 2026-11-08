package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.CreateSessionResponse;
import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import com.yisy.modules.doramcu.dto.MoveFleetRequest;
import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PersistedGameSessionRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import com.yisy.modules.doramcu.service.GameStateBuilder;
import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.unified.GlobalClock;
import com.yisy.modules.doramcu.unified.GlobalClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GameSessionService - New Player Empire Creation Tests")
class GameSessionServiceTest {

    private SolarSystemRepository solarSystemRepository;
    private PersistedGameSessionRepository persistedGameSessionRepository;
    private GalaxyRepository galaxyRepository;
    private TurnScheduler turnScheduler;
    private GameSessionService gameSessionService;

    @BeforeEach
    void setUp() {
        solarSystemRepository = null;
        persistedGameSessionRepository = null;
        galaxyRepository = mock(GalaxyRepository.class);
        when(galaxyRepository.findAll()).thenReturn(new ArrayList<>());
        turnScheduler = mock(TurnScheduler.class);
        when(turnScheduler.getTurnIntervalSeconds()).thenReturn(5L);
        when(turnScheduler.getAccumulatedSeconds(anyString())).thenReturn(0L);
        when(turnScheduler.isEnabled()).thenReturn(true);
        GameStateBuilder gameStateBuilder = mock(GameStateBuilder.class);
        when(gameStateBuilder.buildPlayerSummaries(any(), any())).thenReturn(new ArrayList<>());
        when(gameStateBuilder.buildSubSectorInfo(any(), anyBoolean())).thenReturn(new ArrayList<>());
        when(gameStateBuilder.buildActiveBattles(any())).thenReturn(new ArrayList<>());
        when(gameStateBuilder.buildGameStateResponse(any(), any(), anyBoolean())).thenAnswer(invocation -> {
            com.yisy.modules.doramcu.dto.GameStateResponse resp = new com.yisy.modules.doramcu.dto.GameStateResponse();
            resp.setSubSectors(new ArrayList<>());
            return resp;
        });
        SessionActionHandler sessionActionHandler = new SessionActionHandler(gameStateBuilder, null, null);
        GlobalClockService globalClockService = mock(GlobalClockService.class);
        when(globalClockService.getOrCreateClock(anyString(), any())).thenAnswer(invocation -> {
            String sessionId = invocation.getArgument(0);
            return GlobalClock.forGameType(GameType.DORAMCU_4X);
        });
        gameSessionService = new GameSessionService(
                persistedGameSessionRepository,
                galaxyRepository,
                null,
                turnScheduler,
                gameStateBuilder,
                null,
                null,
                sessionActionHandler,
                globalClockService
        );
    }

    // ==================== createSession - Empire Creation ====================

    @Nested
    @DisplayName("createSession")
    class CreateSessionTests {

        @Test
        @DisplayName("should create session with new empire for player")
        void createSession_shouldCreateNewEmpireForPlayer() {
            CreateSessionResponse response = gameSessionService.createSession(
                    100L, "Human Player", "Test Session");

            assertNotNull(response);
            assertNotNull(response.getSessionId());
            assertEquals("Test Session", response.getSessionName());
            assertEquals(100L, response.getPlayerId());
            assertEquals("Human Player", response.getPlayerName());
        }

        @Test
        @DisplayName("should assign empire ID from game counter, not player ID")
        void createSession_shouldAssignEmpireIdFromCounter() {
            String sessionId = gameSessionService.createSession(
                    100L, "Human Player", "Test Session").getSessionId();

            GameSession session = gameSessionService.getGameSession(sessionId);
            assertNotNull(session);

            Game game = (Game) session.getGame();
            Player player = game.getPlayer(100);
            assertNotNull(player);
            // Empire ID should be 1 (from counter), not 100 (player ID)
            assertEquals(1, player.getEmpireId());
            assertNotEquals(100, player.getEmpireId());
        }

        @Test
        @DisplayName("should create home planet for new player")
        void createSession_shouldCreateHomePlanet() {
            String sessionId = gameSessionService.createSession(
                    100L, "Human Player", "Test Session").getSessionId();

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(100);

            assertNotNull(player);
            assertEquals(1, player.getOwnedPlanetIds().size());
        }

        @Test
        @DisplayName("should add player to session player list")
        void createSession_shouldAddPlayerToSession() {
            String sessionId = gameSessionService.createSession(
                    100L, "Human Player", "Test Session").getSessionId();

            GameSession session = gameSessionService.getGameSession(sessionId);
            assertTrue(session.getPlayerIds().contains(100L));
        }

        @Test
        @DisplayName("should create player in game with correct ID")
        void createSession_shouldCreatePlayerWithCorrectId() {
            String sessionId = gameSessionService.createSession(
                    100L, "Human Player", "Test Session").getSessionId();

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();

            assertEquals(1, game.getPlayerCount());
            Player player = game.getPlayer(100);
            assertNotNull(player);
            assertEquals("Human Player", player.getName());
        }
    }

    // ==================== joinSession - Empire Creation ====================

    @Nested
    @DisplayName("joinSession")
    class JoinSessionTests {

        private String sessionId;

        @BeforeEach
        void createSession() {
            sessionId = gameSessionService.createDefaultSession("Multi Player Session").getSessionId();
        }

        @Test
        @DisplayName("should create new empire for joining player")
        void joinSession_shouldCreateNewEmpire() {
            JoinSessionResponse response = gameSessionService.joinSession(
                    200L, "Joining Player", sessionId);

            assertNotNull(response);
            assertEquals(sessionId, response.getSessionId());
            assertEquals(200L, response.getPlayerId());
            assertEquals("Joining Player", response.getPlayerName());
        }

        @Test
        @DisplayName("should assign unique empire ID to joining player")
        void joinSession_shouldAssignUniqueEmpireId() {
            gameSessionService.joinSession(200L, "Joining Player", sessionId);

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(200);

            assertNotNull(player);
            // Empire ID should be 1 (first from counter), not 200 (player ID)
            assertEquals(1, player.getEmpireId());
            assertNotEquals(200, player.getEmpireId());
        }

        @Test
        @DisplayName("should create home planet for joining player")
        void joinSession_shouldCreateHomePlanet() {
            gameSessionService.joinSession(200L, "Joining Player", sessionId);

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(200);

            assertNotNull(player);
            assertEquals(1, player.getOwnedPlanetIds().size());
        }

        @Test
        @DisplayName("should add joining player to session player list")
        void joinSession_shouldAddPlayerToSession() {
            gameSessionService.joinSession(200L, "Joining Player", sessionId);

            GameSession session = gameSessionService.getGameSession(sessionId);
            assertTrue(session.getPlayerIds().contains(200L));
        }

        @Test
        @DisplayName("should throw when joining non-existent session")
        void joinSession_shouldThrowOnNonExistentSession() {
            assertThrows(NoSuchElementException.class, () ->
                    gameSessionService.joinSession(200L, "Joining Player", "non-existent-id"));
        }

        @Test
        @DisplayName("should throw when player already in session")
        void joinSession_shouldThrowWhenPlayerAlreadyInSession() {
            gameSessionService.joinSession(200L, "Joining Player", sessionId);

            assertThrows(IllegalStateException.class, () ->
                    gameSessionService.joinSession(200L, "Joining Player", sessionId));
        }

        @Test
        @DisplayName("should create player in game with correct ID")
        void joinSession_shouldCreatePlayerWithCorrectId() {
            gameSessionService.joinSession(200L, "Joining Player", sessionId);

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();

            assertEquals(1, game.getPlayerCount());
            Player player = game.getPlayer(200);
            assertNotNull(player);
            assertEquals("Joining Player", player.getName());
        }
    }

    // ==================== Multiple Players - Unique Empires ====================

    @Nested
    @DisplayName("Multiple Players")
    class MultiplePlayersTests {

        @Test
        @DisplayName("multiple players joining should get unique empire IDs")
        void multiplePlayers_shouldGetUniqueEmpireIds() {
            String sessionId = gameSessionService.createDefaultSession("Multi Session").getSessionId();

            gameSessionService.joinSession(200L, "Player One", sessionId);
            gameSessionService.joinSession(201L, "Player Two", sessionId);
            gameSessionService.joinSession(202L, "Player Three", sessionId);

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();

            Player player1 = game.getPlayer(200);
            Player player2 = game.getPlayer(201);
            Player player3 = game.getPlayer(202);

            // All should have unique, sequential empire IDs
            assertNotNull(player1.getEmpireId());
            assertNotNull(player2.getEmpireId());
            assertNotNull(player3.getEmpireId());

            assertNotEquals(player1.getEmpireId(), player2.getEmpireId());
            assertNotEquals(player2.getEmpireId(), player3.getEmpireId());
            assertNotEquals(player1.getEmpireId(), player3.getEmpireId());

            assertEquals(1, player1.getEmpireId());
            assertEquals(2, player2.getEmpireId());
            assertEquals(3, player3.getEmpireId());
        }

        @Test
        @DisplayName("empire IDs should not conflict with player IDs")
        void multiplePlayers_empireIdsShouldNotConflictWithPlayerIds() {
            String sessionId = gameSessionService.createDefaultSession("Multi Session").getSessionId();

            gameSessionService.joinSession(1000L, "Player A", sessionId);
            gameSessionService.joinSession(2000L, "Player B", sessionId);

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();

            Player playerA = game.getPlayer(1000);
            Player playerB = game.getPlayer(2000);

            // Player IDs are 1000 and 2000, empire IDs should be 1 and 2
            assertEquals(1000, playerA.getId());
            assertEquals(1, playerA.getEmpireId());
            assertNotEquals(playerA.getId(), playerA.getEmpireId());

            assertEquals(2000, playerB.getId());
            assertEquals(2, playerB.getEmpireId());
            assertNotEquals(playerB.getId(), playerB.getEmpireId());
        }

        @Test
        @DisplayName("each player should have their own home planet")
        void multiplePlayers_eachShouldHaveHomePlanet() {
            String sessionId = gameSessionService.createDefaultSession("Multi Session").getSessionId();

            gameSessionService.joinSession(200L, "Player One", sessionId);
            gameSessionService.joinSession(201L, "Player Two", sessionId);

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();

            Player player1 = game.getPlayer(200);
            Player player2 = game.getPlayer(201);

            assertEquals(1, player1.getOwnedPlanetIds().size());
            assertEquals(1, player2.getOwnedPlanetIds().size());

            // Their planets should be different
            assertNotEquals(
                    player1.getOwnedPlanetIds().iterator().next(),
                    player2.getOwnedPlanetIds().iterator().next()
            );
        }

        @Test
        @DisplayName("createSession player should get unique empire ID separate from joinSession players")
        void createSessionAndJoinSession_shouldHaveUniqueEmpireIds() {
            // First player creates the session
            String sessionId = gameSessionService.createSession(
                    100L, "Creator", "Test Session").getSessionId();

            // Second player joins
            gameSessionService.joinSession(200L, "Joiner", sessionId);

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();

            Player creator = game.getPlayer(100);
            Player joiner = game.getPlayer(200);

            // Both should have unique empire IDs
            assertNotEquals(creator.getEmpireId(), joiner.getEmpireId());
            assertEquals(1, creator.getEmpireId());
            assertEquals(2, joiner.getEmpireId());
        }
    }

    // ==================== createDefaultSession ====================

    @Nested
    @DisplayName("createDefaultSession")
    class CreateDefaultSessionTests {

        @Test
        @DisplayName("should create session without initial player")
        void createDefaultSession_shouldCreateEmptySession() {
            CreateSessionResponse response = gameSessionService.createDefaultSession("AI Session");

            assertNotNull(response);
            assertNotNull(response.getSessionId());
            assertEquals("AI Session", response.getSessionName());
            assertNull(response.getPlayerId());
            assertNull(response.getPlayerName());
        }

        @Test
        @DisplayName("should create session with no players in game")
        void createDefaultSession_shouldHaveNoPlayers() {
            String sessionId = gameSessionService.createDefaultSession("AI Session").getSessionId();

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();

            assertEquals(0, game.getPlayerCount());
        }

        @Test
        @DisplayName("should allow AI players to join after default session creation")
        void createDefaultSession_shouldAllowAiPlayersToJoin() {
            String sessionId = gameSessionService.createDefaultSession("AI Session").getSessionId();

            // Simulate AI player joining
            gameSessionService.joinSession(1000L, "AI Easy", sessionId);
            gameSessionService.joinSession(1001L, "AI Medium", sessionId);
            gameSessionService.joinSession(1002L, "AI Hard", sessionId);

            GameSession session = gameSessionService.getGameSession(sessionId);
            Game game = (Game) session.getGame();

            assertEquals(3, game.getPlayerCount());
            assertEquals(3, session.getPlayerIds().size());
        }
    }

    // ==================== moveFleet - MDrive (within-system) ====================

    @Nested
    @DisplayName("moveFleet - MDrive (within-system)")
    class MoveFleetMDriveTests {

        private String sessionId;
        private Game game;
        private Player player;
        private Fleet fleet;

        @BeforeEach
        void setUpMoveFleet() {
            // Create session with one player
            sessionId = gameSessionService.createSession(100L, "Test Player", "Move Test").getSessionId();
            GameSession session = gameSessionService.getGameSession(sessionId);
            game = (Game) session.getGame();
            player = game.getPlayer(100);

            // Create a fleet with a frigate and position it
            fleet = game.createFleet("Test Fleet", player);
            Ship frigate = new Ship(game.getNextShipIdAndIncrement(), ShipType.FRIGATE);
            frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
            game.addShip(frigate);
            fleet.addShip(frigate);
            fleet.setPosition(new Hex3D(0, 0, 0));
        }

        @Test
        @DisplayName("should move fleet within system using MDrive")
        void moveFleet_mDrive_shouldSucceed() {
            // Target (1, -1, 0) — distance 1 from origin
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 1, -1, null);

            gameSessionService.moveFleet(100L, sessionId, request);

            Hex3D newPos = fleet.getPosition();
            assertEquals(1, newPos.getX());
            assertEquals(-1, newPos.getY());
        }

        @Test
        @DisplayName("should move fleet within MDrive range")
        void moveFleet_mDrive_withinRange_shouldSucceed() {
            // Frigate MDrive speed is 10, so distance 10 should be allowed
            // Target (10, -10, 0) — distance 10 from origin
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 10, -10, null);

            gameSessionService.moveFleet(100L, sessionId, request);

            Hex3D newPos = fleet.getPosition();
            assertEquals(10, newPos.getX());
        }

        @Test
        @DisplayName("should throw when MDrive distance exceeds range")
        void moveFleet_mDrive_exceedsRange_shouldThrow() {
            // Frigate MDrive speed is 10, distance 11 should fail
            // Target (11, -11, 0) — distance 11 from origin
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 11, -11, null);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    gameSessionService.moveFleet(100L, sessionId, request));

            assertTrue(ex.getMessage().contains("MDrive range"));
        }

        @Test
        @DisplayName("should throw when moving fleet not owned by player")
        void moveFleet_notOwned_shouldThrow() {
            // Create a second player and their fleet
            gameSessionService.joinSession(200L, "Second Player", sessionId);
            Player secondPlayer = game.getPlayer(200);
            Fleet secondFleet = game.createFleet("Second Fleet", secondPlayer);
            Ship frigate = new Ship(game.getNextShipIdAndIncrement(), ShipType.FRIGATE);
            frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
            game.addShip(frigate);
            secondFleet.addShip(frigate);
            secondFleet.setPosition(new Hex3D(0, 0, 0));

            // Player 100 tries to move player 200's fleet
            MoveFleetRequest request = new MoveFleetRequest(secondFleet.getId(), 1, -1, null);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    gameSessionService.moveFleet(100L, sessionId, request));

            assertTrue(ex.getMessage().contains("not owned"));
        }

        @Test
        @DisplayName("should allow any player to move their fleet (simultaneous turns)")
        void moveFleet_simultaneousTurns_anyPlayerCanMove() {
            // Add a second player
            gameSessionService.joinSession(200L, "Second Player", sessionId);

            // Refresh references
            GameSession session = gameSessionService.getGameSession(sessionId);
            Game refreshedGame = (Game) session.getGame();

            // Create fleet for player 200
            Fleet fleet2 = refreshedGame.createFleet("Fleet 2", refreshedGame.getPlayer(200));
            Ship frigate2 = new Ship(refreshedGame.getNextShipIdAndIncrement(), ShipType.FRIGATE);
            frigate2.addComponent(new ShipComponent(WeaponType.BEAM, 1));
            refreshedGame.addShip(frigate2);
            fleet2.addShip(frigate2);
            fleet2.setPosition(new Hex3D(0, 0, 0));

            // Player 200 can move their fleet at any time (simultaneous turns)
            MoveFleetRequest request = new MoveFleetRequest(fleet2.getId(), 1, -1, null);
            gameSessionService.moveFleet(200L, sessionId, request);

            Hex3D newPos = fleet2.getPosition();
            assertEquals(1, newPos.getX());
            assertEquals(-1, newPos.getY());
        }

        @Test
        @DisplayName("should throw when session not found")
        void moveFleet_sessionNotFound_shouldThrow() {
            MoveFleetRequest request = new MoveFleetRequest(1, 1, -1, null);

            assertThrows(NoSuchElementException.class, () ->
                    gameSessionService.moveFleet(100L, "non-existent", request));
        }

        @Test
        @DisplayName("should allow zero-distance move (same position)")
        void moveFleet_mDrive_samePosition_shouldSucceed() {
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, null);

            gameSessionService.moveFleet(100L, sessionId, request);

            Hex3D newPos = fleet.getPosition();
            assertEquals(0, newPos.getX());
            assertEquals(0, newPos.getY());
            assertEquals(0, newPos.getZ());
        }

        @Test
        @DisplayName("should include component MDrive bonus in range check")
        void moveFleet_mDrive_withComponentBonus_shouldExtendRange() {
            // Add MDrive component to extend range
            Ship frigate = (Ship) fleet.getShips().iterator().next();
            frigate.addComponent(ShipComponent.mDrive(2));
            // Frigate base MDrive 10 + bonus 4 = 14 range
            // Target (14, -14, 0) — distance 14 from origin

            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 14, -14, null);

            gameSessionService.moveFleet(100L, sessionId, request);

            Hex3D newPos = fleet.getPosition();
            assertEquals(14, newPos.getX());
        }

        @Test
        @DisplayName("should reduce MDrive range when fleet is damaged")
        void moveFleet_mDrive_damagedFleet_shouldReduceRange() {
            // Damage the ship to 50% hull integrity
            Ship frigate = (Ship) fleet.getShips().iterator().next();
            frigate.takeDamage(25); // 50 - 25 = 25 (50% health)
            // Effective MDrive: base 10 * 0.5 = 5 (rounded)
            // Target (6, -6, 0) — distance 6 from origin, exceeds reduced range

            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 6, -6, null);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    gameSessionService.moveFleet(100L, sessionId, request));

            assertTrue(ex.getMessage().contains("MDrive range"));
        }

        @Test
        @DisplayName("should move multi-ship fleet using slowest ship's MDrive")
        void moveFleet_mDrive_multiShip_shouldUseSlowestSpeed() {
            // Add a second ship (DESTROYER, MDrive 8) to the fleet
            Ship destroyer = new Ship(game.getNextShipIdAndIncrement(), ShipType.DESTROYER);
            destroyer.addComponent(new ShipComponent(WeaponType.BEAM, 1));
            game.addShip(destroyer);
            fleet.addShip(destroyer);
            // Fleet MDrive speed is the slowest ship: min(10, 8) = 8
            // Target (8, -8, 0) — distance 8 from origin

            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 8, -8, null);

            gameSessionService.moveFleet(100L, sessionId, request);

            Hex3D newPos = fleet.getPosition();
            assertEquals(8, newPos.getX());
        }

        @Test
        @DisplayName("should allow any player to move (simultaneous turns)")
        void moveFleet_mDrive_simultaneousTurns_anyPlayerCanMove() {
            // Add a second player
            gameSessionService.joinSession(200L, "Second Player", sessionId);

            // Refresh references after join
            GameSession session = gameSessionService.getGameSession(sessionId);
            Game refreshedGame = (Game) session.getGame();

            // Create fleet for player 200
            Fleet fleet2 = refreshedGame.createFleet("Fleet 2", refreshedGame.getPlayer(200));
            Ship frigate2 = new Ship(refreshedGame.getNextShipIdAndIncrement(), ShipType.FRIGATE);
            frigate2.addComponent(new ShipComponent(WeaponType.BEAM, 1));
            refreshedGame.addShip(frigate2);
            fleet2.addShip(frigate2);
            fleet2.setPosition(new Hex3D(0, 0, 0));

            // Both players can move simultaneously
            MoveFleetRequest requestForPlayer200 = new MoveFleetRequest(fleet2.getId(), 1, -1, null);
            gameSessionService.moveFleet(200L, sessionId, requestForPlayer200);

            Hex3D newPos = fleet2.getPosition();
            assertEquals(1, newPos.getX());
        }

        @Test
        @DisplayName("should return GameStateResponse after successful move")
        void moveFleet_mDrive_shouldReturnGameStateResponse() {
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 1, -1, null);

            var response = gameSessionService.moveFleet(100L, sessionId, request);

            assertNotNull(response);
            // Simultaneous turns: currentPlayerId is null
            assertNull(response.getCurrentPlayerId());
        }
    }

    // ==================== moveFleet - JDrive (between-system) ====================

    @Nested
    @DisplayName("moveFleet - JDrive (between-system)")
    class MoveFleetJDriveTests {

        private String sessionId;
        private Game game;
        private Player player;
        private Fleet fleet;

        @BeforeEach
        void setUpMoveFleet() {
            // Create session with one player
            sessionId = gameSessionService.createSession(100L, "Test Player", "Move Test").getSessionId();
            GameSession session = gameSessionService.getGameSession(sessionId);
            game = (Game) session.getGame();
            player = game.getPlayer(100);

            // Create a fleet with a frigate
            fleet = game.createFleet("Test Fleet", player);
            Ship frigate = new Ship(game.getNextShipIdAndIncrement(), ShipType.FRIGATE);
            frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
            game.addShip(frigate);
            fleet.addShip(frigate);
            fleet.setPosition(new Hex3D(0, 0, 0));

            // Set up solar systems and galaxy with hyperlane
            SolarSystem system1 = new SolarSystem(1);
            SolarSystem system2 = new SolarSystem(2);
            game.getSolarSystems().put(1, system1);
            game.getSolarSystems().put(2, system2);

            Galaxy galaxy = new Galaxy(1, "Test Galaxy");
            galaxy.addHyperlane(1, 2, 5);
            game.addGalaxy(galaxy);
        }

        @Test
        @DisplayName("should jump fleet to another system using JDrive")
        void moveFleet_jDrive_shouldSucceed() {
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 2);

            gameSessionService.moveFleet(100L, sessionId, request);

            Hex3D newPos = fleet.getPosition();
            assertEquals(0, newPos.getX());
            assertEquals(0, newPos.getY());
        }

        @Test
        @DisplayName("should throw when target system does not exist")
        void moveFleet_jDrive_nonExistentSystem_shouldThrow() {
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 999);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    gameSessionService.moveFleet(100L, sessionId, request));

            assertTrue(ex.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("should throw when JDrive is disabled due to low health")
        void moveFleet_jDrive_lowHealth_shouldThrow() {
            // Damage the ship to below 20% hull integrity
            Ship frigate = (Ship) fleet.getShips().iterator().next();
            frigate.takeDamage(45); // 50 - 45 = 5 (10% health, below 20% threshold)

            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 2);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    gameSessionService.moveFleet(100L, sessionId, request));

            assertTrue(ex.getMessage().contains("JDrive"));
        }

        @Test
        @DisplayName("should throw when no hyperlane connects to target system")
        void moveFleet_jDrive_noHyperlane_shouldThrow() {
            // Create a fresh session without a hyperlane
            String noHyperlaneSession = gameSessionService.createSession(300L, "No Hyperlane Player", "No Hyperlane Test").getSessionId();
            GameSession session = gameSessionService.getGameSession(noHyperlaneSession);
            Game noHyperlaneGame = (Game) session.getGame();
            Player noHyperlanePlayer = noHyperlaneGame.getPlayer(300);

            Fleet noHyperlaneFleet = noHyperlaneGame.createFleet("No Hyperlane Fleet", noHyperlanePlayer);
            Ship frigate = new Ship(noHyperlaneGame.getNextShipIdAndIncrement(), ShipType.FRIGATE);
            frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
            noHyperlaneGame.addShip(frigate);
            noHyperlaneFleet.addShip(frigate);
            noHyperlaneFleet.setPosition(new Hex3D(0, 0, 0));

            // Add solar systems but NO hyperlane
            SolarSystem system1 = new SolarSystem(1);
            SolarSystem system2 = new SolarSystem(2);
            noHyperlaneGame.getSolarSystems().put(1, system1);
            noHyperlaneGame.getSolarSystems().put(2, system2);

            Galaxy galaxy = new Galaxy(1, "No Hyperlane Galaxy");
            // Intentionally not adding any hyperlane
            noHyperlaneGame.addGalaxy(galaxy);

            MoveFleetRequest request = new MoveFleetRequest(noHyperlaneFleet.getId(), 0, 0, 2);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    gameSessionService.moveFleet(300L, noHyperlaneSession, request));

            assertTrue(ex.getMessage().contains("hyperlane"));
        }

        @Test
        @DisplayName("should allow JDrive when fleet health is exactly at 20%")
        void moveFleet_jDrive_atThreshold_shouldSucceed() {
            // Damage the ship to exactly 20% hull integrity
            Ship frigate = (Ship) fleet.getShips().iterator().next();
            frigate.takeDamage(40); // 50 - 40 = 10 (20% health, at threshold)

            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 2);

            // Should not throw
            gameSessionService.moveFleet(100L, sessionId, request);
        }

        @Test
        @DisplayName("should allow JDrive when fleet health is above 20%")
        void moveFleet_jDrive_aboveThreshold_shouldSucceed() {
            // Damage the ship to 30% hull integrity
            Ship frigate = (Ship) fleet.getShips().iterator().next();
            frigate.takeDamage(35); // 50 - 35 = 15 (30% health, above threshold)

            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 2);

            gameSessionService.moveFleet(100L, sessionId, request);
        }

        @Test
        @DisplayName("should throw when jumping to non-existent session")
        void moveFleet_jDrive_sessionNotFound_shouldThrow() {
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 2);

            assertThrows(NoSuchElementException.class, () ->
                    gameSessionService.moveFleet(100L, "non-existent", request));
        }

        @Test
        @DisplayName("should jump multi-ship fleet using JDrive")
        void moveFleet_jDrive_multiShip_shouldSucceed() {
            // Add a second ship to the fleet
            Ship destroyer = new Ship(game.getNextShipIdAndIncrement(), ShipType.DESTROYER);
            destroyer.addComponent(new ShipComponent(WeaponType.BEAM, 1));
            game.addShip(destroyer);
            fleet.addShip(destroyer);

            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 2);

            gameSessionService.moveFleet(100L, sessionId, request);
        }

        @Test
        @DisplayName("should throw when all ships in fleet are destroyed")
        void moveFleet_jDrive_allShipsDestroyed_shouldThrow() {
            // Destroy the ship
            Ship frigate = (Ship) fleet.getShips().iterator().next();
            frigate.takeDamage(50); // 50 - 50 = 0 (0% health, destroyed)

            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 2);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    gameSessionService.moveFleet(100L, sessionId, request));

            assertTrue(ex.getMessage().contains("JDrive"));
        }

        @Test
        @DisplayName("should allow JDrive when hyperlane connects from third system")
        void moveFleet_jDrive_hyperlaneFromThirdSystem_shouldSucceed() {
            // Add a third system and hyperlane from system 1 to system 3
            SolarSystem system3 = new SolarSystem(3);
            game.getSolarSystems().put(3, system3);
            Galaxy galaxy = game.getGalaxies().values().iterator().next();
            galaxy.addHyperlane(1, 3, 8);

            // Jump to system 3 (hyperlane connects system 1 -> 3)
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 3);

            gameSessionService.moveFleet(100L, sessionId, request);
        }

        @Test
        @DisplayName("should return GameStateResponse after successful JDrive jump")
        void moveFleet_jDrive_shouldReturnGameStateResponse() {
            MoveFleetRequest request = new MoveFleetRequest(fleet.getId(), 0, 0, 2);

            var response = gameSessionService.moveFleet(100L, sessionId, request);

            assertNotNull(response);
            // Simultaneous turns: currentPlayerId is null
            assertNull(response.getCurrentPlayerId());
        }
    }
}
