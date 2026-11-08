package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Objects;

/**
 * A within-system star system map from Classic Traveller Book 2.
 * <p>
 * While {@link TravellerWorld} describes a star system from the sector map
 * perspective (UWP, trade codes, hyperlanes), SystemMap describes the
 * internal structure of a star system: the star, planets, gas giants,
 * asteroid belts, and their distances from the star in AU.
 * </p>
 * <p>
 * This class enables:
 * </p>
 * <ul>
 *   <li>Querying celestial bodies by type or distance</li>
 *   <li>Calculating travel distances between bodies</li>
 *   <li>Generating random star systems</li>
 *   <li>Identifying habitable zones and landing targets</li>
 * </ul>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   SystemMap map = new SystemMap("Tukkayj System", tukkayjWorld);
 *   map.addPlanet("Tukkayj", 1.0, CelestialBodyType.HABITABLE);
 *   map.addGasGiant("Bormaul", 5.2, "Ammonia");
 *   map.addAsteroidBelt("Kowro", 3.0);
 *
 *   double distance = map.getDistanceBetween("Tukkayj", "Bormaul"); // 4.2 AU
 * </pre>
 */
public class SystemMap {

    /**
     * Types of celestial bodies.
     */
    public enum CelestialBodyType {
        /** The primary star. */
        STAR,

        /** Habitable world with breathable atmosphere. */
        HABITABLE,

        /** Rocky world, not necessarily habitable. */
        ROCKY,

        /** Gas giant planet. */
        GAS_GIANT,

        /** Ice giant planet. */
        ICE_GIANT,

        /** Asteroid belt. */
        ASTEROID_BELT,

        /** Comet cloud. */
        COMET_CLOUD,

        /** Moon or satellite. */
        MOON,

        /** Artificial structure (station, beacon). */
        ARTIFICIAL
    }

    /**
     * A celestial body within a star system.
     */
    public static class CelestialBody {
        private final String name;
        private final CelestialBodyType type;
        private final double distanceAU;
        private final String parent; // null for planets, planet name for moons
        private final String description;
        private final boolean hasAtmosphere;
        private final boolean canLand;

        public CelestialBody(String name, CelestialBodyType type, double distanceAU,
                              String parent, String description,
                              boolean hasAtmosphere, boolean canLand) {
            this.name = Objects.requireNonNull(name);
            this.type = Objects.requireNonNull(type);
            this.distanceAU = distanceAU;
            this.parent = parent;
            this.description = description != null ? description : "";
            this.hasAtmosphere = hasAtmosphere;
            this.canLand = canLand;
        }

        public String getName() { return name; }
        public CelestialBodyType getType() { return type; }
        public double getDistanceAU() { return distanceAU; }
        public String getParent() { return parent; }
        public String getDescription() { return description; }
        public boolean hasAtmosphere() { return hasAtmosphere; }
        public boolean canLand() { return canLand; }

        @Override
        public String toString() {
            return name + " (" + type + ", " + String.format("%.1f", distanceAU) + " AU)";
        }
    }

    /**
     * The name of the star system.
     */
    private final String systemName;

    /**
     * The Traveller world for this system (for UWP reference).
     */
    private final TravellerWorld travellerWorld;

    /**
     * The primary star.
     */
    private final CelestialBody star;

    /**
     * All celestial bodies in the system.
     */
    private final List<CelestialBody> bodies;

    /**
     * Creates a new system map.
     *
     * @param systemName the name of the star system
     * @param travellerWorld the Traveller world for this system
     */
    public SystemMap(String systemName, TravellerWorld travellerWorld) {
        this.systemName = Objects.requireNonNull(systemName);
        this.travellerWorld = travellerWorld;
        this.star = new CelestialBody(systemName + " Star", CelestialBodyType.STAR,
                0.0, null, "Primary star", false, false);
        this.bodies = new ArrayList<>();
        this.bodies.add(star);
    }

    /**
     * Creates a new empty system map (without Traveller world reference).
     *
     * @param systemName the name of the star system
     */
    public SystemMap(String systemName) {
        this(systemName, null);
    }

    // ---- Body Management ----

    /**
     * Adds a planet to the system.
     *
     * @param name the planet name
     * @param distanceAU distance from the star in AU
     * @param type the planet type
     * @return the created celestial body
     */
    public CelestialBody addPlanet(String name, double distanceAU, CelestialBodyType type) {
        return addPlanet(name, distanceAU, type, false, type == CelestialBodyType.HABITABLE);
    }

