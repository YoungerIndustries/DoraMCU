package com.yisy.modules.doramcu.unified;

/**
 * Defines the type of Traveller character a player wishes to create.
 * <p>
 * The Traveller universe supports multiple levels of play, from high-level
 * empire management to full character creation with careers and adventures.
 * </p>
 */
public enum TravellerCharacterType {

    EMPIRE_LEADER(
        "Empire Leader",
        "Rule an entire galactic empire — set policy, manage sectors, and guide civilization across the stars",
        "Manage a full Imperium with thousands of worlds, dealing with politics, economics, and interstellar threats"
    ),
    SECTOR_GOVERNOR(
        "Sector Governor",
        "Govern a sector of 100 star systems — balance trade, defense, and colonial development",
        "Oversee a 10x10 subsector grid, managing systems, stations, and fleets within your jurisdiction"
    ),
    SUBSECTOR_CHIEF(
        "Subsector Chief",
        "Lead a subsector of 25 star systems — focus on local governance and system-level operations",
        "Administer a quadrant of a sector, handling world development, jump routes, and local conflicts"
    ),
    SYSTEM_MANAGER(
        "System Manager",
        "Manage a single star system — develop planets, stations, and local trade networks",
        "Control one system with its planets, moons, and stations, optimizing resources and defense"
    ),
    FULL_TRAVELLER(
        "Full Traveller Character",
        "Create a complete Traveller character with abilities, careers, skills, and adventures",
        "Play the full Traveller game as outlined in the Traveller books — roll abilities, serve careers, "
        + "earn skills, and embark on missions across the galaxy"
    );

    private final String displayName;
    private final String shortDescription;
    private final String fullDescription;

    TravellerCharacterType(String displayName, String shortDescription, String fullDescription) {
        this.displayName = displayName;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    /**
     * Returns true if this type involves full character creation with abilities and careers.
     */
    public boolean isFullCharacter() {
        return this == FULL_TRAVELLER;
    }

    /**
     * Returns true if this type is a management/administrative role.
     */
    public boolean isManagementRole() {
        return this != FULL_TRAVELLER;
    }
}
