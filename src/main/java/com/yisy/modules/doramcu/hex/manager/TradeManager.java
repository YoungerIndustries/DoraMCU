package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.traveller.Commodity;
import com.yisy.modules.doramcu.traveller.TradeCode;
import com.yisy.modules.doramcu.traveller.TravellerWorld;
import com.yisy.modules.doramcu.traveller.WorldEconomy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Coordinates commodity trading between DoraMCU planets using Traveller trade mechanics.
 * <p>
 * The TradeManager bridges the DoraMCU {@link Planet} system with Traveller's
 * {@link WorldEconomy} and {@link MarketConditions} to enable interstellar trade.
 * Each planet with UWP (Universal World Profile) data gets an associated economy
 * that determines commodity availability and pricing.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Creates {@link WorldEconomy} for each planet with UWP data</li>
 *   <li>Calculates commodity prices based on world trade codes and supply/demand</li>
 *   <li>Finds profitable trade routes between owned planets</li>
 *   <li>Executes trade missions that transfer commodities and generate income</li>
 *   <li>Applies commander bonuses from Merchant career to trade profits</li>
 *   <li>Market fluctuation between turns</li>
 * </ul>
 * <p>
 * Trade profits are added to the player's resource pool and can be used for
 * ship construction, facility upgrades, and mercenary contracts.
 * </p>
 */
public class TradeManager {

    private final GameContext context;
    private final Random random;

    /** Maps planet ID to its Traveller world representation for trade. */
    private final Map<Integer, TravellerWorld> planetWorlds = new HashMap<>();

    /** Maps planet ID to its economy profile. */
    private final Map<Integer, WorldEconomy> planetEconomies = new HashMap<>();

    /** History of completed trade missions. */
    private final List<TradeMissionRecord> missionHistory = new ArrayList<>();

    /** Credit exchange service for bidirectional credit-resource conversion. */
    private CreditExchangeService creditExchangeService;

    /** Trade career service for Merchant career integration. */
    private TradeCareerService tradeCareerService;

    /** Trade route network for sector-wide trade simulation. */
    private TradeRouteNetwork tradeRouteNetwork;

    /**
     * Creates a new TradeManager.
     *
     * @param context the shared game context
     */
    public TradeManager(GameContext context) {
        this.context = context;
        this.random = context.getRandom();
        this.creditExchangeService = new CreditExchangeService(context);
        this.tradeCareerService = new TradeCareerService(context);
        this.tradeRouteNetwork = new TradeRouteNetwork(context);
    }

    /**
     * Returns the credit exchange service for this trade manager.
     *
     * @return the credit exchange service
     */
    public CreditExchangeService getCreditExchangeService() {
        return creditExchangeService;
    }

    /**
     * Returns the trade career service for this trade manager.
     *
     * @return the trade career service
     */
    public TradeCareerService getTradeCareerService() {
        return tradeCareerService;
    }

    /**
     * Returns the trade route network for this trade manager.
     *
     * @return the trade route network
     */
    public TradeRouteNetwork getTradeRouteNetwork() {
        return tradeRouteNetwork;
    }

    // ============================================================
    // Economy Management
    // ============================================================

    /**
     * Ensures a planet has a trade economy profile.
     * <p>
     * If the planet has UWP data, creates a {@link TravellerWorld} and
     * {@link WorldEconomy} for it. Planets without UWP data cannot trade.
     * </p>
     *
     * @param planet the planet to prepare for trade
     * @return true if the economy was created or already exists
     */
    public boolean ensureEconomy(Planet planet) {
        if (planet == null || !planet.hasUWP()) {
            return false;
        }
        if (planetEconomies.containsKey(planet.getId())) {
            return true;
        }

        // Create TravellerWorld from Planet UWP data
        TravellerWorld world = createTravellerWorld(planet);
        planetWorlds.put(planet.getId(), world);

        // Create economy profile
        WorldEconomy economy = new WorldEconomy(world);
        planetEconomies.put(planet.getId(), economy);

        return true;
    }

    /**
     * Returns the economy for a planet, or null if not available.
     *
     * @param planetId the planet ID
     * @return the world economy, or null
     */
    public WorldEconomy getEconomy(int planetId) {
        return planetEconomies.get(planetId);
    }

