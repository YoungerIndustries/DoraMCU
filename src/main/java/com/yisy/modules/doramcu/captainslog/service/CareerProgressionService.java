package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.CareerProgressionResponse;
import com.yisy.modules.doramcu.captainslog.dto.CareerUpgradeRequest;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogFocus;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogValue;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogFocusRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implements the Career Progression system for Captain's Log.
 * <p>
 * Characters earn experience points (XP) from completing missions.
 * At career events (every 3rd mission), the player can spend XP on upgrades:
 * <ul>
 *   <li><b>Rank Advancement</b> — Move up one rank (costs XP based on current rank)</li>
 *   <li><b>Discipline Increase</b> — Raise a discipline rating by 1 (costs 3 XP)</li>
 *   <li><b>Add Focus</b> — Gain a new focus in a discipline (costs 2 XP)</li>
 *   <li><b>Add Value</b> — Gain a new core value (costs 2 XP, max 3 values)</li>
 *   <li><b>Retire Value</b> — Retire a value for narrative impact (free, grants 1 XP)</li>
 * </ul>
 * </p>
 */
@Component
public class CareerProgressionService {

    private static final Logger log = LoggerFactory.getLogger(CareerProgressionService.class);

    /** XP awarded for completing a mission. */
    public static final int XP_PER_MISSION = 2;

    /** XP cost to increase a discipline rating. */
    public static final int XP_DISCIPLINE_INCREASE = 3;

    /** XP cost to add a new focus. */
    public static final int XP_ADD_FOCUS = 2;

    /** XP cost to add a new value. */
    public static final int XP_ADD_VALUE = 2;

    /** XP granted for retiring a value. */
    public static final int XP_RETIRE_VALUE = 1;

    /** Maximum discipline rating. */
    public static final int MAX_DISCIPLINE = 4;

    /** Maximum number of values. */
    public static final int MAX_VALUES = 3;

    /** XP cost for rank advancement, indexed by rank ordinal. */
    private static final Map<CaptainLogCharacter.Rank, Integer> RANK_XP_COST = Map.of(
            CaptainLogCharacter.Rank.ENSIGN, 5,
            CaptainLogCharacter.Rank.LIEUTENANT_JUNIOR, 8,
            CaptainLogCharacter.Rank.LIEUTENANT, 12,
            CaptainLogCharacter.Rank.LIEUTENANT_COMMANDER, 18,
            CaptainLogCharacter.Rank.COMMANDER, 25,
            CaptainLogCharacter.Rank.CAPTAIN, 35
            // COMMANDING_OFFICER is the max rank
    );

    /** Suggested focus names for each discipline. */
    private static final Map<CaptainLogCharacter.Discipline, List<String>> SUGGESTED_FOCUSES = Map.of(
            CaptainLogCharacter.Discipline.COMMAND, Arrays.asList(
                    "Fleet Tactics", "Diplomacy", "Crisis Management", "Inspiration", "Negotiation"
            ),
            CaptainLogCharacter.Discipline.LEADERSHIP, Arrays.asList(
                    "Motivation", "Conflict Resolution", "Team Building", "Mentorship", "Public Speaking"
            ),
            CaptainLogCharacter.Discipline.SCIENCE, Arrays.asList(
                    "Astrophysics", "Biology", "Chemistry", "Physics", "Xenobiology"
            ),
            CaptainLogCharacter.Discipline.SECURITY, Arrays.asList(
                    "Hand-to-Hand Combat", "Firearms", "Investigation", "Interrogation", "Stealth"
            ),
            CaptainLogCharacter.Discipline.MEDICINE, Arrays.asList(
                    "Surgery", "Pharmacology", "Psychology", "Trauma Care", "Epidemiology"
            ),
            CaptainLogCharacter.Discipline.OPERATIONS, Arrays.asList(
                    "Navigation", "Piloting", "Engineering", "Communications", "Survival"
            )
    );

    private final CaptainLogCharacterRepository characterRepository;
    private final CaptainLogFocusRepository focusRepository;
    private final CaptainLogValueRepository valueRepository;

    public CareerProgressionService(CaptainLogCharacterRepository characterRepository,
                                     CaptainLogFocusRepository focusRepository,
                                     CaptainLogValueRepository valueRepository) {
        this.characterRepository = characterRepository;
        this.focusRepository = focusRepository;
        this.valueRepository = valueRepository;
    }

