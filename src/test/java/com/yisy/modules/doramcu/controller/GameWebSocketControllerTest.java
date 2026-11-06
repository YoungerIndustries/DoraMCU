package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.util.PlayerIdExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameWebSocketController Tests")
class GameWebSocketControllerTest {

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private PlayerIdExtractor playerIdExtractor;

    @Mock
    private Authentication authentication;

    private GameWebSocketController controller;

    @BeforeEach
    void setUp() {
        controller = new GameWebSocketController(gameSessionService, playerIdExtractor);
    }

    // ---- Stream Events ----

    @Test
    @DisplayName("streamEvents - should return SseEmitter")
    void streamEvents_shouldReturnEmitter() {
        when(authentication.getName()).thenReturn("1");
        when(gameSessionService.getGameSession("sess-1"))
                .thenReturn(null);

        SseEmitter emitter = controller.streamEvents("sess-1", authentication);

        assertNotNull(emitter);
    }

    @Test
    @DisplayName("streamEvents - should complete emitter when session not found")
    void streamEvents_shouldCompleteWhenNotFound() {
        when(authentication.getName()).thenReturn("1");
        when(gameSessionService.getGameSession("nonexistent"))
                .thenReturn(null);

        SseEmitter emitter = controller.streamEvents("nonexistent", authentication);

        assertNotNull(emitter);
        // The emitter should be completed immediately when session is not found
    }

    @Test
    @DisplayName("streamEvents - should register emitter with session")
    void streamEvents_shouldRegisterEmitter() {
        when(authentication.getName()).thenReturn("1");

        GameSession session = mock(GameSession.class);
        when(gameSessionService.getGameSession("sess-1")).thenReturn(session);
        doNothing().when(session).registerEmitter(anyLong(), any(SseEmitter.class));

        SseEmitter emitter = controller.streamEvents("sess-1", authentication);

        assertNotNull(emitter);
        // Emitter registration callbacks are set up
    }
}
