package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * High Guard starship design from Classic Traveller Book 5: High Guard.
 * <p>
 * Unlike Book 2's simple ship design, High Guard introduces:
 * </p>
 * <ul>
 *   <li><b>Armored Hulls</b> — Titanium Steel, Crystaliron, or Bonded Superdense armor</li>
 *   <li><b>Hull Points</b> — structural hit points that absorb damage</li>
 *   <li><b>Thrust</b> — maneuver drive rating that determines combat movement</li>
 *   <li><b>Agility</b> — derived stat for dogfighting and evasion</li>
 *   <li><b>Power Plant</b> — energy points that power systems and weapons</li>
 *   <li><b>Advanced Weapons</b> — particle beams, meson guns, fusion guns, sandcasters</li>
 *   <li><b>Missile Bays</b> — heavy torpedo launchers for capital ships</li>
 *   <li><b>Screens</b> — nuclear dampers and meson screens for defense</li>
 * </ul>
 * <p>
 * Hull points are calculated as: HullRate × (1 + ArmorFactor / 10)
 * where HullRate depends on ship size.
 * </p>
 * <p>
 * Agility is calculated as: (PowerPlantRating × 10) / (TotalTonnage / 100)
 * This represents the energy available for maneuver drives during combat.
 * </p>
 */
public class HighGuardShipDesign {

    private final int id;
    private String name;
    private final int totalTonnage;
    private int techLevel;

    // Hull & Armor
    private HighGuardArmor armor;
    private int hullPoints;
    private int armorProtection;

    // Drives
    private int maneuverDrive;  // Fusion drive rating (thrust)
    private int jumpDrive;       // Jump drive rating
    private int thrust;          // Effective thrust in combat

    // Power
    private int powerPlantRating;
    private int energyPoints;
    private int agility;

    // Computer & Sensors
    private int computerModel;
    private int computerRating;
    private int sensorRating;

    // Weapons
    private final List<WeaponMount> turretWeapons;
    private final List<WeaponMount> bayWeapons;

    // Defense
    private int nuclearDampers;
    private int mesonScreens;

    // Crew
    private int crew;
    private int marines;

    // Cost
    private long constructionCost;

    /**
     * Creates a new High Guard ship design.
     *
     * @param id the unique design identifier
     * @param name the ship's name
     * @param totalTonnage the total ship tonnage (100 to 1,000,000 tons)
     */
    public HighGuardShipDesign(int id, String name, int totalTonnage) {
        if (totalTonnage < 100) {
            throw new IllegalArgumentException("Minimum ship tonnage is 100 tons");
        }
        if (totalTonnage > 1000000) {
            throw new IllegalArgumentException("Maximum ship tonnage is 1,000,000 tons");
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.totalTonnage = totalTonnage;
        this.techLevel = 11;
        this.armor = HighGuardArmor.NONE;
        this.turretWeapons = new ArrayList<>();
        this.bayWeapons = new ArrayList<>();
    }

    // ---- Accessors & Mutators ----

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }
    public int getTotalTonnage() { return totalTonnage; }

    public int getTechLevel() { return techLevel; }
    public void setTechLevel(int techLevel) {
        this.techLevel = Math.max(6, Math.min(15, techLevel));
        recalculateDerivedStats();
    }

    public HighGuardArmor getArmor() { return armor; }
    public void setArmor(HighGuardArmor armor) {
        this.armor = Objects.requireNonNull(armor);
        this.armorProtection = armor.getProtection();
        recalculateDerivedStats();
    }

    public int getHullPoints() { return hullPoints; }
    public int getArmorProtection() { return armorProtection; }

    public int getManeuverDrive() { return maneuverDrive; }
    public void setManeuverDrive(int maneuverDrive) {
        this.maneuverDrive = Math.max(0, Math.min(6, maneuverDrive));
        recalculateDerivedStats();
    }

    public int getJumpDrive() { return jumpDrive; }
    public void setJumpDrive(int jumpDrive) {
        this.jumpDrive = Math.max(0, Math.min(6, jumpDrive));
    }

    public int getThrust() { return thrust; }

    public int getPowerPlantRating() { return powerPlantRating; }
    public void setPowerPlantRating(int powerPlantRating) {
        this.powerPlantRating = Math.max(0, powerPlantRating);
        recalculateDerivedStats();
    }

