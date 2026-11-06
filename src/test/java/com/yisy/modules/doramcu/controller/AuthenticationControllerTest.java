package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.config.MetricsConfig;
import com.yisy.modules.doramcu.dto.LoginRequest;
import com.yisy.modules.doramcu.dto.LoginResponse;
import com.yisy.modules.doramcu.dto.RegisterRequest;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.service.JwtService;
import com.yisy.modules.doramcu.service.PlayerService;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private MetricsConfig metricsConfig;

    @Mock
    private Counter loginSuccessCounter;

    @Mock
    private Counter loginFailureCounter;

    @Mock
    private Authentication authentication;

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private PlayerService playerService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ObjectProvider<PlayerService> playerServiceProvider;

    @Mock
    private ObjectProvider<PlayerRepository> playerRepositoryProvider;

    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        when(metricsConfig.getLoginSuccessCounter()).thenReturn(loginSuccessCounter);
        when(metricsConfig.getLoginFailureCounter()).thenReturn(loginFailureCounter);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(playerServiceProvider.getIfAvailable()).thenReturn(playerService);
        when(playerRepositoryProvider.getIfAvailable()).thenReturn(playerRepository);

        controller = new AuthenticationController(authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                playerServiceProvider, playerRepositoryProvider);
    }

    // ---- Login Success ----

    @Test
    @DisplayName("login - should return 200 OK with token on valid credentials")
    void login_shouldReturnOkWithToken() {
        LoginRequest request = new LoginRequest("admin", "password123");
        String expectedToken = "jwt-token-123";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("admin")).thenReturn(expectedToken);

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedToken, response.getBody().getToken());
        assertEquals("admin", response.getBody().getUsername());
        verify(loginSuccessCounter).increment();
    }

    @Test
    @DisplayName("login - should generate token with correct username")
    void login_shouldGenerateTokenWithCorrectUsername() {
        LoginRequest request = new LoginRequest("user1", "secret");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("user1")).thenReturn("token-for-user1");

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtService).generateToken("user1");
    }

    // ---- Login Failure ----

    @Test
    @DisplayName("login - should return 400 on invalid credentials")
    void login_shouldReturnBadRequestOnInvalidCredentials() {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(loginFailureCounter).increment();
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("login - should not generate token on failed authentication")
    void login_shouldNotGenerateTokenOnFailure() {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        controller.login(request);

        verify(jwtService, never()).generateToken(anyString());
        verify(loginFailureCounter).increment();
    }

    // ---- Authentication Token Verification ----

    @Test
    @DisplayName("login - should pass correct credentials to authentication manager")
    void login_shouldPassCorrectCredentials() {
        LoginRequest request = new LoginRequest("testuser", "testpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("testuser")).thenReturn("token");

        controller.login(request);

        verify(authenticationManager).authenticate(argThat(token -> {
            UsernamePasswordAuthenticationToken upToken = (UsernamePasswordAuthenticationToken) token;
            return "testuser".equals(upToken.getPrincipal())
                    && "testpass".equals(upToken.getCredentials());
        }));
    }

    // ---- Edge Cases ----

    @Test
    @DisplayName("login - should handle empty username")
    void login_shouldHandleEmptyUsername() {
        LoginRequest request = new LoginRequest("", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("login - should handle empty password")
    void login_shouldHandleEmptyPassword() {
        LoginRequest request = new LoginRequest("admin", "");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("login - should handle null username")
    void login_shouldHandleNullUsername() {
        LoginRequest request = new LoginRequest(null, "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("login - should handle null password")
    void login_shouldHandleNullPassword() {
        LoginRequest request = new LoginRequest("admin", null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ---- Registration Success ----

    @Test
    @DisplayName("register - should return 201 CREATED with token on successful registration")
    void register_shouldReturnCreatedWithToken() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        var response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-token", ((LoginResponse) response.getBody()).getToken());
        assertEquals("newplayer", ((LoginResponse) response.getBody()).getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - should encode password before saving")
    void register_shouldEncodePassword() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        controller.register(request);

        verify(passwordEncoder).encode("securepass");
    }

    @Test
    @DisplayName("register - should save user with correct username")
    void register_shouldSaveUserWithCorrectUsername() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        controller.register(request);

        verify(userRepository).save(argThat(user -> "newplayer".equals(user.getUsername())));
    }

    // ---- Registration Failure ----

    @Test
    @DisplayName("register - should return 400 when username already exists")
    void register_shouldReturnBadRequestWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("admin", "password");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));

        var response = controller.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Username already exists", errorBody.get("error"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register - should not generate token when username exists")
    void register_shouldNotGenerateTokenWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("admin", "password");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));

        controller.register(request);

        verify(jwtService, never()).generateToken(anyString());
    }

    // ---- Galaxy List ----

    @Test
    @DisplayName("listGalaxies - should return both predefined galaxies")
    void listGalaxies_shouldReturnBothGalaxies() {
        ResponseEntity<List<String>> response = controller.listGalaxies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("Milky Way"));
        assertTrue(response.getBody().contains("Test Galaxy"));
    }

    // ---- Registration with Galaxy Selection ----

    @Test
    @DisplayName("register - should save user with selected galaxy")
    void register_shouldSaveUserWithSelectedGalaxy() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass", "Test Galaxy");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        var response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userRepository).save(argThat(user ->
                "newplayer".equals(user.getUsername()) && "Test Galaxy".equals(user.getGalaxyName())
        ));
    }

    @Test
    @DisplayName("register - should default to Milky Way when galaxy is null")
    void register_shouldDefaultToMilkyWayWhenGalaxyNull() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        controller.register(request);

        verify(userRepository).save(argThat(user ->
                "Milky Way".equals(user.getGalaxyName())
        ));
    }

    @Test
    @DisplayName("register - should default to Milky Way when galaxy is empty")
    void register_shouldDefaultToMilkyWayWhenGalaxyEmpty() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass", "");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        controller.register(request);

        verify(userRepository).save(argThat(user ->
                "Milky Way".equals(user.getGalaxyName())
        ));
    }

    @Test
    @DisplayName("register - should reject invalid galaxy name")
    void register_shouldRejectInvalidGalaxy() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass", "Andromeda");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());

        var response = controller.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertTrue(errorBody.get("error").contains("Invalid galaxy"));
        verify(userRepository, never()).save(any(User.class));
    }

    // ---- Server Info & PIN Login Tests ----

    @Test
    @DisplayName("getServerInfo - should return server cluster metadata")
    void getServerInfo_shouldReturnServerClusterMetadata() {
        var response = controller.getServerInfo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ONLINE", response.getBody().getStatus());
        assertEquals("DoraMCU Galactic Cluster Alpha", response.getBody().getServerName());
        assertNotNull(response.getBody().getActiveGalaxies());
    }

    @Test
    @DisplayName("pinLogin - should return bad request when PIN does not exist")
    void pinLogin_shouldReturnBadRequestOnInvalidPin() {
        PlayerService mockPlayerService = mock(PlayerService.class);
        PlayerRepository mockPlayerRepo = mock(PlayerRepository.class);
        when(mockPlayerService.findByLoginPin("999999")).thenReturn(Optional.empty());

        ObjectProvider<PlayerService> mockPlayerServiceProvider = mock(ObjectProvider.class);
        ObjectProvider<PlayerRepository> mockPlayerRepoProvider = mock(ObjectProvider.class);
        when(mockPlayerServiceProvider.getIfAvailable()).thenReturn(mockPlayerService);
        when(mockPlayerRepoProvider.getIfAvailable()).thenReturn(mockPlayerRepo);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                mockPlayerServiceProvider, mockPlayerRepoProvider
        );

        var response = authCtrl.pinLogin(new com.yisy.modules.doramcu.dto.PinLoginRequest("999999"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(loginFailureCounter).increment();
    }

    @Test
    @DisplayName("pinLogin - should return token and player details on valid PIN")
    void pinLogin_shouldReturnTokenOnValidPin() {
        PlayerService mockPlayerService = mock(PlayerService.class);
        PlayerRepository mockPlayerRepo = mock(PlayerRepository.class);

        com.yisy.modules.doramcu.model.Player player = new com.yisy.modules.doramcu.model.Player();
        player.setEmail("commander@empire.com");
        player.setLoginPin("123456");
        player.setEmpireName("Terran Federation");
        player.setGovernmentType(com.yisy.modules.doramcu.model.Player.GovernmentType.DEMOCRACY);
        player.setEthos(com.yisy.modules.doramcu.model.Player.Ethos.MATERIALIST);

        when(mockPlayerService.findByLoginPin("123456")).thenReturn(Optional.of(player));
        when(userRepository.findByUsername("commander@empire.com")).thenReturn(Optional.of(new User(1L, "commander@empire.com", "pass")));
        when(jwtService.generateToken("commander@empire.com")).thenReturn("jwt-pin-token");

        ObjectProvider<PlayerService> mockPlayerServiceProvider = mock(ObjectProvider.class);
        ObjectProvider<PlayerRepository> mockPlayerRepoProvider = mock(ObjectProvider.class);
        when(mockPlayerServiceProvider.getIfAvailable()).thenReturn(mockPlayerService);
        when(mockPlayerRepoProvider.getIfAvailable()).thenReturn(mockPlayerRepo);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                mockPlayerServiceProvider, mockPlayerRepoProvider
        );

        var response = authCtrl.pinLogin(new com.yisy.modules.doramcu.dto.PinLoginRequest("123456"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse lr = (LoginResponse) response.getBody();
        assertEquals("jwt-pin-token", lr.getToken());
        assertEquals("commander@empire.com", lr.getUsername());
        assertEquals("123456", lr.getPin());
        assertEquals("Terran Federation", lr.getEmpireName());
        assertEquals("DEMOCRACY", lr.getGovernmentType());
        verify(loginSuccessCounter).increment();
    }
}
