package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GasGiant Tests")
class GasGiantTest {

    @Test
    @DisplayName("Should create gas giant with default radius")
    void testDefaultConstructor() {
        GasGiant giant = new GasGiant(1);
        assertEquals(1, giant.getId());
        assertEquals(GasGiant.DEFAULT_RADIUS, giant.getRadius());
        assertEquals("GasGiant-1", giant.getName());
        assertEquals(GasGiant.DEFAULT_ENERGY_YIELD, giant.getEnergyYield());
        assertEquals(GasGiant.DEFAULT_STATION_SLOTS, giant.getStationSlots());
        assertEquals(GasGiant.DEFAULT_GRAVITATIONAL_PULL, giant.getGravitationalPull());
        assertFalse(giant.hasStrategicDeposit());
        assertTrue(giant.size() > 0);
    }

    @Test
    @DisplayName("Should create gas giant with custom radius")
    void testCustomRadiusConstructor() {
        GasGiant giant = new GasGiant(1, 20);
        assertEquals(20, giant.getRadius());
    }

    @Test
    @DisplayName("Should throw on negative radius")
    void testNegativeRadius() {
        assertThrows(IllegalArgumentException.class, () -> new GasGiant(1, -1));
    }

    @Test
    @DisplayName("Should set and get name")
    void testName() {
        GasGiant giant = new GasGiant(1);
        giant.setName("Jupiter");
        assertEquals("Jupiter", giant.getName());
    }

    @Test
    @DisplayName("Should set and get energy yield")
    void testEnergyYield() {
        GasGiant giant = new GasGiant(1);
        giant.setEnergyYield(15);
        assertEquals(15, giant.getEnergyYield());
    }

    @Test
    @DisplayName("Should throw on negative energy yield")
    void testNegativeEnergyYield() {
        GasGiant giant = new GasGiant(1);
        assertThrows(IllegalArgumentException.class, () -> giant.setEnergyYield(-1));
    }

    @Test
    @DisplayName("Should calculate effective energy yield with station bonus")
    void testEffectiveEnergyYield() {
        GasGiant giant = new GasGiant(1);
        giant.setEnergyYield(10);
        assertEquals(10, giant.getEffectiveEnergyYield());

        giant.addStation("Platform-1");
        assertEquals(12, giant.getEffectiveEnergyYield()); // 10 * 1.15

        giant.addStation("Platform-2");
        assertEquals(13, giant.getEffectiveEnergyYield()); // 10 * 1.3
    }

    @Test
    @DisplayName("Should manage atmospheric resources")
    void testAtmosphericResources() {
        GasGiant giant = new GasGiant(1);
        assertTrue(giant.addAtmosphericResource("Helium-3"));
        assertTrue(giant.addAtmosphericResource("ExoticGas"));
        assertTrue(giant.hasAtmosphericResource("Helium-3"));
        assertFalse(giant.hasAtmosphericResource("Unknown"));
        assertEquals(2, giant.getAtmosphericResources().size());

        assertTrue(giant.removeAtmosphericResource("Helium-3"));
        assertFalse(giant.hasAtmosphericResource("Helium-3"));
    }

    @Test
    @DisplayName("Should return unmodifiable atmospheric resources set")
    void testAtmosphericResourcesUnmodifiable() {
        GasGiant giant = new GasGiant(1);
        giant.addAtmosphericResource("Helium-3");
        assertThrows(UnsupportedOperationException.class, () -> giant.getAtmosphericResources().add("Test"));
    }

    @Test
    @DisplayName("Should manage station slots")
    void testStationSlots() {
        GasGiant giant = new GasGiant(1);
        assertEquals(4, giant.getStationSlots());
        assertTrue(giant.hasAvailableStationSlot());

        for (int i = 1; i <= 4; i++) {
            giant.addStation("Station-" + i);
        }
        assertFalse(giant.hasAvailableStationSlot());
        assertFalse(giant.addStation("Station-5"));
    }

    @Test
    @DisplayName("Should set and get station slots")
    void testSetStationSlots() {
        GasGiant giant = new GasGiant(1);
        giant.setStationSlots(6);
        assertEquals(6, giant.getStationSlots());
    }

    @Test
    @DisplayName("Should throw on negative station slots")
    void testNegativeStationSlots() {
        GasGiant giant = new GasGiant(1);
        assertThrows(IllegalArgumentException.class, () -> giant.setStationSlots(-1));
    }

