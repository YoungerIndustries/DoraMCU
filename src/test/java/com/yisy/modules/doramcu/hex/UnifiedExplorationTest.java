package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedExploration Tests")
class UnifiedExplorationTest {

    @Test
    @DisplayName("Should create exploration")
    void testCreateExploration() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A055430-A", 0, 0);
        UnifiedExploration exploration = new UnifiedExploration(planet, world);
        assertEquals(planet, exploration.getCelestialBody());
        assertEquals(world, exploration.getWorld());
    }

    @Test
    @DisplayName("Should create without world")
    void testCreateWithoutWorld() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        UnifiedExploration exploration = new UnifiedExploration(planet, null);
        assertEquals(planet, exploration.getCelestialBody());
        assertNull(exploration.getWorld());
    }

    @Test
    @DisplayName("Should survey body")
    void testSurveyBody() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A055430-A", 0, 0);
        UnifiedExploration exploration = new UnifiedExploration(planet, world);
        ExplorationActivity result = exploration.surveyBody(true);
        assertNotNull(result);
        assertEquals(1, exploration.getActivities().size());
    }

    @Test
    @DisplayName("Should resource scan")
    void testResourceScan() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A055430-A", 0, 0);
        UnifiedExploration exploration = new UnifiedExploration(planet, world);
        ExplorationActivity result = exploration.resourceScan(true);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should hazard detection")
    void testHazardDetection() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A055430-A", 0, 0);
        UnifiedExploration exploration = new UnifiedExploration(planet, world);
        ExplorationActivity result = exploration.hazardDetection(true);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should deploy beacon")
    void testDeployBeacon() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A055430-A", 0, 0);
        UnifiedExploration exploration = new UnifiedExploration(planet, world);
        exploration.deployBeacon();
        // May or may not succeed depending on random
        assertTrue(exploration.getActivities().size() >= 1);
    }

    @Test
    @DisplayName("Should not deploy beacon without world")
    void testDeployBeaconNoWorld() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        UnifiedExploration exploration = new UnifiedExploration(planet, null);
        assertFalse(exploration.deployBeacon());
    }

    @Test
    @DisplayName("Should get discoveries")
    void testGetDiscoveries() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A055430-A", 0, 0);
        UnifiedExploration exploration = new UnifiedExploration(planet, world);
        assertTrue(exploration.getDiscoveries().isEmpty());
    }

    @Test
    @DisplayName("Should get event log")
    void testEventLog() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A050430-A", 0, 0);
        UnifiedExploration exploration = new UnifiedExploration(planet, world);
        // Run multiple surveys to ensure at least one succeeds
        for (int i = 0; i < 5; i++) {
            exploration.surveyBody(true);
        }
        assertTrue(exploration.getEventLog().size() >= 1);
    }

    @Test
    @DisplayName("Should get summary")
    void testSummary() {
        Planet planet = new Planet(1, PlanetSize.MEDIUM, MineralQuality.RICH);
        TravellerWorld world = new TravellerWorld(0, "TestWorld", "A055430-A", 0, 0);
        UnifiedExploration exploration = new UnifiedExploration(planet, world);
        String summary = exploration.getSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Exploration"));
    }
}
