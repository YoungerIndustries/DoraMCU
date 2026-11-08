package com.yisy.modules.doramcu.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for trade operations.
 * <p>
 * Contains the results of buying, selling, or executing a trade mission,
 * including prices, quantities, profit, and market information.
 * </p>
 */
public class TradeResponse {

    /**
     * Type of operation performed.
     */
    private String operation;

    /**
     * World name where the operation occurred.
     */
    private String worldName;

    /**
     * Commodity name.
     */
    private String commodity;

    /**
     * Quantity of units traded.
     */
    private int quantity;

    /**
     * Price per unit in credits.
     */
    private int pricePerUnit;

    /**
     * Total cost or revenue in credits.
     */
    private int totalCredits;

    /**
     * Profit (positive = gain, negative = loss).
     */
    private int profit;

    /**
     * Return on investment percentage.
     */
    private double roi;

    /**
     * Cargo remaining after the operation (tons).
     */
    private double cargoRemaining;

    /**
     * Cargo capacity of the ship (tons).
     */
    private double cargoCapacity;

    /**
     * Market prices for all commodities at this world.
     */
    private Map<String, Integer> marketPrices;

    /**
     * Trade deal details (for trade missions).
     */
    private List<TradeDealDetail> deals;

    /**
     * Message describing the result.
     */
    private String message;

    // ---- Default constructor for Jackson ----

    public TradeResponse() {
    }

    // ---- Accessors ----

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(int pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public int getProfit() {
        return profit;
    }

    public void setProfit(int profit) {
        this.profit = profit;
    }

    public double getRoi() {
        return roi;
    }

    public void setRoi(double roi) {
        this.roi = roi;
    }

    public double getCargoRemaining() {
        return cargoRemaining;
    }

    public void setCargoRemaining(double cargoRemaining) {
        this.cargoRemaining = cargoRemaining;
    }

    public double getCargoCapacity() {
        return cargoCapacity;
    }

    public void setCargoCapacity(double cargoCapacity) {
        this.cargoCapacity = cargoCapacity;
    }

    public Map<String, Integer> getMarketPrices() {
        return marketPrices;
    }

    public void setMarketPrices(Map<String, Integer> marketPrices) {
        this.marketPrices = marketPrices;
    }

    public List<TradeDealDetail> getDeals() {
        return deals;
    }

    public void setDeals(List<TradeDealDetail> deals) {
        this.deals = deals;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Detail of a single trade deal.
     */
    public static class TradeDealDetail {
        private String commodity;
        private int quantity;
        private int buyPrice;
        private int sellPrice;
        private int profit;
        private String buyWorld;
        private String sellWorld;

        public TradeDealDetail() {
        }

        public String getCommodity() {
            return commodity;
        }

        public void setCommodity(String commodity) {
            this.commodity = commodity;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getBuyPrice() {
            return buyPrice;
        }

        public void setBuyPrice(int buyPrice) {
            this.buyPrice = buyPrice;
        }

        public int getSellPrice() {
            return sellPrice;
        }

        public void setSellPrice(int sellPrice) {
            this.sellPrice = sellPrice;
        }

        public int getProfit() {
            return profit;
        }

        public void setProfit(int profit) {
            this.profit = profit;
        }

        public String getBuyWorld() {
            return buyWorld;
        }

        public void setBuyWorld(String buyWorld) {
            this.buyWorld = buyWorld;
        }

        public String getSellWorld() {
            return sellWorld;
        }

        public void setSellWorld(String sellWorld) {
            this.sellWorld = sellWorld;
        }
    }
}
