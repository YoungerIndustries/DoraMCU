package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.ArmyUnit;
import com.yisy.modules.doramcu.hex.ArmyUnitType;
import com.yisy.modules.doramcu.hex.Fleet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NavyTourAssignment Tests")
class NavyTourAssignmentTest {

    private TravellerCharacter character;
    private Fleet fleet;

    @BeforeEach
    void setUp() {
        character = new TravellerCharacter(1, "Alex Starborn");
        character.rollAbilities();
        fleet = new Fleet(1, "Test Fleet");
    }

    // ---- Construction ----

    @Test
    @DisplayName("Should create a new navy tour assignment")
    void testCreateTour() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Sol Empire", 6);

        assertEquals(1, tour.getId());
        assertSame(character, tour.getCharacter());
        assertSame(fleet, tour.getAssignedFleet());
        assertEquals(1, tour.getFleetId());
        assertEquals("Test Fleet", tour.getFleetName());
        assertEquals("Sol Empire", tour.getEmpireName());
        assertEquals(6, tour.getTotalTurns());
        assertEquals(0, tour.getTurnsServed());
        assertEquals(6, tour.getRemainingTurns());
        assertTrue(tour.isActive());
        assertFalse(tour.isCompleted());
        assertTrue(tour.getEvents().isEmpty());
        assertNotNull(tour.getStartDate());
        assertNull(tour.getEndDate());
    }

    @Test
    @DisplayName("Should reject null character")
    void testRejectNullCharacter() {
        assertThrows(IllegalArgumentException.class, () ->
                new NavyTourAssignment(1, null, fleet, "Empire", 6));
    }

    @Test
    @DisplayName("Should reject null fleet")
    void testRejectNullFleet() {
        assertThrows(IllegalArgumentException.class, () ->
                new NavyTourAssignment(1, character, null, "Empire", 6));
    }

    @Test
    @DisplayName("Should reject zero total turns")
    void testRejectZeroTurns() {
        assertThrows(IllegalArgumentException.class, () ->
                new NavyTourAssignment(1, character, fleet, "Empire", 0));
    }

    @Test
    @DisplayName("Should reject negative total turns")
    void testRejectNegativeTurns() {
        assertThrows(IllegalArgumentException.class, () ->
                new NavyTourAssignment(1, character, fleet, "Empire", -1));
    }

    @Test
    @DisplayName("Should handle null empire name")
    void testNullEmpireName() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, null, 6);
        assertEquals("Unknown Empire", tour.getEmpireName());
    }

    // ---- Tour progression ----

    @Test
    @DisplayName("Should advance turn correctly")
    void testAdvanceTurn() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Empire", 3);

        assertFalse(tour.advanceTurn());
        assertEquals(1, tour.getTurnsServed());
        assertEquals(2, tour.getRemainingTurns());

        assertFalse(tour.advanceTurn());
        assertEquals(2, tour.getTurnsServed());
        assertEquals(1, tour.getRemainingTurns());

        assertTrue(tour.advanceTurn());
        assertEquals(3, tour.getTurnsServed());
        assertEquals(0, tour.getRemainingTurns());
        assertTrue(tour.isCompleted());
        assertFalse(tour.isActive());
    }

    @Test
    @DisplayName("Should not advance inactive tour")
    void testAdvanceInactiveTour() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Empire", 1);
        tour.advanceTurn();

        assertThrows(IllegalStateException.class, tour::advanceTurn);
    }

    // ---- Event recording ----

    @Test
    @DisplayName("Should record events")
    void testRecordEvent() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Empire", 6);

        tour.recordEvent(NavyTourAssignment.TourEventType.BATTLE_ENGAGED, "Battle started");
        assertEquals(1, tour.getEvents().size());

        NavyTourAssignment.TourEvent event = tour.getEvents().get(0);
        assertEquals(0, event.getTurnNumber());
        assertEquals(NavyTourAssignment.TourEventType.BATTLE_ENGAGED, event.getEventType());
        assertEquals("Battle started", event.getDescription());
    }

    @Test
    @DisplayName("Should record events with correct turn numbers")
    void testRecordEventTurnNumbers() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Empire", 6);

        tour.recordEvent(NavyTourAssignment.TourEventType.BATTLE_ENGAGED, "Event 1");
        tour.advanceTurn();
        tour.recordEvent(NavyTourAssignment.TourEventType.BATTLE_VICTORY, "Event 2");

        assertEquals(0, tour.getEvents().get(0).getTurnNumber());
        assertEquals(1, tour.getEvents().get(1).getTurnNumber());
    }

    @Test
    @DisplayName("Should not record events on inactive tour")
    void testRecordEventInactive() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Empire", 1);
        tour.complete();

        assertThrows(IllegalStateException.class, () ->
                tour.recordEvent(NavyTourAssignment.TourEventType.BATTLE_ENGAGED, "Test"));
    }

    // ---- Completion and termination ----

    @Test
    @DisplayName("Should complete tour early")
    void testCompleteEarly() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Empire", 6);
        tour.complete();

        assertFalse(tour.isActive());
        assertNotNull(tour.getEndDate());
        assertEquals(0, tour.getTurnsServed());
    }

    @Test
    @DisplayName("Should terminate tour with reason")
    void testTerminate() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Empire", 6);
        tour.terminate("Fleet destroyed");

        assertFalse(tour.isActive());
        assertNotNull(tour.getEndDate());
        assertEquals(1, tour.getEvents().size());
        assertEquals(NavyTourAssignment.TourEventType.TOUR_TERMINATED, tour.getEvents().get(0).getEventType());
    }

    // ---- Events list immutability ----

    @Test
    @DisplayName("Events list should be unmodifiable")
    void testEventsListImmutability() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Empire", 6);
        assertThrows(UnsupportedOperationException.class, () ->
                tour.getEvents().add(null));
    }

    // ---- Tour event types ----

    @Test
    @DisplayName("TourEventType should have all expected values")
    void testTourEventTypeValues() {
        assertNotNull(NavyTourAssignment.TourEventType.BATTLE_ENGAGED);
        assertNotNull(NavyTourAssignment.TourEventType.BATTLE_VICTORY);
        assertNotNull(NavyTourAssignment.TourEventType.BATTLE_DEFEAT);
        assertNotNull(NavyTourAssignment.TourEventType.RETREAT);
        assertNotNull(NavyTourAssignment.TourEventType.SHIPS_LOST);
        assertNotNull(NavyTourAssignment.TourEventType.REINFORCEMENT);
        assertNotNull(NavyTourAssignment.TourEventType.DEPLOYMENT);
        assertNotNull(NavyTourAssignment.TourEventType.DAMAGE_TAKEN);
        assertNotNull(NavyTourAssignment.TourEventType.REPAIR);
        assertNotNull(NavyTourAssignment.TourEventType.FLEET_DESTROYED);
        assertNotNull(NavyTourAssignment.TourEventType.DISCOVERY);
        assertNotNull(NavyTourAssignment.TourEventType.TOUR_TERMINATED);
        assertNotNull(NavyTourAssignment.TourEventType.OTHER);
    }

    // ---- TourEvent toString ----

    @Test
    @DisplayName("TourEvent toString should include all fields")
    void testTourEventToString() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Empire", 6);
        tour.recordEvent(NavyTourAssignment.TourEventType.BATTLE_VICTORY, "Won at Sol");

        String str = tour.getEvents().get(0).toString();
        assertTrue(str.contains("turn=0"));
        assertTrue(str.contains("BATTLE_VICTORY"));
        assertTrue(str.contains("Won at Sol"));
    }

    // ---- toString ----

    @Test
    @DisplayName("toString should include all key fields")
    void testToString() {
        NavyTourAssignment tour = new NavyTourAssignment(1, character, fleet, "Sol Empire", 6);
        String str = tour.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("Alex Starborn"));
        assertTrue(str.contains("Test Fleet"));
        assertTrue(str.contains("0/6"));
        assertTrue(str.contains("active=true"));
    }
}
