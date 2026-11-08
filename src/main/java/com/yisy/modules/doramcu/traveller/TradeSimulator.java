package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinator for trade simulation across a Traveller sector.
 * <p>
 * The TradeSimulator manages world economies, finds profitable trade routes,
 * executes trade missions, and tracks trade statistics across the sector.
 * </p>
 * <p>
 * It integrates with {@link TravellerSector} to provide trade opportunities
 * between all worlds in the sector, considering market conditions, price levels,
 * and commodity availability.
 * </p>
 */
public class TradeSimulator {

    private final TravellerSector sector;
    private final Map<TravellerWorld, WorldEconomy> economies;
    private final List<TradeMission> missionHistory;
    private int tick;

    /**
     * Creates a trade simulator for the given sector.
     *
     * @param sector the sector to simulate trade for
     */
    public TradeSimulator(TravellerSector sector) {
        this.sector = sector;
        this.economies = new ConcurrentHashMap<>();
        this.missionHistory = new ArrayList<>();
        this.tick = 0;

        // Initialize economies for all worlds
        for (TravellerWorld world : sector.getAllWorlds()) {
            economies.put(world, new WorldEconomy(world));
        }
    }

    // ---- Accessors ----

    /**
     * Returns the sector being simulated.
     *
     * @return the sector
     */
    public TravellerSector getSector() {
        return sector;
    }

    /**
     * Returns the economy for a specific world.
     *
     * @param world the world
     * @return the world's economy, or null if not in the sector
     */
    public WorldEconomy getEconomy(TravellerWorld world) {
        return economies.get(world);
    }

    /**
     * Returns all world economies (immutable).
     *
     * @return map of worlds to economies
     */
    public Map<TravellerWorld, WorldEconomy> getAllEconomies() {
        return Collections.unmodifiableMap(economies);
    }

    /**
     * Returns the mission history (immutable).
     *
     * @return list of completed missions
     */
    public List<TradeMission> getMissionHistory() {
        return Collections.unmodifiableList(missionHistory);
    }

    /**
     * Returns the current simulation tick.
     *
     * @return the tick number
     */
    public int getTick() {
        return tick;
    }

    // ---- Route Finding ----

    /**
     * Finds the most profitable trade route in the sector.
     * <p>
     * Searches all pairs of worlds to find the route with the highest
     * potential profit for the given ship design.
     * </p>
     *
     * @param shipDesign the ship design to use for the trade
     * @return the best trade route, or null if no profitable route exists
     */
    public TradeRouteInfo findBestTradeRoute(ShipDesign shipDesign) {
        List<TravellerWorld> worlds = sector.getAllWorlds();
        TradeRouteInfo bestRoute = null;
        long bestProfit = 0;

        for (int i = 0; i < worlds.size(); i++) {
            for (int j = i + 1; j < worlds.size(); j++) {
                TravellerWorld origin = worlds.get(i);
                TravellerWorld destination = worlds.get(j);
                WorldEconomy originEconomy = economies.get(origin);
                WorldEconomy destinationEconomy = economies.get(destination);

                if (originEconomy == null || destinationEconomy == null) continue;

                // Check both directions
                TradeRouteInfo route1 = evaluateRoute(origin, destination, originEconomy, destinationEconomy, shipDesign);
                TradeRouteInfo route2 = evaluateRoute(destination, origin, destinationEconomy, originEconomy, shipDesign);

                if (route1 != null && route1.estimatedProfit > bestProfit) {
                    bestProfit = route1.estimatedProfit;
                    bestRoute = route1;
                }
                if (route2 != null && route2.estimatedProfit > bestProfit) {
                    bestProfit = route2.estimatedProfit;
                    bestRoute = route2;
                }
            }
        }

        return bestRoute;
    }

