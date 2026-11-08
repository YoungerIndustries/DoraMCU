package com.yisy.modules.doramcu.traveller;

/**
 * Personal armor from Classic Traveller Book 4: Mercenary (1980) Ironmongery section.
 * <p>
 * Armor provides protection against small arms and shrapnel.
 * Each armor type has a protection value against different attack types
 * and a weight that affects movement.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum MercenaryArmor {

    /**
     * Combat Uniform — TL4, basic military uniform.
     * Minimal protection. Weight: 2kg.
     */
    COMBAT_UNIFORM("Combat Uniform", 4, 0, 2000, 20,
            "Basic military uniform with minimal protection"),

    /**
     * Ballistic Cloth Flak Jacket — TL7, flexible armor.
     * Protection against small arms. Weight: 5kg.
     */
    BALLISTIC_CLOTH("Ballistic Cloth Flak Jacket", 7, 2, 5000, 150,
            "Flexible ballistic cloth protection"),

    /**
     * Rigid Armor — TL8, hard plate armor.
     * Better protection but heavier. Weight: 10kg.
     */
    RIGID_ARMOR("Rigid Armor", 8, 4, 10000, 300,
            "Hard plate armor with better protection"),

    /**
     * Battle Dress — TL12, powered armor.
     * Requires Battle Dress skill. Strength enhancement. Weight: 50kg (effective: 10kg).
     */
    BATTLE_DRESS("Battle Dress", 12, 8, 50000, 5000,
            "Powered armor with strength enhancement, requires Battle Dress skill"),

    /**
     * Vacc Suit — TL8, vacuum protection suit.
     * Protection in vacuum and hostile environments. Weight: 15kg.
     */
    VACC_SUIT("Vacc Suit", 8, 1, 15000, 100,
            "Vacuum protection suit"),

    /**
     * Ceramic Armor — TL10, advanced ceramic plates.
     * Lightweight with good protection. Weight: 8kg.
     */
    CERAMIC_ARMOR("Ceramic Armor", 10, 6, 8000, 400,
            "Advanced ceramic plate armor"),

    /**
     * Composite Armor — TL11, layered composite materials.
     * Best non-powered armor. Weight: 12kg.
     */
    COMPOSITE_ARMOR("Composite Armor", 11, 7, 12000, 600,
            "Layered composite armor, best non-powered protection");

    private final String name;
    private final int techLevel;
    private final int protection;
    private final int weightGrams;
    private final int price;
    private final String description;

    MercenaryArmor(String name, int techLevel, int protection, int weightGrams,
                   int price, String description) {
        this.name = name;
        this.techLevel = techLevel;
        this.protection = protection;
        this.weightGrams = weightGrams;
        this.price = price;
        this.description = description;
    }

    /**
     * Returns the name of the armor.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the minimum tech level required.
     *
     * @return the tech level
     */
    public int getTechLevel() {
        return techLevel;
    }

    /**
     * Returns the protection value (damage reduction).
     *
     * @return the protection value
     */
    public int getProtection() {
        return protection;
    }

    /**
     * Returns the weight in grams.
     *
     * @return the weight in grams
     */
    public int getWeightGrams() {
        return weightGrams;
    }

    /**
     * Returns the base price in CR.
     *
     * @return the price
     */
    public int getPrice() {
        return price;
    }

    /**
     * Returns the description of the armor.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns true if this armor requires Battle Dress skill.
     *
     * @return true if battle dress required
     */
    public boolean requiresBattleDress() {
        return this == BATTLE_DRESS;
    }

    /**
     * Returns the effective weight in grams (accounting for powered armor).
     *
     * @return the effective weight
     */
    public int getEffectiveWeightGrams() {
        if (this == BATTLE_DRESS) {
            return 10000; // Strength enhancement reduces effective weight
        }
        return weightGrams;
    }
}
