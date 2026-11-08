package com.yisy.modules.doramcu.traveller;

/**
 * Hull construction types from Classic Traveller Book 2.
 * <p>
 * The hull type determines the ship's structural integrity and cost multiplier.
 * Military ships typically use Reinforced hulls, while merchantmen use Standard.
 * </p>
 */
public enum ShipHullType {

    /**
     * Standard hull — normal construction for merchant and civilian ships.
     * Cost multiplier: 1.0×, Hull points: 1 per 1000 tons.
     */
    STANDARD(1.0, 1),

    /**
     * Reinforced hull — heavy construction for military vessels.
     * Cost multiplier: 1.5×, Hull points: 2 per 1000 tons.
     */
    REINFORCED(1.5, 2),

    /**
     * Double hull — maximum protection for capital ships.
     * Cost multiplier: 2.0×, Hull points: 3 per 1000 tons.
     */
    DOUBLE(2.0, 3);

    private final double costMultiplier;
    private final int hullPointsPer1000Tons;

    ShipHullType(double costMultiplier, int hullPointsPer1000Tons) {
        this.costMultiplier = costMultiplier;
        this.hullPointsPer1000Tons = hullPointsPer1000Tons;
    }

    /**
     * Returns the cost multiplier for this hull type.
     *
     * @return the cost multiplier (1.0–2.0)
     */
    public double getCostMultiplier() {
        return costMultiplier;
    }

    /**
     * Returns the hull points granted per 1000 tons of ship tonnage.
     *
     * @return hull points per 1000 tons
     */
    public int getHullPointsPer1000Tons() {
        return hullPointsPer1000Tons;
    }
}
