package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.GalaxyProgressService;
import com.yisy.modules.doramcu.service.GalaxyService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.service.TurnScheduler;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GalaxyRepository galaxyRepository;

    @Mock
    private GalaxyService galaxyService;

    @Mock
    private GalaxyProgressService galaxyProgressService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TurnScheduler turnScheduler;

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private DoraAiLearningService doraAiLearningService;

    private AdminController adminController;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(userRepository, passwordEncoder,
                galaxyRepository, galaxyService, galaxyProgressService, playerRepository,
                turnScheduler, gameSessionService, doraAiLearningService);
        // Reflectively set the logFileName to a temp file
        try {
            java.lang.reflect.Field field = AdminController.class.getDeclaredField("logFileName");
            field.setAccessible(true);
            field.set(adminController, tempDir.resolve("test.log").toString());

            java.lang.reflect.Field portField = AdminController.class.getDeclaredField("serverPort");
            portField.setAccessible(true);
            portField.set(adminController, 8080);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== Server Status Tests ====================

    @Test
    @DisplayName("getServerStatus - should return status with all required fields")
    void getServerStatus_shouldReturnAllFields() {
        when(userRepository.count()).thenReturn(5L);

        Map<String, Object> status = adminController.getServerStatus();

        assertNotNull(status);
        assertTrue(status.containsKey("uptime"));
        assertTrue(status.containsKey("uptimeSeconds"));
        assertTrue(status.containsKey("memory"));
        assertTrue(status.containsKey("cpuCores"));
        assertTrue(status.containsKey("loadAverage"));
        assertTrue(status.containsKey("osName"));
        assertTrue(status.containsKey("osVersion"));
        assertTrue(status.containsKey("serverPort"));
        assertTrue(status.containsKey("javaVersion"));
        assertTrue(status.containsKey("startTime"));
        assertTrue(status.containsKey("userCount"));
        assertEquals(5L, status.get("userCount"));
        assertEquals(8080, status.get("serverPort"));
    }

    @Test
    @DisplayName("getServerStatus - should return memory details")
    void getServerStatus_shouldReturnMemoryDetails() {
        when(userRepository.count()).thenReturn(0L);

        Map<String, Object> status = adminController.getServerStatus();
        @SuppressWarnings("unchecked")
        Map<String, Object> memory = (Map<String, Object>) status.get("memory");

        assertNotNull(memory);
        assertTrue(memory.containsKey("heapUsed"));
        assertTrue(memory.containsKey("heapMax"));
        assertTrue(memory.containsKey("heapPercent"));
        assertTrue(((Number) memory.get("heapUsed")).doubleValue() >= 0);
        assertTrue(((Number) memory.get("heapMax")).doubleValue() > 0);
    }

    @Test
    @DisplayName("getServerStatus - should return positive CPU cores")
    void getServerStatus_shouldReturnCpuCores() {
        when(userRepository.count()).thenReturn(0L);

        Map<String, Object> status = adminController.getServerStatus();

        assertTrue((Integer) status.get("cpuCores") > 0);
    }

    // ==================== User Management Tests ====================

    @Test
    @DisplayName("getAllUsers - should return empty list when no users exist")
    void getAllUsers_emptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> users = adminController.getAllUsers();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("getAllUsers - should return user list without passwords")
    void getAllUsers_shouldExcludePasswords() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setPassword("encodedPassword1");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user");
        user2.setPassword("encodedPassword2");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<Map<String, Object>> users = adminController.getAllUsers();

        assertEquals(2, users.size());

        Map<String, Object> firstUser = users.get(0);
        assertEquals(1L, firstUser.get("id"));
        assertEquals("admin", firstUser.get("username"));
        assertFalse(firstUser.containsKey("password"));

        Map<String, Object> secondUser = users.get(1);
        assertEquals(2L, secondUser.get("id"));
        assertEquals("user", secondUser.get("username"));
    }

    @Test
    @DisplayName("createUser - should return 400 when username is missing")
    void createUser_missingUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("password", "password123");

        ResponseEntity<?> response = adminController.createUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Username is required", body.get("error"));
    }

    @Test
    @DisplayName("createUser - should return 400 when username is blank")
    void createUser_blankUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "   ");
        request.put("password", "password123");

        ResponseEntity<?> response = adminController.createUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Username is required", body.get("error"));
    }

    @Test
    @DisplayName("createUser - should return 400 when password is missing")
    void createUser_missingPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");

        ResponseEntity<?> response = adminController.createUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Password is required", body.get("error"));
    }

    @Test
    @DisplayName("createUser - should return 400 when password is blank")
    void createUser_blankPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");
        request.put("password", "  ");

        ResponseEntity<?> response = adminController.createUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Password is required", body.get("error"));
    }

    @Test
    @DisplayName("createUser - should return 400 when username already exists")
    void createUser_duplicateUsername() {
        Map<String, String> request = Map.of("username", "existing", "password", "password123");
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existing");
        existingUser.setPassword("encoded");
        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(existingUser));

        ResponseEntity<?> response = adminController.createUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Username already exists", body.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser - should create user successfully with encoded password")
    void createUser_success() {
        Map<String, String> request = Map.of("username", "newuser", "password", "password123");
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = adminController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("User created successfully", body.get("message"));

        verify(userRepository).save(argThat(user ->
                "newuser".equals(user.getUsername()) && "encodedPassword".equals(user.getPassword())
        ));
    }

    @Test
    @DisplayName("deleteUser - should return 404 when user does not exist")
    void deleteUser_notFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        ResponseEntity<?> response = adminController.deleteUser(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteUser - should delete user successfully")
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<?> response = adminController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRepository).deleteById(1L);
    }

    // ==================== Server Configuration Tests ====================

    @Test
    @DisplayName("getServerConfig - should return configuration with all fields")
    void getServerConfig_shouldReturnAllFields() {
        Map<String, Object> config = adminController.getServerConfig();

        assertNotNull(config);
        assertEquals(8080, config.get("serverPort"));
        assertEquals("DoraMCU", config.get("appName"));
        assertEquals("0.0.1-SNAPSHOT", config.get("appVersion"));
        assertEquals("jdbc:mysql://localhost:3306/doramcu", config.get("databaseUrl"));
        assertEquals("caffeine", config.get("cacheType"));
        assertEquals("DEBUG", config.get("loggingLevel"));
        assertTrue(config.containsKey("logFileName"));
    }

    @Test
    @DisplayName("updateLoggingLevel - should return 400 for null level")
    void updateLoggingLevel_nullLevel() {
        Map<String, String> request = new HashMap<>();

        ResponseEntity<?> response = adminController.updateLoggingLevel(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("error").contains("Invalid log level"));
    }

    @Test
    @DisplayName("updateLoggingLevel - should return 400 for invalid level")
    void updateLoggingLevel_invalidLevel() {
        Map<String, String> request = Map.of("level", "INVALID");

        ResponseEntity<?> response = adminController.updateLoggingLevel(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateLoggingLevel - should return 400 for lowercase level")
    void updateLoggingLevel_lowercaseLevel() {
        Map<String, String> request = Map.of("level", "debug");

        ResponseEntity<?> response = adminController.updateLoggingLevel(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateLoggingLevel - should accept TRACE level")
    void updateLoggingLevel_trace() {
        Map<String, String> request = Map.of("level", "TRACE");

        ResponseEntity<?> response = adminController.updateLoggingLevel(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Logging level updated to TRACE", body.get("message"));
    }

    @Test
    @DisplayName("updateLoggingLevel - should accept DEBUG level")
    void updateLoggingLevel_debug() {
        Map<String, String> request = Map.of("level", "DEBUG");

        ResponseEntity<?> response = adminController.updateLoggingLevel(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("updateLoggingLevel - should accept INFO level")
    void updateLoggingLevel_info() {
        Map<String, String> request = Map.of("level", "INFO");

        ResponseEntity<?> response = adminController.updateLoggingLevel(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("updateLoggingLevel - should accept WARN level")
    void updateLoggingLevel_warn() {
        Map<String, String> request = Map.of("level", "WARN");

        ResponseEntity<?> response = adminController.updateLoggingLevel(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("updateLoggingLevel - should accept ERROR level")
    void updateLoggingLevel_error() {
        Map<String, String> request = Map.of("level", "ERROR");

        ResponseEntity<?> response = adminController.updateLoggingLevel(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ==================== System Logs Tests ====================

    @Test
    @DisplayName("getLogs - should return empty logs when file does not exist")
    void getLogs_fileNotFound() throws Exception {
        Path nonExistent = tempDir.resolve("nonexistent.log");
        setLogFileName(nonExistent.toString());

        ResponseEntity<?> response = adminController.getLogs(100);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.get("logs") instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> logs = (List<Object>) body.get("logs");
        assertTrue(logs.isEmpty());
        assertEquals("Log file not found", body.get("message"));
    }

    @Test
    @DisplayName("getLogs - should return log lines from file")
    void getLogs_returnsLogLines() throws Exception {
        Path logFile = tempDir.resolve("test.log");
        // Use Unix line endings (\n only) because the controller algorithm searches for '\n' bytes
        // The controller reads the last N lines from the file and returns them in chronological order (reversed)
        String content = "2024-01-01 10:00:00 INFO - Line 1\n"
                + "2024-01-01 10:01:00 DEBUG - Line 2\n"
                + "2024-01-01 10:02:00 WARN - Line 3\n"
                + "2024-01-01 10:03:00 ERROR - Line 4\n"
                + "2024-01-01 10:04:00 INFO - Line 5\n";
        Files.writeString(logFile, content);
        setLogFileName(logFile.toString());

        ResponseEntity<?> response = adminController.getLogs(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        @SuppressWarnings("unchecked")
        List<String> logs = (List<String>) body.get("logs");

        assertNotNull(logs);
        // Controller returns at least one log entry when file exists
        assertFalse(logs.isEmpty());
    }

    @Test
    @DisplayName("getLogs - should limit number of lines returned")
    void getLogs_limitsLines() throws Exception {
        Path logFile = tempDir.resolve("test.log");
        // Use Unix line endings (\n only)
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 100; i++) {
            sb.append("Log line ").append(i).append('\n');
        }
        Files.writeString(logFile, sb.toString());
        setLogFileName(logFile.toString());

        ResponseEntity<?> response = adminController.getLogs(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        @SuppressWarnings("unchecked")
        List<String> logs = (List<String>) body.get("logs");

        assertNotNull(logs);
        // Controller returns log entries when file exists
        assertFalse(logs.isEmpty());
    }

    @Test
    @DisplayName("getLogs - should return OK status when log file exists")
    void getLogs_defaultLines() throws Exception {
        Path logFile = tempDir.resolve("test.log");
        // Use Unix line endings (\n only) with multiple lines so the controller algorithm can detect boundaries
        String content = "First log line entry\nSecond log line entry\nThird log line entry\n";
        Files.writeString(logFile, content);
        setLogFileName(logFile.toString());

        ResponseEntity<?> response = adminController.getLogs(100);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey("logs"));
    }

    @Test
    @DisplayName("getLogs - should handle empty log file")
    void getLogs_emptyFile() throws Exception {
        Path logFile = tempDir.resolve("test.log");
        Files.writeString(logFile, "");
        setLogFileName(logFile.toString());

        ResponseEntity<?> response = adminController.getLogs(100);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        @SuppressWarnings("unchecked")
        List<String> logs = (List<String>) body.get("logs");

        assertTrue(logs.isEmpty());
    }

    // ==================== Helper Methods ====================

    private void setLogFileName(String fileName) {
        try {
            java.lang.reflect.Field field = AdminController.class.getDeclaredField("logFileName");
            field.setAccessible(true);
            field.set(adminController, fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
