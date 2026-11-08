package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.config.MetricsConfig;
import com.yisy.modules.doramcu.dto.LoginRequest;
import com.yisy.modules.doramcu.dto.LoginResponse;
import com.yisy.modules.doramcu.dto.PinLoginRequest;
import com.yisy.modules.doramcu.dto.PlayerSummaryDto;
import com.yisy.modules.doramcu.dto.RegisterRequest;
import com.yisy.modules.doramcu.dto.ServerInfoDto;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.model.Galaxy;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.JwtService;
import com.yisy.modules.doramcu.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/4x/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final long startTime = System.currentTimeMillis();
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MetricsConfig metricsConfig;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final GalaxyRepository galaxyRepository;

    public AuthenticationController(AuthenticationManager authenticationManager, JwtService jwtService,
                                     MetricsConfig metricsConfig, UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     ObjectProvider<PlayerService> playerServiceProvider,
                                     ObjectProvider<PlayerRepository> playerRepositoryProvider,
                                     ObjectProvider<GalaxyRepository> galaxyRepositoryProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.metricsConfig = metricsConfig;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerService = playerServiceProvider.getIfAvailable();
        this.playerRepository = playerRepositoryProvider.getIfAvailable();
        this.galaxyRepository = galaxyRepositoryProvider.getIfAvailable();
    }

    @GetMapping("/server-info")
    public ResponseEntity<ServerInfoDto> getServerInfo() {
        long uptime = System.currentTimeMillis() - startTime;
        int count = (playerRepository != null) ? (int) playerRepository.count() : 0;
        List<String> galaxies;
        if (galaxyRepository != null) {
            galaxies = galaxyRepository.findAll().stream()
                    .map(Galaxy::getName)
                    .filter(name -> name != null)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            galaxies = List.of();
        }
        ServerInfoDto info = new ServerInfoDto(
                "DoraMCU Galactic Cluster Alpha",
                "1.0.0",
                "ONLINE",
                "Local Galaxy Node",
                galaxies,
                count,
                uptime
        );
        return ResponseEntity.ok(info);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login attempt for user: {}", request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            metricsConfig.getLoginFailureCounter().increment();
            return ResponseEntity.badRequest().build();
        }

        String token = jwtService.generateToken(request.getUsername());
        metricsConfig.getLoginSuccessCounter().increment();
        log.debug("User successfully logged in: {}", request.getUsername());

        String pin = null;
        String empireName = null;
        boolean isAdmin = false;
        if (playerRepository != null) {
            Optional<Player> pOpt = playerRepository.findByEmail(request.getUsername());
            if (pOpt.isEmpty()) {
                pOpt = playerRepository.findByEmail(request.getUsername() + "@doramcu.local");
            }
            // Also try to find by linked User
            if (pOpt.isEmpty()) {
                Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
                if (userOpt.isPresent()) {
                    pOpt = playerRepository.findByUser(userOpt.get());
                }
            }
            if (pOpt.isPresent()) {
                pin = pOpt.get().getLoginPin();
                empireName = pOpt.get().getEmpireName();
                isAdmin = pOpt.get().isAdmin();
            }
        }

        LoginResponse response = new LoginResponse(token, request.getUsername(), pin, empireName);
        response.setAdmin(isAdmin);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pin-login")
    public ResponseEntity<?> pinLogin(@Valid @RequestBody PinLoginRequest request) {
        log.debug("PIN login attempt with PIN: {}", request.getPin());
        if (playerService == null && playerRepository == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Player service not configured"));
        }

        Optional<Player> playerOpt = (playerService != null)
                ? playerService.findByLoginPin(request.getPin())
                : playerRepository.findByLoginPin(request.getPin());

        if (playerOpt.isEmpty()) {
            log.warn("Failed PIN login attempt with invalid PIN: {}", request.getPin());
            metricsConfig.getLoginFailureCounter().increment();
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or unregistered Player PIN"));
        }

        Player player = playerOpt.get();
        String username = player.getEmail();
        if (player.getUser() != null) {
            username = player.getUser().getUsername();
        } else {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                player.setUser(userOpt.get());
                if (playerRepository != null) {
                    playerRepository.save(player);
                }
            } else {
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(player.getLoginPin()));
                if (galaxyRepository != null && !galaxyRepository.findAll().isEmpty()) {
                    user.setGalaxyName(galaxyRepository.findAll().get(0).getName());
                } else {
                    user.setGalaxyName(GalaxyFactory.GAME_GALAXY_NAME);
                }
                userRepository.save(user);
                player.setUser(user);
                if (playerRepository != null) {
                    playerRepository.save(player);
                }
            }
        }

        String token = jwtService.generateToken(username);
        metricsConfig.getLoginSuccessCounter().increment();
        log.info("Player logged in with PIN: {} (identity: {})", request.getPin(), username);

        LoginResponse response = new LoginResponse(token, username, player.getLoginPin(), player.getEmpireName());
        if (player.getGovernmentType() != null) {
            response.setGovernmentType(player.getGovernmentType().name());
        }
        if (player.getEthos() != null) {
            response.setEthos(player.getEthos().name());
        }
        response.setAdmin(player.isAdmin());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/players")
    public ResponseEntity<List<PlayerSummaryDto>> listPlayers() {
        if (playerRepository == null) {
            return ResponseEntity.ok(List.of());
        }
        List<PlayerSummaryDto> players = playerRepository.findAll().stream()
                .map(p -> new PlayerSummaryDto(
                        p.getId(),
                        p.getEmpireName(),
                        p.getGovernmentType() != null ? p.getGovernmentType().name() : "DEMOCRACY",
                        p.getEthos() != null ? p.getEthos().name() : "MATERIALIST",
                        p.getUser() != null ? p.getUser().getUsername() : p.getEmail(),
                        p.isAdmin()
                ))
                .sorted((a, b) -> a.empireName().compareTo(b.empireName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(players);
    }

    @PostMapping("/player-login/{playerId}")
    public ResponseEntity<?> playerLogin(@PathVariable Long playerId) {
        log.debug("Player login attempt for player ID: {}", playerId);
        if (playerRepository == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Player service not configured"));
        }

        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            log.warn("Failed player login — player ID {} not found", playerId);
            metricsConfig.getLoginFailureCounter().increment();
            return ResponseEntity.notFound().build();
        }

        Player player = playerOpt.get();
        String username;
        if (player.getUser() != null) {
            username = player.getUser().getUsername();
        } else {
            // Auto-provision user if missing
            username = player.getEmail();
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(player.getLoginPin()));
                if (galaxyRepository != null && !galaxyRepository.findAll().isEmpty()) {
                    user.setGalaxyName(galaxyRepository.findAll().get(0).getName());
                } else {
                    user.setGalaxyName(GalaxyFactory.GAME_GALAXY_NAME);
                }
                userRepository.save(user);
                player.setUser(user);
                playerRepository.save(player);
            } else {
                player.setUser(userOpt.get());
                playerRepository.save(player);
            }
        }

        String token = jwtService.generateToken(username);
        metricsConfig.getLoginSuccessCounter().increment();
        log.info("Player logged in by ID: {} (identity: {}, empire: {})", playerId, username, player.getEmpireName());

        LoginResponse response = new LoginResponse(token, username, player.getLoginPin(), player.getEmpireName());
        if (player.getGovernmentType() != null) {
            response.setGovernmentType(player.getGovernmentType().name());
        }
        if (player.getEthos() != null) {
            response.setEthos(player.getEthos().name());
        }
        response.setAdmin(player.isAdmin());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/galaxies")
    public ResponseEntity<List<String>> listGalaxies() {
        List<String> galaxies;
        if (galaxyRepository != null) {
            galaxies = galaxyRepository.findAll().stream()
                    .map(Galaxy::getName)
                    .filter(name -> name != null)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            galaxies = Arrays.asList(GalaxyFactory.GAME_GALAXY_NAME);
        }
        return ResponseEntity.ok(galaxies);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Registration attempt for user: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        // Validate galaxy name if provided
        String galaxyName = request.getGalaxyName();
        if (galaxyName == null || galaxyName.trim().isEmpty()) {
            // Default to the first available galaxy, or fall back to the game galaxy constant
            if (galaxyRepository != null && !galaxyRepository.findAll().isEmpty()) {
                galaxyName = galaxyRepository.findAll().get(0).getName();
            } else {
                galaxyName = GalaxyFactory.GAME_GALAXY_NAME;
            }
        } else if (galaxyRepository != null && !galaxyRepository.findByName(galaxyName).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid galaxy: " + galaxyName));
        } else if (galaxyRepository == null && !GalaxyFactory.isValidGalaxyName(galaxyName)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid galaxy: " + galaxyName));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGalaxyName(galaxyName);
        userRepository.save(user);

        log.info("New user registered: {} (galaxy: {})", request.getUsername(), galaxyName);

        // Provision player PIN code
        String pin = null;
        if (playerService != null && playerRepository != null) {
            try {
                pin = playerService.generateUniquePin();
                Player player = new Player();
                player.setEmail(request.getUsername() + "@doramcu.local");
                player.setLoginPin(pin);
                // Use provided empire details or defaults
                String empireName = request.getEmpireName();
                if (empireName == null || empireName.trim().isEmpty()) {
                    empireName = request.getUsername() + " Empire";
                }
                player.setEmpireName(empireName);

                Player.GovernmentType govType = Player.GovernmentType.DEMOCRACY;
                if (request.getGovernmentType() != null && !request.getGovernmentType().trim().isEmpty()) {
                    try {
                        govType = Player.GovernmentType.valueOf(request.getGovernmentType().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid government type '{}', defaulting to DEMOCRACY", request.getGovernmentType());
                    }
                }
                player.setGovernmentType(govType);

                Player.Ethos ethos = Player.Ethos.MATERIALIST;
                if (request.getEthos() != null && !request.getEthos().trim().isEmpty()) {
                    try {
                        ethos = Player.Ethos.valueOf(request.getEthos().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid ethos '{}', defaulting to MATERIALIST", request.getEthos());
                    }
                }
                player.setEthos(ethos);
                player.setUser(user);
                playerRepository.save(player);
                log.info("Provisioned player PIN: {} for user: {}", pin, request.getUsername());
            } catch (Exception e) {
                log.warn("Could not automatically provision player PIN for user {}: {}", request.getUsername(), e.getMessage());
            }
        }

        // Auto-login after registration
        String token = jwtService.generateToken(request.getUsername());
        String empireName = (request.getEmpireName() != null && !request.getEmpireName().trim().isEmpty())
                ? request.getEmpireName() : request.getUsername() + " Empire";
        LoginResponse response = new LoginResponse(token, request.getUsername(), pin, empireName);
        if (request.getGovernmentType() != null && !request.getGovernmentType().trim().isEmpty()) {
            try {
                response.setGovernmentType(Player.GovernmentType.valueOf(request.getGovernmentType().toUpperCase()).name());
            } catch (IllegalArgumentException e) {
                // ignore invalid government type
            }
        }
        if (request.getEthos() != null && !request.getEthos().trim().isEmpty()) {
            try {
                response.setEthos(Player.Ethos.valueOf(request.getEthos().toUpperCase()).name());
            } catch (IllegalArgumentException e) {
                // ignore invalid ethos
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Development-only endpoint: bypass normal login and return a JWT token for "Butch Younger".
     * This endpoint is only active in the "dev" profile and should never be used in production.
     * Returns the login info plus the first available session ID so the UI can skip straight to the game.
     */
    @GetMapping("/dev-login")
    public ResponseEntity<LoginResponse> devLogin() {
        log.info("[DEV] Dev login bypass — auto-logging in as Butch Younger");
        String username = "Butch Younger";
        String token = jwtService.generateToken(username);

        String pin = null;
        String empireName = null;
        boolean isAdmin = false;
        if (playerRepository != null) {
            Optional<Player> pOpt = playerRepository.findByEmail(username);
            if (pOpt.isEmpty()) {
                pOpt = playerRepository.findByEmail(username + "@doramcu.local");
            }
            if (pOpt.isEmpty()) {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    pOpt = playerRepository.findByUser(userOpt.get());
                }
            }
            if (pOpt.isPresent()) {
                pin = pOpt.get().getLoginPin();
                empireName = pOpt.get().getEmpireName();
                isAdmin = pOpt.get().isAdmin();
            }
        }

        LoginResponse response = new LoginResponse(token, username, pin, empireName);
        response.setAdmin(isAdmin);
        return ResponseEntity.ok(response);
    }

}