    /**
     * Finds all profitable trade routes in the sector.
     *
     * @param shipDesign the ship design
     * @param minProfit minimum profit threshold
     * @return list of profitable routes, sorted by profit descending
     */
    public List<TradeRouteInfo> findProfitableRoutes(ShipDesign shipDesign, long minProfit) {
        List<TradeRouteInfo> routes = new ArrayList<>();
        List<TravellerWorld> worlds = sector.getAllWorlds();

        for (int i = 0; i < worlds.size(); i++) {
            for (int j = i + 1; j < worlds.size(); j++) {
                TravellerWorld origin = worlds.get(i);
                TravellerWorld destination = worlds.get(j);
                WorldEconomy originEconomy = economies.get(origin);
                WorldEconomy destinationEconomy = economies.get(destination);

                if (originEconomy == null || destinationEconomy == null) continue;

                TradeRouteInfo route1 = evaluateRoute(origin, destination, originEconomy, destinationEconomy, shipDesign);
                TradeRouteInfo route2 = evaluateRoute(destination, origin, destinationEconomy, originEconomy, shipDesign);

                if (route1 != null && route1.estimatedProfit >= minProfit) {
                    routes.add(route1);
                }
                if (route2 != null && route2.estimatedProfit >= minProfit) {
                    routes.add(route2);
                }
            }
        }

        routes.sort(Comparator.comparingLong((TradeRouteInfo r) -> r.estimatedProfit).reversed());
        return routes;
    }

    /**
     * Evaluates a single trade route between two worlds.
     *
     * @param origin the origin world
     * @param destination the destination world
     * @param originEconomy the origin economy
     * @param destinationEconomy the destination economy
     * @param shipDesign the ship design
     * @return route info, or null if no profitable trade exists
     */
    private TradeRouteInfo evaluateRoute(TravellerWorld origin, TravellerWorld destination,
                                          WorldEconomy originEconomy, WorldEconomy destinationEconomy,
                                          ShipDesign shipDesign) {
        Commodity bestCommodity = null;
        long bestProfit = 0;
        int bestUnits = 0;

        int cargoCapacity = shipDesign.getCargoTons();
        for (Commodity commodity : Commodity.values()) {
            if (!originEconomy.canBuy(commodity)) continue;
            if (!destinationEconomy.canSell(commodity)) continue;

            int buyPrice = originEconomy.getBuyPrice(commodity);
            int sellPrice = destinationEconomy.getSellPrice(commodity);
            int profitPerUnit = sellPrice - buyPrice;

            if (profitPerUnit <= 0) continue;

            int maxUnits = Math.min(
                commodity.maxUnitsForCargo(cargoCapacity),
                Math.min(originEconomy.getMaxTradeUnits(commodity), destinationEconomy.getMaxTradeUnits(commodity))
            );

            long totalProfit = (long) profitPerUnit * maxUnits;
            // Subtract travel cost
            int distance = origin.getJumpDistanceTo(destination);
            int travelCost = (shipDesign.getTotalTonnage() / 100) * Math.max(1, distance);
            totalProfit -= travelCost;

            if (totalProfit > bestProfit) {
                bestProfit = totalProfit;
                bestCommodity = commodity;
                bestUnits = maxUnits;
            }
        }

        if (bestCommodity == null) return null;

        return new TradeRouteInfo(
            origin, destination, bestCommodity, bestUnits,
            bestProfit, origin.getJumpDistanceTo(destination)
        );
    }

    // ---- Mission Execution ----

    /**
     * Executes a trade mission on the best available route.
     *
     * @param shipDesign the ship design
     * @param startingCredits the starting capital
     * @return the completed trade mission
     */
    public TradeMission executeBestMission(ShipDesign shipDesign, long startingCredits) {
        TradeRouteInfo bestRoute = findBestTradeRoute(shipDesign);
        if (bestRoute == null) {
            throw new IllegalStateException("No profitable trade routes available");
        }

        return executeMission(bestRoute.origin, bestRoute.destination, shipDesign, startingCredits);
    }

    /**
     * Executes a trade mission between specific worlds.
     *
     * @param origin the origin world
     * @param destination the destination world
     * @param shipDesign the ship design
     * @param startingCredits the starting capital
     * @return the completed trade mission
     */
    public TradeMission executeMission(TravellerWorld origin, TravellerWorld destination,
                                        ShipDesign shipDesign, long startingCredits) {
        WorldEconomy originEconomy = economies.get(origin);
        WorldEconomy destinationEconomy = economies.get(destination);

        if (originEconomy == null || destinationEconomy == null) {
            throw new IllegalArgumentException("World not in sector");
        }

        TradeMission mission = new TradeMission(origin, destination, shipDesign, startingCredits);
        TradeMission.Status status = mission.execute(originEconomy, destinationEconomy);

        missionHistory.add(mission);
        return mission;
    }

