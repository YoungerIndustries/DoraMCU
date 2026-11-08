package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Tracks a ship's combat status during a Traveller ship-to-ship battle.
 * <p>
 * Unlike the design-time {@link ShipDesign}, this class represents a ship's
 * runtime combat state: which systems are damaged, how many hull points remain,
 * how many missile pods are left, and how many crew have been lost.
 * </p>
 * <p>
 * In Classic Traveller Book 2 combat, each hit on a ship targets a specific
 * hit location. If the location contains a critical system, that system goes
 * offline. If it contains cargo or passengers, the cargo/passengers are lost.
 * If it's empty space, the ship takes hull point damage.
 * </p>
 * <p>
 * A ship is considered destroyed when all hull points are lost or when
 * critical systems (life support, power plant) are completely offline.
 * </p>
 */
public class ShipCombatStatus {

    private final ShipDesign design;
    private final String name;
    private final int side; // 0 = attacker, 1 = defender

    // Hull and structural integrity
    private int hullPoints;
    private final int maxHullPoints;

    // System damage tracking — maps hit location to damage count
    private final Map<HitLocation, Integer> systemDamage;

    // Missile pods remaining
    private int missilePodsRemaining;
    private final int missilePodsTotal;

    // Crew casualties
    private int crewLost;
    private int passengersLost;

    // Ship status flags
    private boolean jumped;
    private boolean retreated;
    private boolean destroyed;

    // Current maneuver capability (can be reduced by fusion drive damage)
    private int currentManeuver;

    // Current radar range (can be reduced by computer damage)
    private int currentRadarRange;

    /**
     * Creates a new combat status for the given ship design.
     *
     * @param design the ship's design specifications
     * @param name the ship's call sign or name
     * @param side the combat side (0 = attacker, 1 = defender)
     */
    public ShipCombatStatus(ShipDesign design, String name, int side) {
        Objects.requireNonNull(design, "Design must not be null");
        this.design = design;
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.side = side;
        this.maxHullPoints = design.getHullPoints();
        this.hullPoints = maxHullPoints;
        this.systemDamage = new EnumMap<>(HitLocation.class);
        this.missilePodsTotal = calculateMissilePods();
        this.missilePodsRemaining = missilePodsTotal;
        this.crewLost = 0;
        this.passengersLost = 0;
        this.currentManeuver = design.getManeuverRating();
        this.currentRadarRange = calculateRadarRange();
        this.destroyed = false;
        this.jumped = false;
        this.retreated = false;
    }

    // ---- Accessors ----

    /** Returns the ship's design. */
    public ShipDesign getDesign() {
        return design;
    }

    /** Returns the ship's name. */
    public String getName() {
        return name;
    }

    /** Returns the combat side (0 = attacker, 1 = defender). */
    public int getSide() {
        return side;
    }

    /** Returns the current hull points. */
    public int getHullPoints() {
        return hullPoints;
    }

    /** Returns the maximum hull points. */
    public int getMaxHullPoints() {
        return maxHullPoints;
    }

    /** Returns the hull health ratio (0.0–1.0). */
    public double getHullHealthRatio() {
        if (maxHullPoints == 0) return 1.0;
        return (double) hullPoints / maxHullPoints;
    }

    /** Returns whether the ship is destroyed. */
    public boolean isDestroyed() {
        return destroyed;
    }

    /** Returns whether the ship has jumped. */
    public boolean hasJumped() {
        return jumped;
    }

    /** Returns whether the ship has retreated. */
    public boolean hasRetreated() {
        return retreated;
    }

    /** Returns the current maneuver capability. */
    public int getCurrentManeuver() {
        return currentManeuver;
    }

    /** Returns the current radar range in hexes. */
    public int getCurrentRadarRange() {
        return currentRadarRange;
    }

    /** Returns the number of missile pods remaining. */
    public int getMissilePodsRemaining() {
        return missilePodsRemaining;
    }

    /** Returns the total number of missile pods. */
    public int getMissilePodsTotal() {
        return missilePodsTotal;
    }

