package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FleetAiCoordinator Tests")
class FleetAiCoordinatorTest {

    @Test
    @DisplayName("should find best AI core in fleet")
    void findBestAiCore() {
        Fleet fleet = new Fleet(1, "Test Fleet");

        Ship ship1 = new Ship(1, ShipType.FRIGATE);
        ship1.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.BASIC));
        fleet.addShip(ship1);

        Ship ship2 = new Ship(2, ShipType.CRUISER);
        ship2.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.ELITE));
        fleet.addShip(ship2);

        FleetAiCoordinator coordinator = new FleetAiCoordinator(fleet);
        AiCoreComponent best = coordinator.getBestAiCore();
        assertNotNull(best);
        assertEquals(AiCoreComponent.Tier.ELITE, best.getTier());
    }

    @Test
    @DisplayName("should return null when no AI cores")
    void noAiCores() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        fleet.addShip(new Ship(1, ShipType.FRIGATE));
        fleet.addShip(new Ship(2, ShipType.FRIGATE));

        FleetAiCoordinator coordinator = new FleetAiCoordinator(fleet);
        AiCoreComponent best = coordinator.getBestAiCore();
        assertNull(best);
    }

    @Test
    @DisplayName("should provide formation bonus")
    void formationBonus() {
        Fleet fleet = new Fleet(1, "Test Fleet");

        Ship ship1 = new Ship(1, ShipType.FRIGATE);
        ship1.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.BASIC));
        fleet.addShip(ship1);

        Ship ship2 = new Ship(2, ShipType.FRIGATE);
        ship2.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.BASIC));
        fleet.addShip(ship2);

        FleetAiCoordinator coordinator = new FleetAiCoordinator(fleet);
        double bonus = coordinator.getFormationBonus();
        assertTrue(bonus > 1.0, "Formation bonus should be > 1.0 with AI Core ships");
    }

    @Test
    @DisplayName("should cap formation bonus")
    void capFormationBonus() {
        Fleet fleet = new Fleet(1, "Test Fleet");

        for (int i = 0; i < 10; i++) {
            Ship ship = new Ship(i, ShipType.FRIGATE);
            ship.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.ELITE));
            fleet.addShip(ship);
        }

        FleetAiCoordinator coordinator = new FleetAiCoordinator(fleet);
        double bonus = coordinator.getFormationBonus();
        assertTrue(bonus <= 1.3, "Formation bonus should be capped");
    }

    @Test
    @DisplayName("should provide coordination evasion bonus")
    void coordinationEvasionBonus() {
        Fleet fleet = new Fleet(1, "Test Fleet");

        Ship ship1 = new Ship(1, ShipType.FRIGATE);
        ship1.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.BASIC));
        fleet.addShip(ship1);

        Ship ship2 = new Ship(2, ShipType.FRIGATE);
        fleet.addShip(ship2);

        FleetAiCoordinator coordinator = new FleetAiCoordinator(fleet);
        double bonus = coordinator.getCoordinationEvasionBonus();
        assertTrue(bonus >= 0.0);
    }

    @Test
    @DisplayName("should provide command damage bonus")
    void commandDamageBonus() {
        Fleet fleet = new Fleet(1, "Test Fleet");

        Ship ship1 = new Ship(1, ShipType.FRIGATE);
        ship1.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.ELITE));
        fleet.addShip(ship1);

        Ship ship2 = new Ship(2, ShipType.FRIGATE);
        fleet.addShip(ship2);

        FleetAiCoordinator coordinator = new FleetAiCoordinator(fleet);
        double bonus = coordinator.getCommandDamageBonus();
        assertTrue(bonus >= 0.0);
    }

    @Test
    @DisplayName("should return base values for empty fleet")
    void emptyFleet() {
        Fleet fleet = new Fleet(1, "Test Fleet");

        FleetAiCoordinator coordinator = new FleetAiCoordinator(fleet);
        assertEquals(1.0, coordinator.getFormationBonus(), 0.001);
        assertEquals(0.0, coordinator.getCoordinationEvasionBonus(), 0.001);
        assertEquals(0.0, coordinator.getCommandDamageBonus(), 0.001);
    }

    @Test
    @DisplayName("should count AI core ships")
    void aiCoreShipCount() {
        Fleet fleet = new Fleet(1, "Test Fleet");

        Ship ship1 = new Ship(1, ShipType.FRIGATE);
        ship1.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.BASIC));
        fleet.addShip(ship1);

        fleet.addShip(new Ship(2, ShipType.FRIGATE));

        Ship ship3 = new Ship(3, ShipType.FRIGATE);
        ship3.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.ADVANCED));
        fleet.addShip(ship3);

        FleetAiCoordinator coordinator = new FleetAiCoordinator(fleet);
        assertEquals(2, coordinator.getAiCoreShipCount());
    }
}
