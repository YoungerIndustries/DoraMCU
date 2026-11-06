package com.yisy.modules.doramcu.hex;

/**
 * Represents the classification of a space station.
 * <p>
 * Station types determine base hull, shields, module slots, and resource costs.
 * Different types serve different roles: military stations focus on defense,
 * shipyards enable ship construction, and mining stations extract resources.
 * </p>
 */
public enum SpaceStationType {
    /**
     * Military station — primary defensive fortification.
     * 500 hull, 100 shields, 4 module slots, 6 ship bay capacity.
     */
    MILITARY(500, 100, 4, 1000, 500, 6),

    /**
     * Shipyard station — enables ship construction.
     * 300 hull, 50 shields, 3 module slots, 10 ship bay capacity.
     */
    SHIPYARD(300, 50, 3, 1500, 750, 10),

    /**
     * Mining station — resource extraction.
     * 200 hull, 30 shields, 2 module slots, 2 ship bay capacity.
     */
    MINING(200, 30, 2, 800, 400, 2),

    /**
     * Research station — science output.
     * 250 hull, 40 shields, 2 module slots, 2 ship bay capacity.
     */
    RESEARCH(250, 40, 2, 900, 450, 2),

    /**
     * Trade station — economic hub.
     * 150 hull, 20 shields, 2 module slots, 4 ship bay capacity.
     */
    TRADE(150, 20, 2, 600, 300, 4),

    /**
     * Colony station — population support.
     * 200 hull, 30 shields, 3 module slots, 5 ship bay capacity.
     */
    COLONY(200, 30, 3, 700, 350, 5);

    private final int baseHullPoints;
    private final int baseShieldPoints;
    private final int baseModuleSlots;
    private final int mineralCost;
    private final int energyCost;
    private final int baseShipBayCapacity;

    SpaceStationType(int baseHullPoints, int baseShieldPoints, int baseModuleSlots,
                     int mineralCost, int energyCost, int baseShipBayCapacity) {
        this.baseHullPoints = baseHullPoints;
        this.baseShieldPoints = baseShieldPoints;
        this.baseModuleSlots = baseModuleSlots;
        this.mineralCost = mineralCost;
        this.energyCost = energyCost;
        this.baseShipBayCapacity = baseShipBayCapacity;
    }

    /**
     * @return the base hull points for this station type
     */
    public int getBaseHullPoints() {
        return baseHullPoints;
    }

    /**
     * @return the base shield points for this station type
     */
    public int getBaseShieldPoints() {
        return baseShieldPoints;
    }

    /**
     * @return the base module slots for this station type
     */
    public int getBaseModuleSlots() {
        return baseModuleSlots;
    }

    /**
     * @return the mineral cost to build this station type
     */
    public int getMineralCost() {
        return mineralCost;
    }

    /**
     * @return the energy cost to build this station type
     */
    public int getEnergyCost() {
        return energyCost;
    }

    /**
     * @return the base ship bay capacity for this station type
     */
    public int getBaseShipBayCapacity() {
        return baseShipBayCapacity;
    }

    // ---- Pipeline Bonuses ----

    /**
     * Returns the mining production bonus multiplier for this station type.
     * Mining stations double mining output; others provide no bonus.
     *
     * @return the mining bonus multiplier (1 = no bonus, 2 = double)
     */
    public int getMiningBonus() {
        return this == MINING ? 2 : 1;
    }

    /**
     * Returns the processing speed bonus multiplier for this station type.
     * Research stations double processing speed; others provide no bonus.
     *
     * @return the processing bonus multiplier (1 = no bonus, 2 = double)
     */
    public int getProcessingBonus() {
        return this == RESEARCH ? 2 : 1;
    }

    /**
     * Returns the assembly speed bonus multiplier for this station type.
     * Shipyards double assembly speed; others provide no bonus.
     *
     * @return the assembly bonus multiplier (1 = no bonus, 2 = double)
     */
    public int getAssemblyBonus() {
        return this == SHIPYARD ? 2 : 1;
    }

    // ---- Pipeline Upkeep ----

    /**
     * Returns the module upkeep cost per turn for this station type.
     * <p>
     * Stations consume construction modules each turn for maintenance.
     * Military stations consume HULL and SHIELD modules.
     * Mining stations consume HULL modules.
     * Shipyards consume ENGINE modules.
     * Research stations consume SHIELD modules.
     * Trade stations consume COMPOSITE modules.
     * Colony stations consume FACILITY modules.
     * </p>
     *
     * @return a map of module types to quantities consumed per turn
     */
    public java.util.Map<ConstructionModuleType, Integer> getModuleUpkeepPerTurn() {
        java.util.Map<ConstructionModuleType, Integer> upkeep = new java.util.EnumMap<>(ConstructionModuleType.class);
        switch (this) {
            case MILITARY:
                upkeep.put(ConstructionModuleType.HULL_MODULE, 1);
                upkeep.put(ConstructionModuleType.SHIELD_MODULE, 1);
                break;
            case MINING:
                upkeep.put(ConstructionModuleType.HULL_MODULE, 1);
                break;
            case SHIPYARD:
                upkeep.put(ConstructionModuleType.ENGINE_MODULE, 1);
                upkeep.put(ConstructionModuleType.HULL_MODULE, 1);
                break;
            case RESEARCH:
                upkeep.put(ConstructionModuleType.SHIELD_MODULE, 1);
                break;
            case TRADE:
                upkeep.put(ConstructionModuleType.FACILITY_MODULE, 1);
                break;
            case COLONY:
                upkeep.put(ConstructionModuleType.FACILITY_MODULE, 1);
                break;
            default:
                break;
        }
        return upkeep;
    }

    // ---- Pipeline Production ----

    /**
     * Returns the raw resource production per turn for this station type.
     * <p>
     * Mining stations extract ORE and GAS.
     * Trade stations generate no raw resources directly.
     * Colony stations support BIO_MASS production.
     * </p>
     *
     * @return a map of raw resource types to quantities produced per turn
     */
    public java.util.Map<RawResourceType, Integer> getRawResourceProductionPerTurn() {
        java.util.Map<RawResourceType, Integer> production = new java.util.EnumMap<>(RawResourceType.class);
        switch (this) {
            case MINING:
                production.put(RawResourceType.ORE, 3);
                production.put(RawResourceType.GAS, 1);
                break;
            case COLONY:
                production.put(RawResourceType.BIO_MASS, 2);
                break;
            default:
                break;
        }
        return production;
    }

    /**
     * Returns the refined resource production per turn for this station type.
     * <p>
     * Trade stations produce POWER_CELL and FUEL from trade routes.
     * Research stations produce COMPOSITE from research output.
     * </p>
     *
     * @return a map of refined resource types to quantities produced per turn
     */
    public java.util.Map<RefinedResourceType, Integer> getRefinedResourceProductionPerTurn() {
        java.util.Map<RefinedResourceType, Integer> production = new java.util.EnumMap<>(RefinedResourceType.class);
        switch (this) {
            case TRADE:
                production.put(RefinedResourceType.POWER_CELL, 2);
                production.put(RefinedResourceType.FUEL, 1);
                break;
            case RESEARCH:
                production.put(RefinedResourceType.COMPOSITE, 1);
                break;
            case SHIPYARD:
                production.put(RefinedResourceType.METAL_ALLOY, 1);
                break;
            default:
                break;
        }
        return production;
    }
}