    /**
     * Returns the economy for a planet, or null if not available.
     *
     * @param planet the planet
     * @return the world economy, or null
     */
    public WorldEconomy getEconomy(Planet planet) {
        if (planet == null) return null;
        return getEconomy(planet.getId());
    }

    /**
     * Returns all economies (immutable).
     *
     * @return map of planet ID to economy
     */
    public Map<Integer, WorldEconomy> getAllEconomies() {
        return Collections.unmodifiableMap(planetEconomies);
    }

    // ============================================================
    // Price Queries
    // ============================================================

    /**
     * Gets the buy price for a commodity on a planet.
     * <p>
     * Buy price is what the trader pays to purchase the commodity.
     * </p>
     *
     * @param planetId the planet ID
     * @param commodity the commodity
     * @return the buy price in credits per unit, or 0 if unavailable
     */
    public int getBuyPrice(int planetId, Commodity commodity) {
        WorldEconomy economy = getEconomy(planetId);
        if (economy == null) return 0;
        return economy.getBuyPrice(commodity);
    }

    /**
     * Gets the sell price for a commodity on a planet.
     * <p>
     * Sell price is what the trader receives when selling the commodity.
     * </p>
     *
     * @param planetId the planet ID
     * @param commodity the commodity
     * @return the sell price in credits per unit, or 0 if unavailable
     */
    public int getSellPrice(int planetId, Commodity commodity) {
        WorldEconomy economy = getEconomy(planetId);
        if (economy == null) return 0;
        return economy.getSellPrice(commodity);
    }

    /**
     * Checks if a commodity can be bought on a planet.
     *
     * @param planetId the planet ID
     * @param commodity the commodity
     * @return true if the commodity can be purchased
     */
    public boolean canBuy(int planetId, Commodity commodity) {
        WorldEconomy economy = getEconomy(planetId);
        if (economy == null) return false;
        return economy.canBuy(commodity);
    }

    /**
     * Checks if a commodity can be sold on a planet.
     *
     * @param planetId the planet ID
     * @param commodity the commodity
     * @return true if the commodity can be sold
     */
    public boolean canSell(int planetId, Commodity commodity) {
        WorldEconomy economy = getEconomy(planetId);
        if (economy == null) return false;
        return economy.canSell(commodity);
    }

    // ============================================================
    // Trade Route Finding
    // ============================================================

    /**
     * Finds the most profitable trade route for a player's planets.
     * <p>
     * Searches all pairs of owned planets to find the route with the
     * highest potential profit for the given commodity.
     * </p>
     *
     * @param player the player whose planets to search
     * @return the best trade route, or null if no profitable route exists
     */
    public TradeRoute findBestTradeRoute(Player player) {
        if (player == null) return null;

        // Find empire for this player, then get owned planets
        Integer empireId = findEmpireIdForPlayer(player);
        List<Planet> ownedPlanets = context.getPlanets().values().stream()
                .filter(p -> empireId != null && empireId.equals(p.getOwnerEmpireId()))
                .collect(Collectors.toList());

        // Ensure economies exist
        for (Planet p : ownedPlanets) {
            ensureEconomy(p);
        }

        return findBestRouteAmong(ownedPlanets);
    }

    /**
     * Finds the most profitable trade route for a specific commodity.
     *
     * @param player the player whose planets to search
     * @param commodity the commodity to trade
     * @return the best trade route for this commodity, or null
     */
    public TradeRoute findBestTradeRoute(Player player, Commodity commodity) {
        if (player == null || commodity == null) return null;

        Integer empireId = findEmpireIdForPlayer(player);
        List<Planet> ownedPlanets = context.getPlanets().values().stream()
                .filter(p -> empireId != null && empireId.equals(p.getOwnerEmpireId()))
                .collect(Collectors.toList());

        for (Planet p : ownedPlanets) {
            ensureEconomy(p);
        }

        TradeRoute bestRoute = null;
        int bestProfit = 0;

        for (int i = 0; i < ownedPlanets.size(); i++) {
            for (int j = i + 1; j < ownedPlanets.size(); j++) {
                Planet from = ownedPlanets.get(i);
                Planet to = ownedPlanets.get(j);
                WorldEconomy fromEcon = getEconomy(from);
                WorldEconomy toEcon = getEconomy(to);

                if (fromEcon == null || toEcon == null) continue;

                // Buy at from, sell at to
                if (fromEcon.canBuy(commodity) && toEcon.canSell(commodity)) {
                    int buyPrice = fromEcon.getBuyPrice(commodity);
                    int sellPrice = toEcon.getSellPrice(commodity);
                    int profit = sellPrice - buyPrice;
                    if (profit > bestProfit) {
                        bestProfit = profit;
                        bestRoute = new TradeRoute(from.getId(), to.getId(), commodity, profit, true);
                    }
                }

                // Buy at to, sell at from
                if (toEcon.canBuy(commodity) && fromEcon.canSell(commodity)) {
                    int buyPrice = toEcon.getBuyPrice(commodity);
                    int sellPrice = fromEcon.getSellPrice(commodity);
                    int profit = sellPrice - buyPrice;
                    if (profit > bestProfit) {
                        bestProfit = profit;
                        bestRoute = new TradeRoute(to.getId(), from.getId(), commodity, profit, true);
                    }
                }
            }
        }

        return bestProfit > 0 ? bestRoute : null;
    }

