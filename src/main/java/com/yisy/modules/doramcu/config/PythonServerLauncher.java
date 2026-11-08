package com.yisy.modules.doramcu.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Auto-starts the DoraAI OpenHands Python server on application startup.
 * <p>
 * The Python server runs on port 8082 and provides the OpenHands agent
 * capabilities (code, fast_code, advanced_chat, run_verify, setup modes).
 * This launcher finds the Python server script, starts it as a child process,
 * and stops it when the application shuts down.
 * </p>
 *
 * Enabled by default when the 'dev' profile is active. Can be disabled with:
 * openhands.server.auto-start=false
 */
@Component
@ConditionalOnProperty(name = "openhands.server.auto-start", havingValue = "true", matchIfMissing = true)
public class PythonServerLauncher {

    private static final Logger log = LoggerFactory.getLogger(PythonServerLauncher.class);

    private final Environment environment;
    private Process pythonProcess;

    public PythonServerLauncher(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void startPythonServer() {
        // Only auto-start in dev profile
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDev = activeProfiles != null && java.util.Arrays.asList(activeProfiles).contains("dev");
        if (!isDev) {
            log.info("Skipping Python server auto-start (not in dev profile)");
            return;
        }

        // Check if already running
        if (isPythonServerRunning()) {
            log.info("Python server is already running on port 8082, skipping auto-start");
            return;
        }

        // Find the dora-ai-openhands directory
        Path openHandsDir = findOpenHandsDirectory();
        if (openHandsDir == null) {
            log.warn("Could not find dora-ai-openhands directory. " +
                    "OpenHands mode will be unavailable. To enable, ensure the dora-ai-openhands module is present.");
            return;
        }

        // Try 'uv run' first (handles Python path + virtual env + dependencies), then fall back to bare 'python'
        boolean useUv = isCommandAvailable("uv");
        String runnerCmd;
        if (useUv) {
            runnerCmd = "uv";
        } else {
            String pythonCmd = findPythonExecutable();
            if (pythonCmd == null) {
                log.warn("Python executable not found. OpenHands mode will be unavailable. " +
                        "Install Python 3.13+ and 'uv', or set PYTHON_HOME environment variable.");
                return;
            }
            runnerCmd = pythonCmd;
        }

        try {
            ProcessBuilder pb;
            if (useUv) {
                // uv run python -m dora_ai_openhands.server
                pb = new ProcessBuilder("uv", "run", "python", "-m", "dora_ai_openhands.server");
                pb.directory(openHandsDir.toFile());
                log.info("Starting Python server: uv run python -m dora_ai_openhands.server (in {})", openHandsDir);
            } else {
                // python -m dora_ai_openhands.server
                Path serverScript = findPythonServerScript();
                if (serverScript == null) {
                    log.warn("Could not find Python server script. OpenHands mode will be unavailable.");
                    return;
                }
                pb = new ProcessBuilder(runnerCmd, serverScript.toString());
                log.info("Starting Python server: {} {}", runnerCmd, serverScript);
            }

            pb.environment().put("DORA_SERVER_PORT", "8082");
            pb.environment().put("DORA_WORKSPACE", System.getProperty("user.dir"));
            pb.redirectErrorStream(true);

            pythonProcess = pb.start();

            // Log output in background
            Thread logThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("[PythonServer] {}", line);
                    }
                } catch (IOException e) {
                    log.warn("Error reading Python server output", e);
                }
            }, "python-server-log");
            logThread.setDaemon(true);
            logThread.start();

            // Wait for the server to become available
            log.info("Waiting for Python server to start on port 8082...");
            if (waitForServer(30, TimeUnit.SECONDS)) {
                log.info("Python server started successfully on port 8082");
            } else {
                log.warn("Python server did not respond within 30 seconds. " +
                        "It may still be starting up. OpenHands mode will attempt connection on demand.");
            }

        } catch (IOException e) {
            log.error("Failed to start Python server: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stopPythonServer() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            log.info("Stopping Python server...");
            pythonProcess.destroy();
            try {
                if (!pythonProcess.waitFor(10, TimeUnit.SECONDS)) {
                    log.warn("Python server did not stop gracefully, forcing...");
                    pythonProcess.destroyForcibly();
                }
                log.info("Python server stopped");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for Python server to stop");
            }
        }
    }

    /**
     * Check if the Python server is already running on port 8082.
     */
    private boolean isPythonServerRunning() {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", 8082), 2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Find the dora-ai-openhands directory (needed for 'uv run' working directory).
     */
    private Path findOpenHandsDirectory() {
        String[] candidates = {
                "dora-ai-openhands",
                "../dora-ai-openhands",
        };

        for (String candidate : candidates) {
            Path path = Paths.get(candidate).toAbsolutePath().normalize();
            if (Files.isDirectory(path)) {
                // Verify it has the expected structure
                Path srcDir = path.resolve("src").resolve("dora_ai_openhands");
                if (Files.isDirectory(srcDir)) {
                    return path;
                }
            }
        }

        return null;
    }

    /**
     * Find the Python server script in the project (fallback for non-uv mode).
     */
    private Path findPythonServerScript() {
        // Check common locations
        String[] candidates = {
                "dora-ai-openhands/src/dora_ai_openhands/server.py",
                "dora-ai-openhands/src/dora_ai_openhands/__main__.py",
                "../dora-ai-openhands/src/dora_ai_openhands/server.py",
        };

        for (String candidate : candidates) {
            Path path = Paths.get(candidate).toAbsolutePath().normalize();
            if (Files.isRegularFile(path)) {
                return path;
            }
        }

        return null;
    }

    /**
     * Find the Python executable.
     */
    private String findPythonExecutable() {
        // Check PYTHON_HOME
        String pythonHome = System.getenv("PYTHON_HOME");
        if (pythonHome != null && !pythonHome.isEmpty()) {
            String pythonPath = pythonHome + File.separator + "python.exe";
            if (Files.isRegularFile(Paths.get(pythonPath))) {
                return pythonPath;
            }
        }

        // Try common commands
        String[] commands = {"python", "python3", "py"};
        for (String cmd : commands) {
            if (isCommandAvailable(cmd)) {
                return cmd;
            }
        }

        return null;
    }

    /**
     * Check if a command is available in PATH.
     */
    private boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process p = pb.start();
            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            return finished && p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Wait for the Python server to become available.
     */
    private boolean waitForServer(long timeout, TimeUnit unit) {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadline) {
            if (isPythonServerRunning()) {
                return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}
