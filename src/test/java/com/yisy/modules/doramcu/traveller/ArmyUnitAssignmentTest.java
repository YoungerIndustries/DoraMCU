package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.ArmyUnit;
import com.yisy.modules.doramcu.hex.ArmyUnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArmyUnitAssignment Tests")
class ArmyUnitAssignmentTest {

    private TravellerCharacter character;
    private ArmyUnit unit;

    @BeforeEach
    void setUp() {
        character = new TravellerCharacter(1, "Alex Starborn");
        character.rollAbilities();
        unit = new ArmyUnit(1, ArmyUnitType.INFANTRY);
    }

    // ---- Construction ----

    @Test
    @DisplayName("Should create a new army unit assignment")
    void testCreateAssignment() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Sol Empire", "Coruscant", 6);

        assertEquals(1, assignment.getId());
        assertSame(character, assignment.getCharacter());
        assertSame(unit, assignment.getAssignedUnit());
        assertEquals(1, assignment.getUnitId());
        assertEquals("Infantry-1", assignment.getUnitName());
        assertEquals("Sol Empire", assignment.getEmpireName());
        assertEquals("Coruscant", assignment.getPlanetName());
        assertEquals(6, assignment.getTotalTurns());
        assertEquals(0, assignment.getTurnsServed());
        assertEquals(6, assignment.getRemainingTurns());
        assertTrue(assignment.isActive());
        assertFalse(assignment.isCompleted());
        assertTrue(assignment.getEvents().isEmpty());
        assertNotNull(assignment.getStartDate());
        assertNull(assignment.getEndDate());
    }

    @Test
    @DisplayName("Should reject null character")
    void testRejectNullCharacter() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArmyUnitAssignment(1, null, unit, "Empire", "Planet", 6));
    }

    @Test
    @DisplayName("Should reject null unit")
    void testRejectNullUnit() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArmyUnitAssignment(1, character, null, "Empire", "Planet", 6));
    }

    @Test
    @DisplayName("Should reject zero total turns")
    void testRejectZeroTurns() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArmyUnitAssignment(1, character, unit, "Empire", "Planet", 0));
    }

    @Test
    @DisplayName("Should handle null empire name")
    void testNullEmpireName() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, null, "Planet", 6);
        assertEquals("Unknown Empire", assignment.getEmpireName());
    }

    @Test
    @DisplayName("Should handle null planet name")
    void testNullPlanetName() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Empire", null, 6);
        assertEquals("Unknown Planet", assignment.getPlanetName());
    }

    // ---- Assignment progression ----

    @Test
    @DisplayName("Should advance turn correctly")
    void testAdvanceTurn() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Empire", "Planet", 3);

        assertFalse(assignment.advanceTurn());
        assertEquals(1, assignment.getTurnsServed());

        assertFalse(assignment.advanceTurn());
        assertEquals(2, assignment.getTurnsServed());

        assertTrue(assignment.advanceTurn());
        assertEquals(3, assignment.getTurnsServed());
        assertTrue(assignment.isCompleted());
        assertFalse(assignment.isActive());
    }

    @Test
    @DisplayName("Should not advance inactive assignment")
    void testAdvanceInactive() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Empire", "Planet", 1);
        assignment.advanceTurn();

        assertThrows(IllegalStateException.class, assignment::advanceTurn);
    }

    // ---- Event recording ----

    @Test
    @DisplayName("Should record events")
    void testRecordEvent() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Empire", "Planet", 6);

        assignment.recordEvent(ArmyUnitAssignment.UnitEventType.COMBAT_ENGAGED, "Combat started");
        assertEquals(1, assignment.getEvents().size());

        ArmyUnitAssignment.UnitEvent event = assignment.getEvents().get(0);
        assertEquals(0, event.getTurnNumber());
        assertEquals(ArmyUnitAssignment.UnitEventType.COMBAT_ENGAGED, event.getEventType());
        assertEquals("Combat started", event.getDescription());
    }

    @Test
    @DisplayName("Should not record events on inactive assignment")
    void testRecordEventInactive() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Empire", "Planet", 1);
        assignment.complete();

        assertThrows(IllegalStateException.class, () ->
                assignment.recordEvent(ArmyUnitAssignment.UnitEventType.COMBAT_ENGAGED, "Test"));
    }

    // ---- Completion and termination ----

    @Test
    @DisplayName("Should complete assignment early")
    void testCompleteEarly() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Empire", "Planet", 6);
        assignment.complete();

        assertFalse(assignment.isActive());
        assertNotNull(assignment.getEndDate());
    }

    @Test
    @DisplayName("Should terminate assignment with reason")
    void testTerminate() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Empire", "Planet", 6);
        assignment.terminate("Unit destroyed");

        assertFalse(assignment.isActive());
        assertEquals(1, assignment.getEvents().size());
        assertEquals(ArmyUnitAssignment.UnitEventType.ASSIGNMENT_TERMINATED,
                assignment.getEvents().get(0).getEventType());
    }

    // ---- Events list immutability ----

    @Test
    @DisplayName("Events list should be unmodifiable")
    void testEventsListImmutability() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Empire", "Planet", 6);
        assertThrows(UnsupportedOperationException.class, () ->
                assignment.getEvents().add(null));
    }

    // ---- Unit event types ----

    @Test
    @DisplayName("UnitEventType should have all expected values")
    void testUnitEventTypeValues() {
        assertNotNull(ArmyUnitAssignment.UnitEventType.COMBAT_ENGAGED);
        assertNotNull(ArmyUnitAssignment.UnitEventType.COMBAT_VICTORY);
        assertNotNull(ArmyUnitAssignment.UnitEventType.COMBAT_DEFEAT);
        assertNotNull(ArmyUnitAssignment.UnitEventType.RETREAT);
        assertNotNull(ArmyUnitAssignment.UnitEventType.CASUALTIES);
        assertNotNull(ArmyUnitAssignment.UnitEventType.REINFORCEMENT);
        assertNotNull(ArmyUnitAssignment.UnitEventType.DEPLOYMENT);
        assertNotNull(ArmyUnitAssignment.UnitEventType.DAMAGE_TAKEN);
        assertNotNull(ArmyUnitAssignment.UnitEventType.RECOVERY);
        assertNotNull(ArmyUnitAssignment.UnitEventType.UNIT_DESTROYED);
        assertNotNull(ArmyUnitAssignment.UnitEventType.EXPERIENCE_GAINED);
        assertNotNull(ArmyUnitAssignment.UnitEventType.MORALE_CHANGE);
        assertNotNull(ArmyUnitAssignment.UnitEventType.DISCOVERY);
        assertNotNull(ArmyUnitAssignment.UnitEventType.ASSIGNMENT_TERMINATED);
        assertNotNull(ArmyUnitAssignment.UnitEventType.OTHER);
    }

    // ---- toString ----

    @Test
    @DisplayName("toString should include all key fields")
    void testToString() {
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(1, character, unit, "Sol Empire", "Coruscant", 6);
        String str = assignment.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("Alex Starborn"));
        assertTrue(str.contains("0/6"));
        assertTrue(str.contains("active=true"));
    }
}
