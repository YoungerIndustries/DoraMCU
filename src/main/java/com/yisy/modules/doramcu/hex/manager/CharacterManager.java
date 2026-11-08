package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.Career;
import com.yisy.modules.doramcu.traveller.Skill;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages Traveller character creation, career progression, and skill acquisition.
 * <p>
 * This manager handles the full lifecycle of a Traveller character:
 * </p>
 * <ul>
 *   <li>Character creation with ability score rolling</li>
 *   <li>Career term generation with assignments, promotions, and skills</li>
 *   <li>Retirement processing</li>
 *   <li>Commander bonus calculations for empire operations</li>
 * </ul>
 * <p>
 * Characters created here serve as commanders for {@link Player} entities,
 * providing career-based bonuses to empire operations.
 * </p>
 */
public class CharacterManager {

    private final GameContext context;
    private final Random random;

    /** Years per career term. */
    public static final int YEARS_PER_TERM = 4;

    /** Minimum terms of service. */
    public static final int MIN_TERMS = 2;

    /** Maximum terms of service. */
    public static final int MAX_TERMS = 4;

    /**
     * Creates a new CharacterManager.
     *
     * @param context the shared game context
     */
    public CharacterManager(GameContext context) {
        this.context = context;
        this.random = context.getRandom();
    }

    // ============================================================
    // Character Creation
    // ============================================================

    /**
     * Creates a new Traveller character with randomly rolled abilities.
     *
     * @param name the character's name
     * @return the newly created character
     */
    public TravellerCharacter createCharacter(String name) {
        return createCharacter(name, null);
    }

    /**
     * Creates a new Traveller character with randomly rolled abilities
     * and assigns them as the commander of the given player.
     *
     * @param name the character's name
     * @param player the player to assign as commander (optional)
     * @return the newly created character
     */
    public TravellerCharacter createCharacter(String name, Player player) {
        int id = random.nextInt(10000, 99999);
        TravellerCharacter character = new TravellerCharacter(id, name);
        character.rollAbilities();

        if (player != null) {
            player.setCommander(character);
        }

        return character;
    }

    /**
     * Creates a new Traveller character with specified ability scores.
     *
     * @param name the character's name
     * @param abilities map of ability scores to set
     * @param player the player to assign as commander (optional)
     * @return the newly created character
     */
    public TravellerCharacter createCharacter(String name, java.util.Map<AbilityScore, Integer> abilities, Player player) {
        int id = random.nextInt(10000, 99999);
        TravellerCharacter character = new TravellerCharacter(id, name);

        if (abilities != null) {
            for (java.util.Map.Entry<AbilityScore, Integer> entry : abilities.entrySet()) {
                character.setAbility(entry.getKey(), entry.getValue());
            }
        } else {
            character.rollAbilities();
        }

        if (player != null) {
            player.setCommander(character);
        }

        return character;
    }

    // ============================================================
    // Career Progression
    // ============================================================

    /**
     * Runs a full career for the character, generating multiple terms.
     * <p>
     * The number of terms is determined randomly between {@link #MIN_TERMS}
     * and {@link #MAX_TERMS}. Each term grants skills, wealth, and may
     * result in promotion.
     * </p>
     *
     * @param character the character to serve the career
     * @param career the career to serve
     */
    public void serveCareer(TravellerCharacter character, Career career) {
        int terms = random.nextInt(MIN_TERMS, MAX_TERMS + 1);
        String rank = "Enlisted";

        for (int i = 0; i < terms; i++) {
            // Check for duty death (1 in 20 chance per term)
            if (random.nextInt(20) == 0 && i < terms - 1) {
                // Killed in duty — add final term and stop
                TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                        career, YEARS_PER_TERM, generateSkills(career, 1), rank,
                        rollWealth(), false, TravellerCharacter.RetirementType.DEAD);
                character.addCareerTour(tour);
                character.setAlive(false);
                return;
            }

            // Generate skills for this term
            List<Skill> skills = generateSkills(career, random.nextInt(1, 3));

            // Determine promotion
            if (random.nextInt(6) == 0) {
                rank = promote(rank);
            }

            // Roll wealth
            long wealth = rollWealth();

            // Check for reenlistment
            boolean retired = (i == terms - 1) || (random.nextInt(4) == 0);

            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    career, YEARS_PER_TERM, skills, rank, wealth, retired,
                    retired ? TravellerCharacter.RetirementType.NORMAL : null);
            character.addCareerTour(tour);

