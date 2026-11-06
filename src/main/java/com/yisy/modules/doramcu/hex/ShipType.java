package com.yisy.modules.doramcu.hex;

/**
 * Represents the classification of a warship, determining base hull points,
 * component slots, build time in turns, MDrive speed, JDrive speed, base evasion, and cubic meters.
 * <p>
 * Ship types follow the 4X tradition where larger ships carry more components
 * but take longer to construct and cost more resources. Smaller ships are
 * cheaper and faster but less capable individually.
 * </p>
 * <p>
 * Ships have two separate drive systems:
 * </p>
 * <ul>
 *   <li><b>MDrive (Maneuver Drive)</b> — Used for movement within a solar system and during combat.
 *       Determines tactical positioning speed and combat maneuverability.</li>
 *   <li><b>JDrive (Jump Drive)</b> — Used for faster-than-light travel between solar systems
 *       via hyperlanes. Determines inter-system travel capability.</li>
 * </ul>
 * <p>
 * Evasion represents the base percentage chance to dodge incoming attacks in combat.
 * </p>
 * <p>
 * Cubic meters determines the number of HullModules the ship contains.
 * Each HullModule is a 10×10×10 grid of M3space cells (1000 cells).
 * The number of modules is calculated as: {@code cubicMeters / 1000}, minimum 1.
 * Modules are arranged in a 3D grid based on the cubic meter dimensions.
 * </p>
 */
public enum ShipType {
    /**
     * Frigate — small, fast, cheap scout/light combat ship.
     * 50 hull points, 3 component slots, 2 turns to build, MDrive 10, JDrive 12, evasion 20%.
     * 1000 cubic meters → 1 hull module.
     */
    FRIGATE(50, 3, 2, 10, 12, 0.20, 1000),

    /**
     * Destroyer — medium escort ship with balanced capabilities.
     * 100 hull points, 5 component slots, 3 turns to build, MDrive 8, JDrive 10, evasion 15%.
     * 2000 cubic meters → 2 hull modules.
     */
    DESTROYER(100, 5, 3, 8, 10, 0.15, 2000),

    /**
     * Cruiser — heavy multi-role combat ship.
     * 200 hull points, 8 component slots, 5 turns to build, MDrive 6, JDrive 8, evasion 10%.
     * 8000 cubic meters → 8 hull modules.
     */
    CRUISER(200, 8, 5, 6, 8, 0.10, 8000),

    /**
     * Battleship — capital ship with maximum firepower.
     * 400 hull points, 12 component slots, 8 turns to build, MDrive 4, JDrive 5, evasion 5%.
     * 27000 cubic meters → 27 hull modules.
     */
    BATTLESHIP(400, 12, 8, 4, 5, 0.05, 27000),

    /**
     * Carrier — deploys fighter wings; fewer direct weapons.
     * 300 hull points, 6 component slots, 7 turns to build, MDrive 5, JDrive 6, evasion 8%.
     * 12000 cubic meters → 12 hull modules.
     */
    CARRIER(300, 6, 7, 5, 6, 0.08, 12000),

    /**
     * Mothership — ultimate superweapon; extremely slow to build.
     * 800 hull points, 20 component slots, 15 turns to build, MDrive 2, JDrive 3, evasion 2%.
     * 64000 cubic meters → 64 hull modules.
     */
    MOTHERSHIP(800, 20, 15, 2, 3, 0.02, 64000);

    private final int baseHullPoints;
    private final int componentSlots;
    private final int buildTimeTurns;
    private final int mDriveSpeed;
    private final int jDriveSpeed;
    private final double baseEvasion;
    private final int cubicMeters;

    ShipType(int baseHullPoints, int componentSlots, int buildTimeTurns, int mDriveSpeed, int jDriveSpeed, double baseEvasion, int cubicMeters) {
        this.baseHullPoints = baseHullPoints;
        this.componentSlots = componentSlots;
        this.buildTimeTurns = buildTimeTurns;
        this.mDriveSpeed = mDriveSpeed;
        this.jDriveSpeed = jDriveSpeed;
        this.baseEvasion = baseEvasion;
        this.cubicMeters = cubicMeters;
    }

