package com.yisy.modules.doramcu.hex.gui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.yisy.modules.doramcu.hex.Difficulty;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Difficulty enum.
 */
class GameCreationUIDifficultyTest {

    @Test
    @DisplayName("Should have 4 difficulty levels")
    void testDifficultyCount() {
        assertEquals(4, Difficulty.values().length);
    }

    @Test
    @DisplayName("Easy should have correct display name")
    void testEasyDisplayName() {
        assertEquals("Easy", Difficulty.EASY.getDisplayName());
    }

    @Test
    @DisplayName("Normal should have correct display name")
    void testNormalDisplayName() {
        assertEquals("Normal", Difficulty.NORMAL.getDisplayName());
    }

    @Test
    @DisplayName("Hard should have correct display name")
    void testHardDisplayName() {
        assertEquals("Hard", Difficulty.HARD.getDisplayName());
    }

    @Test
    @DisplayName("Veteran should have correct display name")
    void testVeteranDisplayName() {
        assertEquals("Veteran", Difficulty.VETERAN.getDisplayName());
    }

    @Test
    @DisplayName("Easy should have highest moon chance")
    void testEasyMoonChance() {
        assertEquals(0.6, Difficulty.EASY.getMoonChance());
    }

    @Test
    @DisplayName("Veteran should have lowest moon chance")
    void testVeteranMoonChance() {
        assertEquals(0.2, Difficulty.VETERAN.getMoonChance());
    }

    @Test
    @DisplayName("Easy should have highest asteroid belt chance")
    void testEasyAsteroidBeltChance() {
        assertEquals(0.6, Difficulty.EASY.getAsteroidBeltChance());
    }

    @Test
    @DisplayName("Veteran should have lowest asteroid belt chance")
    void testVeteranAsteroidBeltChance() {
        assertEquals(0.3, Difficulty.VETERAN.getAsteroidBeltChance());
    }

    @Test
    @DisplayName("Easy should have highest gas giant chance")
    void testEasyGasGiantChance() {
        assertEquals(0.4, Difficulty.EASY.getGasGiantChance());
    }

    @Test
    @DisplayName("Veteran should have lowest gas giant chance")
    void testVeteranGasGiantChance() {
        assertEquals(0.15, Difficulty.VETERAN.getGasGiantChance());
    }

    @Test
    @DisplayName("Easy should have highest nebula chance")
    void testEasyNebulaChance() {
        assertEquals(0.2, Difficulty.EASY.getNebulaChance());
    }

    @Test
    @DisplayName("Veteran should have lowest nebula chance")
    void testVeteranNebulaChance() {
        assertEquals(0.05, Difficulty.VETERAN.getNebulaChance());
    }

    @Test
    @DisplayName("Easy should have highest black hole chance")
    void testEasyBlackHoleChance() {
        assertEquals(0.1, Difficulty.EASY.getBlackHoleChance());
    }

    @Test
    @DisplayName("Veteran should have lowest black hole chance")
    void testVeteranBlackHoleChance() {
        assertEquals(0.02, Difficulty.VETERAN.getBlackHoleChance());
    }

    @Test
    @DisplayName("Normal should have highest comet chance")
    void testNormalHighestCometChance() {
        // Comet chance is not monotonic: EASY=0.03, NORMAL=0.05, HARD=0.03, VETERAN=0.02
        assertEquals(0.05, Difficulty.NORMAL.getCometChance());
    }

    @Test
    @DisplayName("Veteran should have lowest comet chance")
    void testVeteranCometChance() {
        assertEquals(0.02, Difficulty.VETERAN.getCometChance());
    }

    @Test
    @DisplayName("Easy should have highest space station chance")
    void testEasySpaceStationChance() {
        assertEquals(0.3, Difficulty.EASY.getSpaceStationChance());
    }

    @Test
    @DisplayName("Veteran should have lowest space station chance")
    void testVeteranSpaceStationChance() {
        assertEquals(0.1, Difficulty.VETERAN.getSpaceStationChance());
    }

    @Test
    @DisplayName("Easy should have lowest hyperlane density")
    void testEasyHyperlaneDensity() {
        assertEquals(0.5, Difficulty.EASY.getHyperlaneDensity());
    }

    @Test
    @DisplayName("Veteran should have highest hyperlane density")
    void testVeteranHyperlaneDensity() {
        assertEquals(2.0, Difficulty.VETERAN.getHyperlaneDensity());
    }

    @Test
    @DisplayName("Easy should have most planets per system")
    void testEasyPlanetsPerSystem() {
        assertEquals(7, Difficulty.EASY.getPlanetsPerSystem());
    }

    @Test
    @DisplayName("Veteran should have fewest planets per system")
    void testVeteranPlanetsPerSystem() {
        assertEquals(3, Difficulty.VETERAN.getPlanetsPerSystem());
    }

    @Test
    @DisplayName("Normal should have correct values")
    void testNormalValues() {
        Difficulty normal = Difficulty.NORMAL;
        assertEquals(0.4, normal.getMoonChance());
        assertEquals(0.5, normal.getAsteroidBeltChance());
        assertEquals(0.3, normal.getGasGiantChance());
        assertEquals(0.15, normal.getNebulaChance());
        assertEquals(0.05, normal.getBlackHoleChance());
        assertEquals(0.05, normal.getCometChance());
        assertEquals(0.2, normal.getSpaceStationChance());
        assertEquals(1.0, normal.getHyperlaneDensity());
        assertEquals(5, normal.getPlanetsPerSystem());
    }

