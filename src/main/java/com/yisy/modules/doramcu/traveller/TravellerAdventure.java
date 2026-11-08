package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks a Traveller character's adventure state in the 4X universe.
 * <p>
 * When a character with COMPLETED status starts an adventure, this object
 * records their fleet, current position, credits, passenger status, and
 * all witnessed 4X events as character experiences.
 * </p>
 * <p>
 * The character travels as a single-ship fleet by default (starting with a
 * FRIGATE). They can purchase additional ships, book passage on other ships,
 * and witness 4X events like battles, fleet movements, and planet conquests.
 * </p>
 */
public class TravellerAdventure {

    /** Default starting ship type for adventure characters. */
    public static final ShipType DEFAULT_STARTING_SHIP = ShipType.FRIGATE;

    /** Cost in credits for each ship type. */
    public static final java.util.Map<ShipType, Long> SHIP_COSTS = new java.util.EnumMap<>(ShipType.class);

    static {
        SHIP_COSTS.put(ShipType.FRIGATE, 50_000L);
        SHIP_COSTS.put(ShipType.DESTROYER, 150_000L);
        SHIP_COSTS.put(ShipType.CRUISER, 300_000L);
        SHIP_COSTS.put(ShipType.BATTLESHIP, 600_000L);
        SHIP_COSTS.put(ShipType.CARRIER, 450_000L);
        SHIP_COSTS.put(ShipType.MOTHERSHIP, 1_200_000L);
    }

    private final int characterId;
    private int fleetId;
    private int currentSystemId;
    private String currentSystemName;
    private int currentPlanetId;
    private String currentPlanetName;
    private long credits;
    private boolean isPassenger;
    private int passengerShipId;
    private int destinationSystemId;
    private String destinationSystemName;
    private final List<CharacterExperience> adventureLog;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;

    /** Cargo held by the character: commodity name → quantity. */
    private final Map<String, Integer> cargo = new HashMap<>();

    /**
     * Creates a new adventure for the given character.
     *
     * @param characterId the character's ID
     */
    public TravellerAdventure(int characterId) {
        this.characterId = characterId;
        this.adventureLog = new ArrayList<>();
        this.startedAt = LocalDateTime.now();
        this.updatedAt = this.startedAt;
    }

    // ---- Accessors ----

    /** @return the character's ID */
    public int getCharacterId() {
        return characterId;
    }

    /** @return the fleet ID in the 4X game */
    public int getFleetId() {
        return fleetId;
    }

    /**
     * Sets the fleet ID assigned to this adventure.
     *
     * @param fleetId the fleet ID
     */
    public void setFleetId(int fleetId) {
        this.fleetId = fleetId;
        this.updatedAt = LocalDateTime.now();
    }

    /** @return the current solar system ID */
    public int getCurrentSystemId() {
        return currentSystemId;
    }

    /**
     * Sets the current solar system position.
     *
     * @param systemId the system ID
     * @param systemName the system name
     */
    public void setCurrentSystem(int systemId, String systemName) {
        this.currentSystemId = systemId;
        this.currentSystemName = systemName;
        this.updatedAt = LocalDateTime.now();
    }

    /** @return the name of the current solar system */
    public String getCurrentSystemName() {
        return currentSystemName;
    }

    /** @return the current planet ID (0 if not docked at a planet) */
    public int getCurrentPlanetId() {
        return currentPlanetId;
    }

    /**
     * Sets the current planet (docking).
     *
     * @param planetId the planet ID
     * @param planetName the planet name
     */
    public void setCurrentPlanet(int planetId, String planetName) {
        this.currentPlanetId = planetId;
        this.currentPlanetName = planetName;
        this.updatedAt = LocalDateTime.now();
    }

    /** @return the name of the current planet, or null if not docked */
    public String getCurrentPlanetName() {
        return currentPlanetName;
    }

    /** @return true if the character is docked at a planet */
    public boolean isDocked() {
        return currentPlanetId > 0;
    }

    /** @return the character's available credits */
    public long getCredits() {
        return credits;
    }

