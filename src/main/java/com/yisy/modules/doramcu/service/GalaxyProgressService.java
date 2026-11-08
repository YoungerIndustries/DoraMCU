package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.GalaxyGenerationProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service that broadcasts galaxy generation progress via SSE.
 * <p>
 * Clients subscribe to the SSE stream before galaxy generation starts.
 * The DataInitializer sets this service as the progress listener on
 * GalaxyService, so all generation callbacks are broadcast to connected clients.
 * Progress summaries are emitted every 30 seconds to avoid flooding the stream.
 * </p>
 */
@Service
public class GalaxyProgressService implements GalaxyGenerationProgress {

    private static final Logger log = LoggerFactory.getLogger(GalaxyProgressService.class);
    private static final long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    private static final long PROGRESS_INTERVAL_MS = 30_000L; // 30 seconds

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private volatile boolean galaxyReady = false;

    // Progress tracking state
    private volatile int systemsGenerated = 0;
    private volatile int totalSystems = 0;
    private volatile String currentPhase = "idle";
    private volatile Instant lastProgressBroadcast = Instant.now();

    // Scheduled executor for periodic progress summaries
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "galaxy-progress-scheduler");
        t.setDaemon(true);
        return t;
    });
    private volatile ScheduledFuture<?> progressTimer = null;

    /**
     * Registers a new SSE emitter for a client.
     *
     * @param clientId unique client identifier
     * @return the registered SseEmitter
     */
    public SseEmitter subscribe(String clientId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE stream completed for client: {}", clientId);
            emitters.remove(clientId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE stream timed out for client: {}", clientId);
            emitters.remove(clientId);
        });
        emitter.onError((e) -> {
            log.warn("SSE stream error for client: {}", clientId, e);
            emitters.remove(clientId);
        });

        // If galaxy is already ready, immediately notify the client
        if (galaxyReady) {
            try {
                emitter.send(SseEmitter.event()
                        .name("galaxy_ready")
                        .data(Map.of("status", "ready", "message", "Galaxy already generated")));
                emitter.complete();
            } catch (IOException e) {
                log.warn("Failed to send galaxy_ready event", e);
            }
        }

        return emitter;
    }

    /**
     * Sends an SSE event to all connected clients.
     */
    private void broadcast(String eventName, Object data) {
        List<String> failedClients = new ArrayList<>();
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                log.warn("Failed to send event to client: {}", clientId);
                failedClients.add(clientId);
            }
        });
        // Remove failed clients
        failedClients.forEach(emitters::remove);
    }

    /**
     * Sends a simple text message event to all connected clients.
     */
    private void broadcastLog(String message) {
        broadcast("log", Map.of("message", message));
    }

    /**
     * Broadcasts a progress summary with current state.
     */
    private void broadcastProgressSummary() {
        int progress = totalSystems > 0 ? (int) ((systemsGenerated * 100.0) / totalSystems) : 0;
        long elapsedSeconds = Instant.now().getEpochSecond() - lastProgressBroadcast.getEpochSecond();

        Map<String, Object> data = Map.of(
                "phase", currentPhase,
                "systemsGenerated", systemsGenerated,
                "totalSystems", totalSystems,
                "percent", progress,
                "elapsedSeconds", elapsedSeconds
        );
        broadcast("progress_summary", data);
        log.info("Galaxy generation progress: {}% ({}/{} systems) [{}]", progress, systemsGenerated, totalSystems, currentPhase);
    }

    /**
     * Starts the periodic progress summary timer.
     */
    private void startProgressTimer() {
        stopProgressTimer();
        lastProgressBroadcast = Instant.now();
        progressTimer = scheduler.scheduleAtFixedRate(
                this::broadcastProgressSummary,
                PROGRESS_INTERVAL_MS,
                PROGRESS_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Stops the periodic progress summary timer.
     */
    private void stopProgressTimer() {
        if (progressTimer != null && !progressTimer.isDone()) {
            progressTimer.cancel(false);
        }
    }

    /**
     * Resets the progress service state for a new galaxy generation cycle.
     * <p>
     * Call this before starting a new galaxy rebuild to clear the completed state
     * and allow new SSE emitters to receive progress events.
     * </p>
     */
    public void reset() {
        this.galaxyReady = false;
        this.systemsGenerated = 0;
        this.totalSystems = 0;
        this.currentPhase = "idle";
        this.lastProgressBroadcast = Instant.now();
        stopProgressTimer();
        // Complete any remaining emitters from previous runs
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.complete();
            } catch (Exception e) {
                // Ignore - already completed
            }
        });
        emitters.clear();
        log.info("Galaxy progress service reset for new generation cycle");
    }

    // ---- GalaxyGenerationProgress implementation ----

    @Override
    public void onPrepareStep(String stepDescription) {
        this.currentPhase = "preparing";
        broadcastLog("Preparing: " + stepDescription);
    }

    @Override
    public void onStart(int totalSystems) {
        this.totalSystems = totalSystems;
        this.systemsGenerated = 0;
        this.currentPhase = "generating_systems";
        this.lastProgressBroadcast = Instant.now();
        broadcast("start", Map.of("totalSystems", totalSystems));
        broadcastLog("Galaxy generation started. Target: " + totalSystems + " solar systems.");
        // Start periodic progress summaries
        startProgressTimer();
    }

    @Override
    public void onSystemGenerated(int systemsGenerated, int totalSystems) {
        this.systemsGenerated = systemsGenerated;
        this.totalSystems = totalSystems;
        // Individual system progress is aggregated and broadcast every 30 seconds
    }

    @Override
    public void onHyperlanesStart() {
        this.currentPhase = "generating_hyperlanes";
        broadcastLog("Generating hyperlane network...");
    }

    @Override
    public void onComplete() {
        this.currentPhase = "complete";
        // Don't set galaxyReady here — let the admin controller signal final completion
        // so that multi-galaxy rebuilds can continue broadcasting progress
        stopProgressTimer();

        // Final progress summary
        broadcastProgressSummary();

        broadcast("complete", Map.of("status", "complete", "message", "Galaxy generation complete"));
        broadcastLog("✅ Galaxy generation complete!");
        // Don't complete emitters here — they may be needed for the next galaxy in a rebuild batch
    }

    /**
     * Signals that all galaxy generation is finished and completes all SSE emitters.
     * <p>
     * Call this after all galaxies in a rebuild batch are complete.
     * </p>
     */
    public void finishAll() {
        this.galaxyReady = true;
        // Complete all emitters after a short delay
        emitters.forEach((clientId, emitter) -> {
            try {
                Thread.sleep(2000);
                emitter.complete();
            } catch (InterruptedException e) {
                // Ignore
            }
        });
    }

    @Override
    public void onLog(String message) {
        broadcastLog(message);
    }

    @Override
    public void onSystemStep(int systemIndex, String stepDescription) {
        // Skip individual system steps to avoid flooding the SSE stream
    }

    @Override
    public void onPersistStart() {
        this.currentPhase = "persisting_to_database";
        broadcastLog("Persisting galaxy data to database...");
    }

    @Override
    public void onPersistComplete() {
        this.currentPhase = "persisted";
        broadcastLog("Galaxy data persisted successfully.");
    }

    @Override
    public void onReady() {
        this.currentPhase = "ready";
        broadcastLog("Ready to generate galaxy.");
    }
}
