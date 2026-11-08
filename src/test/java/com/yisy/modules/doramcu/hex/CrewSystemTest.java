package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CrewSystem Tests")
class CrewSystemTest {

    @Test
    @DisplayName("Should create empty crew system")
    void testCreateEmpty() {
        CrewSystem crew = new CrewSystem();
        assertEquals(0, crew.getTotalCrewSize());
        assertEquals(0, crew.getActiveCrewCount());
    }

    @Test
    @DisplayName("Should assign to ship")
    void testAssignToShip() {
        CrewSystem crew = new CrewSystem();
        TravellerCharacter character = new TravellerCharacter(0, "Test");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        crew.assignToShip(character, CrewSystem.CrewAssignment.AssignmentType.CAPTAIN, ship);
        assertEquals(1, crew.getTotalCrewSize());
        assertNotNull(crew.getAssignment(character));
    }

    @Test
    @DisplayName("Should assign to station")
    void testAssignToStation() {
        CrewSystem crew = new CrewSystem();
        TravellerCharacter character = new TravellerCharacter(0, "Test");
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.ALPHA);
        crew.assignToStation(character, CrewSystem.CrewAssignment.AssignmentType.CREW, station);
        assertEquals(1, crew.getTotalCrewSize());
    }

    @Test
    @DisplayName("Should remove assignment")
    void testRemoveAssignment() {
        CrewSystem crew = new CrewSystem();
        TravellerCharacter character = new TravellerCharacter(0, "Test");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        crew.assignToShip(character, CrewSystem.CrewAssignment.AssignmentType.CAPTAIN, ship);
        assertNotNull(crew.removeAssignment(character));
        assertEquals(0, crew.getTotalCrewSize());
    }

    @Test
    @DisplayName("Should get crew for ship")
    void testCrewForShip() {
        CrewSystem crew = new CrewSystem();
        TravellerCharacter c1 = new TravellerCharacter(0, "C1");
        TravellerCharacter c2 = new TravellerCharacter(0, "C2");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        crew.assignToShip(c1, CrewSystem.CrewAssignment.AssignmentType.CAPTAIN, ship);
        crew.assignToShip(c2, CrewSystem.CrewAssignment.AssignmentType.PILOT, ship);
        assertEquals(2, crew.getCrewForShip(ship).size());
    }

    @Test
    @DisplayName("Should get crew by type")
    void testCrewByType() {
        CrewSystem crew = new CrewSystem();
        TravellerCharacter c1 = new TravellerCharacter(0, "C1");
        TravellerCharacter c2 = new TravellerCharacter(0, "C2");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        crew.assignToShip(c1, CrewSystem.CrewAssignment.AssignmentType.CAPTAIN, ship);
        crew.assignToShip(c2, CrewSystem.CrewAssignment.AssignmentType.PILOT, ship);
        assertEquals(1, crew.getCrewByType(CrewSystem.CrewAssignment.AssignmentType.CAPTAIN).size());
        assertEquals(1, crew.getCrewByType(CrewSystem.CrewAssignment.AssignmentType.PILOT).size());
    }

    @Test
    @DisplayName("Should get pilot bonus")
    void testPilotBonus() {
        CrewSystem crew = new CrewSystem();
        TravellerCharacter character = new TravellerCharacter(0, "Pilot");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        crew.assignToShip(character, CrewSystem.CrewAssignment.AssignmentType.PILOT, ship);
        assertEquals(1, crew.getPilotBonus(ship));
    }

    @Test
    @DisplayName("Should get gunnery bonus")
    void testGunneryBonus() {
        CrewSystem crew = new CrewSystem();
        TravellerCharacter character = new TravellerCharacter(0, "Gunner");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        crew.assignToShip(character, CrewSystem.CrewAssignment.AssignmentType.GUNNER, ship);
        assertEquals(1, crew.getGunneryBonus(ship));
    }

    @Test
    @DisplayName("Should record casualty")
    void testRecordCasualty() {
        CrewSystem crew = new CrewSystem();
        TravellerCharacter character = new TravellerCharacter(0, "Victim");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        crew.assignToShip(character, CrewSystem.CrewAssignment.AssignmentType.CREW, ship);
        crew.recordCasualty(character);
        assertEquals(1, crew.getCasualties().size());
        assertEquals(0, crew.getActiveCrewCount());
    }

    @Test
    @DisplayName("Should have event log")
    void testEventLog() {
        CrewSystem crew = new CrewSystem();
        TravellerCharacter character = new TravellerCharacter(0, "Test");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        crew.assignToShip(character, CrewSystem.CrewAssignment.AssignmentType.CAPTAIN, ship);
        assertTrue(crew.getEventLog().size() >= 1);
    }
}