    public int getEnergyPoints() { return energyPoints; }
    public void setEnergyPoints(int energyPoints) {
        this.energyPoints = energyPoints;
    }
    public int getAgility() { return agility; }

    public int getComputerModel() { return computerModel; }
    public void setComputerModel(int computerModel) {
        this.computerModel = Math.max(0, Math.min(9, computerModel));
        this.computerRating = computerModel * 10;
    }

    public int getComputerRating() { return computerRating; }
    public int getSensorRating() { return sensorRating; }
    public void setSensorRating(int sensorRating) { this.sensorRating = sensorRating; }

    public List<WeaponMount> getTurretWeapons() { return Collections.unmodifiableList(turretWeapons); }
    public List<WeaponMount> getBayWeapons() { return Collections.unmodifiableList(bayWeapons); }

    public int getNuclearDampers() { return nuclearDampers; }
    public void setNuclearDampers(int nuclearDampers) { this.nuclearDampers = nuclearDampers; }

    public int getMesonScreens() { return mesonScreens; }
    public void setMesonScreens(int mesonScreens) { this.mesonScreens = mesonScreens; }

    public int getCrew() { return crew; }
    public void setCrew(int crew) { this.crew = crew; }

    public int getMarines() { return marines; }
    public void setMarines(int marines) { this.marines = marines; }

    public long getConstructionCost() { return constructionCost; }
    public void setConstructionCost(long constructionCost) { this.constructionCost = constructionCost; }

    /**
     * Adds a turret-mounted weapon.
     */
    public void addTurretWeapon(HighGuardWeapon weapon, HighGuardWeapon.TurretSize size) {
        turretWeapons.add(new WeaponMount(WeaponMount.MountType.TURRET, weapon, size, null));
    }

    /**
     * Adds a missile bay weapon.
     */
    public void addBayWeapon(HighGuardWeapon weapon, HighGuardWeapon.BaySize size) {
        bayWeapons.add(new WeaponMount(WeaponMount.MountType.BAY, weapon, null, size));
    }

    /**
     * Removes the last added turret weapon.
     */
    public void removeLastTurret() {
        if (!turretWeapons.isEmpty()) {
            turretWeapons.remove(turretWeapons.size() - 1);
        }
    }

    /**
     * Removes the last added bay weapon.
     */
    public void removeLastBay() {
        if (!bayWeapons.isEmpty()) {
            bayWeapons.remove(bayWeapons.size() - 1);
        }
    }

    // ---- Derived Stat Calculation ----

    /**
     * Recalculates all derived statistics.
     * <p>
     * Hull Points = HullRate × (1 + ArmorFactor / 10)
     * Agility = (PowerPlant × 10) / (Tonnage / 100)
     * Energy Points = PowerPlant × 10
     * Thrust = ManeuverDrive (reduced if power plant damaged)
     * </p>
     */
    private void recalculateDerivedStats() {
        // Hull points based on ship size
        this.hullPoints = calculateHullPoints();

        // Energy points
        this.energyPoints = powerPlantRating * 10;

        // Thrust = maneuver drive rating
        this.thrust = maneuverDrive;

        // Agility = (PowerPlant × 10) / (Tonnage / 100)
        if (totalTonnage > 0) {
            this.agility = (powerPlantRating * 10) / (totalTonnage / 100);
            if (this.agility < 1 && powerPlantRating > 0) {
                this.agility = 1; // Minimum agility of 1 if powered
            }
        } else {
            this.agility = 0;
        }

        // Construction cost
        this.constructionCost = calculateConstructionCost();
    }

    /**
     * Calculates hull points based on ship size and armor.
     * <p>
     * Hull points scale with ship tonnage:
     * </p>
     * <ul>
     *   <li>100-999 tons: 10 HP per 100 tons</li>
     *   <li>1,000-9,999 tons: 20 HP per 1,000 tons</li>
     *   <li>10,000-99,999 tons: 40 HP per 10,000 tons</li>
     *   <li>100,000+ tons: 80 HP per 100,000 tons</li>
     * </ul>
     * <p>
     * Armor adds bonus hull points: ArmorProtection × 2
     * </p>
     */
    private int calculateHullPoints() {
        int baseHull;
        if (totalTonnage < 1000) {
            baseHull = (totalTonnage / 100) * 10;
        } else if (totalTonnage < 10000) {
            baseHull = (totalTonnage / 1000) * 20;
        } else if (totalTonnage < 100000) {
            baseHull = (totalTonnage / 10000) * 40;
        } else {
            baseHull = (totalTonnage / 100000) * 80;
        }

        // Armor bonus
        int armorBonus = armorProtection * 2;

        return Math.max(10, baseHull + armorBonus);
    }

