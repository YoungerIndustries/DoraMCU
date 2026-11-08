package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.service.GalaxyProgressService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * REST controller for subscribing to galaxy generation progress.
 * <p>
 * Clients connect to the SSE endpoint before or during galaxy generation
 * to receive real-time progress updates. If the galaxy is already generated,
 * the client receives a "galaxy_ready" event immediately.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/4x/galaxy")
public class GalaxyProgressController {

    private final GalaxyProgressService galaxyProgressService;

    public GalaxyProgressController(GalaxyProgressService galaxyProgressService) {
        this.galaxyProgressService = galaxyProgressService;
    }

    /**
     * Subscribe to galaxy generation progress updates.
     *
     * @return SSE emitter streaming progress events
     */
    @GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        String clientId = UUID.randomUUID().toString();
        return galaxyProgressService.subscribe(clientId);
    }
}
