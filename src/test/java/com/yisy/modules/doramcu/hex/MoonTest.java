package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Moon Tests")
class MoonTest {

    @Test
    @DisplayName("Should create moon with default radius and MEDIUM size")
    void testDefaultConstructor() {
        Moon moon = new Moon(1);
        assertEquals(1, moon.getId());
        assertEquals(Moon.DEFAULT_RADIUS, moon.getRadius());
        assertEquals("Moon-1", moon.getName());
        assertEquals(Moon.MoonSize.MEDIUM, moon.getMoonSize());
        assertEquals(3, moon.getMineralYield());
        assertEquals(Moon.DEFAULT_RESEARCH_YIELD, moon.getResearchYield());
        assertEquals(2, moon.getOutpostSlots());
        assertEquals(0.15, moon.getTidalStrength(), 0.001); // 0.1 * 1.5 (MEDIUM multiplier)
        assertEquals(Moon.DEFAULT_GRAVITY_DEFENSE_BONUS, moon.getGravityDefenseBonus());
        assertFalse(moon.hasStrategicDeposit());
        assertNull(moon.getStrategicResource());
        assertNull(moon.getParentPlanetId());
        assertFalse(moon.hasParentPlanet());
        assertTrue(moon.size() > 0);
    }

    @Test
    @DisplayName("Should create moon with custom radius")
    void testCustomRadiusConstructor() {
        Moon moon = new Moon(1, 5);
        assertEquals(5, moon.getRadius());
        assertTrue(moon.size() > 0);
    }

    @Test
    @DisplayName("Should create moon with specific MoonSize")
    void testMoonSizeConstructor() {
        Moon moon = new Moon(1, Moon.MoonSize.LARGE);
        assertEquals(Moon.MoonSize.LARGE, moon.getMoonSize());
        assertEquals(4, moon.getMineralYield());
        assertEquals(3, moon.getOutpostSlots());
        assertEquals(0.2, moon.getTidalStrength(), 0.001); // 0.1 * 2.0 (LARGE multiplier)
    }

    @Test
    @DisplayName("Should create moon with custom radius and MoonSize")
    void testCustomRadiusAndSizeConstructor() {
        Moon moon = new Moon(1, 5, Moon.MoonSize.HUGE);
        assertEquals(5, moon.getRadius());
        assertEquals(Moon.MoonSize.HUGE, moon.getMoonSize());
        assertEquals(5, moon.getMineralYield());
        assertEquals(4, moon.getOutpostSlots());
        assertEquals(0.3, moon.getTidalStrength(), 0.001); // 0.1 * 3.0 (HUGE multiplier)
    }

    @Test
    @DisplayName("Should throw on negative radius")
    void testNegativeRadius() {
        assertThrows(IllegalArgumentException.class, () -> new Moon(1, -1));
    }

    @Test
    @DisplayName("Should throw on null MoonSize")
    void testNullMoonSize() {
        assertThrows(IllegalArgumentException.class, () -> new Moon(1, 3, null));
    }

    @Test
    @DisplayName("Should set and get name")
    void testName() {
        Moon moon = new Moon(1);
        moon.setName("Luna");
        assertEquals("Luna", moon.getName());
    }

    @Test
    @DisplayName("Should set and get MoonSize")
    void testMoonSize() {
        Moon moon = new Moon(1);
        moon.setMoonSize(Moon.MoonSize.TINY);
        assertEquals(Moon.MoonSize.TINY, moon.getMoonSize());
        assertEquals(1, moon.getMineralYield());
        assertEquals(1, moon.getOutpostSlots());
        assertEquals(0.05, moon.getTidalStrength(), 0.001); // 0.1 * 0.5 (TINY multiplier)
    }

    @Test
    @DisplayName("Should throw on null MoonSize setter")
    void testNullMoonSizeSetter() {
        Moon moon = new Moon(1);
        assertThrows(IllegalArgumentException.class, () -> moon.setMoonSize(null));
    }

