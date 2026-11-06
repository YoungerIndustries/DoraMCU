package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StationAiCore Tests")
class StationAiCoreTest {

    private SpaceStation station;
    private StationAiCore aiCore;

    @BeforeEach
    void setUp() {
        station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        aiCore = new StationAiCore(station);
        aiCore.setAiCoreLevel(StationAiCore.Level.BASIC);
    }

    @Test
    @DisplayName("should create AI core for military station")
    void createMilitaryAiCore() {
        assertEquals(station, aiCore.getStation());
        assertEquals(SpaceStationType.MILITARY, aiCore.getStation().getStationType());
    }

    @Test
    @DisplayName("should provide defense bonus for military station")
    void militaryDefenseBonus() {
        assertTrue(aiCore.getDefenseBonus() > 0.0);
    }

    @Test
    @DisplayName("should have correct support radius")
    void supportRadius() {
        int radius = aiCore.getSupportRadius();
        assertTrue(radius > 0 && radius <= 5);
    }

    @Test
    @DisplayName("should provide insights")
    void getInsights() {
        List<StationAiInsight> insights = aiCore.getInsights();
        assertNotNull(insights);
    }

    @Test
    @DisplayName("should support all station types")
    void allStationTypes() {
        for (SpaceStationType type : SpaceStationType.values()) {
            SpaceStation s = new SpaceStation(1, type);
            s.setPosition(new Hex3D(0, 0, 0));
            StationAiCore core = new StationAiCore(s);
            assertNotNull(core);
            assertEquals(type, core.getStation().getStationType());
        }
    }

    @Test
    @DisplayName("should set AI core level on station")
    void setAiCoreLevel() {
        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        station.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(StationAiCore.Level.ADVANCED.getValue(), station.getAiCoreLevel());
        assertTrue(station.hasAiCore());
    }

    @Test
    @DisplayName("station should not have AI core by default")
    void noAiCoreByDefault() {
        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        assertFalse(station.hasAiCore());
        assertEquals(0, station.getAiCoreLevel());
    }
}
