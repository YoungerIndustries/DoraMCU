package com.yisy.modules.doramcu.unified.adapter;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.Career;
import com.yisy.modules.doramcu.traveller.ExplorationActivity;
import com.yisy.modules.doramcu.traveller.MissionType;
import com.yisy.modules.doramcu.traveller.Skill;
import com.yisy.modules.doramcu.traveller.TradeMission;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;

/**
 * Utility for mapping Traveller game concepts to Captain's Log concepts.
 * <p>
 * This converter provides the bridge between the two game systems:
 * </p>
 * <ul>
 *   <li>Traveller careers → Captain's Log assignments</li>
 *   <li>Traveller mission types → Captain's Log mission types</li>
 *   <li>Traveller ranks → Captain's Log ranks</li>
 *   <li>Traveller abilities → Captain's Log attributes</li>
 * </ul>
 */
public final class TravellerToCaptainsLogConverter {

    private TravellerToCaptainsLogConverter() {
        // Utility class
    }

    // ============================================================
    // Career → Assignment Mapping
    // ============================================================

    /**
     * Maps a Traveller career to a Captain's Log assignment.
     * <p>
     * Mapping logic:
     * </p>
     * <ul>
     *   <li>SOLDIER → SECURITY (combat-focused)</li>
     *   <li>PILOT → OPERATIONS (ship operations)</li>
     *   <li>GUNNER → SECURITY (weapons specialist)</li>
     *   <li>EXPLORER → SCIENCE (survey and discovery)</li>
     *   <li>MERCHANT → OPERATIONS (trade and logistics)</li>
     *   <li>NOBLE → COMMAND (leadership and diplomacy)</li>
     *   <li>NAVY → COMMAND (fleet command)</li>
     *   <li>MARINES → SECURITY (ground combat)</li>
     * </ul>
     *
     * @param career the Traveller career
     * @return the corresponding Captain's Log assignment
     */
    public static CaptainLogCharacter.Assignment mapCareerToAssignment(Career career) {
        if (career == null) {
            return CaptainLogCharacter.Assignment.COMMAND;
        }
        return switch (career) {
            case SOLDIER, GUNNER, SPY -> CaptainLogCharacter.Assignment.SECURITY;
            case EXPLORER, SCOUT -> CaptainLogCharacter.Assignment.SCIENCE;
            case MERCHANT, PILOT -> CaptainLogCharacter.Assignment.OPERATIONS;
            case NOBLE -> CaptainLogCharacter.Assignment.COMMAND;
        };
    }

    // ============================================================
    // Rank Mapping
    // ============================================================

    /**
     * Maps a Traveller rank string to a Captain's Log rank.
     * <p>
     * Traveller uses informal rank strings like "Enlisted", "Junior Officer", etc.
     * Captain's Log uses formal Star Trek-style ranks.
     * </p>
     *
     * @param travellerRank the Traveller rank string
     * @return the corresponding Captain's Log rank
     */
    public static CaptainLogCharacter.Rank mapRank(String travellerRank) {
        if (travellerRank == null || travellerRank.isBlank()) {
            return CaptainLogCharacter.Rank.ENSIGN;
        }
        return switch (travellerRank) {
            case "Enlisted" -> CaptainLogCharacter.Rank.ENSIGN;
            case "Senior Enlisted" -> CaptainLogCharacter.Rank.LIEUTENANT_JUNIOR;
            case "Junior Officer" -> CaptainLogCharacter.Rank.LIEUTENANT;
            case "Officer" -> CaptainLogCharacter.Rank.LIEUTENANT_COMMANDER;
            case "Senior Officer" -> CaptainLogCharacter.Rank.COMMANDER;
            case "Commanding Officer" -> CaptainLogCharacter.Rank.CAPTAIN;
            default -> CaptainLogCharacter.Rank.ENSIGN;
        };
    }

    // ============================================================
    // Mission Type Mapping
    // ============================================================

    /**
     * Maps a Traveller mercenary MissionType to a Captain's Log MissionType.
     *
     * @param missionType the Traveller mission type
     * @return the corresponding Captain's Log mission type
     */
    public static CaptainLogMission.MissionType mapMissionType(MissionType missionType) {
        if (missionType == null) {
            return CaptainLogMission.MissionType.COMBAT;
        }
        return switch (missionType) {
            case COMMANDO -> CaptainLogMission.MissionType.COMBAT;
            case CADRE -> CaptainLogMission.MissionType.DIPLOMACY;
            case STRIKER -> CaptainLogMission.MissionType.COMBAT;
            case SECURITY -> CaptainLogMission.MissionType.CRISIS;
        };
    }

