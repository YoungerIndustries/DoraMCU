package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a complete Traveller starship design based on Classic Traveller Book 2 rules.
 * <p>
 * A ship design is defined by its total tonnage and the allocation of that tonnage
 * to various systems: hull, jump drive, fusion drive, power plant, life support,
 * computers, cargo, passengers, and weapons.
 * </p>
 * <p>
 * The design process follows the Classic Traveller rules:
 * </p>
 * <ol>
 *   <li>Set total tonnage (e.g., 2D6×1000 for military, 4D6×1000 for merchant)</li>
 *   <li>Allocate tons to hull type (determines HP and cost)</li>
 *   <li>Allocate tons to jump drive (J-rating)</li>
 *   <li>Allocate tons to fusion drive (M-rating)</li>
 *   <li>Allocate tons to power plant</li>
 *   <li>Allocate tons to life support</li>
 *   <li>Allocate tons to shipboard computers</li>
 *   <li>Allocate remaining tons to cargo, passengers, and weapons</li>
 * </ol>
 * <p>
 * This class validates that all allocations fit within the total tonnage
 * and calculates derived statistics like crew count, ship cost, and maintenance.
 * </p>
 */
public class ShipDesign {

    private final int id;
    private String name;
    private final int totalTonnage;
    private ShipHullType hullType;
    private JumpDrive jumpDrive;
    private FusionDrive fusionDrive;
    private PowerPlant powerPlant;
    private LifeSupport lifeSupport;
    private ShipComputer computer;
    private int cargoTons;
    private int passengerCount;
    private PassengerClass passengerClass;
    private final List<ShipWeaponSystem> weapons;
    private int fuelTons;

    /**
     * Creates a new ship design with the given ID, name, and total tonnage.
     *
     * @param id the unique design identifier
     * @param name the ship's name
     * @param totalTonnage the total ship tonnage in metric tons
     */
    public ShipDesign(int id, String name, int totalTonnage) {
        if (totalTonnage < 1000) {
            throw new IllegalArgumentException("Minimum ship tonnage is 1000 tons");
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.totalTonnage = totalTonnage;
        this.hullType = ShipHullType.STANDARD;
        this.jumpDrive = JumpDrive.NONE;
        this.fusionDrive = FusionDrive.NONE;
        this.powerPlant = PowerPlant.NONE;
        this.lifeSupport = LifeSupport.NONE;
        this.computer = ShipComputer.NONE;
        this.cargoTons = 0;
        this.passengerCount = 0;
        this.passengerClass = PassengerClass.THIRD;
        this.weapons = new ArrayList<>();
        this.fuelTons = 0;
    }

    // ---- Accessors ----

    /** Returns the design ID. */
    public int getId() {
        return id;
    }

    /** Returns the ship's name. */
    public String getName() {
        return name;
    }

    /** Sets the ship's name. */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
    }

    /** Returns the total ship tonnage. */
    public int getTotalTonnage() {
        return totalTonnage;
    }

    /** Returns the hull type. */
    public ShipHullType getHullType() {
        return hullType;
    }

    /** Sets the hull type and validates tonnage. */
    public void setHullType(ShipHullType hullType) {
        this.hullType = Objects.requireNonNull(hullType, "Hull type must not be null");
    }

    /** Returns the jump drive. */
    public JumpDrive getJumpDrive() {
        return jumpDrive;
    }

    /** Sets the jump drive and validates tonnage. */
    public void setJumpDrive(JumpDrive jumpDrive) {
        this.jumpDrive = Objects.requireNonNull(jumpDrive, "Jump drive must not be null");
    }

    /** Returns the fusion drive. */
    public FusionDrive getFusionDrive() {
        return fusionDrive;
    }

    /** Sets the fusion drive and validates tonnage. */
    public void setFusionDrive(FusionDrive fusionDrive) {
        this.fusionDrive = Objects.requireNonNull(fusionDrive, "Fusion drive must not be null");
    }

    /** Returns the power plant. */
    public PowerPlant getPowerPlant() {
        return powerPlant;
    }

    /** Sets the power plant and validates tonnage. */
    public void setPowerPlant(PowerPlant powerPlant) {
        this.powerPlant = Objects.requireNonNull(powerPlant, "Power plant must not be null");
    }

    /** Returns the life support type. */
    public LifeSupport getLifeSupport() {
        return lifeSupport;
    }

    /** Sets the life support type and validates tonnage. */
    public void setLifeSupport(LifeSupport lifeSupport) {
        this.lifeSupport = Objects.requireNonNull(lifeSupport, "Life support must not be null");
    }

