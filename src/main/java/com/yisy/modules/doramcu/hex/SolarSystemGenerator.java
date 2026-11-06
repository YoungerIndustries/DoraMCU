package com.yisy.modules.doramcu.hex;

import java.util.Random;

/**
 * Generates complete solar systems with stars, planets, and other celestial bodies.
 * <p>
 * Extracted from GalaxyGenerator to separate solar system assembly (orbit positioning,
 * body placement) from individual body generation and galaxy orchestration.
 * </p>
 */
public class SolarSystemGenerator {

    private final Random random;
    private final CelestialBodyGenerator bodyGenerator;
    private final GalaxyGenerationConfig config;
    private final GalaxyGenerationProgress progressListener;

    // ID counter for solar system entities
    private int nextSolarSystemEntityId;

    /**
     * Creates a new solar system generator.
     *
     * @param random the random instance
     * @param bodyGenerator the celestial body generator
     * @param config the generation configuration
     * @param progressListener optional progress listener
     */
    public SolarSystemGenerator(Random random, CelestialBodyGenerator bodyGenerator,
                                 GalaxyGenerationConfig config, GalaxyGenerationProgress progressListener) {
        this.random = random;
        this.bodyGenerator = bodyGenerator;
        this.config = config;
        this.progressListener = progressListener;
        this.nextSolarSystemEntityId = 1;
    }

    /**
     * Generates a complete solar system with progress reporting.
     *
     * @param systemIndex the 1-based index of this system (0 disables progress reporting)
     * @return the generated solar system
     */
    public SolarSystem generateSolarSystem(int systemIndex) {
        int entityId = nextSolarSystemEntityId++;
        SolarSystem solarSystem = new SolarSystem(entityId);

        // Generate central star at origin
        Star star = bodyGenerator.generateStar();
        SolarSystemEntity starEntity = new SolarSystemEntity(nextSolarSystemEntityId++);
        starEntity.addStar(star, new Hex3D(0, 0, 0));
        solarSystem.addSystem(starEntity, new Hex3D(0, 0, 0));

        if (systemIndex > 0 && progressListener != null) {
            progressListener.onSystemStep(systemIndex, "  ★ " + star.getName() + " (" + star.getFullClassification() + ")");
        }

        // Generate planets orbiting the star
        int planetCount = config.getPlanetsPerSystem() + random.nextInt(3);
        for (int i = 0; i < planetCount; i++) {
            Planet planet = bodyGenerator.generatePlanet();
            Hex3D planetPos = calculateOrbitPosition(planetCount, i, 10, 8, 5, 3);

            SolarSystemEntity planetEntity = new SolarSystemEntity(nextSolarSystemEntityId++);
            planetEntity.addPlanet(planet, planetPos);
            solarSystem.addSystem(planetEntity, planetPos);

            if (systemIndex > 0 && progressListener != null) {
                progressListener.onSystemStep(systemIndex, "  ● " + planet.getName() + " (" + planet.getPlanetSize().name() + ")");
            }

            // Generate moons for this planet
            if (random.nextDouble() < config.getMoonChance()) {
                int moonCount = 1 + random.nextInt(3);
                for (int j = 0; j < moonCount; j++) {
                    Moon moon = bodyGenerator.generateMoon(planet);
                    Hex3D moonPos = calculateMoonPosition(planetPos, moonCount, j);

                    SolarSystemEntity moonEntity = new SolarSystemEntity(nextSolarSystemEntityId++);
                    moonEntity.addMoon(moon, moonPos);
                    solarSystem.addSystem(moonEntity, moonPos);
                }
                if (systemIndex > 0 && progressListener != null) {
                    progressListener.onSystemStep(systemIndex, "    ◐ Moons added to " + planet.getName());
                }
            }
        }

        if (systemIndex > 0 && progressListener != null) {
            progressListener.onSystemStep(systemIndex, "  → " + planetCount + " planets");
        }

        // Generate asteroid belt
        if (random.nextDouble() < config.getAsteroidBeltChance()) {
            AsteroidBelt belt = bodyGenerator.generateAsteroidBelt();
            Hex3D beltPos = calculateOrbitalPosition(25, 15, 2);
            SolarSystemEntity beltEntity = new SolarSystemEntity(nextSolarSystemEntityId++);
            beltEntity.addAsteroidBelt(belt, beltPos);
            solarSystem.addSystem(beltEntity, beltPos);

            if (systemIndex > 0 && progressListener != null) {
                progressListener.onSystemStep(systemIndex, "  ☄ Asteroid belt");
            }
        }

        // Generate gas giant
        if (random.nextDouble() < config.getGasGiantChance()) {
            GasGiant gasGiant = bodyGenerator.generateGasGiant();
            Hex3D ggPos = calculateOrbitalPosition(35, 20, 2);
            SolarSystemEntity ggEntity = new SolarSystemEntity(nextSolarSystemEntityId++);
            ggEntity.addGasGiant(gasGiant, ggPos);
            solarSystem.addSystem(ggEntity, ggPos);

            if (systemIndex > 0 && progressListener != null) {
                progressListener.onSystemStep(systemIndex, "  ◉ Gas giant: " + gasGiant.getName());
            }
        }

        // Generate nebula
        if (random.nextDouble() < config.getNebulaChance()) {
            Nebula nebula = bodyGenerator.generateNebula();
            Hex3D nPos = calculateOrbitalPosition(40, 30, 5);
            SolarSystemEntity nEntity = new SolarSystemEntity(nextSolarSystemEntityId++);
            nEntity.addNebula(nebula, nPos);
            solarSystem.addSystem(nEntity, nPos);

            if (systemIndex > 0 && progressListener != null) {
                progressListener.onSystemStep(systemIndex, "  ✦ Nebula (Elerium)");
            }
        }

        // Generate black hole
        if (random.nextDouble() < config.getBlackHoleChance()) {
            BlackHole blackHole = bodyGenerator.generateBlackHole();
            Hex3D bhPos = calculateOrbitalPosition(50, 30, 5);
            SolarSystemEntity bhEntity = new SolarSystemEntity(nextSolarSystemEntityId++);
            bhEntity.addBlackHole(blackHole, bhPos);
            solarSystem.addSystem(bhEntity, bhPos);

            if (systemIndex > 0 && progressListener != null) {
                progressListener.onSystemStep(systemIndex, "  ⚫ Black hole (Anti-Matter)");
            }
        }

        // Generate comet
        if (random.nextDouble() < config.getCometChance()) {
            Comet comet = bodyGenerator.generateComet();
            Hex3D cPos = calculateOrbitalPosition(55, 25, 8);
            SolarSystemEntity cEntity = new SolarSystemEntity(nextSolarSystemEntityId++);
            cEntity.addComet(comet, cPos);
            solarSystem.addSystem(cEntity, cPos);

            if (systemIndex > 0 && progressListener != null) {
                progressListener.onSystemStep(systemIndex, "  ☄ Comet");
            }
        }

        // Generate space station
        if (random.nextDouble() < config.getSpaceStationChance()) {
            SpaceStation station = bodyGenerator.generateSpaceStation();
            Hex3D sPos = calculateOrbitalPosition(5, 10, 2);
            SolarSystemEntity sEntity = new SolarSystemEntity(nextSolarSystemEntityId++);
            sEntity.addSpaceStation(station, sPos);
            solarSystem.addSystem(sEntity, sPos);

            if (systemIndex > 0 && progressListener != null) {
                progressListener.onSystemStep(systemIndex, "  ⚙ Space station");
            }
        }

        return solarSystem;
    }

