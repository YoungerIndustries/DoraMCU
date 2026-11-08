package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages trading at a Traveller space station's market.
 * <p>
 * The station market provides access to local commodities at world prices.
 * It wraps the world's {@link WorldEconomy} and adds station-specific
 * features like transaction limits based on market size and transaction logging.
 * </p>
 * <p>
 * In Classic Traveller, the market size determines how much of a single
 * commodity can be bought or sold in one transaction without affecting
 * prices. Larger stations on wealthy worlds have bigger markets.
 * </p>
 */
public class StationMarket {

    private final SpaceStation station;
    private final WorldEconomy economy;
    private final int marketSizeTons;
    private final Map<Commodity, Integer> inventory;
    private final List<MarketTransaction> transactionLog;

    /**
     * Creates a new market for the given station and world.
     *
     * @param station the space station
     * @param world the world the station orbits
     */
    public StationMarket(SpaceStation station, TravellerWorld world) {
        this.station = station;
        this.economy = new WorldEconomy(world);
        this.marketSizeTons = economy.getMarketSize();
        this.inventory = new ConcurrentHashMap<>();
        this.transactionLog = new ArrayList<>();
    }

    // ---- Accessors ----

    /**
     * @return the space station this market belongs to
     */
    public SpaceStation getStation() {
        return station;
    }

    /**
     * @return the world economy this market is based on
     */
    public WorldEconomy getEconomy() {
        return economy;
    }

    /**
     * @return the market size in tons
     */
    public int getMarketSizeTons() {
        return marketSizeTons;
    }

    /**
     * Returns the station's current inventory of a commodity.
     * <p>
     * The station's inventory represents goods available for sale
     * that have been stocked at the station.
     * </p>
     *
     * @param commodity the commodity
     * @return the quantity in units
     */
    public int getInventory(Commodity commodity) {
        return inventory.getOrDefault(commodity, 0);
    }

    /**
     * Returns the transaction log.
     *
     * @return unmodifiable list of transactions
     */
    public List<MarketTransaction> getTransactionLog() {
        return Collections.unmodifiableList(transactionLog);
    }

    // ---- Trading Operations ----

    /**
     * Buys cargo from the station's market.
     * <p>
     * The buyer purchases commodities from the station at the world's
     * sell price. The quantity is limited by the market size.
     * </p>
     *
     * @param commodity the commodity to buy
     * @param units the number of units to buy
     * @return the cost in credits
     */
    public int buy(Commodity commodity, int units) {
        if (units <= 0) return 0;

        // Check availability
        if (!economy.canBuy(commodity)) {
            logTransaction(commodity, units, 0, MarketTransaction.Type.BUY, "Not available");
            return 0;
        }

        // Check market size limit
        int maxUnits = getMaxBuyable(commodity);
        if (units > maxUnits) {
            units = maxUnits;
        }

        int price = economy.getBuyPrice(commodity);
        int totalCost = price * units;

        // Add to station inventory (station sells, so it needs stock)
        // In this model, the station has infinite stock at world prices
        logTransaction(commodity, units, totalCost, MarketTransaction.Type.BUY, "Purchased");

        return totalCost;
    }

    /**
     * Sells cargo to the station's market.
     * <p>
     * The seller sells commodities to the station at the world's
     * buy price. The station adds the goods to its inventory.
     * </p>
     *
     * @param commodity the commodity to sell
     * @param units the number of units to sell
     * @return the revenue in credits
     */
    public int sell(Commodity commodity, int units) {
        if (units <= 0) return 0;

        // Check if commodity is acceptable
        if (!economy.canSell(commodity)) {
            logTransaction(commodity, units, 0, MarketTransaction.Type.SELL, "Not accepted");
            return 0;
        }

        // Check market size limit
        int maxUnits = getMaxSellable(commodity);
        if (units > maxUnits) {
            units = maxUnits;
        }

        int price = economy.getSellPrice(commodity);
        int totalRevenue = price * units;

        // Add to station inventory
        int current = inventory.getOrDefault(commodity, 0);
        inventory.put(commodity, current + units);

        logTransaction(commodity, units, totalRevenue, MarketTransaction.Type.SELL, "Sold to station");

        return totalRevenue;
    }

