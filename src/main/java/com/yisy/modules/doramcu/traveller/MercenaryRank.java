package com.yisy.modules.doramcu.traveller;

/**
 * Military ranks from Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * Ranks are divided into four groups: Enlisted, Non-Commissioned Officers (NCO),
 * Junior Commissioned Officers, Field Grade Officers, and General Officers.
 * Each rank has a pay grade (E1-E9 for enlisted/NCO, O1-O10 for officers),
 * a Traveller rank value, and a monthly salary for mercenary service.
 * </p>
 * <p>
 * Mercenary officers traditionally do not hold general officer rank.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum MercenaryRank {

    // ---- Enlisted Ranks ----

    /**
     * E1: Private — no special skills or responsibilities.
     */
    PRIVATE("E1", "Private", MercenaryRankGroup.ENLISTED, 0, 300, 1),

    /**
     * E2: Lance Corporal — non-commanding specialist (vehicle driver, gunner).
     */
    LANCE_CORPORAL("E2", "Lance Corporal", MercenaryRankGroup.ENLISTED, 0, 400, 2),

    // ---- Non-Commissioned Officers ----

    /**
     * E3: Corporal — fire team leader, vehicle or gun commander.
     */
    CORPORAL("E3", "Corporal", MercenaryRankGroup.NCO, 0, 450, 3),

    /**
     * E4: Lance Sergeant — assistant squad leader or fire team commander.
     */
    LANCE_SERGEANT("E4", "Lance Sergeant", MercenaryRankGroup.NCO, 0, 500, 3),

    /**
     * E5: Sergeant — squad leader.
     */
    SERGEANT("E5", "Sergeant", MercenaryRankGroup.NCO, 0, 550, 4),

    /**
     * E6: Gunnery Sergeant — platoon senior NCO.
     */
    GUNNERY_SERGEANT("E6", "Gunnery Sergeant", MercenaryRankGroup.NCO, 0, 600, 4),

    /**
     * E7: Leading Sergeant — platoon leader or company senior NCO.
     */
    LEADING_SERGEANT("E7", "Leading Sergeant", MercenaryRankGroup.NCO, 0, 700, 5),

    /**
     * E8: First Sergeant — battalion senior NCO.
     */
    FIRST_SERGEANT("E8", "First Sergeant", MercenaryRankGroup.NCO, 0, 800, 6),

    /**
     * E9: Sergeant Major — regiment, brigade, or force senior NCO.
     */
    SERGEANT_MAJOR("E9", "Sergeant Major", MercenaryRankGroup.NCO, 0, 1000, 7),

    // ---- Junior Commissioned Officers ----

    /**
     * O1: Second Lieutenant — platoon leader.
     * Traveller rank 1.
     */
    SECOND_LIEUTENANT("O1", "Second Lieutenant", MercenaryRankGroup.JUNIOR_OFFICER, 1, 1000, 5),

    /**
     * O2: First Lieutenant — platoon leader, company commander or executive officer.
     * Traveller rank 1.
     */
    FIRST_LIEUTENANT("O2", "First Lieutenant", MercenaryRankGroup.JUNIOR_OFFICER, 1, 1200, 6),

    /**
     * O3: Captain — company commander.
     * Traveller rank 2.
     */
    CAPTAIN("O3", "Captain", MercenaryRankGroup.JUNIOR_OFFICER, 2, 1400, 7),

    // ---- Field Grade Commissioned Officers ----

    /**
     * O4: Major — company commander, battalion commander or executive officer,
     * task force commander. Marine Force Commander.
     * Traveller rank 3.
     */
    MAJOR("O4", "Major", MercenaryRankGroup.FIELD_GRADE, 3, 1600, 8),

    /**
     * O5: Lieutenant Colonel — battalion commander, or brigade/regiment/force
     * commander or executive officer.
     * Traveller rank 4.
     */
    LIEUTENANT_COLONEL("O5", "Lieutenant Colonel", MercenaryRankGroup.FIELD_GRADE, 4, 1800, 9),

    /**
     * O6: Colonel — brigade, regiment, force or division commander.
     * Traveller rank 5.
     */
    COLONEL("O6", "Colonel", MercenaryRankGroup.FIELD_GRADE, 5, 2000, 10),

    // ---- General Officers (rarely used in mercenary service) ----

    /**
     * O7: Brigadier General.
     * Traveller rank 6.
     */
    BRIGADIER_GENERAL("O7", "Brigadier General", MercenaryRankGroup.GENERAL, 6, 2500, 12),

    /**
     * O8: Major General.
     * Traveller rank 6.
     */
    MAJOR_GENERAL("O8", "Major General", MercenaryRankGroup.GENERAL, 6, 3000, 14),

    /**
     * O9: Lieutenant General.
     * Traveller rank 6.
     */
    LIEUTENANT_GENERAL("O9", "Lieutenant General", MercenaryRankGroup.GENERAL, 6, 3500, 16),

    /**
     * O10: General.
     * Traveller rank 6.
     */
    GENERAL("O10", "General", MercenaryRankGroup.GENERAL, 6, 4000, 20);

    private final String payGrade;
    private final String name;
    private final MercenaryRankGroup group;
    private final int travellerRank;
    private final int monthlySalary;
    private final int shares;

    MercenaryRank(String payGrade, String name, MercenaryRankGroup group,
                  int travellerRank, int monthlySalary, int shares) {
        this.payGrade = payGrade;
        this.name = name;
        this.group = group;
        this.travellerRank = travellerRank;
        this.monthlySalary = monthlySalary;
        this.shares = shares;
    }

    /**
     * Returns the pay grade (e.g., "E1", "O3").
     *
     * @return the pay grade
     */
    public String getPayGrade() {
        return payGrade;
    }

    /**
     * Returns the rank name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the rank group.
     *
     * @return the group
     */
    public MercenaryRankGroup getGroup() {
        return group;
    }

    /**
     * Returns the Traveller rank value (used for hiring DM calculations).
     *
     * @return the Traveller rank
     */
    public int getTravellerRank() {
        return travellerRank;
    }

    /**
     * Returns the monthly salary in Credit Rating (CR).
     *
     * @return the monthly salary
     */
    public int getMonthlySalary() {
        return monthlySalary;
    }

    /**
     * Returns the number of profit shares this rank receives.
     *
     * @return the number of shares
     */
    public int getShares() {
        return shares;
    }

    /**
     * Returns true if this rank is an officer (commissioned).
     *
     * @return true if officer
     */
    public boolean isOfficer() {
        return group == MercenaryRankGroup.JUNIOR_OFFICER ||
                group == MercenaryRankGroup.FIELD_GRADE ||
                group == MercenaryRankGroup.GENERAL;
    }

    /**
     * Returns true if this rank is an NCO.
     *
     * @return true if NCO
     */
    public boolean isNCO() {
        return group == MercenaryRankGroup.NCO;
    }

    /**
     * Returns the next higher rank, or null if already the highest.
     *
     * @return the next higher rank
     */
    public MercenaryRank nextHigher() {
        int ordinal = ordinal();
        if (ordinal < values().length - 1) {
            return values()[ordinal + 1];
        }
        return null;
    }

    /**
     * Returns the next lower rank, or null if already the lowest.
     *
     * @return the next lower rank
     */
    public MercenaryRank nextLower() {
        int ordinal = ordinal();
        if (ordinal > 0) {
            return values()[ordinal - 1];
        }
        return null;
    }

    /**
     * Groups of military ranks.
     */
    public enum MercenaryRankGroup {
        /** Enlisted personnel (E1-E2) */
        ENLISTED("Enlisted"),
        /** Non-Commissioned Officers (E3-E9) */
        NCO("Non-Commissioned Officer"),
        /** Junior Commissioned Officers (O1-O3) */
        JUNIOR_OFFICER("Junior Officer"),
        /** Field Grade Officers (O4-O6) */
        FIELD_GRADE("Field Grade Officer"),
        /** General Officers (O7-O10) */
        GENERAL("General Officer");

        private final String name;

        MercenaryRankGroup(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
