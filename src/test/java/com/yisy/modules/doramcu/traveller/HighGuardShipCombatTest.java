package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HighGuardShipCombat Tests")
class HighGuardShipCombatTest {

    // ---- Helper methods ----

    private HighGuardShipDesign createBasicShip(int id, String name) {
        HighGuardShipDesign ship = new HighGuardShipDesign(id, name, 4000);
        ship.setTechLevel(11);
        ship.setManeuverDrive(2);
        ship.setJumpDrive(2);
        ship.setPowerPlantRating(10);
        ship.setComputerModel(3);
        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_1);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE);
        return ship;
    }

    private HighGuardShipDesign createArmedShip(int id, String name) {
        HighGuardShipDesign ship = new HighGuardShipDesign(id, name, 20000);
        ship.setTechLevel(12);
        ship.setManeuverDrive(3);
        ship.setJumpDrive(3);
        ship.setPowerPlantRating(20);
        ship.setComputerModel(5);
        ship.setArmor(HighGuardArmor.CRYSTALIRON_2);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE);
        ship.addTurretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.DOUBLE);
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE);
        return ship;
    }

    // ---- CombatPhase enum tests ----

    @Test
    @DisplayName("Should have 6 combat phases")
    void testCombatPhaseCount() {
        assertEquals(6, HighGuardShipCombat.CombatPhase.values().length);
    }

    // ---- CombatOutcome enum tests ----

    @Test
    @DisplayName("Should have 5 combat outcomes")
    void testCombatOutcomeCount() {
        assertEquals(5, HighGuardShipCombat.CombatOutcome.values().length);
    }

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create combat with default range")
    void testDefaultRange() {
        HighGuardShipDesign ship0 = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, null);

        assertEquals(HighGuardRangeBand.LONG, combat.getCurrentRange());
        assertEquals(HighGuardShipCombat.CombatPhase.NOT_STARTED, combat.getCurrentPhase());
        assertEquals(0, combat.getCurrentRound());
        assertFalse(combat.isCompleted());
    }

    @Test
    @DisplayName("Should create combat with specified range")
    void testSpecifiedRange() {
        HighGuardShipDesign ship0 = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.SHORT);

        assertEquals(HighGuardRangeBand.SHORT, combat.getCurrentRange());
    }

    @Test
    @DisplayName("Should set up combat ships correctly")
    void testCombatShipSetup() {
        HighGuardShipDesign ship0 = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs0 = combat.getSide0();
        HighGuardShipCombat.CombatShip cs1 = combat.getSide1();

        assertEquals(0, cs0.getSide());
        assertEquals(1, cs1.getSide());
        assertEquals(ship0.getHullPoints(), cs0.getHullPoints());
        assertEquals(ship1.getHullPoints(), cs1.getHullPoints());
        assertEquals(ship0.getHullPoints(), cs0.getMaxHullPoints());
    }

    @Test
    @DisplayName("Should calculate initiative")
    void testInitiative() {
        HighGuardShipDesign ship0 = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.MEDIUM);

        // Initiative = 2D6 + computer model, so minimum 2+3=5, maximum 12+3=15
        assertTrue(combat.getSide0Initiative() >= 5);
        assertTrue(combat.getSide0Initiative() <= 15);
        assertTrue(combat.getSide1Initiative() >= 5);
        assertTrue(combat.getSide1Initiative() <= 15);
    }

    @Test
    @DisplayName("Should log battle started event")
    void testBattleStartedEvent() {
        HighGuardShipDesign ship0 = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.MEDIUM);

        assertEquals(1, combat.getEventLog().size());
        assertEquals(HighGuardShipCombat.CombatEvent.EventType.BATTLE_STARTED,
                combat.getEventLog().get(0).getType());
    }

    // ---- Execute round tests ----

    @Test
    @DisplayName("Should execute a round and increment round counter")
    void testExecuteRound() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.SHORT);

        boolean ongoing = combat.executeRound();
        assertEquals(1, combat.getCurrentRound());
        // Combat may or may not be ongoing depending on RNG
        // Just verify it ran without error
    }

    @Test
    @DisplayName("Should return false when executing round on completed combat")
    void testExecuteRoundOnCompleted() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.ADJACENT);

        // Fight to completion first
        combat.fightToCompletion();
        assertFalse(combat.executeRound());
    }

    @Test
    @DisplayName("Should generate events during combat")
    void testEventsGenerated() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.SHORT);

        combat.executeRound();
        assertTrue(combat.getEventLog().size() > 1, "Should have more than the initial event");
    }

    @Test
    @DisplayName("Should log round completed event")
    void testRoundCompletedEvent() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.SHORT);

        combat.executeRound();

        boolean hasRoundCompleted = combat.getEventLog().stream()
                .anyMatch(e -> e.getType() == HighGuardShipCombat.CombatEvent.EventType.ROUND_COMPLETED);
        assertTrue(hasRoundCompleted);
    }

    // ---- Fight to completion tests ----

    @Test
    @DisplayName("Should fight to completion and produce an outcome")
    void testFightToCompletion() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.SHORT);

        HighGuardShipCombat.CombatOutcome outcome = combat.fightToCompletion();
        assertNotNull(outcome);
        assertTrue(outcome == HighGuardShipCombat.CombatOutcome.SIDE_0_VICTORY ||
                outcome == HighGuardShipCombat.CombatOutcome.SIDE_1_VICTORY ||
                outcome == HighGuardShipCombat.CombatOutcome.MUTUAL_DESTRUCTION ||
                outcome == HighGuardShipCombat.CombatOutcome.STALEMATE);
    }

    @Test
    @DisplayName("Should mark combat as completed after fightToCompletion")
    void testCompletedAfterFight() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.SHORT);

        combat.fightToCompletion();
        assertTrue(combat.isCompleted());
        assertEquals(HighGuardShipCombat.CombatPhase.COMPLETED, combat.getCurrentPhase());
    }

    @Test
    @DisplayName("Should have at least one ship destroyed after combat")
    void testShipDestroyedAfterCombat() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.ADJACENT);

        combat.fightToCompletion();

        assertTrue(combat.getSide0().isDestroyed() || combat.getSide1().isDestroyed() ||
                combat.getSide0().isRetreated() || combat.getSide1().isRetreated() ||
                combat.getOutcome() == HighGuardShipCombat.CombatOutcome.STALEMATE);
    }

    // ---- Combat outcome tests ----

    @Test
    @DisplayName("Should return SIDE_0_VICTORY when side 1 is destroyed")
    void testSide0Victory() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.ADJACENT);

        // Manually destroy side 1
        combat.getSide1().setDestroyed(true);
        combat.fightToCompletion();

        assertEquals(HighGuardShipCombat.CombatOutcome.SIDE_0_VICTORY, combat.getOutcome());
    }

    @Test
    @DisplayName("Should return SIDE_1_VICTORY when side 0 is destroyed")
    void testSide1Victory() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.ADJACENT);

        // Manually destroy side 0
        combat.getSide0().setDestroyed(true);
        combat.fightToCompletion();

        assertEquals(HighGuardShipCombat.CombatOutcome.SIDE_1_VICTORY, combat.getOutcome());
    }

    @Test
    @DisplayName("Should return MUTUAL_DESTRUCTION when both ships are destroyed")
    void testMutualDestruction() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.ADJACENT);

        // Manually destroy both
        combat.getSide0().setDestroyed(true);
        combat.getSide1().setDestroyed(true);
        combat.fightToCompletion();

        assertEquals(HighGuardShipCombat.CombatOutcome.MUTUAL_DESTRUCTION, combat.getOutcome());
    }

    // ---- CombatShip tests ----

    @Test
    @DisplayName("Should calculate missile pods")
    void testMissilePods() {
        HighGuardShipDesign ship = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        assertTrue(cs.getMissilePods() > 0, "Should have missile pods");
    }

    @Test
    @DisplayName("Should reduce hull points")
    void testHullPoints() {
        HighGuardShipDesign ship = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        int initialHp = cs.getHullPoints();
        cs.setHullPoints(initialHp - 10);
        assertEquals(initialHp - 10, cs.getHullPoints());
    }

    @Test
    @DisplayName("Should set destroyed when hull points reach 0")
    void testDestroyedOnZeroHp() {
        HighGuardShipDesign ship = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        cs.setHullPoints(0);
        assertTrue(cs.isDestroyed());
    }

    @Test
    @DisplayName("Should not go below 0 hull points")
    void testHullPointsFloor() {
        HighGuardShipDesign ship = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        cs.setHullPoints(-100);
        assertEquals(0, cs.getHullPoints());
    }

    @Test
    @DisplayName("Should calculate hull ratio")
    void testHullRatio() {
        HighGuardShipDesign ship = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        assertEquals(1.0, cs.getHullRatio(), 0.01);

        cs.setHullPoints(cs.getMaxHullPoints() / 2);
        assertEquals(0.5, cs.getHullRatio(), 0.1);
    }

    @Test
    @DisplayName("Should damage turret")
    void testDamageTurret() {
        HighGuardShipDesign ship = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        assertTrue(cs.getOperationalTurrets().get(0).isOperational());
        cs.damageTurret(0);
        assertFalse(cs.getOperationalTurrets().get(0).isOperational());
    }

    @Test
    @DisplayName("Should reduce thrust")
    void testReduceThrust() {
        HighGuardShipDesign ship = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        int initialThrust = cs.getCurrentThrust();
        cs.reduceThrust(1);
        assertEquals(initialThrust - 1, cs.getCurrentThrust());
    }

    @Test
    @DisplayName("Should not reduce thrust below 0")
    void testReduceThrustFloor() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Ship 0", 4000);
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        cs.reduceThrust(100);
        assertEquals(0, cs.getCurrentThrust());
    }

    @Test
    @DisplayName("Should use missile pod")
    void testUseMissilePod() {
        HighGuardShipDesign ship = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        int initialPods = cs.getMissilePods();
        cs.useMissilePod();
        assertEquals(initialPods - 1, cs.getMissilePods());
    }

    @Test
    @DisplayName("Should not reduce missile pods below 0")
    void testMissilePodsFloor() {
        HighGuardShipDesign ship = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        // Drain all pods
        for (int i = 0; i < 100; i++) {
            cs.useMissilePod();
        }
        assertEquals(0, cs.getMissilePods());
    }

    @Test
    @DisplayName("Should set retreated status")
    void testRetreated() {
        HighGuardShipDesign ship = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship, ship1, HighGuardRangeBand.MEDIUM);

        HighGuardShipCombat.CombatShip cs = combat.getSide0();
        assertFalse(cs.isRetreated());
        cs.setRetreated(true);
        assertTrue(cs.isRetreated());
    }

    // ---- Event log tests ----

    @Test
    @DisplayName("Event log should be unmodifiable")
    void testUnmodifiableEventLog() {
        HighGuardShipDesign ship0 = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.MEDIUM);

        assertThrows(UnsupportedOperationException.class, () -> combat.getEventLog().clear());
    }

    // ---- Summary test ----

    @Test
    @DisplayName("Summary should contain ship names and stats")
    void testSummary() {
        HighGuardShipDesign ship0 = createBasicShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createBasicShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.MEDIUM);

        String summary = combat.getSummary();
        assertTrue(summary.contains("Ship 0"));
        assertTrue(summary.contains("Ship 1"));
        assertTrue(summary.contains("ongoing"));
    }

    // ---- Multiple rounds tests ----

    @Test
    @DisplayName("Should execute multiple rounds")
    void testMultipleRounds() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.SHORT);

        for (int i = 0; i < 5 && !combat.isCompleted(); i++) {
            combat.executeRound();
        }

        assertTrue(combat.getCurrentRound() > 0);
    }

    @Test
    @DisplayName("Should log battle ended event after completion")
    void testBattleEndedEvent() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.ADJACENT);

        combat.fightToCompletion();

        boolean hasBattleEnded = combat.getEventLog().stream()
                .anyMatch(e -> e.getType() == HighGuardShipCombat.CombatEvent.EventType.BATTLE_ENDED);
        assertTrue(hasBattleEnded);
    }

    // ---- Combat event tests ----

    @Test
    @DisplayName("Should have 18 event types")
    void testEventTypeCount() {
        assertEquals(18, HighGuardShipCombat.CombatEvent.EventType.values().length);
    }

    // ---- Range change during combat tests ----

    @Test
    @DisplayName("Range should potentially change during combat")
    void testRangeChange() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.LONG);

        combat.executeRound();

        // Range may have changed — just verify it's still a valid range band
        assertNotNull(combat.getCurrentRange());
    }

    // ---- Thrust spent event tests ----

    @Test
    @DisplayName("Should log thrust spent event")
    void testThrustSpentEvent() {
        HighGuardShipDesign ship0 = createArmedShip(1, "Ship 0");
        HighGuardShipDesign ship1 = createArmedShip(2, "Ship 1");
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, HighGuardRangeBand.SHORT);

        combat.executeRound();

        boolean hasThrustSpent = combat.getEventLog().stream()
                .anyMatch(e -> e.getType() == HighGuardShipCombat.CombatEvent.EventType.THRUST_SPENT);
        assertTrue(hasThrustSpent);
    }
}
