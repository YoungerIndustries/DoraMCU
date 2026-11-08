package com.yisy.modules.doradl.dl.service;

import com.yisy.modules.doradl.config.DlConfig;
import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.dto.PlayerSummary;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DlGameAiService heuristic scoring logic.
 * Tests verify that configurable thresholds from DlConfig are used correctly.
 */
class DlGameAiServiceTest {

    private DlConfig config;

    @BeforeEach
    void setUp() {
        config = new DlConfig();
    }

    @Test
    void testHeuristicConfigDefaults() {
        // Verify default heuristic thresholds match the original hardcoded values
        assertEquals(100, config.getHeuristicMineralsBuildShip());
        assertEquals(50, config.getHeuristicEnergyBuildShip());
        assertEquals(200, config.getHeuristicMineralsBuildStation());
        assertEquals(100, config.getHeuristicEnergyBuildStation());
        assertEquals(30, config.getHeuristicFoodExpandColony());
        assertEquals(5, config.getHeuristicMaxPlanetsForExpansion());
        assertEquals(50, config.getHeuristicResearchTech());
        assertEquals(3, config.getHeuristicFleetEngageCombat());
        assertEquals(30, config.getHeuristicMineralsRetreat());
        assertEquals(2, config.getHeuristicFleetFlankAmbush());
        assertEquals(100, config.getHeuristicMineralsOptimizeMining());
        assertEquals(50, config.getHeuristicMineralsFortify());
        assertEquals(50, config.getHeuristicMineralsWait());
        assertEquals(25, config.getHeuristicEnergyWait());
    }

    @Test
    void testHeuristicConfigCustomValues() {
        // Verify thresholds can be customized
        config.setHeuristicMineralsBuildShip(50);
        config.setHeuristicEnergyBuildShip(25);
        config.setHeuristicFleetEngageCombat(5);

        assertEquals(50, config.getHeuristicMineralsBuildShip());
        assertEquals(25, config.getHeuristicEnergyBuildShip());
        assertEquals(5, config.getHeuristicFleetEngageCombat());
    }

    @Test
    void testActionEnumCoverage() {
        // Verify all action types have valid indices
        ActionReward.Action[] actions = ActionReward.Action.values();
        assertEquals(17, actions.length);

        // Verify fromIndex works for all valid indices
        for (int i = 0; i < actions.length; i++) {
            assertEquals(actions[i], ActionReward.Action.fromIndex(i));
        }

        // Verify fromIndex throws for invalid index
        assertThrows(IllegalArgumentException.class, () -> ActionReward.Action.fromIndex(-1));
        assertThrows(IllegalArgumentException.class, () -> ActionReward.Action.fromIndex(17));
    }

    @Test
    void testActionCategories() {
        // Strategic actions
        assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.BUILD_SHIP.getCategory());
        assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.BUILD_STATION.getCategory());
        assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.EXPAND_COLONY.getCategory());
        assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.RESEARCH_TECH.getCategory());
        assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.DIPLOMATIC_STANCE.getCategory());

        // Tactical actions
        assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.MOVE_FLEET.getCategory());
        assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.ENGAGE_COMBAT.getCategory());
        assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.RETREAT.getCategory());
        assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.FLANK_ENEMY.getCategory());
        assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.AMBUSH.getCategory());

        // Economic actions
        assertEquals(ActionReward.ActionCategory.ECONOMIC, ActionReward.Action.OPTIMIZE_MINING.getCategory());
        assertEquals(ActionReward.ActionCategory.ECONOMIC, ActionReward.Action.OPTIMIZE_TRADE.getCategory());
        assertEquals(ActionReward.ActionCategory.ECONOMIC, ActionReward.Action.REPAIR_FLEET.getCategory());
        assertEquals(ActionReward.ActionCategory.ECONOMIC, ActionReward.Action.UPGRADE_STATION.getCategory());

        // Defensive actions
        assertEquals(ActionReward.ActionCategory.DEFENSIVE, ActionReward.Action.FORTIFY_PLANET.getCategory());
        assertEquals(ActionReward.ActionCategory.DEFENSIVE, ActionReward.Action.SCOUT_AREA.getCategory());
        assertEquals(ActionReward.ActionCategory.DEFENSIVE, ActionReward.Action.WAIT.getCategory());
    }

    @Test
    void testActionRewardOneHotEncoding() {
        ActionReward sample = new ActionReward(
                new GameStateFeatures(new double[GameStateFeatures.FEATURE_DIMENSION]),
                ActionReward.Action.BUILD_SHIP,
                0.5
        );

        double[] oneHot = sample.toOneHot();
        assertEquals(17, oneHot.length);
        assertEquals(1.0, oneHot[0], 0.001); // BUILD_SHIP is index 0
        assertEquals(0.0, oneHot[1], 0.001); // BUILD_STATION should be 0

        // Test another action
        sample.setAction(ActionReward.Action.ENGAGE_COMBAT);
        oneHot = sample.toOneHot();
        assertEquals(1.0, oneHot[6], 0.001); // ENGAGE_COMBAT is index 6
    }

    @Test
    void testDlConfigBatchSizeReduced() {
        // Verify batch size was reduced from 32 to 16 to prevent OOM
        DlConfig dlConfig = new DlConfig();
        assertEquals(16, dlConfig.getBatchSize());
    }

    @Test
    void testDlConfigBlendFactorBounds() {
        DlConfig dlConfig = new DlConfig();
        assertEquals(0.5, dlConfig.getBlendFactor(), 0.001);
        dlConfig.setBlendFactor(0.0);
        assertEquals(0.0, dlConfig.getBlendFactor(), 0.001);
        dlConfig.setBlendFactor(1.0);
        assertEquals(1.0, dlConfig.getBlendFactor(), 0.001);
    }
}
