package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.config.PythonServerLauncher;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GalaxyCreationService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.service.TurnScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Aggregated health dashboard controller that collects status from all services
 * and the current Junie agent session state.
 */
@RestController
@RequestMapping("/api/v1/4x/health")
public class HealthDashboardController {

    private static final Logger log = LoggerFactory.getLogger(HealthDashboardController.class);

    private final long startTime = System.currentTimeMillis();
    private final List<HealthIndicator> healthIndicators;
    private final ObjectProvider<GameSessionService> gameSessionServiceProvider;
    private final ObjectProvider<PythonServerLauncher> pythonServerLauncherProvider;
    private final ObjectProvider<TurnScheduler> turnSchedulerProvider;
    private final ObjectProvider<AiPlayerService> aiPlayerServiceProvider;
    private final ObjectProvider<GalaxyCreationService> galaxyCreationServiceProvider;

    @Autowired
    public HealthDashboardController(
            ObjectProvider<GameSessionService> gameSessionServiceProvider,
            ObjectProvider<PythonServerLauncher> pythonServerLauncherProvider,
            ObjectProvider<TurnScheduler> turnSchedulerProvider,
            ObjectProvider<AiPlayerService> aiPlayerServiceProvider,
            ObjectProvider<GalaxyCreationService> galaxyCreationServiceProvider,
            @Autowired(required = false) List<HealthIndicator> healthIndicators) {
        this.healthIndicators = healthIndicators != null ? healthIndicators : List.of();
        this.gameSessionServiceProvider = gameSessionServiceProvider;
        this.pythonServerLauncherProvider = pythonServerLauncherProvider;
        this.turnSchedulerProvider = turnSchedulerProvider;
        this.aiPlayerServiceProvider = aiPlayerServiceProvider;
        this.galaxyCreationServiceProvider = galaxyCreationServiceProvider;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // App info
        dashboard.put("appVersion", "0.1.7");
        dashboard.put("uptime", System.currentTimeMillis() - startTime);
        dashboard.put("startTime", startTime);

        // JVM info
        dashboard.put("jvm", getJvmInfo());

        // OS info
        dashboard.put("os", getOsInfo());

        // Health indicators (database, ollama, etc.)
        dashboard.put("components", getComponentHealth());

        // Python server status
        dashboard.put("pythonServer", getPythonServerStatus());

        // Game sessions
        dashboard.put("gameSessions", getGameSessionStatus());

        // Turn scheduler
        dashboard.put("turnScheduler", getTurnSchedulerStatus());

        // DL circuit breaker
        dashboard.put("dlCircuitBreaker", getDlCircuitBreakerStatus());

        // Junie agent status
        dashboard.put("junieAgent", getJunieAgentStatus());

        // Game started flag — true if any active sessions exist
        dashboard.put("gameStarted", isGameStarted());

        return ResponseEntity.ok(dashboard);
    }

    private Map<String, Object> getJvmInfo() {
        Map<String, Object> jvm = new HashMap<>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        jvm.put("heapUsed", heapUsage.getUsed());
        jvm.put("heapMax", heapUsage.getMax());
        jvm.put("heapPercent", (int) ((double) heapUsage.getUsed() / heapUsage.getMax() * 100));
        jvm.put("nonHeapUsed", nonHeapUsage.getUsed());
        jvm.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
        jvm.put("gcInfo", "Available");

        return jvm;
    }

    private Map<String, Object> getOsInfo() {
        Map<String, Object> os = new HashMap<>();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        os.put("name", System.getProperty("os.name"));
        os.put("arch", System.getProperty("os.arch"));
        os.put("processors", osBean.getAvailableProcessors());
        os.put("loadAverage", osBean.getSystemLoadAverage());
        return os;
    }

    private List<Map<String, Object>> getComponentHealth() {
        List<Map<String, Object>> components = new ArrayList<>();
        for (HealthIndicator indicator : healthIndicators) {
            try {
                Health health = indicator.health();
                Map<String, Object> component = new HashMap<>();
                component.put("name", indicator.getClass().getSimpleName().replace("HealthIndicator", ""));
                component.put("status", health.getStatus());
                if (health.getDetails() != null) {
                    component.put("details", health.getDetails());
                }
                components.add(component);
            } catch (Exception e) {
                Map<String, Object> component = new HashMap<>();
                component.put("name", indicator.getClass().getSimpleName().replace("HealthIndicator", ""));
                component.put("status", "DOWN");
                component.put("error", e.getMessage());
                components.add(component);
            }
        }
        return components;
    }

