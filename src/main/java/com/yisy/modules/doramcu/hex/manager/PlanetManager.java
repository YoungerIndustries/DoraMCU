package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.HomeLocation;
import com.yisy.modules.doramcu.hex.MineralQuality;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.PlanetSize;
import com.yisy.modules.doramcu.hex.SolarSystem;
import com.yisy.modules.doramcu.hex.SolarSystemEntity;
import com.yisy.modules.doramcu.hex.StartingFacility;
import com.yisy.modules.doramcu.hex.Player;

import java.util.Map;
import java.util.Random;

/**
 * Manages planet operations: creation, transfer, and home system setup.
 * <p>
 * Includes Dora AI integration for AI-assisted planet naming and analysis.
 * </p>
 */
public class PlanetManager {

    private final GameContext context;
    private final Random random;

    /**
     * Creates a new PlanetManager.
     *
     * @param context the shared game context
     */
    public PlanetManager(GameContext context) {
        this.context = context;
        this.random = context.getRandom();
    }

    /**
     * Generates a planet name based on the given theme.
     *
     * @param theme the theme for the planet name (e.g., "terran", "gas giant", "ice world")
     * @return the planet name
     */
    public String generatePlanetName(String theme) {
        return "Planet-" + random.nextInt(1000);
    }

    /**
     * Generates analysis of a player's planets.
     *
     * @param player the player whose planets to analyze
     * @return the planet analysis string
     */
    public String getPlanetAnalysis(Player player) {
        int planetCount = player.getOwnedPlanetIds().size();
        return String.format(
            "Player owns %d planets",
            planetCount
        );
    }

    /**
     * Adds a planet to the game and assigns it to a player.
     *
     * @param planet the planet to add
     * @param owner the owning player
     * @return the planet added
     */
    public Planet addPlanet(Planet planet, Player owner) {
        if (planet == null) {
            throw new IllegalArgumentException("Planet must not be null");
        }
        if (owner == null) {
            throw new IllegalArgumentException("Owner must not be null");
        }
        int planetId = planet.getId();
        context.getPlanets().put(planetId, planet);
        owner.addPlanet(planetId);
        return planet;
    }

    /**
     * Creates and adds a new planet with an auto-generated ID.
     *
     * @param name the planet name
     * @param size the planet size
     * @param mineralQuality the mineral quality
     * @param owner the owning player
     * @return the new planet
     */
    public Planet createPlanet(String name, PlanetSize size, MineralQuality mineralQuality, Player owner) {
        int planetId = context.getNextPlanetIdAndIncrement();
        Planet planet = new Planet(planetId);
        planet.setName(name);
        planet.setPlanetSize(size);
        planet.setMineralQuality(mineralQuality);
        return addPlanet(planet, owner);
    }

    /**
     * Transfers ownership of a planet from one player to another.
     *
     * @param planetId the planet to transfer
     * @param newOwner the new owner
     * @return true if the transfer succeeded
     */
    public boolean transferPlanet(int planetId, Player newOwner) {
        Planet planet = context.getPlanet(planetId);
        if (planet == null) {
            return false;
        }
        // Find and remove from current owner
        for (Player player : context.getPlayers()) {
            if (player.getOwnedPlanetIds().contains(planetId)) {
                player.removePlanet(planetId);
                break;
            }
        }
        newOwner.addPlanet(planetId);
        return true;
    }

    /**
     * Sets up the home system for a player, creating a home planet with default starting facilities.
     *
     * @param player the player to set up
     * @return the created home planet
     */
    public Planet setupHomeSystem(Player player) {
        return setupHomeSystem(player, PlanetSize.MEDIUM, MineralQuality.NORMAL,
                StartingFacility.COLONY_DOME, StartingFacility.SHIPYARD, StartingFacility.MINING_FACILITY);
    }

