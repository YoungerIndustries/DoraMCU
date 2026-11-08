package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogValue;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogStarshipRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Manages the Downtime Phase between missions in the Captain's Log Solo RPG.
 * <p>
 * Between missions, characters spend time on activities that prepare them for the next adventure.
 * The downtime phase includes:
 * <ul>
 *   <li><b>Downtime Activities</b> — Maintenance, Leave, Research, Training, Diplomacy, Exploration</li>
 *   <li><b>Threat Resolution</b> — Resolve accumulated threat from the previous mission</li>
 *   <li><b>Value Reflection</b> — Retire used/challenged values and gain new ones</li>
 *   <li><b>Character Recovery</b> — Heal injuries and recover constitution</li>
 *   <li><b>Starship Repairs</b> — Reduce system stress on the assigned starship</li>
 * </ul>
 * </p>
 * <p>
 * Each downtime activity requires a task attempt. Success provides benefits; failure may
 * generate complications or extend downtime.
 * </p>
 */
@Service
public class DowntimeService {

    private static final Logger log = LoggerFactory.getLogger(DowntimeService.class);

    private final CaptainLogCharacterRepository characterRepository;
    private final CaptainLogStarshipRepository starshipRepository;
    private final CaptainLogValueRepository valueRepository;
    private final ProbabilityMatrixService probabilityMatrixService;
    private final InjuryService injuryService;
    private final CareerProgressionService careerProgressionService;

    public DowntimeService(CaptainLogCharacterRepository characterRepository,
                           CaptainLogStarshipRepository starshipRepository,
                           CaptainLogValueRepository valueRepository,
                           ProbabilityMatrixService probabilityMatrixService,
                           InjuryService injuryService,
                           CareerProgressionService careerProgressionService) {
        this.characterRepository = characterRepository;
        this.starshipRepository = starshipRepository;
        this.valueRepository = valueRepository;
        this.probabilityMatrixService = probabilityMatrixService;
        this.injuryService = injuryService;
        this.careerProgressionService = careerProgressionService;
    }

    // ==================== Downtime Activities ====================

    /**
     * The six downtime activities available between missions.
     */
    public enum DowntimeActivity {
        MAINTENANCE("Maintenance", "Repair and upgrade the starship systems.",
                "Engines + Operations", "Reduce starship system stress by 1 per system. " +
                        "On success, roll on the Maintenance Events table for a bonus event."),
        LEAVE("Leave", "Take personal time off for rest and recreation.",
                "Fitness + Leadership", "Recover from injuries. On success, fully recover to HEALTHY state. " +
                        "On failure, recover partially (one state improvement)."),
        RESEARCH("Research", "Study a topic of interest or investigate mission findings.",
                "Reason + Science", "Gain insight into a topic. On success, gain a boon on the next " +
                        "related task attempt in the next mission. On failure, the research yields inconclusive results."),
        TRAINING("Training", "Practice skills or learn new techniques.",
                "Insight + Command", "Improve a skill. On success, gain 1 XP toward the next career upgrade. " +
                        "On failure, the training session is less productive — gain no benefit."),
        DIPLOMACY("Diplomacy", "Engage with allies, superiors, or local contacts.",
                "Presence + Leadership", "Strengthen relationships. On success, gain a new contact or ally " +
                        "who can provide a boon once during the next mission. On failure, the meeting is unproductive."),
        EXPLORATION("Exploration", "Investigate an anomaly or explore a nearby system.",
                "Technical + Science", "Discover something new. On success, roll on the Encounter table " +
                        "for a minor encounter. On failure, the exploration yields nothing of note.");

        private final String name;
        private final String description;
        private final String taskAttempt;
        private final String effect;

        DowntimeActivity(String name, String description, String taskAttempt, String effect) {
            this.name = name;
            this.description = description;
            this.taskAttempt = taskAttempt;
            this.effect = effect;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getTaskAttempt() {
            return taskAttempt;
        }

        public String getEffect() {
            return effect;
        }
    }

