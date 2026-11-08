package com.yisy.modules.doramcu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Shared utility for dice rolling across all game systems.
 *
 * Replaces scattered private rollD6(), roll2D6(), rollD20() methods in:
 * - traveller package (combat, careers, world generation)
 * - captainslog package (2d20 task resolution)
 * - shared package (character creation)
 *
 * Uses ThreadLocalRandom for thread-safe, performant random number generation.
 */
public final class DiceUtil {

    private DiceUtil() {
        // Utility class — prevent instantiation
    }

    /**
     * Roll a single D6 die (range 1-6).
     *
     * @return value between 1 and 6 inclusive
     */
    public static int rollD6() {
        return ThreadLocalRandom.current().nextInt(1, 7);
    }

    /**
     * Roll two D6 dice and return the sum (range 2-12).
     *
     * @return sum of two D6 rolls, between 2 and 12 inclusive
     */
    public static int roll2D6() {
        return rollD6() + rollD6();
    }

    /**
     * Roll a single D20 die (range 1-20).
     *
     * @return value between 1 and 20 inclusive
     */
    public static int rollD20() {
        return ThreadLocalRandom.current().nextInt(1, 21);
    }

    /**
     * Roll two D20 dice and return the sum (range 2-40).
     * Used by Captain's Log 2d20 task resolution.
     *
     * @return sum of two D20 rolls, between 2 and 40 inclusive
     */
    public static int roll2D20() {
        return rollD20() + rollD20();
    }

    /**
     * Roll a single die with the specified number of sides (range 1-sides).
     *
     * @param sides number of sides on the die (must be >= 1)
     * @return value between 1 and sides inclusive
     * @throws IllegalArgumentException if sides < 1
     */
    public static int rollDSides(int sides) {
        if (sides < 1) {
            throw new IllegalArgumentException("Dice must have at least 1 side, got: " + sides);
        }
        return ThreadLocalRandom.current().nextInt(1, sides + 1);
    }

    /**
     * Roll N dice with X sides each and return the sum.
     *
     * @param count number of dice to roll
     * @param sides number of sides per die
     * @return sum of all dice rolls
     * @throws IllegalArgumentException if count < 0 or sides < 1
     */
    public static int rollNdX(int count, int sides) {
        if (count < 0) {
            throw new IllegalArgumentException("Dice count must be non-negative, got: " + count);
        }
        if (sides < 1) {
            throw new IllegalArgumentException("Dice must have at least 1 side, got: " + sides);
        }
        int sum = 0;
        for (int i = 0; i < count; i++) {
            sum += rollDSides(sides);
        }
        return sum;
    }

    /**
     * Roll N dice with X sides each and return individual results.
     *
     * @param count number of dice to roll
     * @param sides number of sides per die
     * @return list of individual dice results
     * @throws IllegalArgumentException if count < 0 or sides < 1
     */
    public static List<Integer> rollMultiple(int count, int sides) {
        if (count < 0) {
            throw new IllegalArgumentException("Dice count must be non-negative, got: " + count);
        }
        if (sides < 1) {
            throw new IllegalArgumentException("Dice must have at least 1 side, got: " + sides);
        }
        List<Integer> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            results.add(ThreadLocalRandom.current().nextInt(1, sides + 1));
        }
        return results;
    }

    /**
     * Roll 2D6 with a modifier applied to the result.
     * Common pattern in Traveller skill checks.
     *
     * @param modifier bonus or penalty to add to the roll
     * @return 2D6 roll result plus modifier
     */
    public static int roll2D6WithModifier(int modifier) {
        return roll2D6() + modifier;
    }

    /**
     * Roll a percentile (2D10) result (range 1-100).
     * Used for skill checks and random tables.
     *
     * @return value between 1 and 100 inclusive
     */
    public static int rollPercentile() {
        return ThreadLocalRandom.current().nextInt(1, 101);
    }

    /**
     * Return a zero-based random index into an array or list of the given size.
     * Use this instead of {@link #rollDSides(int)} when selecting array/list elements.
     *
     * @param size number of elements (must be >= 1)
     * @return value between 0 and size-1 inclusive
     * @throws IllegalArgumentException if size < 1
     */
    public static int rollIndex(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be at least 1, got: " + size);
        }
        return ThreadLocalRandom.current().nextInt(size);
    }
}