    /**
     * Maps a TradeMission status to a Captain's Log MissionType.
     *
     * @return TRADE mission type
     */
    public static CaptainLogMission.MissionType mapTradeMissionType() {
        return CaptainLogMission.MissionType.TRADE;
    }

    /**
     * Maps an ExplorationActivity type to a Captain's Log MissionType.
     *
     * @param activityType the exploration activity type
     * @return the corresponding Captain's Log mission type
     */
    public static CaptainLogMission.MissionType mapExplorationType(ExplorationActivity.ActivityType activityType) {
        if (activityType == null) {
            return CaptainLogMission.MissionType.EXPLORATION;
        }
        return switch (activityType) {
            case SURVEY_BODY, RESOURCE_SCAN, FULL_SYSTEM_SURVEY -> CaptainLogMission.MissionType.SCIENCE;
            case HAZARD_DETECTION, ROUTE_MAPPING, BEACON_DEPLOYMENT -> CaptainLogMission.MissionType.EXPLORATION;
        };
    }

    // ============================================================
    // Ability → Attribute Mapping
    // ============================================================

    /**
     * Maps a Traveller ability score to a Captain's Log attribute.
     * <p>
     * Traveller abilities: ST, DX, END, INT, EDU, SOC, APP
     * Captain's Log attributes: STR, DEX, CON, INT, WIS, CHA, TEC
     * </p>
     * <p>
     * Mapping:
     * </p>
     * <ul>
     *   <li>ST → STR (Strength)</li>
     *   <li>DX → DEX (Dexterity)</li>
     *   <li>END → CON (Constitution)</li>
     *   <li>INT → INT (Intelligence)</li>
     *   <li>EDU → WIS (Wisdom — education maps to knowledge/wisdom)</li>
     *   <li>SOC → CHA (Charisma — social maps to presence)</li>
     *   <li>APP → TEC (Technical — appearance/misc maps to technical)</li>
     * </ul>
     *
     * @param ability the Traveller ability
     * @return the attribute name used in Captain's Log setters
     */
    public static String mapAbilityToAttribute(AbilityScore ability) {
        if (ability == null) return null;
        return switch (ability) {
            case ST -> "strength";
            case DEX -> "dexterity";
            case END -> "constitution";
            case INT -> "intelligence";
            case EDU -> "wisdom";
            case SOC -> "charisma";
            case APP -> "technical";
        };
    }

    // ============================================================
    // Retirement Type → Mission Status Mapping
    // ============================================================

    /**
     * Maps a Traveller retirement type to a Captain's Log mission status.
     *
     * @param retirementType the retirement type
     * @return the corresponding mission status
     */
    public static CaptainLogMission.MissionStatus mapRetirementToStatus(
            TravellerCharacter.RetirementType retirementType) {
        if (retirementType == null) {
            return CaptainLogMission.MissionStatus.COMPLETED;
        }
        return switch (retirementType) {
            case NORMAL, HONORABLE -> CaptainLogMission.MissionStatus.COMPLETED;
            case MEDICAL, DISHONORABLE -> CaptainLogMission.MissionStatus.ABANDONED;
            case DEAD -> CaptainLogMission.MissionStatus.ABANDONED;
        };
    }

    // ============================================================
    // Narrative Helpers
    // ============================================================

    /**
     * Generates a mission name from a Traveller career and rank.
     *
     * @param career the career
     * @param rank the rank achieved
     * @param termNumber the term number (1-based)
     * @return a descriptive mission name
     */
    public static String generateCareerMissionName(Career career, String rank, int termNumber) {
        return String.format("Service Term %d: %s %s", termNumber, rank, career.getName());
    }

    /**
     * Generates a mission name from a mercenary ticket description.
     *
     * @param missionType the mercenary mission type
     * @param world the world name
     * @return a descriptive mission name
     */
    public static String generateMercenaryMissionName(MissionType missionType, String world) {
        return String.format("%s Operation: %s", missionType.getName(), world);
    }

