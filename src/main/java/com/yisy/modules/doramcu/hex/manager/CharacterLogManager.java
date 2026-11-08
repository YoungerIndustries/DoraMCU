package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.traveller.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages character experience logging and integrates with {@link CharacterManager}
 * to automatically record events during character creation and career progression.
 * <p>
 * This manager maintains a {@link CharacterLog} for each character and provides
 * methods to:
 * </p>
 * <ul>
 *   <li>Create characters with automatic logging of their creation</li>
 *   <li>Run career tours with detailed experience logging</li>
 *   <li>Log mission outcomes, battles, and other adventures</li>
 *   <li>Retrieve logs for book generation</li>
 * </ul>
 * <p>
 * Use {@link BookGenerator} to transform the logged experiences into a narrative book.
 * </p>
 */
public class CharacterLogManager {

    private final CharacterManager characterManager;
    private final GameContext context;
    private final Random random;
    private final Map<Integer, CharacterLog> logs;

    /**
     * Creates a new CharacterLogManager.
     *
     * @param context the shared game context
     */
    public CharacterLogManager(GameContext context) {
        this.context = context;
        this.characterManager = new CharacterManager(context);
        this.random = context.getRandom();
        this.logs = new ConcurrentHashMap<>();
    }

    // ============================================================
    // Character Creation with Logging
    // ============================================================

    /**
     * Creates a new character and automatically logs their creation.
     *
     * @param name     the character's name
     * @param homeworld the homeworld where they were born
     * @return the newly created character
     */
    public TravellerCharacter createCharacter(String name, String homeworld) {
        TravellerCharacter character = characterManager.createCharacter(name);
        CharacterLog log = new CharacterLog(character);
        logs.put(character.getId(), log);

        // Log the character creation
        log.logCharacterCreation(homeworld);

        return character;
    }

    /**
     * Creates a new character with specified abilities and logs their creation.
     *
     * @param name      the character's name
     * @param abilities map of ability scores
     * @param homeworld the homeworld where they were born
     * @param player    the player to assign as commander (optional)
     * @return the newly created character
     */
    public TravellerCharacter createCharacter(String name, Map<AbilityScore, Integer> abilities,
                                               String homeworld, Player player) {
        TravellerCharacter character = characterManager.createCharacter(name, abilities, player);
        CharacterLog log = new CharacterLog(character);
        logs.put(character.getId(), log);

        // Log the character creation
        log.logCharacterCreation(homeworld);

        return character;
    }

    /**
     * Generates a full character with random career and logs all events.
     *
     * @param name      the character's name
     * @param homeworld the homeworld where they were born
     * @return the fully generated character
     */
    public TravellerCharacter generateFullCharacter(String name, String homeworld) {
        TravellerCharacter character = createCharacter(name, homeworld);

        // Pick a random career
        Career[] careers = Career.values();
        Career career = careers[random.nextInt(careers.length)];

        // Serve the career with logging
        serveCareerWithLogging(character, career);

        return character;
    }

    /**
     * Generates a full character with a specified career and logs all events.
     *
     * @param name      the character's name
     * @param homeworld the homeworld where they were born
     * @param career    the career to serve
     * @return the fully generated character
     */
    public TravellerCharacter generateFullCharacter(String name, String homeworld, Career career) {
        TravellerCharacter character = createCharacter(name, homeworld);
        serveCareerWithLogging(character, career);
        return character;
    }

    // ============================================================
    // Career Progression with Logging
    // ============================================================

