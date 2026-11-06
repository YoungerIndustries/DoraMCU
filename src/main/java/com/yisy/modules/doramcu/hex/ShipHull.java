package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the 3D hull module grid for a ship.
 * <p>
 * A ship's hull is represented as one or more {@link HullModule} instances arranged in a 3D grid.
 * Each module is a 10×10×10 M3space grid where components are installed. This class handles
 * module initialization, component installation/removal at M3space positions, hull integrity
 * calculations, and hull repair operations.
 * <p>
 * Component installation verifies that target cells are within the hull shape and in EMPTY state
 * before placing the component. Removal frees all occupied cells back to EMPTY state.
 */
public class ShipHull {

    private final Ship ship;
    private final List<HullModule> hullModules;
    private int[] moduleGridDimensions;
    private final Map<ShipComponent, List<M3space>> componentSpaces;

    /**
     * Creates hull management for the given ship.
     *
     * @param ship the ship this hull belongs to
     */
    public ShipHull(Ship ship) {
        this.ship = ship;
        this.hullModules = new ArrayList<>();
        this.componentSpaces = new HashMap<>();
        initializeModules();
    }

    // ---- Initialization ----

    private void initializeModules() {
        ShipType shipType = ship.getShipType();
        this.moduleGridDimensions = shipType.getModuleGridDimensions();
        int moduleCount = shipType.getHullModuleCount();
        for (int i = 0; i < moduleCount; i++) {
            HullModule module = new HullModule(ship.getName() + "-Module-" + i);
            hullModules.add(module);
        }
    }

    // ---- Accessors ----

    public List<HullModule> getHullModules() {
        return List.copyOf(hullModules);
    }

    public int[] getModuleGridDimensions() {
        return moduleGridDimensions.clone();
    }

    public int getHullModuleCount() {
        return hullModules.size();
    }

    public Map<ShipComponent, List<M3space>> getComponentSpaces() {
        return Map.copyOf(componentSpaces);
    }

    // ---- Module management ----

    public void setHullModules(List<HullModule> modules) {
        if (modules == null) {
            throw new IllegalArgumentException("Modules must not be null");
        }
        hullModules.clear();
        hullModules.addAll(modules);
    }

    public HullModule getModuleAt(int mx, int my, int mz) {
        if (mx < 0 || mx >= moduleGridDimensions[0] ||
            my < 0 || my >= moduleGridDimensions[1] ||
            mz < 0 || mz >= moduleGridDimensions[2]) {
            return null;
        }
        int index = mz * moduleGridDimensions[0] * moduleGridDimensions[1] +
                    my * moduleGridDimensions[0] + mx;
        if (index < hullModules.size()) {
            return hullModules.get(index);
        }
        return null;
    }

    public HullModule getModuleAt(int index) {
        if (index < 0 || index >= hullModules.size()) {
            return null;
        }
        return hullModules.get(index);
    }

    // ---- Component installation ----

    /**
     * Finds the module and local coordinates for a global M3space position.
     */
    public Map<String, Object> findModuleForPosition(int globalX, int globalY, int globalZ) {
        int mx = globalX / HullModule.WIDTH;
        int my = globalY / HullModule.HEIGHT;
        int mz = globalZ / HullModule.DEPTH;
        int localX = globalX % HullModule.WIDTH;
        int localY = globalY % HullModule.HEIGHT;
        int localZ = globalZ % HullModule.DEPTH;

        HullModule module = getModuleAt(mx, my, mz);
        if (module == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("module", module);
        result.put("localX", localX);
        result.put("localY", localY);
        result.put("localZ", localZ);
        return result;
    }

    /**
     * Installs a component at specific M3space positions within any hull module.
     */
    public boolean installComponent(ShipComponent component, List<M3space> spaces) {
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null");
        }
        if (spaces == null || spaces.isEmpty()) {
            throw new IllegalArgumentException("Spaces must not be empty");
        }
        if (hullModules.isEmpty()) {
            throw new IllegalStateException("No hull modules assigned");
        }
        if (ship.getComponentsInternal().contains(component)) {
            throw new IllegalStateException("Component already installed");
        }
        if (ship.getOccupiedSlots() >= ship.getMaxComponentSlots()) {
            throw new IllegalStateException("No available component slots");
        }

        for (M3space space : spaces) {
            HullModule module = findModuleForSpace(space);
            if (module == null || !module.isShapeCell(space)) {
                throw new IllegalArgumentException("Space " + space + " is outside the hull shape");
            }
            M3space stored = module.get(space);
            if (stored == null || !stored.isInState(HexState.EMPTY)) {
                throw new IllegalArgumentException("Space " + space + " is not available");
            }
        }

        for (M3space space : spaces) {
            HullModule module = findModuleForSpace(space);
            if (module != null) {
                M3space stored = module.get(space);
                if (stored != null) {
                    stored.place(component);
                }
            }
        }

        // Directly add to ship's component list to avoid circular call
        ship.getComponentsInternal().add(component);
        componentSpaces.put(component, new ArrayList<>(spaces));
        return true;
    }

