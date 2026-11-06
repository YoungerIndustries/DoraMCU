package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerRepository playerRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(userRepository, playerRepository);
    }

    // ---- loadUserByUsername - User found ----

    @Test
    @DisplayName("loadUserByUsername - should return UserDetails when User is found")
    void loadUserByUsername_shouldReturnUserDetailsForUser() {
        User user = new User(1L, "admin", "password123");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("loadUserByUsername - should prefer User over Player when both exist")
    void loadUserByUsername_shouldPreferUserOverPlayer() {
        User user = new User(1L, "player@example.com", "userpass");
        Player player = new Player(1L, "player@example.com", "123456", "Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(userRepository.findByUsername("player@example.com")).thenReturn(Optional.of(user));
        when(playerRepository.findByEmail("player@example.com")).thenReturn(Optional.of(player));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("player@example.com");

        assertEquals("userpass", userDetails.getPassword());
        verify(userRepository).findByUsername("player@example.com");
        verify(playerRepository, never()).findByEmail(anyString());
    }

    // ---- loadUserByUsername - Player found ----

    @Test
    @DisplayName("loadUserByUsername - should return UserDetails when Player is found")
    void loadUserByUsername_shouldReturnUserDetailsForPlayer() {
        User user = null;
        Player player = new Player(1L, "player@example.com", "123456", "Test Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(userRepository.findByUsername("player@example.com")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("player@example.com")).thenReturn(Optional.of(player));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("player@example.com");

        assertNotNull(userDetails);
        assertEquals("player@example.com", userDetails.getUsername());
        assertEquals("123456", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PLAYER")));
    }

    // ---- loadUserByUsername - Not found ----

    @Test
    @DisplayName("loadUserByUsername - should throw UsernameNotFoundException when not found")
    void loadUserByUsername_shouldThrowWhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("unknown")
        );
    }

    @Test
    @DisplayName("loadUserByUsername - should include username in exception message")
    void loadUserByUsername_shouldIncludeUsernameInException() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("missing")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("missing")
        );

        assertTrue(ex.getMessage().contains("missing"));
    }
}
