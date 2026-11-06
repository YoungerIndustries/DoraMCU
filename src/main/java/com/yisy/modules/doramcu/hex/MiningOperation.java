package com.yisy.modules.doramcu.hex;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a mining operation at a celestial body.
 * <p>
 * Mining operations extract raw resources from celestial bodies. The type and
 * amount of resources mined depends on the celestial body type and its properties.
 * Mining requires a facility (SpaceStation with MINING type or planet mining infrastructure).
 * </p>
 */
public class MiningOperation {

    private final CelestialBody celestialBody;
    private final int ownerEmpireId;
    private final int miningBonus;
    private final Map<RawResourceType, Integer> productionRate;
    private final int maxStorageCapacity;
    private final ResourceStorage<RawResourceType> storage;

    /**
     * Creates a mining operation for the given celestial body.
     *
     * @param celestialBody the celestial body being mined
     * @param ownerEmpireId the empire ID that owns this mining operation
     */
    public MiningOperation(CelestialBody celestialBody, int ownerEmpireId) {
        this(celestialBody, ownerEmpireId, 1);
    }

    /**
     * Creates a mining operation for the given celestial body with a mining bonus.
     *
     * @param celestialBody the celestial body being mined
     * @param ownerEmpireId the empire ID that owns this mining operation
     * @param miningBonus the mining bonus multiplier from nearby stations (1 = no bonus)
     */
    public MiningOperation(CelestialBody celestialBody, int ownerEmpireId, int miningBonus) {
        if (celestialBody == null) {
            throw new IllegalArgumentException("Celestial body must not be null");
        }
        this.celestialBody = celestialBody;
        this.ownerEmpireId = ownerEmpireId;
        this.miningBonus = Math.max(1, miningBonus);
        this.productionRate = calculateProductionRate(celestialBody, miningBonus);
        this.maxStorageCapacity = calculateStorageCapacity(celestialBody);
        this.storage = new ResourceStorage<>(RawResourceType.class, maxStorageCapacity);
    }

    /**
     * Calculates the production rate based on celestial body type and properties.
     */
    private Map<RawResourceType, Integer> calculateProductionRate(CelestialBody body, int miningBonus) {
        Map<RawResourceType, Integer> rate = new EnumMap<>(RawResourceType.class);

        // Base production from size
        int sizeMultiplier = 1;
        if (body instanceof Planet planet) {
            switch (planet.getPlanetSize()) {
                case SMALL: sizeMultiplier = 1; break;
                case MEDIUM: sizeMultiplier = 2; break;
                case LARGE: sizeMultiplier = 3; break;
            }
            // Planets produce ore and bio-mass
            int oreRate = (int) (sizeMultiplier * planet.getMineralQuality().getProductionBonus());
            rate.put(RawResourceType.ORE, Math.max(1, oreRate) * miningBonus);
            rate.put(RawResourceType.BIO_MASS, sizeMultiplier * miningBonus);
            // Planets with crystal deposits produce crystals
            if (planet.hasStrategicResource(StrategicResource.ELERIUM)) {
                rate.put(RawResourceType.CRYSTAL, sizeMultiplier * miningBonus);
            }
        } else if (body instanceof Asteroid || body instanceof AsteroidBelt) {
            // Asteroids produce ore and dust
            rate.put(RawResourceType.ORE, 2 * miningBonus);
            rate.put(RawResourceType.DUST, 3 * miningBonus);
        } else if (body instanceof GasGiant) {
            // Gas giants produce gas
            rate.put(RawResourceType.GAS, 3 * miningBonus);
        } else if (body instanceof Comet) {
            // Comets produce crystals
            rate.put(RawResourceType.CRYSTAL, 2 * miningBonus);
        } else if (body instanceof Nebula) {
            // Nebulae produce dust
            rate.put(RawResourceType.DUST, 2 * miningBonus);
        } else if (body instanceof Star) {
            // Stars produce crystals (energy harvesting)
            rate.put(RawResourceType.CRYSTAL, 1 * miningBonus);
        }

        return Map.copyOf(rate);
    }

    /** @return the mining bonus multiplier from nearby stations */
    public int getMiningBonus() {
        return miningBonus;
    }

    /**
     * Calculates the storage capacity based on celestial body size.
     */
    private int calculateStorageCapacity(CelestialBody body) {
        int base = 50;
        if (body instanceof Planet planet) {
            switch (planet.getPlanetSize()) {
                case SMALL: base = 50; break;
                case MEDIUM: base = 100; break;
                case LARGE: base = 150; break;
            }
        } else if (body instanceof Asteroid || body instanceof AsteroidBelt) {
            base = 30;
        } else if (body instanceof GasGiant) {
            base = 80;
        } else if (body instanceof Nebula) {
            base = 60;
        }
        return base;
    }

    /** @return the celestial body being mined */
    public CelestialBody getCelestialBody() {
        return celestialBody;
    }

    /** @return the empire ID that owns this mining operation */
    public int getOwnerEmpireId() {
        return ownerEmpireId;
    }

    /**
     * Returns the production rate per turn for each raw resource type.
     *
     * @return unmodifiable map of raw resource types to production amounts
     */
    public Map<RawResourceType, Integer> getProductionRate() {
        return productionRate;
    }

    /** @return the raw resource storage at this mining operation */
    public ResourceStorage<RawResourceType> getStorage() {
        return storage;
    }

    /**
     * Processes one turn of mining, adding produced resources to storage.
     *
     * @return the total amount of resources mined this turn
     */
    public int processTurn() {
        int totalMined = 0;
        for (Map.Entry<RawResourceType, Integer> entry : productionRate.entrySet()) {
            int added = storage.add(entry.getKey(), entry.getValue());
            totalMined += added;
        }
        return totalMined;
    }

    /**
     * Transfers resources from this mining operation's storage to the given destination.
     *
     * @param type the raw resource type to transfer
     * @param destination the destination storage
     * @return the amount transferred
     */
    public int transferTo(RawResourceType type, ResourceStorage<RawResourceType> destination) {
        int available = storage.getAmount(type);
        if (available <= 0) {
            return 0;
        }
        int canAccept = destination.getRemainingCapacity(type);
        int transfer = Math.min(available, canAccept);
        storage.remove(type, transfer);
        destination.add(type, transfer);
        return transfer;
    }

    /**
     * Returns a summary of this mining operation.
     */
    public String getSummary() {
        return String.format("Mining{body=%s, owner=%d, production=%s, storage=%s/%d}",
                celestialBody.getName(),
                ownerEmpireId,
                productionRate,
                storage.getTotalAmount(),
                storage.getTotalCapacity());
    }
}
