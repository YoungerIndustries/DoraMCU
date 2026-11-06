package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;

import java.util.*;

/**
 * Provides game state context for chat responses.
 * <p>
 * Extracts relevant game information from the current game state and formats it
 * as text that can be included in the chat system prompt, enabling the AI to
 * provide game-aware responses.
 * </p>
 */
public class GameContextProvider {

    /**
     * Builds a text representation of the current game state for chat context.
     *
     * @param player the player requesting the context
     * @param fleets the player's fleets
     * @param stations the player's stations
     * @param resources the player's resources
     * @return a formatted string describing the game state
     */
    public static String buildGameStateContext(Player player, List<Fleet> fleets, List<SpaceStation> stations, Map<String, Integer> resources) {
        if (player == null) {
            return "No game state available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Current Game State\n\n");

        // Empire info
        sb.append("### Empire: ").append(player.getEmpireName()).append("\n");
        sb.append("- Government: ").append(player.getGovernmentType()).append("\n");
        sb.append("- Ethos: ").append(player.getEthos()).append("\n");
        sb.append("- Planets: ").append(player.getOwnedPlanetIds().size()).append("\n");

        // Resources
        if (resources != null && !resources.isEmpty()) {
            sb.append("\n### Resources\n");
            for (Map.Entry<String, Integer> entry : resources.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        // Fleets
        if (fleets != null && !fleets.isEmpty()) {
            sb.append("\n### Fleets (").append(fleets.size()).append(")\n");
            for (Fleet fleet : fleets) {
                sb.append("- Fleet #").append(fleet.getId()).append(" (").append(fleet.getShips().size()).append(" ships)");
                if (fleet.getPosition() != null) {
                    sb.append(" at ").append(fleet.getPosition());
                }
                sb.append("\n");
            }
        } else {
            sb.append("\n### Fleets: None\n");
        }

        // Stations
        if (stations != null && !stations.isEmpty()) {
            sb.append("\n### Space Stations (").append(stations.size()).append(")\n");
            for (SpaceStation station : stations) {
                sb.append("- ").append(station.getStationType()).append(" at ").append(station.getPosition());
                if (station.hasAiCore()) {
                    sb.append(" [AI Core: Level ").append(station.getAiCoreLevel()).append("]");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Builds a text representation of enemy state for threat assessment.
     *
     * @param enemyFleets the enemy fleets
     * @return a formatted string describing enemy positions and strength
     */
    public static String buildEnemyContext(List<Fleet> enemyFleets) {
        if (enemyFleets == null || enemyFleets.isEmpty()) {
            return "No known enemy fleets.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("### Enemy Fleets (").append(enemyFleets.size()).append(")\n");
        for (Fleet enemy : enemyFleets) {
            sb.append("- Enemy fleet #").append(enemy.getId()).append(" (").append(enemy.getShips().size()).append(" ships)");
            if (enemy.getPosition() != null) {
                sb.append(" at ").append(enemy.getPosition());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Builds a concise summary of the game state for quick reference.
     *
     * @param player the player
     * @param fleets the player's fleets
     * @param resources the player's resources
     * @return a brief summary string
     */
    public static String buildSummary(Player player, List<Fleet> fleets, Map<String, Integer> resources) {
        if (player == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(player.getEmpireName()).append(" (").append(player.getGovernmentType()).append(")");
        sb.append(" has ").append(player.getOwnedPlanetIds().size()).append(" planets");

        if (fleets != null) {
            sb.append(", ").append(fleets.size()).append(" fleets");
        }

        if (resources != null) {
            int minerals = resources.getOrDefault("minerals", 0);
            int energy = resources.getOrDefault("energy", 0);
            sb.append(" with ").append(minerals).append(" minerals, ").append(energy).append(" energy");
        }

        return sb.toString();
    }
}