    @Test
    @DisplayName("Should set and get mineral yield")
    void testMineralYield() {
        Moon moon = new Moon(1);
        moon.setMineralYield(10);
        assertEquals(10, moon.getMineralYield());
    }

    @Test
    @DisplayName("Should throw on negative mineral yield")
    void testNegativeMineralYield() {
        Moon moon = new Moon(1);
        assertThrows(IllegalArgumentException.class, () -> moon.setMineralYield(-1));
    }

    @Test
    @DisplayName("Should set and get research yield")
    void testResearchYield() {
        Moon moon = new Moon(1);
        moon.setResearchYield(5);
        assertEquals(5, moon.getResearchYield());
    }

    @Test
    @DisplayName("Should throw on negative research yield")
    void testNegativeResearchYield() {
        Moon moon = new Moon(1);
        assertThrows(IllegalArgumentException.class, () -> moon.setResearchYield(-1));
    }

    @Test
    @DisplayName("Should calculate effective mineral yield with outpost bonus")
    void testEffectiveMineralYield() {
        Moon moon = new Moon(1);
        moon.setMineralYield(10);
        assertEquals(10, moon.getEffectiveMineralYield());

        moon.addOutpost("Miner-1");
        assertEquals(11, moon.getEffectiveMineralYield()); // 10 * 1.1

        moon.addOutpost("Miner-2");
        assertEquals(12, moon.getEffectiveMineralYield()); // 10 * 1.2
    }

    @Test
    @DisplayName("Should calculate effective research yield with outpost bonus")
    void testEffectiveResearchYield() {
        Moon moon = new Moon(1);
        moon.setResearchYield(10);
        assertEquals(10, moon.getEffectiveResearchYield());

        moon.addOutpost("Lab-1");
        assertEquals(12, moon.getEffectiveResearchYield()); // 10 * 1.15 rounded

        moon.addOutpost("Lab-2");
        assertEquals(13, moon.getEffectiveResearchYield()); // 10 * 1.3
    }

    @Test
    @DisplayName("Should manage outpost slots")
    void testOutpostSlots() {
        Moon moon = new Moon(1);
        assertEquals(2, moon.getOutpostSlots());
        assertEquals(2, moon.getAvailableOutpostSlots());
        assertTrue(moon.hasAvailableOutpostSlot());

        moon.addOutpost("Outpost-1");
        assertEquals(1, moon.getAvailableOutpostSlots());

        moon.addOutpost("Outpost-2");
        assertEquals(0, moon.getAvailableOutpostSlots());
        assertFalse(moon.hasAvailableOutpostSlot());

        assertFalse(moon.addOutpost("Outpost-3"));
    }

    @Test
    @DisplayName("Should set and get outpost slots")
    void testSetOutpostSlots() {
        Moon moon = new Moon(1);
        moon.setOutpostSlots(5);
        assertEquals(5, moon.getOutpostSlots());
    }

    @Test
    @DisplayName("Should throw on negative outpost slots")
    void testNegativeOutpostSlots() {
        Moon moon = new Moon(1);
        assertThrows(IllegalArgumentException.class, () -> moon.setOutpostSlots(-1));
    }

    @Test
    @DisplayName("Should remove outpost")
    void testRemoveOutpost() {
        Moon moon = new Moon(1);
        moon.addOutpost("Outpost-1");
        moon.addOutpost("Outpost-2");
        assertTrue(moon.removeOutpost("Outpost-1"));
        assertEquals(1, moon.getOccupiedOutposts().size());
        assertEquals(1, moon.getAvailableOutpostSlots());
        assertFalse(moon.removeOutpost("NonExistent"));
    }

    @Test
    @DisplayName("Should return unmodifiable occupied outposts list")
    void testOccupiedOutpostsUnmodifiable() {
        Moon moon = new Moon(1);
        moon.addOutpost("Outpost-1");
        assertThrows(UnsupportedOperationException.class, () -> moon.getOccupiedOutposts().add("Outpost-2"));
    }