    /**
     * Sets up the home system for a player, registering the solar system with the given Galaxy.
     *
     * @param galaxy the galaxy to register the home system with
     * @param player the player to set up
     * @return the created home planet
     */
    public Planet setupHomeSystem(Galaxy galaxy, Player player) {
        return setupHomeSystem(galaxy, player, PlanetSize.MEDIUM, MineralQuality.NORMAL,
                StartingFacility.COLONY_DOME, StartingFacility.SHIPYARD, StartingFacility.MINING_FACILITY);
    }

    /**
     * Sets up the home system for a player with custom properties, registering with the given Galaxy.
     *
     * @param galaxy the galaxy to register the home system with
     * @param player the player to set up
     * @param size the planet size
     * @param mineralQuality the mineral quality
     * @param facilities the starting facilities to build
     * @return the created home planet
     */
    public Planet setupHomeSystem(Galaxy galaxy, Player player, PlanetSize size, MineralQuality mineralQuality,
                                  StartingFacility... facilities) {
        Planet planet = setupHomeSystem(player, size, mineralQuality, facilities);
        if (galaxy != null) {
            Integer homeSystemId = player.getHomeSystemId();
            if (homeSystemId != null) {
                SolarSystem homeSystem = context.getSolarSystems().get(homeSystemId);
                if (homeSystem != null) {
                    galaxy.registerSolarSystem(homeSystemId, homeSystem);
                }
            }
        }
        return planet;
    }

    /**
     * Sets up the home system for a player with custom planet properties and starting facilities.
     *
     * @param player the player to set up
     * @param size the planet size
     * @param mineralQuality the mineral quality
     * @param facilities the starting facilities to build
     * @return the created home planet
     */
    public Planet setupHomeSystem(Player player, PlanetSize size, MineralQuality mineralQuality,
                                  StartingFacility... facilities) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        if (size == null) {
            throw new IllegalArgumentException("Planet size must not be null");
        }
        if (mineralQuality == null) {
            throw new IllegalArgumentException("Mineral quality must not be null");
        }

        // Create the home solar system
        int homeSystemId = context.getNextSolarSystemIdAndIncrement();
        SolarSystem homeSolarSystem = new SolarSystem(homeSystemId);
        context.getSolarSystems().put(homeSystemId, homeSolarSystem);

        // Create the solar system entity for this system
        SolarSystemEntity homeEntity = new SolarSystemEntity(homeSystemId);

        // Create the home planet
        int planetId = context.getNextPlanetIdAndIncrement();
        Planet homePlanet = new Planet(planetId, size, mineralQuality);
        homePlanet.setName(player.getName() + " Prime");

        // Add starting facilities
        if (facilities != null) {
            for (StartingFacility facility : facilities) {
                if (facility != null) {
                    homePlanet.addStartingFacility(facility);
                }
            }
        }

        // Add the planet to the solar system entity at position (0,0,0)
        Hex3D planetPos = new Hex3D(0, 0, 0);
        homeEntity.addPlanet(homePlanet, planetPos);

        // Add the solar system entity to the solar system at origin
        Hex3D entityPos = new Hex3D(0, 0, 0);
        homeSolarSystem.addSystem(homeEntity, entityPos);

        // Register the planet with the game
        context.getPlanets().put(homePlanet.getId(), homePlanet);
        player.addPlanet(homePlanet.getId());

        // Set home location atomically with all coordinates
        int sectorRow = random.nextInt(10); // 0-9
        int sectorCol = random.nextInt(10); // 0-9
        int subSectorRow = random.nextInt(10); // 0-9
        int subSectorCol = random.nextInt(10); // 0-9

        HomeLocation homeLocation = HomeLocation.builder()
                .planetId(homePlanet.getId())
                .systemId(homeSystemId)
                .sector(sectorRow, sectorCol)
                .subSector(subSectorRow, subSectorCol)
                .build();
        player.setHomeLocation(homeLocation);

        return homePlanet;
    }
}
