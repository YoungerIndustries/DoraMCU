package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmpireAiCore Tests")
class EmpireAiCoreTest {

    private Player player;
    private EmpireAiCore aiCore;

    @BeforeEach
    void setUp() {
        player = new Player(1, "test@test.com", "Test Empire");
        player.setGovernmentType(Player.GovernmentType.DICTATORSHIP);
        player.setEthos(Player.Ethos.MILITARIST);
        aiCore = new EmpireAiCore(1, player, 5);
    }

    @Test
    @DisplayName("should create AI core with correct personality")
    void createAiCore() {
        assertEquals(1, aiCore.getId());
        assertEquals(player, aiCore.getPlayer());
        assertEquals(Player.GovernmentType.DICTATORSHIP, aiCore.getPersonality().getGovernmentType());
        assertEquals(Player.Ethos.MILITARIST, aiCore.getPersonality().getEthos());
        assertEquals(5, aiCore.getResearchLevel());
    }

    @Test
    @DisplayName("should clamp research level to [0, 10]")
    void clampResearchLevel() {
        EmpireAiCore low = new EmpireAiCore(1, player, -5);
        assertEquals(0, low.getResearchLevel());

        EmpireAiCore high = new EmpireAiCore(2, player, 15);
        assertEquals(10, high.getResearchLevel());

        EmpireAiCore normal = new EmpireAiCore(3, player, 5);
        assertEquals(5, normal.getResearchLevel());
    }

    @Test
    @DisplayName("should return wait decision when no fleets available")
    void decideNoFleets() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        AiDecision decision = aiCore.decide(context);
        assertEquals(AiDecision.ActionType.WAIT, decision.getActionType());
    }

    @Test
    @DisplayName("should return wait decision when context is null")
    void decideNullContext() {
        AiDecision decision = aiCore.decide(null);
        assertEquals(AiDecision.ActionType.WAIT, decision.getActionType());
    }

    @Test
    @DisplayName("should evaluate combat action based on aggression")
    void evaluateCombatAction() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        double score = aiCore.evaluateAction(AiDecision.ActionType.ENGAGE_COMBAT, context);
        // Dictatorship + Militarist should have high aggression
        assertTrue(score > 0.5, "Aggressive personality should score combat highly");
    }

    @Test
    @DisplayName("should evaluate retreat action based on risk tolerance")
    void evaluateRetreatAction() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        double score = aiCore.evaluateAction(AiDecision.ActionType.RETREAT, context);
        // High risk tolerance means lower retreat score
        assertTrue(score >= 0.0 && score <= 1.0);
    }

    @Test
    @DisplayName("should find nearest enemy fleet")
    void findNearestEnemy() {
        Fleet myFleet = new Fleet(1, "My Fleet");
        myFleet.setPosition(new Hex3D(0, 0, 0));
        Ship ship = new Ship(1, ShipType.FRIGATE);
        myFleet.addShip(ship);

        Fleet enemy1 = new Fleet(2, "Enemy 1");
        enemy1.setPosition(new Hex3D(3, -3, 0));
        enemy1.addShip(new Ship(2, ShipType.FRIGATE));

        Fleet enemy2 = new Fleet(3, "Enemy 2");
        enemy2.setPosition(new Hex3D(10, -10, 0));
        enemy2.addShip(new Ship(3, ShipType.FRIGATE));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(List.of(myFleet))
                .enemyFleets(List.of(enemy1, enemy2))
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        Fleet nearest = aiCore.findNearestEnemy(context);
        assertNotNull(nearest);
        assertEquals(2, nearest.getId()); // enemy1 is closer
    }

    @Test
    @DisplayName("should return null when no enemies")
    void findNearestEnemyNone() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        Fleet nearest = aiCore.findNearestEnemy(context);
        assertNull(nearest);
    }

    @Test
    @DisplayName("should select best fleet by ship count")
    void selectBestFleet() {
        Fleet fleet1 = new Fleet(1, "Fleet 1");
        fleet1.addShip(new Ship(1, ShipType.FRIGATE));

        Fleet fleet2 = new Fleet(2, "Fleet 2");
        fleet2.addShip(new Ship(2, ShipType.FRIGATE));
        fleet2.addShip(new Ship(3, ShipType.FRIGATE));
        fleet2.addShip(new Ship(4, ShipType.FRIGATE));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(List.of(fleet1, fleet2))
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        Fleet best = aiCore.selectBestFleet(context);
        assertNotNull(best);
        assertEquals(2, best.getId()); // fleet2 has more ships
    }

    @Test
    @DisplayName("should choose ship type based on resources and personality")
    void chooseShipType() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(60)
                .energy(40)
                .food(30)
                .credits(50)
                .build();

        ShipType type = aiCore.chooseShipType(context);
        assertNotNull(type);
        // Aggressive personality with sufficient resources should prefer larger ships
        assertEquals(ShipType.CRUISER, type);
    }

    @Test
    @DisplayName("should have tactical and strategic cores")
    void hasSubCores() {
        assertNotNull(aiCore.getTacticalCore());
        assertNotNull(aiCore.getStrategicCore());
    }

    @Test
    @DisplayName("toString should not throw")
    void toStringDoesNotThrow() {
        String str = aiCore.toString();
        assertNotNull(str);
        assertTrue(str.contains("EmpireAiCore"));
        assertTrue(str.contains("Test Empire"));
    }

    @Test
    @DisplayName("should map DL actions correctly")
    void toDlAction() {
        assertEquals(com.yisy.modules.doradl.model.ActionReward.Action.ENGAGE_COMBAT,
                EmpireAiCore.toDlAction(AiDecision.ActionType.ENGAGE_COMBAT));
        assertEquals(com.yisy.modules.doradl.model.ActionReward.Action.MOVE_FLEET,
                EmpireAiCore.toDlAction(AiDecision.ActionType.MOVE_FLEET));
        assertEquals(com.yisy.modules.doradl.model.ActionReward.Action.BUILD_SHIP,
                EmpireAiCore.toDlAction(AiDecision.ActionType.BUILD_SHIP));
    }

    @Test
    @DisplayName("should map DL actions back correctly")
    void fromDlAction() {
        assertEquals(AiDecision.ActionType.ENGAGE_COMBAT,
                EmpireAiCore.fromDlAction(com.yisy.modules.doradl.model.ActionReward.Action.ENGAGE_COMBAT));
        assertEquals(AiDecision.ActionType.MOVE_FLEET,
                EmpireAiCore.fromDlAction(com.yisy.modules.doradl.model.ActionReward.Action.MOVE_FLEET));
        assertEquals(AiDecision.ActionType.SCOUT,
                EmpireAiCore.fromDlAction(com.yisy.modules.doradl.model.ActionReward.Action.SCOUT_AREA));
    }
}
