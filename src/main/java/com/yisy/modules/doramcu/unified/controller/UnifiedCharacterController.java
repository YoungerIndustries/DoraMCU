package com.yisy.modules.doramcu.unified.controller;

import com.yisy.modules.doramcu.traveller.Career;
import com.yisy.modules.doramcu.traveller.CharacterStatus;
import com.yisy.modules.doramcu.traveller.TravellerAdventureService;
import com.yisy.modules.doramcu.traveller.TourOfDutyOption;
import com.yisy.modules.doramcu.traveller.TourOfDutyRequest;
import com.yisy.modules.doramcu.traveller.TourOfDutyService;
import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.unified.TravellerCharacterType;
import com.yisy.modules.doramcu.unified.TravellerTo4XIntegrationService;
import com.yisy.modules.doramcu.unified.UnifiedCharacterCreationService;
import com.yisy.modules.doramcu.unified.UnifiedCharacterLoginService;
import com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the unified character login and creation system.
 * <p>
 * This controller provides the entry point for players to:
 * </p>
 * <ul>
 *   <li>Log in and see all their characters across all game systems</li>
 *   <li>Search for characters by name or email</li>
 *   <li>Create new characters with game type selection</li>
 *   <li>Select Traveller character types (Empire Leader, Sector Governor, etc.)</li>
 * </ul>
 * <p>
 * All endpoints are under {@code /api/v1/unified}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/unified")
public class UnifiedCharacterController {

    private static final Logger log = LoggerFactory.getLogger(UnifiedCharacterController.class);

    private final UnifiedCharacterLoginService loginService;
    private final UnifiedCharacterCreationService creationService;
    private final TravellerTo4XIntegrationService integrationService;
    private final TourOfDutyService tourOfDutyService;
    private final TravellerAdventureService adventureService;

    public UnifiedCharacterController(
            UnifiedCharacterLoginService loginService,
            UnifiedCharacterCreationService creationService,
            TravellerTo4XIntegrationService integrationService,
            TourOfDutyService tourOfDutyService,
            TravellerAdventureService adventureService) {
        this.loginService = loginService;
        this.creationService = creationService;
        this.integrationService = integrationService;
        this.tourOfDutyService = tourOfDutyService;
        this.adventureService = adventureService;
    }

    // ============================================================
    // Login Endpoints
    // ============================================================

