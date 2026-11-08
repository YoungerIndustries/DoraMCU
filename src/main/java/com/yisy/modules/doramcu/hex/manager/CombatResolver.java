package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Battle;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.traveller.AbstractBattle;
import com.yisy.modules.doramcu.traveller.HighGuardRangeBand;
import com.yisy.modules.doramcu.traveller.HighGuardShipCombat;
import com.yisy.modules.doramcu.traveller.HighGuardShipDesign;
import com.yisy.modules.doramcu.traveller.MercenaryUnit;
import com.yisy.modules.doramcu.traveller.MissionType;
import com.yisy.modules.doramcu.traveller.ShipCombatStatus;
import com.yisy.modules.doramcu.traveller.ShipDesign;
import com.yisy.modules.doramcu.traveller.ShipToShipBattle;
import com.yisy.modules.doramcu.traveller.UnitType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages combat operations: battle creation, fighting, retreats, and reinforcements.
 * <p>
 * Includes Dora AI integration for AI-assisted combat analysis.
 * </p>
 */
public class CombatResolver {

    private final GameContext context;

    /**
     * Creates a new CombatResolver.
     *
     * @param context the shared game context
     */
    public CombatResolver(GameContext context) {
        this.context = context;
    }

    /**
     * Generates combat analysis for active battles.
     *
     * @return the combat analysis string
     */
    public String getCombatAnalysis() {
        List<Battle> activeBattles = getActiveBattles();
        return String.format(
            "%d active battles in progress",
            activeBattles.size()
        );
    }

    /**
     * Creates a new structured battle between two fleets at the given location.
     *
     * @param attacker the attacking fleet
     * @param defender the defending fleet
     * @param location the hex position where the battle takes place
     * @return the new battle
     */
    public Battle createBattle(Fleet attacker, Fleet defender, Hex3D location) {
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
        int battleId = context.getNextBattleIdAndIncrement();
        Battle battle = new Battle(battleId, attacker, defender, location);
        return context.addBattle(battle);
    }

    /**
     * Creates a new structured battle with a defender planet for bonus damage reduction.
     *
     * @param attacker the attacking fleet
     * @param defender the defending fleet
     * @param location the hex position where the battle takes place
     * @param defenderPlanet the planet where the defender is positioned
     * @return the new battle
     */
    public Battle createBattle(Fleet attacker, Fleet defender, Hex3D location, Planet defenderPlanet) {
        Battle battle = createBattle(attacker, defender, location);
        battle.setDefenderPlanet(defenderPlanet);
        return battle;
    }

    /**
     * Fights a battle to completion and returns the result.
     *
     * @param battleId the battle ID
     * @return the battle result
     */
    public Battle.BattleResult fightBattle(int battleId) {
        Battle battle = context.getBattle(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Battle not found: " + battleId);
        }
        return battle.fightToCompletion();
    }

    /**
     * Fights a battle one round at a time.
     *
     * @param battleId the battle ID
     * @return the round result, or null if the battle ended in stalemate
     */
    public Battle.RoundResult fightBattleRound(int battleId) {
        Battle battle = context.getBattle(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Battle not found: " + battleId);
        }
        return battle.fightRound();
    }

    /**
     * Starts a battle that has been created but not yet started.
     *
     * @param battleId the battle ID
     */
    public void startBattle(int battleId) {
        Battle battle = context.getBattle(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Battle not found: " + battleId);
        }
        battle.start();
    }

    /**
     * Retreats from an active battle, ending it prematurely.
     *
     * @param battleId the battle ID
     */
    public void retreatBattle(int battleId) {
        Battle battle = context.getBattle(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Battle not found: " + battleId);
        }
        battle.retreat();
    }

    /**
     * Adds reinforcement ships to the attacker side of an active battle.
     *
     * @param battleId the battle ID
     * @param reinforcements the fleet containing ships to add
     * @return the number of ships added
     */
    public int reinforceAttacker(int battleId, Fleet reinforcements) {
        Battle battle = context.getBattle(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Battle not found: " + battleId);
        }
        return battle.reinforceAttacker(reinforcements);
    }

    /**
     * Adds reinforcement ships to the defender side of an active battle.
     *
     * @param battleId the battle ID
     * @param reinforcements the fleet containing ships to add
     * @return the number of ships added
     */
    public int reinforceDefender(int battleId, Fleet reinforcements) {
        Battle battle = context.getBattle(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Battle not found: " + battleId);
        }
        return battle.reinforceDefender(reinforcements);
    }

    /**
     * Returns all active (in-progress) battles.
     *
     * @return list of active battles
     */
    public List<Battle> getActiveBattles() {
        return context.getBattles().values().stream()
                .filter(b -> b.getState() == Battle.BattleState.IN_PROGRESS)
                .collect(Collectors.toList());
    }

    /**
     * Returns all completed battles.
     *
     * @return list of completed battles
     */
    public List<Battle> getCompletedBattles() {
        return context.getBattles().values().stream()
                .filter(b -> b.getState() == Battle.BattleState.COMPLETED)
                .collect(Collectors.toList());
    }

    // ============================================================
    // Traveller Combat System Delegation
    // ============================================================

    /**
     * Result of a Traveller-style combat resolution.
     */
    public static class CombatResult {
        private final CombatType type;
        private final String winner;
        private final String loser;
        private final int attackerDamage;
        private final int defenderDamage;
        private final String details;

        public CombatResult(CombatType type, String winner, String loser,
                            int attackerDamage, int defenderDamage, String details) {
            this.type = type;
            this.winner = winner;
            this.loser = loser;
            this.attackerDamage = attackerDamage;
            this.defenderDamage = defenderDamage;
            this.details = details;
        }