    /**
     * Installs a component at a single M3space position within the first hull module.
     */
    public boolean installComponent(ShipComponent component, int x, int y, int z) {
        if (hullModules.isEmpty()) {
            throw new IllegalStateException("No hull modules assigned");
        }
        HullModule module = hullModules.get(0);
        M3space space = module.get(x, y, z);
        if (space == null) {
            throw new IllegalArgumentException("Position (" + x + ", " + y + ", " + z + ") is out of bounds");
        }
        return installComponent(component, List.of(space));
    }

    /**
     * Installs a component at a single M3space position within a specific hull module.
     */
    public boolean installComponent(ShipComponent component, int moduleIndex, int x, int y, int z) {
        HullModule module = getModuleAt(moduleIndex);
        if (module == null) {
            throw new IllegalArgumentException("Module index " + moduleIndex + " is out of bounds");
        }
        M3space space = module.get(x, y, z);
        if (space == null) {
            throw new IllegalArgumentException("Position (" + x + ", " + y + ", " + z + ") is out of bounds");
        }
        return installComponent(component, List.of(space));
    }

    /**
     * Removes an installed component and frees its occupied M3space cells.
     */
    public boolean removeComponent(ShipComponent component) {
        List<M3space> spaces = componentSpaces.remove(component);
        if (spaces != null) {
            for (M3space space : spaces) {
                HullModule module = findModuleForSpace(space);
                if (module != null) {
                    M3space stored = module.get(space);
                    if (stored != null) {
                        stored.remove();
                    }
                }
            }
        }
        // Directly remove from ship's component list to avoid circular call
        boolean removed = ship.getComponentsInternal().remove(component);
        if (removed) {
            ship.recalculateStatsInternal();
        }
        return removed;
    }

    /**
     * Clears all components and frees their occupied M3space cells.
     */
    public void clearAllComponents() {
        for (List<M3space> spaces : componentSpaces.values()) {
            for (M3space space : spaces) {
                HullModule module = findModuleForSpace(space);
                if (module != null) {
                    M3space stored = module.get(space);
                    if (stored != null) {
                        stored.remove();
                    }
                }
            }
        }
        componentSpaces.clear();
    }

    /**
     * Returns the M3space cells occupied by the given component.
     */
    public List<M3space> getOccupiedSpaces(ShipComponent component) {
        return List.copyOf(componentSpaces.getOrDefault(component, List.of()));
    }

    /**
     * Returns all M3space cells currently occupied by any component.
     */
    public List<M3space> getAllOccupiedSpaces() {
        List<M3space> all = new ArrayList<>();
        for (List<M3space> spaces : componentSpaces.values()) {
            all.addAll(spaces);
        }
        return all;
    }

    /**
     * Checks if a given M3space position is available in the first module.
     */
    public boolean isSpaceAvailable(int x, int y, int z) {
        if (hullModules.isEmpty()) {
            return false;
        }
        HullModule module = hullModules.get(0);
        M3space space = module.get(x, y, z);
        if (space == null) {
            return false;
        }
        return module.isShapeCell(space) && space.isInState(HexState.EMPTY);
    }

