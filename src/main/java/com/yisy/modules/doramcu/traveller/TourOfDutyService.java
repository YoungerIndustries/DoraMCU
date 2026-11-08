package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.manager.CharacterLogManager;
import com.yisy.modules.doramcu.traveller.TourOfDutyRequest.ResolveMode;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Core service managing the tour-of-duty decision lifecycle for Traveller characters.
 * <p>
 * When a character needs to serve a career term, the player can choose:
 * </p>
 * <ol>
 *   <li><b>Immediate resolution</b> — Resolve the tour using Traveller rules right away,
 *       gaining skills, wealth, and experience immediately.</li>
 *   <li><b>4X game service</b> — Serve the tour in the 4X game for a specified number of turns.
 *       The character's status changes to ON_DUTY and they cannot log in until the tour completes.</li>
 * </ol>
 * <p>
 * Status transitions:
 * </p>
 * <ul>
 *   <li>TRAINING → ON_DUTY (player chooses 4X tour)</li>
 *   <li>ON_DUTY → TRAINING (tour turns exhausted)</li>
 *   <li>TRAINING → COMPLETED (all career tours resolved)</li>
 * </ul>
 */
@Service
public class TourOfDutyService {

    private static final Logger log = LoggerFactory.getLogger(TourOfDutyService.class);
    private static final int MAX_CAREER_TOURS = 4;

    private final CharacterService characterService;
    private final MilitaryTourService militaryTourService;
    private final GameSessionService gameSessionService;

    public TourOfDutyService(
            CharacterService characterService,
            MilitaryTourService militaryTourService,
            GameSessionService gameSessionService) {
        this.characterService = characterService;
        this.militaryTourService = militaryTourService;
        this.gameSessionService = gameSessionService;
    }

    // ============================================================
    // Tour Options
    // ============================================================

    /**
     * Get the available tour options for a character.
     * <p>
     * Returns the remaining career terms that the character can still serve.
     * A character can serve up to {@link #MAX_CAREER_TOURS} tours total.
     * </p>
     *
     * @param characterId the character ID
     * @return list of available tour options (empty if no tours remain)
     */
    public List<TourOfDutyOption> getTourOptions(int characterId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }

        if (!character.isAlive()) {
            log.info("Character {} is dead — no tour options", character.getName());
            return List.of();
        }

        if (character.getStatus() == CharacterStatus.ON_DUTY) {
            log.info("Character {} is on duty — no new tour options", character.getName());
            return List.of();
        }

        if (character.getStatus() == CharacterStatus.COMPLETED) {
            log.info("Character {} has completed all tours", character.getName());
            return List.of();
        }

        int toursServed = character.getCareerTours().size();
        if (toursServed >= MAX_CAREER_TOURS) {
            log.info("Character {} has served maximum tours ({})", character.getName(), MAX_CAREER_TOURS);
            character.setStatus(CharacterStatus.COMPLETED);
            return List.of();
        }

        List<TourOfDutyOption> options = new ArrayList<>();
        int remainingTours = MAX_CAREER_TOURS - toursServed;

        for (int i = 0; i < remainingTours; i++) {
            int termNumber = toursServed + i + 1;
            // Determine the career for this term
            Career career = determineNextCareer(character, i);
            options.add(new TourOfDutyOption(career, termNumber));
        }

