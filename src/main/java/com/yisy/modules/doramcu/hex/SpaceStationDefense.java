package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Fleet and ship bay management for a space station.
 * <p>
 * Extracted from SpaceStation.java to separate defense operations from
 * station identity and economic logic.
 * </p>
 * <p>
 * Based on 4X game patterns from Stellaris and Galactic Civilizations IV,
 * where stations provide docking and repair services for fleets.
 * </p>
 */
public class SpaceStationDefense {

    private final SpaceStation station;
    private final List<Ship> dockedShips;
    private final List<Fleet> stationedFleets;

    /**
     * Creates defense manager for the given station.
     *
     * @param station the space station
     */
    public SpaceStationDefense(SpaceStation station) {
        if (station == null) {
            throw new IllegalArgumentException("Station must not be null");
        }
        this.station = station;
        this.dockedShips = new ArrayList<>();
        this.stationedFleets = new ArrayList<>();
    }

    // ---- Ship Bay Management ----

    /**
     * Returns the list of ships currently docked at this station.
     *
     * @return unmodifiable list of docked ships
     */
    public List<Ship> getDockedShips() {
        return List.copyOf(dockedShips);
    }

    /**
     * Returns the maximum number of ships that can dock at this station.
     *
     * @return the maximum docked ship capacity
     */
    public int getMaxDockedShips() {
        return station.getMaxDockedShips();
    }

    /**
     * Returns the number of ships currently docked.
     *
     * @return the docked ship count
     */
    public int getDockedShipCount() {
        return dockedShips.size();
    }

    /**
     * Returns the number of available docking slots.
     *
     * @return the number of available slots
     */
    public int getAvailableDockingSlots() {
        return station.getMaxDockedShips() - dockedShips.size();
    }

    /**
     * Checks if the ship bay is full.
     *
     * @return true if no more ships can dock
     */
    public boolean isShipBayFull() {
        return dockedShips.size() >= station.getMaxDockedShips();
    }

    /**
     * Docks a ship at this station.
     * <p>
     * Docked ships receive enhanced repair and shield recharge benefits.
     * </p>
     *
     * @param ship the ship to dock
     * @return true if the ship was docked successfully
     * @throws IllegalStateException if the ship bay is full
     */
    public boolean dockShip(Ship ship) {
        if (ship == null) {
            throw new IllegalArgumentException("Ship must not be null");
        }
        if (dockedShips.size() >= station.getMaxDockedShips()) {
            throw new IllegalStateException("Ship bay is full: " + station.getMaxDockedShips() + " slots occupied");
        }
        return dockedShips.add(ship);
    }

    /**
     * Undocks a ship from this station.
     *
     * @param ship the ship to undock
     * @return true if the ship was undocked successfully
     */
    public boolean undockShip(Ship ship) {
        return dockedShips.remove(ship);
    }

    /**
     * Checks if a ship is currently docked at this station.
     *
     * @param ship the ship to check
     * @return true if the ship is docked
     */
    public boolean hasDockedShip(Ship ship) {
        return dockedShips.contains(ship);
    }

    /**
     * Undocks all ships from this station.
     */
    public void undockAllShips() {
        dockedShips.clear();
    }

    /**
     * Returns the hull repair bonus multiplier for docked ships.
     * <p>
     * Docked ships repair hull at this rate multiplier per turn.
     * </p>
     *
     * @return the hull repair bonus multiplier (default 2.0x)
     */
    public double getDockedHullRepairBonus() {
        return 2.0;
    }

    /**
     * Returns the shield recharge bonus multiplier for docked ships.
     * <p>
     * Docked ships recharge shields at this rate multiplier per turn.
     * </p>
     *
     * @return the shield recharge bonus multiplier (default 2.0x)
     */
    public double getDockedShieldRechargeBonus() {
        return 2.0;
    }

    /**
     * Repairs all docked ships' hull and shields.
     * <p>
     * Called each turn to restore docked ships at enhanced rates.
     * Docked ships receive 2x the normal repair rate.
     * </p>
     */
    public void repairDockedShips() {
        double hullBonus = getDockedHullRepairBonus();
        double shieldBonus = getDockedShieldRechargeBonus();

        for (Ship ship : dockedShips) {
            // Repair hull - uses station's hull repair rate with bonus multiplier
            int hullRepair = Math.max(1, (int) Math.ceil(ship.getMaxHullPoints() * station.getHullRepairRate() * hullBonus));
            ship.repairHull(hullRepair);

            // Recharge shields - uses station's shield recharge rate with bonus multiplier
            int shieldRecharge = Math.max(1, (int) Math.ceil(ship.getMaxShieldPoints() * station.getShieldRechargeRate() * shieldBonus));
            ship.rechargeShields(shieldRecharge);
        }
    }