    /**
     * Checks if a given M3space position is available in a specific module.
     */
    public boolean isSpaceAvailable(int moduleIndex, int x, int y, int z) {
        HullModule module = getModuleAt(moduleIndex);
        if (module == null) {
            return false;
        }
        M3space space = module.get(x, y, z);
        if (space == null) {
            return false;
        }
        return module.isShapeCell(space) && space.isInState(HexState.EMPTY);
    }

    /**
     * Returns the total number of available M3space cells across all hull modules.
     */
    public int getAvailableSpaces() {
        int count = 0;
        for (HullModule module : hullModules) {
            count += (int) module.getShapeCells().stream()
                    .filter(space -> space.isInState(HexState.EMPTY))
                    .count();
        }
        return count;
    }

    // ---- Hull Integrity ----

    public double getHullIntegrity() {
        if (hullModules.isEmpty()) {
            return ship.getHullHealthRatio();
        }
        int totalCells = 0;
        int destroyedCells = 0;
        for (HullModule module : hullModules) {
            totalCells += module.getShapeCellCount();
            destroyedCells += (int) module.getShapeCells().stream()
                    .filter(M3space::isDestroyed)
                    .count();
        }
        if (totalCells == 0) {
            return 0.0;
        }
        return 1.0 - ((double) destroyedCells / totalCells);
    }

    public int getDestroyedCellCount() {
        int count = 0;
        for (HullModule module : hullModules) {
            count += (int) module.getShapeCells().stream()
                    .filter(M3space::isDestroyed)
                    .count();
        }
        return count;
    }

    public int getDamagedCellCount() {
        int count = 0;
        for (HullModule module : hullModules) {
            count += (int) module.getShapeCells().stream()
                    .filter(space -> space.isDamaged() && !space.isDestroyed())
                    .count();
        }
        return count;
    }

    /**
     * Repairs all destroyed M3space cells across all hull modules.
     */
    public int repairHull() {
        int count = 0;
        for (HullModule module : hullModules) {
            for (M3space space : module.getShapeCells()) {
                if (space.isDestroyed() && space.repair()) {
                    count++;
                }
            }
        }
        return count;
    }

    // ---- Hull shape and material ----

    public int getHullCellCount() {
        int count = 0;
        for (HullModule module : hullModules) {
            count += module.getShapeCellCount();
        }
        return count;
    }

    public HullShape getHullShape() {
        if (hullModules.isEmpty()) {
            return HullShape.CUBE;
        }
        return hullModules.get(0).getShape();
    }

    public void setHullShape(HullShape shape) {
        if (shape == null) {
            throw new IllegalArgumentException("Shape must not be null");
        }
        for (HullModule module : hullModules) {
            module.setShape(shape);
        }
    }

    public Material getExteriorMaterial() {
        Map<Material, Long> breakdown = getHullMaterialBreakdown();
        if (breakdown.isEmpty()) {
            return Material.Aluminum;
        }
        return breakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Material.Aluminum);
    }

    public void setExteriorMaterial(Material material) {
        for (HullModule module : hullModules) {
            module.setExteriorMaterial(material);
        }
    }

    public void setInteriorMaterial(Material material) {
        for (HullModule module : hullModules) {
            module.setInteriorMaterial(material);
        }
    }

    public Map<Material, Long> getHullMaterialBreakdown() {
        Map<Material, Long> breakdown = new java.util.EnumMap<>(Material.class);
        for (HullModule module : hullModules) {
            for (M3space cell : module.getExteriorCells()) {
                Material mat = cell.getMaterial();
                breakdown.merge(mat, 1L, Long::sum);
            }
        }
        return Map.copyOf(breakdown);
    }

    // ---- Internal helper ----

    private HullModule findModuleForSpace(M3space space) {
        for (HullModule module : hullModules) {
            for (M3space stored : module.getAll()) {
                if (stored == space) {
                    return module;
                }
            }
        }
        return null;
    }
}