        return options;
    }

    /**
     * Determines the next career for a character based on their existing tours.
     * <p>
     * Tries to vary careers — a character should not serve the same career
     * consecutively unless it's their first tour.
     * </p>
     *
     * @param character the character
     * @param optionIndex the index of the option (0-based among remaining tours)
     * @return the suggested career for this tour
     */
    private Career determineNextCareer(TravellerCharacter character, int optionIndex) {
        List<TravellerCharacter.CareerTour> tours = character.getCareerTours();

        if (tours.isEmpty()) {
            // First tour — pick a default military career
            return Career.SOLDIER;
        }

        // Get the last served career
        Career lastCareer = tours.get(tours.size() - 1).getCareer();

        // Suggest a different career
        Career[] allCareers = Career.values();
        for (Career career : allCareers) {
            if (career != lastCareer && career.isMilitaryCareer()) {
                return career;
            }
        }

        // Fallback to any military career
        return Career.SOLDIER;
    }

    // ============================================================
    // Tour Resolution
    // ============================================================

    /**
     * Resolve a tour immediately using Traveller rules.
     * <p>
     * The character serves the career term right away, gaining skills,
     * wealth, and experience. The character remains in TRAINING status
     * (or transitions to COMPLETED if this was the last tour).
     * </p>
     *
     * @param request the tour resolution request
     * @return the completed career tour
     */
    public TravellerCharacter.CareerTour resolveTourImmediately(TourOfDutyRequest request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid tour request: " + request);
        }

        if (!request.isImmediateMode()) {
            throw new IllegalArgumentException("Resolve mode must be IMMEDIATE for immediate resolution");
        }

        TravellerCharacter character = characterService.getCharacter(request.getCharacterId());
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + request.getCharacterId());
        }

        if (character.getStatus() != CharacterStatus.TRAINING) {
            throw new IllegalStateException("Character must be in TRAINING status to resolve a tour. Current: " + character.getStatus());
        }

        log.info("Character {} resolves {} tour immediately", character.getName(), request.getCareer());

        // Serve the career tour using CharacterService
        TravellerCharacter.CareerTour tour = characterService.serveCareerTour(request.getCharacterId(), request.getCareer());

        // Check if character has remaining tours
        int toursServed = character.getCareerTours().size();
        if (toursServed >= MAX_CAREER_TOURS || tour.isRetired()) {
            character.setStatus(CharacterStatus.COMPLETED);
            log.info("Character {} has completed all career tours", character.getName());
        } else {
            // Character remains in TRAINING for next tour decision
            character.setStatus(CharacterStatus.TRAINING);
        }

        return tour;
    }

    /**
     * Start a tour in the 4X game.
     * <p>
     * The character's status changes to ON_DUTY. They are assigned to
     * a fleet or unit in the 4X game and will record experiences during
     * the tour. The character cannot log in until the tour completes.
     * </p>
     *
     * @param request the tour request with FOUR_X resolve mode
     * @return unified character summary with ON_DUTY status
     */
    public UnifiedCharacterSummary startTourIn4X(TourOfDutyRequest request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid tour request: " + request);
        }

        if (!request.isFourXMode()) {
            throw new IllegalArgumentException("Resolve mode must be FOUR_X for 4X tour");
        }

        if (request.getTourTurns() <= 0) {
            throw new IllegalArgumentException("Tour turns must be positive for 4X mode");
        }

        TravellerCharacter character = characterService.getCharacter(request.getCharacterId());
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + request.getCharacterId());
        }

        if (character.getStatus() != CharacterStatus.TRAINING) {
            throw new IllegalStateException("Character must be in TRAINING status to start a 4X tour. Current: " + character.getStatus());
        }

        log.info("Character {} starts {} tour in 4X game for {} turns",
                character.getName(), request.getCareer(), request.getTourTurns());

        // Set character to ON_DUTY
        character.setStatus(CharacterStatus.ON_DUTY);

        // Set up the tour completion callback
        militaryTourService.setTourCompletionCallback((charId, tourType) -> {
            log.info("Tour completion callback: character {} (tour type: {})", charId, tourType);
            completeTour(charId);
        });

        // Get or create a game session
        Game game = null;
        com.yisy.modules.doramcu.model.GameSession session = gameSessionService.getDefaultSession();
        if (session != null) {
            game = (Game) session.getGame();
        }
        if (game == null) {
            // No active session — create a new game
            game = new Game("4X Campaign");
        }
        CharacterLogManager logManager = new CharacterLogManager(game);
        militaryTourService.setLogManager(logManager);

        // Assign the character to a tour based on career type
        if (request.getCareer().isNavyCareer()) {
            // Navy character — assign to fleet tour
            assignNavyTour(character, game, request.getCareer(), request.getTourTurns());
        } else if (request.getCareer().isArmyCareer()) {
            // Army character — assign to unit tour
            assignArmyTour(character, game, request.getCareer(), request.getTourTurns());
        } else {
            // Non-military careers default to army-style unit assignment
            assignArmyTour(character, game, request.getCareer(), request.getTourTurns());
        }

        // Build summary
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId((long) character.getId());
        summary.setFullName(character.getName());
        summary.setGameRole(request.getCareer().getDisplayName());
        summary.setDescription("Serving " + request.getCareer().getDisplayName() + " tour in 4X game");
        summary.setAlive(character.isAlive());
        summary.setCharacterStatus(CharacterStatus.ON_DUTY);

        return summary;
    }

    /**
     * Assigns a navy character to a fleet tour in the 4X game.
     */
    private void assignNavyTour(TravellerCharacter character, Game game, Career career, int tourTurns) {
        // Find or create a player and fleet for the tour
        Player player = findOrCreatePlayer(game, character);

        // Find an existing fleet or create one
        Fleet fleet = findOrCreateFleet(game, player, character);

        militaryTourService.assignNavyTour(character, fleet, "4X Empire", tourTurns);
        log.info("Character {} assigned to navy fleet tour: {}", character.getName(), fleet.getName());
    }

    /**
     * Assigns an army character to a unit tour in the 4X game.
     */
    private void assignArmyTour(TravellerCharacter character, Game game, Career career, int tourTurns) {
        Player player = findOrCreatePlayer(game, character);

        // Find an army unit
        Empire empire = game.getEmpire(character.getId());
        if (empire != null) {
            ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);
            militaryTourService.assignArmyUnit(character, unit, "4X Empire", "Home World", tourTurns);
            log.info("Character {} assigned to army unit tour", character.getName());
        }
    }

    /**
     * Finds or creates a player in the game for the character.
     */
    private Player findOrCreatePlayer(Game game, TravellerCharacter character) {
        int playerId = character.getId();

        // Check if player already exists
        for (Player p : game.getPlayers()) {
            if (p.getId() == playerId) {
                return p;
            }
        }

        // Create a new player
        game.setupPlayer(playerId, character.getName());
        Player player = game.getPlayer(playerId);
        player.setCommander(character);
        return player;
    }

    /**
     * Finds or creates a fleet for the character.
     */
    private Fleet findOrCreateFleet(Game game, Player player, TravellerCharacter character) {
        Empire empire = game.getEmpire(character.getId());
        if (empire != null && empire.getNavy().getFleets() != null && !empire.getNavy().getFleets().isEmpty()) {
            return empire.getNavy().getFleets().get(0);
        }

        // Create a starting fleet
        Fleet fleet = new Fleet(character.getId(), character.getName() + "'s Fleet");
        game.addFleet(fleet, player);
        if (empire != null) {
            empire.getNavy().addFleet(fleet);
        }
        return fleet;
    }

    // ============================================================
    // Tour Completion
    // ============================================================

    /**
     * Complete a tour for a character.
     * <p>
     * Called when 4X tour turns are exhausted. The character's status
     * reverts to TRAINING, allowing them to log in and continue
     * character creation.
     * </p>
     *
     * @param characterId the character ID
     */
    public void completeTour(int characterId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }

        if (character.getStatus() != CharacterStatus.ON_DUTY) {
            log.warn("Character {} is not ON_DUTY (status: {}) — skipping tour completion",
                    character.getName(), character.getStatus());
            return;
        }

        log.info("Character {} tour completed — status reverted to TRAINING", character.getName());

        // Revert to TRAINING so character can log in
        character.setStatus(CharacterStatus.TRAINING);

        // Log the tour completion experience
        CharacterLogManager logManager = militaryTourService.getLogManager();
        if (logManager != null) {
            CharacterLog charLog = logManager.getLog(character);
            if (charLog != null) {
                charLog.log(ExperienceType.CAREER_COMPLETION,
                        "Tour of Duty Completed",
                        "Character completed their tour of duty in the 4X game and is now available for further character development.")
                        .location("4X Game")
                        .characterAge(character.getAge());
            }
        }

        // Check if character has remaining tours
        int toursServed = character.getCareerTours().size();
        if (toursServed >= MAX_CAREER_TOURS) {
            character.setStatus(CharacterStatus.COMPLETED);
            log.info("Character {} has completed all career tours after 4X duty", character.getName());
        }
    }

    // ============================================================
    // Query Methods
    // ============================================================

    /**
     * Get all characters currently on duty.
     *
     * @return list of characters with ON_DUTY status
     */
    public List<TravellerCharacter> getActiveTours() {
        List<TravellerCharacter> all = characterService.listCharacters();
        List<TravellerCharacter> onDuty = new ArrayList<>();

        for (TravellerCharacter character : all) {
            if (character.getStatus() == CharacterStatus.ON_DUTY) {
                onDuty.add(character);
            }
        }

        return onDuty;
    }

    /**
     * Get the current status of a character.
     *
     * @param characterId the character ID
     * @return the character's status, or null if not found
     */
    public CharacterStatus getCharacterStatus(int characterId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            return null;
        }
        return character.getStatus();
    }

    /**
     * Check if a character is available for login.
     *
     * @param characterId the character ID
     * @return true if the character can be logged into
     */
    public boolean isAvailableForLogin(int characterId) {
        CharacterStatus status = getCharacterStatus(characterId);
        if (status == null) {
            return false;
        }
        return status.isAvailableForLogin();
    }

    /**
     * Get the number of remaining tours for a character.
     *
     * @param characterId the character ID
     * @return number of remaining tours (0 if completed or maxed out)
     */
    public int getRemainingTours(int characterId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            return 0;
        }

        if (!character.isAlive()) {
            return 0;
        }

        int toursServed = character.getCareerTours().size();
        int remaining = MAX_CAREER_TOURS - toursServed;
        return Math.max(0, remaining);
    }
}
