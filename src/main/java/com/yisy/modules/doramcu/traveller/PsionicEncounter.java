package com.yisy.modules.doramcu.traveller;

import java.util.*;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Random psionic encounter generator from Classic Traveller B03: Worlds & Adventures.
 * <p>
 * Psionic encounters can occur during adventures when characters with psionic
 * ability travel through the Imperium. These encounters range from detecting
 * other psionics to being targeted by hostile psionic attacks.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 3: Worlds & Adventures (1977).
 * </p>
 */
public class PsionicEncounter {

    /**
     * Types of psionic encounters.
     */
    public enum EncounterType {
        /**
         * The character detects another psionic nearby.
         */
        DETECTION("Detection", "You sense a psionic presence nearby."),

        /**
         * A friendly psionic offers help or information.
         */
        ALLY("Ally", "A friendly psionic approaches you."),

        /**
         * A hostile psionic attempts to attack or deceive.
         */
        HOSTILE("Hostile", "A hostile psionic targets you."),

        /**
         * The character experiences a psionic vision or premonition.
         */
        VISION("Vision", "You experience a vivid psionic vision."),

        /**
         * A psionic entity or phenomenon is encountered.
         */
        ENTITY("Entity", "You encounter a psionic entity."),

        /**
         * The character's powers surge unexpectedly.
         */
        SURGE("Surge", "Your psionic powers surge with unexpected strength."),

        /**
         * The character's powers are temporarily suppressed.
         */
        SUPPRESSION("Suppression", "Your psionic powers are dampened by an unknown force."),

        /**
         * A psionic artifact or location is discovered.
         */
        ARTIFACT("Artifact", "You discover a psionic artifact or location.");

        private final String displayName;
        private final String description;

        EncounterType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Checks if a psionic encounter occurs.
     * <p>
     * Roll 2D6: encounter on 2-7 (67% chance for detected psionics).
     * Non-psionics have a 16% chance (roll 2 only).
     * </p>
     *
     * @param isPsionic whether the character has detected psionic ability
     * @return true if an encounter occurs
     */
    public boolean checkEncounter(boolean isPsionic) {
        int roll = DiceUtil.roll2D6();
        return isPsionic ? roll <= 7 : roll == 2;
    }

    /**
     * Generates a random psionic encounter.
     *
     * @param detectionLevel the character's psionic detection level
     * @return the encounter details
     */
    public PsionicEncounterResult generateEncounter(PsionicAbility.DetectionLevel detectionLevel) {
        EncounterType type;
        int roll = DiceUtil.roll2D6();

        // Higher detection levels see more varied encounters
        int maxType = switch (detectionLevel) {
            case LATENT -> 2;
            case WEAK -> 3;
            case AVERAGE -> 5;
            case STRONG -> 7;
            case MASTER -> 8;
            default -> 1;
        };

        type = EncounterType.values()[Math.min(roll - 1, EncounterType.values().length - 1)];
        if (type.ordinal() >= maxType) {
            type = EncounterType.values()[maxType - 1];
        }

        String narrative = generateNarrative(type, detectionLevel);
        return new PsionicEncounterResult(type, narrative, roll);
    }

    /**
     * Generates a narrative description for the encounter.
     */
    private String generateNarrative(EncounterType type, PsionicAbility.DetectionLevel level) {
        String strength = level.getDisplayName().toLowerCase();
        return switch (type) {
            case DETECTION -> "Your " + strength + " psionic senses tingle. You detect another psionic presence nearby, but cannot determine their intentions.";
            case ALLY -> "A fellow psionic approaches you with an offer of assistance. Their " + strength + " abilities resonate with your own.";
            case HOSTILE -> "A hostile psionic locks onto your mind. Your " + strength + " defenses strain against their mental assault.";
            case VISION -> "A vivid vision overwhelms your senses. Images of the future flash before your " + strength + " mind.";
            case ENTITY -> "You sense a powerful psionic entity — its presence dwarfs your " + strength + " abilities.";
            case SURGE -> "Your psionic powers surge unexpectedly! For a moment, your " + strength + " abilities feel far stronger.";
            case SUPPRESSION -> "An invisible force dampens your psionic abilities. Your " + strength + " powers feel muted and distant.";
            case ARTIFACT -> "You discover an object radiating psionic energy. It could enhance your " + strength + " abilities.";
        };
    }

    /**
     * Result of a psionic encounter.
     */
    public static class PsionicEncounterResult {
        private final EncounterType type;
        private final String narrative;
        private final int roll;

        PsionicEncounterResult(EncounterType type, String narrative, int roll) {
            this.type = type;
            this.narrative = narrative;
            this.roll = roll;
        }

        public EncounterType getType() {
            return type;
        }

        public String getNarrative() {
            return narrative;
        }

        public int getRoll() {
            return roll;
        }

        @Override
        public String toString() {
            return String.format("Psionic Encounter [%s]: %s", type.getDisplayName(), narrative);
        }
    }
}
