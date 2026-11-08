package com.yisy.modules.doramcu.traveller;

/**
 * Life support types from Classic Traveller Book 2.
 * <p>
 * Life support determines crew survivability when the ship is damaged.
 * Better life support allows the ship to operate in vacuum and survive
 * hull breaches with minimal crew loss.
 * </p>
 * <p>
 * Tonnage cost: Life support type × 1% of total tonnage (minimum 1000 tons).
 * </p>
 */
public enum LifeSupport {

    /**
     * No life support — ship cannot support crew in space.
     * Crew dies immediately if hull is breached.
     */
    NONE(0, 0),

    /**
     * Standard life support — basic atmosphere and temperature control.
     * Crew survives normal operations; 2D6 crew die per hull hit.
     * Tonnage: 1% of ship tonnage.
     */
    STANDARD(1, 2),

    /**
     * Redundant life support — duplicate systems for critical areas.
     * 1D6 crew die per hull hit.
     * Tonnage: 2% of ship tonnage.
     */
    REDUNDANT(2, 1),

    /**
     * Recyclers — advanced air/water recycling systems.
     * 1D6÷2 crew die per hull hit.
     * Tonnage: 3% of ship tonnage.
     */
    RECYCLERS(3, 1),

    /**
     * Closed loop — fully sealed environment.
     * No crew deaths from hull breaches (unless direct hit on crew quarters).
     * Tonnage: 4% of ship tonnage.
     */
    CLOSED_LOOP(4, 0),

    /**
     * Vacuum — ship can operate in vacuum; crew can EVA safely.
     * No crew deaths from hull breaches; allows boarding actions.
     * Tonnage: 5% of ship tonnage.
     */
    VACUUM(5, 0);

    private final int tonsPercent;
    private final int crewDeathDicePerHit;

    LifeSupport(int tonsPercent, int crewDeathDicePerHit) {
        this.tonsPercent = tonsPercent;
        this.crewDeathDicePerHit = crewDeathDicePerHit;
    }

    /**
     * Returns the tonnage percentage for this life support type.
     *
     * @return the percentage of ship tonnage
     */
    public int getTonsPercent() {
        return tonsPercent;
    }

    /**
     * Returns the number of D6 dice rolled for crew deaths per hull hit.
     * 0 means no crew deaths from hull breaches.
     *
     * @return crew death dice per hull hit
     */
    public int getCrewDeathDicePerHit() {
        return crewDeathDicePerHit;
    }

    /**
     * Calculates the tons required for this life support on a ship of the given tonnage.
     *
     * @param totalTonnage the total ship tonnage in tons
     * @return tons consumed by life support
     */
    public int getTonsRequired(int totalTonnage) {
        if (tonsPercent == 0) return 0;
        return Math.max(1000, (totalTonnage * tonsPercent) / 100);
    }

    /**
     * Returns whether this life support allows vacuum operations.
     *
     * @return true if vacuum operations are possible
     */
    public boolean allowsVacuumOperations() {
        return this == VACUUM;
    }

    /**
     * Returns whether this life support prevents crew deaths from hull breaches.
     *
     * @return true if crew is protected from hull breach deaths
     */
    public boolean preventsCrewDeaths() {
        return crewDeathDicePerHit == 0 && this != NONE;
    }
}
