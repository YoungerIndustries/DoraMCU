package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Nebula Tests")
class NebulaTest {

    // ---- NebulaSize enum tests ----

    @Test
    @DisplayName("Should have 5 NebulaSize enum values")
    void testNebulaSizeValues() {
        Nebula.NebulaSize[] values = Nebula.NebulaSize.values();
        assertEquals(5, values.length);
    }

    @Test
    @DisplayName("Should have correct TINY nebula size properties")
    void testNebulaSizeTiny() {
        Nebula.NebulaSize size = Nebula.NebulaSize.TINY;
        assertEquals(3, size.getBaseRadius());
        assertEquals(1, size.getBaseEleriumYield());
        assertEquals(1, size.getBaseStationSlots());
        assertEquals(0.2, size.getBaseSensorJamming());
        assertEquals(0.3, size.getBaseCloakingBonus());
    }

    @Test
    @DisplayName("Should have correct SMALL nebula size properties")
    void testNebulaSizeSmall() {
        Nebula.NebulaSize size = Nebula.NebulaSize.SMALL;
        assertEquals(5, size.getBaseRadius());
        assertEquals(2, size.getBaseEleriumYield());
        assertEquals(1, size.getBaseStationSlots());
        assertEquals(0.3, size.getBaseSensorJamming());
        assertEquals(0.4, size.getBaseCloakingBonus());
    }

    @Test
    @DisplayName("Should have correct MEDIUM nebula size properties")
    void testNebulaSizeMedium() {
        Nebula.NebulaSize size = Nebula.NebulaSize.MEDIUM;
        assertEquals(10, size.getBaseRadius());
        assertEquals(3, size.getBaseEleriumYield());
        assertEquals(2, size.getBaseStationSlots());
        assertEquals(0.4, size.getBaseSensorJamming());
        assertEquals(0.6, size.getBaseCloakingBonus());
    }

    @Test
    @DisplayName("Should have correct LARGE nebula size properties")
    void testNebulaSizeLarge() {
        Nebula.NebulaSize size = Nebula.NebulaSize.LARGE;
        assertEquals(15, size.getBaseRadius());
        assertEquals(5, size.getBaseEleriumYield());
        assertEquals(3, size.getBaseStationSlots());
        assertEquals(0.5, size.getBaseSensorJamming());
        assertEquals(0.7, size.getBaseCloakingBonus());
    }

    @Test
    @DisplayName("Should have correct MASSIVE nebula size properties")
    void testNebulaSizeMassive() {
        Nebula.NebulaSize size = Nebula.NebulaSize.MASSIVE;
        assertEquals(20, size.getBaseRadius());
        assertEquals(8, size.getBaseEleriumYield());
        assertEquals(4, size.getBaseStationSlots());
        assertEquals(0.6, size.getBaseSensorJamming());
        assertEquals(0.8, size.getBaseCloakingBonus());
    }

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create nebula with default radius")
    void testDefaultConstructor() {
        Nebula nebula = new Nebula(1);
        assertEquals(1, nebula.getId());
        assertEquals(Nebula.DEFAULT_RADIUS, nebula.getRadius());
        assertEquals("Nebula-1", nebula.getName());
        assertEquals(Nebula.DEFAULT_ELERIUM_YIELD, nebula.getEleriumYield());
        assertEquals(Nebula.DEFAULT_STATION_SLOTS, nebula.getStationSlots());
        assertEquals(Nebula.DEFAULT_SENSOR_JAMMING, nebula.getSensorJamming());
        assertEquals(Nebula.DEFAULT_CLOAKING_BONUS, nebula.getCloakingBonus());
        assertTrue(nebula.hasStrategicDeposit());
        assertEquals(StrategicResource.ELERIUM, nebula.getStrategicResource());
        assertEquals(Nebula.NebulaSize.MEDIUM, nebula.getNebulaSize());
        assertTrue(nebula.size() > 0);
    }

