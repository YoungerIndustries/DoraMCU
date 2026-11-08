package com.yisy.modules.doramcu.traveller;

/**
 * High Guard starship armor types from Classic Traveller Book 5: High Guard.
 * <p>
 * Unlike Book 2 (which has no armor), High Guard introduces three armor types
 * that provide protection against ship-to-ship weapons. Armor is measured in
 * "layers" (percent of hull volume) and provides a protection value that
 * reduces incoming damage.
 * </p>
 * <p>
 * Armor types and their properties:
 * </p>
 * <ul>
 *   <li><b>Titanium Steel</b> (TL7) — 2 protection per 5% layer, costs 5% of base hull per layer</li>
 *   <li><b>Crystaliron</b> (TL10) — 4 protection per 5% layer, costs 20% of base hull per layer</li>
 *   <li><b>Bonded Superdense</b> (TL14) — 6 protection per 5% layer, costs 50% of base hull per layer</li>
 * </ul>
 * <p>
 * Maximum armor is limited by Tech Level:
 * </p>
 * <ul>
 *   <li>Titanium Steel: TL or 9, whichever is less</li>
 *   <li>Crystaliron: TL or 13, whichever is less</li>
 *   <li>Bonded Superdense: TL (no cap beyond TL)</li>
 * </ul>
 * <p>
 * In combat, armor reduces the number of damage-causing hits.
 * Each point of armor absorbs one point of damage before it reaches the hull.
 * </p>
 */
public enum HighGuardArmor {

    /**
     * No armor — standard hull only.
     */
    NONE("None", ArmorMaterial.NONE, 0, 0, 0, 0, 0.0,
            "No armor — standard hull only"),

    // ---- Titanium Steel (TL7+) ----

    /**
     * Titanium Steel — 1 layer (5% of hull).
     * Protection: 2 points.
     */
    TITANIUM_STEEL_1("Titanium Steel x1", ArmorMaterial.TITANIUM_STEEL, 7, 1, 2, 5, 0.05,
            "Basic armor — 2 protection, 5% hull volume"),

    /**
     * Titanium Steel — 2 layers (10% of hull).
     * Protection: 4 points.
     */
    TITANIUM_STEEL_2("Titanium Steel x2", ArmorMaterial.TITANIUM_STEEL, 7, 2, 4, 10, 0.10,
            "Light armor — 4 protection, 10% hull volume"),

    /**
     * Titanium Steel — 3 layers (15% of hull).
     * Protection: 6 points.
     */
    TITANIUM_STEEL_3("Titanium Steel x3", ArmorMaterial.TITANIUM_STEEL, 7, 3, 6, 15, 0.15,
            "Medium armor — 6 protection, 15% hull volume"),

    /**
     * Titanium Steel — 4 layers (20% of hull).
     * Protection: 8 points.
     */
    TITANIUM_STEEL_4("Titanium Steel x4", ArmorMaterial.TITANIUM_STEEL, 7, 4, 8, 20, 0.20,
            "Heavy armor — 8 protection, 20% hull volume"),

    /**
     * Titanium Steel — 5 layers (25% of hull).
     * Protection: 10 points.
     */
    TITANIUM_STEEL_5("Titanium Steel x5", ArmorMaterial.TITANIUM_STEEL, 7, 5, 10, 25, 0.25,
            "Very heavy armor — 10 protection, 25% hull volume"),

    // ---- Crystaliron (TL10+) ----

    /**
     * Crystaliron — 1 layer (5% of hull).
     * Protection: 4 points.
     */
    CRYSTALIRON_1("Crystaliron x1", ArmorMaterial.CRYSTALIRON, 10, 1, 4, 5, 0.20,
            "Advanced armor — 4 protection, 20% hull volume"),

    /**
     * Crystaliron — 2 layers (10% of hull).
     * Protection: 8 points.
     */
    CRYSTALIRON_2("Crystaliron x2", ArmorMaterial.CRYSTALIRON, 10, 2, 8, 10, 0.20,
            "Strong armor — 8 protection, 20% hull volume"),