    /** Returns the shipboard computer. */
    public ShipComputer getComputer() {
        return computer;
    }

    /** Sets the shipboard computer and validates tonnage. */
    public void setComputer(ShipComputer computer) {
        this.computer = Objects.requireNonNull(computer, "Computer must not be null");
    }

    /** Returns the cargo capacity in tons. */
    public int getCargoTons() {
        return cargoTons;
    }

    /** Sets the cargo capacity in tons. */
    public void setCargoTons(int cargoTons) {
        if (cargoTons < 0) {
            throw new IllegalArgumentException("Cargo tons cannot be negative");
        }
        this.cargoTons = cargoTons;
    }

    /** Returns the number of passengers. */
    public int getPassengerCount() {
        return passengerCount;
    }

    /** Sets the passenger count. */
    public void setPassengerCount(int passengerCount) {
        if (passengerCount < 0) {
            throw new IllegalArgumentException("Passenger count cannot be negative");
        }
        this.passengerCount = passengerCount;
    }

    /** Returns the passenger comfort class. */
    public PassengerClass getPassengerClass() {
        return passengerClass;
    }

    /** Sets the passenger comfort class. */
    public void setPassengerClass(PassengerClass passengerClass) {
        this.passengerClass = Objects.requireNonNull(passengerClass, "Passenger class must not be null");
    }

    /** Returns the fuel tons carried. */
    public int getFuelTons() {
        return fuelTons;
    }

    /** Sets the fuel tons carried. */
    public void setFuelTons(int fuelTons) {
        if (fuelTons < 0) {
            throw new IllegalArgumentException("Fuel tons cannot be negative");
        }
        this.fuelTons = fuelTons;
    }

    // ---- Weapons ----

    /** Returns an unmodifiable list of weapon systems. */
    public List<ShipWeaponSystem> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    /**
     * Adds a weapon system to this design.
     *
     * @param weapon the weapon system to add
     */
    public void addWeapon(ShipWeaponSystem weapon) {
        Objects.requireNonNull(weapon, "Weapon must not be null");
        weapons.add(weapon);
    }

    /**
     * Removes a weapon system from this design.
     *
     * @param weapon the weapon system to remove
     */
    public void removeWeapon(ShipWeaponSystem weapon) {
        weapons.remove(weapon);
    }

    // ---- Calculated statistics ----

    /**
     * Calculates the total tons consumed by all fixed systems
     * (jump drive, fusion drive, power plant, life support, computer).
     *
     * @return tons consumed by fixed systems
     */
    public int getFixedSystemTons() {
        return jumpDrive.getTonsRequired(totalTonnage)
                + fusionDrive.getTonsRequired(totalTonnage)
                + powerPlant.getTonsRequired(totalTonnage)
                + lifeSupport.getTonsRequired(totalTonnage)
                + computer.getTonsRequired(totalTonnage);
    }

    /**
     * Calculates the total tons consumed by weapons.
     *
     * @return tons consumed by all weapons
     */
    public int getWeaponTons() {
        return weapons.stream().mapToInt(ShipWeaponSystem::getTotalTons).sum();
    }

    /**
     * Calculates the tons consumed by passengers.
     *
     * @return tons consumed by passengers
     */
    public int getPassengerTons() {
        return passengerClass.getTonsRequired(passengerCount);
    }

    /**
     * Calculates the total allocated tons (fixed systems + weapons + passengers + cargo + fuel).
     *
     * @return total allocated tons
     */
    public int getTotalAllocatedTons() {
        return getFixedSystemTons() + getWeaponTons() + getPassengerTons() + cargoTons + fuelTons;
    }

    /**
     * Returns the remaining unallocated tons.
     *
     * @return unallocated tons (total – allocated)
     */
    public int getRemainingTons() {
        return totalTonnage - getTotalAllocatedTons();
    }

    /**
     * Validates that the design does not exceed total tonnage.
     *
     * @return true if the design is valid (allocated ≤ total)
     */
    public boolean isValid() {
        return getTotalAllocatedTons() <= totalTonnage;
    }

    /**
     * Calculates the ship's hull points based on tonnage and hull type.
     *
     * @return total hull points
     */
    public int getHullPoints() {
        return (totalTonnage / 1000) * hullType.getHullPointsPer1000Tons();
    }

