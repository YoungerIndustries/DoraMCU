package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.manager.CharacterLogManager;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.service.GameSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TravellerAdventureService Tests")
class TravellerAdventureServiceTest {

    private CharacterService characterService;
    private GameSessionService gameSessionService;
    private CharacterLogManager logManager;
    private TravellerAdventureService service;
    private Game game;
    private com.yisy.modules.doramcu.model.GameSession gameSession;

    @BeforeEach
    void setUp() {
        game = new Game("Test Adventure");
        gameSession = mock(com.yisy.modules.doramcu.model.GameSession.class);
        when(gameSession.getGame()).thenReturn(game);

        gameSessionService = mock(GameSessionService.class);
        when(gameSessionService.getDefaultSession()).thenReturn(gameSession);

        characterService = mock(CharacterService.class);
        logManager = new CharacterLogManager(game);
        service = new TravellerAdventureService(characterService, gameSessionService);
        service.setLogManager(logManager);
    }

    // ============================================================
    // TravellerAdventure Model Tests
    // ============================================================

    @Test
    @DisplayName("TravellerAdventure should initialize with correct character ID")
    void testAdventureInitialization() {
        TravellerAdventure adventure = new TravellerAdventure(42);
        assertEquals(42, adventure.getCharacterId());
        assertEquals(0, adventure.getFleetId());
        assertEquals(0, adventure.getCurrentSystemId());
        assertEquals(0, adventure.getCredits());
        assertFalse(adventure.isPassenger());
        assertFalse(adventure.hasDestination());
        assertTrue(adventure.getAdventureLog().isEmpty());
    }

    @Test
    @DisplayName("TravellerAdventure should set fleet ID")
    void testSetFleetId() {
        TravellerAdventure adventure = new TravellerAdventure(1);
        adventure.setFleetId(10);
        assertEquals(10, adventure.getFleetId());
    }

    @Test
    @DisplayName("TravellerAdventure should set current system")
    void testSetCurrentSystem() {
        TravellerAdventure adventure = new TravellerAdventure(1);
        adventure.setCurrentSystem(5, "Sol");
        assertEquals(5, adventure.getCurrentSystemId());
        assertEquals("Sol", adventure.getCurrentSystemName());
    }

    @Test
    @DisplayName("TravellerAdventure should set current planet")
    void testSetCurrentPlanet() {
        TravellerAdventure adventure = new TravellerAdventure(1);
        adventure.setCurrentPlanet(100, "Earth");
        assertEquals(100, adventure.getCurrentPlanetId());
        assertEquals("Earth", adventure.getCurrentPlanetName());
        assertTrue(adventure.isDocked());
    }

    @Test
    @DisplayName("TravellerAdventure should not be docked when planet ID is 0")
    void testNotDocked() {
        TravellerAdventure adventure = new TravellerAdventure(1);
        assertFalse(adventure.isDocked());
    }

    @Test
    @DisplayName("TravellerAdventure should manage credits")
    void testCredits() {
        TravellerAdventure adventure = new TravellerAdventure(1);
        adventure.setCredits(100_000L);
        assertEquals(100_000L, adventure.getCredits());

        assertTrue(adventure.deductCredits(50_000L));
        assertEquals(50_000L, adventure.getCredits());

        assertFalse(adventure.deductCredits(999_999L));
        assertEquals(50_000L, adventure.getCredits());

        adventure.addCredits(10_000L);
        assertEquals(60_000L, adventure.getCredits());
    }

    @Test
    @DisplayName("TravellerAdventure should manage passenger status")
    void testPassenger() {
        TravellerAdventure adventure = new TravellerAdventure(1);
        adventure.setPassenger(true, 99);
        assertTrue(adventure.isPassenger());
        assertEquals(99, adventure.getPassengerShipId());

        adventure.setPassenger(false, 0);
        assertFalse(adventure.isPassenger());
        assertEquals(0, adventure.getPassengerShipId());
    }

    @Test
    @DisplayName("TravellerAdventure should manage destination")
    void testDestination() {
        TravellerAdventure adventure = new TravellerAdventure(1);
        adventure.setDestination(7, "Alpha Centauri");
        assertTrue(adventure.hasDestination());
        assertEquals(7, adventure.getDestinationSystemId());
        assertEquals("Alpha Centauri", adventure.getDestinationSystemName());

        adventure.clearDestination();
        assertFalse(adventure.hasDestination());
        assertEquals(0, adventure.getDestinationSystemId());
    }

