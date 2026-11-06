package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a single ground army unit stationed on a planet.
 * <p>
 * Army units are the building blocks of ground forces, each with a specific
 * type (infantry, armor, artillery, etc.), current strength, and experience level.
 * </p>
 */
public class ArmyUnit {

    private final int id;
    private final ArmyUnitType unitType;
    private String name;
    private int currentStrength;
    private int maxStrength;
    private int experience;
    private int morale;
    private Planet stationedPlanet;

    /**
     * Creates a new army unit of the given type.
     *
     * @param id the unique identifier for this unit
     * @param unitType the type of army unit
     */
    public ArmyUnit(int id, ArmyUnitType unitType) {
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type must not be null");
        }
        this.id = id;
        this.unitType = unitType;
        this.name = unitType.getDisplayName() + "-" + id;
        this.maxStrength = 100;
        this.currentStrength = 100;
        this.experience = 0;
        this.morale = 100;
    }

    // ---- Accessors ----

    /** Returns the unique identifier. */
    public int getId() {
        return id;
    }

    /** Returns the unit type. */
    public ArmyUnitType getUnitType() {
        return unitType;
    }

    /** Returns the display name. */
    public String getName() {
        return name;
    }

    /** Sets the display name. */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Unit name must not be null or blank");
        }
        this.name = name;
    }

    /** Returns the current strength (0-100). */
    public int getCurrentStrength() {
        return currentStrength;
    }

    /** Returns the maximum strength. */
    public int getMaxStrength() {
        return maxStrength;
    }

    /** Returns the experience level. */
    public int getExperience() {
        return experience;
    }

    /** Returns the morale (0-100). */
    public int getMorale() {
        return morale;
    }

    /** Returns the stationed planet, or null if in transit. */
    public Planet getStationedPlanet() {
        return stationedPlanet;
    }

    /** Sets the stationed planet. */
    public void setStationedPlanet(Planet planet) {
        this.stationedPlanet = planet;
    }

    // ---- Combat Stats ----

    /**
     * Returns the effective offense stat, modified by strength and experience.
     */
    public int getEffectiveOffense() {
        double strengthRatio = currentStrength / (double) maxStrength;
        double expBonus = 1.0 + (experience * 0.05);
        double moraleBonus = Math.max(0.5, morale / 100.0);
        return (int) (unitType.getOffense() * strengthRatio * expBonus * moraleBonus);
    }

    /**
     * Returns the effective defense stat, modified by strength and experience.
     */
    public int getEffectiveDefense() {
        double strengthRatio = currentStrength / (double) maxStrength;
        double expBonus = 1.0 + (experience * 0.05);
        double moraleBonus = Math.max(0.5, morale / 100.0);
        return (int) (unitType.getDefense() * strengthRatio * expBonus * moraleBonus);
    }

    // ---- Actions ----

    /**
     * Damages this unit. Returns true if the unit is destroyed.
     *
     * @param damage the amount of damage to apply
     * @return true if the unit was destroyed
     */
    public boolean takeDamage(int damage) {
        currentStrength = Math.max(0, currentStrength - damage);
        if (currentStrength <= 0) {
            morale = 0;
        }
        return currentStrength <= 0;
    }

    /**
     * Repairs this unit by the given amount.
     *
     * @param repair the amount to repair
     */
    public void repair(int repair) {
        currentStrength = Math.min(maxStrength, currentStrength + repair);
    }

    /**
     * Gains experience from combat.
     *
     * @param exp the experience points to gain
     */
    public void gainExperience(int exp) {
        experience += exp;
    }

    /**
     * Adjusts morale.
     *
     * @param delta the morale change (positive or negative)
     */
    public void adjustMorale(int delta) {
        morale = Math.max(0, Math.min(100, morale + delta));
    }

    /**
     * Returns the per-turn supply cost for this unit.
     */
    public int getSupplyCost() {
        return unitType.getSupplyCost();
    }

    /**
     * Returns the mobility rating for this unit.
     */
    public int getMobility() {
        return unitType.getMobility();
    }

    @Override
    public String toString() {
        return String.format("ArmyUnit{id=%d, type=%s, name='%s', strength=%d/%d, exp=%d, morale=%d}",
                id, unitType, name, currentStrength, maxStrength, experience, morale);
    }
}