    @Test
    @DisplayName("Should remove station")
    void testRemoveStation() {
        GasGiant giant = new GasGiant(1);
        giant.addStation("Station-1");
        assertTrue(giant.removeStation("Station-1"));
        assertEquals(0, giant.getOccupiedStations().size());
    }

    @Test
    @DisplayName("Should set and get gravitational pull")
    void testGravitationalPull() {
        GasGiant giant = new GasGiant(1);
        assertEquals(0.3, giant.getGravitationalPull());
        giant.setGravitationalPull(0.5);
        assertEquals(0.5, giant.getGravitationalPull());
    }

    @Test
    @DisplayName("Should throw on invalid gravitational pull")
    void testInvalidGravitationalPull() {
        GasGiant giant = new GasGiant(1);
        assertThrows(IllegalArgumentException.class, () -> giant.setGravitationalPull(-0.1));
        assertThrows(IllegalArgumentException.class, () -> giant.setGravitationalPull(1.1));
    }

    @Test
    @DisplayName("Should calculate effective movement speed")
    void testEffectiveMovementSpeed() {
        GasGiant giant = new GasGiant(1);
        giant.setGravitationalPull(0.3);
        assertEquals(7, giant.getEffectiveMovementSpeed(10)); // 10 * 0.7
        assertEquals(1, giant.getEffectiveMovementSpeed(1)); // minimum 1
    }

    @Test
    @DisplayName("Should calculate slingshot bonus")
    void testSlingshotBonus() {
        GasGiant giant = new GasGiant(1);
        giant.setGravitationalPull(0.3);
        assertEquals(3, giant.getSlingshotBonus(10)); // 10 * 0.3
    }

    @Test
    @DisplayName("Should manage strategic resource deposit")
    void testStrategicDeposit() {
        GasGiant giant = new GasGiant(1);
        assertFalse(giant.hasStrategicDeposit());
        giant.setStrategicResource(StrategicResource.PROMETHION);
        assertTrue(giant.hasStrategicDeposit());
        assertEquals(StrategicResource.PROMETHION, giant.getStrategicResource());
    }

    @Test
    @DisplayName("Should return atmosphere hexes")
    void testAtmosphere() {
        GasGiant giant = new GasGiant(1, 12);
        List<Hex3D> atmosphere = giant.getAtmosphere();
        assertTrue(atmosphere.size() > 0);
        Hex3D center = new Hex3D(0, 0, 0);
        int atmosphereInner = (int) Math.floor(12 * 0.75);
        for (Hex3D hex : atmosphere) {
            int dist = center.distance(hex);
            assertTrue(dist > atmosphereInner && dist <= 12);
        }
    }

    @Test
    @DisplayName("Should return core hexes")
    void testCore() {
        GasGiant giant = new GasGiant(1, 12);
        List<Hex3D> core = giant.getCore();
        assertTrue(core.size() > 0);
        Hex3D center = new Hex3D(0, 0, 0);
        int coreRadius = (int) Math.floor(12 * 0.25);
        for (Hex3D hex : core) {
            assertTrue(center.distance(hex) <= coreRadius);
        }
    }

    @Test
    @DisplayName("Should find hexes by state")
    void testFindByState() {
        GasGiant giant = new GasGiant(1, 3);
        List<Hex3D> empty = giant.findByState(HexState.EMPTY);
        assertTrue(empty.size() > 0);
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
        GasGiant g1 = new GasGiant(1);
        GasGiant g2 = new GasGiant(1);
        GasGiant g3 = new GasGiant(2);
        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());
        assertNotEquals(g1, g3);
    }

    @Test
    @DisplayName("Should have correct toString format")
    void testToString() {
        GasGiant giant = new GasGiant(1);
        giant.setName("Jupiter");
        String str = giant.toString();
        assertTrue(str.contains("name='Jupiter'"));
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("energy=8"));
    }

    @Test
    @DisplayName("Should integrate with SolarSystemEntity")
    void testSolarSystemIntegration() {
        SolarSystemEntity system = new SolarSystemEntity(1);
        GasGiant giant = new GasGiant(1);
        giant.setName("Jupiter");

        system.addGasGiant(giant, new Hex3D(30, -30, 0));
        assertEquals(1, system.getGasGiantCount());
        assertEquals(giant, system.getGasGiant(1));

        SystemBody body = system.getAt(new Hex3D(30, -30, 0));
        assertNotNull(body);
        assertEquals(SystemBody.BodyType.GAS_GIANT, body.getType());
    }
}
