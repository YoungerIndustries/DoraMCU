package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A space station orbiting a Traveller world.
 * <p>
 * Space stations provide essential services for interstellar travel:
 * refueling, repair, trading, ship construction, and more. The station's
 * capabilities are determined by its type (Alpha through Outpost) and
 * the facilities it contains.
 * </p>
 * <p>
 * In Classic Traveller, every world with a starport code has an associated
 * space station. The station type is derived from the UWP starport code,
 * and the available facilities depend on the station type and world conditions.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   // Create a station for a world with Alpha starport
 *   TravellerWorld world = ...; // UWP with starport A
 *   SpaceStation station = SpaceStation.fromWorld(world);
 *
 *   // Dock a ship and refuel
 *   station.dockShip(ship);
 *   station.refuel(ship.getFuelTank(), true); // Full tank
 * </pre>
 */
public class SpaceStation {

    private final int id;
    private final String name;
    private final TravellerWorld world;
    private final StationType stationType;
    private final Set<StationFacility> facilities;
    private final StationDock dock;
    private final StationMarket market;
    private final StationService service;
    private boolean operational;
    private final List<String> eventLog;

    /**
     * Creates a new space station.
     *
     * @param id the unique station identifier
     * @param name the name of the station
     * @param world the world the station orbits
     * @param stationType the type of station (Alpha through Outpost)
     * @param facilities the set of facilities available
     */
    public SpaceStation(int id, String name, TravellerWorld world,
                         StationType stationType, Set<StationFacility> facilities) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Station name must not be null");
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.stationType = Objects.requireNonNull(stationType, "Station type must not be null");
        this.facilities = new HashSet<>(facilities);
        this.dock = new StationDock(this);
        this.market = new StationMarket(this, world);
        this.service = new StationService(this);
        this.operational = true;
        this.eventLog = new ArrayList<>();
    }

    /**
     * Creates a space station derived from a world's UWP.
     * <p>
     * The station type is determined by the UWP starport code, and
     * facilities are automatically assigned based on the station type
     * and world trade codes.
     * </p>
     *
     * @param world the world to create the station for
     * @return the new space station, or null if the world has no starport
     */
    public static SpaceStation fromWorld(TravellerWorld world) {
        Objects.requireNonNull(world, "World must not be null");
        UniversalWorldProfile uwp = world.getUwp();
        int starportCode = uwp.getStarport();

        StationType stationType = StationType.fromStarportCode(starportCode);
        if (stationType == null) {
            return null; // No starport
        }

        // Derive facilities from station type and trade codes
        Set<StationFacility> facilities = deriveFacilities(world, stationType);

        String name = world.getName() + " Orbital";
        return new SpaceStation(world.getId(), name, world, stationType, facilities);
    }

    /**
     * Derives the set of facilities based on station type and world conditions.
     *
     * @param world the world
     * @param stationType the station type
     * @return the set of facilities
     */
    private static Set<StationFacility> deriveFacilities(TravellerWorld world, StationType stationType) {
        Set<StationFacility> facilities = new HashSet<>();

        // Essential facilities are always available (except Outpost)
        if (stationType != StationType.OUTPOST) {
            facilities.add(StationFacility.REFUELING);
            facilities.add(StationFacility.REPAIR);
        }

        // Trade facilities based on station type
        if (stationType.getMaxFacilities() >= 1) {
            facilities.add(StationFacility.MARKET);
            facilities.add(StationFacility.CUSTOMS);
        }

        // Support facilities based on station type
        if (stationType.getMaxFacilities() >= 3) {
            facilities.add(StationFacility.PASSENGER);
            facilities.add(StationFacility.CREW_QUARTERS);
            facilities.add(StationFacility.INFORMATION);
        }

        // Construction facilities for larger stations
        if (stationType.getMaxFacilities() >= 5) {
            facilities.add(StationFacility.SHIPYARD);
            facilities.add(StationFacility.MEDICAL);
        }

        // Military facilities based on world conditions
        if (world.hasTradeCode(TradeCode.MO) || world.isCapital()) {
            facilities.add(StationFacility.MILITARY);
        }

        return facilities;
    }

    // ---- Accessors ----

    /**
     * @return the unique station identifier
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name of the station
     */
    public String getName() {
        return name;
    }

    /**
     * @return the world the station orbits
     */
    public TravellerWorld getWorld() {
        return world;
    }

    /**
     * @return the type of station (Alpha through Outpost)
     */
    public StationType getStationType() {
        return stationType;
    }

    /**
     * @return the set of available facilities
     */
    public Set<StationFacility> getFacilities() {
        return Collections.unmodifiableSet(facilities);
    }

    /**
     * @return the docking manager
     */
    public StationDock getDock() {
        return dock;
    }

    /**
     * @return the station market
     */
    public StationMarket getMarket() {
        return market;
    }

    /**
     * @return the station service provider
     */
    public StationService getService() {
        return service;
    }

    /**
     * Checks if the station is operational.
     *
     * @return true if the station is operational
     */
    public boolean isOperational() {
        return operational;
    }

    /**
     * Sets the operational status of the station.
     *
     * @param operational the operational status
     */
    public void setOperational(boolean operational) {
        this.operational = operational;
        logEvent("Station " + (operational ? "became operational" : "went offline"));
    }

    /**
     * Checks if a specific facility is available.
     *
     * @param facility the facility to check
     * @return true if the facility is available and operational
     */
    public boolean hasFacility(StationFacility facility) {
        return operational && facilities.contains(facility);
    }

    /**
     * @return the number of ships currently docked
     */
    public int getDockedShipCount() {
        return dock.getDockedShipCount();
    }

    /**
     * @return true if no more ships can dock
     */
    public boolean isDockFull() {
        return dock.isFull();
    }

    /**
     * @return the list of event log entries
     */
    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    // ---- Convenience Methods ----

    /**
     * Docks a ship at this station.
     *
     * @param shipName the name of the ship
     * @param tonnage the tonnage of the ship
     * @return true if the ship was successfully docked
     */
    public boolean dockShip(String shipName, int tonnage) {
        if (!operational) return false;
        boolean result = dock.dockShip(shipName, tonnage);
        if (result) {
            logEvent("Ship " + shipName + " docked");
        }
        return result;
    }

    /**
     * Undocks a ship from this station.
     *
     * @param shipName the name of the ship
     * @return true if the ship was successfully undocked
     */
    public boolean undockShip(String shipName) {
        boolean result = dock.undockShip(shipName);
        if (result) {
            logEvent("Ship " + shipName + " undocked");
        }
        return result;
    }

    /**
     * Refuels a ship's fuel tank at this station.
     *
     * @param fuelTank the ship's fuel tank
     * @param fullTank true to fill the tank completely
     * @return the cost in credits
     */
    public int refuel(FuelTank fuelTank, boolean fullTank) {
        if (!hasFacility(StationFacility.REFUELING)) {
            throw new IllegalStateException("Refueling facility not available");
        }
        int cost = service.refuel(fuelTank, fullTank);
        logEvent("Refueled ship, cost: " + cost + " credits");
        return cost;
    }

    /**
     * Repairs a ship's hull at this station.
     *
     * @param currentHull the ship's current hull points
     * @param maxHull the ship's maximum hull points
     * @return the cost in credits
     */
    public int repairHull(int currentHull, int maxHull) {
        if (!hasFacility(StationFacility.REPAIR)) {
            throw new IllegalStateException("Repair facility not available");
        }
        int cost = service.repairHull(currentHull, maxHull);
        logEvent("Repaired hull, cost: " + cost + " credits");
        return cost;
    }

    /**
     * Buys cargo at this station's market.
     *
     * @param commodity the commodity to buy
     * @param units the number of units to buy
     * @return the cost in credits
     */
    public int buyCargo(Commodity commodity, int units) {
        if (!hasFacility(StationFacility.MARKET)) {
            throw new IllegalStateException("Market not available");
        }
        int cost = market.buy(commodity, units);
        logEvent("Bought " + units + " units of " + commodity + " for " + cost + " credits");
        return cost;
    }

    /**
     * Sells cargo at this station's market.
     *
     * @param commodity the commodity to sell
     * @param units the number of units to sell
     * @return the revenue in credits
     */
    public int sellCargo(Commodity commodity, int units) {
        if (!hasFacility(StationFacility.MARKET)) {
            throw new IllegalStateException("Market not available");
        }
        int revenue = market.sell(commodity, units);
        logEvent("Sold " + units + " units of " + commodity + " for " + revenue + " credits");
        return revenue;
    }

    /**
     * Adds a facility to the station.
     * <p>
     * Only available if the station type can support more facilities.
     * </p>
     *
     * @param facility the facility to add
     * @return true if the facility was added
     */
    public boolean addFacility(StationFacility facility) {
        if (facilities.size() >= stationType.getMaxFacilities()) {
            return false;
        }
        boolean added = facilities.add(facility);
        if (added) {
            logEvent("Added facility: " + facility.getName());
        }
        return added;
    }

    /**
     * Removes a facility from the station.
     *
     * @param facility the facility to remove
     * @return true if the facility was removed
     */
    public boolean removeFacility(StationFacility facility) {
        boolean removed = facilities.remove(facility);
        if (removed) {
            logEvent("Removed facility: " + facility.getName());
        }
        return removed;
    }

    /**
     * Upgrades the station to the next larger type.
     *
     * @return true if the station was upgraded
     */
    public boolean upgrade() {
        StationType nextType = stationType.upgrade();
        if (nextType == null) {
            return false; // Already maximum
        }
        // Note: In a full implementation, this would require construction time and cost
        logEvent("Station upgraded from " + stationType + " to " + nextType);
        return true;
    }

    private void logEvent(String event) {
        eventLog.add(event);
    }

    @Override
    public String toString() {
        return name + " (" + stationType + " station at " + world.getName() + ")";
    }
}
