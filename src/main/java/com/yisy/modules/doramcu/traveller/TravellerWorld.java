package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * A Traveller star system from Classic Traveller Supplement 3: The Spinward Marches.
 * <p>
 * Each star system contains a primary habitable world described by a
 * {@link UniversalWorldProfile} (UWP), along with sector map coordinates,
 * trade codes, and hyperlane connections to neighboring systems.
 * </p>
 * <p>
 * In Classic Traveller, a sector map is a grid where each cell represents
 * a star system. The x,y coordinates identify the system's position on the
 * sector map, and the distance between systems determines jump travel time.
 * </p>
 * <p>
 * Example from the Spinward Marches:
 * </p>
 * <pre>
 *   Tukkayj:  UWP A544430-D, Trade Ag, Coordinates (05,07)
 *   Mesna:    UWP 45565432, Trade Ni, Coordinates (03,05)
 * </pre>
 */
public class TravellerWorld {

    /**
     * Unique identifier for this world within the sector.
     */
    private final int id;

    /**
     * The name of the star system.
     */
    private final String name;

    /**
     * The Universal World Profile describing the primary world.
     */
    private final UniversalWorldProfile uwp;

    /**
     * X coordinate on the sector map (0-based).
     */
    private final int sectorX;

    /**
     * Y coordinate on the sector map (0-based).
     */
    private final int sectorY;

    /**
     * Trade codes derived from or assigned to this world.
     */
    private final Set<TradeCode> tradeCodes;

    /**
     * Port wealth — determines the economic value of the world's starport.
     * Derived from population and government, or set explicitly.
     */
    private final int portWealth;

    /**
     * Population in millions. Derived from UWP population code.
     */
    private final long populationMillions;

    /**
     * Government type name. Derived from UWP or set explicitly.
     */
    private final String government;

    /**
     * Law level name. Derived from UWP or set explicitly.
     */
    private final String lawLevel;

    /**
     * True if this world is aligned with the Imperium.
     */
    private boolean imperiumAligned;

    /**
     * True if this world is a capital world.
     */
    private boolean isCapital;

    /**
     * List of hyperlane connections to other worlds.
     */
    private final List<Hyperlane> hyperlanes;

