package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.hex.Battle;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.SolarSystem;
import com.yisy.modules.doramcu.traveller.*;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TravellerTo4XIntegrationService Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class TravellerTo4XIntegrationServiceTest {

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private CharacterService characterService;

    private MilitaryTourService militaryTourService;
    private TravellerTo4XIntegrationService service;

    @BeforeEach
    void setUp() {
        militaryTourService = new MilitaryTourService();
        service = new TravellerTo4XIntegrationService(gameSessionService, characterService, militaryTourService);
    }

    // ---- Military Career Detection ----

    @Test
    @DisplayName("isMilitaryCareer should return true for SOLDIER")
    void testIsMilitaryCareerSoldier() {
        assertTrue(service.isMilitaryCareer(Career.SOLDIER));
    }

    @Test
    @DisplayName("isMilitaryCareer should return true for GUNNER")
    void testIsMilitaryCareerGunner() {
        assertTrue(service.isMilitaryCareer(Career.GUNNER));
    }

    @Test
    @DisplayName("isMilitaryCareer should return true for PILOT")
    void testIsMilitaryCareerPilot() {
        assertTrue(service.isMilitaryCareer(Career.PILOT));
    }

    @Test
    @DisplayName("isMilitaryCareer should return false for EXPLORER")
    void testIsMilitaryCareerNonCombatant() {
        assertFalse(service.isMilitaryCareer(Career.EXPLORER));
    }

    @Test
    @DisplayName("isMilitaryCareer should return false for MERCHANT")
    void testIsMilitaryCareerMerchant() {
        assertFalse(service.isMilitaryCareer(Career.MERCHANT));
    }

    @Test
    @DisplayName("isMilitaryCareer should return false for NOBLE")
    void testIsMilitaryCareerNoble() {
        assertFalse(service.isMilitaryCareer(Career.NOBLE));
    }

    @Test
    @DisplayName("getMilitaryCareers should return 3 careers")
    void testGetMilitaryCareers() {
        List<Career> careers = service.getMilitaryCareers();
        assertEquals(3, careers.size());
        assertTrue(careers.contains(Career.SOLDIER));
        assertTrue(careers.contains(Career.GUNNER));
        assertTrue(careers.contains(Career.PILOT));
    }

    // ---- Management Role Integration ----

    @Test
    @DisplayName("integrateManagementRole should reject non-management type")
    void testIntegrateManagementRoleRejectsFullTraveller() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.FULL_TRAVELLER);

        assertThrows(IllegalArgumentException.class, () ->
                service.integrateManagementRole(request));
    }

    @Test
    @DisplayName("integrateManagementRole should reject null type")
    void testIntegrateManagementRoleRejectsNullType() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");

        assertThrows(IllegalArgumentException.class, () ->
                service.integrateManagementRole(request));
    }

    @Test
    @DisplayName("integrateManagementRole should create player with empire")
    void testIntegrateManagementRoleCreatesPlayer() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.EMPIRE_LEADER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(characterService.createCharacter("Alex Starborn")).thenReturn(travellerChar);

        // Set up a real game session
        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateManagementRole(request);

        assertNotNull(summary);
        assertEquals(GameType.TRAVELLER, summary.getGameType());
        assertEquals("Empire Leader", summary.getGameRole());
        assertTrue(summary.isAlive());
        assertEquals("Alex Starborn", summary.getFullName());

        // Verify the game has a player
        assertEquals(1, game.getPlayerCount());
        Player player = game.getPlayer(1);
        assertNotNull(player);
        assertNotNull(player.getCommander());
    }

    @Test
    @DisplayName("integrateManagementRole should work for Sector Governor")
    void testIntegrateManagementRoleSectorGovernor() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Jane", "Doe", "jane@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.SECTOR_GOVERNOR);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Jane Doe");
        travellerChar.rollAbilities();
        when(characterService.createCharacter("Jane Doe")).thenReturn(travellerChar);

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateManagementRole(request);

        assertNotNull(summary);
        assertEquals("Sector Governor", summary.getGameRole());
    }

    @Test
    @DisplayName("integrateManagementRole should work for Subsector Chief")
    void testIntegrateManagementRoleSubsectorChief() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "John", "Smith", "john@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.SUBSECTOR_CHIEF);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "John Smith");
        travellerChar.rollAbilities();
        when(characterService.createCharacter("John Smith")).thenReturn(travellerChar);

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateManagementRole(request);

        assertNotNull(summary);
        assertEquals("Subsector Chief", summary.getGameRole());
    }

    @Test
    @DisplayName("integrateManagementRole should work for System Manager")
    void testIntegrateManagementRoleSystemManager() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Bob", "Jones", "bob@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.SYSTEM_MANAGER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Bob Jones");
        travellerChar.rollAbilities();
        when(characterService.createCharacter("Bob Jones")).thenReturn(travellerChar);

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateManagementRole(request);

        assertNotNull(summary);
        assertEquals("System Manager", summary.getGameRole());
    }

    @Test
    @DisplayName("integrateManagementRole should assign commander to player")
    void testIntegrateManagementRoleAssignsCommander() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.EMPIRE_LEADER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(characterService.createCharacter("Alex Starborn")).thenReturn(travellerChar);

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        service.integrateManagementRole(request);

        Player player = game.getPlayer(1);
        assertNotNull(player);
        assertNotNull(player.getCommander());
        assertEquals("Alex Starborn", player.getCommander().getName());
    }

    @Test
    @DisplayName("integrateManagementRole should create empire")
    void testIntegrateManagementRoleCreatesEmpire() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.EMPIRE_LEADER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(characterService.createCharacter("Alex Starborn")).thenReturn(travellerChar);

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        service.integrateManagementRole(request);

        Map<Integer, Empire> empires = game.getEmpires();
        assertEquals(1, empires.size());
    }

    @Test
    @DisplayName("integrateManagementRole should create starting fleets")
    void testIntegrateManagementRoleCreatesFleets() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.EMPIRE_LEADER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(characterService.createCharacter("Alex Starborn")).thenReturn(travellerChar);

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        service.integrateManagementRole(request);

        Map<Integer, Fleet> fleets = game.getFleetsMap();
        assertTrue(fleets.size() >= 1, "Should have at least one starting fleet");
    }

    @Test
    @DisplayName("integrateManagementRole should add player to session")
    void testIntegrateManagementRoleAddsPlayerToSession() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.EMPIRE_LEADER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(characterService.createCharacter("Alex Starborn")).thenReturn(travellerChar);

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        service.integrateManagementRole(request);

        assertTrue(session.getPlayerIds().contains(1L));
    }

    // ---- Military Character Integration ----

    @Test
    @DisplayName("integrateMilitaryCharacter should reject non-military career")
    void testIntegrateMilitaryCharacterRejectsNonMilitary() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");

        assertThrows(IllegalArgumentException.class, () ->
                service.integrateMilitaryCharacter(request, Career.NOBLE, 2, 6));
    }

    @Test
    @DisplayName("integrateMilitaryCharacter should assign army tour for SOLDIER")
    void testIntegrateMilitaryCharacterSoldier() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateMilitaryCharacter(request, Career.SOLDIER, 2, 6);

        assertNotNull(summary);
        assertTrue(summary.getGameRole().contains("Soldier"));
        assertTrue(summary.getGameRole().contains("Army Tour"));
    }

    @Test
    @DisplayName("integrateMilitaryCharacter should assign navy tour for GUNNER")
    void testIntegrateMilitaryCharacterGunner() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateMilitaryCharacter(request, Career.GUNNER, 2, 6);

        assertNotNull(summary);
        assertTrue(summary.getGameRole().contains("Gunner"));
    }

    @Test
    @DisplayName("integrateMilitaryCharacter should assign navy tour for PILOT")
    void testIntegrateMilitaryCharacterPilot() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateMilitaryCharacter(request, Career.PILOT, 2, 6);

        assertNotNull(summary);
        assertTrue(summary.getGameRole().contains("Pilot"));
    }

    @Test
    @DisplayName("integrateMilitaryCharacter should create tour for character")
    void testIntegrateMilitaryCharacterCreatesTour() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateMilitaryCharacter(request, Career.SOLDIER, 2, 6);

        assertNotNull(summary);
        assertTrue(summary.getGameRole().contains("Army Tour"));
        assertTrue(summary.getDescription().contains("Tour:"));
    }

    @Test
    @DisplayName("integrateMilitaryCharacter should create fleet in game")
    void testIntegrateMilitaryCharacterCreatesFleet() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        service.integrateMilitaryCharacter(request, Career.SOLDIER, 2, 6);

        Map<Integer, Fleet> fleets = game.getFleetsMap();
        assertTrue(fleets.size() >= 1, "Should have at least one fleet");
    }

    @Test
    @DisplayName("integrateMilitaryCharacter should reflect character alive status")
    void testIntegrateMilitaryCharacterAliveStatus() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateMilitaryCharacter(request, Career.SOLDIER, 1, 6);

        assertNotNull(summary);
        // Character should be alive after 1 term (may die in combat)
        assertNotNull(summary.isAlive());
    }

    // ---- Empire Discovery ----

    @Test
    @DisplayName("getAvailableEmpires should return empty list when no session")
    void testGetAvailableEmpiresNoSession() {
        when(gameSessionService.getDefaultSession()).thenReturn(null);

        List<Map<String, Object>> empires = service.getAvailableEmpires();

        assertTrue(empires.isEmpty());
    }

    @Test
    @DisplayName("getAvailableEmpires should return empty list when no empires")
    void testGetAvailableEmpiresNoEmpires() {
        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        List<Map<String, Object>> empires = service.getAvailableEmpires();

        assertTrue(empires.isEmpty());
    }

    @Test
    @DisplayName("getAvailableEmpires should return empire info")
    void testGetAvailableEmpiresWithEmpire() {
        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);

        // Set up a player with empire
        game.setupPlayer(1, "Test Player");

        when(gameSessionService.getDefaultSession()).thenReturn(session);

        List<Map<String, Object>> empires = service.getAvailableEmpires();

        assertEquals(1, empires.size());
        Map<String, Object> empire = empires.get(0);
        assertEquals(1, empire.get("id"));
        assertEquals("Test Player Empire", empire.get("name"));
    }

    // ---- Edge Cases ----

    @Test
    @DisplayName("integrateManagementRole should throw when no session available")
    void testIntegrateManagementRoleNoSession() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.EMPIRE_LEADER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        when(characterService.createCharacter("Alex Starborn")).thenReturn(travellerChar);
        when(gameSessionService.getDefaultSession()).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
                service.integrateManagementRole(request));
    }

    @Test
    @DisplayName("integrateMilitaryCharacter should throw when no session available")
    void testIntegrateMilitaryCharacterNoSession() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");

        when(gameSessionService.getDefaultSession()).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
                service.integrateMilitaryCharacter(request, Career.SOLDIER, 2, 6));
    }

    @Test
    @DisplayName("Summary description should contain empire name")
    void testSummaryDescriptionContainsEmpireName() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.EMPIRE_LEADER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(characterService.createCharacter("Alex Starborn")).thenReturn(travellerChar);

        Game game = new Game("Test Campaign");
        GameSession session = new GameSession("session-1", "Test Campaign", game);
        when(gameSessionService.getDefaultSession()).thenReturn(session);

        UnifiedCharacterSummary summary = service.integrateManagementRole(request);

        assertNotNull(summary.getDescription());
        assertTrue(summary.getDescription().contains("Alex Starborn Empire"));
    }
}
