package com.yisy.modules.doramcu.traveller;

/**
 * Branch of service in Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * Mercenaries come from either the Army or the Marines.
 * The Army is divided into Infantry, Cavalry, Artillery, and Support.
 * The Marines consist of Marine Infantry and Support.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum BranchOfService {

    /**
     * Army — ground forces with Infantry, Cavalry, Artillery, and Support.
     * <p>
     * Enlisted men and NCOs receive +2 DM for reenlistment.
     * </p>
     */
    ARMY("Army", "Ground forces", 2),

    /**
     * Marines — shipboard and amphibious forces.
     * <p>
     * Marines cross-trained in artillery or cavalry receive +1 DM for reenlistment.
     * </p>
     */
    MARINES("Marines", "Shipboard and amphibious forces", 0);

    private final String name;
    private final String description;
    private final int reenlistmentDm;

    BranchOfService(String name, String description, int reenlistmentDm) {
        this.name = name;
        this.description = description;
        this.reenlistmentDm = reenlistmentDm;
    }

    /**
     * Returns the name of the branch.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the branch.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the reenlistment die modifier for this branch.
     *
     * @return the reenlistment DM
     */
    public int getReenlistmentDm() {
        return reenlistmentDm;
    }
}
