package com.yisy.modules.doramcu.traveller;

/**
 * Skills available in Classic Traveller character creation.
 * <p>
 * Skills represent specific abilities a character has learned through
 * career service, education, or personal study. Each skill enhances
 * a character's capabilities in a specific area.
 * </p>
 * <p>
 * Skills are organized by career skill tables. When a character gains
 * a skill during a career tour, one is randomly selected from the
 * career's skill table.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1: Characters & Combat (1981).
 * </p>
 */
public enum Skill {

    // Combat skills
    MELEE("Melee", "Unarmed and melee weapon combat", SkillCategory.COMBAT),
    FIREARMS("Firearms", "Pistol and rifle combat", SkillCategory.COMBAT),
    SKIRMISH("Skirmish", "Small unit tactics and combat", SkillCategory.COMBAT),
    GUERRILLA("Guerrilla", "Irregular warfare and sabotage", SkillCategory.COMBAT),

    // Leadership and tactics
    LEADERSHIP("Leadership", "Command and motivation of troops", SkillCategory.LEADERSHIP),
    TACTICS("Tactics", "Strategic planning and battlefield command", SkillCategory.LEADERSHIP),

    // Technical skills
    ELECTRONICS("Electronics", "Repair and operation of electronic devices", SkillCategory.TECHNICAL),
    COMPUTERS("Computers", "Computer programming and operation", SkillCategory.TECHNICAL),
    GRAVITIC("Gravitic", "Gravity drive operation and repair", SkillCategory.TECHNICAL),
    MISSILE("Missile", "Missile weapons operation and maintenance", SkillCategory.TECHNICAL),
    LASER("Laser", "Laser weapons operation and maintenance", SkillCategory.TECHNICAL),

    // Navigation and piloting
    NAVIGATION("Navigation", "Starship and planetary navigation", SkillCategory.NAVIGATION),
    STARSHIPS("Starships", "Starship operation and systems", SkillCategory.NAVIGATION),

    // Scientific skills
    ASTRONOMY("Astronomy", "Study of celestial bodies and phenomena", SkillCategory.SCIENTIFIC),
    GEOLOGY("Geology", "Study of planetary geology and minerals", SkillCategory.SCIENTIFIC),
    BIOLOGY("Biology", "Study of living organisms", SkillCategory.SCIENTIFIC),
    CHEMISTRY("Chemistry", "Study of chemical compounds and reactions", SkillCategory.SCIENTIFIC),
    PHYSICS("Physics", "Study of physical laws and phenomena", SkillCategory.SCIENTIFIC),

    // Social skills
    LANGUAGE("Language", "Knowledge of an additional language", SkillCategory.SOCIAL),
    PSYCHOLOGY("Psychology", "Understanding of behavior and motivation", SkillCategory.SOCIAL),
    FORGERY("Forgery", "Creation of false documents and items", SkillCategory.SOCIAL),

    // Survival skills
    SURVIVAL("Survival", "Living in hostile environments", SkillCategory.SURVIVAL),
    MEDICAL("Medical", "First aid and medical treatment", SkillCategory.SURVIVAL),

    // Stealth skills
    STEALTH("Stealth", "Moving unseen and avoiding detection", SkillCategory.STEALTH);

    private final String name;
    private final String description;
    private final SkillCategory category;

    Skill(String name, String description, SkillCategory category) {
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
    public SkillCategory getCategory() {
        return category;
    }

    /**
     * Categories of skills in Classic Traveller.
     */
    public enum SkillCategory {
        COMBAT("Combat"),
        LEADERSHIP("Leadership"),
        TECHNICAL("Technical"),
        NAVIGATION("Navigation"),
        SCIENTIFIC("Scientific"),
        SOCIAL("Social"),
        SURVIVAL("Survival"),
        STEALTH("Stealth");

        private final String name;

        SkillCategory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
