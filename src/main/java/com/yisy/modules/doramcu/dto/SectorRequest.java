package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for sector generation operations.
 */
public class SectorRequest {

    /**
     * Name of the sector to generate.
     */
    @NotNull
    private String sectorName;

    /**
     * Number of worlds to generate in the sector.
     */
    @Min(1)
    private int worldCount = 20;

    /**
     * Random seed for reproducible generation.
     * Null for random seed.
     */
    private Long seed;

    /**
     * Average jump distance between worlds (in parsecs).
     */
    @Min(1)
    private int averageJumpDistance = 4;

    // ---- Default constructor for Jackson ----

    public SectorRequest() {
    }

    // ---- Accessors ----

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public int getWorldCount() {
        return worldCount;
    }

    public void setWorldCount(int worldCount) {
        this.worldCount = worldCount;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public int getAverageJumpDistance() {
        return averageJumpDistance;
    }

    public void setAverageJumpDistance(int averageJumpDistance) {
        this.averageJumpDistance = averageJumpDistance;
    }
}
