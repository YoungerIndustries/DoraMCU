package com.yisy.modules.doramcu.traveller;

/**
 * Lifecycle status of a Traveller character during creation and career service.
 * <p>
 * A character transitions through these states:
 * </p>
 * <ol>
 *   <li><b>TRAINING</b> — Character is available for login, between tours or
 *       just created. The player can log in to continue character creation.</li>
 *   <li><b>ON_DUTY</b> — Character is currently serving a tour of duty in the
 *       4X game. The character is not available for login until the tour completes.</li>
 *   <li><b>COMPLETED</b> — Character creation and all career tours are finished.
 *       The character is ready for adventure play.</li>
 * </ol>
 * <p>
 * Status transitions:
 * </p>
 * <ul>
 *   <li>TRAINING → ON_DUTY (player chooses to serve a tour in the 4X game)</li>
 *   <li>ON_DUTY → TRAINING (tour turns exhausted, character may continue creation)</li>
 *   <li>TRAINING → COMPLETED (all career tours resolved)</li>
 * </ul>
 */
public enum CharacterStatus {

    /**
     * Character is available for login, between tours or just created.
     */
    TRAINING("Training", "Character is available for login"),

    /**
     * Character is serving a tour of duty in the 4X game.
     * Not available for login until the tour completes.
     */
    ON_DUTY("On Duty", "Character is serving in the 4X game"),

    /**
     * Character creation and all career tours are finished.
     * The character is ready for adventure play.
     */
    COMPLETED("Completed", "Character creation is complete"),

    /**
     * Character is actively traveling the 4X universe with a ship.
     * Available for login — the player controls their character's travel
     * and witnesses 4X events like battles, fleet movements, and conquests.
     */
    TRAVELLING("Travelling", "Character is exploring the 4X universe");

    private final String displayName;
    private final String description;

    CharacterStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Returns a human-readable name for display.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a short description of this status.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns true if the character is available for login.
     * TRAVELLING characters are available — the player controls their travel.
     */
    public boolean isAvailableForLogin() {
        return this == TRAINING || this == COMPLETED || this == TRAVELLING;
    }
}
