package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Resolves a single combat maneuver between two ships in hex combat.
 * <p>
 * A combat maneuver represents one ship attacking another, considering:
 * </p>
 * <ul>
 *   <li>Weapon type and range</li>
 *   <li>Tactical posture of both ships</li>
 *   <li>Evasion chance based on MDrive and tactics</li>
 *   <li>Shield absorption before hull damage</li>
 *   <li>Random hit/miss resolution</li>
 * </ul>
 * <p>
 * The resolution process:
 * </p>
 * <ol>
 *   <li>Check if the weapon can reach the target (range check)</li>
 *   <li>Roll to hit, modified by tactics and MDrive</li>
 *   <li>If hit, calculate damage modified by attacker's tactic</li>
 *   <li>Apply damage to shields first, then hull</li>
 *   <li>Record the result with all details</li>
 * </ol>
 */
public class CombatManeuver {

    private static final Random random = new Random();

    /**
     * Base hit chance percentage (before modifiers).
     */
    private static final int BASE_HIT_CHANCE = 70;

    /**
     * Result of a single combat maneuver.
     */
    public static class ManeuverResult {
        private final Ship attacker;
        private final Ship defender;
        private final WeaponType weaponType;
        private final ShipTactic attackerTactic;
        private final ShipTactic defenderTactic;
        private final boolean hit;
        private final boolean evaded;
        private final int damageDealt;
        private final int shieldDamage;
        private final int hullDamage;
        private final int hitRoll;
        private final int hitThreshold;
        private final String description;

        ManeuverResult(Ship attacker, Ship defender, WeaponType weaponType,
                       ShipTactic attackerTactic, ShipTactic defenderTactic,
                       boolean hit, boolean evaded, int damageDealt,
                       int shieldDamage, int hullDamage,
                       int hitRoll, int hitThreshold, String description) {
            this.attacker = attacker;
            this.defender = defender;
            this.weaponType = weaponType;
            this.attackerTactic = attackerTactic;
            this.defenderTactic = defenderTactic;
            this.hit = hit;
            this.evaded = evaded;
            this.damageDealt = damageDealt;
            this.shieldDamage = shieldDamage;
            this.hullDamage = hullDamage;
            this.hitRoll = hitRoll;
            this.hitThreshold = hitThreshold;
            this.description = description;
        }

        /** @return the attacking ship */
        public Ship getAttacker() { return attacker; }

        /** @return the defending ship */
        public Ship getDefender() { return defender; }

        /** @return the weapon type used */
        public WeaponType getWeaponType() { return weaponType; }

        /** @return the attacker's tactic */
        public ShipTactic getAttackerTactic() { return attackerTactic; }

        /** @return the defender's tactic */
        public ShipTactic getDefenderTactic() { return defenderTactic; }

        /** @return true if the attack hit */
        public boolean isHit() { return hit; }

        /** @return true if the attack was evaded */
        public boolean isEvaded() { return evaded; }

        /** @return the total damage dealt */
        public int getDamageDealt() { return damageDealt; }

        /** @return the damage absorbed by shields */
        public int getShieldDamage() { return shieldDamage; }

        /** @return the damage that reached the hull */
        public int getHullDamage() { return hullDamage; }

        /** @return the hit roll value (1-100) */
        public int getHitRoll() { return hitRoll; }

        /** @return the hit threshold (roll <= threshold = hit) */
        public int getHitThreshold() { return hitThreshold; }

