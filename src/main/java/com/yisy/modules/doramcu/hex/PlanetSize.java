package com.yisy.modules.doramcu.hex;

/**
 * Represents the size category of a planet, which determines maximum population capacity,
 * base resource yields, building slots, and planet radius.
 * <p>
 * Planet size affects the total number of population slots, base resource production,
 * and the number of available building slots. Larger planets can support more population,
 * produce more resources, and accommodate more buildings.
 * </p>
 * <p>
 * Follows the size classification pattern used in other celestial bodies (StarSize, MoonSize,
 * NebulaSize, BlackHoleSize, CometSize, AsteroidSize).
 * </p>
 */
public enum PlanetSize {
    /**
     * Small planet — limited capacity, modest resources, few buildings.
     * Base radius 5, mineral yield 3, food yield 4, research yield 2, 3 building slots.
     */
    SMALL(5, 3, 4, 2, 3, 5),

    /**
     * Medium planet — balanced capacity and resources.
     * Base radius 8, mineral yield 5, food yield 6, research yield 4, 5 building slots.
     */
    MEDIUM(10, 5, 6, 4, 5, 8),

    /**
     * Large planet — high capacity and strong resource output.
     * Base radius 10, mineral yield 8, food yield 8, research yield 6, 7 building slots.
     */
    LARGE(15, 8, 8, 6, 7, 10),

    /**
     * Huge planet — maximum capacity and exceptional resources.
     * Base radius 12, mineral yield 12, food yield 10, research yield 8, 10 building slots.
     */
    HUGE(20, 12, 10, 8, 10, 12);

    private final int maxPopulation;
    private final int baseRadius;
    private final int baseMineralYield;
    private final int baseFoodYield;
    private final int baseResearchYield;
    private final int buildingSlots;

    PlanetSize(int maxPopulation, int baseMineralYield, int baseFoodYield, int baseResearchYield, int buildingSlots, int baseRadius) {
        this.maxPopulation = maxPopulation;
        this.baseMineralYield = baseMineralYield;
        this.baseFoodYield = baseFoodYield;
        this.baseResearchYield = baseResearchYield;
        this.buildingSlots = buildingSlots;
        this.baseRadius = baseRadius;
    }

    /**
     * Returns the maximum population this planet size can support.
     *
     * @return the maximum population capacity
     */
    public int getMaxPopulation() {
        return maxPopulation;
    }

    /**
     * Returns the base radius for a planet of this size.
     *
     * @return the base radius in hex cells
     */
    public int getBaseRadius() {
        return baseRadius;
    }

    /**
     * Returns the base mineral yield for a planet of this size.
     *
     * @return the base mineral production per turn
     */
    public int getBaseMineralYield() {
        return baseMineralYield;
    }

    /**
     * Returns the base food yield for a planet of this size.
     *
     * @return the base food production per turn
     */
    public int getBaseFoodYield() {
        return baseFoodYield;
    }

    /**
     * Returns the base research yield for a planet of this size.
     *
     * @return the base research production per turn
     */
    public int getBaseResearchYield() {
        return baseResearchYield;
    }

    /**
     * Returns the number of building slots available for a planet of this size.
     *
     * @return the number of building slots
     */
    public int getBuildingSlots() {
        return buildingSlots;
    }
}
