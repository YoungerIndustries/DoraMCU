package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * DTO for a Traveller-style ship design.
 * <p>
 * Used in the ship design editor UI to create and manage tonnage-based
 * ship designs before converting them to actual ships.
 * </p>
 */
public class ShipDesignDto {

    private int id;
    private String name;
    private int totalTonnage;
    private String hullType;
    private int jumpRating;
    private int maneuverRating;
    private String powerPlant;
    private String lifeSupport;
    private int computerClass;
    private int cargoTons;
    private int passengerCount;
    private String passengerClass;
    private int fuelTons;
    private List<WeaponDto> weapons;
    private int crewSize;
    private int cost;
    private int combatValue;
    private int crewCost;

    public ShipDesignDto() {
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

    public int getTotalTonnage() {
        return totalTonnage;
    }

    public void setTotalTonnage(int totalTonnage) {
        this.totalTonnage = totalTonnage;
    }

    public String getHullType() {
        return hullType;
    }

    public void setHullType(String hullType) {
        this.hullType = hullType;
    }

    public int getJumpRating() {
        return jumpRating;
    }

    public void setJumpRating(int jumpRating) {
        this.jumpRating = jumpRating;
    }

    public int getManeuverRating() {
        return maneuverRating;
    }

    public void setManeuverRating(int maneuverRating) {
        this.maneuverRating = maneuverRating;
    }

    public String getPowerPlant() {
        return powerPlant;
    }

    public void setPowerPlant(String powerPlant) {
        this.powerPlant = powerPlant;
    }

    public String getLifeSupport() {
        return lifeSupport;
    }

    public void setLifeSupport(String lifeSupport) {
        this.lifeSupport = lifeSupport;
    }

    public int getComputerClass() {
        return computerClass;
    }

    public void setComputerClass(int computerClass) {
        this.computerClass = computerClass;
    }

    public int getCargoTons() {
        return cargoTons;
    }

    public void setCargoTons(int cargoTons) {
        this.cargoTons = cargoTons;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }

    public String getPassengerClass() {
        return passengerClass;
    }

    public void setPassengerClass(String passengerClass) {
        this.passengerClass = passengerClass;
    }

    public int getFuelTons() {
        return fuelTons;
    }

    public void setFuelTons(int fuelTons) {
        this.fuelTons = fuelTons;
    }

    public List<WeaponDto> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<WeaponDto> weapons) {
        this.weapons = weapons;
    }

    public int getCrewSize() {
        return crewSize;
    }

    public void setCrewSize(int crewSize) {
        this.crewSize = crewSize;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getCombatValue() {
        return combatValue;
    }

    public void setCombatValue(int combatValue) {
        this.combatValue = combatValue;
    }

    public int getCrewCost() {
        return crewCost;
    }

    public void setCrewCost(int crewCost) {
        this.crewCost = crewCost;
    }

    /**
     * Simplified weapon DTO for ship design.
     */
    public static class WeaponDto {
        private String weaponType;
        private int quantity;
        private String hitLocation;

        public String getWeaponType() {
            return weaponType;
        }

        public void setWeaponType(String weaponType) {
            this.weaponType = weaponType;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getHitLocation() {
            return hitLocation;
        }

        public void setHitLocation(String hitLocation) {
            this.hitLocation = hitLocation;
        }
    }
}
