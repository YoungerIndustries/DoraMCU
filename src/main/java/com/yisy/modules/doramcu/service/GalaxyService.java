package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.GalaxyGenerationProgress;
import com.yisy.modules.doramcu.repository.AsteroidBeltRepository;
import com.yisy.modules.doramcu.repository.AsteroidRepository;
import com.yisy.modules.doramcu.repository.BlackHoleRepository;
import com.yisy.modules.doramcu.repository.CometRepository;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.GasGiantRepository;
import com.yisy.modules.doramcu.repository.MoonRepository;
import com.yisy.modules.doramcu.repository.NebulaRepository;
import com.yisy.modules.doramcu.repository.PlanetRepository;
import com.yisy.modules.doramcu.repository.SectorRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import com.yisy.modules.doramcu.repository.SpaceStationRepository;
import com.yisy.modules.doramcu.repository.StarRepository;
import com.yisy.modules.doramcu.repository.SubSectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service layer for galaxy generation and persistence.
 * <p>
 * Uses {@link com.yisy.modules.doramcu.hex.GalaxyGenerator} to create galaxy data in memory,
 * then persists all entities to the database. This allows the game to load galaxies from
 * the database instead of keeping everything in memory.
 * </p>
 */
@Service
public class GalaxyService {

    private final GalaxyRepository galaxyRepository;
    private final SectorRepository sectorRepository;
    private final SubSectorRepository subSectorRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final StarRepository starRepository;
    private final PlanetRepository planetRepository;
    private final MoonRepository moonRepository;
    private final AsteroidBeltRepository asteroidBeltRepository;
    private final GasGiantRepository gasGiantRepository;
    private final NebulaRepository nebulaRepository;
    private final BlackHoleRepository blackHoleRepository;
    private final CometRepository cometRepository;
    private final SpaceStationRepository spaceStationRepository;
    private final AsteroidRepository asteroidRepository;

    private GalaxyGenerationProgress progressListener;

    public GalaxyService(GalaxyRepository galaxyRepository,
                         SectorRepository sectorRepository,
                         SubSectorRepository subSectorRepository,
                         SolarSystemRepository solarSystemRepository,
                         StarRepository starRepository,
                         PlanetRepository planetRepository,
                         MoonRepository moonRepository,
                         AsteroidBeltRepository asteroidBeltRepository,
                         GasGiantRepository gasGiantRepository,
                         NebulaRepository nebulaRepository,
                         BlackHoleRepository blackHoleRepository,
                         CometRepository cometRepository,
                         SpaceStationRepository spaceStationRepository,
                         AsteroidRepository asteroidRepository) {
        this.galaxyRepository = galaxyRepository;
        this.sectorRepository = sectorRepository;
        this.subSectorRepository = subSectorRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.starRepository = starRepository;
        this.planetRepository = planetRepository;
        this.moonRepository = moonRepository;
        this.asteroidBeltRepository = asteroidBeltRepository;
        this.gasGiantRepository = gasGiantRepository;
        this.nebulaRepository = nebulaRepository;
        this.blackHoleRepository = blackHoleRepository;
        this.cometRepository = cometRepository;
        this.spaceStationRepository = spaceStationRepository;
        this.asteroidRepository = asteroidRepository;
    }

    /**
     * Sets the progress listener for galaxy generation callbacks.
     * <p>
     * The listener receives updates during galaxy generation, allowing UI components
     * to display progress bars and status messages.
     * </p>
     *
     * @param listener the progress callback, or {@code null} to disable progress reporting
     */
    public void setProgressListener(GalaxyGenerationProgress listener) {
        this.progressListener = listener;
    }

    /**
     * Checks whether any galaxy exists in the database.
     *
     * @return {@code true} if at least one galaxy is stored
     */
    public boolean hasGalaxies() {
        return galaxyRepository.count() > 0;
    }

    /**
     * Checks whether a galaxy with the given name exists in the database.
     *
     * @param name the exact galaxy name to look up
     * @return {@code true} if a galaxy with that name is stored
     */
    public boolean hasGalaxy(String name) {
        return galaxyRepository.findByName(name).isPresent();
    }

    /**
     * Returns all galaxies stored in the database.
     *
     * @return list of all galaxies
     */
    public List<com.yisy.modules.doramcu.model.Galaxy> getAllGalaxies() {
        return galaxyRepository.findAll();
    }

    /**
     * Result of galaxy generation containing both the database entity and the in-memory galaxy.
     */
    public static class GalaxyGenerationResult {
        private final com.yisy.modules.doramcu.model.Galaxy databaseGalaxy;
        private final com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy;

