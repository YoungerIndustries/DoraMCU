package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.config.OllamaConfig;
import com.yisy.modules.doraai.service.AiProvider.Message;
import com.yisy.modules.doraai.service.OllamaService.OllamaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OllamaServiceTest {

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
    void message_emptyRole_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Message("", "Hello"));
    }

    @Test
    void message_systemRole_works() {
        Message message = new Message("system", "You are a helpful assistant");

        assertEquals("system", message.role());
        assertEquals("You are a helpful assistant", message.content());
    }

    @Test
    void message_assistantRole_works() {
        Message message = new Message("assistant", "Here is the answer");

        assertEquals("assistant", message.role());
        assertEquals("Here is the answer", message.content());
    }

    @Test
    void ollamaException_messageOnly() {
        OllamaException ex = new OllamaException("Connection failed");

        assertEquals("Connection failed", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void ollamaException_messageAndCause() {
        RuntimeException cause = new RuntimeException("Timeout");
        OllamaException ex = new OllamaException("Connection failed", cause);

        assertEquals("Connection failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void ollamaException_isRuntimeException() {
        OllamaException ex = new OllamaException("Error");

        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void ollamaConfig_defaultValues() {
        OllamaConfig config = new OllamaConfig();

        assertEquals("http://localhost:11434", config.getBaseUrl());
        assertEquals("llama3", config.getModel());
        assertEquals(0.7, config.getTemperature());
        assertEquals(2048, config.getMaxTokens());
        assertEquals(5000, config.getConnectTimeoutMs());
        assertEquals(120000, config.getReadTimeoutMs());
    }

    @Test
    void ollamaConfig_setters() {
        OllamaConfig config = new OllamaConfig();
        config.setBaseUrl("http://custom:11434");
        config.setModel("codellama");
        config.setTemperature(0.5);
        config.setMaxTokens(4096);
        config.setConnectTimeoutMs(10000);
        config.setReadTimeoutMs(60000);

        assertEquals("http://custom:11434", config.getBaseUrl());
        assertEquals("codellama", config.getModel());
        assertEquals(0.5, config.getTemperature());
        assertEquals(4096, config.getMaxTokens());
        assertEquals(10000, config.getConnectTimeoutMs());
        assertEquals(60000, config.getReadTimeoutMs());
    }

    @Test
    void ollamaService_isAvailable_returnsFalseWhenOffline() {
        OllamaConfig config = new OllamaConfig();
        config.setBaseUrl("http://localhost:9999"); // Non-existent port
        config.setConnectTimeoutMs(100);

        OllamaService service = new OllamaService(config);

        // Should return false, not throw
        boolean available = service.isAvailable();
        assertFalse(available);
    }

    @Test
    void transcriptionResult_validConstructor() {
        OllamaService.TranscriptionResult result = new OllamaService.TranscriptionResult(
                "Hello World", "en", 0.95);

        assertEquals("Hello World", result.text());
        assertEquals("en", result.language());
        assertEquals(0.95, result.confidence());
    }

    @Test
    void transcriptionResult_emptyText() {
        OllamaService.TranscriptionResult result = new OllamaService.TranscriptionResult(
                "", "zh", 0.8);

        assertEquals("", result.text());
        assertEquals("zh", result.language());
        assertEquals(0.8, result.confidence());
    }
}
