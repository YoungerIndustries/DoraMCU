package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a complete trade mission from origin to destination.
 * <p>
 * A trade mission includes:
 * </p>
 * <ul>
 *   <li>Loading cargo at the origin world</li>
 *   <li>Traveling to the destination world</li>
 *   <li>Unloading and selling cargo at the destination</li>
 *   <li>Calculating profit after expenses</li>
 * </ul>
 * <p>
 * The mission tracks all trade deals, cargo operations, travel costs,
 * and the final profit/loss for the trip.
 * </p>
 */
public class TradeMission {

    private final TravellerWorld origin;
    private final TravellerWorld destination;
    private final ShipDesign shipDesign;
    private final CargoHold cargoHold;
    private final List<TradeDeal> deals;
    private final List<String> events;
    private final long startingCredits;
    private long totalSpent;
    private long totalEarned;
    private Status status;
    private String failureReason;

    /**
     * Creates a new trade mission.
     *
     * @param origin the origin world
     * @param destination the destination world
     * @param shipDesign the ship used for the mission
     * @param startingCredits the starting capital in credits
     */
    public TradeMission(TravellerWorld origin, TravellerWorld destination,
                        ShipDesign shipDesign, long startingCredits) {
        if (startingCredits < 0) {
            throw new IllegalArgumentException("Starting credits cannot be negative");
        }
        this.origin = origin;
        this.destination = destination;
        this.shipDesign = shipDesign;
        this.cargoHold = new CargoHold(shipDesign);
        this.deals = new ArrayList<>();
        this.events = new ArrayList<>();
        this.startingCredits = startingCredits;
        this.totalSpent = 0;
        this.totalEarned = 0;
        this.status = Status.PLANNED;
        this.failureReason = null;
    }

    // ---- Accessors ----

    /**
     * Returns the origin world.
     *
     * @return the origin world
     */
    public TravellerWorld getOrigin() {
        return origin;
    }

    /**
     * Returns the destination world.
     *
     * @return the destination world
     */
    public TravellerWorld getDestination() {
        return destination;
    }

    /**
     * Returns the ship design used for this mission.
     *
     * @return the ship design
     */
    public ShipDesign getShipDesign() {
        return shipDesign;
    }

    /**
     * Returns the cargo hold.
     *
     * @return the cargo hold
     */
    public CargoHold getCargoHold() {
        return cargoHold;
    }

    /**
     * Returns the list of trade deals (immutable).
     *
     * @return the trade deals
     */
    public List<TradeDeal> getDeals() {
        return Collections.unmodifiableList(deals);
    }

    /**
     * Returns the list of event log entries (immutable).
     *
     * @return the event log
     */
    public List<String> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Returns the starting capital.
     *
     * @return starting credits
     */
    public long getStartingCredits() {
        return startingCredits;
    }

    /**
     * Returns the total credits spent on this mission.
     *
     * @return total spent
     */
    public long getTotalSpent() {
        return totalSpent;
    }

    /**
     * Returns the total credits earned on this mission.
     *
     * @return total earned
     */
    public long getTotalEarned() {
        return totalEarned;
    }

    /**
     * Returns the current status of the mission.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the reason for mission failure, if applicable.
     *
     * @return the failure reason, or null if not failed
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Returns the current credits (starting + earned - spent).
     *
     * @return current credits
     */
    public long getCurrentCredits() {
        return startingCredits + totalEarned - totalSpent;
    }

    /**
     * Returns the net profit/loss for this mission.
     *
     * @return profit (positive) or loss (negative)
     */
    public long getNetProfit() {
        return totalEarned - totalSpent;
    }

    /**
     * Returns the return on investment percentage.
     *
     * @return ROI as a percentage (e.g., 50.0 means 50% return)
     */
    public double getReturnOnInvestment() {
        if (totalSpent == 0) return 0.0;
        return ((double) getNetProfit() / totalSpent) * 100.0;
    }

    // ---- Mission Execution ----

