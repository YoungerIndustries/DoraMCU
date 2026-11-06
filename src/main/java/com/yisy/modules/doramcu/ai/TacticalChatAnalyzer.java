package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;

import java.util.*;

/**
 * Provides tactical analysis for chat responses.
 * <p>
 * Analyzes combat situations, fleet compositions, and tactical options
 * to provide actionable advice for fleet commanders.
 * </p>
 */
public class TacticalChatAnalyzer {

    /**
     * Analyzes a combat situation between two fleets.
     *
     * @param myFleet the player's fleet
     * @param enemyFleet the enemy fleet
     * @return a formatted string with combat analysis
     */
    public static String analyzeCombat(Fleet myFleet, Fleet enemyFleet) {
        if (myFleet == null || enemyFleet == null) {
            return "No combat data available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Combat Analysis\n\n");

        // Fleet comparison
        sb.append("### Fleet Comparison\n");
        sb.append("- **Your Fleet**: ").append(myFleet.getShips().size()).append(" ships\n");
        sb.append("- **Enemy Fleet**: ").append(enemyFleet.getShips().size()).append(" ships\n");

        // Calculate fleet strengths
        double myStrength = calculateFleetStrength(myFleet);
        double enemyStrength = calculateFleetStrength(enemyFleet);
        double ratio = enemyStrength > 0 ? myStrength / enemyStrength : Double.MAX_VALUE;

        sb.append("- **Your Strength**: ").append(String.format("%.1f", myStrength)).append("\n");
        sb.append("- **Enemy Strength**: ").append(String.format("%.1f", enemyStrength)).append("\n");
        sb.append("- **Strength Ratio**: ").append(String.format("%.2f", ratio)).append("\n\n");

        // Combat prediction
        sb.append("### Combat Prediction\n");
        if (ratio >= 2.0) {
            sb.append("- ✅ **Strong advantage**: You should win this engagement with minimal losses.\n");
            sb.append("- Recommended tactic: Direct assault.\n");
        } else if (ratio >= 1.2) {
            sb.append("- ✅ **Slight advantage**: You can win, but expect some losses.\n");
            sb.append("- Recommended tactic: Flanking maneuver to maximize your advantage.\n");
        } else if (ratio >= 0.8) {
            sb.append("- ⚠️ **Even match**: The outcome is uncertain.\n");
            sb.append("- Recommended tactic: Use terrain and formations to gain an edge.\n");
        } else if (ratio >= 0.5) {
            sb.append("- ⚠️ **Disadvantage**: You are outgunned.\n");
            sb.append("- Recommended tactic: Consider retreat or ambush tactics.\n");
        } else {
            sb.append("- ❌ **Severe disadvantage**: Engaging will likely result in heavy losses.\n");
            sb.append("- Recommended tactic: Retreat and reinforce before engaging.\n");
        }

        // Ship composition analysis
        sb.append("\n### Ship Composition\n");
        sb.append("Your fleet:\n");
        appendShipComposition(sb, myFleet.getShips(), "  ");

        sb.append("\nEnemy fleet:\n");
        appendShipComposition(sb, enemyFleet.getShips(), "  ");

        // AI Core analysis
        sb.append("\n### AI Core Analysis\n");
        int myAiCores = countAiCores(myFleet);
        int enemyAiCores = countAiCores(enemyFleet);

        sb.append("- Your AI Cores: ").append(myAiCores).append("\n");
        sb.append("- Enemy AI Cores: ").append(enemyAiCores).append("\n");

        if (myAiCores > enemyAiCores) {
            sb.append("- ✅ Tactical AI advantage: Your fleets will make better decisions in combat.\n");
        } else if (myAiCores < enemyAiCores) {
            sb.append("- ⚠️ Enemy has superior tactical AI. Expect adaptive enemy tactics.\n");
        } else {
            sb.append("- Tactical AI is balanced.\n");
        }

        // Position analysis
        if (myFleet.getPosition() != null && enemyFleet.getPosition() != null) {
            sb.append("\n### Position Analysis\n");
            int distance = hexDistance(myFleet.getPosition(), enemyFleet.getPosition());
            sb.append("- Distance: ").append(distance).append(" hexes\n");

            if (distance <= 2) {
                sb.append("- ⚠️ Enemy is very close. Prepare for imminent combat.\n");
            } else if (distance <= 5) {
                sb.append("- Enemy is within engagement range.\n");
            } else {
                sb.append("- Enemy is at a safe distance.\n");
            }
        }

        return sb.toString();
    }

