package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.*;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCombat;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCombatRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogMissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Handles combat encounters for the Captain's Log system.
 * <p>
 * Combat follows the simplified Captain's Log rules:
 * <ul>
 *   <li>Each round: perform a task attempt</li>
 *   <li>Success = deal 1 hit to opponent</li>
 *   <li>Failure = take 1 hit yourself</li>
 *   <li>Natural 20 = complication (generates Threat)</li>
 *   <li>Extra success = gain Momentum</li>
 *   <li>Repeat until one side is defeated</li>
 * </ul>
 * </p>
 */
@Component
public class CaptainLogCombatService {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogCombatService.class);

    private final CaptainLogCombatRepository combatRepository;
    private final CaptainLogMissionRepository missionRepository;
    private final CaptainLogCharacterRepository characterRepository;
    private final DiceService diceService;

    public CaptainLogCombatService(CaptainLogCombatRepository combatRepository,
                                    CaptainLogMissionRepository missionRepository,
                                    CaptainLogCharacterRepository characterRepository,
                                    DiceService diceService) {
        this.combatRepository = combatRepository;
        this.missionRepository = missionRepository;
        this.characterRepository = characterRepository;
        this.diceService = diceService;
    }

    /**
     * Starts a new combat encounter.
     */
    @Transactional
    public CaptainLogCombat startCombat(StartCombatRequest request) {
        log.info("Starting combat: {} vs {}", request.getCharacterId(), request.getOpponentName());

        CaptainLogMission mission = missionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + request.getMissionId()));
        CaptainLogCharacter character = characterRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + request.getCharacterId()));

        CaptainLogCombat combat;
        if (request.getType() == CaptainLogCombat.CombatType.STARSHIP) {
            CaptainLogCombat.OpponentScale scale = request.getOpponentScale() != null
                    ? request.getOpponentScale() : CaptainLogCombat.OpponentScale.MEDIUM;
            combat = new CaptainLogCombat(mission, character, request.getOpponentName(), scale, request.getDescription());
        } else {
            combat = new CaptainLogCombat(mission, character, request.getOpponentName(), request.getDescription());
        }

        // Set the attribute/discipline for task attempts
        if (request.getAttribute() != null) {
            combat.setCharacterAttribute(request.getAttribute().name());
        }
        if (request.getDiscipline() != null) {
            combat.setCharacterDiscipline(request.getDiscipline().name());
        }

        return combatRepository.save(combat);
    }

    /**
     * Retrieves a combat by ID.
     */
    public Optional<CaptainLogCombat> getCombat(Long id) {
        return combatRepository.findById(id);
    }

    /**
     * Lists all combats for a mission.
     */
    public List<CaptainLogCombat> listCombats(Long missionId) {
        return combatRepository.findByMissionIdOrderByCreatedAtDesc(missionId);
    }

    /**
     * Runs a single combat round.
     * <p>
     * The character performs a task attempt. On success, the opponent takes 1 hit.
     * On failure, the character takes 1 hit. Complications generate Threat.
     * Extra successes generate Momentum.
     * </p>
     */
    @Transactional
    public CombatRoundResponse runCombatRound(Long combatId) {
        CaptainLogCombat combat = combatRepository.findById(combatId)
                .orElseThrow(() -> new IllegalArgumentException("Combat not found: " + combatId));

        if (combat.getStatus() != CaptainLogCombat.CombatStatus.ACTIVE) {
            throw new IllegalStateException("Combat is not active: " + combat.getStatus());
        }

        // Advance round
        combat.setCurrentRound(combat.getCurrentRound() + 1);

        // Build task attempt request
        TaskAttemptRequest request = new TaskAttemptRequest();
        request.setCharacterId(combat.getCharacter().getId());
        request.setMissionId(combat.getMission().getId());

        // Set attribute and discipline
        if (combat.getCharacterAttribute() != null) {
            request.setAttribute(CaptainLogCharacter.Attribute.valueOf(combat.getCharacterAttribute()));
        } else {
            request.setAttribute(CaptainLogCharacter.Attribute.FITNESS);
        }
        if (combat.getCharacterDiscipline() != null) {
            request.setDiscipline(CaptainLogCharacter.Discipline.valueOf(combat.getCharacterDiscipline()));
        } else {
            request.setDiscipline(CaptainLogCharacter.Discipline.SECURITY);
        }
        request.setDifficulty(1);

        // Roll the task attempt
        CaptainLogCharacter character = combat.getCharacter();
        TaskAttemptResponse taskResult = diceService.rollTaskAttempt(request, character);

        // Build response
        CombatRoundResponse response = new CombatRoundResponse();
        response.setCombatId(combat.getId());
        response.setRound(combat.getCurrentRound());
        response.setTaskAttempt(taskResult);

        // Apply results
        StringBuilder desc = new StringBuilder();
        desc.append("Round ").append(combat.getCurrentRound()).append(": ");

        if (taskResult.isSuccess()) {
            combat.setOpponentHits(Math.max(0, combat.getOpponentHits() - 1));
            desc.append("You strike ").append(combat.getOpponentName())
                .append(" for 1 hit! (").append(combat.getOpponentHits())
                .append("/").append(combat.getOpponentMaxHits()).append(" remaining)");
        } else {
            combat.setCharacterHits(Math.max(0, combat.getCharacterHits() - 1));
            desc.append(combat.getOpponentName()).append(" strikes you for 1 hit! (")
                .append(combat.getCharacterHits()).append("/").append(combat.getCharacterMaxHits())
                .append(" remaining)");
        }

        if (taskResult.isComplication()) {
            desc.append(" Complication! Something goes wrong.");
        }
        if (taskResult.getMomentumGenerated() > 0) {
            desc.append(" You gain 1 Momentum.");
        }

        // Check for combat end
        boolean ended = false;
        if (combat.getOpponentHits() <= 0) {
            combat.setStatus(CaptainLogCombat.CombatStatus.VICTORY);
            combat.setEndedAt(LocalDateTime.now());
            combat.setResultSummary(combat.getOpponentName() + " has been defeated in " + combat.getCurrentRound() + " rounds.");
            desc.append(" Victory! ").append(combat.getOpponentName()).append(" is defeated.");
            ended = true;
        } else if (combat.getCharacterHits() <= 0) {
            combat.setStatus(CaptainLogCombat.CombatStatus.DEFEAT);
            combat.setEndedAt(LocalDateTime.now());
            combat.setResultSummary("Defeated by " + combat.getOpponentName() + " after " + combat.getCurrentRound() + " rounds.");
            desc.append(" Defeat! You have been overwhelmed.");
            ended = true;
        }

        // Update mission threat pool from complications
        if (taskResult.getThreatGenerated() > 0) {
            combat.getMission().setThreat(combat.getMission().getThreat() + taskResult.getThreatGenerated());
        }

        combatRepository.save(combat);

        response.setCharacterHits(combat.getCharacterHits());
        response.setCharacterMaxHits(combat.getCharacterMaxHits());
        response.setOpponentHits(combat.getOpponentHits());
        response.setOpponentMaxHits(combat.getOpponentMaxHits());
        response.setCombatEnded(ended);
        response.setStatus(combat.getStatus().name());
        response.setDescription(desc.toString());

        log.info("Combat round {}: {} (ended={})", combat.getCurrentRound(), desc, ended);
        return response;
    }

    /**
     * Retreats from an active combat.
     */
    @Transactional
    public CaptainLogCombat retreat(Long combatId) {
        CaptainLogCombat combat = combatRepository.findById(combatId)
                .orElseThrow(() -> new IllegalArgumentException("Combat not found: " + combatId));

        if (combat.getStatus() != CaptainLogCombat.CombatStatus.ACTIVE) {
            throw new IllegalStateException("Combat is not active: " + combat.getStatus());
        }

        combat.setStatus(CaptainLogCombat.CombatStatus.RETREAT);
        combat.setEndedAt(LocalDateTime.now());
        combat.setResultSummary("Retreated from combat with " + combat.getOpponentName() + " after " + combat.getCurrentRound() + " rounds.");

        log.info("Retreated from combat: {}", combatId);
        return combatRepository.save(combat);
    }
}