    /**
     * Returns the total damage output from all docked ships.
     *
     * @return the total damage value
     */
    public int getDockedShipDamage() {
        return dockedShips.stream()
                .mapToInt(Ship::getTotalDamage)
                .sum();
    }

    /**
     * Returns the total shield strength from all docked ships.
     *
     * @return the total shield value
     */
    public int getDockedShipShields() {
        return dockedShips.stream()
                .mapToInt(Ship::getMaxShieldPoints)
                .sum();
    }

    // ---- Fleet Management ----

    /**
     * Returns the list of fleets currently stationed at this station.
     *
     * @return unmodifiable list of stationed fleets
     */
    public List<Fleet> getStationedFleets() {
        return List.copyOf(stationedFleets);
    }

    /**
     * Returns the number of fleets currently stationed at this station.
     *
     * @return the stationed fleet count
     */
    public int getStationedFleetCount() {
        return stationedFleets.size();
    }

    /**
     * Stations a fleet at this space station.
     * <p>
     * Stationed fleets receive defense bonuses from the station's point defense modules
     * and enhanced repair rates.
     * </p>
     *
     * @param fleet the fleet to station
     * @return true if the fleet was stationed successfully
     * @throws IllegalArgumentException if fleet is null
     */
    public boolean stationFleet(Fleet fleet) {
        if (fleet == null) {
            throw new IllegalArgumentException("Fleet must not be null");
        }
        return stationedFleets.add(fleet);
    }

    /**
     * Removes a fleet from this station.
     *
     * @param fleet the fleet to remove
     * @return true if the fleet was removed successfully
     */
    public boolean removeFleet(Fleet fleet) {
        return stationedFleets.remove(fleet);
    }

    /**
     * Checks if a fleet is currently stationed at this station.
     *
     * @param fleet the fleet to check
     * @return true if the fleet is stationed
     */
    public boolean hasStationedFleet(Fleet fleet) {
        return stationedFleets.contains(fleet);
    }

    /**
     * Removes all fleets from this station.
     */
    public void clearStationedFleets() {
        stationedFleets.clear();
    }

    /**
     * Returns the defense bonus provided by this station to stationed fleets.
     * <p>
     * The defense bonus is derived from the station's point defense modules.
     * Each point defense module provides a 15% damage reduction.
     * </p>
     *
     * @return the defense bonus multiplier (0.0 to 1.0)
     */
    public double getDefenseBonus() {
        return station.getTotalPointDefense();
    }

    /**
     * Returns the total damage output from all stationed fleets.
     *
     * @return the total damage value
     */
    public int getStationedFleetDamage() {
        return stationedFleets.stream()
                .mapToInt(Fleet::getTotalDamage)
                .sum();
    }

    /**
     * Returns the total shield strength from all stationed fleets.
     *
     * @return the total shield value
     */
    public int getStationedFleetShields() {
        return stationedFleets.stream()
                .mapToInt(Fleet::getTotalShieldPoints)
                .sum();
    }

    /**
     * Returns the total hull points from all stationed fleets.
     *
     * @return the total hull value
     */
    public int getStationedFleetHull() {
        return stationedFleets.stream()
                .mapToInt(Fleet::getTotalHullPoints)
                .sum();
    }

    /**
     * Returns the total number of ships across all stationed fleets.
     *
     * @return the total ship count
     */
    public int getStationedShipCount() {
        return stationedFleets.stream()
                .mapToInt(Fleet::getShipCount)
                .sum();
    }

    /**
     * Repairs all stationed fleets' shields and hull.
     * <p>
     * Called each turn to restore stationed fleets at enhanced rates.
     * Stationed fleets receive 2x the normal repair rate.
     * </p>
     */
    public void repairStationedFleets() {
        for (Fleet fleet : stationedFleets) {
            fleet.endTurnRepair();
        }
    }
}
