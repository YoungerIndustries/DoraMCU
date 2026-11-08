package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.hex.GalaxyGenerationProgress;
import com.yisy.modules.doramcu.hex.GalaxyGenerator;
import com.yisy.modules.doramcu.model.Device;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.DeviceRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GalaxyCreationService;
import com.yisy.modules.doramcu.service.GalaxyProgressService;
import com.yisy.modules.doramcu.service.GalaxyService;
import com.yisy.modules.doramcu.service.GameSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = false)
    CommandLineRunner initBasicData(DeviceRepository deviceRepository, UserRepository userRepository,
                                    PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            log.warn("SEED DATA IS ENABLED - This should only be used in development environments!");
            log.info("Initializing database with seed data...");
            // Only seed data if the database is empty
            if (userRepository.count() == 0) {
                // Admin user: Butch Younger with admin rights
                userRepository.save(new User(null, "Butch Younger", passwordEncoder.encode("3214")));
                // Game users
                userRepository.save(new User(null, "player1", passwordEncoder.encode("123456")));
                userRepository.save(new User(null, "player2", passwordEncoder.encode("234567")));
                userRepository.save(new User(null, "player3", passwordEncoder.encode("345678")));
                log.info("4 default users created (Butch Younger, player1, player2, player3)");
            }
            if (deviceRepository.count() == 0) {
                deviceRepository.save(new Device(null, "Sensor-001", "active", "Building A"));
                deviceRepository.save(new Device(null, "Sensor-002", "inactive", "Building B"));
                deviceRepository.save(new Device(null, "Controller-001", "active", "Server Room"));
                deviceRepository.save(new Device(null, "Controller-002", "active", "Command Center"));
                deviceRepository.save(new Device(null, "Scanner-001", "active", "Orbital Station"));
                log.info("5 default devices created");
            }
            if (playerRepository.count() == 0) {
                // Admin player: Butch Younger with admin rights, linked to "Butch Younger" user
                Player butchPlayer = new Player();
                butchPlayer.setEmail("butch.younger@empire.com");
                butchPlayer.setLoginPin("3214");
                butchPlayer.setEmpireName("Interstellar Alliance");
                butchPlayer.setGovernmentType(Player.GovernmentType.DEMOCRACY);
                butchPlayer.setEthos(Player.Ethos.LIBERTARIAN);
                butchPlayer.setAdmin(true);
                Optional<User> butchUserOpt = userRepository.findByUsername("Butch Younger");
                butchUserOpt.ifPresent(butchPlayer::setUser);
                playerRepository.save(butchPlayer);
                log.info("Admin player 'Butch Younger' created with PIN 3214, empire 'Interstellar Alliance', democracy, libertarian");

                // Terran Republic - Democratic materialist empire
                Player player1 = new Player();
                player1.setEmail("player1@empire.com");
                player1.setLoginPin("123456");
                player1.setEmpireName("Terran Republic");
                player1.setGovernmentType(Player.GovernmentType.DEMOCRACY);
                player1.setEthos(Player.Ethos.MATERIALIST);
                playerRepository.save(player1);

                // Solar Imperium - Monarchist militarist empire
                Player player2 = new Player();
                player2.setEmail("player2@empire.com");
                player2.setLoginPin("234567");
                player2.setEmpireName("Solar Imperium");
                player2.setGovernmentType(Player.GovernmentType.MONARCHY);
                player2.setEthos(Player.Ethos.MILITARIST);
                playerRepository.save(player2);

                // United Colonies - Libertarian confederation
                Player player3 = new Player();
                player3.setEmail("player3@empire.com");
                player3.setLoginPin("345678");
                player3.setEmpireName("United Colonies");
                player3.setGovernmentType(Player.GovernmentType.CONFEDERATION);
                player3.setEthos(Player.Ethos.LIBERTARIAN);
                playerRepository.save(player3);

                // Additional players for multiplayer scenarios
                Player player4 = new Player();
                player4.setEmail("player4@empire.com");
                player4.setLoginPin("456789");
                player4.setEmpireName("Stellar Hegemony");
                player4.setGovernmentType(Player.GovernmentType.OLIGARCHY);
                player4.setEthos(Player.Ethos.EQUALIST);
                playerRepository.save(player4);

                Player player5 = new Player();
                player5.setEmail("player5@empire.com");
                player5.setLoginPin("567890");
                player5.setEmpireName("Divine Theocracy");
                player5.setGovernmentType(Player.GovernmentType.THEOCRACY);
                player5.setEthos(Player.Ethos.SPIRITUALIST);
                playerRepository.save(player5);

                Player player6 = new Player();
                player6.setEmail("player6@empire.com");
                player6.setLoginPin("678901");
                player6.setEmpireName("Corporate Syndicate");
                player6.setGovernmentType(Player.GovernmentType.CORPORATE);
                player6.setEthos(Player.Ethos.PACIFIST);
                playerRepository.save(player6);

                log.info("8 default players created (including admin)");
            }
            log.info("Basic database initialization completed");
        };
    }

    /**
     * First CommandLineRunner: generates the galaxy synchronously if it doesn't exist.
     * Creates AI-controlled Empire instances BEFORE any players join, so planets
     * can be assigned to empires with shipyard and industrial facilities.
     * When players later join the game session, their empires are converted from
     * AI-controlled to player-controlled.
     * This runs BEFORE any other initialization, ensuring the galaxy is fully built
     * and persisted before game sessions are created.
     */
    @Bean
    @ConditionalOnProperty(name = "doramcu.galaxy.auto-generate", havingValue = "true", matchIfMissing = false)
    CommandLineRunner initGalaxyFirst(GalaxyService galaxyService,
                                       GalaxyProgressService galaxyProgressService,
                                       PlayerRepository playerRepository) {
        return args -> {
            // Set progress listener to broadcast SSE events to connected clients
            galaxyService.setProgressListener(galaxyProgressService);

            java.util.List<Player> players = playerRepository.findAll();
            log.info("Checking galaxy persistence for {} player slots...", players.size());

            // Generate Game Galaxy ("Milky Way") only if it doesn't already exist
            if (!galaxyService.hasGalaxy(GalaxyFactory.GAME_GALAXY_NAME)) {
                log.info("Galaxy '{}' not found. Starting synchronous generation...", GalaxyFactory.GAME_GALAXY_NAME);

                Map<Integer, Empire> empires = buildAiEmpires(players);
                for (Empire e : empires.values()) {
                    log.info("Created AI-controlled empire: {} (id={}, ruler=AI)", e.getName(), e.getId());
                }

                galaxyService.generateAndSaveGalaxyWithEmpires(
                        GalaxyFactory.GAME_GALAXY_NAME,
                        empires,
                        GalaxyFactory.gameGalaxyConfig().getSystemCount(),
                        GalaxyFactory.gameGalaxyConfig().getPlanetsPerSystem(),
                        GalaxyFactory.gameGalaxyConfig().getMoonChance(),
                        GalaxyFactory.gameGalaxyConfig().getAsteroidBeltChance(),
                        GalaxyFactory.gameGalaxyConfig().getGasGiantChance(),
                        GalaxyFactory.gameGalaxyConfig().getNebulaChance(),
                        GalaxyFactory.gameGalaxyConfig().getBlackHoleChance(),
                        GalaxyFactory.gameGalaxyConfig().getCometChance(),
                        GalaxyFactory.gameGalaxyConfig().getSpaceStationChance(),
                        GalaxyFactory.gameGalaxyConfig().getHyperlaneDensity(),
                        GalaxyFactory.GAME_GALAXY_SEED
                );
                log.info("Galaxy '{}' generated with {} AI-controlled empires and persisted at startup",
                        GalaxyFactory.GAME_GALAXY_NAME, players.size());
            } else {
                log.info("Galaxy '{}' already exists in database. Skipping generation.", GalaxyFactory.GAME_GALAXY_NAME);
            }

            // Signal galaxy ready to all connected SSE clients so the frontend modal closes
            galaxyProgressService.finishAll();
            log.info("Galaxy ready signal broadcast to connected clients");

        };
    }

    /**
     * Builds a map of AI-controlled Empire instances from the given players.
     *
     * @param players the list of players
     * @return map of empire ID to Empire instance
     */
    private static Map<Integer, Empire> buildAiEmpires(java.util.List<Player> players) {
        Map<Integer, Empire> empires = new HashMap<>();
        for (Player player : players) {
            int empireId = player.getId().intValue();
            Empire empire = new Empire(empireId, player.getEmpireName(), -1);
            empires.put(empireId, empire);
        }
        return empires;
    }

    /**
     * Second CommandLineRunner: creates the default game session after galaxy is ready.
     * Uses GalaxyCreationService to orchestrate the complete sequence:
     * galaxy creation → session creation → 3 AI players → ready for human players to join.
     */
    @Bean
    @ConditionalOnProperty(name = "doramcu.galaxy.auto-generate", havingValue = "true", matchIfMissing = false)
    CommandLineRunner initGameSessionAfterGalaxy(GalaxyCreationService galaxyCreationService,
                                                  GameSessionService gameSessionService) {
        return args -> {
            // Always ensure a default game session exists, after galaxy is ready.
            if (gameSessionService.getDefaultSession() == null) {
                log.info("Creating default game session with AI players using GalaxyCreationService...");
                String defaultSessionName = "Galactic Conquest";

                // Use GalaxyCreationService to orchestrate the complete sequence
                GalaxyCreationService.GalaxyCreationResult result =
                        galaxyCreationService.createGalaxyWithAiPlayers(defaultSessionName);

                log.info("Default game session created: {} ({})", result.getSessionId(), defaultSessionName);
                log.info("AI players added: {}", result.getAiPlayers().size());
                for (GalaxyCreationService.AiPlayerInfo aiPlayer : result.getAiPlayers()) {
                    log.info("  - {} (id={}, difficulty={})", aiPlayer.getPlayerName(),
                            aiPlayer.getPlayerId(), aiPlayer.getDifficulty());
                }
                log.info("Galaxy ready for human players to join with empire creation");
            } else {
                log.info("Default game session already exists: {} ({})",
                        gameSessionService.getDefaultSession().getName(),
                        gameSessionService.getDefaultSession().getSessionId());
            }
        };
    }

    /**
     * Generates the game galaxy synchronously with the given progress listener.
     * Used by GameLauncher to show the startup modal while generating.
     *
     * @param galaxyService the galaxy service
     * @param progressListener the progress listener (e.g., GalaxyStartupModal)
     */
    public static void generateGalaxyWithProgress(GalaxyService galaxyService, GalaxyGenerationProgress progressListener) {
        log.info("Generating galaxy with progress reporting...");
        galaxyService.setProgressListener(progressListener);

        galaxyService.generateAndSaveGalaxy(
                GalaxyFactory.GAME_GALAXY_NAME,
                GalaxyFactory.gameGalaxyConfig().getSystemCount(),
                GalaxyFactory.gameGalaxyConfig().getPlanetsPerSystem(),
                GalaxyFactory.gameGalaxyConfig().getMoonChance(),
                GalaxyFactory.gameGalaxyConfig().getAsteroidBeltChance(),
                GalaxyFactory.gameGalaxyConfig().getGasGiantChance(),
                GalaxyFactory.gameGalaxyConfig().getNebulaChance(),
                GalaxyFactory.gameGalaxyConfig().getBlackHoleChance(),
                GalaxyFactory.gameGalaxyConfig().getCometChance(),
                GalaxyFactory.gameGalaxyConfig().getSpaceStationChance(),
                GalaxyFactory.gameGalaxyConfig().getHyperlaneDensity(),
                GalaxyFactory.GAME_GALAXY_SEED
        );
        log.info("Galaxy generation with progress reporting complete");
    }

    /**
     * Last CommandLineRunner: signals that all server-side initialization is complete.
     * The GUI is launched by GameLauncher after SpringApplication.run() returns,
     * so this runner only logs the completion status.
     */
    @Bean
    CommandLineRunner signalInitializationComplete() {
        return args -> {
            // Detect headless mode — log different message based on mode
            boolean headless = "true".equals(System.getProperty("java.awt.headless"));
            if (!headless) {
                try {
                    headless = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance();
                } catch (java.awt.HeadlessException e) {
                    headless = true;
                }
            }
            if (headless) {
                log.info("All initialization complete — server is ready on port 8080 (headless mode)");
            } else {
                log.info("All server initialization complete — ready to launch GUI");
            }
        };
    }
}
