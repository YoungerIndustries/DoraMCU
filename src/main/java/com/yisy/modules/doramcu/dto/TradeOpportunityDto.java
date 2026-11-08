package com.yisy.modules.doramcu.dto;

/**
 * DTO for a trade opportunity at a specific world.
 * <p>
 * Shows commodity prices, market conditions, and available quantity for trading.
 * </p>
 */
public class TradeOpportunityDto {

    private String commodity;
    private int buyPrice;
    private int sellPrice;
    private int availableQuantity;
    private String marketCondition;
    private int worldId;
    private String worldName;

    public TradeOpportunityDto() {
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
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

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getMarketCondition() {
        return marketCondition;
    }

    public void setMarketCondition(String marketCondition) {
        this.marketCondition = marketCondition;
    }

    public int getWorldId() {
        return worldId;
    }

    public void setWorldId(int worldId) {
        this.worldId = worldId;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
}
