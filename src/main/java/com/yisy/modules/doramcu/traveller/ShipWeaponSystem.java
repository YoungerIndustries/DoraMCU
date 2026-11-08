package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Weapon systems mounted on Traveller starships.
 * <p>
 * Implements the Classic Traveller Book 2 weapon system rules:
 * lasers and missiles, each occupying specific hit locations
 * on the ship's hex map.
 * </p>
 * <p>
 * Lasers are defined by their size (diameter in meters) and number.
 * Missiles are defined by their type and launchers.
 * </p>
 */
public class ShipWeaponSystem {

    /**
     * Types of ship-mounted weapons from Classic Traveller.
     */
    public enum WeaponType {

        /**
         * Small laser — 10–19 meters diameter.
         * Damage: 2D6 MD per hit.
         * Tonnage: 100 tons per laser.
         */
        SMALL_LASER(10, 19, 2, 100),

        /**
         * Medium laser — 20–29 meters diameter.
         * Damage: 4D6 MD per hit.
         * Tonnage: 200 tons per laser.
         */
        MEDIUM_LASER(20, 29, 4, 200),

        /**
         * Large laser — 30–39 meters diameter.
         * Damage: 6D6 MD per hit.
         * Tonnage: 300 tons per laser.
         */
        LARGE_LASER(30, 39, 6, 300),

        /**
         * Huge laser — 40+ meters diameter.
         * Damage: 8D6 MD per hit.
         * Tonnage: 400 tons per laser.
         */
        HUGE_LASER(40, 99, 8, 400),

        /**
         * Missile launcher — fires missile pods.
         * Damage depends on missile type (see MissileType).
         * Tonnage: 200 tons per launcher.
         */
        MISSILE_LAUNCHER(0, 0, 0, 200);

        private final int minDiameter;
        private final int maxDiameter;
        private final int damageDice;
        private final int tonsPerUnit;

        WeaponType(int minDiameter, int maxDiameter, int damageDice, int tonsPerUnit) {
            this.minDiameter = minDiameter;
            this.maxDiameter = maxDiameter;
            this.damageDice = damageDice;
            this.tonsPerUnit = tonsPerUnit;
        }

        /**
         * Returns the minimum laser diameter in meters (0 for missiles).
         */
        public int getMinDiameter() {
            return minDiameter;
        }

        /**
         * Returns the maximum laser diameter in meters (0 for missiles).
         */
        public int getMaxDiameter() {
            return maxDiameter;
        }

        /**
         * Returns the number of D6 dice for damage (0 for missiles).
         */
        public int getDamageDice() {
            return damageDice;
        }

        /**
         * Returns the tonnage cost per unit of this weapon.
         */
        public int getTonsPerUnit() {
            return tonsPerUnit;
        }

        /**
         * Returns whether this is a laser weapon.
         */
        public boolean isLaser() {
            return this != MISSILE_LAUNCHER;
        }

        /**
         * Returns whether this is a missile launcher.
         */
        public boolean isMissile() {
            return this == MISSILE_LAUNCHER;
        }
    }

    /**
     * Types of missiles from Classic Traveller.
     */
    public enum MissileType {

        /**
         * Standard warhead — 2D6 MD damage.
         */
        STANDARD(2),

        /**
         * Heavy warhead — 4D6 MD damage.
         */
        HEAVY(4),

        /**
         * Special warhead — 6D6 MD damage.
         */
        SPECIAL(6),

        /**
         * Nuclear warhead — 8D6 MD damage.
         */
        NUCLEAR(8);

        private final int damageDice;

        MissileType(int damageDice) {
            this.damageDice = damageDice;
        }

        /**
         * Returns the number of D6 dice for damage.
         */
        public int getDamageDice() {
            return damageDice;
        }
    }

    private final WeaponType weaponType;
    private final int quantity;
    private final MissileType missileType;
    private final String hitLocation;

    /**
     * Creates a laser weapon system.
     *
     * @param laserType the laser type (SMALL, MEDIUM, LARGE, HUGE)
     * @param quantity the number of lasers
     * @param hitLocation the hex location on the ship (e.g., "A1", "B2")
     */
    public ShipWeaponSystem(WeaponType laserType, int quantity, String hitLocation) {
        if (!laserType.isLaser()) {
            throw new IllegalArgumentException("Use missile constructor for missile launchers");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.weaponType = laserType;
        this.quantity = quantity;
        this.missileType = null;
        this.hitLocation = Objects.requireNonNull(hitLocation, "Hit location must not be null");
    }

    /**
     * Creates a missile launcher system.
     *
     * @param missileType the type of missiles fired
     * @param launchers the number of launchers
     * @param pods the number of missile pods (6 missiles per pod)
     * @param hitLocation the hex location on the ship
     */
    public ShipWeaponSystem(MissileType missileType, int launchers, int pods, String hitLocation) {
        if (launchers < 1) {
            throw new IllegalArgumentException("Launchers must be at least 1");
        }
        this.weaponType = WeaponType.MISSILE_LAUNCHER;
        this.quantity = launchers;
        this.missileType = Objects.requireNonNull(missileType, "Missile type must not be null");
        this.hitLocation = Objects.requireNonNull(hitLocation, "Hit location must not be null");
    }

    /**
     * Returns the weapon type.
     */
    public WeaponType getWeaponType() {
        return weaponType;
    }

    /**
     * Returns the quantity of weapons (lasers or launchers).
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the missile type, or null if this is a laser.
     */
    public MissileType getMissileType() {
        return missileType;
    }

    /**
     * Returns the hit location hex on the ship.
     */
    public String getHitLocation() {
        return hitLocation;
    }

    /**
     * Returns whether this is a laser weapon.
     */
    public boolean isLaser() {
        return weaponType.isLaser();
    }

    /**
     * Returns whether this is a missile launcher.
     */
    public boolean isMissile() {
        return weaponType.isMissile();
    }

    /**
     * Calculates the total tonnage for this weapon system.
     *
     * @return total tons consumed
     */
    public int getTotalTons() {
        return weaponType.getTonsPerUnit() * quantity;
    }

    /**
     * Calculates the average damage of this weapon system.
     * For lasers: damageDice × quantity × 3.5 (average of D6).
     * For missiles: missileType damageDice × quantity × 3.5.
     *
     * @return average damage in megatons
     */
    public double getAverageDamage() {
        int dice;
        if (isLaser()) {
            dice = weaponType.getDamageDice();
        } else if (missileType != null) {
            dice = missileType.getDamageDice();
        } else {
            return 0;
        }
        return dice * quantity * 3.5;
    }

    /**
     * Returns the number of D6 dice for a single attack.
     */
    public int getDamageDice() {
        if (isLaser()) {
            return weaponType.getDamageDice() * quantity;
        } else if (missileType != null) {
            return missileType.getDamageDice() * quantity;
        }
        return 0;
    }
}
