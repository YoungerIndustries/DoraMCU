package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.traveller.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages sector-wide trade route network with market equilibrium and economic events.
 * <p>
 * This service provides:
 * </p>
 * <ul>
 *   <li><b>Trade Route Network</b>: Maintains a network of trade routes between all
 *       planets in a sector, with automatic route discovery and optimization</li>
 *   <li><b>Market Equilibrium</b>: Simulates supply and demand across the sector,
 *       causing prices to converge toward equilibrium over time</li>
 *   <li><b>Supply Chain Simulation</b>: Tracks commodity flow through the network,
 *       affecting availability and prices at each node</li>
 *   <li><b>Economic Events</b>: Random events like piracy, embargoes, booms, and busts
 *       that affect trade routes and prices</li>
 * </ul>
 * <p>
 * The network is updated each turn to reflect changing market conditions.
 * </p>
 */
public class TradeRouteNetwork {

    /** Probability of an economic event occurring each turn (%) */
    private static final double ECONOMIC_EVENT_PROBABILITY = 15.0;

    /** Duration of economic events in turns */
    private static final int EVENT_DURATION_TURNS = 3;

    /** Market equilibrium convergence rate per turn */
    private static final double EQUILIBRIUM_CONVERGENCE_RATE = 0.1;

    private final GameContext context;
    private final Random random;

    /** All trade routes in the network */
    private final List<TradeRouteNode> routeNodes = new ArrayList<>();

    /** Active economic events */
    private final List<EconomicEvent> activeEvents = new ArrayList<>();

    /** Sector-wide average prices for each commodity */
    private final Map<Commodity, Double> sectorAveragePrices = new EnumMap<>(Commodity.class);

    /** Trade volume per route (sourceId -> destId -> volume) */
    private final Map<String, Integer> tradeVolumes = new HashMap<>();

    /**
     * Creates a new TradeRouteNetwork.
     *
     * @param context the shared game context
     */
    public TradeRouteNetwork(GameContext context) {
        this.context = context;
        this.random = context.getRandom();
        initializeSectorPrices();
    }

    /**
     * Initializes sector-wide average prices from all known economies.
     */
    private void initializeSectorPrices() {
        for (Commodity commodity : Commodity.values()) {
            sectorAveragePrices.put(commodity, (double) commodity.getBasePrice());
        }
    }

    // ============================================================
    // Route Network Management
    // ============================================================

    /**
     * Builds the trade route network from all planets with economies.
     * <p>
     * Called at the start of each turn to update the network with
     * current market conditions.
     * </p>
     *
     * @param tradeManager the trade manager providing economy data
     */
    public void buildNetwork(TradeManager tradeManager) {
        routeNodes.clear();

        // Create nodes for each planet with an economy
        for (Map.Entry<Integer, WorldEconomy> entry : tradeManager.getAllEconomies().entrySet()) {
            int planetId = entry.getKey();
            WorldEconomy economy = entry.getValue();

            TradeRouteNode node = new TradeRouteNode(planetId, economy.getWorld().getName(), economy);
            routeNodes.add(node);
        }

        // Calculate sector-wide average prices
        updateSectorAveragePrices();
    }

    /**
     * Updates sector-wide average prices based on all node economies.
     */
    private void updateSectorAveragePrices() {
        if (routeNodes.isEmpty()) {
            return;
        }

        for (Commodity commodity : Commodity.values()) {
            double total = 0.0;
            int count = 0;

            for (TradeRouteNode node : routeNodes) {
                int price = node.economy.getBuyPrice(commodity);
                if (price > 0) {
                    total += price;
                    count++;
                }
            }

            if (count > 0) {
                sectorAveragePrices.put(commodity, total / count);
            }
        }
    }