    /**
     * Sets the character's credits (converted from career wealth).
     *
     * @param credits the credit amount
     */
    public void setCredits(long credits) {
        this.credits = credits;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deducts credits from the character's balance.
     *
     * @param amount the amount to deduct
     * @return true if the deduction succeeded, false if insufficient funds
     */
    public boolean deductCredits(long amount) {
        if (amount > credits) {
            return false;
        }
        this.credits -= amount;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * Adds credits to the character's balance.
     *
     * @param amount the amount to add
     */
    public void addCredits(long amount) {
        this.credits += amount;
        this.updatedAt = LocalDateTime.now();
    }

    /** @return true if the character is currently riding as a passenger */
    public boolean isPassenger() {
        return isPassenger;
    }

    /**
     * Sets the passenger status.
     *
     * @param passenger true if riding as passenger
     * @param shipId the ship ID they are riding on, or 0 if not applicable
     */
    public void setPassenger(boolean passenger, int shipId) {
        this.isPassenger = passenger;
        this.passengerShipId = shipId;
        this.updatedAt = LocalDateTime.now();
    }

    /** @return the ship ID the character is riding on */
    public int getPassengerShipId() {
        return passengerShipId;
    }

    /** @return the destination system ID, or 0 if no destination set */
    public int getDestinationSystemId() {
        return destinationSystemId;
    }

    /**
     * Sets the planned destination system.
     *
     * @param systemId the destination system ID
     * @param systemName the destination system name
     */
    public void setDestination(int systemId, String systemName) {
        this.destinationSystemId = systemId;
        this.destinationSystemName = systemName;
        this.updatedAt = LocalDateTime.now();
    }

    /** @return the name of the destination system */
    public String getDestinationSystemName() {
        return destinationSystemName;
    }

    /** @return true if the character has a planned destination */
    public boolean hasDestination() {
        return destinationSystemId > 0;
    }

    /**
     * Clears the destination after arrival.
     */
    public void clearDestination() {
        this.destinationSystemId = 0;
        this.destinationSystemName = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns the adventure log of witnessed experiences.
     *
     * @return unmodifiable list of experiences
     */
    public List<CharacterExperience> getAdventureLog() {
        return java.util.Collections.unmodifiableList(adventureLog);
    }

    /**
     * Adds an experience to the adventure log.
     *
     * @param experience the experience to add
     */
    public void addExperience(CharacterExperience experience) {
        if (experience != null) {
            this.adventureLog.add(experience);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /** @return when the adventure started */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /** @return when the adventure was last updated */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the cost of a ship type in credits.
     *
     * @param shipType the ship type
     * @return the cost in credits
     */
    public static long getShipCost(ShipType shipType) {
        return SHIP_COSTS.getOrDefault(shipType, 0L);
    }

    // ============================================================
    // Trade / Cargo
    // ============================================================

    /**
     * Returns the cargo map (commodity → quantity).
     *
     * @return unmodifiable cargo map
     */
    public Map<String, Integer> getCargo() {
        return java.util.Collections.unmodifiableMap(cargo);
    }

    /**
     * Returns the quantity of a specific commodity in cargo.
     *
     * @param commodity the commodity name
     * @return quantity held, or 0 if none
     */
    public int getCargoQuantity(String commodity) {
        return cargo.getOrDefault(commodity, 0);
    }

    /**
     * Adds cargo to the character's inventory.
     *
     * @param commodity the commodity name
     * @param quantity the quantity to add
     */
    public void addCargo(String commodity, int quantity) {
        if (quantity > 0) {
            cargo.merge(commodity, quantity, Integer::sum);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Removes cargo from the character's inventory.
     *
     * @param commodity the commodity name
     * @param quantity the quantity to remove
     * @return true if the cargo was removed successfully
     */
    public boolean removeCargo(String commodity, int quantity) {
        int current = cargo.getOrDefault(commodity, 0);
        if (current >= quantity) {
            int remaining = current - quantity;
            if (remaining <= 0) {
                cargo.remove(commodity);
            } else {
                cargo.put(commodity, remaining);
            }
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * Returns the total cargo units carried.
     *
     * @return total cargo units
     */
    public int getTotalCargo() {
        return cargo.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Result of a trade operation (buy or sell).
     */
    public static class TradeResult {
        private final String commodity;
        private final int quantity;
        private final long cost;
        private final long revenue;
        private final long profit;
        private final long creditsAfter;
        private final String message;

        public TradeResult(String commodity, int quantity, long cost, long revenue, long profit,
                           long creditsAfter, String message) {
            this.commodity = commodity;
            this.quantity = quantity;
            this.cost = cost;
            this.revenue = revenue;
            this.profit = profit;
            this.creditsAfter = creditsAfter;
            this.message = message;
        }

        public String getCommodity() { return commodity; }
        public int getQuantity() { return quantity; }
        public long getCost() { return cost; }
        public long getRevenue() { return revenue; }
        public long getProfit() { return profit; }
        public long getCreditsAfter() { return creditsAfter; }
        public String getMessage() { return message; }
    }

    @Override
    public String toString() {
        return "TravellerAdventure{" +
                "characterId=" + characterId +
                ", fleetId=" + fleetId +
                ", system=" + currentSystemName +
                ", planet=" + currentPlanetName +
                ", credits=" + credits +
                ", cargo=" + cargo +
                ", passenger=" + isPassenger +
                ", experiences=" + adventureLog.size() +
                '}';
    }
}
