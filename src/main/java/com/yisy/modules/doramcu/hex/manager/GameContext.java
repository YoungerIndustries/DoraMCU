package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Battle;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.SolarSystem;
import com.yisy.modules.doramcu.hex.Player;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Shared context interface for all game managers.
 * <p>
 * Provides access to the core game state (players, planets, fleets, etc.)
 * without exposing the internal Game class structure. Managers interact
 * with game state through this interface.
 * </p>
 */
public interface GameContext {

    // ---- Game state accessors ----

    /** Returns the game name. */
    String getName();

    /** Returns the list of players. */
    List<Player> getPlayers();

    /** Returns the current turn number (derived from currentTime / turnDuration). */
    int getCurrentTurn();

    /** Returns the current game time in seconds since game start. */
    long getCurrentTime();

    /** Returns the turn duration in seconds. */
    long getTurnDuration();

    /** Returns the index of the current player. */
    int getCurrentPlayerIndex();

    /** Sets the current player index. */
    void setCurrentPlayerIndex(int index);

    /** Returns the current player whose turn it is. */
    Player getCurrentPlayer();

    /** Returns true if the game has ended. */
    boolean isGameEnded();

    /** Sets whether the game has ended. */
    void setGameEnded(boolean ended);

    /** Increments the turn counter by one turn duration. */
    void incrementTurn();

    /** Returns the random number generator. */
    Random getRandom();

    // ---- Player management ----

    /** Returns the number of active (non-eliminated) players. */
    int getActivePlayerCount();

    /** Adds a player to the game. */
    Player addPlayer(Player player);

    /** Returns a player by ID, or null if not found. */
    Player getPlayer(int playerId);

    // ---- Planet management ----

    /** Returns the map of all planets by ID. */
    Map<Integer, Planet> getPlanets();

    /** Returns a planet by ID, or null if not found. */
    Planet getPlanet(int planetId);

    // ---- Fleet management ----

    /** Returns the map of all fleets by ID. */
    Map<Integer, Fleet> getFleets();

    /** Returns a fleet by ID, or null if not found. */
    Fleet getFleet(int fleetId);

    /** Adds a fleet to the game. */
    Fleet addFleet(Fleet fleet, Player owner);

    // ---- Ship management ----

    /** Returns the map of all ships by ID. */
    Map<Integer, Ship> getShips();

    /** Returns a ship by ID, or null if not found. */
    Ship getShip(int shipId);

    /** Registers a ship in the game's global ship registry. */
    Ship addShip(Ship ship);

    /** Returns the next ship ID and increments the counter. */
    int getNextShipIdAndIncrement();

    // ---- Galaxy management ----

    /** Returns the map of all galaxies by ID. */
    Map<Integer, Galaxy> getGalaxies();

    /** Returns a galaxy by ID, or null if not found. */
    Galaxy getGalaxy(int galaxyId);

    /** Adds a galaxy to the game. */
    Galaxy addGalaxy(Galaxy galaxy);

    /** Returns the map of standalone solar systems. */
    Map<Integer, SolarSystem> getSolarSystems();

    // ---- Battle management ----

    /** Returns the map of all battles by ID. */
    Map<Integer, Battle> getBattles();

    /** Returns a battle by ID, or null if not found. */
    Battle getBattle(int battleId);

    /** Creates and registers a new battle. */
    Battle addBattle(Battle battle);

    /** Returns the next battle ID and increments the counter. */
    int getNextBattleIdAndIncrement();

    // ---- ID generation ----

    /** Returns the next planet ID and increments the counter. */
    int getNextPlanetIdAndIncrement();

    /** Returns the next fleet ID and increments the counter. */
    int getNextFleetIdAndIncrement();

    /** Returns the next solar system ID and increments the counter. */
    int getNextSolarSystemIdAndIncrement();

    // ---- Space station management ----

    /** Returns the list of all space stations. */
    List<SpaceStation> getSpaceStations();

    // ---- Manager access ----

    /** Returns the facility construction manager, or null if not available. */
    default FacilityConstructionManager getFacilityConstructionManager() {
        return null;
    }

    /** Returns the primary galaxy, or null if none exists. */
    default Galaxy getPrimaryGalaxy() {
        Map<Integer, Galaxy> galaxies = getGalaxies();
        if (galaxies.isEmpty()) {
            return null;
        }
        return galaxies.values().iterator().next();
    }
}