    /**
     * Adds a planet with detailed properties.
     *
     * @param name the planet name
     * @param distanceAU distance from the star in AU
     * @param type the planet type
     * @param hasAtmosphere true if the planet has an atmosphere
     * @param canLand true if landing is possible
     * @return the created celestial body
     */
    public CelestialBody addPlanet(String name, double distanceAU, CelestialBodyType type,
                                    boolean hasAtmosphere, boolean canLand) {
        return addPlanet(name, distanceAU, type, "", hasAtmosphere, canLand);
    }

    /**
     * Adds a planet with full details.
     *
     * @param name the planet name
     * @param distanceAU distance from the star in AU
     * @param type the planet type
     * @param description description of the planet
     * @param hasAtmosphere true if the planet has an atmosphere
     * @param canLand true if landing is possible
     * @return the created celestial body
     */
    public CelestialBody addPlanet(String name, double distanceAU, CelestialBodyType type,
                                    String description, boolean hasAtmosphere, boolean canLand) {
        CelestialBody body = new CelestialBody(name, type, distanceAU, null,
                description, hasAtmosphere, canLand);
        bodies.add(body);
        return body;
    }

    /**
     * Adds a gas giant to the system.
     *
     * @param name the gas giant name
     * @param distanceAU distance from the star in AU
     * @param atmosphereType atmosphere description (e.g., "Hydrogen/Helium")
     * @return the created celestial body
     */
    public CelestialBody addGasGiant(String name, double distanceAU, String atmosphereType) {
        return addPlanet(name, distanceAU, CelestialBodyType.GAS_GIANT,
                atmosphereType, true, false);
    }

    /**
     * Adds an asteroid belt to the system.
     *
     * @param name the asteroid belt name
     * @param distanceAU distance from the star in AU
     * @return the created celestial body
     */
    public CelestialBody addAsteroidBelt(String name, double distanceAU) {
        return addPlanet(name, distanceAU, CelestialBodyType.ASTEROID_BELT,
                "Asteroid belt", false, false);
    }

    /**
     * Adds a moon orbiting a planet.
     *
     * @param name the moon name
     * @param parentPlanet the parent planet name
     * @param description description of the moon
     * @param canLand true if landing is possible
     * @return the created celestial body
     */
    public CelestialBody addMoon(String name, String parentPlanet,
                                  String description, boolean canLand) {
        CelestialBody parent = findBody(parentPlanet);
        double distance = parent != null ? parent.getDistanceAU() : 0;
        CelestialBody moon = new CelestialBody(name, CelestialBodyType.MOON,
                distance, parentPlanet, description, false, canLand);
        bodies.add(moon);
        return moon;
    }

    /**
     * Adds an artificial structure (station, beacon).
     *
     * @param name the structure name
     * @param distanceAU distance from the star in AU
     * @param description description of the structure
     * @return the created celestial body
     */
    public CelestialBody addArtificialStructure(String name, double distanceAU, String description) {
        return addPlanet(name, distanceAU, CelestialBodyType.ARTIFICIAL,
                description, false, true);
    }

    // ---- Queries ----

    /**
     * Finds a celestial body by name.
     *
     * @param name the body name
     * @return the body, or null if not found
     */
    public CelestialBody findBody(String name) {
        for (CelestialBody body : bodies) {
            if (body.getName().equalsIgnoreCase(name)) {
                return body;
            }
        }
        return null;
    }

