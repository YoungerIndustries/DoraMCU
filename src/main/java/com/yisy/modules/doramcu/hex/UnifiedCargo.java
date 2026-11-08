package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.*;

/**
 * A unified cargo system that integrates Traveller's CargoHold with DoraMCU ships.
 * <p>
 * This class provides commodity-based cargo management for UnifiedShip,
 * combining Traveller's bulk-ton cargo system with DoraMCU's resource storage.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Commodity-based cargo with bulk tons (from Traveller)</li>
 *   <li>Cargo capacity derived from ship tonnage</li>
 *   <li>Load/unload operations with history tracking</li>
 *   <li>Market value calculation</li>
 *   <li>Integration with SpaceStation markets</li>
 * </ul>
 */
public class UnifiedCargo {

    private final CargoHold cargoHold;
    private final Ship ship;

    /**
     * Creates a unified cargo system for the given ship.
     *
     * @param ship the ship that owns this cargo hold
     */
    public UnifiedCargo(Ship ship) {
        this.ship = Objects.requireNonNull(ship, "Ship must not be null");
        // Cargo capacity based on ship tonnage
        int capacityTons;
        if (ship instanceof UnifiedShip us) {
            capacityTons = us.getTotalTonnage() / 10; // 10% of tonnage for cargo
        } else {
            capacityTons = 100; // Default for non-UnifiedShips
        }
        this.cargoHold = new CargoHold(Math.max(10, capacityTons));
    }

    /**
     * Creates a unified cargo system with a specific capacity.
     *
     * @param ship the ship that owns this cargo hold
     * @param capacityTons the cargo capacity in tons
     */
    public UnifiedCargo(Ship ship, int capacityTons) {
        this.ship = Objects.requireNonNull(ship, "Ship must not be null");
        this.cargoHold = new CargoHold(capacityTons);
    }

    // ---- Accessors ----

    /**
     * Returns the underlying CargoHold.
     */
    public CargoHold getCargoHold() {
        return cargoHold;
    }

    /**
     * Returns the ship that owns this cargo hold.
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Returns the total cargo capacity in tons.
     */
    public int getCapacityTons() {
        return cargoHold.getCapacityTons();
    }

    /**
     * Returns the current cargo weight in tons.
     */
    public double getCurrentWeightTons() {
        return cargoHold.getCurrentWeightTons();
    }

    /**
     * Returns the remaining cargo capacity in tons.
     */
    public double getRemainingCapacityTons() {
        return cargoHold.getRemainingCapacityTons();
    }

    /**
     * Checks if the cargo hold is full.
     */
    public boolean isFull() {
        return cargoHold.isFull();
    }

    /**
     * Checks if the cargo hold is empty.
     */
    public boolean isEmpty() {
        return cargoHold.isEmpty();
    }

    /**
     * Returns the quantity of a specific commodity.
     */
    public int getQuantity(Commodity commodity) {
        return cargoHold.getQuantity(commodity);
    }

    /**
     * Returns the set of commodities currently in the cargo hold.
     */
    public Set<Commodity> getCommodities() {
        return cargoHold.getCommodities();
    }

    /**
     * Returns an unmodifiable map of all commodities and their quantities.
     */
    public Map<Commodity, Integer> getCargoMap() {
        return cargoHold.getCargoMap();
    }

    // ---- Cargo Operations ----

    /**
     * Loads cargo onto the ship.
     *
     * @param commodity the commodity to load
     * @param units the number of units to load
     * @return the actual number of units loaded
     */
    public int loadCargo(Commodity commodity, int units) {
        return cargoHold.loadCargo(commodity, units);
    }

    /**
     * Unloads cargo from the ship.
     *
     * @param commodity the commodity to unload
     * @param units the number of units to unload
     * @return the actual number of units unloaded
     */
    public int unloadCargo(Commodity commodity, int units) {
        return cargoHold.unloadCargo(commodity, units);
    }

    /**
     * Unloads all cargo of a specific commodity.
     */
    public int unloadAll(Commodity commodity) {
        return cargoHold.unloadAll(commodity);
    }

    /**
     * Unloads all cargo from the ship.
     */
    public int unloadAll() {
        return cargoHold.unloadAll();
    }

    /**
     * Checks how many units of a commodity can be loaded.
     */
    public int getMaxLoadable(Commodity commodity) {
        return cargoHold.getMaxLoadable(commodity);
    }

    /**
     * Calculates the total value of cargo at the given prices.
     */
    public long getTotalValue(WorldEconomy economy) {
        return cargoHold.getTotalValue(economy);
    }

    /**
     * Returns the cargo loading history.
     */
    public List<CargoHold.CargoEntry> getCargoHistory() {
        return cargoHold.getCargoHistory();
    }

    /**
     * Clears the cargo history.
     */
    public void clearHistory() {
        cargoHold.clearHistory();
    }

    /**
     * Transfers cargo from this ship to another ship's cargo hold.
     *
     * @param commodity the commodity to transfer
     * @param units the number of units to transfer
     * @param destination the destination cargo hold
     * @return the number of units transferred
     */
    public int transferTo(Commodity commodity, int units, UnifiedCargo destination) {
        int unloaded = cargoHold.unloadCargo(commodity, units);
        if (unloaded <= 0) return 0;
        return destination.cargoHold.loadCargo(commodity, unloaded);
    }

    /**
     * Transfers cargo from this ship to a station's market.
     *
     * @param commodity the commodity to sell
     * @param units the number of units to sell
     * @param station the station to sell at
     * @return the revenue in credits
     */
    public int sellAtStation(Commodity commodity, int units, UnifiedSpaceStation station) {
        int available = cargoHold.getQuantity(commodity);
        int toSell = Math.min(units, available);
        if (toSell <= 0) return 0;

        int revenue = station.sellCargo(commodity, toSell);
        cargoHold.unloadCargo(commodity, toSell);
        return revenue;
    }

    /**
     * Buys cargo from a station's market and loads it onto the ship.
     *
     * @param commodity the commodity to buy
     * @param units the number of units to buy
     * @param station the station to buy from
     * @return the cost in credits
     */
    public int buyFromStation(Commodity commodity, int units, UnifiedSpaceStation station) {
        int maxLoadable = cargoHold.getMaxLoadable(commodity);
        int toBuy = Math.min(units, maxLoadable);
        if (toBuy <= 0) return 0;

        int cost = station.buyCargo(commodity, toBuy);
        cargoHold.loadCargo(commodity, toBuy);
        return cost;
    }

    @Override
    public String toString() {
        return cargoHold.getSummary();
    }
}