        public CombatType getType() { return type; }
        public String getWinner() { return winner; }
        public String getLoser() { return loser; }
        public int getAttackerDamage() { return attackerDamage; }
        public int getDefenderDamage() { return defenderDamage; }
        public String getDetails() { return details; }

        @Override
        public String toString() {
            return String.format("CombatResult{type=%s, winner='%s', loser='%s', attackerDmg=%d, defenderDmg=%d}",
                    type, winner, loser, attackerDamage, defenderDamage);
        }
    }

    /**
     * Types of combat systems available.
     */
    public enum CombatType {
        /** Hex fleet-vs-fleet combat using Battle system. */
        FLEET_BATTLE,
        /** Traveller ship-to-ship combat using ShipToShipBattle. */
        SHIP_DUEL,
        /** High Guard naval combat using HighGuardShipCombat. */
        HIGH_GUARD_NAVAL,
        /** Mercenary abstract ground combat using AbstractBattle. */
        GROUND_COMBAT
    }

    /**
     * Resolves combat between two Traveller ShipDesigns using ShipToShipBattle.
     * <p>
     * Converts each design to a ShipCombatStatus and runs the battle to completion.
     * </p>
     *
     * @param attackerDesign the attacking ship design
     * @param defenderDesign the defending ship design
     * @return the combat result
     */
    public CombatResult resolveShipDesignCombat(ShipDesign attackerDesign, ShipDesign defenderDesign) {
        if (attackerDesign == null) {
            throw new IllegalArgumentException("Attacker design must not be null");
        }
        if (defenderDesign == null) {
            throw new IllegalArgumentException("Defender design must not be null");
        }

        List<ShipCombatStatus> side0 = new ArrayList<>();
        side0.add(new ShipCombatStatus(attackerDesign, attackerDesign.getName(), 0));
        List<ShipCombatStatus> side1 = new ArrayList<>();
        side1.add(new ShipCombatStatus(defenderDesign, defenderDesign.getName(), 1));

        ShipToShipBattle battle = new ShipToShipBattle(side0, side1);
        ShipToShipBattle.BattleResult result = battle.fightToCompletion();

        String winner;
        String loser;
        switch (result.getOutcome()) {
            case SIDE_0_VICTORY:
                winner = attackerDesign.getName();
                loser = defenderDesign.getName();
                break;
            case SIDE_1_VICTORY:
                winner = defenderDesign.getName();
                loser = attackerDesign.getName();
                break;
            case MUTUAL_DESTRUCTION:
            default:
                winner = "Draw";
                loser = "Draw";
                break;
        }

        return new CombatResult(CombatType.SHIP_DUEL, winner, loser, 0, 0,
                result.getOutcome().name() + " in " + result.getTotalRounds() + " rounds");
    }

    /**
     * Resolves combat between two High Guard ShipDesigns using HighGuardShipCombat.
     *
     * @param attackerDesign the attacking ship design
     * @param defenderDesign the defending ship design
     * @return the combat result
     */
    public CombatResult resolveHighGuardCombat(HighGuardShipDesign attackerDesign, HighGuardShipDesign defenderDesign) {
        if (attackerDesign == null) {
            throw new IllegalArgumentException("Attacker design must not be null");
        }
        if (defenderDesign == null) {
            throw new IllegalArgumentException("Defender design must not be null");
        }

        HighGuardShipCombat combat = new HighGuardShipCombat(attackerDesign, defenderDesign,
                HighGuardRangeBand.MEDIUM);
        HighGuardShipCombat.CombatOutcome outcome = combat.fightToCompletion();

        String winner;
        String loser;
        switch (outcome) {
            case SIDE_0_VICTORY:
                winner = attackerDesign.getName();
                loser = defenderDesign.getName();
                break;
            case SIDE_1_VICTORY:
                winner = defenderDesign.getName();
                loser = attackerDesign.getName();
                break;
            case MUTUAL_DESTRUCTION:
            default:
                winner = "Draw";
                loser = "Draw";
                break;
        }

        return new CombatResult(CombatType.HIGH_GUARD_NAVAL, winner, loser, 0, 0,
                outcome.name());
    }

    /**
     * Resolves ground combat between two Mercenary units using AbstractBattle.
     * <p>
     * Converts each unit to a BattleForce and runs the abstract battle to completion.
     * </p>
     *
     * @param attackerUnit the attacking mercenary unit
     * @param defenderUnit the defending mercenary unit
     * @return the combat result
     */
    public CombatResult resolveGroundCombat(MercenaryUnit attackerUnit, MercenaryUnit defenderUnit) {
        if (attackerUnit == null) {
            throw new IllegalArgumentException("Attacker unit must not be null");
        }
        if (defenderUnit == null) {
            throw new IllegalArgumentException("Defender unit must not be null");
        }

        AbstractBattle.BattleForce ownForce = new AbstractBattle.BattleForce(
                MissionType.COMMANDO, 11, attackerUnit.getUnitType(),
                attackerUnit.getLeadershipBonus(), attackerUnit.getStrength(),
                true);
        AbstractBattle.BattleForce enemyForce = new AbstractBattle.BattleForce(
                MissionType.COMMANDO, 11, defenderUnit.getUnitType(),
                defenderUnit.getLeadershipBonus(), defenderUnit.getStrength(),
                true);

        AbstractBattle battle = new AbstractBattle(ownForce, enemyForce);
        boolean ownWon = battle.resolve();

        String winner;
        String loser;
        if (ownWon) {
            winner = attackerUnit.getName();
            loser = defenderUnit.getName();
        } else {
            winner = defenderUnit.getName();
            loser = attackerUnit.getName();
        }

        return new CombatResult(CombatType.GROUND_COMBAT, winner, loser,
                enemyForce.getCasualties(), ownForce.getCasualties(),
                ownWon ? "Attacker victory" : "Defender victory");
    }
}
