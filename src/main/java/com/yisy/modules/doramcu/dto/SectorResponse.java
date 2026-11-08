package com.yisy.modules.doramcu.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for sector and world operations.
 */
public class SectorResponse {

    /**
     * Sector name.
     */
    private String sectorName;

    /**
     * Total number of worlds in the sector.
     */
    private int worldCount;

    /**
     * Total number of hyperlanes.
     */
    private int hyperlaneCount;

    /**
     * Capital world name.
     */
    private String capitalWorld;

    /**
     * List of worlds in the sector.
     */
    private List<WorldSummary> worlds;

    /**
     * Single world detail (for world lookup).
     */
    private WorldDetail worldDetail;

    /**
     * Message describing the result.
     */
    private String message;

    // ---- Default constructor for Jackson ----

    public SectorResponse() {
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

    public int getHyperlaneCount() {
        return hyperlaneCount;
    }

    public void setHyperlaneCount(int hyperlaneCount) {
        this.hyperlaneCount = hyperlaneCount;
    }

    public String getCapitalWorld() {
        return capitalWorld;
    }

    public void setCapitalWorld(String capitalWorld) {
        this.capitalWorld = capitalWorld;
    }

    public List<WorldSummary> getWorlds() {
        return worlds;
    }

    public void setWorlds(List<WorldSummary> worlds) {
        this.worlds = worlds;
    }

    public WorldDetail getWorldDetail() {
        return worldDetail;
    }

    public void setWorldDetail(WorldDetail worldDetail) {
        this.worldDetail = worldDetail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Brief summary of a world in the sector.
     */
    public static class WorldSummary {
        private int id;
        private String name;
        private String uwp;
        private List<String> tradeCodes;
        private int x;
        private int y;
        private boolean isCapital;
        private int hyperlaneCount;

        public WorldSummary() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUwp() {
            return uwp;
        }

        public void setUwp(String uwp) {
            this.uwp = uwp;
        }

        public List<String> getTradeCodes() {
            return tradeCodes;
        }

        public void setTradeCodes(List<String> tradeCodes) {
            this.tradeCodes = tradeCodes;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public boolean isCapital() {
            return isCapital;
        }

        public void setCapital(boolean capital) {
            isCapital = capital;
        }

        public int getHyperlaneCount() {
            return hyperlaneCount;
        }

        public void setHyperlaneCount(int hyperlaneCount) {
            this.hyperlaneCount = hyperlaneCount;
        }
    }

    /**
     * Detailed information about a single world.
     */
    public static class WorldDetail {
        private int id;
        private String name;
        private String uwp;
        private String port;
        private String atmosphere;
        private String gravity;
        private String starport;
        private int technologyLevel;
        private int population;
        private String governmentType;
        private String lawLevel;
        private List<String> tradeCodes;
        private int x;
        private int y;
        private boolean isCapital;
        private double portWealth;
        private List<String> hyperlanes;
        private boolean isHabitable;
        private boolean isBreathable;

        public WorldDetail() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUwp() {
            return uwp;
        }

        public void setUwp(String uwp) {
            this.uwp = uwp;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getAtmosphere() {
            return atmosphere;
        }

        public void setAtmosphere(String atmosphere) {
            this.atmosphere = atmosphere;
        }

        public String getGravity() {
            return gravity;
        }

        public void setGravity(String gravity) {
            this.gravity = gravity;
        }

        public String getStarport() {
            return starport;
        }

        public void setStarport(String starport) {
            this.starport = starport;
        }

        public int getTechnologyLevel() {
            return technologyLevel;
        }

        public void setTechnologyLevel(int technologyLevel) {
            this.technologyLevel = technologyLevel;
        }

        public int getPopulation() {
            return population;
        }

        public void setPopulation(int population) {
            this.population = population;
        }

        public String getGovernmentType() {
            return governmentType;
        }

        public void setGovernmentType(String governmentType) {
            this.governmentType = governmentType;
        }

        public String getLawLevel() {
            return lawLevel;
        }

        public void setLawLevel(String lawLevel) {
            this.lawLevel = lawLevel;
        }

        public List<String> getTradeCodes() {
            return tradeCodes;
        }

        public void setTradeCodes(List<String> tradeCodes) {
            this.tradeCodes = tradeCodes;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public boolean isCapital() {
            return isCapital;
        }

        public void setCapital(boolean capital) {
            isCapital = capital;
        }

        public double getPortWealth() {
            return portWealth;
        }

        public void setPortWealth(double portWealth) {
            this.portWealth = portWealth;
        }

        public List<String> getHyperlanes() {
            return hyperlanes;
        }

        public void setHyperlanes(List<String> hyperlanes) {
            this.hyperlanes = hyperlanes;
        }

        public boolean isHabitable() {
            return isHabitable;
        }

        public void setHabitable(boolean habitable) {
            isHabitable = habitable;
        }

        public boolean isBreathable() {
            return isBreathable;
        }

        public void setBreathable(boolean breathable) {
            isBreathable = breathable;
        }
    }
}
