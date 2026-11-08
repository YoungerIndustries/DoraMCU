package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.SectorRepository;
import com.yisy.modules.doramcu.repository.SubSectorRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.GalaxyProgressService;
import com.yisy.modules.doramcu.service.GalaxyService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.service.TurnScheduler;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/4x/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GalaxyRepository galaxyRepository;
    private final SectorRepository sectorRepository;
    private final SubSectorRepository subSectorRepository;
    private final GalaxyService galaxyService;
    private final GalaxyProgressService galaxyProgressService;
    private final PlayerRepository playerRepository;
    private final TurnScheduler turnScheduler;
    private final GameSessionService gameSessionService;
    private final DoraAiLearningService doraAiLearningService;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${logging.file.name:logs/doramcu.log}")
    private String logFileName;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           GalaxyRepository galaxyRepository,
                           SectorRepository sectorRepository,
                           SubSectorRepository subSectorRepository,
                           GalaxyService galaxyService,
                           GalaxyProgressService galaxyProgressService,
                           PlayerRepository playerRepository,
                           TurnScheduler turnScheduler,
                           GameSessionService gameSessionService,
                           DoraAiLearningService doraAiLearningService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.galaxyRepository = galaxyRepository;
        this.sectorRepository = sectorRepository;
        this.subSectorRepository = subSectorRepository;
        this.galaxyService = galaxyService;
        this.galaxyProgressService = galaxyProgressService;
        this.playerRepository = playerRepository;
        this.turnScheduler = turnScheduler;
        this.gameSessionService = gameSessionService;
        this.doraAiLearningService = doraAiLearningService;
    }

    // ==================== Admin Rights Check ====================

    @GetMapping("/check-admin")
    public ResponseEntity<Map<String, Object>> checkAdminRights(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        String username = authentication.getName();

        // Look up the player by email first
        Optional<Player> playerOpt = playerRepository.findByEmail(username);
        if (playerOpt.isEmpty()) {
            playerOpt = playerRepository.findByEmail(username + "@doramcu.local");
        }
        // Also try to find by linked User
        if (playerOpt.isEmpty()) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                playerOpt = playerRepository.findByUser(userOpt.get());
            }
        }

        boolean hasAdminRights = playerOpt.map(Player::isAdmin).orElse(false);
        result.put("isAdmin", hasAdminRights);
        result.put("username", username);

        if (hasAdminRights) {
            log.info("Admin rights check passed for user: {}", username);
            return ResponseEntity.ok(result);
        } else {
            log.warn("Admin rights check failed for user: {}", username);
            result.put("message", "Admin rights required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
    }

    // ==================== Server Status ====================

    @GetMapping("/server/status")
    public Map<String, Object> getServerStatus() {
        log.debug("Admin requested server status");
        Map<String, Object> status = new HashMap<>();

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        // Uptime
        long uptimeMs = runtimeBean.getUptime();
        Duration uptime = Duration.ofMillis(uptimeMs);
        status.put("uptime", formatUptime(uptime));
        status.put("uptimeSeconds", uptime.getSeconds());

        // Memory
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        status.put("memory", Map.of(
                "heapUsed", heapUsage.getUsed(),
                "heapMax", heapUsage.getMax(),
                "heapPercent", String.format("%.1f", heapUsage.getUsed() * 100.0 / heapUsage.getMax())
        ));

        // CPU
        status.put("cpuCores", osBean.getAvailableProcessors());
        status.put("loadAverage", osBean.getSystemLoadAverage());
        status.put("osName", System.getProperty("os.name"));
        status.put("osVersion", System.getProperty("os.version"));

        // Server info
        status.put("serverPort", serverPort);
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("startTime", LocalDateTime.now().toString());

        // User count
        status.put("userCount", userRepository.count());

        return status;
    }

    // ==================== User Management ====================

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        log.debug("Admin requested all users");
        List<Map<String, Object>> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            users.add(map);
        });
        return users;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        log.info("Admin created user: {}", username);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User created successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        log.info("Admin deleted user with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Server Configuration ====================

    @GetMapping("/config")
    public Map<String, Object> getServerConfig() {
        log.debug("Admin requested server configuration");
        Map<String, Object> config = new HashMap<>();
        config.put("serverPort", serverPort);
        config.put("logFileName", logFileName);
        config.put("appName", "DoraMCU");
        config.put("appVersion", "0.0.1-SNAPSHOT");
        config.put("databaseUrl", "jdbc:mysql://localhost:3306/doramcu");
        config.put("cacheType", "caffeine");
        config.put("loggingLevel", "DEBUG");
        return config;
    }

    @PutMapping("/config/logging-level")
    public ResponseEntity<?> updateLoggingLevel(@RequestBody Map<String, String> request) {
        String level = request.get("level");
        if (level == null || !level.matches("^(TRACE|DEBUG|INFO|WARN|ERROR)$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid log level. Use: TRACE, DEBUG, INFO, WARN, ERROR"));
        }
        log.info("Admin updated logging level to: {}", level);
        return ResponseEntity.ok(Map.of("message", "Logging level updated to " + level));
    }

    // ==================== System Logs ====================

    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(@RequestParam(defaultValue = "100") int lines) {
        try {
            Path logPath = Path.of(logFileName);
            if (!Files.exists(logPath)) {
                return ResponseEntity.ok(Map.of("logs", List.of(), "message", "Log file not found"));
            }

            List<String> logLines = new ArrayList<>();
            try (RandomAccessFile raf = new RandomAccessFile(logFileName, "r")) {
                long fileLength = raf.length();
                long position = fileLength;
                int lineCount = 0;
                byte[] buffer = new byte[1024];

                while (position > 0 && lineCount < lines) {
                    long readPosition = Math.max(0, position - 1024);
                    raf.seek(readPosition);
                    int bytesRead = raf.read(buffer);

                    for (int i = 0; i < bytesRead; i++) {
                        if (buffer[i] == '\n') {
                            if (readPosition + i + 1 < position) {
                                long lineStart = readPosition;
                                long lineEnd = position;
                                raf.seek(lineStart);
                                byte[] lineBytes = new byte[(int) (lineEnd - lineStart)];
                                raf.read(lineBytes);
                                logLines.add(new String(lineBytes).trim());
                                lineCount++;
                            }
                            position = readPosition + i;
                        }
                    }
                    position = readPosition;
                }
            }

            Collections.reverse(logLines);
            return ResponseEntity.ok(Map.of("logs", logLines));
        } catch (IOException e) {
            log.error("Failed to read log file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to read log file"));
        }
    }

    // ==================== Galaxy Management ====================

    @GetMapping("/galaxies")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getGalaxies() {
        log.debug("Admin requested galaxy list");
        try {
            long count = galaxyRepository.count();
            log.info("Galaxy repository returned {} galaxies", count);
            List<Map<String, Object>> galaxies = new ArrayList<>();
            galaxyRepository.findAll().forEach(galaxy -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", galaxy.getId());
                map.put("name", galaxy.getName());
                map.put("totalSectors", galaxy.getTotalSectors());
                map.put("hyperlaneDensity", galaxy.getHyperlaneDensity());
                galaxies.add(map);
            });
            return ResponseEntity.ok(galaxies);
        } catch (Exception e) {
            log.error("Failed to list galaxies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load galaxies: " + e.getMessage()));
        }
    }

    @PostMapping("/galaxies/rebuild")
    public ResponseEntity<?> rebuildGalaxies(@RequestBody(required = false) Map<String, List<String>> request) {

        // Determine which galaxies to rebuild
        List<String> galaxyNames;
        if (request != null && request.containsKey("galaxies")) {
            galaxyNames = request.get("galaxies");
        } else {
            // Default: rebuild the single galaxy
            galaxyNames = List.of(GalaxyFactory.GAME_GALAXY_NAME);
        }

        log.info("Admin requested rebuild of galaxies: {}", galaxyNames);

        List<String> rebuilt = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        java.util.List<Player> players = playerRepository.findAll();
        // Reset progress service to clear any previous completed state
        galaxyProgressService.reset();
        galaxyService.setProgressListener(galaxyProgressService);

        for (String name : galaxyNames) {
            try {
                // Delete existing galaxy if present
                galaxyRepository.findByName(name).ifPresent(g -> {
                    log.info("Deleting existing galaxy '{}' for rebuild", name);
                    galaxyRepository.delete(g);
                });

                // Build AI-controlled empires
                Map<Integer, Empire> empires = new HashMap<>();
                for (Player player : players) {
                    int empireId = player.getId().intValue();
                    Empire empire = new Empire(empireId, player.getEmpireName(), -1);
                    empires.put(empireId, empire);
                }

                // Generate with appropriate config
                GalaxyGenerationConfig config;
                long seed;
                if (GalaxyFactory.GAME_GALAXY_NAME.equals(name)) {
                    config = GalaxyFactory.gameGalaxyConfig();
                    seed = GalaxyFactory.GAME_GALAXY_SEED;
                } else {
                    errors.add("Unknown galaxy: " + name);
                    continue;
                }

                galaxyService.generateAndSaveGalaxyWithEmpires(
                        name, empires,
                        config.getSystemCount(), config.getPlanetsPerSystem(),
                        config.getMoonChance(), config.getAsteroidBeltChance(),
                        config.getGasGiantChance(), config.getNebulaChance(),
                        config.getBlackHoleChance(), config.getCometChance(),
                        config.getSpaceStationChance(), config.getHyperlaneDensity(),
                        seed
                );
                rebuilt.add(name);
                log.info("Galaxy '{}' rebuilt successfully", name);
            } catch (Exception e) {
                String errorMsg = "Failed to rebuild " + name + ": " + e.getMessage();
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        }

        // Signal that all galaxy generation is finished and complete SSE emitters
        galaxyProgressService.finishAll();

        Map<String, Object> result = new HashMap<>();
        result.put("rebuilt", rebuilt);
        result.put("message", "Rebuilt " + rebuilt.size() + " galaxy/galaxies: " + String.join(", ", rebuilt));
        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }
        return ResponseEntity.ok(result);
    }

    // ==================== Turn Management ====================

    @GetMapping("/turn/status")
    public ResponseEntity<Map<String, Object>> getTurnStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", turnScheduler.isEnabled());
        status.put("turnIntervalSeconds", turnScheduler.getTurnIntervalSeconds());

        // Gather per-session turn info
        List<Map<String, Object>> sessions = new ArrayList<>();
        for (var entry : gameSessionService.getAllSessions().entrySet()) {
            String sessionId = entry.getKey();
            var session = entry.getValue();
            com.yisy.modules.doramcu.hex.Game game = (com.yisy.modules.doramcu.hex.Game) session.getGame();
            Map<String, Object> info = new HashMap<>();
            info.put("sessionId", sessionId);
            info.put("sessionName", session.getName());
            info.put("currentTurn", game.getCurrentTurn());
            info.put("currentTime", game.getCurrentTime());
            info.put("playerCount", game.getPlayers().size());
            info.put("accumulatedSeconds", turnScheduler.getAccumulatedSeconds(sessionId));
            info.put("turnMode", "simultaneous");
            sessions.add(info);
        }
        status.put("sessions", sessions);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/turn/advance")
    public ResponseEntity<Map<String, Object>> advanceTurn(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "sessionId is required"));
        }

        var session = gameSessionService.getGameSession(sessionId);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Session not found: " + sessionId));
        }

        com.yisy.modules.doramcu.hex.Game game = (com.yisy.modules.doramcu.hex.Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            game.nextTurn();
            // Simultaneous turns: no nextPlayer() cycling

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Turn advanced successfully (simultaneous turns)");
            result.put("turn", game.getCurrentTurn());
            result.put("sessionId", sessionId);

            gameSessionService.broadcastEvent(sessionId,
                    new com.yisy.modules.doramcu.dto.GameEvent(
                            com.yisy.modules.doramcu.dto.GameEvent.EventType.TURN_CHANGED,
                            sessionId,
                            Map.of(
                                    "turn", game.getCurrentTurn(),
                                    "adminAdvanced", true
                            )));

            return ResponseEntity.ok(result);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    @PostMapping("/turn/interval")
    public ResponseEntity<Map<String, Object>> setTurnInterval(@RequestBody Map<String, Long> request) {
        Long seconds = request.get("seconds");
        if (seconds == null || seconds < 1) {
            return ResponseEntity.badRequest().body(Map.of("error", "seconds must be >= 1"));
        }

        turnScheduler.setTurnIntervalSeconds(seconds);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Turn interval set to " + seconds + " seconds");
        result.put("turnIntervalSeconds", turnScheduler.getTurnIntervalSeconds());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/turn/toggle")
    public ResponseEntity<Map<String, Object>> toggleTurnScheduler(@RequestBody Map<String, Boolean> request) {
        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "enabled is required"));
        }

        turnScheduler.setEnabled(enabled);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Turn scheduler " + (enabled ? "enabled" : "disabled"));
        result.put("enabled", turnScheduler.isEnabled());
        return ResponseEntity.ok(result);
    }

    // ==================== Dora AI Learning Management ====================

    @GetMapping("/ai/learning/status")
    public ResponseEntity<Map<String, Object>> getLearningStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("learningEnabled", doraAiLearningService.isLearningEnabled());
        status.put("experienceBufferSize", doraAiLearningService.getExperienceBufferSize());
        status.put("totalActionsRecorded", doraAiLearningService.getTotalActionsRecorded());
        status.put("totalTrainingCycles", doraAiLearningService.getTotalTrainingCycles());
        long lastTraining = doraAiLearningService.getLastTrainingTime();
        status.put("lastTrainingTime", lastTraining > 0 ? LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastTraining), java.time.ZoneId.systemDefault()).toString() : "Never");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/ai/learning/toggle")
    public ResponseEntity<Map<String, Object>> toggleLearning(@RequestBody Map<String, Boolean> request) {
        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "enabled is required"));
        }
        doraAiLearningService.setLearningEnabled(enabled);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Learning " + (enabled ? "enabled" : "disabled"));
        result.put("learningEnabled", doraAiLearningService.isLearningEnabled());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ai/learning/train")
    public ResponseEntity<Map<String, Object>> triggerTraining() {
        int trained = doraAiLearningService.trainOnAccumulatedExperience();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Trained on " + trained + " samples");
        result.put("samplesTrained", trained);
        result.put("totalTrainingCycles", doraAiLearningService.getTotalTrainingCycles());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ai/learning/save-model")
    public ResponseEntity<Map<String, Object>> saveModel() {
        try {
            String path = doraAiLearningService.saveModel();
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Model saved successfully");
            result.put("modelPath", path);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save model: " + e.getMessage()));
        }
    }

    @PostMapping("/ai/learning/clear-experience")
    public ResponseEntity<Map<String, Object>> clearExperience() {
        doraAiLearningService.clearExperience();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Experience buffer cleared");
        return ResponseEntity.ok(result);
    }

    // ==================== Sector/SubSector Renaming ====================

    @PutMapping("/galaxies/{galaxyId}/sectors/{row}/{col}/name")
    public ResponseEntity<Map<String, Object>> renameSector(
            @PathVariable Long galaxyId,
            @PathVariable int row,
            @PathVariable int col,
            @RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
        }

        com.yisy.modules.doramcu.model.Galaxy dbGalaxy = galaxyRepository.findById(galaxyId)
                .orElseThrow(() -> new IllegalArgumentException("Galaxy not found: " + galaxyId));

        com.yisy.modules.doramcu.model.Sector dbSector = galaxyService.findSectorByGalaxyAndPosition(galaxyId, row, col);
        if (dbSector == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Sector not found at (" + row + ", " + col + ")"));
        }

        dbSector.setName(name);
        sectorRepository.save(dbSector);

        // Update in-memory galaxy if present in active game sessions
        gameSessionService.updateSectorNameInMemory(dbGalaxy.getName(), row, col, name);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Sector renamed to '" + name + "'");
        result.put("sectorRow", row);
        result.put("sectorCol", col);
        result.put("name", name);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/galaxies/{galaxyId}/sectors/{row}/{col}/subsectors/{subRow}/{subCol}/name")
    public ResponseEntity<Map<String, Object>> renameSubSector(
            @PathVariable Long galaxyId,
            @PathVariable int row,
            @PathVariable int col,
            @PathVariable int subRow,
            @PathVariable int subCol,
            @RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
        }

        com.yisy.modules.doramcu.model.Galaxy dbGalaxy = galaxyRepository.findById(galaxyId)
                .orElseThrow(() -> new IllegalArgumentException("Galaxy not found: " + galaxyId));

        com.yisy.modules.doramcu.model.Sector dbSector = galaxyService.findSectorByGalaxyAndPosition(galaxyId, row, col);
        if (dbSector == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Sector not found at (" + row + ", " + col + ")"));
        }

        com.yisy.modules.doramcu.model.SubSector dbSubSector = galaxyService.findSubSectorBySectorAndPosition(dbSector.getId(), subRow, subCol);
        if (dbSubSector == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "SubSector not found at (" + subRow + ", " + subCol + ")"));
        }

        dbSubSector.setName(name);
        subSectorRepository.save(dbSubSector);

        // Update in-memory galaxy if present in active game sessions
        gameSessionService.updateSubSectorNameInMemory(dbGalaxy.getName(), row, col, subRow, subCol, name);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "SubSector renamed to '" + name + "'");
        result.put("sectorRow", row);
        result.put("sectorCol", col);
        result.put("subSectorRow", subRow);
        result.put("subSectorCol", subCol);
        result.put("name", name);
        return ResponseEntity.ok(result);
    }

    // ==================== Utility ====================

    private String formatUptime(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.getSeconds();

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
        }
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        }
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        }
        return String.format("%ds", seconds);
    }
}