    /**
     * Unified character login.
     * <p>
     * Player provides name and/or email to find their characters.
     * If no criteria are provided, returns the available games list.
     * </p>
     *
     * @param request the login request with optional name and email
     * @return response with matching characters and available games
     */
    @PostMapping("/login")
    public ResponseEntity<UnifiedLoginResponse> unifiedLogin(
            @RequestBody(required = false) UnifiedLoginRequest request) {
        log.debug("POST /api/v1/unified/login - unified character login");

        if (request == null) {
            request = new UnifiedLoginRequest();
        }

        UnifiedLoginResponse response = loginService.processLogin(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search for characters by name.
     *
     * @param firstName the character's first name
     * @param lastName  the character's last name (optional)
     * @return list of matching characters
     */
    @GetMapping("/characters/search")
    public ResponseEntity<List<UnifiedCharacterSummary>> searchCharacters(
            @RequestParam String firstName,
            @RequestParam(required = false) String lastName) {
        log.debug("GET /api/v1/unified/characters/search?firstName={}&lastName={}", firstName, lastName);

        List<UnifiedCharacterSummary> characters = loginService.getCharactersByName(firstName, lastName);
        return ResponseEntity.ok(characters);
    }

    /**
     * List all characters for a player by email.
     *
     * @param email the player's email address
     * @return list of all characters for that player
     */
    @GetMapping("/characters/email")
    public ResponseEntity<List<UnifiedCharacterSummary>> getCharactersByEmail(
            @RequestParam String email) {
        log.debug("GET /api/v1/unified/characters/email?email={}", email);

        List<UnifiedCharacterSummary> characters = loginService.getCharactersByEmail(email);
        return ResponseEntity.ok(characters);
    }

    // ============================================================
    // Game Selection Endpoints
    // ============================================================

    /**
     * Get the list of available games for character creation.
     *
     * @return list of available game types
     */
    @GetMapping("/games")
    public ResponseEntity<List<GameType>> getAvailableGames() {
        log.debug("GET /api/v1/unified/games - listing available games");

        List<GameType> games = loginService.getAvailableGames();
        return ResponseEntity.ok(games);
    }

    /**
     * Get the Traveller character types for the character type selection screen.
     * <p>
     * This is called after the player selects Traveller as their game.
     * </p>
     *
     * @return list of Traveller character types with descriptions
     */
    @GetMapping("/traveller/types")
    public ResponseEntity<List<TravellerCharacterType>> getTravellerCharacterTypes() {
        log.debug("GET /api/v1/unified/traveller/types - listing Traveller character types");

        List<TravellerCharacterType> types = loginService.getTravellerCharacterTypes();
        return ResponseEntity.ok(types);
    }

    /**
     * Get detailed information about a specific Traveller character type.
     *
     * @param type the character type name
     * @return details about the character type
     */
    @GetMapping("/traveller/types/{type}")
    public ResponseEntity<Map<String, String>> getTravellerCharacterType(
            @PathVariable String type) {
        log.debug("GET /api/v1/unified/traveller/types/{}", type);

        try {
            TravellerCharacterType charType = TravellerCharacterType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(Map.of(
                    "type", type,
                    "displayName", charType.getDisplayName(),
                    "shortDescription", charType.getShortDescription(),
                    "fullDescription", charType.getFullDescription(),
                    "isFullCharacter", String.valueOf(charType.isFullCharacter()),
                    "isManagementRole", String.valueOf(charType.isManagementRole())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown Traveller character type: " + type));
        }
    }

    // ============================================================
    // Character Creation Endpoints
    // ============================================================

    /**
     * Start the character creation flow.
     * <p>
     * Returns the available games for the player to choose from.
     * This is the first step — the player selects which game to play.
     * </p>
     *
     * @return available games for character creation
     */
    @PostMapping("/characters/create/start")
    public ResponseEntity<Map<String, Object>> startCharacterCreation() {
        log.debug("POST /api/v1/unified/characters/create/start - starting character creation");

        List<GameType> games = creationService.getAvailableGames();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message", "Select which game you wish to play first",
                "games", games
        ));
    }

    /**
     * Get the character type options for Traveller.
     * <p>
     * Called after the player selects Traveller as their game.
     * Presents the character type options: Empire Leader, Sector Governor,
     * Subsector Chief, System Manager, or Full Traveller Character.
     * </p>
     *
     * @return list of Traveller character types
     */
    @PostMapping("/characters/create/traveller/types")
    public ResponseEntity<Map<String, Object>> getTravellerCreationTypes() {
        log.debug("POST /api/v1/unified/characters/create/traveller/types - Traveller character type selection");

        List<TravellerCharacterType> types = creationService.getTravellerCharacterTypes();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message", "What kind of character do you wish to be?",
                "gameType", GameType.TRAVELLER,
                "characterTypes", types
        ));
    }