        /** @return a human-readable description */
        public String getDescription() { return description; }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * Resolves a combat maneuver between two ships.
     * <p>
     * The attacker fires at the defender, and the result is determined by:
     * </p>
     * <ul>
     *   <li>Hit chance based on weapon type and tactics</li>
     *   <li>Evasion chance based on defender's MDrive and tactic</li>
     *   <li>Damage calculation based on weapon and attacker's tactic</li>
     *   <li>Shield absorption before hull damage</li>
     * </ul>
     *
     * @param attacker the attacking ship
     * @param defender the defending ship
     * @param weaponType the weapon type used
     * @param attackerTactic the attacker's current tactic
     * @param defenderTactic the defender's current tactic
     * @return the result of the maneuver
     */
    public static ManeuverResult resolve(Ship attacker, Ship defender,
                                          WeaponType weaponType,
                                          ShipTactic attackerTactic,
                                          ShipTactic defenderTactic) {
        // Calculate hit chance
        int hitChance = calculateHitChance(weaponType, attackerTactic, defenderTactic, defender);

        // Roll to hit
        int hitRoll = random.nextInt(100) + 1;
        boolean hit = hitRoll <= hitChance;

        if (!hit) {
            String desc = String.format("%s's %s attack misses %s (roll %d > %d)",
                    attacker.getName(), weaponType, defender.getName(), hitRoll, hitChance);
            return new ManeuverResult(attacker, defender, weaponType, attackerTactic, defenderTactic,
                    false, false, 0, 0, 0, hitRoll, hitChance, desc);
        }

        // Check for evasion
        boolean evaded = checkEvasion(defender, defenderTactic);
        if (evaded) {
            String desc = String.format("%s evades %s's %s attack",
                    defender.getName(), attacker.getName(), weaponType);
            return new ManeuverResult(attacker, defender, weaponType, attackerTactic, defenderTactic,
                    true, true, 0, 0, 0, hitRoll, hitChance, desc);
        }

        // Calculate damage
        int baseDamage = weaponType.getBaseDamage();
        int effectiveDamage = attackerTactic.calculateEffectiveDamage(baseDamage);

        // Apply defender's incoming damage modifier
        effectiveDamage = defenderTactic.calculateEffectiveIncomingDamage(effectiveDamage);

        // Apply damage: shields first, then hull
        int currentShields = defender.getCurrentShieldPoints();
        int shieldDamage = Math.min(effectiveDamage, currentShields);
        int hullDamage = Math.max(0, effectiveDamage - shieldDamage);

        String desc;
        if (shieldDamage > 0 && hullDamage > 0) {
            desc = String.format("%s's %s hits %s: %d damage (%d shield, %d hull)",
                    attacker.getName(), weaponType, defender.getName(),
                    effectiveDamage, shieldDamage, hullDamage);
        } else if (shieldDamage > 0) {
            desc = String.format("%s's %s hits %s's shields: %d damage absorbed",
                    attacker.getName(), weaponType, defender.getName(), shieldDamage);
        } else {
            desc = String.format("%s's %s penetrates %s's shields: %d hull damage",
                    attacker.getName(), weaponType, defender.getName(), hullDamage);
        }

        return new ManeuverResult(attacker, defender, weaponType, attackerTactic, defenderTactic,
                true, false, effectiveDamage, shieldDamage, hullDamage,
                hitRoll, hitChance, desc);
    }

    /**
     * Resolves a full exchange between two ships (both fire).
     *
     * @param shipA the first ship
     * @param shipB the second ship
     * @param tacticA ship A's tactic
     * @param tacticB ship B's tactic
     * @return array of two results: [A attacks B, B attacks A]
     */
    public static ManeuverResult[] resolveExchange(Ship shipA, Ship shipB,
                                                    ShipTactic tacticA, ShipTactic tacticB) {
        // Determine primary weapon for each ship
        WeaponType weaponA = shipA.getComponents().stream()
                .filter(c -> c.getType() == ShipComponent.ComponentType.WEAPON)
                .map(ShipComponent::getWeaponType)
                .findFirst()
                .orElse(WeaponType.BEAM);

        WeaponType weaponB = shipB.getComponents().stream()
                .filter(c -> c.getType() == ShipComponent.ComponentType.WEAPON)
                .map(ShipComponent::getWeaponType)
                .findFirst()
                .orElse(WeaponType.BEAM);

        ManeuverResult resultA = resolve(shipA, shipB, weaponA, tacticA, tacticB);
        ManeuverResult resultB = resolve(shipB, shipA, weaponB, tacticB, tacticA);

        return new ManeuverResult[]{resultA, resultB};
    }

