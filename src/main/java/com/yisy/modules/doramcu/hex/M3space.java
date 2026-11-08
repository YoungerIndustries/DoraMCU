package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single cell in a 3D hull module grid.
 * <p>
 * Each M3space cell has 3D coordinates (x, y, z) within a HullModule's 10×10×10 volume,
 * a material type, and a state machine that tracks whether the cell is empty, occupied,
 * damaged, or destroyed.
 * </p>
 */
public class M3space {

    private final int x;
    private final int y;
    private final int z;
    private Material material;
    private HexState state = HexState.EMPTY;
    private ShipComponent component;
    private Object payload;
    private int hitpoints;

    /**
     * Creates a new M3space cell at origin with default Aluminum material.
     */
    public M3space() {
        this(0, 0, 0, Material.Aluminum);
    }

    /**
     * Creates a new M3space cell at the given coordinates with the specified material.
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     * @param material the initial material of this cell
     */
    public M3space(int x, int y, int z, Material material) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
        this.hitpoints = material.ordinal();
    }

    /**
     * Creates a new M3space cell at the given coordinates with default Aluminum material.
     * Used for lookup in maps where material is not relevant.
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     */
    public M3space(int x, int y, int z) {
        this(x, y, z, Material.Aluminum);
    }

    // ---- Coordinate accessors ----

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    // ---- Material ----

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
        this.hitpoints = material.ordinal();
    }

    // ---- Hitpoints ----

    /**
     * Returns the current hitpoints of this cell.
     */
    public int getHitpoints() {
        return hitpoints;
    }

    /**
     * Returns the maximum hitpoints based on material.
     */
    public int getMaxHitpoints() {
        return material.ordinal();
    }

    /**
     * Sets the hitpoints, clamped to [0, max].
     */
    public void setHitpoints(int hitpoints) {
        this.hitpoints = Math.max(0, Math.min(hitpoints, getMaxHitpoints()));
    }

    /**
     * Applies damage to this cell.
     *
     * @param damage the amount of damage
     * @return true if the cell was destroyed by this damage
     */
    public boolean damage(int damage) {
        int oldHitpoints = this.hitpoints;
        this.hitpoints = Math.max(0, this.hitpoints - damage);
        if (this.hitpoints == 0 && oldHitpoints > 0) {
            this.state = HexState.DESTROYED;
            this.component = null;
            this.payload = null;
            return true;
        }
        return false;
    }

    // ---- State machine ----

    public HexState getState() {
        return state;
    }

    public boolean isInState(HexState state) {
        return this.state == state;
    }

    public boolean isDestroyed() {
        return state == HexState.DESTROYED;
    }

    /**
     * Returns true if the cell is damaged (hitpoints below max).
     */
    public boolean isDamaged() {
        return hitpoints < getMaxHitpoints() && hitpoints >= 0;
    }

    /**
     * Places a payload on this cell (EMPTY → OCCUPIED).
     *
     * @param payload the payload to place
     * @return true if placement succeeded
     */
    public boolean place(String payload) {
        if (state == HexState.EMPTY) {
            this.state = HexState.OCCUPIED;
            this.payload = payload;
            return true;
        }
        return false;
    }

    /**
     * Places a component on this cell (EMPTY → OCCUPIED).
     *
     * @param component the component to place
     */
    public void place(ShipComponent component) {
        if (state == HexState.EMPTY) {
            this.state = HexState.OCCUPIED;
            this.component = component;
        }
    }

    /**
     * Removes the payload from this cell (OCCUPIED → EMPTY).
     *
     * @return the removed payload, or null if empty
     */
    public Object remove() {
        if (state == HexState.OCCUPIED || state == HexState.SELECTED) {
            Object p = this.payload;
            this.state = HexState.EMPTY;
            this.payload = null;
            this.component = null;
            return p;
        }
        return null;
    }

    /**
     * Blocks this cell (EMPTY → BLOCKED).
     *
     * @return true if blocking succeeded
     */
    public boolean block() {
        if (state == HexState.EMPTY) {
            this.state = HexState.BLOCKED;
            return true;
        }
        return false;
    }

    /**
     * Unblocks this cell (BLOCKED → EMPTY).
     *
     * @return true if unblocking succeeded
     */
    public boolean unblock() {
        if (state == HexState.BLOCKED) {
            this.state = HexState.EMPTY;
            return true;
        }
        return false;
    }

    /**
     * Selects this cell (OCCUPIED → SELECTED).
     *
     * @return true if selection succeeded
     */
    public boolean select() {
        if (state == HexState.OCCUPIED) {
            this.state = HexState.SELECTED;
            return true;
        }
        return false;
    }

    /**
     * Deselects this cell (SELECTED → OCCUPIED).
     *
     * @return true if deselection succeeded
     */
    public boolean deselect() {
        if (state == HexState.SELECTED) {
            this.state = HexState.OCCUPIED;
            return true;
        }
        return false;
    }

    /**
     * Destroys this cell (any state → DESTROYED).
     *
     * @return true if destruction succeeded (cell was not already destroyed)
     */
    public boolean destroy() {
        if (state == HexState.DESTROYED) {
            return false;
        }
        this.state = HexState.DESTROYED;
        this.component = null;
        this.payload = null;
        this.hitpoints = 0;
        return true;
    }

    /**
     * Repairs a destroyed cell (DESTROYED → EMPTY).
     *
     * @return true if the cell was repaired
     */
    public boolean repair() {
        if (state == HexState.DESTROYED) {
            this.state = HexState.EMPTY;
            this.component = null;
            this.payload = null;
            this.hitpoints = getMaxHitpoints();
            return true;
        }
        return false;
    }

    /**
     * Resets this cell to EMPTY state, clearing payload and component.
     */
    public void reset() {
        this.state = HexState.EMPTY;
        this.component = null;
        this.payload = null;
        this.hitpoints = getMaxHitpoints();
    }

    /**
     * Sends a raw event to the state machine.
     *
     * @param event the event to process
     * @return true if the event caused a state transition
     */
    public boolean sendEvent(HexEvent event) {
        switch (event) {
            case PLACE:
                return place("");
            case REMOVE:
                return remove() != null;
            case SELECT:
                return select();
            case DESELECT:
                return deselect();
            case BLOCK:
                return block();
            case UNBLOCK:
                return unblock();
            case DESTROY:
                return destroy();
            case REPAIR:
                return repair();
            default:
                return false;
        }
    }

    /**
     * Returns the list of events valid in the current state.
     */
    public List<HexEvent> getPossibleEvents() {
        List<HexEvent> events = new ArrayList<>();
        switch (state) {
            case EMPTY:
                events.add(HexEvent.PLACE);
                events.add(HexEvent.BLOCK);
                events.add(HexEvent.DESTROY);
                break;
            case OCCUPIED:
                events.add(HexEvent.REMOVE);
                events.add(HexEvent.SELECT);
                events.add(HexEvent.DESTROY);
                break;
            case SELECTED:
                events.add(HexEvent.DESELECT);
                events.add(HexEvent.DESTROY);
                break;
            case BLOCKED:
                events.add(HexEvent.UNBLOCK);
                events.add(HexEvent.DESTROY);
                break;
            case DESTROYED:
                events.add(HexEvent.REPAIR);
                break;
        }
        return events;
    }

    // ---- Component / Payload ----

    public ShipComponent getComponent() {
        return component;
    }

    /**
     * Returns the payload stored in this cell.
     */
    public Object getPayload() {
        return payload;
    }

    // ---- Neighbor methods ----

    /**
     * Returns a new M3space at the given offset from this cell.
     */
    public M3space neighbor(int dx, int dy, int dz) {
        return new M3space(x + dx, y + dy, z + dz);
    }

    /**
     * Returns the 6 face-adjacent neighbors (±1 in one axis).
     */
    public List<M3space> faceNeighbors() {
        int[][] dirs = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        List<M3space> neighbors = new ArrayList<>(6);
        for (int[] d : dirs) {
            neighbors.add(new M3space(x + d[0], y + d[1], z + d[2]));
        }
        return neighbors;
    }

    /**
     * Returns the 12 edge-adjacent neighbors (±1 in two axes).
     */
    public List<M3space> edgeNeighbors() {
        int[][] dirs = {
            {1,1,0},{1,-1,0},{-1,1,0},{-1,-1,0},
            {1,0,1},{1,0,-1},{-1,0,1},{-1,0,-1},
            {0,1,1},{0,1,-1},{0,-1,1},{0,-1,-1}
        };
        List<M3space> neighbors = new ArrayList<>(12);
        for (int[] d : dirs) {
            neighbors.add(new M3space(x + d[0], y + d[1], z + d[2]));
        }
        return neighbors;
    }

    /**
     * Returns the 8 corner-adjacent neighbors (±1 in all three axes).
     */
    public List<M3space> cornerNeighbors() {
        int[][] dirs = {
            {1,1,1},{1,1,-1},{1,-1,1},{1,-1,-1},
            {-1,1,1},{-1,1,-1},{-1,-1,1},{-1,-1,-1}
        };
        List<M3space> neighbors = new ArrayList<>(8);
        for (int[] d : dirs) {
            neighbors.add(new M3space(x + d[0], y + d[1], z + d[2]));
        }
        return neighbors;
    }

    /**
     * Returns all 26 neighbors (face + edge + corner).
     */
    public List<M3space> allNeighbors() {
        List<M3space> neighbors = new ArrayList<>(26);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    neighbors.add(new M3space(x + dx, y + dy, z + dz));
                }
            }
        }
        return neighbors;
    }

    // ---- Distance methods ----

    /**
     * Returns the Manhattan distance to another cell.
     */
    public int distance(M3space other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y) + Math.abs(z - other.z);
    }

    /**
     * Returns the Chebyshev distance to another cell.
     */
    public int chebyshevDistance(M3space other) {
        return Math.max(Math.abs(x - other.x), Math.max(Math.abs(y - other.y), Math.abs(z - other.z)));
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
        return String.format("M3space(%d,%d,%d)[%s,%s]", x, y, z, material, state);
    }
}
