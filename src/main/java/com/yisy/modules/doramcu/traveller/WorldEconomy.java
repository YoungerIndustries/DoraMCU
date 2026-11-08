package com.yisy.modules.doramcu.traveller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Complete economy profile for a single Traveller world.
 * <p>
 * Combines price level, market conditions, and trade code effects to provide
 * a unified view of the world's economic state. This is the primary interface
 * for trade operations on a world.
 * </p>
 * <p>
 * The economy profile determines:
 * </p>
 * <ul>
 *   <li>What commodities are available for buying and selling</li>
 *   <li>The actual prices for each commodity</li>
 *   <li>Trade restrictions based on law level and government</li>
 *   <li>Market size based on population and port wealth</li>
 * </ul>
 */
public class WorldEconomy {

    private final TravellerWorld world;
    private final PriceLevel priceLevel;
    private final MarketConditions marketConditions;
    private final int marketSize;
    private final boolean tradeRestricted;
    private final Set<Commodity> restrictedCommodities;
    private final Random random = new Random();

    /** B07 Actual Value table: 2D6 roll → percentage of base price. */
    private static final int[] ACTUAL_VALUE_TABLE = {
            0,   // index 0 (unused)
            0,   // index 1 (unused)
            40,  // 2 = 40%
            50,  // 3 = 50%
            60,  // 4 = 60%
            70,  // 5 = 70%
            80,  // 6 = 80%
            100, // 7 = 100%
            120, // 8 = 120%
            150, // 9 = 150%
            200, // 10 = 200%
            300, // 11 = 300%
            400  // 12 = 400%
    };

    /**
     * Creates an economy profile for the given world.
     *
     * @param world the world to create the economy for
     */
    public WorldEconomy(TravellerWorld world) {
        this.world = world;
        this.priceLevel = PriceLevel.fromWorld(world);
        this.marketConditions = new MarketConditions(world);
        this.marketSize = calculateMarketSize();
        this.tradeRestricted = checkTradeRestricted();
        this.restrictedCommodities = determineRestrictedCommodities();
    }

    /**
     * Creates an economy profile with custom market conditions.
     *
     * @param world the world
     * @param marketConditions custom market conditions
     */
    public WorldEconomy(TravellerWorld world, MarketConditions marketConditions) {
        this.world = world;
        this.priceLevel = PriceLevel.fromWorld(world);
        this.marketConditions = marketConditions;
        this.marketSize = calculateMarketSize();
        this.tradeRestricted = checkTradeRestricted();
        this.restrictedCommodities = determineRestrictedCommodities();
    }

    /**
     * Calculates the market size based on population and port wealth.
     * <p>
     * Market size determines the maximum quantity of a single commodity
     * that can be bought or sold in one transaction without affecting prices.
     * </p>
     *
     * @return market size in tons
     */
    private int calculateMarketSize() {
        long populationMillions = world.getPopulationMillions();
        int portWealth = world.getPortWealth();

        // Market size scales with population and wealth
        int baseSize = (int) (populationMillions / 100); // 1% of population in millions
        int wealthMultiplier = portWealth + 1;

        return Math.max(100, baseSize * wealthMultiplier);
    }

    /**
     * Checks if trade is restricted on this world.
     * <p>
     * Trade is restricted on worlds with high law levels or specific governments.
     * </p>
     *
     * @return true if trade is restricted
     */
    private boolean checkTradeRestricted() {
        UniversalWorldProfile uwp = world.getUwp();

        // High law levels restrict trade
        if (uwp.getLawLevel() >= 7) {
            return true;
        }

        // Anarchy worlds have unregulated but dangerous trade
        if (world.hasTradeCode(TradeCode.AN)) {
            return false; // No restrictions, but risky
        }

        return false;
    }

    /**
     * Determines which commodities are restricted on this world.
     *
     * @return set of restricted commodities
     */
    private Set<Commodity> determineRestrictedCommodities() {
        UniversalWorldProfile uwp = world.getUwp();
        return Collections.emptySet(); // Restrictions handled by market conditions
    }

    // ---- Accessors ----

    /**
     * Returns the world this economy belongs to.
     *
     * @return the world
     */
    public TravellerWorld getWorld() {
        return world;
    }

    /**
     * Returns the price level for this world.
     *
     * @return the price level
     */
    public PriceLevel getPriceLevel() {
        return priceLevel;
    }

    /**
     * Returns the market conditions for this world.
     *
     * @return the market conditions
     */
    public MarketConditions getMarketConditions() {
        return marketConditions;
    }

    /**
     * Returns the market size in tons.
     * <p>
     * This is the maximum quantity of a single commodity that can be
     * bought or sold without affecting prices.
     * </p>
     *
     * @return market size in tons
     */
    public int getMarketSize() {
        return marketSize;
    }

    /**
     * Checks if trade is restricted on this world.
     *
     * @return true if trade is restricted
     */
    public boolean isTradeRestricted() {
        return tradeRestricted;
    }

    // ---- Trading Operations ----

    /**
     * Checks if a commodity can be bought on this world.
     *
     * @param commodity the commodity
     * @return true if the commodity can be purchased
     */
    public boolean canBuy(Commodity commodity) {
        if (tradeRestricted && restrictedCommodities.contains(commodity)) {
            return false;
        }
        return marketConditions.canBuy(commodity);
    }

    /**
     * Checks if a commodity can be sold on this world.
     *
     * @param commodity the commodity
     * @return true if the commodity can be sold
     */
    public boolean canSell(Commodity commodity) {
        if (tradeRestricted && restrictedCommodities.contains(commodity)) {
            return false;
        }
        return marketConditions.canSell(commodity);
    }

