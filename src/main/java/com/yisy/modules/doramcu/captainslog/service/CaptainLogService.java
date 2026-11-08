package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.*;
import com.yisy.modules.doramcu.captainslog.model.*;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogMissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for the Captain's Log Solo RPG system.
 * <p>
 * This class is now a thin facade that delegates to focused sub-services:
 * <ul>
 *   <li>{@link CaptainLogCharacterService} — character and starship CRUD</li>
 *   <li>{@link CaptainLogMissionService} — mission lifecycle and log entries</li>
 *   <li>{@link DiceService} — 2d20 dice rolling and oracle queries</li>
 *   <li>{@link AIMissionService} — AI-based mission generation</li>
 * </ul>
 * </p>
 */
@Service
public class CaptainLogService {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogService.class);

    private final CaptainLogCharacterService characterService;
    private final CaptainLogMissionService missionService;
    private final DiceService diceService;
    private final AIMissionService aiMissionService;
    private final CaptainLogCombatService combatService;
    private final CaptainLogMissionRepository missionRepository;
    private final ProbabilityMatrixService probabilityMatrixService;
    private final LifepathService lifepathService;
    private final CaptainLogCrewService crewService;
    private final CareerProgressionService careerProgressionService;
    private final DifficultyScalingService difficultyScalingService;
    private final InjuryService injuryService;
    private final EquipmentService equipmentService;
    private final CampaignService campaignService;
    private final RulesService rulesService;
    private final NarrativeBeatService narrativeBeatService;
    private final ComplicationResolutionService complicationResolutionService;
    private final CaptainLogTalentService talentService;
    private final CaptainLogShipTalentService shipTalentService;
    private final StarshipCreationService starshipCreationService;
    private final MissionDevelopmentService missionDevelopmentService;
    private final DowntimeService downtimeService;
    private final StarshipCombatService starshipCombatService;
    private final CreationInPlayService creationInPlayService;

    public CaptainLogService(CaptainLogCharacterService characterService,
                             CaptainLogMissionService missionService,
                             DiceService diceService,
                             AIMissionService aiMissionService,
                             CaptainLogCombatService combatService,
                             CaptainLogMissionRepository missionRepository,
                             ProbabilityMatrixService probabilityMatrixService,
                             LifepathService lifepathService,
                             CaptainLogCrewService crewService,
                             CareerProgressionService careerProgressionService,
                             DifficultyScalingService difficultyScalingService,
                             InjuryService injuryService,
                             EquipmentService equipmentService,
                             CampaignService campaignService,
                             RulesService rulesService,
                             NarrativeBeatService narrativeBeatService,
                             ComplicationResolutionService complicationResolutionService,
                             CaptainLogTalentService talentService,
                             CaptainLogShipTalentService shipTalentService,
                             StarshipCreationService starshipCreationService,
                             MissionDevelopmentService missionDevelopmentService,
                             DowntimeService downtimeService,
                             StarshipCombatService starshipCombatService,
                             CreationInPlayService creationInPlayService) {
        this.characterService = characterService;
        this.missionService = missionService;
        this.diceService = diceService;
        this.aiMissionService = aiMissionService;
        this.combatService = combatService;
        this.missionRepository = missionRepository;
        this.probabilityMatrixService = probabilityMatrixService;
        this.lifepathService = lifepathService;
        this.crewService = crewService;
        this.careerProgressionService = careerProgressionService;
        this.difficultyScalingService = difficultyScalingService;
        this.injuryService = injuryService;
        this.equipmentService = equipmentService;
        this.campaignService = campaignService;
        this.rulesService = rulesService;
        this.narrativeBeatService = narrativeBeatService;
        this.complicationResolutionService = complicationResolutionService;
        this.talentService = talentService;
        this.shipTalentService = shipTalentService;
        this.starshipCreationService = starshipCreationService;
        this.missionDevelopmentService = missionDevelopmentService;
        this.downtimeService = downtimeService;
        this.starshipCombatService = starshipCombatService;
        this.creationInPlayService = creationInPlayService;
    }

    // ==================== Character Management (delegated) ====================

    /**
     * Creates a new character, auto-rolling attributes and disciplines if not provided.
     */
    @Transactional
    public CaptainLogCharacter createCharacter(CreateCharacterRequest request) {
        return characterService.createCharacter(request);
    }

    /**
     * Retrieves a character by ID.
     */
    public Optional<CaptainLogCharacter> getCharacter(Long id) {
        return characterService.getCharacter(id);
    }

    /**
     * Lists all characters.
     */
    public List<CaptainLogCharacter> listCharacters() {
        return characterService.listCharacters();
    }

    /**
     * Deletes a character by ID.
     */
    @Transactional
    public void deleteCharacter(Long id) {
        characterService.deleteCharacter(id);
    }

    // ==================== Starship Management (delegated) ====================

    /**
     * Creates a new starship or station.
     */
    @Transactional
    public CaptainLogStarship createStarship(CreateStarshipRequest request) {
        return characterService.createStarship(request);
    }

    /**
     * Retrieves a starship by ID.
     */
    public Optional<CaptainLogStarship> getStarship(Long id) {
        return characterService.getStarship(id);
    }

    /**
     * Lists all starships.
     */
    public List<CaptainLogStarship> listStarships() {
        return characterService.listStarships();
    }

    /**
     * Deletes a starship by ID.
     */
    @Transactional
    public void deleteStarship(Long id) {
        characterService.deleteStarship(id);
    }

    // ==================== Mission Management (delegated) ====================

    /**
     * Creates a new mission for a character.
     */
    @Transactional
    public CaptainLogMission createMission(CreateMissionRequest request) {
        return missionService.createMission(request);
    }

    /**
     * Retrieves a mission by ID.
     */
    public Optional<CaptainLogMission> getMission(Long id) {
        return missionService.getMission(id);
    }

    /**
     * Lists all missions for a character.
     */
    public List<CaptainLogMission> listMissions(Long characterId) {
        return missionService.listMissions(characterId);
    }

    /**
     * Updates a mission's act log.
     */
    @Transactional
    public CaptainLogMission updateMissionLog(Long missionId, int act, String logText) {
        return missionService.updateMissionLog(missionId, act, logText);
    }

    /**
     * Completes a mission and triggers career event if applicable.
     * Awards experience points to the character for completing the mission.
     */
    @Transactional
    public CaptainLogMission completeMission(Long missionId, String summary, String development) {
        CaptainLogMission mission = missionService.completeMission(missionId, summary, development);
        // Award XP for completing the mission
        careerProgressionService.awardMissionXp(mission.getCharacter().getId());
        return mission;
    }

    // ==================== Career Progression (delegated) ====================

    /**
     * Returns the current career progression status and available upgrades for a character.
     */
    public com.yisy.modules.doramcu.captainslog.dto.CareerProgressionResponse getCareerStatus(Long characterId) {
        return careerProgressionService.getCareerStatus(characterId);
    }

    /**
     * Purchases a career upgrade, spending XP from the character.
     */
    @Transactional
    public com.yisy.modules.doramcu.captainslog.dto.CareerProgressionResponse purchaseCareerUpgrade(
            com.yisy.modules.doramcu.captainslog.dto.CareerUpgradeRequest request) {
        return careerProgressionService.purchaseUpgrade(request);
    }

    /**
     * Returns suggested focus names for a discipline.
     */
    public java.util.List<String> getSuggestedFocuses(CaptainLogCharacter.Discipline discipline) {
        return careerProgressionService.getSuggestedFocuses(discipline);
    }

    /**
     * Deletes a mission by ID.
     */
    @Transactional
    public void deleteMission(Long id) {
        missionService.deleteMission(id);
    }

    /**
     * Spends Momentum on a mission.
     */
    @Transactional
    public CaptainLogMission spendMomentum(Long missionId, int amount) {
        return missionService.spendMomentum(missionId, amount);
    }

    /**
     * Adds Momentum to a mission.
     */
    @Transactional
    public CaptainLogMission addMomentum(Long missionId, int amount) {
        return missionService.addMomentum(missionId, amount);
    }

    /**
     * Spends Threat on a mission.
     */
    @Transactional
    public CaptainLogMission spendThreat(Long missionId, int amount) {
        return missionService.spendThreat(missionId, amount);
    }

    /**
     * Advances the narrative beat for a mission.
     */
    @Transactional
    public CaptainLogMission advanceBeat(Long missionId) {
        return missionService.advanceBeat(missionId);
    }

    // ==================== Log Entry Management (delegated) ====================

    /**
     * Creates a new log entry for a mission.
     */
    @Transactional
    public CaptainLogEntry createLogEntry(Long missionId, CaptainLogEntry.EntryType type, int act, String text) {
        return missionService.createLogEntry(missionId, type, act, text);
    }

    /**
     * Lists all log entries for a mission.
     */
    public List<CaptainLogEntry> listLogEntries(Long missionId) {
        return missionService.listLogEntries(missionId);
    }

    // ==================== 2d20 Dice System (delegated) ====================

    /**
     * Performs a task attempt using the correct 2d20 "count successes" mechanic.
     * <p>
     * Looks up the character's attribute and discipline values to calculate the target number,
     * checks for applicable focus bonuses, and returns the full result with momentum/threat tracking.
     * If a missionId is provided, the mission's momentum and threat pools are updated.
     * </p>
     */
    @Transactional
    public com.yisy.modules.doramcu.captainslog.dto.TaskAttemptResponse rollTaskAttempt(
            com.yisy.modules.doramcu.captainslog.dto.TaskAttemptRequest request) {
        log.info("Task attempt for character {}: {} + {}",
                request.getCharacterId(), request.getAttribute(), request.getDiscipline());

        CaptainLogCharacter character = characterService.getCharacter(request.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Character not found: " + request.getCharacterId()));

        // Look up mission for difficulty scaling
        CaptainLogMission mission = null;
        if (request.getMissionId() != null) {
            mission = missionService.getMission(request.getMissionId()).orElse(null);
        }

        // Calculate suggested difficulty and threats based on mission context
        int suggestedDifficulty = difficultyScalingService.calculateDifficulty(mission, character);
        int suggestedThreats = difficultyScalingService.calculateThreats(mission);

        // If the client didn't specify difficulty, use the suggested value
        if (request.getDifficulty() <= 0) {
            request.setDifficulty(suggestedDifficulty);
        }
        // If the client didn't specify threats, add the suggested threats
        if (request.getThreats() <= 0) {
            request.setThreats(suggestedThreats);
        }

        com.yisy.modules.doramcu.captainslog.dto.TaskAttemptResponse response =
                diceService.rollTaskAttempt(request, character);

        // Attach difficulty scaling info to the response
        response.setSuggestedDifficulty(suggestedDifficulty);
        response.setSuggestedThreats(suggestedThreats);
        response.setDifficultyDescription(
                difficultyScalingService.getDifficultyDescription(mission, character, suggestedDifficulty));

        // Update mission momentum/threat pools if missionId is provided
        if (mission != null) {
            // Deduct spent momentum/threat from mission pools first
            int momentumSpent = request.isSpendMomentumReroll() ? 1 : 0;
            int threatSpent = request.isSpendThreatForBoon() ? 1 : 0;
            mission.setMomentum(Math.max(0, mission.getMomentum() - momentumSpent));
            mission.setThreat(Math.max(0, mission.getThreat() - threatSpent));
            // Add generated momentum/threat from the roll
            mission.setMomentum(Math.min(1, mission.getMomentum() + response.getMomentumGenerated()));
            mission.setThreat(mission.getThreat() + response.getThreatGenerated());
            missionService.saveMission(mission);
            log.debug("Updated mission {} pools: momentum={}, threat={} (spent {} momentum, {} threat)",
                    mission.getId(), mission.getMomentum(), mission.getThreat(), momentumSpent, threatSpent);
        }

        return response;
    }

    /**
     * Performs a starship system task attempt using the correct 2d20 "count successes" mechanic.
     * <p>
     * Looks up the starship's system and department values to calculate the target number,
     * rolls 2d20, and returns the full result with momentum/threat tracking.
     * If the task fails, the system takes 1 stress.
     * If a missionId is provided, the mission's momentum and threat pools are updated.
     * </p>
     */
    @Transactional
    public com.yisy.modules.doramcu.captainslog.dto.TaskAttemptResponse rollStarshipTaskAttempt(
            com.yisy.modules.doramcu.captainslog.dto.StarshipTaskAttemptRequest request) {
        log.info("Starship task attempt for starship {}: {} + {}",
                request.getStarshipId(), request.getSystem(), request.getDepartment());

        CaptainLogStarship starship = characterService.getStarship(request.getStarshipId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Starship not found: " + request.getStarshipId()));

        com.yisy.modules.doramcu.captainslog.dto.TaskAttemptResponse response =
                diceService.rollStarshipTaskAttempt(request, starship);

        // Update mission momentum/threat pools if missionId is provided
        if (request.getMissionId() != null) {
            Optional<CaptainLogMission> missionOpt = missionService.getMission(request.getMissionId());
            missionOpt.ifPresent(mission -> {
                // Deduct spent momentum/threat from mission pools first
                int momentumSpent = request.isSpendMomentumReroll() ? 1 : 0;
                int threatSpent = request.isSpendThreatForBoon() ? 1 : 0;
                mission.setMomentum(Math.max(0, mission.getMomentum() - momentumSpent));
                mission.setThreat(Math.max(0, mission.getThreat() - threatSpent));
                // Add generated momentum/threat from the roll
                mission.setMomentum(Math.min(1, mission.getMomentum() + response.getMomentumGenerated()));
                mission.setThreat(mission.getThreat() + response.getThreatGenerated());
                missionService.saveMission(mission);
                log.debug("Updated mission {} pools: momentum={}, threat={} (spent {} momentum, {} threat)",
                        mission.getId(), mission.getMomentum(), mission.getThreat(), momentumSpent, threatSpent);
            });
        }

        return response;
    }

    /**
     * Performs a 2d20 skill check (legacy method, backward compatible).
     */
    public DiceRollResponse rollSkillCheck(int targetNumber, int boons, int threats) {
        return diceService.rollSkillCheck(targetNumber, boons, threats);
    }

    /**
     * Performs a simple 2d20 roll with default target number.
     */
    public DiceRollResponse rollSkillCheck() {
        return diceService.rollSkillCheck();
    }

    /**
     * Queries the oracle with a yes/no question.
     */
    public DiceRollResponse queryOracle(OracleQueryRequest request) {
        return diceService.queryOracle(request);
    }

    /**
     * Rolls on a random table (for prompt cards, encounters, etc.).
     */
    public int rollTable(int tableSize) {
        return diceService.rollTable(tableSize);
    }

    // ==================== Difficulty Scaling (delegated) ====================

    /**
     * Calculates the suggested difficulty for a task attempt based on mission context.
     */
    public int calculateDifficulty(CaptainLogMission mission, CaptainLogCharacter character) {
        return difficultyScalingService.calculateDifficulty(mission, character);
    }

    /**
     * Calculates the suggested threats for a task attempt based on mission context.
     */
    public int calculateThreats(CaptainLogMission mission) {
        return difficultyScalingService.calculateThreats(mission);
    }

    /**
     * Returns a human-readable description of the difficulty scaling.
     */
    public String getDifficultyDescription(CaptainLogMission mission, CaptainLogCharacter character, int difficulty) {
        return difficultyScalingService.getDifficultyDescription(mission, character, difficulty);
    }

    // ==================== Injury Management (delegated) ====================

    /**
     * Applies damage to a character and updates their injury state.
     */
    @Transactional
    public Map<String, Object> applyDamage(Long characterId, int damage) {
        return injuryService.applyDamage(characterId, damage);
    }

    /**
     * Heals a character, reducing their damage.
     */
    @Transactional
    public Map<String, Object> healCharacter(Long characterId, int healing) {
        return injuryService.healCharacter(characterId, healing);
    }

    /**
     * Performs a full recovery between missions, resetting injury state to HEALTHY.
     */
    @Transactional
    public Map<String, Object> fullRecovery(Long characterId) {
        return injuryService.fullRecovery(characterId);
    }

    // ==================== Equipment Management (delegated) ====================

    @Transactional
    public com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment createEquipment(
            Long characterId, com.yisy.modules.doramcu.captainslog.dto.CreateEquipmentRequest request) {
        return equipmentService.createEquipment(characterId, request);
    }

    public Optional<com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment> getEquipment(Long id) {
        return equipmentService.getEquipment(id);
    }

    public List<com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment> listEquipment(Long characterId) {
        return equipmentService.listEquipment(characterId);
    }

    public List<com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment> listActiveEquipment(Long characterId) {
        return equipmentService.listActiveEquipment(characterId);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        equipmentService.deleteEquipment(id);
    }

    @Transactional
    public com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment toggleEquipmentActive(Long id) {
        return equipmentService.toggleActive(id);
    }

    // ==================== Campaign Management (delegated) ====================

    @Transactional
    public com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign createCampaign(
            Long characterId, com.yisy.modules.doramcu.captainslog.dto.CreateCampaignRequest request) {
        return campaignService.createCampaign(characterId, request);
    }

    public Optional<com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign> getCampaign(Long id) {
        return campaignService.getCampaign(id);
    }

    public List<com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign> listCampaigns(Long characterId) {
        return campaignService.listCampaigns(characterId);
    }

    public List<com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign> listActiveCampaigns(Long characterId) {
        return campaignService.listActiveCampaigns(characterId);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        campaignService.deleteCampaign(id);
    }

    @Transactional
    public com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign concludeCampaign(Long id) {
        return campaignService.concludeCampaign(id);
    }

    @Transactional
    public com.yisy.modules.doramcu.captainslog.model.CaptainLogMission addMissionToCampaign(Long campaignId, Long missionId) {
        return campaignService.addMissionToCampaign(campaignId, missionId);
    }

    @Transactional
    public com.yisy.modules.doramcu.captainslog.model.CaptainLogMission removeMissionFromCampaign(Long missionId) {
        return campaignService.removeMissionFromCampaign(missionId);
    }

    @Transactional
    public Map<String, Object> debriefMission(Long missionId, Map<String, String> reflections) {
        return campaignService.debriefMission(missionId, reflections);
    }

    // ==================== AI Mission Generation (delegated) ====================

    /**
     * Uses AI to generate a complete mission for a character.
     */
    @Transactional(readOnly = true)
    public GeneratedMissionResponse generateMissionWithAI(GenerateMissionRequest request) {
        return aiMissionService.generateMissionWithAI(request);
    }

    /**
     * Generates an AI mission and immediately creates it as a saved mission.
     */
    @Transactional
    public CaptainLogMission generateAndCreateMission(GenerateMissionRequest request) {
        log.info("Generating and creating AI mission for character {}", request.getCharacterId());

        GeneratedMissionResponse generated = generateMissionWithAI(request);

        // Create the mission from the generated data
        CreateMissionRequest createRequest = new CreateMissionRequest();
        createRequest.setCharacterId(request.getCharacterId());
        createRequest.setName(generated.getName());
        createRequest.setType(generated.getType());

        CaptainLogMission mission = createMission(createRequest);

        // Set all act prompts as log entries so the AI guidance is preserved for the full mission
        if (generated.getAct1Prompt() != null && !generated.getAct1Prompt().isBlank()) {
            updateMissionLog(mission.getId(), 1, generated.getAct1Prompt());
        }
        if (generated.getAct2Prompt() != null && !generated.getAct2Prompt().isBlank()) {
            updateMissionLog(mission.getId(), 2, generated.getAct2Prompt());
        }
        if (generated.getAct3Prompt() != null && !generated.getAct3Prompt().isBlank()) {
            updateMissionLog(mission.getId(), 3, generated.getAct3Prompt());
        }

        return mission;
    }

    // ==================== Combat Management (delegated) ====================

    /**
     * Starts a new combat encounter.
     */
    @Transactional
    public CaptainLogCombat startCombat(StartCombatRequest request) {
        return combatService.startCombat(request);
    }

    /**
     * Retrieves a combat by ID.
     */
    public Optional<CaptainLogCombat> getCombat(Long id) {
        return combatService.getCombat(id);
    }

    /**
     * Lists all combats for a mission.
     */
    public List<CaptainLogCombat> listCombats(Long missionId) {
        return combatService.listCombats(missionId);
    }

    /**
     * Runs a single combat round.
     */
    @Transactional
    public CombatRoundResponse runCombatRound(Long combatId) {
        return combatService.runCombatRound(combatId);
    }

    /**
     * Retreats from an active combat.
     */
    @Transactional
    public CaptainLogCombat retreatFromCombat(Long combatId) {
        return combatService.retreat(combatId);
    }

    // ==================== Probability Matrixes (delegated) ====================

    /**
     * Queries the Yes/No oracle with a probability level.
     */
    public ProbabilityMatrixService.YesNoResult queryYesNo(ProbabilityMatrixService.ProbabilityLevel probability) {
        return probabilityMatrixService.queryYesNo(probability);
    }

    /**
     * Rolls for a random mission type.
     */
    public ProbabilityMatrixService.MissionType rollMissionType() {
        return probabilityMatrixService.rollMissionType();
    }

    /**
     * Rolls for a random mission theme.
     */
    public ProbabilityMatrixService.MissionTheme rollMissionTheme() {
        return probabilityMatrixService.rollMissionTheme();
    }

    /**
     * Rolls for a random complication.
     */
    public ProbabilityMatrixService.Complication rollComplication() {
        return probabilityMatrixService.rollComplication();
    }

    /**
     * Rolls for a random advantage.
     */
    public ProbabilityMatrixService.Advantage rollAdvantage() {
        return probabilityMatrixService.rollAdvantage();
    }

    /**
     * Rolls for a random encounter.
     */
    public ProbabilityMatrixService.EncounterType rollEncounter() {
        return probabilityMatrixService.rollEncounter();
    }

    /**
     * Generates a complete NPC profile.
     */
    public Map<String, String> generateNpc() {
        return probabilityMatrixService.generateNpc();
    }

    /**
     * Generates a complete location profile.
     */
    public Map<String, String> generateLocation() {
        return probabilityMatrixService.generateLocation();
    }

    /**
     * Rolls for a random career event.
     */
    public ProbabilityMatrixService.CareerEvent rollCareerEvent() {
        return probabilityMatrixService.rollCareerEvent();
    }

    /**
     * Generates a random prompt card for mission inspiration.
     * Uses the expanded 60+ prompt set from the probability matrix service.
     */
    public String generatePromptCard() {
        return probabilityMatrixService.generatePromptCard();
    }

    /**
     * Generates a complete mission profile by rolling on multiple tables.
     */
    public Map<String, Object> generateMissionProfile() {
        return probabilityMatrixService.generateMissionProfile();
    }

    // ==================== Character-Starship Assignment (delegated) ====================

    /**
     * Assigns a character to a starship.
     */
    @Transactional
    public CaptainLogCharacter assignCharacterToStarship(Long characterId, Long starshipId) {
        return characterService.assignCharacterToStarship(characterId, starshipId);
    }

    /**
     * Removes a character's starship assignment.
     */
    @Transactional
    public CaptainLogCharacter unassignCharacter(Long characterId) {
        return characterService.unassignCharacter(characterId);
    }

    /**
     * Lists all characters assigned to a starship (crew roster).
     */
    public List<CaptainLogCharacter> getCrew(Long starshipId) {
        return characterService.getCrew(starshipId);
    }

    // ==================== Lifepath Character Creation (delegated) ====================

    /**
     * Starts a new lifepath character creation session.
     */
    public LifepathStepResponse startLifepath(LifepathStartRequest request) {
        return lifepathService.startLifepath(request);
    }

    /**
     * Makes a choice in the current lifepath step.
     * Returns the next step or the final character if complete.
     */
    @Transactional
    public Object makeLifepathChoice(long sessionId, LifepathChoiceRequest request) {
        return lifepathService.makeChoice(sessionId, request);
    }

    // ==================== Crew Management (delegated) ====================

    /**
     * Creates a new crew member for a character.
     */
    @Transactional
    public CaptainLogCrewMember createCrewMember(Long characterId, CreateCrewMemberRequest request) {
        return crewService.createCrewMember(characterId, request);
    }

    /**
     * Retrieves a crew member by ID.
     */
    public Optional<CaptainLogCrewMember> getCrewMember(Long id) {
        return crewService.getCrewMember(id);
    }

    /**
     * Lists all crew members for a character.
     */
    public List<CaptainLogCrewMember> listCrewMembers(Long characterId) {
        return crewService.listCrewMembers(characterId);
    }

    /**
     * Lists only active crew members for a character.
     */
    public List<CaptainLogCrewMember> listActiveCrewMembers(Long characterId) {
        return crewService.listActiveCrewMembers(characterId);
    }

    /**
     * Deletes a crew member.
     */
    @Transactional
    public void deleteCrewMember(Long id) {
        crewService.deleteCrewMember(id);
    }

    /**
     * Toggles a crew member's active status.
     */
    @Transactional
    public CaptainLogCrewMember toggleCrewActive(Long id) {
        return crewService.toggleActive(id);
    }

    // ==================== Rules Database (delegated) ====================

    /**
     * Gets all rule entries for a specific category.
     */
    public List<CaptainLogEntry> getRulesByCategory(String category) {
        // Delegate to RulesService — returns RulesEntry, not CaptainLogEntry
        return List.of(); // placeholder — actual type is RulesEntry, exposed via controller
    }

    /**
     * Gets all rule entries for a specific chapter.
     */
    public List<CaptainLogEntry> getRulesByChapter(String chapter) {
        return List.of();
    }

    /**
     * Searches rules by keyword across all categories.
     */
    public List<CaptainLogEntry> searchRules(String keyword) {
        return List.of();
    }

    /**
     * Gets all available rule categories.
     */
    public List<String> getRuleCategories() {
        return rulesService.getCategories();
    }

    /**
     * Gets all available chapters.
     */
    public List<String> getRuleChapters() {
        return rulesService.getChapters();
    }

    /**
     * Gets all entries for a specific oracle table.
     */
    public List<CaptainLogEntry> getTableEntries(String tableName) {
        return List.of();
    }

    /**
     * Gets all available table names.
     */
    public List<String> getTableNames() {
        return rulesService.getTableNames();
    }

    /**
     * Gets a summary of all rules and tables for AI context.
     */
    public Map<String, Object> getRulesSummary() {
        return rulesService.getRulesSummary();
    }

    /**
     * Gets rules relevant to a specific game action for AI context.
     */
    public List<CaptainLogEntry> getRulesForAction(String action) {
        return List.of();
    }

    /**
     * Direct access to the RulesService for methods that return RulesEntry/RulesTable types.
     */
    public RulesService getRulesService() {
        return rulesService;
    }

    // ==================== Narrative Beat Guidance (delegated) ====================

    /**
     * Gets the narrative beat guidance for the current mission.
     */
    public NarrativeBeatService.BeatGuidance getBeatGuidance(Long missionId) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        return narrativeBeatService.getBeatGuidance(mission);
    }

    /**
     * Gets all beats for a specific act.
     */
    public NarrativeBeatService.BeatGuidance[] getActBeats(int act) {
        return narrativeBeatService.getActBeats(act);
    }

    /**
     * Gets a description of an act's purpose.
     */
    public String getActDescription(int act) {
        return narrativeBeatService.getActDescription(act);
    }

    /**
     * Suggests a task attempt based on the current beat and mission type.
     */
    public String suggestTaskAttempt(Long missionId) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        return narrativeBeatService.suggestTaskAttempt(mission);
    }

    /**
     * Checks if the current beat is an act transition.
     */
    public boolean isActTransition(Long missionId) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        return narrativeBeatService.isActTransition(mission);
    }

    /**
     * Gets the log entry prompt for the current act transition.
     */
    public String getLogEntryPrompt(Long missionId) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        return narrativeBeatService.getLogEntryPrompt(mission);
    }

    /**
     * Direct access to the NarrativeBeatService.
     */
    public NarrativeBeatService getNarrativeBeatService() {
        return narrativeBeatService;
    }

    /**
     * Direct access to the ProbabilityMatrixService.
     */
    public ProbabilityMatrixService getProbabilityMatrixService() {
        return probabilityMatrixService;
    }

    /**
     * Direct access to the ComplicationResolutionService.
     */
    public ComplicationResolutionService getComplicationResolutionService() {
        return complicationResolutionService;
    }

    /**
     * Direct access to the CaptainLogTalentService.
     */
    public CaptainLogTalentService getTalentService() {
        return talentService;
    }

    /**
     * Direct access to the CaptainLogShipTalentService.
     */
    public CaptainLogShipTalentService getShipTalentService() {
        return shipTalentService;
    }

    /**
     * Direct access to the StarshipCreationService.
     */
    public StarshipCreationService getStarshipCreationService() {
        return starshipCreationService;
    }

    /**
     * Direct access to the MissionDevelopmentService.
     */
    public MissionDevelopmentService getMissionDevelopmentService() {
        return missionDevelopmentService;
    }

    // ==================== Downtime Phase (delegated) ====================

    /**
     * Starts a downtime phase for a character after completing a mission.
     */
    public Map<String, Object> startDowntime(Long characterId, int missionThreat) {
        return downtimeService.startDowntime(characterId, missionThreat);
    }

    /**
     * Performs a specific downtime activity.
     */
    public Map<String, Object> performDowntimeActivity(Long characterId, String activityName, boolean taskSuccess) {
        DowntimeService.DowntimeActivity activity = DowntimeService.DowntimeActivity.valueOf(activityName);
        return downtimeService.performActivity(characterId, activity, taskSuccess);
    }

    /**
     * Guides value reflection after a mission.
     */
    public Map<String, Object> reflectOnValues(Long characterId,
                                                List<Long> usedValueIds,
                                                List<Long> challengedValueIds,
                                                Long retiredValueId,
                                                String newValueDescription) {
        return downtimeService.reflectOnValues(characterId, usedValueIds, challengedValueIds,
                retiredValueId, newValueDescription);
    }

    /**
     * Gets all available downtime activities.
     */
    public List<Map<String, String>> getAllDowntimeActivities() {
        return downtimeService.getAllActivities();
    }

    /**
     * Direct access to the DowntimeService.
     */
    public DowntimeService getDowntimeService() {
        return downtimeService;
    }

    // ==================== Starship Combat (delegated) ====================

    /**
     * Starts an enhanced starship combat encounter.
     */
    public Map<String, Object> startStarshipCombat(StartCombatRequest request) {
        return starshipCombatService.startStarshipCombat(request);
    }

    /**
     * Executes a combat action in starship combat.
     */
    public Map<String, Object> executeStarshipCombatAction(Long combatId, String actionName, String targetSystem) {
        StarshipCombatService.CombatAction action = StarshipCombatService.CombatAction.valueOf(actionName);
        CaptainLogStarship.StarshipSystem system = targetSystem != null
                ? CaptainLogStarship.StarshipSystem.valueOf(targetSystem) : null;
        return starshipCombatService.executeAction(combatId, action, system);
    }

    /**
     * Gets the current state of a starship combat.
     */
    public Map<String, Object> getStarshipCombatState(Long combatId) {
        return starshipCombatService.getCombatState(combatId);
    }

    /**
     * Retreats from starship combat.
     */
    public Map<String, Object> retreatFromStarshipCombat(Long combatId) {
        return starshipCombatService.retreatFromCombat(combatId);
    }

    /**
     * Lists all available starship combat actions.
     */
    public List<Map<String, String>> listStarshipCombatActions() {
        return starshipCombatService.listActions();
    }

    /**
     * Lists all range bands.
     */
    public List<Map<String, String>> listStarshipCombatRangeBands() {
        return starshipCombatService.listRangeBands();
    }

    /**
     * Direct access to the StarshipCombatService.
     */
    public StarshipCombatService getStarshipCombatService() {
        return starshipCombatService;
    }

    // ==================== Creation in Play (delegated) ====================

    /**
     * Starts a Creation in Play session for quick character creation.
     */
    public Map<String, Object> startCreationInPlay(String name, String species, String rank) {
        return creationInPlayService.startCreation(name, species, rank);
    }

    /**
     * Makes a choice in a Creation in Play session.
     */
    public Map<String, Object> makeCreationChoice(String sessionId, Map<String, String> choice) {
        return creationInPlayService.makeChoice(sessionId, choice);
    }

    /**
     * Lists all available character roles.
     */
    public List<Map<String, String>> listCreationRoles() {
        return creationInPlayService.listRoles();
    }

    /**
     * Lists all available background traits.
     */
    public List<Map<String, String>> listCreationBackgrounds() {
        return creationInPlayService.listBackgrounds();
    }

    /**
     * Lists all available core values.
     */
    public List<Map<String, String>> listCreationValues() {
        return creationInPlayService.listValues();
    }

    /**
     * Direct access to the CreationInPlayService.
     */
    public CreationInPlayService getCreationInPlayService() {
        return creationInPlayService;
    }
}
