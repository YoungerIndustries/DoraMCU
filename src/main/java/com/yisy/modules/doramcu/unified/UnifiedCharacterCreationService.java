package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.unified.TravellerTo4XIntegrationService;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service that handles unified character creation across all game systems.
 * <p>
 * The creation flow is:
 * </p>
 * <ol>
 *   <li>Player selects which game they wish to play first</li>
 *   <li>If Traveller is selected, player chooses their character type
 *       (Empire Leader, Sector Governor, Subsector Chief, System Manager, or Full Traveller)</li>
 *   <li>Character is created in the appropriate system</li>
 * </ol>
 */
@Service
public class UnifiedCharacterCreationService {

    private static final Logger log = LoggerFactory.getLogger(UnifiedCharacterCreationService.class);

    private final CharacterService travellerCharacterService;
    private final CaptainLogService captainLogService;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TravellerTo4XIntegrationService integrationService;

    public UnifiedCharacterCreationService(
            CharacterService travellerCharacterService,
            CaptainLogService captainLogService,
            PlayerRepository playerRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TravellerTo4XIntegrationService integrationService) {
        this.travellerCharacterService = travellerCharacterService;
        this.captainLogService = captainLogService;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.integrationService = integrationService;
    }

    /**
     * Create a new character through the unified system.
     * <p>
     * The game type determines which subsystem creates the character.
     * </p>
     *
     * @param request the character creation request
     * @return summary of the created character
     */
    public UnifiedCharacterSummary createCharacter(com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid character creation request: " +
                    "game type, name, and email are required");
        }

        log.info("Creating character: {} for game {} (email: {})",
                request.getFullName(), request.getGameType(), request.getEmail());

        switch (request.getGameType()) {
            case TRAVELLER:
                return createTravellerCharacter(request);
            case CAPTAINS_LOG:
                return createCaptainsLogCharacter(request);
            case DORAMCU_4X:
                return createDoraMCUPlayer(request);
            default:
                throw new IllegalArgumentException("Unknown game type: " + request.getGameType());
        }
    }

    /**
     * Get the available Traveller character types for the selection screen.
     */
    public java.util.List<TravellerCharacterType> getTravellerCharacterTypes() {
        return java.util.List.of(TravellerCharacterType.values());
    }

    /**
     * Get the available games for the initial selection screen.
     */
    public java.util.List<GameType> getAvailableGames() {
        return java.util.List.of(GameType.values());
    }

    // ---- Character Creation Methods ----

    private UnifiedCharacterSummary createTravellerCharacter(com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest request) {
        TravellerCharacterType charType = request.getTravellerCharacterType();

        if (charType == null) {
            // Default to full traveller character if not specified
            charType = TravellerCharacterType.FULL_TRAVELLER;
        }

        if (charType.isManagementRole()) {
            // Management role — delegate to 4X integration service
            // This creates the Traveller character AND sets up the 4X player with empire,
            // home system, and starting fleets
            return integrationService.integrateManagementRole(request);
        } else {
            // Full Traveller character with abilities and careers
            TravellerCharacter character = travellerCharacterService.createCharacter(request.getFullName());
            character.rollAbilities();

            UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
            summary.setId((long) character.getId());
            summary.setFirstName(request.getFirstName());
            summary.setLastName(request.getLastName());
            summary.setFullName(character.getName());
            summary.setGameType(GameType.TRAVELLER);
            summary.setGameRole(TravellerCharacterType.FULL_TRAVELLER.getDisplayName());
            summary.setDescription(String.format(
                    "Abilities: DEX %d, ST %d, END %d, INT %d, EDU %d, SOC %d, APP %d",
                    character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.DEX),
                    character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.ST),
                    character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.END),
                    character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.INT),
                    character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.EDU),
                    character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.SOC),
                    character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.APP)));
            summary.setAlive(true);
            return summary;
        }
    }

    private UnifiedCharacterSummary createCaptainsLogCharacter(com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest request) {
        // Create the Captain's Log character
        com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest clRequest =
                new com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest();
        clRequest.setName(request.getFullName());
        clRequest.setPronouns(request.getPronouns());
        clRequest.setTrait1(request.getTrait1());
        clRequest.setTrait2(request.getTrait2());
        clRequest.setTrait3(request.getTrait3());

        CaptainLogCharacter character = captainLogService.createCharacter(clRequest);

        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId(character.getId());
        summary.setFirstName(request.getFirstName());
        summary.setLastName(request.getLastName());
        summary.setFullName(character.getName());
        summary.setGameType(GameType.CAPTAINS_LOG);
        summary.setGameRole(character.getRank().name() + " — " + character.getAssignment().name());
        summary.setDescription(String.format(
                "Rank: %s, Assignment: %s, Environment: %s",
                character.getRank(), character.getAssignment(), character.getEnvironment()));
        summary.setAlive(true);
        return summary;
    }

    private UnifiedCharacterSummary createDoraMCUPlayer(com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest request) {
        // Check if player already exists
        Optional<Player> existingPlayer = playerRepository.findByEmail(request.getEmail());
        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
            summary.setId(player.getId());
            summary.setFullName(player.getEmpireName());
            summary.setGameType(GameType.DORAMCU_4X);
            summary.setGameRole("Empire Ruler");
            summary.setDescription("Empire: " + player.getEmpireName() +
                    " (" + player.getGovernmentType() + ", " + player.getEthos() + ")");
            summary.setAlive(true);
            return summary;
        }

        // Create a new player
        String empireName = request.getFullName() + " Empire";
        String loginPin = generateLoginPin();

        // Ensure user exists
        String username = request.getEmail();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(loginPin));
            user.setGalaxyName("Main Galaxy");
            userRepository.save(user);
            userOpt = userRepository.findByUsername(username);
        }

        Player player = new Player();
        player.setEmail(request.getEmail());
        player.setLoginPin(loginPin);
        player.setEmpireName(empireName);
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.MATERIALIST);
        player.setUser(userOpt.get());
        player = playerRepository.save(player);

        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId(player.getId());
        summary.setFirstName(request.getFirstName());
        summary.setLastName(request.getLastName());
        summary.setFullName(empireName);
        summary.setGameType(GameType.DORAMCU_4X);
        summary.setGameRole("Empire Ruler");
        summary.setDescription("Empire: " + empireName +
                " (" + player.getGovernmentType() + ", " + player.getEthos() + ")");
        summary.setAlive(true);
        return summary;
    }

    private String generateLoginPin() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        java.util.Random random = new java.util.Random();
        StringBuilder pin = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            pin.append(chars.charAt(random.nextInt(chars.length())));
        }
        // Ensure uniqueness
        String generatedPin = pin.toString();
        while (playerRepository.existsByLoginPin(generatedPin)) {
            pin.setLength(0);
            for (int i = 0; i < 6; i++) {
                pin.append(chars.charAt(random.nextInt(chars.length())));
            }
            generatedPin = pin.toString();
        }
        return generatedPin;
    }
}
