package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a 3D map of {@link M3space} cells arranged in a 10×10×10 volume.
 * <p>
 * A {@code HullModule} is a fixed-size 3D hex map used to model a ship hull module
 * or any bounded 3D space. It contains 1000 M3space cells arranged in a 10×10×10 cube,
 * providing O(1) lookup by coordinates.
 * <p>
 * The grid coordinates range from (0, 0, 0) to (9, 9, 9), covering all three axes.
 *
 * <pre>
 *   Z (depth)
 *   |
 *   |  Layer 9  +------------------+
 *   |           | .  .  .  .  .  . |
 *   |  Layer 8  | .  .  .  .  .  . |
 *   |           | ... (10 layers)  |
 *   |  Layer 1  | .  .  .  .  .  . |
 *   |           | .  .  .  .  .  . |
 *   |  Layer 0  +------------------+
 *   |           0  1  2  3  4  5  6  7  8  9  -> X
 *   +-> Y
 * </pre>
 */
public class HullModule {

    public static final int WIDTH = 10;
    public static final int HEIGHT = 10;
    public static final int DEPTH = 10;
    public static final int SIZE = WIDTH * HEIGHT * DEPTH;

    private final Map<M3space, M3space> cells;
    private final String name;
    private HullShape shape;

    /**
     * Creates a new HullModule with the given name, initialized with a 10×10×10 grid of M3space cells.
     * Default shape is CUBE (all cells included).
     *
     * @param name the name of this hull module
     */
    public HullModule(String name) {
        this.name = name;
        this.cells = new HashMap<>();
        this.shape = HullShape.CUBE;
        initializeGrid();
    }

    /**
     * Creates a new unnamed HullModule with a 10×10×10 grid of M3space cells.
     */
    public HullModule() {
        this("HullModule");
    }

