package com.yisy.modules.doramcu.traveller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A single logged experience in a character's life.
 * <p>
 * Each experience captures a meaningful event — from character creation
 * through career service, missions, battles, and personal moments.
 * Experiences are stored chronologically in a {@link CharacterLog}
 * and later used by {@link BookGenerator} to produce a narrative book.
 * </p>
 * <p>
 * Key fields:
 * </p>
 * <ul>
 *   <li><b>type</b> — categorizes the event (battle, exploration, etc.)</li>
 *   <li><b>title</b> — a short headline for the event</li>
 *   <li><b>description</b> — factual summary of what happened</li>
 *   <li><b>narrative</b> — rich, prose-style text for the book</li>
 *   <li><b>location</b> — where the event took place</li>
 *   <li><b>date</b> — when the event occurred (in-game date)</li>
 *   <li><b>characterAge</b> — the character's age at the time</li>
 *   <li><b>outcome</b> — the result (success, failure, mixed)</li>
 * </ul>
 */
public class CharacterExperience {

    /**
     * Possible outcomes for an experience.
     */
    public enum Outcome {
        /** The experience ended in success. */
        SUCCESS("Success"),
        /** The experience ended in failure. */
        FAILURE("Failure"),
        /** The outcome was mixed — partial success or pyrrhic victory. */
        MIXED("Mixed"),
        /** The outcome is unknown or inconclusive. */
        UNKNOWN("Unknown");

        private final String displayName;

        Outcome(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final ExperienceType type;
    private final String title;
    private String description;
    private String narrative;
    private String location;
    private LocalDate date;
    private int characterAge;
    private Outcome outcome;
    private final long sequenceNumber;

    // For internal ordering
    private static long nextSequence = 0;

    /**
     * Creates a new experience with the given type and title.
     *
     * @param type  the type of experience
     * @param title a short headline for the event
     */
    public CharacterExperience(ExperienceType type, String title) {
        this.type = Objects.requireNonNull(type, "Type must not be null");
        this.title = Objects.requireNonNull(title, "Title must not be null");
        this.description = "";
        this.narrative = "";
        this.location = "";
        this.outcome = Outcome.UNKNOWN;
        this.sequenceNumber = nextSequence++;
    }

    // ============================================================
    // Fluent setters
    // ============================================================

    /**
     * Sets the factual description of what happened.
     *
     * @param description the description
     * @return this experience for chaining
     */
    public CharacterExperience description(String description) {
        this.description = Objects.requireNonNull(description, "Description must not be null");
        return this;
    }

    /**
     * Sets the rich narrative text for the book.
     *
     * @param narrative the narrative prose
     * @return this experience for chaining
     */
    public CharacterExperience narrative(String narrative) {
        this.narrative = Objects.requireNonNull(narrative, "Narrative must not be null");
        return this;
    }

    /**
     * Sets the location where the event took place.
     *
     * @param location the location (e.g., "Sol-3 (Earth)", "Lone Mountain 1117")
     * @return this experience for chaining
     */
    public CharacterExperience location(String location) {
        this.location = Objects.requireNonNull(location, "Location must not be null");
        return this;
    }

    /**
     * Sets the in-game date of the event.
     *
     * @param date the date
     * @return this experience for chaining
     */
    public CharacterExperience date(LocalDate date) {
        this.date = Objects.requireNonNull(date, "Date must not be null");
        return this;
    }

    /**
     * Sets the character's age at the time of the event.
     *
     * @param age the age in years
     * @return this experience for chaining
     */
    public CharacterExperience characterAge(int age) {
        this.characterAge = age;
        return this;
    }

    /**
     * Sets the outcome of the experience.
     *
     * @param outcome the outcome
     * @return this experience for chaining
     */
    public CharacterExperience outcome(Outcome outcome) {
        this.outcome = Objects.requireNonNull(outcome, "Outcome must not be null");
        return this;
    }

    // ============================================================
    // Getters
    // ============================================================

    public ExperienceType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getNarrative() {
        return narrative;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getCharacterAge() {
        return characterAge;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Returns a formatted date string for display.
     *
     * @return the formatted date, or "Unknown date" if not set
     */
    public String getFormattedDate() {
        if (date == null) {
            return "Unknown date";
        }
        return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }

    /**
     * Returns a one-line summary of this experience.
     *
     * @return a summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        if (location != null && !location.isEmpty()) {
            sb.append(" (").append(location).append(")");
        }
        if (date != null) {
            sb.append(" — ").append(getFormattedDate());
        }
        if (outcome != Outcome.UNKNOWN) {
            sb.append(" [").append(outcome.getDisplayName()).append("]");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "CharacterExperience{type=" + type.getDisplayName() +
                ", title='" + title + "', outcome=" + outcome.getDisplayName() + "}";
    }
}
