package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.Objects;

/**
 * Unified ship that bridges the DoraMCU hull module system with the Traveller
 * drive, fuel, and movement systems.
 * <p>
 * This class extends {@link Ship} to retain the DoraMCU 3D hull module grid
 * (10×10×10 M3space cells), component slots, and combat stats, while adding
 * Traveller-style capabilities:
 * </p>
 * <ul>
 *   <li><b>FusionDrive</b> — rating-based within-system maneuvering (0M–9M)</li>
 *   <li><b>JumpDrive</b> — rating-based FTL travel (J-1 to J-9)</li>
 *   <li><b>FuelTank</b> — tonnage-based fuel management for jumps and maneuvers</li>
 *   <li><b>ManeuverStatus</b> — detailed within-system burn tracking with velocity/acceleration</li>
 *   <li><b>JumpTravelStatus</b> — multi-leg FTL jump planning with fuel consumption</li>
 *   <li><b>HighGuardArmor</b> — armored hull protection (Titanium Steel, Crystaliron, Bonded Superdense)</li>
 * </ul>
 * <p>
 * Speed mapping between systems:
 * </p>
 * <ul>
 *   <li>MDrive speed = FusionDrive rating × 3 + component bonuses</li>
 *   <li>JDrive speed = JumpDrive rating × 2 + component bonuses</li>
 * </ul>
 * <p>
 * A UnifiedShip can be created from either path:
 * </p>
 * <ul>
 *   <li><b>DoraMCU path:</b> ShipType → base stats → components → add Traveller drives</li>
 *   <li><b>Traveller path:</b> HighGuardShipDesign → tonnage → drives → map to hull modules</li>
 * </ul>
 */
public class UnifiedShip extends Ship {

    // ---- Traveller drive ratings ----

    private FusionDrive fusionDrive;
    private JumpDrive jumpDrive;

    // ---- Fuel management ----

    private FuelTank fuelTank;

    // ---- Movement status ----

    private ManeuverStatus maneuverStatus;
    private JumpTravelStatus jumpTravelStatus;

    // ---- High Guard armor ----

    private HighGuardArmor armor;

    // ---- Tonnage (Traveller concept) ----

    private int totalTonnage;

    /**
     * Creates a UnifiedShip from a DoraMCU ShipType.
     * <p>
     * The ship starts with no Traveller drives (NONE) and default fuel tank.
     * Call {@link #setFusionDrive(FusionDrive)} and {@link #setJumpDrive(JumpDrive)}
     * to configure Traveller-style drives.
     * </p>
     *
     * @param id the unique ship identifier
     * @param shipType the DoraMCU ship classification
     */
    public UnifiedShip(int id, ShipType shipType) {
        super(id, shipType);
        this.fusionDrive = FusionDrive.NONE;
        this.jumpDrive = JumpDrive.NONE;
        this.armor = HighGuardArmor.NONE;
        this.totalTonnage = shipType.getCubicMeters();
        initializeFuelTank();
    }

    /**
     * Creates a UnifiedShip from a Traveller HighGuardShipDesign.
     * <p>
     * Maps the design's tonnage to a ShipType, creates hull modules,
     * and configures drives from the design's ratings.
     * </p>
     *
     * @param design the High Guard ship design
     */
    public UnifiedShip(HighGuardShipDesign design) {
        Objects.requireNonNull(design, "Design must not be null");
        this(1, mapTonnageToShipType(design.getTotalTonnage()));
        setName(design.getName());
        this.fusionDrive = FusionDrive.fromRating(design.getManeuverDrive());
        this.jumpDrive = JumpDrive.fromRating(design.getJumpDrive());
        this.armor = design.getArmor();
        this.totalTonnage = design.getTotalTonnage();

        // Map armor to hull shape and material
        applyArmorToHull();

        // Reinitialize fuel tank with correct drives
        initializeFuelTank();

        // Initialize movement status with a default world
        initializeMovementStatus();
    }

    // ---- Initialization ----

    private void initializeFuelTank() {
        if (fusionDrive != FusionDrive.NONE) {
            this.fuelTank = new FuelTank(fusionDrive, totalTonnage);
        } else {
            // Default fuel tank for ships without fusion drive
            this.fuelTank = new FuelTank(1000, 1000, 1000);
        }
    }

    private void initializeMovementStatus() {
        // Create a default TravellerWorld for movement status
        TravellerWorld defaultWorld = new TravellerWorld(
                getId(), "Unknown System",
                UniversalWorldProfile.parse("05555500"),
                0, 0, false, false);

        if (fuelTank != null) {
            this.maneuverStatus = new ManeuverStatus(
                    defaultWorld,
                    fusionDrive.getRating(),
                    totalTonnage,
                    fuelTank,
                    1.0);

            this.jumpTravelStatus = new JumpTravelStatus(
                    defaultWorld,
                    fuelTank,
                    jumpDrive.getRating());
        }
    }

