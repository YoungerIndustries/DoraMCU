package com.yisy.modules.doramcu.hex;

import java.util.List;

/**
 * Combat statistics and damage/repair logic for a space station.
 * <p>
 * Extracted from SpaceStation.java to separate combat calculations from
 * station identity and management logic.
 * </p>
 * <p>
 * Based on 4X game patterns from Stellaris and Galactic Civilizations IV,
 * where stations have independent combat stats from their economic functions.
 * </p>
 */
public class SpaceStationCombatStats {

    private final SpaceStation station;

    /**
     * Creates combat stats for the given station.
     *
     * @param station the space station
     */
    public SpaceStationCombatStats(SpaceStation station) {
        if (station == null) {
            throw new IllegalArgumentException("Station must not be null");
        }
        this.station = station;
    }

    // ---- Combat Capabilities ----

    /**
     * Returns the total damage output from all weapon modules.
     *
     * @return the total damage value
     */
    public int getTotalDamage() {
        return station.getModules().stream()
                .filter(m -> m.getType() == StationModule.ModuleType.WEAPON)
                .mapToInt(StationModule::getDamage)
                .sum();
    }

    /**
     * Returns the effective damage output modified by hull health.
     * <p>
     * Damage is reduced proportionally to hull damage: at 50% hull, damage is 50%.
     * </p>
     *
     * @return the effective damage value
     */
    public int getEffectiveDamage() {
        int base = getTotalDamage();
        double healthRatio = station.getHullHealthRatio();
        return (int) Math.round(base * healthRatio);
    }

    /**
     * Returns the total shield strength from all shield modules.
     *
     * @return the total shield value
     */
    public int getTotalShield() {
        return station.getModules().stream()
                .filter(m -> m.getType() == StationModule.ModuleType.SHIELD)
                .mapToInt(StationModule::getShieldStrength)
                .sum();
    }

    /**
     * Returns the effective shield strength.
     * <p>
     * Base shield from station type, plus bonus from shield modules.
     * </p>
     *
     * @return the effective shield value
     */
    public int getEffectiveShield() {
        return station.getShieldPoints() + getTotalShield();
    }

    /**
     * Returns the total point defense value from all point defense modules.
     * <p>
     * Point defense reduces incoming missile damage.
     * </p>
     *
     * @return the total point defense value (0.0 to 1.0)
     */
    public double getTotalPointDefense() {
        return station.getModules().stream()
                .filter(m -> m.getType() == StationModule.ModuleType.POINT_DEFENSE)
                .mapToDouble(StationModule::getPointDefenseValue)
                .sum();
    }

    /**
     * Returns the total accuracy bonus from all targeting modules.
     *
     * @return the total accuracy bonus (0.0 to 1.0)
     */
    public double getTotalAccuracyBonus() {
        return station.getModules().stream()
                .filter(m -> m.getType() == StationModule.ModuleType.TARGETING)
                .mapToDouble(StationModule::getAccuracyBonus)
                .sum();
    }

    /**
     * Returns the total hull reinforcement from all hull modules.
     *
     * @return the total bonus hull points
     */
    public int getTotalHullBonus() {
        return station.getModules().stream()
                .filter(m -> m.getType() == StationModule.ModuleType.HULL_REINFORCEMENT)
                .mapToInt(StationModule::getHullBonus)
                .sum();
    }

    /**
     * Returns the effective hull points including reinforcement modules.
     *
     * @return the effective hull points
     */
    public int getEffectiveHull() {
        return station.getHullPoints() + getTotalHullBonus();
    }

    // ---- Damage and Repair ----

    /**
     * Takes damage, absorbing with shields first, then hull.
     *
     * @param damage the amount of damage to take
     */
    public void takeDamage(int damage) {
        if (damage <= 0) {
            return;
        }
        int remaining = damage;

        // Shields absorb first
        if (station.getCurrentShieldPoints() > 0) {
            int shieldAbsorb = Math.min(station.getCurrentShieldPoints(), remaining);
            station.repairShield(-shieldAbsorb); // negative amount reduces shields
            remaining -= shieldAbsorb;
        }

        // Hull absorbs remainder
        if (remaining > 0) {
            station.repairHull(-remaining); // negative amount reduces hull
        }
    }

    /**
     * Repairs shields by the given amount.
     *
     * @param amount the amount to repair (positive to repair, negative to damage)
     */
    public void repairShield(int amount) {
        station.repairShield(amount);
    }

    /**
     * Repairs hull by the given amount.
     *
     * @param amount the amount to repair (positive to repair, negative to damage)
     */
    public void repairHull(int amount) {
        station.repairHull(amount);
    }

    /**
     * Recharges shields by the configured rate.
     * <p>
     * Called each turn to gradually restore shields.
     * </p>
     */
    public void rechargeShield() {
        station.rechargeShield();
    }

    /**
     * Repairs hull by the configured rate.
     * <p>
     * Called each turn to gradually restore hull.
     * </p>
     */
    public void repairHull() {
        station.repairHull();
    }

    /**
     * Fully repairs both shields and hull.
     */
    public void fullRepair() {
        station.fullRepair();
    }
}
