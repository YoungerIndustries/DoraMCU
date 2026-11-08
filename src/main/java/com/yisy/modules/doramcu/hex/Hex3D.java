package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single hexagonal cell in a 3D hex map.
 * <p>
 * Each hex uses cubic coordinates (x, y, z) where x + y + z = 0.
 * The cell lifecycle is managed by an internal {@link StateMachine} that enforces
 * valid state transitions between {@link HexState} values triggered by {@link HexEvent} events.
 *
 * <p><b>State transition diagram:</b></p>
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
public class Hex3D {

    // Transition table: maps (state, event) -> newState for fast lookups without StateMachine overhead
    private static final HexState[][] TRANSITIONS = buildTransitionTable();

    private static HexState[][] buildTransitionTable() {
        HexState[][] table = new HexState[HexState.values().length][HexEvent.values().length];
        // EMPTY transitions
        table[HexState.EMPTY.ordinal()][HexEvent.PLACE.ordinal()] = HexState.OCCUPIED;
        table[HexState.EMPTY.ordinal()][HexEvent.BLOCK.ordinal()] = HexState.BLOCKED;
        table[HexState.EMPTY.ordinal()][HexEvent.DESTROY.ordinal()] = HexState.DESTROYED;
        // OCCUPIED transitions
        table[HexState.OCCUPIED.ordinal()][HexEvent.REMOVE.ordinal()] = HexState.EMPTY;
        table[HexState.OCCUPIED.ordinal()][HexEvent.SELECT.ordinal()] = HexState.SELECTED;
        table[HexState.OCCUPIED.ordinal()][HexEvent.DESTROY.ordinal()] = HexState.DESTROYED;
        // SELECTED transitions
        table[HexState.SELECTED.ordinal()][HexEvent.DESELECT.ordinal()] = HexState.OCCUPIED;
        table[HexState.SELECTED.ordinal()][HexEvent.DESTROY.ordinal()] = HexState.DESTROYED;
        // BLOCKED transitions
        table[HexState.BLOCKED.ordinal()][HexEvent.UNBLOCK.ordinal()] = HexState.EMPTY;
        table[HexState.BLOCKED.ordinal()][HexEvent.DESTROY.ordinal()] = HexState.DESTROYED;
        // DESTROYED transitions
        table[HexState.DESTROYED.ordinal()][HexEvent.REPAIR.ordinal()] = HexState.EMPTY;
        return table;
    }

    // Possible events for each state (pre-computed, shared across all instances)
    private static final List<HexEvent>[] POSSIBLE_EVENTS = new List[HexState.values().length];
    static {
        POSSIBLE_EVENTS[HexState.EMPTY.ordinal()] = Arrays.asList(HexEvent.PLACE, HexEvent.BLOCK, HexEvent.DESTROY);
        POSSIBLE_EVENTS[HexState.OCCUPIED.ordinal()] = Arrays.asList(HexEvent.REMOVE, HexEvent.SELECT, HexEvent.DESTROY);
        POSSIBLE_EVENTS[HexState.SELECTED.ordinal()] = Arrays.asList(HexEvent.DESELECT, HexEvent.DESTROY);
        POSSIBLE_EVENTS[HexState.BLOCKED.ordinal()] = Arrays.asList(HexEvent.UNBLOCK, HexEvent.DESTROY);
        POSSIBLE_EVENTS[HexState.DESTROYED.ordinal()] = Collections.singletonList(HexEvent.REPAIR);
    }

    private final int x;
    private final int y;
    private final int z;
    private HexState state = HexState.EMPTY;  // Simple enum field instead of StateMachine instance
    private Object payload;

    /**
     * Creates a 3D hex cell at the given cubic coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate (must satisfy x + y + z = 0)
     * @throws IllegalArgumentException if x + y + z != 0
     */
    public Hex3D(int x, int y, int z) {
        if (x + y + z != 0) {
            throw new IllegalArgumentException("Cubic coordinates must sum to 0: (" + x + ", " + y + ", " + z + ")");
        }
        this.x = x;
        this.y = y;
        this.z = z;
        // state defaults to EMPTY
    }

    /**
     * Creates a 3D hex cell; the z coordinate is derived as {@code -x - y}.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Hex3D(int x, int y) {
        this(x, y, -x - y);
    }

    /**
     * Creates a 3D hex cell at the origin (0, 0, 0).
     */
    public Hex3D() {
        this(0, 0, 0);
    }

    // ---- Coordinate accessors ----

    /** @return the x coordinate */
    public int getX() {
        return x;
    }

    /** @return the y coordinate */
    public int getY() {
        return y;
    }

    /** @return the z coordinate */
    public int getZ() {
        return z;
    }

    // ---- State machine operations ----

    /** @return the current state of this hex cell */
    public HexState getState() {
        return state;
    }

    /** @return true if the hex is in the given state */
    public boolean isInState(HexState state) {
        return this.state == state;
    }

