package com.yisy.modules.doramcu.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HomeController Tests")
class HomeControllerTest {

    private final HomeController homeController = new HomeController();

    // ---- health endpoint ----

    @Test
    @DisplayName("health - should return status UP")
    void health_shouldReturnStatusUp() {
        Map<String, String> response = homeController.health();

        assertNotNull(response);
        assertEquals("UP", response.get("status"));
    }

    @Test
    @DisplayName("health - should return timestamp")
    void health_shouldReturnTimestamp() {
        Map<String, String> response = homeController.health();

        assertNotNull(response);
        assertNotNull(response.get("timestamp"));
        // Verify timestamp is parseable as LocalDateTime
        assertDoesNotThrow(() -> LocalDateTime.parse(response.get("timestamp")));
    }

    @Test
    @DisplayName("health - should return map with 2 entries")
    void health_shouldReturnTwoEntries() {
        Map<String, String> response = homeController.health();

        assertEquals(2, response.size());
        assertTrue(response.containsKey("status"));
        assertTrue(response.containsKey("timestamp"));
    }

    // ---- info endpoint ----

    @Test
    @DisplayName("info - should return application name")
    void info_shouldReturnApplicationName() {
        Map<String, String> response = homeController.info();

        assertNotNull(response);
        assertEquals("DoraMCU", response.get("application"));
    }

    @Test
    @DisplayName("info - should return version")
    void info_shouldReturnVersion() {
        Map<String, String> response = homeController.info();

        assertEquals("0.0.1-SNAPSHOT", response.get("version"));
    }

    @Test
    @DisplayName("info - should return timestamp")
    void info_shouldReturnTimestamp() {
        Map<String, String> response = homeController.info();

        assertNotNull(response.get("timestamp"));
        assertDoesNotThrow(() -> LocalDateTime.parse(response.get("timestamp")));
    }

    @Test
    @DisplayName("info - should return map with 3 entries")
    void info_shouldReturnThreeEntries() {
        Map<String, String> response = homeController.info();

        assertEquals(3, response.size());
        assertTrue(response.containsKey("application"));
        assertTrue(response.containsKey("version"));
        assertTrue(response.containsKey("timestamp"));
    }
}