    /**
     * Finds all profitable trade routes for a player.
     *
     * @param player the player
     * @return list of profitable trade routes, sorted by profit descending
     */
    public List<TradeRoute> findAllTradeRoutes(Player player) {
        List<TradeRoute> routes = new ArrayList<>();

        Integer empireId = findEmpireIdForPlayer(player);
        List<Planet> ownedPlanets = context.getPlanets().values().stream()
                .filter(p -> empireId != null && empireId.equals(p.getOwnerEmpireId()))
                .collect(Collectors.toList());

        for (Planet p : ownedPlanets) {
            ensureEconomy(p);
        }

        for (int i = 0; i < ownedPlanets.size(); i++) {
            for (int j = i + 1; j < ownedPlanets.size(); j++) {
                Planet p1 = ownedPlanets.get(i);
                Planet p2 = ownedPlanets.get(j);
                WorldEconomy econ1 = getEconomy(p1);
                WorldEconomy econ2 = getEconomy(p2);

                if (econ1 == null || econ2 == null) continue;

                for (Commodity commodity : Commodity.values()) {
                    // Direction 1: buy at p1, sell at p2
                    if (econ1.canBuy(commodity) && econ2.canSell(commodity)) {
                        int profit = econ2.getSellPrice(commodity) - econ1.getBuyPrice(commodity);
                        if (profit > 0) {
                            routes.add(new TradeRoute(p1.getId(), p2.getId(), commodity, profit, true));
                        }
                    }
                    // Direction 2: buy at p2, sell at p1
                    if (econ2.canBuy(commodity) && econ1.canSell(commodity)) {
                        int profit = econ1.getSellPrice(commodity) - econ2.getBuyPrice(commodity);
                        if (profit > 0) {
                            routes.add(new TradeRoute(p2.getId(), p1.getId(), commodity, profit, true));
                        }
                    }
                }
            }
        }

        routes.sort((a, b) -> Integer.compare(b.profitPerUnit, a.profitPerUnit));
        return routes;
    }

    // ============================================================
    // Trade Execution
    // ============================================================

