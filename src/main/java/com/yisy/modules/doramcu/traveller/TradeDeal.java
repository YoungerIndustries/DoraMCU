package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single trade transaction (buy or sell).
 * <p>
 * A trade deal records the commodity, quantity, price per unit,
 * total cost/revenue, and the world where the transaction occurred.
 * </p>
 * <p>
 * Trade deals are the atomic unit of trading in the system. A complete
 * trade mission consists of multiple trade deals: buying cargo at one
 * world and selling it at another.
 * </p>
 */
public class TradeDeal {

    private final Commodity commodity;
    private final int units;
    private final int pricePerUnit;
    private final long totalValue;
    private final TravellerWorld world;
    private final Type type;
    private final String notes;

    /**
     * Creates a new trade deal.
     *
     * @param type whether this is a buy or sell deal
     * @param commodity the commodity traded
     * @param units the number of units traded
     * @param pricePerUnit the price per unit in credits
     * @param world the world where the trade occurred
     */
    public TradeDeal(Type type, Commodity commodity, int units, int pricePerUnit, TravellerWorld world) {
        this(type, commodity, units, pricePerUnit, world, null);
    }

    /**
     * Creates a new trade deal with notes.
     *
     * @param type whether this is a buy or sell deal
     * @param commodity the commodity traded
     * @param units the number of units traded
     * @param pricePerUnit the price per unit in credits
     * @param world the world where the trade occurred
     * @param notes optional notes about the deal
     */
    public TradeDeal(Type type, Commodity commodity, int units, int pricePerUnit, TravellerWorld world, String notes) {
        if (units <= 0) {
            throw new IllegalArgumentException("Units must be positive");
        }
        if (pricePerUnit < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.type = type;
        this.commodity = commodity;
        this.units = units;
        this.pricePerUnit = pricePerUnit;
        this.totalValue = (long) units * pricePerUnit;
        this.world = world;
        this.notes = notes;
    }

    // ---- Accessors ----

    /**
     * Returns the type of this trade deal.
     *
     * @return BUY or SELL
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the commodity traded.
     *
     * @return the commodity
     */
    public Commodity getCommodity() {
        return commodity;
    }

    /**
     * Returns the number of units traded.
     *
     * @return the number of units
     */
    public int getUnits() {
        return units;
    }

    /**
     * Returns the price per unit in credits.
     *
     * @return price per unit
     */
    public int getPricePerUnit() {
        return pricePerUnit;
    }

    /**
     * Returns the total value of this deal in credits.
     * <p>
     * For BUY deals, this is the cost (money spent).
     * For SELL deals, this is the revenue (money earned).
     * </p>
     *
     * @return total value in credits
     */
    public long getTotalValue() {
        return totalValue;
    }

    /**
     * Returns the world where this trade occurred.
     *
     * @return the world
     */
    public TravellerWorld getWorld() {
        return world;
    }

    /**
     * Returns any notes attached to this deal.
     *
     * @return the notes, or null if none
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Returns the bulk weight of this deal in tons.
     *
     * @return tons consumed
     */
    public double getBulkTons() {
        return commodity.tonsForUnits(units);
    }

    // ---- Convenience Methods ----

    /**
     * Creates a buy deal from a world economy.
     *
     * @param economy the world's economy
     * @param commodity the commodity to buy
     * @param units the number of units to buy
     * @return the trade deal
     */
    public static TradeDeal createBuyDeal(WorldEconomy economy, Commodity commodity, int units) {
        int price = economy.getBuyPrice(commodity);
        if (price <= 0) {
            throw new IllegalArgumentException("Commodity not available for buying on " + economy.getWorld().getName());
        }
        return new TradeDeal(Type.BUY, commodity, units, price, economy.getWorld());
    }

    /**
     * Creates a sell deal from a world economy.
     *
     * @param economy the world's economy
     * @param commodity the commodity to sell
     * @param units the number of units to sell
     * @return the trade deal
     */
    public static TradeDeal createSellDeal(WorldEconomy economy, Commodity commodity, int units) {
        int price = economy.getSellPrice(commodity);
        if (price <= 0) {
            throw new IllegalArgumentException("Commodity not available for selling on " + economy.getWorld().getName());
        }
        return new TradeDeal(Type.SELL, commodity, units, price, economy.getWorld());
    }

    /**
     * Calculates the profit when pairing this deal with another.
     * <p>
     * If this is a BUY deal and the other is a SELL deal for the same
     * commodity, returns the profit (sell revenue - buy cost).
     * </p>
     *
     * @param other the other deal (should be the opposite type)
     * @return the profit in credits, or 0 if not a valid pair
     */
    public long calculateProfitWith(TradeDeal other) {
        if (this.commodity != other.commodity) {
            return 0;
        }
        if (this.type == Type.BUY && other.type == Type.SELL) {
            return other.totalValue - this.totalValue;
        }
        if (this.type == Type.SELL && other.type == Type.BUY) {
            return this.totalValue - other.totalValue;
        }
        return 0;
    }

    /**
     * Checks if this deal is profitable when paired with another.
     *
     * @param other the other deal
     * @return true if the pair generates positive profit
     */
    public boolean isProfitableWith(TradeDeal other) {
        return calculateProfitWith(other) > 0;
    }

    // ---- Type Enum ----

    /**
     * Type of trade transaction.
     */
    public enum Type {
        /**
         * Buying cargo (spending credits).
         */
        BUY,

        /**
         * Selling cargo (earning credits).
         */
        SELL;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(" ").append(units).append("x ")
          .append(commodity.getName()).append(" @ ").append(pricePerUnit)
          .append(" cr on ").append(world.getName());
        if (notes != null && !notes.isEmpty()) {
            sb.append(" (").append(notes).append(")");
        }
        return sb.toString();
    }
}