    private void applyArmorToHull() {
        if (armor == null || armor == HighGuardArmor.NONE) {
            setHullShape(HullShape.CUBE);
            setExteriorMaterial(Material.Aluminum);
            return;
        }

        // Map armor material to hull shape and exterior material
        HighGuardArmor.ArmorMaterial material = armor.getMaterial();
        switch (material) {
            case TITANIUM_STEEL:
                setHullShape(HullShape.CUBE);
                setExteriorMaterial(Material.Titanium);
                break;
            case CRYSTALIRON:
                setHullShape(HullShape.DOME);
                setExteriorMaterial(Material.Crystaliron);
                break;
            case BONDED_SUPERDENSE:
                setHullShape(HullShape.DOME);
                setExteriorMaterial(Material.Superdense);
                break;
            default:
                setHullShape(HullShape.CUBE);
                setExteriorMaterial(Material.Aluminum);
                break;
        }
    }

    /**
     * Maps a Traveller tonnage value to the closest DoraMCU ShipType.
     */
    private static ShipType mapTonnageToShipType(int tonnage) {
        if (tonnage <= 1000) return ShipType.FRIGATE;
        if (tonnage <= 2000) return ShipType.DESTROYER;
        if (tonnage <= 12000) return ShipType.CRUISER;
        if (tonnage <= 27000) return ShipType.BATTLESHIP;
        if (tonnage <= 64000) return ShipType.CARRIER;
        return ShipType.MOTHERSHIP;
    }

    // ---- Drive Accessors ----

    /**
     * Returns the FusionDrive rating for within-system maneuvering.
     *
     * @return the fusion drive
     */
    public FusionDrive getFusionDrive() {
        return fusionDrive;
    }

    /**
     * Sets the FusionDrive rating and recalculates fuel tank capacity.
     *
     * @param fusionDrive the new fusion drive
     */
    public void setFusionDrive(FusionDrive fusionDrive) {
        this.fusionDrive = Objects.requireNonNull(fusionDrive, "Fusion drive must not be null");
        initializeFuelTank();
        if (maneuverStatus != null) {
            // Update maneuver status with new rating
            this.maneuverStatus = new ManeuverStatus(
                    maneuverStatus.getStarSystem(),
                    fusionDrive.getRating(),
                    totalTonnage,
                    fuelTank,
                    maneuverStatus.getDistanceFromStar());
        }
    }

    /**
     * Returns the JumpDrive rating for FTL travel.
     *
     * @return the jump drive
     */
    public JumpDrive getJumpDrive() {
        return jumpDrive;
    }

    /**
     * Sets the JumpDrive rating.
     *
     * @param jumpDrive the new jump drive
     */
    public void setJumpDrive(JumpDrive jumpDrive) {
        this.jumpDrive = Objects.requireNonNull(jumpDrive, "Jump drive must not be null");
        if (jumpTravelStatus != null) {
            // Recreate jump travel status with new rating
            TravellerWorld world = jumpTravelStatus.getCurrentWorld();
            this.jumpTravelStatus = new JumpTravelStatus(
                    world,
                    fuelTank,
                    jumpDrive.getRating());
        }
    }

    // ---- Fuel Accessors ----

    /**
     * Returns the ship's fuel tank.
     *
     * @return the fuel tank, or null if not initialized
     */
    public FuelTank getFuelTank() {
        return fuelTank;
    }

    /**
     * Returns the current fuel level in tons.
     *
     * @return current fuel tons
     */
    public int getCurrentFuelTons() {
        return fuelTank != null ? fuelTank.getCurrentFuelTons() : 0;
    }

    /**
     * Returns the maximum fuel capacity in tons.
     *
     * @return max fuel capacity
     */
    public int getMaxFuelCapacity() {
        return fuelTank != null ? fuelTank.getMaxCapacityTons() : 0;
    }

    /**
     * Returns the number of jumps possible with current fuel.
     *
     * @return jumps remaining
     */
    public int getJumpsRemaining() {
        return fuelTank != null ? fuelTank.getJumpsRemaining() : 0;
    }

    /**
     * Refuels the ship.
     *
     * @param tons the amount of fuel to add
     * @return the actual amount added
     */
    public int refuel(int tons) {
        if (fuelTank == null) return 0;
        return fuelTank.refuel(tons);
    }

    /**
     * Refuels the ship to full capacity.
     *
     * @return the amount of fuel added
     */
    public int refuelToFull() {
        if (fuelTank == null) return 0;
        return fuelTank.refuelToFull();
    }

    // ---- Maneuver Status Accessors ----

