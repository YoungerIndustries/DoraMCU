package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.manager.CharacterLogManager;
import com.yisy.modules.doramcu.hex.manager.GalaxyManager;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.service.GameSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Core service managing the Traveller adventure lifecycle in the 4X universe.
 * <p>
 * When a character with COMPLETED status starts an adventure, this service:
 * </p>
 * <ol>
 *   <li>Sets the character status to TRAVELLING</li>
 *   <li>Creates a single-ship fleet (FRIGATE by default)</li>
 *   <li>Positions the character at a starting system</li>
 *   <li>Converts career wealth to credits for ship purchases</li>
 * </ol>
 * <p>
 * During the adventure, the character can:
 * </p>
 * <ul>
 *   <li>Travel to other systems</li>
 *   <li>Purchase ships with their credits</li>
 *   <li>Book passage on other ships</li>
 *   <li>Witness 4X events (battles, fleet movements, conquests)</li>
 * </ul>
 * <p>
 * All witnessed events are recorded as {@link CharacterExperience} entries
 * in the character's {@link CharacterLog}.
 * </p>
 */
@Service
public class TravellerAdventureService {

    private static final Logger log = LoggerFactory.getLogger(TravellerAdventureService.class);

    /** Maximum hex distance for nearby event detection. */
    public static final int NEARBY_EVENT_RANGE = 3;

    /** Default starting credits for adventure characters with no wealth. */
    public static final long DEFAULT_STARTING_CREDITS = 10_000L;

    private final CharacterService characterService;
    private final GameSessionService gameSessionService;
    private CharacterLogManager logManager;

    /** Maps character ID to their adventure state. */
    private final Map<Integer, TravellerAdventure> adventures = new HashMap<>();

    /** Next adventure event ID for unique event identifiers. */
    private int nextEventId = 1;

    public TravellerAdventureService(
            CharacterService characterService,
            GameSessionService gameSessionService) {
        this.characterService = characterService;
        this.gameSessionService = gameSessionService;
    }

    /**
     * Sets the character log manager for a specific game session.
     * <p>
     * Must be called before adventure operations that log experiences,
     * to ensure experiences are recorded to the correct game context.
     * </p>
     *
     * @param logManager the character log manager for the current game session
     */
    public void setLogManager(CharacterLogManager logManager) {
        this.logManager = logManager;
    }

    // ============================================================
    // Adventure Lifecycle
    // ============================================================

    /**
     * Starts an adventure for a completed Traveller character.
     * <p>
     * The character must have COMPLETED status and be alive.
     * Creates a single-ship fleet with a FRIGATE and positions the character
     * at a starting system in the 4X game.
     * </p>
     *
     * @param characterId the character's ID
     * @return the created TravellerAdventure
     * @throws IllegalArgumentException if the character is not found or not eligible
     * @throws IllegalStateException if the character is not in COMPLETED status
     */
    public TravellerAdventure startAdventure(int characterId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }

        if (!character.isAlive()) {
            throw new IllegalStateException("Character " + character.getName() + " is dead");
        }

        // Check if already in an adventure (before status check — TRAVELLING chars can re-enter)
        if (adventures.containsKey(characterId)) {
            log.info("Character {} already has an active adventure", character.getName());
            return adventures.get(characterId);
        }

        if (character.getStatus() != CharacterStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Character must be in COMPLETED status to start adventure, current: " + character.getStatus().getDisplayName());
        }

        // Get or create a game session
        Game game = getOrCreateGame();

        // Create the adventure
        TravellerAdventure adventure = new TravellerAdventure(characterId);

        // Convert wealth to credits (1 wealth d6 = 5,000 credits, minimum default)
        long credits = Math.max(character.getWealth() * 5_000L, DEFAULT_STARTING_CREDITS);
        adventure.setCredits(credits);

        // Create a single-ship fleet with a starting frigate
        Fleet fleet = createStartingFleet(game, character);
        adventure.setFleetId(fleet.getId());

        // Position at a starting system
        positionAtStartingSystem(adventure, game);