    /** Returns the number of crew lost. */
    public int getCrewLost() {
        return crewLost;
    }

    /** Returns the number of passengers lost. */
    public int getPassengersLost() {
        return passengersLost;
    }

    /** Returns the estimated crew count from design. */
    public int getEstimatedCrew() {
        return design.getEstimatedCrew();
    }

    /** Returns the remaining crew. */
    public int getRemainingCrew() {
        return design.getEstimatedCrew() - crewLost;
    }

    /** Returns the remaining passengers. */
    public int getRemainingPassengers() {
        return design.getPassengerCount() - passengersLost;
    }

    // ---- System damage ----

    /**
     * Returns the damage count for a specific hit location.
     *
     * @param location the hit location
     * @return the number of hits on this location
     */
    public int getSystemDamage(HitLocation location) {
        return systemDamage.getOrDefault(location, 0);
    }

    /**
     * Returns whether a system at the given location is offline.
     * A system goes offline after 1 hit on critical systems, 2 hits on others.
     *
     * @param location the hit location
     * @return true if the system is offline
     */
    public boolean isSystemOffline(HitLocation location) {
        int damage = getSystemDamage(location);
        // Critical systems go offline after 1 hit
        HitLocation.SystemType type = location.getDefaultSystem();
        if (type == HitLocation.SystemType.JUMP_DRIVE ||
            type == HitLocation.SystemType.FUSION_DRIVE ||
            type == HitLocation.SystemType.POWER_PLANT ||
            type == HitLocation.SystemType.LIFE_SUPPORT ||
            type == HitLocation.SystemType.COMPUTER) {
            return damage >= 1;
        }
        // Other systems need 2 hits to go offline
        return damage >= 2;
    }

    /**
     * Records a hit on the given location.
     *
     * @param location the hit location
     */
    public void recordHit(HitLocation location) {
        systemDamage.merge(location, 1, Integer::sum);
    }

    // ---- Damage application ----

    /**
     * Applies hull point damage.
     *
     * @param damage the amount of hull damage
     */
    public void applyHullDamage(int damage) {
        hullPoints = Math.max(0, hullPoints - damage);
        if (hullPoints <= 0) {
            destroyed = true;
        }
    }

    /**
     * Applies a missile hit, consuming one pod.
     *
     * @return true if a missile pod was available and fired
     */
    public boolean consumeMissilePod() {
        if (missilePodsRemaining <= 0) {
            return false;
        }
        missilePodsRemaining--;
        return true;
    }

    /**
     * Records crew casualties.
     *
     * @param casualties the number of crew lost
     */
    public void recordCrewCasualties(int casualties) {
        crewLost = Math.min(crewLost + casualties, design.getEstimatedCrew());
    }

    /**
     * Records passenger casualties.
     *
     * @param casualties the number of passengers lost
     */
    public void recordPassengerCasualties(int casualties) {
        passengersLost = Math.min(passengersLost + casualties, design.getPassengerCount());
    }

    /**
     * Reduces maneuver capability due to fusion drive damage.
     *
     * @param reduction the amount to reduce maneuver by
     */
    public void reduceManeuver(int reduction) {
        currentManeuver = Math.max(0, currentManeuver - reduction);
    }

    /**
     * Sets the ship as having jumped.
     */
    public void setJumped() {
        this.jumped = true;
    }

    /**
     * Sets the ship as having retreated.
     */
    public void setRetreated() {
        this.retreated = true;
    }

    // ---- Weapon availability ----

    /**
     * Returns the number of functional laser banks.
     * A laser bank is functional if its hit location is not offline.
     *
     * @return number of functional laser banks
     */
    public int getFunctionalLaserBanks() {
        int functional = 0;
        for (ShipWeaponSystem weapon : design.getWeapons()) {
            if (weapon.isLaser()) {
                HitLocation loc = HitLocation.fromLabel(weapon.getHitLocation());
                if (loc == null || !isSystemOffline(loc)) {
                    functional++;
                }
            }
        }
        return functional;
    }

