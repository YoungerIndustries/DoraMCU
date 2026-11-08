package com.yisy.modules.doramcu.traveller;

/**
 * Calculated statistics for a Traveller starship design.
 * <p>
 * This class provides a snapshot of all derived statistics for a ship design,
 * including tonnage allocation breakdown, combat capabilities, crew requirements,
 * and financial costs. It is computed from the {@link ShipDesign} and can be
 * used for display, comparison, or persistence without holding a reference
 * to the mutable design.
 * </p>
 */
public class ShipDesignStats {

    private final int totalTonnage;
    private final int fixedSystemTons;
    private final int jumpDriveTons;
    private final int fusionDriveTons;
    private final int powerPlantTons;
    private final int lifeSupportTons;
    private final int computerTons;
    private final int weaponTons;
    private final int passengerTons;
    private final int cargoTons;
    private final int fuelTons;
    private final int unallocatedTons;
    private final int hullPoints;
    private final int estimatedCrew;
    private final long constructionCost;
    private final long annualMaintenanceCost;
    private final int jumpRating;
    private final int maneuverRating;
    private final int totalDamageDice;
    private final double averageDamage;
    private final int gunneryBonus;
    private final int passengerCount;
    private final boolean isValid;

    /**
     * Computes a stats snapshot from the given ship design.
     *
     * @param design the ship design to compute stats for
     */
    public ShipDesignStats(ShipDesign design) {
        this.totalTonnage = design.getTotalTonnage();
        this.jumpDriveTons = design.getJumpDrive().getTonsRequired(design.getTotalTonnage());
        this.fusionDriveTons = design.getFusionDrive().getTonsRequired(design.getTotalTonnage());
        this.powerPlantTons = design.getPowerPlant().getTonsRequired(design.getTotalTonnage());
        this.lifeSupportTons = design.getLifeSupport().getTonsRequired(design.getTotalTonnage());
        this.computerTons = design.getComputer().getTonsRequired(design.getTotalTonnage());
        this.fixedSystemTons = design.getFixedSystemTons();
        this.weaponTons = design.getWeaponTons();
        this.passengerTons = design.getPassengerTons();
        this.cargoTons = design.getCargoTons();
        this.fuelTons = design.getFuelTons();
        this.unallocatedTons = design.getRemainingTons();
        this.hullPoints = design.getHullPoints();
        this.estimatedCrew = design.getEstimatedCrew();
        this.constructionCost = design.getConstructionCost();
        this.annualMaintenanceCost = design.getAnnualMaintenanceCost();
        this.jumpRating = design.getMaxJumpParsecs();
        this.maneuverRating = design.getManeuverRating();
        this.totalDamageDice = design.getTotalDamageDice();
        this.averageDamage = design.getTotalAverageDamage();
        this.gunneryBonus = design.getGunneryBonus();
        this.passengerCount = design.getPassengerCount();
        this.isValid = design.isValid();
    }

    /** Returns the total ship tonnage. */
    public int getTotalTonnage() {
        return totalTonnage;
    }

    /** Returns the total tons consumed by fixed systems. */
    public int getFixedSystemTons() {
        return fixedSystemTons;
    }

    /** Returns the tons consumed by the jump drive. */
    public int getJumpDriveTons() {
        return jumpDriveTons;
    }

    /** Returns the tons consumed by the fusion drive. */
    public int getFusionDriveTons() {
        return fusionDriveTons;
    }

    /** Returns the tons consumed by the power plant. */
    public int getPowerPlantTons() {
        return powerPlantTons;
    }

    /** Returns the tons consumed by life support. */
    public int getLifeSupportTons() {
        return lifeSupportTons;
    }

    /** Returns the tons consumed by the computer. */
    public int getComputerTons() {
        return computerTons;
    }

    /** Returns the tons consumed by weapons. */
    public int getWeaponTons() {
        return weaponTons;
    }

    /** Returns the tons consumed by passengers. */
    public int getPassengerTons() {
        return passengerTons;
    }

    /** Returns the cargo capacity in tons. */
    public int getCargoTons() {
        return cargoTons;
    }

    /** Returns the fuel tons carried. */
    public int getFuelTons() {
        return fuelTons;
    }

    /** Returns the unallocated tons remaining. */
    public int getUnallocatedTons() {
        return unallocatedTons;
    }

    /** Returns the ship's hull points. */
    public int getHullPoints() {
        return hullPoints;
    }

    /** Returns the estimated crew count. */
    public int getEstimatedCrew() {
        return estimatedCrew;
    }

    /** Returns the construction cost in credits. */
    public long getConstructionCost() {
        return constructionCost;
    }

    /** Returns the annual maintenance cost in credits. */
    public long getAnnualMaintenanceCost() {
        return annualMaintenanceCost;
    }

    /** Returns the jump rating. */
    public int getJumpRating() {
        return jumpRating;
    }

    /** Returns the maneuver rating. */
    public int getManeuverRating() {
        return maneuverRating;
    }

    /** Returns the total damage dice (D6). */
    public int getTotalDamageDice() {
        return totalDamageDice;
    }

    /** Returns the average damage in megatons. */
    public double getAverageDamage() {
        return averageDamage;
    }

    /** Returns the gunnery dice modifier. */
    public int getGunneryBonus() {
        return gunneryBonus;
    }

    /** Returns the passenger count. */
    public int getPassengerCount() {
        return passengerCount;
    }

    /** Returns true if the design is valid (does not exceed tonnage). */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Returns the percentage of total tonnage allocated to fixed systems.
     */
    public double getFixedSystemPercentage() {
        return (double) fixedSystemTons / totalTonnage * 100.0;
    }

    /**
     * Returns the percentage of total tonnage allocated to weapons.
     */
    public double getWeaponPercentage() {
        return (double) weaponTons / totalTonnage * 100.0;
    }

    /**
     * Returns the percentage of total tonnage allocated to cargo.
     */
    public double getCargoPercentage() {
        return (double) cargoTons / totalTonnage * 100.0;
    }

    /**
     * Returns the percentage of total tonnage that is unallocated.
     */
    public double getUnallocatedPercentage() {
        return (double) unallocatedTons / totalTonnage * 100.0;
    }

    @Override
    public String toString() {
        return String.format(
            "ShipDesignStats{tons=%d, J=%d, M=%d, HP=%d, crew=%d, cost=%d cr, damage=%dD6, valid=%s}",
            totalTonnage, jumpRating, maneuverRating, hullPoints, estimatedCrew,
            constructionCost, totalDamageDice, isValid
        );
    }
}
