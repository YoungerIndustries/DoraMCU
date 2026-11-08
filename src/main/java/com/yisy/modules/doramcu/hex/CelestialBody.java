package com.yisy.modules.doramcu.hex;

/**
 * Abstract base class for all celestial bodies in the game.
 * <p>
 * Provides common fields and methods shared by {@link Star}, {@link Planet},
 * {@link GasGiant}, {@link Asteroid}, {@link Comet}, {@link Moon},
 * {@link AsteroidBelt}, {@link BlackHole}, and {@link Nebula}.
 * <p>
 * All celestial bodies have:
 * <ul>
 *   <li>A unique identifier</li>
 *   <li>A radius defining their 3D hex sphere</li>
 *   <li>A {@link Hex3DMap} for internal structure</li>
 *   <li>A display name</li>
 *   <li>Optional strategic resource deposits</li>
 * </ul>
 * <p>
 * Subclasses add type-specific properties (e.g., energy yield for stars,
 * mineral yield for asteroids, tidal strength for moons).
 */
public abstract class CelestialBody {

    protected final int id;
    protected final int radius;
    protected final Hex3DMap map;
    protected String name;
    protected boolean hasStrategicDeposit;
    protected StrategicResource strategicResource;
    protected boolean hexGeometryGenerated = false;  // Lazy generation flag

    /**
     * Creates a new celestial body with the given ID and radius.
     *
     * @param id the unique identifier
     * @param radius the radius of the 3D hex sphere
     * @throws IllegalArgumentException if radius is negative
     */
    protected CelestialBody(int id, int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius must be non-negative: " + radius);
        }
        this.id = id;
        this.radius = radius;
        this.map = new Hex3DMap();
        this.name = defaultName(id);
        this.hasStrategicDeposit = false;
        this.strategicResource = null;
    }

    /**
     * Returns a default name for this celestial body type.
     * Subclasses override to provide type-specific naming.
     *
     * @param id the unique identifier
     * @return a default name string
     */
    protected abstract String defaultName(int id);

    /**
     * Generates the 3D hex sphere for this celestial body.
     * Called lazily — hex geometry is only generated when actually needed.
     * For strategic views (galaxy map, sector cards), this is skipped entirely.
     */
    protected void generate() {
        if (hexGeometryGenerated) return;
        Hex3D center = new Hex3D(0, 0, 0);
        map.generateRange(center, radius);
        hexGeometryGenerated = true;
    }

    /**
     * Forces generation of hex geometry if not already generated.
     * Use this when tactical operations require the full hex map.
     */
    public void ensureGeometryGenerated() {
        if (!hexGeometryGenerated) {
            generate();
        }
    }

    // ---- Accessors ----

    /**
     * @return the unique identifier for this celestial body
     */
    public int getId() {
        return id;
    }

    /**
     * @return the display name of this celestial body
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this celestial body.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the radius of the 3D hex sphere
     */
    public int getRadius() {
        return radius;
    }

    /**
     * @return the total number of hex cells in this celestial body
     */
    public int size() {
        ensureGeometryGenerated();
        return map.size();
    }

    /**
     * @return true if hex geometry has been generated for this body
     */
    public boolean isHexGeometryGenerated() {
        return hexGeometryGenerated;
    }

    /**
     * @return the internal 3D hex map (triggers lazy generation if needed)
     */
    public Hex3DMap getMap() {
        ensureGeometryGenerated();
        return map;
    }

    // ---- Strategic Resources ----

    /**
     * @return true if this celestial body has a strategic resource deposit
     */
    public boolean hasStrategicDeposit() {
        return hasStrategicDeposit;
    }

    /**
     * Sets whether this celestial body has a strategic resource deposit.
     *
     * @param has true if it has a deposit
     */
    public void setHasStrategicDeposit(boolean has) {
        this.hasStrategicDeposit = has;
    }

    /**
     * @return the strategic resource type, or null if none
     */
    public StrategicResource getStrategicResource() {
        return strategicResource;
    }

    /**
     * Sets the strategic resource type for this celestial body.
     *
     * @param resource the strategic resource type
     */
    public void setStrategicResource(StrategicResource resource) {
        this.strategicResource = resource;
        this.hasStrategicDeposit = resource != null;
    }

    /**
     * @return a string representation of this celestial body
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", radius=" + radius +
                ", size=" + size() +
                ", strategic=" + (hasStrategicDeposit ? strategicResource : "none") +
                '}';
    }
}
