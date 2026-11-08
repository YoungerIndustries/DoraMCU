package com.yisy.modules.doramcu.captainslog.dto;

/**
 * Request to start a new lifepath character creation session.
 * <p>
 * The player provides the character's name and optionally a species.
 * The system returns the first lifepath step.
 * </p>
 */
public class LifepathStartRequest {

    /** Character's name. */
    private String name;

    /** Character's species (e.g., "Human", "Vulcan", "Klingon"). Optional. */
    private String species;

    /** Character's pronouns (e.g., "he/him", "she/her", "they/them"). Optional. */
    private String pronouns;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }
}