    /**
     * Crystaliron — 3 layers (15% of hull).
     * Protection: 12 points.
     */
    CRYSTALIRON_3("Crystaliron x3", ArmorMaterial.CRYSTALIRON, 10, 3, 12, 15, 0.30,
            "Heavy armor — 12 protection, 30% hull volume"),

    /**
     * Crystaliron — 4 layers (20% of hull).
     * Protection: 16 points.
     */
    CRYSTALIRON_4("Crystaliron x4", ArmorMaterial.CRYSTALIRON, 10, 4, 16, 20, 0.40,
            "Very heavy armor — 16 protection, 40% hull volume"),

    /**
     * Crystaliron — 5 layers (25% of hull).
     * Protection: 20 points.
     */
    CRYSTALIRON_5("Crystaliron x5", ArmorMaterial.CRYSTALIRON, 10, 5, 20, 25, 0.50,
            "Maximum crystaliron — 20 protection, 50% hull volume"),

    // ---- Bonded Superdense (TL14+) ----

    /**
     * Bonded Superdense — 1 layer (5% of hull).
     * Protection: 6 points.
     */
    BONDED_SUPERDENSE_1("Bonded Superdense x1", ArmorMaterial.BONDED_SUPERDENSE, 14, 1, 6, 5, 0.50,
            "Ultra armor — 6 protection, 50% hull volume"),

    /**
     * Bonded Superdense — 2 layers (10% of hull).
     * Protection: 12 points.
     */
    BONDED_SUPERDENSE_2("Bonded Superdense x2", ArmorMaterial.BONDED_SUPERDENSE, 14, 2, 12, 10, 0.50,
            "Ultra heavy armor — 12 protection, 50% hull volume"),

    /**
     * Bonded Superdense — 3 layers (15% of hull).
     * Protection: 18 points.
     */
    BONDED_SUPERDENSE_3("Bonded Superdense x3", ArmorMaterial.BONDED_SUPERDENSE, 14, 3, 18, 15, 0.75,
            "Maximum superdense — 18 protection, 75% hull volume");

    private final String name;
    private final ArmorMaterial material;
    private final int minTechLevel;
    private final int layers;
    private final int protection;
    private final int hullPercent;
    private final double costMultiplier;
    private final String description;

    HighGuardArmor(String name, ArmorMaterial material, int minTechLevel, int layers,
                   int protection, int hullPercent, double costMultiplier, String description) {
        this.name = name;
        this.material = material;
        this.minTechLevel = minTechLevel;
        this.layers = layers;
        this.protection = protection;
        this.hullPercent = hullPercent;
        this.costMultiplier = costMultiplier;
        this.description = description;
    }

    /**
     * Returns the display name of this armor configuration.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the material type of this armor.
     */
    public ArmorMaterial getMaterial() {
        return material;
    }

    /**
     * Returns the minimum Tech Level required to build this armor.
     */
    public int getMinTechLevel() {
        return minTechLevel;
    }

    /**
     * Returns the number of armor layers.
     * <p>
     * Each layer represents 5% of the ship's hull volume.
     * </p>
     */
    public int getLayers() {
        return layers;
    }

    /**
     * Returns the total protection value.
     * <p>
     * This value is subtracted from incoming damage before it reaches the hull.
     * For example, if a ship with protection 8 is hit for 12 damage,
     * only 4 damage reaches the hull.
     * </p>
     */
    public int getProtection() {
        return protection;
    }

    /**
     * Returns the percentage of hull volume consumed by this armor.
     */
    public int getHullPercent() {
        return hullPercent;
    }

    /**
     * Returns the tons of hull volume consumed by this armor for a given ship size.
     *
     * @param totalTonnage the ship's total tonnage
     * @return the tons consumed by armor
     */
    public int getTonsRequired(int totalTonnage) {
        return (int) (totalTonnage * costMultiplier);
    }

    /**
     * Returns the cost multiplier for this armor.
     * <p>
     * This is the fraction of the base hull cost added by the armor.
     * </p>
     */
    public double getCostMultiplier() {
        return costMultiplier;
    }

