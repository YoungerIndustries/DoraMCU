package com.yisy.modules.doramcu.traveller;

/**
 * Imperial Navy ranks from Classic Traveller Book 5: High Guard.
 * <p>
 * The Imperial Navy uses a hierarchical rank structure from enlisted personnel
 * through commissioned officers to flag officers. Each rank has a numeric level
 * for comparison and promotion tracking.
 * </p>
 * <p>
 * Rank structure:
 * </p>
 * <ul>
 *   <li>Enlisted (0–4): Seaman to Chief Petty Officer</li>
 *   <li>Warrant Officers (5–6): Warrant Officer to Chief Warrant Officer</li>
 *   <li>Junior Officers (7–9): Ensign to Lieutenant</li>
 *   <li>Senior Officers (10–12): Lieutenant Commander to Captain</li>
 *   <li>Flag Officers (13–16): Commodore to Admiral of the Fleet</li>
 * </ul>
 */
public enum NavalRank {

    /**
     * Seaman — entry-level enlisted personnel.
     */
    SEAMAN(0, "Seaman", "Enlisted"),

    /**
     * Seaman 2nd Class — experienced enlisted sailor.
     */
    SEAMAN_2ND(1, "Seaman 2nd Class", "Enlisted"),

    /**
     * Seaman 1st Class — senior enlisted sailor.
     */
    SEAMAN_1ST(2, "Seaman 1st Class", "Enlisted"),

    /**
     * Petty Officer — junior NCO equivalent.
     */
    PETTY_OFFICER(3, "Petty Officer", "Enlisted"),

    /**
     * Chief Petty Officer — senior NCO equivalent.
     */
    CHIEF_PETTY_OFFICER(4, "Chief Petty Officer", "Enlisted"),

    /**
     * Warrant Officer — specialized technical officer.
     */
    WARRANT_OFFICER(5, "Warrant Officer", "Warrant"),

    /**
     * Chief Warrant Officer — senior technical officer.
     */
    CHIEF_WARRANT(6, "Chief Warrant Officer", "Warrant"),

    /**
     * Ensign — lowest commissioned officer rank; departmental officer.
     */
    ENSIGN(7, "Ensign", "Junior Officer"),

    /**
     * Lieutenant Junior Grade — junior departmental officer.
     */
    LIEUTENANT_JG(8, "Lieutenant Junior Grade", "Junior Officer"),

    /**
     * Lieutenant — departmental head or executive officer of small ships.
     */
    LIEUTENANT(9, "Lieutenant", "Junior Officer"),

    /**
     * Lieutenant Commander — senior departmental officer or XO of medium ships.
     */
    LIEUTENANT_COMMANDER(10, "Lieutenant Commander", "Senior Officer"),

    /**
     * Commander — commanding officer of destroyers and frigates.
     */
    COMMANDER(11, "Commander", "Senior Officer"),

    /**
     * Captain — commanding officer of cruisers and battleships.
     */
    CAPTAIN(12, "Captain", "Senior Officer"),

    /**
     * Commodore — commander of a squadron (2–4 ships).
     */
    COMMODORE(13, "Commodore", "Flag Officer"),

    /**
     * Rear Admiral — commander of a task force (multiple squadrons).
     */
    REAR_ADMIRAL(14, "Rear Admiral", "Flag Officer"),

    /**
     * Vice Admiral — commander of a sub-sectoral fleet.
     */
    VICE_ADMIRAL(15, "Vice Admiral", "Flag Officer"),

    /**
     * Admiral — commander of a sectoral fleet.
     */
    ADMIRAL(16, "Admiral", "Flag Officer"),

    /**
     * Admiral of the Fleet — highest naval rank; rare, ceremonial or wartime.
     */
    ADMIRAL_OF_THE_FLEET(17, "Admiral of the Fleet", "Flag Officer");

    private final int level;
    private final String title;
    private final String category;

    NavalRank(int level, String title, String category) {
        this.level = level;
        this.title = title;
        this.category = category;
    }

    /**
     * Returns the numeric rank level for comparison.
     *
     * @return the rank level (0–17)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the formal title of this rank.
     *
     * @return the title string
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the rank category (Enlisted, Warrant, Junior Officer, Senior Officer, Flag Officer).
     *
     * @return the category string
     */
    public String getCategory() {
        return category;
    }

    /**
     * Checks if this rank is a commissioned officer (Ensign or above).
     *
     * @return true if commissioned officer
     */
    public boolean isCommissionedOfficer() {
        return level >= ENSIGN.level;
    }

    /**
     * Checks if this rank is a flag officer (Commodore or above).
     *
     * @return true if flag officer
     */
    public boolean isFlagOfficer() {
        return level >= COMMODORE.level;
    }

    /**
     * Checks if this rank can command a ship of the given tonnage.
     * <p>
     * Command requirements:
     * </p>
     * <ul>
     *   <li>Commander: up to 10,000 tons (destroyers, frigates)</li>
     *   <li>Captain: up to 100,000 tons (cruisers, battleships)</li>
     *   <li>Commodore+: any single ship, but typically commands squadrons</li>
     * </ul>
     *
     * @param tonnage the ship's total tonnage
     * @return true if this rank can command the ship
     */
    public boolean canCommandShip(int tonnage) {
        if (tonnage <= 10000) {
            return level >= COMMANDER.level;
        } else if (tonnage <= 100000) {
            return level >= CAPTAIN.level;
        } else {
            return level >= COMMODORE.level;
        }
    }

    /**
     * Returns the next higher rank, or null if already at the top.
     *
     * @return the next rank, or null
     */
    public NavalRank nextRank() {
        NavalRank[] all = values();
        if (level >= all[all.length - 1].level) {
            return null;
        }
        return all[level + 1];
    }

    /**
     * Returns the next lower rank, or null if already at the bottom.
     *
     * @return the previous rank, or null
     */
    public NavalRank previousRank() {
        if (level <= 0) {
            return null;
        }
        return values()[level - 1];
    }

    /**
     * Finds the rank with the given level.
     *
     * @param level the rank level (0–17)
     * @return the matching rank, or null if not found
     */
    public static NavalRank fromLevel(int level) {
        for (NavalRank rank : values()) {
            if (rank.level == level) {
                return rank;
            }
        }
        return null;
    }

    /**
     * Finds the rank with the given title (case-insensitive).
     *
     * @param title the rank title
     * @return the matching rank, or null if not found
     */
    public static NavalRank fromTitle(String title) {
        if (title == null) {
            return null;
        }
        for (NavalRank rank : values()) {
            if (rank.title.equalsIgnoreCase(title)) {
                return rank;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return title;
    }
}
