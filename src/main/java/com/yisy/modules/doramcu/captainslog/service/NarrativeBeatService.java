package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.EnumMap;

/**
 * Provides structured narrative beat guidance for the Captain's Log Solo RPG.
 * <p>
 * The rulebook defines 15 narrative beats across 3 acts (5 beats per act).
 * Each beat has a specific purpose, narrative prompt, and suggested actions.
 * This service returns guidance tailored to the current act, beat, and mission type.
 * </p>
 * <p>
 * Act 1 (Opening Log): Establishes the situation and introduces the conflict.
 * Act 2 (Supplementary Logs): Develops the conflict and raises the stakes.
 * Act 3 (Closing Log): Resolves the mission and shows the consequences.
 * </p>
 */
@Component
public class NarrativeBeatService {

    private static final Logger log = LoggerFactory.getLogger(NarrativeBeatService.class);

    /**
     * Represents the guidance for a single narrative beat.
     */
    public static class BeatGuidance {
        private final int act;
        private final int beat;
        private final String title;
        private final String narrativePrompt;
        private final String suggestedAction;
        private final List<String> oracleQuestions;
        private final boolean requiresTaskAttempt;
        private final boolean isActTransition;

        public BeatGuidance(int act, int beat, String title, String narrativePrompt,
                           String suggestedAction, List<String> oracleQuestions,
                           boolean requiresTaskAttempt, boolean isActTransition) {
            this.act = act;
            this.beat = beat;
            this.title = title;
            this.narrativePrompt = narrativePrompt;
            this.suggestedAction = suggestedAction;
            this.oracleQuestions = oracleQuestions;
            this.requiresTaskAttempt = requiresTaskAttempt;
            this.isActTransition = isActTransition;
        }

        public int getAct() { return act; }
        public int getBeat() { return beat; }
        public String getTitle() { return title; }
        public String getNarrativePrompt() { return narrativePrompt; }
        public String getSuggestedAction() { return suggestedAction; }
        public List<String> getOracleQuestions() { return oracleQuestions; }
        public boolean isRequiresTaskAttempt() { return requiresTaskAttempt; }
        public boolean isActTransition() { return isActTransition; }
    }