    /**
     * Returns the description of this armor configuration.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the maximum armor protection for a given Tech Level and material.
     *
     * @param material the armor material
     * @param techLevel the ship's Tech Level
     * @return the maximum protection value
     */
    public static int getMaxProtection(ArmorMaterial material, int techLevel) {
        switch (material) {
            case TITANIUM_STEEL:
                return Math.min(techLevel, 9);
            case CRYSTALIRON:
                return Math.min(techLevel, 13);
            case BONDED_SUPERDENSE:
                return techLevel;
            default:
                return 0;
        }
    }

    /**
     * Returns the best available armor for a given Tech Level.
     *
     * @param techLevel the ship's Tech Level
     * @return the best armor type available
     */
    public static HighGuardArmor bestForTechLevel(int techLevel) {
        if (techLevel >= 14) {
            return BONDED_SUPERDENSE_3;
        } else if (techLevel >= 10) {
            return CRYSTALIRON_5;
        } else if (techLevel >= 7) {
            return TITANIUM_STEEL_5;
        } else {
            return NONE;
        }
    }

    /**
     * Returns the tons of hull consumed by armor for a given Tech Level and desired protection.
     * <p>
     * High Guard armor allocation formula:
     * </p>
     * <ul>
     *   <li>TL7-9: 4 × (DesiredArmorFactor + 1)% of hull rate</li>
     *   <li>TL10-11: 3 × (DesiredArmorFactor + 1)% of hull rate</li>
     *   <li>TL12-13: 2 × (DesiredArmorFactor + 1)% of hull rate</li>
     *   <li>TL14-15: 1 × (DesiredArmorFactor + 1)% of hull rate</li>
     * </ul>
     *
     * @param totalTonnage the ship's total tonnage
     * @param desiredProtection the desired armor protection value
     * @param techLevel the ship's Tech Level
     * @return the tons consumed by armor
     */
    public static int calculateArmorTons(int totalTonnage, int desiredProtection, int techLevel) {
        if (desiredProtection <= 0) {
            return 0;
        }

        int multiplier;
        if (techLevel <= 9) {
            multiplier = 4;
        } else if (techLevel <= 11) {
            multiplier = 3;
        } else if (techLevel <= 13) {
            multiplier = 2;
        } else {
            multiplier = 1;
        }

        int percent = multiplier * (desiredProtection + 1);
        return (int) (totalTonnage * percent / 100.0);
    }

    /**
     * Returns the cost of armor in MCr per ton.
     * <p>
     * Cost = MCr(0.3 + (0.1 × DesiredArmorFactor)) per ton
     * </p>
     *
     * @param desiredProtection the desired armor protection value
     * @return the cost in MCr per ton
     */
    public static double calculateArmorCostPerTon(int desiredProtection) {
        if (desiredProtection <= 0) {
            return 0.0;
        }
        return 0.3 + (0.1 * desiredProtection);
    }

    /**
     * Types of armor materials in High Guard.
     */
    public enum ArmorMaterial {
        /**
         * No armor material.
         */
        NONE("None", 0, 0, 0.0),

        /**
         * Titanium Steel — TL7, 2 protection per 5% layer, costs 5% of base hull per layer.
         */
        TITANIUM_STEEL("Titanium Steel", 7, 2, 0.05),

        /**
         * Crystaliron — TL10, 4 protection per 5% layer, costs 20% of base hull per layer.
         */
        CRYSTALIRON("Crystaliron", 10, 4, 0.20),

        /**
         * Bonded Superdense — TL14, 6 protection per 5% layer, costs 50% of base hull per layer.
         */
        BONDED_SUPERDENSE("Bonded Superdense", 14, 6, 0.50);

        private final String name;
        private final int minTechLevel;
        private final int protectionPerLayer;
        private final double costPerLayer;

        ArmorMaterial(String name, int minTechLevel, int protectionPerLayer, double costPerLayer) {
            this.name = name;
            this.minTechLevel = minTechLevel;
            this.protectionPerLayer = protectionPerLayer;
            this.costPerLayer = costPerLayer;
        }

        public String getName() {
            return name;
        }

        public int getMinTechLevel() {
            return minTechLevel;
        }

        public int getProtectionPerLayer() {
            return protectionPerLayer;
        }

        public double getCostPerLayer() {
            return costPerLayer;
        }
    }
}