    // ---- Simulation ----

    /**
     * Advances the simulation by one tick.
     * <p>
     * Each tick simulates the passage of time, causing market conditions
     * to fluctuate across all worlds.
     * </p>
     */
    public void advanceTick() {
        tick++;
        for (WorldEconomy economy : economies.values()) {
            economy.fluctuate();
        }
    }

    /**
     * Advances the simulation by multiple ticks.
     *
     * @param ticks number of ticks to advance
     */
    public void advanceTicks(int ticks) {
        for (int i = 0; i < ticks; i++) {
            advanceTick();
        }
    }

    // ---- Statistics ----

    /**
     * Returns the total profit from all completed missions.
     *
     * @return total profit in credits
     */
    public long getTotalProfit() {
        return missionHistory.stream()
            .mapToLong(TradeMission::getNetProfit)
            .sum();
    }

    /**
     * Returns the average profit per mission.
     *
     * @return average profit in credits
     */
    public double getAverageProfit() {
        if (missionHistory.isEmpty()) return 0.0;
        return (double) getTotalProfit() / missionHistory.size();
    }

    /**
     * Returns the number of completed missions.
     *
     * @return mission count
     */
    public int getMissionCount() {
        return missionHistory.size();
    }

    /**
     * Returns the most traded commodity.
     *
     * @return the most traded commodity, or null if no missions
     */
    public Commodity getMostTradedCommodity() {
        Map<Commodity, Integer> counts = new ConcurrentHashMap<>();
        for (TradeMission mission : missionHistory) {
            for (TradeDeal deal : mission.getDeals()) {
                counts.merge(deal.getCommodity(), deal.getUnits(), Integer::sum);
            }
        }

        return counts.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Returns a summary of the trade simulator state.
     *
     * @return a formatted summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Trade Simulator Summary\n");
        sb.append("=======================\n");
        sb.append("Sector: ").append(sector.getName()).append("\n");
        sb.append("Tick: ").append(tick).append("\n");
        sb.append("Worlds: ").append(economies.size()).append("\n");
        sb.append("Missions: ").append(missionHistory.size()).append("\n");
        sb.append("Total Profit: ").append(getTotalProfit()).append(" cr\n");
        sb.append("Average Profit: ").append(String.format("%.0f", getAverageProfit())).append(" cr\n");

        Commodity mostTraded = getMostTradedCommodity();
        if (mostTraded != null) {
            sb.append("Most Traded: ").append(mostTraded.getName()).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getSummary();
    }

    /**
     * Information about a potential trade route.
     */
    public static class TradeRouteInfo {
        private final TravellerWorld origin;
        private final TravellerWorld destination;
        private final Commodity bestCommodity;
        private final int recommendedUnits;
        private final long estimatedProfit;
        private final int distance;

        TradeRouteInfo(TravellerWorld origin, TravellerWorld destination,
                       Commodity bestCommodity, int recommendedUnits,
                       long estimatedProfit, int distance) {
            this.origin = origin;
            this.destination = destination;
            this.bestCommodity = bestCommodity;
            this.recommendedUnits = recommendedUnits;
            this.estimatedProfit = estimatedProfit;
            this.distance = distance;
        }

        public TravellerWorld getOrigin() {
            return origin;
        }

        public TravellerWorld getDestination() {
            return destination;
        }

        public Commodity getBestCommodity() {
            return bestCommodity;
        }

        public int getRecommendedUnits() {
            return recommendedUnits;
        }

        public long getEstimatedProfit() {
            return estimatedProfit;
        }

        public int getDistance() {
            return distance;
        }

        @Override
        public String toString() {
            return String.format("%s → %s: %d units of %s, profit %d cr (%d jumps)",
                origin.getName(), destination.getName(),
                recommendedUnits, bestCommodity.getName(),
                estimatedProfit, distance);
        }
    }
}
