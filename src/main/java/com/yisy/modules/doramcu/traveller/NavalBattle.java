package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Objects;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Fleet-level naval battle from Classic Traveller Book 5: High Guard.
 * <p>
 * A naval battle is fought between two opposing fleets, each consisting
 * of one or more squadrons. The battle proceeds in rounds, with each
 * fleet choosing actions and resolving combat.
 * </p>
 * <p>
 * Battle structure:
 * </p>
 * <ol>
 *   <li>Initialization: fleets position and declare formations</li>
 *   <li>Each round: both fleets choose actions simultaneously</li>
 *   <li>Combat resolution: damage is calculated and applied</li>
 *   <li>End condition: one fleet is destroyed or retreats</li>
 * </ol>
 * <p>
 * The battle coordinator tracks:
 * </p>
 * <ul>
 *   <li>Current round number</li>
 *   <li>Fleet positions and formations</li>
 *   <li>Damage dealt and received</li>
 *   <li>Ship losses on both sides</li>
 *   <li>Battle events and outcomes</li>
 * </ul>
 */
public class NavalBattle {

    private final NavalFleet fleet1;
    private final NavalFleet fleet2;
    private final TravellerWorld battleSystem;
    private int currentRound;
    private boolean isActive;
    private final List<BattleRound> rounds;
    private final List<String> battleLog;
    private BattleOutcome outcome;

    /**
     * Possible outcomes of a naval battle.
     */
    public enum BattleOutcome {
        /**
         * Battle is in progress.
         */
        IN_PROGRESS("In Progress"),

        /**
         * Fleet 1 won the battle.
         */
        FLEET_1_VICTORY("Fleet 1 Victory"),

        /**
         * Fleet 2 won the battle.
         */
        FLEET_2_VICTORY("Fleet 2 Victory"),

        /**
         * Both fleets retreated — draw.
         */
        DRAW("Draw"),

        /**
         * Battle was cancelled.
         */
        CANCELLED("Cancelled");

        private final String description;

