package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Response DTO for a ship-to-ship battle result.
 * <p>
 * Contains the complete battle outcome including round-by-round results,
 * damage statistics, ship losses, and event logs.
 * </p>
 */
public class BattleResponse {

    /**
     * Unique battle identifier.
     */
    private int battleId;

    /**
     * Name of the battle.
     */
    private String battleName;

    /**
     * Battle outcome (ATTACKER_VICTORY, DEFENDER_VICTORY, MUTUAL_DESTRUCTION, etc.).
     */
    private String outcome;

    /**
     * Total number of rounds fought.
     */
    private int totalRounds;

    /**
     * Total damage dealt by the attacker.
     */
    private int totalAttackerDamage;

    /**
     * Total damage dealt by the defender.
     */
    private int totalDefenderDamage;

    /**
     * Number of attacker ships lost.
     */
    private int attackerShipsLost;

    /**
     * Number of defender ships lost.
     */
    private int defenderShipsLost;

    /**
     * Initial number of attacker ships.
     */
    private int initialAttackerShips;

    /**
     * Initial number of defender ships.
     */
    private int initialDefenderShips;

    /**
     * Attacker ship design name.
     */
    private String attackerShipName;

    /**
     * Defender ship design name.
     */
    private String defenderShipName;

    /**
     * Round-by-round results.
     */
    private List<RoundResult> roundResults;

    /**
     * Event log entries.
     */
    private List<String> eventLog;

    // ---- Default constructor for Jackson ----

    public BattleResponse() {
    }

    // ---- Accessors ----

    public int getBattleId() {
        return battleId;
    }

    public void setBattleId(int battleId) {
        this.battleId = battleId;
    }

    public String getBattleName() {
        return battleName;
    }

    public void setBattleName(String battleName) {
        this.battleName = battleName;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public int getTotalAttackerDamage() {
        return totalAttackerDamage;
    }

    public void setTotalAttackerDamage(int totalAttackerDamage) {
        this.totalAttackerDamage = totalAttackerDamage;
    }

    public int getTotalDefenderDamage() {
        return totalDefenderDamage;
    }

    public void setTotalDefenderDamage(int totalDefenderDamage) {
        this.totalDefenderDamage = totalDefenderDamage;
    }

    public int getAttackerShipsLost() {
        return attackerShipsLost;
    }

    public void setAttackerShipsLost(int attackerShipsLost) {
        this.attackerShipsLost = attackerShipsLost;
    }

    public int getDefenderShipsLost() {
        return defenderShipsLost;
    }

    public void setDefenderShipsLost(int defenderShipsLost) {
        this.defenderShipsLost = defenderShipsLost;
    }

    public int getInitialAttackerShips() {
        return initialAttackerShips;
    }

    public void setInitialAttackerShips(int initialAttackerShips) {
        this.initialAttackerShips = initialAttackerShips;
    }

    public int getInitialDefenderShips() {
        return initialDefenderShips;
    }

    public void setInitialDefenderShips(int initialDefenderShips) {
        this.initialDefenderShips = initialDefenderShips;
    }

    public String getAttackerShipName() {
        return attackerShipName;
    }

    public void setAttackerShipName(String attackerShipName) {
        this.attackerShipName = attackerShipName;
    }

    public String getDefenderShipName() {
        return defenderShipName;
    }

    public void setDefenderShipName(String defenderShipName) {
        this.defenderShipName = defenderShipName;
    }

    public List<RoundResult> getRoundResults() {
        return roundResults;
    }

    public void setRoundResults(List<RoundResult> roundResults) {
        this.roundResults = roundResults;
    }

    public List<String> getEventLog() {
        return eventLog;
    }

    public void setEventLog(List<String> eventLog) {
        this.eventLog = eventLog;
    }

    /**
     * Returns the casualty ratio (attacker lost / defender lost).
     * A ratio less than 1.0 favors the attacker.
     */
    public double getCasualtyRatio() {
        if (defenderShipsLost == 0) {
            return attackerShipsLost > 0 ? Double.POSITIVE_INFINITY : 0.0;
        }
        return (double) attackerShipsLost / defenderShipsLost;
    }

    /**
     * Round-by-round result summary.
     */
    public static class RoundResult {
        private int roundNumber;
        private int attackerDamageDealt;
        private int defenderDamageDealt;
        private int attackerShipsDestroyed;
        private int defenderShipsDestroyed;
        private int attackerRemainingHull;
        private int defenderRemainingHull;

        public RoundResult() {
        }

        public RoundResult(int roundNumber, int attackerDamageDealt, int defenderDamageDealt,
                            int attackerShipsDestroyed, int defenderShipsDestroyed,
                            int attackerRemainingHull, int defenderRemainingHull) {
            this.roundNumber = roundNumber;
            this.attackerDamageDealt = attackerDamageDealt;
            this.defenderDamageDealt = defenderDamageDealt;
            this.attackerShipsDestroyed = attackerShipsDestroyed;
            this.defenderShipsDestroyed = defenderShipsDestroyed;
            this.attackerRemainingHull = attackerRemainingHull;
            this.defenderRemainingHull = defenderRemainingHull;
        }

        public int getRoundNumber() {
            return roundNumber;
        }

        public void setRoundNumber(int roundNumber) {
            this.roundNumber = roundNumber;
        }

        public int getAttackerDamageDealt() {
            return attackerDamageDealt;
        }

        public void setAttackerDamageDealt(int attackerDamageDealt) {
            this.attackerDamageDealt = attackerDamageDealt;
        }

        public int getDefenderDamageDealt() {
            return defenderDamageDealt;
        }

        public void setDefenderDamageDealt(int defenderDamageDealt) {
            this.defenderDamageDealt = defenderDamageDealt;
        }

        public int getAttackerShipsDestroyed() {
            return attackerShipsDestroyed;
        }

        public void setAttackerShipsDestroyed(int attackerShipsDestroyed) {
            this.attackerShipsDestroyed = attackerShipsDestroyed;
        }

        public int getDefenderShipsDestroyed() {
            return defenderShipsDestroyed;
        }

        public void setDefenderShipsDestroyed(int defenderShipsDestroyed) {
            this.defenderShipsDestroyed = defenderShipsDestroyed;
        }

        public int getAttackerRemainingHull() {
            return attackerRemainingHull;
        }

        public void setAttackerRemainingHull(int attackerRemainingHull) {
            this.attackerRemainingHull = attackerRemainingHull;
        }

        public int getDefenderRemainingHull() {
            return defenderRemainingHull;
        }

        public void setDefenderRemainingHull(int defenderRemainingHull) {
            this.defenderRemainingHull = defenderRemainingHull;
        }
    }
}
