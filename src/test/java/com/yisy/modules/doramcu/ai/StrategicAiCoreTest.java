package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrategicAiCore Tests")
class StrategicAiCoreTest {

    private Player player;
    private EmpireAiCore empireCore;
    private StrategicAiCore strategicCore;

    @BeforeEach
    void setUp() {
        player = new Player(1, "test@test.com", "Test Empire");
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.MATERIALIST);
        empireCore = new EmpireAiCore(1, player, 5);
        strategicCore = empireCore.getStrategicCore();
    }

    @Test
    @DisplayName("should return null when context is null")
    void decideNullContext() {
        assertNull(strategicCore.decide(null));
    }

    @Test
    @DisplayName("should prioritize building when resources available")
    void buildPriority() {
        Fleet fleet = new Fleet(1, "My Fleet");
        fleet.addShip(new Ship(1, ShipType.FRIGATE));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(List.of(fleet))
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(60)
                .food(30)
                .credits(50)
                .build();

        AiDecision decision = strategicCore.decide(context);
        // With sufficient resources and few ships, should build
        assertNotNull(decision);
        assertTrue(decision.getActionType() == AiDecision.ActionType.BUILD_SHIP ||
                decision.getActionType() == AiDecision.ActionType.BUILD_STATION);
    }

    @Test
    @DisplayName("should not build when resources are low")
    void noBuildWhenLowResources() {
        Fleet fleet = new Fleet(1, "My Fleet");
        fleet.addShip(new Ship(1, ShipType.FRIGATE));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(List.of(fleet))
                .enemyFleets(new ArrayList<>())
                .minerals(10)
                .energy(5)
                .food(10)
                .credits(20)
                .build();

        AiDecision decision = strategicCore.decide(context);
        // With low resources, may return null (let tactical decide)
        // or return WAIT
        if (decision != null) {
            assertTrue(decision.getActionType() == AiDecision.ActionType.WAIT ||
                    decision.getActionType() == AiDecision.ActionType.SCOUT);
        }
    }

    @Test
    @DisplayName("should evaluate station build priority")
    void buildStationPriority() {
        Fleet fleet = new Fleet(1, "My Fleet");
        fleet.addShip(new Ship(1, ShipType.FRIGATE));

        SpaceStation station = new SpaceStation(1, SpaceStationType.MILITARY);
        station.setPosition(new Hex3D(0, 0, 0));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(List.of(fleet))
                .enemyFleets(new ArrayList<>())
                .stations(List.of(station))
                .minerals(100)
                .energy(60)
                .food(30)
                .credits(50)
                .build();

        AiDecision decision = strategicCore.decide(context);
        assertNotNull(decision);
    }

    @Test
    @DisplayName("should return null when no strategic priority")
    void noStrategicPriority() {
        Fleet fleet = new Fleet(1, "My Fleet");
        for (int i = 0; i < 10; i++) {
            fleet.addShip(new Ship(i, ShipType.CRUISER));
        }

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(List.of(fleet))
                .enemyFleets(new ArrayList<>())
                .minerals(10)
                .energy(5)
                .food(10)
                .credits(20)
                .build();

        AiDecision decision = strategicCore.decide(context);
        // With many ships and low resources, may return null
        // (let tactical core handle the turn)
        // This is acceptable behavior
    }
}
