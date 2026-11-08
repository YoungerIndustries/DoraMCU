package com.yisy.modules.doramcu.traveller;

/**
 * Represents a single career term that can be resolved by the player.
 * <p>
 * When a character has remaining career terms, each term is presented
 * as an option that the player can resolve either immediately (Traveller manner)
 * or by serving in the 4X game for a specified number of turns.
 * </p>
 */
public class TourOfDutyOption {

    private final Career career;
    private final int termNumber;
    private final String description;

    /**
     * Creates a tour option for the given career and term number.
     *
     * @param career the career for this tour
     * @param termNumber the term number (1-based, e.g., 1 for first term)
     */
    public TourOfDutyOption(Career career, int termNumber) {
        this.career = career;
        this.termNumber = termNumber;
        this.description = String.format("%s Term %d", career.getDisplayName(), termNumber);
    }

    /**
     * Returns the career for this tour.
     */
    public Career getCareer() {
        return career;
    }

    /**
     * Returns the term number (1-based).
     */
    public int getTermNumber() {
        return termNumber;
    }

    /**
     * Returns a human-readable description of this tour option.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "TourOfDutyOption{" +
                "career=" + career +
                ", term=" + termNumber +
                ", description='" + description + '\'' +
                '}';
    }
}
