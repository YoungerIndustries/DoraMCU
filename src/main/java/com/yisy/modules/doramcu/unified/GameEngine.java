package com.yisy.modules.doramcu.unified;

import java.util.List;

/**
 * Minimal interface for game engines that can be managed by shared infrastructure.
 * <p>
 * This interface extracts only the common operations needed by shared services
 * like {@code TurnScheduler} and {@code GameSession}, without depending on any
 * game-specific types. Each game system (4X, Traveller, Captain's Log) can implement
 * this interface to integrate with the shared session management layer.
 * </p>
 * <p>
 * Game-specific operations (fleet movement, combat, resource management, etc.)
 * remain in the concrete game engine classes and their dedicated services.
 * </p>
 */
public interface GameEngine {

    /**
     * Returns the name of this game instance.
     *
     * @return the game name
     */
    String getName();

    /**
     * Returns the list of players in this game.
     * <p>
     * The player type is generic to accommodate different game systems:
     * the 4X game uses {@code hex.Player}, Traveller uses {@code TravellerCharacter},
     * and Captain's Log uses its own character model.
     * </p>
     *
     * @return the list of players
     */
    List<?> getPlayers();

    /**
     * Advances the game by one turn.
     * <p>
     * Called by {@code TurnScheduler} to auto-advance turns in active sessions.
     * </p>
     */
    void nextTurn();

    /**
     * Returns the current turn number.
     *
     * @return the current turn number
     */
    int getCurrentTurn();
}
