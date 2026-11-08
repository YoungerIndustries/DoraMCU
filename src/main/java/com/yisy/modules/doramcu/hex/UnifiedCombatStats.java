package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.*;

/**
 * Unified combat statistics that merge DoraMCU's component-based combat with
 * Traveller's High Guard ship-to-ship combat system.
 * <p>
 * This class extends {@link ShipCombatStats} to add:
 * </p>
 * <ul>
 *   <li><b>Armor Protection</b> — High Guard armor reduces incoming damage</li>
 *   <li><b>Thrust</b> — maneuver drive rating for combat movement</li>
 *   <li><b>Agility</b> — derived stat for dogfighting and evasion</li>
 *   <li><b>Energy Points</b> — power plant output for systems and weapons</li>
 *   <li><b>Range Bands</b> — High Guard range-based combat (Adjacent to Distant)</li>
 *   <li><b>Nuclear Dampers</b> — defense against nuclear weapons</li>
 *   <li><b>Meson Screens</b> — defense against meson guns</li>
 * </ul>
 * <p>
 * Damage calculation combines both systems:
 * </p>
 * <ol>
 *   <li>Calculate base damage from DoraMCU components</li>
 *   <li>Add High Guard weapon damage from turret/bay mounts</li>
 *   <li>Apply armor protection reduction</li>
 *   <li>Apply shield absorption (shield-first model)</li>
 *   <li>Remaining damage reduces hull points</li>
 * </ol>
 */
public class UnifiedCombatStats extends ShipCombatStats {

    private final UnifiedShip unifiedShip;

    // ---- High Guard combat fields ----

    private int thrust;
    private int agility;
    private int energyPoints;
    private int nuclearDampers;
    private int mesonScreens;
    private int computerModel;

    // ---- High Guard weapon mounts ----

    private final List<HighGuardShipDesign.WeaponMount> turretWeapons;
    private final List<HighGuardShipDesign.WeaponMount> bayWeapons;

    /**
     * Creates unified combat stats for the given UnifiedShip.
     *
     * @param unifiedShip the unified ship
     */
    public UnifiedCombatStats(UnifiedShip unifiedShip) {
        super(unifiedShip);
        this.unifiedShip = unifiedShip;
        this.turretWeapons = new ArrayList<>();
        this.bayWeapons = new ArrayList<>();
        this.thrust = unifiedShip.getFusionDrive().getRating();
        this.agility = 0;
        this.energyPoints = 0;
        recalculateDerivedStats();
    }

    /**
     * Creates unified combat stats from a High Guard ship design.
     *
     * @param unifiedShip the unified ship
     * @param design the High Guard design to import stats from
     */
    public UnifiedCombatStats(UnifiedShip unifiedShip, HighGuardShipDesign design) {
        this(unifiedShip);
        this.thrust = design.getThrust();
        this.agility = design.getAgility();
        this.energyPoints = design.getEnergyPoints();
        this.nuclearDampers = design.getNuclearDampers();
        this.mesonScreens = design.getMesonScreens();
        this.computerModel = design.getComputerModel();
        this.turretWeapons.addAll(design.getTurretWeapons());
        this.bayWeapons.addAll(design.getBayWeapons());
    }

    // ---- Derived Stat Calculation ----

    /**
     * Recalculates all derived statistics from the ship's current state.
     */
    public void recalculateDerivedStats() {
        // Thrust from fusion drive
        this.thrust = unifiedShip.getFusionDrive().getRating();

        // Agility from fusion drive and tonnage
        int tonnage = unifiedShip.getTotalTonnage();
        if (tonnage > 0 && thrust > 0) {
            this.agility = (thrust * 10) / (tonnage / 100);
            if (this.agility < 1) {
                this.agility = 1;
            }
        } else {
            this.agility = 0;
        }

        // Energy points from fusion drive (power plant proxy)
        this.energyPoints = thrust * 10;
    }

    // ---- High Guard Accessors ----

    /**
     * Returns the effective thrust for combat maneuvering.
     *
     * @return the thrust value
     */
    public int getThrust() {
        return thrust;
    }

