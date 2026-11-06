package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.StateMachine;

import java.util.Objects;

/**
 * Represents a single 3D hexagonal space cell (M3-space) in a three-dimensional hex grid.
 * <p>
 * Unlike {@link Hex3D}, which uses cubic coordinates constrained to a single plane (x + y + z = 0),
 * {@code M3space} uses true 3D cubic coordinates (x, y, z) without the planar constraint,
 * allowing cells to exist in a full three-dimensional volume.
 * <p>
 * Each M3space cell supports 26 face-, edge-, and corner-adjacent neighbors in 3D space,
 * and maintains its own state machine for lifecycle management between {@link HexState} values.
 *
 * <p><b>Coordinate system:</b></p>
 * <pre>
 *   The 3D hex grid uses offset coordinates where each cell is identified by (x, y, z).
 *   - Face neighbors: 6 directions (±1 on one axis, 0 on others)
 *   - Edge neighbors: 12 directions (±1 on two axes, 0 on one)
 *   - Corner neighbors: 8 directions (±1 on all three axes)
 * </pre>
 */
public class M3space {

    private static final StateMachine<HexState, HexEvent> TEMPLATE = createTemplateStateMachine();

    private final int x;
    private final int y;
    private final int z;
    private final StateMachine<HexState, HexEvent> stateMachine;
    private Object payload;
    private Material material;
    private int hitpoints;

