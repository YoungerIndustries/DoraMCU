package com.yisy.modules.doramcu.hex;

import java.util.Objects;

/**
 * Represents a single component mounted in a ship's component slot.
 * <p>
 * Components are the building blocks of ship design. Each component occupies
 * one slot and contributes to the ship's combat capabilities. Components include
 * weapons (beam, missile, mass driver), shields (elerium-powered), and hull
 * reinforcement modules.
 * </p>
 * <p>
 * Shield components require Elerium, while weapon components require their
 * respective strategic resources as defined by {@link WeaponType}.
 * </p>
 */
public class ShipComponent {

    /**
     * Type of ship component.
     */
    public enum ComponentType {
        /**
         * A weapon component — deals damage to enemy ships.
         */
        WEAPON,

        /**
         * A shield component — absorbs incoming damage.
         * Requires Elerium strategic resource.
         */
        SHIELD,

        /**
         * A hull reinforcement component — increases ship durability.
         * Requires Durantium strategic resource.
         */
        HULL_REINFORCEMENT,

        /**
         * An MDrive (Maneuver Drive) component — enhances within-system movement.
         * MDrive is used for tactical positioning within a solar system and during combat.
         * Higher MDrive capacity allows faster maneuvering and repositioning.
         */
        MDRIVE,

        /**
         * A JDrive (Jump Drive) component — enables faster-than-light travel between systems.
         * JDrive is used for traveling along hyperlanes between solar systems.
         * Higher JDrive capacity allows longer jump ranges and faster inter-system travel.
         */
        JDRIVE
    }

    private final ComponentType type;
    private final WeaponType weaponType;
    private final int quantity;

    /**
     * Creates a weapon component.
     *
     * @param weaponType the type of weapon
     * @param quantity the number of this weapon in the slot (1-3)
     */
    public ShipComponent(WeaponType weaponType, int quantity) {
        if (weaponType == null) {
            throw new IllegalArgumentException("Weapon type must not be null");
        }
        if (quantity < 1 || quantity > 3) {
            throw new IllegalArgumentException("Quantity must be between 1 and 3: " + quantity);
        }
        this.type = ComponentType.WEAPON;
        this.weaponType = weaponType;
        this.quantity = quantity;
    }

    /**
     * Creates a shield component.
     *
     * @param quantity the number of shield generators (1-3)
     */
    public ShipComponent(int quantity) {
        this(ComponentType.SHIELD, null, quantity);
    }

    /**
     * Creates a hull reinforcement component.
     *
     * @param quantity the number of reinforcement layers (1-3)
     */
    public static ShipComponent hullReinforcement(int quantity) {
        return new ShipComponent(ComponentType.HULL_REINFORCEMENT, null, quantity);
    }

    /**
     * Creates an MDrive (Maneuver Drive) component.
     * <p>
     * MDrive enhances within-system movement speed for tactical positioning and combat.
     * </p>
     *
     * @param quantity the drive level (1-3), higher values provide more speed bonus
     * @return a new MDrive component
     */
    public static ShipComponent mDrive(int quantity) {
        return new ShipComponent(ComponentType.MDRIVE, null, quantity);
    }

    /**
     * Creates a JDrive (Jump Drive) component.
     * <p>
     * JDrive enables faster-than-light travel between solar systems via hyperlanes.
     * </p>
     *
     * @param quantity the drive level (1-3), higher values provide more jump range
     * @return a new JDrive component
     */
    public static ShipComponent jDrive(int quantity) {
        return new ShipComponent(ComponentType.JDRIVE, null, quantity);
    }

    private ShipComponent(ComponentType type, WeaponType weaponType, int quantity) {
        if (type == null) {
            throw new IllegalArgumentException("Component type must not be null");
        }
        if (quantity < 1 || quantity > 3) {
            throw new IllegalArgumentException("Quantity must be between 1 and 3: " + quantity);
        }
        this.type = type;
        this.weaponType = weaponType;
        this.quantity = quantity;
    }

    /**
     * Creates a shield component (convenience factory).
     *
     * @param quantity the number of shield generators (1-3)
     * @return a new shield component
     */
    public static ShipComponent shield(int quantity) {
        return new ShipComponent(quantity);
    }

