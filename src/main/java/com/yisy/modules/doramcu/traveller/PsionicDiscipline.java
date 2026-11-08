package com.yisy.modules.doramcu.traveller;

/**
 * The six psionic disciplines from Classic Traveller B03: Worlds & Adventures.
 * <p>
 * Each discipline represents a category of psychic power. Characters with
 * psionic ability can develop one or more disciplines over time.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 3: Worlds & Adventures (1977).
 * </p>
 */
public enum PsionicDiscipline {

    /**
     * Mind reading, mental attacks, illusion, empathy.
     * Uses the character's In (Intuition) score for checks.
     */
    TELEPATHY("Telepathy", "Mind reading, mental attacks, illusion, empathy", 1),

    /**
     * Remote viewing, scrying, precognition.
     * Uses the character's In (Intuition) score for checks.
     */
    CLAIRVOYANCE("Clairvoyance", "Remote viewing, scrying, precognition", 2),

    /**
     * Moving objects, force blasts, telekinetic manipulation.
     * Uses the character's In (Intuition) score for checks.
     */
    TELEKINESIS("Telekinesis", "Moving objects, force blasts, manipulation", 3),

    /**
     * Detecting life, psionics, thoughts, emotions.
     * Uses the character's In (Intuition) score for checks.
     */
    AWARENESS("Awareness", "Detecting life, psionics, thoughts, emotions", 4),

    /**
     * Short-range movement, phasing through matter.
     * Uses the character's In (Intuition) score for checks.
     */
    TELEPORTATION("Teleportation", "Short-range movement, phasing", 5),

    /**
     * Time manipulation, probability alteration, reality warping.
     * Uses the character's In (Intuition) score for checks.
     */
    SPECIAL("Special", "Time manipulation, probability, reality warping", 6);

    private final String displayName;
    private final String description;
    private final int tableValue;

    PsionicDiscipline(String displayName, String description, int tableValue) {
        this.displayName = displayName;
        this.description = description;
        this.tableValue = tableValue;
    }

    /**
     * Returns the human-readable name of this discipline.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a description of what this discipline can do.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the 2D6 table value for this discipline (used in TABLE_PSIONICS).
     */
    public int getTableValue() {
        return tableValue;
    }
}
