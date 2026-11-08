package com.yisy.modules.doramcu.traveller;

/**
 * Types of character experiences that can be logged.
 * <p>
 * Each type represents a category of event in a character's life,
 * from creation through career service and adventures.
 * The book generator uses these types to organize chapters
 * and determine narrative tone.
 * </p>
 */
public enum ExperienceType {

    /**
     * Character creation — rolling abilities and choosing a name.
     */
    CHARACTER_CREATION("Character Creation", "The beginning of a life"),

    /**
     * Starting a new career tour.
     */
    CAREER_START("Career Start", "Entering a new profession"),

    /**
     * Completing a career tour.
     */
    CAREER_COMPLETION("Career Completion", "Finishing a term of service"),

    /**
     * Earning a skill during service.
     */
    SKILL_GAIN("Skill Gain", "Learning something new"),

    /**
     * Promotion within a career.
     */
    PROMOTION("Promotion", "Rising through the ranks"),

    /**
     * Retirement from a career.
     */
    RETIREMENT("Retirement", "Leaving service behind"),

    /**
     * A combat mission or battle.
     */
    BATTLE("Battle", "The clash of arms"),

    /**
     * A successful mission.
     */
    MISSION_SUCCESS("Mission Success", "Victory achieved"),

    /**
     * A failed mission.
     */
    MISSION_FAILURE("Mission Failure", "Defeat and its consequences"),

    /**
     * Exploring a new world or system.
     */
    EXPLORATION("Exploration", "Discovering the unknown"),

    /**
     * A trade deal or commercial venture.
     */
    TRADE("Trade", "Commerce and profit"),

    /**
     * A social encounter — diplomacy, negotiation, or intrigue.
     */
    SOCIAL("Social Encounter", "Dealing with others"),

    /**
     * Suffering an injury or wound.
     */
    INJURY("Injury", "The cost of conflict"),

    /**
     * A close call or narrow escape.
     */
    NARROW_ESCAPE("Narrow Escape", "Surviving against the odds"),

    /**
     * Discovering something significant — a world, artifact, or secret.
     */
    DISCOVERY("Discovery", "Uncovering hidden truths"),

    /**
     * A betrayal or act of treachery.
     */
    BETRAYAL("Betrayal", "Trust broken"),

    /**
     * An act of heroism or sacrifice.
     */
    HEROISM("Heroism", "Going beyond the call"),

    /**
     * Death of the character.
     */
    DEATH("Death", "The end of the road"),

    /**
     * A personal reflection or character development moment.
     */
    REFLECTION("Reflection", "Moments of introspection"),

    /**
     * A relationship formed — ally, friend, or mentor.
     */
    RELATIONSHIP("Relationship", "Bonds with others"),

    /**
     * Acquiring a significant item, ship, or asset.
     */
    ACQUISITION("Acquisition", "Gaining something valuable"),

    /**
     * Losing something significant.
     */
    LOSS("Loss", "What was taken away"),

    /**
     * A custom or miscellaneous experience.
     */
    OTHER("Other", "An event beyond categorization");

    private final String displayName;
    private final String subtitle;

    ExperienceType(String displayName, String subtitle) {
        this.displayName = displayName;
        this.subtitle = subtitle;
    }

    /**
     * Returns the display name for this experience type.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a short subtitle suitable for chapter headings.
     *
     * @return the subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }
}
