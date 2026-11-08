package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manages the 3D hex geometry of a planet.
 * <p>
 * A planet is represented as a solid sphere of 3D hex cells centered at the origin (0, 0, 0),
 * with distinct geological layers (core, mantle, crust, atmosphere) and an optional orbiting moon.
 * <p>
 * This class handles all hex map generation, layer queries, and coordinate operations.
 */
public class PlanetGeometry {

    public static final int DEFAULT_RADIUS = 10;
    public static final int DEFAULT_ATMOSPHERE_THICKNESS = 2;
    public static final int DEFAULT_MOON_DISTANCE = 20;
    public static final int DEFAULT_MOON_RADIUS = 3;

    private final int radius;
    private final int atmosphereThickness;
    private final int moonDistance;
    private final int moonRadius;
    private final Hex3DMap map;
    private boolean geometryGenerated = false;  // Lazy generation flag

    /**
     * Creates planet geometry with default radius of 10.
     */
    public PlanetGeometry() {
        this(DEFAULT_RADIUS, DEFAULT_ATMOSPHERE_THICKNESS, DEFAULT_MOON_DISTANCE, DEFAULT_MOON_RADIUS);
    }

    /**
     * Creates planet geometry with custom radius.
     *
     * @param radius the radius of the planet (0 for a single center hex)
     */
    public PlanetGeometry(int radius) {
        this(radius, DEFAULT_ATMOSPHERE_THICKNESS, DEFAULT_MOON_DISTANCE, DEFAULT_MOON_RADIUS);
    }

    /**
     * Creates planet geometry with custom radius and atmosphere thickness.
     */
    public PlanetGeometry(int radius, int atmosphereThickness) {
        this(radius, atmosphereThickness, DEFAULT_MOON_DISTANCE, DEFAULT_MOON_RADIUS);
    }

    /**
     * Creates planet geometry with full customization.
     *
     * @param radius the radius of the planet
     * @param atmosphereThickness the thickness of the atmosphere layer
     * @param moonDistance the orbit distance of the moon (0 for no moon)
     * @param moonRadius the radius of the moon (0 for no moon)
     */
    public PlanetGeometry(int radius, int atmosphereThickness, int moonDistance, int moonRadius) {
        if (radius < 0) throw new IllegalArgumentException("Radius must be non-negative: " + radius);
        if (atmosphereThickness < 0) throw new IllegalArgumentException("Atmosphere thickness must be non-negative: " + atmosphereThickness);
        if (moonDistance < 0) throw new IllegalArgumentException("Moon distance must be non-negative: " + moonDistance);
        if (moonRadius < 0) throw new IllegalArgumentException("Moon radius must be non-negative: " + moonRadius);

        this.radius = radius;
        this.atmosphereThickness = atmosphereThickness;
        this.moonDistance = moonDistance;
        this.moonRadius = moonRadius;
        this.map = new Hex3DMap();
        // Geometry is generated lazily on first access
    }

    // ---- Generation ----

    private void generate() {
        if (geometryGenerated) return;
        Hex3D center = new Hex3D(0, 0, 0);
        int totalRadius = radius + atmosphereThickness;
        map.generateRange(center, totalRadius);
        generateMoon();
        geometryGenerated = true;
    }

    /**
     * Forces generation of geometry if not already generated.
     */
    public void ensureGeometryGenerated() {
        if (!geometryGenerated) {
            generate();
        }
    }

    private void generateMoon() {
        if (moonDistance == 0 || moonRadius == 0) {
            return;
        }
        Hex3D moonCenter = new Hex3D(moonDistance, -moonDistance, 0);
        map.generateRange(moonCenter, moonRadius);
    }

    // ---- Accessors ----

    public int getRadius() {
        return radius;
    }

    public int getAtmosphereThickness() {
        return atmosphereThickness;
    }

    public int getTotalRadius() {
        return radius + atmosphereThickness;
    }

    public int getMoonDistance() {
        return moonDistance;
    }

    public int getMoonRadius() {
        return moonRadius;
    }

    public boolean hasMoon() {
        return moonDistance > 0 && moonRadius > 0;
    }

    public int size() {
        ensureGeometryGenerated();
        return map.size();
    }

