package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.CreateSessionResponse;
import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GalaxyCreationService Tests")
class GalaxyCreationServiceTest {

    private GalaxyService galaxyService;
    private GalaxyProgressService galaxyProgressService;
    private GameSessionService gameSessionService;
    private AiPlayerService aiPlayerService;
    private PlayerRepository playerRepository;
    private GalaxyCreationService galaxyCreationService;

    @BeforeEach
    void setUp() {
        galaxyService = Mockito.mock(GalaxyService.class);
        galaxyProgressService = Mockito.mock(GalaxyProgressService.class);
        gameSessionService = Mockito.mock(GameSessionService.class);
        aiPlayerService = Mockito.mock(AiPlayerService.class);
        playerRepository = Mockito.mock(PlayerRepository.class);

        galaxyCreationService = new GalaxyCreationService(
                galaxyService,
                galaxyProgressService,
                gameSessionService,
                aiPlayerService,
                playerRepository
        );
    }

    // ---- createGalaxyWithAiPlayers ----

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should return result with session ID and name")
    void createGalaxyWithAiPlayers_shouldReturnResultWithSessionIdAndName() {
        // Arrange
        setupMockPlayers(List.of(
                new Player(1L, "p1@test.com", "111111", "Empire Alpha",
                        Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST),
                new Player(2L, "p2@test.com", "222222", "Empire Beta",
                        Player.GovernmentType.AUTHORITARIAN, Player.Ethos.MILITARIST)
        ));
        setupMockGalaxyService(false);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        GalaxyCreationService.GalaxyCreationResult result =
                galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert
        assertNotNull(result);
        assertEquals("test-session", result.getSessionId());
        assertEquals("Test Galaxy", result.getSessionName());
    }

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should return 3 AI players")
    void createGalaxyWithAiPlayers_shouldReturnThreeAiPlayers() {
        // Arrange
        setupMockPlayers(List.of(
                new Player(1L, "p1@test.com", "111111", "Empire Alpha",
                        Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST)
        ));
        setupMockGalaxyService(false);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        GalaxyCreationService.GalaxyCreationResult result =
                galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getAiPlayers().size());
    }

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should create AI players with correct difficulties")
    void createGalaxyWithAiPlayers_shouldCreateAiPlayersWithCorrectDifficulties() {
        // Arrange
        setupMockPlayers(List.of(
                new Player(1L, "p1@test.com", "111111", "Empire Alpha",
                        Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST)
        ));
        setupMockGalaxyService(false);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        GalaxyCreationService.GalaxyCreationResult result =
                galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert
        List<GalaxyCreationService.AiPlayerInfo> aiPlayers = result.getAiPlayers();
        assertEquals(3, aiPlayers.size());

        GalaxyCreationService.AiPlayerInfo first = aiPlayers.get(0);
        assertEquals("AI Vanguard", first.getPlayerName());
        assertEquals("EASY", first.getDifficulty());

        GalaxyCreationService.AiPlayerInfo second = aiPlayers.get(1);
        assertEquals("AI Strategist", second.getPlayerName());
        assertEquals("MEDIUM", second.getDifficulty());

        GalaxyCreationService.AiPlayerInfo third = aiPlayers.get(2);
        assertEquals("AI Overlord", third.getPlayerName());
        assertEquals("HARD", third.getDifficulty());
    }

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should call createDefaultSession with session name")
    void createGalaxyWithAiPlayers_shouldCallCreateDefaultSession() {
        // Arrange
        setupMockPlayers(List.of(
                new Player(1L, "p1@test.com", "111111", "Empire Alpha",
                        Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST)
        ));
        setupMockGalaxyService(false);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert
        verify(gameSessionService).createDefaultSession("Test Galaxy");
    }

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should set progress listener on galaxy service")
    void createGalaxyWithAiPlayers_shouldSetProgressListener() {
        // Arrange
        setupMockPlayers(List.of(
                new Player(1L, "p1@test.com", "111111", "Empire Alpha",
                        Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST)
        ));
        setupMockGalaxyService(false);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert
        verify(galaxyService).setProgressListener(galaxyProgressService);
    }

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should generate galaxy with empires from players")
    void createGalaxyWithAiPlayers_shouldGenerateGalaxyWithEmpires() {
        // Arrange
        Player player1 = new Player(1L, "p1@test.com", "111111", "Empire Alpha",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        Player player2 = new Player(2L, "p2@test.com", "222222", "Empire Beta",
                Player.GovernmentType.AUTHORITARIAN, Player.Ethos.MILITARIST);
        setupMockPlayers(List.of(player1, player2));
        setupMockGalaxyService(false);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert
        verify(galaxyService).generateAndSaveGalaxyWithEmpires(
                eq(com.yisy.modules.doramcu.hex.GalaxyFactory.GAME_GALAXY_NAME),
                argThat(empires -> empires.size() == 2),
                anyInt(), anyInt(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyLong()
        );
    }

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should skip galaxy generation if galaxy already exists")
    void createGalaxyWithAiPlayers_shouldSkipGalaxyGenerationIfExists() {
        // Arrange
        setupMockPlayers(List.of(
                new Player(1L, "p1@test.com", "111111", "Empire Alpha",
                        Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST)
        ));
        // Galaxy already exists
        when(galaxyService.hasGalaxies()).thenReturn(true);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert
        verify(galaxyService, never()).generateAndSaveGalaxyWithEmpires(any(), any(), anyInt(), anyInt(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyLong());
        verify(galaxyService, never()).setProgressListener(any());
    }

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should create empires with AI ruler ID (-1)")
    void createGalaxyWithAiPlayers_shouldCreateEmpiresWithAiRulerId() {
        // Arrange
        Player player = new Player(1L, "p1@test.com", "111111", "Empire Alpha",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        setupMockPlayers(List.of(player));
        setupMockGalaxyService(false);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert - verify the empires map passed to generateAndSaveGalaxyWithEmpires
        verify(galaxyService).generateAndSaveGalaxyWithEmpires(
                eq(com.yisy.modules.doramcu.hex.GalaxyFactory.GAME_GALAXY_NAME),
                argThat(empires -> {
                    Empire empire = empires.get(1);
                    return empire != null && empire.getRulerPlayerId() == -1;
                }),
                anyInt(), anyInt(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyLong()
        );
    }

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should handle empty player list")
    void createGalaxyWithAiPlayers_shouldHandleEmptyPlayerList() {
        // Arrange
        setupMockPlayers(List.of());
        setupMockGalaxyService(false);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        GalaxyCreationService.GalaxyCreationResult result =
                galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert
        assertNotNull(result);
        assertEquals("test-session", result.getSessionId());
        assertEquals(3, result.getAiPlayers().size());
    }

    @Test
    @DisplayName("createGalaxyWithAiPlayers - should call AI player service for all 3 difficulties")
    void createGalaxyWithAiPlayers_shouldCallAiPlayerServiceForAllDifficulties() {
        // Arrange
        setupMockPlayers(List.of(
                new Player(1L, "p1@test.com", "111111", "Empire Alpha",
                        Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST)
        ));
        setupMockGalaxyService(false);
        setupMockSessionService("test-session", "Test Galaxy");
        setupMockAiPlayers();

        // Act
        galaxyCreationService.createGalaxyWithAiPlayers("Test Galaxy");

        // Assert
        verify(aiPlayerService).createAiPlayer("test-session", "AI Vanguard",
                AiPlayerService.Difficulty.EASY);
        verify(aiPlayerService).createAiPlayer("test-session", "AI Strategist",
                AiPlayerService.Difficulty.MEDIUM);
        verify(aiPlayerService).createAiPlayer("test-session", "AI Overlord",
                AiPlayerService.Difficulty.HARD);
    }

    // ---- joinPlayerWithEmpire ----

    @Test
    @DisplayName("joinPlayerWithEmpire - should return join response")
    void joinPlayerWithEmpire_shouldReturnJoinResponse() {
        // Arrange
        Long playerId = 1L;
        String playerName = "Human Player";
        String sessionId = "test-session";

        JoinSessionResponse mockResponse = new JoinSessionResponse(
                sessionId, "Test Galaxy", playerId, playerName);
        when(gameSessionService.joinSession(playerId, playerName, sessionId))
                .thenReturn(mockResponse);

        // Act
        JoinSessionResponse response =
                galaxyCreationService.joinPlayerWithEmpire(playerId, playerName, sessionId);

        // Assert
        assertNotNull(response);
        assertEquals(sessionId, response.getSessionId());
        assertEquals(playerId, response.getPlayerId());
        assertEquals(playerName, response.getPlayerName());
    }

    @Test
    @DisplayName("joinPlayerWithEmpire - should delegate to gameSessionService.joinSession")
    void joinPlayerWithEmpire_shouldDelegateToGameSessionService() {
        // Arrange
        Long playerId = 2L;
        String playerName = "Another Player";
        String sessionId = "another-session";

        JoinSessionResponse mockResponse = new JoinSessionResponse(
                sessionId, "Test Galaxy", playerId, playerName);
        when(gameSessionService.joinSession(playerId, playerName, sessionId))
                .thenReturn(mockResponse);

        // Act
        galaxyCreationService.joinPlayerWithEmpire(playerId, playerName, sessionId);

        // Assert
        verify(gameSessionService).joinSession(playerId, playerName, sessionId);
    }

    @Test
    @DisplayName("joinPlayerWithEmpire - should preserve session name in response")
    void joinPlayerWithEmpire_shouldPreserveSessionName() {
        // Arrange
        Long playerId = 5L;
        String playerName = "New Player";
        String sessionId = "test-session";
        String sessionName = "Galactic Conquest";

        JoinSessionResponse mockResponse = new JoinSessionResponse(
                sessionId, sessionName, playerId, playerName);
        when(gameSessionService.joinSession(playerId, playerName, sessionId))
                .thenReturn(mockResponse);

        // Act
        JoinSessionResponse response =
                galaxyCreationService.joinPlayerWithEmpire(playerId, playerName, sessionId);

        // Assert
        assertEquals(sessionName, response.getSessionName());
    }

    // ---- Nested classes ----

    @Nested
    @DisplayName("GalaxyCreationResult Tests")
    class GalaxyCreationResultTests {

        @Test
        @DisplayName("should store and retrieve session ID")
        void shouldStoreAndRetrieveSessionId() {
            GalaxyCreationService.GalaxyCreationResult result =
                    new GalaxyCreationService.GalaxyCreationResult("session-123", "Test", List.of());

            assertEquals("session-123", result.getSessionId());
        }

        @Test
        @DisplayName("should store and retrieve session name")
        void shouldStoreAndRetrieveSessionName() {
            GalaxyCreationService.GalaxyCreationResult result =
                    new GalaxyCreationService.GalaxyCreationResult("session-123", "My Galaxy", List.of());

            assertEquals("My Galaxy", result.getSessionName());
        }

        @Test
        @DisplayName("should store and retrieve AI players list")
        void shouldStoreAndRetrieveAiPlayers() {
            GalaxyCreationService.AiPlayerInfo ai1 =
                    new GalaxyCreationService.AiPlayerInfo(1001L, "AI One", "EASY");
            GalaxyCreationService.AiPlayerInfo ai2 =
                    new GalaxyCreationService.AiPlayerInfo(1002L, "AI Two", "HARD");
            List<GalaxyCreationService.AiPlayerInfo> aiPlayers = List.of(ai1, ai2);

            GalaxyCreationService.GalaxyCreationResult result =
                    new GalaxyCreationService.GalaxyCreationResult("session-123", "Test", aiPlayers);

            assertEquals(2, result.getAiPlayers().size());
            assertEquals("AI One", result.getAiPlayers().get(0).getPlayerName());
            assertEquals("AI Two", result.getAiPlayers().get(1).getPlayerName());
        }

        @Test
        @DisplayName("should handle empty AI players list")
        void shouldHandleEmptyAiPlayersList() {
            GalaxyCreationService.GalaxyCreationResult result =
                    new GalaxyCreationService.GalaxyCreationResult("session-123", "Test", List.of());

            assertTrue(result.getAiPlayers().isEmpty());
        }
    }

    @Nested
    @DisplayName("AiPlayerInfo Tests")
    class AiPlayerInfoTests {

        @Test
        @DisplayName("should store and retrieve player ID")
        void shouldStoreAndRetrievePlayerId() {
            GalaxyCreationService.AiPlayerInfo info =
                    new GalaxyCreationService.AiPlayerInfo(1001L, "AI Test", "MEDIUM");

            assertEquals(1001L, info.getPlayerId());
        }

        @Test
        @DisplayName("should store and retrieve player name")
        void shouldStoreAndRetrievePlayerName() {
            GalaxyCreationService.AiPlayerInfo info =
                    new GalaxyCreationService.AiPlayerInfo(1001L, "AI Commander", "EASY");

            assertEquals("AI Commander", info.getPlayerName());
        }

        @Test
        @DisplayName("should store and retrieve difficulty")
        void shouldStoreAndRetrieveDifficulty() {
            GalaxyCreationService.AiPlayerInfo info =
                    new GalaxyCreationService.AiPlayerInfo(1001L, "AI Test", "HARD");

            assertEquals("HARD", info.getDifficulty());
        }

        @Test
        @DisplayName("should support all difficulty levels")
        void shouldSupportAllDifficultyLevels() {
            GalaxyCreationService.AiPlayerInfo easy =
                    new GalaxyCreationService.AiPlayerInfo(1001L, "Easy AI", "EASY");
            GalaxyCreationService.AiPlayerInfo medium =
                    new GalaxyCreationService.AiPlayerInfo(1002L, "Medium AI", "MEDIUM");
            GalaxyCreationService.AiPlayerInfo hard =
                    new GalaxyCreationService.AiPlayerInfo(1003L, "Hard AI", "HARD");

            assertEquals("EASY", easy.getDifficulty());
            assertEquals("MEDIUM", medium.getDifficulty());
            assertEquals("HARD", hard.getDifficulty());
        }
    }

    // ---- Helper methods ----

    private void setupMockPlayers(List<Player> players) {
        when(playerRepository.findAll()).thenReturn(players);
    }

    private void setupMockGalaxyService(boolean hasGalaxies) {
        when(galaxyService.hasGalaxies()).thenReturn(hasGalaxies);
    }

    private void setupMockSessionService(String sessionId, String sessionName) {
        CreateSessionResponse response = new CreateSessionResponse(sessionId, sessionName, null, null);
        when(gameSessionService.createDefaultSession(anyString())).thenReturn(response);
    }

    private void setupMockAiPlayers() {
        JoinSessionResponse easyResponse = new JoinSessionResponse(
                "test-session", "Test Galaxy", 1001L, "AI Vanguard");
        JoinSessionResponse mediumResponse = new JoinSessionResponse(
                "test-session", "Test Galaxy", 1002L, "AI Strategist");
        JoinSessionResponse hardResponse = new JoinSessionResponse(
                "test-session", "Test Galaxy", 1003L, "AI Overlord");

        when(aiPlayerService.createAiPlayer(eq("test-session"), eq("AI Vanguard"),
                eq(AiPlayerService.Difficulty.EASY))).thenReturn(easyResponse);
        when(aiPlayerService.createAiPlayer(eq("test-session"), eq("AI Strategist"),
                eq(AiPlayerService.Difficulty.MEDIUM))).thenReturn(mediumResponse);
        when(aiPlayerService.createAiPlayer(eq("test-session"), eq("AI Overlord"),
                eq(AiPlayerService.Difficulty.HARD))).thenReturn(hardResponse);
    }
}
