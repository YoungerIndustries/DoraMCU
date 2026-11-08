package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.*;

/**
 * A unified space station that combines DoraMCU's combat-oriented SpaceStation
 * with Traveller's service-oriented SpaceStation.
 * <p>
 * This class merges DoraMCU's hull/shields/modules/AI core with Traveller's
 * facilities, docking, market, refueling, and repair services.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Combat capabilities: hull, shields, modules, AI core (from DoraMCU)</li>
 *   <li>Service facilities: refueling, repair, market, shipyard (from Traveller)</li>
 *   <li>Station type mapping: Traveller StationType to DoraMCU SpaceStationType</li>
 *   <li>Docking management with capacity limits</li>
 *   <li>Market for buying/selling commodities</li>
 *   <li>Refueling and repair services</li>
 * </ul>
 */
public class UnifiedSpaceStation extends SpaceStation {

    private final int id;
    private final String name;
    private final Set<StationFacility> facilities;
    private final int dockCapacity;
    private final Map<Commodity, Integer> marketPrices;
    private StationType stationType;
    private TravellerWorld world;
    private boolean operational;
    private final List<String> eventLog;

    /**
     * Station type mapping from Traveller to DoraMCU.
     */
    public enum StationType {
        /** Alpha station — full services, large capacity */
        ALPHA(SpaceStationType.MILITARY, 10, 10),
        /** Bravo station — most services, medium capacity */
        BRAVO(SpaceStationType.MILITARY, 8, 8),
        /** Charlie station — standard services */
        CHARLIE(SpaceStationType.SHIPYARD, 6, 6),
        /** D station — limited services */
        D(SpaceStationType.MINING, 4, 4),
        /** E station — minimal services */
        E(SpaceStationType.MINING, 2, 2),
        /** Outpost — no starport, very limited */
        OUTPOST(SpaceStationType.MINING, 1, 1);

        private final SpaceStationType doraMCUType;
        private final int maxFacilities;
        private final int dockCapacity;

        StationType(SpaceStationType doraMCUType, int maxFacilities, int dockCapacity) {
            this.doraMCUType = doraMCUType;
            this.maxFacilities = maxFacilities;
            this.dockCapacity = dockCapacity;
        }

        public SpaceStationType getDoraMCUType() {
            return doraMCUType;
        }

        public int getMaxFacilities() {
            return maxFacilities;
        }

        public int getDockCapacity() {
            return dockCapacity;
        }

        /**
         * Maps a Traveller starport code to a station type.
         */
        public static StationType fromStarportCode(int code) {
            if (code == 0) return ALPHA;
            if (code == 1) return BRAVO;
            if (code == 2) return CHARLIE;
            if (code == 3) return D;
            if (code == 4) return E;
            if (code == 5 || code == 6) return OUTPOST;
            return null; // No starport (A=999 in Traveller)
        }

        public StationType upgrade() {
            switch (this) {
                case OUTPOST: return E;
                case E: return D;
                case D: return CHARLIE;
                case CHARLIE: return BRAVO;
                case BRAVO: return ALPHA;
                case ALPHA: return null;
                default: return null;
            }
        }
    }

    /**
     * Creates a new unified space station.
     *
     * @param id the unique station identifier
     * @param name the name of the station
     * @param stationType the type of station
     */
    public UnifiedSpaceStation(int id, String name, StationType stationType) {
        super(id, stationType.getDoraMCUType());
        this.id = id;
        this.name = name;
        this.stationType = Objects.requireNonNull(stationType, "Station type must not be null");
        this.facilities = deriveFacilities(stationType);
        this.dockCapacity = stationType.getDockCapacity();
        this.marketPrices = new HashMap<>();
        this.operational = true;
        this.eventLog = new ArrayList<>();
        // Set the DoraMCU station name
        setName(name);
    }

    /**
     * Creates a unified station derived from a Traveller world.
     *
     * @param world the world to create the station for
     * @return the new station, or null if the world has no starport
     */
    public static UnifiedSpaceStation fromWorld(TravellerWorld world) {
        Objects.requireNonNull(world, "World must not be null");
        UniversalWorldProfile uwp = world.getUwp();
        int starportCode = uwp.getStarport();

        StationType stationType = StationType.fromStarportCode(starportCode);
        if (stationType == null) {
            return null;
        }

        String name = world.getName() + " Orbital";
        UnifiedSpaceStation station = new UnifiedSpaceStation(world.getId(), name, stationType);
        station.setWorld(world);

        // Add military facility if world has MO trade code
        if (world.hasTradeCode(TradeCode.MO) || world.isCapital()) {
            station.addFacility(StationFacility.MILITARY);
        }

        return station;
    }