    /**
     * Returns the within-system maneuver status.
     *
     * @return the maneuver status, or null if not initialized
     */
    public ManeuverStatus getManeuverStatus() {
        return maneuverStatus;
    }

    /**
     * Sets the current star system for maneuver tracking.
     *
     * @param world the current star system
     */
    public void setCurrentSystem(TravellerWorld world) {
        Objects.requireNonNull(world, "World must not be null");
        if (maneuverStatus != null) {
            this.maneuverStatus = new ManeuverStatus(
                    world,
                    fusionDrive.getRating(),
                    totalTonnage,
                    fuelTank,
                    1.0);
        }
        if (jumpTravelStatus != null) {
            this.jumpTravelStatus = new JumpTravelStatus(
                    world,
                    fuelTank,
                    jumpDrive.getRating());
        }
    }

    /**
     * Starts a new maneuver plan within the current system.
     *
     * @param plan the maneuver plan to execute
     * @return true if the plan was started
     */
    public boolean startManeuver(ManeuverPlan plan) {
        return maneuverStatus != null && maneuverStatus.startManeuver(plan);
    }

    /**
     * Executes the next burn in the current maneuver plan.
     *
     * @return true if a burn was executed
     */
    public boolean executeNextBurn() {
        return maneuverStatus != null && maneuverStatus.executeNextBurn();
    }

    /**
     * Aborts the current maneuver plan.
     */
    public void abortManeuver() {
        if (maneuverStatus != null) {
            maneuverStatus.abortManeuver();
        }
    }

    /**
     * Enters orbit around a celestial body.
     *
     * @param body the name of the celestial body
     */
    public void enterOrbit(String body) {
        if (maneuverStatus != null) {
            maneuverStatus.enterOrbit(body);
        }
    }

    /**
     * Lands on a celestial body's surface.
     *
     * @param body the name of the celestial body
     */
    public void land(String body) {
        if (maneuverStatus != null) {
            maneuverStatus.land(body);
        }
    }

    /**
     * Takes off from the current location.
     */
    public void takeOff() {
        if (maneuverStatus != null) {
            maneuverStatus.takeOff();
        }
    }

    // ---- Jump Travel Accessors ----

    /**
     * Returns the inter-system jump travel status.
     *
     * @return the jump travel status, or null if not initialized
     */
    public JumpTravelStatus getJumpTravelStatus() {
        return jumpTravelStatus;
    }

    /**
     * Starts a new jump plan.
     *
     * @param plan the jump plan to execute
     * @return true if the plan was started
     */
    public boolean startJumpPlan(JumpPlan plan) {
        return jumpTravelStatus != null && jumpTravelStatus.startJumpPlan(plan);
    }

    /**
     * Executes the next jump in the current plan.
     *
     * @return true if a jump was executed
     */
    public boolean executeNextJump() {
        return jumpTravelStatus != null && jumpTravelStatus.executeNextJump();
    }

    /**
     * Aborts the current jump plan.
     */
    public void abortJumpPlan() {
        if (jumpTravelStatus != null) {
            jumpTravelStatus.abortJumpPlan();
        }
    }

    /**
     * Returns the current star system.
     *
     * @return the current world
     */
    public TravellerWorld getCurrentWorld() {
        return jumpTravelStatus != null ? jumpTravelStatus.getCurrentWorld() : null;
    }

    /**
     * Returns the list of visited worlds.
     *
     * @return immutable list of visited worlds
     */
    public java.util.List<TravellerWorld> getVisitedWorlds() {
        return jumpTravelStatus != null ? jumpTravelStatus.getVisitedWorlds() : java.util.Collections.emptyList();
    }

    // ---- Armor Accessors ----

    /**
     * Returns the High Guard armor type.
     *
     * @return the armor type
     */
    public HighGuardArmor getArmor() {
        return armor;
    }

    /**
     * Sets the armor type and updates hull shape/material accordingly.
     *
     * @param armor the new armor type
     */
    public void setArmor(HighGuardArmor armor) {
        this.armor = Objects.requireNonNull(armor, "Armor must not be null");
        applyArmorToHull();
    }

    /**
     * Returns the armor protection value.
     *
     * @return the protection value (0 for no armor)
     */
    public int getArmorProtection() {
        return armor != null ? armor.getProtection() : 0;
    }

    // ---- Tonnage Accessors ----

    /**
     * Returns the total ship tonnage.
     *
     * @return the total tonnage in tons
     */
    public int getTotalTonnage() {
        return totalTonnage;
    }

    /**
     * Sets the total ship tonnage and recalculates fuel tank.
     *
     * @param totalTonnage the new total tonnage
     */
    public void setTotalTonnage(int totalTonnage) {
        if (totalTonnage <= 0) {
            throw new IllegalArgumentException("Tonnage must be positive");
        }
        this.totalTonnage = totalTonnage;
        initializeFuelTank();
    }

