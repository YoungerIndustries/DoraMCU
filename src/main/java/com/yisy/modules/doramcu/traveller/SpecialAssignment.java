package com.yisy.modules.doramcu.traveller;

/**
 * Special assignments in Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * When a character rolls "Special" on the General Assignment table,
 * they receive one of these special assignments instead of a unit assignment.
 * Different special assignments are available for enlisted/NCOs versus officers.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum SpecialAssignment {

    // ---- Enlisted and NCO Special Assignments ----

    /**
     * Cross Training — train in another arm of service.
     * <p>
     * Enlisted/NCO only. Allows transfer to that arm after reenlisting.
     * </p>
     */
    CROSS_TRAINING("Cross Training", "Train in another arm of service",
            SpecialAssignmentTier.ENLISTED_NCO),

    /**
     * Specialist School — receive expertise in a specific field.
     * <p>
     * Enlisted/NCO only. Skills: Admin, Medical, Commo, Computer, Mechanical, Electronics.
     * </p>
     */
    SPECIALIST_SCHOOL("Specialist School", "Receive expertise in a specific field",
            SpecialAssignmentTier.ENLISTED_NCO),

    /**
     * Commando School — elite special operations training.
     * <p>
     * Available to both enlisted/NCO and officers.
     * Skills: Brawling, Gun Combat, Demolitions, Wilderness Survival,
     * Recon, Vacc Suit, Blade Combat, Instruction (roll 5+ for each).
     * </p>
     */
    COMMANDO_SCHOOL("Commando School", "Elite special operations training",
            SpecialAssignmentTier.BOTH),

    /**
     * Protected Forces Training — hostile environment operations.
     * <p>
     * Enlisted/NCO only. Skills: Vacc Suit, Zero-G Combat (roll 3+ for each).
     * </p>
     */
    PROTECTED_FORCES("Protected Forces", "Hostile environment operations",
            SpecialAssignmentTier.ENLISTED_NCO),

    /**
     * Recruiting — automatic Recruiting skill.
     * <p>
     * Available to both enlisted/NCO and officers.
     * </p>
     */
    RECRUITING_DUTY("Recruiting", "Automatic Recruiting skill",
            SpecialAssignmentTier.BOTH),

    /**
     * Officer Candidate School — commissioning training.
     * <p>
     * Enlisted/NCO only. Grants commission as Second Lieutenant.
     * Skills: one command, one staff, one MOS.
     * </p>
     */
    OCS("Officer Candidate School", "Commissioning training",
            SpecialAssignmentTier.ENLISTED_NCO),

    // ---- Officer Special Assignments ----

    /**
     * Intelligence School — intelligence gathering training.
     * <p>
     * Officers only. Skills: Forgery, Bribery, Streetwise, Interrogation (roll 4+ for each).
     * </p>
     */
    INTELLIGENCE_SCHOOL("Intelligence School", "Intelligence gathering training",
            SpecialAssignmentTier.OFFICER),

    /**
     * Command College — advanced command training.
     * <p>
     * Officers only. Skills: Tactics, Leadership, Recon (roll 4+ for each).
     * </p>
     */
    COMMAND_COLLEGE("Command College", "Advanced command training",
            SpecialAssignmentTier.OFFICER),

    /**
     * Staff College — advanced staff training.
     * <p>
     * Officers only. Skills: Admin, Combat Engineering, Computer (roll 4+ for each).
     * </p>
     */
    STAFF_COLLEGE("Staff College", "Advanced staff training",
            SpecialAssignmentTier.OFFICER),

    /**
     * Military Attache / Aide — diplomatic posting.
     * <p>
     * Officers only. May grant promotion and +1 Social Standing.
     * </p>
     */
    MILITARY_ATTACH("Military Attache/Aide", "Diplomatic posting",
            SpecialAssignmentTier.OFFICER);

    private final String name;
    private final String description;
    private final SpecialAssignmentTier tier;

    SpecialAssignment(String name, String description, SpecialAssignmentTier tier) {
        this.name = name;
        this.description = description;
        this.tier = tier;
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
     * Returns the tier of this assignment.
     *
     * @return the tier
     */
    public SpecialAssignmentTier getTier() {
        return tier;
    }

    /**
     * Returns true if this assignment is available to enlisted/NCO personnel.
     *
     * @return true if available to enlisted/NCO
     */
    public boolean isAvailableToEnlisted() {
        return tier == SpecialAssignmentTier.ENLISTED_NCO || tier == SpecialAssignmentTier.BOTH;
    }

    /**
     * Returns true if this assignment is available to officers.
     *
     * @return true if available to officers
     */
    public boolean isAvailableToOfficers() {
        return tier == SpecialAssignmentTier.OFFICER || tier == SpecialAssignmentTier.BOTH;
    }

    /**
     * Tiers of special assignments.
     */
    public enum SpecialAssignmentTier {
        /** Available only to enlisted and NCO personnel */
        ENLISTED_NCO("Enlisted/NCO"),
        /** Available only to commissioned officers */
        OFFICER("Officer"),
        /** Available to both enlisted/NCO and officers */
        BOTH("Both");

        private final String name;

        SpecialAssignmentTier(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
