package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fleet Tests")
class FleetTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Should create fleet with default name")
    void testDefaultConstructor() {
        Fleet fleet = new Fleet(1);
        assertEquals(1, fleet.getId());
        assertEquals("Fleet-1", fleet.getName());
        assertTrue(fleet.isEmpty());
        assertEquals(0, fleet.getShipCount());
    }

    @Test
    @DisplayName("Should create fleet with custom name")
    void testCustomNameConstructor() {
        Fleet fleet = new Fleet(2, "Alpha Fleet");
        assertEquals(2, fleet.getId());
        assertEquals("Alpha Fleet", fleet.getName());
        assertTrue(fleet.isEmpty());
    }

    @Test
    @DisplayName("Should throw on null name")
    void testNullNameConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new Fleet(3, null));
    }

    @Test
    @DisplayName("Should throw on blank name")
    void testBlankNameConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new Fleet(4, "   "));
    }

    @Test
    @DisplayName("Should throw on empty name")
    void testEmptyNameConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new Fleet(5, ""));
    }

    @Test
    @DisplayName("Should accept negative ID")
    void testNegativeId() {
        Fleet fleet = new Fleet(-1, "Negative Fleet");
        assertEquals(-1, fleet.getId());
    }

    // ---- Accessors ----

    @Test
    @DisplayName("Should return correct ID")
    void testGetId() {
        Fleet fleet = new Fleet(100, "Test Fleet");
        assertEquals(100, fleet.getId());
    }

    @Test
    @DisplayName("Should return correct name")
    void testGetName() {
        Fleet fleet = new Fleet(1, "Bravo Fleet");
        assertEquals("Bravo Fleet", fleet.getName());
    }

    @Test
    @DisplayName("Should set new name")
    void testSetName() {
        Fleet fleet = new Fleet(1);
        fleet.setName("Charlie Fleet");
        assertEquals("Charlie Fleet", fleet.getName());
    }

    @Test
    @DisplayName("Should throw on null name set")
    void testSetNullName() {
        Fleet fleet = new Fleet(1);
        assertThrows(IllegalArgumentException.class, () -> fleet.setName(null));
    }

    @Test
    @DisplayName("Should throw on blank name set")
    void testSetBlankName() {
        Fleet fleet = new Fleet(1);
        assertThrows(IllegalArgumentException.class, () -> fleet.setName("   "));
    }

    // ---- Position ----

    @Test
    @DisplayName("Should have null position initially")
    void testInitialPosition() {
        Fleet fleet = new Fleet(1);
        assertNull(fleet.getPosition());
    }

    @Test
    @DisplayName("Should set position")
    void testSetPosition() {
        Fleet fleet = new Fleet(1);
        Hex3D pos = new Hex3D(5, -3, -2);
        fleet.setPosition(pos);
        assertEquals(pos, fleet.getPosition());
    }

    @Test
    @DisplayName("Should move from null position")
    void testMoveFromNull() {
        Fleet fleet = new Fleet(1);
        Hex3D pos = new Hex3D(5, -3, -2);
        int distance = fleet.move(pos);
        assertEquals(0, distance);
        assertEquals(pos, fleet.getPosition());
    }

    @Test
    @DisplayName("Should move and return distance")
    void testMoveReturnsDistance() {
        Fleet fleet = new Fleet(1);
        fleet.setPosition(new Hex3D(0, 0, 0));
        Hex3D newPos = new Hex3D(5, -3, -2);
        int distance = fleet.move(newPos);
        assertEquals(5, distance);
        assertEquals(newPos, fleet.getPosition());
    }

    // ---- Home System ----

    @Test
    @DisplayName("Should have null home system initially")
    void testInitialHomeSystem() {
        Fleet fleet = new Fleet(1);
        assertNull(fleet.getHomeSystem());
    }

    @Test
    @DisplayName("Should set home system")
    void testSetHomeSystem() {
        Fleet fleet = new Fleet(1);
        Planet planet = new Planet(10);
        fleet.setHomeSystem(planet);
        assertEquals(planet, fleet.getHomeSystem());
    }

    // ---- Repair Rates ----

    @Test
    @DisplayName("Should have default shield recharge rate")
    void testDefaultShieldRechargeRate() {
        Fleet fleet = new Fleet(1);
        assertEquals(Fleet.DEFAULT_SHIELD_RECHARGE_RATE, fleet.getShieldRechargeRate());
    }

    @Test
    @DisplayName("Should have default hull repair rate")
    void testDefaultHullRepairRate() {
        Fleet fleet = new Fleet(1);
        assertEquals(Fleet.DEFAULT_HULL_REPAIR_RATE, fleet.getHullRepairRate());
    }

    @Test
    @DisplayName("Should set shield recharge rate")
    void testSetShieldRechargeRate() {
        Fleet fleet = new Fleet(1);
        fleet.setShieldRechargeRate(20);
        assertEquals(20, fleet.getShieldRechargeRate());
    }

    @Test
    @DisplayName("Should set hull repair rate")
    void testSetHullRepairRate() {
        Fleet fleet = new Fleet(1);
        fleet.setHullRepairRate(10);
        assertEquals(10, fleet.getHullRepairRate());
    }

    @Test
    @DisplayName("Should throw on negative shield recharge rate")
    void testNegativeShieldRechargeRate() {
        Fleet fleet = new Fleet(1);
        assertThrows(IllegalArgumentException.class, () -> fleet.setShieldRechargeRate(-1));
    }

    @Test
    @DisplayName("Should throw on negative hull repair rate")
    void testNegativeHullRepairRate() {
        Fleet fleet = new Fleet(1);
        assertThrows(IllegalArgumentException.class, () -> fleet.setHullRepairRate(-1));
    }

    @Test
    @DisplayName("Should allow zero repair rates")
    void testZeroRepairRates() {
        Fleet fleet = new Fleet(1);
        fleet.setShieldRechargeRate(0);
        fleet.setHullRepairRate(0);
        assertEquals(0, fleet.getShieldRechargeRate());
        assertEquals(0, fleet.getHullRepairRate());
    }

    // ---- Ship Management ----

    @Test
    @DisplayName("Should add ship to fleet")
    void testAddShip() {
        Fleet fleet = new Fleet(1);
        Ship ship = new Ship(1, ShipType.FRIGATE);
        assertTrue(fleet.addShip(ship));
        assertEquals(1, fleet.getShipCount());
        assertFalse(fleet.isEmpty());
    }

    @Test
    @DisplayName("Should throw on null ship add")
    void testAddNullShip() {
        Fleet fleet = new Fleet(1);
        assertThrows(IllegalArgumentException.class, () -> fleet.addShip(null));
    }

    @Test
    @DisplayName("Should add multiple ships")
    void testAddMultipleShips() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        Ship s2 = new Ship(2, ShipType.CRUISER);
        Ship s3 = new Ship(3, ShipType.BATTLESHIP);

        fleet.addShip(s1);
        fleet.addShip(s2);
        fleet.addShip(s3);

        assertEquals(3, fleet.getShipCount());
    }

    @Test
    @DisplayName("Should remove ship from fleet")
    void testRemoveShip() {
        Fleet fleet = new Fleet(1);
        Ship ship = new Ship(1, ShipType.FRIGATE);
        fleet.addShip(ship);

        assertTrue(fleet.removeShip(ship));
        assertEquals(0, fleet.getShipCount());
        assertTrue(fleet.isEmpty());
    }

    @Test
    @DisplayName("Should return false removing non-existent ship")
    void testRemoveNonExistentShip() {
        Fleet fleet = new Fleet(1);
        Ship ship = new Ship(1, ShipType.FRIGATE);
        assertFalse(fleet.removeShip(ship));
    }

    @Test
    @DisplayName("Should clear all ships")
    void testClearShips() {
        Fleet fleet = new Fleet(1);
        fleet.addShip(new Ship(1, ShipType.FRIGATE));
        fleet.addShip(new Ship(2, ShipType.CRUISER));

        fleet.clearShips();
        assertEquals(0, fleet.getShipCount());
        assertTrue(fleet.isEmpty());
    }

    @Test
    @DisplayName("Should return unmodifiable ships list")
    void testGetShipsUnmodifiable() {
        Fleet fleet = new Fleet(1);
        assertThrows(UnsupportedOperationException.class, () -> fleet.getShips().add(new Ship(1, ShipType.FRIGATE)));
    }

    @Test
    @DisplayName("Should get ships by type")
    void testGetShipsByType() {
        Fleet fleet = new Fleet(1);
        fleet.addShip(new Ship(1, ShipType.FRIGATE));
        fleet.addShip(new Ship(2, ShipType.FRIGATE));
        fleet.addShip(new Ship(3, ShipType.CRUISER));

        assertEquals(2, fleet.getShipsByType(ShipType.FRIGATE).size());
        assertEquals(1, fleet.getShipsByType(ShipType.CRUISER).size());
        assertEquals(0, fleet.getShipsByType(ShipType.BATTLESHIP).size());
    }

    @Test
    @DisplayName("Should get destroyed ships")
    void testGetDestroyedShips() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.takeDamage(1000); // Destroy it

        fleet.addShip(s1);
        fleet.addShip(s2);

        assertEquals(1, fleet.getDestroyedShips().size());
        assertTrue(fleet.getDestroyedShips().contains(s2));
    }

    @Test
    @DisplayName("Should get active ships")
    void testGetActiveShips() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.takeDamage(1000); // Destroy it

        fleet.addShip(s1);
        fleet.addShip(s2);

        assertEquals(1, fleet.getActiveShips().size());
        assertTrue(fleet.getActiveShips().contains(s1));
    }

    @Test
    @DisplayName("Should remove destroyed ships")
    void testRemoveDestroyedShips() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        Ship s2 = new Ship(2, ShipType.FRIGATE);
        Ship s3 = new Ship(3, ShipType.FRIGATE);
        s2.takeDamage(1000);
        s3.takeDamage(1000);

        fleet.addShip(s1);
        fleet.addShip(s2);
        fleet.addShip(s3);

        int removed = fleet.removeDestroyedShips();
        assertEquals(2, removed);
        assertEquals(1, fleet.getShipCount());
    }

    // ---- Fleet-level Combat Stats ----

    @Test
    @DisplayName("Should have zero damage when empty")
    void testEmptyFleetDamage() {
        Fleet fleet = new Fleet(1);
        assertEquals(0, fleet.getTotalDamage());
    }

    @Test
    @DisplayName("Should sum damage from all ships")
    void testTotalDamage() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 2));

        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.addComponent(new ShipComponent(WeaponType.MISSILE, 1));

        fleet.addShip(s1);
        fleet.addShip(s2);

        int expected = s1.getTotalDamage() + s2.getTotalDamage();
        assertEquals(expected, fleet.getTotalDamage());
    }

    @Test
    @DisplayName("Should exclude destroyed ships from damage")
    void testDamageExcludesDestroyed() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 2));

        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.addComponent(new ShipComponent(WeaponType.MISSILE, 1));
        s2.takeDamage(1000); // Destroy

        fleet.addShip(s1);
        fleet.addShip(s2);

        assertEquals(s1.getTotalDamage(), fleet.getTotalDamage());
    }

    @Test
    @DisplayName("Should calculate effective damage with planet bonuses")
    void testEffectiveDamage() {
        Fleet fleet = new Fleet(1);
        Planet planet = new Planet(10);
        planet.setPopulationStat(PopulationAttribute.RESOLVE, 8);

        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 2));
        s1.setHomePlanet(planet);

        fleet.addShip(s1);

        // Base damage: 10 * 2 = 20, Resolve bonus: (8-5) * 0.05 = 15%
        // Effective: 20 * 1.15 = 23
        assertEquals(23, fleet.getEffectiveDamage());
    }

    @Test
    @DisplayName("Should sum shield points from all ships")
    void testTotalShieldPoints() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(ShipComponent.shield(2));

        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.addComponent(ShipComponent.shield(1));

        fleet.addShip(s1);
        fleet.addShip(s2);

        assertEquals(60, fleet.getTotalShieldPoints()); // 40 + 20
    }

    @Test
    @DisplayName("Should sum hull points from all ships")
    void testTotalHullPoints() {
        Fleet fleet = new Fleet(1);
        fleet.addShip(new Ship(1, ShipType.FRIGATE));
        fleet.addShip(new Ship(2, ShipType.CRUISER));

        assertEquals(250, fleet.getTotalHullPoints()); // 50 + 200
    }

    @Test
    @DisplayName("Should get max range from all ships")
    void testMaxRange() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 1)); // Range 1

        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.addComponent(new ShipComponent(WeaponType.MISSILE, 1)); // Range 3

        fleet.addShip(s1);
        fleet.addShip(s2);

        assertEquals(3, fleet.getMaxRange());
    }

    @Test
    @DisplayName("Should have zero range when empty")
    void testEmptyFleetRange() {
        Fleet fleet = new Fleet(1);
        assertEquals(0, fleet.getMaxRange());
    }

    @Test
    @DisplayName("Should calculate fleet health ratio")
    void testFleetHealthRatio() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        fleet.addShip(s1);

        assertEquals(1.0, fleet.getFleetHealthRatio(), 0.001);

        s1.takeDamage(25); // 25/50 remaining
        assertEquals(0.5, fleet.getFleetHealthRatio(), 0.001);
    }

    @Test
    @DisplayName("Should have zero health ratio when empty")
    void testEmptyFleetHealthRatio() {
        Fleet fleet = new Fleet(1);
        assertEquals(0.0, fleet.getFleetHealthRatio());
    }

    @Test
    @DisplayName("Should be destroyed when all ships destroyed")
    void testIsDestroyed() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        fleet.addShip(s1);

        assertFalse(fleet.isDestroyed());
        s1.takeDamage(1000);
        assertTrue(fleet.isDestroyed());
    }

    @Test
    @DisplayName("Should be destroyed when empty")
    void testEmptyFleetIsDestroyed() {
        Fleet fleet = new Fleet(1);
        assertTrue(fleet.isDestroyed());
    }

    @Test
    @DisplayName("Should not be destroyed when some ships survive")
    void testNotDestroyedWhenSomeSurvive() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        Ship s2 = new Ship(2, ShipType.CRUISER);
        fleet.addShip(s1);
        fleet.addShip(s2);

        s1.takeDamage(1000); // Destroy s1
        assertFalse(fleet.isDestroyed()); // s2 still alive
    }

    // ---- Current Points ----

    @Test
    @DisplayName("Should get current shield points")
    void testCurrentShieldPoints() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(ShipComponent.shield(2));
        s1.rechargeShields(40); // Full shields
        fleet.addShip(s1);

        assertEquals(40, fleet.getCurrentShieldPoints());

        s1.takeDamage(20); // Shields down to 20
        assertEquals(20, fleet.getCurrentShieldPoints());
    }

    @Test
    @DisplayName("Should get current hull points")
    void testCurrentHullPoints() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        fleet.addShip(s1);

        assertEquals(50, fleet.getCurrentHullPoints());

        s1.takeDamage(25);
        assertEquals(25, fleet.getCurrentHullPoints());
    }

    // ---- Resource Costs ----

    @Test
    @DisplayName("Should have zero cost when empty")
    void testEmptyFleetCost() {
        Fleet fleet = new Fleet(1);
        assertEquals(0, fleet.getTotalMineralCost());
        assertEquals(0, fleet.getTotalEnergyCost());
    }

    @Test
    @DisplayName("Should sum mineral costs")
    void testTotalMineralCost() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 1));

        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.addComponent(new ShipComponent(WeaponType.MISSILE, 1));
        s2.addComponent(ShipComponent.shield(1));

        fleet.addShip(s1);
        fleet.addShip(s2);

        // Each ship has hull cost + component cost
        assertEquals(s1.getMineralCost() + s2.getMineralCost(), fleet.getTotalMineralCost());
    }

    @Test
    @DisplayName("Should sum energy costs")
    void testTotalEnergyCost() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 1));

        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.addComponent(new ShipComponent(WeaponType.MISSILE, 1));
        s2.addComponent(ShipComponent.shield(1));

        fleet.addShip(s1);
        fleet.addShip(s2);

        // Each ship has hull cost + component cost
        assertEquals(s1.getEnergyCost() + s2.getEnergyCost(), fleet.getTotalEnergyCost());
    }

    @Test
    @DisplayName("Should aggregate strategic resources")
    void testTotalRequiredStrategicResources() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 2));

        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.addComponent(new ShipComponent(WeaponType.BEAM, 1));

        fleet.addShip(s1);
        fleet.addShip(s2);

        Map<StrategicResource, Integer> resources = fleet.getTotalRequiredStrategicResources();
        assertEquals(3, resources.get(StrategicResource.ELERIUM).intValue()); // 2 + 1
    }

    @Test
    @DisplayName("Should get unique resource types")
    void testTotalRequiredResourceTypes() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 1));

        Ship s2 = new Ship(2, ShipType.FRIGATE);
        s2.addComponent(new ShipComponent(WeaponType.MISSILE, 1));

        fleet.addShip(s1);
        fleet.addShip(s2);

        Set<StrategicResource> types = fleet.getTotalRequiredResourceTypes();
        assertTrue(types.contains(StrategicResource.ELERIUM));
        assertTrue(types.contains(StrategicResource.ANTI_MATTER));
        assertEquals(2, types.size());
    }

    @Test
    @DisplayName("Should check if all ships can be built")
    void testCanBuildAllWith() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 1));

        fleet.addShip(s1);

        Set<StrategicResource> available = Set.of(StrategicResource.ELERIUM);
        assertTrue(fleet.canBuildAllWith(available));

        available = Set.of(StrategicResource.ANTI_MATTER);
        assertFalse(fleet.canBuildAllWith(available));
    }

    @Test
    @DisplayName("Should return true for empty fleet build check")
    void testEmptyFleetCanBuild() {
        Fleet fleet = new Fleet(1);
        assertTrue(fleet.canBuildAllWith(Set.of()));
    }

    // ---- Fleet Operations ----

    @Test
    @DisplayName("Should recharge shields and repair hull")
    void testEndTurnRepair() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(ShipComponent.shield(2));
        s1.rechargeShields(40);
        s1.takeDamage(30); // Shields: 10, Hull: 50

        fleet.addShip(s1);
        fleet.endTurnRepair();

        assertEquals(20, s1.getCurrentShieldPoints()); // 10 + 10 recharge
        assertEquals(50, s1.getCurrentHullPoints()); // Hull was full, stays full
    }

    @Test
    @DisplayName("Should fully repair all ships")
    void testFullRepairAll() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(ShipComponent.shield(2));
        s1.rechargeShields(40);
        s1.takeDamage(30);

        fleet.addShip(s1);
        fleet.fullRepairAll();

        assertEquals(50, s1.getCurrentHullPoints());
        assertEquals(40, s1.getCurrentShieldPoints());
    }

    // ---- Damage Distribution ----

    @Test
    @DisplayName("Should distribute damage to weakest ships first")
    void testDamageDistribution() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        Ship s2 = new Ship(2, ShipType.FRIGATE);

        s1.takeDamage(40); // 10/50 hull remaining (weaker)

        fleet.addShip(s1);
        fleet.addShip(s2);

        fleet.distributeDamage(30);

        // s1 should take damage first (weakest)
        assertTrue(s1.getCurrentHullPoints() < s2.getCurrentHullPoints());
    }

    @Test
    @DisplayName("Should handle zero damage distribution")
    void testZeroDamageDistribution() {
        Fleet fleet = new Fleet(1);
        fleet.addShip(new Ship(1, ShipType.FRIGATE));

        assertEquals(0, fleet.distributeDamage(0));
    }

    @Test
    @DisplayName("Should handle negative damage distribution")
    void testNegativeDamageDistribution() {
        Fleet fleet = new Fleet(1);
        fleet.addShip(new Ship(1, ShipType.FRIGATE));

        assertEquals(0, fleet.distributeDamage(-10));
    }

    // ---- Object Contract ----

    @Test
    @DisplayName("Should equal same ID")
    void testEqualsSameId() {
        Fleet f1 = new Fleet(1, "Fleet A");
        Fleet f2 = new Fleet(1, "Fleet B");
        assertEquals(f1, f2);
    }

    @Test
    @DisplayName("Should not equal different ID")
    void testEqualsDifferentId() {
        Fleet f1 = new Fleet(1, "Fleet A");
        Fleet f2 = new Fleet(2, "Fleet B");
        assertNotEquals(f1, f2);
    }

    @Test
    @DisplayName("Should not equal null")
    void testEqualsNull() {
        Fleet fleet = new Fleet(1);
        assertNotEquals(null, fleet);
    }

    @Test
    @DisplayName("Should not equal different class")
    void testEqualsDifferentClass() {
        Fleet fleet = new Fleet(1);
        assertNotEquals(fleet, "Fleet");
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCode() {
        Fleet f1 = new Fleet(1, "Fleet A");
        Fleet f2 = new Fleet(1, "Fleet B");
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    @DisplayName("Should have correct toString")
    void testToString() {
        Fleet fleet = new Fleet(1, "Alpha");
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        fleet.addShip(s1);

        String str = fleet.toString();
        assertTrue(str.contains("Alpha"));
        assertTrue(str.contains("ships=1"));
    }

    // ---- Integration Tests ----

    @Test
    @DisplayName("Should build complete fleet with mixed ships")
    void testCompleteFleetBuild() {
        Fleet fleet = new Fleet(1, "Task Force Alpha");
        Planet planet = new Planet(10);
        planet.addStrategicDeposit(StrategicResource.ELERIUM);
        planet.addStrategicDeposit(StrategicResource.ANTI_MATTER);

        // Frigate
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 2));
        frigate.setHomePlanet(planet);

        // Cruiser
        Ship cruiser = new Ship(2, ShipType.CRUISER);
        cruiser.addComponent(new ShipComponent(WeaponType.MISSILE, 2));
        cruiser.addComponent(ShipComponent.shield(2));
        cruiser.setHomePlanet(planet);

        // Battleship
        Ship battleship = new Ship(3, ShipType.BATTLESHIP);
        battleship.addComponent(new ShipComponent(WeaponType.BEAM, 3));
        battleship.addComponent(new ShipComponent(WeaponType.MISSILE, 2));
        battleship.addComponent(ShipComponent.shield(3));
        battleship.setHomePlanet(planet);

        fleet.addShip(frigate);
        fleet.addShip(cruiser);
        fleet.addShip(battleship);

        assertEquals(3, fleet.getShipCount());
        assertEquals("Task Force Alpha", fleet.getName());
        assertFalse(fleet.isEmpty());
        assertFalse(fleet.isDestroyed());

        // Check aggregated stats
        assertTrue(fleet.getTotalDamage() > 0);
        assertTrue(fleet.getTotalShieldPoints() > 0);
        assertTrue(fleet.getTotalHullPoints() > 0);

        // Check resources
        Set<StrategicResource> types = fleet.getTotalRequiredResourceTypes();
        assertTrue(types.contains(StrategicResource.ELERIUM));
        assertTrue(types.contains(StrategicResource.ANTI_MATTER));
    }

    @Test
    @DisplayName("Should handle fleet combat with planet bonuses")
    void testFleetCombatWithPlanetBonuses() {
        Planet strongPlanet = new Planet(10);
        strongPlanet.setPopulationStat(PopulationAttribute.RESOLVE, 10);

        Planet weakPlanet = new Planet(11);
        weakPlanet.setPopulationStat(PopulationAttribute.RESOLVE, 1);

        Fleet strongFleet = new Fleet(1, "Strong");
        Ship s1 = new Ship(1, ShipType.CRUISER);
        s1.addComponent(new ShipComponent(WeaponType.BEAM, 3));
        s1.setHomePlanet(strongPlanet);
        strongFleet.addShip(s1);

        Fleet weakFleet = new Fleet(2, "Weak");
        Ship s2 = new Ship(2, ShipType.CRUISER);
        s2.addComponent(new ShipComponent(WeaponType.BEAM, 3));
        s2.setHomePlanet(weakPlanet);
        weakFleet.addShip(s2);

        // Strong fleet should have higher effective damage
        assertTrue(strongFleet.getEffectiveDamage() > weakFleet.getEffectiveDamage());
    }

    @Test
    @DisplayName("Should handle end turn repair with custom rates")
    void testEndTurnRepairCustomRates() {
        Fleet fleet = new Fleet(1);
        fleet.setShieldRechargeRate(20);
        fleet.setHullRepairRate(10);

        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(ShipComponent.shield(2));
        s1.rechargeShields(40);
        s1.takeDamage(30); // Shields: 10, Hull: 50

        fleet.addShip(s1);
        fleet.endTurnRepair();

        assertEquals(30, s1.getCurrentShieldPoints()); // 10 + 20
    }

    @Test
    @DisplayName("Should handle fleet with no weapons")
    void testFleetNoWeapons() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(ShipComponent.shield(2));
        fleet.addShip(s1);

        assertEquals(0, fleet.getTotalDamage());
        assertEquals(0, fleet.getMaxRange());
    }

    @Test
    @DisplayName("Should handle fleet with only hull reinforcement")
    void testFleetHullReinforcement() {
        Fleet fleet = new Fleet(1);
        Ship s1 = new Ship(1, ShipType.FRIGATE);
        s1.addComponent(ShipComponent.hullReinforcement(2));
        fleet.addShip(s1);

        assertEquals(50 + 30, fleet.getTotalHullPoints()); // 50 base + 30 bonus
        assertEquals(0, fleet.getTotalDamage());
    }

    @Test
    @DisplayName("Should verify constants")
    void testConstants() {
        assertEquals(10, Fleet.DEFAULT_SHIELD_RECHARGE_RATE);
        assertEquals(5, Fleet.DEFAULT_HULL_REPAIR_RATE);
    }

    // ---- Speed and Evasion ----

    @Test
    @DisplayName("Should return 0 speed for empty fleet")
    void testFleetSpeedEmpty() {
        Fleet fleet = new Fleet(1);
        assertEquals(0, fleet.getFleetSpeed());
    }

    @Test
    @DisplayName("Should return minimum MDrive speed of active ships")
    void testFleetMDriveSpeedMinimum() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        Ship battleship = new Ship(2, ShipType.BATTLESHIP);
        battleship.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);
        fleet.addShip(battleship);

        // Battleship MDrive is slower (4) than frigate (10)
        assertEquals(4, fleet.getFleetMDriveSpeed());
    }

    @Test
    @DisplayName("Should return minimum JDrive speed of active ships")
    void testFleetJDriveSpeedMinimum() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        Ship battleship = new Ship(2, ShipType.BATTLESHIP);
        battleship.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);
        fleet.addShip(battleship);

        // Battleship JDrive is slower (5) than frigate (12)
        assertEquals(5, fleet.getFleetJDriveSpeed());
    }

    @Test
    @DisplayName("Should return 0 MDrive speed for empty fleet")
    void testFleetMDriveSpeedEmpty() {
        Fleet fleet = new Fleet(1);
        assertEquals(0, fleet.getFleetMDriveSpeed());
    }

    @Test
    @DisplayName("Should return 0 JDrive speed for empty fleet")
    void testFleetJDriveSpeedEmpty() {
        Fleet fleet = new Fleet(1);
        assertEquals(0, fleet.getFleetJDriveSpeed());
    }

    @Test
    @DisplayName("Should return effective JDrive speed reduced by damage")
    void testEffectiveFleetJDriveSpeedDamaged() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);

        int fullSpeed = fleet.getEffectiveFleetJDriveSpeed();
        assertEquals(12, fullSpeed);

        // Damage the ship to 50% hull
        frigate.takeDamage(25);
        int damagedSpeed = fleet.getEffectiveFleetJDriveSpeed();
        assertTrue(damagedSpeed < fullSpeed, "Damaged JDrive speed should be lower");
        assertTrue(damagedSpeed >= 0, "JDrive speed should be at least 0");
    }

    @Test
    @DisplayName("Should return 0 effective JDrive speed when fleet health below 20%")
    void testEffectiveFleetJDriveSpeedDisabled() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);

        // Damage to 10% hull (below 20% threshold)
        frigate.takeDamage(45);
        int speed = fleet.getEffectiveFleetJDriveSpeed();
        assertEquals(0, speed, "JDrive should be disabled below 20% health");
    }

    @Test
    @DisplayName("Should include MDrive component bonus in fleet speed")
    void testFleetMDriveSpeedWithComponentBonus() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        frigate.addComponent(ShipComponent.mDrive(2));
        fleet.addShip(frigate);

        // Frigate base MDrive is 10, MDrive x2 adds +4 = 14
        assertEquals(14, fleet.getFleetMDriveSpeed());
    }

    @Test
    @DisplayName("Should include JDrive component bonus in fleet speed")
    void testFleetJDriveSpeedWithComponentBonus() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        frigate.addComponent(ShipComponent.jDrive(2));
        fleet.addShip(frigate);

        // Frigate base JDrive is 12, JDrive x2 adds +4 = 16
        assertEquals(16, fleet.getFleetJDriveSpeed());
    }

    @Test
    @DisplayName("Should return effective MDrive speed reduced by damage")
    void testEffectiveFleetMDriveSpeedDamaged() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);

        int fullSpeed = fleet.getEffectiveFleetMDriveSpeed();
        assertEquals(10, fullSpeed);

        // Damage the ship to 50% hull
        frigate.takeDamage(25);
        int damagedSpeed = fleet.getEffectiveFleetMDriveSpeed();
        assertTrue(damagedSpeed < fullSpeed, "Damaged MDrive speed should be lower");
        assertTrue(damagedSpeed >= 1, "MDrive speed should be at least 1");
    }

    @Test
    @DisplayName("Should return minimum speed of active ships")
    void testFleetSpeedMinimum() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        Ship battleship = new Ship(2, ShipType.BATTLESHIP);
        battleship.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);
        fleet.addShip(battleship);

        // Battleship is slower (4) than frigate (10)
        assertEquals(4, fleet.getFleetSpeed());
    }

    @Test
    @DisplayName("Should return effective speed reduced by damage")
    void testEffectiveFleetSpeedDamaged() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);

        int fullSpeed = fleet.getEffectiveFleetSpeed();
        assertEquals(10, fullSpeed);

        // Damage the ship to 50% hull
        frigate.takeDamage(25);
        int damagedSpeed = fleet.getEffectiveFleetSpeed();
        assertTrue(damagedSpeed < fullSpeed, "Damaged speed should be lower");
        assertTrue(damagedSpeed >= 1, "Speed should be at least 1");
    }

    @Test
    @DisplayName("Should return minimum 1 effective speed")
    void testEffectiveFleetSpeedMinimum() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);

        // Destroy almost all hull
        frigate.takeDamage(49);
        int speed = fleet.getEffectiveFleetSpeed();
        assertTrue(speed >= 1);
    }

    @Test
    @DisplayName("Should return 0 effective speed for empty fleet")
    void testEffectiveFleetSpeedEmpty() {
        Fleet fleet = new Fleet(1);
        assertEquals(0, fleet.getEffectiveFleetSpeed());
    }

    @Test
    @DisplayName("Should return max evasion of active ships")
    void testFleetEvasionMax() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        Ship battleship = new Ship(2, ShipType.BATTLESHIP);
        battleship.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);
        fleet.addShip(battleship);

        // Frigate has higher evasion (20%) than battleship (5%)
        assertEquals(0.2, fleet.getFleetEvasion(), 0.001);
    }

    @Test
    @DisplayName("Should return 0 evasion for empty fleet")
    void testFleetEvasionEmpty() {
        Fleet fleet = new Fleet(1);
        assertEquals(0.0, fleet.getFleetEvasion(), 0.001);
    }

    @Test
    @DisplayName("Should return weighted average evasion")
    void testAverageFleetEvasion() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);

        // Single ship: average equals its evasion
        assertEquals(0.2, fleet.getAverageFleetEvasion(), 0.001);
    }

    @Test
    @DisplayName("Should return 0 average evasion for empty fleet")
    void testAverageFleetEvasionEmpty() {
        Fleet fleet = new Fleet(1);
        assertEquals(0.0, fleet.getAverageFleetEvasion(), 0.001);
    }

    @Test
    @DisplayName("Should boost effective evasion with shields")
    void testEffectiveFleetEvasionWithShields() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        frigate.addComponent(ShipComponent.shield(2));
        frigate.fullRepair(); // Set current shields to max
        fleet.addShip(frigate);

        double baseEvasion = fleet.getAverageFleetEvasion();
        double effectiveEvasion = fleet.getEffectiveFleetEvasion();

        // With shields at full, evasion should be boosted by 50% then multiplied by health ratio (1.0)
        // 0.2 * 1.5 * 1.0 = 0.3
        assertEquals(0.3, effectiveEvasion, 0.001, "Shields should boost evasion by 50%");
        assertTrue(effectiveEvasion > baseEvasion, "Shields should boost evasion");
    }

    @Test
    @DisplayName("Should reduce effective evasion with damage")
    void testEffectiveFleetEvasionWithDamage() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);

        double fullEvasion = fleet.getEffectiveFleetEvasion();
        frigate.takeDamage(25); // 50% hull
        double damagedEvasion = fleet.getEffectiveFleetEvasion();

        assertTrue(damagedEvasion < fullEvasion, "Damage should reduce evasion");
    }

    @Test
    @DisplayName("Should cap effective evasion at 1.0")
    void testEffectiveFleetEvasionCap() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        frigate.addComponent(ShipComponent.shield(3));
        fleet.addShip(frigate);

        double effectiveEvasion = fleet.getEffectiveFleetEvasion();
        assertTrue(effectiveEvasion <= 1.0);
    }

    // ---- Owner ----

    @Test
    @DisplayName("Should have null owner initially")
    void testOwnerInitiallyNull() {
        Fleet fleet = new Fleet(1);
        assertNull(fleet.getOwner());
    }

    @Test
    @DisplayName("Should set and get owner")
    void testSetOwner() {
        Fleet fleet = new Fleet(1);
        Game game = new Game("TestGame");
        Player player = game.createPlayer("TestPlayer");
        fleet.setOwner(player);
        assertEquals(player, fleet.getOwner());
    }

    // ---- Home Station ----

    @Test
    @DisplayName("Should have null home station initially")
    void testHomeStationInitiallyNull() {
        Fleet fleet = new Fleet(1);
        assertNull(fleet.getHomeStation());
    }

    @Test
    @DisplayName("Should set and get home station")
    void testSetHomeStation() {
        Fleet fleet = new Fleet(1);
        SpaceStation station = new SpaceStation(1);
        fleet.setHomeStation(station);
        assertEquals(station, fleet.getHomeStation());
    }

    // ---- Station Defense Bonus ----

    @Test
    @DisplayName("Should return 0 defense bonus with no station")
    void testStationDefenseBonusNone() {
        Fleet fleet = new Fleet(1);
        assertEquals(0.0, fleet.getStationDefenseBonus(), 0.001);
    }

    @Test
    @DisplayName("Should return point defense from home station")
    void testStationDefenseBonus() {
        Fleet fleet = new Fleet(1);
        SpaceStation station = new SpaceStation(1);
        station.addModule(StationModule.pointDefense(2));
        fleet.setHomeStation(station);

        double bonus = fleet.getStationDefenseBonus();
        assertEquals(0.3, bonus, 0.001); // 0.15 * 2
    }

    @Test
    @DisplayName("Should return 0 defense bonus for destroyed station")
    void testStationDefenseBonusDestroyed() {
        Fleet fleet = new Fleet(1);
        SpaceStation station = new SpaceStation(1);
        station.addModule(StationModule.pointDefense(2));
        station.takeDamage(1000); // Destroy the station (500 hull + 100 shield)
        fleet.setHomeStation(station);

        assertTrue(station.isDestroyed());
        assertEquals(0.0, fleet.getStationDefenseBonus(), 0.001);
    }

    @Test
    @DisplayName("Should reduce incoming damage with station defense")
    void testEffectiveDamageReceived() {
        Fleet fleet = new Fleet(1);
        SpaceStation station = new SpaceStation(1);
        station.addModule(StationModule.pointDefense(2));
        fleet.setHomeStation(station);

        int reduced = fleet.getEffectiveDamageReceived(100);
        assertEquals(70, reduced); // 100 * (1 - 0.3) = 70
    }

    @Test
    @DisplayName("Should not reduce damage without station")
    void testEffectiveDamageReceivedNoStation() {
        Fleet fleet = new Fleet(1);
        int reduced = fleet.getEffectiveDamageReceived(100);
        assertEquals(100, reduced);
    }

    // ---- toString ----

    @Test
    @DisplayName("Should include mDrive, jDrive and evasion in toString")
    void testToStringWithSpeedEvasion() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);

        String str = fleet.toString();
        assertTrue(str.contains("mDrive="));
        assertTrue(str.contains("jDrive="));
        assertTrue(str.contains("evasion="));
        assertTrue(str.contains("Test Fleet"));
    }

    // ---- Copy Fleet ----

    @Test
    @DisplayName("Should copy owner and home station")
    void testCopyFleetOwnerAndStation() {
        Fleet fleet = new Fleet(1, "Original");
        Game game = new Game("TestGame");
        Player player = game.createPlayer("TestPlayer");
        SpaceStation station = new SpaceStation(1);
        fleet.setOwner(player);
        fleet.setHomeStation(station);

        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);

        // Verify copy preserves owner and home station
        Fleet copy = fleet.copyFleet();
        assertEquals(player, copy.getOwner());
        assertEquals(station, copy.getHomeStation());
        assertNotSame(fleet, copy);
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should compute fleet speed with mixed ship types")
    void testFleetSpeedMixedTypes() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        Ship destroyer = new Ship(2, ShipType.DESTROYER);
        destroyer.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        Ship cruiser = new Ship(3, ShipType.CRUISER);
        cruiser.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);
        fleet.addShip(destroyer);
        fleet.addShip(cruiser);

        // Cruiser is slowest at 6
        assertEquals(6, fleet.getFleetSpeed());
    }

    @Test
    @DisplayName("Should exclude destroyed ships from speed calculation")
    void testFleetSpeedExcludesDestroyed() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        Ship battleship = new Ship(2, ShipType.BATTLESHIP);
        battleship.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);
        fleet.addShip(battleship);

        // Destroy the battleship (400 hull points)
        battleship.takeDamage(400);
        assertTrue(battleship.isDestroyed());

        // Only frigate remains, speed should be 10
        assertEquals(10, fleet.getFleetSpeed());
    }

    @Test
    @DisplayName("Should exclude destroyed ships from evasion calculation")
    void testFleetEvasionExcludesDestroyed() {
        Fleet fleet = new Fleet(1);
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        frigate.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        Ship battleship = new Ship(2, ShipType.BATTLESHIP);
        battleship.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        fleet.addShip(frigate);
        fleet.addShip(battleship);

        // Destroy the battleship (400 hull points)
        battleship.takeDamage(400);
        assertTrue(battleship.isDestroyed());

        // Only frigate remains, evasion should be 0.2
        assertEquals(0.2, fleet.getFleetEvasion(), 0.001);
    }

    @Test
    @DisplayName("Should integrate FleetNameDatabase with fleet")
    void testFleetNameDatabaseIntegration() {
        FleetNameDatabase db = new FleetNameDatabase();
        Fleet fleet = new Fleet(1, "Temporary");
        db.assignName(fleet);
        assertEquals("Alpha Strike", fleet.getName());
    }
}
