package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.Hyperlane;
import com.yisy.modules.doramcu.hex.SolarSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages galaxy and solar system operations: creation, lookup, and aggregation queries.
 * <p>
 * Includes Dora AI integration for AI-assisted galaxy analysis.
 * </p>
 */
public class GalaxyManager {

    private final GameContext context;

    /**
     * Creates a new GalaxyManager.
     *
     * @param context the shared game context
     */
    public GalaxyManager(GameContext context) {
        this.context = context;
    }

    /**
     * Generates a description of the galaxy state.
     *
     * @return the galaxy description string
     */
    public String getGalaxyDescription() {
        int galaxies = getGalaxyCount();
        int systems = getTotalSolarSystemCount();
        int planets = getTotalPlanetCount();
        int stars = getTotalStarCount();
        return String.format(
            "%d galaxies, %d solar systems, %d stars, %d planets",
            galaxies, systems, stars, planets
        );
    }

    /**
     * Returns the number of galaxies in the game.
     *
     * @return the galaxy count
     */
    public int getGalaxyCount() {
        return context.getGalaxies().size();
    }

    /**
     * Creates and adds a new galaxy with the given ID and default settings.
     *
     * @param id the galaxy ID
     * @return the new galaxy
     */
    public Galaxy createGalaxy(int id) {
        Galaxy galaxy = new Galaxy(id);
        return context.addGalaxy(galaxy);
    }

    /**
     * Creates and adds a new galaxy with the given ID and name.
     *
     * @param id the galaxy ID
     * @param name the galaxy name
     * @return the new galaxy
     */
    public Galaxy createGalaxy(int id, String name) {
        Galaxy galaxy = new Galaxy(id, name);
        return context.addGalaxy(galaxy);
    }

    /**
     * Removes a galaxy from the game.
     *
     * @param galaxyId the galaxy ID to remove
     * @return the removed galaxy, or null if not found
     */
    public Galaxy removeGalaxy(int galaxyId) {
        return context.getGalaxies().remove(galaxyId);
    }

    /**
     * Returns a solar system by ID, or null if not found.
     * Searches both standalone solar systems and galaxy-contained systems.
     *
     * @param systemId the solar system ID
     * @return the solar system, or null
     */
    public SolarSystem getSolarSystem(int systemId) {
        // Check standalone solar systems first
        Map<Integer, SolarSystem> solarSystems = context.getSolarSystems();
        SolarSystem system = solarSystems.get(systemId);
        if (system != null) {
            return system;
        }
        // Search in galaxies
        for (Galaxy galaxy : context.getGalaxies().values()) {
            system = galaxy.getSystem(systemId);
            if (system != null) {
                return system;
            }
        }
        return null;
    }

    /**
     * Returns all solar systems across all galaxies.
     *
     * @return list of all solar systems in all galaxies
     */
    public List<SolarSystem> getAllSolarSystems() {
        List<SolarSystem> allSystems = new ArrayList<>();
        for (Galaxy galaxy : context.getGalaxies().values()) {
            allSystems.addAll(galaxy.getAllSystems().values());
        }
        return allSystems;
    }

    /**
     * Returns the total number of solar systems across all galaxies.
     *
     * @return total solar system count
     */
    public int getTotalSolarSystemCount() {
        return context.getGalaxies().values().stream()
                .mapToInt(Galaxy::size)
                .sum();
    }

    /**
     * Returns the total number of celestial bodies across all galaxies.
     *
     * @return total body count
     */
    public int getTotalBodyCount() {
        return context.getGalaxies().values().stream()
                .mapToInt(Galaxy::getTotalBodyCount)
                .sum();
    }

    /**
     * Returns the total number of stars across all galaxies.
     *
     * @return total star count
     */
    public int getTotalStarCount() {
        return context.getGalaxies().values().stream()
                .mapToInt(Galaxy::getTotalStarCount)
                .sum();
    }

    /**
     * Returns the total number of planets across all galaxies.
     *
     * @return total planet count
     */
    public int getTotalPlanetCount() {
        return context.getGalaxies().values().stream()
                .mapToInt(Galaxy::getTotalPlanetCount)
                .sum();
    }

    /**
     * Finds a solar system by ID across all galaxies.
     *
     * @param systemId the solar system ID
     * @return the solar system, or null if not found
     */
    public SolarSystem findSolarSystem(int systemId) {
        for (Galaxy galaxy : context.getGalaxies().values()) {
            SolarSystem system = galaxy.getSystem(systemId);
            if (system != null) {
                return system;
            }
        }
        return null;
    }

    /**
     * Returns all hyperlanes across all galaxies.
     *
     * @return list of all hyperlanes
     */
    public List<Hyperlane> getAllHyperlanes() {
        List<Hyperlane> allHyperlanes = new ArrayList<>();
        for (Galaxy galaxy : context.getGalaxies().values()) {
            allHyperlanes.addAll(galaxy.getAllHyperlanes());
        }
        return allHyperlanes;
    }
}
