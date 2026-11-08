package com.yisy.modules.doramcu.traveller;

/**
 * Ability scores for Classic Traveller character creation.
 * <p>
 * Each ability is rolled as 2D6, giving a range of 2–12 with a bell curve
 * distribution centered on 7. These abilities determine a character's
 * capabilities across all areas of life in the Traveller universe.
 * </p>
 * <p>
 * The seven abilities are:
 * </p>
 * <ul>
 *   <li>Dexterity (Dex) — agility, reflexes, hand-eye coordination</li>
 *   <li>Saving Throw (St) — stress resistance, luck, willpower</li>
 *   <li>Endurance (End) — physical strength, stamina, health</li>
 *   <li>Intelligence (Int) — reasoning, learning, perception</li>
 *   <li>Education (Edu) — formal schooling, knowledge breadth</li>
 *   <li>Social Standing (Soc) — family status, connections, influence</li>
 *   <li>Appearance (App) — physical attractiveness, charisma</li>
 * </ul>
 * <p>
 * Derived from Classic Traveller Book 1: Characters & Combat (1981).
 * </p>
 */
public enum AbilityScore {

    /**
     * Dexterity — measures agility, reflexes, and hand-eye coordination.
     * <p>
     * Affects melee combat, piloting, stealth, and technical skills.
     * High Dexterity means quick reactions and precise movements.
     * </p>
     */
    DEX("Dexterity", "Dex", "Agility, reflexes, hand-eye coordination"),

    /**
     * Saving Throw — measures stress resistance, luck, and willpower.
     * <p>
     * Affects resistance to fear, pain, and psychological pressure.
     * High Saving Throw means the character can endure extreme situations.
     * </p>
     */
    ST("Saving Throw", "St", "Stress resistance, luck, willpower"),

    /**
     * Endurance — measures physical strength, stamina, and health.
     * <p>
     * Affects melee damage, resistance to disease and poison, and physical labor.
     * High Endurance means the character can withstand physical punishment.
     * </p>
     */
    END("Endurance", "End", "Physical strength, stamina, health"),

    /**
     * Intelligence — measures reasoning, learning, and perception.
     * <p>
     * Affects technical skills, language learning, and problem-solving.
     * High Intelligence means the character learns quickly and thinks clearly.
     * </p>
     */
    INT("Intelligence", "Int", "Reasoning, learning, perception"),

    /**
     * Education — measures formal schooling and knowledge breadth.
     * <p>
     * Affects academic skills, professional knowledge, and cultural awareness.
     * High Education means the character has broad formal knowledge.
     * </p>
     */
    EDU("Education", "Edu", "Formal schooling, knowledge breadth"),

    /**
     * Social Standing — measures family status, connections, and influence.
     * <p>
     * Affects social interactions, access to resources, and political influence.
     * High Social Standing means the character comes from a prominent family.
     * </p>
     */
    SOC("Social Standing", "Soc", "Family status, connections, influence"),

    /**
     * Appearance — measures physical attractiveness and charisma.
     * <p>
     * Affects social interactions, persuasion, and first impressions.
     * High Appearance means the character is physically attractive and charming.
     * </p>
     */
    APP("Appearance", "App", "Physical attractiveness, charisma");

    private final String fullName;
    private final String abbreviation;
    private final String description;

    AbilityScore(String fullName, String abbreviation, String description) {
        this.fullName = fullName;
        this.abbreviation = abbreviation;
        this.description = description;
    }

    /**
     * Returns the full name of the ability.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the abbreviation of the ability.
     *
     * @return the abbreviation
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Returns the description of the ability.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