            if (retired) {
                break;
            }
        }
    }

    /**
     * Runs a full career for the character with a specified number of terms.
     *
     * @param character the character to serve the career
     * @param career the career to serve
     * @param terms the number of terms to serve
     */
    public void serveCareer(TravellerCharacter character, Career career, int terms) {
        String rank = "Enlisted";

        for (int i = 0; i < terms; i++) {
            if (random.nextInt(20) == 0 && i < terms - 1) {
                TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                        career, YEARS_PER_TERM, generateSkills(career, 1), rank,
                        rollWealth(), false, TravellerCharacter.RetirementType.DEAD);
                character.addCareerTour(tour);
                character.setAlive(false);
                return;
            }

            List<Skill> skills = generateSkills(career, random.nextInt(1, 3));

            if (random.nextInt(6) == 0) {
                rank = promote(rank);
            }

            long wealth = rollWealth();
            boolean retired = (i == terms - 1) || (random.nextInt(4) == 0);

            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    career, YEARS_PER_TERM, skills, rank, wealth, retired,
                    retired ? TravellerCharacter.RetirementType.NORMAL : null);
            character.addCareerTour(tour);

            if (retired) {
                break;
            }
        }
    }

    /**
     * Generates a full character with random career.
     * <p>
     * This is a convenience method that creates a character, rolls abilities,
     * and serves a random career.
     * </p>
     *
     * @param name the character's name
     * @return the fully generated character
     */
    public TravellerCharacter generateFullCharacter(String name) {
        return generateFullCharacter(name, null);
    }

    /**
     * Generates a full character with random career and assigns as commander.
     *
     * @param name the character's name
     * @param player the player to assign as commander (optional)
     * @return the fully generated character
     */
    public TravellerCharacter generateFullCharacter(String name, Player player) {
        TravellerCharacter character = createCharacter(name, player);

        // Pick a random career
        Career[] careers = Career.values();
        Career career = careers[random.nextInt(careers.length)];

        // Serve the career
        serveCareer(character, career);

        return character;
    }

    /**
     * Generates a full character with a specified career.
     *
     * @param name the character's name
     * @param career the career to serve
     * @param player the player to assign as commander (optional)
     * @return the fully generated character
     */
    public TravellerCharacter generateFullCharacter(String name, Career career, Player player) {
        TravellerCharacter character = createCharacter(name, player);
        serveCareer(character, career);
        return character;
    }

    // ============================================================
    // Commander Bonus Calculations
    // ============================================================

    /**
     * Calculates the army combat bonus for a player's commander.
     *
     * @param player the player
     * @return the die modifier for ground combat
     */
    public double getArmyCombatBonus(Player player) {
        return player.getCommanderBonus(Player.CommanderBonusType.ARMY_COMBAT);
    }

    /**
     * Calculates the ship evasion bonus for a player's commander.
     *
     * @param player the player
     * @return the evasion percentage bonus
     */
    public double getShipEvasionBonus(Player player) {
        return player.getCommanderBonus(Player.CommanderBonusType.SHIP_EVASION);
    }

    /**
     * Calculates the ship damage bonus for a player's commander.
     *
     * @param player the player
     * @return the damage percentage bonus
     */
    public double getShipDamageBonus(Player player) {
        return player.getCommanderBonus(Player.CommanderBonusType.SHIP_DAMAGE);
    }

    /**
     * Calculates the survey speed bonus for a player's commander.
     *
     * @param player the player
     * @return the survey speed percentage bonus
     */
    public double getSurveySpeedBonus(Player player) {
        return player.getCommanderBonus(Player.CommanderBonusType.SURVEY_SPEED);
    }

    /**
     * Calculates the trade profit bonus for a player's commander.
     *
     * @param player the player
     * @return the trade profit percentage bonus
     */
    public double getTradeProfitBonus(Player player) {
        return player.getCommanderBonus(Player.CommanderBonusType.TRADE_PROFIT);
    }

    /**
     * Calculates the government/diplomacy bonus for a player's commander.
     *
     * @param player the player
     * @return the diplomacy die modifier
     */
    public double getGovernmentBonus(Player player) {
        return player.getCommanderBonus(Player.CommanderBonusType.GOVERNMENT);
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Generates skills for a career term.
     *
     * @param career the career
     * @param count the number of skills to generate
     * @return list of generated skills
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
     *
     * @param currentRank the current rank
     * @return the new rank
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
     *
     * @return the wealth amount in credits
     */
    private long rollWealth() {
        long wealth = 0;
        for (int i = 0; i < 3; i++) {
            wealth += (random.nextInt(6) + 1);
        }
        return wealth * 1000;
    }
}
