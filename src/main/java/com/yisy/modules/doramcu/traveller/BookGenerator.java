package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generates a narrative book from a {@link CharacterLog}.
 * <p>
 * The book is organized into chapters grouped by life phases:
 * </p>
 * <ul>
 *   <li><b>Prologue</b> — character creation and early life</li>
 *   <li><b>Career chapters</b> — one per career served, with sub-sections for each tour</li>
 *   <li><b>Adventure chapters</b> — missions, battles, exploration, and other experiences</li>
 *   <li><b>Epilogue</b> — final summary and character status</li>
 * </ul>
 * <p>
 * Each chapter contains the narrative text from the logged experiences,
 * with chapter headings, dates, and location context.
 * </p>
 * <p>
 * Usage:
 * </p>
 * <pre>
 * CharacterLog log = ...;
 * BookGenerator generator = new BookGenerator();
 * String book = generator.generateBook(log);
 * </pre>
 */
public class BookGenerator {

    /**
     * Generates a complete narrative book from a character log.
     *
     * @param log the character's experience log
     * @return the full book as a formatted string
     */
    public String generateBook(CharacterLog log) {
        Objects.requireNonNull(log, "Log must not be null");

        TravellerCharacter character = log.getCharacter();
        List<CharacterExperience> experiences = log.getExperiences();

        if (experiences.isEmpty()) {
            return "The story of " + character.getName()
                    + " has not yet been written.\n";
        }

        StringBuilder book = new StringBuilder();

        // Title page
        appendTitlePage(book, character);

        // Table of contents
        appendTableOfContents(book, log);

        // Chapters
        appendChapters(book, log);

        // Epilogue
        appendEpilogue(book, character, log);

        return book.toString();
    }

    /**
     * Generates a summary book — shorter version with key highlights.
     *
     * @param log the character's experience log
     * @return a condensed book
     */
    public String generateSummaryBook(CharacterLog log) {
        Objects.requireNonNull(log, "Log must not be null");

        TravellerCharacter character = log.getCharacter();
        List<CharacterExperience> experiences = log.getExperiences();

        if (experiences.isEmpty()) {
            return character.getName() + " — No experiences recorded.\n";
        }

        StringBuilder book = new StringBuilder();

        // Short title
        book.append("========================================\n");
        book.append("  ").append(character.getName()).append("\n");
        book.append("  A Brief Chronicle\n");
        book.append("========================================\n\n");

        // Character summary
        book.append("AGE: ").append(character.getAge()).append("\n");
        book.append("STATUS: ").append(character.isAlive() ? "Alive" : "Deceased").append("\n");
        book.append("WEALTH: ").append(character.getWealth()).append(" credits\n");

        Career primaryCareer = character.getPrimaryCareer();
        if (primaryCareer != null) {
            book.append("PRIMARY CAREER: ").append(primaryCareer.getName()).append("\n");
        }

        book.append("\n--- ABILITIES ---\n");
        for (AbilityScore ability : AbilityScore.values()) {
            book.append("  ").append(ability.getAbbreviation()).append(": ")
                    .append(character.getAbility(ability)).append("\n");
        }

        book.append("\n--- SKILLS (").append(character.getSkills().size()).append(") ---\n");
        for (Skill skill : character.getSkills()) {
            book.append("  - ").append(skill.getName()).append("\n");
        }

        book.append("\n--- KEY EVENTS ---\n");
        for (CharacterExperience exp : experiences) {
            if (!exp.getNarrative().isEmpty()) {
                book.append("  ").append(exp.getSummary()).append("\n");
            }
        }

        book.append("\n========================================\n");
        book.append("  End of Chronicle\n");
        book.append("========================================\n");

        return book.toString();
    }

    // ============================================================
    // Book Sections
    // ============================================================

    /**
     * Appends the title page.
     */
    private void appendTitlePage(StringBuilder book, TravellerCharacter character) {
        book.append("========================================\n");
        book.append("  THE CHRONICLES OF\n");
        book.append("  ").append(character.getName()).append("\n");
        book.append("========================================\n\n");

        book.append("A Traveller's Tale\n");
        book.append("From Birth to ").append(character.isAlive() ? "Today" : "Death").append("\n\n");

        book.append("Character ID: ").append(character.getId()).append("\n");
        book.append("Age: ").append(character.getAge()).append("\n");
        book.append("Status: ").append(character.isAlive() ? "Alive and Adventuring" : "Deceased").append("\n");
        book.append("Wealth: ").append(character.getWealth()).append(" credits\n\n");

        // Abilities
        book.append("--- ABILITIES ---\n");
        for (AbilityScore ability : AbilityScore.values()) {
            book.append("  ").append(ability.getFullName()).append(" (").append(ability.getAbbreviation()).append("): ")
                    .append(character.getAbility(ability)).append("\n");
        }
        book.append("\n");

        // Skills
        if (!character.getSkills().isEmpty()) {
            book.append("--- SKILLS ---\n");
            for (Skill skill : character.getSkills()) {
                book.append("  - ").append(skill.getName()).append(": ").append(skill.getDescription()).append("\n");
            }
            book.append("\n");
        }

        book.append("\n");
    }