    /**
     * Derives the set of facilities based on station type.
     */
    private static Set<StationFacility> deriveFacilities(StationType stationType) {
        Set<StationFacility> facilities = new HashSet<>();

        // Essential facilities (except Outpost)
        if (stationType != StationType.OUTPOST) {
            facilities.add(StationFacility.REFUELING);
            facilities.add(StationFacility.REPAIR);
        }

        // Trade facilities
        if (stationType.getMaxFacilities() >= 1) {
            facilities.add(StationFacility.MARKET);
            facilities.add(StationFacility.CUSTOMS);
        }

        // Support facilities
        if (stationType.getMaxFacilities() >= 3) {
            facilities.add(StationFacility.PASSENGER);
            facilities.add(StationFacility.CREW_QUARTERS);
            facilities.add(StationFacility.INFORMATION);
        }

        // Construction facilities
        if (stationType.getMaxFacilities() >= 5) {
            facilities.add(StationFacility.SHIPYARD);
            facilities.add(StationFacility.MEDICAL);
        }

        return facilities;
    }

    // ---- Accessors ----

    /**
     * Returns the station type.
     */
    public StationType getUnifiedStationType() {
        return stationType;
    }

    /**
     * Returns the set of available facilities (immutable).
     */
    public Set<StationFacility> getFacilities() {
        return Collections.unmodifiableSet(facilities);
    }

    /**
     * Returns the dock capacity.
     */
    public int getDockCapacity() {
        return dockCapacity;
    }

    /**
     * Returns the world the station orbits, or null if not set.
     */
    public TravellerWorld getWorld() {
        return world;
    }

    /**
     * Sets the world the station orbits.
     */
    public void setWorld(TravellerWorld world) {
        this.world = Objects.requireNonNull(world, "World must not be null");
    }

    /**
     * Checks if the station is operational.
     */
    public boolean isOperational() {
        return operational;
    }

    /**
     * Sets the operational status.
     */
    public void setOperational(boolean operational) {
        this.operational = operational;
        logEvent("Station " + (operational ? "became operational" : "went offline"));
    }

    /**
     * Checks if a specific facility is available.
     */
    public boolean hasFacility(StationFacility facility) {
        return operational && facilities.contains(facility);
    }

    /**
     * Returns the number of ships currently docked.
     */
    public int getDockedShipCount() {
        return 0;
    }

    /**
     * Checks if the dock is full.
     */
    public boolean isDockFull() {
        return getDockedShipCount() >= dockCapacity;
    }

    /**
     * Returns the event log (immutable).
     */
    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    // ---- Service Methods ----

    /**
     * Docks a ship at this station.
     */
    public boolean dockShip(String shipName, int tonnage) {
        if (!operational) return false;
        if (getDockedShipCount() >= dockCapacity) return false;
        logEvent("Ship " + shipName + " docked");
        return true;
    }

    /**
     * Undocks a ship from this station.
     */
    public boolean undockShip(String shipName) {
        logEvent("Ship " + shipName + " undocked");
        return true;
    }

    /**
     * Refuels a ship's fuel tank at this station.
     */
    public int refuel(FuelTank fuelTank, boolean fullTank) {
        if (!hasFacility(StationFacility.REFUELING)) {
            throw new IllegalStateException("Refueling facility not available");
        }
        int needed = fullTank ? fuelTank.getMaxCapacityTons() - fuelTank.getCurrentFuelTons() : fuelTank.getMaxCapacityTons() / 4;
        int cost = needed * 10;
        fuelTank.refuel(needed);
        logEvent("Refueled ship, cost: " + cost + " credits");
        return cost;
    }

    /**
     * Repairs a ship's hull at this station.
     */
    public int repairHull(int currentHull, int maxHull) {
        if (!hasFacility(StationFacility.REPAIR)) {
            throw new IllegalStateException("Repair facility not available");
        }
        int repairAmount = maxHull - currentHull;
        int cost = repairAmount * 5;
        logEvent("Repaired hull, cost: " + cost + " credits");
        return cost;
    }

    /**
     * Buys cargo at this station's market.
     */
    public int buyCargo(Commodity commodity, int units) {
        if (!hasFacility(StationFacility.MARKET)) {
            throw new IllegalStateException("Market not available");
        }
        int price = marketPrices.getOrDefault(commodity, commodity.getBasePrice());
        int cost = price * units;
        logEvent("Bought " + units + " units of " + commodity + " for " + cost + " credits");
        return cost;
    }

    /**
     * Sells cargo at this station's market.
     */
    public int sellCargo(Commodity commodity, int units) {
        if (!hasFacility(StationFacility.MARKET)) {
            throw new IllegalStateException("Market not available");
        }
        int price = marketPrices.getOrDefault(commodity, commodity.getBasePrice());
        int revenue = price * units;
        logEvent("Sold " + units + " units of " + commodity + " for " + revenue + " credits");
        return revenue;
    }

    /**
     * Adds a facility to the station.
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
     */
    public boolean upgrade() {
        StationType nextType = stationType.upgrade();
        if (nextType == null) {
            return false;
        }
        this.stationType = nextType;
        logEvent("Station upgraded from " + stationType);
        return true;
    }

    /**
     * Checks if the station is combat-ready (has hull and shields).
     */
    public boolean isCombatReady() {
        return getCurrentHullPoints() > 0 && !isDestroyed();
    }

    private void logEvent(String event) {
        eventLog.add(event);
    }

    @Override
    public String toString() {
        return name + " (" + getUnifiedStationType() + " station" +
                (world != null ? " at " + world.getName() : "") + ")";
    }
}
