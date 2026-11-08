package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.SpaceStation;

import java.util.List;
import java.util.Map;

/**
 * Provides utility methods for building game state context strings
 * used in AI chat system prompts.
 */
public final class GameContextProvider {

    private GameContextProvider() {
    }

    /**
     * Builds a formatted game state context string for use in chat system prompts.
     *
     * @param player the player whose context to build
     * @param ownedFleets the player's owned fleets
     * @param stations the player's stations
     * @param resources the player's resources map
     * @return a formatted string describing the current game state
     */
    public static String buildGameStateContext(Player player, List<Fleet> ownedFleets,
                                                List<SpaceStation> stations,
                                                Map<String, Integer> resources) {
        StringBuilder sb = new StringBuilder();

        sb.append("Player: ").append(player.getEmpireName()).append("\n");
        sb.append("Planets: ").append(player.getPlanetCount()).append("\n");
        sb.append("Fleets: ").append(ownedFleets.size()).append("\n");

        if (stations != null) {
            sb.append("Stations: ").append(stations.size()).append("\n");
        }

        if (resources != null) {
            sb.append("Resources:\n");
            for (Map.Entry<String, Integer> entry : resources.entrySet()) {
                sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        // Fleet summary
        if (!ownedFleets.isEmpty()) {
            sb.append("\nFleet Summary:\n");
            for (Fleet fleet : ownedFleets) {
                sb.append("  - Fleet #").append(fleet.getId())
                  .append(" (").append(fleet.getShips().size()).append(" ships)");
                if (fleet.getPosition() != null) {
                    sb.append(" at (").append(fleet.getPosition().getX())
                      .append(", ").append(fleet.getPosition().getY()).append(")");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