    /**
     * Returns the agility stat for dogfighting.
     *
     * @return the agility value
     */
    public int getAgility() {
        return agility;
    }

    /**
     * Returns the available energy points.
     *
     * @return the energy points
     */
    public int getEnergyPoints() {
        return energyPoints;
    }

    /**
     * Returns the number of nuclear dampers.
     *
     * @return nuclear dampers count
     */
    public int getNuclearDampers() {
        return nuclearDampers;
    }

    /**
     * Sets the number of nuclear dampers.
     *
     * @param dampers the number of dampers
     */
    public void setNuclearDampers(int dampers) {
        this.nuclearDampers = Math.max(0, dampers);
    }

    /**
     * Returns the number of meson screens.
     *
     * @return meson screens count
     */
    public int getMesonScreens() {
        return mesonScreens;
    }

    /**
     * Sets the number of meson screens.
     *
     * @param screens the number of screens
     */
    public void setMesonScreens(int screens) {
        this.mesonScreens = Math.max(0, screens);
    }

    /**
     * Returns the computer model number.
     *
     * @return the computer model
     */
    public int getComputerModel() {
        return computerModel;
    }

    /**
     * Sets the computer model.
     *
     * @param model the computer model (0-9)
     */
    public void setComputerModel(int model) {
        this.computerModel = Math.max(0, Math.min(9, model));
    }

    // ---- Weapon Mount Management ----

    /**
     * Returns the list of turret-mounted weapons.
     *
     * @return unmodifiable list of turret weapons
     */
    public List<HighGuardShipDesign.WeaponMount> getTurretWeapons() {
        return Collections.unmodifiableList(turretWeapons);
    }

    /**
     * Returns the list of bay-mounted weapons.
     *
     * @return unmodifiable list of bay weapons
     */
    public List<HighGuardShipDesign.WeaponMount> getBayWeapons() {
        return Collections.unmodifiableList(bayWeapons);
    }

    /**
     * Adds a turret-mounted weapon.
     *
     * @param weapon the High Guard weapon
     * @param size the turret size
     */
    public void addTurretWeapon(HighGuardWeapon weapon, HighGuardWeapon.TurretSize size) {
        turretWeapons.add(new HighGuardShipDesign.WeaponMount(
                HighGuardShipDesign.WeaponMount.MountType.TURRET, weapon, size, null));
    }

    /**
     * Adds a bay-mounted weapon.
     *
     * @param weapon the High Guard weapon
     * @param size the bay size
     */
    public void addBayWeapon(HighGuardWeapon weapon, HighGuardWeapon.BaySize size) {
        bayWeapons.add(new HighGuardShipDesign.WeaponMount(
                HighGuardShipDesign.WeaponMount.MountType.BAY, weapon, null, size));
    }

    /**
     * Returns the total number of weapon mounts.
     *
     * @return total weapon mounts
     */
    public int getTotalWeaponMounts() {
        return turretWeapons.size() + bayWeapons.size();
    }

    // ---- Unified Damage Calculations ----

    /**
     * Returns the total damage combining DoraMCU components and High Guard weapons.
     * <p>
     * DoraMCU components provide base damage. High Guard weapons add additional
     * damage dice based on their type and mount configuration.
     * </p>
     *
     * @return the total damage value
     */
    @Override
    public int getTotalDamage() {
        int doraDamage = super.getTotalDamage();
        int highGuardDamage = calculateHighGuardDamage();
        return doraDamage + highGuardDamage;
    }

    /**
     * Calculates the damage from High Guard weapon mounts.
     *
     * @return the High Guard weapon damage
     */
    private int calculateHighGuardDamage() {
        int damage = 0;
        for (HighGuardShipDesign.WeaponMount mount : turretWeapons) {
            if (mount.isOperational()) {
                damage += mount.getWeapon().getBaseDamageDice() * mount.getTurretSize().getMultiplier();
            }
        }
        for (HighGuardShipDesign.WeaponMount mount : bayWeapons) {
            if (mount.isOperational()) {
                damage += mount.getWeapon().getBaseDamageDice() * 2;
            }
        }
        return damage;
    }