    // ---- Unified Speed Calculations ----

    /**
     * Returns the unified MDrive speed that combines:
     * <ul>
     *   <li>DoraMCU ShipType base MDrive speed</li>
     *   <li>Component MDrive bonuses</li>
     *   <li>FusionDrive rating bonus (rating × 3)</li>
     * </ul>
     *
     * @return the unified MDrive speed
     */
    @Override
    public int getMDriveSpeed() {
        int baseSpeed = super.getMDriveSpeed();
        int fusionBonus = fusionDrive.getRating() * 3;
        return baseSpeed + fusionBonus;
    }

    /**
     * Returns the unified JDrive speed that combines:
     * <ul>
     *   <li>DoraMCU ShipType base JDrive speed</li>
     *   <li>Component JDrive bonuses</li>
     *   <li>JumpDrive rating bonus (rating × 2)</li>
     * </ul>
     *
     * @return the unified JDrive speed
     */
    @Override
    public int getJDriveSpeed() {
        int baseSpeed = super.getJDriveSpeed();
        int jumpBonus = jumpDrive.getRating() * 2;
        return baseSpeed + jumpBonus;
    }

    /**
     * Returns the effective MDrive speed, reduced by damage and fuel status.
     * <p>
     * If the ship has no fuel, MDrive speed is halved.
     * </p>
     *
     * @return the effective MDrive speed
     */
    @Override
    public int getEffectiveMDriveSpeed() {
        int speed = super.getEffectiveMDriveSpeed();

        // Reduce speed if fuel is low
        if (fuelTank != null && fuelTank.isEmpty()) {
            speed = Math.max(1, speed / 2);
        }

        return speed;
    }

    /**
     * Returns the effective JDrive speed, reduced by damage and fuel status.
     * <p>
     * If the ship has no fuel, JDrive speed is 0 (cannot jump).
     * </p>
     *
     * @return the effective JDrive speed
     */
    @Override
    public int getEffectiveJDriveSpeed() {
        int speed = super.getEffectiveJDriveSpeed();

        // Cannot jump without fuel
        if (fuelTank != null && fuelTank.isEmpty()) {
            return 0;
        }

        return speed;
    }

    // ---- Drive Tonnage Calculations ----

    /**
     * Returns the tons consumed by the fusion drive.
     *
     * @return tons consumed by fusion drive
     */
    public int getFusionDriveTons() {
        return fusionDrive.getTonsRequired(totalTonnage);
    }

    /**
     * Returns the tons consumed by the jump drive.
     *
     * @return tons consumed by jump drive
     */
    public int getJumpDriveTons() {
        return jumpDrive.getTonsRequired(totalTonnage);
    }

    /**
     * Returns the total tons consumed by all drive systems.
     *
     * @return total drive tons
     */
    public int getTotalDriveTons() {
        return getFusionDriveTons() + getJumpDriveTons();
    }

    // ---- Status Checks ----

    /**
     * Returns true if the ship can perform within-system maneuvers.
     *
     * @return true if maneuvering is possible
     */
    public boolean canManeuver() {
        return fusionDrive != FusionDrive.NONE &&
                fuelTank != null && !fuelTank.isEmpty() &&
                maneuverStatus != null && maneuverStatus.canStartManeuver();
    }

    /**
     * Returns true if the ship can perform FTL jumps.
     *
     * @return true if jumping is possible
     */
    public boolean canJump() {
        return jumpDrive != JumpDrive.NONE &&
                fuelTank != null && !fuelTank.isEmpty() &&
                jumpTravelStatus != null && jumpTravelStatus.canStartJump();
    }

    /**
     * Returns true if the ship is stranded (no fuel and cannot move).
     *
     * @return true if stranded
     */
    public boolean isStranded() {
        if (fuelTank == null) return false;
        return fuelTank.isEmpty() &&
                (maneuverStatus == null || !maneuverStatus.isInMotion());
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return String.format(
                "UnifiedShip{name='%s', type=%s, tonnage=%d, hull=%d/%d, shields=%d/%d, " +
                        "fusion=%s, jump=%s, armor=%s, fuel=%s, damage=%d, mDrive=%d, jDrive=%d, " +
                        "evasion=%.0f%%, slots=%d/%d}",
                getName(), getShipType(), totalTonnage,
                getCurrentHullPoints(), getMaxHullPoints(),
                getCurrentShieldPoints(), getMaxShieldPoints(),
                fusionDrive, jumpDrive, armor,
                fuelTank != null ? fuelTank.toString() : "N/A",
                getTotalDamage(), getMDriveSpeed(), getJDriveSpeed(),
                getBaseEvasion() * 100,
                getOccupiedSlots(), getMaxComponentSlots());
    }
}
