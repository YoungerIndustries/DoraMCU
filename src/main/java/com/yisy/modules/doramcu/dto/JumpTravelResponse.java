package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Response DTO for jump travel operations.
 */
public class JumpTravelResponse {

    /**
     * Type of operation performed.
     */
    private String operation;

    /**
     * Source world name.
     */
    private String sourceWorld;

    /**
     * Destination world name.
     */
    private String destinationWorld;

    /**
     * Number of jumps in the route.
     */
    private int jumpCount;

    /**
     * Total distance in parsecs.
     */
    private double totalDistance;

    /**
     * Total fuel required in tons.
     */
    private int totalFuelRequired;

    /**
     * Current fuel available in tons.
     */
    private int currentFuel;

    /**
     * Maximum fuel capacity in tons.
     */
    private int maxFuel;

    /**
     * Whether the route is safe (all jumps within hyperlanes).
     */
    private boolean isSafe;

    /**
     * Whether the ship has enough fuel for the route.
     */
    private boolean hasEnoughFuel;

    /**
     * Current travel state.
     */
    private String travelState;

    /**
     * Current system name.
     */
    private String currentSystem;

    /**
     * Route waypoints.
     */
    private List<RouteWaypoint> waypoints;

    /**
     * Message describing the result.
     */
    private String message;

    // ---- Default constructor for Jackson ----

    public JumpTravelResponse() {
    }

    // ---- Accessors ----

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getSourceWorld() {
        return sourceWorld;
    }

    public void setSourceWorld(String sourceWorld) {
        this.sourceWorld = sourceWorld;
    }

    public String getDestinationWorld() {
        return destinationWorld;
    }

    public void setDestinationWorld(String destinationWorld) {
        this.destinationWorld = destinationWorld;
    }

    public int getJumpCount() {
        return jumpCount;
    }

    public void setJumpCount(int jumpCount) {
        this.jumpCount = jumpCount;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getTotalFuelRequired() {
        return totalFuelRequired;
    }

    public void setTotalFuelRequired(int totalFuelRequired) {
        this.totalFuelRequired = totalFuelRequired;
    }

    public int getCurrentFuel() {
        return currentFuel;
    }

    public void setCurrentFuel(int currentFuel) {
        this.currentFuel = currentFuel;
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    public void setMaxFuel(int maxFuel) {
        this.maxFuel = maxFuel;
    }

    public boolean isSafe() {
        return isSafe;
    }

    public void setSafe(boolean safe) {
        isSafe = safe;
    }

    public boolean isHasEnoughFuel() {
        return hasEnoughFuel;
    }

    public void setHasEnoughFuel(boolean hasEnoughFuel) {
        hasEnoughFuel = hasEnoughFuel;
    }

    public String getTravelState() {
        return travelState;
    }

    public void setTravelState(String travelState) {
        this.travelState = travelState;
    }

    public String getCurrentSystem() {
        return currentSystem;
    }

    public void setCurrentSystem(String currentSystem) {
        this.currentSystem = currentSystem;
    }

    public List<RouteWaypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<RouteWaypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * A single waypoint in a jump route.
     */
    public static class RouteWaypoint {
        private int jumpNumber;
        private String worldName;
        private int worldId;
        private String uwp;
        private double distanceFromPrevious;
        private boolean isSafeJump;
        private int fuelCost;

        public RouteWaypoint() {
        }

        public int getJumpNumber() {
            return jumpNumber;
        }

        public void setJumpNumber(int jumpNumber) {
            this.jumpNumber = jumpNumber;
        }

        public String getWorldName() {
            return worldName;
        }

        public void setWorldName(String worldName) {
            this.worldName = worldName;
        }

        public int getWorldId() {
            return worldId;
        }

        public void setWorldId(int worldId) {
            this.worldId = worldId;
        }

        public String getUwp() {
            return uwp;
        }

        public void setUwp(String uwp) {
            this.uwp = uwp;
        }

        public double getDistanceFromPrevious() {
            return distanceFromPrevious;
        }

        public void setDistanceFromPrevious(double distanceFromPrevious) {
            this.distanceFromPrevious = distanceFromPrevious;
        }

        public boolean isSafeJump() {
            return isSafeJump;
        }

        public void setSafeJump(boolean safeJump) {
            isSafeJump = safeJump;
        }

        public int getFuelCost() {
            return fuelCost;
        }

        public void setFuelCost(int fuelCost) {
            this.fuelCost = fuelCost;
        }
    }
}
