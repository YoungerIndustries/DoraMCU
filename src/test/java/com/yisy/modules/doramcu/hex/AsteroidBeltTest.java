package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AsteroidBelt Tests")
class AsteroidBeltTest {

    @Test
    @DisplayName("Should create asteroid belt with default radius")
    void testDefaultConstructor() {
        AsteroidBelt belt = new AsteroidBelt(1);
        assertEquals(1, belt.getId());
        assertEquals(AsteroidBelt.DEFAULT_RADIUS, belt.getRadius());
        assertEquals("AsteroidBelt-1", belt.getName());
        assertEquals(AsteroidBelt.DEFAULT_MINERAL_YIELD, belt.getMineralYield());
        assertEquals(AsteroidBelt.DEFAULT_STATION_SLOTS, belt.getStationSlots());
        assertEquals(AsteroidBelt.DEFAULT_MOVEMENT_PENALTY, belt.getMovementPenalty());
        assertFalse(belt.hasStrategicDeposit());
        assertNull(belt.getStrategicResource());
        assertTrue(belt.size() > 0);
    }

    @Test
    @DisplayName("Should create asteroid belt with custom radius")
    void testCustomRadiusConstructor() {
        AsteroidBelt belt = new AsteroidBelt(1, 15);
        assertEquals(15, belt.getRadius());
        assertTrue(belt.size() > 0);
    }

    @Test
    @DisplayName("Should throw on negative radius")
    void testNegativeRadius() {
        assertThrows(IllegalArgumentException.class, () -> new AsteroidBelt(1, -1));
    }

    @Test
    @DisplayName("Should set and get name")
    void testName() {
        AsteroidBelt belt = new AsteroidBelt(1);
        belt.setName("Kuiper Belt");
        assertEquals("Kuiper Belt", belt.getName());
    }

    @Test
    @DisplayName("Should set and get mineral yield")
    void testMineralYield() {
        AsteroidBelt belt = new AsteroidBelt(1);
        belt.setMineralYield(10);
        assertEquals(10, belt.getMineralYield());
    }

    @Test
    @DisplayName("Should throw on negative mineral yield")
    void testNegativeMineralYield() {
        AsteroidBelt belt = new AsteroidBelt(1);
        assertThrows(IllegalArgumentException.class, () -> belt.setMineralYield(-1));
    }

    @Test
    @DisplayName("Should calculate effective mineral yield with station bonus")
    void testEffectiveMineralYield() {
        AsteroidBelt belt = new AsteroidBelt(1);
        belt.setMineralYield(10);
        assertEquals(10, belt.getEffectiveMineralYield());

        belt.addStation("Miner-1");
        assertEquals(12, belt.getEffectiveMineralYield()); // 10 * 1.2

        belt.addStation("Miner-2");
        assertEquals(14, belt.getEffectiveMineralYield()); // 10 * 1.4
    }

    @Test
    @DisplayName("Should manage station slots")
    void testStationSlots() {
        AsteroidBelt belt = new AsteroidBelt(1);
        assertEquals(3, belt.getStationSlots());
        assertEquals(3, belt.getAvailableStationSlots());
        assertTrue(belt.hasAvailableStationSlot());

        belt.addStation("Station-1");
        assertEquals(2, belt.getAvailableStationSlots());

        belt.addStation("Station-2");
        belt.addStation("Station-3");
        assertEquals(0, belt.getAvailableStationSlots());
        assertFalse(belt.hasAvailableStationSlot());

        assertFalse(belt.addStation("Station-4"));
    }

    @Test
    @DisplayName("Should set and get station slots")
    void testSetStationSlots() {
        AsteroidBelt belt = new AsteroidBelt(1);
        belt.setStationSlots(5);
        assertEquals(5, belt.getStationSlots());
    }

    @Test
    @DisplayName("Should throw on negative station slots")
    void testNegativeStationSlots() {
        AsteroidBelt belt = new AsteroidBelt(1);
        assertThrows(IllegalArgumentException.class, () -> belt.setStationSlots(-1));
    }

    @Test
    @DisplayName("Should remove station")
    void testRemoveStation() {
        AsteroidBelt belt = new AsteroidBelt(1);
        belt.addStation("Station-1");
        assertTrue(belt.removeStation("Station-1"));
        assertEquals(0, belt.getOccupiedStations().size());
        assertFalse(belt.removeStation("NonExistent"));
    }

    @Test
    @DisplayName("Should return unmodifiable occupied stations list")
    void testOccupiedStationsUnmodifiable() {
        AsteroidBelt belt = new AsteroidBelt(1);
        belt.addStation("Station-1");
        assertThrows(UnsupportedOperationException.class, () -> belt.getOccupiedStations().add("Station-2"));
    }

    @Test
    @DisplayName("Should set and get movement penalty")
    void testMovementPenalty() {
        AsteroidBelt belt = new AsteroidBelt(1);
        assertEquals(0.5, belt.getMovementPenalty());
        belt.setMovementPenalty(0.75);
        assertEquals(0.75, belt.getMovementPenalty());
    }