    /**
     * Suggests downtime activities based on the character's current state.
     * Prioritizes activities that address the character's most pressing needs.
     */
    public List<DowntimeActivity> suggestActivities(CaptainLogCharacter character, int missionThreat) {
        List<DowntimeActivity> suggestions = new ArrayList<>();

        // If injured, prioritize Leave
        if (character.getInjuryState() != CaptainLogCharacter.InjuryState.HEALTHY) {
            suggestions.add(DowntimeActivity.LEAVE);
        }

        // If high threat, suggest activities that resolve it
        if (missionThreat >= 2) {
            suggestions.add(DowntimeActivity.MAINTENANCE);
            suggestions.add(DowntimeActivity.DIPLOMACY);
        }

        // If has XP, suggest Training
        if (character.getExperiencePoints() >= CareerProgressionService.XP_DISCIPLINE_INCREASE) {
            suggestions.add(DowntimeActivity.TRAINING);
        }

        // Always add Research and Exploration as options
        if (!suggestions.contains(DowntimeActivity.RESEARCH)) {
            suggestions.add(DowntimeActivity.RESEARCH);
        }
        if (!suggestions.contains(DowntimeActivity.EXPLORATION)) {
            suggestions.add(DowntimeActivity.EXPLORATION);
        }

        return suggestions;
    }

    /**
     * Gets the attribute and discipline for a downtime activity's task attempt.
     */
    public Map<String, String> getActivityTaskAttempt(DowntimeActivity activity) {
        String[] parts = activity.getTaskAttempt().split(" \\+ ");
        return Map.of("attribute", parts[0], "discipline", parts[1]);
    }

    // ==================== Downtime Phase ====================

    /**
     * Starts a downtime phase for a character after completing a mission.
     * <p>
     * This performs the automatic downtime actions:
     * <ul>
     *   <li>Resolves any accumulated threat from the mission</li>
     *   <li>Recovers the character from injuries</li>
     *   <li>Repairs the starship's system stress</li>
     *   <li>Resets value used/challenged flags</li>
     * </ul>
     * </p>
     *
     * @param characterId the character entering downtime
     * @param missionThreat the threat accumulated during the mission
     * @return a map with the downtime results
     */
    @Transactional
    public Map<String, Object> startDowntime(Long characterId, int missionThreat) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Map<String, Object> result = new HashMap<>();
        result.put("characterId", characterId);
        result.put("characterName", character.getName());
        result.put("activities", new ArrayList<Map<String, Object>>());

        // 1. Resolve threat
        Map<String, Object> threatResult = resolveThreat(character, missionThreat);
        result.put("threatResolution", threatResult);

        // 2. Recover from injuries
        Map<String, Object> recoveryResult = injuryService.fullRecovery(characterId);
        result.put("recovery", recoveryResult);

        // 3. Repair starship
        if (character.getAssignedStarship() != null) {
            Map<String, Object> repairResult = repairStarship(character.getAssignedStarship().getId());
            result.put("starshipRepair", repairResult);
        }

        // 4. Reset value flags
        List<Map<String, Object>> valueResults = resetValueFlags(characterId);
        result.put("valueReset", valueResults);

        // 5. Suggest activities
        List<DowntimeActivity> suggestions = suggestActivities(character, missionThreat);
        result.put("suggestedActivities", suggestions.stream()
                .map(DowntimeActivity::name)
                .toList());

        log.info("Downtime started for {} — threat={}, recovery={}, starship={}",
                character.getName(), threatResult, recoveryResult,
                character.getAssignedStarship() != null ? "repaired" : "no ship");

        return result;
    }

    /**
     * Performs a specific downtime activity.
     * <p>
     * The player must provide the task attempt result (success/failure) from the dice roll.
     * The activity's effect depends on the result.
     * </p>
     *
     * @param characterId the character performing the activity
     * @param activity the downtime activity to perform
     * @param taskSuccess whether the task attempt succeeded
     * @return a map with the activity result
     */
    @Transactional
    public Map<String, Object> performActivity(Long characterId, DowntimeActivity activity, boolean taskSuccess) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Map<String, Object> result = new HashMap<>();
        result.put("characterId", characterId);
        result.put("activity", activity.name());
        result.put("success", taskSuccess);

        switch (activity) {
            case MAINTENANCE -> result.putAll(performMaintenance(character, taskSuccess));
            case LEAVE -> result.putAll(performLeave(character, characterId, taskSuccess));
            case RESEARCH -> result.putAll(performResearch(character, taskSuccess));
            case TRAINING -> result.putAll(performTraining(character, characterId, taskSuccess));
            case DIPLOMACY -> result.putAll(performDiplomacy(character, taskSuccess));
            case EXPLORATION -> result.putAll(performExploration(character, taskSuccess));
        }

        log.info("{} performed {} downtime activity — success={}",
                character.getName(), activity.getName(), taskSuccess);

