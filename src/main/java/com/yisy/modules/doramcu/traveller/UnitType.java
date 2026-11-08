package com.yisy.modules.doramcu.traveller;

/**
 * Military unit types from Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * Units are organized hierarchically from Fire Team (smallest) to
 * Brigade (largest commonly used). Each unit type has a typical
 * strength and a die roll value for the Unit Size Table.
 * </p>
 * <p>
 * The preservation level is 40% of a unit's effective strength —
 * the number of casualties at which the unit surrenders or withdraws.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum UnitType {

    /**
     * Fire Team — smallest organizational unit (4 men).
     * <p>
     * Commanded by a Corporal.
     * </p>
     */
    FIRE_TEAM("Fire Team", 4, 1, 40),

    /**
     * Squad — 2-3 fire teams (9 men).
     * <p>
     * Commanded by a Lance Sergeant or Sergeant.
     * </p>
     */
    SQUAD("Squad", 9, 3, 40),

    /**
     * Section — 2 squads (19 men).
     * <p>
     * Intermediate between squad and platoon; often omitted.
     * Commanded by a Sergeant or Gunnery Sergeant.
     * </p>
     */
    SECTION("Section", 19, 5, 40),

    /**
     * Platoon — up to 3 squads or 2 sections (41-63 men).
     * <p>
     * Commanded by a Lieutenant or Leading Sergeant.
     * </p>
     */
    PLATOON("Platoon", 52, 6, 40),

    /**
     * Company — several platoons (127 men).
     * <p>
     * Commanded by a Captain.
     * </p>
     */
    COMPANY("Company", 127, 7, 40),

    /**
     * Battalion — several companies (452 men).
     * <p>
     * Commanded by a field grade officer (Major to Lt Colonel).
     * </p>
     */
    BATTALION("Battalion", 452, 8, 40),

    /**
     * Reinforced Battalion — battalion with additional support (635 men).
     */
    REINFORCED_BN("Reinforced Battalion", 635, 9, 40),

    /**
     * Brigade — multiple battalions (1500 men).
     * <p>
     * Commanded by a Colonel or higher.
     * </p>
     */
    BRIGADE("Brigade", 1500, 10, 40);

    private final String name;
    private final int typicalStrength;
    private final int dieRoll;
    private final int preservationPercent;

    UnitType(String name, int typicalStrength, int dieRoll, int preservationPercent) {
        this.name = name;
        this.typicalStrength = typicalStrength;
        this.dieRoll = dieRoll;
        this.preservationPercent = preservationPercent;
    }

    /**
     * Returns the name of the unit type.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the typical number of combat effectives.
     *
     * @return the typical strength
     */
    public int getTypicalStrength() {
        return typicalStrength;
    }

    /**
     * Returns the die roll value on the Unit Size Table.
     *
     * @return the die roll
     */
    public int getDieRoll() {
        return dieRoll;
    }

    /**
     * Returns the preservation percentage (casualties before surrender).
     *
     * @return the preservation percentage
     */
    public int getPreservationPercent() {
        return preservationPercent;
    }

    /**
     * Returns the preservation level (number of casualties before surrender).
     *
     * @param strength the actual unit strength
     * @return the preservation level
     */
    public int getPreservationLevel(int strength) {
        return (int) (strength * preservationPercent / 100.0);
    }

    /**
     * Returns the number of levels this unit is above a Fire Team.
     * Used for "down N" element engaged calculations.
     *
     * @return the level above fire team
     */
    public int getLevelAboveFireTeam() {
        return ordinal();
    }

    /**
     * Returns the unit type N levels below this one.
     *
     * @param levelsDown the number of levels to go down
     * @return the unit type, or FIRE_TEAM if already at bottom
     */
    public UnitType downLevels(int levelsDown) {
        int index = Math.max(0, ordinal() - levelsDown);
        return values()[index];
    }

    /**
     * Returns the unit type for a given die roll on the Unit Size Table.
     *
     * @param roll the die roll (1-10+)
     * @return the unit type
     */
    public static UnitType fromDieRoll(int roll) {
        UnitType result = FIRE_TEAM;
        for (UnitType type : values()) {
            if (roll >= type.dieRoll) {
                result = type;
            }
        }
        return result;
    }
}
