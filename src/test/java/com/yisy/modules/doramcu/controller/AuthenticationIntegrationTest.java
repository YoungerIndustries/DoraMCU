package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.yisy.modules.doramcu.DoraMcuLauncher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DoraMcuLauncher.class)
@ActiveProfiles("test")
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.yisy.modules.doramcu.repository.PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new org.springframework.security.web.FilterChainProxy(
                        context.getBean(org.springframework.security.web.SecurityFilterChain.class)))
                .build();

        playerRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        userRepository.save(admin);

        com.yisy.modules.doramcu.model.Player player = new com.yisy.modules.doramcu.model.Player();
        player.setEmail("player1@empire.com");
        player.setLoginPin("123456");
        player.setEmpireName("Terran Republic");
        player.setGovernmentType(com.yisy.modules.doramcu.model.Player.GovernmentType.DEMOCRACY);
        player.setEthos(com.yisy.modules.doramcu.model.Player.Ethos.MATERIALIST);
        player.setUser(admin);
        playerRepository.save(player);
    }

    @Test
    @DisplayName("POST /auth/login - should return token with valid credentials")
    void login_success() throws Exception {
        mockMvc.perform(post("/api/v1/4x/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 with invalid password")
    void login_invalidPassword() throws Exception {
        mockMvc.perform(post("/api/v1/4x/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 with non-existent user")
    void login_nonExistentUser() throws Exception {
        mockMvc.perform(post("/api/v1/4x/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"unknown\",\"password\":\"password\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 with empty username")
    void login_emptyUsername() throws Exception {
        mockMvc.perform(post("/api/v1/4x/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"admin123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 with empty password")
    void login_emptyPassword() throws Exception {
        mockMvc.perform(post("/api/v1/4x/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 with missing username")
    void login_missingUsername() throws Exception {
        mockMvc.perform(post("/api/v1/4x/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"admin123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 with missing password")
    void login_missingPassword() throws Exception {
        mockMvc.perform(post("/api/v1/4x/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - should return 200 and use token to access protected endpoint")
    void login_thenAccessProtectedEndpoint() throws Exception {
        // Login and extract token
        String response = mockMvc.perform(post("/api/v1/4x/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token from JSON response (simple parsing for test)
        String token = response.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        // Use token to access protected endpoint
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/4x/admin/server/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uptime").isString());
    }

    @Test
    @DisplayName("GET /api/v1/4x/auth/server-info - should return server cluster details")
    void serverInfo_success() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/4x/auth/server-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serverName").value("DoraMCU Galactic Cluster Alpha"))
                .andExpect(jsonPath("$.status").value("ONLINE"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    @DisplayName("POST /api/v1/4x/auth/pin-login - should authenticate player with valid PIN")
    void pinLogin_success() throws Exception {
        mockMvc.perform(post("/api/v1/4x/auth/pin-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.pin").value("123456"))
                .andExpect(jsonPath("$.empireName").value("Terran Republic"));
    }

    @Test
    @DisplayName("POST /api/v1/4x/auth/pin-login - should return 400 for invalid PIN")
    void pinLogin_invalidPin() throws Exception {
        mockMvc.perform(post("/api/v1/4x/auth/pin-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\":\"999999\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
    }
}
