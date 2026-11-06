package com.yisy.modules.doramcu.hex;

import java.util.Random;

/**
 * Generates individual celestial bodies with random properties.
 * <p>
 * Extracted from GalaxyGenerator to separate the creation of individual
 * celestial bodies (stars, planets, moons, etc.) from the galaxy orchestration
 * and solar system assembly logic.
 * </p>
 */
public class CelestialBodyGenerator {

    private final Random random;
    private final StarNameDatabase starNames;
    private final PlanetNameDatabase planetNames;

    // ID counters
    private int nextStarId;
    private int nextPlanetId;
    private int nextMoonId;
    private int nextAsteroidBeltId;
    private int nextGasGiantId;
    private int nextNebulaId;
    private int nextBlackHoleId;
    private int nextCometId;
    private int nextSpaceStationId;

    /**
     * Creates a new celestial body generator.
     *
     * @param random the random instance for reproducibility
     * @param starNames the star name database
     * @param planetNames the planet name database
     */
    public CelestialBodyGenerator(Random random, StarNameDatabase starNames, PlanetNameDatabase planetNames) {
        this.random = random;
        this.starNames = starNames;
        this.planetNames = planetNames;
        resetIdCounters();
    }

    /**
     * Resets all ID counters to their initial values.
     */
    public void resetIdCounters() {
        this.nextStarId = 1;
        this.nextPlanetId = 1;
        this.nextMoonId = 1;
        this.nextAsteroidBeltId = 1;
        this.nextGasGiantId = 1;
        this.nextNebulaId = 1;
        this.nextBlackHoleId = 1;
        this.nextCometId = 1;
        this.nextSpaceStationId = 1;
    }

    // ---- Celestial Body Generation ----

    /**
     * Generates a random star with random type and size.
     *
     * @return the generated star
     */
    public Star generateStar() {
        int id = nextStarId++;
        Star.StarType type = randomStarType();
        Star.StarSize size = randomStarSize();
        Star star = new Star(id, size, type);
        starNames.assignName(star);
        return star;
    }

    /**
     * Generates a random planet with random size and mineral quality.
     *
     * @return the generated planet
     */
    public Planet generatePlanet() {
        int id = nextPlanetId++;
        PlanetSize size = randomPlanetSize();
        MineralQuality quality = randomMineralQuality();

        Planet planet = new Planet(id);
        planet.setPlanetSize(size);
        planet.setMineralQuality(quality);
        planetNames.assignName(planet);

        // Resource production handled by ResourcePipelineManager mining operations

        // Set random population stats (1-10)
        for (PopulationAttribute attr : PopulationAttribute.values()) {
            planet.setPopulationStat(attr, random.nextInt(10) + 1);
        }

        // Set random population
        planet.setCurrentPopulation(random.nextInt(size.getMaxPopulation() + 1));

        // Add strategic resource deposits (10% chance per resource)
        for (StrategicResource strategic : StrategicResource.values()) {
            if (random.nextDouble() < 0.1) {
                planet.addStrategicDeposit(strategic);
            }
        }

        return planet;
    }

    /**
     * Generates a random moon orbiting the given parent planet.
     *
     * @param parentPlanet the parent planet
     * @return the generated moon
     */
    public Moon generateMoon(Planet parentPlanet) {
        int id = nextMoonId++;
        Moon.MoonSize size = randomMoonSize();
        Moon moon = new Moon(id, size);
        moon.setParentPlanet(parentPlanet.getId());

        // Random strategic deposit
        if (random.nextDouble() < 0.15) {
            StrategicResource[] resources = StrategicResource.values();
            moon.setStrategicResource(resources[random.nextInt(resources.length)]);
        }

        return moon;
    }

    /**
     * Generates a random asteroid belt.
     *
     * @return the generated asteroid belt
     */
    public AsteroidBelt generateAsteroidBelt() {
        int id = nextAsteroidBeltId++;
        int radius = 5 + random.nextInt(10);
        AsteroidBelt belt = new AsteroidBelt(id, radius);

        // Random strategic deposit
        if (random.nextDouble() < 0.2) {
            StrategicResource[] resources = StrategicResource.values();
            belt.setStrategicResource(resources[random.nextInt(resources.length)]);
        }

        return belt;
    }

    /**
     * Generates a random gas giant.
     *
     * @return the generated gas giant
     */
    public GasGiant generateGasGiant() {
        int id = nextGasGiantId++;
        int radius = 8 + random.nextInt(12);
        GasGiant gasGiant = new GasGiant(id, radius);

        // Random strategic deposit
        if (random.nextDouble() < 0.15) {
            StrategicResource[] resources = StrategicResource.values();
            gasGiant.setStrategicResource(resources[random.nextInt(resources.length)]);
        }

        return gasGiant;
    }