    /**
     * Creates a new Traveller world with the given name, UWP, and explicit flags.
     *
     * @param id the unique world identifier
     * @param name the name of the star system
     * @param uwp the Universal World Profile
     * @param sectorX the X coordinate on the sector map
     * @param sectorY the Y coordinate on the sector map
     * @param imperiumAligned true if aligned with the Imperium
     * @param isCapital true if this is a capital world
     */
    public TravellerWorld(int id, String name, UniversalWorldProfile uwp, int sectorX, int sectorY,
                           boolean imperiumAligned, boolean isCapital) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "World name must not be null");
        this.uwp = Objects.requireNonNull(uwp, "UWP must not be null");
        this.sectorX = sectorX;
        this.sectorY = sectorY;

        // Derive trade codes from UWP
        this.tradeCodes = new TreeSet<>();
        TradeCode primary = uwp.derivePrimaryTradeCode();
        if (primary != null) {
            this.tradeCodes.add(primary);
        }

        // Derive port wealth from population and government
        this.portWealth = calculatePortWealth(uwp);

        // Derive population in millions
        this.populationMillions = (long) (uwp.getPopulationBillions() * 1000);

        // Derive government and law level
        this.government = uwp.getGovernmentType();
        this.lawLevel = uwp.getLawLevelDescription();

        this.imperiumAligned = imperiumAligned;
        this.isCapital = isCapital;
        this.hyperlanes = new ArrayList<>();
    }

    // ---- Deprecated constructors ----

    /**
     * Creates a new Traveller world with the given name and UWP string.
     *
     * @param id the unique world identifier
     * @param name the name of the star system
     * @param uwp the Universal World Profile string (e.g., "A544430-D")
     * @param sectorX the X coordinate on the sector map
     * @param sectorY the Y coordinate on the sector map
     */
    public TravellerWorld(int id, String name, String uwp, int sectorX, int sectorY) {
        this(id, name, UniversalWorldProfile.parse(uwp), sectorX, sectorY, false, false);
    }

    /**
     * Creates a new Traveller world with the given name and UWP object.
     *
     * @param id the unique world identifier
     * @param name the name of the star system
     * @param uwp the Universal World Profile
     * @param sectorX the X coordinate on the sector map
     * @param sectorY the Y coordinate on the sector map
     */
    public TravellerWorld(int id, String name, UniversalWorldProfile uwp, int sectorX, int sectorY) {
        this(id, name, uwp, sectorX, sectorY, false, false);
    }

    /**
     * Calculates the port wealth from UWP population and government.
     * <p>
     * Port wealth = (Population + Government) / 2, rounded down.
     * This is a simplified version of the Classic Traveller formula.
     * </p>
     */
    private int calculatePortWealth(UniversalWorldProfile uwp) {
        return (uwp.getPopulation() + uwp.getGovernment()) / 2;
    }

    // ---- Accessors ----

    /**
     * @return the unique world identifier
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name of the star system
     */
    public String getName() {
        return name;
    }

    /**
     * @return the Universal World Profile
     */
    public UniversalWorldProfile getUwp() {
        return uwp;
    }

    /**
     * @return the X coordinate on the sector map
     */
    public int getSectorX() {
        return sectorX;
    }

    /**
     * @return the Y coordinate on the sector map
     */
    public int getSectorY() {
        return sectorY;
    }

    /**
     * @return the set of trade codes (immutable)
     */
    public Set<TradeCode> getTradeCodes() {
        return Collections.unmodifiableSet(tradeCodes);
    }

    /**
     * @return the port wealth level
     */
    public int getPortWealth() {
        return portWealth;
    }

    /**
     * @return the population in millions
     */
    public long getPopulationMillions() {
        return populationMillions;
    }

    /**
     * @return the government type description
     */
    public String getGovernment() {
        return government;
    }

    /**
     * @return the law level description
     */
    public String getLawLevel() {
        return lawLevel;
    }

    /**
     * @return true if this world is aligned with the Imperium
     */
    public boolean isImperiumAligned() {
        return imperiumAligned;
    }

    /**
     * @return true if this world is a capital world
     */
    public boolean isCapital() {
        return isCapital;
    }

    /**
     * @return the list of hyperlane connections (immutable)
     */
    public List<Hyperlane> getHyperlanes() {
        return Collections.unmodifiableList(hyperlanes);
    }

    /**
     * @return the number of hyperlane connections
     */
    public int getHyperlaneCount() {
        return hyperlanes.size();
    }

    // ---- Derived Properties ----

    /**
     * @return true if the primary world is habitable by humans
     */
    public boolean isHabitable() {
        return uwp.isHabitable();
    }

    /**
     * @return true if the world has a functional starport
     */
    public boolean hasStarport() {
        return uwp.hasStarport();
    }

    /**
     * @return true if the world has no atmosphere
     */
    public boolean isVacuum() {
        return uwp.isVacuum();
    }

    /**
     * Returns the primary trade code, or null if none.
     *
     * @return the primary trade code
     */
    public TradeCode getPrimaryTradeCode() {
        return tradeCodes.isEmpty() ? null : tradeCodes.iterator().next();
    }

    /**
     * Checks if this world has the given trade code.
     *
     * @param code the trade code to check
     * @return true if the world has this trade code
     */
    public boolean hasTradeCode(TradeCode code) {
        return tradeCodes.contains(code);
    }

    /**
     * Calculates the jump distance to another world.
     * <p>
     * Distance is calculated using the Euclidean distance between
     * sector map coordinates, rounded up to the nearest integer.
     * </p>
     *
     * @param other the other world
     * @return the jump distance in parsecs
     */
    public int getJumpDistanceTo(TravellerWorld other) {
        int dx = Math.abs(this.sectorX - other.sectorX);
        int dy = Math.abs(this.sectorY - other.sectorY);
        return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
    }

    /**
     * Checks if this world is within jump range of another world.
     *
     * @param other the other world
     * @param maxJump the maximum jump range (e.g., 3 for J-3 drive)
     * @return true if the other world is within jump range
     */
    public boolean isWithinJumpRange(TravellerWorld other, int maxJump) {
        return getJumpDistanceTo(other) <= maxJump;
    }

    // ---- Mutators ----

    /**
     * Adds a trade code to this world.
     *
     * @param code the trade code to add
     * @return true if the set changed
     */
    public boolean addTradeCode(TradeCode code) {
        return tradeCodes.add(Objects.requireNonNull(code));
    }

    /**
     * Adds a hyperlane connection to another world.
     *
     * @param hyperlane the hyperlane to add
     */
    public void addHyperlane(Hyperlane hyperlane) {
        Objects.requireNonNull(hyperlane, "Hyperlane must not be null");
        this.hyperlanes.add(hyperlane);
    }

    /**
     * Sets whether this world is aligned with the Imperium.
     *
     * @param aligned true if aligned with the Imperium
     */
    public void setImperiumAligned(boolean aligned) {
        this.imperiumAligned = aligned;
    }

    /**
     * Sets whether this world is a capital world.
     *
     * @param capital true if this is a capital world
     */
    public void setCapital(boolean capital) {
        this.isCapital = capital;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" [").append(uwp.getUwpString()).append("]");
        if (!tradeCodes.isEmpty()) {
            sb.append(" Trade:").append(tradeCodes);
        }
        sb.append(" (").append(sectorX).append(",").append(sectorY).append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravellerWorld that = (TravellerWorld) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
