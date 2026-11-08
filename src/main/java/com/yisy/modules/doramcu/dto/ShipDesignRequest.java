package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for creating or updating a Traveller starship design.
 * <p>
 * Maps directly to the ShipDesign model fields, allowing clients to
 * specify all aspects of a starship design through the REST API.
 * </p>
 */
public class ShipDesignRequest {

    /** Ship name (required for creation). */
    @NotBlank(message = "Ship name is required")
    private String name;

    /** Total ship tonnage in metric tons (minimum 1000). */
    @NotNull(message = "Total tonnage is required")
    @Min(value = 1000, message = "Minimum ship tonnage is 1000 tons")
    private Integer totalTonnage;

    /** Hull construction type (STANDARD, REINFORCED, DOUBLE). */
    private String hullType;

    /** Jump drive rating (0-9, where 0 means no jump capability). */
    @Min(value = 0, message = "Jump rating must be 0-9")
    private Integer jumpRating;

    /** Fusion drive maneuver rating (0-9, where 0 means no maneuver capability). */
    @Min(value = 0, message = "Maneuver rating must be 0-9")
    private Integer maneuverRating;

    /** Power plant size (NONE, STANDARD, DOUBLE, TRIPLE, QUADRUPLE). */
    private String powerPlant;

    /** Life support type (STANDARD, DOUBLED, VACUUM, NONE). */
    private String lifeSupport;

    /** Shipboard computer class (0-4, where 0 means no computer). */
    @Min(value = 0, message = "Computer class must be 0-4")
    private Integer computerClass;

    /** Cargo capacity in tons. */
    @Min(value = 0, message = "Cargo tons cannot be negative")
    private Integer cargoTons;

    /** Number of passengers. */
    @Min(value = 0, message = "Passenger count cannot be negative")
    private Integer passengerCount;

    /** Passenger comfort class (TROOP, MILITARY, FIRST, SECOND, THIRD, VIP). */
    private String passengerClass;

    /** Fuel tons carried. */
    @Min(value = 0, message = "Fuel tons cannot be negative")
    private Integer fuelTons;

    /** List of weapon systems to mount on the ship. */
    private List<WeaponRequest> weapons;

    /**
     * Nested DTO for individual weapon systems.
     */
    public static class WeaponRequest {

        /** Weapon type (SMALL_LASER, MEDIUM_LASER, LARGE_LASER, HUGE_LASER, MISSILE_LAUNCHER). */
        @NotBlank(message = "Weapon type is required")
        private String weaponType;

        /** Number of weapons (lasers or launchers). */
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        /** Missile type for missile launchers (STANDARD, HEAVY, SPECIAL, NUCLEAR). */
        private String missileType;

        /** Number of missile pods (6 missiles per pod). */
        private Integer missilePods;

        /** Hit location hex on the ship (e.g., "A1", "B2"). */
        @NotBlank(message = "Hit location is required")
        private String hitLocation;

        public String getWeaponType() {
            return weaponType;
        }

        public void setWeaponType(String weaponType) {
            this.weaponType = weaponType;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getMissileType() {
            return missileType;
        }

        public void setMissileType(String missileType) {
            this.missileType = missileType;
        }

        public Integer getMissilePods() {
            return missilePods;
        }

        public void setMissilePods(Integer missilePods) {
            this.missilePods = missilePods;
        }

        public String getHitLocation() {
            return hitLocation;
        }

        public void setHitLocation(String hitLocation) {
            this.hitLocation = hitLocation;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotalTonnage() {
        return totalTonnage;
    }

    public void setTotalTonnage(Integer totalTonnage) {
        this.totalTonnage = totalTonnage;
    }

    public String getHullType() {
        return hullType;
    }

    public void setHullType(String hullType) {
        this.hullType = hullType;
    }

    public Integer getJumpRating() {
        return jumpRating;
    }

    public void setJumpRating(Integer jumpRating) {
        this.jumpRating = jumpRating;
    }

    public Integer getManeuverRating() {
        return maneuverRating;
    }

    public void setManeuverRating(Integer maneuverRating) {
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

    public Integer getComputerClass() {
        return computerClass;
    }

    public void setComputerClass(Integer computerClass) {
        this.computerClass = computerClass;
    }

    public Integer getCargoTons() {
        return cargoTons;
    }

    public void setCargoTons(Integer cargoTons) {
        this.cargoTons = cargoTons;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public String getPassengerClass() {
        return passengerClass;
    }

    public void setPassengerClass(String passengerClass) {
        this.passengerClass = passengerClass;
    }

    public Integer getFuelTons() {
        return fuelTons;
    }

    public void setFuelTons(Integer fuelTons) {
        this.fuelTons = fuelTons;
    }

    public List<WeaponRequest> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<WeaponRequest> weapons) {
        this.weapons = weapons;
    }
}
