package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Response DTO for space station operations.
 */
public class StationResponse {

    private String operation;
    private String stationName;
    private String worldName;
    private String stationType;
    private boolean operational;
    private List<String> facilities;
    private int dockedShips;
    private int maxShips;
    private boolean dockFull;
    private String shipName;
    private boolean success;
    private int cost;
    private int fuelAdded;
    private int hullRepaired;
    private List<String> dockedShipNames;
    private String message;

    public StationResponse() {
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public String getStationType() { return stationType; }
    public void setStationType(String stationType) { this.stationType = stationType; }

    public boolean isOperational() { return operational; }
    public void setOperational(boolean operational) { this.operational = operational; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) { this.facilities = facilities; }

    public int getDockedShips() { return dockedShips; }
    public void setDockedShips(int dockedShips) { this.dockedShips = dockedShips; }

    public int getMaxShips() { return maxShips; }
    public void setMaxShips(int maxShips) { this.maxShips = maxShips; }

    public boolean isDockFull() { return dockFull; }
    public void setDockFull(boolean dockFull) { this.dockFull = dockFull; }

    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public int getFuelAdded() { return fuelAdded; }
    public void setFuelAdded(int fuelAdded) { this.fuelAdded = fuelAdded; }

    public int getHullRepaired() { return hullRepaired; }
    public void setHullRepaired(int hullRepaired) { this.hullRepaired = hullRepaired; }

    public List<String> getDockedShipNames() { return dockedShipNames; }
    public void setDockedShipNames(List<String> dockedShipNames) { this.dockedShipNames = dockedShipNames; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