    /**
     * Attempts to process an event and transition to a new state.
     *
     * @param event the event to process
     * @return true if a state transition occurred
     */
    public boolean sendEvent(HexEvent event) {
        HexState newState = TRANSITIONS[this.state.ordinal()][event.ordinal()];
        if (newState != null && newState != this.state) {
            this.state = newState;
            return true;
        }
        return false;
    }

    /** @return the list of events that can be processed in the current state */
    public List<HexEvent> getPossibleEvents() {
        return POSSIBLE_EVENTS[this.state.ordinal()];
    }

    /** Resets this hex to its initial state (EMPTY). */
    public void reset() {
        this.state = HexState.EMPTY;
        this.payload = null;
    }

    // ---- Convenience methods ----

    /**
     * Place an entity on this hex (EMPTY → OCCUPIED).
     *
     * @param payload the entity to place
     * @return true if placed successfully
     */
    public boolean place(Object payload) {
        if (sendEvent(HexEvent.PLACE)) {
            this.payload = payload;
            return true;
        }
        return false;
    }

    /**
     * Remove the entity from this hex (OCCUPIED → EMPTY).
     *
     * @return the removed payload, or null if not occupied
     */
    public Object remove() {
        if (sendEvent(HexEvent.REMOVE)) {
            Object p = this.payload;
            this.payload = null;
            return p;
        }
        return null;
    }

    /**
     * Block this hex so it cannot be occupied (EMPTY → BLOCKED).
     *
     * @return true if blocked successfully
     */
    public boolean block() {
        return sendEvent(HexEvent.BLOCK);
    }

    /**
     * Unblock this hex (BLOCKED → EMPTY).
     *
     * @return true if unblocked successfully
     */
    public boolean unblock() {
        return sendEvent(HexEvent.UNBLOCK);
    }

    /**
     * Select this hex (OCCUPIED → SELECTED).
     *
     * @return true if selected successfully
     */
    public boolean select() {
        return sendEvent(HexEvent.SELECT);
    }

    /**
     * Deselect this hex (SELECTED → OCCUPIED).
     *
     * @return true if deselected successfully
     */
    public boolean deselect() {
        return sendEvent(HexEvent.DESELECT);
    }

    /** @return the payload stored on this hex, or null if empty */
    public Object getPayload() {
        return payload;
    }

    // ---- 3D Hex arithmetic ----

    /**
     * Returns the neighbor hex in the given direction.
     *
     * @param direction the direction to move
     * @return a new Hex3D at the neighbor position
     */
    public Hex3D neighbor(Hex3DDirection direction) {
        return new Hex3D(x + direction.getDx(), y + direction.getDy(), z + direction.getDz());
    }

    /**
     * Returns all six face-adjacent neighboring hexes.
     */
    public List<Hex3D> neighbors() {
        List<Hex3D> result = new ArrayList<>(6);
        for (Hex3DDirection dir : Hex3DDirection.values()) {
            result.add(neighbor(dir));
        }
        return result;
    }

    /**
     * Computes the hex distance (number of steps) between this hex and another.
     * <p>
     * For cubic coordinates, the distance is half the sum of absolute differences.
     *
     * @param other the other hex
     * @return the distance in hex steps
     */
    public int distance(Hex3D other) {
        return (Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z)) / 2;
    }

    /**
     * Returns a linear interpolation between this hex and another.
     *
     * @param other the target hex
     * @param t     interpolation factor (0.0 = this, 1.0 = other)
     * @return the interpolated hex (rounded to the nearest valid hex)
     */
    public Hex3D lerp(Hex3D other, double t) {
        double lx = this.x + (other.x - this.x) * t;
        double ly = this.y + (other.y - this.y) * t;
        double lz = this.z + (other.z - this.z) * t;

        int ix = (int) Math.round(lx);
        int iy = (int) Math.round(ly);
        int iz = (int) Math.round(lz);

        double dx = Math.abs(ix - lx);
        double dy = Math.abs(iy - ly);
        double dz = Math.abs(iz - lz);

        if (dx > dy && dx > dz) {
            ix = -iy - iz;
        } else if (dy > dz) {
            iy = -ix - iz;
        } else {
            iz = -ix - iy;
        }
        return new Hex3D(ix, iy, iz);
    }

    /**
     * Returns a list of hexes forming a straight line from this hex to another.
     *
     * @param other the target hex
     * @return list of hexes on the line (inclusive)
     */
    public List<Hex3D> line(Hex3D other) {
        List<Hex3D> result = new ArrayList<>();
        int steps = distance(other);
        for (int i = 0; i <= steps; i++) {
            result.add(lerp(other, (double) i / Math.max(steps, 1)));
        }
        return result;
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hex3D hex3D = (Hex3D) o;
        return x == hex3D.x && y == hex3D.y && z == hex3D.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Hex3D(%d, %d, %d)[%s]", x, y, z, state);
    }
}
