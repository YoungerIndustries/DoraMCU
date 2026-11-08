package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.CreateMissionRequest;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogEntry;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogEntryRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogMissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Handles mission lifecycle and log entry management for the Captain's Log system.
 * <p>
 * Extracted from {@link CaptainLogService} to isolate mission operations
 * from character, dice, and AI logic.
 * </p>
 */
@Component
public class CaptainLogMissionService {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogMissionService.class);

    private final CaptainLogCharacterRepository characterRepository;
    private final CaptainLogMissionRepository missionRepository;
    private final CaptainLogEntryRepository logEntryRepository;

    public CaptainLogMissionService(CaptainLogCharacterRepository characterRepository,
                          CaptainLogMissionRepository missionRepository,
                          CaptainLogEntryRepository logEntryRepository) {
        this.characterRepository = characterRepository;
        this.missionRepository = missionRepository;
        this.logEntryRepository = logEntryRepository;
    }

    // ==================== Mission Management ====================

    /**
     * Creates a new mission for a character.
     */
    @Transactional
    public CaptainLogMission createMission(CreateMissionRequest request) {
        log.info("Creating mission '{}' for character {}", request.getName(), request.getCharacterId());

        CaptainLogCharacter character = characterRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + request.getCharacterId()));

        // Determine next mission number
        List<CaptainLogMission> existingMissions = missionRepository
                .findByCharacterIdOrderByMissionNumberAsc(request.getCharacterId());
        int nextNumber = existingMissions.size() + 1;

        CaptainLogMission mission = new CaptainLogMission(character, nextNumber, request.getName());
        mission.setType(request.getType());
        mission.setStatus(CaptainLogMission.MissionStatus.IN_PROGRESS);

        return missionRepository.save(mission);
    }

    /**
     * Retrieves a mission by ID.
     */
    public Optional<CaptainLogMission> getMission(Long id) {
        return missionRepository.findById(id);
    }

    /**
     * Lists all missions for a character.
     */
    public List<CaptainLogMission> listMissions(Long characterId) {
        return missionRepository.findByCharacterIdOrderByMissionNumberAsc(characterId);
    }

    /**
     * Saves an updated mission entity.
     */
    @Transactional
    public CaptainLogMission saveMission(CaptainLogMission mission) {
        return missionRepository.save(mission);
    }

    /**
     * Spends Momentum on a mission.
     * @param missionId the mission ID
     * @param amount amount to spend (typically 1)
     * @return the updated mission
     */
    @Transactional
    public CaptainLogMission spendMomentum(Long missionId, int amount) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        int newMomentum = Math.max(0, mission.getMomentum() - amount);
        mission.setMomentum(newMomentum);
        log.info("Spent {} momentum on mission {}. Remaining: {}", amount, missionId, newMomentum);
        return missionRepository.save(mission);
    }

    /**
     * Adds Momentum to a mission (e.g., from narrative advantage).
     * @param missionId the mission ID
     * @param amount amount to add (capped at max 1 in Captain's Log)
     * @return the updated mission
     */
    @Transactional
    public CaptainLogMission addMomentum(Long missionId, int amount) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        mission.setMomentum(mission.getMomentum() + amount);
        log.info("Added {} momentum to mission {}. Current: {}", amount, missionId, mission.getMomentum());
        return missionRepository.save(mission);
    }

    /**
     * Spends Threat on a mission to gain a boon.
     * @param missionId the mission ID
     * @param amount amount to spend
     * @return the updated mission
     */
    @Transactional
    public CaptainLogMission spendThreat(Long missionId, int amount) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        int newThreat = Math.max(0, mission.getThreat() - amount);
        mission.setThreat(newThreat);
        log.info("Spent {} threat on mission {}. Remaining: {}", amount, missionId, newThreat);
        return missionRepository.save(mission);
    }

    /**
     * Advances the narrative beat for a mission.
     * @param missionId the mission ID
     * @return the updated mission
     */
    @Transactional
    public CaptainLogMission advanceBeat(Long missionId) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        boolean actAdvanced = mission.advanceBeat();
        log.info("Advanced beat on mission {}. Act={}, Beat={}. Act advanced: {}",
                missionId, mission.getCurrentAct(), mission.getCurrentBeat(), actAdvanced);
        return missionRepository.save(mission);
    }

    /**
     * Updates a mission's act log.
     */
    @Transactional
    public CaptainLogMission updateMissionLog(Long missionId, int act, String logText) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

        switch (act) {
            case 1:
                mission.setAct1Log(logText);
                break;
            case 2:
                mission.setAct2Log(logText);
                break;
            case 3:
                mission.setAct3Log(logText);
                break;
            default:
                throw new IllegalArgumentException("Invalid act number: " + act);
        }

        mission.setCurrentAct(Math.max(mission.getCurrentAct(), act));
        return missionRepository.save(mission);
    }

    /**
     * Completes a mission and triggers career event if applicable.
     */
    @Transactional
    public CaptainLogMission completeMission(Long missionId, String summary, String development) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

        mission.setStatus(CaptainLogMission.MissionStatus.COMPLETED);
        mission.setSummary(summary);
        mission.setDevelopment(development);
        mission.setCompletedAt(LocalDateTime.now());

        // Check for career event (every 3rd mission)
        if (mission.getMissionNumber() % 3 == 0) {
            mission.setCareerEvent(true);
            mission.setCareerEventDescription(generateCareerEvent(mission.getMissionNumber()));

            // Update character's career events
            CaptainLogCharacter character = mission.getCharacter();
            character.setCareerEvents(character.getCareerEvents() + 1);
            character.setMissionsCompleted(character.getMissionsCompleted() + 1);
            characterRepository.save(character);
        } else {
            CaptainLogCharacter character = mission.getCharacter();
            character.setMissionsCompleted(character.getMissionsCompleted() + 1);
            characterRepository.save(character);
        }

        return missionRepository.save(mission);
    }

    /**
     * Generates a career event description based on mission number.
     */
    String generateCareerEvent(int missionNumber) {
        String[] events = {
            "Promotion: Your service has been recognized. Gain a rank advancement.",
            "New Assignment: You've been transferred to a new post with different challenges.",
            "Personal Loss: A close companion has been lost. Reflect on your values.",
            "Discovery: You've made a significant discovery that changes your perspective.",
            "Political Intrigue: Your career is entangled in Federation politics.",
            "Mentorship: A senior officer takes you under their wing, offering guidance.",
            "Field Promotion: Exceptional performance earns you temporary command.",
            "Reputation: Your actions have made you known across the sector.",
            "New Technology: You've been assigned to test experimental equipment.",
            "Diplomatic Incident: Your mission has political ramifications.",
            "Crew Bond: Your crew has become like family. They follow you anywhere.",
            "Scar: A physical or emotional scar from past missions remains.",
            "Award: You've received a commendation for your service.",
            "Rival: A rival officer challenges your methods and decisions.",
            "Retirement Option: You've completed your tour. Consider your next steps."
        };
        return events[missionNumber % events.length];
    }

    /**
     * Deletes a mission by ID.
     */
    @Transactional
    public void deleteMission(Long id) {
        missionRepository.deleteById(id);
        log.info("Deleted mission: {}", id);
    }

    // ==================== Log Entry Management ====================

    /**
     * Creates a new log entry for a mission.
     */
    @Transactional
    public CaptainLogEntry createLogEntry(Long missionId, CaptainLogEntry.EntryType type, int act, String text) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

        CaptainLogEntry entry = new CaptainLogEntry(act, type, text);
        entry.setMission(mission);
        return logEntryRepository.save(entry);
    }

    /**
     * Lists all log entries for a mission.
     */
    public List<CaptainLogEntry> listLogEntries(Long missionId) {
        return logEntryRepository.findByMissionIdOrderByCreatedAtAsc(missionId);
    }
}
