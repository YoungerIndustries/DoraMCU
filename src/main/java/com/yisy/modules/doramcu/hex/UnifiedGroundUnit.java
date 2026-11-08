package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

/**
 * A unified ground unit that combines DoraMCU's ArmyUnit with Traveller's MarineUnit.
 * <p>
 * This class merges DoraMCU's ground combat stats with Traveller's boarding
 * combat capabilities, allowing units to fight both on planets and aboard ships.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Ground combat: offense/defense from ArmyUnitType</li>
 *   <li>Boarding combat: combat rating, equipment, morale from MarineUnit</li>
 *   <li>Dual-mode: can fight on planets and aboard ships</li>
 *   <li>Experience and morale system</li>
 * </ul>
 */
public class UnifiedGroundUnit {

    private final int id;
    private final String name;
    private final ArmyUnitType unitType;
    private final MarineUnit.EquipmentLevel equipment;
    private final MarineUnit.Morale morale;
    private int currentStrength;
    private int maxStrength;
    private int experience;
    private int moraleValue;
    private Planet stationedPlanet;
    private Ship assignedShip;

    /**
     * Creates a new unified ground unit.
     *
     * @param id the unique identifier
     * @param name the unit name
     * @param unitType the army unit type
     * @param equipment the marine equipment level
     * @param morale the marine morale level
     */
    public UnifiedGroundUnit(int id, String name, ArmyUnitType unitType,
                              MarineUnit.EquipmentLevel equipment, MarineUnit.Morale morale) {
        this.id = id;
        this.name = name;
        this.unitType = unitType;
        this.equipment = equipment;
        this.morale = morale;
        this.maxStrength = 100;
        this.currentStrength = 100;
        this.experience = 0;
        this.moraleValue = 100;
    }

    /**
     * Creates a unified ground unit from an ArmyUnit.
     *
     * @param armyUnit the army unit to convert
     */
    public UnifiedGroundUnit(ArmyUnit armyUnit) {
        this.id = armyUnit.getId();
        this.name = armyUnit.getName();
        this.unitType = armyUnit.getUnitType();
        this.equipment = MarineUnit.EquipmentLevel.STANDARD;
        this.morale = MarineUnit.Morale.REGULAR;
        this.maxStrength = armyUnit.getMaxStrength();
        this.currentStrength = armyUnit.getCurrentStrength();
        this.experience = armyUnit.getExperience();
        this.moraleValue = armyUnit.getMorale();
    }

    /**
     * Creates a unified ground unit from a MarineUnit.
     *
     * @param marineUnit the marine unit to convert
     */
    public UnifiedGroundUnit(MarineUnit marineUnit) {
        this.id = 0;
        this.name = marineUnit.getName();
        this.unitType = ArmyUnitType.INFANTRY;
        this.equipment = marineUnit.getEquipment();
        this.morale = marineUnit.getMorale();
        this.maxStrength = marineUnit.getSize() * 10;
        this.currentStrength = marineUnit.getRemainingMarines() * 10;
        this.experience = 0;
        this.moraleValue = 100;
    }

    // ---- Accessors ----

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArmyUnitType getUnitType() {
        return unitType;
    }

    public MarineUnit.EquipmentLevel getEquipment() {
        return equipment;
    }

    public MarineUnit.Morale getMorale() {
        return morale;
    }

    public int getCurrentStrength() {
        return currentStrength;
    }

    public int getMaxStrength() {
        return maxStrength;
    }

    public int getExperience() {
        return experience;
    }

    public int getMoraleValue() {
        return moraleValue;
    }

    public Planet getStationedPlanet() {
        return stationedPlanet;
    }

    public void setStationedPlanet(Planet planet) {
        this.stationedPlanet = planet;
    }

    public Ship getAssignedShip() {
        return assignedShip;
    }

    public void setAssignedShip(Ship ship) {
        this.assignedShip = ship;
    }

    // ---- Ground Combat Stats ----

    /**
     * Returns the effective offense stat for ground combat.
     */
    public int getEffectiveOffense() {
        double strengthRatio = currentStrength / (double) maxStrength;
        double expBonus = 1.0 + (experience * 0.05);
        double moraleBonus = Math.max(0.5, moraleValue / 100.0);
        return (int) (unitType.getOffense() * strengthRatio * expBonus * moraleBonus);
    }

    /**
     * Returns the effective defense stat for ground combat.
     */
    public int getEffectiveDefense() {
        double strengthRatio = currentStrength / (double) maxStrength;
        double expBonus = 1.0 + (experience * 0.05);
        double moraleBonus = Math.max(0.5, moraleValue / 100.0);
        return (int) (unitType.getDefense() * strengthRatio * expBonus * moraleBonus);
    }

    // ---- Boarding Combat Stats ----

    /**
     * Returns the combat rating for boarding actions.
     */
    public int getCombatRating() {
        return unitType.getOffense() + equipment.getBonus();
    }

    /**
     * Returns the effective combat power for boarding actions.
     */
    public double getEffectiveCombatPower() {
        double strengthRatio = (double) currentStrength / maxStrength;
        return getCombatRating() * (1 + equipment.getBonus() * 0.1) * strengthRatio;
    }

    /**
     * Returns the breaching capability for boarding actions.
     */
    public int getBreachingCapability() {
        return Math.max(1, Math.min(6, unitType.getMobility() + equipment.getBonus()));
    }

    // ---- Actions ----

    /**
     * Damages this unit. Returns true if the unit is destroyed.
     */
    public boolean takeDamage(int damage) {
        currentStrength = Math.max(0, currentStrength - damage);
        if (currentStrength <= 0) {
            moraleValue = 0;
        }
        return currentStrength <= 0;
    }

    /**
     * Repairs this unit.
     */
    public void repair(int repair) {
        currentStrength = Math.min(maxStrength, currentStrength + repair);
    }

    /**
     * Gains experience from combat.
     */
    public void gainExperience(int exp) {
        experience += exp;
    }

    /**
     * Adjusts morale.
     */
    public void adjustMorale(int delta) {
        moraleValue = Math.max(0, Math.min(100, moraleValue + delta));
    }

    /**
     * Checks if this unit is combat-effective.
     */
    public boolean isCombatEffective() {
        return currentStrength > 0;
    }

    /**
     * Checks if this unit is at full strength.
     */
    public boolean isFullStrength() {
        return currentStrength == maxStrength;
    }

    /**
     * Returns the casualty percentage.
     */
    public double getCasualtyPercentage() {
        if (maxStrength <= 0) return 100.0;
        return (1.0 - (double) currentStrength / maxStrength) * 100.0;
    }

    /**
     * Performs a morale check.
     *
     * @param random the random number generator
     * @return true if the unit holds position
     */
    public boolean moraleCheck(java.util.Random random) {
        if (getCasualtyPercentage() < 50.0) return true;

        int roll = random.nextInt(12) + 2; // 2D6
        int threshold = 7 + morale.getBonus();
        return roll <= threshold;
    }

    /**
     * Returns the supply cost per turn.
     */
    public int getSupplyCost() {
        return unitType.getSupplyCost();
    }

    /**
     * Returns the mobility rating.
     */
    public int getMobility() {
        return unitType.getMobility();
    }

    @Override
    public String toString() {
        return String.format("UnifiedGroundUnit{id=%d, name='%s', type=%s, strength=%d/%d, " +
                        "equipment=%s, morale=%s, exp=%d}",
                id, name, unitType, currentStrength, maxStrength,
                equipment.getName(), morale.getName(), experience);
    }
}
