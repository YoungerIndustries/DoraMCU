package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedCargo Tests")
class UnifiedCargoTest {

    @Test
    @DisplayName("Should create cargo for ship")
    void testCreateCargo() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        UnifiedCargo cargo = new UnifiedCargo(ship);
        assertEquals(ship, cargo.getShip());
        assertTrue(cargo.getCapacityTons() > 0);
        assertTrue(cargo.isEmpty());
    }

    @Test
    @DisplayName("Should create cargo with capacity")
    void testCreateCargoWithCapacity() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        UnifiedCargo cargo = new UnifiedCargo(ship, 500);
        assertEquals(500, cargo.getCapacityTons());
    }

    @Test
    @DisplayName("Should load and unload cargo")
    void testLoadUnload() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        UnifiedCargo cargo = new UnifiedCargo(ship, 100);
        int loaded = cargo.loadCargo(Commodity.FOOD, 5);
        assertEquals(5, loaded);
        assertEquals(5, cargo.getQuantity(Commodity.FOOD));
        assertFalse(cargo.isEmpty());

        int unloaded = cargo.unloadCargo(Commodity.FOOD, 3);
        assertEquals(3, unloaded);
        assertEquals(2, cargo.getQuantity(Commodity.FOOD));
    }

    @Test
    @DisplayName("Should unload all")
    void testUnloadAll() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        UnifiedCargo cargo = new UnifiedCargo(ship, 100);
        cargo.loadCargo(Commodity.FOOD, 5);
        cargo.loadCargo(Commodity.FUEL, 3);
        int total = cargo.unloadAll();
        assertEquals(8, total);
        assertTrue(cargo.isEmpty());
    }

    @Test
    @DisplayName("Should get remaining capacity")
    void testRemainingCapacity() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        UnifiedCargo cargo = new UnifiedCargo(ship, 100);
        double remaining = cargo.getRemainingCapacityTons();
        assertTrue(remaining > 0);
    }

    @Test
    @DisplayName("Should get cargo map")
    void testCargoMap() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        UnifiedCargo cargo = new UnifiedCargo(ship, 100);
        cargo.loadCargo(Commodity.FOOD, 5);
        cargo.loadCargo(Commodity.FUEL, 3);
        assertEquals(2, cargo.getCargoMap().size());
    }

    @Test
    @DisplayName("Should transfer cargo")
    void testTransfer() {
        UnifiedShip ship1 = UnifiedShipFactory.createFrigate(1);
        UnifiedShip ship2 = UnifiedShipFactory.createFrigate(2);
        UnifiedCargo cargo1 = new UnifiedCargo(ship1, 100);
        UnifiedCargo cargo2 = new UnifiedCargo(ship2, 100);
        cargo1.loadCargo(Commodity.FOOD, 10);
        int transferred = cargo1.transferTo(Commodity.FOOD, 5, cargo2);
        assertEquals(5, transferred);
        assertEquals(5, cargo1.getQuantity(Commodity.FOOD));
        assertEquals(5, cargo2.getQuantity(Commodity.FOOD));
    }

    @Test
    @DisplayName("Should get cargo history")
    void testCargoHistory() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        UnifiedCargo cargo = new UnifiedCargo(ship, 100);
        cargo.loadCargo(Commodity.FOOD, 5);
        assertTrue(cargo.getCargoHistory().size() >= 1);
    }

    @Test
    @DisplayName("Should clear history")
    void testClearHistory() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        UnifiedCargo cargo = new UnifiedCargo(ship, 100);
        cargo.loadCargo(Commodity.FOOD, 5);
        cargo.clearHistory();
        assertTrue(cargo.getCargoHistory().isEmpty());
    }
}
