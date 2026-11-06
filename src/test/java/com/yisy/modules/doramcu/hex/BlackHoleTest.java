package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BlackHole Tests")
class BlackHoleTest {

    // ---- BlackHoleSize enum tests ----

    @Test
    @DisplayName("Should have 5 BlackHoleSize enum values")
    void testBlackHoleSizeValues() {
        BlackHole.BlackHoleSize[] values = BlackHole.BlackHoleSize.values();
        assertEquals(5, values.length);
    }

    @Test
    @DisplayName("Should have correct TINY black hole size properties")
    void testBlackHoleSizeTiny() {
        BlackHole.BlackHoleSize size = BlackHole.BlackHoleSize.TINY;
        assertEquals(3, size.getBaseRadius());
        assertEquals(1, size.getBaseAntimatterYield());
        assertEquals(0, size.getBaseStationSlots());
        assertEquals(0.4, size.getBaseGravitationalPull());
        assertEquals(0.02, size.getBaseDestructionChance());
    }

    @Test
    @DisplayName("Should have correct SMALL black hole size properties")
    void testBlackHoleSizeSmall() {
        BlackHole.BlackHoleSize size = BlackHole.BlackHoleSize.SMALL;
        assertEquals(5, size.getBaseRadius());
        assertEquals(1, size.getBaseAntimatterYield());
        assertEquals(1, size.getBaseStationSlots());
        assertEquals(0.5, size.getBaseGravitationalPull());
        assertEquals(0.03, size.getBaseDestructionChance());
    }

    @Test
    @DisplayName("Should have correct MEDIUM black hole size properties")
    void testBlackHoleSizeMedium() {
        BlackHole.BlackHoleSize size = BlackHole.BlackHoleSize.MEDIUM;
        assertEquals(6, size.getBaseRadius());
        assertEquals(2, size.getBaseAntimatterYield());
        assertEquals(1, size.getBaseStationSlots());
        assertEquals(0.7, size.getBaseGravitationalPull());
        assertEquals(0.05, size.getBaseDestructionChance());
    }

    @Test
    @DisplayName("Should have correct LARGE black hole size properties")
    void testBlackHoleSizeLarge() {
        BlackHole.BlackHoleSize size = BlackHole.BlackHoleSize.LARGE;
        assertEquals(10, size.getBaseRadius());
        assertEquals(4, size.getBaseAntimatterYield());
        assertEquals(2, size.getBaseStationSlots());
        assertEquals(0.8, size.getBaseGravitationalPull());
        assertEquals(0.08, size.getBaseDestructionChance());
    }

    @Test
    @DisplayName("Should have correct MASSIVE black hole size properties")
    void testBlackHoleSizeMassive() {
        BlackHole.BlackHoleSize size = BlackHole.BlackHoleSize.MASSIVE;
        assertEquals(15, size.getBaseRadius());
        assertEquals(6, size.getBaseAntimatterYield());
        assertEquals(2, size.getBaseStationSlots());
        assertEquals(0.9, size.getBaseGravitationalPull());
        assertEquals(0.12, size.getBaseDestructionChance());
    }

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create black hole with default radius")
    void testDefaultConstructor() {
        BlackHole blackHole = new BlackHole(1);
        assertEquals(1, blackHole.getId());
        assertEquals(BlackHole.DEFAULT_RADIUS, blackHole.getRadius());
        assertEquals("BlackHole-1", blackHole.getName());
        assertEquals(BlackHole.DEFAULT_ANTIMATTER_YIELD, blackHole.getAntimatterYield());
        assertEquals(BlackHole.DEFAULT_STATION_SLOTS, blackHole.getStationSlots());
        assertEquals(BlackHole.DEFAULT_GRAVITATIONAL_PULL, blackHole.getGravitationalPull());
        assertEquals(BlackHole.DEFAULT_DESTRUCTION_CHANCE, blackHole.getDestructionChance());
        assertTrue(blackHole.hasStrategicDeposit());
        assertEquals(StrategicResource.ANTI_MATTER, blackHole.getStrategicResource());
        assertEquals(BlackHole.BlackHoleSize.MEDIUM, blackHole.getBlackHoleSize());
        assertTrue(blackHole.size() > 0);
    }

