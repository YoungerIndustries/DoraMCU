package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Response DTO for a Traveller starship design.
 * <p>
 * Returns all design fields plus calculated statistics, allowing clients to
 * display complete ship information including tonnage allocation, combat power,
 * crew requirements, and financial costs.
 * </p>
 */
public class ShipDesignResponse {

    private final int id;
    private final String name;
    private final int totalTonnage;
    private final String hullType;
    private final int jumpRating;
    private final int maneuverRating;
    private final String powerPlant;
    private final String lifeSupport;
    private final int computerClass;
    private final int cargoTons;
    private final int passengerCount;
    private final String passengerClass;
    private final int fuelTons;
    private final List<WeaponResponse> weapons;
    private final DesignStats stats;

    public ShipDesignResponse(
            int id, String name, int totalTonnage, String hullType,
            int jumpRating, int maneuverRating, String powerPlant,
            String lifeSupport, int computerClass, int cargoTons,
            int passengerCount, String passengerClass, int fuelTons,
            List<WeaponResponse> weapons, DesignStats stats) {
        this.id = id;
        this.name = name;
        this.totalTonnage = totalTonnage;
        this.hullType = hullType;
        this.jumpRating = jumpRating;
        this.maneuverRating = maneuverRating;
        this.powerPlant = powerPlant;
        this.lifeSupport = lifeSupport;
        this.computerClass = computerClass;
        this.cargoTons = cargoTons;
        this.passengerCount = passengerCount;
        this.passengerClass = passengerClass;
        this.fuelTons = fuelTons;
        this.weapons = weapons;
        this.stats = stats;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalTonnage() {
        return totalTonnage;
    }

    public String getHullType() {
        return hullType;
    }

    public int getJumpRating() {
        return jumpRating;
    }

    public int getManeuverRating() {
        return maneuverRating;
    }

    public String getPowerPlant() {
        return powerPlant;
    }

    public String getLifeSupport() {
        return lifeSupport;
    }

    public int getComputerClass() {
        return computerClass;
    }

    public int getCargoTons() {
        return cargoTons;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public String getPassengerClass() {
        return passengerClass;
    }

    public int getFuelTons() {
        return fuelTons;
    }

    public List<WeaponResponse> getWeapons() {
        return weapons;
    }

    public DesignStats getStats() {
        return stats;
    }

    /**
     * Nested DTO for weapon system information.
     */
    public static class WeaponResponse {
        private final String weaponType;
        private final int quantity;
        private final String missileType;
        private final String hitLocation;
        private final int damageDice;
        private final int tons;

        public WeaponResponse(String weaponType, int quantity, String missileType,
                              String hitLocation, int damageDice, int tons) {
            this.weaponType = weaponType;
            this.quantity = quantity;
            this.missileType = missileType;
            this.hitLocation = hitLocation;
            this.damageDice = damageDice;
            this.tons = tons;
        }

        public String getWeaponType() {
            return weaponType;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getMissileType() {
            return missileType;
        }

        public String getHitLocation() {
            return hitLocation;
        }

        public int getDamageDice() {
            return damageDice;
        }

        public int getTons() {
            return tons;
        }
    }

    /**
     * Nested DTO for calculated design statistics.
     */
    public static class DesignStats {
        private final int fixedSystemTons;
        private final int weaponTons;
        private final int passengerTons;
        private final int unallocatedTons;
        private final int hullPoints;
        private final int estimatedCrew;
        private final long constructionCost;
        private final long annualMaintenanceCost;
        private final int totalDamageDice;
        private final double averageDamage;
        private final int gunneryBonus;
        private final boolean isValid;

        public DesignStats(int fixedSystemTons, int weaponTons, int passengerTons,
                           int unallocatedTons, int hullPoints, int estimatedCrew,
                           long constructionCost, long annualMaintenanceCost,
                           int totalDamageDice, double averageDamage,
                           int gunneryBonus, boolean isValid) {
            this.fixedSystemTons = fixedSystemTons;
            this.weaponTons = weaponTons;
            this.passengerTons = passengerTons;
            this.unallocatedTons = unallocatedTons;
            this.hullPoints = hullPoints;
            this.estimatedCrew = estimatedCrew;
            this.constructionCost = constructionCost;
            this.annualMaintenanceCost = annualMaintenanceCost;
            this.totalDamageDice = totalDamageDice;
            this.averageDamage = averageDamage;
            this.gunneryBonus = gunneryBonus;
            this.isValid = isValid;
        }

        public int getFixedSystemTons() {
            return fixedSystemTons;
        }

        public int getWeaponTons() {
            return weaponTons;
        }

        public int getPassengerTons() {
            return passengerTons;
        }

        public int getUnallocatedTons() {
            return unallocatedTons;
        }

        public int getHullPoints() {
            return hullPoints;
        }

        public int getEstimatedCrew() {
            return estimatedCrew;
        }

        public long getConstructionCost() {
            return constructionCost;
        }

        public long getAnnualMaintenanceCost() {
            return annualMaintenanceCost;
        }

        public int getTotalDamageDice() {
            return totalDamageDice;
        }

        public double getAverageDamage() {
            return averageDamage;
        }

        public int getGunneryBonus() {
            return gunneryBonus;
        }

        public boolean isValid() {
            return isValid;
        }
    }
}