    /**
     * Executes a trade mission between two planets.
     * <p>
     * The trader buys the commodity at the source planet and sells it
     * at the destination planet. Profit is calculated and added to the
     * player's resources, modified by commander bonuses and resource
     * scarcity modifiers from the CreditExchangeService.
     * </p>
     *
     * @param player the player executing the trade
     * @param sourcePlanetId the planet to buy from
     * @param destPlanetId the planet to sell at
     * @param commodity the commodity to trade
     * @param units the number of units to trade
     * @return the trade result with profit/loss
     */
    public TradeResult executeTrade(Player player, int sourcePlanetId, int destPlanetId,
                                     Commodity commodity, int units) {
        if (player == null || commodity == null || units <= 0) {
            return new TradeResult(false, 0, 0, 0, "Invalid trade parameters");
        }

        WorldEconomy sourceEcon = getEconomy(sourcePlanetId);
        WorldEconomy destEcon = getEconomy(destPlanetId);

        if (sourceEcon == null) {
            return new TradeResult(false, 0, 0, 0, "Source planet has no trade economy");
        }
        if (destEcon == null) {
            return new TradeResult(false, 0, 0, 0, "Destination planet has no trade economy");
        }

        // Check availability
        if (!sourceEcon.canBuy(commodity)) {
            return new TradeResult(false, 0, 0, 0, commodity.getName() + " not available for purchase at source");
        }
        if (!destEcon.canSell(commodity)) {
            return new TradeResult(false, 0, 0, 0, commodity.getName() + " not accepted at destination");
        }

        // Calculate costs and revenue
        int buyPrice = sourceEcon.getBuyPrice(commodity);
        int sellPrice = destEcon.getSellPrice(commodity);

        // Check market size limits
        int maxUnits = Math.min(sourceEcon.getMaxTradeUnits(commodity), destEcon.getMaxTradeUnits(commodity));
        int actualUnits = Math.min(units, maxUnits);
        if (actualUnits <= 0) {
            return new TradeResult(false, 0, 0, 0, "Market too small for trade");
        }

        long cost = (long) buyPrice * actualUnits;
        long revenue = (long) sellPrice * actualUnits;
        long grossProfit = revenue - cost;

        // Apply commander trade bonus
        double bonus = player.getCommanderBonus(Player.CommanderBonusType.TRADE_PROFIT);
        long netProfit = (long) (grossProfit * (1.0 + bonus / 100.0));

        boolean profitable = netProfit > 0;

        // Record the mission
        TradeMissionRecord record = new TradeMissionRecord(
                context.getCurrentTurn(), player.getId(), sourcePlanetId, destPlanetId,
                commodity, actualUnits, cost, revenue, netProfit
        );
        missionHistory.add(record);

        return new TradeResult(profitable, cost, revenue, netProfit, actualUnits);
    }

    /**
     * Executes a trade mission with resource scarcity feedback.
     * <p>
     * Like {@link #executeTrade(Player, int, int, Commodity, int)} but also
     * applies resource scarcity modifiers from the CreditExchangeService.
     * When DoraMCU resources are scarce, trade prices are adjusted to
     * reflect the increased value of the corresponding commodities.
     * </p>
     *
     * @param player the player executing the trade
     * @param sourcePlanetId the planet to buy from
     * @param destPlanetId the planet to sell at
     * @param commodity the commodity to trade
     * @param units the number of units to trade
     * @param pipeline the resource pipeline for scarcity feedback (may be null)
     * @return the trade result with profit/loss
     */
    public TradeResult executeTradeWithScarcityFeedback(Player player, int sourcePlanetId, int destPlanetId,
                                                         Commodity commodity, int units,
                                                         com.yisy.modules.doramcu.hex.ResourcePipelineManager pipeline) {
        if (player == null || commodity == null || units <= 0) {
            return new TradeResult(false, 0, 0, 0, "Invalid trade parameters");
        }

        WorldEconomy sourceEcon = getEconomy(sourcePlanetId);
        WorldEconomy destEcon = getEconomy(destPlanetId);

        if (sourceEcon == null) {
            return new TradeResult(false, 0, 0, 0, "Source planet has no trade economy");
        }
        if (destEcon == null) {
            return new TradeResult(false, 0, 0, 0, "Destination planet has no trade economy");
        }

        // Check availability
        if (!sourceEcon.canBuy(commodity)) {
            return new TradeResult(false, 0, 0, 0, commodity.getName() + " not available for purchase at source");
        }
        if (!destEcon.canSell(commodity)) {
            return new TradeResult(false, 0, 0, 0, commodity.getName() + " not accepted at destination");
        }

        // Calculate base costs and revenue
        int buyPrice = sourceEcon.getBuyPrice(commodity);
        int sellPrice = destEcon.getSellPrice(commodity);

        // Apply resource scarcity modifiers if pipeline is available
        if (pipeline != null) {
            double scarcityModifier = getResourceScarcityModifier(pipeline, commodity);
            buyPrice = (int) Math.round(buyPrice * scarcityModifier);
            sellPrice = (int) Math.round(sellPrice * scarcityModifier);
        }

        // Check market size limits
        int maxUnits = Math.min(sourceEcon.getMaxTradeUnits(commodity), destEcon.getMaxTradeUnits(commodity));
        int actualUnits = Math.min(units, maxUnits);
        if (actualUnits <= 0) {
            return new TradeResult(false, 0, 0, 0, "Market too small for trade");
        }

        long cost = (long) buyPrice * actualUnits;
        long revenue = (long) sellPrice * actualUnits;
        long grossProfit = revenue - cost;

        // Apply commander trade bonus
        double bonus = player.getCommanderBonus(Player.CommanderBonusType.TRADE_PROFIT);
        long netProfit = (long) (grossProfit * (1.0 + bonus / 100.0));

        boolean profitable = netProfit > 0;

        // Feed profit to pipeline if available
        if (pipeline != null && netProfit > 0) {
            feedTradeProfitToPipeline(pipeline, netProfit, commodity);
        }

        // Record the mission
        TradeMissionRecord record = new TradeMissionRecord(
                context.getCurrentTurn(), player.getId(), sourcePlanetId, destPlanetId,
                commodity, actualUnits, cost, revenue, netProfit
        );
        missionHistory.add(record);

        return new TradeResult(profitable, cost, revenue, netProfit, actualUnits);
    }

