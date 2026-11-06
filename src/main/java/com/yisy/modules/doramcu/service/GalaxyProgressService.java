package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.GalaxyGenerationProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that broadcasts galaxy generation progress via SSE.
 * <p>
 * Clients subscribe to the SSE stream before galaxy generation starts.
 * The DataInitializer sets this service as the progress listener on
 * GalaxyService, so all generation callbacks are broadcast to connected clients.
 * </p>
 */
@Service
public class GalaxyProgressService implements GalaxyGenerationProgress {

    private static final Logger log = LoggerFactory.getLogger(GalaxyProgressService.class);
    private static final long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private volatile boolean galaxyReady = false;

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

    // ---- GalaxyGenerationProgress implementation ----

    @Override
    public void onPrepareStep(String stepDescription) {
        broadcastLog("Preparing: " + stepDescription);
    }

    @Override
    public void onStart(int totalSystems) {
        broadcast("start", Map.of("totalSystems", totalSystems));
        broadcastLog("Galaxy generation started. Target: " + totalSystems + " solar systems.");
    }

    @Override
    public void onSystemGenerated(int systemsGenerated, int totalSystems) {
        int progress = totalSystems > 0 ? (int) ((systemsGenerated * 100.0) / totalSystems) : 0;
        broadcast("progress", Map.of(
                "systemsGenerated", systemsGenerated,
                "totalSystems", totalSystems,
                "percent", progress
        ));
    }

    @Override
    public void onHyperlanesStart() {
        broadcastLog("Generating hyperlane network...");
    }

    @Override
    public void onComplete() {
        this.galaxyReady = true;
        broadcast("complete", Map.of("status", "complete", "message", "Galaxy generation complete"));
        broadcastLog("✅ Galaxy generation complete!");
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
        broadcastLog("Persisting galaxy data to database...");
    }

    @Override
    public void onPersistComplete() {
        broadcastLog("Galaxy data persisted successfully.");
    }

    @Override
    public void onReady() {
        broadcastLog("Ready to generate galaxy.");
    }
}