    /**
     * Loads cargo at the origin world.
     * <p>
     * Buys the most profitable commodities that fit in the cargo hold,
     * prioritizing by profit margin.
     * </p>
     *
     * @param originEconomy the origin world's economy
     * @param destinationEconomy the destination economy (to calculate profit potential)
     * @return true if cargo was loaded successfully
     */
    public boolean loadCargo(WorldEconomy originEconomy, WorldEconomy destinationEconomy) {
        if (status != Status.PLANNED) {
            fail("Cannot load cargo: mission is " + status);
            return false;
        }

        events.add("Arrived at " + origin.getName() + " — loading cargo");

        // Find best commodities to buy
        List<CommodityCandidate> candidates = new ArrayList<>();
        for (Commodity commodity : Commodity.values()) {
            if (!originEconomy.canBuy(commodity)) continue;
            if (!destinationEconomy.canSell(commodity)) continue;

            int buyPrice = originEconomy.getBuyPrice(commodity);
            int sellPrice = destinationEconomy.getSellPrice(commodity);
            int profitPerUnit = sellPrice - buyPrice;

            if (profitPerUnit > 0) {
                int maxUnits = Math.min(
                    commodity.maxUnitsForCargo((int) cargoHold.getRemainingCapacityTons()),
                    originEconomy.getMaxTradeUnits(commodity)
                );
                candidates.add(new CommodityCandidate(commodity, profitPerUnit, maxUnits, buyPrice));
            }
        }

        // Sort by profit per ton (value density)
        candidates.sort((a, b) -> {
            double aDensity = a.profitPerUnit / Math.max(0.01, a.commodity.getBulkTons());
            double bDensity = b.profitPerUnit / Math.max(0.01, b.commodity.getBulkTons());
            return Double.compare(bDensity, aDensity);
        });

        // Buy commodities in order of profitability
        for (CommodityCandidate candidate : candidates) {
            if (cargoHold.isFull()) break;
            if (getCurrentCredits() <= 0) break;

            int units = candidate.maxUnits;
            int buyPrice = candidate.buyPrice;

            // Limit by available credits
            long maxAffordable = getCurrentCredits() / buyPrice;
            units = Math.min(units, (int) maxAffordable);

            if (units <= 0) continue;

            // Load cargo
            int loaded = cargoHold.loadCargo(candidate.commodity, units);
            if (loaded > 0) {
                long cost = (long) loaded * buyPrice;
                totalSpent += cost;

                TradeDeal deal = new TradeDeal(
                    TradeDeal.Type.BUY, candidate.commodity, loaded, buyPrice, origin
                );
                deals.add(deal);

                events.add("Bought " + loaded + "x " + candidate.commodity.getName() +
                          " @ " + buyPrice + " cr = " + cost + " cr");
            }
        }

        if (cargoHold.isEmpty()) {
            events.add("No profitable cargo available at " + origin.getName());
        } else {
            events.add("Cargo loaded: " + cargoHold.getSummary());
        }

        status = Status.LOADED;
        return true;
    }

    /**
     * Simulates travel from origin to destination.
     *
     * @return true if travel was successful
     */
    public boolean travel() {
        if (status != Status.LOADED) {
            fail("Cannot travel: mission is " + status + " (expected LOADED)");
            return false;
        }

        events.add("Departing " + origin.getName() + " for " + destination.getName());

        // Calculate travel cost (fuel consumption)
        int distance = origin.getJumpDistanceTo(destination);
        int fuelCostPerJump = shipDesign.getTotalTonnage() / 100; // 1% of tonnage per jump
        int jumps = Math.max(1, distance);
        int totalFuelCost = fuelCostPerJump * jumps;

        totalSpent += totalFuelCost;
        events.add("Travel cost: " + jumps + " jump(s) × " + fuelCostPerJump +
                  " cr fuel = " + totalFuelCost + " cr");

        // Check for random events (piracy, equipment failure, etc.)
        // Simplified — no random events in base implementation
        events.add("Arrived at " + destination.getName());

        status = Status.IN_TRANSIT_COMPLETE;
        return true;
    }

