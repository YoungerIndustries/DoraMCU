package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipType Enum Tests")
class ShipTypeTest {

    @Test
    @DisplayName("values - should have 6 ship types")
    void values_shouldHaveSixTypes() {
        ShipType[] types = ShipType.values();

        assertEquals(6, types.length);
    }

    @Test
    @DisplayName("FRIGATE - should have correct base stats")
    void frigate_shouldHaveCorrectStats() {
        ShipType frigate = ShipType.FRIGATE;

        assertEquals(50, frigate.getBaseHullPoints());
        assertEquals(3, frigate.getComponentSlots());
        assertEquals(2, frigate.getBuildTimeTurns());
        assertEquals(10, frigate.getBaseSpeed());
        assertEquals(0.20, frigate.getBaseEvasion());
    }

    @Test
    @DisplayName("DESTROYER - should have correct base stats")
    void destroyer_shouldHaveCorrectStats() {
        ShipType destroyer = ShipType.DESTROYER;

        assertEquals(100, destroyer.getBaseHullPoints());
        assertEquals(5, destroyer.getComponentSlots());
        assertEquals(3, destroyer.getBuildTimeTurns());
        assertEquals(8, destroyer.getBaseSpeed());
        assertEquals(0.15, destroyer.getBaseEvasion());
    }

    @Test
    @DisplayName("CRUISER - should have correct base stats")
    void cruiser_shouldHaveCorrectStats() {
        ShipType cruiser = ShipType.CRUISER;

        assertEquals(200, cruiser.getBaseHullPoints());
        assertEquals(8, cruiser.getComponentSlots());
        assertEquals(5, cruiser.getBuildTimeTurns());
        assertEquals(6, cruiser.getBaseSpeed());
        assertEquals(0.10, cruiser.getBaseEvasion());
    }

    @Test
    @DisplayName("BATTLESHIP - should have correct base stats")
    void battleship_shouldHaveCorrectStats() {
        ShipType battleship = ShipType.BATTLESHIP;

        assertEquals(400, battleship.getBaseHullPoints());
        assertEquals(12, battleship.getComponentSlots());
        assertEquals(8, battleship.getBuildTimeTurns());
        assertEquals(4, battleship.getBaseSpeed());
        assertEquals(0.05, battleship.getBaseEvasion());
    }

    @Test
    @DisplayName("CARRIER - should have correct base stats")
    void carrier_shouldHaveCorrectStats() {
        ShipType carrier = ShipType.CARRIER;

        assertEquals(300, carrier.getBaseHullPoints());
        assertEquals(6, carrier.getComponentSlots());
        assertEquals(7, carrier.getBuildTimeTurns());
        assertEquals(5, carrier.getBaseSpeed());
        assertEquals(0.08, carrier.getBaseEvasion());
    }

    @Test
    @DisplayName("MOTHERSHIP - should have correct base stats")
    void mothership_shouldHaveCorrectStats() {
        ShipType mothership = ShipType.MOTHERSHIP;

        assertEquals(800, mothership.getBaseHullPoints());
        assertEquals(20, mothership.getComponentSlots());
        assertEquals(15, mothership.getBuildTimeTurns());
        assertEquals(2, mothership.getBaseSpeed());
        assertEquals(0.02, mothership.getBaseEvasion());
    }

    @Test
    @DisplayName("Hull points should increase with ship size")
    void hullPoints_shouldIncreaseWithSize() {
        assertTrue(ShipType.FRIGATE.getBaseHullPoints() < ShipType.DESTROYER.getBaseHullPoints());
        assertTrue(ShipType.DESTROYER.getBaseHullPoints() < ShipType.CRUISER.getBaseHullPoints());
        assertTrue(ShipType.CRUISER.getBaseHullPoints() < ShipType.BATTLESHIP.getBaseHullPoints());
        assertTrue(ShipType.BATTLESHIP.getBaseHullPoints() < ShipType.MOTHERSHIP.getBaseHullPoints());
    }

    @Test
    @DisplayName("Speed should decrease with ship size")
    void speed_shouldDecreaseWithSize() {
        assertTrue(ShipType.FRIGATE.getBaseSpeed() > ShipType.DESTROYER.getBaseSpeed());
        assertTrue(ShipType.DESTROYER.getBaseSpeed() > ShipType.CRUISER.getBaseSpeed());
        assertTrue(ShipType.CRUISER.getBaseSpeed() > ShipType.BATTLESHIP.getBaseSpeed());
        assertTrue(ShipType.BATTLESHIP.getBaseSpeed() > ShipType.MOTHERSHIP.getBaseSpeed());
    }

    @Test
    @DisplayName("Evasion should decrease with ship size")
    void evasion_shouldDecreaseWithSize() {
        assertTrue(ShipType.FRIGATE.getBaseEvasion() > ShipType.DESTROYER.getBaseEvasion());
        assertTrue(ShipType.DESTROYER.getBaseEvasion() > ShipType.CRUISER.getBaseEvasion());
        assertTrue(ShipType.CRUISER.getBaseEvasion() > ShipType.BATTLESHIP.getBaseEvasion());
        assertTrue(ShipType.BATTLESHIP.getBaseEvasion() > ShipType.MOTHERSHIP.getBaseEvasion());
    }

    @Test
    @DisplayName("valueOf - should resolve by name")
    void valueOf_shouldResolveByName() {
        assertSame(ShipType.CRUISER, ShipType.valueOf("CRUISER"));
    }

    @Test
    @DisplayName("valueOf - should throw on invalid name")
    void valueOf_shouldThrowOnInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> ShipType.valueOf("INVALID"));
    }
}
