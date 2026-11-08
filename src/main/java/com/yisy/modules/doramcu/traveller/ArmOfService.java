package com.yisy.modules.doramcu.traveller;

import java.util.Arrays;
import java.util.List;

/**
 * Arms of service in Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * The Army consists of Infantry, Cavalry, Artillery, and Support.
 * The Marines consist of Marine Infantry and Support.
 * Commando is a special arm that cannot be entered directly;
 * characters must attend Commando School first.
 * </p>
 * <p>
 * Each arm has a Military Occupational Specialty (MOS) skill table
 * used during advanced training and skill acquisition.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum ArmOfService {

    /**
     * Infantry — the primary ground combat arm.
     */
    INFANTRY("Infantry", "Primary ground combat arm",
            Arrays.asList(
                    MercenarySkill.COMBAT_RIFLEMAN,
                    MercenarySkill.COMBAT_RIFLEMAN,
                    MercenarySkill.HEAVY_WEAPONS,
                    MercenarySkill.HEAVY_WEAPONS,
                    MercenarySkill.VEHICLE,
                    MercenarySkill.RECON,
                    MercenarySkill.VACC_SUIT,
                    MercenarySkill.BATTLE_DRESS)),

    /**
     * Cavalry — mechanized and armored forces.
     */
    CAVALRY("Cavalry", "Mechanized and armored forces",
            Arrays.asList(
                    MercenarySkill.VEHICLE,
                    MercenarySkill.VEHICLE,
                    MercenarySkill.VEHICLE,
                    MercenarySkill.HEAVY_WEAPONS,
                    MercenarySkill.HEAVY_WEAPONS,
                    MercenarySkill.RECON,
                    MercenarySkill.COMPUTER_MERC,
                    MercenarySkill.VACC_SUIT)),

    /**
     * Artillery — indirect fire support.
     */
    ARTILLERY("Artillery", "Indirect fire support",
            Arrays.asList(
                    MercenarySkill.FA_GUNNERY,
                    MercenarySkill.FA_GUNNERY,
                    MercenarySkill.VEHICLE,
                    MercenarySkill.VEHICLE,
                    MercenarySkill.FWD_OBSERVER,
                    MercenarySkill.MECHANICAL,
                    MercenarySkill.ELECTRONICS_MERC,
                    MercenarySkill.COMPUTER_MERC)),

    /**
     * Marines — shipboard and amphibious forces.
     */
    MARINE("Marine", "Shipboard and amphibious forces",
            Arrays.asList(
                    MercenarySkill.COMBAT_RIFLEMAN,
                    MercenarySkill.COMBAT_RIFLEMAN,
                    MercenarySkill.ZERO_G_COMBAT,
                    MercenarySkill.ZERO_G_COMBAT,
                    MercenarySkill.HEAVY_WEAPONS,
                    MercenarySkill.FWD_OBSERVER,
                    MercenarySkill.MEDIC,
                    MercenarySkill.BATTLE_DRESS)),

    /**
     * Support — logistics, engineering, and medical.
     */
    SUPPORT("Support", "Logistics, engineering, and medical",
            Arrays.asList(
                    MercenarySkill.VEHICLE,
                    MercenarySkill.COMBAT_ENGINEERING,
                    MercenarySkill.VEHICLE,
                    MercenarySkill.MECHANICAL,
                    MercenarySkill.ELECTRONICS_MERC,
                    MercenarySkill.MEDIC,
                    MercenarySkill.COMPUTER_MERC,
                    MercenarySkill.VACC_SUIT)),

    /**
     * Commando — elite special operations.
     * <p>
     * Cannot be entered directly; requires Commando School.
     * </p>
     */
    COMMANDO("Commando", "Elite special operations",
            Arrays.asList(
                    MercenarySkill.COMBAT_RIFLEMAN,
                    MercenarySkill.COMBAT_RIFLEMAN,
                    MercenarySkill.HEAVY_WEAPONS,
                    MercenarySkill.DEMOLITIONS,
                    MercenarySkill.WILDERNESS_SURVIVAL,
                    MercenarySkill.RECON,
                    MercenarySkill.BATTLE_DRESS,
                    MercenarySkill.COMBAT_RIFLEMAN));

    private final String name;
    private final String description;
    private final List<MercenarySkill> mosTable;

    ArmOfService(String name, String description, List<MercenarySkill> mosTable) {
        this.name = name;
        this.description = description;
        this.mosTable = mosTable;
    }

    /**
     * Returns the name of the arm.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the arm.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the MOS skill table for this arm.
     * <p>
     * When a character gains a MOS skill, one is randomly selected
     * from this table by rolling 1D6 (with +1 if world TL 12+).
     * </p>
     *
     * @return the MOS skill table
     */
    public List<MercenarySkill> getMosTable() {
        return mosTable;
    }

    /**
     * Returns true if this arm can be entered directly upon enlistment.
     * <p>
     * Commando cannot be entered directly; characters must attend
     * Commando School and then transfer.
     * </p>
     *
     * @return true if enterable directly
     */
    public boolean isEnterableDirectly() {
        return this != COMMANDO;
    }
}
