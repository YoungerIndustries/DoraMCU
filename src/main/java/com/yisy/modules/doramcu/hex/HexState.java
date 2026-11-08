package com.yisy.modules.doramcu.hex;

/**
 * Represents the lifecycle state of a {@link Hex} cell.
 * <p>
 * States and transitions:
 * <pre>
 *          PLACE        SELECT
 *  EMPTY ──────► OCCUPIED ──────► SELECTED
 *   │              │                  │
 *   │ BLOCK        │ REMOVE           │ DESELECT
 *   │              │                  │
 *   ▼              ▼                  ▼
 *  BLOCKED      EMPTY ◄──────────┘
 *   │
 *   │ UNBLOCK
 *   ▼
 *  EMPTY
 * </pre>
 */
public enum HexState {
    /** Cell is empty and available for placement. */
    EMPTY,
    /** Cell is occupied by an entity. */
    OCCUPIED,
    /** Cell is occupied and currently selected. */
    SELECTED,
    /** Cell is blocked and cannot be occupied. */
    BLOCKED,
    /** Cell is destroyed and must be repaired before use. */
    DESTROYED
}
