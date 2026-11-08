package com.yisy.modules.doramcu.traveller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A chronological log of all experiences for a {@link TravellerCharacter}.
 * <p>
 * The log records every significant event in a character's life — from
 * creation through career service, missions, and personal moments.
 * Experiences are stored in chronological order and can be queried
 * by type, outcome, or date range.
 * </p>
 * <p>
 * The log is the primary data source for {@link BookGenerator}, which
 * transforms these structured entries into a narrative book.
 * </p>
 */
public class CharacterLog {

    private final TravellerCharacter character;
    private final List<CharacterExperience> experiences;
    private final LocalDate createdAt;

    /**
     * Creates a new log for the given character.
     *
     * @param character the character whose experiences are logged
     */
    public CharacterLog(TravellerCharacter character) {
        this.character = Objects.requireNonNull(character, "Character must not be null");
        this.experiences = new ArrayList<>();
        this.createdAt = LocalDate.now();
    }

    // ============================================================
    // Adding experiences
    // ============================================================

    /**
     * Adds an experience to the log.
     *
     * @param experience the experience to add
     */
    public void addExperience(CharacterExperience experience) {
        Objects.requireNonNull(experience, "Experience must not be null");
        experiences.add(experience);
    }

    /**
     * Creates and adds a new experience in one call.
     *
     * @param type  the type of experience
     * @param title a short headline
     * @return the created experience for further configuration
     */
    public CharacterExperience log(ExperienceType type, String title) {
        CharacterExperience exp = new CharacterExperience(type, title);
        // Auto-fill character age if not set
        exp.characterAge(character.getAge());
        addExperience(exp);
        return exp;
    }

    /**
     * Logs a quick one-line experience with just a description.
     *
     * @param type        the type of experience
     * @param title       a short headline
     * @param description what happened
     */
    public CharacterExperience log(ExperienceType type, String title, String description) {
        return log(type, title).description(description);
    }

    /**
     * Logs a full experience with narrative text.
     *
     * @param type        the type of experience
     * @param title       a short headline
     * @param description factual summary
     * @param narrative   rich prose for the book
     */
    public CharacterExperience log(ExperienceType type, String title, String description, String narrative) {
        return log(type, title).description(description).narrative(narrative);
    }

    // ============================================================
    // Convenience logging methods
    // ============================================================

    /**
     * Logs character creation with ability scores.
     *
     * @param location where the character was born
     */
    public void logCharacterCreation(String location) {
        StringBuilder abilities = new StringBuilder();
        for (AbilityScore ability : AbilityScore.values()) {
            if (abilities.length() > 0) {
                abilities.append(", ");
            }
            abilities.append(ability.getAbbreviation()).append("=")
                    .append(character.getAbility(ability));
        }

        String narrative = character.getName() + " was born on " + location
                + ". At age 18, their abilities were rolled: " + abilities
                + ". This would shape the rest of their life.";

        log(ExperienceType.CHARACTER_CREATION, "Birth and Abilities",
                "Character created with abilities: " + abilities, narrative)
                .location(location)
                .characterAge(18)
                .outcome(CharacterExperience.Outcome.SUCCESS);
    }

    /**
     * Logs the start of a career tour.
     *
     * @param career the career being joined
     */
    public void logCareerStart(Career career) {
        String narrative = character.getName()
                + " enlisted in the " + career.getName() + " service,"
                + " beginning a new chapter of their life.";

        log(ExperienceType.CAREER_START, "Joining the " + career.getName() + " Service",
                "Started serving in " + career.getName() + " career", narrative)
                .characterAge(character.getAge())
                .outcome(CharacterExperience.Outcome.SUCCESS);
    }

    /**
     * Logs the completion of a career tour.
     *
     * @param tour the completed tour
     */
    public void logCareerCompletion(TravellerCharacter.CareerTour tour) {
        StringBuilder skills = new StringBuilder();
        for (int i = 0; i < tour.getSkillsLearned().size(); i++) {
            if (i > 0) skills.append(", ");
            skills.append(tour.getSkillsLearned().get(i).getName());
        }

        String narrative = "After " + tour.getYearsServed() + " years of service,"
                + " reaching the rank of " + tour.getRank() + ","
                + " " + character.getName() + " learned: " + skills
                + ". They earned " + tour.getWealthEarned() + " credits.";

        log(ExperienceType.CAREER_COMPLETION,
                "Tour Complete — " + tour.getCareer().getName() + " (" + tour.getRank() + ")",
                "Completed " + tour.getYearsServed() + "-year tour in "
                        + tour.getCareer().getName() + " as " + tour.getRank()
                        + ". Skills: " + skills + ". Wealth: " + tour.getWealthEarned() + " credits",
                narrative)
                .characterAge(character.getAge())
                .outcome(CharacterExperience.Outcome.SUCCESS);
    }

    /**
     * Logs a promotion.
     *
     * @param oldRank the previous rank
     * @param newRank the new rank
     * @param career  the career
     */
    public void logPromotion(String oldRank, String newRank, Career career) {
        String narrative = character.getName()
                + " was promoted from " + oldRank + " to " + newRank
                + " in the " + career.getName() + " service."
                + " Their dedication and skill had not gone unnoticed.";

        log(ExperienceType.PROMOTION, "Promoted to " + newRank,
                "Promoted from " + oldRank + " to " + newRank + " in " + career.getName(),
                narrative)
                .characterAge(character.getAge())
                .outcome(CharacterExperience.Outcome.SUCCESS);
    }

