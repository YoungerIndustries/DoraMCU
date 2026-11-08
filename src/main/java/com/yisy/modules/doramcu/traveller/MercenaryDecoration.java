package com.yisy.modules.doramcu.traveller;

/**
 * Military decorations and awards from Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * Decorations are awarded for meritorious conduct in combat assignments.
 * They contribute to experience points for morale calculations and affect
 * hiring DMs for mercenary service.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum MercenaryDecoration {

    /**
     * Meritorious Conduct Under Fire (MCUF).
     * <p>
     * Awarded when decoration roll meets the threshold.
     * </p>
     */
    MCUF("MCUF", "Meritorious Conduct Under Fire", 1),

    /**
     * Conspicuous Gallantry (MCG).
     * <p>
     * Awarded when decoration roll is at least 3 above the threshold.
     * </p>
     */
    MCG("MCG", "Conspicuous Gallantry", 2),

    /**
     * Starburst for Extreme Heroism (SEH).
     * <p>
     * Awarded when decoration roll is at least 6 above the threshold.
     * </p>
     */
    SEH("SEH", "Starburst for Extreme Heroism", 3),

    /**
     * Purple Heart.
     * <p>
     * Awarded when wounded or injured in a combat assignment.
     * </p>
     */
    PURPLE_HEART("Purple Heart", "Wounded in combat", 1),

    /**
     * Combat Service Ribbon (CSR).
     * <p>
     * Awarded each time a character serves in a combat assignment.
     * </p>
     */
    COMBAT_SERVICE_RIBBON("CSR", "Combat Service Ribbon", 1),

    /**
     * Combat Command Ribbon.
     * <p>
     * Awarded to officers who hold a command assignment in a combat unit.
     * </p>
     */
    COMBAT_COMMAND("Combat Command", "Combat Command Ribbon", 1);

    private final String abbreviation;
    private final String name;
    private final int experiencePoints;

    MercenaryDecoration(String abbreviation, String name, int experiencePoints) {
        this.abbreviation = abbreviation;
        this.name = name;
        this.experiencePoints = experiencePoints;
    }

    /**
     * Returns the abbreviation (e.g., "MCUF", "CSR").
     *
     * @return the abbreviation
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Returns the full name of the decoration.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the experience points this decoration contributes.
     *
     * @return the experience points
     */
    public int getExperiencePoints() {
        return experiencePoints;
    }
}
