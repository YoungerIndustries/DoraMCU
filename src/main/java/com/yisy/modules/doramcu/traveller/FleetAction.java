package com.yisy.modules.doramcu.traveller;

/**
 * Fleet actions from Classic Traveller Book 5: High Guard.
 * <p>
 * Fleet actions are the tactical maneuvers a fleet can perform during combat.
 * Each action has a different effect on the fleet's position, combat effectiveness,
 * and vulnerability.
 * </p>
 * <p>
 * Actions are resolved each round of fleet combat. The fleet commander chooses
 * one action per round, and the action determines the fleet's combat modifiers
 * for that round.
 * </p>
 */
public enum FleetAction {

    /**
     * Hold — the fleet maintains its current position and formation.
     * <p>
     * No movement, no special combat effects. Ships fire at full effectiveness.
     * Used when the fleet is satisfied with its current position.
     * </p>
     * Combat modifier: 0. Movement: none.
     */
    HOLD("Hold", 0, 0, "Maintain position and formation", false),

    /**
     * Advance — the fleet moves toward the enemy.
     * <p>
     * The fleet closes the distance to the enemy. Ships can fire while advancing,
     * but with reduced accuracy due to movement.
     * </p>
     * Combat modifier: -1. Movement: +1 distance toward enemy.
     */
    ADVANCE("Advance", -1, 1, "Move toward the enemy", false),

    /**
     * Retreat — the fleet moves away from the enemy.
     * <p>
     * The fleet increases the distance from the enemy. Ships can fire while
     * retreating, but with reduced accuracy. Used to disengage or reposition.
     * </p>
     * Combat modifier: -2. Movement: -1 distance from enemy.
     */
    RETREAT("Retreat", -2, -1, "Move away from the enemy", false),

    /**
     * Turn — the fleet changes direction.
     * <p>
     * The fleet rotates to face a new direction. No forward movement.
     * Used to present a different arc to the enemy or to reposition.
     * </p>
     * Combat modifier: -1. Movement: rotation only.
     */
    TURN("Turn", -1, 0, "Change direction", false),

    /**
     * Missile Salvo — the fleet fires all missile launchers.
     * <p>
     * All ships in the fleet fire their missile batteries simultaneously.
     * This is a high-damage action but leaves the fleet vulnerable while
     * missiles are in flight.
     * </p>
     * Combat modifier: +2 for missiles, -1 for lasers. Movement: none.
     */
    MISSILE_SALVO("Missile Salvo", 0, 0, "Fire all missile launchers", true),

    /**
     * Laser Barrage — the fleet fires all laser batteries.
     * <p>
     * All ships in the fleet fire their laser batteries simultaneously.
     * This is the standard combat action, with full accuracy and damage.
     * </p>
     * Combat modifier: +1 for lasers. Movement: none.
     */
    LASER_BARRAGE("Laser Barrage", 1, 0, "Fire all laser batteries", true),

    /**
     * Board — the fleet attempts to board enemy ships.
     <p>
     * The fleet closes to boarding range and launches boarding parties.
     * Requires the fleet to be adjacent to the enemy. High risk, high reward.
     * </p>
     * Combat modifier: -2. Movement: close to boarding range.
     */
    BOARD("Board", -2, 0, "Attempt to board enemy ships", true),

    /**
     * Ram — the fleet attempts to ram enemy ships.
     * <p>
     * The fleet accelerates toward the enemy at maximum speed.
     * Causes massive damage to both fleets. Desperate measure.
     * </p>
     * Combat modifier: +3 for damage, -3 for defense. Movement: maximum speed.
     */
    RAM("Ram", 3, -3, "Ram enemy ships at maximum speed", true),

    /**
     * Evasive Maneuvers — the fleet performs evasive actions.
     * <p>
     * The fleet performs random maneuvers to avoid enemy fire.
     * Reduces incoming damage but also reduces outgoing fire accuracy.
     * </p>
     * Combat modifier: -2 for offense, +2 for defense. Movement: random.
     */
    EVASIVE("Evasive Maneuvers", -2, 2, "Perform evasive actions", false),

    /**
     * Reform — the fleet reforms its formation.
     * <p>
     * The fleet changes to a different formation. No combat this round,
     * but the new formation may provide advantages in subsequent rounds.
     * </p>
     * Combat modifier: 0 (no fire). Movement: formation change.
     */
    REFORM("Reform", 0, 0, "Change formation", false),

    /**
     * Support Fire — the fleet provides supporting fire for another fleet.
     * <p>
     * The fleet fires in support of an allied fleet. The supported fleet
     * gains bonuses, while the supporting fleet fires at reduced effectiveness.
     * </p>
     * Combat modifier: +1 for supported fleet, -1 for supporting fleet.
     */
    SUPPORT_FIRE("Support Fire", 0, 1, "Provide supporting fire for allies", true),

    /**
     * Electronic Warfare — the fleet performs electronic attacks.
     * <p>
     * The fleet uses ECM and sensors to disrupt enemy systems.
     * Reduces enemy accuracy and detection range.
     * </p>
     * Combat modifier: -1 for enemy offense, -1 for enemy detection.
     */
    ELECTRONIC_WARFARE("Electronic Warfare", 0, 0, "Disrupt enemy systems", true);

    private final String name;
    private final int combatModifier;
    private final int defenseModifier;
    private final String description;
    private final boolean isOffensive;

    FleetAction(String name, int combatModifier, int defenseModifier,
                String description, boolean isOffensive) {
        this.name = name;
        this.combatModifier = combatModifier;
        this.defenseModifier = defenseModifier;
        this.description = description;
        this.isOffensive = isOffensive;
    }

    /**
     * Returns the action name.
     *
     * @return the name string
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the combat modifier for this action.
     * <p>
     * Positive values indicate better offensive capability.
     * Negative values indicate worse offensive capability.
     * </p>
     *
     * @return the combat modifier
     */
    public int getCombatModifier() {
        return combatModifier;
    }

    /**
     * Returns the defense modifier for this action.
     * <p>
     * Positive values indicate better defense.
     * Negative values indicate worse defense.
     * </p>
     *
     * @return the defense modifier
     */
    public int getDefenseModifier() {
        return defenseModifier;
    }

    /**
     * Returns the description of this action.
     *
     * @return the description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this is an offensive action (fires weapons).
     *
     * @return true if offensive
     */
    public boolean isOffensive() {
        return isOffensive;
    }

    /**
     * Checks if this action involves movement.
     *
     * @return true if the fleet moves
     */
    public boolean involvesMovement() {
        return this == ADVANCE || this == RETREAT || this == TURN || this == RAM;
    }

    /**
     * Checks if this action involves firing weapons.
     *
     * @return true if weapons are fired
     */
    public boolean involvesFiring() {
        return this == MISSILE_SALVO || this == LASER_BARRAGE ||
               this == BOARD || this == RAM || this == SUPPORT_FIRE;
    }

    /**
     * Finds the action with the given name (case-insensitive).
     *
     * @param name the action name
     * @return the matching action, or null if not found
     */
    public static FleetAction fromName(String name) {
        if (name == null) {
            return null;
        }
        for (FleetAction action : values()) {
            if (action.name.equalsIgnoreCase(name)) {
                return action;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
