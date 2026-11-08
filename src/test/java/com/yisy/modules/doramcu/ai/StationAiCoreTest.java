package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

    // ---- Constructor and Accessors ----

    @Test
    @DisplayName("should create AI core for military station")
    void createMilitaryAiCore() {
        assertEquals(station, aiCore.getStation());
        assertEquals(SpaceStationType.MILITARY, aiCore.getStation().getStationType());
    }

    @Test
    @DisplayName("should throw on null station")
    void nullStationThrows() {
        assertThrows(IllegalArgumentException.class, () -> new StationAiCore(null));
    }

    @Test
    @DisplayName("should throw on null level")
    void nullLevelThrows() {
        assertThrows(IllegalArgumentException.class, () -> aiCore.setAiCoreLevel(null));
    }

    @Test
    @DisplayName("should not have AI core by default (NONE level)")
    void noAiCoreByDefault() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        assertFalse(core.hasAiCore());
        assertEquals(0, core.getLevelValue());
        assertEquals(StationAiCore.Level.NONE, core.getAiCoreLevel());
    }

    @Test
    @DisplayName("should set and get AI core level")
    void setAiCoreLevel() {
        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        station.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(StationAiCore.Level.ADVANCED.getValue(), station.getAiCoreLevel());
        assertTrue(station.hasAiCore());
    }

    @Test
    @DisplayName("should support all AI core levels")
    void allAiCoreLevels() {
        for (StationAiCore.Level level : StationAiCore.Level.values()) {
            SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
            s.setPosition(new Hex3D(0, 0, 0));
            StationAiCore core = new StationAiCore(s);
            core.setAiCoreLevel(level);
            assertEquals(level, core.getAiCoreLevel());
            assertEquals(level.getValue(), core.getLevelValue());
        }
    }

    @Test
    @DisplayName("should return correct hasAiCore for each level")
    void hasAiCorePerLevel() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);

        assertFalse(core.hasAiCore()); // NONE
        core.setAiCoreLevel(StationAiCore.Level.BASIC);
        assertTrue(core.hasAiCore());
        core.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertTrue(core.hasAiCore());
        core.setAiCoreLevel(StationAiCore.Level.ELITE);
        assertTrue(core.hasAiCore());
    }

    // ---- Defense Bonus ----

    @Test
    @DisplayName("should provide defense bonus for military station")
    void militaryDefenseBonus() {
        assertTrue(aiCore.getDefenseBonus() > 0.0);
    }

    @Test
    @DisplayName("should return zero defense bonus when no AI core")
    void noDefenseBonusWithoutAiCore() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        assertEquals(0.0, core.getDefenseBonus(), 0.001);
    }

    @Test
    @DisplayName("should scale defense bonus by level")
    void defenseBonusScalesByLevel() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);

        core.setAiCoreLevel(StationAiCore.Level.BASIC);
        assertEquals(0.05, core.getDefenseBonus(), 0.001); // Level 1: 5%

        core.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(0.10, core.getDefenseBonus(), 0.001); // Level 2: 10%

        core.setAiCoreLevel(StationAiCore.Level.ELITE);
        assertEquals(0.15, core.getDefenseBonus(), 0.001); // Level 3: 15%
    }

    // ---- Support Radius ----

    @Test
    @DisplayName("should have correct support radius")
    void supportRadius() {
        int radius = aiCore.getSupportRadius();
        assertTrue(radius > 0 && radius <= 5);
    }

    @Test
    @DisplayName("should return zero support radius when no AI core")
    void noSupportRadiusWithoutAiCore() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        assertEquals(0, core.getSupportRadius());
    }

    @Test
    @DisplayName("should scale support radius by level")
    void supportRadiusScalesByLevel() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);

        core.setAiCoreLevel(StationAiCore.Level.BASIC);
        assertEquals(3, core.getSupportRadius()); // 2 + 1

        core.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(4, core.getSupportRadius()); // 2 + 2

        core.setAiCoreLevel(StationAiCore.Level.ELITE);
        assertEquals(5, core.getSupportRadius()); // 2 + 3
    }

    // ---- Economic Bonuses ----

    @Test
    @DisplayName("should scale economic efficiency bonus by level")
    void economicEfficiencyBonusScalesByLevel() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.TRADE);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);

        assertEquals(0.0, core.getEconomicEfficiencyBonus(), 0.001); // NONE
        core.setAiCoreLevel(StationAiCore.Level.BASIC);
        assertEquals(0.05, core.getEconomicEfficiencyBonus(), 0.001);
        core.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(0.10, core.getEconomicEfficiencyBonus(), 0.001);
        core.setAiCoreLevel(StationAiCore.Level.ELITE);
        assertEquals(0.15, core.getEconomicEfficiencyBonus(), 0.001);
    }

    @Test
    @DisplayName("should scale mining bonus by level")
    void miningBonusScalesByLevel() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MINING);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);

        assertEquals(0.0, core.getMiningBonus(), 0.001); // NONE
        core.setAiCoreLevel(StationAiCore.Level.BASIC);
        assertEquals(0.10, core.getMiningBonus(), 0.001);
        core.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(0.20, core.getMiningBonus(), 0.001);
        core.setAiCoreLevel(StationAiCore.Level.ELITE);
        assertEquals(0.30, core.getMiningBonus(), 0.001);
    }

    @Test
    @DisplayName("should scale assembly bonus by level")
    void assemblyBonusScalesByLevel() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.SHIPYARD);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);

        assertEquals(0.0, core.getAssemblyBonus(), 0.001); // NONE
        core.setAiCoreLevel(StationAiCore.Level.BASIC);
        assertEquals(0.05, core.getAssemblyBonus(), 0.001);
        core.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(0.10, core.getAssemblyBonus(), 0.001);
        core.setAiCoreLevel(StationAiCore.Level.ELITE);
        assertEquals(0.15, core.getAssemblyBonus(), 0.001);
    }

    @Test
    @DisplayName("should scale growth bonus by level")
    void growthBonusScalesByLevel() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.COLONY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);

        assertEquals(0.0, core.getGrowthBonus(), 0.001); // NONE
        core.setAiCoreLevel(StationAiCore.Level.BASIC);
        assertEquals(0.05, core.getGrowthBonus(), 0.001);
        core.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(0.10, core.getGrowthBonus(), 0.001);
        core.setAiCoreLevel(StationAiCore.Level.ELITE);
        assertEquals(0.15, core.getGrowthBonus(), 0.001);
    }

    @Test
    @DisplayName("should return research points equal to level")
    void researchPointsPerTurn() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.RESEARCH);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);

        assertEquals(0, core.getResearchPointsPerTurn()); // NONE
        core.setAiCoreLevel(StationAiCore.Level.BASIC);
        assertEquals(1, core.getResearchPointsPerTurn());
        core.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(2, core.getResearchPointsPerTurn());
        core.setAiCoreLevel(StationAiCore.Level.ELITE);
        assertEquals(3, core.getResearchPointsPerTurn());
    }

    // ---- Station Type Support ----

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

    // ---- Insights ----

    @Test
    @DisplayName("should provide insights")
    void getInsights() {
        List<StationAiInsight> insights = aiCore.getInsights();
        assertNotNull(insights);
    }

    @Test
    @DisplayName("should return empty insights initially")
    void emptyInsightsInitially() {
        assertTrue(aiCore.getInsights().isEmpty());
    }

    @Test
    @DisplayName("should return all insight types")
    void allInsightTypes() {
        StationAiInsight.InsightType[] types = StationAiInsight.InsightType.values();
        assertEquals(7, types.length);
    }

    // ---- processTurn() Tests ----

    @Test
    @DisplayName("should skip processTurn when no AI core installed")
    void processTurnNoAiCore() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        // No level set — should be NONE
        core.processTurn(null);
        assertTrue(core.getInsights().isEmpty());
    }

    @Test
    @DisplayName("should process military station with null context")
    void processMilitaryStationNullContext() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        core.setAiCoreLevel(StationAiCore.Level.BASIC);
        core.processTurn(null);
        // With null context, military station should produce no threat insights
        assertTrue(core.getInsights().isEmpty());
    }

    @Test
    @DisplayName("should process military station and detect nearby enemy fleet")
    void processMilitaryStationDetectThreat() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        core.setAiCoreLevel(StationAiCore.Level.BASIC);

        Fleet enemyFleet = new Fleet(100, "Enemy Fleet");
        enemyFleet.setPosition(new Hex3D(1, 0, -1)); // distance 1 from station

        AiDecisionContext context = AiDecisionContext.builder()
                .ownedFleets(new ArrayList<>())
                .enemyFleets(List.of(enemyFleet))
                .build();

        core.processTurn(context);
        List<StationAiInsight> insights = core.getInsights();
        assertFalse(insights.isEmpty());
        assertTrue(insights.stream().anyMatch(i -> i.getType() == StationAiInsight.InsightType.THREAT_DETECTED));
    }

    @Test
    @DisplayName("should not detect enemy fleet beyond support radius")
    void processMilitaryStationEnemyOutOfRange() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        core.setAiCoreLevel(StationAiCore.Level.BASIC); // radius = 3

        Fleet enemyFleet = new Fleet(100, "Enemy Fleet");
        enemyFleet.setPosition(new Hex3D(10, 0, -10)); // distance 10, far beyond radius

        AiDecisionContext context = AiDecisionContext.builder()
                .ownedFleets(new ArrayList<>())
                .enemyFleets(List.of(enemyFleet))
                .build();

        core.processTurn(context);
        List<StationAiInsight> insights = core.getInsights();
        assertFalse(insights.stream().anyMatch(i -> i.getType() == StationAiInsight.InsightType.THREAT_DETECTED));
    }

    @Test
    @DisplayName("should process research station and generate research insight")
    void processResearchStation() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.RESEARCH);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        core.setAiCoreLevel(StationAiCore.Level.ADVANCED); // 2 research points

        core.processTurn(null);
        List<StationAiInsight> insights = core.getInsights();
        assertEquals(1, insights.size());
        assertEquals(StationAiInsight.InsightType.RESEARCH_OUTPUT, insights.get(0).getType());
        assertTrue(insights.get(0).getDescription().contains("2"));
    }

    @Test
    @DisplayName("should process trade station and generate economic insight")
    void processTradeStation() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.TRADE);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        core.setAiCoreLevel(StationAiCore.Level.ELITE); // 15% efficiency

        core.processTurn(null);
        List<StationAiInsight> insights = core.getInsights();
        assertEquals(1, insights.size());
        assertEquals(StationAiInsight.InsightType.ECONOMIC_OPTIMIZATION, insights.get(0).getType());
        assertTrue(insights.get(0).getDescription().contains("15"));
    }

    @Test
    @DisplayName("should process mining station and generate resource insight")
    void processMiningStation() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MINING);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        core.setAiCoreLevel(StationAiCore.Level.BASIC); // 10% mining bonus

        core.processTurn(null);
        List<StationAiInsight> insights = core.getInsights();
        assertEquals(1, insights.size());
        assertEquals(StationAiInsight.InsightType.RESOURCE_EXTRACTION, insights.get(0).getType());
        assertTrue(insights.get(0).getDescription().contains("10"));
    }

    @Test
    @DisplayName("should process shipyard station and generate production insight")
    void processShipyardStation() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.SHIPYARD);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        core.setAiCoreLevel(StationAiCore.Level.ADVANCED); // 10% assembly bonus

        core.processTurn(null);
        List<StationAiInsight> insights = core.getInsights();
        assertEquals(1, insights.size());
        assertEquals(StationAiInsight.InsightType.PRODUCTION_SCHEDULING, insights.get(0).getType());
        assertTrue(insights.get(0).getDescription().contains("10"));
    }

    @Test
    @DisplayName("should process colony station and generate growth insight")
    void processColonyStation() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.COLONY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        core.setAiCoreLevel(StationAiCore.Level.BASIC); // 5% growth bonus

        core.processTurn(null);
        List<StationAiInsight> insights = core.getInsights();
        assertEquals(1, insights.size());
        assertEquals(StationAiInsight.InsightType.COLONY_MANAGEMENT, insights.get(0).getType());
        assertTrue(insights.get(0).getDescription().contains("5"));
    }

    @Test
    @DisplayName("should clear insights on each processTurn call")
    void processTurnClearsInsights() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.RESEARCH);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);
        core.setAiCoreLevel(StationAiCore.Level.BASIC);

        core.processTurn(null);
        assertEquals(1, core.getInsights().size());

        core.processTurn(null);
        assertEquals(1, core.getInsights().size()); // cleared and regenerated
    }

    // ---- Fleet Support ----

    @Test
    @DisplayName("should return zero fleet support when no AI core")
    void fleetSupportNoAiCore() {
        SpaceStation s = new SpaceStation(1, SpaceStationType.MILITARY);
        s.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core = new StationAiCore(s);

        Fleet fleet = new Fleet(1, "Test Fleet");
        fleet.setPosition(new Hex3D(1, 0, -1));
        assertEquals(0.0, core.getFleetSupportBonus(fleet), 0.001);
    }

    @Test
    @DisplayName("should return zero fleet support for null fleet")
    void fleetSupportNullFleet() {
        assertEquals(0.0, aiCore.getFleetSupportBonus(null), 0.001);
    }

    @Test
    @DisplayName("should return zero fleet support for fleet without position")
    void fleetSupportNoPosition() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        // No position set
        assertEquals(0.0, aiCore.getFleetSupportBonus(fleet), 0.001);
    }

    @Test
    @DisplayName("should return fleet support bonus for fleet in range")
    void fleetSupportInRange() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        fleet.setPosition(new Hex3D(1, 0, -1)); // distance 1 from station at (0,0,0)
        double bonus = aiCore.getFleetSupportBonus(fleet);
        assertTrue(bonus > 0.0, "Fleet in range should receive support bonus");
    }

    @Test
    @DisplayName("should return zero fleet support for fleet out of range")
    void fleetSupportOutOfRange() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        fleet.setPosition(new Hex3D(10, 0, -10)); // distance 10, far beyond radius 3
        assertEquals(0.0, aiCore.getFleetSupportBonus(fleet), 0.001);
    }

    @Test
    @DisplayName("should check fleet in range")
    void isFleetInRange() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        fleet.setPosition(new Hex3D(1, 0, -1)); // distance 1
        assertTrue(aiCore.isFleetInRange(fleet));
    }

    @Test
    @DisplayName("should check fleet out of range")
    void isFleetOutOfRange() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        fleet.setPosition(new Hex3D(10, 0, -10)); // distance 10
        assertFalse(aiCore.isFleetInRange(fleet));
    }

    @Test
    @DisplayName("should return false for null fleet in range check")
    void isFleetInRangeNull() {
        assertFalse(aiCore.isFleetInRange(null));
    }

    @Test
    @DisplayName("should return false for fleet without position in range check")
    void isFleetInRangeNoPosition() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        assertFalse(aiCore.isFleetInRange(fleet));
    }

    // ---- SpaceStation Delegation ----

    @Test
    @DisplayName("should set AI core level on station")
    void setAiCoreLevelOnStation() {
        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        station.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(StationAiCore.Level.ADVANCED.getValue(), station.getAiCoreLevel());
        assertTrue(station.hasAiCore());
    }

    @Test
    @DisplayName("station should not have AI core by default")
    void noAiCoreByDefaultOnStation() {
        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        assertFalse(station.hasAiCore());
        assertEquals(0, station.getAiCoreLevel());
    }

    @Test
    @DisplayName("station should throw on null AI core level")
    void stationSetAiCoreLevelNullThrows() {
        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> station.setAiCoreLevel(null));
    }

    @Test
    @DisplayName("station should lazily initialize AI core")
    void stationLazyAiCoreInit() {
        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        StationAiCore core1 = station.getAiCore();
        StationAiCore core2 = station.getAiCore();
        assertSame(core1, core2, "AI Core should be lazily initialized and reused");
    }

    @Test
    @DisplayName("station should return AI support radius")
    void stationAiSupportRadius() {
        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        station.setAiCoreLevel(StationAiCore.Level.ADVANCED);
        assertEquals(4, station.getAiSupportRadius()); // 2 + 2
    }

    @Test
    @DisplayName("station should return AI defense bonus")
    void stationAiDefenseBonus() {
        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));
        station.setAiCoreLevel(StationAiCore.Level.ELITE);
        assertEquals(0.15, station.getAiDefenseBonus(), 0.001);
    }

    // ---- StationAiInsight Tests ----

    @Test
    @DisplayName("should create insight with correct fields")
    void createInsight() {
        Hex3D pos = new Hex3D(1, 2, -3);
        StationAiInsight insight = new StationAiInsight(
                StationAiInsight.InsightType.THREAT_DETECTED,
                "Enemy detected", pos);

        assertEquals(StationAiInsight.InsightType.THREAT_DETECTED, insight.getType());
        assertEquals("Enemy detected", insight.getDescription());
        assertEquals(pos, insight.getPosition());
    }

    @Test
    @DisplayName("insight toString should include all fields")
    void insightToString() {
        StationAiInsight insight = new StationAiInsight(
                StationAiInsight.InsightType.DEFENSE_BONUS,
                "5% bonus", new Hex3D(0, 0, 0));
        String str = insight.toString();
        assertTrue(str.contains("DEFENSE_BONUS"));
        assertTrue(str.contains("5% bonus"));
    }
}