        public GalaxyGenerationResult(com.yisy.modules.doramcu.model.Galaxy databaseGalaxy, com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy) {
            this.databaseGalaxy = databaseGalaxy;
            this.inMemoryGalaxy = inMemoryGalaxy;
        }

        public com.yisy.modules.doramcu.model.Galaxy getDatabaseGalaxy() {
            return databaseGalaxy;
        }

        public com.yisy.modules.doramcu.hex.Galaxy getInMemoryGalaxy() {
            return inMemoryGalaxy;
        }
    }

    /**
     * Generates a galaxy in memory without any database transaction.
     * <p>
     * This method is safe to call from a background thread as it does not
     * hold any database connections. Use {@link #persistGalaxy(String, Galaxy, double)}
     * to persist the result to the database.
     * </p>
     *
     * @return the in-memory galaxy
     */
    public com.yisy.modules.doramcu.hex.Galaxy generateGalaxyInMemory(
            String name, int systemCount,
            int planetsPerSystem,
            double moonChance, double asteroidBeltChance, double gasGiantChance,
            double nebulaChance, double blackHoleChance, double cometChance,
            double spaceStationChance, long seed) {

        // Fire pre-initialization progress callbacks
        if (progressListener != null) {
            progressListener.onPrepareStep("Loading star name database...");
            progressListener.onPrepareStep("Loading planet name database...");
            progressListener.onPrepareStep("Loading galaxy name database...");
            progressListener.onPrepareStep("Initializing random number generator (seed: " + seed + ")...");
            progressListener.onPrepareStep("Configuring galaxy name: " + name + "...");
            progressListener.onPrepareStep("Setting system count: " + systemCount + "...");
            progressListener.onPrepareStep("Setting planets per system: " + planetsPerSystem + "...");
            progressListener.onPrepareStep("Configuring celestial body probabilities...");
            progressListener.onPrepareStep("Preparing galaxy generation parameters...");
            progressListener.onReady();
        }

        GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                .systemCount(systemCount)
                .planetsPerSystem(planetsPerSystem)
                .moonChance(moonChance)
                .asteroidBeltChance(asteroidBeltChance)
                .gasGiantChance(gasGiantChance)
                .nebulaChance(nebulaChance)
                .blackHoleChance(blackHoleChance)
                .cometChance(cometChance)
                .spaceStationChance(spaceStationChance)
                .build();
        com.yisy.modules.doramcu.hex.GalaxyGenerator generator = new com.yisy.modules.doramcu.hex.GalaxyGenerator(seed);
        generator.setConfig(config);

        // Attach progress listener if set
        if (progressListener != null) {
            generator.setProgressListener(progressListener);
        }

        return generator.generateGalaxy(1, name);
    }