        // Set character status to TRAVELLING
        character.setStatus(CharacterStatus.TRAVELLING);

        // Record the adventure start experience
        CharacterExperience startExp = new CharacterExperience(
                ExperienceType.EXPLORATION,
                "Adventure Begins"
        ).description(character.getName() + " begins their adventure in the stars.")
                .narrative(character.getName() + " sets out into the galaxy with a " +
                        TravellerAdventure.DEFAULT_STARTING_SHIP + ", ready to explore the unknown.")
                .location(adventure.getCurrentSystemName())
                .date(LocalDate.now())
                .characterAge(character.getAge())
                .outcome(CharacterExperience.Outcome.SUCCESS);
        adventure.addExperience(startExp);

        // Log to character's log
        CharacterLog charLog = logManager.getLog(characterId);
        if (charLog != null) {
            charLog.addExperience(startExp);
        }

        adventures.put(characterId, adventure);
        log.info("Started adventure for character {} in system {}", character.getName(), adventure.getCurrentSystemName());

        return adventure;
    }

    /**
     * Ends an adventure for a character.
     * <p>
     * Sets the character status back to COMPLETED and archives the adventure log.
     * </p>
     *
     * @param characterId the character's ID
     * @throws IllegalArgumentException if the character is not found
     * @throws IllegalStateException if the character is not TRAVELLING
     */
    public void endAdventure(int characterId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }

        if (character.getStatus() != CharacterStatus.TRAVELLING) {
            throw new IllegalStateException(
                    "Character is not travelling, current status: " + character.getStatus().getDisplayName());
        }

        TravellerAdventure adventure = adventures.remove(characterId);
        if (adventure == null) {
            log.warn("No adventure found for character {}", character.getName());
        } else {
            // Record the adventure end experience
            CharacterExperience endExp = new CharacterExperience(
                    ExperienceType.REFLECTION,
                    "Adventure Ends"
            ).description(character.getName() + " concludes their adventure.")
                    .narrative(character.getName() + " has witnessed " + adventure.getAdventureLog().size() +
                            " events during their travels across the galaxy.")
                    .location(adventure.getCurrentSystemName())
                    .date(LocalDate.now())
                    .characterAge(character.getAge())
                    .outcome(CharacterExperience.Outcome.SUCCESS);

            adventure.addExperience(endExp);
            CharacterLog charLog = logManager.getLog(characterId);
            if (charLog != null) {
                charLog.addExperience(endExp);
            }
        }

        character.setStatus(CharacterStatus.COMPLETED);
        log.info("Ended adventure for character {}", character.getName());
    }

    // ============================================================
    // Travel
    // ============================================================

    /**
     * Travels to a destination system.
     * <p>
     * Moves the character's fleet to the destination and checks for events
     * en route. Any witnessed events are recorded as experiences.
     * </p>
     *
     * @param characterId the character's ID
     * @param destinationSystemId the destination system ID
     * @return the adventure state after travel
     * @throws IllegalArgumentException if the character or system is not found
     */
    public AdventureState travelToSystem(int characterId, int destinationSystemId) {
        TravellerAdventure adventure = getAdventure(characterId);
        Game game = getOrCreateGame();

        // Find the destination system
        GalaxyManager galaxyManager = new GalaxyManager(game);
        SolarSystem destSystem = galaxyManager.getSolarSystem(destinationSystemId);
        if (destSystem == null) {
            throw new IllegalArgumentException("Solar system not found: " + destinationSystemId);
        }

        String destName = "System-" + destinationSystemId;
        adventure.setCurrentSystem(destinationSystemId, destName);
        adventure.clearDestination();

        // Record travel experience
        CharacterExperience travelExp = new CharacterExperience(
                ExperienceType.EXPLORATION,
                "Travel to " + destName
        ).description("Traveled to " + destName)
                .narrative("The ship jumps through hyperspace, arriving at the " + destName + " system.")
                .location(destName)
                .date(LocalDate.now())
                .characterAge(characterService.getCharacter(characterId).getAge())
                .outcome(CharacterExperience.Outcome.SUCCESS);
        adventure.addExperience(travelExp);

        CharacterLog charLog = logManager.getLog(characterId);
        if (charLog != null) {
            charLog.addExperience(travelExp);
        }

        // Check for events at the destination
        List<AdventureEvent> events = detectNearbyEvents(characterId);
        for (AdventureEvent event : events) {
            if (event.getSystemId() == destinationSystemId) {
                witnessEvent(characterId, event);
            }
        }

        log.info("Character {} traveled to {}", characterService.getCharacter(characterId).getName(), destName);
        return getAdventureState(characterId);
    }

    /**
     * Sets a destination for the character.
     *
     * @param characterId the character's ID
     * @param destinationSystemId the destination system ID
     */
    public void setDestination(int characterId, int destinationSystemId) {
        TravellerAdventure adventure = getAdventure(characterId);
        Game game = getOrCreateGame();

        GalaxyManager galaxyManager = new GalaxyManager(game);
        SolarSystem destSystem = galaxyManager.getSolarSystem(destinationSystemId);
        if (destSystem == null) {
            throw new IllegalArgumentException("Solar system not found: " + destinationSystemId);
        }

        String destName = "System-" + destinationSystemId;
        adventure.setDestination(destinationSystemId, destName);
        log.info("Character {} set destination to {}", characterService.getCharacter(characterId).getName(), destName);
    }

    // ============================================================
    // Ship Management
    // ============================================================

    /**
     * Purchases a new ship for the character.
     * <p>
     * Deducts the ship cost from the character's credits and adds the ship
     * to their fleet.
     * </p>
     *
     * @param characterId the character's ID
     * @param shipType the type of ship to purchase
     * @return the purchased ship
     * @throws IllegalArgumentException if insufficient credits
     */
    public Ship purchaseShip(int characterId, ShipType shipType) {
        TravellerAdventure adventure = getAdventure(characterId);
        Game game = getOrCreateGame();

        long cost = TravellerAdventure.getShipCost(shipType);
        if (!adventure.deductCredits(cost)) {
            throw new IllegalArgumentException(
                    "Insufficient credits: need " + cost + ", have " + adventure.getCredits());
        }

        // Create the ship
        int shipId = game.getNextShipIdAndIncrement();
        Ship ship = new Ship(shipId, shipType);
        ship.setName(characterService.getCharacter(characterId).getName() + "'s " + shipType.name());
        game.addShip(ship);

        // Add to fleet
        Fleet fleet = game.getFleets().get(adventure.getFleetId());
        if (fleet != null) {
            fleet.addShip(ship);
        }

        // Record acquisition experience
        CharacterExperience acqExp = new CharacterExperience(
                ExperienceType.ACQUISITION,
                "Purchased " + shipType.name()
        ).description("Bought a " + shipType.name() + " for " + cost + " credits.")
                .narrative("The character acquired a new " + shipType.name() +
                        " named '" + ship.getName() + "' to expand their fleet.")
                .location(adventure.getCurrentSystemName())
                .date(LocalDate.now())
                .characterAge(characterService.getCharacter(characterId).getAge())
                .outcome(CharacterExperience.Outcome.SUCCESS);
        adventure.addExperience(acqExp);

        CharacterLog charLog = logManager.getLog(characterId);
        if (charLog != null) {
            charLog.addExperience(acqExp);
        }

        log.info("Character {} purchased {} for {} credits",
                characterService.getCharacter(characterId).getName(), shipType, cost);
        return ship;
    }

    // ============================================================
    // Passenger Booking
    // ============================================================

    /**
     * Books passage on another ship traveling to a destination.
     *
     * @param characterId the character's ID
     * @param destinationSystemId the destination system ID
     * @return true if passage was booked
     */
    public boolean bookPassage(int characterId, int destinationSystemId) {
        TravellerAdventure adventure = getAdventure(characterId);
        Game game = getOrCreateGame();

        // Find the destination system
        GalaxyManager galaxyManager = new GalaxyManager(game);
        SolarSystem destSystem = galaxyManager.getSolarSystem(destinationSystemId);
        if (destSystem == null) {
            throw new IllegalArgumentException("Solar system not found: " + destinationSystemId);
        }

        String destName = "System-" + destinationSystemId;

        // Find any fleet that could provide passage
        int passengerShipId = 0;
        for (Fleet fleet : game.getFleets().values()) {
            if (fleet.getId() != adventure.getFleetId() && !fleet.getShips().isEmpty()) {
                passengerShipId = fleet.getShips().get(0).getId();
                break;
            }
        }

        if (passengerShipId == 0) {
            log.info("No available ships for passage to {}", destName);
            return false;
        }

        adventure.setPassenger(true, passengerShipId);
        adventure.setDestination(destinationSystemId, destName);

        // Record travel experience
        CharacterExperience travelExp = new CharacterExperience(
                ExperienceType.TRADE,
                "Booked Passage to " + destName
        ).description("Booked passage on a ship traveling to " + destName)
                .narrative("The character secured passage aboard a merchant vessel bound for " + destName + ".")
                .location(adventure.getCurrentSystemName())
                .date(LocalDate.now())
                .characterAge(characterService.getCharacter(characterId).getAge())
                .outcome(CharacterExperience.Outcome.SUCCESS);
        adventure.addExperience(travelExp);

        CharacterLog charLog = logManager.getLog(characterId);
        if (charLog != null) {
            charLog.addExperience(travelExp);
        }

        log.info("Character {} booked passage to {}",
                characterService.getCharacter(characterId).getName(), destName);
        return true;
    }

    /**
     * Cancels passenger status (character is back in their own ship).
     *
     * @param characterId the character's ID
     */
    public void cancelPassage(int characterId) {
        TravellerAdventure adventure = getAdventure(characterId);
        adventure.setPassenger(false, 0);
        log.info("Character {} cancelled passage", characterService.getCharacter(characterId).getName());
    }

    // ============================================================
    // Event Detection and Witnessing
    // ============================================================

    /**
     * Detects nearby 4X events that the character can witness.
     * <p>
     * Scans for battles, fleet movements, and planet conquests within
     * {@link #NEARBY_EVENT_RANGE} hex distance of the character's current system.
     * </p>
     *
     * @param characterId the character's ID
     * @return list of nearby events
     */
    public List<AdventureEvent> getNearbyEvents(int characterId) {
        return detectNearbyEvents(characterId);
    }

    /**
     * Witnesses a specific event and records it as a character experience.
     *
     * @param characterId the character's ID
     * @param event the event to witness
     * @return the recorded experience
     */
    public CharacterExperience witnessEvent(int characterId, AdventureEvent event) {
        TravellerAdventure adventure = getAdventure(characterId);
        TravellerCharacter character = characterService.getCharacter(characterId);

        ExperienceType expType = event.toExperienceType();
        CharacterExperience exp = new CharacterExperience(
                expType,
                event.getEventType().name().replace('_', ' ')
        ).description(event.getDescription())
                .narrative("While near " + event.getSystemName() +
                        " (distance: " + event.getDistanceHex() + " hexes), " +
                        character.getName() + " witnessed: " + event.getDescription())
                .location(event.getSystemName())
                .date(LocalDate.now())
                .characterAge(character.getAge())
                .outcome(CharacterExperience.Outcome.SUCCESS);

        adventure.addExperience(exp);

        CharacterLog charLog = logManager.getLog(characterId);
        if (charLog != null) {
            charLog.addExperience(exp);
        }

        log.info("Character {} witnessed event: {}", character.getName(), event.getDescription());
        return exp;
    }

    // ============================================================
    // Query Methods
    // ============================================================

    /**
     * Gets the current adventure state for a character.
     *
     * @param characterId the character's ID
     * @return the adventure state DTO
     */
    public AdventureState getAdventureState(int characterId) {
        TravellerAdventure adventure = getAdventure(characterId);
        TravellerCharacter character = characterService.getCharacter(characterId);
        Game game = getOrCreateGame();

        AdventureState state = new AdventureState(characterId, character.getName());
        state.setCurrentSystemName(adventure.getCurrentSystemName());
        state.setCurrentPlanetName(adventure.getCurrentPlanetName());
        state.setCredits(adventure.getCredits());
        state.setPassenger(adventure.isPassenger());
        state.setDestinationSystemName(adventure.getDestinationSystemName());

        // Get ship info
        Fleet fleet = game.getFleets().get(adventure.getFleetId());
        if (fleet != null && !fleet.getShips().isEmpty()) {
            Ship primaryShip = fleet.getShips().get(0);
            state.setShipName(primaryShip.getName());
            state.setShipType(primaryShip.getShipType().name());
            state.setShipHullPoints(primaryShip.getCurrentHullPoints());
            state.setShipShieldPoints(primaryShip.getCurrentShieldPoints());
        }

        // Get nearby events
        List<AdventureEvent> events = detectNearbyEvents(characterId);
        for (AdventureEvent event : events) {
            state.addNearbyEvent(event);
        }

        // Get recent experiences (last 10)
        List<CharacterExperience> log = adventure.getAdventureLog();
        int start = Math.max(0, log.size() - 10);
        for (int i = start; i < log.size(); i++) {
            state.addRecentExperience(log.get(i));
        }

        return state;
    }

    /**
     * Lists reachable systems from the character's current position.
     *
     * @param characterId the character's ID
     * @return list of system info maps
     */
    public List<Map<String, Object>> getReachableSystems(int characterId) {
        getAdventure(characterId); // validates existence
        Game game = getOrCreateGame();

        List<Map<String, Object>> systems = new ArrayList<>();
        for (Map.Entry<Integer, SolarSystem> entry : game.getSolarSystems().entrySet()) {
            SolarSystem system = entry.getValue();
            Map<String, Object> info = new HashMap<>();
            info.put("systemId", system.getId());
            info.put("systemName", "System-" + system.getId());
            systems.add(info);
        }
        return systems;
    }

    /**
     * Gets the adventure for a character.
     *
     * @param characterId the character's ID
     * @return the adventure
     * @throws IllegalArgumentException if no active adventure
     */
    public TravellerAdventure getAdventure(int characterId) {
        TravellerAdventure adventure = adventures.get(characterId);
        if (adventure == null) {
            TravellerCharacter character = characterService.getCharacter(characterId);
            if (character == null) {
                throw new IllegalArgumentException("Character not found: " + characterId);
            }
            throw new IllegalStateException(
                    "Character " + character.getName() + " has no active adventure");
        }
        return adventure;
    }

    /**
     * Checks if a character has an active adventure.
     *
     * @param characterId the character's ID
     * @return true if the character has an active adventure
     */
    public boolean hasActiveAdventure(int characterId) {
        return adventures.containsKey(characterId);
    }

    /**
     * Returns all active adventures.
     *
     * @return unmodifiable map of character ID to adventure
     */
    public Map<Integer, TravellerAdventure> getAllAdventures() {
        return Collections.unmodifiableMap(adventures);
    }

    // ============================================================
    // Internal Helpers
    // ============================================================

    /**
     * Gets the current game or creates a new one.
     */
    private Game getOrCreateGame() {
        com.yisy.modules.doramcu.model.GameSession session = gameSessionService.getDefaultSession();
        Game game = (session != null) ? (Game) session.getGame() : null;
        if (game == null) {
            game = new Game("Traveller Adventure");
        }
        return game;
    }

    /**
     * Creates a starting fleet with a single frigate.
     */
    private Fleet createStartingFleet(Game game, TravellerCharacter character) {
        int fleetId = game.getNextFleetIdAndIncrement();
        Fleet fleet = new Fleet(fleetId, character.getName() + "'s Fleet");

        int shipId = game.getNextShipIdAndIncrement();
        Ship ship = new Ship(shipId, TravellerAdventure.DEFAULT_STARTING_SHIP);
        ship.setName(character.getName() + "'s Ship");
        game.addShip(ship);

        fleet.addShip(ship);

        // Create a player to own the fleet
        Player player = game.createPlayer(character.getId(), character.getName());
        game.addFleet(fleet, player);

        return fleet;
    }

    /**
     * Positions the character at a starting system.
     */
    private void positionAtStartingSystem(TravellerAdventure adventure, Game game) {
        // Find the first available system or use a default one
        Map<Integer, SolarSystem> systems = game.getSolarSystems();
        if (systems.isEmpty()) {
            // Use a default system name
            adventure.setCurrentSystem(1, "Sol");
        } else {
            // Use the first system
            SolarSystem firstSystem = systems.values().iterator().next();
            adventure.setCurrentSystem(firstSystem.getId(), "System-" + firstSystem.getId());
        }

        // Position the fleet at the system
        Fleet fleet = game.getFleets().get(adventure.getFleetId());
        if (fleet != null) {
            fleet.setPosition(new Hex3D(0, 0, 0));
        }
    }

    /**
     * Detects nearby 4X events within range of the character.
     */
    private List<AdventureEvent> detectNearbyEvents(int characterId) {
        TravellerAdventure adventure = adventures.get(characterId);
        if (adventure == null) {
            return Collections.emptyList();
        }

        Game game = getOrCreateGame();
        List<AdventureEvent> events = new ArrayList<>();

        // Detect battles
        for (Battle battle : game.getBattles().values()) {
            if (battle.getState() == Battle.BattleState.IN_PROGRESS ||
                battle.getState() == Battle.BattleState.COMPLETED) {
                int eventId = nextEventId++;
                String desc = String.format("Battle between %s and %s",
                        battle.getAttacker().getName(), battle.getDefender().getName());
                events.add(new AdventureEvent(eventId, AdventureEvent.EventType.BATTLE,
                        adventure.getCurrentSystemId(), adventure.getCurrentSystemName(),
                        desc, 0));
            }
        }

        // Detect fleet movements (fleets other than the character's)
        for (Fleet fleet : game.getFleets().values()) {
            if (fleet.getId() != adventure.getFleetId() && fleet.getPosition() != null) {
                int eventId = nextEventId++;
                String desc = fleet.getName() + " is present in the system with " +
                        fleet.getShipCount() + " ships";
                events.add(new AdventureEvent(eventId, AdventureEvent.EventType.FLEET_MOVEMENT,
                        adventure.getCurrentSystemId(), adventure.getCurrentSystemName(),
                        desc, 0));
            }
        }

        return events;
    }

    // ============================================================
    // Trade Operations
    // ============================================================

    /**
     * Buys a commodity at the character's current system.
     * <p>
     * The character must have an active adventure and enough credits.
     * The commodity price is determined by the WorldEconomy for the current system.
     * </p>
     *
     * @param characterId the character's ID
     * @param commodity the commodity name
     * @param quantity the quantity to buy
     * @return the trade result with cost and updated credits
     * @throws IllegalArgumentException if the character has no active adventure
     * @throws IllegalStateException if the character has insufficient credits
     */
    public TravellerAdventure.TradeResult buyCommodity(int characterId, String commodity, int quantity) {
        TravellerAdventure adventure = getAdventure(characterId);
        com.yisy.modules.doramcu.hex.manager.TradeManager tradeManager = getOrCreateTradeManager();

        Commodity comm = Commodity.valueOf(commodity.toUpperCase());
        int systemId = adventure.getCurrentSystemId();
        long pricePerUnit = tradeManager.getBuyPrice(systemId, comm);
        long totalCost = pricePerUnit * quantity;

        if (adventure.getCredits() < totalCost) {
            throw new IllegalStateException(
                    "Insufficient credits: need " + totalCost + " cr, have " + adventure.getCredits() + " cr");
        }

        adventure.addCredits(-totalCost);
        adventure.addCargo(commodity, quantity);

        CharacterExperience exp = new CharacterExperience(
                ExperienceType.TRADE,
                "Bought " + quantity + " units of " + commodity
        ).description("Purchased " + quantity + " units of " + commodity +
                " at " + adventure.getCurrentSystemName() + " for " + totalCost + " cr");
        adventure.addExperience(exp);

        log.info("Character {} bought {} units of {} for {} cr at {}",
                characterId, quantity, commodity, totalCost, adventure.getCurrentSystemName());

        return new TravellerAdventure.TradeResult(
                commodity, quantity, totalCost, 0, 0,
                adventure.getCredits(),
                "Bought " + quantity + " units of " + commodity + " for " + totalCost + " cr");
    }

    /**
     * Sells a commodity at the character's current system.
     * <p>
     * The character must have the commodity in cargo.
     * The commodity price is determined by the WorldEconomy for the current system.
     * </p>
     *
     * @param characterId the character's ID
     * @param commodity the commodity name
     * @param quantity the quantity to sell
     * @return the trade result with revenue and updated credits
     * @throws IllegalArgumentException if the character has no active adventure
     * @throws IllegalStateException if the character has insufficient cargo
     */
    public TravellerAdventure.TradeResult sellCommodity(int characterId, String commodity, int quantity) {
        TravellerAdventure adventure = getAdventure(characterId);
        com.yisy.modules.doramcu.hex.manager.TradeManager tradeManager = getOrCreateTradeManager();

        int available = adventure.getCargoQuantity(commodity);
        if (available < quantity) {
            throw new IllegalStateException(
                    "Insufficient cargo: have " + available + " units of " + commodity +
                            ", need " + quantity);
        }

        Commodity comm = Commodity.valueOf(commodity.toUpperCase());
        int systemId = adventure.getCurrentSystemId();
        long pricePerUnit = tradeManager.getSellPrice(systemId, comm);
        long totalRevenue = pricePerUnit * quantity;

        adventure.removeCargo(commodity, quantity);
        adventure.addCredits(totalRevenue);

        CharacterExperience exp = new CharacterExperience(
                ExperienceType.TRADE,
                "Sold " + quantity + " units of " + commodity
        ).description("Sold " + quantity + " units of " + commodity +
                " at " + adventure.getCurrentSystemName() + " for " + totalRevenue + " cr");
        adventure.addExperience(exp);

        log.info("Character {} sold {} units of {} for {} cr at {}",
                characterId, quantity, commodity, totalRevenue, adventure.getCurrentSystemName());

        return new TravellerAdventure.TradeResult(
                commodity, quantity, 0, totalRevenue, totalRevenue,
                adventure.getCredits(),
                "Sold " + quantity + " units of " + commodity + " for " + totalRevenue + " cr");
    }

    /**
     * Gets the market prices at the character's current system.
     *
     * @param characterId the character's ID
     * @return a map of commodity name to price
     */
    public Map<String, Long> getMarketPrices(int characterId) {
        TravellerAdventure adventure = getAdventure(characterId);
        com.yisy.modules.doramcu.hex.manager.TradeManager tradeManager = getOrCreateTradeManager();
        int systemId = adventure.getCurrentSystemId();
        Map<String, Long> prices = new HashMap<>();
        for (Commodity comm : Commodity.values()) {
            long price = tradeManager.getBuyPrice(systemId, comm);
            if (price > 0) {
                prices.put(comm.name(), price);
            }
        }
        return prices;
    }

    /**
     * Gets the TradeManager instance, creating one if needed.
     */
    private com.yisy.modules.doramcu.hex.manager.TradeManager getOrCreateTradeManager() {
        Game game = getOrCreateGame();
        com.yisy.modules.doramcu.hex.manager.TradeManager tradeManager = game.getTradeManager();
        if (tradeManager != null) {
            return tradeManager;
        }
        // Fallback: create a simple trade manager
        return new com.yisy.modules.doramcu.hex.manager.TradeManager(game);
    }
}