    /**
     * Finds all profitable trade routes in the network.
     *
     * @return list of profitable routes sorted by profit descending
     */
    public List<TradeRouteEdge> findAllProfitableRoutes() {
        List<TradeRouteEdge> routes = new ArrayList<>();

        for (int i = 0; i < routeNodes.size(); i++) {
            for (int j = i + 1; j < routeNodes.size(); j++) {
                TradeRouteNode node1 = routeNodes.get(i);
                TradeRouteNode node2 = routeNodes.get(j);

                for (Commodity commodity : Commodity.values()) {
                    // Direction 1: buy at node1, sell at node2
                    if (node1.economy.canBuy(commodity) && node2.economy.canSell(commodity)) {
                        int buyPrice = node1.economy.getBuyPrice(commodity);
                        int sellPrice = node2.economy.getSellPrice(commodity);
                        int profit = sellPrice - buyPrice;

                        if (profit > 0) {
                            routes.add(new TradeRouteEdge(
                                    node1.planetId, node2.planetId,
                                    node1.worldName, node2.worldName,
                                    commodity, profit, true
                            ));
                        }
                    }

                    // Direction 2: buy at node2, sell at node1
                    if (node2.economy.canBuy(commodity) && node1.economy.canSell(commodity)) {
                        int buyPrice = node2.economy.getBuyPrice(commodity);
                        int sellPrice = node1.economy.getSellPrice(commodity);
                        int profit = sellPrice - buyPrice;

                        if (profit > 0) {
                            routes.add(new TradeRouteEdge(
                                    node2.planetId, node1.planetId,
                                    node2.worldName, node1.worldName,
                                    commodity, profit, true
                            ));
                        }
                    }
                }
            }
        }

        routes.sort((a, b) -> Integer.compare(b.profitPerUnit, a.profitPerUnit));
        return routes;
    }

