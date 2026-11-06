package com.yisy.modules.doramcu.hex;

import java.util.Objects;

/**
 * Immutable value object representing a player's home location within the galaxy.
 * <p>
 * Consolidates 6 separate home location fields into a single atomic value,
 * ensuring consistency and enabling atomic updates.
 * </p>
 * <p>
 * Based on 4X game patterns from Stellaris and Galactic Civilizations IV,
 * where a player's capital system is identified by its full galaxy path.
 * </p>
 */
public class HomeLocation {

    private final Integer planetId;
    private final Integer systemId;
    private final Integer sectorRow;
    private final Integer sectorCol;
    private final Integer subSectorRow;
    private final Integer subSectorCol;

    /**
     * Creates a home location with all fields set.
     *
     * @param planetId the home planet ID
     * @param systemId the home solar system ID
     * @param sectorRow the sector row (0-9)
     * @param sectorCol the sector column (0-9)
     * @param subSectorRow the subsector row (0-9)
     * @param subSectorCol the subsector column (0-9)
     */
    public HomeLocation(Integer planetId, Integer systemId, Integer sectorRow, Integer sectorCol,
                        Integer subSectorRow, Integer subSectorCol) {
        this.planetId = planetId;
        this.systemId = systemId;
        this.sectorRow = sectorRow;
        this.sectorCol = sectorCol;
        this.subSectorRow = subSectorRow;
        this.subSectorCol = subSectorCol;
    }

    /**
     * Creates a home location with only planet and system IDs (no galaxy coordinates).
     *
     * @param planetId the home planet ID
     * @param systemId the home solar system ID
     */
    public HomeLocation(Integer planetId, Integer systemId) {
        this(planetId, systemId, null, null, null, null);
    }

    /**
     * Creates a builder for constructing HomeLocation instances incrementally.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the home planet ID, or null if not set. */
    public Integer getPlanetId() {
        return planetId;
    }

    /** Returns the home solar system ID, or null if not set. */
    public Integer getSystemId() {
        return systemId;
    }

    /** Returns the sector row (0-9), or null if not in a galaxy. */
    public Integer getSectorRow() {
        return sectorRow;
    }

    /** Returns the sector column (0-9), or null if not in a galaxy. */
    public Integer getSectorCol() {
        return sectorCol;
    }

    /** Returns the subsector row (0-9), or null if not set. */
    public Integer getSubSectorRow() {
        return subSectorRow;
    }

    /** Returns the subsector column (0-9), or null if not set. */
    public Integer getSubSectorCol() {
        return subSectorCol;
    }

    /** Returns true if this location has a planet set. */
    public boolean hasPlanet() {
        return planetId != null;
    }

    /** Returns true if this location has a system set. */
    public boolean hasSystem() {
        return systemId != null;
    }

    /** Returns true if this location has galaxy coordinates set. */
    public boolean hasGalaxyCoordinates() {
        return sectorRow != null && sectorCol != null;
    }

    /**
     * Returns a new HomeLocation with the planet ID updated.
     *
     * @param planetId the new planet ID
     * @return a new HomeLocation instance
     */
    public HomeLocation withPlanetId(Integer planetId) {
        return new HomeLocation(planetId, systemId, sectorRow, sectorCol, subSectorRow, subSectorCol);
    }

    /**
     * Returns a new HomeLocation with the system ID updated.
     *
     * @param systemId the new system ID
     * @return a new HomeLocation instance
     */
    public HomeLocation withSystemId(Integer systemId) {
        return new HomeLocation(planetId, systemId, sectorRow, sectorCol, subSectorRow, subSectorCol);
    }

    /**
     * Returns a new HomeLocation with the sector coordinates updated.
     *
     * @param sectorRow the sector row
     * @param sectorCol the sector column
     * @return a new HomeLocation instance
     */
    public HomeLocation withSector(Integer sectorRow, Integer sectorCol) {
        return new HomeLocation(planetId, systemId, sectorRow, sectorCol, subSectorRow, subSectorCol);
    }

    /**
     * Returns a new HomeLocation with the subsector coordinates updated.
     *
     * @param subSectorRow the subsector row
     * @param subSectorCol the subsector column
     * @return a new HomeLocation instance
     */
    public HomeLocation withSubSector(Integer subSectorRow, Integer subSectorCol) {
        return new HomeLocation(planetId, systemId, sectorRow, sectorCol, subSectorRow, subSectorCol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HomeLocation that = (HomeLocation) o;
        return Objects.equals(planetId, that.planetId) &&
                Objects.equals(systemId, that.systemId) &&
                Objects.equals(sectorRow, that.sectorRow) &&
                Objects.equals(sectorCol, that.sectorCol) &&
                Objects.equals(subSectorRow, that.subSectorRow) &&
                Objects.equals(subSectorCol, that.subSectorCol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planetId, systemId, sectorRow, sectorCol, subSectorRow, subSectorCol);
    }

    @Override
    public String toString() {
        if (hasGalaxyCoordinates()) {
            return "HomeLocation{planet=" + planetId + ", system=" + systemId +
                    ", sector=[" + sectorRow + "," + sectorCol + "]" +
                    ", subSector=[" + subSectorRow + "," + subSectorCol + "]}";
        }
        return "HomeLocation{planet=" + planetId + ", system=" + systemId + "}";
    }

    /**
     * Builder for constructing HomeLocation instances incrementally.
     */
    public static class Builder {
        private Integer planetId;
        private Integer systemId;
        private Integer sectorRow;
        private Integer sectorCol;
        private Integer subSectorRow;
        private Integer subSectorCol;

        public Builder planetId(Integer planetId) {
            this.planetId = planetId;
            return this;
        }

        public Builder systemId(Integer systemId) {
            this.systemId = systemId;
            return this;
        }

        public Builder sectorRow(Integer sectorRow) {
            this.sectorRow = sectorRow;
            return this;
        }

        public Builder sectorCol(Integer sectorCol) {
            this.sectorCol = sectorCol;
            return this;
        }

        public Builder subSectorRow(Integer subSectorRow) {
            this.subSectorRow = subSectorRow;
            return this;
        }

        public Builder subSectorCol(Integer subSectorCol) {
            this.subSectorCol = subSectorCol;
            return this;
        }

        public Builder sector(Integer row, Integer col) {
            this.sectorRow = row;
            this.sectorCol = col;
            return this;
        }

        public Builder subSector(Integer row, Integer col) {
            this.subSectorRow = row;
            this.subSectorCol = col;
            return this;
        }

        public HomeLocation build() {
            return new HomeLocation(planetId, systemId, sectorRow, sectorCol, subSectorRow, subSectorCol);
        }
    }
}
