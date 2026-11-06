package com.yisy.modules.doramcu.hex;

/**
 * Manages combat statistics for a ship.
 * <p>
 * This class handles all combat-related calculations including hull points, shield points,
 * damage output, speed, and evasion. It also applies home planet population stat bonuses
 * to effective values (Resolve for damage/hull, Intelligence for shields, Diligence for build time).
 * <p>
 * Damage absorption follows a shield-first model: shields absorb damage before hull takes any hit.
 * Repair operations restore hull and shield points independently.
 */
public class ShipCombatStats {

    private final Ship ship;

    /**
     * Creates combat stats management for the given ship.
     *
     * @param ship the ship this belongs to
     */
    public ShipCombatStats(Ship ship) {
        this.ship = ship;
    }

    // ---- Hull and Shield ----

    public int getMaxHullPoints() {
        int base = ship.getShipType().getBaseHullPoints();
        int hullBonus = ship.getComponents().stream().mapToInt(ShipComponent::getHullBonus).sum();
        return base + hullBonus;
    }

    public int getCurrentHullPoints() {
        return ship.getCurrentHullPoints();
    }

    public int getCurrentShieldPoints() {
        return ship.getCurrentShieldPoints();
    }

    public int getMaxShieldPoints() {
        return ship.getComponents().stream().mapToInt(ShipComponent::getShieldStrength).sum();
    }

    public boolean isDestroyed() {
        return ship.getCurrentHullPoints() <= 0;
    }

    public double getHullHealthRatio() {
        int max = getMaxHullPoints();
        if (max <= 0) return 0.0;
        return (double) Math.max(0, ship.getCurrentHullPoints()) / max;
    }

    public double getShieldHealthRatio() {
        int max = getMaxShieldPoints();
        if (max <= 0) return 0.0;
        return (double) Math.max(0, ship.getCurrentShieldPoints()) / max;
    }

    // ---- Combat calculations ----

    public int getTotalDamage() {
        return ship.getComponents().stream().mapToInt(ShipComponent::getDamage).sum();
    }

    public int getEffectiveDamage() {
        int base = getTotalDamage();
        double bonus = 0.0;

        // Home planet Resolve bonus
        Planet homePlanet = ship.getHomePlanet();
        if (homePlanet != null) {
            int resolve = homePlanet.getPopulationStat(PopulationAttribute.RESOLVE);
            bonus += (resolve - 5) * 0.05;
        }

        // AI Core damage bonus (target priority and firing solutions)
        AiCoreComponent aiCore = ship.getAiCore();
        if (aiCore != null) {
            bonus += aiCore.getDamageBonus();
        }

        return (int) Math.round(Math.max(0, base * (1.0 + bonus)));
    }

    public int getEffectiveShieldStrength() {
        int base = getMaxShieldPoints();
        double bonus = 0.0;

        // Home planet Intelligence bonus
        Planet homePlanet = ship.getHomePlanet();
        if (homePlanet != null) {
            int intelligence = homePlanet.getPopulationStat(PopulationAttribute.INTELLIGENCE);
            bonus += (intelligence - 5) * 0.05;
        }

        // AI Core shield bonus (shield modulation and deflection)
        AiCoreComponent aiCore = ship.getAiCore();
        if (aiCore != null) {
            bonus += aiCore.getShieldBonus();
        }

        return (int) Math.round(Math.max(0, base * (1.0 + bonus)));
    }

    public int getEffectiveHullPoints() {
        int base = getMaxHullPoints();
        Planet homePlanet = ship.getHomePlanet();
        if (homePlanet == null) {
            return base;
        }
        int resolve = homePlanet.getPopulationStat(PopulationAttribute.RESOLVE);
        double bonus = (resolve - 5) * 0.05;
        return (int) Math.round(Math.max(0, base * (1.0 + bonus)));
    }

    public int getMaxRange() {
        return ship.getComponents().stream()
                .filter(c -> c.getType() == ShipComponent.ComponentType.WEAPON && c.getWeaponType() != null)
                .mapToInt(c -> c.getWeaponType().getRange())
                .max()
                .orElse(0);
    }

    // ---- Speed and Evasion ----

    /**
     * Returns the MDrive (Maneuver Drive) base speed for this ship.
     * <p>
     * MDrive is used for movement within a solar system and during combat.
     * Includes bonuses from MDrive components.
     * </p>
     *
     * @return the MDrive base speed
     */
    public int getMDriveSpeed() {
        int base = ship.getShipType().getMDriveSpeed();
        int componentBonus = ship.getComponents().stream()
                .mapToInt(ShipComponent::getMDriveBonus)
                .sum();
        return base + componentBonus;
    }