    /**
     * @return true if geometry has been generated
     */
    public boolean isGeometryGenerated() {
        return geometryGenerated;
    }

    /**
     * @return the underlying 3D hex map (triggers lazy generation if needed)
     */
    public Hex3DMap getMap() {
        ensureGeometryGenerated();
        return map;
    }

    // ---- Hex operations ----

    public Hex3D get(int x, int y) {
        ensureGeometryGenerated();
        return map.get(x, y);
    }

    public Hex3D get(Hex3D hex) {
        ensureGeometryGenerated();
        return map.get(hex);
    }

    public boolean contains(int x, int y) {
        ensureGeometryGenerated();
        return map.contains(x, y);
    }

    public boolean contains(Hex3D hex) {
        ensureGeometryGenerated();
        return map.get(hex) != null;
    }

    public Collection<Hex3D> getAll() {
        ensureGeometryGenerated();
        return map.getAll();
    }

    public List<Hex3D> findByState(HexState state) {
        ensureGeometryGenerated();
        return map.findByState(state);
    }

    public List<Hex3D> getNeighbors(Hex3D hex) {
        ensureGeometryGenerated();
        return map.getNeighbors(hex);
    }

    // ---- Occupancy statistics ----

    public int getOccupiedCount() {
        return findByState(HexState.OCCUPIED).size();
    }

    public int getEmptyCount() {
        return findByState(HexState.EMPTY).size();
    }

    public int getBlockedCount() {
        return findByState(HexState.BLOCKED).size();
    }

    public int getSelectedCount() {
        return findByState(HexState.SELECTED).size();
    }

    public double getOccupancyRatio() {
        if (size() == 0) return 0.0;
        return (double) getOccupiedCount() / size();
    }

    // ---- Geological layers ----

    public int getCoreRadius() {
        return (int) Math.floor(radius * 0.25);
    }

    public int getMantleOuterRadius() {
        return (int) Math.floor(radius * 0.75);
    }

    public List<Hex3D> getCore() {
        int coreRadius = getCoreRadius();
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> center.distance(h) <= coreRadius)
                .toList();
    }

    public List<Hex3D> getMantle() {
        int coreRadius = getCoreRadius();
        int mantleOuter = getMantleOuterRadius();
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> {
                    int dist = center.distance(h);
                    return dist > coreRadius && dist <= mantleOuter;
                })
                .toList();
    }

    public List<Hex3D> getCrust() {
        int mantleOuter = getMantleOuterRadius();
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> {
                    int dist = center.distance(h);
                    return dist > mantleOuter && dist <= radius;
                })
                .toList();
    }

    public List<Hex3D> getAtmosphere() {
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> {
                    int dist = center.distance(h);
                    return dist > radius && dist <= getTotalRadius();
                })
                .toList();
    }

    public Hex3D getMoonCenter() {
        if (!hasMoon()) {
            return null;
        }
        return map.get(moonDistance, -moonDistance);
    }

    public List<Hex3D> getMoon() {
        if (!hasMoon()) {
            return List.of();
        }
        Hex3D moonCenter = getMoonCenter();
        if (moonCenter == null) {
            return List.of();
        }
        return map.getAll().stream()
                .filter(h -> moonCenter.distance(h) <= moonRadius)
                .toList();
    }

    public List<Hex3D> getSurface() {
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> center.distance(h) == radius)
                .toList();
    }

    public String getLayerName(Hex3D hex) {
        if (hex == null) {
            return "UNKNOWN";
        }
        if (hasMoon()) {
            Hex3D moonCenter = getMoonCenter();
            if (moonCenter != null && moonCenter.distance(hex) <= moonRadius) {
                return "MOON";
            }
        }
        Hex3D center = new Hex3D(0, 0, 0);
        int dist = center.distance(hex);
        int coreRadius = getCoreRadius();
        int mantleOuter = getMantleOuterRadius();

        if (dist <= coreRadius) {
            return "CORE";
        } else if (dist <= mantleOuter) {
            return "MANTLE";
        } else if (dist <= radius) {
            return "CRUST";
        } else {
            return "ATMOSPHERE";
        }
    }

    public int[] getLayerSizes() {
        return new int[]{getCore().size(), getMantle().size(), getCrust().size(), getAtmosphere().size(), getMoon().size()};
    }
}
