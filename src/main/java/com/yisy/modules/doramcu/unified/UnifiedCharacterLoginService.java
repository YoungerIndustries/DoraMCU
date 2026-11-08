package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.traveller.CharacterStatus;
import com.yisy.modules.doramcu.traveller.TourOfDutyService;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.shared.model.SharedCharacter;
import com.yisy.modules.doramcu.shared.repository.SharedCharacterRepository;
import com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginResponse;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service that provides unified character login across all game systems.
 * <p>
 * When a player logs in, they are presented with a list of their characters
 * from all game systems (Traveller, Captain's Log, DoraMCU 4X, Shared).
 * The player can search by character name or email to find their characters.
 * </p>
 * <p>
 * The login flow is:
 * </p>
 * <ol>
 *   <li>Player provides name or email</li>
 *   <li>System searches all character stores for matching characters</li>
 *   <li>System returns list of characters with game type info</li>
 *   <li>Player selects a character or creates a new one</li>
 * </ol>
 */
@Service
public class UnifiedCharacterLoginService {

    private static final Logger log = LoggerFactory.getLogger(UnifiedCharacterLoginService.class);

    private final CaptainLogCharacterRepository clCharacterRepository;
    private final SharedCharacterRepository sharedCharacterRepository;
    private final PlayerRepository playerRepository;
    private final CharacterService travellerCharacterService;
    private final TourOfDutyService tourOfDutyService;

    public UnifiedCharacterLoginService(
            CaptainLogCharacterRepository clCharacterRepository,
            SharedCharacterRepository sharedCharacterRepository,
            PlayerRepository playerRepository,
            CharacterService travellerCharacterService,
            TourOfDutyService tourOfDutyService) {
        this.clCharacterRepository = clCharacterRepository;
        this.sharedCharacterRepository = sharedCharacterRepository;
        this.playerRepository = playerRepository;
        this.travellerCharacterService = travellerCharacterService;
        this.tourOfDutyService = tourOfDutyService;
    }

    /**
     * Process a unified login request.
     * <p>
     * Searches all character stores for matching characters based on the
     * provided name or email.
     * </p>
     *
     * @param request the login request with name and/or email
     * @return response containing matching characters and available games
     */
    public UnifiedLoginResponse processLogin(UnifiedLoginRequest request) {
        log.info("Processing unified login request: firstName={}, lastName={}, email={}",
                request.getFirstName(), request.getLastName(), request.getEmail());

        UnifiedLoginResponse response = new UnifiedLoginResponse();

        // If no search criteria, return available games and prompt for input
        if (!request.hasSearchCriteria()) {
            response.setNeedsCharacterCreation(true);
            populateAvailableGames(response);
            return response;
        }

        List<UnifiedCharacterSummary> allCharacters = new ArrayList<>();

        // Search by email — find all characters for this player
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Search Captain's Log characters by name (email is stored in Player, not CL character)
            // For now, search all CL characters and filter by name if provided
            List<CaptainLogCharacter> clChars = clCharacterRepository.findAll();
            for (CaptainLogCharacter clChar : clChars) {
                // Check if this character belongs to a player with this email
                // CL characters don't have direct email linkage, so we match by name
                if (request.getFirstName() == null && request.getLastName() == null) {
                    // No name filter — add all CL characters for this email
                    allCharacters.add(toUnifiedSummary(clChar, null));
                } else {
                    // Name filter — check if name matches
                    String clName = clChar.getName();
                    if (nameMatches(clName, request.getFirstName(), request.getLastName())) {
                        allCharacters.add(toUnifiedSummary(clChar, null));
                    }
                }
            }

            // Search Shared characters
            List<SharedCharacter> sharedChars = sharedCharacterRepository.findAll();
            for (SharedCharacter sharedChar : sharedChars) {
                if (request.getFirstName() == null && request.getLastName() == null) {
                    allCharacters.add(toUnifiedSummary(sharedChar));
                } else {
                    String sharedName = sharedChar.getName();
                    if (nameMatches(sharedName, request.getFirstName(), request.getLastName())) {
                        allCharacters.add(toUnifiedSummary(sharedChar));
                    }
                }
            }

            // Search Player (4X) by email
            Optional<Player> playerOpt = playerRepository.findByEmail(request.getEmail());
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                UnifiedCharacterSummary playerSummary = new UnifiedCharacterSummary();
                playerSummary.setId(player.getId());
                playerSummary.setFullName(player.getEmpireName());
                playerSummary.setGameType(GameType.DORAMCU_4X);
                playerSummary.setGameRole("Empire Ruler");
                playerSummary.setDescription("Empire: " + player.getEmpireName() +
                        " (" + player.getGovernmentType() + ", " + player.getEthos() + ")");
                playerSummary.setAlive(true);
                allCharacters.add(playerSummary);
            }

            // Search Traveller characters (in-memory)
            List<TravellerCharacter> travellerChars = travellerCharacterService.listCharacters();
            for (TravellerCharacter travellerChar : travellerChars) {
                if (request.getFirstName() == null && request.getLastName() == null) {
                    allCharacters.add(toUnifiedSummary(travellerChar));
                } else {
                    String travellerName = travellerChar.getName();
                    if (nameMatches(travellerName, request.getFirstName(), request.getLastName())) {
                        allCharacters.add(toUnifiedSummary(travellerChar));
                    }
                }
            }
        }
        // Search by name only (no email)
        else if (request.getFirstName() != null || request.getLastName() != null) {
            String searchName = request.getFullName();

            // Search Captain's Log
            List<CaptainLogCharacter> clChars = clCharacterRepository.findByNameContainingIgnoreCase(
                    request.getFirstName() != null ? request.getFirstName() :
                    (request.getLastName() != null ? request.getLastName() : ""));
            for (CaptainLogCharacter clChar : clChars) {
                if (nameMatches(clChar.getName(), request.getFirstName(), request.getLastName())) {
                    allCharacters.add(toUnifiedSummary(clChar, null));
                }
            }

            // Search Shared characters
            List<SharedCharacter> sharedChars = sharedCharacterRepository.findByNameContainingIgnoreCase(
                    request.getFirstName() != null ? request.getFirstName() :
                    (request.getLastName() != null ? request.getLastName() : ""));
            for (SharedCharacter sharedChar : sharedChars) {
                if (nameMatches(sharedChar.getName(), request.getFirstName(), request.getLastName())) {
                    allCharacters.add(toUnifiedSummary(sharedChar));
                }
            }

            // Search Traveller characters
            List<TravellerCharacter> travellerChars = travellerCharacterService.listCharacters();
            for (TravellerCharacter travellerChar : travellerChars) {
                if (nameMatches(travellerChar.getName(), request.getFirstName(), request.getLastName())) {
                    allCharacters.add(toUnifiedSummary(travellerChar));
                }
            }
        }

        response.setCharacters(allCharacters);
        response.setPlayerEmail(request.getEmail());

        if (allCharacters.isEmpty()) {
            response.setNeedsCharacterCreation(true);
        } else if (allCharacters.size() > 1) {
            response.setNeedsCharacterSelection(true);
        }

        populateAvailableGames(response);
        return response;
    }

    /**
     * Get all characters for a specific player email.
     */
    public List<UnifiedCharacterSummary> getCharactersByEmail(String email) {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, email);
        return processLogin(request).getCharacters();
    }

    /**
     * Get all characters matching a name.
     */
    public List<UnifiedCharacterSummary> getCharactersByName(String firstName, String lastName) {
        UnifiedLoginRequest request = new UnifiedLoginRequest(firstName, lastName, null);
        return processLogin(request).getCharacters();
    }

    /**
     * Get available games for character creation.
     */
    public List<GameType> getAvailableGames() {
        return List.of(GameType.values());
    }

    /**
     * Get Traveller character types for the character creation screen.
     */
    public List<TravellerCharacterType> getTravellerCharacterTypes() {
        return List.of(TravellerCharacterType.values());
    }

    // ---- Helper Methods ----

    private boolean nameMatches(String characterName, String firstName, String lastName) {
        if (characterName == null || characterName.isBlank()) {
            return false;
        }
        String lowerName = characterName.toLowerCase();
        if (firstName != null && !firstName.isBlank()) {
            if (!lowerName.contains(firstName.toLowerCase())) {
                return false;
            }
        }
        if (lastName != null && !lastName.isBlank()) {
            if (!lowerName.contains(lastName.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private UnifiedCharacterSummary toUnifiedSummary(CaptainLogCharacter clChar, String email) {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId(clChar.getId());
        String[] parts = parseName(clChar.getName());
        summary.setFirstName(parts[0]);
        summary.setLastName(parts[1]);
        summary.setFullName(clChar.getName());
        summary.setGameType(GameType.CAPTAINS_LOG);
        summary.setGameRole(clChar.getRank().name() + " — " + clChar.getAssignment().name());
        summary.setDescription(String.format("Rank: %s, Assignment: %s, Environment: %s",
                clChar.getRank(), clChar.getAssignment(), clChar.getEnvironment()));
        summary.setAlive(true);
        return summary;
    }

    private UnifiedCharacterSummary toUnifiedSummary(SharedCharacter sharedChar) {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId(sharedChar.getId());
        String[] parts = parseName(sharedChar.getName());
        summary.setFirstName(parts[0]);
        summary.setLastName(parts[1]);
        summary.setFullName(sharedChar.getName());
        summary.setGameType(sharedChar.getOriginSystem());
        summary.setGameRole(sharedChar.getOriginSystem().name());
        summary.setDescription("Shared character — " + sharedChar.getOriginSystem().getDisplayName());
        summary.setAlive(true);
        return summary;
    }

    private UnifiedCharacterSummary toUnifiedSummary(TravellerCharacter travellerChar) {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId((long) travellerChar.getId());
        String[] parts = parseName(travellerChar.getName());
        summary.setFirstName(parts[0]);
        summary.setLastName(parts[1]);
        summary.setFullName(travellerChar.getName());
        summary.setGameType(GameType.TRAVELLER);
        String role = travellerChar.getCareerTours().isEmpty() ? "Civilian" :
                travellerChar.getCareerTours().get(travellerChar.getCareerTours().size() - 1).getRank();
        summary.setGameRole(role);
        summary.setDescription(String.format("Abilities: DEX %d, ST %d, END %d, INT %d, EDU %d, SOC %d, APP %d",
                travellerChar.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.DEX),
                travellerChar.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.ST),
                travellerChar.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.END),
                travellerChar.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.INT),
                travellerChar.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.EDU),
                travellerChar.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.SOC),
                travellerChar.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.APP)));
        summary.setAlive(travellerChar.isAlive());
        summary.setCharacterStatus(travellerChar.getStatus());

        // Add status description for ON_DUTY characters
        if (travellerChar.getStatus() == CharacterStatus.ON_DUTY) {
            summary.setDescription(summary.getDescription() + " [ON DUTY — not available for login]");
        }

        return summary;
    }

    private String[] parseName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{"", ""};
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return new String[]{parts[0], parts[parts.length - 1]};
    }

    private void populateAvailableGames(UnifiedLoginResponse response) {
        // Traveller game info
        UnifiedLoginResponse.GameTypeInfo travellerGame = new UnifiedLoginResponse.GameTypeInfo(
                GameType.TRAVELLER, GameType.TRAVELLER.getDisplayName(), GameType.TRAVELLER.getDescription());
        for (TravellerCharacterType type : TravellerCharacterType.values()) {
            travellerGame.addCharacterType(type.getDisplayName());
        }
        response.addAvailableGame(travellerGame);

        // Captain's Log game info
        UnifiedLoginResponse.GameTypeInfo clGame = new UnifiedLoginResponse.GameTypeInfo(
                GameType.CAPTAINS_LOG, GameType.CAPTAINS_LOG.getDisplayName(), GameType.CAPTAINS_LOG.getDescription());
        clGame.addCharacterType("Starfleet Officer");
        clGame.addCharacterType("Civilian");
        response.addAvailableGame(clGame);

        // DoraMCU 4X game info
        UnifiedLoginResponse.GameTypeInfo doraGame = new UnifiedLoginResponse.GameTypeInfo(
                GameType.DORAMCU_4X, GameType.DORAMCU_4X.getDisplayName(), GameType.DORAMCU_4X.getDescription());
        doraGame.addCharacterType("Empire Leader");
        response.addAvailableGame(doraGame);
    }
}
