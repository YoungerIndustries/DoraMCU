package com.yisy.modules.doramcu.traveller;

/**
 * Mercenary-specific skills from Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * These skills extend the base Traveller skill set with military-specific
 * expertise needed for mercenary operations. They include specialized
 * weapon skills, combat engineering, reconnaissance, and other military
 * capabilities.
 * </p>
 * <p>
 * Skills are organized by category. Some skills require a specific expertise
 * choice when first acquired (e.g., Gun Combat requires choosing a weapon
 * type, Heavy Weapons requires choosing a specific weapon system).
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum MercenarySkill {

    // ---- Gun Combat skills (specialized small arms) ----

    /**
     * Combat Rifleman — rifles, carbines, assault rifles, ACRs, gauss rifles.
     * <p>
     * The first Gun Combat skill acquired must be Combat Rifleman.
     * </p>
     */
    COMBAT_RIFLEMAN("Combat Rifleman", "Rifles, carbines, assault rifles, ACRs, gauss rifles",
            MercenarySkillCategory.GUN_COMBAT),

    /**
     * Pistol — revolvers and automatic pistols.
     */
    PISTOL("Pistol", "Revolvers and automatic pistols",
            MercenarySkillCategory.GUN_COMBAT),

    /**
     * Submachine Gun — submachine guns only.
     */
    SUBMACHINE_GUN("Submachine Gun", "Submachine guns",
            MercenarySkillCategory.GUN_COMBAT),

    /**
     * Laser Weapons — laser carbines and laser rifles.
     */
    LASER_WEAPONS("Laser Weapons", "Laser carbines and laser rifles",
            MercenarySkillCategory.GUN_COMBAT),

    /**
     * Zero-G Weapons — accelerator rifles and snub pistols.
     * <p>
     * Requires Zero-G Combat expertise.
     * </p>
     */
    ZERO_G_WEAPONS("Zero-G Weapons", "Accelerator rifles and snub pistols",
            MercenarySkillCategory.GUN_COMBAT),

    /**
     * Heavy Weapons — heavy infantry support weapons.
     * <p>
     * Must choose a specific weapon system when first acquired.
     * </p>
     */
    HEAVY_WEAPONS("Heavy Weapons", "Heavy infantry support weapons",
            MercenarySkillCategory.GUN_COMBAT),

    /**
     * High Energy Weapons — plasma and fusion guns.
     * <p>
     * Does not include PGMP-13 and FGMP-14 (battle dress only).
     * </p>
     */
    HIGH_ENERGY_WEAPONS("High Energy Weapons", "Plasma and fusion guns",
            MercenarySkillCategory.GUN_COMBAT),

    /**
     * Auto Weapons — auto-rifles, light machine guns, heavy machine guns.
     */
    AUTO_WEAPONS("Auto Weapons", "Auto-rifles, light and heavy machine guns",
            MercenarySkillCategory.GUN_COMBAT),

    // ---- Combat skills ----

    /**
     * Brawling — unarmed combat.
     */
    BRAWLING("Brawling", "Unarmed combat",
            MercenarySkillCategory.COMBAT),

    /**
     * Blade Combat — melee weapons.
     * <p>
     * Marines must take Cutlass unless they pass a saving throw.
     * </p>
     */
    BLADE_COMBAT("Blade Combat", "Melee weapons (cutlass, knife, etc.)",
            MercenarySkillCategory.COMBAT),

    /**
     * Demolitions — explosives handling and placement.
     * <p>
     * Untrained personnel risk mishaps when handling explosives.
     * </p>
     */
    DEMOLITIONS("Demolitions", "Explosives handling and placement",
            MercenarySkillCategory.COMBAT),

    /**
     * Combat Engineering — field fortifications, mines, sensors.
     */
    COMBAT_ENGINEERING("Combat Engineering", "Field fortifications, mines, sensors",
            MercenarySkillCategory.COMBAT),

    /**
     * Reconnaissance — military scouting and tracking.
     * <p>
     * Higher skill means lower chance of being detected and higher
     * chance of spotting the enemy in advance.
     * </p>
     */
    RECON("Reconnaissance", "Military scouting and tracking",
            MercenarySkillCategory.COMBAT),

    /**
     * Zero-G Combat — fighting in zero gravity environments.
     * <p>
     * Required for Zero-G Weapons skill.
     * </p>
     */
    ZERO_G_COMBAT("Zero-G Combat", "Fighting in zero gravity",
            MercenarySkillCategory.COMBAT),

    // ---- Artillery skills ----

    /**
     * FA Gunnery — field artillery gunnery.
     * <p>
     * Must choose a specific weapon system when first acquired.
     * Increased skill grants familiarity with earlier TL systems.
     * </p>
     */
    FA_GUNNERY("FA Gunnery", "Field artillery gunnery",
            MercenarySkillCategory.ARTILLERY),

    /**
     * Forward Observer — artillery fire direction and correction.
     */
    FWD_OBSERVER("Forward Observer", "Artillery fire direction and correction",
            MercenarySkillCategory.ARTILLERY),

    // ---- Vehicle skills ----

    /**
     * Vehicle — driving military vehicles.
     * <p>
     * Must choose wheeled, tracked, or grav vehicle when first acquired.
     * </p>
     */
    VEHICLE("Vehicle", "Driving military vehicles (wheeled, tracked, grav)",
            MercenarySkillCategory.VEHICLE),

    // ---- Protective equipment ----

    /**
     * Vacc Suit — vacuum suit operation.
     */
    VACC_SUIT("Vacc Suit", "Vacuum suit operation",
            MercenarySkillCategory.EQUIPMENT),

    /**
     * Battle Dress — powered armor operation.
     * <p>
     * Required for PGMP-13 and FGMP-14 weapons.
     * Without Battle Dress, firing these weapons risks mishaps.
     * </p>
     */
    BATTLE_DRESS("Battle Dress", "Powered armor operation",
            MercenarySkillCategory.EQUIPMENT),

    // ---- Support skills ----

    /**
     * Instruction — training and teaching.
     * <p>
     * Required to conduct basic training and skill training.
     * Can teach skills up to one less than instructor's level.
     * </p>
     */
    INSTRUCTION("Instruction", "Training and teaching",
            MercenarySkillCategory.SUPPORT),

    /**
     * Recruiting — acquiring personnel.
     * <p>
     * Affects quality and quantity of recruits.
     * Recruiting-1 has approximately the same effect as Leadership-4.
     * </p>
     */
    RECRUITING("Recruiting", "Acquiring personnel",
            MercenarySkillCategory.SUPPORT),

    /**
     * Medical — battlefield medicine.
     */
    MEDIC("Medic", "Battlefield medicine",
            MercenarySkillCategory.SUPPORT),

    /**
     * Mechanical — vehicle and equipment maintenance.
     */
    MECHANICAL("Mechanical", "Vehicle and equipment maintenance",
            MercenarySkillCategory.SUPPORT),

    /**
     * Electronics — electronic equipment repair.
     */
    ELECTRONICS_MERC("Electronics", "Electronic equipment repair",
            MercenarySkillCategory.SUPPORT),

    /**
     * Computer — military computer systems.
     */
    COMPUTER_MERC("Computer", "Military computer systems",
            MercenarySkillCategory.SUPPORT),

    // ---- Intelligence skills (officers only) ----

    /**
     * Forgery — creating false documents.
     */
    FORGERY_MERC("Forgery", "Creating false documents",
            MercenarySkillCategory.INTELLIGENCE),

    /**
     * Bribery — obtaining information through payment.
     */
    BRIBERY("Bribery", "Obtaining information through payment",
            MercenarySkillCategory.INTELLIGENCE),

    /**
     * Streetwise — urban survival and information gathering.
     */
    STREETWISE("Streetwise", "Urban survival and information gathering",
            MercenarySkillCategory.INTELLIGENCE),

    /**
     * Interrogation — extracting information from prisoners.
     * <p>
     * Higher skill means better ability to derive conclusions
     * from partial information and detect lies.
     * </p>
     */
    INTERROGATION("Interrogation", "Extracting information from prisoners",
            MercenarySkillCategory.INTELLIGENCE),

    // ---- Command and Staff skills ----

    /**
     * Tactics — battlefield command and planning.
     */
    TACTICS_MERC("Tactics", "Battlefield command and planning",
            MercenarySkillCategory.COMMAND),

    /**
     * Leadership — motivating and commanding troops.
     * <p>
     * Provides morale bonus to nearby friendly units.
     * </p>
     */
    LEADER("Leader", "Motivating and commanding troops",
            MercenarySkillCategory.COMMAND),

    /**
     * Admin — administrative and logistical management.
     */
    ADMIN("Admin", "Administrative and logistical management",
            MercenarySkillCategory.STAFF),

    // ---- Miscellaneous ----

    /**
     * Gambling — games of chance.
     */
    GAMBLING("Gambling", "Games of chance",
            MercenarySkillCategory.MISC),

    /**
     * Gunnery — shipboard weapons operation.
     */
    GUNNERY("Gunnery", "Shipboard weapons operation",
            MercenarySkillCategory.SHIPBOARD),

    /**
     * Ship's Boat — small boat operation.
     */
    SHIPS_BOAT("Ship's Boat", "Small boat operation",
            MercenarySkillCategory.SHIPBOARD),

    /**
     * Wilderness Survival — living off the land.
     */
    WILDERNESS_SURVIVAL("Wilderness Survival", "Living off the land",
            MercenarySkillCategory.SURVIVAL),

    /**
     * Battle Dress specific skill — shipboard operations.
     */
    SHIPBOARD_MEDIC("Shipboard Medic", "Shipboard medical operations",
            MercenarySkillCategory.SHIPBOARD);

    private final String name;
    private final String description;
    private final MercenarySkillCategory category;

    MercenarySkill(String name, String description, MercenarySkillCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    /**
     * Returns the name of the skill.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the skill.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the category of the skill.
     *
     * @return the category
     */
    public MercenarySkillCategory getCategory() {
        return category;
    }

    /**
     * Categories of Mercenary skills.
     */
    public enum MercenarySkillCategory {
        /** Gun combat skills for small arms */
        GUN_COMBAT("Gun Combat"),
        /** General combat skills */
        COMBAT("Combat"),
        /** Artillery skills */
        ARTILLERY("Artillery"),
        /** Vehicle operation skills */
        VEHICLE("Vehicle"),
        /** Protective equipment skills */
        EQUIPMENT("Equipment"),
        /** Support and service skills */
        SUPPORT("Support"),
        /** Intelligence gathering skills */
        INTELLIGENCE("Intelligence"),
        /** Command and leadership skills */
        COMMAND("Command"),
        /** Staff and administrative skills */
        STAFF("Staff"),
        /** Shipboard skills */
        SHIPBOARD("Shipboard"),
        /** Survival skills */
        SURVIVAL("Survival"),
        /** Miscellaneous skills */
        MISC("Miscellaneous");

        private final String name;

        MercenarySkillCategory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
