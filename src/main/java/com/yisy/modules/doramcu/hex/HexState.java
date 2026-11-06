package com.yisy.modules.doramcu.hex;

/**
 * Represents the possible states of a hex cell in a hex map.
 * <p>
 * State transitions follow these rules:
 * <ul>
 *   <li>EMPTY → OCCUPIED (place)</li>
 *   <li>EMPTY → BLOCKED (block)</li>
 *   <li>OCCUPIED → EMPTY (remove)</li>
 *   <li>OCCUPIED → SELECTED (select)</li>
 *   <li>SELECTED → OCCUPIED (deselect)</li>
 *   <li>BLOCKED → EMPTY (unblock)</li>
 *   <li>Any → DESTROYED (destroy)</li>
 *   <li>DESTROYED → EMPTY (repair)</li>
 * </ul>
 */
public enum HexState {
    /** The hex cell is empty and available */
    EMPTY,

    /** The hex cell contains an entity or object */
    OCCUPIED,

    /** The hex cell is blocked and cannot be occupied */
    BLOCKED,

    /** The hex cell is currently selected (highlighted) */
    SELECTED,

    /** The hex cell is destroyed and unusable */
    DESTROYED
}
