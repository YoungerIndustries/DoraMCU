package com.yisy.modules.doramcu.traveller;

/**
 * Naval ship types from Classic Traveller Book 5: High Guard.
 * <p>
 * The Imperial Navy classifies ships by role and size. Each type has a typical
 * tonnage range, crew complement, and armament profile.
 * </p>
 * <p>
 * Ship types determine the minimum rank required to command and the typical
 * role within a fleet formation.
 * </p>
 */
public enum NavalShipType {

    /**
     * Corvette — small fast ship for patrol and reconnaissance.
     * Tonnage: 1,000–4,999 tons. Crew: 20–50.
     * Armament: 1–2 small lasers.
     */
    CORVETTE("Corvette", 1000, 4999, 20, 50, 1, 2, "Patrol/Reconnaissance"),

    /**
     * Frigate — light warship for escort and patrol duties.
     * Tonnage: 5,000–9,999 tons. Crew: 50–100.
     * Armament: 2–4 medium lasers.
     */
    FRIGATE("Frigate", 5000, 9999, 50, 100, 2, 4, "Escort/Patrol"),

    /**
     * Destroyer — medium warship, backbone of the Imperial Navy.
     * Tonnage: 10,000–19,999 tons. Crew: 100–200.
     * Armament: 4–6 medium lasers, missile launchers.
     */
    DESTROYER("Destroyer", 10000, 19999, 100, 200, 4, 6, "General Purpose"),

    /**
     * Light Cruiser — larger warship with heavy armament.
     * Tonnage: 20,000–39,999 tons. Crew: 200–400.
     * Armament: 6–8 large lasers, missile launchers.
     */
    LIGHT_CRUISER("Light Cruiser", 20000, 39999, 200, 400, 6, 8, "Combat/Strike"),

    /**
     * Heavy Cruiser — capital ship with maximum armament.
     * Tonnage: 40,000–79,999 tons. Crew: 400–800.
     * Armament: 8–12 large lasers, heavy missile launchers.
     */
    HEAVY_CRUISER("Heavy Cruiser", 40000, 79999, 400, 800, 8, 12, "Capital Ship"),

    /**
     * Battleship — largest combat vessel, fleet anchor.
     * Tonnage: 80,000–159,999 tons. Crew: 800–1500.
     * Armament: 12–16 large lasers, heavy missile launchers.
     */
    BATTLESHIP("Battleship", 80000, 159999, 800, 1500, 12, 16, "Fleet Anchor"),

    /**
     * Battlecruiser — fast capital ship, sacrifices armor for speed.
     * Tonnage: 80,000–159,999 tons. Crew: 800–1500.
     * Armament: 10–14 large lasers, heavy missile launchers, high M-rating.
     */
    BATTLECRUISER("Battlecruiser", 80000, 159999, 800, 1500, 10, 14, "Fast Capital Ship"),

    /**
     * Carrier — aircraft transport and command ship.
     * Tonnage: 40,000–159,999 tons. Crew: 500–1000 + air wing.
     * Armament: 4–6 medium lasers, carries fighters and gunships.
     */
    CARRIER("Carrier", 40000, 159999, 500, 1000, 4, 6, "Aircraft Carrier"),

    /**
     * Support Ship — logistics and repair vessel.
     * Tonnage: 20,000–79,999 tons. Crew: 200–500.
     * Armament: 2–4 small lasers, extensive repair facilities.
     */
    SUPPORT("Support Ship", 20000, 79999, 200, 500, 2, 4, "Logistics/Repair"),

    /**
     * Mothership — massive transport or colony ship.
     * Tonnage: 160,000+ tons. Crew: 1000+.
     * Armament: Variable, primarily transport capacity.
     */
    MOTHERSHIP("Mothership", 160000, 500000, 1000, 5000, 0, 8, "Transport/Colony");

    private final String name;
    private final int minTonnage;
    private final int maxTonnage;
    private final int minCrew;
    private final int maxCrew;
    private final int minLasers;
    private final int maxLasers;
    private final String role;

    NavalShipType(String name, int minTonnage, int maxTonnage, int minCrew, int maxCrew,
                  int minLasers, int maxLasers, String role) {
        this.name = name;
        this.minTonnage = minTonnage;
        this.maxTonnage = maxTonnage;
        this.minCrew = minCrew;
        this.maxCrew = maxCrew;
        this.minLasers = minLasers;
        this.maxLasers = maxLasers;
        this.role = role;
    }

    /**
     * Returns the ship type name.
     *
     * @return the name string
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the minimum typical tonnage for this ship type.
     *
     * @return minimum tonnage
     */
    public int getMinTonnage() {
        return minTonnage;
    }

    /**
     * Returns the maximum typical tonnage for this ship type.
     *
     * @return maximum tonnage
     */
    public int getMaxTonnage() {
        return maxTonnage;
    }

    /**
     * Returns the minimum typical crew complement.
     *
     * @return minimum crew
     */
    public int getMinCrew() {
        return minCrew;
    }

    /**
     * Returns the maximum typical crew complement.
     *
     * @return maximum crew
     */
    public int getMaxCrew() {
        return maxCrew;
    }

    /**
     * Returns the minimum typical number of laser banks.
     *
     * @return minimum lasers
     */
    public int getMinLasers() {
        return minLasers;
    }

    /**
     * Returns the maximum typical number of laser banks.
     *
     * @return maximum lasers
     */
    public int getMaxLasers() {
        return maxLasers;
    }

    /**
     * Returns the primary role description.
     *
     * @return the role string
     */
    public String getRole() {
        return role;
    }

    /**
     * Checks if the given tonnage falls within the typical range for this ship type.
     *
     * @param tonnage the ship's tonnage
     * @return true if within range
     */
    public boolean isTypicalTonnage(int tonnage) {
        return tonnage >= minTonnage && tonnage <= maxTonnage;
    }

    /**
     * Returns the typical tonnage (midpoint of range) for this ship type.
     *
     * @return typical tonnage
     */
    public int getTypicalTonnage() {
        return (minTonnage + maxTonnage) / 2;
    }

    /**
     * Returns the minimum rank required to command this ship type.
     *
     * @return the minimum commanding rank
     */
    public NavalRank getMinimumCommandRank() {
        if (maxTonnage <= 9999) {
            return NavalRank.COMMANDER;
        } else if (maxTonnage <= 39999) {
            return NavalRank.CAPTAIN;
        } else {
            return NavalRank.COMMANDER;
        }
    }

    /**
     * Finds the ship type for the given tonnage.
     *
     * @param tonnage the ship's tonnage
     * @return the matching ship type, or null if no match
     */
    public static NavalShipType fromTonnage(int tonnage) {
        for (NavalShipType type : values()) {
            if (type.isTypicalTonnage(tonnage)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Finds the ship type with the given name (case-insensitive).
     *
     * @param name the ship type name
     * @return the matching type, or null if not found
     */
    public static NavalShipType fromName(String name) {
        if (name == null) {
            return null;
        }
        for (NavalShipType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
