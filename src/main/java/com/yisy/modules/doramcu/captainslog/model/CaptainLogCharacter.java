package com.yisy.modules.doramcu.captainslog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a Captain's Log character in the Solo RPG system.
 * <p>
 * Based on the Star Trek Adventures / Modiphius 2d20 system, a character has:
 * background traits, rank, assignment, environment, outlook, education, attributes,
 * disciplines with focuses, values, and career progression tracking.
 * </p>
 */
@Entity
@Table(name = "cl_characters", indexes = {
        @Index(name = "idx_cl_character_name", columnList = "name")
})
public class CaptainLogCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Pronouns for the character (e.g., "he/him", "she/her", "they/them"). */
    @Column(length = 50)
    private String pronouns;

    /** Character background trait 1. */
    @Column(length = 100)
    private String trait1;

    /** Character background trait 2. */
    @Column(length = 100)
    private String trait2;

    /** Character background trait 3. */
    @Column(length = 100)
    private String trait3;

    /** Starting rank (e.g., ENSIGN, LIEUTENANT, COMMANDER, CAPTAIN). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rank rank;

    /** Assignment type (e.g., COMMAND, SCIENCE, SECURITY, ENGINEERING, MEDICAL). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Assignment assignment;

    /** Environment background (e.g., CITY, RURAL, SPACE, WILD). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Environment environment;

    /** Early outlook (e.g., OPTIMISTIC, PESSIMISTIC, PRAGMATIC, IDEALISTIC). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Outlook outlook;

    /** Education background (e.g., ACADEMIC, MILITARY, SELF_TAUGHT, APPRENTICE). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Education education;

    /** Attribute: Fitness (FIT). */
    @Column(nullable = false)
    private int fitness = 0;

    /** Attribute: Technical (TEC). */
    @Column(nullable = false)
    private int technical = 0;

    /** Attribute: Strength (STR). */
    @Column(nullable = false)
    private int strength = 0;

    /** Attribute: Reason (REA). */
    @Column(nullable = false)
    private int reason = 0;

    /** Attribute: Insight (INS). */
    @Column(nullable = false)
    private int insight = 0;

    /** Attribute: Presence (PRE). */
    @Column(nullable = false)
    private int presence = 0;

    /** Constitution track value (hit points). */
    @Column(nullable = false)
    private int constitution = 0;

    /** Discipline: Command. */
    @Column(nullable = false)
    private int command = 0;

    /** Discipline: Leadership. */
    @Column(nullable = false)
    private int leadership = 0;

    /** Discipline: Science. */
    @Column(nullable = false)
    private int science = 0;

    /** Discipline: Security. */
    @Column(nullable = false)
    private int security = 0;

    /** Discipline: Medicine. */
    @Column(nullable = false)
    private int medicine = 0;

    /** Discipline: Operations. */
    @Column(nullable = false)
    private int operations = 0;

    /** Focuses (specialties) within disciplines — stored as "DISCIPLINE: Focus Name" pairs. */
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaptainLogFocus> focuses = new ArrayList<>();

    /** Character values (moral compass) — up to 3 core values. */
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaptainLogValue> values = new ArrayList<>();

    /** Crew members serving under this character. */
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CaptainLogCrewMember> crewMembers = new ArrayList<>();

    /** Character talents (special abilities). */
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CaptainLogCharacterTalent> talents = new ArrayList<>();

    /** Equipment inventory — stored as JSON-like text list. */
    @Column(columnDefinition = "TEXT")
    private String equipment;

    /** The starship this character is assigned to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "starship_id")
    @JsonIgnore
    private CaptainLogStarship assignedStarship;

    /** Total career events accumulated across missions. */
    @Column(nullable = false)
    private int careerEvents = 0;

    /** Number of missions completed. */
    @Column(nullable = false)
    private int missionsCompleted = 0;

    /** Experience points accumulated from missions and career events. */
    @Column(nullable = false)
    private int experiencePoints = 0;

    /** Current injury state of the character. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InjuryState injuryState = InjuryState.HEALTHY;

    /**
     * Injury states for the Captain's Log system.
     * Characters progress through these states as they take damage.
     */
    public enum InjuryState {
        HEALTHY, STUNNED, INJURED, DOWN
    }

    /**
     * Attributes for Captain's Log characters (Star Trek Adventures system).
     */
    public enum Attribute {
        FITNESS, TECHNICAL, STRENGTH, REASON, INSIGHT, PRESENCE
    }

    /**
     * Disciplines for Captain's Log characters.
     */
    public enum Discipline {
        COMMAND, LEADERSHIP, SCIENCE, SECURITY, MEDICINE, OPERATIONS
    }

    /**
     * Starting ranks for Captain's Log characters.
     */
    public enum Rank {
        ENSIGN, LIEUTENANT_JUNIOR, LIEUTENANT, LIEUTENANT_COMMANDER,
        COMMANDER, CAPTAIN, COMMANDING_OFFICER
    }

    /**
     * Assignment types determine the character's primary role.
     */
    public enum Assignment {
        COMMAND, SCIENCE, SECURITY, ENGINEERING, MEDICAL, OPERATIONS
    }

    /**
     * Environment backgrounds shape the character's upbringing.
     */
    public enum Environment {
        CITY, RURAL, SPACE, WILD, COLONY, FEDERATION_WORLD, NEUTRAL_ZONE
    }

    /**
     * Early outlook shapes the character's worldview.
     */
    public enum Outlook {
        OPTIMISTIC, PESSIMISTIC, PRAGMATIC, IDEALISTIC, CYNICAL, ADVENTUROUS
    }

    /**
     * Education backgrounds determine early training.
     */
    public enum Education {
        ACADEMIC, MILITARY, SELF_TAUGHT, APPRENTICE, ACADEMY, FIELD_TRAINING
    }

    public CaptainLogCharacter() {
    }

    public CaptainLogCharacter(String name, Rank rank, Assignment assignment) {
        this.name = name;
        this.rank = rank;
        this.assignment = assignment;
    }

    // --- Getters and Setters ---

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

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public String getTrait1() {
        return trait1;
    }

    public void setTrait1(String trait1) {
        this.trait1 = trait1;
    }

    public String getTrait2() {
        return trait2;
    }

    public void setTrait2(String trait2) {
        this.trait2 = trait2;
    }

    public String getTrait3() {
        return trait3;
    }

    public void setTrait3(String trait3) {
        this.trait3 = trait3;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Outlook getOutlook() {
        return outlook;
    }

    public void setOutlook(Outlook outlook) {
        this.outlook = outlook;
    }

    public Education getEducation() {
        return education;
    }

    public void setEducation(Education education) {
        this.education = education;
    }

    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public int getTechnical() {
        return technical;
    }

    public void setTechnical(int technical) {
        this.technical = technical;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public int getInsight() {
        return insight;
    }

    public void setInsight(int insight) {
        this.insight = insight;
    }

    public int getPresence() {
        return presence;
    }

    public void setPresence(int presence) {
        this.presence = presence;
    }

    public int getConstitution() {
        return constitution;
    }

    public void setConstitution(int constitution) {
        this.constitution = constitution;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getLeadership() {
        return leadership;
    }

    public void setLeadership(int leadership) {
        this.leadership = leadership;
    }

    public int getScience() {
        return science;
    }

    public void setScience(int science) {
        this.science = science;
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        this.security = security;
    }

    public int getMedicine() {
        return medicine;
    }

    public void setMedicine(int medicine) {
        this.medicine = medicine;
    }

    public int getOperations() {
        return operations;
    }

    public void setOperations(int operations) {
        this.operations = operations;
    }

    public List<CaptainLogFocus> getFocuses() {
        return focuses;
    }

    public void setFocuses(List<CaptainLogFocus> focuses) {
        this.focuses = focuses;
    }

    public List<CaptainLogValue> getValues() {
        return values;
    }

    public void setValues(List<CaptainLogValue> values) {
        this.values = values;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public int getCareerEvents() {
        return careerEvents;
    }

    public void setCareerEvents(int careerEvents) {
        this.careerEvents = careerEvents;
    }

    public int getMissionsCompleted() {
        return missionsCompleted;
    }

    public void setMissionsCompleted(int missionsCompleted) {
        this.missionsCompleted = missionsCompleted;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public InjuryState getInjuryState() {
        return injuryState;
    }

    public void setInjuryState(InjuryState injuryState) {
        this.injuryState = injuryState;
    }

    public CaptainLogStarship getAssignedStarship() {
        return assignedStarship;
    }

    public void setAssignedStarship(CaptainLogStarship assignedStarship) {
        this.assignedStarship = assignedStarship;
    }

    public List<CaptainLogCrewMember> getCrewMembers() {
        return crewMembers;
    }

    public void setCrewMembers(List<CaptainLogCrewMember> crewMembers) {
        this.crewMembers = crewMembers;
    }

    public List<CaptainLogCharacterTalent> getTalents() {
        return talents;
    }

    public void setTalents(List<CaptainLogCharacterTalent> talents) {
        this.talents = talents;
    }

    @Override
    public String toString() {
        return "CaptainLogCharacter{id=" + id + ", name='" + name + "', rank=" + rank +
                ", assignment=" + assignment + "}";
    }

    // --- Helper methods for task attempts ---

    /**
     * Returns the value of the given attribute.
     */
    public int getAttributeValue(Attribute attribute) {
        if (attribute == null) return 0;
        switch (attribute) {
            case FITNESS: return fitness;
            case TECHNICAL: return technical;
            case STRENGTH: return strength;
            case REASON: return reason;
            case INSIGHT: return insight;
            case PRESENCE: return presence;
            default: return 0;
        }
    }

    /**
     * Returns the rating of the given discipline.
     */
    public int getDisciplineRating(Discipline discipline) {
        if (discipline == null) return 0;
        switch (discipline) {
            case COMMAND: return command;
            case LEADERSHIP: return leadership;
            case SCIENCE: return science;
            case SECURITY: return security;
            case MEDICINE: return medicine;
            case OPERATIONS: return operations;
            default: return 0;
        }
    }

    /**
     * Checks if the character has a focus matching the given discipline and focus name.
     */
    public boolean hasFocus(Discipline discipline, String focusName) {
        if (discipline == null || focusName == null || focuses == null) return false;
        for (CaptainLogFocus focus : focuses) {
            if (focus.getDiscipline() != null &&
                focus.getDiscipline().name().equals(discipline.name()) &&
                focus.getName() != null &&
                focus.getName().equalsIgnoreCase(focusName)) {
                return true;
            }
        }
        return false;
    }
}
