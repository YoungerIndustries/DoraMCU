package com.yisy.modules.doramcu.traveller;

import java.util.*;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Abstract battle resolution from Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * The abstract system resolves mercenary missions involving large numbers
 * of troops. It is conducted in two parts: preparation and resolution.
 * </p>
 * <p>
 * Preparation determines mission, tech level, size, and efficiency of
 * both opposing forces. Resolution is conducted in a series of combats,
 * each between part or all of the two opposing forces.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public class AbstractBattle {

    /**
     * Encounter type in abstract battles.
     */
    public enum EncounterType {
        /**
         * Own force surprised.
         */
        OWN_SURPRISED("Own Surprised", -3),
        /**
         * Own force attacking.
         */
        OWN_ATTACKING("Own Attacking", -2),
        /**
         * Own force attacking (stronger).
         */
        OWN_ATTACKING_STRONG("Own Attacking", -1),
        /**
         * Own force attacking (equal).
         */
        OWN_ATTACKING_EQUAL("Own Attacking", 0),
        /**
         * Own force attacking (favorable).
         */
        OWN_ATTACKING_FAVORABLE("Own Attacking", 1),
        /**
         * Firefight.
         */
        FIREFIGHT("Firefight", 2),
        /**
         * Firefight (slight).
         */
        FIREFIGHT_SLIGHT("Firefight", 3),
        /**
         * Firefight (moderate).
         */
        FIREFIGHT_MODERATE("Firefight", 4),
        /**
         * Firefight (heavy).
         */
        FIREFIGHT_HEAVY("Firefight", 5),
        /**
         * Enemy attacking.
         */
        ENEMY_ATTACKING("Enemy Attacking", 6),
        /**
         * Enemy attacking (stronger).
         */
        ENEMY_ATTACKING_STRONG("Enemy Attacking", 7),
        /**
         * Enemy attacking (strong).
         */
        ENEMY_ATTACKING_STRONGER("Enemy Attacking", 8),
        /**
         * Enemy attacking (very strong).
         */
        ENEMY_ATTACKING_VERY_STRONG("Enemy Attacking", 9),
        /**
         * Enemy surprised.
         */
        ENEMY_SURPRISED("Enemy Surprised", 10);

        private final String name;
        private final int dieValue;

        EncounterType(String name, int dieValue) {
            this.name = name;
            this.dieValue = dieValue;
        }

        public String getName() {
            return name;
        }

        public int getDieValue() {
            return dieValue;
        }
    }

    /**
     * Represents one side in an abstract battle.
     */
    public static class BattleForce {
        private final MissionType mission;
        private final int techLevel;
        private final UnitType unitType;
        private final int efficiency;
        private int strength;
        private int casualties;
        private final boolean isMercenary;

        public BattleForce(MissionType mission, int techLevel, UnitType unitType,
                           int efficiency, int strength, boolean isMercenary) {
            this.mission = mission;
            this.techLevel = techLevel;
            this.unitType = unitType;
            this.efficiency = efficiency;
            this.strength = strength;
            this.casualties = 0;
            this.isMercenary = isMercenary;
        }

        public MissionType getMission() { return mission; }
        public int getTechLevel() { return techLevel; }
        public UnitType getUnitType() { return unitType; }
        public int getEfficiency() { return efficiency; }
        public int getStrength() { return strength; }
        public int getCasualties() { return casualties; }
        public boolean isMercenary() { return isMercenary; }
        public int getPreservationLevel() { return unitType.getPreservationLevel(strength); }

        public void addCasualties(int number) {
            casualties += number;
            strength = Math.max(0, strength - number);
        }

        public boolean isDefeated() {
            return strength <= 0 || casualties >= getPreservationLevel();
        }
    }

    /**
     * Result of a single combat round.
     */
    public static class CombatResult {
        private final EncounterType encounterType;
        private final int ownCasualties;
        private final int enemyCasualties;
        private final UnitType ownElement;
        private final UnitType enemyElement;

        public CombatResult(EncounterType encounterType, int ownCasualties, int enemyCasualties,
                            UnitType ownElement, UnitType enemyElement) {
            this.encounterType = encounterType;
            this.ownCasualties = ownCasualties;
            this.enemyCasualties = enemyCasualties;
            this.ownElement = ownElement;
            this.enemyElement = enemyElement;
        }

        public EncounterType getEncounterType() { return encounterType; }
        public int getOwnCasualties() { return ownCasualties; }
        public int getEnemyCasualties() { return enemyCasualties; }
        public UnitType getOwnElement() { return ownElement; }
        public UnitType getEnemyElement() { return enemyElement; }
    }

    private final BattleForce ownForce;
    private final BattleForce enemyForce;
    private final List<CombatResult> combatResults;

    /**
     * Creates a new abstract battle.
     *
     * @param ownForce the player's force
     * @param enemyForce the enemy force
     */
    public AbstractBattle(BattleForce ownForce, BattleForce enemyForce) {
        this.ownForce = Objects.requireNonNull(ownForce, "Own force must not be null");
        this.enemyForce = Objects.requireNonNull(enemyForce, "Enemy force must not be null");
        this.combatResults = new ArrayList<>();
    }

    /**
     * Resolves the entire battle through a series of combats.
     *
     * @return true if the own force won
     */
    public boolean resolve() {
        while (!ownForce.isDefeated() && !enemyForce.isDefeated()) {
            CombatResult result = resolveCombat();
            combatResults.add(result);

            ownForce.addCasualties(result.ownCasualties);
            enemyForce.addCasualties(result.enemyCasualties);
        }
        return !ownForce.isDefeated();
    }

    /**
     * Resolves a single combat round.
     */
    private CombatResult resolveCombat() {
        // 1. Element engaged
        UnitType ownElement = determineElementEngaged(ownForce);
        UnitType enemyElement = determineElementEngaged(enemyForce);

        // 2. Encounter type
        EncounterType encounter = determineEncounterType();

        // 3. Combat resolution
        int ownCasualties = 0;
        int enemyCasualties = 0;

        boolean ownFires = encounter != EncounterType.OWN_SURPRISED;
        boolean enemyFires = encounter != EncounterType.ENEMY_SURPRISED;

        if (ownFires) {
            enemyCasualties = resolveFiring(ownElement, enemyElement, ownForce, enemyForce);
        }
        if (enemyFires) {
            ownCasualties = resolveFiring(enemyElement, ownElement, enemyForce, ownForce);
        }

        return new CombatResult(encounter, ownCasualties, enemyCasualties, ownElement, enemyElement);
    }

    private UnitType determineElementEngaged(BattleForce force) {
        int roll = DiceUtil.rollD6();
        // Commando/Security: +2, Indigenous: -2
        if (force.mission == MissionType.COMMANDO || force.mission == MissionType.SECURITY) {
            roll += 2;
        }
        if (!force.isMercenary) {
            roll -= 2;
        }

        // Element Engaged table
        if (roll >= 7) {
            return force.unitType; // full
        } else {
            int down = 7 - roll; // down 1 to down 5
            return force.unitType.downLevels(down);
        }
    }

    private EncounterType determineEncounterType() {
        int roll = DiceUtil.rollD6();

        // DMs
        if (ownForce.unitType.ordinal() < enemyForce.unitType.ordinal()) {
            roll += 1; // own element smaller
        } else if (ownForce.unitType.ordinal() > enemyForce.unitType.ordinal()) {
            roll -= 1; // enemy element smaller
        }

        int effDiff = ownForce.efficiency - enemyForce.efficiency;
        roll += effDiff;

        if (ownForce.mission == MissionType.SECURITY) roll += 1;
        if (enemyForce.mission == MissionType.SECURITY) roll -= 1;
        if (ownForce.mission == MissionType.STRIKER) roll -= 1;
        if (enemyForce.mission == MissionType.STRIKER) roll += 1;

        // Map to encounter type
        roll = Math.max(1, Math.min(12, roll));
        EncounterType[] types = EncounterType.values();
        int index = Math.min(roll - 1, types.length - 1);
        return types[index];
    }

    private int resolveFiring(UnitType firingUnit, UnitType targetUnit,
                              BattleForce firingForce, BattleForce targetForce) {
        // Firing DM from matrix
        int dm = firingUnit.ordinal() - targetUnit.ordinal();

        // Attacker DM: -2
        if (firingForce == ownForce) {
            // Check if own is attacking
            // (simplified: always apply -2 for attacker)
        }

        // Tech level difference
        dm += firingForce.techLevel - targetForce.techLevel;

        // Fire results
        int fireRoll = DiceUtil.rollD6() + dm;
        int casualtyPercent = getFireResultsPercent(fireRoll);

        // Calculate actual casualties from the engaged element
        int elementStrength = targetUnit.getTypicalStrength();
        int casualties = (int) (elementStrength * casualtyPercent / 100.0);
        if (casualtyPercent > 0 && casualties == 0) casualties = 1;

        return Math.max(0, casualties);
    }

    private int getFireResultsPercent(int roll) {
        switch (roll) {
            case -2: return 0;
            case -1: return 1;
            case 0: return 1;
            case 1: return 5;
            case 2: return 5;
            case 3: return 10;
            case 4: return 10;
            case 5: return 20;
            case 6: return 20;
            case 7: return 30;
            case 8: return 40;
            default: return 50;
        }
    }

    /**
     * Returns the list of combat results (immutable).
     *
     * @return the combat results
     */
    public List<CombatResult> getCombatResults() {
        return Collections.unmodifiableList(combatResults);
    }

    /**
     * Returns the own force.
     *
     * @return the own force
     */
    public BattleForce getOwnForce() {
        return ownForce;
    }

    /**
     * Returns the enemy force.
     *
     * @return the enemy force
     */
    public BattleForce getEnemyForce() {
        return enemyForce;
    }

    /**
     * Creates a random enemy force for a given mission type.
     *
     * @param mission the mission type
     * @return a random enemy force
     */
    public static BattleForce createRandomEnemyForce(MissionType mission) {
        // Tech level: indigenous +4, mercenary +6, cadre +5
        boolean isMercenary = DiceUtil.rollD6() >= 4; // 4-6 = mercenary
        int techDm = isMercenary ? 6 : 4;
        if (mission == MissionType.CADRE) techDm = 5;
        int techLevel = Math.max(1, Math.min(15, DiceUtil.rollD6() + techDm));

        // Size
        int sizeDm = mission.getSizeDm();
        if (!isMercenary) sizeDm += 1; // indigenous +1
        int sizeRoll = DiceUtil.rollD6() + sizeDm;
        UnitType unitType = UnitType.fromDieRoll(sizeRoll);

        // Efficiency
        int efficiency = DiceUtil.rollD6();
        if (isMercenary && mission != MissionType.CADRE) efficiency += 2;
        else if (mission == MissionType.CADRE) efficiency += 1;

        return new BattleForce(mission, techLevel, unitType, efficiency,
                unitType.getTypicalStrength(), isMercenary);
    }

}
