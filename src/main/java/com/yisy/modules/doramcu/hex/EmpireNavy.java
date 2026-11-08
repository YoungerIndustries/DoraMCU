package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages the Navy component of an Empire.
 * <p>
 * The Navy module handles all space-based military operations: fleet creation,
 * ship construction, fleet deployment, combat statistics, and orbital defense.
 * </p>
 * <p>
 * Each empire has one Navy instance that tracks all fleets and ships across
 * the galaxy.
 * </p>
 */
public class EmpireNavy {

    private final int empireId;
    private final List<Fleet> fleets;
    private int nextFleetId;

    /**
     * Creates a new navy for the given empire.
     *
     * @param empireId the ID of the owning empire
     */
    public EmpireNavy(int empireId) {
        this.empireId = empireId;
        this.fleets = new ArrayList<>();
        this.nextFleetId = 1;
    }

    // ---- Accessors ----

    /** Returns the empire ID. */
    public int getEmpireId() {
        return empireId;
    }

    /** Returns an unmodifiable list of all fleets. */
    public List<Fleet> getFleets() {
        return Collections.unmodifiableList(fleets);
    }

    /** Returns the total number of fleets. */
    public int getFleetCount() {
        return fleets.size();
    }

    // ---- Fleet Management ----

    /**
     * Adds an existing fleet to this navy (used to sync fleets created by FleetManager).
     *
     * @param fleet the fleet to add
     */
    public void addFleet(Fleet fleet) {
        if (fleet != null && !fleets.contains(fleet)) {
            fleets.add(fleet);
        }
    }

    /**
     * Creates a new fleet.
     *
     * @param name the name of the fleet
     * @return the newly created fleet
     */
    public Fleet createFleet(String name) {
        Fleet fleet = new Fleet(nextFleetId++, name);
        fleets.add(fleet);
        return fleet;
    }

    /**
     * Creates a new fleet with a default name.
     *
     * @return the newly created fleet
     */
    public Fleet createFleet() {
        return createFleet("Fleet-" + nextFleetId);
    }

    /**
     * Removes a fleet from the navy.
     *
     * @param fleet the fleet to remove
     * @return true if the fleet was removed
     */
    public boolean removeFleet(Fleet fleet) {
        return fleets.remove(fleet);
    }

    /**
     * Removes all destroyed fleets (no ships remaining).
     *
     * @return the number of fleets removed
     */
    public int removeDestroyedFleets() {
        int before = fleets.size();
        fleets.removeIf(f -> f.getShipCount() == 0);
        return before - fleets.size();
    }

    // ---- Ship Statistics ----

    /**
     * Returns the total number of ships across all fleets.
     */
    public int getTotalShipCount() {
        return fleets.stream().mapToInt(Fleet::getShipCount).sum();
    }

    /**
     * Returns a breakdown of ships by type across all fleets.
     */
    public Map<ShipType, Integer> getShipTypeBreakdown() {
        return fleets.stream()
                .flatMap(f -> f.getShips().stream())
                .collect(Collectors.groupingBy(
                        Ship::getShipType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    // ---- Combat Statistics ----

    /**
     * Returns the total damage output of all fleets.
     */
    public int getTotalDamage() {
        return fleets.stream().mapToInt(Fleet::getTotalDamage).sum();
    }

    /**
     * Returns the total shield points of all fleets.
     */
    public int getTotalShieldPoints() {
        return fleets.stream().mapToInt(Fleet::getTotalShieldPoints).sum();
    }

    /**
     * Returns the total current shield points of all fleets.
     */
    public int getCurrentShieldPoints() {
        return fleets.stream().mapToInt(Fleet::getCurrentShieldPoints).sum();
    }

    /**
     * Returns the total hull points of all fleets.
     */
    public int getTotalHullPoints() {
        return fleets.stream().mapToInt(Fleet::getTotalHullPoints).sum();
    }

    /**
     * Returns the total current hull points of all fleets.
     */
    public int getCurrentHullPoints() {
        return fleets.stream().mapToInt(Fleet::getCurrentHullPoints).sum();
    }

    // ---- Deployment ----

    /**
     * Returns all fleets at the given position.
     *
     * @param position the hex position
     * @return list of fleets at that position
     */
    public List<Fleet> getFleetsAtPosition(Hex3D position) {
        if (position == null) return Collections.emptyList();
        return fleets.stream()
                .filter(f -> f.getPosition() != null && f.getPosition().equals(position))
                .collect(Collectors.toList());
    }

    /**
     * Returns all fleets stationed at the given planet.
     *
     * @param planet the planet
     * @return list of fleets stationed at the planet
     */
    public List<Fleet> getFleetsAtPlanet(Planet planet) {
        if (planet == null) return Collections.emptyList();
        return fleets.stream()
                .filter(f -> f.getHomeSystem() == planet)
                .collect(Collectors.toList());
    }

    // ---- End Turn Processing ----

    /**
     * Processes end-of-turn effects for all fleets:
     * - Shield recharge
     * - Hull repair
     * - Remove destroyed fleets
     */
    public void endTurn() {
        for (Fleet fleet : fleets) {
            fleet.endTurnRepair();
        }
        removeDestroyedFleets();
    }

    @Override
    public String toString() {
        return String.format("EmpireNavy{empireId=%d, fleets=%d, ships=%d, damage=%d}",
                empireId, getFleetCount(), getTotalShipCount(), getTotalDamage());
    }
}
