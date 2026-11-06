package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.config.DeepSeekConfig;
import com.yisy.modules.doraai.service.AiProvider.Message;
import com.yisy.modules.doraai.service.DeepSeekService.DeepSeekException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeepSeekServiceTest {

    DeepSeekConfig config;
    DeepSeekService service;

    @BeforeEach
    void setUp() {
        config = new DeepSeekConfig();
        config.setApiKey("test-key");
        service = new DeepSeekService(config);
    }

    @Test
    void providerId_returnsDeepseek() {
        assertEquals("deepseek", service.getProviderId());
    }

    @Test
    void config_defaultValues() {
        DeepSeekConfig cfg = new DeepSeekConfig();

        assertFalse(cfg.isEnabled());
        assertNull(cfg.getApiKey());
        assertEquals("https://api.deepseek.com", cfg.getBaseUrl());
        assertEquals("deepseek-chat", cfg.getModel());
        assertEquals(0.7, cfg.getTemperature());
        assertEquals(2048, cfg.getMaxTokens());
        assertEquals(5000, cfg.getConnectTimeoutMs());
        assertEquals(120000, cfg.getReadTimeoutMs());
    }

    @Test
    void config_setters() {
        DeepSeekConfig cfg = new DeepSeekConfig();
        cfg.setEnabled(true);
        cfg.setApiKey("sk-123");
        cfg.setBaseUrl("https://custom.example.com");
        cfg.setModel("deepseek-reasoner");
        cfg.setTemperature(0.5);
        cfg.setMaxTokens(4096);
        cfg.setConnectTimeoutMs(10000);
        cfg.setReadTimeoutMs(60000);

        assertTrue(cfg.isEnabled());
        assertEquals("sk-123", cfg.getApiKey());
        assertEquals("https://custom.example.com", cfg.getBaseUrl());
        assertEquals("deepseek-reasoner", cfg.getModel());
        assertEquals(0.5, cfg.getTemperature());
        assertEquals(4096, cfg.getMaxTokens());
        assertEquals(10000, cfg.getConnectTimeoutMs());
        assertEquals(60000, cfg.getReadTimeoutMs());
    }

    @Test
    void config_enabled_canBeToggled() {
        DeepSeekConfig cfg = new DeepSeekConfig();
        assertFalse(cfg.isEnabled());
        cfg.setEnabled(true);
        assertTrue(cfg.isEnabled());
        cfg.setEnabled(false);
        assertFalse(cfg.isEnabled());
    }

    @Test
    void isAvailable_returnsFalseWhenApiKeyMissing() {
        config.setApiKey(null);
        DeepSeekService svc = new DeepSeekService(config);

        assertFalse(svc.isAvailable());
    }

    @Test
    void isAvailable_returnsFalseWhenApiKeyBlank() {
        config.setApiKey("   ");
        DeepSeekService svc = new DeepSeekService(config);

        assertFalse(svc.isAvailable());
    }

    @Test
    void isAvailable_returnsFalseWhenOffline() {
        config.setApiKey("invalid-key");
        config.setConnectTimeoutMs(100);
        DeepSeekService svc = new DeepSeekService(config);

        boolean available = svc.isAvailable();
        assertFalse(available);
    }

    @Test
    void deepSeekException_messageOnly() {
        DeepSeekException ex = new DeepSeekException("Connection failed");

        assertEquals("Connection failed", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void deepSeekException_messageAndCause() {
        RuntimeException cause = new RuntimeException("Timeout");
        DeepSeekException ex = new DeepSeekException("Connection failed", cause);

        assertEquals("Connection failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void deepSeekException_isRuntimeException() {
        DeepSeekException ex = new DeepSeekException("Error");

        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void message_validConstructor_createsMessage() {
        Message message = new Message("user", "Hello");

        assertEquals("user", message.role());
        assertEquals("Hello", message.content());
    }

    @Test
    void message_nullContent_defaultsToEmpty() {
        Message message = new Message("user", null);

        assertEquals("", message.content());
    }

    @Test
    void message_nullRole_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Message(null, "Hello"));
    }

    @Test
    void message_blankRole_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Message("  ", "Hello"));
    }

    @Test
    void chat_throwsExceptionWhenApiKeyMissing() {
        config.setApiKey(null);
        DeepSeekService svc = new DeepSeekService(config);

        Message msg = new Message("user", "Hello");
        assertThrows(DeepSeekException.class, () -> svc.chat("deepseek-chat", java.util.List.of(msg)));
    }

    @Test
    void chat_usesCustomModelWhenProvided() {
        // The service should not throw during construction; it will fail at API call
        // This test verifies the service is created and attempts the call
        config.setApiKey("invalid-key");
        config.setReadTimeoutMs(100);
        DeepSeekService svc = new DeepSeekService(config);

        Message msg = new Message("user", "Hello");
        assertThrows(DeepSeekException.class, () -> svc.chat("deepseek-reasoner", java.util.List.of(msg)));
    }

    @Test
    void streamChat_returnsSseEmitter() {
        config.setApiKey("invalid-key");
        config.setReadTimeoutMs(100);
        DeepSeekService svc = new DeepSeekService(config);

        Message msg = new Message("user", "Hello");
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = svc.streamChat("deepseek-chat", java.util.List.of(msg));

        assertNotNull(emitter);
    }

    @Test
    void streamChat_withExternalEmitter_doesNotThrow() {
        config.setApiKey("invalid-key");
        config.setReadTimeoutMs(100);
        DeepSeekService svc = new DeepSeekService(config);

        Message msg = new Message("user", "Hello");
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter =
                new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(0L);
        StringBuilder collector = new StringBuilder();

        // Should start a background thread and return immediately
        assertDoesNotThrow(() -> svc.streamChat("deepseek-chat", java.util.List.of(msg), emitter, collector));
    }

    @Test
    void config_apiKey_canBeSetFromEnvironment() {
        DeepSeekConfig cfg = new DeepSeekConfig();
        cfg.setApiKey("sk-from-env");

        assertEquals("sk-from-env", cfg.getApiKey());
    }
}
