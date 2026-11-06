package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.config.MetricsConfig;
import com.yisy.modules.doramcu.dto.LoginRequest;
import com.yisy.modules.doramcu.dto.LoginResponse;
import com.yisy.modules.doramcu.dto.PinLoginRequest;
import com.yisy.modules.doramcu.dto.RegisterRequest;
import com.yisy.modules.doramcu.dto.ServerInfoDto;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
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

    public AuthenticationController(AuthenticationManager authenticationManager, JwtService jwtService,
                                     MetricsConfig metricsConfig, UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     ObjectProvider<PlayerService> playerServiceProvider,
                                     ObjectProvider<PlayerRepository> playerRepositoryProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.metricsConfig = metricsConfig;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerService = playerServiceProvider.getIfAvailable();
        this.playerRepository = playerRepositoryProvider.getIfAvailable();
    }

    @GetMapping("/server-info")
    public ResponseEntity<ServerInfoDto> getServerInfo() {
        long uptime = System.currentTimeMillis() - startTime;
        int count = (playerRepository != null) ? (int) playerRepository.count() : 0;
        List<String> galaxies = Arrays.asList(
                GalaxyFactory.GAME_GALAXY_NAME,
                GalaxyFactory.TEST_GALAXY_NAME
        );
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
                user.setGalaxyName(GalaxyFactory.GAME_GALAXY_NAME);
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

    @GetMapping("/galaxies")
    public ResponseEntity<List<String>> listGalaxies() {
        List<String> galaxies = Arrays.asList(
                GalaxyFactory.GAME_GALAXY_NAME,
                GalaxyFactory.TEST_GALAXY_NAME
        );
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
            galaxyName = GalaxyFactory.GAME_GALAXY_NAME;
        } else if (!GalaxyFactory.isValidGalaxyName(galaxyName)) {
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
                player.setEmpireName(request.getUsername() + " Empire");
                player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
                player.setEthos(Player.Ethos.MATERIALIST);
                player.setUser(user);
                playerRepository.save(player);
                log.info("Provisioned player PIN: {} for user: {}", pin, request.getUsername());
            } catch (Exception e) {
                log.warn("Could not automatically provision player PIN for user {}: {}", request.getUsername(), e.getMessage());
            }
        }

        // Auto-login after registration
        String token = jwtService.generateToken(request.getUsername());
        LoginResponse response = new LoginResponse(token, request.getUsername(), pin, request.getUsername() + " Empire");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