    /**
     * Restocks the station's inventory with a commodity.
     * <p>
     * Used when the station receives new shipments of goods.
     * </p>
     *
     * @param commodity the commodity to restock
     * @param units the number of units to add
     */
    public void restock(Commodity commodity, int units) {
        if (units <= 0) return;

        int current = inventory.getOrDefault(commodity, 0);
        inventory.put(commodity, current + units);
    }

    /**
     * Calculates the maximum number of units of a commodity
     * that can be bought in one transaction.
     *
     * @param commodity the commodity
     * @return the maximum units
     */
    public int getMaxBuyable(Commodity commodity) {
        if (commodity.getBulkTons() <= 0) {
            return 1000; // Zero-bulk items have arbitrary limit
        }
        return (int) (marketSizeTons / commodity.getBulkTons());
    }

    /**
     * Calculates the maximum number of units of a commodity
     * that can be sold in one transaction.
     *
     * @param commodity the commodity
     * @return the maximum units
     */
    public int getMaxSellable(Commodity commodity) {
        if (commodity.getBulkTons() <= 0) {
            return 1000; // Zero-bulk items have arbitrary limit
        }
        return (int) (marketSizeTons / commodity.getBulkTons());
    }

    /**
     * Finds the most profitable commodity to buy at this station
     * and sell at another station.
     *
     * @param destinationMarket the destination market to sell at
     * @return the most profitable commodity, or null if no profit possible
     */
    public Commodity findBestTrade(StationMarket destinationMarket) {
        Commodity best = null;
        int bestProfit = 0;

        for (Commodity commodity : Commodity.values()) {
            if (!economy.canBuy(commodity)) continue;
            if (!destinationMarket.economy.canSell(commodity)) continue;

            int buyPrice = economy.getBuyPrice(commodity);
            int sellPrice = destinationMarket.economy.getSellPrice(commodity);
            int profit = sellPrice - buyPrice;

            if (profit > bestProfit) {
                bestProfit = profit;
                best = commodity;
            }
        }

        return best;
    }

    /**
     * Calculates the potential profit for trading a commodity
     * between this station and another.
     *
     * @param commodity the commodity
     * @param destinationMarket the destination market
     * @return the profit per unit (positive = profit, negative = loss)
     */
    public int calculateProfit(Commodity commodity, StationMarket destinationMarket) {
        int buyPrice = economy.getBuyPrice(commodity);
        int sellPrice = destinationMarket.economy.getSellPrice(commodity);
        return sellPrice - buyPrice;
    }

    private void logTransaction(Commodity commodity, int units, int credits,
                                 MarketTransaction.Type type, String note) {
        transactionLog.add(new MarketTransaction(commodity, units, credits, type, note));
    }

    // ---- Inner Classes ----

    /**
     * A recorded market transaction.
     */
    public static class MarketTransaction {
        private final Commodity commodity;
        private final int units;
        private final int credits;
        private final Type type;
        private final String note;
        private final long timestamp;

        MarketTransaction(Commodity commodity, int units, int credits, Type type, String note) {
            this.commodity = commodity;
            this.units = units;
            this.credits = credits;
            this.type = type;
            this.note = note;
            this.timestamp = System.currentTimeMillis();
        }

        public Commodity getCommodity() {
            return commodity;
        }

        public int getUnits() {
            return units;
        }

        public int getCredits() {
            return credits;
        }

        public Type getType() {
            return type;
        }

        public String getNote() {
            return note;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return type + " " + units + "x " + commodity + " for " + credits + " credits — " + note;
        }

        /**
         * Type of market transaction.
         */
        public enum Type {
            /**
             * Buying from the station.
             */
            BUY,

            /**
             * Selling to the station.
             */
            SELL;
        }
    }
}