    /**
     * Returns the number of functional missile launchers.
     *
     * @return number of functional missile launchers
     */
    public int getFunctionalMissileLaunchers() {
        int functional = 0;
        for (ShipWeaponSystem weapon : design.getWeapons()) {
            if (weapon.isMissile()) {
                HitLocation loc = HitLocation.fromLabel(weapon.getHitLocation());
                if (loc == null || !isSystemOffline(loc)) {
                    functional++;
                }
            }
        }
        return functional;
    }

    /**
     * Returns the total damage dice from functional lasers.
     *
     * @return total laser damage dice
     */
    public int getFunctionalLaserDamageDice() {
        int totalDice = 0;
        for (ShipWeaponSystem weapon : design.getWeapons()) {
            if (weapon.isLaser()) {
                HitLocation loc = HitLocation.fromLabel(weapon.getHitLocation());
                if (loc == null || !isSystemOffline(loc)) {
                    totalDice += weapon.getDamageDice();
                }
            }
        }
        return totalDice;
    }

    /**
     * Returns whether the jump drive is functional.
     *
     * @return true if jump drive can operate
     */
    public boolean isJumpDriveFunctional() {
        // Check if any jump drive location is offline
        for (Map.Entry<HitLocation, Integer> entry : systemDamage.entrySet()) {
            if (entry.getKey().getDefaultSystem() == HitLocation.SystemType.JUMP_DRIVE &&
                entry.getValue() >= 1) {
                return false;
            }
        }
        return design.getJumpDrive().getRating() > 0;
    }

    /**
     * Returns whether the fusion drive is functional.
     *
     * @return true if fusion drive can operate
     */
    public boolean isFusionDriveFunctional() {
        for (Map.Entry<HitLocation, Integer> entry : systemDamage.entrySet()) {
            if (entry.getKey().getDefaultSystem() == HitLocation.SystemType.FUSION_DRIVE &&
                entry.getValue() >= 1) {
                return false;
            }
        }
        return design.getFusionDrive().getRating() > 0;
    }

    /**
     * Returns whether the power plant is functional.
     *
     * @return true if power plant can operate
     */
    public boolean isPowerPlantFunctional() {
        for (Map.Entry<HitLocation, Integer> entry : systemDamage.entrySet()) {
            if (entry.getKey().getDefaultSystem() == HitLocation.SystemType.POWER_PLANT &&
                entry.getValue() >= 1) {
                return false;
            }
        }
        return design.getPowerPlant().getSize() > 0;
    }

    /**
     * Returns whether the computer is functional.
     *
     * @return true if computer can operate
     */
    public boolean isComputerFunctional() {
        for (Map.Entry<HitLocation, Integer> entry : systemDamage.entrySet()) {
            if (entry.getKey().getDefaultSystem() == HitLocation.SystemType.COMPUTER &&
                entry.getValue() >= 1) {
                return false;
            }
        }
        return design.getComputer().getClassLevel() > 0;
    }

    // ---- Internal calculations ----

    private int calculateMissilePods() {
        int pods = 0;
        for (ShipWeaponSystem weapon : design.getWeapons()) {
            if (weapon.isMissile()) {
                // Each missile launcher typically carries 6 pods
                pods += weapon.getQuantity() * 6;
            }
        }
        return pods;
    }

    private int calculateRadarRange() {
        // Radar range is based on computer class and ship tonnage
        // Class 0: 1 hex, Class 1: 2 hexes, Class 2: 3 hexes, Class 3: 4 hexes, Class 4: 5 hexes
        int baseRange = design.getComputer().getClassLevel() + 1;
        // Larger ships have better radar
        int tonnageBonus = design.getTotalTonnage() / 10000;
        return baseRange + tonnageBonus;
    }

    @Override
    public String toString() {
        return String.format("ShipCombatStatus{name='%s', side=%d, hull=%d/%d, maneuver=%d, " +
                        "radar=%d, missiles=%d/%d, crewLost=%d, destroyed=%s}",
                name, side, hullPoints, maxHullPoints, currentManeuver,
                currentRadarRange, missilePodsRemaining, missilePodsTotal,
                crewLost, destroyed);
    }
}
