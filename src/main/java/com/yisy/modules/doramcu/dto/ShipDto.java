package com.yisy.modules.doramcu.dto;

/**
 * DTO representing a ship within a fleet in the game state response.
 */
public class ShipDto {

    private int id;
    private String name;
    private String shipType;
    private int mDriveSpeed;
    private int jDriveSpeed;
    private double hullHealthRatio;
    private int currentHullPoints;
    private int maxHullPoints;
    private int currentShieldPoints;
    private int maxShieldPoints;
    private int totalDamage;
    private boolean destroyed;

    public ShipDto() {
    }

    public ShipDto(int id, String name, String shipType, int mDriveSpeed, int jDriveSpeed,
                   double hullHealthRatio, int currentHullPoints, int maxHullPoints,
                   int currentShieldPoints, int maxShieldPoints, int totalDamage, boolean destroyed) {
        this.id = id;
        this.name = name;
        this.shipType = shipType;
        this.mDriveSpeed = mDriveSpeed;
        this.jDriveSpeed = jDriveSpeed;
        this.hullHealthRatio = hullHealthRatio;
        this.currentHullPoints = currentHullPoints;
        this.maxHullPoints = maxHullPoints;
        this.currentShieldPoints = currentShieldPoints;
        this.maxShieldPoints = maxShieldPoints;
        this.totalDamage = totalDamage;
        this.destroyed = destroyed;
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

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public int getMDriveSpeed() {
        return mDriveSpeed;
    }

    public void setMDriveSpeed(int mDriveSpeed) {
        this.mDriveSpeed = mDriveSpeed;
    }

    public int getJDriveSpeed() {
        return jDriveSpeed;
    }

    public void setJDriveSpeed(int jDriveSpeed) {
        this.jDriveSpeed = jDriveSpeed;
    }

    public double getHullHealthRatio() {
        return hullHealthRatio;
    }

    public void setHullHealthRatio(double hullHealthRatio) {
        this.hullHealthRatio = hullHealthRatio;
    }

    public int getCurrentHullPoints() {
        return currentHullPoints;
    }

    public void setCurrentHullPoints(int currentHullPoints) {
        this.currentHullPoints = currentHullPoints;
    }

    public int getMaxHullPoints() {
        return maxHullPoints;
    }

    public void setMaxHullPoints(int maxHullPoints) {
        this.maxHullPoints = maxHullPoints;
    }

    public int getCurrentShieldPoints() {
        return currentShieldPoints;
    }

    public void setCurrentShieldPoints(int currentShieldPoints) {
        this.currentShieldPoints = currentShieldPoints;
    }

    public int getMaxShieldPoints() {
        return maxShieldPoints;
    }

    public void setMaxShieldPoints(int maxShieldPoints) {
        this.maxShieldPoints = maxShieldPoints;
    }

    public int getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(int totalDamage) {
        this.totalDamage = totalDamage;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