    // ============================================================
    // Market Fluctuation
    // ============================================================

    /**
     * Simulates market fluctuations for all tracked economies.
     * <p>
     * Called at end-of-turn to simulate economic changes between turns.
     * Market conditions may shift, affecting commodity availability and prices.
     * </p>
     */
    public void fluctuateMarkets() {
        for (WorldEconomy economy : planetEconomies.values()) {
            economy.fluctuate();
        }
    }

    // ============================================================
    // Resource Pipeline Integration
    // ============================================================

    /**
     * Connects trade profits to a ResourcePipelineManager using dynamic exchange rates.
     * <p>
     * Converts trade profit (in credits) to refined resources based on
     * commodity type. The conversion rate is adjusted by resource scarcity:
     * when a resource is scarce, fewer units are produced per credit.
     * </p>
     * <p>
     * The commodity-to-resource mapping is:
     * </p>
     * <ul>
     *   <li>Food, Medicine → Nutrient</li>
     *   <li>Metals, Machinery → Metal Alloy</li>
     *   <li>Fuel, Chemicals → Fuel</li>
     *   <li>Electronics, Computers → Power Cell</li>
     *   <li>Textiles, Plastics → Composite</li>
     *   <li>Other → Metal Alloy (default)</li>
     * </ul>
     *
     * @param pipeline the resource pipeline to feed
     * @param profit the trade profit in credits
     * @param commodity the commodity traded
     * @return the amount of resources added
     */
    public int feedTradeProfitToPipeline(com.yisy.modules.doramcu.hex.ResourcePipelineManager pipeline,
                                          long profit, Commodity commodity) {
        if (pipeline == null || profit <= 0 || commodity == null) {
            return 0;
        }

        // Use CreditExchangeService for dynamic conversion
        int added = creditExchangeService.convertCreditsToResources(pipeline, profit, commodity);

        // Also add credits to empire balance for tracking
        int empireId = pipeline.getOwnerEmpireId();
        creditExchangeService.addTradeCredits(empireId, profit);

        return added;
    }

    /**
     * Converts DoraMCU resources to Traveller credits for purchasing commodities.
     * <p>
     * This enables the reverse flow: when the player has excess resources,
     * they can be converted to credits for Traveller trade operations.
     * </p>
     *
     * @param pipeline the resource pipeline to draw from
     * @param resourceType the resource type to convert
     * @param amount the amount of resources to convert
     * @return the number of credits generated
     */
    public long convertResourcesToCredits(com.yisy.modules.doramcu.hex.ResourcePipelineManager pipeline,
                                           com.yisy.modules.doramcu.hex.RefinedResourceType resourceType,
                                           int amount) {
        return creditExchangeService.convertResourcesToCredits(pipeline, resourceType, amount);
    }

    /**
     * Gets the resource scarcity modifier for a commodity's trade price.
     * <p>
     * When DoraMCU resources are scarce, the corresponding Traveller commodity
     * becomes more expensive. This creates a bidirectional feedback loop.
     * </p>
     *
     * @param pipeline the resource pipeline to check
     * @param commodity the Traveller commodity
     * @return price modifier (1.0 = no change, 2.0 = double price, 0.7 = 30% discount)
     */
    public double getResourceScarcityModifier(com.yisy.modules.doramcu.hex.ResourcePipelineManager pipeline,
                                               Commodity commodity) {
        return creditExchangeService.getResourceScarcityModifier(pipeline, commodity);
    }

