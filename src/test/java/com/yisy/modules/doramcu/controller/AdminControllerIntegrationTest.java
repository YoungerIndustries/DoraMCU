package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.yisy.modules.doramcu.DoraMcuApplication;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DoraMcuApplication.class)
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("logging.file.name", () -> System.getProperty("java.io.tmpdir") + "/doramcu-test.log");
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new org.springframework.security.web.FilterChainProxy(
                        context.getBean(org.springframework.security.web.SecurityFilterChain.class)))
                .build();

        // Create a test user and generate a valid JWT token
        userRepository.deleteAll();
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        userRepository.save(admin);

        adminToken = jwtService.generateToken("admin");
    }

    // ==================== Server Status Integration Tests ====================

    @Test
    @DisplayName("GET /api/v1/admin/server/status - should return 403 without token")
    void getServerStatus_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/server/status"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/server/status - should return 200 with valid token")
    void getServerStatus_authenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/server/status")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uptime").isString())
                .andExpect(jsonPath("$.uptimeSeconds").isNumber())
                .andExpect(jsonPath("$.memory").isMap())
                .andExpect(jsonPath("$.memory.heapUsed").isNumber())
                .andExpect(jsonPath("$.memory.heapMax").isNumber())
                .andExpect(jsonPath("$.memory.heapPercent").isString())
                .andExpect(jsonPath("$.cpuCores").isNumber())
                .andExpect(jsonPath("$.osName").isString())
                .andExpect(jsonPath("$.serverPort").isNumber())
                .andExpect(jsonPath("$.javaVersion").isString())
                .andExpect(jsonPath("$.startTime").isString())
                .andExpect(jsonPath("$.userCount").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/admin/server/status - should return correct user count")
    void getServerStatus_userCount() throws Exception {
        // Add another user
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword(passwordEncoder.encode("pass"));
        userRepository.save(user2);

        mockMvc.perform(get("/api/v1/admin/server/status")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCount").value(2));
    }

    // ==================== User Management Integration Tests ====================

    @Test
    @DisplayName("GET /api/v1/admin/users - should return 403 without token")
    void getAllUsers_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/users - should return user list without passwords")
    void getAllUsers_authenticated() throws Exception {
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword(passwordEncoder.encode("pass"));
        userRepository.save(user2);

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].username").isString())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/admin/users - should create user successfully")
    void createUser_success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"newpass123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"));

        // Verify user was actually saved
        assertTrue(userRepository.findByUsername("newuser").isPresent());
    }

    @Test
    @DisplayName("POST /api/v1/admin/users - should return 400 for missing username")
    void createUser_missingUsername() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"newpass123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is required"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/users - should return 400 for blank username")
    void createUser_blankUsername() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"   \",\"password\":\"newpass123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is required"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/users - should return 400 for missing password")
    void createUser_missingPassword() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password is required"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/users - should return 400 for duplicate username")
    void createUser_duplicateUsername() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"newpass123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/users/{id} - should delete user successfully")
    void deleteUser_success() throws Exception {
        User userToDelete = new User();
        userToDelete.setUsername("todelete");
        userToDelete.setPassword(passwordEncoder.encode("pass"));
        userToDelete = userRepository.save(userToDelete);

        mockMvc.perform(delete("/api/v1/admin/users/{id}", userToDelete.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(userToDelete.getId()));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/users/{id} - should return 404 for non-existent user")
    void deleteUser_notFound() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/{id}", 999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ==================== Server Configuration Integration Tests ====================

    @Test
    @DisplayName("GET /api/v1/admin/config - should return server configuration")
    void getServerConfig_authenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/config")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serverPort").isNumber())
                .andExpect(jsonPath("$.appName").value("DoraMCU"))
                .andExpect(jsonPath("$.appVersion").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.databaseUrl").isString())
                .andExpect(jsonPath("$.cacheType").value("caffeine"))
                .andExpect(jsonPath("$.loggingLevel").isString())
                .andExpect(jsonPath("$.logFileName").isString());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/config/logging-level - should update logging level")
    void updateLoggingLevel_success() throws Exception {
        for (String level : new String[]{"TRACE", "DEBUG", "INFO", "WARN", "ERROR"}) {
            mockMvc.perform(put("/api/v1/admin/config/logging-level")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"level\":\"" + level + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logging level updated to " + level));
        }
    }

    @Test
    @DisplayName("PUT /api/v1/admin/config/logging-level - should return 400 for invalid level")
    void updateLoggingLevel_invalid() throws Exception {
        mockMvc.perform(put("/api/v1/admin/config/logging-level")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"INVALID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/config/logging-level - should return 400 for lowercase level")
    void updateLoggingLevel_lowercase() throws Exception {
        mockMvc.perform(put("/api/v1/admin/config/logging-level")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"debug\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/config/logging-level - should return 400 for null level")
    void updateLoggingLevel_null() throws Exception {
        mockMvc.perform(put("/api/v1/admin/config/logging-level")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== System Logs Integration Tests ====================

    @Test
    @DisplayName("GET /api/v1/admin/logs - should return empty logs when file does not exist")
    void getLogs_fileNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("lines", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs").isArray())
                .andExpect(jsonPath("$.logs").isEmpty())
                .andExpect(jsonPath("$.message").value("Log file not found"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/logs - should use default 100 lines when not specified")
    void getLogs_defaultLines() throws Exception {
        mockMvc.perform(get("/api/v1/admin/logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs").isArray());
    }

    // ==================== Authentication Tests ====================

    @Test
    @DisplayName("Should reject invalid JWT token")
    void rejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/server/status")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject missing Authorization header")
    void rejectMissingAuthHeader() throws Exception {
        mockMvc.perform(get("/api/v1/admin/server/status"))
                .andExpect(status().isForbidden());
    }
}