    /**
     * Gets the buy price for a commodity on this world.
     * <p>
     * Buy price is what the trader pays to purchase the commodity.
     * Uses the B07 Actual Value table (2D6 roll) for price variation.
     * </p>
     *
     * @param commodity the commodity
     * @return the buy price in credits per unit, or 0 if unavailable
     */
    public int getBuyPrice(Commodity commodity) {
        return getBuyPrice(commodity, true);
    }

    /**
     * Gets the buy price for a commodity on this world.
     *
     * @param commodity the commodity
     * @param useActualValue if true, rolls 2D6 on the B07 Actual Value table
     * @return the buy price in credits per unit, or 0 if unavailable
     */
    public int getBuyPrice(Commodity commodity, boolean useActualValue) {
        int basePrice = marketConditions.getPrice(commodity, priceLevel);
        if (basePrice == 0) return 0;
        if (!useActualValue) return basePrice;
        // Roll 2D6 on the B07 Actual Value table
        int roll = roll2D6();
        int percentage = ACTUAL_VALUE_TABLE[roll];
        return (int) (basePrice * percentage / 100.0);
    }

    /**
     * Gets the sell price for a commodity on this world.
     * <p>
     * Sell price is what the trader receives when selling the commodity.
     * Sell price is typically lower than buy price to reflect merchant margins.
     * Uses the B07 Actual Value table (2D6 roll) for price variation.
     * </p>
     *
     * @param commodity the commodity
     * @return the sell price in credits per unit, or 0 if unavailable
     */
    public int getSellPrice(Commodity commodity) {
        return getSellPrice(commodity, true);
    }

    /**
     * Gets the sell price for a commodity on this world.
     *
     * @param commodity the commodity
     * @param useActualValue if true, rolls 2D6 on the B07 Actual Value table
     * @return the sell price in credits per unit, or 0 if unavailable
     */
    public int getSellPrice(Commodity commodity, boolean useActualValue) {
        int basePrice = getBuyPrice(commodity, useActualValue);
        if (basePrice == 0) return 0;
        // Sellers get 80% of the buy price (merchant margin)
        return (int) (basePrice * 0.8);
    }

    /**
     * Rolls two six-sided dice and returns the sum (2-12).
     *
     * @return the sum of two d6 rolls
     */
    int roll2D6() {
        return (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
    }

    /**
     * Calculates the maximum units of a commodity that can be traded.
     * <p>
     * Limited by market size and commodity bulk.
     * </p>
     *
     * @param commodity the commodity
     * @return maximum units that can be traded
     */
    public int getMaxTradeUnits(Commodity commodity) {
        if (!marketConditions.isAvailable(commodity)) {
            return 0;
        }
        return commodity.maxUnitsForCargo(marketSize);
    }

    /**
     * Returns all commodities available for buying on this world.
     *
     * @return set of buyable commodities
     */
    public Set<Commodity> getBuyableCommodities() {
        return marketConditions.getBuyableCommodities();
    }

    /**
     * Returns all commodities available for selling on this world.
     *
     * @return set of sellable commodities
     */
    public Set<Commodity> getSellableCommodities() {
        return marketConditions.getSellableCommodities();
    }

    /**
     * Returns a summary of all commodity prices on this world.
     *
     * @return map of commodity to price
     */
    public Map<Commodity, Integer> getAllPrices() {
        Map<Commodity, Integer> prices = new LinkedHashMap<>();
        for (Commodity commodity : Commodity.values()) {
            int price = getBuyPrice(commodity);
            if (price > 0) {
                prices.put(commodity, price);
            }
        }
        return Collections.unmodifiableMap(prices);
    }

    /**
     * Simulates market fluctuations over time.
     * Called between trade missions to simulate economic changes.
     */
    public void fluctuate() {
        marketConditions.fluctuate();
    }

    /**
     * Calculates the potential profit for trading a commodity
     * between this world and another world.
     *
     * @param commodity the commodity to trade
     * @param otherEconomy the other world's economy
     * @return positive if profitable to buy here and sell there,
     *         negative if profitable to buy there and sell here,
     *         zero if no profitable trade exists
     */
    public int calculateTradeProfit(Commodity commodity, WorldEconomy otherEconomy) {
        int buyHere = getBuyPrice(commodity);
        int sellThere = otherEconomy.getSellPrice(commodity);
        int buyThere = otherEconomy.getBuyPrice(commodity);
        int sellHere = getSellPrice(commodity);

        // Profit from buying here and selling there
        int profitBuyHere = (sellThere > 0 && buyHere > 0) ? (sellThere - buyHere) : Integer.MIN_VALUE;

        // Profit from buying there and selling here
        int profitBuyThere = (sellHere > 0 && buyThere > 0) ? (sellHere - buyThere) : Integer.MIN_VALUE;

        return Math.max(profitBuyHere, profitBuyThere);
    }

    /**
     * Finds the most profitable commodity to trade between this world and another.
     *
     * @param otherEconomy the other world's economy
     * @return the most profitable commodity, or null if no profitable trade exists
     */
    public Commodity findBestTradeCommodity(WorldEconomy otherEconomy) {
        Commodity best = null;
        int bestProfit = 0;

        for (Commodity commodity : Commodity.values()) {
            int profit = calculateTradeProfit(commodity, otherEconomy);
            if (profit > bestProfit) {
                bestProfit = profit;
                best = commodity;
            }
        }

        return best;
    }

    @Override
    public String toString() {
        return String.format("WorldEconomy{world=%s, priceLevel=%s, marketSize=%d tons}",
                world.getName(), priceLevel.getName(), marketSize);
    }
}