    /**
     * Runs a full career for the character with detailed experience logging.
     *
     * @param character the character to serve the career
     * @param career    the career to serve
     */
    public void serveCareerWithLogging(TravellerCharacter character, Career career) {
        CharacterLog log = getOrCreateLog(character);
        if (log == null) {
            return;
        }

        int terms = random.nextInt(CharacterManager.MIN_TERMS, CharacterManager.MAX_TERMS + 1);
        String rank = "Enlisted";
        String oldRank = rank;

        // Log career start
        log.logCareerStart(career);

        for (int i = 0; i < terms; i++) {
            // Check for duty death
            if (random.nextInt(20) == 0 && i < terms - 1) {
                // Killed in duty
                List<Skill> skills = generateSkills(career, 1);
                TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                        career, CharacterManager.YEARS_PER_TERM, skills, rank,
                        rollWealth(), false, TravellerCharacter.RetirementType.DEAD);
                character.addCareerTour(tour);
                character.setAlive(false);

                // Log the death
                log.logCareerCompletion(tour);
                log.log(ExperienceType.DEATH, "Killed in Duty",
                        character.getName() + " was killed during service in " + career.getName(),
                        character.getName() + " did not survive their " + (i + 1)
                                + "nd term of service. At the rank of " + rank
                                + ", they fell while serving in the " + career.getName()
                                + " at age " + character.getAge()
                                + ". Their story ends here, but their sacrifice echoes through the stars.");
                return;
            }

            // Generate skills for this term
            List<Skill> skills = generateSkills(career, random.nextInt(1, 3));

            // Determine promotion
            if (random.nextInt(6) == 0) {
                oldRank = rank;
                rank = promote(rank);
                // Log the promotion
                log.logPromotion(oldRank, rank, career);
            }

            // Roll wealth
            long wealth = rollWealth();

            // Check for reenlistment
            boolean retired = (i == terms - 1) || (random.nextInt(4) == 0);

            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    career, CharacterManager.YEARS_PER_TERM, skills, rank, wealth, retired,
                    retired ? TravellerCharacter.RetirementType.NORMAL : null);
            character.addCareerTour(tour);

            // Log the tour completion
            log.logCareerCompletion(tour);

            // Log skills learned
            for (Skill skill : skills) {
                log.log(ExperienceType.SKILL_GAIN, "Learned " + skill.getName(),
                        character.getName() + " gained the " + skill.getName()
                                + " skill during " + career.getName() + " service."
                                + " (" + skill.getDescription() + ")",
                        "During a quiet moment between assignments, " + character.getName()
                                + " mastered " + skill.getName().toLowerCase()
                                + ". The skill would prove "
                                + (random.nextBoolean() ? "invaluable in the years to come."
                                : "useful in unexpected situations.")
                                + " " + skill.getDescription() + " — this was now part of their expertise.");
            }

