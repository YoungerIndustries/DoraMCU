package com.yisy.modules.doramcu.traveller;

/**
 * Hit locations on a Traveller starship hex map from Classic Traveller Book 2.
 * <p>
 * Each ship is represented as a hex grid with labeled positions.
 * When a weapon hits a ship, the hit location determines which system is damaged.
 * </p>
 * <p>
 * The standard Traveller ship hex map uses a diamond-shaped layout:
 * </p>
 * <pre>
 *         A1
 *       A2  B2
 *     A3  B3  C3
 *   A4  B4  C4  D4
 *     A5  B5  C5
 *       A6  B6
 *         A7
 * </pre>
 * <p>
 * Each position can contain a specific system (jump drive, fusion drive, power plant,
 * life support, computer, cargo, passengers, or weapons) or be empty space.
 * </p>
 * <p>
 * A hit on a system causes it to go offline. Critical systems (jump drive, fusion drive,
 * power plant, life support, computer) have special effects when damaged.
 * </p>
 */
public enum HitLocation {

    /**
     * Top hex — typically unused or small laser placement.
     */
    A1("A1", SystemType.EMPTY),

    /**
     * Upper-left hex — often used for jump drive or cargo.
     */
    A2("A2", SystemType.EMPTY),

    /**
     * Upper-right hex — often used for jump drive or cargo.
     */
    B2("B2", SystemType.EMPTY),

    /**
     * Mid-left hex — often used for fusion drive or cargo.
     */
    A3("A3", SystemType.EMPTY),

    /**
     * Center-left hex — often used for power plant or life support.
     */
    B3("B3", SystemType.EMPTY),

    /**
     * Center-right hex — often used for computer or cargo.
     */
    C3("C3", SystemType.EMPTY),

    /**
     * Bottom-left hex — often used for cargo or passengers.
     */
    A4("A4", SystemType.EMPTY),

    /**
     * Bottom-center-left hex — often used for cargo or passengers.
     */
    B4("B4", SystemType.EMPTY),

    /**
     * Bottom-center-right hex — often used for cargo or passengers.
     */
    C4("C4", SystemType.EMPTY),

    /**
     * Bottom-right hex — often used for cargo or passengers.
     */
    D4("D4", SystemType.EMPTY),

    /**
     * Lower-left hex — often used for cargo or passengers.
     */
    A5("A5", SystemType.EMPTY),

    /**
     * Lower-center hex — often used for cargo or passengers.
     */
    B5("B5", SystemType.EMPTY),

    /**
     * Lower-right hex — often used for cargo or passengers.
     */
    C5("C5", SystemType.EMPTY),

    /**
     * Bottom-left hex — often used for cargo or small lasers.
     */
    A6("A6", SystemType.EMPTY),

    /**
     * Bottom-right hex — often used for cargo or small lasers.
     */
    B6("B6", SystemType.EMPTY),

    /**
     * Bottommost hex — typically unused or small laser placement.
     */
    A7("A7", SystemType.EMPTY);

    private final String label;
    private final SystemType defaultSystem;

    HitLocation(String label, SystemType defaultSystem) {
        this.label = label;
        this.defaultSystem = defaultSystem;
    }

    /**
     * Returns the hex label (e.g., "A1", "B3").
     *
     * @return the hex label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the default system type for this location.
     *
     * @return the default system type
     */
    public SystemType getDefaultSystem() {
        return defaultSystem;
    }

    /**
     * Finds the HitLocation by its hex label.
     *
     * @param label the hex label (e.g., "A1", "B3")
     * @return the matching HitLocation, or null if not found
     */
    public static HitLocation fromLabel(String label) {
        for (HitLocation loc : values()) {
            if (loc.label.equalsIgnoreCase(label)) {
                return loc;
            }
        }
        return null;
    }

    /**
     * Types of systems that can occupy a hit location.
     */
    public enum SystemType {
        /** Empty space — no critical system here. */
        EMPTY,

        /** Jump drive — FTL travel. If damaged, ship cannot jump. */
        JUMP_DRIVE,

        /** Fusion drive — within-system maneuvering. If damaged, ship's maneuver is reduced. */
        FUSION_DRIVE,

        /** Power plant — energy for all systems. If damaged, weapons may go offline. */
        POWER_PLANT,

        /** Life support — crew survival. If damaged, crew deaths occur. */
        LIFE_SUPPORT,

        /** Shipboard computer — navigation and gunnery. If damaged, targeting accuracy drops. */
        COMPUTER,

        /** Cargo hold — freight storage. If damaged, cargo is lost. */
        CARGO,

        /** Passenger quarters — crew and passengers. If damaged, casualties occur. */
        PASSENGERS,

        /** Laser battery — weapon system. If damaged, laser goes offline. */
        LASER,

        /** Missile launcher — weapon system. If damaged, missile launcher goes offline. */
        MISSILE,

        /** Hull — structural integrity. If damaged, ship takes hull points loss. */
        HULL,

        /** Crew quarters — crew living areas. If damaged, crew casualties occur. */
        CREW
    }
}