    /**
     * Appends the table of contents.
     */
    private void appendTableOfContents(StringBuilder book, CharacterLog log) {
        book.append("========================================\n");
        book.append("  TABLE OF CONTENTS\n");
        book.append("========================================\n\n");

        int chapterNumber = 1;

        // Group experiences by life phase
        List<CharacterExperience> creationExperiences = log.getByType(ExperienceType.CHARACTER_CREATION);
        if (!creationExperiences.isEmpty()) {
            book.append("  Chapter ").append(chapterNumber).append(": Prologue — Birth and Abilities\n");
            chapterNumber++;
        }

        // Career chapters
        List<CharacterExperience> careerStarts = log.getByType(ExperienceType.CAREER_START);
        for (CharacterExperience careerStart : careerStarts) {
            book.append("  Chapter ").append(chapterNumber).append(": ")
                    .append(careerStart.getTitle()).append("\n");
            chapterNumber++;
        }

        // Adventure chapter (if there are non-career experiences)
        long adventureCount = log.getExperiences().stream()
                .filter(e -> isAdventureType(e.getType()))
                .count();
        if (adventureCount > 0) {
            book.append("  Chapter ").append(chapterNumber).append(": Adventures Among the Stars\n");
            chapterNumber++;
        }

        book.append("  Epilogue — ").append(log.getCharacter().isAlive() ? "The Road Ahead" : "A Life Remembered").append("\n\n");

        book.append("========================================\n\n\n");
    }

    /**
     * Appends all chapters.
     */
    private void appendChapters(StringBuilder book, CharacterLog log) {
        int chapterNumber = 1;

        // Prologue — character creation
        List<CharacterExperience> creationExperiences = log.getByType(ExperienceType.CHARACTER_CREATION);
        if (!creationExperiences.isEmpty()) {
            book.append("========================================\n");
            book.append("  CHAPTER ").append(chapterNumber).append(": PROLOGUE\n");
            book.append("  ").append(ExperienceType.CHARACTER_CREATION.getSubtitle()).append("\n");
            book.append("========================================\n\n");

            for (CharacterExperience exp : creationExperiences) {
                appendExperience(book, exp);
            }

            book.append("\n\n");
            chapterNumber++;
        }

        // Career chapters — group by career
        Map<Career, List<CharacterExperience>> careerExperiences = groupByCareer(log);
        for (Map.Entry<Career, List<CharacterExperience>> entry : careerExperiences.entrySet()) {
            Career career = entry.getKey();
            List<CharacterExperience> exps = entry.getValue();

            book.append("========================================\n");
            book.append("  CHAPTER ").append(chapterNumber).append(": ")
                    .append(career.getName()).append(" SERVICE\n");
            book.append("  ").append(career.getDescription()).append("\n");
            book.append("========================================\n\n");

            for (CharacterExperience exp : exps) {
                appendExperience(book, exp);
            }

            book.append("\n\n");
            chapterNumber++;
        }

        // Adventure chapter
        List<CharacterExperience> adventures = log.getExperiences().stream()
                .filter(e -> isAdventureType(e.getType()))
                .toList();

        if (!adventures.isEmpty()) {
            book.append("========================================\n");
            book.append("  CHAPTER ").append(chapterNumber).append(": ADVENTURES AMONG THE STARS\n");
            book.append("  Beyond the Call of Duty\n");
            book.append("========================================\n\n");

            for (CharacterExperience exp : adventures) {
                appendExperience(book, exp);
            }

            book.append("\n\n");
        }
    }

    /**
     * Appends the epilogue.
     */
    private void appendEpilogue(StringBuilder book, TravellerCharacter character, CharacterLog log) {
        book.append("========================================\n");
        book.append("  EPILOGUE\n");
        if (character.isAlive()) {
            book.append("  The Road Ahead\n");
        } else {
            book.append("  A Life Remembered\n");
        }
        book.append("========================================\n\n");

        if (character.isAlive()) {
            book.append(character.getName())
                    .append(" stands at age ").append(character.getAge())
                    .append(", with ").append(log.getExperienceCount())
                    .append(" recorded experiences behind them.\n\n");

            book.append("With ").append(character.getWealth()).append(" credits in the bank")
                    .append(" and ").append(character.getSkills().size()).append(" skills mastered,")
                    .append(" the stars still hold many secrets yet to be uncovered.\n\n");

            Career primaryCareer = character.getPrimaryCareer();
            if (primaryCareer != null) {
                book.append("A veteran of the ").append(primaryCareer.getName())
                        .append(" service, ").append(character.getName())
                        .append("'s story is far from over.\n");
            }

            book.append("\nThe adventure continues...\n");
        } else {
            book.append(character.getName())
                    .append("'s story ended at age ").append(character.getAge())
                    .append(". Through ").append(log.getExperienceCount())
                    .append(" recorded experiences, they lived a life of ").append(getLifeDescription(log)).append(".\n\n");

            book.append("Their skills in ");
            boolean first = true;
            for (Skill skill : character.getSkills()) {
                if (!first) book.append(", ");
                book.append(skill.getName().toLowerCase());
                first = false;
            }
            book.append(" will be remembered by those who knew them.\n\n");

            book.append("Rest in peace, ").append(character.getName()).append(".\n");
        }

        book.append("\n========================================\n");
        book.append("  END OF CHRONICLE\n");
        book.append("========================================\n");
    }