    /**
     * Maps a Traveller career to a Captain's Log mission type.
     *
     * @param career the Traveller career
     * @return the corresponding Captain's Log mission type
     */
    public static CaptainLogMission.MissionType mapCareerToMissionType(Career career) {
        if (career == null) {
            return CaptainLogMission.MissionType.EXPLORATION;
        }
        return switch (career) {
            case SOLDIER, GUNNER -> CaptainLogMission.MissionType.COMBAT;
            case EXPLORER, SCOUT -> CaptainLogMission.MissionType.SCIENCE;
            case MERCHANT -> CaptainLogMission.MissionType.TRADE;
            case NOBLE -> CaptainLogMission.MissionType.DIPLOMACY;
            case PILOT -> CaptainLogMission.MissionType.EXPLORATION;
            case SPY -> CaptainLogMission.MissionType.INVESTIGATION;
        };
    }

    /**
     * Generates a mission name from a trade route.
     *
     * @param origin the origin world name
     * @param destination the destination world name
     * @return a descriptive mission name
     */
    public static String generateTradeMissionName(String origin, String destination) {
        return String.format("Trade Run: %s to %s", origin, destination);
    }

    /**
     * Generates a mission name from an exploration activity.
     *
     * @param activityType the exploration activity type
     * @param target the target description
     * @return a descriptive mission name
     */
    public static String generateExplorationMissionName(ExplorationActivity.ActivityType activityType,
                                                        String target) {
        return String.format("Exploration: %s — %s", activityType.name().replace("_", " "), target);
    }

    /**
     * Generates an Act 1 (Opening Log) narrative for a career term.
     *
     * @param characterName the character's name
     * @param career the career
     * @param rank the starting rank
     * @param termNumber the term number
     * @return the Act 1 narrative text
     */
    public static String generateCareerAct1(String characterName, Career career, String rank, int termNumber) {
        return String.format(
            "%s begins their %dnd term of service in the %s, holding the rank of %s. "
            + "The assignment brings new challenges and opportunities for growth. "
            + "During this term, %s will face the demands of %s service, "
            + "learning new skills and proving their worth.",
            characterName, termNumber, career.getName(), rank,
            characterName, career.getName().toLowerCase()
        );
    }

    /**
     * Generates an Act 2 (Supplementary Logs) narrative for a career term.
     *
     * @param skillsLearned the skills gained during the term
     * @param promoted whether the character was promoted
     * @param newRank the new rank if promoted, null otherwise
     * @return the Act 2 narrative text
     */
    public static String generateCareerAct2(java.util.List<Skill> skillsLearned,
                                            boolean promoted, String newRank) {
        StringBuilder sb = new StringBuilder();
        sb.append("The term progresses with increasing responsibility. ");

        if (!skillsLearned.isEmpty()) {
            sb.append("New skills are acquired: ");
            sb.append(skillsLearned.stream().map(Skill::getName).reduce((a, b) -> a + ", " + b).orElse(""));
            sb.append(". ");
        }

        if (promoted && newRank != null) {
            sb.append("Performance is recognized with a promotion to ").append(newRank).append(". ");
        } else {
            sb.append("Duties are carried out with dedication and professionalism. ");
        }

        return sb.toString();
    }

    /**
     * Generates an Act 3 (Closing Log) narrative for a career term.
     *
     * @param characterName the character's name
     * @param retired whether the character retired after this term
     * @param retirementType the retirement type, or null if not retired
     * @param wealthEarned the wealth earned during the term
     * @return the Act 3 narrative text
     */
    public static String generateCareerAct3(String characterName, boolean retired,
                                            TravellerCharacter.RetirementType retirementType,
                                            long wealthEarned) {
        if (!retired) {
            return String.format(
                "%s completes the term with %d credits earned. "
                + "The experience gained will serve them well in future endeavors.",
                characterName, wealthEarned
            );
        }

        String retirementDesc = retirementType != null ? retirementType.getName().toLowerCase() : "normal";
        return String.format(
            "%s concludes their service with a %s retirement, "
            + "earning %d credits. The years of service have shaped them into "
            + "a seasoned professional, ready for whatever comes next.",
            characterName, retirementDesc, wealthEarned
        );
    }

    /**
     * Generates a development summary for a career mission.
     *
     * @param skillsLearned the skills gained
     * @param promoted whether promoted
     * @return a development summary string
     */
    public static String generateCareerDevelopment(java.util.List<Skill> skillsLearned, boolean promoted) {
        StringBuilder sb = new StringBuilder();
        if (!skillsLearned.isEmpty()) {
            sb.append("Gained ").append(skillsLearned.size()).append(" new skill(s). ");
        }
        if (promoted) {
            sb.append("Promoted to higher rank. ");
        }
        if (sb.isEmpty()) {
            sb.append("Completed term of service with steady performance.");
        }
        return sb.toString();
    }
}
