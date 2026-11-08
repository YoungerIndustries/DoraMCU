package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedFleet Tests")
class UnifiedFleetTest {

    @Test
    @DisplayName("Should create fleet with default type")
    void testCreateDefaultFleet() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        assertEquals(1, fleet.getId());
        assertEquals("Test Fleet", fleet.getName());
        assertEquals(NavalFleet.FleetType.SUB_SECTORAL, fleet.getFleetType());
        assertEquals(FleetFormation.WEDGE, fleet.getFormation());
        assertEquals(FleetAction.HOLD, fleet.getCurrentAction());
    }

    @Test
    @DisplayName("Should create fleet with specific type")
    void testCreateFleetWithType() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Sector Fleet", NavalFleet.FleetType.SECTORAL);
        assertEquals(NavalFleet.FleetType.SECTORAL, fleet.getFleetType());
    }

    @Test
    @DisplayName("Should add and remove squadrons")
    void testSquadronManagement() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        UnifiedSquadron squadron = new UnifiedSquadron(0, "Alpha Squadron");

        assertTrue(fleet.addSquadron(squadron));
        assertEquals(1, fleet.getSquadronCount());
        assertTrue(fleet.getSquadrons().contains(squadron));

        assertTrue(fleet.removeSquadron(squadron));
        assertEquals(0, fleet.getSquadronCount());
    }

    @Test
    @DisplayName("Should create squadron via helper")
    void testCreateSquadron() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        UnifiedSquadron squadron = fleet.createSquadron("Bravo");
        assertEquals(1, fleet.getSquadronCount());
        assertEquals("Bravo", squadron.getName());
    }

    @Test
    @DisplayName("Should set commander")
    void testSetCommander() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        NavalOfficer officer = new NavalOfficer(0, "Admiral Smith", NavalRank.CAPTAIN);
        fleet.setCommander(officer);
        assertEquals(officer, fleet.getCommander());
    }

    @Test
    @DisplayName("Should change formation")
    void testChangeFormation() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        fleet.setFormation(FleetFormation.WEDGE);
        assertEquals(FleetFormation.WEDGE, fleet.getFormation());
    }

    @Test
    @DisplayName("Should set current action")
    void testSetAction() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        fleet.setCurrentAction(FleetAction.LASER_BARRAGE);
        assertEquals(FleetAction.LASER_BARRAGE, fleet.getCurrentAction());
    }

    @Test
    @DisplayName("Should move to system")
    void testMoveToSystem() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A055430-A", 0, 0);
        fleet.moveToSystem(world);
        assertEquals(world, fleet.getCurrentSystem());
    }

    @Test
    @DisplayName("Should not be combat-ready without commander")
    void testCombatReadyWithoutCommander() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        fleet.addShip(ship);
        assertFalse(fleet.isCombatReady());
    }

    @Test
    @DisplayName("Should be combat-ready with commander and ships")
    void testCombatReadyWithCommander() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        fleet.addShip(ship);
        NavalOfficer officer = new NavalOfficer(0, "Captain", NavalRank.CAPTAIN);
        fleet.setCommander(officer);
        assertTrue(fleet.isCombatReady());
    }

    @Test
    @DisplayName("Should return strength status")
    void testStrengthStatus() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        assertEquals("Empty", fleet.getStrengthStatus());

        for (int i = 0; i < 2; i++) {
            fleet.addShip(UnifiedShipFactory.createFrigate(i + 1));
        }
        assertEquals("Weak", fleet.getStrengthStatus());
    }

    @Test
    @DisplayName("Should have fleet log")
    void testFleetLog() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        assertTrue(fleet.getFleetLog().size() >= 1);
        assertTrue(fleet.getFleetLog().get(0).contains("Fleet created"));
    }

    @Test
    @DisplayName("Should calculate total tonnage")
    void testTotalTonnage() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        UnifiedSquadron squadron = fleet.createSquadron("Alpha");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        squadron.addShip(ship);
        assertTrue(fleet.getTotalTonnage() >= 1000);
    }

    @Test
    @DisplayName("Should calculate combat power with commander bonus")
    void testCombatPowerWithCommander() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        fleet.addShip(ship);
        NavalOfficer officer = new NavalOfficer(0, "Captain", NavalRank.CAPTAIN);
        fleet.setCommander(officer);
        assertTrue(fleet.getTotalCombatPower() > 0);
    }

    @Test
    @DisplayName("Should return action modifiers")
    void testActionModifiers() {
        UnifiedFleet fleet = new UnifiedFleet(1, "Test Fleet");
        fleet.setCurrentAction(FleetAction.LASER_BARRAGE);
        assertEquals(FleetAction.LASER_BARRAGE.getCombatModifier(), fleet.getActionCombatModifier());
        assertEquals(FleetAction.LASER_BARRAGE.getDefenseModifier(), fleet.getActionDefenseModifier());
    }
}
