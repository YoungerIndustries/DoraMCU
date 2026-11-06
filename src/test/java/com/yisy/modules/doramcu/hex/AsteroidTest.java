package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Asteroid Tests")
class AsteroidTest {

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create asteroid with default values")
    void testDefaultConstructor() {
        Asteroid asteroid = new Asteroid(1);

        assertEquals(1, asteroid.getId());
        assertEquals("Asteroid-1", asteroid.getName());
        assertEquals(Asteroid.AsteroidSize.MEDIUM, asteroid.getAsteroidSize());
        assertEquals(Asteroid.DEFAULT_RADIUS, asteroid.getRadius());
        assertEquals(1, asteroid.getMineralYield());
        assertEquals(1, asteroid.getOutpostSlots());
        assertEquals(0.3, asteroid.getMovementPenalty());
        assertFalse(asteroid.hasStrategicDeposit());
        assertNull(asteroid.getStrategicResource());
        assertNull(asteroid.getParentBeltId());
        assertFalse(asteroid.belongsToBelt());
    }

    @Test
    @DisplayName("Should create asteroid with custom radius")
    void testConstructorWithRadius() {
        Asteroid asteroid = new Asteroid(2, 2);

        assertEquals(2, asteroid.getId());
        assertEquals(2, asteroid.getRadius());
        assertEquals(Asteroid.AsteroidSize.MEDIUM, asteroid.getAsteroidSize());
    }

    @Test
    @DisplayName("Should create asteroid with custom size")
    void testConstructorWithSize() {
        Asteroid asteroid = new Asteroid(3, Asteroid.AsteroidSize.LARGE);

        assertEquals(3, asteroid.getId());
        assertEquals(Asteroid.AsteroidSize.LARGE, asteroid.getAsteroidSize());
        assertEquals(2, asteroid.getMineralYield());
        assertEquals(2, asteroid.getOutpostSlots());
        assertEquals(0.4, asteroid.getMovementPenalty());
    }

    @Test
    @DisplayName("Should create asteroid with custom radius and size")
    void testConstructorWithRadiusAndSize() {
        Asteroid asteroid = new Asteroid(4, 2, Asteroid.AsteroidSize.PROTOPLANET);

        assertEquals(4, asteroid.getId());
        assertEquals(2, asteroid.getRadius());
        assertEquals(Asteroid.AsteroidSize.PROTOPLANET, asteroid.getAsteroidSize());
        assertEquals(3, asteroid.getMineralYield());
        assertEquals(3, asteroid.getOutpostSlots());
        assertEquals(0.5, asteroid.getMovementPenalty());
    }

    @Test
    @DisplayName("Should throw on negative radius")
    void testConstructorNegativeRadius() {
        assertThrows(IllegalArgumentException.class, () -> new Asteroid(1, -1));
    }

    @Test
    @DisplayName("Should throw on null size")
    void testConstructorNullSize() {
        assertThrows(IllegalArgumentException.class, () -> new Asteroid(1, 1, null));
    }

    // ---- AsteroidSize enum tests ----

    @Test
    @DisplayName("Should have 5 AsteroidSize enum values")
    void testAsteroidSizeValues() {
        Asteroid.AsteroidSize[] values = Asteroid.AsteroidSize.values();
        assertEquals(5, values.length);
    }

    @Test
    @DisplayName("Should return correct PEBBLE properties")
    void testPebbleProperties() {
        Asteroid.AsteroidSize size = Asteroid.AsteroidSize.PEBBLE;
        assertEquals(0, size.getBaseMineralYield());
        assertEquals(0, size.getBaseOutpostSlots());
        assertEquals(0.1, size.getMovementPenalty());
    }

    @Test
    @DisplayName("Should return correct SMALL properties")
    void testSmallProperties() {
        Asteroid.AsteroidSize size = Asteroid.AsteroidSize.SMALL;
        assertEquals(1, size.getBaseMineralYield());
        assertEquals(0, size.getBaseOutpostSlots());
        assertEquals(0.2, size.getMovementPenalty());
    }