    /**
     * Calculates the hit chance for an attack.
     *
     * @param weaponType the weapon type
     * @param attackerTactic the attacker's tactic
     * @param defenderTactic the defender's tactic
     * @param defender the defending ship
     * @return the hit chance percentage (1-100)
     */
    private static int calculateHitChance(WeaponType weaponType,
                                           ShipTactic attackerTactic,
                                           ShipTactic defenderTactic,
                                           Ship defender) {
        int hitChance = BASE_HIT_CHANCE;

        // Weapon range modifier: longer range = slightly harder to hit
        hitChance -= (weaponType.getRange() - 1) * 5;

        // Attacker tactic modifier
        if (attackerTactic == ShipTactic.CONCENTRATED_FIRE) {
            hitChance += 10;
        } else if (attackerTactic == ShipTactic.EVASIVE) {
            hitChance -= 10;
        }

        // Defender tactic modifier
        if (defenderTactic == ShipTactic.EVASIVE) {
            hitChance -= 15;
        } else if (defenderTactic == ShipTactic.FLANKING) {
            hitChance -= 5;
        } else if (defenderTactic == ShipTactic.DECOY) {
            hitChance -= 10;
        }

        // Defender size modifier: larger ships are easier to hit
        int defenderSize = defender.getShipType().getBaseHullPoints() / 100;
        hitChance += Math.min(defenderSize, 10);

        // Clamp to 1-100
        return Math.max(1, Math.min(100, hitChance));
    }

    /**
     * Checks if the defender evades the attack.
     *
     * @param defender the defending ship
     * @param defenderTactic the defender's tactic
     * @return true if the attack was evaded
     */
    private static boolean checkEvasion(Ship defender, ShipTactic defenderTactic) {
        int evasionChance = 0;

        // Base evasion from MDrive
        int mDrive = defender.getComponents().stream()
                .filter(c -> c.getType() == ShipComponent.ComponentType.MDRIVE)
                .mapToInt(ShipComponent::getQuantity)
                .sum();
        evasionChance += mDrive * 3;

        // Tactic evasion bonus
        evasionChance += defenderTactic.getEvasionBonusPercent();

        // Evasive tactic bonus
        if (defenderTactic == ShipTactic.EVASIVE) {
            evasionChance += 10;
        }

        if (evasionChance <= 0) return false;

        int roll = random.nextInt(100) + 1;
        return roll <= evasionChance;
    }

    /**
     * Simulates a fleet-vs-fleet combat round with individual ship maneuvers.
     * <p>
     * Each ship in the attacker fleet targets a ship in the defender fleet,
     * and vice versa. Targets are selected by the highest damage threat.
     * </p>
     *
     * @param attackerFleet the attacking fleet
     * @param defenderFleet the defending fleet
     * @param attackerTactic the attacker fleet's tactic
     * @param defenderTactic the defender fleet's tactic
     * @return list of all maneuver results
     */
    public static List<ManeuverResult> resolveFleetRound(Fleet attackerFleet,
                                                          Fleet defenderFleet,
                                                          ShipTactic attackerTactic,
                                                          ShipTactic defenderTactic) {
        List<ManeuverResult> results = new ArrayList<>();

        List<Ship> attackers = attackerFleet.getActiveShips();
        List<Ship> defenders = defenderFleet.getActiveShips();

        if (attackers.isEmpty() || defenders.isEmpty()) {
            return results;
        }

        // Attacker fires at defender
        for (Ship attacker : attackers) {
            Ship target = selectTarget(defenders);
            WeaponType weapon = attacker.getComponents().stream()
                    .filter(c -> c.getType() == ShipComponent.ComponentType.WEAPON)
                    .map(ShipComponent::getWeaponType)
                    .findFirst()
                    .orElse(WeaponType.BEAM);

            results.add(resolve(attacker, target, weapon, attackerTactic, defenderTactic));
        }

        // Defender fires back at attacker
        for (Ship defender : defenders) {
            Ship target = selectTarget(attackers);
            WeaponType weapon = defender.getComponents().stream()
                    .filter(c -> c.getType() == ShipComponent.ComponentType.WEAPON)
                    .map(ShipComponent::getWeaponType)
                    .findFirst()
                    .orElse(WeaponType.BEAM);

            results.add(resolve(defender, target, weapon, defenderTactic, attackerTactic));
        }

        return results;
    }

    /**
     * Selects the highest-threat target from a list of ships.
     * <p>
     * The target with the highest total damage is considered the biggest threat.
     * </p>
     *
     * @param ships the list of potential targets
     * @return the highest-threat ship
     */
    private static Ship selectTarget(List<Ship> ships) {
        return ships.stream()
                .max((a, b) -> Integer.compare(
                        a.getComponents().stream().mapToInt(ShipComponent::getDamage).sum(),
                        b.getComponents().stream().mapToInt(ShipComponent::getDamage).sum()))
                .orElse(ships.get(0));
    }
}
