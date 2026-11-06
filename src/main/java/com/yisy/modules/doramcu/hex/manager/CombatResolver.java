package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Battle;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;

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
}