    /**
     * Unloads and sells cargo at the destination world.
     *
     * @param destinationEconomy the destination world's economy
     * @return true if cargo was sold successfully
     */
    public boolean unloadCargo(WorldEconomy destinationEconomy) {
        if (status != Status.IN_TRANSIT_COMPLETE) {
            fail("Cannot unload: mission is " + status + " (expected IN_TRANSIT_COMPLETE)");
            return false;
        }

        events.add("Selling cargo at " + destination.getName());

        long totalRevenue = 0;
        int totalUnitsSold = 0;

        for (Commodity commodity : cargoHold.getCommodities()) {
            int units = cargoHold.getQuantity(commodity);
            if (units <= 0) continue;

            if (!destinationEconomy.canSell(commodity)) {
                events.add("Cannot sell " + commodity.getName() + " on " + destination.getName());
                continue;
            }

            int sellPrice = destinationEconomy.getSellPrice(commodity);
            int actualUnits = Math.min(units, destinationEconomy.getMaxTradeUnits(commodity));

            int unloaded = cargoHold.unloadCargo(commodity, actualUnits);
            long revenue = (long) unloaded * sellPrice;
            totalRevenue += revenue;
            totalUnitsSold += unloaded;

            TradeDeal deal = new TradeDeal(
                TradeDeal.Type.SELL, commodity, unloaded, sellPrice, destination
            );
            deals.add(deal);

            events.add("Sold " + unloaded + "x " + commodity.getName() +
                      " @ " + sellPrice + " cr = " + revenue + " cr");
        }

        totalEarned += totalRevenue;

        // Unload any remaining unsold cargo
        int remaining = cargoHold.unloadAll();
        if (remaining > 0) {
            events.add("Discarded " + remaining + " units of unsold cargo");
        }

        events.add("Total revenue: " + totalRevenue + " cr");
        events.add("Net profit: " + getNetProfit() + " cr (" +
                  String.format("%.1f", getReturnOnInvestment()) + "% ROI)");

        if (getNetProfit() >= 0) {
            status = Status.COMPLETED;
        } else {
            status = Status.COMPLETED_WITH_LOSS;
        }

        return true;
    }

    /**
     * Executes the complete trade mission automatically.
     *
     * @param originEconomy the origin world's economy
     * @param destinationEconomy the destination world's economy
     * @return the final status of the mission
     */
    public Status execute(WorldEconomy originEconomy, WorldEconomy destinationEconomy) {
        if (status != Status.PLANNED) {
            fail("Mission already executed");
            return status;
        }

        if (!loadCargo(originEconomy, destinationEconomy)) {
            return status;
        }

        if (!travel()) {
            return status;
        }

        if (!unloadCargo(destinationEconomy)) {
            return status;
        }

        return status;
    }

    /**
     * Marks the mission as failed with a reason.
     */
    private void fail(String reason) {
        this.failureReason = reason;
        this.status = Status.FAILED;
        events.add("MISSION FAILED: " + reason);
    }

    /**
     * Returns a summary of the trade mission.
     *
     * @return a formatted summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Trade Mission: ").append(origin.getName())
          .append(" → ").append(destination.getName()).append("\n");
        sb.append("Ship: ").append(shipDesign.getName()).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Starting Credits: ").append(startingCredits).append(" cr\n");
        sb.append("Total Spent: ").append(totalSpent).append(" cr\n");
        sb.append("Total Earned: ").append(totalEarned).append(" cr\n");
        sb.append("Net Profit: ").append(getNetProfit()).append(" cr\n");
        sb.append("ROI: ").append(String.format("%.1f", getReturnOnInvestment())).append("%\n");
        sb.append("Deals: ").append(deals.size()).append("\n");

        if (failureReason != null) {
            sb.append("Failure: ").append(failureReason).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getSummary();
    }

    /**
     * Mission status.
     */
    public enum Status {
        /**
         * Mission is planned but not yet started.
         */
        PLANNED,

        /**
         * Cargo has been loaded at the origin.
         */
        LOADED,

        /**
         * Travel is complete, arrived at destination.
         */
        IN_TRANSIT_COMPLETE,

        /**
         * Mission completed successfully with profit.
         */
        COMPLETED,

        /**
         * Mission completed but resulted in a loss.
         */
        COMPLETED_WITH_LOSS,

        /**
         * Mission failed.
         */
        FAILED;
    }

    /**
     * Candidate commodity for purchase, sorted by profitability.
     */
    private static class CommodityCandidate {
        final Commodity commodity;
        final int profitPerUnit;
        final int maxUnits;
        final int buyPrice;

        CommodityCandidate(Commodity commodity, int profitPerUnit, int maxUnits, int buyPrice) {
            this.commodity = commodity;
            this.profitPerUnit = profitPerUnit;
            this.maxUnits = maxUnits;
            this.buyPrice = buyPrice;
        }
    }
}
