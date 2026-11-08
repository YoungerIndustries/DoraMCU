package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.TravellerCharacter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Represents a player in the 4X empire game with Empire characteristics.
 * <p>
 * Each player controls an Empire with a unique identity, government type, and ethos.
 * The player has an email address for account management and a unique login PIN
 * for authentication.
 * </p>
 * <p>
 * The player also maintains game state: resource stockpiles, owned planets and fleets,
 * ship construction queues, and home system location within the galaxy.
 * </p>
 * <p>
 * The login PIN is a 6-digit number generated automatically upon player creation,
 * ensuring uniqueness across all players.
 * </p>
 */
public class Player {

    /** Minimum value for the login PIN. */
    public static final int MIN_PIN = 100_000;

    /** Maximum value for the login PIN. */
    public static final int MAX_PIN = 999_999;

// Flat resource starting values removed – use ResourcePipelineManager for mining/processing

    /**
     * Government types available for an Empire.
     * <p>
     * Based on Stellaris government mechanics, each type defines how the empire
     * is structured and governed.
     * </p>
     */
    public enum GovernmentType {
        /** Democratic republic with elected leaders. */
        DEMOCRACY,
        /** Authoritarian state with centralized control. */
        AUTHORITARIAN,
        /** Monarchy with hereditary rule. */
        MONARCHY,
        /** Oligarchy ruled by a small elite group. */
        OLIGARCHY,
        /** Theocracy governed by religious doctrine. */
        THEOCRACY,
        /** Dictatorship with absolute power. */
        DICTATORSHIP,
        /** Confederation of independent states. */
        CONFEDERATION,
        /** Corporate state run by business interests. */
        CORPORATE
    }

    /**
     * Ethos types that define the core values of an Empire.
     * <p>
     * Based on Stellaris ethos system, each ethos shapes the empire's
     * cultural identity and provides gameplay bonuses.
     * </p>
     */
    public enum Ethos {
        /** Values logic and scientific progress. */
        MATERIALIST,
        /** Values spirituality and the divine. */
        SPIRITUALIST,
        /** Values individual freedom and rights. */
        LIBERTARIAN,
        /** Values collective unity and order. */
        EQUALIST,
        /** Values military strength and expansion. */
        MILITARIST,
        /** Values harmony with nature. */
        PACIFIST
    }

    private final int id;
    private final String email;
    private final String loginPin;
    private String empireName;
    private GovernmentType governmentType;
    private Ethos ethos;

    // ---- Game state fields (merged from hex.player.Player) ----

    private Set<Integer> ownedPlanetIds;
    private Set<Integer> ownedFleetIds;
    private List<ShipOrder> shipQueue;
    private boolean eliminated;
    private Integer empireId;
    private HomeLocation homeLocation;

    /** Optional Traveller character serving as this player's empire commander. */
    private TravellerCharacter commander;

    /**
     * Creates a new player with the given ID and email/name.
     * <p>
     * If the string contains '@', it is treated as an email address and the empire name
     * defaults to "Unnamed Empire". Otherwise, it is treated as a display name, the email
     * is derived from it (e.g., "player.name@game.local"), and the empire name is set to the name.
     * Government defaults to DEMOCRACY and ethos to MATERIALIST. Login PIN is auto-generated.
     * </p>
     *
     * @param id the unique identifier for this player
     * @param emailOrName the email address or display name
     */
    public Player(int id, String emailOrName) {
        if (emailOrName == null || emailOrName.isBlank()) {
            throw new IllegalArgumentException("Email/name must not be null or blank");
        }
        this.id = id;
        this.loginPin = generateUniquePin();
        this.governmentType = GovernmentType.DEMOCRACY;
        this.ethos = Ethos.MATERIALIST;
        if (emailOrName.contains("@")) {
            // Treat as email
            this.email = emailOrName;
            this.empireName = "Unnamed Empire";
        } else {
            // Treat as name
            this.email = emailOrName.toLowerCase().replace(" ", ".") + "@game.local";
            this.empireName = emailOrName;
        }
        initGameState();
    }

