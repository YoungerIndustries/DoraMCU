package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

/**
 * DTO for creating a new crew member.
 */
public class CreateCrewMemberRequest {

    /** Crew member's name. */
    private String name;

    /** Crew member's species. Optional. */
    private String species;

    /** Crew member's role on the ship (e.g., "Chief Engineer"). */
    private String role;

    /** Primary discipline area of expertise. */
    private CaptainLogCharacter.Discipline primaryDiscipline;

    /** Secondary discipline area. Optional. */
    private CaptainLogCharacter.Discipline secondaryDiscipline;

    /** Expertise rating (provides boon on task attempts). Default: 1. */
    private Integer expertiseRating;

    /** Relationship to the character. Optional. */
    private String relationship;

    /** Brief narrative description. Optional. */
    private String description;

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public CaptainLogCharacter.Discipline getPrimaryDiscipline() {
        return primaryDiscipline;
    }

    public void setPrimaryDiscipline(CaptainLogCharacter.Discipline primaryDiscipline) {
        this.primaryDiscipline = primaryDiscipline;
    }

    public CaptainLogCharacter.Discipline getSecondaryDiscipline() {
        return secondaryDiscipline;
    }

    public void setSecondaryDiscipline(CaptainLogCharacter.Discipline secondaryDiscipline) {
        this.secondaryDiscipline = secondaryDiscipline;
    }

    public Integer getExpertiseRating() {
        return expertiseRating;
    }

    public void setExpertiseRating(Integer expertiseRating) {
        this.expertiseRating = expertiseRating;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
