package com.yisy.modules.doramcu.captainslog.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a crew member in the Captain's Log Solo RPG system.
 * <p>
 * Crew members are NPCs who serve on the character's starship and can assist
 * with task attempts in their area of expertise. They have roles, key skills,
 * and a relationship to the character.
 * </p>
 */
@Entity
@Table(name = "cl_crew_members", indexes = {
        @Index(name = "idx_cl_crew_character", columnList = "character_id")
})
public class CaptainLogCrewMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Crew member's name. */
    @Column(nullable = false)
    private String name;

    /** Crew member's species. */
    @Column(length = 50)
    private String species;

    /** Crew member's role on the ship (e.g., "Chief Engineer", "Science Officer"). */
    @Column(nullable = false, length = 100)
    private String role;

    /** Primary discipline area of expertise. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CaptainLogCharacter.Discipline primaryDiscipline;

    /** Secondary discipline area. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CaptainLogCharacter.Discipline secondaryDiscipline;

    /**
     * The crew member provides a boon on task attempts in their primary discipline.
     * This represents their expertise assisting the character.
     */
    @Column(nullable = false)
    private int expertiseRating = 1;

    /** Relationship to the character (e.g., "Trusted Friend", "Rival", "Mentor"). */
    @Column(length = 100)
    private String relationship;

    /** Brief narrative description of the crew member. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Whether the crew member is currently active/available. */
    @Column(nullable = false)
    private boolean active = true;

    /** The character this crew member serves under. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private CaptainLogCharacter character;

    public CaptainLogCrewMember() {
    }

    public CaptainLogCrewMember(String name, String role, CaptainLogCharacter.Discipline primaryDiscipline) {
        this.name = name;
        this.role = role;
        this.primaryDiscipline = primaryDiscipline;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public int getExpertiseRating() {
        return expertiseRating;
    }

    public void setExpertiseRating(int expertiseRating) {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public CaptainLogCharacter getCharacter() {
        return character;
    }

    public void setCharacter(CaptainLogCharacter character) {
        this.character = character;
    }
}
