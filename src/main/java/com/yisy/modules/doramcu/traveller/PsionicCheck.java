package com.yisy.modules.doramcu.traveller;

import java.util.*;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Resolves psionic power checks from Classic Traveller B03: Worlds & Adventures.
 * <p>
 * To use a psionic power, the character rolls 2D6 and compares against a target
 * number based on their In (Intuition) score, discipline level, and the difficulty
 * of the specific power attempt.
 * </p>
 * <p>
 * Target = 12 - (In / 2) - disciplineLevel + difficulty
 * Success = roll <= target
 * </p>
 * <p>
 * Derived from Classic Traveller Book 3: Worlds & Adventures (1977).
 * </p>
 */
public class PsionicCheck {

    /**
     * Difficulty levels for psionic power attempts.
     */
    public enum Difficulty {
        /**
         * Simple power use, e.g., basic mind reading of an unguarded target.
         */
        EASY(0, "Easy"),

        /**
         * Standard power use, e.g., reading surface thoughts.
         */
        NORMAL(2, "Normal"),

        /**
         * Difficult power use, e.g., telekinetic lift of heavy object.
         */
        HARD(4, "Hard"),

        /**
         * Very difficult, e.g., teleportation across a room.
         */
        VERY_HARD(6, "Very Hard"),

        /**
         * Nearly impossible, e.g., time manipulation.
         */
        NEAR_IMPOSSIBLE(8, "Near Impossible");

        private final int modifier;
        private final String displayName;

        Difficulty(int modifier, String displayName) {
            this.modifier = modifier;
            this.displayName = displayName;
        }

        public int getModifier() {
            return modifier;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Performs a psionic power check.
     *
     * @param inScore the character's In (Intuition) score
     * @param disciplineLevel the character's level in the relevant discipline (1-5)
     * @param difficulty the difficulty of the power attempt
     * @return the result of the check
     */
    public PsionicResult performCheck(int inScore, int disciplineLevel, Difficulty difficulty) {
        if (inScore < 2) {
            return new PsionicResult(false, 0, 0, "No psionic ability");
        }
        if (disciplineLevel < 1) {
            return new PsionicResult(false, 0, 0, "No discipline trained");
        }

        int target = 12 - (inScore / 2) - disciplineLevel + difficulty.getModifier();
        int roll = DiceUtil.roll2D6();
        boolean success = roll <= target;

        return new PsionicResult(success, roll, target,
                success ? "Power succeeded" : "Power failed");
    }

    /**
     * Performs a psionic power check with a specific roll (for seeded gameplay).
     *
     * @param inScore the character's In (Intuition) score
     * @param disciplineLevel the character's level in the relevant discipline
     * @param difficulty the difficulty of the power attempt
     * @param roll the 2D6 roll result
     * @return the result of the check
     */
    public PsionicResult performCheck(int inScore, int disciplineLevel, Difficulty difficulty, int roll) {
        if (inScore < 2) {
            return new PsionicResult(false, roll, 0, "No psionic ability");
        }
        if (disciplineLevel < 1) {
            return new PsionicResult(false, roll, 0, "No discipline trained");
        }

        int target = 12 - (inScore / 2) - disciplineLevel + difficulty.getModifier();
        boolean success = roll <= target;

        return new PsionicResult(success, roll, target,
                success ? "Power succeeded" : "Power failed");
    }

    /**
     * Returns the fatigue cost for a power attempt.
     * <p>
     * Failed attempts cost 1 fatigue. Successful attempts cost 1 + difficulty modifier / 2.
     * </p>
     *
     * @param success whether the power succeeded
     * @param difficulty the difficulty of the attempt
     * @return the fatigue points to add
     */
    public int getFatigueCost(boolean success, Difficulty difficulty) {
        if (!success) {
            return 1;
        }
        return 1 + difficulty.getModifier() / 2;
    }

    /**
     * Result of a psionic power check.
     */
    public static class PsionicResult {
        private final boolean success;
        private final int roll;
        private final int target;
        private final String message;

        PsionicResult(boolean success, int roll, int target, String message) {
            this.success = success;
            this.roll = roll;
            this.target = target;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getRoll() {
            return roll;
        }

        public int getTarget() {
            return target;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("Psionic check: %s (rolled %d, target %d)",
                    success ? "SUCCESS" : "FAILED", roll, target);
        }
    }
}
