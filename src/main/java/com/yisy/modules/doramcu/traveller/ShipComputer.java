package com.yisy.modules.doramcu.traveller;

/**
 * Shipboard computer types from Classic Traveller Book 2.
 * <p>
 * Computers control navigation, gunnery, and ship operations.
 * Better computers improve accuracy, reduce crew requirements,
 * and enable complex tactical operations.
 * </p>
 * <p>
 * Tonnage cost: Computer class × 1% of total tonnage (minimum 1000 tons).
 * </p>
 */
public enum ShipComputer {

    /**
     * No computer — ship operates manually with severe penalties.
     */
    NONE(0, 0),

    /**
     * Class 0 — basic navigation computer.
     * Minimum for safe jump travel.
     * Tonnage: 1% of ship tonnage.
     */
    CLASS_0(0, 1),

    /**
     * Class 1 — standard navigation and gunnery.
     * +1 DM to gunnery rolls.
     * Tonnage: 2% of ship tonnage.
     */
    CLASS_1(1, 2),

    /**
     * Class 2 — advanced tactical computer.
     * +2 DM to gunnery rolls; enables missile targeting.
     * Tonnage: 3% of ship tonnage.
     */
    CLASS_2(2, 3),

    /**
     * Class 3 — superior tactical computer.
     * +3 DM to gunnery rolls; enables complex fleet tactics.
     * Tonnage: 4% of ship tonnage.
     */
    CLASS_3(3, 4),

    /**
     * Class 4 — maximum computer capability.
     * +4 DM to gunnery rolls; full fleet command capability.
     * Tonnage: 5% of ship tonnage.
     */
    CLASS_4(4, 5);

    private final int classLevel;
    private final int tonsPercent;

    ShipComputer(int classLevel, int tonsPercent) {
        this.classLevel = classLevel;
        this.tonsPercent = tonsPercent;
    }

    /**
     * Returns the computer class level (0–4).
     *
     * @return the class level
     */
    public int getClassLevel() {
        return classLevel;
    }

    /**
     * Returns the gunnery dice modifier provided by this computer.
     *
     * @return the dice modifier (+0 to +4)
     */
    public int getGunneryBonus() {
        return classLevel;
    }

    /**
     * Calculates the tons required for this computer on a ship of the given tonnage.
     *
     * @param totalTonnage the total ship tonnage in tons
     * @return tons consumed by the computer
     */
    public int getTonsRequired(int totalTonnage) {
        if (tonsPercent == 0) return 0;
        return Math.max(1000, (totalTonnage * tonsPercent) / 100);
    }

    /**
     * Returns whether this computer supports missile targeting.
     *
     * @return true if missile targeting is supported
     */
    public boolean supportsMissileTargeting() {
        return classLevel >= 2;
    }

    /**
     * Returns whether this computer supports fleet command operations.
     *
     * @return true if fleet command is supported
     */
    public boolean supportsFleetCommand() {
        return classLevel >= 3;
    }

    /**
     * Returns the minimum computer class required for safe jump travel.
     *
     * @return CLASS_0 (minimum for jumps)
     */
    public static ShipComputer minimumForJumpTravel() {
        return CLASS_0;
    }

    /**
     * Finds the computer with the given class level.
     *
     * @param level the class level (0–4)
     * @return the matching computer, or NONE if not found
     */
    public static ShipComputer fromClass(int level) {
        for (ShipComputer computer : values()) {
            if (computer.classLevel == level) {
                return computer;
            }
        }
        return NONE;
    }
}