    /**
     * Creates an M3space cell at the given 3D coordinates with the specified material.
     * Hitpoints are initialized to the material's default value.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param material the material of this cell
     */
    public M3space(int x, int y, int z, Material material) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
        this.hitpoints = material.getValue();
        this.stateMachine = new StateMachine<>(TEMPLATE);
    }

    /**
     * Creates an M3space cell at the given 3D coordinates with default Aluminum material.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public M3space(int x, int y, int z) {
        this(x, y, z, Material.Aluminum);
    }

    /**
     * Creates an M3space cell at the origin (0, 0, 0) with default Aluminum material.
     */
    public M3space() {
        this(0, 0, 0, Material.Aluminum);
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

    // ---- Material and hitpoints ----

    /** @return the material of this cell */
    public Material getMaterial() {
        return material;
    }

    /**
     * Sets the material of this cell and resets hitpoints to the material's default value.
     *
     * @param material the new material
     */
    public void setMaterial(Material material) {
        this.material = material;
        this.hitpoints = material.getValue();
    }

    /** @return the current hitpoints of this cell */
    public int getHitpoints() {
        return hitpoints;
    }

    /** @return the maximum hitpoints based on the material's value */
    public int getMaxHitpoints() {
        return material.getValue();
    }

    /**
     * Sets the current hitpoints (clamped between 0 and max).
     *
     * @param hitpoints the new hitpoints value
     */
    public void setHitpoints(int hitpoints) {
        this.hitpoints = Math.max(0, Math.min(hitpoints, getMaxHitpoints()));
    }

    /**
     * Deals damage to this cell, reducing hitpoints by the given amount.
     * If hitpoints drop to 0 or below, the cell is automatically destroyed.
     *
     * @param damage the amount of damage to deal
     * @return true if the cell was destroyed by this damage
     */
    public boolean damage(int damage) {
        this.hitpoints = Math.max(0, this.hitpoints - damage);
        if (this.hitpoints <= 0) {
            return destroy();
        }
        return false;
    }

    /** @return true if the cell has any remaining hitpoints */
    public boolean isDamaged() {
        return hitpoints < getMaxHitpoints();
    }

    // ---- State machine operations ----

    /** @return the current state of this M3space cell */
    public HexState getState() {
        return stateMachine.getCurrentState();
    }

    /** @return true if the cell is in the given state */
    public boolean isInState(HexState state) {
        return stateMachine.isInState(state);
    }

    /**
     * Attempts to process an event and transition to a new state.
     *
     * @param event the event to process
     * @return true if a state transition occurred
     */
    public boolean sendEvent(HexEvent event) {
        return stateMachine.sendEvent(event);
    }

    /** @return the list of events that can be processed in the current state */
    public java.util.List<HexEvent> getPossibleEvents() {
        return stateMachine.getPossibleEvents();
    }

    /** Resets this M3space cell to its initial state (EMPTY). */
    public void reset() {
        stateMachine.reset();
        this.payload = null;
    }

    // ---- Convenience methods ----

    /**
     * Place an entity on this M3space cell (EMPTY → OCCUPIED).
     *
     * @param payload the entity to place
     * @return true if placed successfully
     */
    public boolean place(Object payload) {
        if (stateMachine.sendEvent(HexEvent.PLACE)) {
            this.payload = payload;
            return true;
        }
        return false;
    }

    /**
     * Remove the entity from this M3space cell (OCCUPIED → EMPTY).
     *
     * @return the removed payload, or null if not occupied
     */
    public Object remove() {
        if (stateMachine.sendEvent(HexEvent.REMOVE)) {
            Object p = this.payload;
            this.payload = null;
            return p;
        }
        return null;
    }

    /**
     * Block this cell so it cannot be occupied (EMPTY → BLOCKED).
     *
     * @return true if blocked successfully
     */
    public boolean block() {
        return stateMachine.sendEvent(HexEvent.BLOCK);
    }

    /**
     * Unblock this cell (BLOCKED → EMPTY).
     *
     * @return true if unblocked successfully
     */
    public boolean unblock() {
        return stateMachine.sendEvent(HexEvent.UNBLOCK);
    }

    /**
     * Select this cell (OCCUPIED → SELECTED).
     *
     * @return true if selected successfully
     */
    public boolean select() {
        return stateMachine.sendEvent(HexEvent.SELECT);
    }

    /**
     * Deselect this cell (SELECTED → OCCUPIED).
     *
     * @return true if deselected successfully
     */
    public boolean deselect() {
        return stateMachine.sendEvent(HexEvent.DESELECT);
    }

    /** @return the payload stored on this cell, or null if empty */
    public Object getPayload() {
        return payload;
    }

    // ---- 3D Hex arithmetic ----

    /**
     * Returns the neighbor M3space in the given 3D direction.
     *
     * @param dx the x offset
     * @param dy the y offset
     * @param dz the z offset
     * @return a new M3space at the neighbor position
     */
    public M3space neighbor(int dx, int dy, int dz) {
        return new M3space(x + dx, y + dy, z + dz);
    }

    /**
     * Returns all 6 face-adjacent neighboring cells.
     *
     * @return list of 6 face neighbors
     */
    public java.util.List<M3space> faceNeighbors() {
        java.util.List<M3space> result = new java.util.ArrayList<>(6);
        int[] deltas = {1, -1, 0};
        for (int i = 0; i < 3; i++) {
            for (int d : deltas) {
                if (d == 0) continue;
                int nx = x + (i == 0 ? d : 0);
                int ny = y + (i == 1 ? d : 0);
                int nz = z + (i == 2 ? d : 0);
                result.add(new M3space(nx, ny, nz));
            }
        }
        return result;
    }

    /**
     * Returns all 12 edge-adjacent neighboring cells.
     *
     * @return list of 12 edge neighbors
     */
    public java.util.List<M3space> edgeNeighbors() {
        java.util.List<M3space> result = new java.util.ArrayList<>(12);
        int[] axes = {0, 1, 2};
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 3; j++) {
                for (int di : new int[]{1, -1}) {
                    for (int dj : new int[]{1, -1}) {
                        int nx = x + (i == 0 ? di : j == 0 ? dj : 0);
                        int ny = y + (i == 1 ? di : j == 1 ? dj : 0);
                        int nz = z + (i == 2 ? di : j == 2 ? dj : 0);
                        result.add(new M3space(nx, ny, nz));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns all 8 corner-adjacent neighboring cells.
     *
     * @return list of 8 corner neighbors
     */
    public java.util.List<M3space> cornerNeighbors() {
        java.util.List<M3space> result = new java.util.ArrayList<>(8);
        for (int dx : new int[]{1, -1}) {
            for (int dy : new int[]{1, -1}) {
                for (int dz : new int[]{1, -1}) {
                    result.add(new M3space(x + dx, y + dy, z + dz));
                }
            }
        }
        return result;
    }

    /**
     * Returns all 26 neighboring cells (face, edge, and corner).
     *
     * @return list of all 26 neighbors
     */
    public java.util.List<M3space> allNeighbors() {
        java.util.List<M3space> result = new java.util.ArrayList<>(26);
        result.addAll(faceNeighbors());
        result.addAll(edgeNeighbors());
        result.addAll(cornerNeighbors());
        return result;
    }

    /**
     * Computes the Manhattan distance between this cell and another.
     *
     * @param other the other cell
     * @return the Manhattan distance
     */
    public int distance(M3space other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
    }

    /**
     * Computes the Chebyshev distance (max of absolute differences) between this cell and another.
     *
     * @param other the other cell
     * @return the Chebyshev distance
     */
    public int chebyshevDistance(M3space other) {
        return Math.max(Math.abs(this.x - other.x),
                Math.max(Math.abs(this.y - other.y), Math.abs(this.z - other.z)));
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        M3space m3space = (M3space) o;
        return x == m3space.x && y == m3space.y && z == m3space.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("M3space(%d, %d, %d)[%s]%s(%d/%d)", x, y, z, stateMachine.getCurrentState(), material, hitpoints, getMaxHitpoints());
    }

    // ---- State machine factory ----

    /**
     * Destroys this M3space cell, transitioning it to the DESTROYED state.
     * A destroyed cell can be destroyed from any state (EMPTY, OCCUPIED, BLOCKED, SELECTED).
     * Once destroyed, the cell remains destroyed — there is no recovery transition.
     *
     * @return true if the cell was successfully destroyed
     */
    public boolean destroy() {
        if (stateMachine.sendEvent(HexEvent.DESTROY)) {
            this.payload = null;
            return true;
        }
        return false;
    }

    /** @return true if this cell is in the DESTROYED state */
    public boolean isDestroyed() {
        return stateMachine.isInState(HexState.DESTROYED);
    }

    /**
     * Repairs this M3space cell, transitioning it from DESTROYED back to EMPTY.
     * A repaired cell becomes empty and available for use again.
     *
     * @return true if the cell was successfully repaired
     */
    public boolean repair() {
        return stateMachine.sendEvent(HexEvent.REPAIR);
    }

    // ---- State machine factory ----

    private static StateMachine<HexState, HexEvent> createTemplateStateMachine() {
        return StateMachine.<HexState, HexEvent>builder()
                .initialState(HexState.EMPTY)

                // EMPTY transitions
                .onTransition(HexState.EMPTY, HexEvent.PLACE, HexState.OCCUPIED)
                .onTransition(HexState.EMPTY, HexEvent.BLOCK, HexState.BLOCKED)

                // OCCUPIED transitions
                .onTransition(HexState.OCCUPIED, HexEvent.REMOVE, HexState.EMPTY)
                .onTransition(HexState.OCCUPIED, HexEvent.SELECT, HexState.SELECTED)

                // SELECTED transitions
                .onTransition(HexState.SELECTED, HexEvent.DESELECT, HexState.OCCUPIED)

                // BLOCKED transitions
                .onTransition(HexState.BLOCKED, HexEvent.UNBLOCK, HexState.EMPTY)

                // Destroy transitions — any state can be destroyed
                .onTransition(HexState.EMPTY, HexEvent.DESTROY, HexState.DESTROYED)
                .onTransition(HexState.OCCUPIED, HexEvent.DESTROY, HexState.DESTROYED)
                .onTransition(HexState.BLOCKED, HexEvent.DESTROY, HexState.DESTROYED)
                .onTransition(HexState.SELECTED, HexEvent.DESTROY, HexState.DESTROYED)

                // Repair transition — destroyed cell restored to empty
                .onTransition(HexState.DESTROYED, HexEvent.REPAIR, HexState.EMPTY)

                .build();
    }
}
