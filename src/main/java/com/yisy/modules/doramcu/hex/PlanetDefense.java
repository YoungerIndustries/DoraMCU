package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages stationed fleets and defense for a planet.
 * <p>
 * Stationed fleets receive defense bonuses from the planet's population Resolve stat
 * and any starting facilities. This class tracks fleet stationing, removal,
 * and aggregate combat statistics.
 */
public class PlanetDefense {

    private final List<Fleet> stationedFleets;
    private final Planet planet;

    /**
     * Creates planet defense linked to the given planet.
     *
     * @param planet the planet this defense belongs to
     */
    public PlanetDefense(Planet planet) {
        this.planet = planet;
        this.stationedFleets = new ArrayList<>();
    }

    /**
     * Returns the list of fleets currently stationed at this planet.
     */
    public List<Fleet> getStationedFleets() {
        return List.copyOf(stationedFleets);
    }

    /**
     * Returns the number of fleets currently stationed.
     */
    public int getStationedFleetCount() {
        return stationedFleets.size();
    }

    /**
     * Stations a fleet at this planet.
     *
     * @param fleet the fleet to station
     * @return true if the fleet was stationed successfully
     */
    public boolean stationFleet(Fleet fleet) {
        if (fleet == null) {
            throw new IllegalArgumentException("Fleet must not be null");
        }
        return stationedFleets.add(fleet);
    }

    /**
     * Removes a fleet from this planet.
     *
     * @param fleet the fleet to remove
     * @return true if the fleet was removed
     */
    public boolean removeFleet(Fleet fleet) {
        return stationedFleets.remove(fleet);
    }

    /**
     * Checks if a fleet is stationed at this planet.
     *
     * @param fleet the fleet to check
     * @return true if stationed
     */
    public boolean hasStationedFleet(Fleet fleet) {
        return stationedFleets.contains(fleet);
    }

    /**
     * Removes all fleets from this planet.
     */
    public void clearStationedFleets() {
        stationedFleets.clear();
    }

    /**
     * Returns the defense bonus provided by this planet to stationed fleets.
     * Derived from the planet's Resolve population stat.
     *
     * @return the defense bonus multiplier (0.0 to 0.5)
     */
    public double getDefenseBonus() {
        int resolve = planet.getPopulationStat(PopulationAttribute.RESOLVE);
        return Math.max(0.0, (resolve - 5) * 0.05);
    }

    /**
     * Returns the total damage output from all stationed fleets.
     */
    public int getStationedFleetDamage() {
        return stationedFleets.stream()
                .mapToInt(Fleet::getTotalDamage)
                .sum();
    }

    /**
     * Returns the total shield strength from all stationed fleets.
     */
    public int getStationedFleetShields() {
        return stationedFleets.stream()
                .mapToInt(Fleet::getTotalShieldPoints)
                .sum();
    }

    /**
     * Returns the total hull points from all stationed fleets.
     */
    public int getStationedFleetHull() {
        return stationedFleets.stream()
                .mapToInt(Fleet::getTotalHullPoints)
                .sum();
    }

    /**
     * Returns the total number of ships across all stationed fleets.
     */
    public int getStationedShipCount() {
        return stationedFleets.stream()
                .mapToInt(Fleet::getShipCount)
                .sum();
    }

    /**
     * Repairs all stationed fleets' shields and hull.
     * Called each turn to restore stationed fleets.
     */
    public void repairStationedFleets() {
        for (Fleet fleet : stationedFleets) {
            fleet.endTurnRepair();
        }
    }
}
