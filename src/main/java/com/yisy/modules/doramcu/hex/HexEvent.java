package com.yisy.modules.doramcu.hex;

/**
 * Events that trigger state transitions in a {@link Hex} cell's lifecycle.
 * <p>
 * Each event is valid only in certain states; invalid transitions are silently ignored.
 */
public enum HexEvent {
    /** Place an entity on an empty hex (EMPTY → OCCUPIED). */
    PLACE,
    /** Remove the entity from an occupied hex (OCCUPIED → EMPTY). */
    REMOVE,
    /** Select an occupied hex (OCCUPIED → SELECTED). */
    SELECT,
    /** Deselect a selected hex (SELECTED → OCCUPIED). */
    DESELECT,
    /** Block an empty hex so it cannot be occupied (EMPTY → BLOCKED). */
    BLOCK,
    /** Unblock a blocked hex (BLOCKED → EMPTY). */
    UNBLOCK,
    /** Destroy a hex in any occupied state (→ DESTROYED). */
    DESTROY,
    /** Repair a destroyed hex (DESTROYED → EMPTY). */
    REPAIR
}