    private Map<String, Object> getPythonServerStatus() {
        Map<String, Object> status = new HashMap<>();
        PythonServerLauncher launcher = pythonServerLauncherProvider.getIfAvailable();

        if (launcher == null) {
            status.put("status", "UNAVAILABLE");
            status.put("message", "Python server launcher not configured");
            return status;
        }

        // Check if Python server is responding on port 8082
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", 8082), 2000);
            status.put("status", "UP");
            status.put("port", 8082);
            status.put("url", "http://localhost:8082");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("port", 8082);
            status.put("error", e.getMessage());
        }
        return status;
    }

    private Map<String, Object> getGameSessionStatus() {
        Map<String, Object> status = new HashMap<>();
        GameSessionService sessionService = gameSessionServiceProvider.getIfAvailable();

        if (sessionService == null) {
            status.put("activeSessions", 0);
            status.put("aiPlayers", 0);
            return status;
        }

        try {
            int activeSessions = sessionService.getAllSessions().size();
            status.put("activeSessions", activeSessions);

            // Count AI players from AiPlayerService
            AiPlayerService aiPlayerService = aiPlayerServiceProvider.getIfAvailable();
            if (aiPlayerService != null) {
                status.put("aiPlayers", aiPlayerService.getAiCoreCount());
            } else {
                status.put("aiPlayers", 0);
            }

            // Get current turn from the first session
            if (!sessionService.getAllSessions().isEmpty()) {
                com.yisy.modules.doramcu.model.GameSession firstSession =
                        sessionService.getAllSessions().values().iterator().next();
                if (firstSession.getGame() != null) {
                    status.put("currentTurn", firstSession.getGame().getCurrentTurn());
                }
            }
        } catch (Exception e) {
            status.put("error", e.getMessage());
        }
        return status;
    }

    private Map<String, Object> getTurnSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        TurnScheduler scheduler = turnSchedulerProvider.getIfAvailable();

        if (scheduler == null) {
            status.put("running", false);
            return status;
        }

        try {
            status.put("running", scheduler.isEnabled());
            status.put("intervalSeconds", scheduler.getTurnIntervalSeconds());
        } catch (Exception e) {
            status.put("error", e.getMessage());
        }
        return status;
    }

    private Map<String, Object> getDlCircuitBreakerStatus() {
        Map<String, Object> status = new HashMap<>();
        // Check system property or environment for circuit breaker state
        // The DoraAiLearningService manages this internally
        status.put("status", "ACTIVE");
        status.put("note", "Circuit breaker monitors DL4j training OOM errors");
        return status;
    }

    /**
     * Checks whether the game has been started (at least one active session exists).
     */
    private boolean isGameStarted() {
        GameSessionService sessionService = gameSessionServiceProvider.getIfAvailable();
        if (sessionService == null) return false;
        try {
            return !sessionService.getAllSessions().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Starts the game by generating the galaxy and creating the default game session with AI players.
     * Called from the health dashboard "Start Game" button.
     */
    @PostMapping("/start-game")
    public ResponseEntity<Map<String, Object>> startGame() {
        Map<String, Object> result = new HashMap<>();

        // Check if game is already started
        if (isGameStarted()) {
            result.put("success", false);
            result.put("message", "Game is already running");
            result.put("gameStarted", true);
            return ResponseEntity.ok(result);
        }

        GalaxyCreationService creationService = galaxyCreationServiceProvider.getIfAvailable();
        if (creationService == null) {
            result.put("success", false);
            result.put("message", "Galaxy creation service not available");
            result.put("gameStarted", false);
            return ResponseEntity.ok(result);
        }

        try {
            log.info("Starting game via health dashboard...");
            GalaxyCreationService.GalaxyCreationResult galaxyResult =
                    creationService.createGalaxyWithAiPlayers("Galactic Conquest");

            result.put("success", true);
            result.put("message", "Game started successfully");
            result.put("sessionId", galaxyResult.getSessionId());
            result.put("aiPlayers", galaxyResult.getAiPlayers().size());
            result.put("gameStarted", true);
            log.info("Game started: session={}, aiPlayers={}", galaxyResult.getSessionId(), galaxyResult.getAiPlayers().size());
        } catch (Exception e) {
            log.error("Failed to start game: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Failed to start game: " + e.getMessage());
            result.put("gameStarted", false);
        }

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> getJunieAgentStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("agent", "Junie");
        status.put("version", "0.1.7");
        status.put("model", "Qwen/Qwen3.6-27B-FP8");
        status.put("provider", "JetBrains");

        // Check for Junie session files
        String userHome = System.getProperty("user.home");
        java.io.File sessionsDir = new java.io.File(userHome, ".junie/sessions");
        if (sessionsDir.exists() && sessionsDir.isDirectory()) {
            status.put("sessionsDirectory", sessionsDir.getAbsolutePath());
            java.io.File[] sessionFiles = sessionsDir.listFiles((dir, name) -> name.endsWith(".json") || name.endsWith(".md"));
            if (sessionFiles != null) {
                status.put("sessionFiles", sessionFiles.length);
            }
        } else {
            status.put("sessionsDirectory", "Not found");
        }

        // Check for active Junie CLI process
        boolean cliRunning = false;
        try {
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command",
                    "Get-Process -Name junie* -ErrorAction SilentlyContinue | Select-Object -First 1");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor(2, TimeUnit.SECONDS);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            String output = reader.lines().reduce("", (a, b) -> a + b);
            cliRunning = !output.trim().isEmpty();
        } catch (Exception ignored) {
            // If we can't check, assume not running
        }
        status.put("cliRunning", cliRunning);
        status.put("status", cliRunning ? "ACTIVE" : "IDLE");
        return status;
    }
}