    /**
     * Initializes the 10×10×10 grid of M3space cells.
     * Exterior cells (on the shape's surface) default to Aluminum (material 1).
     * Interior cells (fully surrounded within the shape) default to Empty (material 0).
     * Cells outside the shape default to Empty.
     */
    private void initializeGrid() {
        for (int z = 0; z < DEPTH; z++) {
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    Material material;
                    if (shape.contains(x, y, z)) {
                        if (shape.isExterior(x, y, z)) {
                            material = Material.Aluminum;
                        } else {
                            material = Material.Empty;
                        }
                    } else {
                        material = Material.Empty;
                    }
                    M3space cell = new M3space(x, y, z, material);
                    cells.put(cell, cell);
                }
            }
        }
    }

    // ---- Name accessors ----

    /** @return the name of this hull module */
    public String getName() {
        return name;
    }

    // ---- Grid dimensions ----

    /** @return the width of the grid (10) */
    public int getWidth() {
        return WIDTH;
    }

    /** @return the height of the grid (10) */
    public int getHeight() {
        return HEIGHT;
    }

    /** @return the depth of the grid (10) */
    public int getDepth() {
        return DEPTH;
    }

    // ---- Shape operations ----

    /**
     * Returns the current shape of this hull module.
     *
     * @return the current shape (default is CUBE)
     */
    public HullShape getShape() {
        return shape;
    }

    /**
     * Sets the shape of this hull module, determining which cells are considered part of the hull.
     * Cells outside the shape remain in the grid but are excluded from shape queries.
     *
     * @param newShape the shape to apply
     */
    public void setShape(HullShape newShape) {
        if (newShape == null) {
            throw new IllegalArgumentException("Shape must not be null");
        }
        this.shape = newShape;
    }

    /**
     * Checks whether the cell at the given coordinates is within the current shape.
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     * @return true if the cell is within the current shape boundaries
     */
    public boolean isShapeCell(int x, int y, int z) {
        return shape.contains(x, y, z);
    }

    /**
     * Checks whether the given cell is within the current shape.
     *
     * @param space the cell to check
     * @return true if the cell is within the current shape boundaries
     */
    public boolean isShapeCell(M3space space) {
        return isShapeCell(space.getX(), space.getY(), space.getZ());
    }

    /**
     * Returns all cells that are within the current shape.
     *
     * @return list of cells inside the shape boundaries
     */
    public List<M3space> getShapeCells() {
        return cells.values().stream()
                .filter(cell -> shape.contains(cell.getX(), cell.getY(), cell.getZ()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of cells within the current shape.
     *
     * @return count of cells inside the shape boundaries
     */
    public int getShapeCellCount() {
        return (int) cells.values().stream()
                .filter(cell -> shape.contains(cell.getX(), cell.getY(), cell.getZ()))
                .count();
    }

    // ---- Exterior / Interior operations ----

    /**
     * Checks whether the cell at the given coordinates is on the exterior surface of the current shape.
     * A cell is exterior if it is within the shape and at least one of its 6 face neighbors is outside the shape.
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     * @return true if the cell is on the exterior surface of the current shape
     */
    public boolean isExteriorCell(int x, int y, int z) {
        return shape.isExterior(x, y, z);
    }

    /**
     * Checks whether the given cell is on the exterior surface of the current shape.
     *
     * @param space the cell to check
     * @return true if the cell is on the exterior surface of the current shape
     */
    public boolean isExteriorCell(M3space space) {
        return isExteriorCell(space.getX(), space.getY(), space.getZ());
    }

    /**
     * Checks whether the cell at the given coordinates is an interior cell of the current shape.
     * A cell is interior if it is within the shape and all 6 face neighbors are also within the shape.
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     * @return true if the cell is an interior cell of the current shape
     */
    public boolean isInteriorCell(int x, int y, int z) {
        return shape.isInterior(x, y, z);
    }

    /**
     * Checks whether the given cell is an interior cell of the current shape.
     *
     * @param space the cell to check
     * @return true if the cell is an interior cell of the current shape
     */
    public boolean isInteriorCell(M3space space) {
        return isInteriorCell(space.getX(), space.getY(), space.getZ());
    }

    /**
     * Returns all cells on the exterior surface of the current shape.
     *
     * @return list of exterior cells
     */
    public List<M3space> getExteriorCells() {
        return cells.values().stream()
                .filter(cell -> shape.isExterior(cell.getX(), cell.getY(), cell.getZ()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of exterior cells in the current shape.
     *
     * @return count of exterior cells
     */
    public int getExteriorCellCount() {
        return (int) cells.values().stream()
                .filter(cell -> shape.isExterior(cell.getX(), cell.getY(), cell.getZ()))
                .count();
    }

    /**
     * Returns all interior cells of the current shape.
     *
     * @return list of interior cells
     */
    public List<M3space> getInteriorCells() {
        return cells.values().stream()
                .filter(cell -> shape.isInterior(cell.getX(), cell.getY(), cell.getZ()))
                .collect(Collectors.toList());
    }

    // ---- CRUD operations ----

    /**
     * Retrieves an M3space cell by its 3D coordinates.
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     * @return the cell at the given position, or null if out of bounds
     */
    public M3space get(int x, int y, int z) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || z < 0 || z >= DEPTH) {
            return null;
        }
        return cells.get(new M3space(x, y, z));
    }

    /**
     * Retrieves an M3space cell by its 2D coordinates on the Z=0 layer (backward compatibility).
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @return the cell at the given position on layer 0, or null if out of bounds
     */
    public M3space get(int x, int y) {
        return get(x, y, 0);
    }

    /**
     * Retrieves an M3space cell by another M3space instance.
     *
     * @param space the space to look up
     * @return the stored cell, or null if not present
     */
    public M3space get(M3space space) {
        return cells.get(space);
    }

    /**
     * @return true if the grid contains a cell at the given 3D coordinates
     */
    public boolean contains(int x, int y, int z) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && z >= 0 && z < DEPTH
                && cells.containsKey(new M3space(x, y, z));
    }

    /**
     * @return true if the grid contains a cell at the given 2D coordinates on layer Z=0
     */
    public boolean contains(int x, int y) {
        return contains(x, y, 0);
    }

    /**
     * @return all cells in the grid
     */
    public Collection<M3space> getAll() {
        return Collections.unmodifiableCollection(cells.values());
    }

    /**
     * @return the number of cells in the grid (always 1000)
     */
    public int size() {
        return cells.size();
    }

    // ---- Material operations ----

    /**
     * Changes the material of all exterior cells (on the shape's surface) to the specified material.
     * Exterior cells are those within the shape that have at least one face neighbor outside the shape.
     *
     * @param material the new material for exterior cells
     */
    public void setExteriorMaterial(Material material) {
        for (M3space cell : getExteriorCells()) {
            cell.setMaterial(material);
        }
    }

    /**
     * Changes the material of all interior cells to the specified material.
     * Interior cells are those within the shape that are fully surrounded by other shape cells.
     *
     * @param material the new material for interior cells
     */
    public void setInteriorMaterial(Material material) {
        for (M3space cell : getInteriorCells()) {
            cell.setMaterial(material);
        }
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HullModule that = (HullModule) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return String.format("HullModule{name='%s', size=%d}", name, cells.size());
    }
}