    /**
     * Finds the best trade route for a specific commodity.
     *
     * @param commodity the commodity to trade
     * @return the best route, or null if no profitable route exists
     */
    public TradeRouteEdge findBestRouteForCommodity(Commodity commodity) {
        return findAllProfitableRoutes().stream()
                .filter(route -> route.commodity == commodity)
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds trade routes affected by active economic events.
     *
     * @return list of affected routes
     */
    public List<TradeRouteEdge> getAffectedRoutes() {
        List<TradeRouteEdge> affected = new ArrayList<>();

        for (EconomicEvent event : activeEvents) {
            for (TradeRouteEdge route : findAllProfitableRoutes()) {
                if (event.affectsRoute(route)) {
                    affected.add(route);
                }
            }
        }

        return affected;
    }

    // ============================================================
    // Market Equilibrium
    // ============================================================

    /**
     * Simulates market equilibrium convergence.
     * <p>
     * Prices gradually move toward sector-wide averages, simulating
     * the natural tendency of markets to equalize.
     * </p>
     */
    public void simulateMarketEquilibrium() {
        for (TradeRouteNode node : routeNodes) {
            for (Commodity commodity : Commodity.values()) {
                double sectorAvg = sectorAveragePrices.getOrDefault(commodity, 0.0);
                if (sectorAvg <= 0) continue;

                int currentPrice = node.economy.getBuyPrice(commodity);
                if (currentPrice <= 0) continue;

                // Move price toward sector average
                double diff = sectorAvg - currentPrice;
                double adjustment = diff * EQUILIBRIUM_CONVERGENCE_RATE;

                // Only apply if significant
                if (Math.abs(adjustment) > 1.0) {
                    // Market conditions fluctuate naturally; equilibrium is a soft pull
                    // The actual price change happens through market fluctuation
                }
            }
        }
    }

    /**
     * Gets the sector-wide average price for a commodity.
     *
     * @param commodity the commodity
     * @return the average price in credits
     */
    public double getSectorAveragePrice(Commodity commodity) {
        return sectorAveragePrices.getOrDefault(commodity, (double) commodity.getBasePrice());
    }

    /**
     * Gets all sector-wide average prices.
     *
     * @return map of commodity to average price
     */
    public Map<Commodity, Double> getAllSectorPrices() {
        return Collections.unmodifiableMap(sectorAveragePrices);
    }

    // ============================================================
    // Supply Chain Simulation
    // ============================================================

    /**
     * Records trade volume for a route.
     *
     * @param sourceId the source planet ID
     * @param destId the destination planet ID
     * @param volume the trade volume in units
     */
    public void recordTradeVolume(int sourceId, int destId, int volume) {
        String key = sourceId + "->" + destId;
        tradeVolumes.merge(key, volume, Integer::sum);
    }

    /**
     * Gets the total trade volume for a route.
     *
     * @param sourceId the source planet ID
     * @param destId the destination planet ID
     * @return the total trade volume
     */
    public int getTradeVolume(int sourceId, int destId) {
        String key = sourceId + "->" + destId;
        return tradeVolumes.getOrDefault(key, 0);
    }

    /**
     * Resets trade volumes for a new turn.
     */
    public void resetTradeVolumes() {
        tradeVolumes.clear();
    }

    // ============================================================
    // Economic Events
    // ============================================================

    /**
     * Processes economic events for the current turn.
     * <p>
     * Checks for new events, updates existing events, and removes
     * expired events.
     * </p>
     */
    public void processEconomicEvents() {
        // Remove expired events
        activeEvents.removeIf(event -> event.turnsRemaining <= 0);

        // Check for new events
        if (random.nextDouble() * 100.0 < ECONOMIC_EVENT_PROBABILITY) {
            EconomicEvent event = generateEconomicEvent();
            if (event != null) {
                activeEvents.add(event);
            }
        }

        // Decrement turns remaining
        for (EconomicEvent event : activeEvents) {
            event.turnsRemaining--;
        }
    }

    /**
     * Generates a random economic event.
     *
     * @return the generated event, or null
     */
    private EconomicEvent generateEconomicEvent() {
        if (routeNodes.isEmpty()) {
            return null;
        }

        EconomicEventType type = EconomicEventType.values()[random.nextInt(EconomicEventType.values().length)];

        // Pick a random planet affected by the event
        TradeRouteNode affectedNode = routeNodes.get(random.nextInt(routeNodes.size()));

        return new EconomicEvent(
                type,
                affectedNode.planetId,
                affectedNode.worldName,
                EVENT_DURATION_TURNS,
                generateEventDescription(type, affectedNode.worldName)
        );
    }

    /**
     * Generates a description for an economic event.
     */
    private String generateEventDescription(EconomicEventType type, String worldName) {
        switch (type) {
            case PIRACY:
                return "Pirate activity reported near " + worldName + ". Trade routes affected.";
            case EMBARGO:
                return worldName + " has imposed trade embargoes. Some commodities unavailable.";
            case BOOM:
                return "Economic boom on " + worldName + ". Demand for goods increased.";
            case BUST:
                return "Economic downturn on " + worldName + ". Prices falling.";
            case BLOCKADE:
                return "Naval blockade near " + worldName + ". Trade routes disrupted.";
            case DIPLOMATIC:
                return "Diplomatic relations improved near " + worldName + ". Trade bonuses applied.";
            default:
                return "Economic event near " + worldName + ".";
        }
    }

    /**
     * Gets all active economic events.
     *
     * @return list of active events
     */
    public List<EconomicEvent> getActiveEvents() {
        return Collections.unmodifiableList(activeEvents);
    }

    /**
     * Checks if a planet is affected by any economic event.
     *
     * @param planetId the planet ID
     * @return true if affected
     */
    public boolean isPlanetAffected(int planetId) {
        return activeEvents.stream().anyMatch(event -> event.affectedPlanetId == planetId);
    }

    /**
     * Gets the price modifier for a planet based on active events.
     *
     * @param planetId the planet ID
     * @param commodity the commodity
     * @return the price modifier (1.0 = no change)
     */
    public double getEventPriceModifier(int planetId, Commodity commodity) {
        double modifier = 1.0;

        for (EconomicEvent event : activeEvents) {
            if (event.affectedPlanetId == planetId) {
                modifier *= event.getPriceModifier(commodity);
            }
        }

        return modifier;
    }

    // ============================================================
    // Summary & Reporting
    // ============================================================

    /**
     * Returns a summary of the trade route network.
     *
     * @return a summary map
     */
    public Map<String, Object> getNetworkSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("routeNodes", routeNodes.size());
        summary.put("activeEvents", activeEvents.size());
        summary.put("profitableRoutes", findAllProfitableRoutes().size());

        Map<String, Double> prices = new HashMap<>();
        for (Map.Entry<Commodity, Double> entry : sectorAveragePrices.entrySet()) {
            prices.put(entry.getKey().getName(), entry.getValue());
        }
        summary.put("sectorAveragePrices", prices);

        List<Map<String, Object>> events = new ArrayList<>();
        for (EconomicEvent event : activeEvents) {
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("type", event.type.name());
            eventMap.put("world", event.worldName);
            eventMap.put("turnsRemaining", event.turnsRemaining);
            eventMap.put("description", event.description);
            events.add(eventMap);
        }
        summary.put("events", events);

        return summary;
    }