    /**
     * Appends a single experience to the book.
     */
    private void appendExperience(StringBuilder book, CharacterExperience exp) {
        // Section heading
        book.append("--- ").append(exp.getTitle()).append(" ---\n");

        // Date and location
        if (exp.getDate() != null || !exp.getLocation().isEmpty()) {
            book.append("\n");
            if (exp.getDate() != null) {
                book.append("Date: ").append(exp.getFormattedDate()).append("\n");
            }
            if (!exp.getLocation().isEmpty()) {
                book.append("Location: ").append(exp.getLocation()).append("\n");
            }
        }

        // Age context
        if (exp.getCharacterAge() > 0) {
            book.append("Age: ").append(exp.getCharacterAge()).append("\n");
        }

        book.append("\n");

        // Narrative (primary text for the book)
        if (!exp.getNarrative().isEmpty()) {
            book.append(exp.getNarrative()).append("\n\n");
        } else if (!exp.getDescription().isEmpty()) {
            // Fall back to description if no narrative
            book.append(exp.getDescription()).append("\n\n");
        }

        // Outcome
        if (exp.getOutcome() != CharacterExperience.Outcome.UNKNOWN) {
            book.append("[Outcome: ").append(exp.getOutcome().getDisplayName()).append("]\n\n");
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Groups experiences by career.
     */
    private Map<Career, List<CharacterExperience>> groupByCareer(CharacterLog log) {
        Map<Career, List<CharacterExperience>> grouped = new LinkedHashMap<>();

        for (CharacterExperience exp : log.getExperiences()) {
            // Try to determine the career from the experience
            Career career = determineCareer(exp);
            if (career != null) {
                grouped.computeIfAbsent(career, k -> new ArrayList<>()).add(exp);
            }
        }

        return grouped;
    }

    /**
     * Determines which career an experience belongs to.
     */
    private Career determineCareer(CharacterExperience exp) {
        String title = exp.getTitle().toLowerCase();

        for (Career career : Career.values()) {
            if (title.contains(career.getName().toLowerCase())) {
                return career;
            }
        }

        // Check description
        String desc = exp.getDescription().toLowerCase();
        for (Career career : Career.values()) {
            if (desc.contains(career.getName().toLowerCase())) {
                return career;
            }
        }

        // Career-related types default to the first career found in the title
        if (isCareerType(exp.getType())) {
            // Try to extract career name from title
            for (Career career : Career.values()) {
                if (exp.getTitle().contains(career.getName())) {
                    return career;
                }
            }
        }

        return null;
    }

    /**
     * Checks if an experience type is career-related.
     */
    private boolean isCareerType(ExperienceType type) {
        return type == ExperienceType.CAREER_START
                || type == ExperienceType.CAREER_COMPLETION
                || type == ExperienceType.SKILL_GAIN
                || type == ExperienceType.PROMOTION
                || type == ExperienceType.RETIREMENT;
    }

    /**
     * Checks if an experience type is an adventure (non-career).
     */
    private boolean isAdventureType(ExperienceType type) {
        return type == ExperienceType.BATTLE
                || type == ExperienceType.MISSION_SUCCESS
                || type == ExperienceType.MISSION_FAILURE
                || type == ExperienceType.EXPLORATION
                || type == ExperienceType.TRADE
                || type == ExperienceType.SOCIAL
                || type == ExperienceType.INJURY
                || type == ExperienceType.NARROW_ESCAPE
                || type == ExperienceType.DISCOVERY
                || type == ExperienceType.BETRAYAL
                || type == ExperienceType.HEROISM
                || type == ExperienceType.REFLECTION
                || type == ExperienceType.RELATIONSHIP
                || type == ExperienceType.ACQUISITION
                || type == ExperienceType.LOSS
                || type == ExperienceType.OTHER;
    }

    /**
     * Generates a description of the character's life based on their experiences.
     */
    private String getLifeDescription(CharacterLog log) {
        long battles = log.getByType(ExperienceType.BATTLE).size();
        long successes = log.getByType(ExperienceType.MISSION_SUCCESS).size();
        long explorations = log.getByType(ExperienceType.EXPLORATION).size();
        long heroisms = log.getByType(ExperienceType.HEROISM).size();

        if (battles > 3) return "war and sacrifice";
        if (explorations > 3) return "discovery and wonder";
        if (heroisms > 0) return "courage and honor";
        if (successes > battles) return "triumph and adventure";
        return "service and dedication";
    }
}