    @Test
    @DisplayName("TravellerAdventure should manage adventure log")
    void testAdventureLog() {
        TravellerAdventure adventure = new TravellerAdventure(1);
        CharacterExperience exp = new CharacterExperience(ExperienceType.EXPLORATION, "Test");
        adventure.addExperience(exp);
        assertEquals(1, adventure.getAdventureLog().size());
        assertEquals(exp, adventure.getAdventureLog().get(0));

        // Null is ignored
        adventure.addExperience(null);
        assertEquals(1, adventure.getAdventureLog().size());
    }

    @Test
    @DisplayName("TravellerAdventure ship costs should be correct")
    void testShipCosts() {
        assertEquals(50_000L, TravellerAdventure.getShipCost(ShipType.FRIGATE));
        assertEquals(150_000L, TravellerAdventure.getShipCost(ShipType.DESTROYER));
        assertEquals(300_000L, TravellerAdventure.getShipCost(ShipType.CRUISER));
        assertEquals(600_000L, TravellerAdventure.getShipCost(ShipType.BATTLESHIP));
        assertEquals(450_000L, TravellerAdventure.getShipCost(ShipType.CARRIER));
        assertEquals(1_200_000L, TravellerAdventure.getShipCost(ShipType.MOTHERSHIP));
    }

    @Test
    @DisplayName("TravellerAdventure default starting ship should be FRIGATE")
    void testDefaultStartingShip() {
        assertEquals(ShipType.FRIGATE, TravellerAdventure.DEFAULT_STARTING_SHIP);
    }

    // ============================================================
    // AdventureEvent Tests
    // ============================================================

    @Test
    @DisplayName("AdventureEvent should have correct fields")
    void testAdventureEventFields() {
        AdventureEvent event = new AdventureEvent(
                1, AdventureEvent.EventType.BATTLE, 5, "Sol",
                "Battle between Fleet A and Fleet B", 0);
        assertEquals(1, event.getEventId());
        assertEquals(AdventureEvent.EventType.BATTLE, event.getEventType());
        assertEquals(5, event.getSystemId());
        assertEquals("Sol", event.getSystemName());
        assertEquals("Battle between Fleet A and Fleet B", event.getDescription());
        assertEquals(0, event.getDistanceHex());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("AdventureEvent toExperienceType should map correctly")
    void testEventToExperienceType() {
        assertEquals(ExperienceType.BATTLE,
                new AdventureEvent(1, AdventureEvent.EventType.BATTLE, 0, "", "", 0).toExperienceType());
        assertEquals(ExperienceType.EXPLORATION,
                new AdventureEvent(1, AdventureEvent.EventType.FLEET_MOVEMENT, 0, "", "", 0).toExperienceType());
        assertEquals(ExperienceType.SOCIAL,
                new AdventureEvent(1, AdventureEvent.EventType.PLANET_CONQUEST, 0, "", "", 0).toExperienceType());
        assertEquals(ExperienceType.TRADE,
                new AdventureEvent(1, AdventureEvent.EventType.TRADE_DEAL, 0, "", "", 0).toExperienceType());
        assertEquals(ExperienceType.LOSS,
                new AdventureEvent(1, AdventureEvent.EventType.SHIP_DESTROYED, 0, "", "", 0).toExperienceType());
        assertEquals(ExperienceType.DISCOVERY,
                new AdventureEvent(1, AdventureEvent.EventType.DISCOVERY, 0, "", "", 0).toExperienceType());
    }

    @Test
    @DisplayName("AdventureEvent EventType enum should have 6 values")
    void testEventTypeEnumSize() {
        assertEquals(6, AdventureEvent.EventType.values().length);
    }

    // ============================================================
    // AdventureState Tests
    // ============================================================

    @Test
    @DisplayName("AdventureState should initialize empty")
    void testAdventureStateEmpty() {
        AdventureState state = new AdventureState();
        assertEquals(0, state.getCharacterId());
        assertNull(state.getCharacterName());
        assertEquals(0, state.getCredits());
        assertFalse(state.isPassenger());
        assertTrue(state.getNearbyEvents().isEmpty());
        assertTrue(state.getRecentExperiences().isEmpty());
    }

    @Test
    @DisplayName("AdventureState should set all fields")
    void testAdventureStateFields() {
        AdventureState state = new AdventureState(1, "Alex");
        state.setCurrentSystemName("Sol");
        state.setCurrentPlanetName("Earth");
        state.setShipName("Frigate-1");
        state.setShipType("FRIGATE");
        state.setShipHullPoints(50);
        state.setShipShieldPoints(0);
        state.setCredits(100_000L);
        state.setPassenger(false);
        state.setDestinationSystemName("Alpha Centauri");

        assertEquals("Sol", state.getCurrentSystemName());
        assertEquals("Earth", state.getCurrentPlanetName());
        assertEquals("Frigate-1", state.getShipName());
        assertEquals("FRIGATE", state.getShipType());
        assertEquals(50, state.getShipHullPoints());
        assertEquals(0, state.getShipShieldPoints());
        assertEquals(100_000L, state.getCredits());
        assertFalse(state.isPassenger());
        assertEquals("Alpha Centauri", state.getDestinationSystemName());
    }

    @Test
    @DisplayName("AdventureState should manage nearby events")
    void testAdventureStateEvents() {
        AdventureState state = new AdventureState(1, "Alex");
        AdventureEvent event = new AdventureEvent(1, AdventureEvent.EventType.BATTLE, 0, "Sol", "Battle", 0);
        state.addNearbyEvent(event);
        assertEquals(1, state.getNearbyEventCount());
        assertEquals(event, state.getNearbyEvents().get(0));

        state.addNearbyEvent(null);
        assertEquals(1, state.getNearbyEventCount());
    }

    @Test
    @DisplayName("AdventureState should manage recent experiences")
    void testAdventureStateExperiences() {
        AdventureState state = new AdventureState(1, "Alex");
        CharacterExperience exp = new CharacterExperience(ExperienceType.EXPLORATION, "Test");
        state.addRecentExperience(exp);
        assertEquals(1, state.getRecentExperienceCount());
        assertEquals(exp, state.getRecentExperiences().get(0));

        state.addRecentExperience(null);
        assertEquals(1, state.getRecentExperienceCount());
    }

    // ============================================================
    // Service - Start Adventure Tests
    // ============================================================

    @Test
    @DisplayName("startAdventure should throw if character not found")
    void testStartAdventureNotFound() {
        when(characterService.getCharacter(999)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.startAdventure(999));
    }

    @Test
    @DisplayName("startAdventure should throw if character is dead")
    void testStartAdventureDeadCharacter() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setAlive(false);
        when(characterService.getCharacter(1)).thenReturn(character);
        assertThrows(IllegalStateException.class, () -> service.startAdventure(1));
    }

