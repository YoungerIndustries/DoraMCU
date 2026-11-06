package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * DTO representing a fleet in the game state response.
 * Includes movement stats for MDrive (within-system) and JDrive (between-system).
 */
public class FleetDto {

    private int id;
    private String name;
    private int positionX;
    private int positionY;
    private int positionZ;
    /** Subsector row (0-9) where this fleet is located, for map positioning. */
    private int subSectorRow;
    /** Subsector column (0-9) where this fleet is located, for map positioning. */
    private int subSectorCol;
    private int mDriveSpeed;
    private int jDriveSpeed;
    private int effectiveMDriveSpeed;
    private int effectiveJDriveSpeed;
    private double healthRatio;
    private int shipCount;
    private int totalDamage;
    private int currentShieldPoints;
    private int totalShieldPoints;
    private int currentHullPoints;
    private int totalHullPoints;
    private List<ShipDto> ships;

    public FleetDto() {
    }

    public FleetDto(int id, String name, int positionX, int positionY, int positionZ,
                    int subSectorRow, int subSectorCol,
                    int mDriveSpeed, int jDriveSpeed, int effectiveMDriveSpeed, int effectiveJDriveSpeed,
                    double healthRatio, int shipCount, int totalDamage,
                    int currentShieldPoints, int totalShieldPoints,
                    int currentHullPoints, int totalHullPoints,
                    List<ShipDto> ships) {
        this.id = id;
        this.name = name;
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.subSectorRow = subSectorRow;
        this.subSectorCol = subSectorCol;
        this.mDriveSpeed = mDriveSpeed;
        this.jDriveSpeed = jDriveSpeed;
        this.effectiveMDriveSpeed = effectiveMDriveSpeed;
        this.effectiveJDriveSpeed = effectiveJDriveSpeed;
        this.healthRatio = healthRatio;
        this.shipCount = shipCount;
        this.totalDamage = totalDamage;
        this.currentShieldPoints = currentShieldPoints;
        this.totalShieldPoints = totalShieldPoints;
        this.currentHullPoints = currentHullPoints;
        this.totalHullPoints = totalHullPoints;
        this.ships = ships;
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

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public int getPositionZ() {
        return positionZ;
    }

    public void setPositionZ(int positionZ) {
        this.positionZ = positionZ;
    }

    public int getSubSectorRow() {
        return subSectorRow;
    }

    public void setSubSectorRow(int subSectorRow) {
        this.subSectorRow = subSectorRow;
    }

    public int getSubSectorCol() {
        return subSectorCol;
    }

    public void setSubSectorCol(int subSectorCol) {
        this.subSectorCol = subSectorCol;
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

    public int getEffectiveMDriveSpeed() {
        return effectiveMDriveSpeed;
    }

    public void setEffectiveMDriveSpeed(int effectiveMDriveSpeed) {
        this.effectiveMDriveSpeed = effectiveMDriveSpeed;
    }

    public int getEffectiveJDriveSpeed() {
        return effectiveJDriveSpeed;
    }

    public void setEffectiveJDriveSpeed(int effectiveJDriveSpeed) {
        this.effectiveJDriveSpeed = effectiveJDriveSpeed;
    }

    public double getHealthRatio() {
        return healthRatio;
    }

    public void setHealthRatio(double healthRatio) {
        this.healthRatio = healthRatio;
    }

    public int getShipCount() {
        return shipCount;
    }

    public void setShipCount(int shipCount) {
        this.shipCount = shipCount;
    }

    public int getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(int totalDamage) {
        this.totalDamage = totalDamage;
    }

    public int getCurrentShieldPoints() {
        return currentShieldPoints;
    }

    public void setCurrentShieldPoints(int currentShieldPoints) {
        this.currentShieldPoints = currentShieldPoints;
    }

    public int getTotalShieldPoints() {
        return totalShieldPoints;
    }

    public void setTotalShieldPoints(int totalShieldPoints) {
        this.totalShieldPoints = totalShieldPoints;
    }

    public int getCurrentHullPoints() {
        return currentHullPoints;
    }

    public void setCurrentHullPoints(int currentHullPoints) {
        this.currentHullPoints = currentHullPoints;
    }

    public int getTotalHullPoints() {
        return totalHullPoints;
    }

    public void setTotalHullPoints(int totalHullPoints) {
        this.totalHullPoints = totalHullPoints;
    }

    public List<ShipDto> getShips() {
        return ships;
    }

    public void setShips(List<ShipDto> ships) {
        this.ships = ships;
    }
}