    @Test
    @DisplayName("Should create black hole with BlackHoleSize")
    void testBlackHoleSizeConstructor() {
        BlackHole blackHole = new BlackHole(1, BlackHole.BlackHoleSize.LARGE);
        assertEquals(1, blackHole.getId());
        assertEquals(10, blackHole.getRadius());
        assertEquals(4, blackHole.getAntimatterYield());
        assertEquals(2, blackHole.getStationSlots());
        assertEquals(0.8, blackHole.getGravitationalPull());
        assertEquals(0.08, blackHole.getDestructionChance());
        assertEquals(BlackHole.BlackHoleSize.LARGE, blackHole.getBlackHoleSize());
        assertTrue(blackHole.size() > 0);
    }

    @Test
    @DisplayName("Should create black hole with TINY BlackHoleSize")
    void testTinyBlackHoleSizeConstructor() {
        BlackHole blackHole = new BlackHole(1, BlackHole.BlackHoleSize.TINY);
        assertEquals(3, blackHole.getRadius());
        assertEquals(1, blackHole.getAntimatterYield());
        assertEquals(0, blackHole.getStationSlots());
        assertEquals(0.4, blackHole.getGravitationalPull());
        assertEquals(0.02, blackHole.getDestructionChance());
    }

    @Test
    @DisplayName("Should create black hole with MASSIVE BlackHoleSize")
    void testMassiveBlackHoleSizeConstructor() {
        BlackHole blackHole = new BlackHole(1, BlackHole.BlackHoleSize.MASSIVE);
        assertEquals(15, blackHole.getRadius());
        assertEquals(6, blackHole.getAntimatterYield());
        assertEquals(2, blackHole.getStationSlots());
        assertEquals(0.9, blackHole.getGravitationalPull());
        assertEquals(0.12, blackHole.getDestructionChance());
    }

    @Test
    @DisplayName("Should throw on null BlackHoleSize")
    void testNullBlackHoleSize() {
        assertThrows(IllegalArgumentException.class, () -> new BlackHole(1, null));
    }

    @Test
    @DisplayName("Should create black hole with custom radius")
    void testCustomRadiusConstructor() {
        BlackHole blackHole = new BlackHole(1, 10);
        assertEquals(10, blackHole.getRadius());
    }

    @Test
    @DisplayName("Should throw on negative radius")
    void testNegativeRadius() {
        assertThrows(IllegalArgumentException.class, () -> new BlackHole(1, -1));
    }

    // ---- BlackHoleSize setter tests ----

    @Test
    @DisplayName("Should get and set BlackHoleSize")
    void testSetBlackHoleSize() {
        BlackHole blackHole = new BlackHole(1);
        assertEquals(BlackHole.BlackHoleSize.MEDIUM, blackHole.getBlackHoleSize());

        blackHole.setBlackHoleSize(BlackHole.BlackHoleSize.LARGE);
        assertEquals(BlackHole.BlackHoleSize.LARGE, blackHole.getBlackHoleSize());
        assertEquals(4, blackHole.getAntimatterYield());
        assertEquals(2, blackHole.getStationSlots());
        assertEquals(0.8, blackHole.getGravitationalPull());
        assertEquals(0.08, blackHole.getDestructionChance());
    }

