package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Ship;

import java.util.List;

/**
 * Provides utility methods for tactical fleet analysis used in chat responses.
 */
public final class TacticalChatAnalyzer {

    private TacticalChatAnalyzer() {
    }

    /**
     * Generates a fleet analysis report.
     *
     * @param fleet the fleet to analyze
     * @return a formatted analysis string
     */
    public static String analyzeFleet(Fleet fleet) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Fleet Analysis #").append(fleet.getId()).append(" ===\n");
        sb.append("Ships: ").append(fleet.getShips().size()).append("\n");

        if (fleet.getPosition() != null) {
            sb.append("Position: (").append(fleet.getPosition().getX())
              .append(", ").append(fleet.getPosition().getY())
              .append(", ").append(fleet.getPosition().getZ()).append(")\n");
        }

        // Ship type breakdown
        long frigateCount = fleet.getShips().stream()
                .filter(s -> s.getShipType().name().contains("FRIGATE")).count();
        long destroyerCount = fleet.getShips().stream()
                .filter(s -> s.getShipType().name().contains("DESTROYER")).count();
        long cruiserCount = fleet.getShips().stream()
                .filter(s -> s.getShipType().name().contains("CRUISER")).count();

        if (frigateCount > 0) sb.append("  - Frigates: ").append(frigateCount).append("\n");
        if (destroyerCount > 0) sb.append("  - Destroyers: ").append(destroyerCount).append("\n");
        if (cruiserCount > 0) sb.append("  - Cruisers: ").append(cruiserCount).append("\n");

        return sb.toString();
    }

    /**
     * Generates tactical recommendations based on fleet composition and enemy presence.
     *
     * @param fleet the friendly fleet
     * @param enemyFleets the list of enemy fleets
     * @return a formatted recommendations string
     */
    public static String tacticalRecommendations(Fleet fleet, List<Fleet> enemyFleets) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nTactical Recommendations:\n");

        int friendlyShips = fleet.getShips().size();
        int totalEnemyShips = 0;
        for (Fleet enemy : enemyFleets) {
            totalEnemyShips += enemy.getShips().size();
        }

        if (totalEnemyShips == 0) {
            sb.append("  - No immediate threats detected. Consider scouting or expanding.\n");
        } else if (friendlyShips >= totalEnemyShips * 2) {
            sb.append("  - You have a significant advantage. Consider engaging the enemy.\n");
        } else if (friendlyShips >= totalEnemyShips) {
            sb.append("  - Forces are roughly equal. Engage with caution.\n");
        } else {
            sb.append("  - You are outnumbered. Consider retreating or reinforcing.\n");
        }

        return sb.toString();
    }

    /**
     * Generates a combat analysis between two fleets.
     *
     * @param myFleet the friendly fleet
     * @param enemyFleet the enemy fleet
     * @return a formatted combat analysis string
     */
    public static String analyzeCombat(Fleet myFleet, Fleet enemyFleet) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Combat Analysis ===\n\n");
        sb.append("Friendly Fleet #").append(myFleet.getId())
          .append(": ").append(myFleet.getShips().size()).append(" ships\n");
        sb.append("Enemy Fleet #").append(enemyFleet.getId())
          .append(": ").append(enemyFleet.getShips().size()).append(" ships\n");

        int ratio = myFleet.getShips().size() / Math.max(1, enemyFleet.getShips().size());
        sb.append("\nShip Ratio: ").append(ratio).append(":1\n");

        if (ratio >= 3) {
            sb.append("Outcome Prediction: Decisive victory likely.\n");
        } else if (ratio >= 2) {
            sb.append("Outcome Prediction: Victory likely with minor losses.\n");
        } else if (ratio >= 1) {
            sb.append("Outcome Prediction: Victory possible but costly.\n");
        } else {
            sb.append("Outcome Prediction: Defeat likely. Consider retreating.\n");
        }

        return sb.toString();
    }
}