    @Test
    @DisplayName("Should set and get tidal strength")
    void testTidalStrength() {
        Moon moon = new Moon(1);
        moon.setTidalStrength(0.5);
        assertEquals(0.5, moon.getTidalStrength());
    }

    @Test
    @DisplayName("Should throw on tidal strength out of range")
    void testTidalStrengthOutOfRange() {
        Moon moon = new Moon(1);
        assertThrows(IllegalArgumentException.class, () -> moon.setTidalStrength(-0.1));
        assertThrows(IllegalArgumentException.class, () -> moon.setTidalStrength(1.1));
    }

    @Test
    @DisplayName("Should calculate tidal mineral bonus")
    void testTidalMineralBonus() {
        Moon moon = new Moon(1);
        moon.setTidalStrength(0.2);
        assertEquals(0.1, moon.getTidalMineralBonus()); // 0.2 * 0.5
    }

    @Test
    @DisplayName("Should calculate tidal stability reduction")
    void testTidalStabilityReduction() {
        Moon moon = new Moon(1);
        moon.setTidalStrength(0.2);
        assertEquals(0.06, moon.getTidalStabilityReduction()); // 0.2 * 0.3
    }

    @Test
    @DisplayName("Should set and get gravity defense bonus")
    void testGravityDefenseBonus() {
        Moon moon = new Moon(1);
        moon.setGravityDefenseBonus(0.2);
        assertEquals(0.2, moon.getGravityDefenseBonus());
    }

    @Test
    @DisplayName("Should throw on gravity defense bonus out of range")
    void testGravityDefenseBonusOutOfRange() {
        Moon moon = new Moon(1);
        assertThrows(IllegalArgumentException.class, () -> moon.setGravityDefenseBonus(-0.1));
        assertThrows(IllegalArgumentException.class, () -> moon.setGravityDefenseBonus(1.1));
    }

    @Test
    @DisplayName("Should calculate defended damage")
    void testDefendedDamage() {
        Moon moon = new Moon(1);
        moon.setGravityDefenseBonus(0.1);
        assertEquals(90, moon.getDefendedDamage(100)); // 100 * 0.9

        moon.setGravityDefenseBonus(0.2);
        assertEquals(80, moon.getDefendedDamage(100)); // 100 * 0.8
    }

    @Test
    @DisplayName("Should bind and unbind parent planet")
    void testParentPlanet() {
        Moon moon = new Moon(1);
        assertFalse(moon.hasParentPlanet());
        assertNull(moon.getParentPlanetId());

        moon.setParentPlanet(42);
        assertTrue(moon.hasParentPlanet());
        assertEquals(42, moon.getParentPlanetId());

        moon.unbindFromPlanet();
        assertFalse(moon.hasParentPlanet());
        assertNull(moon.getParentPlanetId());
    }

    @Test
    @DisplayName("Should manage strategic deposits")
    void testStrategicDeposit() {
        Moon moon = new Moon(1);
        assertFalse(moon.hasStrategicDeposit());
        assertNull(moon.getStrategicResource());

        moon.setStrategicResource(StrategicResource.ELERIUM);
        assertTrue(moon.hasStrategicDeposit());
        assertEquals(StrategicResource.ELERIUM, moon.getStrategicResource());

        moon.setStrategicResource(null);
        assertFalse(moon.hasStrategicDeposit());
        assertNull(moon.getStrategicResource());
    }

    @Test
    @DisplayName("Should manage all strategic resource types")
    void testAllStrategicResources() {
        Moon moon = new Moon(1);
        for (StrategicResource resource : StrategicResource.values()) {
            moon.setStrategicResource(resource);
            assertEquals(resource, moon.getStrategicResource());
            assertTrue(moon.hasStrategicDeposit());
        }
    }

    // ---- Hex operations ----

    @Test
    @DisplayName("Should return hex map")
    void testGetMap() {
        Moon moon = new Moon(1);
        assertNotNull(moon.getMap());
        assertTrue(moon.getMap().size() > 0);
    }

    @Test
    @DisplayName("Should contain center hex")
    void testContainsCenter() {
        Moon moon = new Moon(1);
        assertTrue(moon.contains(0, 0));
    }