    /**
     * Logs retirement from a career.
     *
     * @param career          the career retired from
     * @param rank            the final rank
     * @param retirementType  the type of retirement
     * @param totalWealth     total wealth at retirement
     */
    public void logRetirement(Career career, String rank,
                               TravellerCharacter.RetirementType retirementType, long totalWealth) {
        String narrative;
        CharacterExperience.Outcome outcome;

        switch (retirementType) {
            case HONORABLE:
                narrative = character.getName()
                        + " retired honorably from the " + career.getName()
                        + " service at the rank of " + rank
                        + ", with " + totalWealth + " credits to their name."
                        + " Their service had been exemplary.";
                outcome = CharacterExperience.Outcome.SUCCESS;
                break;
            case DISHONORABLE:
                narrative = character.getName()
                        + " was dishonorably discharged from the " + career.getName()
                        + " service. The circumstances remain unclear.";
                outcome = CharacterExperience.Outcome.FAILURE;
                break;
            case MEDICAL:
                narrative = character.getName()
                        + " was medically retired from the " + career.getName()
                        + " service. Injury or illness ended their career.";
                outcome = CharacterExperience.Outcome.MIXED;
                break;
            case DEAD:
                narrative = character.getName()
                        + " died while serving in the " + career.getName()
                        + " service at the rank of " + rank
                        + ". Their sacrifice will not be forgotten.";
                outcome = CharacterExperience.Outcome.FAILURE;
                break;
            default:
                narrative = character.getName()
                        + " retired from the " + career.getName()
                        + " service at the rank of " + rank
                        + ", with " + totalWealth + " credits.";
                outcome = CharacterExperience.Outcome.SUCCESS;
        }

        log(ExperienceType.RETIREMENT, "Retirement from " + career.getName(),
                "Retired from " + career.getName() + " as " + rank
                        + " (" + retirementType.getName() + "). Wealth: " + totalWealth + " credits",
                narrative)
                .characterAge(character.getAge())
                .outcome(outcome);
    }

    /**
     * Logs a battle experience.
     *
     * @param title     description of the battle
     * @param location  where it took place
     * @param outcome   the result
     * @param narrative the story of the battle
     */
    public void logBattle(String title, String location,
                           CharacterExperience.Outcome outcome, String narrative) {
        log(ExperienceType.BATTLE, title,
                "Battle at " + location + " — " + outcome.getDisplayName(), narrative)
                .location(location)
                .characterAge(character.getAge())
                .outcome(outcome);
    }

    /**
     * Logs a mission.
     *
     * @param title     mission description
     * @param location  where it took place
     * @param success   whether it succeeded
     * @param narrative the story
     */
    public void logMission(String title, String location, boolean success, String narrative) {
        ExperienceType type = success ? ExperienceType.MISSION_SUCCESS : ExperienceType.MISSION_FAILURE;
        log(type, title,
                (success ? "Successful mission: " : "Failed mission: ") + title + " at " + location,
                narrative)
                .location(location)
                .characterAge(character.getAge())
                .outcome(success ? CharacterExperience.Outcome.SUCCESS : CharacterExperience.Outcome.FAILURE);
    }

    /**
     * Logs an exploration event.
     *
     * @param title     what was explored
     * @param location  the world or system
     * @param narrative the story
     */
    public void logExploration(String title, String location, String narrative) {
        log(ExperienceType.EXPLORATION, title,
                "Explored " + location, narrative)
                .location(location)
                .characterAge(character.getAge())
                .outcome(CharacterExperience.Outcome.SUCCESS);
    }

    // ============================================================
    // Querying
    // ============================================================

    /**
     * Returns all experiences (immutable list).
     *
     * @return the experiences
     */
    public List<CharacterExperience> getExperiences() {
        return Collections.unmodifiableList(experiences);
    }

    /**
     * Returns experiences filtered by type.
     *
     * @param type the type to filter by
     * @return matching experiences
     */
    public List<CharacterExperience> getByType(ExperienceType type) {
        return experiences.stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Returns experiences filtered by outcome.
     *
     * @param outcome the outcome to filter by
     * @return matching experiences
     */
    public List<CharacterExperience> getByOutcome(CharacterExperience.Outcome outcome) {
        return experiences.stream()
                .filter(e -> e.getOutcome() == outcome)
                .collect(Collectors.toList());
    }

    /**
     * Returns experiences within a date range.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return matching experiences
     */
    public List<CharacterExperience> getByDateRange(LocalDate from, LocalDate to) {
        return experiences.stream()
                .filter(e -> e.getDate() != null
                        && !e.getDate().isBefore(from)
                        && !e.getDate().isAfter(to))
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of experiences logged.
     *
     * @return the count
     */
    public int getExperienceCount() {
        return experiences.size();
    }

    /**
     * Returns the associated character.
     *
     * @return the character
     */
    public TravellerCharacter getCharacter() {
        return character;
    }

    /**
     * Returns when this log was created.
     *
     * @return the creation date
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns a summary of the log.
     *
     * @return a summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Character Log: ").append(character.getName()).append("\n");
        sb.append("Total experiences: ").append(experiences.size()).append("\n");

        // Count by type
        long battles = experiences.stream().filter(e -> e.getType() == ExperienceType.BATTLE).count();
        long missions = experiences.stream()
                .filter(e -> e.getType() == ExperienceType.MISSION_SUCCESS
                        || e.getType() == ExperienceType.MISSION_FAILURE).count();
        long careers = experiences.stream()
                .filter(e -> e.getType() == ExperienceType.CAREER_START
                        || e.getType() == ExperienceType.CAREER_COMPLETION).count();

        if (battles > 0) sb.append("  Battles: ").append(battles).append("\n");
        if (missions > 0) sb.append("  Missions: ").append(missions).append("\n");
        if (careers > 0) sb.append("  Career events: ").append(careers).append("\n");

        return sb.toString();
    }

    @Override
    public String toString() {
        return "CharacterLog{character=" + character.getName()
                + ", experiences=" + experiences.size() + "}";
    }
}