    /**
     * Calculates the estimated crew count.
     * Formula: (totalTonnage / 100) + drive crew + weapon crew + power plant crew.
     *
     * @return estimated crew count
     */
    public int getEstimatedCrew() {
        int base = totalTonnage / 100;
        int driveCrew = jumpDrive.getRating() + fusionDrive.getRating();
        int weaponCrew = weapons.size();
        int powerCrew = powerPlant.getCrewRequired();
        return base + driveCrew + weaponCrew + powerCrew;
    }

    /**
     * Calculates the ship's construction cost in credits.
     * Formula: hull cost + drive costs + system costs + weapon costs.
     *
     * @return construction cost in credits
     */
    public long getConstructionCost() {
        // Hull cost: tonnage × 100 × hull multiplier
        long hullCost = (long) ((long) totalTonnage * 100 * hullType.getCostMultiplier());

        // Jump drive: rating × tonnage × 50
        long jumpCost = (long) jumpDrive.getRating() * totalTonnage * 50;

        // Fusion drive: rating × tonnage × 10
        long fusionCost = (long) fusionDrive.getRating() * totalTonnage * 10;

        // Power plant: size × tonnage × 5
        long powerCost = (long) powerPlant.getSize() * totalTonnage * 5;

        // Life support: tons × 10
        long lifeSupportCost = (long) lifeSupport.getTonsRequired(totalTonnage) * 10;

        // Computer: class × tonnage × 2
        long computerCost = (long) computer.getClassLevel() * totalTonnage * 2;

        // Weapons: tons × 50
        long weaponCost = (long) getWeaponTons() * 50;

        return hullCost + jumpCost + fusionCost + powerCost + lifeSupportCost + computerCost + weaponCost;
    }

    /**
     * Calculates the annual maintenance cost in credits.
     * Formula: construction cost × 0.5%.
     *
     * @return annual maintenance cost in credits
     */
    public long getAnnualMaintenanceCost() {
        return getConstructionCost() / 200;
    }

    /**
     * Returns the maximum jump distance in parsecs.
     *
     * @return maximum jump distance
     */
    public int getMaxJumpParsecs() {
        return jumpDrive.getMaxJumpParsecs();
    }

    /**
     * Returns the maneuver rating.
     *
     * @return maneuver rating (0–9)
     */
    public int getManeuverRating() {
        return fusionDrive.getRating();
    }

    /**
     * Calculates the total average damage output of all weapons.
     *
     * @return average damage in megatons
     */
    public double getTotalAverageDamage() {
        return weapons.stream().mapToDouble(ShipWeaponSystem::getAverageDamage).sum();
    }

    /**
     * Returns the total number of D6 damage dice from all weapons.
     *
     * @return total damage dice
     */
    public int getTotalDamageDice() {
        return weapons.stream().mapToInt(ShipWeaponSystem::getDamageDice).sum();
    }

    /**
     * Returns the gunnery dice modifier from the ship's computer.
     *
     * @return gunnery bonus
     */
    public int getGunneryBonus() {
        return computer.getGunneryBonus();
    }

    // ---- Design validation ----

    /**
     * Validates the complete design and throws if invalid.
     *
     * @throws IllegalStateException if the design exceeds total tonnage
     */
    public void validate() {
        int allocated = getTotalAllocatedTons();
        if (allocated > totalTonnage) {
            throw new IllegalStateException(
                "Design exceeds total tonnage: " + allocated + " > " + totalTonnage +
                " (overflow: " + (allocated - totalTonnage) + " tons)"
            );
        }
        // Jump drive requires at least Class 0 computer
        if (jumpDrive.getRating() > 0 && computer == ShipComputer.NONE) {
            throw new IllegalStateException("Jump drive requires at least a Class 0 computer");
        }
        // Weapons require a power plant
        if (!weapons.isEmpty() && powerPlant == PowerPlant.NONE) {
            throw new IllegalStateException("Weapons require a power plant");
        }
        // Missiles require at least Class 2 computer
        if (weapons.stream().anyMatch(ShipWeaponSystem::isMissile) &&
            computer.getClassLevel() < 2) {
            throw new IllegalStateException("Missile targeting requires at least a Class 2 computer");
        }
    }

    @Override
    public String toString() {
        return String.format("ShipDesign{id=%d, name='%s', %d tons, J-%d, M-%d, %d HP, crew=%d, cost=%d cr}",
                id, name, totalTonnage, jumpDrive.getRating(), fusionDrive.getRating(),
                getHullPoints(), getEstimatedCrew(), getConstructionCost());
    }
}