    @Test
    @DisplayName("Should create nebula with NebulaSize")
    void testNebulaSizeConstructor() {
        Nebula nebula = new Nebula(1, Nebula.NebulaSize.LARGE);
        assertEquals(1, nebula.getId());
        assertEquals(15, nebula.getRadius());
        assertEquals(5, nebula.getEleriumYield());
        assertEquals(3, nebula.getStationSlots());
        assertEquals(0.5, nebula.getSensorJamming());
        assertEquals(0.7, nebula.getCloakingBonus());
        assertEquals(Nebula.NebulaSize.LARGE, nebula.getNebulaSize());
        assertTrue(nebula.size() > 0);
    }

    @Test
    @DisplayName("Should create nebula with TINY NebulaSize")
    void testTinyNebulaSizeConstructor() {
        Nebula nebula = new Nebula(1, Nebula.NebulaSize.TINY);
        assertEquals(3, nebula.getRadius());
        assertEquals(1, nebula.getEleriumYield());
        assertEquals(1, nebula.getStationSlots());
        assertEquals(0.2, nebula.getSensorJamming());
        assertEquals(0.3, nebula.getCloakingBonus());
    }

    @Test
    @DisplayName("Should create nebula with MASSIVE NebulaSize")
    void testMassiveNebulaSizeConstructor() {
        Nebula nebula = new Nebula(1, Nebula.NebulaSize.MASSIVE);
        assertEquals(20, nebula.getRadius());
        assertEquals(8, nebula.getEleriumYield());
        assertEquals(4, nebula.getStationSlots());
        assertEquals(0.6, nebula.getSensorJamming());
        assertEquals(0.8, nebula.getCloakingBonus());
    }

    @Test
    @DisplayName("Should throw on null NebulaSize")
    void testNullNebulaSize() {
        assertThrows(IllegalArgumentException.class, () -> new Nebula(1, null));
    }

    @Test
    @DisplayName("Should create nebula with custom radius")
    void testCustomRadiusConstructor() {
        Nebula nebula = new Nebula(1, 15);
        assertEquals(15, nebula.getRadius());
    }

    @Test
    @DisplayName("Should throw on negative radius")
    void testNegativeRadius() {
        assertThrows(IllegalArgumentException.class, () -> new Nebula(1, -1));
    }

    @Test
    @DisplayName("Should set and get name")
    void testName() {
        Nebula nebula = new Nebula(1);
        nebula.setName("Orion Nebula");
        assertEquals("Orion Nebula", nebula.getName());
    }

    @Test
    @DisplayName("Should set and get Elerium yield")
    void testEleriumYield() {
        Nebula nebula = new Nebula(1);
        nebula.setEleriumYield(10);
        assertEquals(10, nebula.getEleriumYield());
    }

    @Test
    @DisplayName("Should throw on negative Elerium yield")
    void testNegativeEleriumYield() {
        Nebula nebula = new Nebula(1);
        assertThrows(IllegalArgumentException.class, () -> nebula.setEleriumYield(-1));
    }

    @Test
    @DisplayName("Should calculate effective Elerium yield with station bonus")
    void testEffectiveEleriumYield() {
        Nebula nebula = new Nebula(1);
        nebula.setEleriumYield(4);
        assertEquals(4, nebula.getEffectiveEleriumYield());

        nebula.addStation("Extractor-1");
        assertEquals(5, nebula.getEffectiveEleriumYield()); // 4 * 1.25

        nebula.addStation("Extractor-2");
        assertEquals(6, nebula.getEffectiveEleriumYield()); // 4 * 1.5
    }

    @Test
    @DisplayName("Should manage station slots")
    void testStationSlots() {
        Nebula nebula = new Nebula(1);
        assertEquals(2, nebula.getStationSlots());
        assertTrue(nebula.hasAvailableStationSlot());

        nebula.addStation("Station-1");
        nebula.addStation("Station-2");
        assertFalse(nebula.hasAvailableStationSlot());
        assertFalse(nebula.addStation("Station-3"));
    }