    @Test
    @DisplayName("Hard should have correct values")
    void testHardValues() {
        Difficulty hard = Difficulty.HARD;
        assertEquals(0.3, hard.getMoonChance());
        assertEquals(0.4, hard.getAsteroidBeltChance());
        assertEquals(0.2, hard.getGasGiantChance());
        assertEquals(0.1, hard.getNebulaChance());
        assertEquals(0.03, hard.getBlackHoleChance());
        assertEquals(0.03, hard.getCometChance());
        assertEquals(0.15, hard.getSpaceStationChance());
        assertEquals(1.5, hard.getHyperlaneDensity());
        assertEquals(4, hard.getPlanetsPerSystem());
    }

    @Test
    @DisplayName("Difficulty should decrease most celestial body chances as difficulty increases")
    void testDifficultyDecreasesCelestialBodies() {
        Difficulty[] difficulties = Difficulty.values();

        for (int i = 0; i < difficulties.length - 1; i++) {
            assertTrue(difficulties[i].getMoonChance() >= difficulties[i + 1].getMoonChance(),
                    "Moon chance should decrease: " + difficulties[i] + " vs " + difficulties[i + 1]);
            assertTrue(difficulties[i].getAsteroidBeltChance() >= difficulties[i + 1].getAsteroidBeltChance(),
                    "Asteroid belt chance should decrease");
            assertTrue(difficulties[i].getGasGiantChance() >= difficulties[i + 1].getGasGiantChance(),
                    "Gas giant chance should decrease");
            assertTrue(difficulties[i].getNebulaChance() >= difficulties[i + 1].getNebulaChance(),
                    "Nebula chance should decrease");
            assertTrue(difficulties[i].getBlackHoleChance() >= difficulties[i + 1].getBlackHoleChance(),
                    "Black hole chance should decrease");
            // Note: Comet chance is NOT monotonic (EASY=0.03, NORMAL=0.05, HARD=0.03, VETERAN=0.02)
            assertTrue(difficulties[i].getSpaceStationChance() >= difficulties[i + 1].getSpaceStationChance(),
                    "Space station chance should decrease");
        }
    }

    @Test
    @DisplayName("Difficulty should increase hyperlane density as difficulty increases")
    void testDifficultyIncreasesHyperlaneDensity() {
        Difficulty[] difficulties = Difficulty.values();

        for (int i = 0; i < difficulties.length - 1; i++) {
            assertTrue(difficulties[i].getHyperlaneDensity() <= difficulties[i + 1].getHyperlaneDensity(),
                    "Hyperlane density should increase: " + difficulties[i] + " vs " + difficulties[i + 1]);
        }
    }

    @Test
    @DisplayName("Difficulty should decrease planets per system as difficulty increases")
    void testDifficultyDecreasesPlanetsPerSystem() {
        Difficulty[] difficulties = Difficulty.values();

        for (int i = 0; i < difficulties.length - 1; i++) {
            assertTrue(difficulties[i].getPlanetsPerSystem() >= difficulties[i + 1].getPlanetsPerSystem(),
                    "Planets per system should decrease: " + difficulties[i] + " vs " + difficulties[i + 1]);
        }
    }

    @Test
    @DisplayName("All chances should be between 0 and 1")
    void testAllChancesInRange() {
        for (Difficulty difficulty : Difficulty.values()) {
            assertTrue(difficulty.getMoonChance() >= 0 && difficulty.getMoonChance() <= 1,
                    "Moon chance out of range for " + difficulty);
            assertTrue(difficulty.getAsteroidBeltChance() >= 0 && difficulty.getAsteroidBeltChance() <= 1,
                    "Asteroid belt chance out of range for " + difficulty);
            assertTrue(difficulty.getGasGiantChance() >= 0 && difficulty.getGasGiantChance() <= 1,
                    "Gas giant chance out of range for " + difficulty);
            assertTrue(difficulty.getNebulaChance() >= 0 && difficulty.getNebulaChance() <= 1,
                    "Nebula chance out of range for " + difficulty);
            assertTrue(difficulty.getBlackHoleChance() >= 0 && difficulty.getBlackHoleChance() <= 1,
                    "Black hole chance out of range for " + difficulty);
            assertTrue(difficulty.getCometChance() >= 0 && difficulty.getCometChance() <= 1,
                    "Comet chance out of range for " + difficulty);
            assertTrue(difficulty.getSpaceStationChance() >= 0 && difficulty.getSpaceStationChance() <= 1,
                    "Space station chance out of range for " + difficulty);
        }
    }

    @Test
    @DisplayName("All hyperlane densities should be positive")
    void testHyperlaneDensityPositive() {
        for (Difficulty difficulty : Difficulty.values()) {
            assertTrue(difficulty.getHyperlaneDensity() > 0,
                    "Hyperlane density should be positive for " + difficulty);
        }
    }

    @Test
    @DisplayName("All planets per system should be positive")
    void testPlanetsPerSystemPositive() {
        for (Difficulty difficulty : Difficulty.values()) {
            assertTrue(difficulty.getPlanetsPerSystem() > 0,
                    "Planets per system should be positive for " + difficulty);
        }
    }

    @Test
    @DisplayName("valueOf should return correct enum")
    void testValueOf() {
        assertSame(Difficulty.EASY, Difficulty.valueOf("EASY"));
        assertSame(Difficulty.NORMAL, Difficulty.valueOf("NORMAL"));
        assertSame(Difficulty.HARD, Difficulty.valueOf("HARD"));
        assertSame(Difficulty.VETERAN, Difficulty.valueOf("VETERAN"));
    }

    @Test
    @DisplayName("valueOf should throw for invalid name")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Difficulty.valueOf("INVALID"));
    }
}
