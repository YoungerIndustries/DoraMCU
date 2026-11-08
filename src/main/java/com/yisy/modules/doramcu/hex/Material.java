package com.yisy.modules.doramcu.hex;

/**
 * Represents the material type of an {@link M3space} cell.
 * <p>
 * Each material is identified by a unique integer value.
 */
public enum Material {
    /** No material, value = 0 */
    Empty(0),

    /** Lightweight metal, value = 1 */
    Aluminum(1),

    /** Standard structural metal, value = 2 */
    Steel(2),

    /** Strong and lightweight alloy, value = 3 */
    Titanium(3),

    /** Advanced blended material, value = 4 */
    Composite(4),

    /** Volatile material, value = 5 */
    Explosive(5),

    /** Advanced crystal-based armor material, value = 6 */
    Crystaliron(6),

    /** Ultra-dense bonded armor material, value = 7 */
    Superdense(7);

    private final int value;

    Material(int value) {
        this.value = value;
    }

    /** @return the integer identifier for this material */
    public int getValue() {
        return value;
    }

    /**
     * Returns the Material corresponding to the given integer value.
     *
     * @param value the integer value (0-5)
     * @return the matching Material
     * @throws IllegalArgumentException if no material matches the value
     */
    public static Material fromValue(int value) {
        for (Material material : values()) {
            if (material.value == value) {
                return material;
            }
        }
        throw new IllegalArgumentException("No material with value: " + value);
    }
}