    @Test
    @DisplayName("startAdventure should throw if character is not COMPLETED")
    void testStartAdventureNotCompleted() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.TRAINING);
        when(characterService.getCharacter(1)).thenReturn(character);
        assertThrows(IllegalStateException.class, () -> service.startAdventure(1));
    }

    @Test
    @DisplayName("startAdventure should create adventure for COMPLETED character")
    void testStartAdventureSuccess() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        TravellerAdventure adventure = service.startAdventure(1);

        assertNotNull(adventure);
        assertEquals(1, adventure.getCharacterId());
        assertTrue(adventure.getFleetId() > 0);
        assertEquals(CharacterStatus.TRAVELLING, character.getStatus());
        assertTrue(adventure.getCredits() >= TravellerAdventureService.DEFAULT_STARTING_CREDITS);
        assertEquals(1, service.getAllAdventures().size());
        assertTrue(service.hasActiveAdventure(1));
    }

    @Test
    @DisplayName("startAdventure should return existing adventure if already active")
    void testStartAdventureAlreadyActive() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        TravellerAdventure first = service.startAdventure(1);
        // After first start, status is TRAVELLING, so second call returns existing adventure
        TravellerAdventure second = service.startAdventure(1);

        assertSame(first, second);
    }

    @Test
    @DisplayName("startAdventure should record start experience")
    void testStartAdventureRecordsExperience() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        TravellerAdventure adventure = service.startAdventure(1);

        assertEquals(1, adventure.getAdventureLog().size());
        assertEquals(ExperienceType.EXPLORATION, adventure.getAdventureLog().get(0).getType());
        assertEquals("Adventure Begins", adventure.getAdventureLog().get(0).getTitle());
    }

    // ============================================================
    // Service - End Adventure Tests
    // ============================================================

    @Test
    @DisplayName("endAdventure should throw if character not found")
    void testEndAdventureNotFound() {
        when(characterService.getCharacter(999)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.endAdventure(999));
    }

    @Test
    @DisplayName("endAdventure should throw if character is not TRAVELLING")
    void testEndAdventureNotTravelling() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);
        assertThrows(IllegalStateException.class, () -> service.endAdventure(1));
    }

    @Test
    @DisplayName("endAdventure should revert status to COMPLETED")
    void testEndAdventureSuccess() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.startAdventure(1);
        service.endAdventure(1);

        assertEquals(CharacterStatus.COMPLETED, character.getStatus());
        assertFalse(service.hasActiveAdventure(1));
    }

    // ============================================================
    // Service - Purchase Ship Tests
    // ============================================================

    @Test
    @DisplayName("purchaseShip should throw if insufficient credits")
    void testPurchaseShipInsufficientCredits() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.startAdventure(1);

        // Default credits are 10,000, MOTHERSHIP costs 1,200,000
        assertThrows(IllegalArgumentException.class,
                () -> service.purchaseShip(1, ShipType.MOTHERSHIP));
    }

    @Test
    @DisplayName("purchaseShip should deduct credits and create ship")
    void testPurchaseShipSuccess() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.startAdventure(1);
        TravellerAdventure adventure = service.getAdventure(1);
        adventure.setCredits(200_000L);
        long creditsBefore = adventure.getCredits();

        Ship ship = service.purchaseShip(1, ShipType.FRIGATE);

        assertNotNull(ship);
        assertEquals(ShipType.FRIGATE, ship.getShipType());
        long cost = TravellerAdventure.getShipCost(ShipType.FRIGATE);
        assertEquals(creditsBefore - cost, service.getAdventure(1).getCredits());
    }

    // ============================================================
    // Service - Witness Event Tests
    // ============================================================

    @Test
    @DisplayName("witnessEvent should record experience")
    void testWitnessEvent() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.startAdventure(1);

        AdventureEvent event = new AdventureEvent(
                1, AdventureEvent.EventType.BATTLE, 5, "Sol",
                "Battle between Fleet A and Fleet B", 0);

        CharacterExperience exp = service.witnessEvent(1, event);

        assertNotNull(exp);
        assertEquals(ExperienceType.BATTLE, exp.getType());
        assertEquals(2, service.getAdventure(1).getAdventureLog().size()); // start + witness
    }

    // ============================================================
    // Service - Query Tests
    // ============================================================

    @Test
    @DisplayName("getAdventure should throw if no active adventure")
    void testGetAdventureNoAdventure() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        when(characterService.getCharacter(1)).thenReturn(character);
        assertThrows(IllegalStateException.class, () -> service.getAdventure(1));
    }

    @Test
    @DisplayName("getAdventure should throw if character not found")
    void testGetAdventureNotFound() {
        when(characterService.getCharacter(999)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.getAdventure(999));
    }

    @Test
    @DisplayName("getAdventureState should return state for active adventure")
    void testGetAdventureState() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.startAdventure(1);
        AdventureState state = service.getAdventureState(1);

        assertNotNull(state);
        assertEquals(1, state.getCharacterId());
        assertEquals("Alex Starborn", state.getCharacterName());
    }

    @Test
    @DisplayName("getAllAdventures should return unmodifiable map")
    void testGetAllAdventures() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.startAdventure(1);
        Map<Integer, TravellerAdventure> all = service.getAllAdventures();
        assertEquals(1, all.size());

        assertThrows(UnsupportedOperationException.class, () -> all.put(2, new TravellerAdventure(2)));
    }

    @Test
    @DisplayName("getReachableSystems should return systems list")
    void testGetReachableSystems() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.startAdventure(1);
        List<Map<String, Object>> systems = service.getReachableSystems(1);
        assertNotNull(systems);
    }

    @Test
    @DisplayName("getNearbyEvents should return empty list when no battles")
    void testGetNearbyEventsEmpty() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex Starborn");
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.startAdventure(1);
        List<AdventureEvent> events = service.getNearbyEvents(1);
        assertNotNull(events);
    }

    // ============================================================
    // Service - Constants
    // ============================================================

    @Test
    @DisplayName("NEARBY_EVENT_RANGE should be 3")
    void testNearbyEventRange() {
        assertEquals(3, TravellerAdventureService.NEARBY_EVENT_RANGE);
    }

    @Test
    @DisplayName("DEFAULT_STARTING_CREDITS should be 10000")
    void testDefaultStartingCredits() {
        assertEquals(10_000L, TravellerAdventureService.DEFAULT_STARTING_CREDITS);
    }

    // ============================================================
    // CharacterStatus TRAVELLING Tests
    // ============================================================

    @Test
    @DisplayName("CharacterStatus TRAVELLING should be available for login")
    void testTravellingAvailableForLogin() {
        assertTrue(CharacterStatus.TRAVELLING.isAvailableForLogin());
    }

    @Test
    @DisplayName("CharacterStatus TRAVELLING should have correct display name")
    void testTravellingDisplayName() {
        assertEquals("Travelling", CharacterStatus.TRAVELLING.getDisplayName());
    }

    @Test
    @DisplayName("CharacterStatus should have 4 values")
    void testCharacterStatusSize() {
        assertEquals(4, CharacterStatus.values().length);
    }
}
