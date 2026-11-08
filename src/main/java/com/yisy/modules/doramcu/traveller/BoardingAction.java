package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Coordinates a boarding action from Classic Traveller High Guard.
 * <p>
 * A boarding action is a multi-phase assault where one ship attempts to
 * board and capture another. The process follows these phases:
 * </p>
 * <ol>
 *   <li><b>Launch</b> — Boarding shuttles are launched with marine units</li>
 *   <li><b>Approach</b> — Shuttles approach the target while point defenses fire</li>
 *   <li><b>Docking</b> — Surviving shuttles attempt to dock with the target</li>
 *   <li><b>Boarding Combat</b> — Marines fight room-to-room on the target ship</li>
 *   <li><b>Resolution</b> — The boarding either succeeds (ship captured) or fails</li>
 * </ol>
 * <p>
 * The boarding action coordinator tracks all phases, resolves the combat,
 * and determines the outcome based on marine casualties, objectives captured,
 * and the target ship's condition.
 * </p>
 */
public class BoardingAction {

    /**
     * Phase of the boarding action.
     */
    public enum Phase {
        /** Shuttles are being launched. */
        LAUNCH,
        /** Shuttles are approaching the target. */
        APPROACH,
        /** Shuttles are attempting to dock. */
        DOCKING,
        /** Marines are fighting on the target ship. */
        COMBAT,
        /** The boarding action has concluded. */
        COMPLETED
    }

    /**
     * Outcome of a boarding action.
     */
    public enum Outcome {
        /** The boarding succeeded — the target ship was captured. */
        CAPTURED("Ship Captured"),
        /** The boarding failed — marines were repelled. */
        REPELLED("Boarding Repelled"),
        /** The boarding was aborted before completion. */
        ABORTED("Boarding Aborted"),
        /** Both sides suffered heavy losses — mutual destruction. */
        MUTUAL_DESTRUCTION("Mutual Destruction");

        private final String description;

