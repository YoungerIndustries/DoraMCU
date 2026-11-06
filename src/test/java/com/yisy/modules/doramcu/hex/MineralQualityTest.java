package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MineralQuality Enum Tests")
class MineralQualityTest {

    @Test
    @DisplayName("values - should have 5 mineral qualities")
    void values_shouldHaveFiveQualities() {
        MineralQuality[] qualities = MineralQuality.values();

        assertEquals(5, qualities.length);
    }

    @Test
    @DisplayName("ULTRA_POOR - should have -0.4 bonus")
    void ultraPoor_shouldHaveCorrectBonus() {
        assertEquals(-0.4, MineralQuality.ULTRA_POOR.getProductionBonus());
    }

    @Test
    @DisplayName("POOR - should have -0.2 bonus")
    void poor_shouldHaveCorrectBonus() {
        assertEquals(-0.2, MineralQuality.POOR.getProductionBonus());
    }

    @Test
    @DisplayName("NORMAL - should have 0.0 bonus")
    void normal_shouldHaveCorrectBonus() {
        assertEquals(0.0, MineralQuality.NORMAL.getProductionBonus());
    }

    @Test
    @DisplayName("RICH - should have 0.2 bonus")
    void rich_shouldHaveCorrectBonus() {
        assertEquals(0.2, MineralQuality.RICH.getProductionBonus());
    }

    @Test
    @DisplayName("ULTRA_RICH - should have 0.4 bonus")
    void ultraRich_shouldHaveCorrectBonus() {
        assertEquals(0.4, MineralQuality.ULTRA_RICH.getProductionBonus());
    }

    @Test
    @DisplayName("Bonus should increase with quality")
    void bonus_shouldIncreaseWithQuality() {
        assertTrue(MineralQuality.ULTRA_POOR.getProductionBonus() < MineralQuality.POOR.getProductionBonus());
        assertTrue(MineralQuality.POOR.getProductionBonus() < MineralQuality.NORMAL.getProductionBonus());
        assertTrue(MineralQuality.NORMAL.getProductionBonus() < MineralQuality.RICH.getProductionBonus());
        assertTrue(MineralQuality.RICH.getProductionBonus() < MineralQuality.ULTRA_RICH.getProductionBonus());
    }

    @Test
    @DisplayName("valueOf - should resolve by name")
    void valueOf_shouldResolveByName() {
        assertSame(MineralQuality.RICH, MineralQuality.valueOf("RICH"));
    }

    @Test
    @DisplayName("valueOf - should throw on invalid name")
    void valueOf_shouldThrowOnInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> MineralQuality.valueOf("INVALID"));
    }
}