    /**
     * Gets all resource scarcity modifiers for all commodities.
     *
     * @param pipeline the resource pipeline to check
     * @return map of commodity to price modifier
     */
    public Map<Commodity, Double> getAllScarcityModifiers(com.yisy.modules.doramcu.hex.ResourcePipelineManager pipeline) {
        return creditExchangeService.getAllScarcityModifiers(pipeline);
    }

    /**
     * Maps a Traveller commodity to a DoraMCU refined resource type.
     * <p>
     * This mapping determines what resources are produced when trade profit
     * is converted to materials for the resource pipeline.
     * </p>
     *
     * @param commodity the Traveller commodity
     * @return the corresponding refined resource type
     */
    public com.yisy.modules.doramcu.hex.RefinedResourceType mapCommodityToResource(Commodity commodity) {
        if (commodity == null) {
            return com.yisy.modules.doramcu.hex.RefinedResourceType.METAL_ALLOY;
        }

        String name = commodity.getName().toLowerCase();
        String combined = name;

        // Food and medicine → Nutrient
        if (combined.contains("food") || combined.contains("medicine") || combined.contains("drug")
                || combined.contains("nutrient") || combined.contains("biological")) {
            return com.yisy.modules.doramcu.hex.RefinedResourceType.NUTRIENT;
        }

        // Fuel and chemicals → Fuel
        if (combined.contains("fuel") || combined.contains("chemical") || combined.contains("explosive")
                || combined.contains("propellant")) {
            return com.yisy.modules.doramcu.hex.RefinedResourceType.FUEL;
        }

        // Electronics and computers → Power Cell
        if (combined.contains("electronic") || combined.contains("computer") || combined.contains("circuit")
                || combined.contains("power") || combined.contains("energy")) {
            return com.yisy.modules.doramcu.hex.RefinedResourceType.POWER_CELL;
        }

        // Textiles and plastics → Composite
        if (combined.contains("textile") || combined.contains("plastic") || combined.contains("fabric")
                || combined.contains("cloth") || combined.contains("polymer")) {
            return com.yisy.modules.doramcu.hex.RefinedResourceType.COMPOSITE;
        }

        // Metals and machinery → Metal Alloy (default)
        return com.yisy.modules.doramcu.hex.RefinedResourceType.METAL_ALLOY;
    }

    /**
     * Processes end-of-turn trade activities for all empires.
     * <p>
     * Fluctuates markets, processes economic events, builds the trade route network,
     * and auto-trades for empires with enabled auto-trade.
     * Trade profits are fed into the empire's resource pipeline.
     * </p>
     *
     * @param pipelines map of empire ID to resource pipeline manager
     */
    public void processEndOfTurn(Map<Integer, com.yisy.modules.doramcu.hex.ResourcePipelineManager> pipelines) {
        // Build trade route network
        tradeRouteNetwork.buildNetwork(this);

        // Process economic events
        tradeRouteNetwork.processEconomicEvents();

        // Fluctuate markets first
        fluctuateMarkets();

        // Auto-trade for each empire with a pipeline
        for (Map.Entry<Integer, com.yisy.modules.doramcu.hex.ResourcePipelineManager> entry : pipelines.entrySet()) {
            int empireId = entry.getKey();
            com.yisy.modules.doramcu.hex.ResourcePipelineManager pipeline = entry.getValue();

            // Find the empire's planets
            List<Planet> ownedPlanets = context.getPlanets().values().stream()
                    .filter(p -> empireId == p.getOwnerEmpireId())
                    .collect(Collectors.toList());

            if (ownedPlanets.size() < 2) continue; // Need at least 2 planets to trade

            // Ensure economies
            for (Planet p : ownedPlanets) {
                ensureEconomy(p);
            }

            // Find best route and execute
            TradeRoute route = findBestRouteAmong(ownedPlanets);
            if (route != null && route.profitable) {
                // Convert profit to resources
                long profit = route.profitPerUnit * 10; // Assume 10 units per auto-trade
                int added = feedTradeProfitToPipeline(pipeline, profit, route.commodity);
                if (added > 0) {
                    // Record the auto-trade
                    TradeMissionRecord record = new TradeMissionRecord(
                            context.getCurrentTurn(), empireId, route.sourcePlanetId,
                            route.destPlanetId, route.commodity, 10,
                            -profit / 2, profit * 3 / 2, profit
                    );
                    missionHistory.add(record);

                    // Record trade volume in network
                    tradeRouteNetwork.recordTradeVolume(route.sourcePlanetId, route.destPlanetId, 10);
                }
            }
        }

        // Reset trade volumes for next turn
        tradeRouteNetwork.resetTradeVolumes();
    }

