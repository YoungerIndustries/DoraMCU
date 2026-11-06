package com.yisy.modules.doramcu.hex;

/**
 * Represents a single ground facility on a planet.
 * <p>
 * Facilities are the foundational structures of an empire's infrastructure:
 * shipyards, mining operations, power plants, military bases, research labs,
 * and more. Each facility provides specific bonuses and has an upgrade level.
 * </p>
 */
public class PlanetFacility {

    private final int id;
    private final StartingFacility facilityType;
    private int level;
    private boolean operational;

    /**
     * Creates a new facility of the given type.
     *
     * @param id the unique identifier for this facility
     * @param facilityType the type of facility
     */
    public PlanetFacility(int id, StartingFacility facilityType) {
        if (facilityType == null) {
            throw new IllegalArgumentException("Facility type must not be null");
        }
        this.id = id;
        this.facilityType = facilityType;
        this.level = 1;
        this.operational = true;
    }

    // ---- Accessors ----

    /** Returns the unique identifier. */
    public int getId() {
        return id;
    }

    /** Returns the facility type. */
    public StartingFacility getFacilityType() {
        return facilityType;
    }

    /** Returns the current upgrade level. */
    public int getLevel() {
        return level;
    }

    /** Returns whether the facility is operational. */
    public boolean isOperational() {
        return operational;
    }

    // ---- Actions ----

    /**
     * Upgrades the facility by one level.
     *
     * @param newLevel the new level
     */
    public void upgrade(int newLevel) {
        if (newLevel < 1) {
            throw new IllegalArgumentException("Facility level must be at least 1");
        }
        this.level = newLevel;
    }

    /**
     * Takes the facility offline.
     */
    public void disable() {
        this.operational = false;
    }

    /**
     * Brings the facility back online.
     */
    public void enable() {
        this.operational = true;
    }

    // ---- Bonuses ----

    /**
     * Returns the effective mineral bonus, scaled by level.
     */
    public int getEffectiveMineralBonus() {
        return operational ? facilityType.getMineralBonus() * level : 0;
    }

    /**
     * Returns the effective energy bonus, scaled by level.
     */
    public int getEffectiveEnergyBonus() {
        return operational ? facilityType.getEnergyBonus() * level : 0;
    }

    /**
     * Returns the effective food bonus, scaled by level.
     */
    public int getEffectiveFoodBonus() {
        return operational ? facilityType.getFoodBonus() * level : 0;
    }

    /**
     * Returns the effective research bonus, scaled by level.
     */
    public int getEffectiveResearchBonus() {
        return operational ? facilityType.getResearchBonus() * level : 0;
    }

    /**
     * Returns the effective defense bonus, scaled by level.
     */
    public int getEffectiveDefenseBonus() {
        return operational ? facilityType.getDefenseBonus() * level : 0;
    }

    /**
     * Returns the credit income generated per turn, scaled by level.
     */
    public int getEffectiveCreditIncome() {
        return operational ? facilityType.getCreditIncome() * level : 0;
    }

    /**
     * Returns the upkeep cost per turn, scaled by level.
     */
    public int getEffectiveUpkeepCost() {
        return operational ? facilityType.getUpkeepCost() * level : 0;
    }

    @Override
    public String toString() {
        return String.format("PlanetFacility{id=%d, type=%s, level=%d, operational=%s}",
                id, facilityType, level, operational);
    }
}