    /**
     * Calculates the construction cost in credits.
     * <p>
     * Cost = HullCost + DriveCost + ArmorCost + WeaponCost
     * </p>
     */
    private long calculateConstructionCost() {
        long cost = 0;

        // Hull cost: MCr 0.8 per ton
        cost += (long) (totalTonnage * 0.8);

        // Drive cost
        cost += maneuverDrive * totalTonnage / 10;
        cost += jumpDrive * totalTonnage / 5;

        // Power plant cost
        cost += powerPlantRating * totalTonnage / 20;

        // Armor cost
        if (armor != HighGuardArmor.NONE) {
            cost += (long) (totalTonnage * HighGuardArmor.calculateArmorCostPerTon(armorProtection));
        }

        // Weapon cost (simplified)
        for (WeaponMount mount : turretWeapons) {
            cost += mount.getWeapon().getTurretTonnage(HighGuardWeapon.TurretSize.SINGLE) * 10;
        }
        for (WeaponMount mount : bayWeapons) {
            cost += mount.getBaySize().getTons() * 100;
        }

        return cost;
    }

    // ---- Utility Methods ----

    /**
     * Returns the total number of weapon mounts.
     */
    public int getTotalWeaponMounts() {
        return turretWeapons.size() + bayWeapons.size();
    }

    /**
     * Returns the total damage dice from all weapons.
     */
    public int getTotalDamageDice() {
        int total = 0;
        for (WeaponMount mount : turretWeapons) {
            total += mount.getWeapon().getBaseDamageDice() * mount.getTurretSize().getMultiplier();
        }
        for (WeaponMount mount : bayWeapons) {
            total += mount.getWeapon().getBaseDamageDice() * 2; // Bays do double
        }
        return total;
    }

    /**
     * Returns the tons consumed by all systems.
     */
    public int getSystemTons() {
        int tons = 0;

        // Drives
        if (maneuverDrive > 0) {
            tons += totalTonnage / (10 + maneuverDrive * 2);
        }
        if (jumpDrive > 0) {
            tons += totalTonnage / (5 + jumpDrive);
        }

        // Power plant
        tons += powerPlantRating * totalTonnage / 100;

        // Armor
        if (armor != HighGuardArmor.NONE) {
            tons += armor.getTonsRequired(totalTonnage);
        }

        // Weapons
        for (WeaponMount mount : turretWeapons) {
            tons += mount.getWeapon().getTurretTonnage(mount.getTurretSize());
        }
        for (WeaponMount mount : bayWeapons) {
            tons += mount.getBaySize().getTons();
        }

        // Computer
        tons += computerModel * 10;

        return tons;
    }

    /**
     * Returns the remaining tons available for cargo, fuel, etc.
     */
    public int getRemainingTons() {
        return Math.max(0, totalTonnage - getSystemTons());
    }

    /**
     * Returns whether the design is valid (all systems fit).
     */
    public boolean isValid() {
        return getSystemTons() <= totalTonnage;
    }

    /**
     * Returns a summary string of the ship design.
     */
    public String getSummary() {
        return String.format("%s (%d tons) — M%d J%d PP%d A%d HP%d Ag%d",
                name, totalTonnage, maneuverDrive, jumpDrive,
                powerPlantRating, armorProtection, hullPoints, agility);
    }

    // ---- Hull Module Integration ----

    /**
     * Returns the number of HullModules for this ship design.
     * <p>
     * Calculated as total tonnage divided by 1000, minimum 1.
     * </p>
     *
     * @return the number of hull modules
     */
    public int getHullModuleCount() {
        return Math.max(1, totalTonnage / 1000);
    }