    /**
     * Returns the base hull points for this ship type.
     *
     * @return the base hull points
     */
    public int getBaseHullPoints() {
        return baseHullPoints;
    }

    /**
     * Returns the number of component slots available on this ship type.
     *
     * @return the number of component slots
     */
    public int getComponentSlots() {
        return componentSlots;
    }

    /**
     * Returns the number of turns required to build this ship type.
     *
     * @return the build time in turns
     */
    public int getBuildTimeTurns() {
        return buildTimeTurns;
    }

    /**
     * Returns the MDrive (Maneuver Drive) speed for this ship type.
     * <p>
     * MDrive is used for movement within a solar system and during combat.
     * It determines tactical positioning speed and combat maneuverability.
     * </p>
     *
     * @return the MDrive speed value
     */
    public int getMDriveSpeed() {
        return mDriveSpeed;
    }

    /**
     * Returns the JDrive (Jump Drive) speed for this ship type.
     * <p>
     * JDrive is used for faster-than-light travel between solar systems
     * via hyperlanes. It determines inter-system travel capability.
     * </p>
     *
     * @return the JDrive speed value
     */
    public int getJDriveSpeed() {
        return jDriveSpeed;
    }

    /**
     * Returns the base speed for this ship type (alias for MDrive speed).
     * <p>
     * Deprecated: Use {@link #getMDriveSpeed()} for system movement
     * or {@link #getJDriveSpeed()} for inter-system travel.
     * </p>
     *
     * @return the MDrive speed value
     * @deprecated Use {@link #getMDriveSpeed()} instead
     */
    @Deprecated
    public int getBaseSpeed() {
        return mDriveSpeed;
    }

    /**
     * Returns the base evasion chance for this ship type.
     * <p>
     * Evasion represents the percentage chance to dodge incoming attacks.
     * Smaller ships have higher evasion, making them harder to hit.
     * </p>
     *
     * @return the base evasion chance (0.0 to 1.0)
     */
    public double getBaseEvasion() {
        return baseEvasion;
    }

    /**
     * Returns the cubic meters volume of this ship type.
     * <p>
     * This determines the number of HullModules the ship contains.
     * Each HullModule is a 10×10×10 grid of M3space cells (1000 cells).
     * </p>
     *
     * @return the cubic meters volume
     */
    public int getCubicMeters() {
        return cubicMeters;
    }

    /**
     * Returns the number of HullModules for this ship type.
     * <p>
     * Calculated as cubic meters divided by 1000 (cells per module), minimum 1.
     * </p>
     *
     * @return the number of hull modules
     */
    public int getHullModuleCount() {
        return Math.max(1, cubicMeters / 1000);
    }

    /**
     * Returns the 3D dimensions of the module grid for this ship type.
     * <p>
     * The module count is distributed across X, Y, Z axes to form a compact 3D arrangement.
     * For example, 8 modules → 2×2×2 grid, 27 modules → 3×3×3 grid.
     * Non-perfect cubes are distributed as evenly as possible.
     * </p>
     *
     * @return int array of {width, height, depth} for the module grid
     */
    public int[] getModuleGridDimensions() {
        int count = getHullModuleCount();
        if (count <= 1) {
            return new int[]{1, 1, 1};
        }
        // Calculate cube root for base dimension
        int base = (int) Math.round(Math.pow(count, 1.0 / 3.0));
        if (base < 1) base = 1;

        // Distribute modules as evenly as possible
        int x = base;
        int y = base;
        int z = base;

        // Adjust to match exact count
        int product = x * y * z;
        while (product > count) {
            // Reduce the largest dimension
            if (x >= y && x >= z) x--;
            else if (y >= x && y >= z) y--;
            else z--;
            product = x * y * z;
        }
        while (product < count) {
            // Increase the smallest dimension
            if (x <= y && x <= z) x++;
            else if (y <= x && y <= z) y++;
            else z++;
            product = x * y * z;
        }

        return new int[]{x, y, z};
    }
}
