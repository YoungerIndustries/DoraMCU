package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.traveller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service layer for Traveller character creation and management.
 * <p>
 * Provides character generation, career service simulation, and skill acquisition
 * following Classic Traveller Book 1 rules.
 * </p>
 */
@Service
public class CharacterService {

    private static final Logger log = LoggerFactory.getLogger(CharacterService.class);
    private static final int TOUR_YEARS = 4;

    private final Map<Integer, TravellerCharacter> characters = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final Random random = new Random();

    /**
     * Creates a new character with randomly rolled abilities.
     *
     * @param name the character's name
     * @return the created character
     */
    public TravellerCharacter createCharacter(String name) {
        log.info("Creating character: {}", name);
        int id = nextId.getAndIncrement();
        TravellerCharacter character = new TravellerCharacter(id, name);
        character.rollAbilities();
        characters.put(id, character);
        return character;
    }

    /**
     * Creates a character with manually set abilities.
     *
     * @param name the character's name
     * @param abilities map of ability scores
     * @return the created character
     */
    public TravellerCharacter createCharacter(String name, Map<AbilityScore, Integer> abilities) {
        log.info("Creating character with manual abilities: {}", name);
        int id = nextId.getAndIncrement();
        TravellerCharacter character = new TravellerCharacter(id, name);
        for (Map.Entry<AbilityScore, Integer> entry : abilities.entrySet()) {
            character.setAbility(entry.getKey(), entry.getValue());
        }
        characters.put(id, character);
        return character;
    }

    /**
     * Gets a character by ID.
     *
     * @param id the character ID
     * @return the character, or null if not found
     */
    public TravellerCharacter getCharacter(int id) {
        return characters.get(id);
    }

    /**
     * Lists all characters.
     *
     * @return list of all characters
     */
    public List<TravellerCharacter> listCharacters() {
        return new ArrayList<>(characters.values());
    }

    /**
     * Simulates a career tour for a character.
     * <p>
     * Each tour lasts 4 years. The character may gain skills, wealth,
     * and promotions. At the end of each tour, a retirement check is made.
     * </p>
     *
     * @param characterId the character ID
     * @param career the career to serve
     * @return the career tour result
     */
    public TravellerCharacter.CareerTour serveCareerTour(int characterId, Career career) {
        TravellerCharacter character = characters.get(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }
        if (!character.isAlive()) {
            throw new IllegalStateException("Character is dead");
        }

        log.info("Character {} serves in {} for {} years", character.getName(), career.getName(), TOUR_YEARS);

        // Determine skills gained
        List<Skill> skillsLearned = new ArrayList<>();
        int skillRolls = 2; // Standard: 2 skill rolls per tour
        for (int i = 0; i < skillRolls; i++) {
            Skill skill = randomSkillFromCareer(career);
            if (!skillsLearned.contains(skill)) {
                skillsLearned.add(skill);
            }
        }

        // Determine rank
        String rank = determineRank(career, character);

        // Determine wealth (D6 x 1000 credits per tour)
        long wealthEarned = (random.nextInt(6) + 1) * 1000;

        // Retirement check
        boolean retired = checkRetirement(character);
        TravellerCharacter.RetirementType retirementType = retired ? determineRetirementType() : TravellerCharacter.RetirementType.NORMAL;

        if (retired && retirementType == TravellerCharacter.RetirementType.DEAD) {
            character.setAlive(false);
        }

        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                career, TOUR_YEARS, skillsLearned, rank, wealthEarned, retired, retirementType);
        character.addCareerTour(tour);

        return tour;
    }

    /**
     * Runs a full character generation with multiple career tours.
     * <p>
     * The character serves 2–4 careers before retiring to adventure.
     * </p>
     *
     * @param name the character's name
     * @return the fully generated character
     */
    public TravellerCharacter generateFullCharacter(String name) {
        TravellerCharacter character = createCharacter(name);

        // Serve 2–4 careers
        int numCareers = random.nextInt(3) + 2;
        for (int i = 0; i < numCareers; i++) {
            if (!character.isAlive()) {
                break;
            }

            // Pick a random career
            Career[] careers = Career.values();
            Career career = careers[random.nextInt(careers.length)];

            TravellerCharacter.CareerTour tour = serveCareerTour(character.getId(), career);

            // If retired, stop serving
            if (tour.isRetired()) {
                break;
            }
        }

        log.info("Generated character: {}", character);
        return character;
    }

    /**
     * Selects a random skill from a career's skill table.
     */
    private Skill randomSkillFromCareer(Career career) {
        List<Skill> table = career.getSkillTable();
        return table.get(random.nextInt(table.size()));
    }

    /**
     * Determines the rank achieved in a career.
     */
    private String determineRank(Career career, TravellerCharacter character) {
        // Simple rank determination based on ability scores
        int totalAbilities = 0;
        for (int value : character.getAbilities().values()) {
            totalAbilities += value;
        }

        String rank;
        if (totalAbilities >= 56) {
            rank = "Senior Officer";
        } else if (totalAbilities >= 49) {
            rank = "Junior Officer";
        } else if (totalAbilities >= 42) {
            rank = "NCO";
        } else {
            rank = "Enlisted";
        }
        return rank;
    }

    /**
     * Checks if the character retires from the current career.
     * <p>
     * Standard retirement check: roll 2D6, retire on 12+.
     * </p>
     */
    private boolean checkRetirement(TravellerCharacter character) {
        int roll = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
        return roll >= 12;
    }

    /**
     * Determines the type of retirement.
     */
    private TravellerCharacter.RetirementType determineRetirementType() {
        int roll = random.nextInt(6) + 1;
        switch (roll) {
            case 1:
                return TravellerCharacter.RetirementType.DISHONORABLE;
            case 2:
                return TravellerCharacter.RetirementType.MEDICAL;
            case 3:
                return TravellerCharacter.RetirementType.DEAD;
            case 4:
            case 5:
                return TravellerCharacter.RetirementType.NORMAL;
            case 6:
                return TravellerCharacter.RetirementType.HONORABLE;
            default:
                return TravellerCharacter.RetirementType.NORMAL;
        }
    }
}