    /**
     * Returns all bodies of a given type.
     *
     * @param type the body type
     * @return list of bodies (immutable)
     */
    public List<CelestialBody> getBodiesByType(CelestialBodyType type) {
        List<CelestialBody> result = new ArrayList<>();
        for (CelestialBody body : bodies) {
            if (body.getType() == type) {
                result.add(body);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns all habitable worlds.
     *
     * @return list of habitable bodies (immutable)
     */
    public List<CelestialBody> getHabitableWorlds() {
        return getBodiesByType(CelestialBodyType.HABITABLE);
    }

    /**
     * Returns all bodies where landing is possible.
     *
     * @return list of landable bodies (immutable)
     */
    public List<CelestialBody> getLandableBodies() {
        List<CelestialBody> result = new ArrayList<>();
        for (CelestialBody body : bodies) {
            if (body.canLand()) {
                result.add(body);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns all bodies within a given distance range from the star.
     *
     * @param minAU minimum distance in AU
     * @param maxAU maximum distance in AU
     * @return list of bodies (immutable)
     */
    public List<CelestialBody> getBodiesInRange(double minAU, double maxAU) {
        List<CelestialBody> result = new ArrayList<>();
        for (CelestialBody body : bodies) {
            if (body.getDistanceAU() >= minAU && body.getDistanceAU() <= maxAU) {
                result.add(body);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the habitable zone range for this system.
     * <p>
     * Simplified: 0.7-1.5 AU for a sun-like star.
     * </p>
     *
     * @return array of {min, max} AU
     */
    public double[] getHabitableZone() {
        return new double[]{0.7, 1.5};
    }

    /**
     * Returns all bodies in the habitable zone.
     *
     * @return list of bodies (immutable)
     */
    public List<CelestialBody> getBodiesInHabitableZone() {
        double[] zone = getHabitableZone();
        return getBodiesInRange(zone[0], zone[1]);
    }

    // ---- Distance Calculations ----

    /**
     * Calculates the distance between two celestial bodies.
     * <p>
     * Distance = |body1.distance - body2.distance| (simplified 1D model).
     * </p>
     *
     * @param body1Name the first body name
     * @param body2Name the second body name
     * @return distance in AU, or -1 if either body is not found
     */
    public double getDistanceBetween(String body1Name, String body2Name) {
        CelestialBody body1 = findBody(body1Name);
        CelestialBody body2 = findBody(body2Name);
        if (body1 == null || body2 == null) {
            return -1;
        }
        return Math.abs(body1.getDistanceAU() - body2.getDistanceAU());
    }

    /**
     * Calculates the distance from a body to the star.
     *
     * @param bodyName the body name
     * @return distance in AU, or -1 if not found
     */
    public double getDistanceToStar(String bodyName) {
        CelestialBody body = findBody(bodyName);
        return body != null ? body.getDistanceAU() : -1;
    }

    // ---- Accessors ----

    /**
     * @return the system name
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * @return the Traveller world, or null if not set
     */
    public TravellerWorld getTravellerWorld() {
        return travellerWorld;
    }

    /**
     * @return the primary star
     */
    public CelestialBody getStar() {
        return star;
    }

    /**
     * @return all celestial bodies (immutable)
     */
    public List<CelestialBody> getAllBodies() {
        return Collections.unmodifiableList(bodies);
    }

    /**
     * @return the number of celestial bodies (including the star)
     */
    public int getBodyCount() {
        return bodies.size();
    }

    // ---- System Generation ----

    /**
     * Generates a random star system based on the Traveller world's UWP.
     * <p>
     * Uses the UWP planet size and atmosphere to determine the types
     * of bodies in the system.
     * </p>
     *
     * @param random the random number generator
     * @return this system map (for chaining)
     */
    public SystemMap generateFromUWP(Random random) {
        if (travellerWorld == null) {
            return this;
        }

        UniversalWorldProfile uwp = travellerWorld.getUwp();
        int planetSize = uwp.getPlanetSize();
        int atmosphere = uwp.getAtmosphere();

        // Add the primary habitable world
        CelestialBodyType primaryType;
        boolean hasAtmo = atmosphere >= 1 && atmosphere <= 7;
        boolean canLand = planetSize <= 6;

        if (planetSize >= 7) {
            primaryType = CelestialBodyType.GAS_GIANT;
        } else if (uwp.isHabitable()) {
            primaryType = CelestialBodyType.HABITABLE;
        } else {
            primaryType = CelestialBodyType.ROCKY;
        }

        double primaryDistance = 0.8 + random.nextDouble() * 0.8; // 0.8-1.6 AU
        addPlanet(travellerWorld.getName(), primaryDistance, primaryType,
                uwp.getPlanetSizeDescription() + " world",
                hasAtmo, canLand);

        // Add additional bodies based on modifier
        String modifier = uwp.getModifier();
        if (modifier != null && !modifier.isEmpty()) {
            // Modifier indicates additional bodies
            int additionalBodies = modifier.length();
            for (int i = 0; i < additionalBodies; i++) {
                double distance = 2.0 + random.nextDouble() * 10.0;
                if (random.nextDouble() < 0.3) {
                    addGasGiant("Planet " + (i + 2), distance, "Hydrogen/Helium");
                } else {
                    addPlanet("Planet " + (i + 2), distance, CelestialBodyType.ROCKY,
                            false, true);
                }
            }
        } else {
            // Add 1-3 random additional bodies
            int numBodies = 1 + random.nextInt(3);
            for (int i = 0; i < numBodies; i++) {
                double distance = 2.0 + random.nextDouble() * 10.0;
                if (random.nextDouble() < 0.4) {
                    addGasGiant("Planet " + (i + 2), distance, "Hydrogen/Helium");
                } else if (random.nextDouble() < 0.2) {
                    addAsteroidBelt("Belt " + (i + 1), distance);
                } else {
                    addPlanet("Planet " + (i + 2), distance, CelestialBodyType.ROCKY,
                            false, true);
                }
            }
        }

        return this;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(systemName).append(" System (").append(bodies.size()).append(" bodies):\n");
        for (CelestialBody body : bodies) {
            sb.append("  ").append(body).append("\n");
        }
        return sb.toString();
    }
}
