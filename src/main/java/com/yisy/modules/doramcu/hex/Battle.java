package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a structured battle encounter between two fleets in a 4X empire game.
 * <p>
 * Unlike the simple {@link Fleet#engage(Fleet)} method which returns a single result,
 * the Battle class provides a full battle lifecycle with round-by-round tracking,
 * defender bonuses from planetary defenses, event logging, and reinforcement support.
 * </p>
 * <p>
 * A battle progresses through states: INITIATED → IN_PROGRESS → COMPLETED.
 * Each round, both fleets fire simultaneously, damage is distributed to shields then hull,
 * and destroyed ships are removed. The battle ends when one fleet is completely destroyed
 * or when manually ended.
 * </p>
 * <p>
 * Defender bonuses can be applied when the defender is positioned at a planet with
 * defense structures. The defender receives a damage reduction bonus based on the
 * planet's defense level.
 * </p>
 */
public class Battle {

    /** Default maximum rounds before a battle is considered a stalemate. */
    public static final int DEFAULT_MAX_ROUNDS = 50;

    /** Default defender damage reduction bonus from planetary defenses. */
    public static final double DEFAULT_DEFENDER_BONUS = 0.15;

    /**
     * The current state of a battle.
     */
    public enum BattleState {
        /** Battle has been created but not yet started. */
        INITIATED,
        /** Battle is actively in progress; rounds are being processed. */
        IN_PROGRESS,
        /** Battle has concluded with a winner or draw. */
        COMPLETED
    }

    /**
     * The outcome of a completed battle.
     */
    public enum BattleOutcome {
        /** The attacker fleet won. */
        ATTACKER_VICTORY,
        /** The defender fleet won. */
        DEFENDER_VICTORY,
        /** Both fleets were destroyed. */
        MUTUAL_DESTRUCTION,
        /** The battle was ended manually before a natural conclusion. */
        RETREAT,
        /** The battle exceeded the maximum rounds and ended in stalemate. */
        STALEMATE
    }

    /**
     * Represents a single event that occurred during a battle.
     */
    public static class BattleEvent {
        private final int roundNumber;
        private final EventType eventType;
        private final String description;
        private final Ship ship;

        public enum EventType {
            /** A round of combat has been completed. */
            ROUND_COMPLETED,
            /** A ship has been destroyed. */
            SHIP_DESTROYED,
            /** A fleet has been reinforced with new ships. */
            REINFORCEMENT_ARRIVED,
            /** The battle has started. */
            BATTLE_STARTED,
            /** The battle has ended. */
            BATTLE_ENDED,
            /** A defender bonus was applied. */
            DEFENDER_BONUS_APPLIED
        }

        BattleEvent(int roundNumber, EventType eventType, String description, Ship ship) {
            this.roundNumber = roundNumber;
            this.eventType = eventType;
            this.description = description;
            this.ship = ship;
        }

        /** @return the round number when this event occurred */
        public int getRoundNumber() {
            return roundNumber;
        }

        /** @return the type of event */
        public EventType getEventType() {
            return eventType;
        }

        /** @return a human-readable description of the event */
        public String getDescription() {
            return description;
        }

        /** @return the ship involved in this event, or null if not applicable */
        public Ship getShip() {
            return ship;
        }

        @Override
        public String toString() {
            return String.format("BattleEvent{round=%d, type=%s, desc='%s'}",
                    roundNumber, eventType, description);
        }
    }

    /**
     * Represents the result of a single combat round.
     */
    public static class RoundResult {
        private final int roundNumber;
        private final int attackerDamageDealt;
        private final int defenderDamageDealt;
        private final int attackerShipsDestroyed;
        private final int defenderShipsDestroyed;
        private final int attackerRemainingHull;
        private final int defenderRemainingHull;
        private final int attackerRemainingShields;
        private final int defenderRemainingShields;

        RoundResult(int roundNumber, int attackerDamageDealt, int defenderDamageDealt,
                    int attackerShipsDestroyed, int defenderShipsDestroyed,
                    int attackerRemainingHull, int defenderRemainingHull,
                    int attackerRemainingShields, int defenderRemainingShields) {
            this.roundNumber = roundNumber;
            this.attackerDamageDealt = attackerDamageDealt;
            this.defenderDamageDealt = defenderDamageDealt;
            this.attackerShipsDestroyed = attackerShipsDestroyed;
            this.defenderShipsDestroyed = defenderShipsDestroyed;
            this.attackerRemainingHull = attackerRemainingHull;
            this.defenderRemainingHull = defenderRemainingHull;
            this.attackerRemainingShields = attackerRemainingShields;
            this.defenderRemainingShields = defenderRemainingShields;
        }

        /** @return the round number (1-based) */
        public int getRoundNumber() {
            return roundNumber;
        }

        /** @return the damage dealt by the attacker this round */
        public int getAttackerDamageDealt() {
            return attackerDamageDealt;
        }

        /** @return the damage dealt by the defender this round */
        public int getDefenderDamageDealt() {
            return defenderDamageDealt;
        }

        /** @return the number of attacker ships destroyed this round */
        public int getAttackerShipsDestroyed() {
            return attackerShipsDestroyed;
        }

        /** @return the number of defender ships destroyed this round */
        public int getDefenderShipsDestroyed() {
            return defenderShipsDestroyed;
        }

        /** @return the remaining hull points of the attacker fleet */
        public int getAttackerRemainingHull() {
            return attackerRemainingHull;
        }

        /** @return the remaining hull points of the defender fleet */
        public int getDefenderRemainingHull() {
            return defenderRemainingHull;
        }

        /** @return the remaining shield points of the attacker fleet */
        public int getAttackerRemainingShields() {
            return attackerRemainingShields;
        }

        /** @return the remaining shield points of the defender fleet */
        public int getDefenderRemainingShields() {
            return defenderRemainingShields;
        }

        @Override
        public String toString() {
            return String.format("RoundResult{round=%d, atkDmg=%d, defDmg=%d, " +
                    "atkLost=%d, defLost=%d, atkHull=%d, defHull=%d}",
                    roundNumber, attackerDamageDealt, defenderDamageDealt,
                    attackerShipsDestroyed, defenderShipsDestroyed,
                    attackerRemainingHull, defenderRemainingHull);
        }
    }

    /**
     * Comprehensive result of a completed battle.
     */
    public static class BattleResult {
        private final BattleOutcome outcome;
        private final int totalRounds;
        private final int totalAttackerDamage;
        private final int totalDefenderDamage;
        private final int totalAttackerShipsLost;
        private final int totalDefenderShipsLost;
        private final int initialAttackerShips;
        private final int initialDefenderShips;
        private final List<RoundResult> roundResults;
        private final List<BattleEvent> events;

        BattleResult(BattleOutcome outcome, int totalRounds,
                     int totalAttackerDamage, int totalDefenderDamage,
                     int totalAttackerShipsLost, int totalDefenderShipsLost,
                     int initialAttackerShips, int initialDefenderShips,
                     List<RoundResult> roundResults, List<BattleEvent> events) {
            this.outcome = outcome;
            this.totalRounds = totalRounds;
            this.totalAttackerDamage = totalAttackerDamage;
            this.totalDefenderDamage = totalDefenderDamage;
            this.totalAttackerShipsLost = totalAttackerShipsLost;
            this.totalDefenderShipsLost = totalDefenderShipsLost;
            this.initialAttackerShips = initialAttackerShips;
            this.initialDefenderShips = initialDefenderShips;
            this.roundResults = Collections.unmodifiableList(new ArrayList<>(roundResults));
            this.events = Collections.unmodifiableList(new ArrayList<>(events));
        }

        /** @return the outcome of the battle */
        public BattleOutcome getOutcome() {
            return outcome;
        }

        /** @return the total number of rounds fought */
        public int getTotalRounds() {
            return totalRounds;
        }

        /** @return the total damage dealt by the attacker across all rounds */
        public int getTotalAttackerDamage() {
            return totalAttackerDamage;
        }

        /** @return the total damage dealt by the defender across all rounds */
        public int getTotalDefenderDamage() {
            return totalDefenderDamage;
        }

        /** @return the total number of attacker ships lost */
        public int getTotalAttackerShipsLost() {
            return totalAttackerShipsLost;
        }

        /** @return the total number of defender ships lost */
        public int getTotalDefenderShipsLost() {
            return totalDefenderShipsLost;
        }

        /** @return the initial number of ships in the attacker fleet */
        public int getInitialAttackerShips() {
            return initialAttackerShips;
        }

        /** @return the initial number of ships in the defender fleet */
        public int getInitialDefenderShips() {
            return initialDefenderShips;
        }

        /** @return the list of round-by-round results */
        public List<RoundResult> getRoundResults() {
            return roundResults;
        }

        /** @return the list of all battle events */
        public List<BattleEvent> getEvents() {
            return events;
        }

        /**
         * Returns the casualty ratio (attacker lost / defender lost).
         * A ratio less than 1.0 favors the attacker.
         *
         * @return the casualty ratio, or 0.0 if no ships were lost
         */
        public double getCasualtyRatio() {
            if (totalDefenderShipsLost == 0) {
                return totalAttackerShipsLost > 0 ? Double.POSITIVE_INFINITY : 0.0;
            }
            return (double) totalAttackerShipsLost / totalDefenderShipsLost;
        }

        @Override
        public String toString() {
            return String.format("BattleResult{outcome=%s, rounds=%d, atkLost=%d/%d, defLost=%d/%d, " +
                    "atkDmg=%d, defDmg=%d}",
                    outcome, totalRounds,
                    totalAttackerShipsLost, initialAttackerShips,
                    totalDefenderShipsLost, initialDefenderShips,
                    totalAttackerDamage, totalDefenderDamage);
        }
    }

    private final int id;
    private final Fleet attacker;
    private final Fleet defender;
    private final Hex3D location;
    private Planet defenderPlanet;
    private double defenderBonus;
    private int maxRounds;

    // Battle state
    private BattleState state;
    private BattleOutcome outcome;
    private int currentRound;
    private final List<RoundResult> roundResults;
    private final List<BattleEvent> events;

    // Deep copies for simulation
    private Fleet attackerCopy;
    private Fleet defenderCopy;

    // Statistics
    private int totalAttackerDamage;
    private int totalDefenderDamage;
    private int initialAttackerShipCount;
    private int initialDefenderShipCount;

    /**
     * Creates a new battle between two fleets at the given location.
     *
     * @param id the unique battle identifier
     * @param attacker the attacking fleet
     * @param defender the defending fleet
     * @param location the hex position where the battle takes place
     */
    public Battle(int id, Fleet attacker, Fleet defender, Hex3D location) {
        if (attacker == null) {
            throw new IllegalArgumentException("Attacker fleet must not be null");
        }
        if (defender == null) {
            throw new IllegalArgumentException("Defender fleet must not be null");
        }
        if (attacker.isEmpty()) {
            throw new IllegalArgumentException("Attacker fleet must not be empty");
        }
        if (defender.isEmpty()) {
            throw new IllegalArgumentException("Defender fleet must not be empty");
        }
        this.id = id;
        this.attacker = attacker;
        this.defender = defender;
        this.location = location;
        this.defenderBonus = DEFAULT_DEFENDER_BONUS;
        this.maxRounds = DEFAULT_MAX_ROUNDS;
        this.state = BattleState.INITIATED;
        this.outcome = null;
        this.currentRound = 0;
        this.roundResults = new ArrayList<>();
        this.events = new ArrayList<>();
        this.totalAttackerDamage = 0;
        this.totalDefenderDamage = 0;
    }

    // ---- Accessors ----

    /** @return the unique battle identifier */
    public int getId() {
        return id;
    }

    /** @return the original attacker fleet (not the simulation copy) */
    public Fleet getAttacker() {
        return attacker;
    }

    /** @return the original defender fleet (not the simulation copy) */
    public Fleet getDefender() {
        return defender;
    }

    /** @return the hex position where the battle takes place */
    public Hex3D getLocation() {
        return location;
    }

    /**
     * Sets the planet where the defender is positioned.
     * When a defender planet is set, the defender receives a damage reduction bonus.
     *
     * @param planet the defender's planet, or null to remove the bonus
     */
    public void setDefenderPlanet(Planet planet) {
        this.defenderPlanet = planet;
    }

    /** @return the current battle state */
    public BattleState getState() {
        return state;
    }

    /** @return the battle outcome, or null if not yet completed */
    public BattleOutcome getOutcome() {
        return outcome;
    }

    /** @return the current round number (0 before battle starts) */
    public int getCurrentRound() {
        return currentRound;
    }

    /** @return the list of round-by-round results so far */
    public List<RoundResult> getRoundResults() {
        return Collections.unmodifiableList(roundResults);
    }

    /** @return the list of all battle events so far */
    public List<BattleEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /** @return the total damage dealt by the attacker so far */
    public int getTotalAttackerDamage() {
        return totalAttackerDamage;
    }

    /** @return the total damage dealt by the defender so far */
    public int getTotalDefenderDamage() {
        return totalDefenderDamage;
    }

    // ---- Battle lifecycle ----

    /**
     * Starts the battle by creating deep copies of both fleets.
     * After calling this, the battle transitions to IN_PROGRESS state.
     */
    public void start() {
        if (state != BattleState.INITIATED) {
            throw new IllegalStateException("Battle can only be started from INITIATED state, current: " + state);
        }

        // Create deep copies for simulation
        attackerCopy = attacker.copyFleet();
        defenderCopy = defender.copyFleet();

        initialAttackerShipCount = attackerCopy.getShipCount();
        initialDefenderShipCount = defenderCopy.getShipCount();

        state = BattleState.IN_PROGRESS;
        addEvent(0, BattleEvent.EventType.BATTLE_STARTED,
                String.format("Battle started: %s vs %s at %s",
                        attacker.getName(), defender.getName(), location), null);
    }

    /**
     * Simulates a single round of combat.
     * Both fleets fire simultaneously, damage is distributed, and destroyed ships are tracked.
     *
     * @return the result of this round
     */
    public RoundResult fightRound() {
        if (state != BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Battle must be in progress to fight a round, current: " + state);
        }

        currentRound++;

        // Check for stalemate
        if (currentRound > maxRounds) {
            endBattle(BattleOutcome.STALEMATE);
            return null;
        }

        int attackerShipsBefore = attackerCopy.getActiveShipCount();
        int defenderShipsBefore = defenderCopy.getActiveShipCount();

        // Attacker fires at defender
        int attackDamage = attackerCopy.getTotalDamage();
        int effectiveDefenderDamage = (int) Math.round(attackDamage * (1.0 - defenderBonus));
        totalAttackerDamage += effectiveDefenderDamage;
        if (defenderBonus > 0) {
            addEvent(currentRound, BattleEvent.EventType.DEFENDER_BONUS_APPLIED,
                    String.format("Defender bonus applied: %.0f%% damage reduction (%d -> %d)",
                            defenderBonus * 100, attackDamage, effectiveDefenderDamage), null);
        }
        defenderCopy.distributeDamage(effectiveDefenderDamage);

        // Check if defender is destroyed after attacker fires
        if (defenderCopy.isDestroyed()) {
            recordRoundResult(attackDamage, 0,
                    attackerShipsBefore - attackerCopy.getActiveShipCount(),
                    defenderShipsBefore - defenderCopy.getActiveShipCount());
            endBattle(BattleOutcome.ATTACKER_VICTORY);
            return roundResults.get(roundResults.size() - 1);
        }

        // Defender fires back at attacker
        int defendDamage = defenderCopy.getTotalDamage();
        totalDefenderDamage += defendDamage;
        attackerCopy.distributeDamage(defendDamage);

        // Record destroyed ship events
        recordDestroyedShips(attackerCopy, defenderCopy);

        // Record round result
        RoundResult result = recordRoundResult(attackDamage, defendDamage,
                attackerShipsBefore - attackerCopy.getActiveShipCount(),
                defenderShipsBefore - defenderCopy.getActiveShipCount());

        // Check if battle is over
        if (attackerCopy.isDestroyed() && defenderCopy.isDestroyed()) {
            endBattle(BattleOutcome.MUTUAL_DESTRUCTION);
        } else if (attackerCopy.isDestroyed()) {
            endBattle(BattleOutcome.DEFENDER_VICTORY);
        } else if (defenderCopy.isDestroyed()) {
            endBattle(BattleOutcome.ATTACKER_VICTORY);
        }

        return result;
    }

    /**
     * Fights the battle to completion, running all remaining rounds.
     *
     * @return the final battle result
     */
    public BattleResult fightToCompletion() {
        if (state == BattleState.INITIATED) {
            start();
        }
        if (state == BattleState.COMPLETED) {
            return getBattleResult();
        }

        while (state == BattleState.IN_PROGRESS) {
            RoundResult round = fightRound();
            if (round == null) break; // stalemate
        }

        return getBattleResult();
    }

    /**
     * Ends the battle prematurely with a RETREAT outcome.
     */
    public void retreat() {
        if (state != BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Can only retreat during an active battle, current: " + state);
        }
        endBattle(BattleOutcome.RETREAT);
    }

    /**
     * Adds reinforcement ships to the attacker fleet mid-battle.
     *
     * @param reinforcements the fleet containing ships to add
     * @return the number of ships added
     */
    public int reinforceAttacker(Fleet reinforcements) {
        if (state != BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Can only reinforce during an active battle, current: " + state);
        }
        if (reinforcements == null || reinforcements.isEmpty()) {
            return 0;
        }
        int added = 0;
        for (Ship ship : reinforcements.getActiveShips()) {
            attackerCopy.addShip(ship);
            added++;
        }
        addEvent(currentRound, BattleEvent.EventType.REINFORCEMENT_ARRIVED,
                String.format("Attacker reinforced with %d ships", added), null);
        return added;
    }

    /**
     * Adds reinforcement ships to the defender fleet mid-battle.
     *
     * @param reinforcements the fleet containing ships to add
     * @return the number of ships added
     */
    public int reinforceDefender(Fleet reinforcements) {
        if (state != BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Can only reinforce during an active battle, current: " + state);
        }
        if (reinforcements == null || reinforcements.isEmpty()) {
            return 0;
        }
        int added = 0;
        for (Ship ship : reinforcements.getActiveShips()) {
            defenderCopy.addShip(ship);
            added++;
        }
        addEvent(currentRound, BattleEvent.EventType.REINFORCEMENT_ARRIVED,
                String.format("Defender reinforced with %d ships", added), null);
        return added;
    }

    // ---- Internal methods ----

    private void recordDestroyedShips(Fleet attackerCopy, Fleet defenderCopy) {
        // Find newly destroyed ships in attacker
        for (Ship ship : attackerCopy.getShips()) {
            if (ship.isDestroyed()) {
                addEvent(currentRound, BattleEvent.EventType.SHIP_DESTROYED,
                        String.format("Attacker ship destroyed: %s", ship.getName()), ship);
            }
        }
        // Find newly destroyed ships in defender
        for (Ship ship : defenderCopy.getShips()) {
            if (ship.isDestroyed()) {
                addEvent(currentRound, BattleEvent.EventType.SHIP_DESTROYED,
                        String.format("Defender ship destroyed: %s", ship.getName()), ship);
            }
        }
    }

    private RoundResult recordRoundResult(int attackDamage, int defendDamage,
                                          int attackerLost, int defenderLost) {
        RoundResult result = new RoundResult(
                currentRound,
                attackDamage,
                defendDamage,
                attackerLost,
                defenderLost,
                attackerCopy.getCurrentHullPoints(),
                defenderCopy.getCurrentHullPoints(),
                attackerCopy.getCurrentShieldPoints(),
                defenderCopy.getCurrentShieldPoints()
        );
        roundResults.add(result);
        addEvent(currentRound, BattleEvent.EventType.ROUND_COMPLETED,
                String.format("Round %d: attacker dealt %d, defender dealt %d",
                        currentRound, attackDamage, defendDamage), null);
        return result;
    }

    private void endBattle(BattleOutcome outcome) {
        this.outcome = outcome;
        this.state = BattleState.COMPLETED;
        addEvent(currentRound, BattleEvent.EventType.BATTLE_ENDED,
                String.format("Battle ended: %s", outcome), null);
    }

    private void addEvent(int round, BattleEvent.EventType type, String description, Ship ship) {
        events.add(new BattleEvent(round, type, description, ship));
    }

    private BattleResult getBattleResult() {
        int attackerLost = initialAttackerShipCount - attackerCopy.getActiveShipCount();
        int defenderLost = initialDefenderShipCount - defenderCopy.getActiveShipCount();
        return new BattleResult(
                outcome,
                currentRound,
                totalAttackerDamage,
                totalDefenderDamage,
                attackerLost,
                defenderLost,
                initialAttackerShipCount,
                initialDefenderShipCount,
                roundResults,
                events
        );
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Battle battle = (Battle) o;
        return id == battle.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Battle{id=%d, state=%s, round=%d, outcome=%s, location=%s, " +
                        "attacker='%s'(%d ships), defender='%s'(%d ships)}",
                id, state, currentRound, outcome, location,
                attacker.getName(), attacker.getShipCount(),
                defender.getName(), defender.getShipCount());
    }
}