        Outcome(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ShipDesign attackerShip;
    private final ShipDesign defenderShip;
    private final List<BoardingShuttle> shuttles;
    private final MarineUnit defenderMarines;
    private final int defenderPointDefense;
    private Phase currentPhase;
    private Outcome outcome;
    private final List<String> eventLog;
    private final Random random;
    private int combatRounds;

    /**
     * Creates a new boarding action.
     *
     * @param attackerShip the ship launching the boarding action
     * @param defenderShip the ship being boarded
     * @param shuttles the boarding shuttles with marine units
     * @param defenderMarines the defending marine unit
     * @param defenderPointDefense the defender's point defense rating (1-12)
     */
    public BoardingAction(ShipDesign attackerShip, ShipDesign defenderShip,
                           List<BoardingShuttle> shuttles,
                           MarineUnit defenderMarines,
                           int defenderPointDefense) {
        if (attackerShip == null) {
            throw new IllegalArgumentException("Attacker ship must not be null");
        }
        if (defenderShip == null) {
            throw new IllegalArgumentException("Defender ship must not be null");
        }
        if (shuttles == null || shuttles.isEmpty()) {
            throw new IllegalArgumentException("At least one shuttle is required");
        }

        this.attackerShip = attackerShip;
        this.defenderShip = defenderShip;
        this.shuttles = new ArrayList<>(shuttles);
        this.defenderMarines = defenderMarines;
        this.defenderPointDefense = Math.max(1, Math.min(12, defenderPointDefense));
        this.currentPhase = Phase.LAUNCH;
        this.outcome = null;
        this.eventLog = new ArrayList<>();
        this.random = new Random();
        this.combatRounds = 0;
    }

    // ---- Accessors ----

    /** @return the attacking ship's design */
    public ShipDesign getAttackerShip() {
        return attackerShip;
    }

    /** @return the defending ship's design */
    public ShipDesign getDefenderShip() {
        return defenderShip;
    }

    /** @return the list of boarding shuttles */
    public List<BoardingShuttle> getShuttles() {
        return Collections.unmodifiableList(shuttles);
    }

    /** @return the defending marine unit */
    public MarineUnit getDefenderMarines() {
        return defenderMarines;
    }

    /** @return the current phase */
    public Phase getCurrentPhase() {
        return currentPhase;
    }

    /** @return the outcome, or null if not completed */
    public Outcome getOutcome() {
        return outcome;
    }

    /** @return the event log */
    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    /** @return the number of combat rounds fought */
    public int getCombatRounds() {
        return combatRounds;
    }

    // ---- Phase Execution ----

    /**
     * Executes the entire boarding action from launch to resolution.
     *
     * @return the outcome of the boarding action
     */
    public Outcome execute() {
        logEvent("Boarding action initiated: " + attackerShip.getName() +
                " -> " + defenderShip.getName());

        // Phase 1: Launch
        currentPhase = Phase.LAUNCH;
        logEvent("Phase: LAUNCH — " + shuttles.size() + " shuttles launched");

        // Phase 2: Approach
        currentPhase = Phase.APPROACH;
        resolveApproach();

        // Check if all shuttles were destroyed
        int survivingShuttles = 0;
        for (BoardingShuttle shuttle : shuttles) {
            if (!shuttle.isDestroyed()) survivingShuttles++;
        }

        if (survivingShuttles == 0) {
            outcome = Outcome.REPELLED;
            currentPhase = Phase.COMPLETED;
            logEvent("All shuttles destroyed — boarding repelled");
            return outcome;
        }

        // Phase 3: Docking
        currentPhase = Phase.DOCKING;
        resolveDocking();

        // Check if any shuttle docked
        int dockedShuttles = 0;
        for (BoardingShuttle shuttle : shuttles) {
            if (shuttle.isDocked()) dockedShuttles++;
        }

        if (dockedShuttles == 0) {
            outcome = Outcome.REPELLED;
            currentPhase = Phase.COMPLETED;
            logEvent("No shuttles docked — boarding repelled");
            return outcome;
        }

        // Phase 4: Combat
        currentPhase = Phase.COMBAT;
        resolveCombat();

        // Phase 5: Resolution
        currentPhase = Phase.COMPLETED;
        resolveOutcome();

        return outcome;
    }

    /**
     * Resolves the approach phase: point defenses fire at shuttles.
     */
    private void resolveApproach() {
        logEvent("Phase: APPROACH — Point defenses engage shuttles");

        for (BoardingShuttle shuttle : shuttles) {
            // Point defense roll: 2D6 vs shuttle stealth + armor
            int pdRoll = random.nextInt(12) + 2; // 2D6
            int defense = shuttle.getStealth() + shuttle.getArmor();
            int hitThreshold = 10 - defense;

            if (pdRoll >= hitThreshold) {
                // Hit! Calculate damage
                int damage = defenderPointDefense / 2;
                shuttle.takeDamage(damage);
                logEvent(shuttle.getName() + " hit by point defense: " +
                        damage + " damage (" + shuttle.getCurrentHull() + "/" +
                        shuttle.getHullPoints() + " hull)");

                if (shuttle.isDestroyed()) {
                    logEvent(shuttle.getName() + " DESTROYED by point defense");
                }
            } else {
                logEvent(shuttle.getName() + " evades point defense fire");
            }
        }
    }

    /**
     * Resolves the docking phase: surviving shuttles attempt to dock.
     */
    private void resolveDocking() {
        logEvent("Phase: DOCKING — Shuttles attempt to dock");

        for (BoardingShuttle shuttle : shuttles) {
            if (shuttle.isDestroyed()) continue;

            boolean docked = shuttle.attemptDocking(defenderPointDefense, random);
            if (docked) {
                logEvent(shuttle.getName() + " successfully docked with " +
                        defenderShip.getName());
            } else {
                logEvent(shuttle.getName() + " failed to dock — drifting away");
            }
        }
    }

    /**
     * Resolves the combat phase: room-to-room fighting.
     */
    private void resolveCombat() {
        logEvent("Phase: COMBAT — Marines engage in room-to-room combat");

        // Collect all attacking marine units from docked shuttles
        List<MarineUnit> attackers = new ArrayList<>();
        for (BoardingShuttle shuttle : shuttles) {
            if (shuttle.isDocked()) {
                attackers.add(shuttle.getMarineUnit());
            }
        }

        if (attackers.isEmpty()) {
            logEvent("No marines boarded — combat phase skipped");
            return;
        }

        // Fight up to 10 rounds of combat
        for (int round = 1; round <= 10; round++) {
            combatRounds = round;

            // Check if either side is defeated
            double attackerPower = 0;
            for (MarineUnit unit : attackers) {
                attackerPower += unit.getEffectiveCombatPower();
            }

            double defenderPower = defenderMarines.getEffectiveCombatPower();

            if (defenderPower <= 0) {
                logEvent("Round " + round + " — Defenders defeated");
                break;
            }
            if (attackerPower <= 0) {
                logEvent("Round " + round + " — Attackers defeated");
                break;
            }

            // Resolve combat round
            CombatResult result = resolveCombatRound(attackers, defenderMarines);
            logEvent("Round " + round + " — Attackers: " + result.attackerCasualties +
                    " casualties, Defenders: " + result.defenderCasualties + " casualties");

            // Morale checks
            if (!defenderMarines.moraleCheck(random)) {
                logEvent("Defender marines break and retreat!");
                break;
            }

            boolean attackersBroke = false;
            for (MarineUnit unit : attackers) {
                if (!unit.moraleCheck(random)) {
                    logEvent(unit.getName() + " breaks and retreats!");
                    attackersBroke = true;
                }
            }
            if (attackersBroke) {
                // Check if any attackers remain
                boolean anyRemaining = false;
                for (MarineUnit unit : attackers) {
                    if (unit.isCombatEffective()) {
                        anyRemaining = true;
                        break;
                    }
                }
                if (!anyRemaining) break;
            }
        }
    }

    /**
     * Resolves a single round of boarding combat.
     */
    private CombatResult resolveCombatRound(List<MarineUnit> attackers, MarineUnit defender) {
        // Calculate total combat power
        double attackerPower = 0;
        for (MarineUnit unit : attackers) {
            attackerPower += unit.getEffectiveCombatPower();
        }
        double defenderPower = defender.getEffectiveCombatPower();

        // Attacker roll
        int attackerRoll = random.nextInt(12) + 2; // 2D6
        int defenderCasualties = 0;
        if (attackerRoll * attackerPower > 7 * defenderPower) {
            defenderCasualties = Math.max(1, (int) (attackerPower / 3));
            defender.applyCasualties(defenderCasualties);
        }

        // Defender roll
        int defenderRoll = random.nextInt(12) + 2; // 2D6
        int attackerCasualties = 0;
        if (defenderRoll * defenderPower > 7 * attackerPower) {
            attackerCasualties = Math.max(1, (int) (defenderPower / 3));
            // Distribute casualties among attacker units
            for (MarineUnit unit : attackers) {
                if (attackerCasualties <= 0) break;
                int unitCasualties = Math.min(unit.getRemainingMarines(),
                        (attackerCasualties + attackers.size()) / attackers.size() + 1);
                unit.applyCasualties(unitCasualties);
                attackerCasualties -= unitCasualties;
            }
        }

        return new CombatResult(attackerCasualties, defenderCasualties);
    }

    /**
     * Determines the final outcome of the boarding action.
     */
    private void resolveOutcome() {
        // Check if defenders are defeated
        if (!defenderMarines.isCombatEffective()) {
            outcome = Outcome.CAPTURED;
            logEvent("Outcome: " + outcome.getDescription() +
                    " — " + defenderShip.getName() + " captured!");
            return;
        }

        // Check if attackers are defeated
        boolean anyAttackersAlive = false;
        for (BoardingShuttle shuttle : shuttles) {
            MarineUnit unit = shuttle.getMarineUnit();
            if (unit.isCombatEffective()) {
                anyAttackersAlive = true;
                break;
            }
        }

        if (!anyAttackersAlive) {
            outcome = Outcome.REPELLED;
            logEvent("Outcome: " + outcome.getDescription() +
                    " — All boarding marines eliminated");
            return;
        }

        // Both sides still have forces — check relative strength
        double attackerPower = 0;
        for (BoardingShuttle shuttle : shuttles) {
            attackerPower += shuttle.getMarineUnit().getEffectiveCombatPower();
        }
        double defenderPower = defenderMarines.getEffectiveCombatPower();

        if (attackerPower > defenderPower * 2) {
            outcome = Outcome.CAPTURED;
            logEvent("Outcome: " + outcome.getDescription() +
                    " — Attackers overwhelm defenders");
        } else if (defenderPower > attackerPower * 2) {
            outcome = Outcome.REPELLED;
            logEvent("Outcome: " + outcome.getDescription() +
                    " — Defenders repel attackers");
        } else {
            outcome = Outcome.MUTUAL_DESTRUCTION;
            logEvent("Outcome: " + outcome.getDescription() +
                    " — Both sides suffer heavy losses");
        }
    }

    private void logEvent(String event) {
        eventLog.add(event);
    }

    /**
     * Result of a single combat round.
     */
    private static class CombatResult {
        final int attackerCasualties;
        final int defenderCasualties;

        CombatResult(int attackerCasualties, int defenderCasualties) {
            this.attackerCasualties = attackerCasualties;
            this.defenderCasualties = defenderCasualties;
        }
    }
}
