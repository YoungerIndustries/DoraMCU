package com.yisy.modules.doramcu.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AiDifficultyConfig configuration properties.
 */
class AiDifficultyConfigTest {

    @Test
    @DisplayName("Should create config with default easy values")
    void testEasyDefaults() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Easy easy = config.getEasy();

        assertNotNull(easy);
        assertEquals(150, easy.getBuildMinerals());
        assertEquals(80, easy.getBuildEnergy());
    }

    @Test
    @DisplayName("Should create config with default medium values")
    void testMediumDefaults() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Medium medium = config.getMedium();

        assertNotNull(medium);
        assertEquals(100, medium.getBuildMinerals());
        assertEquals(50, medium.getBuildEnergy());
        assertEquals(200, medium.getCruiserMinerals());
        assertEquals(100, medium.getCruiserEnergy());
        assertEquals(100, medium.getDestroyerMinerals());
        assertEquals(50, medium.getDestroyerEnergy());
    }

    @Test
    @DisplayName("Should create config with default hard values")
    void testHardDefaults() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Hard hard = config.getHard();

        assertNotNull(hard);
        assertEquals(80, hard.getBuildMinerals());
        assertEquals(40, hard.getBuildEnergy());
        assertEquals(3, hard.getMaxShipsPerTurn());
        assertEquals(300, hard.getBattleshipMinerals());
        assertEquals(150, hard.getBattleshipEnergy());
        assertEquals(200, hard.getCruiserMinerals());
        assertEquals(100, hard.getCruiserEnergy());
        assertEquals(100, hard.getDestroyerMinerals());
        assertEquals(50, hard.getDestroyerEnergy());
    }

    @Test
    @DisplayName("Should update easy values")
    void testEasySetters() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Easy easy = config.getEasy();

        easy.setBuildMinerals(200);
        easy.setBuildEnergy(100);

        assertEquals(200, easy.getBuildMinerals());
        assertEquals(100, easy.getBuildEnergy());
    }

    @Test
    @DisplayName("Should update medium values")
    void testMediumSetters() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Medium medium = config.getMedium();

        medium.setBuildMinerals(120);
        medium.setBuildEnergy(60);
        medium.setCruiserMinerals(250);
        medium.setCruiserEnergy(120);
        medium.setDestroyerMinerals(110);
        medium.setDestroyerEnergy(55);

        assertEquals(120, medium.getBuildMinerals());
        assertEquals(60, medium.getBuildEnergy());
        assertEquals(250, medium.getCruiserMinerals());
        assertEquals(120, medium.getCruiserEnergy());
        assertEquals(110, medium.getDestroyerMinerals());
        assertEquals(55, medium.getDestroyerEnergy());
    }

    @Test
    @DisplayName("Should update hard values")
    void testHardSetters() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Hard hard = config.getHard();

        hard.setBuildMinerals(50);
        hard.setBuildEnergy(25);
        hard.setMaxShipsPerTurn(5);
        hard.setBattleshipMinerals(400);
        hard.setBattleshipEnergy(200);
        hard.setCruiserMinerals(100);
        hard.setCruiserEnergy(50);
        hard.setDestroyerMinerals(60);
        hard.setDestroyerEnergy(30);

        assertEquals(50, hard.getBuildMinerals());
        assertEquals(25, hard.getBuildEnergy());
        assertEquals(5, hard.getMaxShipsPerTurn());
        assertEquals(400, hard.getBattleshipMinerals());
        assertEquals(200, hard.getBattleshipEnergy());
        assertEquals(100, hard.getCruiserMinerals());
        assertEquals(50, hard.getCruiserEnergy());
        assertEquals(60, hard.getDestroyerMinerals());
        assertEquals(30, hard.getDestroyerEnergy());
    }

    @Test
    @DisplayName("Should have easy requiring more resources than medium")
    void testEasyRequiresMoreResources() {
        AiDifficultyConfig config = new AiDifficultyConfig();

        // Easy should require more resources than medium (harder for AI to build)
        assertTrue(config.getEasy().getBuildMinerals() > config.getMedium().getBuildMinerals());
        assertTrue(config.getEasy().getBuildEnergy() > config.getMedium().getBuildEnergy());
    }

    @Test
    @DisplayName("Should have medium requiring more resources than hard")
    void testMediumRequiresMoreResources() {
        AiDifficultyConfig config = new AiDifficultyConfig();

        // Medium should require more resources than hard (easier for AI to build)
        assertTrue(config.getMedium().getBuildMinerals() > config.getHard().getBuildMinerals());
        assertTrue(config.getMedium().getBuildEnergy() > config.getHard().getBuildEnergy());
    }

    @Test
    @DisplayName("Should have cruiser costs higher than destroyer costs in medium")
    void testCruiserCostsHigherThanDestroyer() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Medium medium = config.getMedium();

        assertTrue(medium.getCruiserMinerals() > medium.getDestroyerMinerals());
        assertTrue(medium.getCruiserEnergy() > medium.getDestroyerEnergy());
    }

    @Test
    @DisplayName("Should have cruiser costs higher than destroyer costs in hard")
    void testCruiserCostsHigherThanDestroyerHard() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Hard hard = config.getHard();

        assertTrue(hard.getCruiserMinerals() > hard.getDestroyerMinerals());
        assertTrue(hard.getCruiserEnergy() > hard.getDestroyerEnergy());
    }

    @Test
    @DisplayName("Should return same instance for repeated getter calls")
    void testGetterReturnsSameInstance() {
        AiDifficultyConfig config = new AiDifficultyConfig();

        assertSame(config.getEasy(), config.getEasy());
        assertSame(config.getMedium(), config.getMedium());
        assertSame(config.getHard(), config.getHard());
    }

    @Test
    @DisplayName("Should have battleship costs higher than cruiser costs in hard")
    void testBattleshipCostsHigherThanCruiser() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Hard hard = config.getHard();

        assertTrue(hard.getBattleshipMinerals() > hard.getCruiserMinerals());
        assertTrue(hard.getBattleshipEnergy() > hard.getCruiserEnergy());
    }

    @Test
    @DisplayName("Should have hard maxShipsPerTurn greater than zero")
    void testHardMaxShipsPerTurn() {
        AiDifficultyConfig config = new AiDifficultyConfig();
        AiDifficultyConfig.Hard hard = config.getHard();

        assertTrue(hard.getMaxShipsPerTurn() > 0);
    }
}