    /**
     * Creates a new player with the given ID, email, and empire name.
     * <p>
     * The login PIN is auto-generated as a unique 6-digit number.
     * Government defaults to DEMOCRACY and ethos to MATERIALIST.
     * </p>
     *
     * @param id the unique identifier for this player
     * @param email the email address for account management
     * @param empireName the name of the player's empire
     */
    public Player(int id, String email, String empireName) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        if (empireName == null || empireName.isBlank()) {
            throw new IllegalArgumentException("Empire name must not be null or blank");
        }
        this.id = id;
        this.email = email;
        this.loginPin = generateUniquePin();
        this.empireName = empireName;
        this.governmentType = GovernmentType.DEMOCRACY;
        this.ethos = Ethos.MATERIALIST;
        initGameState();
    }

    /**
     * Creates a new player with full Empire characteristics.
     * <p>
     * The login PIN is auto-generated as a unique 6-digit number.
     * </p>
     *
     * @param id the unique identifier for this player
     * @param email the email address for account management
     * @param empireName the name of the player's empire
     * @param governmentType the government type of the empire
     * @param ethos the ethos of the empire
     */
    public Player(int id, String email, String empireName, GovernmentType governmentType, Ethos ethos) {
        this(id, email, empireName, governmentType, ethos, null);
    }

    /**
     * Creates a new player with full Empire characteristics and a custom login PIN.
     * <p>
     * Use this constructor for admin/game master accounts with a fixed PIN.
     * If {@code loginPin} is null, a random PIN is auto-generated instead.
     * </p>
     *
     * @param id the unique identifier for this player
     * @param email the email address for account management
     * @param empireName the name of the player's empire
     * @param governmentType the government type of the empire
     * @param ethos the ethos of the empire
     * @param loginPin the custom login PIN (e.g. "3214") or null for auto-generation
     */
    public Player(int id, String email, String empireName, GovernmentType governmentType, Ethos ethos, String loginPin) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        if (empireName == null || empireName.isBlank()) {
            throw new IllegalArgumentException("Empire name must not be null or blank");
        }
        if (governmentType == null) {
            throw new IllegalArgumentException("Government type must not be null");
        }
        if (ethos == null) {
            throw new IllegalArgumentException("Ethos must not be null");
        }
        this.id = id;
        this.email = email;
        this.loginPin = (loginPin != null && !loginPin.isBlank()) ? loginPin : generateUniquePin();
        this.empireName = empireName;
        this.governmentType = governmentType;
        this.ethos = ethos;
        initGameState();
    }

    /**
     * Initializes the game state fields (planets, fleets, etc.).
     */
    private void initGameState() {
        this.ownedPlanetIds = new HashSet<>();
        this.ownedFleetIds = new HashSet<>();
        this.shipQueue = new ArrayList<>();
        this.eliminated = false;
        this.empireId = null;
        this.homeLocation = null;
    }

    /**
     * Generates a unique 6-digit login PIN.
     * <p>
     * The PIN is randomly generated between 100000 and 999999.
     * </p>
     *
     * @return a 6-digit PIN string
     */
    private String generateUniquePin() {
        int pin = ThreadLocalRandom.current().nextInt(MIN_PIN, MAX_PIN + 1);
        return String.valueOf(pin);
    }

    /** Returns the player ID. */
    public int getId() {
        return id;
    }

    /** Returns the player's email address. */
    public String getEmail() {
        return email;
    }

    /** Returns the unique 6-digit login PIN. */
    public String getLoginPin() {
        return loginPin;
    }

    /**
     * Returns the login PIN as an integer.
     *
     * @return the numeric PIN value
     */
    public int getLoginPinInt() {
        return Integer.parseInt(loginPin);
    }

    /** Returns the empire name. */
    public String getEmpireName() {
        return empireName;
    }

    /**
     * Sets the empire name.
     *
     * @param empireName the new empire name
     */
    public void setEmpireName(String empireName) {
        if (empireName == null || empireName.isBlank()) {
            throw new IllegalArgumentException("Empire name must not be null or blank");
        }
        this.empireName = empireName;
    }

    /** Returns the government type. */
    public GovernmentType getGovernmentType() {
        return governmentType;
    }

    /**
     * Sets the government type.
     *
     * @param governmentType the new government type
     */
    public void setGovernmentType(GovernmentType governmentType) {
        if (governmentType == null) {
            throw new IllegalArgumentException("Government type must not be null");
        }
        this.governmentType = governmentType;
    }

    /** Returns the empire ethos. */
    public Ethos getEthos() {
        return ethos;
    }

    /**
     * Sets the empire ethos.
     *
     * @param ethos the new ethos
     */
    public void setEthos(Ethos ethos) {
        if (ethos == null) {
            throw new IllegalArgumentException("Ethos must not be null");
        }
        this.ethos = ethos;
    }

    /**
     * Validates the login PIN.
     *
     * @param pin the PIN to validate
     * @return true if the PIN matches
     */
    public boolean validateLoginPin(String pin) {
        return loginPin.equals(pin);
    }

    /**
     * Validates the login PIN.
     *
     * @param pin the numeric PIN to validate
     * @return true if the PIN matches
     */
    public boolean validateLoginPin(int pin) {
        return loginPin.equals(String.valueOf(pin));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Player player = (Player) o;
        return id == player.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a masked version of the login PIN for display purposes.
     * <p>
     * Shows only the first and last digit, masking the middle digits with asterisks.
     * Example: "1****9"
     * </p>
     *
     * @return the masked PIN string
     */
    public String maskPin() {
        if (loginPin == null || loginPin.length() < 2) {
            return "******";
        }
        return loginPin.charAt(0) + "****" + loginPin.charAt(loginPin.length() - 1);
    }

    // ========================================================================
    // Game state methods (merged from hex.player.Player)
    // ========================================================================

    /**
     * Returns the player's display name (same as empire name).
     *
     * @return the empire name
     */
    public String getName() {
        return empireName;
    }

    // Flat resource methods removed – use ResourcePipelineManager for mining/processing

    /**
     * Returns the IDs of planets owned by this player.
     *
     * @return unmodifiable set of planet IDs
     */
    public Set<Integer> getOwnedPlanetIds() {
        return Collections.unmodifiableSet(ownedPlanetIds);
    }

    /**
     * Adds a planet to this player's ownership.
     *
     * @param planetId the planet ID to add
     */
    public void addPlanet(int planetId) {
        ownedPlanetIds.add(planetId);
    }

    /**
     * Removes a planet from this player's ownership.
     *
     * @param planetId the planet ID to remove
     */
    public void removePlanet(int planetId) {
        ownedPlanetIds.remove(planetId);
    }

    /**
     * Returns the number of planets owned by this player.
     *
     * @return the planet count
     */
    public int getPlanetCount() {
        return ownedPlanetIds.size();
    }

    /**
     * Returns the IDs of fleets owned by this player.
     *
     * @return unmodifiable set of fleet IDs
     */
    public Set<Integer> getOwnedFleetIds() {
        return Collections.unmodifiableSet(ownedFleetIds);
    }

    /**
     * Adds a fleet to this player's ownership.
     *
     * @param fleetId the fleet ID to add
     */
    public void addFleet(int fleetId) {
        ownedFleetIds.add(fleetId);
    }

    /**
     * Removes a fleet from this player's ownership.
     *
     * @param fleetId the fleet ID to remove
     */
    public void removeFleet(int fleetId) {
        ownedFleetIds.remove(fleetId);
    }

    /**
     * Returns the ship construction queue.
     *
     * @return unmodifiable list of pending ship orders
     */
    public List<ShipOrder> getShipQueue() {
        return Collections.unmodifiableList(shipQueue);
    }

    /**
     * Adds a ship to the construction queue.
     *
     * @param order the ship order to add
     */
    public void addShipOrder(ShipOrder order) {
        shipQueue.add(order);
    }

    /**
     * Removes the first completed ship order from the queue.
     *
     * @return the completed ship order, or null if queue is empty
     */
    public ShipOrder completeNextShipOrder() {
        if (shipQueue.isEmpty()) {
            return null;
        }
        return shipQueue.remove(0);
    }

    /**
     * Returns the set of strategic resources available from owned planets.
     *
     * @param planets the global planet map
     * @return set of available strategic resources
     */
    public Set<StrategicResource> getAvailableStrategicResources(Map<Integer, Planet> planets) {
        return ownedPlanetIds.stream()
                .map(planets::get)
                .filter(p -> p != null)
                .flatMap(p -> p.getStrategicDeposits().stream())
                .collect(Collectors.toSet());
    }

    // getTotalProduction removed – use ResourcePipelineManager for mining/processing

    /**
     * Returns true if this player has been eliminated from the game.
     *
     * @return true if eliminated
     */
    public boolean isEliminated() {
        return eliminated;
    }

    /**
     * Eliminates this player from the game.
     */
    public void eliminate() {
        this.eliminated = true;
    }

    /**
     * Returns the empire ID this player heads.
     *
     * @return the empire ID, or null if not assigned
     */
    public Integer getEmpireId() {
        return empireId;
    }

    /**
     * Sets the empire this player heads.
     *
     * @param empireId the empire ID
     */
    public void setEmpireId(Integer empireId) {
        this.empireId = empireId;
    }

    // ---- Home location methods ----

    /**
     * Returns this player's home location.
     *
     * @return the home location, or null if not set
     */
    public HomeLocation getHomeLocation() {
        return homeLocation;
    }

    /**
     * Sets this player's home location.
     * <p>
     * The home planet is automatically added to the player's owned planets.
     * </p>
     *
     * @param homeLocation the new home location
     */
    public void setHomeLocation(HomeLocation homeLocation) {
        this.homeLocation = homeLocation;
        if (homeLocation != null && homeLocation.hasPlanet()) {
            ownedPlanetIds.add(homeLocation.getPlanetId());
        }
    }

    /**
     * Returns the ID of this player's home planet.
     *
     * @return the home planet ID, or null if not set
     */
    public Integer getHomePlanetId() {
        return homeLocation != null ? homeLocation.getPlanetId() : null;
    }

    /**
     * Sets the home planet for this player.
     * <p>
     * The home planet is automatically added to the player's owned planets.
     * </p>
     *
     * @param planetId the home planet ID
     */
    public void setHomePlanet(int planetId) {
        if (homeLocation != null) {
            this.homeLocation = homeLocation.withPlanetId(planetId);
        } else {
            this.homeLocation = new HomeLocation(planetId, null);
        }
        ownedPlanetIds.add(planetId);
    }

    /**
     * Returns the ID of this player's home solar system.
     *
     * @return the home system ID, or null if not set
     */
    public Integer getHomeSystemId() {
        return homeLocation != null ? homeLocation.getSystemId() : null;
    }

    /**
     * Sets the home solar system for this player.
     *
     * @param systemId the home system ID
     */
    public void setHomeSystem(int systemId) {
        if (homeLocation != null) {
            this.homeLocation = homeLocation.withSystemId(systemId);
        } else {
            this.homeLocation = new HomeLocation(null, systemId);
        }
    }

    /**
     * Returns true if this player has a home planet set.
     *
     * @return true if home planet is set
     */
    public boolean hasHomePlanet() {
        return homeLocation != null && homeLocation.hasPlanet();
    }

    /**
     * Returns true if this player has a home system set.
     *
     * @return true if home system is set
     */
    public boolean hasHomeSystem() {
        return homeLocation != null && homeLocation.hasSystem();
    }

    /**
     * Returns the sector row of this player's home system.
     *
     * @return the sector row (0-9), or null if not in a galaxy
     */
    public Integer getHomeSectorRow() {
        return homeLocation != null ? homeLocation.getSectorRow() : null;
    }

    /**
     * Sets the sector row for this player's home system.
     *
     * @param row the sector row (0-9)
     */
    public void setHomeSectorRow(int row) {
        if (homeLocation != null) {
            this.homeLocation = homeLocation.withSector(row, homeLocation.getSectorCol());
        }
    }

    /**
     * Returns the sector column of this player's home system.
     *
     * @return the sector column (0-9), or null if not in a galaxy
     */
    public Integer getHomeSectorCol() {
        return homeLocation != null ? homeLocation.getSectorCol() : null;
    }

    /**
     * Sets the sector column for this player's home system.
     *
     * @param col the sector column (0-9)
     */
    public void setHomeSectorCol(int col) {
        if (homeLocation != null) {
            this.homeLocation = homeLocation.withSector(homeLocation.getSectorRow(), col);
        }
    }

    /**
     * Returns the subsector row of this player's home system.
     *
     * @return the subsector row (0-9), or null if not set
     */
    public Integer getHomeSubSectorRow() {
        return homeLocation != null ? homeLocation.getSubSectorRow() : null;
    }

    /**
     * Sets the subsector row for this player's home system.
     *
     * @param row the subsector row (0-9)
     */
    public void setHomeSubSectorRow(int row) {
        if (homeLocation != null) {
            this.homeLocation = homeLocation.withSubSector(row, homeLocation.getSubSectorCol());
        }
    }

    /**
     * Returns the subsector column of this player's home system.
     *
     * @return the subsector column (0-9), or null if not set
     */
    public Integer getHomeSubSectorCol() {
        return homeLocation != null ? homeLocation.getSubSectorCol() : null;
    }

    /**
     * Sets the subsector column for this player's home system.
     *
     * @param col the subsector column (0-9)
     */
    public void setHomeSubSectorCol(int col) {
        if (homeLocation != null) {
            this.homeLocation = homeLocation.withSubSector(homeLocation.getSubSectorRow(), col);
        }
    }

    /**
     * Returns the sector name for this player's home system.
     *
     * @return the sector name, or "Unassigned" if not in a galaxy
     */
    public String getHomeSectorName() {
        if (homeLocation == null || !homeLocation.hasGalaxyCoordinates()) {
            return "Unassigned";
        }
        return SectorBuilder.generateSectorName(homeLocation.getSectorRow(), homeLocation.getSectorCol());
    }

    /**
     * Returns the subsector name for this player's home system.
     *
     * @return the subsector name, or "Unassigned" if not set
     */
    public String getHomeSubSectorName() {
        if (homeLocation == null || homeLocation.getSubSectorRow() == null || homeLocation.getSubSectorCol() == null) {
            return "Unassigned";
        }
        return SectorBuilder.generateSubSectorName(homeLocation.getSubSectorRow(), homeLocation.getSubSectorCol());
    }

    // ========================================================================
    // Commander (TravellerCharacter) integration
    // ========================================================================

    /**
     * Returns the Traveller character serving as this player's empire commander.
     *
     * @return the commander, or null if not assigned
     */
    public TravellerCharacter getCommander() {
        return commander;
    }

    /**
     * Sets the Traveller character serving as this player's empire commander.
     *
     * @param commander the commander character, or null to remove
     */
    public void setCommander(TravellerCharacter commander) {
        this.commander = commander;
    }

    /**
     * Returns true if this player has a Traveller character commander.
     *
     * @return true if a commander is assigned
     */
    public boolean hasCommander() {
        return commander != null;
    }

    /**
     * Returns the commander bonus for a given bonus type.
     * <p>
     * Based on the commander's career and skills, this provides modifiers
     * to various empire operations:
     * </p>
     * <ul>
     *   <li>ARMY_COMBAT — Soldier/Mercenary career provides +1 DM per rank</li>
     *   <li>SHIP_EVASION — Pilot career provides +1% evasion per skill level</li>
     *   <li>SHIP_DAMAGE — Gunner career provides +1% damage per skill level</li>
     *   <li>SURVEY_SPEED — Explorer career provides +10% survey speed</li>
     *   <li>TRADE_PROFIT — Merchant career provides +5% profit per skill level</li>
     *   <li>GOVERNMENT — Noble career provides +1 to diplomacy checks</li>
     * </ul>
     *
     * @param type the bonus type
     * @return the bonus value (0 if no commander or no applicable career)
     */
    public double getCommanderBonus(CommanderBonusType type) {
        if (commander == null) {
            return 0;
        }
        return commander.getCommanderBonus(type);
    }

    /**
     * Types of commander bonuses derived from Traveller character careers and skills.
     */
    public enum CommanderBonusType {
        /** Ground combat die modifier from Soldier/Mercenary career. */
        ARMY_COMBAT,
        /** Ship evasion percentage bonus from Pilot career. */
        SHIP_EVASION,
        /** Ship damage percentage bonus from Gunner career. */
        SHIP_DAMAGE,
        /** Planet survey speed percentage bonus from Explorer career. */
        SURVEY_SPEED,
        /** Trade profit percentage bonus from Merchant career. */
        TRADE_PROFIT,
        /** Diplomacy/government bonus from Noble career. */
        GOVERNMENT
    }

    @Override
    public String toString() {
        String cmd = hasCommander() ? ", commander='" + commander.getName() + "'" : "";
        return String.format("Player{id=%d, email='%s', empire='%s', gov=%s, ethos=%s, eliminated=%s, homePlanet=%s%s}",
                id, email, empireName, governmentType, ethos, eliminated, getHomePlanetId(), cmd);
    }
}