    /**
     * Persists an in-memory galaxy to the database.
     * <p>
     * This method is transactional and should be called after {@link #generateGalaxyInMemory}
     * completes. It saves all entities in a single transaction.
     * </p>
     *
     * @return the saved database galaxy entity
     */
    @Transactional
    public com.yisy.modules.doramcu.model.Galaxy persistGalaxy(String name,
            com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy,
            double hyperlaneDensity) {

        // Notify progress listener of persistence start
        if (progressListener != null) {
            progressListener.onPersistStart();
            progressListener.onLog("Persisting galaxy to database...");
        }

        com.yisy.modules.doramcu.model.Galaxy dbGalaxy = new com.yisy.modules.doramcu.model.Galaxy();
        dbGalaxy.setName(name);
        dbGalaxy.setHyperlaneDensity(hyperlaneDensity);
        dbGalaxy = galaxyRepository.save(dbGalaxy);

        // Persist all sectors and subsectors with their names
        int systemCount = 0;
        for (com.yisy.modules.doramcu.hex.Galaxy.Sector sector : inMemoryGalaxy.getAllSectors()) {
            com.yisy.modules.doramcu.model.Sector dbSector = new com.yisy.modules.doramcu.model.Sector();
            dbSector.setGalaxy(dbGalaxy);
            dbSector.setSectorRow(sector.getRow());
            dbSector.setSectorCol(sector.getCol());
            dbSector.setName(sector.getName());
            dbSector.setTradeType(sector.getTradeType());
            dbSector = sectorRepository.save(dbSector);

            for (com.yisy.modules.doramcu.hex.SubSector subSector : sector.getAllSubSectors()) {
                com.yisy.modules.doramcu.model.SubSector dbSubSector = new com.yisy.modules.doramcu.model.SubSector();
                dbSubSector.setSector(dbSector);
                dbSubSector.setSubSectorRow(subSector.getSubRow());
                dbSubSector.setSubSectorCol(subSector.getSubCol());
                dbSubSector.setName(subSector.getName());
                subSectorRepository.save(dbSubSector);
            }

            for (com.yisy.modules.doramcu.hex.SubSector subSector : sector.getAllSubSectors()) {
                for (com.yisy.modules.doramcu.hex.GalacticEntry entry : subSector.getAllEntries()) {
                    com.yisy.modules.doramcu.hex.SolarSystem inMemorySystem = entry.getSystem();

                    com.yisy.modules.doramcu.model.SolarSystem dbSystem = new com.yisy.modules.doramcu.model.SolarSystem();
                    dbSystem.setName("System-" + inMemorySystem.getId());
                    dbSystem.setGalaxy(dbGalaxy);
                    dbSystem.setSectorRow(sector.getRow());
                    dbSystem.setSectorCol(sector.getCol());
                    dbSystem.setSubSectorRow(subSector.getSubRow());
                    dbSystem.setSubSectorCol(subSector.getSubCol());
                    dbSystem.setPosX(entry.getX());
                    dbSystem.setPosY(entry.getY());
                    dbSystem.setPosZ(entry.getZ());
                    dbSystem = solarSystemRepository.save(dbSystem);

                    for (com.yisy.modules.doramcu.hex.SolarSystemEntity entity : inMemorySystem.getAllSystems().values()) {
                        persistStar(entity, dbSystem);
                        persistPlanet(entity, dbSystem);
                        persistMoon(entity, dbSystem);
                        persistAsteroidBelt(entity, dbSystem);
                        persistAsteroid(entity, dbSystem);
                        persistGasGiant(entity, dbSystem);
                        persistNebula(entity, dbSystem);
                        persistBlackHole(entity, dbSystem);
                        persistComet(entity, dbSystem);
                        persistSpaceStation(entity, dbSystem);
                    }
                    systemCount++;
                }
            }
        }

        // Verify galaxy persistence
        long persistedGalaxies = galaxyRepository.count();
        long persistedSystems = solarSystemRepository.count();
        if (persistedGalaxies == 0 || persistedSystems == 0) {
            throw new RuntimeException("Galaxy persistence verification failed: galaxies=" + persistedGalaxies + ", systems=" + persistedSystems);
        }

        // Notify progress listener of persistence complete
        if (progressListener != null) {
            progressListener.onLog("Saved " + systemCount + " solar systems to database.");
            progressListener.onLog("Verification: " + persistedGalaxies + " galaxy(ies), " + persistedSystems + " solar systems persisted.");
            progressListener.onPersistComplete();
        }

        return dbGalaxy;
    }

    /**
     * Convenience method that generates and persists in one call.
     * <p>
     * Note: Galaxy generation runs without a transaction, only persistence is transactional.
     * This avoids holding a database connection during the long-running generation phase.
     * </p>
     */
    public GalaxyGenerationResult generateAndSaveGalaxy(
            String name, int systemCount,
            int planetsPerSystem,
            double moonChance, double asteroidBeltChance, double gasGiantChance,
            double nebulaChance, double blackHoleChance, double cometChance,
            double spaceStationChance, double hyperlaneDensity, long seed) {

        // Step 1: Generate in memory (no transaction)
        com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy = generateGalaxyInMemory(
                name, systemCount, planetsPerSystem,
                moonChance, asteroidBeltChance, gasGiantChance,
                nebulaChance, blackHoleChance, cometChance,
                spaceStationChance, seed);

        // Step 2: Persist to database (transactional)
        com.yisy.modules.doramcu.model.Galaxy dbGalaxy = persistGalaxy(name, inMemoryGalaxy, hyperlaneDensity);

        return new GalaxyGenerationResult(dbGalaxy, inMemoryGalaxy);
    }

    /**
     * Generates and persists a galaxy with pre-configured empires.
     * Each empire's planets will be assigned shipyard and industrial facilities
     * based on probability during galaxy generation.
     * <p>
     * Note: Galaxy generation runs without a transaction, only persistence is transactional.
     * </p>
     *
     * @param name the galaxy name
     * @param empires map of empire ID to Empire instances (each headed by a player)
     * @param systemCount number of solar systems to generate
     * @param planetsPerSystem planets per solar system
     * @param moonChance probability of moons
     * @param asteroidBeltChance probability of asteroid belts
     * @param gasGiantChance probability of gas giants
     * @param nebulaChance probability of nebulas
     * @param blackHoleChance probability of black holes
     * @param cometChance probability of comets
     * @param spaceStationChance probability of space stations
     * @param hyperlaneDensity hyperlane density
     * @param seed random seed
     * @return generation result with database entity and in-memory galaxy
     */
    public GalaxyGenerationResult generateAndSaveGalaxyWithEmpires(
            String name, Map<Integer, com.yisy.modules.doramcu.hex.Empire> empires,
            int systemCount,
            int planetsPerSystem,
            double moonChance, double asteroidBeltChance, double gasGiantChance,
            double nebulaChance, double blackHoleChance, double cometChance,
            double spaceStationChance, double hyperlaneDensity, long seed) {

        // Step 1: Generate in memory with empires (no transaction)
        com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy = generateGalaxyInMemoryWithEmpires(
                name, empires, systemCount, planetsPerSystem,
                moonChance, asteroidBeltChance, gasGiantChance,
                nebulaChance, blackHoleChance, cometChance,
                spaceStationChance, seed);

        // Step 2: Persist to database (transactional)
        com.yisy.modules.doramcu.model.Galaxy dbGalaxy = persistGalaxy(name, inMemoryGalaxy, hyperlaneDensity);

        return new GalaxyGenerationResult(dbGalaxy, inMemoryGalaxy);
    }