    /**
     * All 15 narrative beats across 3 acts.
     * Each beat: [act][beat] = guidance
     */
    private static final BeatGuidance[][] BEATS = {
        // Act 1: Opening Log — Establish the situation
        {
            null, // beat 0 (1-indexed)
            new BeatGuidance(1, 1, "The Call to Action",
                "Your mission begins. What brings you to this moment? Describe the situation as you receive your orders or discover the problem that sets this mission in motion.",
                "Set the scene. Who gave you this mission? What do you know so far? What do you think the stakes are?",
                Arrays.asList(
                    "Is there something hidden about this mission?",
                    "Do you have all the information you need?",
                    "Is there a time constraint?"
                ),
                false, false),
            new BeatGuidance(1, 2, "Gathering Information",
                "Before acting, you need more information. What do you learn as you prepare? Roll on the Encounter table or use the oracle to discover what awaits.",
                "Ask the oracle about the situation. What resources do you have? Who can help?",
                Arrays.asList(
                    "What do sensors reveal about the target area?",
                    "Is there an unexpected complication already?",
                    "Do you have the right equipment for this task?"
                ),
                true, false),
            new BeatGuidance(1, 3, "First Contact or Discovery",
                "You encounter the first major element of the mission. This could be a person, a place, or a problem. Describe what you find and how it affects your plans.",
                "Perform a task attempt related to your mission type. This is your first real test.",
                Arrays.asList(
                    "Is this what you expected to find?",
                    "Is there someone else here?",
                    "Does this change your understanding of the mission?"
                ),
                true, false),
            new BeatGuidance(1, 4, "Complications Arise",
                "Things are not as simple as they seemed. A new complication emerges that challenges your initial approach. The situation becomes more complex.",
                "Roll on the Complication table or spend Threat to introduce a narrative twist.",
                Arrays.asList(
                    "Does the complication make the mission harder?",
                    "Is there someone responsible for this?",
                    "Can you overcome this with your current resources?"
                ),
                true, false),
            new BeatGuidance(1, 5, "The Point of No Return",
                "Act 1 concludes. You've gathered enough information and faced the first challenges. Now you must commit to a course of action. Write your Opening Log entry.",
                "Write an in-character log entry summarizing Act 1. Decide on your plan for Act 2.",
                Arrays.asList(
                    "Do you have a solid plan?",
                    "Is there momentum you can carry forward?",
                    "What threat looms largest?"
                ),
                false, true)
        },
        // Act 2: Supplementary Logs — Develop the conflict
        {
            null,
            new BeatGuidance(2, 1, "Raising the Stakes",
                "Act 2 begins. The conflict deepens. New information reveals the true scope of the problem. The stakes are higher than you thought.",
                "Describe how the situation has escalated. What new information do you have?",
                Arrays.asList(
                    "Has the enemy adapted to your tactics?",
                    "Is there a bigger threat behind this mission?",
                    "Do you need to change your approach?"
                ),
                false, false),
            new BeatGuidance(2, 2, "The Midpoint Crisis",
                "The heart of the mission. A major challenge or confrontation occurs. This is the turning point — success here gives you the upper hand, failure makes things worse.",
                "Perform a critical task attempt. This is the most important roll of the mission. Consider spending Momentum.",
                Arrays.asList(
                    "Do you succeed in your main objective?",
                    "Is there a cost to this success?",
                    "Does an unexpected ally appear?"
                ),
                true, false),
            new BeatGuidance(2, 3, "Consequences and Reactions",
                "The aftermath of the midpoint crisis. Deal with the consequences of your actions. Allies and enemies react to what just happened.",
                "Resolve the consequences. Roll on the Advantage or Complication table based on the midpoint outcome.",
                Arrays.asList(
                    "What is the immediate consequence?",
                    "How do your allies react?",
                    "What does the enemy do next?"
                ),
                true, false),
            new BeatGuidance(2, 4, "The Dark Moment",
                "All seems lost. The worst complication hits. This is the lowest point before the final push. What goes wrong when it can't get any worse?",
                "Roll on the Complication table. This is the biggest threat of the mission. Consider spending Threat for a Boon.",
                Arrays.asList(
                    "Is there any hope left?",
                    "What resource can you call upon?",
                    "Does someone sacrifice something to help?"
                ),
                true, false),
            new BeatGuidance(2, 5, "Gathering Strength",
                "Act 2 concludes. Despite the darkness, you find a way forward. A last resource, ally, or insight gives you the means to face the final act. Write your Supplementary Log.",
                "Write an in-character log entry. What gives you hope? What plan will you execute in Act 3?",
                Arrays.asList(
                    "Do you have a path to victory?",
                    "What will you sacrifice to succeed?",
                    "Is there a hidden advantage you can use?"
                ),
                false, true)
        },
        // Act 3: Closing Log — Resolve the mission
        {
            null,
            new BeatGuidance(3, 1, "The Final Plan",
                "Act 3 begins. You have a plan. Describe your final approach to resolving the mission. What are you willing to risk?",
                "Outline your plan. What resources do you have left? What's your endgame?",
                Arrays.asList(
                    "Is your plan solid?",
                    "Do you have enough resources?",
                    "Is there a better way?"
                ),
                false, false),
            new BeatGuidance(3, 2, "The Climax",
                "The final confrontation. Everything comes down to this moment. Execute your plan and face the ultimate challenge of the mission.",
                "Perform the most critical task attempt of the mission. Spend all remaining Momentum and Threat as needed.",
                Arrays.asList(
                    "Do you achieve your primary objective?",
                    "Is there a twist at the last moment?",
                    "What is the cost of victory?"
                ),
                true, false),
            new BeatGuidance(3, 3, "Resolution",
                "The immediate aftermath. The dust settles. What is the outcome? Who won? What was lost? Describe the resolution of the main conflict.",
                "Resolve the outcome. Roll on the Advantage table if you succeeded, or the Complication table if you failed.",
                Arrays.asList(
                    "What is the final outcome?",
                    "Who was affected by this mission?",
                    "What changes as a result?"
                ),
                false, false),
            new BeatGuidance(3, 4, "Aftermath and Reflection",
                "Deal with the consequences. What does this mission mean for your character? What did you learn? What values were tested?",
                "Reflect on your values. Which were used? Which were challenged? Consider character development.",
                Arrays.asList(
                    "What did you learn from this mission?",
                    "Has your character changed?",
                    "Is there a lingering consequence?"
                ),
                false, false),
            new BeatGuidance(3, 5, "The Closing Log",
                "Mission complete. Write your final log entry. Summarize the mission, its outcome, and its impact on your career. What comes next?",
                "Write your Closing Log. Complete the mission. Consider career progression and XP.",
                Arrays.asList(
                    "How do you feel about this mission?",
                    "What do you look forward to next?",
                    "Is there a thread that will continue?"
                ),
                false, true)
        }
    };

