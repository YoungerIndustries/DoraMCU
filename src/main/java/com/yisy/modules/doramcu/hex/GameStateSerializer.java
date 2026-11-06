package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes and deserializes Game state to JSON-compatible Maps.
 * <p>
 * Extracted from Game to separate persistence logic from the core game controller.
 * Galaxy/solar system data is NOT serialized here as it is stored separately
 * in the database by GalaxyService.
 * </p>
 */
public class GameStateSerializer {

    /**
     * Serializes the essential game state to a JSON-compatible Map.
     *
     * @param game the game to serialize
     * @return a map representing the serializable game state
     */
    public Map<String, Object> serialize(Game game) {
        Map<String, Object> state = new HashMap<>();
        state.put("name", game.getName());
        state.put("currentTime", game.getCurrentTime());
        state.put("turnDuration", game.getTurnDuration());
        state.put("currentPlayerIndex", game.getCurrentPlayerIndex());
        state.put("gameEnded", game.isGameEnded());
        state.put("nextPlanetId", game.getCurrentNextPlanetId());
        state.put("nextFleetId", game.getCurrentNextFleetId());
        state.put("nextShipId", game.getCurrentNextShipId());
        state.put("nextBattleId", game.getCurrentNextBattleId());
        state.put("nextSolarSystemId", game.getCurrentNextSolarSystemId());

        state.put("players", serializePlayers(game));
        state.put("planets", serializePlanets(game));
        state.put("fleets", serializeFleets(game));

        return state;
    }

    private List<Map<String, Object>> serializePlayers(Game game) {
        List<Map<String, Object>> playerStates = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            Map<String, Object> ps = new HashMap<>();
            ps.put("id", p.getId());
            ps.put("name", p.getName());
            ps.put("resources", new HashMap<String, Integer>()); // Flat resources removed
            ps.put("ownedPlanetIds", java.util.Set.copyOf(p.getOwnedPlanetIds()));
            ps.put("ownedFleetIds", java.util.Set.copyOf(p.getOwnedFleetIds()));
            ps.put("eliminated", p.isEliminated());
            ps.put("homePlanetId", p.getHomePlanetId());
            ps.put("homeSystemId", p.getHomeSystemId());
            ps.put("homeSectorRow", p.getHomeSectorRow());
            ps.put("homeSectorCol", p.getHomeSectorCol());
            ps.put("homeSubSectorRow", p.getHomeSubSectorRow());
            ps.put("homeSubSectorCol", p.getHomeSubSectorCol());
            playerStates.add(ps);
        }
        return playerStates;
    }

    private List<Map<String, Object>> serializePlanets(Game game) {
        List<Map<String, Object>> planetStates = new ArrayList<>();
        for (Planet planet : game.getPlanets().values()) {
            Map<String, Object> pl = new HashMap<>();
            pl.put("id", planet.getId());
            pl.put("name", planet.getName());
            pl.put("size", planet.getPlanetSize().name());
            pl.put("mineralQuality", planet.getMineralQuality().name());
            planetStates.add(pl);
        }
        return planetStates;
    }

    private List<Map<String, Object>> serializeFleets(Game game) {
        List<Map<String, Object>> fleetStates = new ArrayList<>();
        for (Fleet fleet : game.getFleets().values()) {
            Map<String, Object> fl = new HashMap<>();
            fl.put("id", fleet.getId());
            fl.put("name", fleet.getName());
            if (fleet.getOwner() != null) {
                fl.put("ownerId", fleet.getOwner().getId());
            }
            if (fleet.getPosition() != null) {
                fl.put("positionX", fleet.getPosition().getX());
                fl.put("positionY", fleet.getPosition().getY());
                fl.put("positionZ", fleet.getPosition().getZ());
            }
            List<Integer> shipIds = new ArrayList<>();
            for (Ship ship : fleet.getShips()) {
                shipIds.add(ship.getId());
            }
            fl.put("shipIds", shipIds);
            fleetStates.add(fl);
        }
        return fleetStates;
    }
}
