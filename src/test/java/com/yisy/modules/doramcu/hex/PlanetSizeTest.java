package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlanetSize Enum Tests")
class PlanetSizeTest {

    @Test
    @DisplayName("values - should have 4 planet sizes")
    void values_shouldHaveFourSizes() {
        PlanetSize[] sizes = PlanetSize.values();

        assertEquals(4, sizes.length);
    }

    @Test
    @DisplayName("SMALL - should have correct stats")
    void small_shouldHaveCorrectStats() {
        PlanetSize small = PlanetSize.SMALL;

        assertEquals(5, small.getMaxPopulation());
        assertEquals(5, small.getBaseRadius());
        assertEquals(3, small.getBaseMineralYield());
        assertEquals(4, small.getBaseFoodYield());
        assertEquals(2, small.getBaseResearchYield());
        assertEquals(3, small.getBuildingSlots());
    }

    @Test
    @DisplayName("MEDIUM - should have correct stats")
    void medium_shouldHaveCorrectStats() {
        PlanetSize medium = PlanetSize.MEDIUM;

        assertEquals(10, medium.getMaxPopulation());
        assertEquals(8, medium.getBaseRadius());
        assertEquals(5, medium.getBaseMineralYield());
        assertEquals(6, medium.getBaseFoodYield());
        assertEquals(4, medium.getBaseResearchYield());
        assertEquals(5, medium.getBuildingSlots());
    }

    @Test
    @DisplayName("LARGE - should have correct stats")
    void large_shouldHaveCorrectStats() {
        PlanetSize large = PlanetSize.LARGE;

        assertEquals(15, large.getMaxPopulation());
        assertEquals(10, large.getBaseRadius());
        assertEquals(8, large.getBaseMineralYield());
        assertEquals(8, large.getBaseFoodYield());
        assertEquals(6, large.getBaseResearchYield());
        assertEquals(7, large.getBuildingSlots());
    }

    @Test
    @DisplayName("HUGE - should have correct stats")
    void huge_shouldHaveCorrectStats() {
        PlanetSize huge = PlanetSize.HUGE;

        assertEquals(20, huge.getMaxPopulation());
        assertEquals(12, huge.getBaseRadius());
        assertEquals(12, huge.getBaseMineralYield());
        assertEquals(10, huge.getBaseFoodYield());
        assertEquals(8, huge.getBaseResearchYield());
        assertEquals(10, huge.getBuildingSlots());
    }

    @Test
    @DisplayName("Max population should increase with size")
    void maxPopulation_shouldIncreaseWithSize() {
        assertTrue(PlanetSize.SMALL.getMaxPopulation() < PlanetSize.MEDIUM.getMaxPopulation());
        assertTrue(PlanetSize.MEDIUM.getMaxPopulation() < PlanetSize.LARGE.getMaxPopulation());
        assertTrue(PlanetSize.LARGE.getMaxPopulation() < PlanetSize.HUGE.getMaxPopulation());
    }

    @Test
    @DisplayName("Building slots should increase with size")
    void buildingSlots_shouldIncreaseWithSize() {
        assertTrue(PlanetSize.SMALL.getBuildingSlots() < PlanetSize.MEDIUM.getBuildingSlots());
        assertTrue(PlanetSize.MEDIUM.getBuildingSlots() < PlanetSize.LARGE.getBuildingSlots());
        assertTrue(PlanetSize.LARGE.getBuildingSlots() < PlanetSize.HUGE.getBuildingSlots());
    }

    @Test
    @DisplayName("Base radius should increase with size")
    void baseRadius_shouldIncreaseWithSize() {
        assertTrue(PlanetSize.SMALL.getBaseRadius() < PlanetSize.MEDIUM.getBaseRadius());
        assertTrue(PlanetSize.MEDIUM.getBaseRadius() < PlanetSize.LARGE.getBaseRadius());
        assertTrue(PlanetSize.LARGE.getBaseRadius() < PlanetSize.HUGE.getBaseRadius());
    }

    @Test
    @DisplayName("valueOf - should resolve by name")
    void valueOf_shouldResolveByName() {
        assertSame(PlanetSize.LARGE, PlanetSize.valueOf("LARGE"));
    }

    @Test
    @DisplayName("valueOf - should throw on invalid name")
    void valueOf_shouldThrowOnInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> PlanetSize.valueOf("INVALID"));
    }
}
