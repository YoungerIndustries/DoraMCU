package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * A unified battle that combines DoraMCU's Battle with Traveller's NavalBattle.
 * <p>
 * This class extends DoraMCU's round-by-round combat with Traveller's fleet actions,
 * formation bonuses, commander competence, and boarding phases.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Fleet actions from FleetAction enum (Attack, Defend, Maneuver, Board, Retreat)</li>
 *   <li>Formation bonuses from FleetFormation enum</li>
 *   <li>Commander competence bonuses</li>
 *   <li>Boarding action integration via HullModule grid</li>
 *   <li>Round-by-round combat with deep fleet copies</li>
 *   <li>Reinforcement support</li>
 *   <li>Event logging</li>
 * </ul>
 */
public class UnifiedBattle {

    /** Default maximum rounds before a battle is considered a stalemate. */
    public static final int DEFAULT_MAX_ROUNDS = 50;

    /** Default defender damage reduction bonus from planetary defenses. */
    public static final double DEFAULT_DEFENDER_BONUS = 0.15;

    /**
     * The outcome of a completed battle.
     */
    public enum BattleOutcome {
        /** The attacker fleet won. */
        ATTACKER_VICTORY("Attacker Victory"),
        /** The defender fleet won. */
        DEFENDER_VICTORY("Defender Victory"),
        /** Both fleets were destroyed. */
        MUTUAL_DESTRUCTION("Mutual Destruction"),
        /** The battle was ended manually before a natural conclusion. */
        RETREAT("Retreat"),
        /** The battle exceeded the maximum rounds and ended in stalemate. */
        STALEMATE("Stalemate");

        private final String description;