    // ============================================================
    // Mission History
    // ============================================================

    /**
     * Returns the trade mission history (immutable).
     *
     * @return list of completed trade missions
     */
    public List<TradeMissionRecord> getMissionHistory() {
        return Collections.unmodifiableList(missionHistory);
    }

    /**
     * Returns the mission history for a specific player.
     *
     * @param playerId the player ID
     * @return list of missions for that player
     */
    public List<TradeMissionRecord> getMissionHistory(int playerId) {
        return missionHistory.stream()
                .filter(r -> r.playerId == playerId)
                .collect(Collectors.toList());
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Creates a TravellerWorld from a DoraMCU Planet's UWP data.
     */
    private TravellerWorld createTravellerWorld(Planet planet) {
        // Build UWP string from Planet fields using the correct getter names
        int atmosphere = planet.getUwpAtmosphere() != null ? planet.getUwpAtmosphere() : 5;
        int gravity = planet.getUwpGravity() != null ? planet.getUwpGravity() : 4;
        int population = planet.getUwpPopulationLevel() != null ? planet.getUwpPopulationLevel() : 4;
        int starport = planet.getUwpStarport() != null ? planet.getUwpStarport() : 4;
        int government = planet.getUwpGovernmentType() != null ? planet.getUwpGovernmentType() : 0;
        int law = planet.getUwpLawLevel() != null ? planet.getUwpLawLevel() : 0;

        // Build UWP string: P C A G M T - Mod
        String uwpString = "5" + atmosphere + gravity + starport + population + government + law + "- ";

        TravellerWorld world = new TravellerWorld(
                planet.getId(),
                planet.getName(),
                uwpString,
                0, 0
        );

        // Add trade codes from the planet
        Set<TradeCode> codes = planet.getTradeCodes();
        if (codes != null) {
            for (TradeCode code : codes) {
                world.addTradeCode(code);
            }
        }

        return world;
    }

    /**
     * Finds the empire ID for a player by matching the ruler player ID.
     */
    private Integer findEmpireIdForPlayer(Player player) {
        for (com.yisy.modules.doramcu.hex.Empire empire : context.getEmpires().values()) {
            if (empire.getRulerPlayerId() == player.getId()) {
                return empire.getId();
            }
        }
        return null;
    }

    /**
     * Finds the best route among a list of planets.
     */
    private TradeRoute findBestRouteAmong(List<Planet> planets) {
        TradeRoute bestRoute = null;
        int bestProfit = 0;

        for (int i = 0; i < planets.size(); i++) {
            for (int j = i + 1; j < planets.size(); j++) {
                Planet p1 = planets.get(i);
                Planet p2 = planets.get(j);
                WorldEconomy econ1 = getEconomy(p1);
                WorldEconomy econ2 = getEconomy(p2);

                if (econ1 == null || econ2 == null) continue;

                for (Commodity commodity : Commodity.values()) {
                    // Direction 1: buy at p1, sell at p2
                    if (econ1.canBuy(commodity) && econ2.canSell(commodity)) {
                        int profit = econ2.getSellPrice(commodity) - econ1.getBuyPrice(commodity);
                        if (profit > bestProfit) {
                            bestProfit = profit;
                            bestRoute = new TradeRoute(p1.getId(), p2.getId(), commodity, profit, true);
                        }
                    }
                    // Direction 2: buy at p2, sell at p1
                    if (econ2.canBuy(commodity) && econ1.canSell(commodity)) {
                        int profit = econ1.getSellPrice(commodity) - econ2.getBuyPrice(commodity);
                        if (profit > bestProfit) {
                            bestProfit = profit;
                            bestRoute = new TradeRoute(p2.getId(), p1.getId(), commodity, profit, true);
                        }
                    }
                }
            }
        }

        return bestProfit > 0 ? bestRoute : null;
    }

    /**
     * Buys a commodity at a world.
     * @param player the player buying
     * @param worldId the world/planet ID
     * @param commodityName the commodity name
     * @param quantity the quantity to buy
     * @return the total cost in credits
     */
    public int buyCommodity(Player player, int worldId, String commodityName, int quantity) {
        Commodity commodity = resolveCommodity(commodityName);
        if (commodity == null) {
            throw new IllegalArgumentException("Unknown commodity: " + commodityName);
        }
        WorldEconomy economy = getEconomy(worldId);
        if (economy == null) {
            throw new IllegalArgumentException("No economy found for world: " + worldId);
        }
        int price = economy.getBuyPrice(commodity);
        return price * quantity;
    }

    /**
     * Sells a commodity at a world.
     * @param player the player selling
     * @param worldId the world/planet ID
     * @param commodityName the commodity name
     * @param quantity the quantity to sell
     * @return the total revenue in credits
     */
    public int sellCommodity(Player player, int worldId, String commodityName, int quantity) {
        Commodity commodity = resolveCommodity(commodityName);
        if (commodity == null) {
            throw new IllegalArgumentException("Unknown commodity: " + commodityName);
        }
        WorldEconomy economy = getEconomy(worldId);
        if (economy == null) {
            throw new IllegalArgumentException("No economy found for world: " + worldId);
        }
        int price = economy.getSellPrice(commodity);
        return price * quantity;
    }

    /**
     * Resolves a commodity by name (case-insensitive).
     */
    private Commodity resolveCommodity(String name) {
        if (name == null) return null;
        for (Commodity c : Commodity.values()) {
            if (c.name().equalsIgnoreCase(name) || c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    // ============================================================
    // Inner Classes
    // ============================================================

    /**
     * Represents a trade route between two planets.
     */
    public static class TradeRoute {
        public final int sourcePlanetId;
        public final int destPlanetId;
        public final Commodity commodity;
        public final int profitPerUnit;
        public final boolean profitable;

        public TradeRoute(int sourcePlanetId, int destPlanetId, Commodity commodity,
                          int profitPerUnit, boolean profitable) {
            this.sourcePlanetId = sourcePlanetId;
            this.destPlanetId = destPlanetId;
            this.commodity = commodity;
            this.profitPerUnit = profitPerUnit;
            this.profitable = profitable;
        }

        @Override
        public String toString() {
            return String.format("TradeRoute{%s -> %s: %s, profit=%d/unit}",
                    sourcePlanetId, destPlanetId, commodity.getName(), profitPerUnit);
        }
    }

    /**
     * Result of a trade execution.
     */
    public static class TradeResult {
        public final boolean profitable;
        public final long cost;
        public final long revenue;
        public final long netProfit;
        public final int units;
        public final String message;

        public TradeResult(boolean profitable, long cost, long revenue, long netProfit, String message) {
            this.profitable = profitable;
            this.cost = cost;
            this.revenue = revenue;
            this.netProfit = netProfit;
            this.units = 0;
            this.message = message;
        }

        public TradeResult(boolean profitable, long cost, long revenue, long netProfit, int units) {
            this.profitable = profitable;
            this.cost = cost;
            this.revenue = revenue;
            this.netProfit = netProfit;
            this.units = units;
            this.message = profitable ? "Trade successful" : "Trade not profitable";
        }

        @Override
        public String toString() {
            return String.format("TradeResult{profitable=%s, cost=%d, revenue=%d, profit=%d, units=%d}",
                    profitable, cost, revenue, netProfit, units);
        }
    }

    /**
     * Record of a completed trade mission.
     */
    public static class TradeMissionRecord {
        public final int turn;
        public final int playerId;
        public final int sourcePlanetId;
        public final int destPlanetId;
        public final Commodity commodity;
        public final int units;
        public final long cost;
        public final long revenue;
        public final long profit;

        public TradeMissionRecord(int turn, int playerId, int sourcePlanetId, int destPlanetId,
                                   Commodity commodity, int units, long cost, long revenue, long profit) {
            this.turn = turn;
            this.playerId = playerId;
            this.sourcePlanetId = sourcePlanetId;
            this.destPlanetId = destPlanetId;
            this.commodity = commodity;
            this.units = units;
            this.cost = cost;
            this.revenue = revenue;
            this.profit = profit;
        }

        @Override
        public String toString() {
            return String.format("Mission{turn=%d, player=%d, %d->%d, %s x%d, profit=%d}",
                    turn, playerId, sourcePlanetId, destPlanetId, commodity.getName(), units, profit);
        }
    }
}