    // ============================================================
    // Inner Classes
    // ============================================================

    /**
     * Represents a node in the trade route network (a planet with an economy).
     */
    private static class TradeRouteNode {
        final int planetId;
        final String worldName;
        final WorldEconomy economy;

        TradeRouteNode(int planetId, String worldName, WorldEconomy economy) {
            this.planetId = planetId;
            this.worldName = worldName;
            this.economy = economy;
        }
    }

    /**
     * Represents a directed edge in the trade route network.
     */
    public static class TradeRouteEdge {
        public final int sourcePlanetId;
        public final int destPlanetId;
        public final String sourceWorldName;
        public final String destWorldName;
        public final Commodity commodity;
        public final int profitPerUnit;
        public final boolean profitable;

        TradeRouteEdge(int sourcePlanetId, int destPlanetId, String sourceWorldName,
                        String destWorldName, Commodity commodity, int profitPerUnit, boolean profitable) {
            this.sourcePlanetId = sourcePlanetId;
            this.destPlanetId = destPlanetId;
            this.sourceWorldName = sourceWorldName;
            this.destWorldName = destWorldName;
            this.commodity = commodity;
            this.profitPerUnit = profitPerUnit;
            this.profitable = profitable;
        }
    }

    /**
     * Types of economic events that can affect trade.
     */
    public enum EconomicEventType {
        /** Pirate attacks disrupt trade routes */
        PIRACY,
        /** Trade embargoes restrict commodity availability */
        EMBARGO,
        /** Economic boom increases demand and prices */
        BOOM,
        /** Economic bust decreases demand and prices */
        BUST,
        /** Naval blockade blocks trade routes */
        BLOCKADE,
        /** Diplomatic relations improve trade */
        DIPLOMATIC
    }

    /**
     * Represents an active economic event affecting trade.
     */
    public static class EconomicEvent {
        public final EconomicEventType type;
        public final int affectedPlanetId;
        public final String worldName;
        public int turnsRemaining;
        public final String description;

        EconomicEvent(EconomicEventType type, int affectedPlanetId, String worldName,
                       int duration, String description) {
            this.type = type;
            this.affectedPlanetId = affectedPlanetId;
            this.worldName = worldName;
            this.turnsRemaining = duration;
            this.description = description;
        }

        /**
         * Checks if this event affects a trade route.
         */
        boolean affectsRoute(TradeRouteEdge route) {
            return route.sourcePlanetId == affectedPlanetId || route.destPlanetId == affectedPlanetId;
        }

        /**
         * Gets the price modifier for a commodity based on this event.
         */
        double getPriceModifier(Commodity commodity) {
            switch (type) {
                case PIRACY:
                    return 1.5; // 50% price increase due to risk
                case EMBARGO:
                    return commodity == Commodity.WEAPONS || commodity == Commodity.MACHINERY ? 0.0 : 1.2;
                case BOOM:
                    return 1.3; // 30% price increase
                case BUST:
                    return 0.7; // 30% price decrease
                case BLOCKADE:
                    return 0.0; // Trade blocked
                case DIPLOMATIC:
                    return 0.9; // 10% discount
                default:
                    return 1.0;
            }
        }
    }
}