    /**
     * Generates a random nebula.
     *
     * @return the generated nebula
     */
    public Nebula generateNebula() {
        int id = nextNebulaId++;
        Nebula.NebulaSize size = randomNebulaSize();
        return new Nebula(id, size);
    }

    /**
     * Generates a random black hole.
     *
     * @return the generated black hole
     */
    public BlackHole generateBlackHole() {
        int id = nextBlackHoleId++;
        BlackHole.BlackHoleSize size = randomBlackHoleSize();
        return new BlackHole(id, size);
    }

    /**
     * Generates a random comet.
     *
     * @return the generated comet
     */
    public Comet generateComet() {
        int id = nextCometId++;
        Comet.CometSize size = randomCometSize();
        Comet comet = new Comet(id, size);

        // Random strategic deposit
        if (random.nextDouble() < 0.1) {
            StrategicResource[] resources = StrategicResource.values();
            comet.setStrategicResource(resources[random.nextInt(resources.length)]);
        }

        return comet;
    }

    /**
     * Generates a random space station.
     *
     * @return the generated space station
     */
    public SpaceStation generateSpaceStation() {
        int id = nextSpaceStationId++;
        SpaceStationType type = randomSpaceStationType();
        SpaceStation station = new SpaceStation(id, type);

        // Add random modules, respecting the station's slot limit
        int maxSlots = station.getModuleSlots();
        int moduleCount = 1 + random.nextInt(Math.min(maxSlots, 3));
        for (int i = 0; i < moduleCount && station.getAvailableSlots() > 0; i++) {
            StationModule module = randomStationModule();
            station.addModule(module);
        }

        return station;
    }

    /**
     * Generates a random station module.
     *
     * @return a random station module
     */
    private StationModule randomStationModule() {
        StationModule.ModuleType moduleType = randomStationModuleType();
        int quantity = 1 + random.nextInt(3);

        switch (moduleType) {
            case WEAPON:
                WeaponType[] weaponTypes = WeaponType.values();
                return new StationModule(weaponTypes[random.nextInt(weaponTypes.length)], quantity);
            case SHIELD:
                return new StationModule(quantity);
            case POINT_DEFENSE:
                return StationModule.pointDefense(quantity);
            case TARGETING:
                return StationModule.targeting(quantity);
            case HULL_REINFORCEMENT:
                return StationModule.hullReinforcement(quantity);
            case RESOURCE:
                return StationModule.resource(quantity);
            default:
                return new StationModule(quantity);
        }
    }

    // ---- Random Enum Selectors ----

    private Star.StarType randomStarType() {
        Star.StarType[] types = Star.StarType.values();
        return types[random.nextInt(types.length)];
    }

    private Star.StarSize randomStarSize() {
        Star.StarSize[] sizes = Star.StarSize.values();
        return sizes[random.nextInt(sizes.length)];
    }

    private PlanetSize randomPlanetSize() {
        PlanetSize[] sizes = PlanetSize.values();
        return sizes[random.nextInt(sizes.length)];
    }

    private MineralQuality randomMineralQuality() {
        MineralQuality[] qualities = MineralQuality.values();
        return qualities[random.nextInt(qualities.length)];
    }

    private Moon.MoonSize randomMoonSize() {
        Moon.MoonSize[] sizes = Moon.MoonSize.values();
        return sizes[random.nextInt(sizes.length)];
    }

    private Nebula.NebulaSize randomNebulaSize() {
        Nebula.NebulaSize[] sizes = Nebula.NebulaSize.values();
        return sizes[random.nextInt(sizes.length)];
    }

    private BlackHole.BlackHoleSize randomBlackHoleSize() {
        BlackHole.BlackHoleSize[] sizes = BlackHole.BlackHoleSize.values();
        return sizes[random.nextInt(sizes.length)];
    }

    private Comet.CometSize randomCometSize() {
        Comet.CometSize[] sizes = Comet.CometSize.values();
        return sizes[random.nextInt(sizes.length)];
    }

    private SpaceStationType randomSpaceStationType() {
        SpaceStationType[] types = SpaceStationType.values();
        return types[random.nextInt(types.length)];
    }

    private StationModule.ModuleType randomStationModuleType() {
        StationModule.ModuleType[] types = StationModule.ModuleType.values();
        return types[random.nextInt(types.length)];
    }
}
