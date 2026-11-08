package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for generating chat advice based on empire personality
 * and game state.
 */
public final class EmpireChatAdvisor {

    private EmpireChatAdvisor() {
    }

    /**
     * Generates personality-based tips for a player.
     *
     * @param player the player to generate tips for
     * @return list of personality tips
     */
    public static List<String> generatePersonalityTips(Player player) {
        List<String> tips = new ArrayList<>();
        AiPersonality personality = AiPersonality.fromPlayer(player);

        switch (personality.getLabel()) {
            case "Aggressive":
                tips.add("Prioritize building combat fleets and engaging enemies early.");
                tips.add("Consider risky maneuvers for tactical advantage.");
                break;
            case "Economic":
                tips.add("Focus on resource generation and trade routes.");
                tips.add("Build mining and trade stations before expanding militarily.");
                break;
            case "Scientific":
                tips.add("Invest in research stations and technology upgrades.");
                tips.add("Long-term advantages will outweigh short-term losses.");
                break;
            case "Expansionist":
                tips.add("Expand your territory aggressively to secure resources.");
                tips.add("Scout new areas and establish colonies quickly.");
                break;
            default:
                tips.add("Maintain a balanced approach across all areas.");
                tips.add("Adapt your strategy based on the current situation.");
                break;
        }

        return tips;
    }

    /**
     * Generates a brief strategic recommendation based on the current state.
     *
     * @param aiCore the empire's AI core
     * @param context the current decision context
     * @return a brief recommendation string, or null if no recommendation
     */
    public static String generateBriefAdvice(EmpireAiCore aiCore, AiDecisionContext context) {
        if (context == null) {
            return null;
        }

        int enemyCount = context.getEnemyFleets().size();
        int friendlyCount = context.getOwnedFleets().size();

        if (enemyCount > friendlyCount * 2) {
            return "You are outnumbered. Consider retreating and fortifying your positions.";
        } else if (enemyCount > 0 && enemyCount <= friendlyCount) {
            return "Enemy forces detected. You have sufficient strength to engage.";
        } else if (context.getMinerals() < 50) {
            return "Resources are low. Focus on mining and resource generation.";
        }

        return null;
    }

    /**
     * Generates a full strategic analysis report.
     *
     * @param aiCore the empire's AI core
     * @param context the current decision context
     * @return a formatted strategic analysis string
     */
    public static String generateStrategicAdvice(EmpireAiCore aiCore, AiDecisionContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Strategic Analysis for ").append(aiCore.getPlayer().getEmpireName()).append(" ===\n\n");

        // Personality analysis
        AiPersonality personality = aiCore.getPersonality();
        sb.append("Personality: ").append(personality.getLabel()).append("\n");
        sb.append("  - Aggression: ").append(String.format("%.0f", personality.getAggression() * 100)).append("%\n");
        sb.append("  - Expansion: ").append(String.format("%.0f", personality.getExpansionDrive() * 100)).append("%\n");
        sb.append("  - Economic: ").append(String.format("%.0f", personality.getEconomicFocus() * 100)).append("%\n");
        sb.append("  - Scientific: ").append(String.format("%.0f", personality.getScientificFocus() * 100)).append("%\n\n");

        // Fleet analysis
        if (context != null) {
            sb.append("Fleet Strength: ").append(context.getOwnedFleets().size()).append(" fleets\n");
            sb.append("Enemy Threats: ").append(context.getEnemyFleets().size()).append(" fleets\n");
            sb.append("Resources - Minerals: ").append(context.getMinerals())
              .append(", Energy: ").append(context.getEnergy()).append("\n");
        }

        // Recommendations
        sb.append("\nRecommendations:\n");
        List<String> tips = generatePersonalityTips(aiCore.getPlayer());
        for (String tip : tips) {
            sb.append("  - ").append(tip).append("\n");
        }

        String brief = generateBriefAdvice(aiCore, context);
        if (brief != null) {
            sb.append("  - ").append(brief).append("\n");
        }

        return sb.toString();
    }
}