        return result;
    }

    // ==================== Activity Implementations ====================

    private Map<String, Object> performMaintenance(CaptainLogCharacter character, boolean success) {
        Map<String, Object> result = new HashMap<>();

        if (success) {
            // Repair starship systems
            if (character.getAssignedStarship() != null) {
                Map<String, Object> repairResult = repairStarship(character.getAssignedStarship().getId());
                result.put("starshipRepair", repairResult);
            }

            // Roll on Maintenance Events table for bonus
            var event = probabilityMatrixService.rollMaintenanceEvent();
            result.put("bonusEvent", event.name());
            result.put("bonusEventDescription", event.getDescription());
            result.put("narrative", "Maintenance is complete. The ship is in excellent condition. " +
                    "Additionally, " + event.getDescription());
        } else {
            result.put("narrative", "Maintenance encounters unexpected complications. " +
                    "Some systems remain stressed, but the ship is operational.");
        }

        return result;
    }

    private Map<String, Object> performLeave(CaptainLogCharacter character, Long characterId, boolean success) {
        Map<String, Object> result = new HashMap<>();

        if (success) {
            Map<String, Object> recoveryResult = injuryService.fullRecovery(characterId);
            result.put("recovery", recoveryResult);
            result.put("narrative", character.getName() + " takes well-deserved leave and fully recovers. " +
                    "Time off restores energy and resolve.");
        } else {
            // Partial recovery — just heal 1 damage
            Map<String, Object> healResult = injuryService.healCharacter(characterId, 1);
            result.put("recovery", healResult);
            result.put("narrative", character.getName() + " attempts to relax, but worries about the mission keep " +
                    "them from fully recovering. Some healing occurs, but not complete rest.");
        }

        return result;
    }

    private Map<String, Object> performResearch(CaptainLogCharacter character, boolean success) {
        Map<String, Object> result = new HashMap<>();

        if (success) {
            // Research yields a boon on the next related task attempt
            result.put("boonOnNextMission", true);
            result.put("narrative", character.getName() + " makes a breakthrough in their research. " +
                    "The findings will provide valuable insight on the next mission.");
        } else {
            result.put("boonOnNextMission", false);
            result.put("narrative", character.getName() + " spends time researching, but the results are inconclusive. " +
                    "More data is needed before any conclusions can be drawn.");
        }

        return result;
    }

    private Map<String, Object> performTraining(CaptainLogCharacter character, Long characterId, boolean success) {
        Map<String, Object> result = new HashMap<>();

        if (success) {
            // Gain 1 XP toward career upgrade
            careerProgressionService.awardXp(characterId, 1);
            result.put("xpGained", 1);
            result.put("currentXp", character.getExperiencePoints() + 1);
            result.put("narrative", character.getName() + " completes an intensive training session and feels " +
                    "sharper than before. The effort earns recognition from superiors.");
        } else {
            result.put("xpGained", 0);
            result.put("narrative", character.getName() + " attempts training, but the session is disrupted. " +
                    "No meaningful progress is made this time.");
        }

        return result;
    }

    private Map<String, Object> performDiplomacy(CaptainLogCharacter character, boolean success) {
        Map<String, Object> result = new HashMap<>();

        if (success) {
            // Generate a contact who can help
            var npc = probabilityMatrixService.generateNpc();
            result.put("contact", npc);
            result.put("boonToken", true);
            result.put("narrative", character.getName() + " meets with " + npc.get("name") + ", " +
                    npc.get("role") + ". They offer assistance on the next mission — a single boon on a related task.");
        } else {
            result.put("narrative", character.getName() + " attempts to reach out to contacts, but everyone is " +
                    "unavailable or unhelpful. No new alliances are formed.");
        }

        return result;
    }

    private Map<String, Object> performExploration(CaptainLogCharacter character, boolean success) {
        Map<String, Object> result = new HashMap<>();

        if (success) {
            // Roll on encounter table
            var encounter = probabilityMatrixService.rollEncounter();
            result.put("encounter", encounter.name());
            result.put("encounterDescription", encounter.getDescription());
            result.put("narrative", character.getName() + " ventures into nearby space and discovers something " +
                    "unexpected: " + encounter.getDescription());
        } else {
            result.put("narrative", character.getName() + " explores the area but finds nothing of interest. " +
                    "The sector appears quiet — for now.");
        }

        return result;
    }

    // ==================== Threat Resolution ====================

    /**
     * Resolves accumulated threat from a mission.
     * <p>
     * For each point of threat, a complication occurs during downtime.
     * The player can spend Momentum to remove Threat before resolution.
     * </p>
     *
     * @param character the character resolving threat
     * @param threat the amount of threat to resolve
     * @return a map with the resolution results
     */
    Map<String, Object> resolveThreat(CaptainLogCharacter character, int threat) {
        Map<String, Object> result = new HashMap<>();
        result.put("threatResolved", threat);
        result.put("complications", new ArrayList<String>());

        if (threat <= 0) {
            result.put("narrative", "No threat to resolve. The mission concluded without complications.");
            return result;
        }

        for (int i = 0; i < threat; i++) {
            // Roll on complication table
            var complication = probabilityMatrixService.rollComplication();
            String complicationText = complication.getDescription();
            ((List<String>) result.get("complications")).add(complicationText);
            log.info("Threat complication for {}: {}", character.getName(), complicationText);
        }

        result.put("narrative", threat + " point" + (threat > 1 ? "s" : "") +
                " of threat resolved. Complications arise during downtime: " +
                String.join("; ", (List<String>) result.get("complications")));

        return result;
    }

    // ==================== Starship Repairs ====================

    /**
     * Repairs a starship by reducing system stress.
     * <p>
     * During downtime, all stressed systems are reduced by 1.
     * </p>
     *
     * @param starshipId the starship to repair
     * @return a map with the repair results
     */
    @Transactional
    Map<String, Object> repairStarship(Long starshipId) {
        CaptainLogStarship starship = starshipRepository.findById(starshipId)
                .orElseThrow(() -> new IllegalArgumentException("Starship not found: " + starshipId));

        Map<String, Object> result = new HashMap<>();
        result.put("starshipId", starshipId);
        result.put("starshipName", starship.getName());
        result.put("systemsRepaired", new ArrayList<String>());

        int totalReduced = 0;

        // Reduce stress on all systems
        if (starship.getCommsStress() > 0) {
            starship.reduceSystemStress(CaptainLogStarship.StarshipSystem.COMMS, 1);
            totalReduced++;
            ((List<String>) result.get("systemsRepaired")).add("Comms");
        }
        if (starship.getComputersStress() > 0) {
            starship.reduceSystemStress(CaptainLogStarship.StarshipSystem.COMPUTERS, 1);
            totalReduced++;
            ((List<String>) result.get("systemsRepaired")).add("Computers");
        }
        if (starship.getEnginesStress() > 0) {
            starship.reduceSystemStress(CaptainLogStarship.StarshipSystem.ENGINES, 1);
            totalReduced++;
            ((List<String>) result.get("systemsRepaired")).add("Engines");
        }
        if (starship.getSensorsStress() > 0) {
            starship.reduceSystemStress(CaptainLogStarship.StarshipSystem.SENSORS, 1);
            totalReduced++;
            ((List<String>) result.get("systemsRepaired")).add("Sensors");
        }
        if (starship.getStructureStress() > 0) {
            starship.reduceSystemStress(CaptainLogStarship.StarshipSystem.STRUCTURE, 1);
            totalReduced++;
            ((List<String>) result.get("systemsRepaired")).add("Structure");
        }
        if (starship.getWeaponsStress() > 0) {
            starship.reduceSystemStress(CaptainLogStarship.StarshipSystem.WEAPONS, 1);
            totalReduced++;
            ((List<String>) result.get("systemsRepaired")).add("Weapons");
        }

        starshipRepository.save(starship);
        result.put("totalSystemsRepaired", totalReduced);
        result.put("narrative", totalReduced > 0 ?
                totalReduced + " system" + (totalReduced > 1 ? "s" : "") + " repaired on " + starship.getName() + "." :
                "All systems on " + starship.getName() + " are already at full capacity.");

        return result;
    }

    // ==================== Value Reflection ====================

    /**
     * Resets the used/challenged flags on all values after a mission.
     * <p>
     * Returns the list of values that were used or challenged during the mission,
     * so the player can decide whether to retire any of them.
     * </p>
     *
     * @param characterId the character whose values to reset
     * @return a list of maps with value info
     */
    @Transactional
    List<Map<String, Object>> resetValueFlags(Long characterId) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        List<CaptainLogValue> values = valueRepository.findByCharacterId(characterId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (CaptainLogValue value : values) {
            Map<String, Object> valueInfo = new HashMap<>();
            valueInfo.put("id", value.getId());
            valueInfo.put("description", value.getDescription());
            valueInfo.put("wasUsed", value.isUsed());
            valueInfo.put("wasChallenged", value.isChallenged());

            // Reset flags
            value.setUsed(false);
            value.setChallenged(false);
            valueRepository.save(value);

            result.add(valueInfo);
        }

        return result;
    }

    /**
     * Guides the player through value reflection after a mission.
     * <p>
     * The player reviews their values and decides:
     * <ul>
     *   <li>Which values were used during the mission?</li>
     *   <li>Which values were challenged?</li>
     *   <li>Should any value be retired?</li>
     *   <li>Should a new value be added?</li>
     * </ul>
     * </p>
     *
     * @param characterId the character reflecting on values
     * @param usedValueIds the IDs of values used during the mission
     * @param challengedValueIds the IDs of values challenged during the mission
     * @param retiredValueId the ID of the value to retire (optional)
     * @param newValueDescription the description of a new value to add (optional)
     * @return a map with the reflection results
     */
    @Transactional
    public Map<String, Object> reflectOnValues(Long characterId,
                                                List<Long> usedValueIds,
                                                List<Long> challengedValueIds,
                                                Long retiredValueId,
                                                String newValueDescription) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Map<String, Object> result = new HashMap<>();
        result.put("characterId", characterId);
        result.put("characterName", character.getName());

        // Mark used values
        for (Long id : usedValueIds) {
            valueRepository.findById(id).ifPresent(value -> {
                if (value.getCharacter().getId().equals(characterId)) {
                    value.setUsed(true);
                    valueRepository.save(value);
                }
            });
        }
        result.put("usedValues", usedValueIds);

        // Mark challenged values
        for (Long id : challengedValueIds) {
            valueRepository.findById(id).ifPresent(value -> {
                if (value.getCharacter().getId().equals(characterId)) {
                    value.setChallenged(true);
                    valueRepository.save(value);
                }
            });
        }
        result.put("challengedValues", challengedValueIds);

        // Retire a value (grants 1 XP)
        if (retiredValueId != null) {
            Optional<CaptainLogValue> retiredValue = valueRepository.findById(retiredValueId);
            if (retiredValue.isPresent() && retiredValue.get().getCharacter().getId().equals(characterId)) {
                String retiredDesc = retiredValue.get().getDescription();
                valueRepository.delete(retiredValue.get());
                careerProgressionService.awardXp(characterId, CareerProgressionService.XP_RETIRE_VALUE);
                result.put("retiredValue", retiredDesc);
                result.put("xpFromRetirement", CareerProgressionService.XP_RETIRE_VALUE);
                log.info("{} retired value '{}'", character.getName(), retiredDesc);
            }
        }

        // Add a new value (costs 2 XP)
        if (newValueDescription != null && !newValueDescription.isEmpty()) {
            int currentValues = valueRepository.findByCharacterId(characterId).size();
            if (currentValues < CareerProgressionService.MAX_VALUES) {
                if (character.getExperiencePoints() >= CareerProgressionService.XP_ADD_VALUE) {
                    CaptainLogValue newValue = new CaptainLogValue(newValueDescription);
                    newValue.setCharacter(character);
                    valueRepository.save(newValue);
                    careerProgressionService.spendXp(characterId, CareerProgressionService.XP_ADD_VALUE);
                    result.put("newValue", newValueDescription);
                    result.put("xpCost", CareerProgressionService.XP_ADD_VALUE);
                    log.info("{} gained new value: '{}'", character.getName(), newValueDescription);
                } else {
                    result.put("newValueError", "Not enough XP to add a new value. " +
                            "Need " + CareerProgressionService.XP_ADD_VALUE + " XP, have " +
                            character.getExperiencePoints() + ".");
                }
            } else {
                result.put("newValueError", "Maximum number of values reached (" +
                        CareerProgressionService.MAX_VALUES + "). Retire one first.");
            }
        }

        // Build narrative summary
        StringBuilder narrative = new StringBuilder();
        narrative.append(character.getName()).append(" reflects on the mission and their core values.\n");

        if (!usedValueIds.isEmpty()) {
            narrative.append("Values upheld: ").append(usedValueIds.size()).append(".\n");
        }
        if (!challengedValueIds.isEmpty()) {
            narrative.append("Values challenged: ").append(challengedValueIds.size()).append(".\n");
        }
        if (retiredValueId != null) {
            narrative.append("A value was retired, making room for growth.\n");
        }
        if (newValueDescription != null && !newValueDescription.isEmpty()) {
            narrative.append("A new value was adopted: '").append(newValueDescription).append("'.\n");
        }

        result.put("narrative", narrative.toString());
        return result;
    }

    /**
     * Gets all available downtime activities with descriptions.
     */
    public List<Map<String, String>> getAllActivities() {
        List<Map<String, String>> activities = new ArrayList<>();
        for (DowntimeActivity activity : DowntimeActivity.values()) {
            Map<String, String> activityInfo = new HashMap<>();
            activityInfo.put("name", activity.name());
            activityInfo.put("title", activity.getName());
            activityInfo.put("description", activity.getDescription());
            activityInfo.put("taskAttempt", activity.getTaskAttempt());
            activityInfo.put("effect", activity.getEffect());
            activities.add(activityInfo);
        }
        return activities;
    }
}