    /**
     * Calculates an orbit position for a planet.
     */
    private Hex3D calculateOrbitPosition(int count, int index, double baseRadius, double spacing, double angleSpread, double zSpread) {
        double orbitRadius = baseRadius + (index * spacing) + (random.nextDouble() * 5);
        double angle = (2 * Math.PI * index) / count + (random.nextDouble() - 0.5) * angleSpread;

        int px = (int) Math.round(orbitRadius * Math.cos(angle));
        int py = (int) Math.round(orbitRadius * Math.sin(angle));
        int pz = (int) Math.round((random.nextDouble() - 0.5) * zSpread);

        return adjustToValidCubeCoordinates(px, py, pz);
    }

    /**
     * Calculates a moon's position relative to its parent planet.
     */
    private Hex3D calculateMoonPosition(Hex3D parentPos, int count, int index) {
        double moonOrbit = 3 + (index * 2) + random.nextDouble();
        double moonAngle = (2 * Math.PI * index) / count;

        int mx = parentPos.getX() + (int) Math.round(moonOrbit * Math.cos(moonAngle));
        int my = parentPos.getY() + (int) Math.round(moonOrbit * Math.sin(moonAngle));
        int mz = parentPos.getZ() + (int) Math.round((random.nextDouble() - 0.5) * 2);

        return adjustToValidCubeCoordinates(mx, my, mz);
    }

    /**
     * Calculates a random orbital position for a celestial body.
     */
    private Hex3D calculateOrbitalPosition(double baseRadius, double radiusSpread, double zSpread) {
        double radius = baseRadius + random.nextDouble() * radiusSpread;
        double angle = random.nextDouble() * 2 * Math.PI;

        int x = (int) Math.round(radius * Math.cos(angle));
        int y = (int) Math.round(radius * Math.sin(angle));
        int z = (int) Math.round((random.nextDouble() - 0.5) * zSpread);

        return adjustToValidCubeCoordinates(x, y, z);
    }

    /**
     * Adjusts coordinates to be valid cube coordinates (x + y + z = 0).
     */
    private Hex3D adjustToValidCubeCoordinates(int x, int y, int z) {
        int sum = x + y + z;
        if (sum > 0) x -= sum;
        else if (sum < 0) y -= sum;
        return new Hex3D(x, y, z);
    }

}
