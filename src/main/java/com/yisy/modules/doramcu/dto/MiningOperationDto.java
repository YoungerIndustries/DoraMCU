package com.yisy.modules.doramcu.dto;

import java.util.Map;

/**
 * Detailed information about a mining operation.
 * <p>
 * Includes the celestial body being mined, production rates per turn,
 * current storage levels, and mining bonus from nearby stations.
 * </p>
 */
public class MiningOperationDto {

    /** Unique ID of the celestial body being mined */
    private int celestialBodyId;

    /** Name of the celestial body */
    private String celestialBodyName;

    /** Type of celestial body (Planet, Asteroid, GasGiant, etc.) */
    private String celestialBodyType;

    /** Empire ID that owns this mining operation */
    private int ownerEmpireId;

    /** Mining bonus multiplier from nearby stations (1 = no bonus) */
    private int miningBonus;

    /** Production rate per turn by raw resource type */
    private Map<String, Integer> productionRate;

    /** Current raw resource storage amounts */
    private Map<String, Integer> storage;

    /** Maximum storage capacity */
    private int maxStorageCapacity;

    public MiningOperationDto() {
    }

    public MiningOperationDto(int celestialBodyId, String celestialBodyName, String celestialBodyType,
                               int ownerEmpireId, int miningBonus,
                               Map<String, Integer> productionRate,
                               Map<String, Integer> storage, int maxStorageCapacity) {
        this.celestialBodyId = celestialBodyId;
        this.celestialBodyName = celestialBodyName;
        this.celestialBodyType = celestialBodyType;
        this.ownerEmpireId = ownerEmpireId;
        this.miningBonus = miningBonus;
        this.productionRate = productionRate;
        this.storage = storage;
        this.maxStorageCapacity = maxStorageCapacity;
    }

    public int getCelestialBodyId() {
        return celestialBodyId;
    }

    public void setCelestialBodyId(int celestialBodyId) {
        this.celestialBodyId = celestialBodyId;
    }

    public String getCelestialBodyName() {
        return celestialBodyName;
    }

    public void setCelestialBodyName(String celestialBodyName) {
        this.celestialBodyName = celestialBodyName;
    }

    public String getCelestialBodyType() {
        return celestialBodyType;
    }

    public void setCelestialBodyType(String celestialBodyType) {
        this.celestialBodyType = celestialBodyType;
    }

    public int getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(int ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }

    public int getMiningBonus() {
        return miningBonus;
    }

    public void setMiningBonus(int miningBonus) {
        this.miningBonus = miningBonus;
    }

    public Map<String, Integer> getProductionRate() {
        return productionRate;
    }

    public void setProductionRate(Map<String, Integer> productionRate) {
        this.productionRate = productionRate;
    }

    public Map<String, Integer> getStorage() {
        return storage;
    }

    public void setStorage(Map<String, Integer> storage) {
        this.storage = storage;
    }

    public int getMaxStorageCapacity() {
        return maxStorageCapacity;
    }

    public void setMaxStorageCapacity(int maxStorageCapacity) {
        this.maxStorageCapacity = maxStorageCapacity;
    }
}
