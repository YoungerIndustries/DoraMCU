package com.yisy.modules.doramcu.traveller;

/**
 * Space station types from Classic Traveller Book 2.
 * <p>
 * In Classic Traveller, space stations are classified by their size and
 * capabilities, ranging from small outposts to massive orbital facilities.
 * The station type determines the maximum number of facilities, docking
 * capacity, and service quality.
 * </p>
 * <p>
 * Station types correspond to the starport code in the UWP:
 * </p>
 * <ul>
 *   <li>Alpha (A) — Massive orbital complex, unlimited capacity</li>
 *   <li>Bravo (B) — Large orbital station, high capacity</li>
 *   <li>Charlie (C) — Medium orbital station</li>
 *   <li>Delta (D) — Small orbital station</li>
 *   <li>Echo (E) — Space dock, limited facilities</li>
 *   <li>Outpost — Minimal landing pad or buoy</li>
 * </ul>
 */
public enum StationType {

    /**
     * Massive orbital complex — the pinnacle of space station design.
     * <p>
     * Alpha stations can accommodate any number of ships simultaneously,
     * offer all possible facilities, and provide the best service rates.
     * Typically found on capital worlds and major trade hubs.
     * </p>
     */
    ALPHA("Alpha", Integer.MAX_VALUE, 10, 0.8, 0.8),

    /**
     * Large orbital station — a major spaceport with extensive facilities.
     * <p>
     * Bravo stations can handle large ships and offer most facilities
     * at good rates. Common on important trade worlds.
     * </p>
     */
    BRAVO("Bravo", 20, 8, 0.9, 0.9),

    /**
     * Medium orbital station — a standard spaceport.
     * <p>
     * Charlie stations provide adequate facilities for most ships
     * at moderate rates. The most common station type.
     * </p>
     */
    CHARLIE("Charlie", 10, 5, 1.0, 1.0),

    /**
     * Small orbital station — a basic spaceport.
     * <p>
     * Delta stations offer limited facilities at higher rates.
     * Found on frontier worlds and minor trade stops.
     * </p>
     */
    DELTA("Delta", 5, 3, 1.2, 1.3),

    /**
     * Space dock — minimal orbital facilities.
     * <p>
     * Echo stations provide only basic docking and refueling.
     * Service rates are high due to limited competition.
     * </p>
     */
    ECHO("Echo", 2, 1, 1.5, 1.5),

    /**
     * Outpost — a landing pad or orbital buoy.
     * <p>
     * Outposts provide no facilities beyond a place to park.
     * Ships must be self-sufficient.
     * </p>
     */
    OUTPOST("Outpost", 1, 0, 2.0, 2.0);

    private final String name;
    private final int maxShips;
    private final int maxFacilities;
    private final double serviceRateMultiplier;
    private final double repairRateMultiplier;

    StationType(String name, int maxShips, int maxFacilities,
                double serviceRateMultiplier, double repairRateMultiplier) {
        this.name = name;
        this.maxShips = maxShips;
        this.maxFacilities = maxFacilities;
        this.serviceRateMultiplier = serviceRateMultiplier;
        this.repairRateMultiplier = repairRateMultiplier;
    }

    /**
     * Returns the human-readable name of this station type.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the maximum number of ships that can dock simultaneously.
     *
     * @return maximum ships (Integer.MAX_VALUE for unlimited)
     */
    public int getMaxShips() {
        return maxShips;
    }

    /**
     * Returns the maximum number of facilities this station type can support.
     *
     * @return maximum facilities
     */
    public int getMaxFacilities() {
        return maxFacilities;
    }

    /**
     * Returns the service rate multiplier for refueling and other services.
     * <p>
     * Lower values mean cheaper services. Alpha stations have the best rates (0.8×),
     * while outposts have the worst (2.0×).
     * </p>
     *
     * @return service rate multiplier
     */
    public double getServiceRateMultiplier() {
        return serviceRateMultiplier;
    }

    /**
     * Returns the repair rate multiplier for hull and system repairs.
     * <p>
     * Lower values mean cheaper repairs. Alpha stations have the best rates (0.8×),
     * while outposts have the worst (2.0×).
     * </p>
     *
     * @return repair rate multiplier
     */
    public double getRepairRateMultiplier() {
        return repairRateMultiplier;
    }

    /**
     * Checks if this station type can accommodate the given number of ships.
     *
     * @param ships the number of ships
     * @return true if the station can accommodate the ships
     */
    public boolean canAccommodate(int ships) {
        return ships <= maxShips;
    }

    /**
     * Derives the station type from a UWP starport code.
     * <p>
     * UWP starport codes map to station types as follows:
     * </p>
     * <ul>
     *   <li>A (0) → Alpha</li>
     *   <li>B (1) → Bravo</li>
     *   <li>C (2) → Charlie</li>
     *   <li>D (3) → Delta</li>
     *   <li>E (4) → Echo</li>
     *   <li>F (5) → Echo (with penalties)</li>
     *   <li>GG (6) → Outpost</li>
     *   <li>None (9) → No station</li>
     * </ul>
     *
     * @param starportCode the UWP starport code (0-9)
     * @return the corresponding station type, or null if no station
     */
    public static StationType fromStarportCode(int starportCode) {
        switch (starportCode) {
            case 0: return ALPHA;
            case 1: return BRAVO;
            case 2: return CHARLIE;
            case 3: return DELTA;
            case 4: return ECHO;
            case 5: return ECHO;
            case 6: return OUTPOST;
            case 9: return null; // No starport
            default: return OUTPOST;
        }
    }

    /**
     * Returns the next larger station type, or null if already Alpha.
     *
     * @return the next larger station type
     */
    public StationType upgrade() {
        switch (this) {
            case OUTPOST: return ECHO;
            case ECHO: return DELTA;
            case DELTA: return CHARLIE;
            case CHARLIE: return BRAVO;
            case BRAVO: return ALPHA;
            case ALPHA: return null;
            default: return null;
        }
    }

    /**
     * Returns the next smaller station type, or null if already Outpost.
     *
     * @return the next smaller station type
     */
    public StationType downgrade() {
        switch (this) {
            case ALPHA: return BRAVO;
            case BRAVO: return CHARLIE;
            case CHARLIE: return DELTA;
            case DELTA: return ECHO;
            case ECHO: return OUTPOST;
            case OUTPOST: return null;
            default: return null;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