    /**
     * Returns the 3D dimensions of the module grid for this ship design.
     * <p>
     * The module count is distributed across X, Y, Z axes to form a compact 3D arrangement.
     * </p>
     *
     * @return int array of {width, height, depth} for the module grid
     */
    public int[] getModuleGridDimensions() {
        int count = getHullModuleCount();
        if (count <= 1) {
            return new int[]{1, 1, 1};
        }
        int base = (int) Math.round(Math.pow(count, 1.0 / 3.0));
        if (base < 1) base = 1;

        int x = base;
        int y = base;
        int z = base;

        int product = x * y * z;
        while (product > count) {
            if (x >= y && x >= z) x--;
            else if (y >= x && y >= z) y--;
            else z--;
            product = x * y * z;
        }
        while (product < count) {
            if (x <= y && x <= z) x++;
            else if (y <= x && y <= z) y++;
            else z++;
            product = x * y * z;
        }

        return new int[]{x, y, z};
    }

    /**
     * Returns the HullShape corresponding to this design's armor type.
     * <p>
     * Mapping:
     * </p>
     * <ul>
     *   <li>NONE / Titanium Steel → CUBE</li>
     *   <li>Crystaliron / Bonded Superdense → DOME</li>
     * </ul>
     *
     * @return the corresponding HullShape
     */
    public com.yisy.modules.doramcu.hex.HullShape getHullShape() {
        if (armor == null || armor == HighGuardArmor.NONE) {
            return com.yisy.modules.doramcu.hex.HullShape.CUBE;
        }
        switch (armor.getMaterial()) {
            case TITANIUM_STEEL:
                return com.yisy.modules.doramcu.hex.HullShape.CUBE;
            case CRYSTALIRON:
            case BONDED_SUPERDENSE:
                return com.yisy.modules.doramcu.hex.HullShape.DOME;
            default:
                return com.yisy.modules.doramcu.hex.HullShape.CUBE;
        }
    }

    /**
     * Returns the Material corresponding to this design's armor type.
     * <p>
     * Mapping:
     * </p>
     * <ul>
     *   <li>NONE → Aluminum</li>
     *   <li>Titanium Steel → Titanium</li>
     *   <li>Crystaliron → Crystaliron</li>
     *   <li>Bonded Superdense → Superdense</li>
     * </ul>
     *
     * @return the corresponding Material
     */
    public com.yisy.modules.doramcu.hex.Material getExteriorMaterial() {
        if (armor == null || armor == HighGuardArmor.NONE) {
            return com.yisy.modules.doramcu.hex.Material.Aluminum;
        }
        switch (armor.getMaterial()) {
            case TITANIUM_STEEL:
                return com.yisy.modules.doramcu.hex.Material.Titanium;
            case CRYSTALIRON:
                return com.yisy.modules.doramcu.hex.Material.Crystaliron;
            case BONDED_SUPERDENSE:
                return com.yisy.modules.doramcu.hex.Material.Superdense;
            default:
                return com.yisy.modules.doramcu.hex.Material.Aluminum;
        }
    }

    /**
     * A weapon mount on a High Guard ship.
     */
    public static class WeaponMount {
        private final MountType mountType;
        private final HighGuardWeapon weapon;
        private final HighGuardWeapon.TurretSize turretSize;
        private final HighGuardWeapon.BaySize baySize;
        private boolean isOperational;

        public enum MountType {
            TURRET("Turret"),
            BAY("Missile Bay");

            private final String name;
            MountType(String name) { this.name = name; }
            public String getName() { return name; }
        }

        public WeaponMount(MountType mountType, HighGuardWeapon weapon,
                    HighGuardWeapon.TurretSize turretSize, HighGuardWeapon.BaySize baySize) {
            this.mountType = mountType;
            this.weapon = weapon;
            this.turretSize = turretSize;
            this.baySize = baySize;
            this.isOperational = true;
        }

        public MountType getMountType() { return mountType; }
        public HighGuardWeapon getWeapon() { return weapon; }
        public HighGuardWeapon.TurretSize getTurretSize() { return turretSize; }
        public HighGuardWeapon.BaySize getBaySize() { return baySize; }
        public boolean isOperational() { return isOperational; }
        public void setOperational(boolean operational) { this.isOperational = operational; }

        /**
         * Returns the number of weapons in this mount.
         */
        public int getWeaponCount() {
            if (mountType == MountType.TURRET && turretSize != null) {
                return turretSize.getMultiplier();
            } else if (mountType == MountType.BAY) {
                return 2; // Bays fire 2 missiles per turn
            }
            return 1;
        }

        @Override
        public String toString() {
            if (mountType == MountType.TURRET) {
                return String.format("%s %s", turretSize.getName(), weapon.getName());
            } else {
                return String.format("%s %s", baySize.getName(), weapon.getName());
            }
        }
    }
}
