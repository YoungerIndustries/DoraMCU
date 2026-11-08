package com.yisy.modules.doramcu.traveller;

/**
 * Price levels for Traveller worlds, affecting commodity prices.
 * <p>
 * Price levels range from Very Low (0) to Very High (8), with Normal (4)
 * as the baseline. The price level is derived from the world's population
 * and government type, modified by trade codes.
 * </p>
 * <p>
 * In Classic Traveller, price level determines the multiplier applied to
 * base commodity prices when buying or selling on a world.
 * </p>
 */
public enum PriceLevel {

    /**
     * Very Low prices — impoverished world, minimal commerce.
     * Price multiplier: 0.5× base price.
     */
    VERY_LOW(0, "Very Low", 0.5),

    /**
     * Low prices — developing world with limited wealth.
     * Price multiplier: 0.75× base price.
     */
    LOW(1, "Low", 0.75),

    /**
     * Below Normal prices — slightly below average economy.
     * Price multiplier: 0.85× base price.
     */
    BELOW_NORMAL(2, "Below Normal", 0.85),

    /**
     * Low Normal prices — near-average economy, slightly low.
     * Price multiplier: 0.95× base price.
     */
    LOW_NORMAL(3, "Low Normal", 0.95),

    /**
     * Normal prices — average economy, baseline for trade.
     * Price multiplier: 1.0× base price.
     */
    NORMAL(4, "Normal", 1.0),

    /**
     * High Normal prices — above-average economy.
     * Price multiplier: 1.1× base price.
     */
    HIGH_NORMAL(5, "High Normal", 1.1),

    /**
     * High prices — wealthy world with strong economy.
     * Price multiplier: 1.25× base price.
     */
    HIGH(6, "High", 1.25),

    /**
     * Very High prices — extremely wealthy world.
     * Price multiplier: 1.5× base price.
     */
    VERY_HIGH(7, "Very High", 1.5),

    /**
     * Astronomical prices — ultra-wealthy or isolated world.
     * Price multiplier: 2.0× base price.
     */
    ASTRONOMICAL(8, "Astronomical", 2.0);

    private final int code;
    private final String name;
    private final double priceMultiplier;

    PriceLevel(int code, String name, double priceMultiplier) {
        this.code = code;
        this.name = name;
        this.priceMultiplier = priceMultiplier;
    }

    /**
     * Returns the numeric code for this price level (0–8).
     *
     * @return the price level code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the human-readable name of this price level.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the price multiplier applied to base commodity prices.
     * <p>
     * A multiplier of 1.0 means base price, 0.5 means half price,
     * 2.0 means double price, etc.
     * </p>
     *
     * @return the price multiplier
     */
    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    /**
     * Calculates the adjusted price for a commodity on this world.
     *
     * @param basePrice the base price of the commodity
     * @return the adjusted price in credits
     */
    public int getAdjustedPrice(int basePrice) {
        return (int) Math.round(basePrice * priceMultiplier);
    }

    /**
     * Finds the price level with the given numeric code.
     *
     * @param code the price level code (0–8)
     * @return the matching price level, or NORMAL if out of range
     */
    public static PriceLevel fromCode(int code) {
        if (code < 0) return VERY_LOW;
        if (code > 8) return ASTRONOMICAL;
        return values()[code];
    }

    /**
     * Derives the price level from a world's UWP.
     * <p>
     * Formula: (Population + Government) / 2, clamped to 0–8.
     * This is the Classic Traveller price level derivation.
     * </p>
     *
     * @param population the UWP population code (0–9)
     * @param government the UWP government code (0–9)
     * @return the derived price level
     */
    public static PriceLevel fromUwp(int population, int government) {
        int code = (population + government) / 2;
        return fromCode(code);
    }

    /**
     * Derives the price level from a Traveller world.
     *
     * @param world the world to derive price level from
     * @return the derived price level
     */
    public static PriceLevel fromWorld(TravellerWorld world) {
        UniversalWorldProfile uwp = world.getUwp();
        int code = (uwp.getPopulation() + uwp.getGovernment()) / 2;

        // Modify by trade codes
        if (world.hasTradeCode(TradeCode.PO)) {
            code = Math.max(0, code - 1);
        }
        if (world.hasTradeCode(TradeCode.UR)) {
            code = Math.min(8, code + 1);
        }
        if (world.hasTradeCode(TradeCode.EC)) {
            code = Math.min(8, code + 1);
        }
        if (world.hasTradeCode(TradeCode.AN)) {
            code = Math.max(0, code - 1);
        }

        return fromCode(code);
    }

    /**
     * Compares price levels by code.
     *
     * @param other the other price level
     * @return negative if this is lower, 0 if equal, positive if higher
     */
    public int compareWith(PriceLevel other) {
        return Integer.compare(this.code, other.code);
    }

    @Override
    public String toString() {
        return name;
    }
}
