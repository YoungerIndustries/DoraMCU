package com.yisy.modules.doramcu.traveller;

/**
 * Commodities available for trade in Classic Traveller.
 * <p>
 * Each commodity has a base price, bulk factor (tons per unit), and rarity
 * that determines availability on different types of worlds. Trade profits
 * come from buying low in one system and selling high in another.
 * </p>
 * <p>
 * Commodities are derived from Classic Traveller Book 1 (Merchant career)
 * and the Spinward Marches supplement trade rules.
 * </p>
 */
public enum Commodity {

    /**
     * Basic foodstuffs — grain, canned goods, etc.
     * High bulk, low value, always in demand.
     */
    FOOD("Food", 100, 10, Rarity.COMMON),

    /**
     * Medicines and pharmaceuticals.
     * Low bulk, high value, essential on frontier worlds.
     */
    MEDICINE("Medicine", 500, 1, Rarity.COMMON),

    /**
     * Precious gems and gemstones.
     * Very low bulk, very high value, rare.
     */
    GEMS("Gems", 5000, 0.1, Rarity.RARE),

    /**
     * Industrial metals and alloys.
     * High bulk, moderate value, needed for manufacturing.
     */
    METALS("Metals", 200, 5, Rarity.COMMON),

    /**
     * Textiles and fabrics.
     * Moderate bulk, moderate value.
     */
    TEXTILES("Textiles", 300, 2, Rarity.COMMON),

    /**
     * Chemicals for industrial and agricultural use.
     * Moderate bulk, moderate value.
     */
    CHEMICALS("Chemicals", 400, 3, Rarity.UNCOMMON),

    /**
     * Electronics and computer components.
     * Low bulk, high value, requires high technology level.
     */
    ELECTRONICS("Electronics", 2000, 0.5, Rarity.UNCOMMON),

    /**
     * Luxury items — art, jewelry, fine goods.
     * Low bulk, very high value, only on wealthy worlds.
     */
    LUXURY("Luxury", 10000, 0.2, Rarity.RARE),

    /**
     * Fuel and energy cells.
     * High bulk, moderate value, always needed.
     */
    FUEL("Fuel", 150, 8, Rarity.COMMON),

    /**
     * Machinery and industrial equipment.
     * Very high bulk, high value.
     */
    MACHINERY("Machinery", 3000, 20, Rarity.UNCOMMON),

    /**
     * Weapons and ammunition.
     * Low bulk, high value, restricted on some worlds.
     */
    WEAPONS("Weapons", 1500, 1, Rarity.UNCOMMON),

    /**
     * Slaves and indentured servants.
     * Moderate bulk, high value, illegal on many worlds.
     */
    SLAVES("Slaves", 8000, 2, Rarity.ILLEGAL),

    /**
     * Timber and wood products.
     * Very high bulk, low value, only on forested worlds.
     */
    TIMBER("Timber", 50, 15, Rarity.UNCOMMON),

    /**
     * Furs and hides.
     * Moderate bulk, moderate value, from cold worlds.
     */
    FURS("Furs", 600, 3, Rarity.UNCOMMON),

    /**
     * Spices and flavorings.
     * Very low bulk, high value, from agricultural worlds.
     */
    SPICES("Spices", 1000, 0.3, Rarity.UNCOMMON),

    /**
     * Alcohol and beverages.
     * Moderate bulk, moderate value.
     */
    ALCOHOL("Alcohol", 400, 2, Rarity.COMMON),

    /**
     * Tobacco and stimulants.
     * Low bulk, high value.
     */
    TOBACCO("Tobacco", 800, 0.5, Rarity.UNCOMMON),

    /**
     * Rare animals and biological specimens.
     * Moderate bulk, very high value, from unique worlds.
     */
    ANIMALS("Animals", 3000, 5, Rarity.RARE),

    /**
     * Information and data — intelligence, charts, secrets.
     * Zero bulk, variable value.
     */
    INFORMATION("Information", 1000, 0, Rarity.RARE),

    /**
     * Religious artifacts and relics.
     * Low bulk, high value, from religious worlds.
     */
    RELIGIOUS("Religious", 2000, 1, Rarity.RARE);

    private final String name;
    private final int basePrice;
    private final double bulkTons;
    private final Rarity rarity;

    Commodity(String name, int basePrice, double bulkTons, Rarity rarity) {
        this.name = name;
        this.basePrice = basePrice;
        this.bulkTons = bulkTons;
        this.rarity = rarity;
    }

    /**
     * Returns the human-readable name of this commodity.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the base price per unit in credits.
     * <p>
     * Actual price varies by world price level and market conditions.
     * </p>
     *
     * @return base price in credits
     */
    public int getBasePrice() {
        return basePrice;
    }

    /**
     * Returns the bulk factor in tons per unit.
     * <p>
     * Determines how many units fit in a given cargo capacity.
     * Zero bulk items (like information) have no weight limit.
     * </p>
     *
     * @return tons per unit
     */
    public double getBulkTons() {
        return bulkTons;
    }

    /**
     * Returns the rarity of this commodity.
     *
     * @return the rarity level
     */
    public Rarity getRarity() {
        return rarity;
    }

    /**
     * Calculates the number of units that fit in the given cargo tons.
     *
     * @param cargoTons available cargo capacity in tons
     * @return maximum units that can be carried
     */
    public int maxUnitsForCargo(int cargoTons) {
        if (bulkTons <= 0) {
            return 1000; // zero-bulk items have arbitrary high limit
        }
        return (int) (cargoTons / bulkTons);
    }

    /**
     * Calculates the tons required for the given number of units.
     *
     * @param units number of units
     * @return tons required
     */
    public double tonsForUnits(int units) {
        return units * bulkTons;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Rarity of a commodity, affecting availability on different worlds.
     */
    public enum Rarity {
        /**
         * Available on most worlds.
         */
        COMMON,

        /**
         * Available on many worlds, but not all.
         */
        UNCOMMON,

        /**
         * Only available on specific types of worlds.
         */
        RARE,

        /**
         * Illegal or restricted on most worlds.
         */
        ILLEGAL;
    }
}