    @Test
    @DisplayName("Should return correct MEDIUM properties")
    void testMediumProperties() {
        Asteroid.AsteroidSize size = Asteroid.AsteroidSize.MEDIUM;
        assertEquals(1, size.getBaseMineralYield());
        assertEquals(1, size.getBaseOutpostSlots());
        assertEquals(0.3, size.getMovementPenalty());
    }

    @Test
    @DisplayName("Should return correct LARGE properties")
    void testLargeProperties() {
        Asteroid.AsteroidSize size = Asteroid.AsteroidSize.LARGE;
        assertEquals(2, size.getBaseMineralYield());
        assertEquals(2, size.getBaseOutpostSlots());
        assertEquals(0.4, size.getMovementPenalty());
    }

    @Test
    @DisplayName("Should return correct PROTOPLANET properties")
    void testProtoplanetProperties() {
        Asteroid.AsteroidSize size = Asteroid.AsteroidSize.PROTOPLANET;
        assertEquals(3, size.getBaseMineralYield());
        assertEquals(3, size.getBaseOutpostSlots());
        assertEquals(0.5, size.getMovementPenalty());
    }

    // ---- Accessor tests ----

    @Test
    @DisplayName("Should set and get name")
    void testSetName() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setName("Ceres");
        assertEquals("Ceres", asteroid.getName());
    }

    @Test
    @DisplayName("Should set and get asteroid size")
    void testSetAsteroidSize() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setAsteroidSize(Asteroid.AsteroidSize.LARGE);
        assertEquals(Asteroid.AsteroidSize.LARGE, asteroid.getAsteroidSize());
        assertEquals(2, asteroid.getMineralYield());
        assertEquals(2, asteroid.getOutpostSlots());
        assertEquals(0.4, asteroid.getMovementPenalty());
    }

    @Test
    @DisplayName("Should throw on null asteroid size")
    void testSetAsteroidSizeNull() {
        Asteroid asteroid = new Asteroid(1);
        assertThrows(IllegalArgumentException.class, () -> asteroid.setAsteroidSize(null));
    }

    @Test
    @DisplayName("Should return hex count based on radius")
    void testSize() {
        Asteroid asteroid = new Asteroid(1, 0);
        assertTrue(asteroid.size() > 0);

        Asteroid largeAsteroid = new Asteroid(2, 2);
        assertTrue(largeAsteroid.size() > asteroid.size());
    }

    // ---- Resource tests ----

    @Test
    @DisplayName("Should set and get mineral yield")
    void testSetMineralYield() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setMineralYield(5);
        assertEquals(5, asteroid.getMineralYield());
    }

    @Test
    @DisplayName("Should throw on negative mineral yield")
    void testSetMineralYieldNegative() {
        Asteroid asteroid = new Asteroid(1);
        assertThrows(IllegalArgumentException.class, () -> asteroid.setMineralYield(-1));
    }

    @Test
    @DisplayName("Should calculate effective mineral yield with no outposts")
    void testEffectiveMineralYieldNoOutposts() {
        Asteroid asteroid = new Asteroid(1);
        assertEquals(1, asteroid.getEffectiveMineralYield());
    }

    @Test
    @DisplayName("Should calculate effective mineral yield with one outpost")
    void testEffectiveMineralYieldOneOutpost() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.addOutpost("Mining-1");
        // 1 * (1.0 + 0.2) = 1.2, rounded to 1
        assertEquals(1, asteroid.getEffectiveMineralYield());
    }

    @Test
    @DisplayName("Should calculate effective mineral yield with high base")
    void testEffectiveMineralYieldHighBase() {
        Asteroid asteroid = new Asteroid(1, Asteroid.AsteroidSize.PROTOPLANET);
        asteroid.addOutpost("Mining-1");
        asteroid.addOutpost("Mining-2");
        // 3 * (1.0 + 0.4) = 4.2, rounded to 4
        assertEquals(4, asteroid.getEffectiveMineralYield());
    }

    // ---- Outpost management tests ----

    @Test
    @DisplayName("Should set and get outpost slots")
    void testSetOutpostSlots() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setOutpostSlots(5);
        assertEquals(5, asteroid.getOutpostSlots());
    }

    @Test
    @DisplayName("Should throw on negative outpost slots")
    void testSetOutpostSlotsNegative() {
        Asteroid asteroid = new Asteroid(1);
        assertThrows(IllegalArgumentException.class, () -> asteroid.setOutpostSlots(-1));
    }

    @Test
    @DisplayName("Should return available outpost slots")
    void testGetAvailableOutpostSlots() {
        Asteroid asteroid = new Asteroid(1);
        assertEquals(1, asteroid.getAvailableOutpostSlots());

        asteroid.addOutpost("Mining-1");
        assertEquals(0, asteroid.getAvailableOutpostSlots());
    }

    @Test
    @DisplayName("Should check available outpost slot")
    void testHasAvailableOutpostSlot() {
        Asteroid asteroid = new Asteroid(1);
        assertTrue(asteroid.hasAvailableOutpostSlot());

        asteroid.addOutpost("Mining-1");
        assertFalse(asteroid.hasAvailableOutpostSlot());
    }

    @Test
    @DisplayName("Should add outpost successfully")
    void testAddOutpost() {
        Asteroid asteroid = new Asteroid(1);
        assertTrue(asteroid.addOutpost("Mining-1"));
        assertEquals(1, asteroid.getOccupiedOutposts().size());
        assertTrue(asteroid.getOccupiedOutposts().contains("Mining-1"));
    }

    @Test
    @DisplayName("Should fail to add outpost when full")
    void testAddOutpostFull() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.addOutpost("Mining-1");
        assertFalse(asteroid.addOutpost("Mining-2"));
    }

    @Test
    @DisplayName("Should throw on null outpost name")
    void testAddOutpostNull() {
        Asteroid asteroid = new Asteroid(1);
        assertThrows(IllegalArgumentException.class, () -> asteroid.addOutpost(null));
    }

    @Test
    @DisplayName("Should throw on empty outpost name")
    void testAddOutpostEmpty() {
        Asteroid asteroid = new Asteroid(1);
        assertThrows(IllegalArgumentException.class, () -> asteroid.addOutpost(""));
    }

    @Test
    @DisplayName("Should throw on whitespace outpost name")
    void testAddOutpostWhitespace() {
        Asteroid asteroid = new Asteroid(1);
        assertThrows(IllegalArgumentException.class, () -> asteroid.addOutpost("   "));
    }

    @Test
    @DisplayName("Should remove outpost")
    void testRemoveOutpost() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.addOutpost("Mining-1");
        assertTrue(asteroid.removeOutpost("Mining-1"));
        assertEquals(0, asteroid.getOccupiedOutposts().size());
    }

    @Test
    @DisplayName("Should fail to remove non-existent outpost")
    void testRemoveOutpostNotFound() {
        Asteroid asteroid = new Asteroid(1);
        assertFalse(asteroid.removeOutpost("NonExistent"));
    }

    @Test
    @DisplayName("Should return unmodifiable outpost list")
    void testGetOccupiedOutpostsUnmodifiable() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.addOutpost("Mining-1");
        assertThrows(UnsupportedOperationException.class, () -> asteroid.getOccupiedOutposts().add("Mining-2"));
    }

    @Test
    @DisplayName("Should clear all outposts")
    void testClearOutposts() {
        Asteroid asteroid = new Asteroid(1, Asteroid.AsteroidSize.PROTOPLANET);
        asteroid.addOutpost("Mining-1");
        asteroid.addOutpost("Mining-2");
        asteroid.clearOutposts();
        assertEquals(0, asteroid.getOccupiedOutposts().size());
        assertEquals(3, asteroid.getAvailableOutpostSlots());
    }

    @Test
    @DisplayName("Should not allow outposts on PEBBLE size")
    void testPebbleNoOutposts() {
        Asteroid asteroid = new Asteroid(1, Asteroid.AsteroidSize.PEBBLE);
        assertEquals(0, asteroid.getOutpostSlots());
        assertFalse(asteroid.hasAvailableOutpostSlot());
        assertFalse(asteroid.addOutpost("Mining-1"));
    }

    // ---- Hazard tests ----

    @Test
    @DisplayName("Should set and get movement penalty")
    void testSetMovementPenalty() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setMovementPenalty(0.7);
        assertEquals(0.7, asteroid.getMovementPenalty());
    }

    @Test
    @DisplayName("Should throw on negative movement penalty")
    void testSetMovementPenaltyNegative() {
        Asteroid asteroid = new Asteroid(1);
        assertThrows(IllegalArgumentException.class, () -> asteroid.setMovementPenalty(-0.1));
    }

    @Test
    @DisplayName("Should throw on movement penalty over 1.0")
    void testSetMovementPenaltyOverOne() {
        Asteroid asteroid = new Asteroid(1);
        assertThrows(IllegalArgumentException.class, () -> asteroid.setMovementPenalty(1.5));
    }

    @Test
    @DisplayName("Should allow zero movement penalty")
    void testSetMovementPenaltyZero() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setMovementPenalty(0.0);
        assertEquals(0.0, asteroid.getMovementPenalty());
    }

    @Test
    @DisplayName("Should allow 1.0 movement penalty")
    void testSetMovementPenaltyOne() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setMovementPenalty(1.0);
        assertEquals(1.0, asteroid.getMovementPenalty());
    }

    @Test
    @DisplayName("Should calculate effective movement speed")
    void testGetEffectiveMovementSpeed() {
        Asteroid asteroid = new Asteroid(1);
        // Default penalty 0.3, base speed 10.0
        assertEquals(7.0, asteroid.getEffectiveMovementSpeed(10.0));
    }

    @Test
    @DisplayName("Should return full speed with zero penalty")
    void testGetEffectiveMovementSpeedZeroPenalty() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setMovementPenalty(0.0);
        assertEquals(10.0, asteroid.getEffectiveMovementSpeed(10.0));
    }

    @Test
    @DisplayName("Should return zero speed with 1.0 penalty")
    void testGetEffectiveMovementSpeedFullPenalty() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setMovementPenalty(1.0);
        assertEquals(0.0, asteroid.getEffectiveMovementSpeed(10.0));
    }

    // ---- Strategic resource tests ----

    @Test
    @DisplayName("Should set and get strategic resource")
    void testSetStrategicResource() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setStrategicResource(StrategicResource.ELERIUM);
        assertEquals(StrategicResource.ELERIUM, asteroid.getStrategicResource());
        assertTrue(asteroid.hasStrategicDeposit());
    }

    @Test
    @DisplayName("Should clear strategic resource")
    void testClearStrategicResource() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setStrategicResource(StrategicResource.ELERIUM);
        asteroid.setStrategicResource(null);
        assertNull(asteroid.getStrategicResource());
        assertFalse(asteroid.hasStrategicDeposit());
    }

    @Test
    @DisplayName("Should set hasStrategicDeposit flag")
    void testSetHasStrategicDeposit() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setHasStrategicDeposit(true);
        assertTrue(asteroid.hasStrategicDeposit());
    }

    // ---- Parent belt relationship tests ----

    @Test
    @DisplayName("Should set and get parent belt ID")
    void testSetParentBeltId() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setParentBeltId(100);
        assertEquals(100, asteroid.getParentBeltId());
        assertTrue(asteroid.belongsToBelt());
    }

    @Test
    @DisplayName("Should detach from parent belt")
    void testDetachFromBelt() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setParentBeltId(100);
        asteroid.setParentBeltId(null);
        assertNull(asteroid.getParentBeltId());
        assertFalse(asteroid.belongsToBelt());
    }

    @Test
    @DisplayName("Should not belong to belt by default")
    void testNoParentBeltByDefault() {
        Asteroid asteroid = new Asteroid(1);
        assertNull(asteroid.getParentBeltId());
        assertFalse(asteroid.belongsToBelt());
    }

    // ---- Hex3DMap operations tests ----

    @Test
    @DisplayName("Should contain center hex")
    void testContainsCenter() {
        Asteroid asteroid = new Asteroid(1);
        assertTrue(asteroid.contains(0, 0));
    }

    @Test
    @DisplayName("Should contain hex within radius")
    void testContainsWithinRadius() {
        Asteroid asteroid = new Asteroid(1, 2);
        assertTrue(asteroid.contains(1, 0));
    }

    @Test
    @DisplayName("Should not contain hex outside radius")
    void testContainsOutsideRadius() {
        Asteroid asteroid = new Asteroid(1, 1);
        assertFalse(asteroid.contains(5, 0));
    }

    @Test
    @DisplayName("Should return all hex cells")
    void testGetAll() {
        Asteroid asteroid = new Asteroid(1);
        Collection<Hex3D> all = asteroid.getAll();
        assertTrue(all.size() > 0);
        assertTrue(all.contains(new Hex3D(0, 0, 0)));
    }

    @Test
    @DisplayName("Should return unmodifiable collection")
    void testGetAllUnmodifiable() {
        Asteroid asteroid = new Asteroid(1);
        assertThrows(UnsupportedOperationException.class, () -> asteroid.getAll().add(new Hex3D(1, 0, -1)));
    }

    @Test
    @DisplayName("Should get hex by coordinate")
    void testGetHex() {
        Asteroid asteroid = new Asteroid(1);
        Hex3D center = new Hex3D(0, 0, 0);
        assertNotNull(asteroid.get(center));
    }

    @Test
    @DisplayName("Should return null for hex not in asteroid")
    void testGetHexNotFound() {
        Asteroid asteroid = new Asteroid(1);
        assertNull(asteroid.get(new Hex3D(100, 0, -100)));
    }

    @Test
    @DisplayName("Should return neighbors of center hex")
    void testGetNeighbors() {
        Asteroid asteroid = new Asteroid(1, 2);
        List<Hex3D> neighbors = asteroid.getNeighbors(new Hex3D(0, 0, 0));
        assertTrue(neighbors.size() > 0);
    }

    @Test
    @DisplayName("Should return map reference")
    void testGetMap() {
        Asteroid asteroid = new Asteroid(1);
        assertNotNull(asteroid.getMap());
        assertTrue(asteroid.getMap().size() > 0);
    }

    // ---- Object contract tests ----

    @Test
    @DisplayName("Should be equal to self")
    void testEqualsSelf() {
        Asteroid asteroid = new Asteroid(1);
        assertEquals(asteroid, asteroid);
    }

    @Test
    @DisplayName("Should be equal to asteroid with same ID")
    void testEqualsSameId() {
        Asteroid a1 = new Asteroid(1);
        Asteroid a2 = new Asteroid(1);
        assertEquals(a1, a2);
    }

    @Test
    @DisplayName("Should not be equal to asteroid with different ID")
    void testEqualsDifferentId() {
        Asteroid a1 = new Asteroid(1);
        Asteroid a2 = new Asteroid(2);
        assertNotEquals(a1, a2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void testEqualsNull() {
        Asteroid asteroid = new Asteroid(1);
        assertNotEquals(null, asteroid);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void testEqualsDifferentClass() {
        Asteroid asteroid = new Asteroid(1);
        assertNotEquals(asteroid, "Asteroid-1");
    }

    @Test
    @DisplayName("Should have consistent hash code")
    void testHashCode() {
        Asteroid a1 = new Asteroid(1);
        Asteroid a2 = new Asteroid(1);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    // ---- toString tests ----

    @Test
    @DisplayName("Should return formatted string")
    void testToString() {
        Asteroid asteroid = new Asteroid(1);
        String str = asteroid.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("name='Asteroid-1'"));
        assertTrue(str.contains("size=MEDIUM"));
        assertTrue(str.contains("minerals=1"));
        assertTrue(str.contains("outposts=0/1"));
        assertTrue(str.contains("movementPenalty=30%"));
        assertTrue(str.contains("strategic=none"));
    }

    @Test
    @DisplayName("Should include strategic resource in toString")
    void testToStringWithStrategicResource() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setStrategicResource(StrategicResource.ELERIUM);
        String str = asteroid.toString();

        assertTrue(str.contains("strategic=ELERIUM"));
    }

    // ---- Integration tests ----

    @Test
    @DisplayName("Should support full mining operation scenario")
    void testFullMiningScenario() {
        Asteroid asteroid = new Asteroid(1, Asteroid.AsteroidSize.LARGE);
        asteroid.setName("Vesta");
        asteroid.setStrategicResource(StrategicResource.DURANTIUM);

        assertEquals("Vesta", asteroid.getName());
        assertEquals(2, asteroid.getMineralYield());
        assertEquals(2, asteroid.getOutpostSlots());

        assertTrue(asteroid.addOutpost("Mining-Alpha"));
        assertTrue(asteroid.addOutpost("Mining-Beta"));
        assertFalse(asteroid.addOutpost("Mining-Gamma"));

        assertEquals(2, asteroid.getOccupiedOutposts().size());
        assertEquals(0, asteroid.getAvailableOutpostSlots());

        // 2 * (1.0 + 0.4) = 2.8, rounded to 3
        assertEquals(3, asteroid.getEffectiveMineralYield());
        assertEquals(StrategicResource.DURANTIUM, asteroid.getStrategicResource());
    }

    @Test
    @DisplayName("Should support asteroid belt membership")
    void testAsteroidBeltMembership() {
        Asteroid asteroid = new Asteroid(1);
        asteroid.setParentBeltId(50);

        assertTrue(asteroid.belongsToBelt());
        assertEquals(50, asteroid.getParentBeltId());

        asteroid.setParentBeltId(null);
        assertFalse(asteroid.belongsToBelt());
    }

    @Test
    @DisplayName("Should support navigation hazard scenario")
    void testNavigationHazardScenario() {
        Asteroid asteroid = new Asteroid(1, Asteroid.AsteroidSize.PROTOPLANET);

        // Protoplanet has 0.5 movement penalty
        assertEquals(0.5, asteroid.getMovementPenalty());

        // Ship with speed 20.0
        double effectiveSpeed = asteroid.getEffectiveMovementSpeed(20.0);
        assertEquals(10.0, effectiveSpeed);

        // Adjust penalty
        asteroid.setMovementPenalty(0.2);
        effectiveSpeed = asteroid.getEffectiveMovementSpeed(20.0);
        assertEquals(16.0, effectiveSpeed);
    }

    @Test
    @DisplayName("Should support all asteroid sizes")
    void testAllAsteroidSizes() {
        Asteroid pebble = new Asteroid(1, Asteroid.AsteroidSize.PEBBLE);
        assertEquals(0, pebble.getMineralYield());
        assertEquals(0, pebble.getOutpostSlots());

        Asteroid small = new Asteroid(2, Asteroid.AsteroidSize.SMALL);
        assertEquals(1, small.getMineralYield());
        assertEquals(0, small.getOutpostSlots());

        Asteroid medium = new Asteroid(3, Asteroid.AsteroidSize.MEDIUM);
        assertEquals(1, medium.getMineralYield());
        assertEquals(1, medium.getOutpostSlots());

        Asteroid large = new Asteroid(4, Asteroid.AsteroidSize.LARGE);
        assertEquals(2, large.getMineralYield());
        assertEquals(2, large.getOutpostSlots());

        Asteroid protoplanet = new Asteroid(5, Asteroid.AsteroidSize.PROTOPLANET);
        assertEquals(3, protoplanet.getMineralYield());
        assertEquals(3, protoplanet.getOutpostSlots());
    }
}
