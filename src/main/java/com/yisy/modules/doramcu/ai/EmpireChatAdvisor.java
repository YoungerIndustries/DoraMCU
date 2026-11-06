package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;

import java.util.*;

/**
 * Provides empire-specific strategic advice for chat responses.
 * <p>
 * Uses the EmpireAiCore system to generate strategic recommendations based on
 * the empire's personality, current game state, and available resources.
 * The advice is formatted as text that can be included in chat responses.
 * </p>
 */
public class EmpireChatAdvisor {

    /**
     * Generates strategic advice for the given empire.
     *
     * @param aiCore the empire's AI core
     * @param context the current decision context
     * @return a formatted string with strategic advice
     */
    public static String generateStrategicAdvice(EmpireAiCore aiCore, AiDecisionContext context) {
        if (aiCore == null || context == null) {
            return "No strategic advice available.";
        }

        StringBuilder sb = new StringBuilder();
        AiPersonality personality = aiCore.getPersonality();

        sb.append("## Strategic Advice for ").append(aiCore.getPlayer().getEmpireName()).append("\n\n");

        // Personality-based advice
        sb.append("### Personality: ").append(personality.getLabel()).append("\n");
        sb.append("Your empire's personality shapes its strategic priorities:\n");
        sb.append("- Aggression: ").append(String.format("%.0f", personality.getAggression() * 100)).append("%\n");
        sb.append("- Expansion Drive: ").append(String.format("%.0f", personality.getExpansionDrive() * 100)).append("%\n");
        sb.append("- Economic Focus: ").append(String.format("%.0f", personality.getEconomicFocus() * 100)).append("%\n");
        sb.append("- Scientific Focus: ").append(String.format("%.0f", personality.getScientificFocus() * 100)).append("%\n");
        sb.append("- Diplomatic Stance: ").append(String.format("%.0f", personality.getDiplomaticStance() * 100)).append("%\n");
        sb.append("- Risk Tolerance: ").append(String.format("%.0f", personality.getRiskTolerance() * 100)).append("%\n");

        // Current decision
        AiDecision decision = aiCore.decide(context);
        sb.append("\n### Recommended Action\n");
        sb.append("- **Action**: ").append(decision.getActionType()).append("\n");
        sb.append("- **Confidence**: ").append(String.format("%.0f", decision.getConfidence() * 100)).append("%\n");
        sb.append("- **Reasoning**: ").append(decision.getReasoning()).append("\n");

        // Resource-based advice
        sb.append("\n### Resource Analysis\n");
        int minerals = context.getMinerals();
        int energy = context.getEnergy();
        int food = context.getFood();

        if (minerals < 50) {
            sb.append("- ⚠️ Low minerals: Consider optimizing mining operations before building.\n");
        } else if (minerals >= 200) {
            sb.append("- ✅ High minerals: Good time to invest in ship or station construction.\n");
        } else {
            sb.append("- Minerals are at a moderate level.\n");
        }

        if (energy < 25) {
            sb.append("- ⚠️ Low energy: Production may be limited. Consider energy infrastructure.\n");
        } else if (energy >= 100) {
            sb.append("- ✅ High energy: Sufficient for heavy production.\n");
        } else {
            sb.append("- Energy levels are adequate.\n");
        }

        if (food < 20) {
            sb.append("- ⚠️ Low food: Colony growth may be affected.\n");
        } else {
            sb.append("- Food supply is stable.\n");
        }

        // Fleet assessment
        List<Fleet> fleets = context.getOwnedFleets();
        sb.append("\n### Fleet Assessment\n");
        if (fleets.isEmpty()) {
            sb.append("- No fleets available. Priority: Build ships at your shipyards.\n");
        } else {
            sb.append("- You command ").append(fleets.size()).append(" fleet(s).\n");
            int totalShips = 0;
            for (Fleet fleet : fleets) {
                totalShips += fleet.getShips().size();
            }
            sb.append("- Total ships: ").append(totalShips).append("\n");

            if (totalShips < 5) {
                sb.append("- ⚠️ Small fleet: Consider expanding your naval forces.\n");
            } else if (totalShips >= 15) {
                sb.append("- ✅ Strong fleet: Capable of major operations.\n");
            } else {
                sb.append("- Fleet size is moderate.\n");
            }
        }

        // Threat assessment
        List<Fleet> enemies = context.getEnemyFleets();
        if (!enemies.isEmpty()) {
            sb.append("\n### Threat Assessment\n");
            sb.append("- ").append(enemies.size()).append(" enemy fleet(s) detected.\n");
            int totalEnemyShips = 0;
            for (Fleet enemy : enemies) {
                totalEnemyShips += enemy.getShips().size();
            }
            sb.append("- Enemy strength: ").append(totalEnemyShips).append(" ships.\n");

            int totalShips = 0;
            for (Fleet fleet : fleets) {
                totalShips += fleet.getShips().size();
            }

            if (totalShips > 0 && totalEnemyShips > 0) {
                double ratio = (double) totalShips / totalEnemyShips;
                if (ratio >= 2.0) {
                    sb.append("- ✅ You have a numerical advantage. Consider engaging.\n");
                } else if (ratio <= 0.5) {
                    sb.append("- ⚠️ Enemy outnumbers you. Consider defensive posture or diplomacy.\n");
                } else {
                    sb.append("- Forces are relatively balanced. Choose engagements carefully.\n");
                }
            }
        } else {
            sb.append("\n### Threat Assessment\n- No immediate threats detected.\n");
        }

        // Station advice
        List<SpaceStation> stations = context.getStations();
        if (stations != null && !stations.isEmpty()) {
            sb.append("\n### Station Status\n");
            int aiCoreCount = 0;
            for (SpaceStation station : stations) {
                if (station.hasAiCore()) {
                    aiCoreCount++;
                }
            }
            sb.append("- ").append(stations.size()).append(" station(s) in operation.\n");
            if (aiCoreCount < stations.size()) {
                sb.append("- ⚠️ ").append(stations.size() - aiCoreCount).append(" station(s) without AI Cores. Upgrading will improve efficiency.\n");
            } else {
                sb.append("- ✅ All stations equipped with AI Cores.\n");
            }
        }

        return sb.toString();
    }

