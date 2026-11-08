package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that initializes a Game instance from database entities.
 * <p>
 * Bridges the JPA persistence layer with the in-memory game logic by loading
 * players and galaxies from the database and constructing a complete {@link Game}
 * instance ready for play.
 * </p>
 * <p>
 * The service performs the following steps:
 * <ol>
 *   <li>Loads all players from the players table</li>
 *   <li>Creates hex.Game.Player instances with starting resources</li>
 *   <li>Loads all galaxies from the galaxies table</li>
 *   <li>Constructs a Game instance with all players and galaxies</li>
 * </ol>
 * </p>
 */
@Service
public class GameInitializationService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GalaxyRepository galaxyRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private SubSectorRepository subSectorRepository;

    @Autowired
    private SolarSystemRepository solarSystemRepository;

    @Autowired
    private StarRepository starRepository;

    @Autowired
    private PlanetRepository planetRepository;

    /**
     * Initializes a new Game instance from database entities.
     * <p>
     * Loads all registered players and galaxies from the database,
     * creates in-memory representations, and returns a fully configured
     * Game instance ready for play.
     * </p>
     *
     * @param gameName the name of the game session
     * @return a fully initialized Game instance
     */
    public Game initializeGame(String gameName) {
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game name must not be null or blank");
        }

        Game game = new Game(gameName);

        // Load and add players from database
        List<com.yisy.modules.doramcu.model.Player> dbPlayers = playerRepository.findAll();
        int gamePlayerId = 1;
        for (com.yisy.modules.doramcu.model.Player dbPlayer : dbPlayers) {
            Player gamePlayer = new Player(gamePlayerId, dbPlayer.getEmpireName());
            gamePlayer.setEmpireId(gamePlayerId);
            game.addPlayer(gamePlayer);

            // Create and register an Empire for this player
            Empire empire = new Empire(gamePlayerId, dbPlayer.getEmpireName() + " Empire", gamePlayerId);
            game.registerEmpire(empire);

            gamePlayerId++;
        }

        // Load and add galaxies from database
        List<com.yisy.modules.doramcu.model.Galaxy> dbGalaxies = galaxyRepository.findAll();
        for (com.yisy.modules.doramcu.model.Galaxy dbGalaxy : dbGalaxies) {
            Galaxy inMemoryGalaxy = convertToInMemoryGalaxy(dbGalaxy);
            game.addGalaxy(inMemoryGalaxy);
        }

        return game;
    }

    /**
     * Initializes a game with only specific players.
     * <p>
     * Useful when starting a game session for a subset of registered players,
     * such as when only certain players have logged in.
     * </p>
     *
     * @param gameName the name of the game session
     * @param playerEmails the emails of players to include
     * @return a fully initialized Game instance
     */
    public Game initializeGameWithPlayers(String gameName, List<String> playerEmails) {
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game name must not be null or blank");
        }
        if (playerEmails == null || playerEmails.isEmpty()) {
            throw new IllegalArgumentException("Player emails must not be null or empty");
        }

        Game game = new Game(gameName);

        // Load only the specified players
        int gamePlayerId = 1;
        for (String email : playerEmails) {
            com.yisy.modules.doramcu.model.Player dbPlayer = playerRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found: " + email));
            Player gamePlayer = new Player(gamePlayerId, dbPlayer.getEmpireName());
            gamePlayer.setEmpireId(gamePlayerId);
            game.addPlayer(gamePlayer);

            // Create and register an Empire for this player
            Empire empire = new Empire(gamePlayerId, dbPlayer.getEmpireName() + " Empire", gamePlayerId);
            game.registerEmpire(empire);

            gamePlayerId++;
        }

        // Load all galaxies
        List<com.yisy.modules.doramcu.model.Galaxy> dbGalaxies = galaxyRepository.findAll();
        for (com.yisy.modules.doramcu.model.Galaxy dbGalaxy : dbGalaxies) {
            Galaxy inMemoryGalaxy = convertToInMemoryGalaxy(dbGalaxy);
            game.addGalaxy(inMemoryGalaxy);
        }

        return game;
    }

    /**
     * Returns all registered players from the database.
     *
     * @return list of all database players
     */
    public List<com.yisy.modules.doramcu.model.Player> getAllRegisteredPlayers() {
        return playerRepository.findAll();
    }

    /**
     * Returns a player by email.
     *
     * @param email the player's email
     * @return the player, or null if not found
     */
    public com.yisy.modules.doramcu.model.Player getPlayerByEmail(String email) {
        return playerRepository.findByEmail(email).orElse(null);
    }

    /**
     * Returns all galaxies from the database.
     *
     * @return list of all database galaxies
     */
    public List<com.yisy.modules.doramcu.model.Galaxy> getAllGalaxies() {
        return galaxyRepository.findAll();
    }

    /**
     * Checks if there are any registered players in the database.
     *
     * @return true if at least one player exists
     */
    public boolean hasRegisteredPlayers() {
        return playerRepository.count() > 0;
    }

    /**
     * Checks if there are any galaxies in the database.
     *
     * @return true if at least one galaxy exists
     */
    public boolean hasGalaxies() {
        return galaxyRepository.count() > 0;
    }

    /**
     * Converts a database galaxy to an in-memory Galaxy instance.
     * <p>
     * Creates a new hex.Galaxy with the same name and populates it with
     * sectors and solar systems from the database.
     * </p>
     *
     * @param dbGalaxy the database galaxy entity
     * @return the in-memory galaxy
     */
    public Galaxy convertToInMemoryGalaxy(com.yisy.modules.doramcu.model.Galaxy dbGalaxy) {
        Galaxy galaxy = new Galaxy(dbGalaxy.getId().intValue(), dbGalaxy.getName());

        // Load all sectors for this galaxy and apply stored names
        List<com.yisy.modules.doramcu.model.Sector> dbSectors = sectorRepository.findByGalaxyId(dbGalaxy.getId());
        for (com.yisy.modules.doramcu.model.Sector dbSector : dbSectors) {
            Galaxy.Sector hexSector = galaxy.getSector(dbSector.getSectorRow(), dbSector.getSectorCol());
            if (hexSector != null) {
                hexSector.setName(dbSector.getName());
                if (dbSector.getTradeType() != null) {
                    hexSector.setTradeType(dbSector.getTradeType());
                }

                // Load all subsectors for this sector and apply stored names
                List<com.yisy.modules.doramcu.model.SubSector> dbSubSectors =
                        subSectorRepository.findBySectorId(dbSector.getId());
                for (com.yisy.modules.doramcu.model.SubSector dbSubSector : dbSubSectors) {
                    com.yisy.modules.doramcu.hex.SubSector hexSubSector =
                            hexSector.getSubSector(dbSubSector.getSubSectorRow(), dbSubSector.getSubSectorCol());
                    if (hexSubSector != null) {
                        hexSubSector.setName(dbSubSector.getName());
                    }
                }
            }
        }

        // Load all solar systems for this galaxy
        List<com.yisy.modules.doramcu.model.SolarSystem> dbSystems =
                solarSystemRepository.findByGalaxyId(dbGalaxy.getId());

        // Group solar systems by sector
        Map<String, List<com.yisy.modules.doramcu.model.SolarSystem>> systemsBySector =
                dbSystems.stream()
                        .collect(Collectors.groupingBy(
                                ss -> ss.getSectorRow() + "-" + ss.getSectorCol()
                        ));

        // Add systems to the galaxy
        for (Map.Entry<String, List<com.yisy.modules.doramcu.model.SolarSystem>> entry :
                systemsBySector.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int sectorRow = Integer.parseInt(parts[0]);
            int sectorCol = Integer.parseInt(parts[1]);

            for (com.yisy.modules.doramcu.model.SolarSystem dbSystem : entry.getValue()) {
                int systemId = dbSystem.getId().intValue();
                // Create in-memory solar system
                SolarSystem inMemorySystem = new SolarSystem(systemId);

                // Note: Celestial bodies are NOT loaded here to keep memory usage low.
                // The buildSubSectorInfo() fallback path queries the database directly
                // for system names, positions, and planet data when needed.
                // loadCelestialBodies(systemId, inMemorySystem);

                // Add to galaxy at the correct position
                galaxy.addSystem(
                        inMemorySystem,
                        sectorRow, sectorCol,
                        dbSystem.getSubSectorRow(), dbSystem.getSubSectorCol(),
                        dbSystem.getPosX(), dbSystem.getPosY(), dbSystem.getPosZ()
                );
            }
        }

        return galaxy;
    }

    /**
     * Loads essential celestial bodies for a solar system from the database.
     * Only loads stars and planets to keep memory usage manageable across 20k+ systems.
     */
    private void loadCelestialBodies(int systemId, SolarSystem inMemorySystem) {
        Random random = new Random(systemId); // Deterministic positions per system

        // Load stars
        com.yisy.modules.doramcu.model.Star dbStar = starRepository.findBySolarSystemId((long) systemId);
        if (dbStar != null) {
            Star star = new Star(dbStar.getId().intValue());
            star.setName(dbStar.getName());
            if (dbStar.getStarSize() != null) {
                try {
                    star.setStarSize(Star.StarSize.valueOf(dbStar.getStarSize().name()));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (dbStar.getStarType() != null) {
                try {
                    star.setStarType(Star.StarType.valueOf(dbStar.getStarType().name()));
                } catch (IllegalArgumentException ignored) {
                }
            }
            SolarSystemEntity starEntity = new SolarSystemEntity(dbStar.getId().intValue() * 100);
            starEntity.addStar(star, new Hex3D(0, 0, 0));
            inMemorySystem.addSystem(starEntity, new Hex3D(0, 0, 0));
        }

        // Load planets
        List<com.yisy.modules.doramcu.model.Planet> dbPlanets = planetRepository.findBySolarSystemId((long) systemId);
        for (com.yisy.modules.doramcu.model.Planet dbPlanet : dbPlanets) {
            Planet planet = new Planet(dbPlanet.getId().intValue());
            planet.setName(dbPlanet.getName());
            if (dbPlanet.getPlanetSize() != null) {
                try {
                    planet.setPlanetSize(PlanetSize.valueOf(dbPlanet.getPlanetSize().name()));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (dbPlanet.getMineralQuality() != null) {
                try {
                    planet.setMineralQuality(MineralQuality.valueOf(dbPlanet.getMineralQuality().name()));
                } catch (IllegalArgumentException ignored) {
                }
            }
            Hex3D planetPos = generateRandomHex3D(random);
            SolarSystemEntity planetEntity = new SolarSystemEntity(dbPlanet.getId().intValue() * 100);
            planetEntity.addPlanet(planet, planetPos);
            inMemorySystem.addSystem(planetEntity, planetPos);
        }
    }

    /**
     * Generates a random Hex3D coordinate where x + y + z = 0.
     * Used for positions within a SolarSystemEntity.
     */
    private Hex3D generateRandomHex3D(Random random) {
        int x = random.nextInt(10) - 5;
        int y = random.nextInt(10) - 5;
        int z = -(x + y);
        return new Hex3D(x, y, z);
    }
}
