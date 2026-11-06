package com.yisy.modules.doramcu.hex;

/**
 * A hyperlane connection between two solar systems enabling FTL travel.
 * <p>
 * Based on design patterns from Stellaris (hyperlanes) and
 * Galactic Civilizations IV (trade routes).
 */
public class Hyperlane {
    private final int fromSystemId;
    private final int toSystemId;
    private final int distance;

    /**
     * Creates a new hyperlane connection.
     *
     * @param fromSystemId the source solar system ID
     * @param toSystemId the destination solar system ID
     * @param distance the travel distance between systems
     */
    public Hyperlane(int fromSystemId, int toSystemId, int distance) {
        this.fromSystemId = fromSystemId;
        this.toSystemId = toSystemId;
        this.distance = distance;
    }

    /**
     * @return the source solar system ID
     */
    public int getFromSystemId() {
        return fromSystemId;
    }

    /**
     * @return the destination solar system ID
     */
    public int getToSystemId() {
        return toSystemId;
    }

    /**
     * @return the travel distance
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Checks if this hyperlane connects to the given system.
     *
     * @param systemId the system ID to check
     * @return true if connected
     */
    public boolean connectsTo(int systemId) {
        return fromSystemId == systemId || toSystemId == systemId;
    }

    @Override
    public String toString() {
        return String.format("Hyperlane{from=%d, to=%d, distance=%d}", fromSystemId, toSystemId, distance);
    }
}
