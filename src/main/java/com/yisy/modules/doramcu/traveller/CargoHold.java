package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages cargo storage on a Traveller starship.
 * <p>
 * Tracks which commodities are loaded, their quantities, and the total
 * bulk weight consumed. Cargo capacity is determined by the ship's
 * design cargo allocation.
 * </p>
 * <p>
 * In Classic Traveller, cargo is measured in tons. Each commodity has
 * a bulk factor (tons per unit) that determines how many units fit
 * in the available cargo space.
 * </p>
 */
public class CargoHold {

    private final int capacityTons;
    private final Map<Commodity, Integer> cargo;
    private final List<CargoEntry> cargoHistory;

    /**
     * Creates an empty cargo hold with the given capacity.
     *
     * @param capacityTons the maximum cargo capacity in tons
     */
    public CargoHold(int capacityTons) {
        if (capacityTons < 0) {
            throw new IllegalArgumentException("Cargo capacity cannot be negative");
        }
        this.capacityTons = capacityTons;
        this.cargo = new EnumMap<>(Commodity.class);
        this.cargoHistory = new ArrayList<>();
    }

    /**
     * Creates a cargo hold from a ship design.
     *
     * @param design the ship design
     */
    public CargoHold(ShipDesign design) {
        this(design.getCargoTons());
    }

    // ---- Accessors ----

    /**
     * Returns the total cargo capacity in tons.
     *
     * @return capacity in tons
     */
    public int getCapacityTons() {
        return capacityTons;
    }

    /**
     * Returns the current cargo weight in tons.
     *
     * @return current weight in tons
     */
    public double getCurrentWeightTons() {
        double total = 0;
        for (Map.Entry<Commodity, Integer> entry : cargo.entrySet()) {
            total += entry.getKey().getBulkTons() * entry.getValue();
        }
        return total;
    }

    /**
     * Returns the remaining cargo capacity in tons.
     *
     * @return remaining capacity in tons
     */
    public double getRemainingCapacityTons() {
        return capacityTons - getCurrentWeightTons();
    }

    /**
     * Checks if the cargo hold is full.
     *
     * @return true if no more cargo can be loaded
     */
    public boolean isFull() {
        return getRemainingCapacityTons() <= 0;
    }

    /**
     * Checks if the cargo hold is empty.
     *
     * @return true if no cargo is loaded
     */
    public boolean isEmpty() {
        return cargo.isEmpty();
    }

    // ---- Cargo Operations ----

    /**
     * Returns the quantity of a specific commodity in the cargo hold.
     *
     * @param commodity the commodity
     * @return the quantity in units
     */
    public int getQuantity(Commodity commodity) {
        return cargo.getOrDefault(commodity, 0);
    }

    /**
     * Loads cargo onto the ship.
     *
     * @param commodity the commodity to load
     * @param units the number of units to load
     * @return the actual number of units loaded (may be less than requested if space is limited)
     */
    public int loadCargo(Commodity commodity, int units) {
        if (units <= 0) return 0;

        double tonsRequired = commodity.tonsForUnits(units);
        double remaining = getRemainingCapacityTons();

        // Calculate actual units that can be loaded
        int actualUnits = units;
        if (commodity.getBulkTons() > 0 && tonsRequired > remaining) {
            actualUnits = (int) (remaining / commodity.getBulkTons());
            if (actualUnits <= 0) return 0;
        }

        // Add to cargo
        int current = cargo.getOrDefault(commodity, 0);
        cargo.put(commodity, current + actualUnits);

        // Record in history
        cargoHistory.add(new CargoEntry(commodity, actualUnits, CargoEntry.Operation.LOAD));

        return actualUnits;
    }

