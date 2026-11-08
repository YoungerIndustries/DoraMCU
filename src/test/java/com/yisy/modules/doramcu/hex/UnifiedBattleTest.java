package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedBattle Tests")
class UnifiedBattleTest {

    private Fleet createFleet(String name) {
        Fleet fleet = new Fleet(0, name);
        fleet.addShip(UnifiedShipFactory.createFrigate(1));
        fleet.addShip(UnifiedShipFactory.createFrigate(2));
        return fleet;
    }

    @Test
    @DisplayName("Should create battle")
    void testCreateBattle() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        Hex3D location = new Hex3D(0, 0, 0);

        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, location);
        assertEquals(1, battle.getId());
        assertEquals(attacker, battle.getAttacker());
        assertEquals(defender, battle.getDefender());
        assertEquals(location, battle.getLocation());
        assertEquals(UnifiedBattle.BattleState.INITIATED, battle.getState());
    }

    @Test
    @DisplayName("Should reject null attacker")
    void testRejectNullAttacker() {
        Fleet defender = createFleet("Defenders");
        assertThrows(IllegalArgumentException.class, () ->
                new UnifiedBattle(1, null, defender, new Hex3D(0, 0, 0)));
    }

    @Test
    @DisplayName("Should reject null defender")
    void testRejectNullDefender() {
        Fleet attacker = createFleet("Attackers");
        assertThrows(IllegalArgumentException.class, () ->
                new UnifiedBattle(1, attacker, null, new Hex3D(0, 0, 0)));
    }

    @Test
    @DisplayName("Should reject empty attacker")
    void testRejectEmptyAttacker() {
        Fleet attacker = new Fleet(0, "Empty");
        Fleet defender = createFleet("Defenders");
        assertThrows(IllegalArgumentException.class, () ->
                new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0)));
    }

    @Test
    @DisplayName("Should start battle")
    void testStartBattle() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0));

        battle.start();
        assertEquals(UnifiedBattle.BattleState.IN_PROGRESS, battle.getState());
    }

    @Test
    @DisplayName("Should not start twice")
    void testStartTwice() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0));
        battle.start();

        assertThrows(IllegalStateException.class, battle::start);
    }

    @Test
    @DisplayName("Should fight a round")
    void testFightRound() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0));
        battle.start();

        UnifiedBattle.RoundResult result = battle.fightRound(FleetAction.LASER_BARRAGE, FleetAction.EVASIVE);
        assertNotNull(result);
        assertEquals(1, result.getRoundNumber());
        assertEquals(FleetAction.LASER_BARRAGE, result.getAttackerAction());
        assertEquals(FleetAction.EVASIVE, result.getDefenderAction());
    }

    @Test
    @DisplayName("Should fight with default actions")
    void testFightRoundDefault() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0));
        battle.start();

        UnifiedBattle.RoundResult result = battle.fightRound();
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should retreat")
    void testRetreat() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0));
        battle.start();
        battle.retreat();

        assertEquals(UnifiedBattle.BattleState.COMPLETED, battle.getState());
        assertEquals(UnifiedBattle.BattleOutcome.RETREAT, battle.getOutcome());
    }

    @Test
    @DisplayName("Should reinforce attacker")
    void testReinforceAttacker() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0));
        battle.start();

        Fleet reinforcements = createFleet("Reinforcements");
        int added = battle.reinforceAttacker(reinforcements);
        assertTrue(added >= 2);
    }

    @Test
    @DisplayName("Should reinforce defender")
    void testReinforceDefender() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0));
        battle.start();

        Fleet reinforcements = createFleet("Reinforcements");
        int added = battle.reinforceDefender(reinforcements);
        assertTrue(added >= 2);
    }

    @Test
    @DisplayName("Should have events")
    void testEvents() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0));
        battle.start();
        battle.fightRound();

        assertTrue(battle.getEvents().size() >= 2);
    }

    @Test
    @DisplayName("Should get battle summary")
    void testBattleSummary() {
        Fleet attacker = createFleet("Attackers");
        Fleet defender = createFleet("Defenders");
        UnifiedBattle battle = new UnifiedBattle(1, attacker, defender, new Hex3D(0, 0, 0));
        String summary = battle.getBattleSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Attackers"));
    }

    @Test
    @DisplayName("Should have outcome enum values")
    void testOutcomeEnum() {
        assertEquals("Attacker Victory", UnifiedBattle.BattleOutcome.ATTACKER_VICTORY.getDescription());
        assertEquals("Defender Victory", UnifiedBattle.BattleOutcome.DEFENDER_VICTORY.getDescription());
        assertEquals("Mutual Destruction", UnifiedBattle.BattleOutcome.MUTUAL_DESTRUCTION.getDescription());
        assertEquals("Retreat", UnifiedBattle.BattleOutcome.RETREAT.getDescription());
        assertEquals("Stalemate", UnifiedBattle.BattleOutcome.STALEMATE.getDescription());
    }
}
