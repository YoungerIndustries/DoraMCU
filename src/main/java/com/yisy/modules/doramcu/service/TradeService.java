package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.TradeRequest;
import com.yisy.modules.doramcu.dto.TradeResponse;
import com.yisy.modules.doramcu.traveller.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for trade operations.
 * <p>
 * Manages buying, selling, and trade missions between Traveller worlds.
 * </p>
 */
@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    private final ShipDesignService shipDesignService;
    private final Map<Integer, TravellerWorld> worlds = new HashMap<>();
    private final Map<Integer, CargoHold> cargoHolds = new HashMap<>();

    public TradeService(ShipDesignService shipDesignService) {
        this.shipDesignService = shipDesignService;
    }

    /**
     * Registers a world for trade operations.
     */
    public void registerWorld(TravellerWorld world) {
        worlds.put(world.getId(), world);
    }

    /**
     * Gets market prices for a world.
     */
    public TradeResponse getMarketPrices(int worldId) {
        TravellerWorld world = worlds.get(worldId);
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + worldId);
        }

        WorldEconomy economy = new WorldEconomy(world);
        TradeResponse response = new TradeResponse();
        response.setOperation("MARKET_PRICES");
        response.setWorldName(world.getName());

        Map<String, Integer> prices = new LinkedHashMap<>();
        for (Commodity commodity : Commodity.values()) {
            int price = economy.getBuyPrice(commodity);
            if (price > 0) {
                prices.put(commodity.getName(), price);
            }
        }
        response.setMarketPrices(prices);
        response.setMessage("Market prices for " + world.getName());

        return response;
    }

    /**
     * Buys cargo at a world.
     */
    public TradeResponse buyCargo(TradeRequest request) {
        TravellerWorld world = worlds.get(request.getWorldId());
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + request.getWorldId());
        }

        Commodity commodity = parseCommodity(request.getCommodity());
        if (commodity == null) {
            throw new IllegalArgumentException("Unknown commodity: " + request.getCommodity());
        }

        ShipDesign design = getShipDesign(request.getShipDesignId());
        CargoHold cargoHold = getCargoHold(request.getShipDesignId(), design);

        WorldEconomy economy = new WorldEconomy(world);
        int price = economy.getBuyPrice(commodity);

        // Calculate max quantity
        int maxUnits = cargoHold.getMaxLoadable(commodity);
        int quantity = Math.min(request.getQuantity(), maxUnits);
        if (quantity <= 0) {
            TradeResponse response = new TradeResponse();
            response.setOperation("BUY");
            response.setWorldName(world.getName());
            response.setCommodity(commodity.getName());
            response.setMessage("No cargo space available");
            return response;
        }

        int totalCost = price * quantity;
        int loaded = cargoHold.loadCargo(commodity, quantity);

        TradeResponse response = new TradeResponse();
        response.setOperation("BUY");
        response.setWorldName(world.getName());
        response.setCommodity(commodity.getName());
        response.setQuantity(loaded);
        response.setPricePerUnit(price);
        response.setTotalCredits(totalCost);
        response.setCargoRemaining(cargoHold.getRemainingCapacityTons());
        response.setCargoCapacity(cargoHold.getCapacityTons());
        response.setMessage("Bought " + loaded + " units of " + commodity +
                " at " + world.getName() + " for " + totalCost + " credits");

        return response;
    }

    /**
     * Sells cargo at a world.
     */
    public TradeResponse sellCargo(TradeRequest request) {
        TravellerWorld world = worlds.get(request.getWorldId());
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + request.getWorldId());
        }

        Commodity commodity = parseCommodity(request.getCommodity());
        if (commodity == null) {
            throw new IllegalArgumentException("Unknown commodity: " + request.getCommodity());
        }

        CargoHold cargoHold = cargoHolds.get(request.getShipDesignId());
        if (cargoHold == null) {
            throw new IllegalArgumentException("No cargo hold found for ship: " +
                    request.getShipDesignId());
        }

        int available = cargoHold.getQuantity(commodity);
        int quantity = Math.min(request.getQuantity(), available);
        if (quantity <= 0) {
            TradeResponse response = new TradeResponse();
            response.setOperation("SELL");
            response.setWorldName(world.getName());
            response.setCommodity(commodity.getName());
            response.setMessage("No " + commodity + " in cargo hold");
            return response;
        }

        WorldEconomy economy = new WorldEconomy(world);
        int price = economy.getSellPrice(commodity);
        int unloaded = cargoHold.unloadCargo(commodity, quantity);
        int totalRevenue = price * unloaded;

        TradeResponse response = new TradeResponse();
        response.setOperation("SELL");
        response.setWorldName(world.getName());
        response.setCommodity(commodity.getName());
        response.setQuantity(unloaded);
        response.setPricePerUnit(price);
        response.setTotalCredits(totalRevenue);
        response.setCargoRemaining(cargoHold.getRemainingCapacityTons());
        response.setCargoCapacity(cargoHold.getCapacityTons());
        response.setMessage("Sold " + unloaded + " units of " + commodity +
                " at " + world.getName() + " for " + totalRevenue + " credits");

        return response;
    }

    /**
     * Executes a trade mission between two worlds.
     */
    public TradeResponse executeTradeMission(TradeRequest request) {
        TravellerWorld sourceWorld = worlds.get(request.getWorldId());
        TravellerWorld destWorld = worlds.get(request.getDestinationWorldId());

        if (sourceWorld == null) {
            throw new IllegalArgumentException("Source world not found: " +
                    request.getWorldId());
        }
        if (destWorld == null) {
            throw new IllegalArgumentException("Destination world not found: " +
                    request.getDestinationWorldId());
        }

        ShipDesign design = getShipDesign(request.getShipDesignId());
        CargoHold cargoHold = getCargoHold(request.getShipDesignId(), design);

        WorldEconomy sourceEconomy = new WorldEconomy(sourceWorld);
        WorldEconomy destEconomy = new WorldEconomy(destWorld);

        // Find best commodity to trade
        Commodity bestCommodity = null;
        int bestProfit = 0;

        for (Commodity commodity : Commodity.values()) {
            if (!sourceEconomy.canBuy(commodity)) continue;
            if (!destEconomy.canSell(commodity)) continue;

            int buyPrice = sourceEconomy.getBuyPrice(commodity);
            int sellPrice = destEconomy.getSellPrice(commodity);
            int profit = sellPrice - buyPrice;

            if (profit > bestProfit) {
                bestProfit = profit;
                bestCommodity = commodity;
            }
        }

        if (bestCommodity == null || bestProfit <= 0) {
            TradeResponse response = new TradeResponse();
            response.setOperation("TRADE_MISSION");
            response.setMessage("No profitable trade found between " +
                    sourceWorld.getName() + " and " + destWorld.getName());
            return response;
        }

        // Buy at source
        int buyPrice = sourceEconomy.getBuyPrice(bestCommodity);
        int maxUnits = cargoHold.getMaxLoadable(bestCommodity);
        int bought = cargoHold.loadCargo(bestCommodity, maxUnits);
        int totalCost = buyPrice * bought;

        // Sell at destination
        int sellPrice = destEconomy.getSellPrice(bestCommodity);
        int sold = cargoHold.unloadAll(bestCommodity);
        int totalRevenue = sellPrice * sold;

        int profit = totalRevenue - totalCost;
        double roi = totalCost > 0 ? (double) profit / totalCost * 100.0 : 0.0;

        // Build response
        TradeResponse response = new TradeResponse();
        response.setOperation("TRADE_MISSION");
        response.setWorldName(sourceWorld.getName() + " -> " + destWorld.getName());
        response.setCommodity(bestCommodity.getName());
        response.setQuantity(sold);
        response.setProfit(profit);
        response.setRoi(roi);
        response.setCargoRemaining(cargoHold.getRemainingCapacityTons());
        response.setCargoCapacity(cargoHold.getCapacityTons());

        // Add deal details
        List<TradeResponse.TradeDealDetail> deals = new ArrayList<>();
        TradeResponse.TradeDealDetail detail = new TradeResponse.TradeDealDetail();
        detail.setCommodity(bestCommodity.getName());
        detail.setQuantity(sold);
        detail.setBuyPrice(buyPrice);
        detail.setSellPrice(sellPrice);
        detail.setProfit(profit);
        detail.setBuyWorld(sourceWorld.getName());
        detail.setSellWorld(destWorld.getName());
        deals.add(detail);
        response.setDeals(deals);

        response.setMessage("Trade mission: bought " + bought + " units of " +
                bestCommodity + " at " + sourceWorld.getName() + " for " + totalCost +
                " credits, sold at " + destWorld.getName() + " for " + totalRevenue +
                " credits. Profit: " + profit + " credits (" + String.format("%.1f", roi) + "% ROI)");

        return response;
    }

    /**
     * Lists all available commodities.
     */
    public List<String> getAvailableCommodities() {
        List<String> commodities = new ArrayList<>();
        for (Commodity c : Commodity.values()) {
            commodities.add(c.getName());
        }
        return commodities;
    }

    // ---- Private Helpers ----

    private Commodity parseCommodity(String name) {
        if (name == null) return null;
        for (Commodity c : Commodity.values()) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    private ShipDesign getShipDesign(int designId) {
        ShipDesign design = shipDesignService.getDesignDomain(designId);
        if (design != null) return design;
        // Fallback: create a basic 1000-ton merchant design
        ShipDesign fallback = new ShipDesign(designId, "Fallback Merchant", 1000);
        fallback.setCargoTons(200);
        return fallback;
    }

    private CargoHold getCargoHold(int shipDesignId, ShipDesign design) {
        CargoHold hold = cargoHolds.get(shipDesignId);
        if (hold == null) {
            hold = new CargoHold(design);
            cargoHolds.put(shipDesignId, hold);
        }
        return hold;
    }
}
