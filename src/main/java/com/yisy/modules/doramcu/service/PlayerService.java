package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for managing 4X empire game players.
 * <p>
 * Handles player registration with auto-generated unique PINs,
 * PIN validation, and player lookup by email or PIN.
 * </p>
 */
@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    private static final int MIN_PIN = 100_000;
    private static final int MAX_PIN = 999_999;

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Generates a unique 6-digit login PIN.
     * <p>
     * Retries until a PIN is found that is not already in use.
     * </p>
     *
     * @return a unique 6-digit PIN string
     */
    public String generateUniquePin() {
        String pin;
        do {
            pin = String.valueOf(ThreadLocalRandom.current().nextInt(MIN_PIN, MAX_PIN + 1));
        } while (playerRepository.existsByLoginPin(pin));
        log.debug("Generated unique PIN: {}", pin);
        return pin;
    }

    /**
     * Registers a new player with the given email and empire characteristics.
     * <p>
     * A unique 6-digit PIN is auto-generated for the player.
     * </p>
     *
     * @param email the player's email address
     * @param empireName the name of the player's empire
     * @param governmentType the government type of the empire
     * @param ethos the ethos of the empire
     * @return the saved player with generated PIN
     * @throws IllegalArgumentException if the email is already registered
     */
    public Player registerPlayer(String email, String empireName,
                                 Player.GovernmentType governmentType, Player.Ethos ethos) {
        if (playerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        String pin = generateUniquePin();
        Player player = new Player();
        player.setEmail(email);
        player.setLoginPin(pin);
        player.setEmpireName(empireName);
        player.setGovernmentType(governmentType);
        player.setEthos(ethos);

        Player saved = playerRepository.save(player);
        log.info("Registered new player: {} with PIN: {}", saved.getEmail(), pin);
        return saved;
    }

    /**
     * Finds a player by email address.
     *
     * @param email the email to search for
     * @return optional player if found
     */
    public Optional<Player> findByEmail(String email) {
        return playerRepository.findByEmail(email);
    }

    /**
     * Finds a player by login PIN.
     *
     * @param pin the 6-digit PIN
     * @return optional player if found
     */
    public Optional<Player> findByLoginPin(String pin) {
        return playerRepository.findByLoginPin(pin);
    }

    /**
     * Validates a login PIN against a player's email.
     *
     * @param email the player's email
     * @param pin the PIN to validate
     * @return true if the PIN matches the player's email
     */
    public boolean validateLoginPin(String email, String pin) {
        return playerRepository.findByEmail(email)
                .map(player -> player.getLoginPin().equals(pin))
                .orElse(false);
    }

    /**
     * Returns a masked version of the PIN for secure display.
     * <p>
     * Shows only the first and last digit, e.g., "1****9".
     * </p>
     *
     * @param pin the 6-digit PIN
     * @return masked PIN string
     */
    public String maskPin(String pin) {
        if (pin == null || pin.length() < 2) {
            return "******";
        }
        return pin.charAt(0) + "****" + pin.charAt(pin.length() - 1);
    }

    /**
     * Retrieves all registered players.
     *
     * @return list of all players
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    /**
     * Retrieves a player by ID.
     *
     * @param id the player ID
     * @return optional player if found
     */
    public Optional<Player> findById(Long id) {
        return playerRepository.findById(id);
    }

    /**
     * Updates the empire name for a player.
     *
     * @param id the player ID
     * @param newEmpireName the new empire name
     * @return the updated player, or empty if not found
     */
    public Optional<Player> updateEmpireName(Long id, String newEmpireName) {
        return playerRepository.findById(id)
                .map(player -> {
                    player.setEmpireName(newEmpireName);
                    return playerRepository.save(player);
                });
    }

    /**
     * Updates the government type for a player.
     *
     * @param id the player ID
     * @param governmentType the new government type
     * @return the updated player, or empty if not found
     */
    public Optional<Player> updateGovernmentType(Long id, Player.GovernmentType governmentType) {
        return playerRepository.findById(id)
                .map(player -> {
                    player.setGovernmentType(governmentType);
                    return playerRepository.save(player);
                });
    }

    /**
     * Updates the ethos for a player.
     *
     * @param id the player ID
     * @param ethos the new ethos
     * @return the updated player, or empty if not found
     */
    public Optional<Player> updateEthos(Long id, Player.Ethos ethos) {
        return playerRepository.findById(id)
                .map(player -> {
                    player.setEthos(ethos);
                    return playerRepository.save(player);
                });
    }
}
