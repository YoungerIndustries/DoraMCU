package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TacticalAiCore Tests")
class TacticalAiCoreTest {

    private Player player;
    private EmpireAiCore empireCore;
    private TacticalAiCore tacticalCore;

    @BeforeEach
    void setUp() {
        player = new Player(1, "test@test.com", "Test Empire");
        player.setGovernmentType(Player.GovernmentType.DICTATORSHIP);
        player.setEthos(Player.Ethos.MILITARIST);
        empireCore = new EmpireAiCore(1, player, 5);
        tacticalCore = empireCore.getTacticalCore();
    }

    @Test
    @DisplayName("should return wait when no valid fleet")
    void decideNoFleet() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        AiDecision decision = tacticalCore.decide(context);
        assertEquals(AiDecision.ActionType.WAIT, decision.getActionType());
    }

    @Test
    @DisplayName("should engage combat when strong and adjacent")
    void engageCombat() {
        Fleet myFleet = new Fleet(1, "My Fleet");
        myFleet.setPosition(new Hex3D(0, 0, 0));
        for (int i = 0; i < 5; i++) {
            myFleet.addShip(new Ship(i, ShipType.CRUISER));
        }

        Fleet enemy = new Fleet(2, "Enemy Fleet");
        enemy.setPosition(new Hex3D(1, 0, -1)); // adjacent
        enemy.addShip(new Ship(5, ShipType.FRIGATE));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(List.of(myFleet))
                .enemyFleets(List.of(enemy))
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        AiDecision decision = tacticalCore.decide(context);
        assertEquals(AiDecision.ActionType.ENGAGE_COMBAT, decision.getActionType());
    }

    @Test
    @DisplayName("should retreat when weak and adjacent")
    void retreat() {
        Player weakPlayer = new Player(1, "weak@test.com", "Weak Empire");
        weakPlayer.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        weakPlayer.setEthos(Player.Ethos.PACIFIST);
        EmpireAiCore weakCore = new EmpireAiCore(1, weakPlayer, 2);

        Fleet myFleet = new Fleet(1, "Weak Fleet");
        myFleet.setPosition(new Hex3D(0, 0, 0));
        myFleet.addShip(new Ship(1, ShipType.FRIGATE));

        Fleet enemy = new Fleet(2, "Enemy Fleet");
        enemy.setPosition(new Hex3D(1, 0, -1)); // adjacent
        for (int i = 0; i < 5; i++) {
            enemy.addShip(new Ship(i + 2, ShipType.CRUISER));
        }

        AiDecisionContext context = AiDecisionContext.builder()
                .player(weakPlayer)
                .ownedFleets(List.of(myFleet))
                .enemyFleets(List.of(enemy))
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        AiDecision decision = weakCore.getTacticalCore().decide(context);
        // Should retreat or move away
        assertTrue(decision.getActionType() == AiDecision.ActionType.RETREAT ||
                decision.getActionType() == AiDecision.ActionType.MOVE_FLEET);
    }

    @Test
    @DisplayName("should scout when no enemies")
    void scout() {
        Fleet myFleet = new Fleet(1, "My Fleet");
        myFleet.setPosition(new Hex3D(0, 0, 0));
        myFleet.addShip(new Ship(1, ShipType.FRIGATE));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(List.of(myFleet))
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        AiDecision decision = tacticalCore.decide(context);
        // Should move or scout when no enemies
        assertTrue(decision.getActionType() == AiDecision.ActionType.MOVE_FLEET ||
                decision.getActionType() == AiDecision.ActionType.SCOUT);
    }

    @Test
    @DisplayName("tactical modes should be enumerable")
    void tacticalModes() {
        TacticalAiCore.TacticalMode[] modes = TacticalAiCore.TacticalMode.values();
        assertEquals(5, modes.length);
    }

    @Test
    @DisplayName("should select target with highest damage")
    void selectTarget() {
        Fleet enemy = new Fleet(1, "Enemy Fleet");
        Ship frigate = new Ship(1, ShipType.FRIGATE);
        Ship cruiser = new Ship(2, ShipType.CRUISER);
        enemy.addShip(frigate);
        enemy.addShip(cruiser);

        Ship target = tacticalCore.selectTarget(enemy.getShips());
        assertNotNull(target);
        // High aggression targets healthiest ship; both are at full hull,
        // so the first ship in the list is returned (FRIGATE)
        assertEquals(ShipType.FRIGATE, target.getShipType());
    }
}