        BattleOutcome(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * A single round of naval battle.
     */
    public static class BattleRound {
        private final int roundNumber;
        private final FleetAction fleet1Action;
        private final FleetAction fleet2Action;
        private final int fleet1DamageDealt;
        private final int fleet2DamageDealt;
        private final int fleet1ShipsLost;
        private final int fleet2ShipsLost;
        private final String events;

        BattleRound(int roundNumber, FleetAction fleet1Action, FleetAction fleet2Action,
                    int fleet1DamageDealt, int fleet2DamageDealt,
                    int fleet1ShipsLost, int fleet2ShipsLost, String events) {
            this.roundNumber = roundNumber;
            this.fleet1Action = fleet1Action;
            this.fleet2Action = fleet2Action;
            this.fleet1DamageDealt = fleet1DamageDealt;
            this.fleet2DamageDealt = fleet2DamageDealt;
            this.fleet1ShipsLost = fleet1ShipsLost;
            this.fleet2ShipsLost = fleet2ShipsLost;
            this.events = events;
        }

        public int getRoundNumber() {
            return roundNumber;
        }

        public FleetAction getFleet1Action() {
            return fleet1Action;
        }

        public FleetAction getFleet2Action() {
            return fleet2Action;
        }

        public int getFleet1DamageDealt() {
            return fleet1DamageDealt;
        }

        public int getFleet2DamageDealt() {
            return fleet2DamageDealt;
        }

        public int getFleet1ShipsLost() {
            return fleet1ShipsLost;
        }

        public int getFleet2ShipsLost() {
            return fleet2ShipsLost;
        }

        public String getEvents() {
            return events;
        }
    }

    /**
     * Creates a new naval battle between two fleets.
     *
     * @param fleet1 the first fleet
     * @param fleet2 the second fleet
     * @param battleSystem the star system where the battle takes place
     */
    public NavalBattle(NavalFleet fleet1, NavalFleet fleet2, TravellerWorld battleSystem) {
        this.fleet1 = Objects.requireNonNull(fleet1, "Fleet 1 must not be null");
        this.fleet2 = Objects.requireNonNull(fleet2, "Fleet 2 must not be null");
        this.battleSystem = Objects.requireNonNull(battleSystem, "Battle system must not be null");
        this.currentRound = 0;
        this.isActive = true;
        this.rounds = new ArrayList<>();
        this.battleLog = new ArrayList<>();
        this.outcome = BattleOutcome.IN_PROGRESS;
        battleLog.add("Battle initiated in " + battleSystem.getName());
        battleLog.add("Fleet 1: " + fleet1.getName());
        battleLog.add("Fleet 2: " + fleet2.getName());
    }

    // ---- Accessors ----

    /**
     * Returns the first fleet.
     *
     * @return fleet 1
     */
    public NavalFleet getFleet1() {
        return fleet1;
    }

    /**
     * Returns the second fleet.
     *
     * @return fleet 2
     */
    public NavalFleet getFleet2() {
        return fleet2;
    }

    /**
     * Returns the star system where the battle takes place.
     *
     * @return the battle system
     */
    public TravellerWorld getBattleSystem() {
        return battleSystem;
    }

    /**
     * Returns the current round number.
     *
     * @return the round number
     */
    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Checks if the battle is still active.
     *
     * @return true if the battle is active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Returns the list of battle rounds (immutable).
     *
     * @return the rounds list
     */
    public List<BattleRound> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    /**
     * Returns the battle log (immutable).
     *
     * @return the battle log
     */
    public List<String> getBattleLog() {
        return Collections.unmodifiableList(battleLog);
    }

    /**
     * Returns the battle outcome.
     *
     * @return the outcome
     */
    public BattleOutcome getOutcome() {
        return outcome;
    }

    // ---- Battle Execution ----

    /**
     * Executes a single round of the battle.
     *
     * @param fleet1Action the action chosen by fleet 1
     * @param fleet2Action the action chosen by fleet 2
     * @return the battle round result
     */
    public BattleRound executeRound(FleetAction fleet1Action, FleetAction fleet2Action) {
        if (!isActive) {
            throw new IllegalStateException("Battle is not active");
        }
        currentRound++;
        StringBuilder events = new StringBuilder();

        // Calculate damage for fleet 1 attacking fleet 2
        int fleet1Damage = calculateDamage(fleet1, fleet2, fleet1Action, fleet2Action, events, "Fleet 1");
        // Calculate damage for fleet 2 attacking fleet 1
        int fleet2Damage = calculateDamage(fleet2, fleet1, fleet2Action, fleet1Action, events, "Fleet 2");

        // Count ships lost
        int fleet1ShipsBefore = fleet1.getTotalShipCount();
        int fleet2ShipsBefore = fleet2.getTotalShipCount();

        // Apply damage (simplified — remove ships based on damage)
        applyDamage(fleet1, fleet2Damage);
        applyDamage(fleet2, fleet1Damage);

        int fleet1ShipsLost = fleet1ShipsBefore - fleet1.getTotalShipCount();
        int fleet2ShipsLost = fleet2ShipsBefore - fleet2.getTotalShipCount();

        // Log events
        String eventText = events.toString();
        battleLog.add("Round " + currentRound + ": " + eventText);
        battleLog.add("  Fleet 1 dealt " + fleet1Damage + " damage, lost " + fleet1ShipsLost + " ships");
        battleLog.add("  Fleet 2 dealt " + fleet2Damage + " damage, lost " + fleet2ShipsLost + " ships");

        // Record battle for commanders
        if (fleet1.getCommander() != null) {
            fleet1.getCommander().recordBattle(fleet2ShipsLost > 0);
        }
        if (fleet2.getCommander() != null) {
            fleet2.getCommander().recordBattle(fleet1ShipsLost > 0);
        }

        // Create round result
        BattleRound round = new BattleRound(currentRound, fleet1Action, fleet2Action,
                fleet1Damage, fleet2Damage, fleet1ShipsLost, fleet2ShipsLost, eventText);
        rounds.add(round);

        // Check for battle end
        checkBattleEnd();

        return round;
    }

    /**
     * Calculates the damage dealt by one fleet to another.
     *
     * @param attacker the attacking fleet
     * @param defender the defending fleet
     * @param attackerAction the action chosen by the attacker
     * @param defenderAction the action chosen by the defender
     * @param events the events StringBuilder
     * @param attackerName the name of the attacker
     * @return the damage dealt
     */
    private int calculateDamage(NavalFleet attacker, NavalFleet defender,
                                FleetAction attackerAction, FleetAction defenderAction,
                                StringBuilder events, String attackerName) {
        int damage = 0;

        // Base combat power
        int combatPower = attacker.getTotalCombatPower();
        int defense = defender.getTotalDefenseRating();

        // Action modifiers
        int attackModifier = attackerAction.getCombatModifier();
        int defenseModifier = defenderAction.getDefenseModifier();

        // Formation bonuses
        for (NavalSquadron squadron : attacker.getSquadrons()) {
            attackModifier += squadron.getFormation().getFrontArcBonus();
        }
        for (NavalSquadron squadron : defender.getSquadrons()) {
            defenseModifier += squadron.getFormation().getDefenseBonus();
        }

        // Commander competence
        if (attacker.getCommander() != null) {
            attackModifier += attacker.getCommander().getCompetenceRating() / 2;
        }
        if (defender.getCommander() != null) {
            defenseModifier += defender.getCommander().getCompetenceRating() / 2;
        }

        // Calculate damage
        int effectiveAttack = combatPower + attackModifier;
        int effectiveDefense = defense + defenseModifier;

        // Roll for hit (2D6 vs 10)
        int hitRoll = DiceUtil.roll2D6();
        if (hitRoll + effectiveAttack >= 10 + effectiveDefense) {
            // Hit — damage is based on attack power
            damage = Math.max(1, effectiveAttack - effectiveDefense / 2);
            events.append(attackerName).append(" hits with ").append(attackerAction.getName());
        } else {
            events.append(attackerName).append(" misses with ").append(attackerAction.getName());
        }

        return damage;
    }

    /**
     * Applies damage to a fleet by removing ships.
     *
     * @param fleet the fleet to damage
     * @param damage the amount of damage
     */
    private void applyDamage(NavalFleet fleet, int damage) {
        if (damage <= 0) {
            return;
        }
        // Simplified: each point of damage removes one ship (starting from smallest)
        int shipsToRemove = Math.min(damage, fleet.getTotalShipCount());
        for (int i = 0; i < shipsToRemove; i++) {
            // Remove a random ship from a random squadron
            if (!fleet.getSquadrons().isEmpty()) {
                NavalSquadron squadron = fleet.getSquadrons().get(DiceUtil.rollIndex(fleet.getSquadrons().size()));
                if (!squadron.getShips().isEmpty()) {
                    ShipDesign ship = squadron.getShips().get(DiceUtil.rollIndex(squadron.getShips().size()));
                    squadron.removeShip(ship);
                    battleLog.add("  Ship destroyed: " + ship.getName());
                }
            }
        }
    }

    /**
     * Checks if the battle has ended and sets the outcome.
     */
    private void checkBattleEnd() {
        boolean fleet1Destroyed = fleet1.getTotalShipCount() == 0;
        boolean fleet2Destroyed = fleet2.getTotalShipCount() == 0;

        if (fleet1Destroyed && fleet2Destroyed) {
            outcome = BattleOutcome.DRAW;
            isActive = false;
            battleLog.add("Battle ended: Draw — both fleets destroyed");
        } else if (fleet2Destroyed) {
            outcome = BattleOutcome.FLEET_1_VICTORY;
            isActive = false;
            battleLog.add("Battle ended: " + fleet1.getName() + " wins");
        } else if (fleet1Destroyed) {
            outcome = BattleOutcome.FLEET_2_VICTORY;
            isActive = false;
            battleLog.add("Battle ended: " + fleet2.getName() + " wins");
        }
    }

    /**
     * Forces the battle to end with the given outcome.
     *
     * @param outcome the forced outcome
     */
    public void endBattle(BattleOutcome outcome) {
        this.outcome = Objects.requireNonNull(outcome, "Outcome must not be null");
        isActive = false;
        battleLog.add("Battle ended: " + outcome.getDescription());
    }

    /**
     * Cancels the battle.
     */
    public void cancelBattle() {
        endBattle(BattleOutcome.CANCELLED);
    }

    /**
     * Runs the entire battle automatically with random actions.
     */
    public void runAutomaticBattle() {
        while (isActive) {
            FleetAction action1 = randomAction();
            FleetAction action2 = randomAction();
            executeRound(action1, action2);
            // Safety limit
            if (currentRound > 50) {
                endBattle(BattleOutcome.DRAW);
                battleLog.add("Battle ended: Draw — round limit reached");
                break;
            }
        }
    }

    /**
     * Returns a random fleet action.
     *
     * @return a random action
     */
    private FleetAction randomAction() {
        FleetAction[] actions = FleetAction.values();
        return actions[DiceUtil.rollIndex(actions.length)];
    }

    // ---- Battle Summary ----

    /**
     * Returns a summary of the battle.
     *
     * @return the battle summary
     */
    public String getBattleSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Battle in ").append(battleSystem.getName()).append("\n");
        sb.append("Rounds: ").append(currentRound).append("\n");
        sb.append("Outcome: ").append(outcome.getDescription()).append("\n");
        sb.append("Fleet 1 (").append(fleet1.getName()).append("): ");
        sb.append(fleet1.getTotalShipCount()).append(" ships remaining\n");
        sb.append("Fleet 2 (").append(fleet2.getName()).append("): ");
        sb.append(fleet2.getTotalShipCount()).append(" ships remaining");
        return sb.toString();
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return getBattleSummary();
    }
}
