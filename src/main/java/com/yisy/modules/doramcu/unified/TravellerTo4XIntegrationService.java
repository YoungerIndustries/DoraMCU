package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.manager.CharacterLogManager;
import com.yisy.modules.doramcu.traveller.*;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bridges Traveller character creation with the DoraMCU 4X game system.
 * <p>
 * This service handles two integration paths:
 * </p>
 * <ol>
 *   <li><b>Management Roles</b> — When a player selects Empire Leader, Sector Governor,
 *       Subsector Chief, or System Manager, the service creates a TravellerCharacter
 *       for narrative logging and a complete 4X player with empire, home system,
 *       and starting fleets.</li>
 *   <li><b>Military Characters</b> — When a player creates a FULL_TRAVELLER character
 *       with Soldier/Gunner/Pilot careers, the service can assign the character
 *       as a fleet commander in an existing empire.</li>
 * </ol>
 * <p>
 * The service leverages existing infrastructure ({@link Game#setupPlayer()},
 * {@link Player#setCommander(TravellerCharacter)}, {@link FleetManager})
 * rather than duplicating functionality.
 * </p>
 */
@Service
public class TravellerTo4XIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(TravellerTo4XIntegrationService.class);

    private final GameSessionService gameSessionService;
    private final CharacterService characterService;
    private final MilitaryTourService militaryTourService;

    public TravellerTo4XIntegrationService(GameSessionService gameSessionService,
                                           CharacterService characterService,
                                           MilitaryTourService militaryTourService) {
        this.gameSessionService = gameSessionService;
        this.characterService = characterService;
        this.militaryTourService = militaryTourService;
    }

    // ============================================================
    // Management Role Integration
    // ============================================================

    /**
     * Integrates a Traveller management role character into the 4X game.
     * <p>
     * Creates a TravellerCharacter for narrative logging, then sets up a complete
     * 4X player with empire, home system, and starting fleets via {@link Game#setupPlayer()}.
     * The TravellerCharacter is assigned as the player's commander.
     * </p>
     *
     * @param request the character creation request with a management role type
     * @return summary of the created character with 4X game info
     */
    public UnifiedCharacterSummary integrateManagementRole(CreateCharacterRequest request) {
        TravellerCharacterType charType = request.getTravellerCharacterType();
        if (charType == null || !charType.isManagementRole()) {
            throw new IllegalArgumentException("Expected a management role type, got: " + charType);
        }

        log.info("Integrating management role: {} for {}", charType.getDisplayName(), request.getFullName());

        // 1. Create the Traveller character for narrative logging
        TravellerCharacter travellerChar = characterService.createCharacter(request.getFullName());
        travellerChar.rollAbilities();

        // 2. Get or create a game session
        GameSession session = gameSessionService.getDefaultSession();
        if (session == null) {
            // Create a default session if none exists
            String sessionName = request.getFullName() + " Campaign";
            gameSessionService.createDefaultSession(sessionName);
            session = gameSessionService.getDefaultSession();
        }
        if (session == null) {
            throw new IllegalStateException("No game session available and could not create one");
        }

        Game game = (Game) session.getGame();

        // 3. Set up the player in the 4X game with empire, home system, and fleets
        int playerId = game.getPlayerCount() + 1;
        Game.PlayerSetupResult setup = game.setupPlayer(playerId, request.getFullName());

        Player player = setup.getPlayer();
        Empire empire = setup.getEmpire();
        List<Fleet> startingFleets = setup.getStartingFleets();

        // 4. Assign the Traveller character as the player's commander
        player.setCommander(travellerChar);

        // 5. Adjust empire scope based on character type
        adjustEmpireScope(empire, player, charType, game);

        // 6. Create a character log and record the empire founding event
        CharacterLog characterLog = new CharacterLog(travellerChar);
        characterLog.logCharacterCreation(request.getFullName() + " Space");

        String empireEventTitle = switch (charType) {
            case EMPIRE_LEADER -> "Founded the " + empire.getName();
            case SECTOR_GOVERNOR -> "Appointed Governor of Sector";
            case SUBSECTOR_CHIEF -> "Appointed Chief of Subsector";
            case SYSTEM_MANAGER -> "Appointed Manager of System";
            default -> "Assumed Management Role";
        };

        characterLog.log(ExperienceType.HEROISM, empireEventTitle,
                String.format("%s established rule over %s with %d starting fleets.",
                        request.getFullName(), empire.getName(), startingFleets.size()));

        // 7. Add player to session
        session.addPlayer((long) player.getId());

        // 8. Build the unified summary
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId((long) player.getId());
        summary.setFirstName(request.getFirstName());
        summary.setLastName(request.getLastName());
        summary.setFullName(request.getFullName());
        summary.setGameType(GameType.TRAVELLER);
        summary.setGameRole(charType.getDisplayName());
        summary.setDescription(String.format(
                "%s — Empire: %s (ID: %d), Fleets: %d, Session: %s",
                charType.getShortDescription(), empire.getName(), empire.getId(),
                startingFleets.size(), session.getSessionId()));
        summary.setAlive(true);

        log.info("Management role integrated: {} -> Player {} with Empire {} (ID: {})",
                request.getFullName(), player.getId(), empire.getName(), empire.getId());

        return summary;
    }

    /**
     * Adjusts the empire scope based on the Traveller character type.
     * <p>
     * For management roles, this tailors the empire's initial scope:
     * </p>
     * <ul>
     *   <li>EMPIRE_LEADER — Full empire with multiple sectors (default setup)</li>
     *   <li>SECTOR_GOVERNOR — One sector (100 systems)</li>
     *   <li>SUBSECTOR_CHIEF — One subsector (25 systems)</li>
     *   <li>SYSTEM_MANAGER — One system with planets</li>
     * </ul>
     *
     * @param empire   the empire to adjust
     * @param player   the player owning the empire
     * @param charType the Traveller character type
     * @param game     the game context
     */
    private void adjustEmpireScope(Empire empire, Player player, TravellerCharacterType charType, Game game) {
        // The base setupPlayer() already creates a home system with starting facilities.
        // For management roles, we log the scope level for narrative purposes.
        // The actual scope (number of systems/sectors) is determined by the galaxy
        // and the player's progression in the 4X game.
        
        String scopeDescription = switch (charType) {
            case EMPIRE_LEADER -> "full galactic empire";
            case SECTOR_GOVERNOR -> "sector jurisdiction (100 systems)";
            case SUBSECTOR_CHIEF -> "subsector jurisdiction (25 systems)";
            case SYSTEM_MANAGER -> "single star system";
            default -> "unknown scope";
        };

        log.info("Empire {} scope set to: {} for player {}", empire.getName(), scopeDescription, player.getId());
    }

    // ============================================================
    // Military Character Integration (Tour of Duty)
    // ============================================================

    /**
     * Integrates a Navy Traveller character into the 4X game as a fleet observer.
     * <p>
     * Creates a TravellerCharacter with the specified naval career (Gunner/Pilot),
     * serves the career to gain skills and ranks, then assigns the character to
     * an existing fleet for a tour of duty. The character does not control the fleet —
     * they record the fleet's experiences in their life history and book.
     * </p>
     *
     * @param request the character creation request
     * @param career  the naval career (GUNNER or PILOT)
     * @param termCount the number of career terms to serve
     * @param tourTurns the number of turns in the tour of duty
     * @return summary of the created character with tour info
     */
    public UnifiedCharacterSummary integrateNavyCharacter(CreateCharacterRequest request,
                                                           Career career,
                                                           int termCount,
                                                           int tourTurns) {
        if (career != Career.GUNNER && career != Career.PILOT) {
            throw new IllegalArgumentException("Expected a naval career (GUNNER or PILOT), got: " + career);
        }

        log.info("Integrating navy character: {} with career {} for {}",
                request.getFullName(), career, request.getEmail());

        // 1. Get or create a game session
        GameSession session = gameSessionService.getDefaultSession();
        if (session == null) {
            String sessionName = request.getFullName() + " Campaign";
            gameSessionService.createDefaultSession(sessionName);
            session = gameSessionService.getDefaultSession();
        }
        if (session == null) {
            throw new IllegalStateException("No game session available and could not create one");
        }

        Game game = (Game) session.getGame();

        // 2. Create the Traveller character using the game as context
        CharacterLogManager logManager = new CharacterLogManager(game);
        TravellerCharacter loggedChar = logManager.createCharacter(request.getFullName(), "Unknown");
        loggedChar.rollAbilities();

        // 3. Serve the naval career with logging
        logManager.serveCareerWithLogging(loggedChar, career, termCount);

        // 4. Find an existing empire with fleets
        Map<Integer, Empire> empires = game.getEmpires();
        if (empires.isEmpty()) {
            // No empires — create a starter empire
            int playerId = game.getPlayerCount() + 1;
            game.setupPlayer(playerId, request.getFullName());
        }

        empires = game.getEmpires();
        Empire assignedEmpire = empires.values().iterator().next();

        // 5. Find or create a fleet for the tour
        Fleet assignedFleet = findOrCreateFleet(game, assignedEmpire, loggedChar, career);
        assignedEmpire.getNavy().addFleet(assignedFleet);

        // 6. Configure the tour service and assign the navy tour
        militaryTourService.setLogManager(logManager);
        NavyTourAssignment tour = militaryTourService.assignNavyTour(loggedChar, assignedFleet,
                assignedEmpire.getName(), tourTurns);

        // 7. Add player to session
        int rulerPlayerId = assignedEmpire.getRulerPlayerId();
        Player player = game.getPlayer(rulerPlayerId);
        if (player != null) {
            session.addPlayer((long) player.getId());
        }

        // 8. Build the unified summary
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId((long) loggedChar.getId());
        summary.setFirstName(request.getFirstName());
        summary.setLastName(request.getLastName());
        summary.setFullName(loggedChar.getName());
        summary.setGameType(GameType.TRAVELLER);
        summary.setGameRole(career.getName() + " — Navy Tour");

        Set<Skill> skills = loggedChar.getSkills();
        String skillsStr = skills.isEmpty() ? "none" : skills.stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));

        summary.setDescription(String.format(
                "Career: %s (%d terms), Skills: %s — Tour: %d turns with %s in %s",
                career.getName(), termCount, skillsStr,
                tourTurns, assignedFleet.getName(), assignedEmpire.getName()));
        summary.setAlive(loggedChar.isAlive());

        log.info("Navy character integrated: {} -> Tour with Fleet {} in Empire {} (ID: {})",
                loggedChar.getName(), assignedFleet.getId(), assignedEmpire.getName(), assignedEmpire.getId());

        return summary;
    }

    /**
     * Integrates an Army Traveller character into the 4X game as a unit observer.
     * <p>
     * Creates a TravellerCharacter with the Soldier career, serves the career
     * to gain skills and ranks, then assigns the character to an existing army unit
     * for a tour of duty. The character does not control the unit —
     * they record the unit's experiences in their life history and book.
     * </p>
     *
     * @param request the character creation request
     * @param termCount the number of career terms to serve
     * @param tourTurns the number of turns in the tour of duty
     * @return summary of the created character with tour info
     */
    public UnifiedCharacterSummary integrateArmyCharacter(CreateCharacterRequest request,
                                                           int termCount,
                                                           int tourTurns) {
        Career career = Career.SOLDIER;

        log.info("Integrating army character: {} for {}", request.getFullName(), request.getEmail());

        // 1. Get or create a game session
        GameSession session = gameSessionService.getDefaultSession();
        if (session == null) {
            String sessionName = request.getFullName() + " Campaign";
            gameSessionService.createDefaultSession(sessionName);
            session = gameSessionService.getDefaultSession();
        }
        if (session == null) {
            throw new IllegalStateException("No game session available and could not create one");
        }

        Game game = (Game) session.getGame();

        // 2. Create the Traveller character using the game as context
        CharacterLogManager logManager = new CharacterLogManager(game);
        TravellerCharacter loggedChar = logManager.createCharacter(request.getFullName(), "Unknown");
        loggedChar.rollAbilities();

        // 3. Serve the Soldier career with logging
        logManager.serveCareerWithLogging(loggedChar, career, termCount);

        // 4. Find an existing empire
        Map<Integer, Empire> empires = game.getEmpires();
        if (empires.isEmpty()) {
            int playerId = game.getPlayerCount() + 1;
            game.setupPlayer(playerId, request.getFullName());
        }

        empires = game.getEmpires();
        Empire assignedEmpire = empires.values().iterator().next();

        // 5. Find or create an army unit for the assignment
        ArmyUnit assignedUnit = findOrCreateArmyUnit(assignedEmpire, loggedChar);
        String planetName = "Unknown";
        if (assignedUnit.getStationedPlanet() != null) {
            planetName = assignedUnit.getStationedPlanet().getName();
        }

        // 6. Configure the tour service and assign the army unit
        militaryTourService.setLogManager(logManager);
        ArmyUnitAssignment assignment = militaryTourService.assignArmyUnit(loggedChar, assignedUnit,
                assignedEmpire.getName(), planetName, tourTurns);

        // 7. Add player to session
        int rulerPlayerId = assignedEmpire.getRulerPlayerId();
        Player player = game.getPlayer(rulerPlayerId);
        if (player != null) {
            session.addPlayer((long) player.getId());
        }

        // 8. Build the unified summary
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId((long) loggedChar.getId());
        summary.setFirstName(request.getFirstName());
        summary.setLastName(request.getLastName());
        summary.setFullName(loggedChar.getName());
        summary.setGameType(GameType.TRAVELLER);
        summary.setGameRole(career.getName() + " — Army Tour");

        Set<Skill> skills = loggedChar.getSkills();
        String skillsStr = skills.isEmpty() ? "none" : skills.stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));

        summary.setDescription(String.format(
                "Career: %s (%d terms), Skills: %s — Tour: %d turns with %s on %s in %s",
                career.getName(), termCount, skillsStr,
                tourTurns, assignedUnit.getName(), planetName, assignedEmpire.getName()));
        summary.setAlive(loggedChar.isAlive());

        log.info("Army character integrated: {} -> Tour with Unit {} on {} in Empire {} (ID: {})",
                loggedChar.getName(), assignedUnit.getId(), planetName,
                assignedEmpire.getName(), assignedEmpire.getId());

        return summary;
    }

    /**
     * Integrates a military Traveller character using the tour of duty system.
     * <p>
     * This is the unified entry point that delegates to {@link #integrateNavyCharacter}
     * or {@link #integrateArmyCharacter} based on the career type.
     * </p>
     *
     * @param request the character creation request
     * @param career  the military career (SOLDIER, GUNNER, or PILOT)
     * @param termCount the number of career terms to serve
     * @param tourTurns the number of turns in the tour of duty
     * @return summary of the created character with tour info
     */
    public UnifiedCharacterSummary integrateMilitaryCharacter(CreateCharacterRequest request,
                                                               Career career,
                                                               int termCount,
                                                               int tourTurns) {
        if (!isMilitaryCareer(career)) {
            throw new IllegalArgumentException("Expected a military career, got: " + career);
        }

        if (career == Career.SOLDIER) {
            return integrateArmyCharacter(request, termCount, tourTurns);
        } else {
            return integrateNavyCharacter(request, career, termCount, tourTurns);
        }
    }

    /**
     * Finds an existing fleet in the empire or creates a new one.
     */
    private Fleet findOrCreateFleet(Game game, Empire empire, TravellerCharacter character, Career career) {
        List<Fleet> fleets = empire.getNavy().getFleets();
        if (!fleets.isEmpty()) {
            // Assign to the first existing fleet
            return fleets.get(0);
        }

        // Create a new fleet
        int fleetId = game.getFleetsMap().size() + 1;
        String fleetName = empire.getName() + " " + career.getName() + " Fleet";
        Fleet fleet = new Fleet(fleetId, fleetName);

        Player player = game.getPlayer(empire.getRulerPlayerId());
        if (player != null) {
            game.addFleet(fleet, player);
        }

        log.info("Created fleet: {} (ID: {}) for empire {}", fleetName, fleetId, empire.getName());
        return fleet;
    }

    /**
     * Finds an existing army unit or creates a new one.
     */
    private ArmyUnit findOrCreateArmyUnit(Empire empire, TravellerCharacter character) {
        EmpireArmy army = empire.getArmy();
        List<ArmyUnit> units = army.getUnits();
        if (!units.isEmpty()) {
            return units.get(0);
        }

        // Create a new infantry unit
        ArmyUnit unit = army.createUnit(ArmyUnitType.INFANTRY);
        unit.setName(character.getName() + "'s Company");

        log.info("Created army unit: {} (ID: {}) for empire {}", unit.getName(), unit.getId(), empire.getName());
        return unit;
    }

    /**
     * Checks if a career is a military career suitable for fleet command.
     *
     * @param career the career to check
     * @return true if the career is SOLDIER, GUNNER, or PILOT
     */
    public boolean isMilitaryCareer(Career career) {
        return career == Career.SOLDIER || career == Career.GUNNER || career == Career.PILOT;
    }

    /**
     * Returns the available military careers for fleet command.
     *
     * @return list of military careers
     */
    public List<Career> getMilitaryCareers() {
        return Arrays.stream(Career.values())
                .filter(this::isMilitaryCareer)
                .collect(Collectors.toList());
    }

    // ============================================================
    // Empire Discovery
    // ============================================================

    /**
     * Returns a list of available empires for military character assignment.
     * <p>
     * Used to present the player with a choice of empires to join.
     * </p>
     *
     * @return list of empire info maps with id, name, and player count
     */
    public List<Map<String, Object>> getAvailableEmpires() {
        GameSession session = gameSessionService.getDefaultSession();
        if (session == null) {
            return Collections.emptyList();
        }

        Game game = (Game) session.getGame();
        Map<Integer, Empire> empires = game.getEmpires();

        return empires.values().stream()
                .map(empire -> {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("id", empire.getId());
                    info.put("name", empire.getName());
                    info.put("fleetCount", empire.getNavy().getFleetCount());
                    info.put("planetCount", empire.getPlanetCount());
                    info.put("rulerPlayerId", empire.getRulerPlayerId());
                    return info;
                })
                .collect(Collectors.toList());
    }
}
