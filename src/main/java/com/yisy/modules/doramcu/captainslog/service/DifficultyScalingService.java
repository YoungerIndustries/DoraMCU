package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Calculates suggested difficulty for task attempts based on mission context.
 * <p>
 * In the Captain's Log system, difficulty represents the number of successes needed
 * on a 2d20 task attempt. The default is 1, but harder situations require more successes.
 * This service provides automated difficulty scaling based on:
 * <ul>
 *   <li>Mission number — later missions are more challenging</li>
 *   <li>Character rank — higher ranks face harder challenges</li>
 *   <li>Mission type — crisis and combat missions are inherently harder</li>
 * </ul>
 * </p>
 * <p>
 * The formula produces a suggested difficulty (1-3) and can also suggest
 * additional threats to add to a task attempt for extra tension.
 * </p>
 */
@Component
public class DifficultyScalingService {

    private static final Logger log = LoggerFactory.getLogger(DifficultyScalingService.class);

    /**
     * Calculates the suggested difficulty for a task attempt based on mission context.
     * <p>
     * Formula:
     * <ul>
     *   <li>Base difficulty = 1</li>
     *   <li>Mission number modifier: +1 if missionNumber >= 8, +2 if missionNumber >= 12</li>
     *   <li>Rank modifier: +1 if rank >= CAPTAIN</li>
     *   <li>Mission type modifier: +1 for COMBAT or CRISIS</li>
     * </ul>
     * The result is capped between 1 and 3.
     * </p>
     *
     * @param mission the current mission (may be null for standalone checks)
     * @param character the character performing the task (may be null)
     * @return the suggested difficulty (1-3)
     */
    public int calculateDifficulty(CaptainLogMission mission, CaptainLogCharacter character) {
        int difficulty = 1; // Base difficulty

        if (mission != null) {
            // Mission number scaling: later missions are harder
            int missionNumber = mission.getMissionNumber();
            if (missionNumber >= 12) {
                difficulty += 2;
            } else if (missionNumber >= 8) {
                difficulty += 1;
            }

            // Mission type scaling: some types are inherently harder
            CaptainLogMission.MissionType type = mission.getType();
            if (type != null) {
                difficulty += getTypeDifficultyModifier(type);
            }
        }

        if (character != null) {
            // Rank scaling: higher ranks face harder challenges
            CaptainLogCharacter.Rank rank = character.getRank();
            if (rank != null) {
                difficulty += getRankDifficultyModifier(rank);
            }
        }

        // Cap difficulty between 1 and 3
        difficulty = Math.max(1, Math.min(3, difficulty));

        log.debug("Calculated difficulty: {} (mission={}, character={})",
                difficulty, mission != null ? mission.getMissionNumber() : "null",
                character != null ? character.getName() : "null");

        return difficulty;
    }

    /**
     * Calculates the suggested number of threats to add to a task attempt.
     * <p>
     * Higher mission numbers and crisis/combat types generate more threat.
     * </p>
     *
     * @param mission the current mission (may be null)
     * @return the suggested number of threats (0-2)
     */
    public int calculateThreats(CaptainLogMission mission) {
        if (mission == null) {
            return 0;
        }

        int threats = 0;

        // Mission number scaling
        int missionNumber = mission.getMissionNumber();
        if (missionNumber >= 10) {
            threats += 1;
        }
        if (missionNumber >= 14) {
            threats += 1;
        }

        // Mission type scaling
        CaptainLogMission.MissionType type = mission.getType();
        if (type != null) {
            switch (type) {
                case CRISIS -> threats += 1;
                case COMBAT -> threats += 1;
                case FIRST_CONTACT -> threats += 1;
                default -> { /* no extra threat */ }
            }
        }

        // Cap threats at 2
        threats = Math.min(2, threats);

        log.debug("Calculated threats: {} for mission {}", threats, mission.getMissionNumber());
        return threats;
    }

    /**
     * Returns a human-readable description of why the difficulty was set to this level.
     */
    public String getDifficultyDescription(CaptainLogMission mission, CaptainLogCharacter character, int difficulty) {
        StringBuilder desc = new StringBuilder();

        if (difficulty == 1) {
            desc.append("Standard difficulty — a routine task.");
        } else if (difficulty == 2) {
            desc.append("Elevated difficulty — the situation is challenging.");
        } else {
            desc.append("Extreme difficulty — this is a critical, high-stakes task.");
        }

        if (mission != null) {
            int missionNumber = mission.getMissionNumber();
            if (missionNumber >= 8) {
                desc.append(" (Mission #").append(missionNumber).append(" adds pressure.)");
            }
            CaptainLogMission.MissionType type = mission.getType();
            if (type != null && (type == CaptainLogMission.MissionType.COMBAT ||
                    type == CaptainLogMission.MissionType.CRISIS)) {
                desc.append(" (").append(type).append(" missions are inherently harder.)");
            }
        }

        if (character != null) {
            CaptainLogCharacter.Rank rank = character.getRank();
            if (rank != null && rank.ordinal() >= CaptainLogCharacter.Rank.CAPTAIN.ordinal()) {
                desc.append(" (").append(rank).append(" rank faces greater challenges.)");
            }
        }

        return desc.toString();
    }

    /**
     * Returns the difficulty modifier for a mission type.
     */
    private int getTypeDifficultyModifier(CaptainLogMission.MissionType type) {
        return switch (type) {
            case COMBAT, CRISIS -> 1;
            case FIRST_CONTACT, INVESTIGATION -> 0;
            case EXPLORATION, SCIENCE, RESCUE, DIPLOMACY, TRADE, PERSONAL -> 0;
        };
    }

    /**
     * Returns the difficulty modifier for a character rank.
     */
    private int getRankDifficultyModifier(CaptainLogCharacter.Rank rank) {
        return switch (rank) {
            case CAPTAIN, COMMANDING_OFFICER -> 1;
            case ENSIGN, LIEUTENANT_JUNIOR, LIEUTENANT, LIEUTENANT_COMMANDER, COMMANDER -> 0;
        };
    }
}
