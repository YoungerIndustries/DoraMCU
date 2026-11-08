package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * A hyperlane connection between two Traveller star systems.
 * <p>
 * In Classic Traveller, hyperlanes represent jump routes between star systems.
 * The distance between systems determines the minimum jump drive rating required
 * for travel. A J-3 drive can reach any system within 3 parsecs.
 * </p>
 * <p>
 * Hyperlanes are bidirectional — if system A connects to system B, then
 * system B also connects to system A with the same distance.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   Tukkayj (05,07) ↔ Mesna (03,05)
 *   Distance: √((5-3)² + (7-5)²) = √8 ≈ 3 parsecs
 *   Minimum jump drive: J-3
 * </pre>
 */
public class Hyperlane {

    /**
     * The source world of this hyperlane.
     */
    private final TravellerWorld from;

    /**
     * The destination world of this hyperlane.
     */
    private final TravellerWorld to;

    /**
     * The distance between the two worlds in parsecs.
     * This determines the minimum jump drive rating required.
     */
    private final int distanceParsecs;

    /**
     * True if this hyperlane is within Imperium space.
     */
    private final boolean imperiumSpace;

    /**
     * True if this hyperlane is considered safe for travel.
     */
    private boolean isSafe;

    /**
     * Creates a new hyperlane between two worlds.
     * <p>
     * The distance is calculated from the sector map coordinates of the two worlds.
     * </p>
     *
     * @param from the source world
     * @param to the destination world
     */
    public Hyperlane(TravellerWorld from, TravellerWorld to) {
        this.from = Objects.requireNonNull(from, "Source world must not be null");
        this.to = Objects.requireNonNull(to, "Destination world must not be null");
        this.distanceParsecs = from.getJumpDistanceTo(to);
        this.imperiumSpace = from.isImperiumAligned() && to.isImperiumAligned();
        this.isSafe = true;
    }

    /**
     * Creates a new hyperlane between two worlds with a specified distance.
     * <p>
     * Use this constructor when the distance differs from the calculated
     * Euclidean distance (e.g., due to nebulae or other obstacles).
     * </p>
     *
     * @param from the source world
     * @param to the destination world
     * @param distanceParsecs the distance in parsecs
     */
    public Hyperlane(TravellerWorld from, TravellerWorld to, int distanceParsecs) {
        this.from = Objects.requireNonNull(from, "Source world must not be null");
        this.to = Objects.requireNonNull(to, "Destination world must not be null");
        if (distanceParsecs < 1) {
            throw new IllegalArgumentException("Distance must be at least 1 parsec");
        }
        this.distanceParsecs = distanceParsecs;
        this.imperiumSpace = from.isImperiumAligned() && to.isImperiumAligned();
        this.isSafe = true;
    }

    // ---- Accessors ----

    /**
     * @return the source world
     */
    public TravellerWorld getFrom() {
        return from;
    }

    /**
     * @return the destination world
     */
    public TravellerWorld getTo() {
        return to;
    }

    /**
     * @return the distance in parsecs
     */
    public int getDistanceParsecs() {
        return distanceParsecs;
    }

    /**
     * @return true if this hyperlane is within Imperium space
     */
    public boolean isImperiumSpace() {
        return imperiumSpace;
    }

    /**
     * @return true if this hyperlane is safe for travel
     */
    public boolean isSafe() {
        return isSafe;
    }

    /**
     * Returns the other world given one endpoint of this hyperlane.
     * <p>
     * If {@code world} equals {@code from}, returns {@code to}.
     * If {@code world} equals {@code to}, returns {@code from}.
     * </p>
     *
     * @param world one endpoint of the hyperlane
     * @return the other endpoint
     * @throws IllegalArgumentException if {@code world} is not an endpoint
     */
    public TravellerWorld getOtherEnd(TravellerWorld world) {
        Objects.requireNonNull(world, "World must not be null");
        if (from.equals(world)) {
            return to;
        } else if (to.equals(world)) {
            return from;
        } else {
            throw new IllegalArgumentException("World is not an endpoint of this hyperlane: " + world.getName());
        }
    }

    /**
     * Checks if the given world is an endpoint of this hyperlane.
     *
     * @param world the world to check
     * @return true if the world is an endpoint
     */
    public boolean connects(TravellerWorld world) {
        return from.equals(world) || to.equals(world);
    }

    /**
     * Checks if a ship with the given jump rating can traverse this hyperlane.
     *
     * @param jumpRating the ship's jump drive rating (e.g., 3 for J-3)
     * @return true if the ship can make the jump
     */
    public boolean isTraversable(int jumpRating) {
        return jumpRating >= distanceParsecs;
    }

    /**
     * Sets whether this hyperlane is safe for travel.
     * <p>
     * Hyperlanes may be unsafe due to piracy, political instability,
     * or hazardous conditions (nebulae, radiation, etc.).
     * </p>
     *
     * @param safe true if the hyperlane is safe
     */
    public void setSafe(boolean safe) {
        this.isSafe = safe;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return from.getName() + " ↔ " + to.getName() + " (" + distanceParsecs + " pc)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hyperlane hyperlane = (Hyperlane) o;
        // Hyperlanes are bidirectional: A→B equals B→A
        boolean forward = from.equals(hyperlane.from) && to.equals(hyperlane.to);
        boolean reverse = from.equals(hyperlane.to) && to.equals(hyperlane.from);
        return (forward || reverse) && distanceParsecs == hyperlane.distanceParsecs;
    }

    @Override
    public int hashCode() {
        // Use min/max to ensure bidirectional equality
        int minId = Math.min(from.getId(), to.getId());
        int maxId = Math.max(from.getId(), to.getId());
        return Objects.hash(minId, maxId, distanceParsecs);
    }
}