    /**
     * Awards experience points for completing a mission.
     */
    @Transactional
    public CaptainLogCharacter awardMissionXp(Long characterId) {
        return awardXp(characterId, XP_PER_MISSION);
    }

    /**
     * Awards a custom amount of experience points to a character.
     */
    @Transactional
    public CaptainLogCharacter awardXp(Long characterId, int amount) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        int previousXp = character.getExperiencePoints();
        character.setExperiencePoints(previousXp + amount);
        character = characterRepository.save(character);
        log.info("Awarded {} XP to {} (total: {})", amount, character.getName(), character.getExperiencePoints());
        return character;
    }

    /**
     * Spends experience points from a character.
     */
    @Transactional
    public CaptainLogCharacter spendXp(Long characterId, int amount) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        if (character.getExperiencePoints() < amount) {
            throw new IllegalArgumentException("Not enough XP. Need " + amount + ", have " + character.getExperiencePoints());
        }

        character.setExperiencePoints(character.getExperiencePoints() - amount);
        character = characterRepository.save(character);
        log.info("Spent {} XP from {} (remaining: {})", amount, character.getName(), character.getExperiencePoints());
        return character;
    }

    /**
     * Returns the current career progression status and available upgrades.
     */
    public CareerProgressionResponse getCareerStatus(Long characterId) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        CareerProgressionResponse response = new CareerProgressionResponse();
        response.setExperiencePoints(character.getExperiencePoints());

        // Calculate rank advancement
        CaptainLogCharacter.Rank currentRank = character.getRank();
        CaptainLogCharacter.Rank nextRank = getNextRank(currentRank);
        if (nextRank != null) {
            int cost = RANK_XP_COST.getOrDefault(currentRank, 999);
            response.setXpForNextRank(cost);
            response.setCanAdvanceRank(character.getExperiencePoints() >= cost);
        } else {
            response.setXpForNextRank(0);
            response.setCanAdvanceRank(false);
        }

        // Check discipline increase availability
        boolean canIncrease = false;
        for (CaptainLogCharacter.Discipline disc : CaptainLogCharacter.Discipline.values()) {
            if (character.getDisciplineRating(disc) < MAX_DISCIPLINE) {
                canIncrease = true;
                break;
            }
        }
        response.setCanIncreaseDiscipline(canIncrease && character.getExperiencePoints() >= XP_DISCIPLINE_INCREASE);

        // Check focus addition availability
        response.setCanAddFocus(character.getExperiencePoints() >= XP_ADD_FOCUS);

        // Build available upgrades list
        List<CareerProgressionResponse.CareerUpgrade> upgrades = new ArrayList<>();

        // Rank advancement
        if (nextRank != null && character.getExperiencePoints() >= RANK_XP_COST.getOrDefault(currentRank, 999)) {
            CareerProgressionResponse.CareerUpgrade upgrade = new CareerProgressionResponse.CareerUpgrade();
            upgrade.setType(CareerProgressionResponse.CareerUpgrade.UpgradeType.RANK_ADVANCEMENT);
            upgrade.setTitle("Advance to " + nextRank.name().replace('_', ' '));
            upgrade.setDescription("Promote from " + currentRank.name().replace('_', ' ') + " to " + nextRank.name().replace('_', ' '));
            upgrade.setXpCost(RANK_XP_COST.getOrDefault(currentRank, 999));
            upgrade.setNextRank(nextRank);
            upgrades.add(upgrade);
        }

        // Discipline increases
        for (CaptainLogCharacter.Discipline disc : CaptainLogCharacter.Discipline.values()) {
            int rating = character.getDisciplineRating(disc);
            if (rating < MAX_DISCIPLINE && character.getExperiencePoints() >= XP_DISCIPLINE_INCREASE) {
                CareerProgressionResponse.CareerUpgrade upgrade = new CareerProgressionResponse.CareerUpgrade();
                upgrade.setType(CareerProgressionResponse.CareerUpgrade.UpgradeType.DISCIPLINE_INCREASE);
                upgrade.setTitle("Increase " + disc.name());
                upgrade.setDescription("Raise " + disc.name() + " from " + rating + " to " + (rating + 1));
                upgrade.setXpCost(XP_DISCIPLINE_INCREASE);
                upgrade.setDiscipline(disc);
                upgrades.add(upgrade);
            }
        }

        // Add focus
        if (character.getExperiencePoints() >= XP_ADD_FOCUS) {
            CareerProgressionResponse.CareerUpgrade upgrade = new CareerProgressionResponse.CareerUpgrade();
            upgrade.setType(CareerProgressionResponse.CareerUpgrade.UpgradeType.ADD_FOCUS);
            upgrade.setTitle("Add a new focus");
            upgrade.setDescription("Gain a specialization in one of your disciplines");
            upgrade.setXpCost(XP_ADD_FOCUS);
            upgrades.add(upgrade);
        }

        // Add value
        if (character.getValues().size() < MAX_VALUES && character.getExperiencePoints() >= XP_ADD_VALUE) {
            CareerProgressionResponse.CareerUpgrade upgrade = new CareerProgressionResponse.CareerUpgrade();
            upgrade.setType(CareerProgressionResponse.CareerUpgrade.UpgradeType.ADD_VALUE);
            upgrade.setTitle("Add a new value");
            upgrade.setDescription("Define a new principle that guides your character");
            upgrade.setXpCost(XP_ADD_VALUE);
            upgrades.add(upgrade);
        }

        // Retire value
        if (!character.getValues().isEmpty()) {
            for (int i = 0; i < character.getValues().size(); i++) {
                CaptainLogValue value = character.getValues().get(i);
                CareerProgressionResponse.CareerUpgrade upgrade = new CareerProgressionResponse.CareerUpgrade();
                upgrade.setType(CareerProgressionResponse.CareerUpgrade.UpgradeType.RETIRE_VALUE);
                upgrade.setTitle("Retire: " + value.getDescription());
                upgrade.setDescription("Retire this value for narrative impact (grants " + XP_RETIRE_VALUE + " XP)");
                upgrade.setXpCost(0);
                upgrades.add(upgrade);
            }
        }

        response.setAvailableUpgrades(upgrades);
        response.setDescription("Character " + character.getName() + " has " + character.getExperiencePoints() + " XP available.");

        return response;
    }

    /**
     * Purchases a career upgrade, spending XP from the character.
     */
    @Transactional
    public CareerProgressionResponse purchaseUpgrade(CareerUpgradeRequest request) {
        CaptainLogCharacter character = characterRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + request.getCharacterId()));

        CareerProgressionResponse response = new CareerProgressionResponse();

        switch (request.getType()) {
            case RANK_ADVANCEMENT -> {
                CaptainLogCharacter.Rank nextRank = getNextRank(character.getRank());
                if (nextRank == null) {
                    throw new IllegalStateException("Character is already at maximum rank");
                }
                int cost = RANK_XP_COST.getOrDefault(character.getRank(), 999);
                if (character.getExperiencePoints() < cost) {
                    throw new IllegalArgumentException("Not enough XP. Need " + cost + ", have " + character.getExperiencePoints());
                }
                character.setExperiencePoints(character.getExperiencePoints() - cost);
                character.setRank(nextRank);
                character = characterRepository.save(character);
                response.setDescription("Promoted to " + nextRank.name().replace('_', ' ') + "!");
                log.info("Character {} promoted to {}", character.getName(), nextRank);
            }
            case DISCIPLINE_INCREASE -> {
                if (request.getDiscipline() == null) {
                    throw new IllegalArgumentException("Discipline is required for DISCIPLINE_INCREASE");
                }
                int current = character.getDisciplineRating(request.getDiscipline());
                if (current >= MAX_DISCIPLINE) {
                    throw new IllegalStateException(request.getDiscipline().name() + " is already at maximum rating");
                }
                if (character.getExperiencePoints() < XP_DISCIPLINE_INCREASE) {
                    throw new IllegalArgumentException("Not enough XP. Need " + XP_DISCIPLINE_INCREASE + ", have " + character.getExperiencePoints());
                }
                character.setExperiencePoints(character.getExperiencePoints() - XP_DISCIPLINE_INCREASE);
                setDisciplineRating(character, request.getDiscipline(), current + 1);
                character = characterRepository.save(character);
                response.setDescription(request.getDiscipline().name() + " increased to " + (current + 1) + "!");
                log.info("Character {} increased {} to {}", character.getName(), request.getDiscipline(), current + 1);
            }
            case ADD_FOCUS -> {
                if (request.getDiscipline() == null) {
                    throw new IllegalArgumentException("Discipline is required for ADD_FOCUS");
                }
                if (request.getFocusName() == null || request.getFocusName().isBlank()) {
                    throw new IllegalArgumentException("Focus name is required for ADD_FOCUS");
                }
                if (character.getExperiencePoints() < XP_ADD_FOCUS) {
                    throw new IllegalArgumentException("Not enough XP. Need " + XP_ADD_FOCUS + ", have " + character.getExperiencePoints());
                }
                character.setExperiencePoints(character.getExperiencePoints() - XP_ADD_FOCUS);
                CaptainLogFocus focus = new CaptainLogFocus();
                focus.setCharacter(character);
                focus.setDiscipline(CaptainLogFocus.Discipline.valueOf(request.getDiscipline().name()));
                focus.setName(request.getFocusName());
                focusRepository.save(focus);
                character.getFocuses().add(focus);
                character = characterRepository.save(character);
                response.setDescription("New focus '" + request.getFocusName() + "' added to " + request.getDiscipline().name() + "!");
                log.info("Character {} gained focus '{}' in {}", character.getName(), request.getFocusName(), request.getDiscipline());
            }
            case ADD_VALUE -> {
                if (request.getValueDescription() == null || request.getValueDescription().isBlank()) {
                    throw new IllegalArgumentException("Value description is required for ADD_VALUE");
                }
                if (character.getValues().size() >= MAX_VALUES) {
                    throw new IllegalStateException("Character already has maximum " + MAX_VALUES + " values");
                }
                if (character.getExperiencePoints() < XP_ADD_VALUE) {
                    throw new IllegalArgumentException("Not enough XP. Need " + XP_ADD_VALUE + ", have " + character.getExperiencePoints());
                }
                character.setExperiencePoints(character.getExperiencePoints() - XP_ADD_VALUE);
                CaptainLogValue value = new CaptainLogValue();
                value.setCharacter(character);
                value.setDescription(request.getValueDescription());
                valueRepository.save(value);
                character.getValues().add(value);
                character = characterRepository.save(character);
                response.setDescription("New value '" + request.getValueDescription() + "' added!");
                log.info("Character {} gained value '{}'", character.getName(), request.getValueDescription());
            }
            case RETIRE_VALUE -> {
                if (request.getValueIndex() == null) {
                    throw new IllegalArgumentException("Value index is required for RETIRE_VALUE");
                }
                if (request.getValueIndex() < 0 || request.getValueIndex() >= character.getValues().size()) {
                    throw new IllegalArgumentException("Invalid value index");
                }
                CaptainLogValue value = character.getValues().get(request.getValueIndex());
                String retiredDescription = value.getDescription();
                character.getValues().remove(value);
                valueRepository.delete(value);
                character.setExperiencePoints(character.getExperiencePoints() + XP_RETIRE_VALUE);
                character = characterRepository.save(character);
                response.setDescription("Value '" + retiredDescription + "' retired. Gained " + XP_RETIRE_VALUE + " XP.");
                log.info("Character {} retired value '{}'", character.getName(), retiredDescription);
            }
            default -> {
                throw new IllegalArgumentException("Unknown upgrade type: " + request.getType());
            }
        }

        response.setExperiencePoints(character.getExperiencePoints());
        return response;
    }

    /**
     * Returns suggested focus names for a discipline.
     */
    public List<String> getSuggestedFocuses(CaptainLogCharacter.Discipline discipline) {
        return SUGGESTED_FOCUSES.getOrDefault(discipline, List.of());
    }

    /**
     * Returns the next rank after the given rank, or null if at maximum.
     */
    private CaptainLogCharacter.Rank getNextRank(CaptainLogCharacter.Rank current) {
        CaptainLogCharacter.Rank[] ranks = CaptainLogCharacter.Rank.values();
        int index = current.ordinal();
        if (index < ranks.length - 1) {
            return ranks[index + 1];
        }
        return null;
    }

    /**
     * Sets a discipline rating on the character entity.
     */
    private void setDisciplineRating(CaptainLogCharacter character, CaptainLogCharacter.Discipline discipline, int rating) {
        switch (discipline) {
            case COMMAND -> character.setCommand(rating);
            case LEADERSHIP -> character.setLeadership(rating);
            case SCIENCE -> character.setScience(rating);
            case SECURITY -> character.setSecurity(rating);
            case MEDICINE -> character.setMedicine(rating);
            case OPERATIONS -> character.setOperations(rating);
        }
    }
}
