package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerService Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService(playerRepository);
    }

    // ---- registerPlayer ----

    @Test
    @DisplayName("registerPlayer - should save new player with generated PIN")
    void registerPlayer_shouldSaveNewPlayer() {
        String email = "player@example.com";
        String empireName = "Test Empire";
        Player.GovernmentType governmentType = Player.GovernmentType.DEMOCRACY;
        Player.Ethos ethos = Player.Ethos.MATERIALIST;

        when(playerRepository.existsByEmail(email)).thenReturn(false);
        when(playerRepository.existsByLoginPin("123456")).thenReturn(false);
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        Player result = playerService.registerPlayer(email, empireName, governmentType, ethos);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(email, result.getEmail());
        assertEquals(empireName, result.getEmpireName());
        assertEquals(governmentType, result.getGovernmentType());
        assertEquals(ethos, result.getEthos());
        assertNotNull(result.getLoginPin());
        assertEquals(6, result.getLoginPin().length());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("registerPlayer - should throw IllegalArgumentException if email already registered")
    void registerPlayer_shouldThrowOnDuplicateEmail() {
        when(playerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                playerService.registerPlayer("existing@example.com", "Empire",
                        Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST)
        );
        verify(playerRepository, never()).save(any());
    }

    // ---- findByEmail ----

    @Test
    @DisplayName("findByEmail - should return player when found")
    void findByEmail_shouldReturnPlayer() {
        Player player = new Player(1L, "player@example.com", "123456", "Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(playerRepository.findByEmail("player@example.com")).thenReturn(Optional.of(player));

        Optional<Player> result = playerService.findByEmail("player@example.com");

        assertTrue(result.isPresent());
        assertEquals("player@example.com", result.get().getEmail());
    }

    @Test
    @DisplayName("findByEmail - should return empty when not found")
    void findByEmail_shouldReturnEmpty() {
        when(playerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<Player> result = playerService.findByEmail("unknown@example.com");

        assertTrue(result.isEmpty());
    }

    // ---- findByLoginPin ----

    @Test
    @DisplayName("findByLoginPin - should return player when found")
    void findByLoginPin_shouldReturnPlayer() {
        Player player = new Player(1L, "player@example.com", "123456", "Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(playerRepository.findByLoginPin("123456")).thenReturn(Optional.of(player));

        Optional<Player> result = playerService.findByLoginPin("123456");

        assertTrue(result.isPresent());
        assertEquals("123456", result.get().getLoginPin());
    }

    @Test
    @DisplayName("findByLoginPin - should return empty when not found")
    void findByLoginPin_shouldReturnEmpty() {
        when(playerRepository.findByLoginPin("999999")).thenReturn(Optional.empty());

        Optional<Player> result = playerService.findByLoginPin("999999");

        assertTrue(result.isEmpty());
    }

    // ---- validateLoginPin ----

    @Test
    @DisplayName("validateLoginPin - should return true when PIN matches")
    void validateLoginPin_shouldReturnTrue() {
        Player player = new Player(1L, "player@example.com", "123456", "Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(playerRepository.findByEmail("player@example.com")).thenReturn(Optional.of(player));

        boolean result = playerService.validateLoginPin("player@example.com", "123456");

        assertTrue(result);
    }

    @Test
    @DisplayName("validateLoginPin - should return false when PIN does not match")
    void validateLoginPin_shouldReturnFalse() {
        Player player = new Player(1L, "player@example.com", "123456", "Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(playerRepository.findByEmail("player@example.com")).thenReturn(Optional.of(player));

        boolean result = playerService.validateLoginPin("player@example.com", "654321");

        assertFalse(result);
    }

    @Test
    @DisplayName("validateLoginPin - should return false when player not found")
    void validateLoginPin_shouldReturnFalseWhenNotFound() {
        when(playerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        boolean result = playerService.validateLoginPin("unknown@example.com", "123456");

        assertFalse(result);
    }

    // ---- maskPin ----

    @Test
    @DisplayName("maskPin - should mask 6-digit PIN showing first and last digit")
    void maskPin_shouldMaskSixDigitPin() {
        String masked = playerService.maskPin("123456");
        assertEquals("1****6", masked);
    }

    @Test
    @DisplayName("maskPin - should return ****** for null PIN")
    void maskPin_shouldReturnStarsForNull() {
        String masked = playerService.maskPin(null);
        assertEquals("******", masked);
    }

    @Test
    @DisplayName("maskPin - should return ****** for single character PIN")
    void maskPin_shouldReturnStarsForShortPin() {
        String masked = playerService.maskPin("1");
        assertEquals("******", masked);
    }

    @Test
    @DisplayName("maskPin - should mask 2-character PIN")
    void maskPin_shouldMaskTwoCharPin() {
        String masked = playerService.maskPin("12");
        assertEquals("1****2", masked);
    }

    // ---- getAllPlayers ----

    @Test
    @DisplayName("getAllPlayers - should return all players")
    void getAllPlayers_shouldReturnAllPlayers() {
        Player player1 = new Player(1L, "p1@example.com", "111111", "Empire1",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        Player player2 = new Player(2L, "p2@example.com", "222222", "Empire2",
                Player.GovernmentType.MONARCHY, Player.Ethos.SPIRITUALIST);
        when(playerRepository.findAll()).thenReturn(List.of(player1, player2));

        List<Player> players = playerService.getAllPlayers();

        assertEquals(2, players.size());
        assertEquals("p1@example.com", players.get(0).getEmail());
    }

    @Test
    @DisplayName("getAllPlayers - should return empty list when no players")
    void getAllPlayers_shouldReturnEmptyList() {
        when(playerRepository.findAll()).thenReturn(Collections.emptyList());

        List<Player> players = playerService.getAllPlayers();

        assertTrue(players.isEmpty());
    }

    // ---- findById ----

    @Test
    @DisplayName("findById - should return player when found")
    void findById_shouldReturnPlayer() {
        Player player = new Player(1L, "player@example.com", "123456", "Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

        Optional<Player> result = playerService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("findById - should return empty when not found")
    void findById_shouldReturnEmpty() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Player> result = playerService.findById(99L);

        assertTrue(result.isEmpty());
    }

    // ---- updateEmpireName ----

    @Test
    @DisplayName("updateEmpireName - should update empire name when player exists")
    void updateEmpireName_shouldUpdate() {
        Player player = new Player(1L, "player@example.com", "123456", "Old Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Player> result = playerService.updateEmpireName(1L, "New Empire");

        assertTrue(result.isPresent());
        assertEquals("New Empire", result.get().getEmpireName());
        verify(playerRepository).save(player);
    }

    @Test
    @DisplayName("updateEmpireName - should return empty when player not found")
    void updateEmpireName_shouldReturnEmpty() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Player> result = playerService.updateEmpireName(99L, "New Empire");

        assertTrue(result.isEmpty());
        verify(playerRepository, never()).save(any());
    }

    // ---- updateGovernmentType ----

    @Test
    @DisplayName("updateGovernmentType - should update government type when player exists")
    void updateGovernmentType_shouldUpdate() {
        Player player = new Player(1L, "player@example.com", "123456", "Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Player> result = playerService.updateGovernmentType(1L, Player.GovernmentType.MONARCHY);

        assertTrue(result.isPresent());
        assertEquals(Player.GovernmentType.MONARCHY, result.get().getGovernmentType());
    }

    @Test
    @DisplayName("updateGovernmentType - should return empty when player not found")
    void updateGovernmentType_shouldReturnEmpty() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Player> result = playerService.updateGovernmentType(99L, Player.GovernmentType.MONARCHY);

        assertTrue(result.isEmpty());
    }

    // ---- updateEthos ----

    @Test
    @DisplayName("updateEthos - should update ethos when player exists")
    void updateEthos_shouldUpdate() {
        Player player = new Player(1L, "player@example.com", "123456", "Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Player> result = playerService.updateEthos(1L, Player.Ethos.SPIRITUALIST);

        assertTrue(result.isPresent());
        assertEquals(Player.Ethos.SPIRITUALIST, result.get().getEthos());
    }

    @Test
    @DisplayName("updateEthos - should return empty when player not found")
    void updateEthos_shouldReturnEmpty() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Player> result = playerService.updateEthos(99L, Player.Ethos.SPIRITUALIST);

        assertTrue(result.isEmpty());
    }
}