        BattleOutcome(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Represents a single event that occurred during a battle.
     */
    public static class BattleEvent {
        private final int roundNumber;
        private final EventType eventType;
        private final String description;

        public enum EventType {
            ROUND_COMPLETED,
            SHIP_DESTROYED,
            REINFORCEMENT_ARRIVED,
            BATTLE_STARTED,
            BATTLE_ENDED,
            DEFENDER_BONUS_APPLIED,
            FLEET_ACTION,
            BOARDING_PHASE,
            FORMATION_CHANGE
        }

        BattleEvent(int roundNumber, EventType eventType, String description) {
            this.roundNumber = roundNumber;
            this.eventType = eventType;
            this.description = description;
        }

        public int getRoundNumber() {
            return roundNumber;
        }

        public EventType getEventType() {
            return eventType;
        }

        public String getDescription() {
            return description;
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
        private final FleetAction attackerAction;
        private final FleetAction defenderAction;
        private final int attackerDamageDealt;
        private final int defenderDamageDealt;
        private final int attackerShipsDestroyed;
        private final int defenderShipsDestroyed;
        private final int attackerRemainingHull;
        private final int defenderRemainingHull;
        private final int attackerRemainingShields;
        private final int defenderRemainingShields;

        RoundResult(int roundNumber, FleetAction attackerAction, FleetAction defenderAction,
                    int attackerDamageDealt, int defenderDamageDealt,
                    int attackerShipsDestroyed, int defenderShipsDestroyed,
                    int attackerRemainingHull, int defenderRemainingHull,
                    int attackerRemainingShields, int defenderRemainingShields) {
            this.roundNumber = roundNumber;
            this.attackerAction = attackerAction;
            this.defenderAction = defenderAction;
            this.attackerDamageDealt = attackerDamageDealt;
            this.defenderDamageDealt = defenderDamageDealt;
            this.attackerShipsDestroyed = attackerShipsDestroyed;
            this.defenderShipsDestroyed = defenderShipsDestroyed;
            this.attackerRemainingHull = attackerRemainingHull;
            this.defenderRemainingHull = defenderRemainingHull;
            this.attackerRemainingShields = attackerRemainingShields;
            this.defenderRemainingShields = defenderRemainingShields;
        }

        public int getRoundNumber() {
            return roundNumber;
        }

        public FleetAction getAttackerAction() {
            return attackerAction;
        }

        public FleetAction getDefenderAction() {
            return defenderAction;
        }

        public int getAttackerDamageDealt() {
            return attackerDamageDealt;
        }

        public int getDefenderDamageDealt() {
            return defenderDamageDealt;
        }

        public int getAttackerShipsDestroyed() {
            return attackerShipsDestroyed;
        }

        public int getDefenderShipsDestroyed() {
            return defenderShipsDestroyed;
        }

        public int getAttackerRemainingHull() {
            return attackerRemainingHull;
        }

        public int getDefenderRemainingHull() {
            return defenderRemainingHull;
        }

        public int getAttackerRemainingShields() {
            return attackerRemainingShields;
        }

        public int getDefenderRemainingShields() {
            return defenderRemainingShields;
        }

        @Override
        public String toString() {
            return String.format("RoundResult{round=%d, atkAction=%s, defAction=%s, " +
                    "atkDmg=%d, defDmg=%d, atkLost=%d, defLost=%d}",
                    roundNumber, attackerAction, defenderAction,
                    attackerDamageDealt, defenderDamageDealt,
                    attackerShipsDestroyed, defenderShipsDestroyed);
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
        private final List<BoardingResult> boardingResults;

        public static class BoardingResult {
            private final int roundNumber;
            private final Ship boardingShip;
            private final Ship targetShip;
            private final BoardingAction.Outcome outcome;

            BoardingResult(int roundNumber, Ship boardingShip, Ship targetShip,
                          BoardingAction.Outcome outcome) {
                this.roundNumber = roundNumber;
                this.boardingShip = boardingShip;
                this.targetShip = targetShip;
                this.outcome = outcome;
            }

            public int getRoundNumber() {
                return roundNumber;
            }

            public Ship getBoardingShip() {
                return boardingShip;
            }

            public Ship getTargetShip() {
                return targetShip;
            }

            public BoardingAction.Outcome getOutcome() {
                return outcome;
            }
        }

        BattleResult(BattleOutcome outcome, int totalRounds,
                     int totalAttackerDamage, int totalDefenderDamage,
                     int totalAttackerShipsLost, int totalDefenderShipsLost,
                     int initialAttackerShips, int initialDefenderShips,
                     List<RoundResult> roundResults, List<BattleEvent> events,
                     List<BoardingResult> boardingResults) {
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
            this.boardingResults = Collections.unmodifiableList(new ArrayList<>(boardingResults));
        }

        public BattleOutcome getOutcome() {
            return outcome;
        }

        public int getTotalRounds() {
            return totalRounds;
        }

        public int getTotalAttackerDamage() {
            return totalAttackerDamage;
        }

        public int getTotalDefenderDamage() {
            return totalDefenderDamage;
        }

        public int getTotalAttackerShipsLost() {
            return totalAttackerShipsLost;
        }

        public int getTotalDefenderShipsLost() {
            return totalDefenderShipsLost;
        }

        public int getInitialAttackerShips() {
            return initialAttackerShips;
        }

        public int getInitialDefenderShips() {
            return initialDefenderShips;
        }

        public List<RoundResult> getRoundResults() {
            return roundResults;
        }

        public List<BattleEvent> getEvents() {
            return events;
        }

        public List<BoardingResult> getBoardingResults() {
            return boardingResults;
        }

        public double getCasualtyRatio() {
            if (totalDefenderShipsLost == 0) {
                return totalAttackerShipsLost > 0 ? Double.POSITIVE_INFINITY : 0.0;
            }
            return (double) totalAttackerShipsLost / totalDefenderShipsLost;
        }

        @Override
        public String toString() {
            return String.format("BattleResult{outcome=%s, rounds=%d, atkLost=%d/%d, defLost=%d/%d}",
                    outcome, totalRounds,
                    totalAttackerShipsLost, initialAttackerShips,
                    totalDefenderShipsLost, initialDefenderShips);
        }
    }

    private final int id;
    private final Fleet attacker;
    private final Fleet defender;
    private final Hex3D location;
    private Planet defenderPlanet;
    private double defenderBonus;
    private int maxRounds;
    private TravellerWorld battleSystem;

    // Battle state
    private BattleState state;
    private BattleOutcome outcome;
    private int currentRound;
    private final List<RoundResult> roundResults;
    private final List<BattleEvent> events;
    private final List<BattleResult.BoardingResult> boardingResults;

    // Deep copies for simulation
    private Fleet attackerCopy;
    private Fleet defenderCopy;

    // Statistics
    private int totalAttackerDamage;
    private int totalDefenderDamage;
    private int initialAttackerShipCount;
    private int initialDefenderShipCount;

    private final Random random;

    public enum BattleState {
        INITIATED,
        IN_PROGRESS,
        COMPLETED
    }

    /**
     * Creates a new unified battle between two fleets at the given location.
     *
     * @param id the unique battle identifier
     * @param attacker the attacking fleet
     * @param defender the defending fleet
     * @param location the hex position where the battle takes place
     */
    public UnifiedBattle(int id, Fleet attacker, Fleet defender, Hex3D location) {
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
        this.boardingResults = new ArrayList<BattleResult.BoardingResult>();
        this.totalAttackerDamage = 0;
        this.totalDefenderDamage = 0;
        this.random = new Random();
    }

    // ---- Accessors ----

    public int getId() {
        return id;
    }

    public Fleet getAttacker() {
        return attacker;
    }

    public Fleet getDefender() {
        return defender;
    }

    public Hex3D getLocation() {
        return location;
    }

    public BattleState getState() {
        return state;
    }

    public BattleOutcome getOutcome() {
        return outcome;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public List<RoundResult> getRoundResults() {
        return Collections.unmodifiableList(roundResults);
    }

    public List<BattleEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public List<BattleResult.BoardingResult> getBoardingResults() {
        return Collections.unmodifiableList(boardingResults);
    }

    public int getTotalAttackerDamage() {
        return totalAttackerDamage;
    }

    public int getTotalDefenderDamage() {
        return totalDefenderDamage;
    }

    public void setDefenderPlanet(Planet planet) {
        this.defenderPlanet = planet;
    }

    public void setBattleSystem(TravellerWorld system) {
        this.battleSystem = Objects.requireNonNull(system, "Battle system must not be null");
    }

    // ---- Battle lifecycle ----

    /**
     * Starts the battle by creating deep copies of both fleets.
     */
    public void start() {
        if (state != BattleState.INITIATED) {
            throw new IllegalStateException("Battle can only be started from INITIATED state, current: " + state);
        }

        attackerCopy = attacker.copyFleet();
        defenderCopy = defender.copyFleet();

        initialAttackerShipCount = attackerCopy.getShipCount();
        initialDefenderShipCount = defenderCopy.getShipCount();

        state = BattleState.IN_PROGRESS;
        addEvent(0, BattleEvent.EventType.BATTLE_STARTED,
                String.format("Battle started: %s vs %s at %s",
                        attacker.getName(), defender.getName(), location));
    }

    /**
     * Simulates a single round of combat with fleet actions.
     *
     * @param attackerAction the action chosen by the attacker
     * @param defenderAction the action chosen by the defender
     * @return the result of this round
     */
    public RoundResult fightRound(FleetAction attackerAction, FleetAction defenderAction) {
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

        // Log fleet actions
        addEvent(currentRound, BattleEvent.EventType.FLEET_ACTION,
                String.format("Attacker action: %s, Defender action: %s", attackerAction, defenderAction));

        // Calculate attacker damage with action modifiers
        int attackDamage = calculateDamage(attackerCopy, defenderCopy, attackerAction, defenderAction, true);
        int effectiveDefenderDamage = (int) Math.round(attackDamage * (1.0 - defenderBonus));
        totalAttackerDamage += effectiveDefenderDamage;
        if (defenderBonus > 0) {
            addEvent(currentRound, BattleEvent.EventType.DEFENDER_BONUS_APPLIED,
                    String.format("Defender bonus applied: %.0f%% damage reduction (%d -> %d)",
                            defenderBonus * 100, attackDamage, effectiveDefenderDamage));
        }
        defenderCopy.distributeDamage(effectiveDefenderDamage);

        // Check for boarding phase
        if (attackerAction == FleetAction.BOARD) {
            resolveBoardingPhase(attackerCopy, defenderCopy);
        }

        // Check if defender is destroyed after attacker fires
        if (defenderCopy.isDestroyed()) {
            RoundResult result = recordRoundResult(attackerAction, defenderAction,
                    attackDamage, 0,
                    attackerShipsBefore - attackerCopy.getActiveShipCount(),
                    defenderShipsBefore - defenderCopy.getActiveShipCount());
            endBattle(BattleOutcome.ATTACKER_VICTORY);
            return result;
        }

        // Defender fires back at attacker
        int defendDamage = calculateDamage(defenderCopy, attackerCopy, defenderAction, attackerAction, false);
        totalDefenderDamage += defendDamage;
        attackerCopy.distributeDamage(defendDamage);

        // Record destroyed ship events
        recordDestroyedShips();

        // Record round result
        RoundResult result = recordRoundResult(attackerAction, defenderAction,
                attackDamage, defendDamage,
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
     * Simulates a single round with default HOLD actions.
     *
     * @return the result of this round
     */
    public RoundResult fightRound() {
        FleetAction attackerAction = FleetAction.HOLD;
        FleetAction defenderAction = FleetAction.HOLD;

        // Use fleet's current action if available
        if (attacker instanceof UnifiedFleet uf) {
            attackerAction = uf.getCurrentAction();
        }
        if (defender instanceof UnifiedFleet uf) {
            defenderAction = uf.getCurrentAction();
        }

        return fightRound(attackerAction, defenderAction);
    }

    /**
     * Calculates damage with formation and action modifiers.
     */
    private int calculateDamage(Fleet attacker, Fleet defender,
                                FleetAction attackerAction, FleetAction defenderAction,
                                boolean isAttacker) {
        int baseDamage = attacker.getTotalDamage();

        // Action combat modifier
        int attackModifier = attackerAction.getCombatModifier();
        int defenseModifier = defenderAction.getDefenseModifier();

        // Formation bonuses
        int formationAttackBonus = 0;
        int formationDefenseBonus = 0;

        if (attacker instanceof UnifiedFleet ufa) {
            formationAttackBonus = ufa.getFormation().getFrontArcBonus() * ufa.getActiveShipCount();
        }
        if (defender instanceof UnifiedFleet ufd) {
            formationDefenseBonus = ufd.getFormation().getDefenseBonus() * ufd.getActiveShipCount();
        }

        // Commander competence bonus
        int commanderBonus = 0;
        if (attacker instanceof UnifiedFleet ufa && ufa.getCommander() != null) {
            commanderBonus += ufa.getCommander().getCompetenceRating() / 2;
        }

        // Apply modifiers
        int effectiveAttack = baseDamage + attackModifier + formationAttackBonus + commanderBonus;
        int effectiveDefense = defenseModifier + formationDefenseBonus;

        // Hit roll (2D6 vs 10)
        int hitRoll = random.nextInt(6) + 1 + random.nextInt(6) + 1;
        if (hitRoll + effectiveAttack >= 10 + effectiveDefense) {
            return Math.max(0, effectiveAttack);
        } else {
            // Miss — reduced damage
            return Math.max(0, effectiveAttack / 2);
        }
    }

    /**
     * Resolves the boarding phase when a fleet chooses the BOARD action.
     */
    private void resolveBoardingPhase(Fleet attacker, Fleet defender) {
        addEvent(currentRound, BattleEvent.EventType.BOARDING_PHASE,
                "Boarding phase initiated");

        // Find ships that can board (UnifiedShips with marines)
        List<Ship> attackerShips = attacker.getActiveShips();
        List<Ship> defenderShips = defender.getActiveShips();

        for (Ship atkShip : attackerShips) {
            if (atkShip instanceof UnifiedShip && !defenderShips.isEmpty()) {
                // Pick a random target
                Ship target = defenderShips.get(random.nextInt(defenderShips.size()));

                // Create a simple boarding result
                BoardingAction.Outcome boardingOutcome;

                // Simple resolution: attacker wins if it has more damage output
                if (atkShip.getTotalDamage() > target.getTotalDamage()) {
                    boardingOutcome = BoardingAction.Outcome.CAPTURED;
                } else if (atkShip.getTotalDamage() < target.getTotalDamage()) {
                    boardingOutcome = BoardingAction.Outcome.REPELLED;
                } else {
                    boardingOutcome = BoardingAction.Outcome.MUTUAL_DESTRUCTION;
                }

                boardingResults.add(new BattleResult.BoardingResult(currentRound, atkShip, target, boardingOutcome));
                addEvent(currentRound, BattleEvent.EventType.BOARDING_PHASE,
                        String.format("Boarding: %s -> %s = %s",
                                atkShip.getName(), target.getName(), boardingOutcome.getDescription()));

                // Apply boarding effects
                if (boardingOutcome == BoardingAction.Outcome.CAPTURED) {
                    // Captured ship is removed from defender
                    defender.removeShip(target);
                } else if (boardingOutcome == BoardingAction.Outcome.MUTUAL_DESTRUCTION) {
                    // Both ships destroyed
                    attacker.removeShip(atkShip);
                    defender.removeShip(target);
                }
            }
        }
    }

    /**
     * Fights the battle to completion with random actions.
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
            FleetAction action1 = randomAction();
            FleetAction action2 = randomAction();
            RoundResult round = fightRound(action1, action2);
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
                String.format("Attacker reinforced with %d ships", added));
        return added;
    }

    /**
     * Adds reinforcement ships to the defender fleet mid-battle.
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
                String.format("Defender reinforced with %d ships", added));
        return added;
    }

    // ---- Internal methods ----

    private void recordDestroyedShips() {
        for (Ship ship : attackerCopy.getShips()) {
            if (ship.isDestroyed()) {
                addEvent(currentRound, BattleEvent.EventType.SHIP_DESTROYED,
                        String.format("Attacker ship destroyed: %s", ship.getName()));
            }
        }
        for (Ship ship : defenderCopy.getShips()) {
            if (ship.isDestroyed()) {
                addEvent(currentRound, BattleEvent.EventType.SHIP_DESTROYED,
                        String.format("Defender ship destroyed: %s", ship.getName()));
            }
        }
    }

    private RoundResult recordRoundResult(FleetAction attackerAction, FleetAction defenderAction,
                                          int attackDamage, int defendDamage,
                                          int attackerLost, int defenderLost) {
        RoundResult result = new RoundResult(
                currentRound,
                attackerAction,
                defenderAction,
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
                        currentRound, attackDamage, defendDamage));
        return result;
    }

    private void endBattle(BattleOutcome outcome) {
        this.outcome = outcome;
        this.state = BattleState.COMPLETED;
        addEvent(currentRound, BattleEvent.EventType.BATTLE_ENDED,
                String.format("Battle ended: %s", outcome.getDescription()));
    }

    private void addEvent(int round, BattleEvent.EventType type, String description) {
        events.add(new BattleEvent(round, type, description));
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
                events,
                new ArrayList<>(boardingResults)
        );
    }

    private FleetAction randomAction() {
        FleetAction[] actions = FleetAction.values();
        return actions[random.nextInt(actions.length)];
    }

    // ---- Battle Summary ----

    /**
     * Returns a summary of the battle.
     *
     * @return the battle summary
     */
    public String getBattleSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Battle at ").append(location).append("\n");
        sb.append("Rounds: ").append(currentRound).append("\n");
        sb.append("Outcome: ").append(outcome != null ? outcome.getDescription() : "In Progress").append("\n");
        sb.append("Attacker (").append(attacker.getName()).append("): ");
        sb.append(attackerCopy != null ? attackerCopy.getActiveShipCount() : initialAttackerShipCount)
          .append(" ships remaining\n");
        sb.append("Defender (").append(defender.getName()).append("): ");
        sb.append(defenderCopy != null ? defenderCopy.getActiveShipCount() : initialDefenderShipCount)
          .append(" ships remaining");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getBattleSummary();
    }
}