    /**
     * Generates a galaxy in memory with pre-configured empires.
     * Planets will be assigned to empires with facilities during generation.
     *
     * @param name the galaxy name
     * @param empires map of empire ID to Empire instances
     * @param systemCount number of solar systems to generate
     * @param planetsPerSystem planets per solar system
     * @param moonChance probability of moons
     * @param asteroidBeltChance probability of asteroid belts
     * @param gasGiantChance probability of gas giants
     * @param nebulaChance probability of nebulas
     * @param blackHoleChance probability of black holes
     * @param cometChance probability of comets
     * @param spaceStationChance probability of space stations
     * @param seed random seed
     * @return the in-memory galaxy with empires and assigned planets
     */
    public com.yisy.modules.doramcu.hex.Galaxy generateGalaxyInMemoryWithEmpires(
            String name, Map<Integer, com.yisy.modules.doramcu.hex.Empire> empires,
            int systemCount,
            int planetsPerSystem,
            double moonChance, double asteroidBeltChance, double gasGiantChance,
            double nebulaChance, double blackHoleChance, double cometChance,
            double spaceStationChance, long seed) {

        // Fire pre-initialization progress callbacks
        if (progressListener != null) {
            progressListener.onPrepareStep("Loading star name database...");
            progressListener.onPrepareStep("Loading planet name database...");
            progressListener.onPrepareStep("Loading galaxy name database...");
            progressListener.onPrepareStep("Initializing random number generator (seed: " + seed + ")...");
            progressListener.onPrepareStep("Configuring galaxy name: " + name + "...");
            progressListener.onPrepareStep("Setting system count: " + systemCount + "...");
            progressListener.onPrepareStep("Setting planets per system: " + planetsPerSystem + "...");
            progressListener.onPrepareStep("Configuring celestial body probabilities...");
            progressListener.onPrepareStep("Preparing galaxy generation parameters...");
            progressListener.onReady();
        }

        GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                .systemCount(systemCount)
                .planetsPerSystem(planetsPerSystem)
                .moonChance(moonChance)
                .asteroidBeltChance(asteroidBeltChance)
                .gasGiantChance(gasGiantChance)
                .nebulaChance(nebulaChance)
                .blackHoleChance(blackHoleChance)
                .cometChance(cometChance)
                .spaceStationChance(spaceStationChance)
                .build();
        com.yisy.modules.doramcu.hex.GalaxyGenerator generator = new com.yisy.modules.doramcu.hex.GalaxyGenerator(seed);
        generator.setConfig(config);

        // Attach progress listener if set
        if (progressListener != null) {
            generator.setProgressListener(progressListener);
        }

        // Generate the galaxy
        com.yisy.modules.doramcu.hex.Galaxy galaxy = generator.generateGalaxy(1, name);

        // Assign planets to empires and establish capitals
        if (empires != null && !empires.isEmpty()) {
            assignPlanetsAndEstablishCapitals(galaxy, empires, new Random(seed));
        }

        return galaxy;
    }