    /**
     * Returns the JDrive (Jump Drive) base speed for this ship.
     * <p>
     * JDrive is used for faster-than-light travel between solar systems via hyperlanes.
     * Includes bonuses from JDrive components.
     * </p>
     *
     * @return the JDrive base speed
     */
    public int getJDriveSpeed() {
        int base = ship.getShipType().getJDriveSpeed();
        int componentBonus = ship.getComponents().stream()
                .mapToInt(ShipComponent::getJDriveBonus)
                .sum();
        return base + componentBonus;
    }

    /**
     * Returns the effective MDrive speed, reduced by damage.
     * <p>
     * Speed is reduced proportionally to hull damage, with a minimum of 1.
     * AI Core provides a speed bonus through engine optimization.
     * </p>
     *
     * @return the effective MDrive speed (minimum 1)
     */
    public int getEffectiveMDriveSpeed() {
        int base = getMDriveSpeed();
        double healthRatio = getHullHealthRatio();
        int reduction = (int) Math.floor((1.0 - healthRatio) * base * 0.5);
        int speed = Math.max(1, base - reduction);

        // AI Core speed bonus (engine optimization)
        AiCoreComponent aiCore = ship.getAiCore();
        if (aiCore != null) {
            speed = (int) Math.round(speed * (1.0 + aiCore.getSpeedBonus()));
        }

        return Math.max(1, speed);
    }

    /**
     * Returns the effective JDrive speed, reduced by damage.
     * <p>
     * JDrive speed is reduced if the ship is damaged, as jump drives
     * require intact systems to operate at full capacity.
     * </p>
     *
     * @return the effective JDrive speed (minimum 0 — damaged ships may not be able to jump)
     */
    public int getEffectiveJDriveSpeed() {
        int base = getJDriveSpeed();
        double healthRatio = getHullHealthRatio();

        // JDrive requires minimum 20% hull integrity to function
        if (healthRatio < 0.2) {
            return 0;
        }

        int reduction = (int) Math.floor((1.0 - healthRatio) * base * 0.5);
        int speed = Math.max(0, base - reduction);

        // AI Core speed bonus (navigation optimization)
        AiCoreComponent aiCore = ship.getAiCore();
        if (aiCore != null) {
            speed = (int) Math.round(speed * (1.0 + aiCore.getSpeedBonus()));
        }

        return speed;
    }

    /**
     * Returns the base speed for this ship (alias for MDrive speed).
     *
     * @return the MDrive base speed
     * @deprecated Use {@link #getMDriveSpeed()} instead
     */
    @Deprecated
    public int getBaseSpeed() {
        return getMDriveSpeed();
    }

    /**
     * Returns the effective speed for this ship (alias for effective MDrive speed).
     *
     * @return the effective MDrive speed
     * @deprecated Use {@link #getEffectiveMDriveSpeed()} instead
     */
    @Deprecated
    public int getEffectiveSpeed() {
        return getEffectiveMDriveSpeed();
    }

    public double getBaseEvasion() {
        return ship.getShipType().getBaseEvasion();
    }

    public double getEffectiveEvasion() {
        double base = getBaseEvasion();
        double evasion = base;

        if (ship.getCurrentShieldPoints() > 0) {
            evasion += base * 0.5;
        }

        double healthRatio = getHullHealthRatio();
        evasion -= base * (1.0 - healthRatio) * 0.1;

        // AI Core evasion bonus (evasive maneuvers and predictive dodging)
        AiCoreComponent aiCore = ship.getAiCore();
        if (aiCore != null) {
            evasion += aiCore.getEvasionBonus();
        }

        return Math.max(0.0, Math.min(1.0, evasion));
    }

    // ---- Damage and repair ----

    /**
     * Applies damage to this ship. Shields absorb damage first, then hull.
     *
     * @return the actual damage dealt
     */
    public int takeDamage(int damage) {
        if (isDestroyed() || damage <= 0) {
            return 0;
        }
        int remaining = damage;

        if (ship.getCurrentShieldPoints() > 0) {
            int shieldAbsorb = Math.min(ship.getCurrentShieldPoints(), remaining);
            ship.rechargeShields(-shieldAbsorb);
            remaining -= shieldAbsorb;
        }

        if (remaining > 0) {
            int hullDamage = Math.min(ship.getCurrentHullPoints(), remaining);
            ship.repairHull(-hullDamage);
            remaining -= hullDamage;
        }

        return damage - remaining;
    }

    /**
     * Repairs this ship's hull by the given amount.
     */
    public void repairHull(int amount) {
        ship.repairHull(amount);
    }

    /**
     * Recharges this ship's shields by the given amount.
     */
    public void rechargeShields(int amount) {
        ship.rechargeShields(amount);
    }

    /**
     * Fully repairs and recharges this ship.
     */
    public void fullRepair() {
        ship.fullRepair();
    }
}