    @Test
    @DisplayName("Should not contain hex outside radius")
    void testContainsOutside() {
        Moon moon = new Moon(1);
        assertFalse(moon.contains(100, -50));
    }

    @Test
    @DisplayName("Should return all hexes")
    void testGetAll() {
        Moon moon = new Moon(1);
        Collection<Hex3D> hexes = moon.getAll();
        assertNotNull(hexes);
        assertEquals(moon.size(), hexes.size());
    }

    @Test
    @DisplayName("Should return hex at coordinate")
    void testGetHex() {
        Moon moon = new Moon(1);
        Hex3D center = new Hex3D(0, 0, 0);
        assertNotNull(moon.get(center));
    }

    @Test
    @DisplayName("Should return null for missing hex")
    void testGetMissing() {
        Moon moon = new Moon(1);
        Hex3D far = new Hex3D(100, -50, -50);
        assertNull(moon.get(far));
    }

    @Test
    @DisplayName("Should return neighbors within moon")
    void testGetNeighbors() {
        Moon moon = new Moon(1);
        Hex3D center = new Hex3D(0, 0, 0);
        List<Hex3D> neighbors = moon.getNeighbors(center);
        assertNotNull(neighbors);
        assertTrue(neighbors.size() > 0);
    }

    @Test
    @DisplayName("Should return empty neighbors for hex outside moon")
    void testGetNeighborsOutside() {
        Moon moon = new Moon(1);
        Hex3D far = new Hex3D(100, -50, -50);
        List<Hex3D> neighbors = moon.getNeighbors(far);
        assertNotNull(neighbors);
        assertTrue(neighbors.isEmpty());
    }

    // ---- Object contract ----