    @Test
    @DisplayName("Should throw on invalid movement penalty")
    void testInvalidMovementPenalty() {
        AsteroidBelt belt = new AsteroidBelt(1);
        assertThrows(IllegalArgumentException.class, () -> belt.setMovementPenalty(-0.1));
        assertThrows(IllegalArgumentException.class, () -> belt.setMovementPenalty(1.1));
    }

    @Test
    @DisplayName("Should calculate effective movement speed")
    void testEffectiveMovementSpeed() {
        AsteroidBelt belt = new AsteroidBelt(1);
        belt.setMovementPenalty(0.5);
        assertEquals(5, belt.getEffectiveMovementSpeed(10));
        assertEquals(3, belt.getEffectiveMovementSpeed(6));
        assertEquals(1, belt.getEffectiveMovementSpeed(1)); // minimum 1
    }

    @Test
    @DisplayName("Should manage strategic resource deposit")
    void testStrategicDeposit() {
        AsteroidBelt belt = new AsteroidBelt(1);
        assertFalse(belt.hasStrategicDeposit());
        assertNull(belt.getStrategicResource());

        belt.setStrategicResource(StrategicResource.DURANTIUM);
        assertTrue(belt.hasStrategicDeposit());
        assertEquals(StrategicResource.DURANTIUM, belt.getStrategicResource());

        belt.setStrategicResource(null);
        assertFalse(belt.hasStrategicDeposit());
        assertNull(belt.getStrategicResource());
    }

    @Test
    @DisplayName("Should contain hexes within radius")
    void testContains() {
        AsteroidBelt belt = new AsteroidBelt(1, 5);
        assertTrue(belt.contains(0, 0));
        assertTrue(belt.contains(5, -5));
        assertFalse(belt.contains(10, -10));
    }

    @Test
    @DisplayName("Should get hex by coordinates")
    void testGetHex() {
        AsteroidBelt belt = new AsteroidBelt(1, 5);
        Hex3D center = belt.get(0, 0);
        assertNotNull(center);
        assertEquals(0, center.getX());
        assertNull(belt.get(10, -10));
    }

    @Test
    @DisplayName("Should return all hexes")
    void testGetAll() {
        AsteroidBelt belt = new AsteroidBelt(1, 3);
        assertTrue(belt.getAll().size() > 0);
    }

    @Test
    @DisplayName("Should return outer edge hexes")
    void testOuterEdge() {
        AsteroidBelt belt = new AsteroidBelt(1, 5);
        List<Hex3D> edge = belt.getOuterEdge();
        assertTrue(edge.size() > 0);
        Hex3D center = new Hex3D(0, 0, 0);
        for (Hex3D hex : edge) {
            assertEquals(5, center.distance(hex));
        }
    }

    @Test
    @DisplayName("Should return center hex")
    void testCenter() {
        AsteroidBelt belt = new AsteroidBelt(1);
        Hex3D center = belt.getCenter();
        assertNotNull(center);
        assertEquals(0, center.getX());
        assertEquals(0, center.getY());
        assertEquals(0, center.getZ());
    }

    @Test
    @DisplayName("Should find hexes by state")
    void testFindByState() {
        AsteroidBelt belt = new AsteroidBelt(1, 3);
        List<Hex3D> empty = belt.findByState(HexState.EMPTY);
        assertTrue(empty.size() > 0);
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
        AsteroidBelt belt1 = new AsteroidBelt(1);
        AsteroidBelt belt2 = new AsteroidBelt(1);
        AsteroidBelt belt3 = new AsteroidBelt(2);

        assertEquals(belt1, belt2);
        assertEquals(belt1.hashCode(), belt2.hashCode());
        assertNotEquals(belt1, belt3);
    }

    @Test
    @DisplayName("Should have correct toString format")
    void testToString() {
        AsteroidBelt belt = new AsteroidBelt(1);
        belt.setName("TestBelt");
        String str = belt.toString();
        assertTrue(str.contains("name='TestBelt'"));
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("minerals=5"));
    }

    @Test
    @DisplayName("Should integrate with SolarSystemEntity")
    void testSolarSystemIntegration() {
        SolarSystemEntity system = new SolarSystemEntity(1);
        AsteroidBelt belt = new AsteroidBelt(1);
        belt.setName("Kuiper Belt");

        system.addAsteroidBelt(belt, new Hex3D(20, -20, 0));
        assertEquals(1, system.getAsteroidBeltCount());
        assertEquals(belt, system.getAsteroidBelt(1));
        assertTrue(system.getTotalHexCount() > 0);

        SystemBody body = system.getAt(new Hex3D(20, -20, 0));
        assertNotNull(body);
        assertEquals(SystemBody.BodyType.ASTEROID_BELT, body.getType());
    }
}
