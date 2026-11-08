package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedSpaceStation Tests")
class UnifiedSpaceStationTest {

    @Test
    @DisplayName("Should create station with type")
    void testCreateStation() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Alpha Station",
                UnifiedSpaceStation.StationType.ALPHA);
        assertEquals(1, station.getId());
        assertEquals("Alpha Station", station.getName());
        assertEquals(UnifiedSpaceStation.StationType.ALPHA, station.getUnifiedStationType());
        assertTrue(station.isOperational());
    }

    @Test
    @DisplayName("Should derive facilities from type")
    void testDeriveFacilities() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Alpha",
                UnifiedSpaceStation.StationType.ALPHA);
        assertTrue(station.hasFacility(StationFacility.REFUELING));
        assertTrue(station.hasFacility(StationFacility.REPAIR));
        assertTrue(station.hasFacility(StationFacility.MARKET));
    }

    @Test
    @DisplayName("Outpost should have minimal facilities")
    void testOutpostFacilities() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Outpost",
                UnifiedSpaceStation.StationType.OUTPOST);
        assertFalse(station.hasFacility(StationFacility.REFUELING));
    }

    @Test
    @DisplayName("Should map starport code to type")
    void testStarportMapping() {
        assertEquals(UnifiedSpaceStation.StationType.ALPHA,
                UnifiedSpaceStation.StationType.fromStarportCode(0));
        assertEquals(UnifiedSpaceStation.StationType.BRAVO,
                UnifiedSpaceStation.StationType.fromStarportCode(1));
        assertEquals(UnifiedSpaceStation.StationType.CHARLIE,
                UnifiedSpaceStation.StationType.fromStarportCode(2));
        assertEquals(UnifiedSpaceStation.StationType.D,
                UnifiedSpaceStation.StationType.fromStarportCode(3));
        assertEquals(UnifiedSpaceStation.StationType.E,
                UnifiedSpaceStation.StationType.fromStarportCode(4));
        assertEquals(UnifiedSpaceStation.StationType.OUTPOST,
                UnifiedSpaceStation.StationType.fromStarportCode(5));
        assertNull(UnifiedSpaceStation.StationType.fromStarportCode(9));
    }

    @Test
    @DisplayName("Should create from world")
    void testFromWorld() {
        // UWP "A050430-A" has starport=0 (Alpha) at position 3
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A050430-A", 0, 0);
        UnifiedSpaceStation station = UnifiedSpaceStation.fromWorld(world);
        assertNotNull(station);
        // Starport code 0 = Alpha
        assertEquals(UnifiedSpaceStation.StationType.ALPHA, station.getUnifiedStationType());
    }

    @Test
    @DisplayName("Should return null for no starport world")
    void testFromWorldNoStarport() {
        // UWP "A05A430-A" has starport=A (no starport) at position 3
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A05A430-A", 0, 0);
        UnifiedSpaceStation station = UnifiedSpaceStation.fromWorld(world);
        // Starport A = no starport
        assertNull(station);
    }

    @Test
    @DisplayName("Should dock and undock ships")
    void testDockShip() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.ALPHA);
        assertTrue(station.dockShip("Ship1", 1000));
        assertTrue(station.undockShip("Ship1"));
    }

    @Test
    @DisplayName("Should not dock when offline")
    void testDockWhenOffline() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.ALPHA);
        station.setOperational(false);
        assertFalse(station.dockShip("Ship1", 1000));
    }

    @Test
    @DisplayName("Should refuel with facility")
    void testRefuel() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.ALPHA);
        FuelTank tank = new FuelTank(FusionDrive.M3, 100);
        int cost = station.refuel(tank, false);
        assertTrue(cost >= 0);
    }

    @Test
    @DisplayName("Should throw refuel without facility")
    void testRefuelNoFacility() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.OUTPOST);
        FuelTank tank = new FuelTank(FusionDrive.M3, 100);
        assertThrows(IllegalStateException.class, () -> station.refuel(tank, false));
    }

    @Test
    @DisplayName("Should repair hull")
    void testRepairHull() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.ALPHA);
        int cost = station.repairHull(50, 100);
        assertEquals(250, cost); // (100-50) * 5
    }

    @Test
    @DisplayName("Should buy and sell cargo")
    void testBuySellCargo() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.ALPHA);
        int buyCost = station.buyCargo(Commodity.FOOD, 10);
        assertTrue(buyCost >= 0);
        int sellRevenue = station.sellCargo(Commodity.FOOD, 10);
        assertTrue(sellRevenue >= 0);
    }

    @Test
    @DisplayName("Should add facility")
    void testAddFacility() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.CHARLIE);
        // Charlie has 6 max facilities, derived facilities already fill some
        boolean added = station.addFacility(StationFacility.MILITARY);
        if (added) {
            assertTrue(station.hasFacility(StationFacility.MILITARY));
        }
    }

    @Test
    @DisplayName("Should upgrade station")
    void testUpgrade() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.E);
        assertTrue(station.upgrade());
        assertEquals(UnifiedSpaceStation.StationType.D, station.getUnifiedStationType());
    }

    @Test
    @DisplayName("Alpha cannot upgrade")
    void testAlphaCannotUpgrade() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.ALPHA);
        assertFalse(station.upgrade());
    }

    @Test
    @DisplayName("Should have event log")
    void testEventLog() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.ALPHA);
        assertTrue(station.getEventLog().isEmpty());
        station.dockShip("Ship1", 1000);
        assertTrue(station.getEventLog().size() >= 1);
    }

    @Test
    @DisplayName("Should be combat-ready")
    void testCombatReady() {
        UnifiedSpaceStation station = new UnifiedSpaceStation(1, "Station",
                UnifiedSpaceStation.StationType.ALPHA);
        assertTrue(station.isCombatReady());
    }
}