    @Test
    @DisplayName("Should update all properties when setting BlackHoleSize to TINY")
    void testSetBlackHoleSizeTiny() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setBlackHoleSize(BlackHole.BlackHoleSize.TINY);
        assertEquals(1, blackHole.getAntimatterYield());
        assertEquals(0, blackHole.getStationSlots());
        assertEquals(0.4, blackHole.getGravitationalPull());
        assertEquals(0.02, blackHole.getDestructionChance());
    }

    @Test
    @DisplayName("Should update all properties when setting BlackHoleSize to MASSIVE")
    void testSetBlackHoleSizeMassive() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setBlackHoleSize(BlackHole.BlackHoleSize.MASSIVE);
        assertEquals(6, blackHole.getAntimatterYield());
        assertEquals(2, blackHole.getStationSlots());
        assertEquals(0.9, blackHole.getGravitationalPull());
        assertEquals(0.12, blackHole.getDestructionChance());
    }

    @Test
    @DisplayName("Should throw on null BlackHoleSize setter")
    void testSetBlackHoleSizeNull() {
        BlackHole blackHole = new BlackHole(1);
        assertThrows(IllegalArgumentException.class, () -> blackHole.setBlackHoleSize(null));
    }

    @Test
    @DisplayName("Should set and get name")
    void testName() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setName("Cygnus X-1");
        assertEquals("Cygnus X-1", blackHole.getName());
    }

    @Test
    @DisplayName("Should set and get Anti-Matter yield")
    void testAntimatterYield() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setAntimatterYield(5);
        assertEquals(5, blackHole.getAntimatterYield());
    }

    @Test
    @DisplayName("Should throw on negative Anti-Matter yield")
    void testNegativeAntimatterYield() {
        BlackHole blackHole = new BlackHole(1);
        assertThrows(IllegalArgumentException.class, () -> blackHole.setAntimatterYield(-1));
    }

    @Test
    @DisplayName("Should calculate effective Anti-Matter yield with station bonus")
    void testEffectiveAntimatterYield() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setAntimatterYield(10);
        assertEquals(10, blackHole.getEffectiveAntimatterYield());

        blackHole.addStation("Extractor-1");
        assertEquals(13, blackHole.getEffectiveAntimatterYield()); // 10 * 1.3
    }

    @Test
    @DisplayName("Should manage station slots")
    void testStationSlots() {
        BlackHole blackHole = new BlackHole(1);
        assertEquals(1, blackHole.getStationSlots());
        assertTrue(blackHole.hasAvailableStationSlot());

        blackHole.addStation("Station-1");
        assertFalse(blackHole.hasAvailableStationSlot());
        assertFalse(blackHole.addStation("Station-2"));
    }

    @Test
    @DisplayName("Should set and get station slots")
    void testSetStationSlots() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setStationSlots(3);
        assertEquals(3, blackHole.getStationSlots());
    }

    @Test
    @DisplayName("Should throw on negative station slots")
    void testNegativeStationSlots() {
        BlackHole blackHole = new BlackHole(1);
        assertThrows(IllegalArgumentException.class, () -> blackHole.setStationSlots(-1));
    }

    @Test
    @DisplayName("Should remove station")
    void testRemoveStation() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.addStation("Station-1");
        assertTrue(blackHole.removeStation("Station-1"));
        assertEquals(0, blackHole.getOccupiedStations().size());
    }

    @Test
    @DisplayName("Should set and get gravitational pull")
    void testGravitationalPull() {
        BlackHole blackHole = new BlackHole(1);
        assertEquals(0.7, blackHole.getGravitationalPull());
        blackHole.setGravitationalPull(0.9);
        assertEquals(0.9, blackHole.getGravitationalPull());
    }

    @Test
    @DisplayName("Should throw on invalid gravitational pull")
    void testInvalidGravitationalPull() {
        BlackHole blackHole = new BlackHole(1);
        assertThrows(IllegalArgumentException.class, () -> blackHole.setGravitationalPull(-0.1));
        assertThrows(IllegalArgumentException.class, () -> blackHole.setGravitationalPull(1.1));
    }

    @Test
    @DisplayName("Should calculate effective movement speed")
    void testEffectiveMovementSpeed() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setGravitationalPull(0.7);
        assertEquals(3, blackHole.getEffectiveMovementSpeed(10)); // 10 * 0.3
        assertEquals(1, blackHole.getEffectiveMovementSpeed(1)); // minimum 1
    }

    @Test
    @DisplayName("Should calculate slingshot bonus")
    void testSlingshotBonus() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setGravitationalPull(0.7);
        assertEquals(11, blackHole.getSlingshotBonus(10)); // 10 * 0.7 * 1.5
    }

    @Test
    @DisplayName("Should set and get destruction chance")
    void testDestructionChance() {
        BlackHole blackHole = new BlackHole(1);
        assertEquals(0.05, blackHole.getDestructionChance());
        blackHole.setDestructionChance(0.1);
        assertEquals(0.1, blackHole.getDestructionChance());
    }

    @Test
    @DisplayName("Should throw on invalid destruction chance")
    void testInvalidDestructionChance() {
        BlackHole blackHole = new BlackHole(1);
        assertThrows(IllegalArgumentException.class, () -> blackHole.setDestructionChance(-0.1));
        assertThrows(IllegalArgumentException.class, () -> blackHole.setDestructionChance(1.1));
    }

    @Test
    @DisplayName("Should check ship destruction with random")
    void testIsShipDestroyed() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setDestructionChance(1.0);
        assertTrue(blackHole.isShipDestroyed(new Random(42)));

        blackHole.setDestructionChance(0.0);
        assertFalse(blackHole.isShipDestroyed(new Random(42)));
    }

    @Test
    @DisplayName("Should manage strategic resource deposit")
    void testStrategicDeposit() {
        BlackHole blackHole = new BlackHole(1);
        assertTrue(blackHole.hasStrategicDeposit());
        assertEquals(StrategicResource.ANTI_MATTER, blackHole.getStrategicResource());

        blackHole.setStrategicResource(null);
        assertFalse(blackHole.hasStrategicDeposit());
        assertNull(blackHole.getStrategicResource());
    }

    @Test
    @DisplayName("Should return event horizon hexes")
    void testEventHorizon() {
        BlackHole blackHole = new BlackHole(1, 6);
        List<Hex3D> horizon = blackHole.getEventHorizon();
        assertTrue(horizon.size() > 0);
        Hex3D center = new Hex3D(0, 0, 0);
        for (Hex3D hex : horizon) {
            assertEquals(6, center.distance(hex));
        }
    }

    @Test
    @DisplayName("Should return inner region hexes")
    void testInnerRegion() {
        BlackHole blackHole = new BlackHole(1, 6);
        List<Hex3D> inner = blackHole.getInnerRegion();
        assertTrue(inner.size() > 0);
        Hex3D center = new Hex3D(0, 0, 0);
        int innerRadius = 6 / 2;
        for (Hex3D hex : inner) {
            assertTrue(center.distance(hex) <= innerRadius);
        }
    }

    @Test
    @DisplayName("Should calculate destruction chance at hex")
    void testDestructionChanceAt() {
        BlackHole blackHole = new BlackHole(1, 6);
        blackHole.setDestructionChance(0.05);

        Hex3D center = new Hex3D(0, 0, 0);
        assertEquals(0.1, blackHole.getDestructionChanceAt(center)); // doubled in inner region

        Hex3D outer = new Hex3D(5, -5, 0);
        assertEquals(0.05, blackHole.getDestructionChanceAt(outer)); // normal at outer

        assertEquals(0.0, blackHole.getDestructionChanceAt(null));
    }

    @Test
    @DisplayName("Should find hexes by state")
    void testFindByState() {
        BlackHole blackHole = new BlackHole(1, 3);
        List<Hex3D> empty = blackHole.findByState(HexState.EMPTY);
        assertTrue(empty.size() > 0);
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
        BlackHole b1 = new BlackHole(1);
        BlackHole b2 = new BlackHole(1);
        BlackHole b3 = new BlackHole(2);
        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, b3);
    }

    @Test
    @DisplayName("Should have correct toString format")
    void testToString() {
        BlackHole blackHole = new BlackHole(1);
        blackHole.setName("Cygnus");
        String str = blackHole.toString();
        assertTrue(str.contains("name='Cygnus'"));
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("antimatter=2"));
        assertTrue(str.contains("gravity=70%"));
        assertTrue(str.contains("destruction=5%"));
    }

    @Test
    @DisplayName("Should integrate with SolarSystemEntity")
    void testSolarSystemIntegration() {
        SolarSystemEntity system = new SolarSystemEntity(1);
        BlackHole blackHole = new BlackHole(1);
        blackHole.setName("Cygnus X-1");

        system.addBlackHole(blackHole, new Hex3D(50, -50, 0));
        assertEquals(1, system.getBlackHoleCount());
        assertEquals(blackHole, system.getBlackHole(1));

        SystemBody body = system.getAt(new Hex3D(50, -50, 0));
        assertNotNull(body);
        assertEquals(SystemBody.BodyType.BLACK_HOLE, body.getType());
    }

    @Test
    @DisplayName("Should remove black hole from solar system")
    void testRemoveBlackHole() {
        SolarSystemEntity system = new SolarSystemEntity(1);
        BlackHole blackHole = new BlackHole(1);
        system.addBlackHole(blackHole, new Hex3D(50, -50, 0));

        BlackHole removed = system.removeBlackHole(1);
        assertEquals(blackHole, removed);
        assertEquals(0, system.getBlackHoleCount());
        assertNull(system.getBlackHole(1));
    }
}