    /**
     * Unloads cargo from the ship.
     *
     * @param commodity the commodity to unload
     * @param units the number of units to unload
     * @return the actual number of units unloaded (may be less than requested)
     */
    public int unloadCargo(Commodity commodity, int units) {
        if (units <= 0) return 0;

        int current = cargo.getOrDefault(commodity, 0);
        if (current <= 0) return 0;

        int actualUnits = Math.min(units, current);

        // Remove from cargo
        int remaining = current - actualUnits;
        if (remaining <= 0) {
            cargo.remove(commodity);
        } else {
            cargo.put(commodity, remaining);
        }

        // Record in history
        cargoHistory.add(new CargoEntry(commodity, actualUnits, CargoEntry.Operation.UNLOAD));

        return actualUnits;
    }

    /**
     * Unloads all cargo of a specific commodity.
     *
     * @param commodity the commodity to unload
     * @return the number of units unloaded
     */
    public int unloadAll(Commodity commodity) {
        int current = cargo.getOrDefault(commodity, 0);
        if (current <= 0) return 0;

        cargo.remove(commodity);
        cargoHistory.add(new CargoEntry(commodity, current, CargoEntry.Operation.UNLOAD));

        return current;
    }

    /**
     * Unloads all cargo from the ship.
     *
     * @return the total number of units unloaded
     */
    public int unloadAll() {
        int total = 0;
        for (Commodity commodity : cargo.keySet()) {
            total += unloadAll(commodity);
        }
        return total;
    }

    /**
     * Checks how many units of a commodity can be loaded.
     *
     * @param commodity the commodity
     * @return the maximum units that can be loaded
     */
    public int getMaxLoadable(Commodity commodity) {
        if (commodity.getBulkTons() <= 0) {
            return 1000; // zero-bulk items have arbitrary high limit
        }
        return (int) (getRemainingCapacityTons() / commodity.getBulkTons());
    }

    /**
     * Returns the set of commodities currently in the cargo hold.
     *
     * @return set of commodities with quantity > 0
     */
    public Set<Commodity> getCommodities() {
        return Collections.unmodifiableSet(cargo.keySet());
    }

    /**
     * Returns an unmodifiable map of all commodities and their quantities.
     *
     * @return the cargo map
     */
    public Map<Commodity, Integer> getCargoMap() {
        return Collections.unmodifiableMap(cargo);
    }

    /**
     * Calculates the total value of cargo at the given prices.
     *
     * @param economy the economy to get prices from
     * @return total value in credits
     */
    public long getTotalValue(WorldEconomy economy) {
        long total = 0;
        for (Map.Entry<Commodity, Integer> entry : cargo.entrySet()) {
            int price = economy.getSellPrice(entry.getKey());
            total += (long) price * entry.getValue();
        }
        return total;
    }

    /**
     * Returns the cargo loading history.
     *
     * @return list of cargo entries (immutable)
     */
    public List<CargoEntry> getCargoHistory() {
        return Collections.unmodifiableList(cargoHistory);
    }

    /**
     * Clears the cargo history.
     */
    public void clearHistory() {
        cargoHistory.clear();
    }

    /**
     * Returns a summary of the cargo hold.
     *
     * @return a formatted string describing the cargo
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("CargoHold{").append((int) getCurrentWeightTons()).append("/")
          .append(capacityTons).append(" tons: ");

        boolean first = true;
        for (Map.Entry<Commodity, Integer> entry : cargo.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey().getName()).append("=").append(entry.getValue());
            first = false;
        }

        if (cargo.isEmpty()) {
            sb.append("(empty)");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getSummary();
    }

    /**
     * A single cargo entry (load or unload operation).
     */
    public static class CargoEntry {
        private final Commodity commodity;
        private final int units;
        private final Operation operation;

        public CargoEntry(Commodity commodity, int units, Operation operation) {
            this.commodity = commodity;
            this.units = units;
            this.operation = operation;
        }

        public Commodity getCommodity() {
            return commodity;
        }

        public int getUnits() {
            return units;
        }

        public Operation getOperation() {
            return operation;
        }

        @Override
        public String toString() {
            return String.format("%s %s %d units", operation, commodity.getName(), units);
        }

        /**
         * Cargo operation type.
         */
        public enum Operation {
            /**
             * Cargo loaded onto the ship.
             */
            LOAD,

            /**
             * Cargo unloaded from the ship.
             */
            UNLOAD;
        }
    }
}