    /**
     * Create a new character through the unified system.
     * <p>
     * The request must include the game type, character name, and email.
     * For Traveller, the travellerCharacterType should also be specified.
     * </p>
     *
     * @param request the character creation request
     * @return the created character summary
     */
    @PostMapping("/characters")
    public ResponseEntity<UnifiedCharacterSummary> createCharacter(
            @RequestBody CreateCharacterRequest request) {
        log.debug("POST /api/v1/unified/characters - creating character: {}", request.getFullName());

        try {
            UnifiedCharacterSummary character = creationService.createCharacter(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(character);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create character: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================
    // 4X Integration Endpoints
    // ============================================================

    /**
     * List available empires for military character assignment.
     * <p>
     * Returns the empires currently in the default game session.
     * </p>
     *
     * @return list of empire info
     */
    @GetMapping("/empires")
    public ResponseEntity<List<Map<String, Object>>> getAvailableEmpires() {
        log.debug("GET /api/v1/unified/empires - listing available empires");

        List<Map<String, Object>> empires = integrationService.getAvailableEmpires();
        return ResponseEntity.ok(empires);
    }

    /**
     * List available military careers for fleet command.
     *
     * @return list of military careers
     */
    @GetMapping("/traveller/military/careers")
    public ResponseEntity<List<Career>> getMilitaryCareers() {
        log.debug("GET /api/v1/unified/traveller/military/careers - listing military careers");

        List<Career> careers = integrationService.getMilitaryCareers();
        return ResponseEntity.ok(careers);
    }

    /**
     * Integrate a military Traveller character into a tour of duty.
     * <p>
     * Creates a Traveller character with the specified military career,
     * serves the career terms, and assigns the character to an existing
     * fleet (navy) or army unit (army) for a tour of duty. The character
     * observes and records the unit's experiences but does not control it.
     * </p>
     *
     * @param request the character creation request
     * @param career  the military career (SOLDIER, GUNNER, or PILOT)
     * @param terms   the number of career terms to serve (default: 2)
     * @param tourTurns the number of turns in the tour of duty (default: 6)
     * @return the created character summary with tour info
     */
    @PostMapping("/traveller/military/integrate")
    public ResponseEntity<UnifiedCharacterSummary> integrateMilitaryCharacter(
            @RequestBody CreateCharacterRequest request,
            @RequestParam Career career,
            @RequestParam(defaultValue = "2") int terms,
            @RequestParam(defaultValue = "6") int tourTurns) {
        log.debug("POST /api/v1/unified/traveller/military/integrate - career={} terms={} tourTurns={}",
                career, terms, tourTurns);

        try {
            UnifiedCharacterSummary character = integrationService.integrateMilitaryCharacter(
                    request, career, terms, tourTurns);
            return ResponseEntity.status(HttpStatus.CREATED).body(character);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to integrate military character: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================
    // Tour of Duty Endpoints
    // ============================================================

    /**
     * Get available tour options for a character.
     * <p>
     * Returns the remaining career terms that the character can still serve,
     * each with the suggested career and term number.
     * </p>
     *
     * @param characterId the character's ID
     * @return list of available tour options
     */
    @GetMapping("/traveller/tour/options/{characterId}")
    public ResponseEntity<List<TourOfDutyOption>> getTourOptions(
            @PathVariable int characterId) {
        log.debug("GET /api/v1/unified/traveller/tour/options/{} - getting tour options", characterId);

        try {
            List<TourOfDutyOption> options = tourOfDutyService.getTourOptions(characterId);
            return ResponseEntity.ok(options);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get tour options: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start a tour in the 4X game.
     * <p>
     * The character's status changes to ON_DUTY and they are assigned
     * to a fleet or unit in the 4X game.
     * </p>
     *
     * @param request the tour request with FOUR_X resolve mode
     * @return character summary with ON_DUTY status
     */
    @PostMapping("/traveller/tour/start")
    public ResponseEntity<UnifiedCharacterSummary> startTourIn4X(
            @RequestBody TourOfDutyRequest request) {
        log.debug("POST /api/v1/unified/traveller/tour/start - starting 4X tour: {}", request);

        try {
            UnifiedCharacterSummary summary = tourOfDutyService.startTourIn4X(request);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Failed to start 4X tour: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resolve a tour immediately using Traveller rules.
     * <p>
     * The character serves the career term right away, gaining skills
     * and wealth immediately.
     * </p>
     *
     * @param request the tour request with IMMEDIATE resolve mode
     * @return success response
     */
    @PostMapping("/traveller/tour/resolve")
    public ResponseEntity<Map<String, Object>> resolveTourImmediately(
            @RequestBody TourOfDutyRequest request) {
        log.debug("POST /api/v1/unified/traveller/tour/resolve - resolving tour immediately: {}", request);

        try {
            var tour = tourOfDutyService.resolveTourImmediately(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "career", tour.getCareer().getDisplayName(),
                    "status", tourOfDutyService.getCharacterStatus(request.getCharacterId()).getDisplayName()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Failed to resolve tour: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get the current tour status of a character.
     *
     * @param characterId the character's ID
     * @return map with character status and remaining tours
     */
    @GetMapping("/traveller/tour/status/{characterId}")
    public ResponseEntity<Map<String, Object>> getTourStatus(
            @PathVariable int characterId) {
        log.debug("GET /api/v1/unified/traveller/tour/status/{} - getting tour status", characterId);

        CharacterStatus status = tourOfDutyService.getCharacterStatus(characterId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "characterId", characterId,
                "status", status.getDisplayName(),
                "description", status.getDescription(),
                "availableForLogin", status.isAvailableForLogin(),
                "remainingTours", tourOfDutyService.getRemainingTours(characterId)
        ));
    }

    /**
     * Complete a tour for a character.
     * <p>
     * Called when 4X tour turns are exhausted. Reverts character status
     * from ON_DUTY to TRAINING.
     * </p>
     *
     * @param characterId the character's ID
     * @return success response
     */
    @PostMapping("/traveller/tour/complete/{characterId}")
    public ResponseEntity<Map<String, Object>> completeTour(
            @PathVariable int characterId) {
        log.debug("POST /api/v1/unified/traveller/tour/complete/{} - completing tour", characterId);

        try {
            tourOfDutyService.completeTour(characterId);
            CharacterStatus status = tourOfDutyService.getCharacterStatus(characterId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", status != null ? status.getDisplayName() : "Unknown"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to complete tour: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * List all characters currently on duty.
     *
     * @return list of characters with ON_DUTY status
     */
    @GetMapping("/traveller/tour/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveTours() {
        log.debug("GET /api/v1/unified/traveller/tour/active - listing active tours");

        var chars = tourOfDutyService.getActiveTours();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (var c : chars) {
            result.add(Map.of(
                    "characterId", c.getId(),
                    "name", c.getName(),
                    "status", c.getStatus().getDisplayName()
            ));
        }
        return ResponseEntity.ok(result);
    }

    // ============================================================
    // Tour Management Endpoints
    // ============================================================

    /**
     * Get the active navy tour for a character.
     *
     * @param characterId the character's ID
     * @return the active tour info or empty if none
     */
    @GetMapping("/tours/navy/{characterId}")
    public ResponseEntity<Map<String, Object>> getNavyTour(@PathVariable Long characterId) {
        log.debug("GET /api/v1/unified/tours/navy/{} - getting navy tour", characterId);
        CharacterStatus status = tourOfDutyService.getCharacterStatus(characterId.intValue());
        return ResponseEntity.ok(Map.of(
                "characterId", characterId,
                "status", status != null ? status.getDisplayName() : "Unknown"
        ));
    }

    /**
     * Get the active army assignment for a character.
     *
     * @param characterId the character's ID
     * @return the active assignment info or empty if none
     */
    @GetMapping("/tours/army/{characterId}")
    public ResponseEntity<Map<String, Object>> getArmyTour(@PathVariable Long characterId) {
        log.debug("GET /api/v1/unified/tours/army/{} - getting army assignment", characterId);
        CharacterStatus status = tourOfDutyService.getCharacterStatus(characterId.intValue());
        return ResponseEntity.ok(Map.of(
                "characterId", characterId,
                "status", status != null ? status.getDisplayName() : "Unknown"
        ));
    }

    /**
     * List all available military careers for tour assignment.
     * <p>
     * Returns careers grouped by type: navy (Gunner, Pilot) and army (Soldier).
     * </p>
     *
     * @return map of career types to careers
     */
    @GetMapping("/traveller/military/tour/careers")
    public ResponseEntity<Map<String, Object>> getTourCareers() {
        log.debug("GET /api/v1/unified/traveller/military/tour/careers - listing tour careers");

        List<Career> allCareers = integrationService.getMilitaryCareers();
        List<Career> navyCareers = allCareers.stream()
                .filter(c -> c == Career.GUNNER || c == Career.PILOT)
                .collect(java.util.stream.Collectors.toList());
        List<Career> armyCareers = allCareers.stream()
                .filter(c -> c == Career.SOLDIER)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "navy", navyCareers,
                "army", armyCareers
        ));
    }

    // ============================================================
    // Adventure Endpoints
    // ============================================================

    /**
     * Start an adventure for a completed Traveller character.
     * <p>
     * Creates a single-ship fleet and positions the character in the 4X universe.
     * </p>
     *
     * @param characterId the character's ID
     * @return adventure state
     */
    @PostMapping("/traveller/adventure/start/{characterId}")
    public ResponseEntity<Map<String, Object>> startAdventure(@PathVariable int characterId) {
        log.debug("POST /api/v1/unified/traveller/adventure/start/{} - starting adventure", characterId);

        try {
            var adventure = adventureService.startAdventure(characterId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "fleetId", adventure.getFleetId(),
                    "system", adventure.getCurrentSystemName(),
                    "credits", adventure.getCredits()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Failed to start adventure: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get the current adventure state for a character.
     *
     * @param characterId the character's ID
     * @return adventure state
     */
    @GetMapping("/traveller/adventure/state/{characterId}")
    public ResponseEntity<Map<String, Object>> getAdventureState(@PathVariable int characterId) {
        log.debug("GET /api/v1/unified/traveller/adventure/state/{} - getting adventure state", characterId);

        try {
            var state = adventureService.getAdventureState(characterId);
            return ResponseEntity.ok(Map.of(
                    "characterId", state.getCharacterId(),
                    "characterName", state.getCharacterName(),
                    "system", state.getCurrentSystemName(),
                    "planet", state.getCurrentPlanetName(),
                    "ship", state.getShipName(),
                    "shipType", state.getShipType(),
                    "credits", state.getCredits(),
                    "passenger", state.isPassenger(),
                    "nearbyEvents", state.getNearbyEventCount(),
                    "experiences", state.getRecentExperienceCount()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("No adventure state: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Travel to a destination system.
     *
     * @param characterId the character's ID
     * @param body        containing destinationSystemId
     * @return adventure state after travel
     */
    @PostMapping("/traveller/adventure/travel/{characterId}")
    public ResponseEntity<Map<String, Object>> travelToSystem(
            @PathVariable int characterId,
            @RequestBody Map<String, Integer> body) {
        log.debug("POST /api/v1/unified/traveller/adventure/travel/{} - traveling", characterId);

        int destId = body.getOrDefault("destinationSystemId", 0);
        if (destId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid destination system ID"));
        }

        try {
            var state = adventureService.travelToSystem(characterId, destId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "system", state.getCurrentSystemName(),
                    "credits", state.getCredits()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Travel failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Purchase a new ship for the character.
     *
     * @param characterId the character's ID
     * @param body        containing shipType
     * @return purchase result
     */
    @PostMapping("/traveller/adventure/purchase-ship/{characterId}")
    public ResponseEntity<Map<String, Object>> purchaseShip(
            @PathVariable int characterId,
            @RequestBody Map<String, String> body) {
        log.debug("POST /api/v1/unified/traveller/adventure/purchase-ship/{} - buying ship", characterId);

        String shipTypeStr = body.getOrDefault("shipType", "");
        if (shipTypeStr.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ship type required"));
        }

        try {
            com.yisy.modules.doramcu.hex.ShipType shipType =
                    com.yisy.modules.doramcu.hex.ShipType.valueOf(shipTypeStr.toUpperCase());
            var ship = adventureService.purchaseShip(characterId, shipType);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "shipId", ship.getId(),
                    "shipName", ship.getName(),
                    "shipType", ship.getShipType().name()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Purchase failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Book passage on a ship traveling to a destination.
     *
     * @param characterId the character's ID
     * @param body        containing destinationSystemId
     * @return booking result
     */
    @PostMapping("/traveller/adventure/book-passage/{characterId}")
    public ResponseEntity<Map<String, Object>> bookPassage(
            @PathVariable int characterId,
            @RequestBody Map<String, Integer> body) {
        log.debug("POST /api/v1/unified/traveller/adventure/book-passage/{} - booking passage", characterId);

        int destId = body.getOrDefault("destinationSystemId", 0);
        if (destId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid destination system ID"));
        }

        try {
            boolean booked = adventureService.bookPassage(characterId, destId);
            return ResponseEntity.ok(Map.of(
                    "success", booked,
                    "booked", booked
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Booking failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List nearby 4X events the character can witness.
     *
     * @param characterId the character's ID
     * @return list of nearby events
     */
    @GetMapping("/traveller/adventure/events/{characterId}")
    public ResponseEntity<List<Map<String, Object>>> getNearbyEvents(@PathVariable int characterId) {
        log.debug("GET /api/v1/unified/traveller/adventure/events/{} - listing nearby events", characterId);

        try {
            var events = adventureService.getNearbyEvents(characterId);
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (var event : events) {
                result.add(Map.of(
                        "eventId", event.getEventId(),
                        "type", event.getEventType().name(),
                        "system", event.getSystemName(),
                        "description", event.getDescription(),
                        "distance", event.getDistanceHex()
                ));
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("No events: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Witness a specific event and record it as an experience.
     *
     * @param characterId the character's ID
     * @param body        containing eventId
     * @return witness result
     */
    @PostMapping("/traveller/adventure/witness/{characterId}")
    public ResponseEntity<Map<String, Object>> witnessEvent(
            @PathVariable int characterId,
            @RequestBody Map<String, Integer> body) {
        log.debug("POST /api/v1/unified/traveller/adventure/witness/{} - witnessing event", characterId);

        int eventId = body.getOrDefault("eventId", 0);
        if (eventId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid event ID"));
        }

        try {
            // Find the event from nearby events
            var events = adventureService.getNearbyEvents(characterId);
            for (var event : events) {
                if (event.getEventId() == eventId) {
                    var exp = adventureService.witnessEvent(characterId, event);
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "type", exp.getType().name(),
                            "title", exp.getTitle()
                    ));
                }
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Event not found: " + eventId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Witness failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List reachable systems from the character's current position.
     *
     * @param characterId the character's ID
     * @return list of reachable systems
     */
    @GetMapping("/traveller/adventure/systems/{characterId}")
    public ResponseEntity<List<Map<String, Object>>> getReachableSystems(@PathVariable int characterId) {
        log.debug("GET /api/v1/unified/traveller/adventure/systems/{} - listing reachable systems", characterId);

        try {
            var systems = adventureService.getReachableSystems(characterId);
            return ResponseEntity.ok(systems);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("No systems: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * End the character's adventure.
     *
     * @param characterId the character's ID
     * @return end result
     */
    @PostMapping("/traveller/adventure/end/{characterId}")
    public ResponseEntity<Map<String, Object>> endAdventure(@PathVariable int characterId) {
        log.debug("POST /api/v1/unified/traveller/adventure/end/{} - ending adventure", characterId);

        try {
            adventureService.endAdventure(characterId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", CharacterStatus.COMPLETED.getDisplayName()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("End adventure failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
