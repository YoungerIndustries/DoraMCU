package com.yisy.modules.doramcu.hex;

/**
 * A module installed in a space station.
 * <p>
 * Modules provide combat capabilities, resource bonuses, and special abilities.
 * Each module occupies one slot and contributes specific stats.
 * </p>
 */
public class StationModule {

    /**
     * Type of station module.
     */
    public enum ModuleType {
        /** Weapon module — deals damage to enemy ships. */
        WEAPON,

        /** Shield module — boosts station shield strength. */
        SHIELD,

        /** Point defense module — reduces incoming missile damage. */
        POINT_DEFENSE,

        /** Targeting module — improves accuracy. */
        TARGETING,

        /** Hull reinforcement module — increases hull points. */
        HULL_REINFORCEMENT,

        /** Resource module — boosts resource production. */
        RESOURCE
    }

    private final ModuleType type;
    private final WeaponType weaponType;
    private final int quantity;
    private final int damage;
    private final int shieldStrength;
    private final double pointDefenseValue;
    private final double accuracyBonus;
    private final int hullBonus;

    /**
     * Creates a weapon module.
     *
     * @param weaponType the type of weapon
     * @param quantity the number of weapon batteries (1-3)
     */
    public StationModule(WeaponType weaponType, int quantity) {
        if (weaponType == null) {
            throw new IllegalArgumentException("Weapon type must not be null");
        }
        if (quantity < 1 || quantity > 3) {
            throw new IllegalArgumentException("Quantity must be between 1 and 3: " + quantity);
        }
        this.type = ModuleType.WEAPON;
        this.weaponType = weaponType;
        this.quantity = quantity;
        this.damage = weaponType.getBaseDamage() * quantity;
        this.shieldStrength = 0;
        this.pointDefenseValue = 0.0;
        this.accuracyBonus = 0.0;
        this.hullBonus = 0;
    }

    /**
     * Creates a shield module.
     *
     * @param quantity the number of shield generators (1-3)
     */
    public StationModule(int quantity) {
        this(ModuleType.SHIELD, null, quantity);
    }

    /**
     * Creates a point defense module.
     *
     * @param quantity the number of point defense batteries (1-3)
     */
    public static StationModule pointDefense(int quantity) {
        return new StationModule(ModuleType.POINT_DEFENSE, null, quantity);
    }

    /**
     * Creates a targeting module.
     *
     * @param quantity the number of targeting arrays (1-3)
     */
    public static StationModule targeting(int quantity) {
        return new StationModule(ModuleType.TARGETING, null, quantity);
    }

    /**
     * Creates a hull reinforcement module.
     *
     * @param quantity the number of reinforcement layers (1-3)
     */
    public static StationModule hullReinforcement(int quantity) {
        return new StationModule(ModuleType.HULL_REINFORCEMENT, null, quantity);
    }

    /**
     * Creates a resource module.
     *
     * @param quantity the number of resource extractors (1-3)
     */
    public static StationModule resource(int quantity) {
        return new StationModule(ModuleType.RESOURCE, null, quantity);
    }

    private StationModule(ModuleType type, WeaponType weaponType, int quantity) {
        if (type == null) {
            throw new IllegalArgumentException("Module type must not be null");
        }
        if (quantity < 1 || quantity > 3) {
            throw new IllegalArgumentException("Quantity must be between 1 and 3: " + quantity);
        }
        this.type = type;
        this.weaponType = weaponType;
        this.quantity = quantity;

        // Calculate stats based on module type
        switch (type) {
            case SHIELD:
                this.damage = 0;
                this.shieldStrength = 20 * quantity;
                this.pointDefenseValue = 0.0;
                this.accuracyBonus = 0.0;
                this.hullBonus = 0;
                break;
            case POINT_DEFENSE:
                this.damage = 0;
                this.shieldStrength = 0;
                this.pointDefenseValue = 0.15 * quantity;
                this.accuracyBonus = 0.0;
                this.hullBonus = 0;
                break;
            case TARGETING:
                this.damage = 0;
                this.shieldStrength = 0;
                this.pointDefenseValue = 0.0;
                this.accuracyBonus = 0.10 * quantity;
                this.hullBonus = 0;
                break;
            case HULL_REINFORCEMENT:
                this.damage = 0;
                this.shieldStrength = 0;
                this.pointDefenseValue = 0.0;
                this.accuracyBonus = 0.0;
                this.hullBonus = 50 * quantity;
                break;
            case RESOURCE:
                this.damage = 0;
                this.shieldStrength = 0;
                this.pointDefenseValue = 0.0;
                this.accuracyBonus = 0.0;
                this.hullBonus = 0;
                break;
            default:
                this.damage = 0;
                this.shieldStrength = 0;
                this.pointDefenseValue = 0.0;
                this.accuracyBonus = 0.0;
                this.hullBonus = 0;
                break;
        }
    }

    /**
     * @return the module type
     */
    public ModuleType getType() {
        return type;
    }

    /**
     * @return the weapon type, or null if not a weapon module
     */
    public WeaponType getWeaponType() {
        return weaponType;
    }

    /**
     * @return the quantity of this module (1-3)
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return the damage output (for weapon modules)
     */
    public int getDamage() {
        return damage;
    }

    /**
     * @return the shield strength bonus (for shield modules)
     */
    public int getShieldStrength() {
        return shieldStrength;
    }

    /**
     * @return the point defense value (for point defense modules)
     */
    public double getPointDefenseValue() {
        return pointDefenseValue;
    }

    /**
     * @return the accuracy bonus (for targeting modules)
     */
    public double getAccuracyBonus() {
        return accuracyBonus;
    }

    /**
     * @return the hull bonus (for hull reinforcement modules)
     */
    public int getHullBonus() {
        return hullBonus;
    }

    @Override
    public String toString() {
        return String.format("StationModule{type=%s, quantity=%d}", type, quantity);
    }
}
