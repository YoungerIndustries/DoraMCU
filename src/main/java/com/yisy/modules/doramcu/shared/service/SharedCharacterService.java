package com.yisy.modules.doramcu.shared.service;

import com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogFocus;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import com.yisy.modules.doramcu.shared.dto.CreateSharedCharacterRequest;
import com.yisy.modules.doramcu.shared.dto.TravelCharacterRequest;
import com.yisy.modules.doramcu.shared.model.SharedCharacter;
import com.yisy.modules.doramcu.shared.model.SharedCharacterSkill;
import com.yisy.modules.doramcu.shared.model.SharedCharacterTravelLog;
import com.yisy.modules.doramcu.shared.repository.SharedCharacterRepository;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service layer for shared characters that can travel between
 * any game systems in the DoraMCU universe (Traveller, Captain's Log, DoraMCU 4X).
 * <p>
 * This service manages the universal character identity, handles
 * attribute conversion between systems, and tracks character travel.
 * </p>
 */
@Service
public class SharedCharacterService {

    private static final Logger log = LoggerFactory.getLogger(SharedCharacterService.class);

    private final SharedCharacterRepository sharedCharacterRepository;
    private final CaptainLogCharacterRepository captainLogRepository;
    private final CaptainLogService captainLogService;
    private final CharacterService travellerCharacterService;
    private final PlayerRepository playerRepository;

    public SharedCharacterService(
            SharedCharacterRepository sharedCharacterRepository,
            CaptainLogCharacterRepository captainLogRepository,
            CaptainLogService captainLogService,
            CharacterService travellerCharacterService,
            PlayerRepository playerRepository) {
        this.sharedCharacterRepository = sharedCharacterRepository;
        this.captainLogRepository = captainLogRepository;
        this.captainLogService = captainLogService;
        this.travellerCharacterService = travellerCharacterService;
        this.playerRepository = playerRepository;
    }

    // ==================== Character Creation ====================

    /**
     * Creates a new shared character with the given request.
     * If attributes are not provided, they are auto-rolled (2D6).
     */
    @Transactional
    public SharedCharacter createSharedCharacter(CreateSharedCharacterRequest request) {
        log.info("Creating shared character: {} (origin: {})", request.getName(), request.getOriginSystem());

        SharedCharacter character = new SharedCharacter(request.getName(), request.getOriginSystem());
        character.setPronouns(request.getPronouns());
        character.setDescription(request.getDescription());
        character.setAge(request.getAge() != null ? request.getAge() : 18);
        character.setRace(request.getRace());
        character.setNotes(request.getNotes());

        // Set or roll attributes (2D6, range 2-12)
        character.setDexterity(request.getDexterity() != null ? request.getDexterity() : DiceUtil.roll2D6());
        character.setTechnical(request.getTechnical() != null ? request.getTechnical() : DiceUtil.roll2D6());
        character.setStrength(request.getStrength() != null ? request.getStrength() : DiceUtil.roll2D6());
        character.setIntelligence(request.getIntelligence() != null ? request.getIntelligence() : DiceUtil.roll2D6());
        character.setWisdom(request.getWisdom() != null ? request.getWisdom() : DiceUtil.roll2D6());
        character.setCharisma(request.getCharisma() != null ? request.getCharisma() : DiceUtil.roll2D6());
        character.setConstitution(
                request.getConstitution() != null
                        ? request.getConstitution()
                        : (character.getStrength() + character.getWisdom()) / 2
        );

        // Create system-specific character
        SharedCharacter savedCharacter = sharedCharacterRepository.save(character);

        if (savedCharacter.getOriginSystem() == GameType.CAPTAINS_LOG) {
            CaptainLogCharacter clChar = createCaptainLogCharacterFromShared(savedCharacter);
            savedCharacter.setClCharacterId(clChar.getId());
        } else if (savedCharacter.getOriginSystem() == GameType.TRAVELLER) {
            TravellerCharacter tChar = createTravellerCharacterFromShared(savedCharacter);
            savedCharacter.setTravellerCharacterId(tChar.getId());
        } else if (savedCharacter.getOriginSystem() == GameType.DORAMCU_4X) {
            // For 4X, the shared character represents the empire ruler;
            // the Player record is linked via doramcu4xPlayerId set by the caller
        }

        return sharedCharacterRepository.save(savedCharacter);
    }

    /**
     * Creates a shared character from an existing Captain's Log character.
     */
    @Transactional
    public SharedCharacter importFromCaptainLog(Long clCharacterId) {
        Optional<CaptainLogCharacter> clOpt = captainLogService.getCharacter(clCharacterId);
        if (clOpt.isEmpty()) {
            throw new IllegalArgumentException("Captain's Log character not found: " + clCharacterId);
        }

        CaptainLogCharacter clChar = clOpt.get();

        // Check if already imported
        Optional<SharedCharacter> existing = sharedCharacterRepository.findByClCharacterId(clCharacterId);
        if (existing.isPresent()) {
            return existing.get();
        }

        SharedCharacter shared = new SharedCharacter(clChar.getName(), GameType.CAPTAINS_LOG);
        shared.setPronouns(clChar.getPronouns());
        shared.setDexterity(clChar.getFitness());
        shared.setTechnical(clChar.getTechnical());
        shared.setStrength(clChar.getStrength());
        shared.setIntelligence(clChar.getReason());
        shared.setWisdom(clChar.getInsight());
        shared.setCharisma(clChar.getPresence());
        shared.setConstitution(clChar.getConstitution());
        shared.setClCharacterId(clCharacterId);

        // Import focuses as skills
        for (CaptainLogFocus focus : clChar.getFocuses()) {
            SharedCharacterSkill skill = new SharedCharacterSkill(focus.getName(), 1);
            skill.setSystemSource("CAPTAINS_LOG");
            skill.setDescription("Focus in " + focus.getDiscipline());
            shared.addSkill(skill);
        }

        return sharedCharacterRepository.save(shared);
    }

    /**
     * Creates a shared character from an existing Traveller character.
     * Maps Traveller abilities (DEX, ST, END, INT, EDU, SOC, APP) to
     * universal attributes (Dexterity, Technical, Strength, Intelligence, Wisdom, Charisma, Constitution).
     */
    @Transactional
    public SharedCharacter importFromTraveller(int travellerCharacterId) {
        TravellerCharacter tChar = travellerCharacterService.getCharacter(travellerCharacterId);
        if (tChar == null) {
            throw new IllegalArgumentException("Traveller character not found: " + travellerCharacterId);
        }

        // Check if already imported
        Optional<SharedCharacter> existing = sharedCharacterRepository.findByTravellerCharacterId(travellerCharacterId);
        if (existing.isPresent()) {
            return existing.get();
        }

        SharedCharacter shared = new SharedCharacter(tChar.getName(), GameType.TRAVELLER);
        // Map Traveller abilities to universal attributes:
        // DEX -> Dexterity, ST -> Strength, END -> Constitution, INT -> Intelligence
        // EDU -> Technical, SOC -> Wisdom, APP -> Charisma
        shared.setDexterity(tChar.getAbility(AbilityScore.DEX));
        shared.setStrength(tChar.getAbility(AbilityScore.ST));
        shared.setConstitution(tChar.getAbility(AbilityScore.END));
        shared.setIntelligence(tChar.getAbility(AbilityScore.INT));
        shared.setTechnical(tChar.getAbility(AbilityScore.EDU));
        shared.setWisdom(tChar.getAbility(AbilityScore.SOC));
        shared.setCharisma(tChar.getAbility(AbilityScore.APP));
        shared.setAge(tChar.getAge());
        shared.setTravellerCharacterId(travellerCharacterId);

        // Import Traveller skills
        for (com.yisy.modules.doramcu.traveller.Skill skill : tChar.getSkills()) {
            SharedCharacterSkill sharedSkill = new SharedCharacterSkill(skill.name(), 1);
            sharedSkill.setSystemSource("TRAVELLER");
            shared.addSkill(sharedSkill);
        }

        return sharedCharacterRepository.save(shared);
    }

    // ==================== Character Travel ====================

    /**
     * Travels a character from its current system to the target system.
     * This creates a system-specific character in the target system
     * and updates the shared character record.
     */
    @Transactional
    public SharedCharacter travelCharacter(Long sharedId, TravelCharacterRequest request) {
        SharedCharacter shared = sharedCharacterRepository.findById(sharedId)
                .orElseThrow(() -> new IllegalArgumentException("Shared character not found: " + sharedId));

        GameType fromSystem = shared.getOriginSystem();
        GameType toSystem = request.getTargetSystem();

        if (fromSystem == toSystem) {
            throw new IllegalArgumentException("Character is already in system: " + toSystem);
        }

        log.info("Character {} traveling from {} to {}", shared.getName(), fromSystem, toSystem);

        // Create the character in the target system
        if (toSystem == GameType.CAPTAINS_LOG) {
            CaptainLogCharacter clChar = createCaptainLogCharacterFromShared(shared);
            shared.setClCharacterId(clChar.getId());
        } else if (toSystem == GameType.TRAVELLER) {
            TravellerCharacter tChar = createTravellerCharacterFromShared(shared);
            shared.setTravellerCharacterId(tChar.getId());
        } else if (toSystem == GameType.DORAMCU_4X) {
            // For 4X, the shared character links to an existing Player record
            // The doramcu4xPlayerId should be set before calling travelCharacter
        }

        // Update origin to target system
        shared.setOriginSystem(toSystem);

        // Log the travel
        SharedCharacterTravelLog travelLog = new SharedCharacterTravelLog(
                shared, fromSystem.name(), toSystem.name());
        travelLog.setTravelNotes(request.getTravelNotes());
        shared.addTravelLog(travelLog);

        return sharedCharacterRepository.save(shared);
    }

    // ==================== Queries ====================

    /**
     * Gets a shared character by ID.
     */
    public Optional<SharedCharacter> getSharedCharacter(Long id) {
        return sharedCharacterRepository.findById(id);
    }

    /**
     * Lists all shared characters.
     */
    public List<SharedCharacter> listSharedCharacters() {
        return sharedCharacterRepository.findAll();
    }

    /**
     * Lists shared characters by game type.
     */
    public List<SharedCharacter> listBySystem(GameType gameType) {
        return sharedCharacterRepository.findByOriginSystem(gameType);
    }

    /**
     * Creates a shared character from an existing 4X Player (Empire ruler).
     */
    @Transactional
    public SharedCharacter importFrom4X(Long playerId) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("4X Player not found: " + playerId);
        }

        Player player = playerOpt.get();

        // Check if already imported
        Optional<SharedCharacter> existing = sharedCharacterRepository.findByDoramcu4xPlayerId(playerId);
        if (existing.isPresent()) {
            return existing.get();
        }

        SharedCharacter shared = new SharedCharacter(player.getEmpireName(), GameType.DORAMCU_4X);
        // 4X Players don't have D&D-style attributes; set defaults
        shared.setDexterity(7);
        shared.setTechnical(7);
        shared.setStrength(7);
        shared.setIntelligence(7);
        shared.setWisdom(7);
        shared.setCharisma(7);
        shared.setConstitution(7);
        shared.setDescription(String.format("Empire: %s (%s, %s)",
                player.getEmpireName(), player.getGovernmentType(), player.getEthos()));
        shared.setDoramcu4xPlayerId(playerId);

        return sharedCharacterRepository.save(shared);
    }

    /**
     * Searches shared characters by name.
     */
    public List<SharedCharacter> searchByName(String name) {
        return sharedCharacterRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Gets the travel history for a shared character.
     */
    public List<SharedCharacterTravelLog> getTravelHistory(Long sharedId) {
        SharedCharacter shared = sharedCharacterRepository.findById(sharedId)
                .orElseThrow(() -> new IllegalArgumentException("Shared character not found: " + sharedId));
        return shared.getTravelLogs();
    }

    // ==================== Skill Management ====================

    /**
     * Adds a skill to a shared character.
     */
    @Transactional
    public SharedCharacterSkill addSkill(Long sharedId, String skillName, int skillLevel, String systemSource) {
        SharedCharacter shared = sharedCharacterRepository.findById(sharedId)
                .orElseThrow(() -> new IllegalArgumentException("Shared character not found: " + sharedId));

        SharedCharacterSkill skill = new SharedCharacterSkill(skillName, skillLevel);
        skill.setSystemSource(systemSource);
        shared.addSkill(skill);

        sharedCharacterRepository.save(shared);
        return skill;
    }

    // ==================== Attribute Conversion ====================

    /**
     * Creates a Captain's Log character from a shared character's attributes.
     * Maps universal attributes to Captain's Log attributes directly (same names).
     */
    private CaptainLogCharacter createCaptainLogCharacterFromShared(SharedCharacter shared) {
        CreateCharacterRequest request = new CreateCharacterRequest();
        request.setName(shared.getName());
        request.setPronouns(shared.getPronouns());
        request.setFitness(shared.getDexterity());
        request.setTechnical(shared.getTechnical());
        request.setStrength(shared.getStrength());
        request.setReason(shared.getIntelligence());
        request.setInsight(shared.getWisdom());
        request.setPresence(shared.getCharisma());
        CaptainLogCharacter clChar = captainLogService.createCharacter(request);

        // Map shared skills to Captain's Log focuses
        for (SharedCharacterSkill skill : shared.getSkills()) {
            CaptainLogFocus focus = new CaptainLogFocus();
            focus.setDiscipline(CaptainLogFocus.Discipline.SECURITY);
            focus.setName(skill.getSkillName());
            focus.setCharacter(clChar);
            clChar.getFocuses().add(focus);
        }

        return captainLogRepository.save(clChar);
    }

    /**
     * Creates a Traveller character from a shared character's attributes.
     * Maps universal attributes to Traveller abilities:
     * Dexterity -> DEX, Strength -> ST, Constitution -> END,
     * Intelligence -> INT, Technical -> EDU, Wisdom -> SOC, Charisma -> APP
     */
    private TravellerCharacter createTravellerCharacterFromShared(SharedCharacter shared) {
        Map<AbilityScore, Integer> abilities = new EnumMap<>(AbilityScore.class);
        abilities.put(AbilityScore.DEX, shared.getDexterity());
        abilities.put(AbilityScore.ST, shared.getStrength());
        abilities.put(AbilityScore.END, shared.getConstitution());
        abilities.put(AbilityScore.INT, shared.getIntelligence());
        abilities.put(AbilityScore.EDU, shared.getTechnical());
        abilities.put(AbilityScore.SOC, shared.getWisdom());
        abilities.put(AbilityScore.APP, shared.getCharisma());

        TravellerCharacter tChar = travellerCharacterService.createCharacter(shared.getName(), abilities);
        // Note: Traveller skills are added via career tours, not directly.
        // Shared skills are stored in the shared character record for reference.

        return tChar;
    }
}
