package com.yisy.modules.doramcu.traveller;

/**
 * A boarding shuttle used to transport marines to a target ship.
 * <p>
 * In Classic Traveller High Guard, boarding actions require shuttle craft
 * to transport marine units from the attacking ship to the target. The
 * shuttle must survive the approach, dock with the target, and deliver
 * the marines intact.
 * </p>
 * <p>
 * Boarding shuttles have:
 * </p>
 * <ul>
 *   <li>Capacity — number of marines that can be transported</li>
 *   <li>Speed — determines how quickly the shuttle can approach</li>
 *   <li>Armor — protects against point defense fire</li>
 *   <li>Stealth — reduces the chance of being detected and shot down</li>
 * </ul>
 * <p>
 * The shuttle must survive the approach phase, where the target ship's
 * point defenses attempt to shoot it down. If the shuttle survives,
 * it docks and the marines begin the boarding action.
 * </p>
 */
public class BoardingShuttle {

    /**
     * Type of boarding shuttle.
     */
    public enum ShuttleType {
        /**
         * Basic transport — minimal protection, low capacity.
         */
        BASIC("Basic Transport", 5, 1, 1, 0),

        /**
         * Standard boarding shuttle — balanced stats.
         */
        STANDARD("Standard Shuttle", 10, 2, 2, 1),

        /**
         * Armored assault shuttle — heavy armor, slower.
         */
        ARMORED("Armored Assault", 8, 1, 4, 0),

        /**
         * Stealth boarding craft — high stealth, low capacity.
         */
        STEALTH("Stealth Craft", 4, 3, 1, 3),

        /**
         * Heavy transport — maximum capacity, slow and vulnerable.
         */
        HEAVY("Heavy Transport", 20, 1, 2, 0);

        private final String name;
        private final int capacity;
        private final int speed;
        private final int armor;
        private final int stealth;

        ShuttleType(String name, int capacity, int speed, int armor, int stealth) {
            this.name = name;
            this.capacity = capacity;
            this.speed = speed;
            this.armor = armor;
            this.stealth = stealth;
        }

        /** @return the human-readable name */
        public String getName() { return name; }

        /** @return the marine capacity */
        public int getCapacity() { return capacity; }

        /** @return the speed rating */
        public int getSpeed() { return speed; }

        /** @return the armor rating */
        public int getArmor() { return armor; }

        /** @return the stealth rating */
        public int getStealth() { return stealth; }
    }

    private final String name;
    private final ShuttleType shuttleType;
    private final MarineUnit marineUnit;
    private final int hullPoints;
    private int currentHull;
    private boolean destroyed;
    private boolean docked;

    /**
     * Creates a new boarding shuttle.
     *
     * @param name the name of the shuttle
     * @param shuttleType the type of shuttle
     * @param marineUnit the marine unit being transported
     */
    public BoardingShuttle(String name, ShuttleType shuttleType, MarineUnit marineUnit) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Shuttle name must not be empty");
        }
        if (marineUnit == null) {
            throw new IllegalArgumentException("Marine unit must not be null");
        }
        if (marineUnit.getSize() > shuttleType.getCapacity()) {
            throw new IllegalArgumentException(
                    "Marine unit size (" + marineUnit.getSize() +
                    ") exceeds shuttle capacity (" + shuttleType.getCapacity() + ")");
        }

        this.name = name;
        this.shuttleType = shuttleType;
        this.marineUnit = marineUnit;
        this.hullPoints = shuttleType.getArmor() * 10;
        this.currentHull = hullPoints;
        this.destroyed = false;
        this.docked = false;
    }

    // ---- Accessors ----

    /** @return the name of the shuttle */
    public String getName() {
        return name;
    }

    /** @return the type of shuttle */
    public ShuttleType getShuttleType() {
        return shuttleType;
    }

    /** @return the marine unit being transported */
    public MarineUnit getMarineUnit() {
        return marineUnit;
    }

    /** @return the maximum hull points */
    public int getHullPoints() {
        return hullPoints;
    }

    /** @return the current hull points */
    public int getCurrentHull() {
        return currentHull;
    }

    /** @return true if the shuttle has been destroyed */
    public boolean isDestroyed() {
        return destroyed;
    }

    /** @return true if the shuttle has successfully docked */
    public boolean isDocked() {
        return docked;
    }

    /**
     * Returns the shuttle's speed rating.
     *
     * @return the speed
     */
    public int getSpeed() {
        return shuttleType.getSpeed();
    }

    /**
     * Returns the shuttle's armor rating.
     *
     * @return the armor
     */
    public int getArmor() {
        return shuttleType.getArmor();
    }

    /**
     * Returns the shuttle's stealth rating.
     *
     * @return the stealth
     */
    public int getStealth() {
        return shuttleType.getStealth();
    }

    // ---- Operations ----

    /**
     * Damages the shuttle from point defense fire.
     *
     * @param damage the amount of damage
     */
    public void takeDamage(int damage) {
        if (destroyed) return;
        currentHull -= damage;
        if (currentHull <= 0) {
            currentHull = 0;
            destroyed = true;
        }
    }

    /**
     * Attempts to dock with the target ship.
     * <p>
     * Docking success depends on the shuttle's speed and the target's
     * point defense capability. A successful docking delivers the marines.
     * </p>
     *
     * @param pointDefenseRating the target's point defense rating (1-12)
     * @param random the random number generator
     * @return true if docking was successful
     */
    public boolean attemptDocking(int pointDefenseRating, java.util.Random random) {
        if (destroyed) return false;

        // Docking roll: 2D6 + speed vs point defense
        int roll = random.nextInt(12) + 2; // 2D6
        int dockingBonus = shuttleType.getSpeed() + shuttleType.getStealth();
        int threshold = 12 + pointDefenseRating - dockingBonus;

        boolean success = roll >= threshold;
        if (success) {
            docked = true;
        }
        return success;
    }

    /**
     * Checks if the shuttle can still operate.
     *
     * @return true if the shuttle is not destroyed
     */
    public boolean isOperational() {
        return !destroyed && currentHull > 0;
    }

    /**
     * Returns the hull percentage remaining.
     *
     * @return the percentage of hull remaining (0-100)
     */
    public double getHullPercentage() {
        if (hullPoints <= 0) return 0.0;
        return (double) currentHull / hullPoints * 100.0;
    }

    @Override
    public String toString() {
        String status;
        if (destroyed) {
            status = "DESTROYED";
        } else if (docked) {
            status = "DOCKED";
        } else {
            status = String.format("%d/%d hull", currentHull, hullPoints);
        }
        return String.format("%s (%s, %d marines, %s)", name, shuttleType.getName(),
                marineUnit.getRemainingMarines(), status);
    }
}
