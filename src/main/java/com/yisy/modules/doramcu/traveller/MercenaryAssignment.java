package com.yisy.modules.doramcu.traveller;

/**
 * Unit assignments in Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * Each term of service consists of four one-year assignments.
 * Each assignment has thresholds for survival, decoration, promotion,
 * and skill acquisition that vary by arm of service.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum MercenaryAssignment {

    /**
     * Training — low-risk peacetime training.
     */
    TRAINING("Training", "Low-risk peacetime training", false),

    /**
     * Garrison — static defense duty.
     */
    GARRISON("Garrison", "Static defense duty", false),

    /**
     * Police Action — limited combat operations.
     */
    POLICE_ACTION("Police Action", "Limited combat operations", true),

    /**
     * Counter Insurgency — irregular warfare.
     */
    COUNTER_INSURGENCY("Counter Insurgency", "Irregular warfare", true),

    /**
     * Raid — offensive combat operations.
     */
    RAID("Raid", "Offensive combat operations", true),

    /**
     * Internal Security — domestic security operations.
     */
    INTERNAL_SECURITY("Internal Security", "Domestic security operations", false),

    /**
     * Ship's Troops — Marines only; shipboard duty.
     */
    SHIPS_TROOPS("Ship's Troops", "Shipboard duty (Marines only)", false);

    private final String name;
    private final String description;
    private final boolean isCombat;

    MercenaryAssignment(String name, String description, boolean isCombat) {
        this.name = name;
        this.description = description;
        this.isCombat = isCombat;
    }

    /**
     * Returns the name of the assignment.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the assignment.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns true if this assignment is classified as combat.
     * <p>
     * Wounds in combat assignments award the Purple Heart.
     * </p>
     *
     * @return true if combat assignment
     */
    public boolean isCombat() {
        return isCombat;
    }

    /**
     * Returns the survival threshold for this assignment by arm of service.
     * <p>
     * Returns Integer.MAX_VALUE for automatic survival.
     * </p>
     *
     * @param arm the arm of service
     * @return the survival threshold (roll >= this to survive)
     */
    public int getSurvivalThreshold(ArmOfService arm) {
        switch (this) {
            case TRAINING:
                return arm == ArmOfService.COMMANDO ? 3 : Integer.MAX_VALUE;
            case GARRISON:
                return arm == ArmOfService.COMMANDO ? Integer.MAX_VALUE : Integer.MAX_VALUE;
            case POLICE_ACTION:
                return arm == ArmOfService.COMMANDO ? 4 :
                        arm == ArmOfService.SUPPORT ? 4 : 5;
            case COUNTER_INSURGENCY:
                return arm == ArmOfService.COMMANDO ? 5 :
                        arm == ArmOfService.SUPPORT ? 5 : 5;
            case RAID:
                return arm == ArmOfService.COMMANDO ? 6 :
                        arm == ArmOfService.SUPPORT ? 6 : 6;
            case INTERNAL_SECURITY:
                return arm == ArmOfService.COMMANDO ? 4 :
                        arm == ArmOfService.SUPPORT ? 4 : 4;
            case SHIPS_TROOPS:
                return 4;
            default:
                return Integer.MAX_VALUE;
        }
    }

    /**
     * Returns the decoration threshold for this assignment by arm of service.
     * <p>
     * Returns Integer.MAX_VALUE if no decoration is possible.
     * </p>
     *
     * @param arm the arm of service
     * @return the decoration threshold (roll >= this for MCUF)
     */
    public int getDecorationThreshold(ArmOfService arm) {
        switch (this) {
            case TRAINING:
            case GARRISON:
                return Integer.MAX_VALUE;
            case POLICE_ACTION:
                return arm == ArmOfService.MARINE ? 8 :
                        arm == ArmOfService.SUPPORT ? 10 :
                        arm == ArmOfService.COMMANDO ? 9 : 9;
            case COUNTER_INSURGENCY:
                return arm == ArmOfService.MARINE ? 9 :
                        arm == ArmOfService.SUPPORT ? 11 :
                        arm == ArmOfService.COMMANDO ? 8 : 10;
            case RAID:
                return arm == ArmOfService.MARINE ? 8 :
                        arm == ArmOfService.SUPPORT ? 8 :
                        arm == ArmOfService.COMMANDO ? 5 : 6;
            case INTERNAL_SECURITY:
                return arm == ArmOfService.SUPPORT ? Integer.MAX_VALUE : 12;
            case SHIPS_TROOPS:
                return 12;
            default:
                return Integer.MAX_VALUE;
        }
    }

    /**
     * Returns the promotion threshold for this assignment by arm of service.
     * <p>
     * Returns Integer.MAX_VALUE if promotion is not allowed (commissioned officers only).
     * </p>
     *
     * @param arm the arm of service
     * @return the promotion threshold (roll >= this for promotion)
     */
    public int getPromotionThreshold(ArmOfService arm) {
        switch (this) {
            case TRAINING:
                return arm == ArmOfService.COMMANDO ? 8 : 6;
            case GARRISON:
                return arm == ArmOfService.COMMANDO ? 9 : 7;
            case POLICE_ACTION:
                return arm == ArmOfService.COMMANDO ? 8 :
                        arm == ArmOfService.SUPPORT ? 9 : 8;
            case COUNTER_INSURGENCY:
                return arm == ArmOfService.COMMANDO ? 7 :
                        arm == ArmOfService.SUPPORT ? 10 : 9;
            case RAID:
                return arm == ArmOfService.COMMANDO ? 6 :
                        arm == ArmOfService.SUPPORT ? 7 : 6;
            case INTERNAL_SECURITY:
                return arm == ArmOfService.COMMANDO ? 7 : 6;
            case SHIPS_TROOPS:
                return 6;
            default:
                return 6;
        }
    }

    /**
     * Returns the skill threshold for this assignment by arm of service.
     * <p>
     * Returns Integer.MAX_VALUE if no skill is gained.
     * </p>
     *
     * @param arm the arm of service
     * @return the skill threshold (roll >= this for a skill)
     */
    public int getSkillThreshold(ArmOfService arm) {
        switch (this) {
            case TRAINING:
                return arm == ArmOfService.COMMANDO ? 6 :
                        arm == ArmOfService.SUPPORT ? 8 : 7;
            case GARRISON:
                return Integer.MAX_VALUE;
            case POLICE_ACTION:
                return arm == ArmOfService.COMMANDO ? 7 :
                        arm == ArmOfService.SUPPORT ? 7 : 7;
            case COUNTER_INSURGENCY:
                return arm == ArmOfService.COMMANDO ? 6 :
                        arm == ArmOfService.SUPPORT ? 7 : 8;
            case RAID:
                return arm == ArmOfService.COMMANDO ? 5 :
                        arm == ArmOfService.SUPPORT ? 6 : 5;
            case INTERNAL_SECURITY:
                return Integer.MAX_VALUE;
            case SHIPS_TROOPS:
                return 6;
            default:
                return Integer.MAX_VALUE;
        }
    }
}