    private void persistStar(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                             com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getStarCount() > 0) {
            for (com.yisy.modules.doramcu.hex.Star star : entity.getAllStars().values()) {
                com.yisy.modules.doramcu.model.Star dbStar = new com.yisy.modules.doramcu.model.Star();
                dbStar.setName(star.getName());
                dbStar.setSolarSystem(dbSystem);
                dbStar.setStarSize(com.yisy.modules.doramcu.model.Star.StarSize.valueOf(star.getStarSize().name()));
                dbStar.setStarType(com.yisy.modules.doramcu.model.Star.StarType.valueOf(star.getStarType().name()));
                dbStar.setRadius(star.getRadius());
                dbStar.setEnergyYield(star.getEnergyYield());
                dbStar.setStationSlots(star.getStationSlots());
                dbStar.setOccupiedSlots(0);
                dbStar.setRadiationDamage(star.getRadiationDamage());
                dbStar.setGravityWellBonus(star.getGravityWellBonus());
                starRepository.save(dbStar);
            }
        }
    }

    private void persistPlanet(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                               com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getPlanetCount() > 0) {
            for (com.yisy.modules.doramcu.hex.Planet planet : entity.getAllPlanets().values()) {
                com.yisy.modules.doramcu.model.Planet dbPlanet = new com.yisy.modules.doramcu.model.Planet();
                dbPlanet.setName(planet.getName());
                dbPlanet.setSolarSystem(dbSystem);
                dbPlanet.setPlanetSize(com.yisy.modules.doramcu.model.Planet.PlanetSize.valueOf(planet.getPlanetSize().name()));
                dbPlanet.setMineralQuality(com.yisy.modules.doramcu.model.Planet.MineralQuality.valueOf(planet.getMineralQuality().name()));
                dbPlanet.setRadius(planet.getRadius());
                // Flat resource production removed – use pipeline modules
                dbPlanet.setMineralProduction(0);
                dbPlanet.setEnergyProduction(0);
                dbPlanet.setFoodProduction(0);
                dbPlanet.setResearchProduction(0);
                dbPlanet.setInfluenceProduction(0);
                dbPlanet.setCurrentPopulation(planet.getCurrentPopulation());
                dbPlanet.setMaxPopulation(planet.getPlanetSize().getMaxPopulation());
                dbPlanet.setSocialStat(planet.getPopulationStat(com.yisy.modules.doramcu.hex.PopulationAttribute.SOCIAL));
                dbPlanet.setIntelligenceStat(planet.getPopulationStat(com.yisy.modules.doramcu.hex.PopulationAttribute.INTELLIGENCE));
                dbPlanet.setDiligenceStat(planet.getPopulationStat(com.yisy.modules.doramcu.hex.PopulationAttribute.DILIGENCE));
                dbPlanet.setResolveStat(planet.getPopulationStat(com.yisy.modules.doramcu.hex.PopulationAttribute.RESOLVE));
                dbPlanet.setStrategicDeposits(convertStrategicDeposits(planet.getStrategicDeposits()));
                planetRepository.save(dbPlanet);
            }
        }
    }

    private void persistMoon(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                             com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getMoonCount() > 0) {
            for (com.yisy.modules.doramcu.hex.Moon moon : entity.getAllMoons().values()) {
                com.yisy.modules.doramcu.model.Moon dbMoon = new com.yisy.modules.doramcu.model.Moon();
                dbMoon.setName("Moon-" + moon.getId());
                dbMoon.setSolarSystem(dbSystem);
                dbMoon.setMoonSize(com.yisy.modules.doramcu.model.Moon.MoonSize.valueOf(moon.getMoonSize().name()));
                dbMoon.setMineralYield(moon.getMineralYield());
                dbMoon.setOutpostSlots(moon.getOutpostSlots());
                if (moon.getStrategicResource() != null) {
                    dbMoon.setStrategicDeposit(com.yisy.modules.doramcu.model.StrategicResource.valueOf(moon.getStrategicResource().name()));
                }
                moonRepository.save(dbMoon);
            }
        }
    }

    private void persistAsteroidBelt(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                                     com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getAsteroidBeltCount() > 0) {
            for (com.yisy.modules.doramcu.hex.AsteroidBelt belt : entity.getAllAsteroidBelts().values()) {
                com.yisy.modules.doramcu.model.AsteroidBelt dbBelt = new com.yisy.modules.doramcu.model.AsteroidBelt();
                dbBelt.setName("Belt-" + belt.getId());
                dbBelt.setSolarSystem(dbSystem);
                dbBelt.setRadius(belt.getRadius());
                dbBelt.setMineralYield(belt.getMineralYield());
                dbBelt.setStationSlots(belt.getStationSlots());
                dbBelt.setMovementPenalty(belt.getMovementPenalty());
                if (belt.getStrategicResource() != null) {
                    dbBelt.setStrategicDeposit(com.yisy.modules.doramcu.model.StrategicResource.valueOf(belt.getStrategicResource().name()));
                }
                asteroidBeltRepository.save(dbBelt);
            }
        }
    }

    private void persistGasGiant(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                                 com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getGasGiantCount() > 0) {
            for (com.yisy.modules.doramcu.hex.GasGiant gg : entity.getAllGasGiants().values()) {
                com.yisy.modules.doramcu.model.GasGiant dbGg = new com.yisy.modules.doramcu.model.GasGiant();
                dbGg.setName("GasGiant-" + gg.getId());
                dbGg.setSolarSystem(dbSystem);
                dbGg.setRadius(gg.getRadius());
                dbGg.setEnergyYield(gg.getEnergyYield());
                dbGg.setStationSlots(gg.getStationSlots());
                dbGg.setGravitationalPull(gg.getGravitationalPull());
                dbGg.setSlingshotBonus(gg.getGravitationalPull());
                gasGiantRepository.save(dbGg);
            }
        }
    }

    private void persistNebula(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                               com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getNebulaCount() > 0) {
            for (com.yisy.modules.doramcu.hex.Nebula nebula : entity.getAllNebulas().values()) {
                com.yisy.modules.doramcu.model.Nebula dbNebula = new com.yisy.modules.doramcu.model.Nebula();
                dbNebula.setName("Nebula-" + nebula.getId());
                dbNebula.setSolarSystem(dbSystem);
                dbNebula.setNebulaSize(com.yisy.modules.doramcu.model.Nebula.NebulaSize.valueOf(nebula.getNebulaSize().name()));
                dbNebula.setRadius(nebula.getRadius());
                dbNebula.setEleriumYield(nebula.getEleriumYield());
                dbNebula.setStationSlots(nebula.getStationSlots());
                dbNebula.setSensorJamming(nebula.getSensorJamming());
                dbNebula.setCloakingBonus(nebula.getCloakingBonus());
                nebulaRepository.save(dbNebula);
            }
        }
    }

    private void persistBlackHole(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                                  com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getBlackHoleCount() > 0) {
            for (com.yisy.modules.doramcu.hex.BlackHole bh : entity.getAllBlackHoles().values()) {
                com.yisy.modules.doramcu.model.BlackHole dbBh = new com.yisy.modules.doramcu.model.BlackHole();
                dbBh.setName("BlackHole-" + bh.getId());
                dbBh.setSolarSystem(dbSystem);
                dbBh.setBlackHoleSize(com.yisy.modules.doramcu.model.BlackHole.BlackHoleSize.valueOf(bh.getBlackHoleSize().name()));
                dbBh.setRadius(bh.getRadius());
                dbBh.setAntiMatterYield(bh.getStrategicResource() != null ? 2 : 0);
                dbBh.setStationSlots(bh.getStationSlots());
                dbBh.setGravitationalPull(bh.getGravitationalPull());
                dbBh.setDestructionChance(bh.getDestructionChance());
                blackHoleRepository.save(dbBh);
            }
        }
    }

    private void persistComet(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                              com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getCometCount() > 0) {
            for (com.yisy.modules.doramcu.hex.Comet comet : entity.getAllComets().values()) {
                com.yisy.modules.doramcu.model.Comet dbComet = new com.yisy.modules.doramcu.model.Comet();
                dbComet.setName("Comet-" + comet.getId());
                dbComet.setSolarSystem(dbSystem);
                dbComet.setCometSize(com.yisy.modules.doramcu.model.Comet.CometSize.valueOf(comet.getCometSize().name()));
                dbComet.setResourceBonus(comet.getResourceBonus());
                dbComet.setDurationTurns(comet.getDurationTurns());
                dbComet.setRemainingTurns(comet.getRemainingTurns());
                dbComet.setHarvested(comet.isHarvested());
                if (comet.getStrategicResource() != null) {
                    dbComet.setStrategicDeposit(com.yisy.modules.doramcu.model.StrategicResource.valueOf(comet.getStrategicResource().name()));
                }
                cometRepository.save(dbComet);
            }
        }
    }

    private void persistSpaceStation(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                                     com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getSpaceStationCount() > 0) {
            for (com.yisy.modules.doramcu.hex.SpaceStation station : entity.getAllSpaceStations().values()) {
                com.yisy.modules.doramcu.model.SpaceStation dbStation = new com.yisy.modules.doramcu.model.SpaceStation();
                dbStation.setName("Station-" + station.getId());
                dbStation.setSolarSystem(dbSystem);
                dbStation.setStationType(com.yisy.modules.doramcu.model.SpaceStation.StationType.valueOf(station.getStationType().name()));
                dbStation.setHexX(station.getPosition().getX());
                dbStation.setHexY(station.getPosition().getY());
                dbStation.setHexZ(station.getPosition().getZ());
                dbStation.setHullPoints(station.getHullPoints());
                dbStation.setCurrentHull(station.getCurrentHullPoints());
                dbStation.setShieldPoints(station.getShieldPoints());
                dbStation.setCurrentShield(station.getCurrentShieldPoints());
                dbStation.setComponentSlots(station.getModuleSlots());
                dbStation.setOccupiedSlots(station.getOccupiedSlots());
                dbStation.setDamage(0);
                spaceStationRepository.save(dbStation);
            }
        }
    }

    private void persistAsteroid(com.yisy.modules.doramcu.hex.SolarSystemEntity entity,
                                 com.yisy.modules.doramcu.model.SolarSystem dbSystem) {
        if (entity.getAsteroidCount() > 0) {
            for (com.yisy.modules.doramcu.hex.Asteroid asteroid : entity.getAllAsteroids().values()) {
                com.yisy.modules.doramcu.model.Asteroid dbAsteroid = new com.yisy.modules.doramcu.model.Asteroid();
                dbAsteroid.setName("Asteroid-" + asteroid.getId());
                dbAsteroid.setSolarSystem(dbSystem);
                dbAsteroid.setAsteroidSize(com.yisy.modules.doramcu.model.Asteroid.AsteroidSize.valueOf(asteroid.getAsteroidSize().name()));
                dbAsteroid.setMineralYield(asteroid.getMineralYield());
                dbAsteroid.setOutpostSlots(asteroid.getOutpostSlots());
                dbAsteroid.setMovementPenalty(asteroid.getMovementPenalty());
                if (asteroid.getStrategicResource() != null) {
                    dbAsteroid.setStrategicDeposit(com.yisy.modules.doramcu.model.StrategicResource.valueOf(asteroid.getStrategicResource().name()));
                }
                asteroidRepository.save(dbAsteroid);
            }
        }
    }

    private Set<com.yisy.modules.doramcu.model.StrategicResource> convertStrategicDeposits(
            Set<com.yisy.modules.doramcu.hex.StrategicResource> deposits) {
        Set<com.yisy.modules.doramcu.model.StrategicResource> converted = new HashSet<>();
        for (com.yisy.modules.doramcu.hex.StrategicResource r : deposits) {
            converted.add(com.yisy.modules.doramcu.model.StrategicResource.valueOf(r.name()));
        }
        return converted;
    }


    @Transactional(readOnly = true)
    public Optional<com.yisy.modules.doramcu.model.Galaxy> getGalaxyById(Long id) {
        return galaxyRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<com.yisy.modules.doramcu.model.SolarSystem> getSolarSystemsByGalaxy(Long galaxyId) {
        return solarSystemRepository.findByGalaxyId(galaxyId);
    }

    @Transactional
    public void deleteAllGalaxies() {
        galaxyRepository.deleteAll();
    }

    /**
     * Assigns planets in the galaxy to empires and establishes capitals.
     * Replaces the removed Galaxy game controller methods.
     */
    private static void assignPlanetsAndEstablishCapitals(
            com.yisy.modules.doramcu.hex.Galaxy galaxy,
            Map<Integer, com.yisy.modules.doramcu.hex.Empire> empires,
            Random random) {
        
        // Assign all planets to empires
        double independentChance = 0.6;
        for (com.yisy.modules.doramcu.hex.SolarSystem system : galaxy.getAllSystems().values()) {
            for (com.yisy.modules.doramcu.hex.SolarSystemEntity entity : system.getAllSystems().values()) {
                for (com.yisy.modules.doramcu.hex.Planet planet : entity.getAllPlanets().values()) {
                    if (random.nextDouble() < independentChance) {
                        planet.setOwnerEmpireId(null);
                    } else {
                        List<com.yisy.modules.doramcu.hex.Empire> empireList = new ArrayList<>(empires.values());
                        com.yisy.modules.doramcu.hex.Empire empire = empireList.get(random.nextInt(empireList.size()));
                        planet.setOwnerEmpireId(empire.getId());
                        empire.assignPlanetFacilities(planet, random,
                                com.yisy.modules.doramcu.hex.Empire.DEFAULT_SHIPYARD_CHANCE,
                                com.yisy.modules.doramcu.hex.Empire.DEFAULT_INDUSTRIAL_CHANCE,
                                com.yisy.modules.doramcu.hex.Empire.DEFAULT_MIN_INDUSTRIAL_LEVEL,
                                com.yisy.modules.doramcu.hex.Empire.DEFAULT_MAX_INDUSTRIAL_LEVEL);
                    }
                }
            }
        }

        // Establish capitals for each empire
        for (com.yisy.modules.doramcu.hex.Empire empire : empires.values()) {
            if (empire.getOwnedPlanetIds().isEmpty()) {
                continue;
            }

            com.yisy.modules.doramcu.hex.Planet bestPlanet = null;
            for (int planetId : empire.getOwnedPlanetIds()) {
                com.yisy.modules.doramcu.hex.Planet planet = findPlanetInGalaxy(galaxy, planetId);
                if (planet == null) continue;

                if (bestPlanet == null) {
                    bestPlanet = planet;
                } else {
                    int sizeCompare = planet.getPlanetSize().ordinal() - bestPlanet.getPlanetSize().ordinal();
                    if (sizeCompare > 0) {
                        bestPlanet = planet;
                    } else if (sizeCompare == 0) {
                        int planetTotal = 0; // Flat resources removed
                        int bestTotal = 0;
                        if (planetTotal > bestTotal) {
                            bestPlanet = planet;
                        }
                    }
                }
            }

            if (bestPlanet == null) {
                continue;
            }

            bestPlanet.setCapital(true);
            bestPlanet.setOwnerEmpireId(empire.getId());
            empire.setCapitalPlanet(bestPlanet.getId());

            for (com.yisy.modules.doramcu.hex.StartingFacility facility : com.yisy.modules.doramcu.hex.StartingFacility.values()) {
                bestPlanet.addStartingFacility(facility);
            }

            for (int planetId : empire.getOwnedPlanetIds()) {
                com.yisy.modules.doramcu.hex.Planet planet = findPlanetInGalaxy(galaxy, planetId);
                if (planet != null) {
                    planet.increaseEconomicLevel(1);
                }
            }
        }
    }

    /**
     * Finds a planet by ID across all solar systems in the galaxy.
     */
    private static com.yisy.modules.doramcu.hex.Planet findPlanetInGalaxy(
            com.yisy.modules.doramcu.hex.Galaxy galaxy, int planetId) {
        for (com.yisy.modules.doramcu.hex.SolarSystem system : galaxy.getAllSystems().values()) {
            for (com.yisy.modules.doramcu.hex.SolarSystemEntity entity : system.getAllSystems().values()) {
                com.yisy.modules.doramcu.hex.Planet planet = entity.getAllPlanets().get(planetId);
                if (planet != null) {
                    return planet;
                }
            }
        }
        return null;
    }

    // ==================== GALAXY FACTORY METHODS ====================

    /**
     * Generates and persists the game galaxy ("Milky Way") if it doesn't already exist.
     * Uses the predefined configuration from {@link GalaxyFactory}.
     *
     * @return the saved database galaxy entity
     */
    @Transactional
    public com.yisy.modules.doramcu.model.Galaxy ensureGameGalaxyExists() {
        List<com.yisy.modules.doramcu.model.Galaxy> galaxies = getAllGalaxies();
        for (com.yisy.modules.doramcu.model.Galaxy galaxy : galaxies) {
            if (GalaxyFactory.GAME_GALAXY_NAME.equals(galaxy.getName())) {
                return galaxy;
            }
        }
        com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy = GalaxyFactory.generateGameGalaxy();
        return persistGalaxy(GalaxyFactory.GAME_GALAXY_NAME, inMemoryGalaxy,
                GalaxyFactory.gameGalaxyConfig().getHyperlaneDensity());
    }

    /**
     * Returns the game galaxy from the database, or generates it if it doesn't exist.
     *
     * @return the game galaxy entity
     */
    @Transactional(readOnly = true)
    public Optional<com.yisy.modules.doramcu.model.Galaxy> getGameGalaxy() {
        List<com.yisy.modules.doramcu.model.Galaxy> galaxies = getAllGalaxies();
        for (com.yisy.modules.doramcu.model.Galaxy galaxy : galaxies) {
            if (GalaxyFactory.GAME_GALAXY_NAME.equals(galaxy.getName())) {
                return Optional.of(galaxy);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a sector by galaxy ID and grid position.
     *
     * @param galaxyId the galaxy ID
     * @param sectorRow the sector row
     * @param sectorCol the sector column
     * @return the sector, or null if not found
     */
    @Transactional(readOnly = true)
    public com.yisy.modules.doramcu.model.Sector findSectorByGalaxyAndPosition(Long galaxyId, int sectorRow, int sectorCol) {
        return sectorRepository.findByGalaxyIdAndSectorRowAndSectorCol(galaxyId, sectorRow, sectorCol);
    }

    /**
     * Finds a subsector by sector ID and grid position.
     *
     * @param sectorId the sector ID
     * @param subSectorRow the subsector row
     * @param subSectorCol the subsector column
     * @return the subsector, or null if not found
     */
    @Transactional(readOnly = true)
    public com.yisy.modules.doramcu.model.SubSector findSubSectorBySectorAndPosition(Long sectorId, int subSectorRow, int subSectorCol) {
        return subSectorRepository.findBySectorIdAndSubSectorRowAndSubSectorCol(sectorId, subSectorRow, subSectorCol);
    }
}