    @Test
    @DisplayName("Should set and get station slots")
    void testSetStationSlots() {
        Nebula nebula = new Nebula(1);
        nebula.setStationSlots(4);
        assertEquals(4, nebula.getStationSlots());
    }

    @Test
    @DisplayName("Should throw on negative station slots")
    void testNegativeStationSlots() {
        Nebula nebula = new Nebula(1);
        assertThrows(IllegalArgumentException.class, () -> nebula.setStationSlots(-1));
    }

    @Test
    @DisplayName("Should remove station")
    void testRemoveStation() {
        Nebula nebula = new Nebula(1);
        nebula.addStation("Station-1");
        assertTrue(nebula.removeStation("Station-1"));
        assertEquals(0, nebula.getOccupiedStations().size());
    }

    @Test
    @DisplayName("Should set and get sensor jamming")
    void testSensorJamming() {
        Nebula nebula = new Nebula(1);
        assertEquals(0.4, nebula.getSensorJamming());
        nebula.setSensorJamming(0.6);
        assertEquals(0.6, nebula.getSensorJamming());
    }

    @Test
    @DisplayName("Should throw on invalid sensor jamming")
    void testInvalidSensorJamming() {
        Nebula nebula = new Nebula(1);
        assertThrows(IllegalArgumentException.class, () -> nebula.setSensorJamming(-0.1));
        assertThrows(IllegalArgumentException.class, () -> nebula.setSensorJamming(1.1));
    }

    @Test
    @DisplayName("Should calculate effective enemy accuracy")
    void testEffectiveEnemyAccuracy() {
        Nebula nebula = new Nebula(1);
        nebula.setSensorJamming(0.4);
        assertEquals(0.6, nebula.getEffectiveEnemyAccuracy(1.0)); // 1.0 * 0.6
        assertEquals(0.3, nebula.getEffectiveEnemyAccuracy(0.5)); // 0.5 * 0.6
        assertEquals(0.0, nebula.getEffectiveEnemyAccuracy(0.0));
    }

    @Test
    @DisplayName("Should set and get cloaking bonus")
    void testCloakingBonus() {
        Nebula nebula = new Nebula(1);
        assertEquals(0.6, nebula.getCloakingBonus());
        nebula.setCloakingBonus(0.8);
        assertEquals(0.8, nebula.getCloakingBonus());
    }

    @Test
    @DisplayName("Should throw on invalid cloaking bonus")
    void testInvalidCloakingBonus() {
        Nebula nebula = new Nebula(1);
        assertThrows(IllegalArgumentException.class, () -> nebula.setCloakingBonus(-0.1));
        assertThrows(IllegalArgumentException.class, () -> nebula.setCloakingBonus(1.1));
    }

    @Test
    @DisplayName("Should calculate effective evasion")
    void testEffectiveEvasion() {
        Nebula nebula = new Nebula(1);
        nebula.setCloakingBonus(0.6);
        assertEquals(0.8, nebula.getEffectiveEvasion(0.2)); // 0.2 + 0.6
        assertEquals(1.0, nebula.getEffectiveEvasion(0.5)); // capped at 1.0
    }

    // ---- NebulaSize setter tests ----

    @Test
    @DisplayName("Should get and set NebulaSize")
    void testSetNebulaSize() {
        Nebula nebula = new Nebula(1);
        assertEquals(Nebula.NebulaSize.MEDIUM, nebula.getNebulaSize());

        nebula.setNebulaSize(Nebula.NebulaSize.LARGE);
        assertEquals(Nebula.NebulaSize.LARGE, nebula.getNebulaSize());
        assertEquals(5, nebula.getEleriumYield());
        assertEquals(3, nebula.getStationSlots());
        assertEquals(0.5, nebula.getSensorJamming());
        assertEquals(0.7, nebula.getCloakingBonus());
    }