    @Test
    @DisplayName("Should return correct toString")
    void testToString() {
        Moon moon = new Moon(1);
        moon.setName("Luna");
        String str = moon.toString();
        assertTrue(str.startsWith("Moon{"));
        assertTrue(str.contains("name='Luna'"));
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("size=MEDIUM"));
        assertTrue(str.contains("radius=3"));
        assertTrue(str.contains("minerals=3"));
        assertTrue(str.contains("research=1"));
        assertTrue(str.contains("outposts=0/2"));
        assertTrue(str.endsWith("}"));
    }

    @Test
    @DisplayName("Should be equal when IDs match")
    void testEquals() {
        Moon moon1 = new Moon(1);
        Moon moon2 = new Moon(1);
        assertEquals(moon1, moon2);
        assertEquals(moon2, moon1);
        assertEquals(moon1, moon1);
    }

    @Test
    @DisplayName("Should not be equal when IDs differ")
    void testNotEquals() {
        Moon moon1 = new Moon(1);
        Moon moon2 = new Moon(2);
        assertNotEquals(moon1, moon2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void testNotEqualsNull() {
        Moon moon = new Moon(1);
        assertNotEquals(null, moon);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void testNotEqualsDifferentClass() {
        Moon moon = new Moon(1);
        assertNotEquals("Moon", moon);
        assertNotEquals(1, moon);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCode() {
        Moon moon1 = new Moon(1);
        Moon moon2 = new Moon(1);
        assertEquals(moon1.hashCode(), moon2.hashCode());
    }

    // ---- MoonSize enum tests ----

    @Test
    @DisplayName("Should have 5 MoonSize values")
    void testMoonSizeValues() {
        assertEquals(5, Moon.MoonSize.values().length);
    }

    @Test
    @DisplayName("Should have correct TINY properties")
    void testTinyProperties() {
        assertEquals(1, Moon.MoonSize.TINY.getBaseMineralYield());
        assertEquals(1, Moon.MoonSize.TINY.getBaseOutpostSlots());
        assertEquals(0.5, Moon.MoonSize.TINY.getTidalMultiplier());
    }

    @Test
    @DisplayName("Should have correct SMALL properties")
    void testSmallProperties() {
        assertEquals(2, Moon.MoonSize.SMALL.getBaseMineralYield());
        assertEquals(2, Moon.MoonSize.SMALL.getBaseOutpostSlots());
        assertEquals(1.0, Moon.MoonSize.SMALL.getTidalMultiplier());
    }

    @Test
    @DisplayName("Should have correct MEDIUM properties")
    void testMediumProperties() {
        assertEquals(3, Moon.MoonSize.MEDIUM.getBaseMineralYield());
        assertEquals(2, Moon.MoonSize.MEDIUM.getBaseOutpostSlots());
        assertEquals(1.5, Moon.MoonSize.MEDIUM.getTidalMultiplier());
    }

    @Test
    @DisplayName("Should have correct LARGE properties")
    void testLargeProperties() {
        assertEquals(4, Moon.MoonSize.LARGE.getBaseMineralYield());
        assertEquals(3, Moon.MoonSize.LARGE.getBaseOutpostSlots());
        assertEquals(2.0, Moon.MoonSize.LARGE.getTidalMultiplier());
    }

    @Test
    @DisplayName("Should have correct HUGE properties")
    void testHugeProperties() {
        assertEquals(5, Moon.MoonSize.HUGE.getBaseMineralYield());
        assertEquals(4, Moon.MoonSize.HUGE.getBaseOutpostSlots());
        assertEquals(3.0, Moon.MoonSize.HUGE.getTidalMultiplier());
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should build complete moon with all features")
    void testCompleteMoonBuild() {
        Moon moon = new Moon(1, 5, Moon.MoonSize.LARGE);
        moon.setName("Titan");
        moon.setMineralYield(8);
        moon.setResearchYield(4);
        moon.setTidalStrength(0.3);
        moon.setGravityDefenseBonus(0.15);
        moon.setParentPlanet(100);
        moon.setStrategicResource(StrategicResource.ELERIUM);

        assertEquals("Titan", moon.getName());
        assertEquals(Moon.MoonSize.LARGE, moon.getMoonSize());
        assertEquals(8, moon.getMineralYield());
        assertEquals(4, moon.getResearchYield());
        assertEquals(0.3, moon.getTidalStrength());
        assertEquals(0.15, moon.getGravityDefenseBonus());
        assertEquals(100, moon.getParentPlanetId());
        assertEquals(StrategicResource.ELERIUM, moon.getStrategicResource());
        assertTrue(moon.hasStrategicDeposit());
        assertTrue(moon.hasParentPlanet());
    }

    @Test
    @DisplayName("Should calculate tidal effects for parent planet")
    void testTidalEffectsOnPlanet() {
        Moon moon = new Moon(1, Moon.MoonSize.HUGE);
        moon.setTidalStrength(0.4);

        // Tidal mineral bonus: 0.4 * 0.5 = 0.2 (20% bonus)
        assertEquals(0.2, moon.getTidalMineralBonus());

        // Tidal stability reduction: 0.4 * 0.3 = 0.12 (12% reduction)
        assertEquals(0.12, moon.getTidalStabilityReduction());
    }

    @Test
    @DisplayName("Should calculate defense bonus for fleet combat")
    void testDefenseBonusForCombat() {
        Moon moon = new Moon(1);
        moon.setGravityDefenseBonus(0.15);

        // Attacker deals 100 damage, defender takes 85
        assertEquals(85, moon.getDefendedDamage(100));

        // Attacker deals 50 damage, defender takes 43 (rounded)
        assertEquals(43, moon.getDefendedDamage(50));
    }

    @Test
    @DisplayName("Should handle zero defense bonus")
    void testZeroDefenseBonus() {
        Moon moon = new Moon(1);
        moon.setGravityDefenseBonus(0.0);
        assertEquals(100, moon.getDefendedDamage(100));
    }

    @Test
    @DisplayName("Should handle maximum defense bonus")
    void testMaximumDefenseBonus() {
        Moon moon = new Moon(1);
        moon.setGravityDefenseBonus(1.0);
        assertEquals(0, moon.getDefendedDamage(100));
    }

    @Test
    @DisplayName("Should handle multiple moons orbiting same planet")
    void testMultipleMoonsSamePlanet() {
        Moon moon1 = new Moon(1, Moon.MoonSize.SMALL);
        Moon moon2 = new Moon(2, Moon.MoonSize.MEDIUM);
        Moon moon3 = new Moon(3, Moon.MoonSize.LARGE);

        moon1.setName("Luna");
        moon2.setName("Phobos");
        moon3.setName("Deimos");

        moon1.setParentPlanet(100);
        moon2.setParentPlanet(100);
        moon3.setParentPlanet(100);

        assertEquals(100, moon1.getParentPlanetId());
        assertEquals(100, moon2.getParentPlanetId());
        assertEquals(100, moon3.getParentPlanetId());

        // Combined tidal effect
        double totalTidal = moon1.getTidalStrength() + moon2.getTidalStrength() + moon3.getTidalStrength();
        assertTrue(totalTidal > 0);
    }

    @Test
    @DisplayName("Should handle outpost removal and yield recalculation")
    void testOutpostRemovalRecalculation() {
        Moon moon = new Moon(1);
        moon.setMineralYield(10);
        moon.setResearchYield(10);

        moon.addOutpost("Miner-1");
        moon.addOutpost("Lab-1");
        assertEquals(12, moon.getEffectiveMineralYield()); // 10 * 1.2
        assertEquals(13, moon.getEffectiveResearchYield()); // 10 * 1.3

        moon.removeOutpost("Miner-1");
        assertEquals(11, moon.getEffectiveMineralYield()); // 10 * 1.1
        assertEquals(12, moon.getEffectiveResearchYield()); // 10 * 1.15
    }

    @Test
    @DisplayName("Should handle size change updating properties")
    void testSizeChangeUpdatesProperties() {
        Moon moon = new Moon(1, Moon.MoonSize.TINY);
        assertEquals(1, moon.getMineralYield());
        assertEquals(1, moon.getOutpostSlots());

        moon.setMoonSize(Moon.MoonSize.HUGE);
        assertEquals(5, moon.getMineralYield());
        assertEquals(4, moon.getOutpostSlots());
    }

    @Test
    @DisplayName("Should handle radius 0 moon")
    void testRadiusZeroMoon() {
        Moon moon = new Moon(1, 0);
        assertEquals(0, moon.getRadius());
        assertEquals(1, moon.size()); // Single center hex
    }

    @Test
    @DisplayName("Should have correct toString format")
    void testToStringFormat() {
        Moon moon = new Moon(42);
        moon.setName("Europa");
        moon.setMoonSize(Moon.MoonSize.LARGE);
        moon.addOutpost("Research-1");

        String str = moon.toString();
        assertTrue(str.startsWith("Moon{"));
        assertTrue(str.contains("name='Europa'"));
        assertTrue(str.contains("id=42"));
        assertTrue(str.contains("size=LARGE"));
        assertTrue(str.contains("outposts=1/3"));
        assertTrue(str.endsWith("}"));
    }

    @Test
    @DisplayName("Should handle all MoonSize constructors")
    void testAllMoonSizeConstructors() {
        for (Moon.MoonSize size : Moon.MoonSize.values()) {
            Moon moon = new Moon(1, size);
            assertEquals(size, moon.getMoonSize());
            assertEquals(size.getBaseMineralYield(), moon.getMineralYield());
            assertEquals(size.getBaseOutpostSlots(), moon.getOutpostSlots());
        }
    }

    @Test
    @DisplayName("Should have correct default tidal strength per size")
    void testDefaultTidalStrengthPerSize() {
        assertEquals(0.05, new Moon(1, Moon.MoonSize.TINY).getTidalStrength(), 0.001);
        assertEquals(0.1, new Moon(1, Moon.MoonSize.SMALL).getTidalStrength(), 0.001);
        assertEquals(0.15, new Moon(1, Moon.MoonSize.MEDIUM).getTidalStrength(), 0.001);
        assertEquals(0.2, new Moon(1, Moon.MoonSize.LARGE).getTidalStrength(), 0.001);
        assertEquals(0.3, new Moon(1, Moon.MoonSize.HUGE).getTidalStrength(), 0.001);
    }

    @Test
    @DisplayName("Should handle strategic resource change")
    void testStrategicResourceChange() {
        Moon moon = new Moon(1);
        moon.setStrategicResource(StrategicResource.ELERIUM);
        assertEquals(StrategicResource.ELERIUM, moon.getStrategicResource());

        moon.setStrategicResource(StrategicResource.ANTI_MATTER);
        assertEquals(StrategicResource.ANTI_MATTER, moon.getStrategicResource());
        assertTrue(moon.hasStrategicDeposit());
    }

    @Test
    @DisplayName("Should handle unbind and rebind planet")
    void testUnbindRebindPlanet() {
        Moon moon = new Moon(1);
        moon.setParentPlanet(100);
        assertEquals(100, moon.getParentPlanetId());

        moon.unbindFromPlanet();
        assertNull(moon.getParentPlanetId());

        moon.setParentPlanet(200);
        assertEquals(200, moon.getParentPlanetId());
    }

    @Test
    @DisplayName("Should handle large radius moon")
    void testLargeRadiusMoon() {
        Moon moon = new Moon(1, 20);
        assertEquals(20, moon.getRadius());
        assertTrue(moon.size() > 100);
    }

    @Test
    @DisplayName("Should handle zero mineral yield")
    void testZeroMineralYield() {
        Moon moon = new Moon(1);
        moon.setMineralYield(0);
        assertEquals(0, moon.getMineralYield());
        assertEquals(0, moon.getEffectiveMineralYield());
    }

    @Test
    @DisplayName("Should handle zero research yield")
    void testZeroResearchYield() {
        Moon moon = new Moon(1);
        moon.setResearchYield(0);
        assertEquals(0, moon.getResearchYield());
        assertEquals(0, moon.getEffectiveResearchYield());
    }

    @Test
    @DisplayName("Should handle zero outpost slots")
    void testZeroOutpostSlots() {
        Moon moon = new Moon(1);
        moon.setOutpostSlots(0);
        assertEquals(0, moon.getOutpostSlots());
        assertFalse(moon.hasAvailableOutpostSlot());
        assertFalse(moon.addOutpost("Outpost-1"));
    }

    @Test
    @DisplayName("Should handle zero tidal strength")
    void testZeroTidalStrength() {
        Moon moon = new Moon(1);
        moon.setTidalStrength(0.0);
        assertEquals(0.0, moon.getTidalStrength());
        assertEquals(0.0, moon.getTidalMineralBonus());
        assertEquals(0.0, moon.getTidalStabilityReduction());
    }

    @Test
    @DisplayName("Should handle maximum tidal strength")
    void testMaximumTidalStrength() {
        Moon moon = new Moon(1);
        moon.setTidalStrength(1.0);
        assertEquals(1.0, moon.getTidalStrength());
        assertEquals(0.5, moon.getTidalMineralBonus());
        assertEquals(0.3, moon.getTidalStabilityReduction());
    }

    @Test
    @DisplayName("Should handle hex operations on small moon")
    void testHexOperationsSmallMoon() {
        Moon moon = new Moon(1, 1);
        assertTrue(moon.contains(0, 0));
        Hex3D center = new Hex3D(0, 0, 0);
        List<Hex3D> neighbors = moon.getNeighbors(center);
        assertTrue(neighbors.size() > 0);
    }

    @Test
    @DisplayName("Should handle equals with same instance")
    void testEqualsSameInstance() {
        Moon moon = new Moon(1);
        assertEquals(moon, moon);
    }

    @Test
    @DisplayName("Should handle hashCode consistency")
    void testHashCodeConsistency() {
        Moon moon = new Moon(1);
        int hash1 = moon.hashCode();
        int hash2 = moon.hashCode();
        assertEquals(hash1, hash2);
    }
}