    /**
     * Mission-type-specific narrative prompts that add flavor to each beat.
     */
    private static final Map<CaptainLogMission.MissionType, String[]> MISSION_TYPE_PROMPTS = new EnumMap<>(CaptainLogMission.MissionType.class);

    static {
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.EXPLORATION, new String[]{
            "Unknown space holds secrets. What lies beyond the edge of the charted stars?",
            "A new world awaits. What will you discover on its surface?",
            "The anomaly defies explanation. What causes it?",
            "First signals suggest life. What form does it take?",
            "The journey home reveals something unexpected."
        });
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.DIPLOMACY, new String[]{
            "Tensions between factions threaten peace. Can you bridge the divide?",
            "A new species seeks contact. How do you establish trust?",
            "Negotiations stall. What hidden interest blocks progress?",
            "An old alliance is tested. Can you renew the bond?",
            "The treaty is signed, but at what cost?"
        });
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.COMBAT, new String[]{
            "Hostile forces block your path. Engage or find another way?",
            "The enemy has a new weapon. How do you counter it?",
            "Reinforcements are needed. Can you hold the line?",
            "The battle turns. What changes the tide?",
            "Victory is won, but the cost is high."
        });
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.SCIENCE, new String[]{
            "An experiment yields unexpected results. What does it mean?",
            "The data suggests something impossible. Can you prove it?",
            "A scientific breakthrough could change everything. What is it?",
            "The phenomenon resists analysis. What approach works?",
            "Your findings will be published. What do they say?"
        });
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.RESCUE, new String[]{
            "Distress signals lead to danger. Who needs saving?",
            "The rescue site is hostile. How do you extract survivors?",
            "Time is running out. What's the fastest route?",
            "Not everyone can be saved. Who do you choose?",
            "The rescue is complete. What did you learn about those you saved?"
        });
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.INVESTIGATION, new String[]{
            "Clues point in multiple directions. Which path is correct?",
            "A witness knows more than they say. How do you get the truth?",
            "The evidence suggests a conspiracy. How deep does it go?",
            "The suspect has an alibi. Is it genuine?",
            "The case is closed. Who was really responsible?"
        });
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.TRADE, new String[]{
            "A valuable resource is in demand. Can you secure it?",
            "The trading partner has ulterior motives. What are they?",
            "Smugglers compete for the same goods. How do you outmaneuver them?",
            "The deal goes wrong. How do you recover?",
            "The trade is complete. What profit did you make?"
        });
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.FIRST_CONTACT, new String[]{
            "Signals from an unknown source. Who is sending them?",
            "First meeting with a new species. How do they react?",
            "Cultural differences create misunderstanding. How do you bridge the gap?",
            "The new species has a request. Can you help?",
            "Contact is established. What does this mean for the galaxy?"
        });
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.CRISIS, new String[]{
            "Disaster strikes. What's the scale of the emergency?",
            "Resources are limited. How do you prioritize?",
            "The crisis worsens. What new threat emerges?",
            "A solution appears. Is it risky enough to work?",
            "The crisis is averted. What was the cost?"
        });
        MISSION_TYPE_PROMPTS.put(CaptainLogMission.MissionType.PERSONAL, new String[]{
            "A personal matter demands attention. What is it?",
            "An old friend needs help. Can you spare the time?",
            "Your past catches up with you. How do you face it?",
            "A moral dilemma tests your values. What do you choose?",
            "The personal matter is resolved. How have you changed?"
        });
    }

    /**
     * Gets the guidance for the current beat of a mission.
     *
     * @param mission the current mission
     * @return the beat guidance, or null if the mission is complete
     */
    public BeatGuidance getBeatGuidance(CaptainLogMission mission) {
        if (mission.getCurrentAct() < 1 || mission.getCurrentAct() > 3) {
            return null;
        }
        if (mission.getCurrentBeat() < 1 || mission.getCurrentBeat() > 5) {
            return null;
        }

        int actIndex = mission.getCurrentAct() - 1;
        int beatIndex = mission.getCurrentBeat();

        BeatGuidance baseGuidance = BEATS[actIndex][beatIndex];
        if (baseGuidance == null) {
            return null;
        }

        // Add mission-type-specific flavor
        String typePrompt = getMissionTypePrompt(mission, mission.getCurrentBeat());

        // Clone and enhance with mission-specific context
        List<String> enhancedQuestions = new ArrayList<>(baseGuidance.getOracleQuestions());
        if (typePrompt != null) {
            enhancedQuestions.add(0, typePrompt);
        }

        return new BeatGuidance(
            baseGuidance.getAct(),
            baseGuidance.getBeat(),
            baseGuidance.getTitle(),
            baseGuidance.getNarrativePrompt() + (typePrompt != null ? "\n\nMission context: " + typePrompt : ""),
            baseGuidance.getSuggestedAction(),
            enhancedQuestions,
            baseGuidance.isRequiresTaskAttempt(),
            baseGuidance.isActTransition()
        );
    }

    /**
     * Gets a mission-type-specific prompt for the current beat.
     */
    private String getMissionTypePrompt(CaptainLogMission mission, int beat) {
        if (mission.getType() == null) {
            return null;
        }
        String[] prompts = MISSION_TYPE_PROMPTS.get(mission.getType());
        if (prompts == null) {
            return null;
        }
        // Use beat index to select a prompt (cycle through if needed)
        int index = Math.min(beat - 1, prompts.length - 1);
        return prompts[index];
    }

    /**
     * Gets a summary of the current act's structure.
     *
     * @param act the act number (1-3)
     * @return a description of the act's purpose
     */
    public String getActDescription(int act) {
        return switch (act) {
            case 1 -> "Act 1: Opening Log — Establish the situation, gather information, and commit to a course of action.";
            case 2 -> "Act 2: Supplementary Logs — Develop the conflict, face the midpoint crisis, and find a way forward through darkness.";
            case 3 -> "Act 3: Closing Log — Execute the final plan, face the climax, and resolve the mission.";
            default -> "Unknown act.";
        };
    }

    /**
     * Gets all beats for a specific act.
     *
     * @param act the act number (1-3)
     * @return an array of 5 beat guidance objects
     */
    public BeatGuidance[] getActBeats(int act) {
        if (act < 1 || act > 3) {
            return new BeatGuidance[0];
        }
        int actIndex = act - 1;
        BeatGuidance[] beats = new BeatGuidance[5];
        for (int i = 1; i <= 5; i++) {
            beats[i - 1] = BEATS[actIndex][i];
        }
        return beats;
    }

    /**
     * Suggests a task attempt based on the current beat and mission type.
     *
     * @param mission the current mission
     * @return a suggested task description, or null if no task is needed
     */
    public String suggestTaskAttempt(CaptainLogMission mission) {
        BeatGuidance guidance = getBeatGuidance(mission);
        if (guidance == null || !guidance.isRequiresTaskAttempt()) {
            return null;
        }

        String typeHint = "";
        if (mission.getType() != null) {
            typeHint = switch (mission.getType()) {
                case EXPLORATION -> "Consider using Insight + Science or Reason + Operations.";
                case DIPLOMACY -> "Consider using Presence + Command or Presence + Leadership.";
                case COMBAT -> "Consider using Fitness + Security or Strength + Security.";
                case SCIENCE -> "Consider using Reason + Science or Technical + Engineering.";
                case RESCUE -> "Consider using Fitness + Medicine or Strength + Operations.";
                case INVESTIGATION -> "Consider using Insight + Science or Reason + Security.";
                case TRADE -> "Consider using Presence + Command or Technical + Operations.";
                case FIRST_CONTACT -> "Consider using Presence + Leadership or Insight + Science.";
                case CRISIS -> "Consider using Fitness + Security or Reason + Engineering.";
                case PERSONAL -> "Consider using Presence + Leadership or Insight + Medicine.";
            };
        }

        return guidance.getSuggestedAction() + " " + typeHint;
    }

    /**
     * Checks if the current beat is an act transition (beat 5).
     *
     * @param mission the current mission
     * @return true if the player should write a log entry and advance the act
     */
    public boolean isActTransition(CaptainLogMission mission) {
        BeatGuidance guidance = getBeatGuidance(mission);
        return guidance != null && guidance.isActTransition();
    }

    /**
     * Gets the log entry prompt for the current act transition.
     *
     * @param mission the current mission
     * @return a prompt for writing the log entry, or null if not at a transition
     */
    public String getLogEntryPrompt(CaptainLogMission mission) {
        if (!isActTransition(mission)) {
            return null;
        }

        return switch (mission.getCurrentAct()) {
            case 1 -> "Write your Opening Log entry. Summarize what you've learned so far, the challenges you've faced, and your plan for the mission ahead. What do you tell Starfleet Command?";
            case 2 -> "Write your Supplementary Log entry. Describe the midpoint crisis, the dark moment, and the glimmer of hope that carries you forward. What has changed since the opening log?";
            case 3 -> "Write your Closing Log entry. Summarize the mission's outcome, the cost of victory (or the lessons of defeat), and how this mission has changed you. What comes next?";
            default -> "Continue your log entry.";
        };
    }
}
