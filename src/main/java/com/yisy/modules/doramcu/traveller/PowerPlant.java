package com.yisy.modules.doramcu.traveller;

/**
 * Power plant types from Classic Traveller Book 2.
 * <p>
 * The power plant provides energy for all ship systems. Larger plants
 * allow more weapons and systems to operate simultaneously but consume
 * more tonnage and require more crew.
 * </p>
 * <p>
 * Tonnage cost: Plant size × 1% of total tonnage (minimum 1000 tons).
 * </p>
 */
public enum PowerPlant {

    /**
     * No power plant — ship has no powered systems.
     */
    NONE(0, 0),

    /**
     * Standard power plant — basic power for small ships.
     * Powers: 1 laser bank or basic systems.
     * Tonnage: 1% of ship tonnage.
     */
    STANDARD(1, 1),

    /**
     * Double power plant — doubled output for medium ships.
     * Powers: 2 laser banks or advanced systems.
     * Tonnage: 2% of ship tonnage.
     */
    DOUBLE(2, 2),

    /**
     * Triple power plant — triple output for large ships.
     * Powers: 3 laser banks or missile systems.
     * Tonnage: 3% of ship tonnage.
     */
    TRIPLE(3, 3),

    /**
     * Quadruple power plant — maximum output for capital ships.
     * Powers: 4+ laser banks and missile systems.
     * Tonnage: 4% of ship tonnage.
     */
    QUADRUPLE(4, 4);

    private final int size;
    private final int powerFactor;

    PowerPlant(int size, int powerFactor) {
        this.size = size;
        this.powerFactor = powerFactor;
    }

    /**
     * Returns the size rating of this power plant (0–4).
     *
     * @return the size rating
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the power factor — the number of weapon banks this plant can power.
     *
     * @return the power factor
     */
    public int getPowerFactor() {
        return powerFactor;
    }

    /**
     * Calculates the tons required for this power plant on a ship of the given tonnage.
     *
     * @param totalTonnage the total ship tonnage in tons
     * @return tons consumed by the power plant
     */
    public int getTonsRequired(int totalTonnage) {
        if (size == 0) return 0;
        return Math.max(1000, (totalTonnage * size) / 100);
    }

    /**
     * Returns the number of crew required to operate this power plant.
     * Standard = 1, Double = 2, Triple = 3, Quadruple = 4.
     *
     * @return crew required
     */
    public int getCrewRequired() {
        return size;
    }
}