    /**
     * Returns the effective damage with armor penetration considerations.
     * <p>
     * Meson guns and fusion guns can penetrate armor. Particle beams are
     * blocked by armor but effective at close range.
     * </p>
     *
     * @return the effective damage value
     */
    @Override
    public int getEffectiveDamage() {
        int base = getTotalDamage();

        // Computer bonus for targeting
        double bonus = computerModel * 0.05;

        // AI Core bonus from DoraMCU system
        AiCoreComponent aiCore = unifiedShip.getAiCore();
        if (aiCore != null) {
            bonus += aiCore.getDamageBonus();
        }

        return (int) Math.round(Math.max(0, base * (1.0 + bonus)));
    }

    /**
     * Returns the effective hull points including armor protection.
     * <p>
     * Armor adds effective hull points by absorbing damage before it reaches
     * the structural hull.
     * </p>
     *
     * @return the effective hull points
     */
    @Override
    public int getEffectiveHullPoints() {
        int base = super.getEffectiveHullPoints();
        int armorBonus = unifiedShip.getArmorProtection() * 2;
        return base + armorBonus;
    }

    /**
     * Returns the effective evasion including agility bonus.
     * <p>
     * Agility provides additional evasion in combat, especially at close range.
     * </p>
     *
     * @return the effective evasion chance (0.0 to 1.0)
     */
    @Override
    public double getEffectiveEvasion() {
        double base = super.getEffectiveEvasion();

        // Agility bonus — higher agility means better evasion
        double agilityBonus = agility * 0.01;
        double evasion = base + agilityBonus;

        return Math.max(0.0, Math.min(1.0, evasion));
    }

    // ---- Armor-Based Damage Absorption ----

    /**
     * Returns the armor protection value.
     *
     * @return the armor protection
     */
    public int getArmorProtection() {
        return unifiedShip.getArmorProtection();
    }

    /**
     * Calculates the damage absorbed by armor.
     * <p>
     * Armor absorbs damage before it reaches shields or hull.
     * Meson guns partially bypass armor.
     * </p>
     *
     * @param incomingDamage the raw incoming damage
     * @param weaponType the type of weapon attacking
     * @return the amount absorbed by armor
     */
    public int calculateArmorAbsorption(int incomingDamage, HighGuardWeapon weaponType) {
        int protection = getArmorProtection();
        if (protection <= 0) return 0;

        // Meson guns bypass 50% of armor
        double bypassFactor = 1.0;
        if (weaponType != null && weaponType.isMeson()) {
            bypassFactor = 0.5;
        }

        int effectiveProtection = (int) (protection * bypassFactor);
        return Math.min(incomingDamage, effectiveProtection);
    }

    /**
     * Applies damage with full armor and shield absorption.
     * <p>
     * Order of absorption:
     * </p>
     * <ol>
     *   <li>Armor absorbs first (reduced by meson bypass)</li>
     *   <li>Shields absorb remaining damage</li>
     *   <li>Hull takes remaining damage</li>
     * </ol>
     *
     * @param damage the incoming damage
     * @param weaponType the attacking weapon type (for armor bypass)
     * @return the actual damage dealt to hull
     */
    public int applyDamageWithArmor(int damage, HighGuardWeapon weaponType) {
        if (damage <= 0 || unifiedShip.isDestroyed()) {
            return 0;
        }

        int remaining = damage;

        // Armor absorption
        int armorAbsorbed = calculateArmorAbsorption(remaining, weaponType);
        remaining -= armorAbsorbed;

        // Shield absorption
        if (remaining > 0 && unifiedShip.getCurrentShieldPoints() > 0) {
            int shieldAbsorb = Math.min(unifiedShip.getCurrentShieldPoints(), remaining);
            unifiedShip.rechargeShields(-shieldAbsorb);
            remaining -= shieldAbsorb;
        }

        // Hull damage
        if (remaining > 0) {
            unifiedShip.repairHull(-remaining);
        }

        return damage - remaining;
    }

