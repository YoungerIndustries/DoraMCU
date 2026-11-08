package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.captainslog.dto.CreateStarshipRequest;
import com.yisy.modules.doramcu.captainslog.model.*;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogStarshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Handles character and starship CRUD operations for the Captain's Log system.
 * <p>
 * Extracted from {@link CaptainLogService} to isolate character and starship
 * management from mission, dice, and AI logic.
 * </p>
 */
@Component
public class CaptainLogCharacterService {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogCharacterService.class);

    private final CaptainLogCharacterRepository characterRepository;
    private final CaptainLogStarshipRepository starshipRepository;
    private final Random random = new Random();

    public CaptainLogCharacterService(CaptainLogCharacterRepository characterRepository,
                            CaptainLogStarshipRepository starshipRepository) {
        this.characterRepository = characterRepository;
        this.starshipRepository = starshipRepository;
    }

    // ==================== Character Management ====================

    /**
     * Creates a new character, auto-rolling attributes and disciplines if not provided.
     */
    @Transactional
    public CaptainLogCharacter createCharacter(CreateCharacterRequest request) {
        log.info("Creating Captain's Log character: {}", request.getName());

        CaptainLogCharacter character = new CaptainLogCharacter();
        character.setName(request.getName());
        character.setPronouns(request.getPronouns());
        character.setTrait1(request.getTrait1());
        character.setTrait2(request.getTrait2());
        character.setTrait3(request.getTrait3());

        // Set background
        character.setRank(request.getRank() != null ? request.getRank() : CaptainLogCharacter.Rank.ENSIGN);
        character.setAssignment(request.getAssignment() != null ? request.getAssignment() : CaptainLogCharacter.Assignment.COMMAND);
        character.setEnvironment(request.getEnvironment() != null ? request.getEnvironment() : CaptainLogCharacter.Environment.CITY);
        character.setOutlook(request.getOutlook() != null ? request.getOutlook() : CaptainLogCharacter.Outlook.OPTIMISTIC);
        character.setEducation(request.getEducation() != null ? request.getEducation() : CaptainLogCharacter.Education.ACADEMY);

        // Roll or set attributes
        character.setFitness(request.getFitness() != null ? request.getFitness() : rollAttribute());
        character.setTechnical(request.getTechnical() != null ? request.getTechnical() : rollAttribute());
        character.setStrength(request.getStrength() != null ? request.getStrength() : rollAttribute());
        character.setReason(request.getReason() != null ? request.getReason() : rollAttribute());
        character.setInsight(request.getInsight() != null ? request.getInsight() : rollAttribute());
        character.setPresence(request.getPresence() != null ? request.getPresence() : rollAttribute());

        // Constitution = average of STR and INS, rounded down
        character.setConstitution((character.getStrength() + character.getInsight()) / 2);

        // Set disciplines
        character.setCommand(request.getCommand() != null ? request.getCommand() : 0);
        character.setLeadership(request.getLeadership() != null ? request.getLeadership() : 0);
        character.setScience(request.getScience() != null ? request.getScience() : 0);
        character.setSecurity(request.getSecurity() != null ? request.getSecurity() : 0);
        character.setMedicine(request.getMedicine() != null ? request.getMedicine() : 0);
        character.setOperations(request.getOperations() != null ? request.getOperations() : 0);

        // Auto-assign disciplines based on assignment if all zero
        if (character.getCommand() == 0 && character.getLeadership() == 0 &&
            character.getScience() == 0 && character.getSecurity() == 0 &&
            character.getMedicine() == 0 && character.getOperations() == 0) {
            assignDefaultDisciplines(character);
        }

        // Add values
        if (request.getValues() != null) {
            for (String valueDesc : request.getValues()) {
                if (character.getValues().size() < 3) {
                    CaptainLogValue value = new CaptainLogValue(valueDesc);
                    value.setCharacter(character);
                    character.getValues().add(value);
                }
            }
        }

        // Add focuses
        if (request.getFocuses() != null) {
            for (String focusDesc : request.getFocuses()) {
                CaptainLogFocus focus = parseFocus(focusDesc);
                if (focus != null) {
                    focus.setCharacter(character);
                    character.getFocuses().add(focus);
                }
            }
        }

        return characterRepository.save(character);
    }

    /**
     * Rolls a single attribute using 2d6 (standard 2d20 attribute roll).
     */
    int rollAttribute() {
        return random.nextInt(6) + 1 + random.nextInt(6) + 1;
    }

    /**
     * Assigns default discipline ratings based on the character's assignment.
     */
    void assignDefaultDisciplines(CaptainLogCharacter character) {
        switch (character.getAssignment()) {
            case COMMAND:
                character.setCommand(2);
                character.setLeadership(1);
                break;
            case SCIENCE:
                character.setScience(2);
                character.setOperations(1);
                break;
            case SECURITY:
                character.setSecurity(2);
                character.setCommand(1);
                break;
            case ENGINEERING:
                character.setOperations(2);
                character.setScience(1);
                break;
            case MEDICAL:
                character.setMedicine(2);
                character.setScience(1);
                break;
            case OPERATIONS:
                character.setOperations(2);
                character.setLeadership(1);
                break;
        }
    }

    /**
     * Parses a focus string like "COMMAND: Starship Tactics" into a CaptainLogFocus.
     */
    CaptainLogFocus parseFocus(String focusDesc) {
        String[] parts = focusDesc.split(":", 2);
        if (parts.length == 2) {
            try {
                CaptainLogFocus.Discipline discipline = CaptainLogFocus.Discipline.valueOf(parts[0].trim().toUpperCase());
                return new CaptainLogFocus(discipline, parts[1].trim());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown discipline in focus: {}", parts[0]);
            }
        }
        return null;
    }

    /**
     * Retrieves a character by ID.
     */
    public Optional<CaptainLogCharacter> getCharacter(Long id) {
        return characterRepository.findById(id);
    }

    /**
     * Lists all characters.
     */
    public List<CaptainLogCharacter> listCharacters() {
        return characterRepository.findAll();
    }

    /**
     * Deletes a character by ID.
     */
    @Transactional
    public void deleteCharacter(Long id) {
        characterRepository.deleteById(id);
        log.info("Deleted character: {}", id);
    }

    // ==================== Starship Management ====================

    /**
     * Creates a new starship or station.
     */
    @Transactional
    public CaptainLogStarship createStarship(CreateStarshipRequest request) {
        log.info("Creating Captain's Log starship: {}", request.getName());

        CaptainLogStarship starship = new CaptainLogStarship();
        starship.setName(request.getName());
        starship.setRegistry(request.getRegistry());
        starship.setTrait1(request.getTrait1());
        starship.setTrait2(request.getTrait2());
        starship.setTrait3(request.getTrait3());
        starship.setShipClass(request.getShipClass() != null ? request.getShipClass() : "UNKNOWN");
        starship.setScale(request.getScale() != null ? request.getScale() : CaptainLogStarship.Scale.MEDIUM);
        starship.setStation(request.isStation());

        // Systems
        starship.setComms(request.getComms() != null ? request.getComms() : 1);
        starship.setComputers(request.getComputers() != null ? request.getComputers() : 1);
        starship.setEngines(request.getEngines() != null ? request.getEngines() : 1);
        starship.setSensors(request.getSensors() != null ? request.getSensors() : 1);
        starship.setStructure(request.getStructure() != null ? request.getStructure() : 1);
        starship.setWeapons(request.getWeapons() != null ? request.getWeapons() : 1);

        // Departments
        starship.setCommand(request.getCommand() != null ? request.getCommand() : 1);
        starship.setScience(request.getScience() != null ? request.getScience() : 1);
        starship.setSecurity(request.getSecurity() != null ? request.getSecurity() : 1);
        starship.setEngineering(request.getEngineering() != null ? request.getEngineering() : 1);
        starship.setMedical(request.getMedical() != null ? request.getMedical() : 1);
        starship.setOperations(request.getOperations() != null ? request.getOperations() : 1);

        // Talents
        if (request.getTalents() != null) {
            for (String talentName : request.getTalents()) {
                CaptainLogShipTalent talent = new CaptainLogShipTalent(talentName, null);
                talent.setStarship(starship);
                starship.getTalents().add(talent);
            }
        }

        return starshipRepository.save(starship);
    }

    /**
     * Retrieves a starship by ID.
     */
    public Optional<CaptainLogStarship> getStarship(Long id) {
        return starshipRepository.findById(id);
    }

    /**
     * Lists all starships.
     */
    public List<CaptainLogStarship> listStarships() {
        return starshipRepository.findAll();
    }

    /**
     * Deletes a starship by ID.
     */
    @Transactional
    public void deleteStarship(Long id) {
        starshipRepository.deleteById(id);
        log.info("Deleted starship: {}", id);
    }

    // ==================== Character-Starship Assignment ====================

    /**
     * Assigns a character to a starship.
     * @param characterId the character ID
     * @param starshipId the starship ID
     * @return the updated character
     */
    @Transactional
    public CaptainLogCharacter assignCharacterToStarship(Long characterId, Long starshipId) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        CaptainLogStarship starship = starshipRepository.findById(starshipId)
                .orElseThrow(() -> new IllegalArgumentException("Starship not found: " + starshipId));
        character.setAssignedStarship(starship);
        log.info("Assigned character {} to starship {}", characterId, starshipId);
        return characterRepository.save(character);
    }

    /**
     * Removes a character's starship assignment.
     * @param characterId the character ID
     * @return the updated character
     */
    @Transactional
    public CaptainLogCharacter unassignCharacter(Long characterId) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        character.setAssignedStarship(null);
        log.info("Unassigned character {} from starship", characterId);
        return characterRepository.save(character);
    }

    /**
     * Lists all characters assigned to a starship (crew roster).
     * @param starshipId the starship ID
     * @return list of crew members
     */
    public List<CaptainLogCharacter> getCrew(Long starshipId) {
        CaptainLogStarship starship = starshipRepository.findById(starshipId)
                .orElseThrow(() -> new IllegalArgumentException("Starship not found: " + starshipId));
        return starship.getCrew();
    }
}