            if (retired) {
                // Log retirement
                TravellerCharacter.RetirementType retirementType =
                        determineRetirementType();
                log.logRetirement(career, rank, retirementType, character.getWealth());
                break;
            }
        }
    }

    /**
     * Runs a career with a specified number of terms and logs all events.
     *
     * @param character the character to serve the career
     * @param career    the career to serve
     * @param terms     the number of terms to serve
     */
    public void serveCareerWithLogging(TravellerCharacter character, Career career, int terms) {
        CharacterLog log = getOrCreateLog(character);
        if (log == null) {
            return;
        }

        log.logCareerStart(career);

        String rank = "Enlisted";
        String oldRank = rank;

        for (int i = 0; i < terms; i++) {
            if (random.nextInt(20) == 0 && i < terms - 1) {
                List<Skill> skills = generateSkills(career, 1);
                TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                        career, CharacterManager.YEARS_PER_TERM, skills, rank,
                        rollWealth(), false, TravellerCharacter.RetirementType.DEAD);
                character.addCareerTour(tour);
                character.setAlive(false);

                log.logCareerCompletion(tour);
                log.log(ExperienceType.DEATH, "Killed in Duty",
                        character.getName() + " was killed during service in " + career.getName(),
                        character.getName() + " fell during their " + (i + 1)
                                + "nd term. Their story ends at age " + character.getAge() + ".");
                return;
            }

            List<Skill> skills = generateSkills(career, random.nextInt(1, 3));

            if (random.nextInt(6) == 0) {
                oldRank = rank;
                rank = promote(rank);
                log.logPromotion(oldRank, rank, career);
            }

            long wealth = rollWealth();
            boolean retired = (i == terms - 1) || (random.nextInt(4) == 0);

            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    career, CharacterManager.YEARS_PER_TERM, skills, rank, wealth, retired,
                    retired ? TravellerCharacter.RetirementType.NORMAL : null);
            character.addCareerTour(tour);

            log.logCareerCompletion(tour);

            for (Skill skill : skills) {
                log.log(ExperienceType.SKILL_GAIN, "Learned " + skill.getName(),
                        character.getName() + " gained the " + skill.getName() + " skill.",
                        character.getName() + " learned " + skill.getName().toLowerCase()
                                + " during this term of service.");
            }

            if (retired) {
                TravellerCharacter.RetirementType retirementType = determineRetirementType();
                log.logRetirement(career, rank, retirementType, character.getWealth());
                break;
            }
        }
    }

    // ============================================================
    // Mission and Adventure Logging
    // ============================================================

    /**
     * Logs a battle experience for a character.
     *
     * @param character the character
     * @param title     description of the battle
     * @param location  where it took place
     * @param outcome   the result
     * @param narrative the story of the battle
     */
    public void logBattle(TravellerCharacter character, String title, String location,
                           CharacterExperience.Outcome outcome, String narrative) {
        CharacterLog log = getOrCreateLog(character);
        if (log != null) {
            log.logBattle(title, location, outcome, narrative);
        }
    }

    /**
     * Logs a mission for a character.
     *
     * @param character the character
     * @param title     mission description
     * @param location  where it took place
     * @param success   whether it succeeded
     * @param narrative the story
     */
    public void logMission(TravellerCharacter character, String title, String location,
                            boolean success, String narrative) {
        CharacterLog log = getOrCreateLog(character);
        if (log != null) {
            log.logMission(title, location, success, narrative);
        }
    }

    /**
     * Logs an exploration event for a character.
     *
     * @param character the character
     * @param title     what was explored
     * @param location  the world or system
     * @param narrative the story
     */
    public void logExploration(TravellerCharacter character, String title,
                                 String location, String narrative) {
        CharacterLog log = getOrCreateLog(character);
        if (log != null) {
            log.logExploration(title, location, narrative);
        }
    }

    /**
     * Logs a custom experience for a character.
     *
     * @param character the character
     * @param type      the experience type
     * @param title     a short headline
     * @param description factual summary
     * @param narrative  rich prose for the book
     */
    public void logExperience(TravellerCharacter character, ExperienceType type,
                               String title, String description, String narrative) {
        CharacterLog log = getOrCreateLog(character);
        if (log != null) {
            log.log(type, title, description, narrative);
        }
    }

    // ============================================================
    // Log Retrieval
    // ============================================================

    /**
     * Returns the log for a character.
     *
     * @param character the character
     * @return the log, or null if no log exists
     */
    public CharacterLog getLog(TravellerCharacter character) {
        return logs.get(character.getId());
    }

    /**
     * Returns the log for a character by ID.
     *
     * @param id the character ID
     * @return the log, or null if no log exists
     */
    public CharacterLog getLog(int id) {
        return logs.get(id);
    }

    /**
     * Gets an existing log or creates a new one for the character.
     *
     * @param character the character
     * @return the log
     */
    public CharacterLog getOrCreateLog(TravellerCharacter character) {
        return logs.computeIfAbsent(character.getId(), id -> new CharacterLog(character));
    }

    /**
     * Returns all logs (immutable view).
     *
     * @return the logs map
     */
    public Map<Integer, CharacterLog> getAllLogs() {
        return Collections.unmodifiableMap(logs);
    }

    /**
     * Returns the underlying CharacterManager for direct access.
     *
     * @return the character manager
     */
    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Generates skills for a career term.
     */
    private List<Skill> generateSkills(Career career, int count) {
        List<Skill> skills = new ArrayList<>();
        List<Skill> skillTable = career.getSkillTable();

        for (int i = 0; i < count; i++) {
            Skill skill = skillTable.get(random.nextInt(skillTable.size()));
            if (!skills.contains(skill)) {
                skills.add(skill);
            }
        }

        return skills;
    }

    /**
     * Promotes a rank to the next level.
     */
    private String promote(String currentRank) {
        return switch (currentRank) {
            case "Enlisted" -> "Senior Enlisted";
            case "Senior Enlisted" -> "Junior Officer";
            case "Junior Officer" -> "Officer";
            case "Officer" -> "Senior Officer";
            case "Senior Officer" -> "Commanding Officer";
            default -> "Commanding Officer";
        };
    }

    /**
     * Rolls wealth using 3D6 × 1000.
     */
    private long rollWealth() {
        long wealth = 0;
        for (int i = 0; i < 3; i++) {
            wealth += (random.nextInt(6) + 1);
        }
        return wealth * 1000;
    }

    /**
     * Determines a random retirement type.
     */
    private TravellerCharacter.RetirementType determineRetirementType() {
        int roll = random.nextInt(100);
        if (roll < 50) {
            return TravellerCharacter.RetirementType.NORMAL;
        } else if (roll < 70) {
            return TravellerCharacter.RetirementType.HONORABLE;
        } else if (roll < 85) {
            return TravellerCharacter.RetirementType.MEDICAL;
        } else if (roll < 95) {
            return TravellerCharacter.RetirementType.DISHONORABLE;
        } else {
            return TravellerCharacter.RetirementType.NORMAL;
        }
    }
}