    /**
     * Applies damage using the default DoraMCU shield-first model.
     *
     * @param damage the incoming damage
     * @return the actual damage dealt
     */
    public int applyDamage(int damage) {
        return super.takeDamage(damage);
    }

    // ---- High Guard Combat Integration ----

    /**
     * Creates a High Guard combat instance between this ship and another.
     *
     * @param opponent the opposing ship's design
     * @param initialRange the starting range band
     * @return a new HighGuardShipCombat instance
     */
    public HighGuardShipCombat createCombat(HighGuardShipDesign opponent,
                                             HighGuardRangeBand initialRange) {
        // Create a temporary design from this ship for combat
        HighGuardShipDesign design = createDesignFromShip();
        return new HighGuardShipCombat(design, opponent, initialRange);
    }

    /**
     * Creates a High Guard ship design from this unified ship's current state.
     * <p>
     * This allows using the High Guard combat engine with a UnifiedShip.
     * </p>
     *
     * @return a new HighGuardShipDesign representing this ship
     */
    public HighGuardShipDesign createDesignFromShip() {
        HighGuardShipDesign design = new HighGuardShipDesign(
                unifiedShip.getId(),
                unifiedShip.getName(),
                unifiedShip.getTotalTonnage());

        design.setArmor(unifiedShip.getArmor());
        design.setManeuverDrive(unifiedShip.getFusionDrive().getRating());
        design.setJumpDrive(unifiedShip.getJumpDrive().getRating());
        design.setPowerPlantRating(thrust);
        design.setNuclearDampers(nuclearDampers);
        design.setMesonScreens(mesonScreens);
        design.setComputerModel(computerModel);

        // Copy weapon mounts
        for (HighGuardShipDesign.WeaponMount mount : turretWeapons) {
            if (mount.getMountType() == HighGuardShipDesign.WeaponMount.MountType.TURRET) {
                design.addTurretWeapon(mount.getWeapon(), mount.getTurretSize());
            }
        }
        for (HighGuardShipDesign.WeaponMount mount : bayWeapons) {
            if (mount.getMountType() == HighGuardShipDesign.WeaponMount.MountType.BAY) {
                design.addBayWeapon(mount.getWeapon(), mount.getBaySize());
            }
        }

        return design;
    }

    // ---- Status Checks ----

    /**
     * Returns true if the ship has active armor protection.
     *
     * @return true if armor protection > 0
     */
    public boolean hasArmor() {
        return getArmorProtection() > 0;
    }

    /**
     * Returns true if the ship has nuclear dampers.
     *
     * @return true if nuclear dampers > 0
     */
    public boolean hasNuclearDampers() {
        return nuclearDampers > 0;
    }

    /**
     * Returns true if the ship has meson screens.
     *
     * @return true if meson screens > 0
     */
    public boolean hasMesonScreens() {
        return mesonScreens > 0;
    }

    /**
     * Returns the defense rating against nuclear weapons.
     * <p>
     * Nuclear dampers provide 2D6 + dampers vs. nuclear attack.
     * </p>
     *
     * @return the nuclear defense rating
     */
    public int getNuclearDefenseRating() {
        return nuclearDampers;
    }

    /**
     * Returns the defense rating against meson guns.
     * <p>
     * Meson screens reduce meson gun effectiveness.
     * </p>
     *
     * @return the meson defense rating
     */
    public int getMesonDefenseRating() {
        return mesonScreens;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return String.format(
                "UnifiedCombatStats{damage=%d, effectiveDamage=%d, hull=%d/%d, " +
                        "shields=%d/%d, armor=%d, thrust=%d, agility=%d, energy=%d, " +
                        "nuclearDampers=%d, mesonScreens=%d, computer=%d, " +
                        "turrets=%d, bays=%d}",
                getTotalDamage(), getEffectiveDamage(),
                unifiedShip.getCurrentHullPoints(), unifiedShip.getMaxHullPoints(),
                unifiedShip.getCurrentShieldPoints(), unifiedShip.getMaxShieldPoints(),
                getArmorProtection(), thrust, agility, energyPoints,
                nuclearDampers, mesonScreens, computerModel,
                turretWeapons.size(), bayWeapons.size());
    }
}
