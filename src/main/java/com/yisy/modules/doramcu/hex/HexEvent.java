package com.yisy.modules.doramcu.hex;

/**
 * Events that trigger state transitions on a hex cell.
 */
public enum HexEvent {
    /** Place an entity on an empty hex → OCCUPIED */
    PLACE,

    /** Remove an entity from an occupied hex → EMPTY */
    REMOVE,

    /** Block an empty hex so it cannot be occupied → BLOCKED */
    BLOCK,

    /** Unblock a blocked hex → EMPTY */
    UNBLOCK,

    /** Select an occupied hex → SELECTED */
    SELECT,

    /** Deselect a selected hex → OCCUPIED */
    DESELECT,

    /** Destroy a hex cell → DESTROYED (can be triggered from any state) */
    DESTROY,

    /** Repair a destroyed hex cell → EMPTY */
    REPAIR
}