    @Test
    @DisplayName("Should update all properties when setting NebulaSize to TINY")
    void testSetNebulaSizeTiny() {
        Nebula nebula = new Nebula(1);
        nebula.setNebulaSize(Nebula.NebulaSize.TINY);
        assertEquals(1, nebula.getEleriumYield());
        assertEquals(1, nebula.getStationSlots());
        assertEquals(0.2, nebula.getSensorJamming());
        assertEquals(0.3, nebula.getCloakingBonus());
    }

    @Test
    @DisplayName("Should update all properties when setting NebulaSize to MASSIVE")
    void testSetNebulaSizeMassive() {
        Nebula nebula = new Nebula(1);
        nebula.setNebulaSize(Nebula.NebulaSize.MASSIVE);
        assertEquals(8, nebula.getEleriumYield());
        assertEquals(4, nebula.getStationSlots());
        assertEquals(0.6, nebula.getSensorJamming());
        assertEquals(0.8, nebula.getCloakingBonus());
    }

    @Test
    @DisplayName("Should throw on null NebulaSize setter")
    void testSetNebulaSizeNull() {
        Nebula nebula = new Nebula(1);
        assertThrows(IllegalArgumentException.class, () -> nebula.setNebulaSize(null));
    }

    // ---- Strategic resource tests ----

    @Test
    @DisplayName("Should manage strategic resource deposit")
    void testStrategicDeposit() {
        Nebula nebula = new Nebula(1);
        assertTrue(nebula.hasStrategicDeposit());
        assertEquals(StrategicResource.ELERIUM, nebula.getStrategicResource());

        nebula.setStrategicResource(null);
        assertFalse(nebula.hasStrategicDeposit());
        assertNull(nebula.getStrategicResource());
    }

    @Test
    @DisplayName("Should return core hexes")
    void testCore() {
        Nebula nebula = new Nebula(1, 10);
        List<Hex3D> core = nebula.getCore();
        assertTrue(core.size() > 0);
        Hex3D center = new Hex3D(0, 0, 0);
        int coreRadius = 10 / 2;
        for (Hex3D hex : core) {
            assertTrue(center.distance(hex) <= coreRadius);
        }
    }

    @Test
    @DisplayName("Should return edge hexes")
    void testEdge() {
        Nebula nebula = new Nebula(1, 10);
        List<Hex3D> edge = nebula.getEdge();
        assertTrue(edge.size() > 0);
        Hex3D center = new Hex3D(0, 0, 0);
        int coreRadius = 10 / 2;
        for (Hex3D hex : edge) {
            int dist = center.distance(hex);
            assertTrue(dist > coreRadius && dist <= 10);
        }
    }

    @Test
    @DisplayName("Should find hexes by state")
    void testFindByState() {
        Nebula nebula = new Nebula(1, 3);
        List<Hex3D> empty = nebula.findByState(HexState.EMPTY);
        assertTrue(empty.size() > 0);
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
        Nebula n1 = new Nebula(1);
        Nebula n2 = new Nebula(1);
        Nebula n3 = new Nebula(2);
        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
        assertNotEquals(n1, n3);
    }

    @Test
    @DisplayName("Should have correct toString format")
    void testToString() {
        Nebula nebula = new Nebula(1);
        nebula.setName("Orion");
        String str = nebula.toString();
        assertTrue(str.contains("name='Orion'"));
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("elerium=3"));
        assertTrue(str.contains("jamming=40%"));
        assertTrue(str.contains("cloaking=60%"));
    }

    @Test
    @DisplayName("Should integrate with SolarSystemEntity")
    void testSolarSystemIntegration() {
        SolarSystemEntity system = new SolarSystemEntity(1);
        Nebula nebula = new Nebula(1);
        nebula.setName("Orion");

        system.addNebula(nebula, new Hex3D(40, -40, 0));
        assertEquals(1, system.getNebulaCount());
        assertEquals(nebula, system.getNebula(1));

        SystemBody body = system.getAt(new Hex3D(40, -40, 0));
        assertNotNull(body);
        assertEquals(SystemBody.BodyType.NEBULA, body.getType());
    }
}
