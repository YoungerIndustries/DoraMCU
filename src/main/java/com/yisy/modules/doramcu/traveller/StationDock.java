package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages ship docking at a Traveller space station.
 * <p>
 * The dock tracks which ships are currently docked, their tonnage,
 * and the duration of their stay. Docking capacity is limited by
 * the station type.
 * </p>
 * <p>
 * In Classic Traveller, docking fees are based on ship tonnage and
 * duration of stay. Larger stations have more docking bays and lower
 * fees due to competition.
 * </p>
 */
public class StationDock {

    /**
     * Docking fee per 1000 tons per week, in credits.
     */
    private static final int BASE_FEE_PER_TON_PER_WEEK = 10;

    private final SpaceStation station;
    private final Map<String, DockedShip> dockedShips;
    private final List<DockEvent> eventLog;

    /**
     * Creates a new dock for the given station.
     *
     * @param station the space station
     */
    public StationDock(SpaceStation station) {
        this.station = station;
        this.dockedShips = new ConcurrentHashMap<>();
        this.eventLog = new ArrayList<>();
    }

    // ---- Accessors ----

    /**
     * @return the space station this dock belongs to
     */
    public SpaceStation getStation() {
        return station;
    }

    /**
     * @return the number of ships currently docked
     */
    public int getDockedShipCount() {
        return dockedShips.size();
    }

    /**
     * @return the maximum number of ships that can dock
     */
    public int getMaxCapacity() {
        return station.getStationType().getMaxShips();
    }

    /**
     * Checks if the dock is full.
     *
     * @return true if no more ships can dock
     */
    public boolean isFull() {
        return dockedShips.size() >= getMaxCapacity();
    }

    /**
     * Checks if a specific ship is docked.
     *
     * @param shipName the name of the ship
     * @return true if the ship is currently docked
     */
    public boolean isDocked(String shipName) {
        return dockedShips.containsKey(shipName);
    }

    /**
     * Returns the list of docked ship names.
     *
     * @return unmodifiable list of docked ship names
     */
    public List<String> getDockedShipNames() {
        return Collections.unmodifiableList(new ArrayList<>(dockedShips.keySet()));
    }

    /**
     * Returns the total tonnage of docked ships.
     *
     * @return total tonnage
     */
    public int getTotalTonnage() {
        int total = 0;
        for (DockedShip ship : dockedShips.values()) {
            total += ship.tonnage;
        }
        return total;
    }

    // ---- Docking Operations ----

    /**
     * Docks a ship at this station.
     *
     * @param shipName the name of the ship
     * @param tonnage the tonnage of the ship
     * @return true if the ship was successfully docked
     */
    public boolean dockShip(String shipName, int tonnage) {
        if (shipName == null || shipName.isEmpty()) {
            return false;
        }
        if (tonnage <= 0) {
            return false;
        }
        if (isFull()) {
            logEvent("Dock full — cannot dock " + shipName);
            return false;
        }
        if (isDocked(shipName)) {
            logEvent("Ship " + shipName + " is already docked");
            return false;
        }

        DockedShip dockedShip = new DockedShip(shipName, tonnage);
        dockedShips.put(shipName, dockedShip);
        logEvent("Ship " + shipName + " (" + tonnage + "kt) docked");
        return true;
    }

    /**
     * Undocks a ship from this station.
     *
     * @param shipName the name of the ship
     * @return true if the ship was successfully undocked
     */
    public boolean undockShip(String shipName) {
        DockedShip removed = dockedShips.remove(shipName);
        if (removed != null) {
            int weeks = removed.getWeeksDocked();
            int fee = calculateDockingFee(removed.tonnage, weeks);
            logEvent("Ship " + shipName + " undocked (fee: " + fee + " credits)");
            return true;
        }
        logEvent("Ship " + shipName + " is not docked");
        return false;
    }

    /**
     * Undocks all ships from this station.
     *
     * @return the number of ships undocked
     */
    public int undockAll() {
        int count = dockedShips.size();
        for (String shipName : new ArrayList<>(dockedShips.keySet())) {
            undockShip(shipName);
        }
        logEvent("All ships undocked (" + count + " ships)");
        return count;
    }

    /**
     * Calculates the docking fee for a ship.
     * <p>
     * The fee is based on ship tonnage, duration of stay, and station type.
     * Larger stations have lower fees due to competition.
     * </p>
     *
     * @param tonnage the ship's tonnage
     * @param weeksDocked the number of weeks docked
     * @return the fee in credits
     */
    public int calculateDockingFee(int tonnage, int weeksDocked) {
        if (weeksDocked <= 0) weeksDocked = 1;

        int baseFee = (tonnage / 1000) * BASE_FEE_PER_TON_PER_WEEK * weeksDocked;
        double multiplier = station.getStationType().getServiceRateMultiplier();

        return (int) (baseFee * multiplier);
    }

    /**
     * Advances the dock by one week, updating all docked ships' stay duration.
     */
    public void advanceWeek() {
        for (DockedShip ship : dockedShips.values()) {
            ship.advanceWeek();
        }
    }

    /**
     * Returns the event log.
     *
     * @return unmodifiable list of dock events
     */
    public List<DockEvent> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    private void logEvent(String message) {
        eventLog.add(new DockEvent(message));
    }

    // ---- Inner Classes ----

    /**
     * Represents a ship currently docked at the station.
     */
    public static class DockedShip {
        private final String name;
        private final int tonnage;
        private final long dockedAt;
        private int weeksDocked;

        DockedShip(String name, int tonnage) {
            this.name = name;
            this.tonnage = tonnage;
            this.dockedAt = System.currentTimeMillis();
            this.weeksDocked = 0;
        }

        public String getName() {
            return name;
        }

        public int getTonnage() {
            return tonnage;
        }

        public int getWeeksDocked() {
            return weeksDocked;
        }

        void advanceWeek() {
            weeksDocked++;
        }
    }

    /**
     * A logged event at the dock.
     */
    public static class DockEvent {
        private final String message;
        private final long timestamp;

        DockEvent(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
