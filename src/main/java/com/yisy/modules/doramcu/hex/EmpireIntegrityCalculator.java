package com.yisy.modules.doramcu.hex;

/**
 * Calculates empire integrity as a percentage of undestroyed hull cells.
 * <p>
 * Extracted from Game to separate the integrity computation from the core
 * game controller. The result is cached until marked dirty.
 * </p>
 */
public class EmpireIntegrityCalculator {

    private volatile double cachedIntegrity = 100.0;
    private volatile boolean dirty = true;

    /**
     * Marks the cached integrity as dirty so it will be recomputed
     * on the next call to {@link #calculate(Game)}.
     */
    public void markDirty() {
        dirty = true;
    }

    /**
     * Returns the empire integrity as a percentage (0-100).
     * Computes the value on demand and caches it until marked dirty.
     *
     * @param game the game to calculate integrity for
     * @return integrity percentage (100 = no damage, 0 = all destroyed)
     */
    public double calculate(Game game) {
        if (!dirty) {
            return cachedIntegrity;
        }
        int totalCells = 0;
        int destroyedCells = 0;
        for (Fleet fleet : game.getFleets().values()) {
            for (Ship ship : fleet.getShips()) {
                for (HullModule module : ship.getHullModules()) {
                    for (M3space space : module.getShapeCells()) {
                        totalCells++;
                        if (space.isDestroyed()) {
                            destroyedCells++;
                        }
                    }
                }
            }
        }
        cachedIntegrity = totalCells > 0
                ? (1.0 - ((double) destroyedCells / totalCells)) * 100.0
                : 100.0;
        dirty = false;
        return cachedIntegrity;
    }
}
