package com.yisy.modules.doramcu.hex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single hexagonal cell in a hex map.
 * <p>
 * Each hex uses axial coordinates (q, r, s) where q + r + s = 0.
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
public class Hex {

    // Transition table: maps (state, event) -> newState for fast lookups without StateMachine overhead
    private static final HexState[][] TRANSITIONS = buildTransitionTable();

    private static HexState[][] buildTransitionTable() {
        HexState[][] table = new HexState[HexState.values().length][HexEvent.values().length];
        table[HexState.EMPTY.ordinal()][HexEvent.PLACE.ordinal()] = HexState.OCCUPIED;
        table[HexState.EMPTY.ordinal()][HexEvent.BLOCK.ordinal()] = HexState.BLOCKED;
        table[HexState.EMPTY.ordinal()][HexEvent.DESTROY.ordinal()] = HexState.DESTROYED;
        table[HexState.OCCUPIED.ordinal()][HexEvent.REMOVE.ordinal()] = HexState.EMPTY;
        table[HexState.OCCUPIED.ordinal()][HexEvent.SELECT.ordinal()] = HexState.SELECTED;
        table[HexState.OCCUPIED.ordinal()][HexEvent.DESTROY.ordinal()] = HexState.DESTROYED;
        table[HexState.SELECTED.ordinal()][HexEvent.DESELECT.ordinal()] = HexState.OCCUPIED;
        table[HexState.SELECTED.ordinal()][HexEvent.DESTROY.ordinal()] = HexState.DESTROYED;
        table[HexState.BLOCKED.ordinal()][HexEvent.UNBLOCK.ordinal()] = HexState.EMPTY;
        table[HexState.BLOCKED.ordinal()][HexEvent.DESTROY.ordinal()] = HexState.DESTROYED;
        table[HexState.DESTROYED.ordinal()][HexEvent.REPAIR.ordinal()] = HexState.EMPTY;
        return table;
    }

    private static final List<HexEvent>[] POSSIBLE_EVENTS = new List[HexState.values().length];
    static {
        POSSIBLE_EVENTS[HexState.EMPTY.ordinal()] = Arrays.asList(HexEvent.PLACE, HexEvent.BLOCK, HexEvent.DESTROY);
        POSSIBLE_EVENTS[HexState.OCCUPIED.ordinal()] = Arrays.asList(HexEvent.REMOVE, HexEvent.SELECT, HexEvent.DESTROY);
        POSSIBLE_EVENTS[HexState.SELECTED.ordinal()] = Arrays.asList(HexEvent.DESELECT, HexEvent.DESTROY);
        POSSIBLE_EVENTS[HexState.BLOCKED.ordinal()] = Arrays.asList(HexEvent.UNBLOCK, HexEvent.DESTROY);
        POSSIBLE_EVENTS[HexState.DESTROYED.ordinal()] = Collections.singletonList(HexEvent.REPAIR);
    }

    private final int q;
    private final int r;
    private final int s;
    private HexState state = HexState.EMPTY;
    private Object payload;

    /**
     * Creates a hex cell at the given axial coordinates.
     *
     * @param q the q coordinate (column)
     * @param r the r coordinate (row)
     * @param s the s coordinate (derived: -q-r, used for convenience)
     */
    public Hex(int q, int r, int s) {
        this.q = q;
        this.r = r;
        this.s = s;
    }

    /**
     * Creates a hex cell; the s coordinate is derived as {@code -q - r}.
     *
     * @param q the q coordinate
     * @param r the r coordinate
     */
    public Hex(int q, int r) {
        this(q, r, -q - r);
    }

    /**
     * Creates a hex cell at the origin (0, 0, 0).
     */
    public Hex() {
        this(0, 0, 0);
    }

    // ---- Coordinate accessors ----

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public int getS() {
        return s;
    }

    // ---- State machine operations ----

    /**
     * @return the current state of this hex cell
     */
    public HexState getState() {
        return state;
    }

    /**
     * @return true if the hex is in the given state
     */
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

    /**
     * @return the list of events that can be processed in the current state
     */
    public List<HexEvent> getPossibleEvents() {
        return POSSIBLE_EVENTS[this.state.ordinal()];
    }

    /**
     * Resets this hex to its initial state (EMPTY).
     */
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

    /**
     * @return the payload stored on this hex, or null if empty
     */
    public Object getPayload() {
        return payload;
    }

    // ---- Hex arithmetic ----

    /**
     * Returns the neighbor hex in the given direction.
     *
     * @param direction the direction to move
     * @return a new Hex at the neighbor position
     */
    public Hex neighbor(HexDirection direction) {
        return new Hex(q + direction.getDq(), r + direction.getDr());
    }

    /**
     * Returns all six neighboring hexes.
     */
    public List<Hex> neighbors() {
        List<Hex> result = new java.util.ArrayList<>(6);
        for (HexDirection dir : HexDirection.values()) {
            result.add(neighbor(dir));
        }
        return result;
    }

    /**
     * Computes the hex distance (number of steps) between this hex and another.
     *
     * @param other the other hex
     * @return the distance in hex steps
     */
    public int distance(Hex other) {
        return (Math.abs(this.q - other.q) + Math.abs(this.r - other.r) + Math.abs(this.s - other.s)) / 2;
    }

    /**
     * Returns a linear interpolation between this hex and another.
     *
     * @param other the target hex
     * @param t     interpolation factor (0.0 = this, 1.0 = other)
     * @return the interpolated hex (rounded to the nearest valid hex)
     */
    public Hex lerp(Hex other, double t) {
        double lq = this.q + (other.q - this.q) * t;
        double lr = this.r + (other.r - this.r) * t;
        double ls = this.s + (other.s - this.s) * t;

        int iq = (int) Math.round(lq);
        int ir = (int) Math.round(lr);
        int is = (int) Math.round(ls);

        double dq = Math.abs(iq - lq);
        double dr = Math.abs(ir - lr);
        double ds = Math.abs(is - ls);

        if (dq > dr && dq > ds) {
            iq = -ir - is;
        } else if (dr > ds) {
            ir = -iq - is;
        } else {
            is = -iq - ir;
        }
        return new Hex(iq, ir, is);
    }

    /**
     * Returns a list of hexes forming a straight line from this hex to another.
     *
     * @param other the target hex
     * @return list of hexes on the line (inclusive)
     */
    public List<Hex> line(Hex other) {
        List<Hex> result = new java.util.ArrayList<>();
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
        Hex hex = (Hex) o;
        return q == hex.q && r == hex.r && s == hex.s;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r, s);
    }

    @Override
    public String toString() {
        return String.format("Hex(%d, %d, %d)[%s]", q, r, s, state);
    }
}