    // ---- Accessors ----

    /**
     * Returns the type of this component.
     *
     * @return the component type
     */
    public ComponentType getType() {
        return type;
    }

    /**
     * Returns the weapon type, or null if this is not a weapon component.
     *
     * @return the weapon type, or null
     */
    public WeaponType getWeaponType() {
        return weaponType;
    }

    /**
     * Returns the quantity of this component (1-3).
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    // ---- Combat calculations ----

    /**
     * Returns the total damage output of this component.
     * <p>
     * For weapon components: base damage × quantity.
     * For non-weapon components: 0.
     * </p>
     *
     * @return the total damage value
     */
    public int getDamage() {
        if (type != ComponentType.WEAPON || weaponType == null) {
            return 0;
        }
        return weaponType.getBaseDamage() * quantity;
    }

    /**
     * Returns the shield strength provided by this component.
     * <p>
     * For shield components: 20 × quantity.
     * For non-shield components: 0.
     * </p>
     *
     * @return the shield strength value
     */
    public int getShieldStrength() {
        if (type != ComponentType.SHIELD) {
            return 0;
        }
        return 20 * quantity;
    }

    /**
     * Returns the hull bonus provided by this component.
     * <p>
     * For hull reinforcement components: 15 × quantity.
     * For non-hull components: 0.
     * </p>
     *
     * @return the hull bonus value
     */
    public int getHullBonus() {
        if (type != ComponentType.HULL_REINFORCEMENT) {
            return 0;
        }
        return 15 * quantity;
    }

    /**
     * Returns the MDrive speed bonus provided by this component.
     * <p>
     * For MDrive components: 2 × quantity (adds to base MDrive speed).
     * For non-MDrive components: 0.
     * </p>
     *
     * @return the MDrive speed bonus value
     */
    public int getMDriveBonus() {
        if (type != ComponentType.MDRIVE) {
            return 0;
        }
        return 2 * quantity;
    }

    /**
     * Returns the JDrive speed bonus provided by this component.
     * <p>
     * For JDrive components: 2 × quantity (adds to base JDrive speed).
     * For non-JDrive components: 0.
     * </p>
     *
     * @return the JDrive speed bonus value
     */
    public int getJDriveBonus() {
        if (type != ComponentType.JDRIVE) {
            return 0;
        }
        return 2 * quantity;
    }

    /**
     * Returns the strategic resource required by this component.
     * <p>
     * Weapons return their weapon type's required resource.
     * Shields require Elerium.
     * Hull reinforcement requires Durantium.
     * MDrive and JDrive require Elerium (power source for drive systems).
     * </p>
     *
     * @return the required strategic resource
     */
    public StrategicResource getRequiredResource() {
        switch (type) {
            case WEAPON:
                return weaponType.getRequiredResource();
            case SHIELD:
                return StrategicResource.ELERIUM;
            case HULL_REINFORCEMENT:
                return StrategicResource.DURANTIUM;
            case MDRIVE:
            case JDRIVE:
                return StrategicResource.ELERIUM;
            default:
                return null;
        }
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipComponent that = (ShipComponent) o;
        return type == that.type && quantity == that.quantity && Objects.equals(weaponType, that.weaponType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, weaponType, quantity);
    }

    @Override
    public String toString() {
        if (type == ComponentType.WEAPON) {
            return String.format("%s x%d (%s)", weaponType, quantity, getDamage());
        }
        if (type == ComponentType.SHIELD) {
            return String.format("Shield x%d (%d)", quantity, getShieldStrength());
        }
        if (type == ComponentType.HULL_REINFORCEMENT) {
            return String.format("Hull+ x%d (+%d)", quantity, getHullBonus());
        }
        if (type == ComponentType.MDRIVE) {
            return String.format("MDrive x%d (+%d)", quantity, getMDriveBonus());
        }
        if (type == ComponentType.JDRIVE) {
            return String.format("JDrive x%d (+%d)", quantity, getJDriveBonus());
        }
        return String.format("%s x%d", type, quantity);
    }
}