    /**
     * Analyzes a single fleet's composition and capabilities.
     *
     * @param fleet the fleet to analyze
     * @return a formatted string with fleet analysis
     */
    public static String analyzeFleet(Fleet fleet) {
        if (fleet == null) {
            return "No fleet data available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Fleet Analysis: #").append(fleet.getId()).append("\n\n");

        // Basic info
        sb.append("- Ships: ").append(fleet.getShips().size()).append("\n");
        sb.append("- Position: ").append(fleet.getPosition() != null ? fleet.getPosition() : "Unknown").append("\n");

        // Ship types
        Map<ShipType, Integer> typeCounts = new HashMap<>();
        double totalHealth = 0;
        double totalDamage = 0;
        double totalShield = 0;
        int aiCoreCount = 0;

        for (Ship ship : fleet.getShips()) {
            typeCounts.merge(ship.getShipType(), 1, Integer::sum);
            totalHealth += ship.getEffectiveHullPoints();
            totalDamage += ship.getEffectiveDamage();
            totalShield += ship.getEffectiveShieldStrength();
            if (ship.hasAiCore()) {
                aiCoreCount++;
            }
        }

        sb.append("\n### Ship Types\n");
        for (Map.Entry<ShipType, Integer> entry : typeCounts.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        sb.append("\n### Combat Stats\n");
        sb.append("- Total Hull: ").append(String.format("%.0f", totalHealth)).append("\n");
        sb.append("- Total Damage: ").append(String.format("%.1f", totalDamage)).append("\n");
        sb.append("- Total Shield: ").append(String.format("%.1f", totalShield)).append("\n");
        sb.append("- AI Core Ships: ").append(aiCoreCount).append("\n");

        // Fleet assessment
        sb.append("\n### Assessment\n");
        if (fleet.getShips().isEmpty()) {
            sb.append("- Empty fleet. No combat capability.\n");
        } else if (totalDamage > 100) {
            sb.append("- ✅ High damage output. This fleet is a significant threat.\n");
        } else if (totalDamage > 50) {
            sb.append("- Moderate damage output. Capable of defensive actions.\n");
        } else {
            sb.append("- Low damage output. Consider reinforcing this fleet.\n");
        }

        return sb.toString();
    }

    /**
     * Provides tactical recommendations for a fleet.
     *
     * @param fleet the player's fleet
     * @param enemies nearby enemy fleets
     * @return a formatted string with tactical recommendations
     */
    public static String tacticalRecommendations(Fleet fleet, List<Fleet> enemies) {
        if (fleet == null) {
            return "No fleet data available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Tactical Recommendations\n\n");

        if (enemies == null || enemies.isEmpty()) {
            sb.append("- No nearby enemies. Consider scouting or moving to strategic positions.\n");
            sb.append("- Focus on fleet maintenance and upgrades.\n");
        } else {
            // Find nearest enemy
            Fleet nearest = null;
            int minDistance = Integer.MAX_VALUE;

            for (Fleet enemy : enemies) {
                if (fleet.getPosition() != null && enemy.getPosition() != null) {
                    int dist = hexDistance(fleet.getPosition(), enemy.getPosition());
                    if (dist < minDistance) {
                        minDistance = dist;
                        nearest = enemy;
                    }
                }
            }

            if (nearest != null) {
                double myStrength = calculateFleetStrength(fleet);
                double enemyStrength = calculateFleetStrength(nearest);
                double ratio = enemyStrength > 0 ? myStrength / enemyStrength : Double.MAX_VALUE;

                sb.append("- Nearest enemy: ").append(minDistance).append(" hexes away.\n");
                sb.append("- Strength ratio: ").append(String.format("%.2f", ratio)).append("\n\n");

                if (ratio >= 1.5) {
                    sb.append("- **Engage**: You have sufficient strength to engage the nearest enemy.\n");
                    sb.append("- Consider flanking maneuvers to maximize your advantage.\n");
                } else if (ratio >= 0.8) {
                    sb.append("- **Caution**: The enemy is relatively strong. Assess before engaging.\n");
                    sb.append("- Wait for reinforcements if possible.\n");
                } else {
                    sb.append("- **Retreat**: The enemy is too strong. Retreat and regroup.\n");
                    sb.append("- Scout for weaker targets or wait for reinforcements.\n");
                }
            }
        }

        // AI Core recommendations
        int aiCoreCount = countAiCores(fleet);
        if (aiCoreCount == 0) {
            sb.append("\n- ⚠️ No AI Cores in this fleet. Installing AI Cores will improve tactical decisions.\n");
        } else if (aiCoreCount < fleet.getShips().size() / 2.0) {
            sb.append("\n- Consider adding more AI Core ships for better fleet coordination.\n");
        } else {
            sb.append("\n- ✅ Good AI Core coverage for tactical coordination.\n");
        }

        return sb.toString();
    }

    /**
     * Calculates the total strength of a fleet.
     */
    private static double calculateFleetStrength(Fleet fleet) {
        if (fleet == null || fleet.getShips().isEmpty()) {
            return 0;
        }

        double strength = 0;
        for (Ship ship : fleet.getShips()) {
            strength += ship.getEffectiveDamage();
            strength += ship.getEffectiveShieldStrength() * 0.5;
            strength += ship.getEffectiveHullPoints() * 0.3;

            // AI Core bonus
            if (ship.hasAiCore()) {
                strength *= (1.0 + ship.getAiCoreLevel() * 0.05);
            }
        }
        return strength;
    }

    /**
     * Counts the number of ships with AI Cores in a fleet.
     */
    private static int countAiCores(Fleet fleet) {
        if (fleet == null) {
            return 0;
        }
        int count = 0;
        for (Ship ship : fleet.getShips()) {
            if (ship.hasAiCore()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Appends ship composition breakdown to a StringBuilder.
     */
    private static void appendShipComposition(StringBuilder sb, List<Ship> ships, String indent) {
        if (ships == null || ships.isEmpty()) {
            sb.append(indent).append("No ships\n");
            return;
        }

        Map<ShipType, Integer> counts = new HashMap<>();
        for (Ship ship : ships) {
            counts.merge(ship.getShipType(), 1, Integer::sum);
        }

        for (Map.Entry<ShipType, Integer> entry : counts.entrySet()) {
            sb.append(indent).append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
    }

    /**
     * Calculates hex distance between two positions.
     */
    private static int hexDistance(Hex3D a, Hex3D b) {
        return (Math.abs(a.getX() - b.getX()) +
                Math.abs(a.getY() - b.getY()) +
                Math.abs(a.getZ() - b.getZ())) / 2;
    }
}