    /**
     * Generates a brief strategic summary for quick chat responses.
     *
     * @param aiCore the empire's AI core
     * @param context the current decision context
     * @return a brief summary string
     */
    public static String generateBriefAdvice(EmpireAiCore aiCore, AiDecisionContext context) {
        if (aiCore == null || context == null) {
            return "No advice available.";
        }

        AiDecision decision = aiCore.decide(context);
        AiPersonality personality = aiCore.getPersonality();

        StringBuilder sb = new StringBuilder();
        sb.append("As a ").append(personality.getLabel()).append(", ");
        sb.append("I recommend ").append(decision.getActionType().name().toLowerCase()).append(": ");
        sb.append(decision.getReasoning());

        return sb.toString();
    }

    /**
     * Generates personality-specific tips based on government type and ethos.
     *
     * @param player the player
     * @return a list of personality-specific tips
     */
    public static List<String> generatePersonalityTips(Player player) {
        if (player == null) {
            return Collections.emptyList();
        }

        AiPersonality personality = AiPersonality.fromPlayer(player);
        List<String> tips = new ArrayList<>();

        // Government-specific tips
        switch (player.getGovernmentType()) {
            case DICTATORSHIP -> tips.add("Dictatorships excel at rapid decision-making. Focus your resources on a single priority at a time.");
            case DEMOCRACY -> tips.add("Democracies benefit from balanced development. Maintain good relations with allies for diplomatic bonuses.");
            case MONARCHY -> tips.add("Monarchies gain loyalty bonuses. Invest in colony development to maximize subject planet output.");
            case THEOCRACY -> tips.add("Theocracies channel faith into power. Research spiritual technologies for unique advantages.");
            case CONFEDERATION -> tips.add("Confederations thrive on cooperation. Leverage trade and alliances for economic strength.");
            case CORPORATE -> tips.add("Corporate states prioritize profit. Optimize trade routes and resource extraction for maximum credits.");
            case AUTHORITARIAN -> tips.add("Authoritarian states enforce strict control. Centralize decision-making for rapid responses.");
            case OLIGARCHY -> tips.add("Oligarchies balance power among elites. Leverage faction alliances for strategic advantage.");
            default -> tips.add("Adapt your strategy to your government type's strengths.");
        }

        // Ethos-specific tips
        switch (player.getEthos()) {
            case MILITARIST -> tips.add("Militarist ethos favors aggressive expansion. Prioritize ship production and combat readiness.");
            case PACIFIST -> tips.add("Pacifist ethos values peace. Focus on economic development and diplomatic solutions.");
            case MATERIALIST -> tips.add("Materialist ethos drives scientific progress. Invest in research and technology.");
            case SPIRITUALIST -> tips.add("Spiritualist ethos seeks meaning. Balance material and spiritual development.");
            case LIBERTARIAN -> tips.add("Libertarian ethos values freedom. Encourage independent colony growth and trade.");
            case EQUALIST -> tips.add("Equalist ethos promotes fairness. Ensure balanced resource distribution across colonies.");
        }

        return tips;
    }
}
