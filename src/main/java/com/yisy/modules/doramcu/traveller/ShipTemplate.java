package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pre-designed naval ship templates from S09 Fighting Ships.
 * <p>
 * Provides ready-to-use ship designs based on the S09 naval ship classes:
 * Scout/Courier, Destroyer, Light Cruiser, Heavy Cruiser, Battlecruiser,
 * Battleship, Carrier, and Dreadnought. Each template defines the standard
 * configuration for its class, including tonnage, drives, weapons, armor,
 * and crew complement.
 * </p>
 * <p>
 * Templates are immutable and can be used to create new ship instances
 * via {@link HighGuardShipFactory}.
 * </p>
 */
public class ShipTemplate {

    /**
     * Ship class category from S09.
     */
    public enum ShipClass {
        /** Scout/Courier — small fast ships for reconnaissance and courier duty. */
        SCOUT("Scout/Courier", 1000, 2000),

        /** Destroyer — light combat ship for screening and patrol. */
        DESTROYER("Destroyer", 4000, 8000),

        /** Light Cruiser — medium combat ship for independent operations. */
        LIGHT_CRUISER("Light Cruiser", 10000, 16000),

        /** Heavy Cruiser — heavy combat ship for fleet actions. */
        HEAVY_CRUISER("Heavy Cruiser", 20000, 32000),

        /** Battlecruiser — fast capital ship with heavy armament. */
        BATTLECRUISER("Battlecruiser", 40000, 60000),

        /** Battleship — line-of-battle capital ship. */
        BATTLESHIP("Battleship", 60000, 100000),

        /** Carrier — ship designed to project fighter strength. */
        CARRIER("Carrier", 40000, 80000),

        /** Dreadnought — largest and most powerful warship. */
        DREADNOUGHT("Dreadnought", 100000, 1000000);

        private final String displayName;
        private final int minTonnage;
        private final int maxTonnage;

        ShipClass(String displayName, int minTonnage, int maxTonnage) {
            this.displayName = displayName;
            this.minTonnage = minTonnage;
            this.maxTonnage = maxTonnage;
        }

        public String getDisplayName() { return displayName; }
        public int getMinTonnage() { return minTonnage; }
        public int getMaxTonnage() { return maxTonnage; }
    }

    /**
     * A turret weapon entry stored in a template.
     */
    public static class TurretEntry {
        private final HighGuardWeapon weapon;
        private final HighGuardWeapon.TurretSize turretSize;

        public TurretEntry(HighGuardWeapon weapon, HighGuardWeapon.TurretSize turretSize) {
            this.weapon = Objects.requireNonNull(weapon);
            this.turretSize = Objects.requireNonNull(turretSize);
        }

        public HighGuardWeapon getWeapon() { return weapon; }
        public HighGuardWeapon.TurretSize getTurretSize() { return turretSize; }
    }

    /**
     * A bay weapon entry stored in a template.
     */
    public static class BayEntry {
        private final HighGuardWeapon weapon;
        private final HighGuardWeapon.BaySize baySize;

        public BayEntry(HighGuardWeapon weapon, HighGuardWeapon.BaySize baySize) {
            this.weapon = Objects.requireNonNull(weapon);
            this.baySize = Objects.requireNonNull(baySize);
        }

        public HighGuardWeapon getWeapon() { return weapon; }
        public HighGuardWeapon.BaySize getBaySize() { return baySize; }
    }

    private final int id;
    private final String name;
    private final ShipClass shipClass;
    private final int totalTonnage;
    private final int maneuverDrive;
    private final int jumpDrive;
    private final int energyPoints;
    private final HighGuardArmor armor;
    private final int nuclearDampers;
    private final int mesonScreens;
    private final int crew;
    private final int marines;
    private final List<TurretEntry> turretWeapons;
    private final List<BayEntry> bayWeapons;
    private final long constructionCost;
    private final String description;

    private ShipTemplate(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "Name must not be null");
        this.shipClass = Objects.requireNonNull(builder.shipClass, "Ship class must not be null");
        this.totalTonnage = builder.totalTonnage;
        this.maneuverDrive = builder.maneuverDrive;
        this.jumpDrive = builder.jumpDrive;
        this.energyPoints = builder.energyPoints;
        this.armor = builder.armor != null ? builder.armor : HighGuardArmor.NONE;
        this.nuclearDampers = builder.nuclearDampers;
        this.mesonScreens = builder.mesonScreens;
        this.crew = builder.crew;
        this.marines = builder.marines;
        this.turretWeapons = Collections.unmodifiableList(new ArrayList<>(builder.turretWeapons));
        this.bayWeapons = Collections.unmodifiableList(new ArrayList<>(builder.bayWeapons));
        this.constructionCost = builder.constructionCost;
        this.description = builder.description != null ? builder.description : "";
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public ShipClass getShipClass() { return shipClass; }
    public int getTotalTonnage() { return totalTonnage; }
    public int getManeuverDrive() { return maneuverDrive; }
    public int getJumpDrive() { return jumpDrive; }
    public int getEnergyPoints() { return energyPoints; }
    public HighGuardArmor getArmor() { return armor; }
    public int getNuclearDampers() { return nuclearDampers; }
    public int getMesonScreens() { return mesonScreens; }
    public int getCrew() { return crew; }
    public int getMarines() { return marines; }
    public List<TurretEntry> getTurretWeapons() { return turretWeapons; }
    public List<BayEntry> getBayWeapons() { return bayWeapons; }
    public long getConstructionCost() { return constructionCost; }
    public String getDescription() { return description; }

    /**
     * Converts this template into a {@link HighGuardShipDesign} instance
     * using the template's own ID.
     *
     * @return a new HighGuardShipDesign based on this template
     */
    public HighGuardShipDesign toDesign() {
        return toDesign(id);
    }

    /**
     * Converts this template into a {@link HighGuardShipDesign} instance
     * with the specified ID.
     *
     * @param designId the ID to assign to the new design
     * @return a new HighGuardShipDesign based on this template
     */
    public HighGuardShipDesign toDesign(int designId) {
        HighGuardShipDesign design = new HighGuardShipDesign(designId, name, totalTonnage);
        design.setManeuverDrive(maneuverDrive);
        design.setJumpDrive(jumpDrive);
        design.setEnergyPoints(energyPoints);
        design.setArmor(armor);
        design.setNuclearDampers(nuclearDampers);
        design.setMesonScreens(mesonScreens);
        design.setCrew(crew);
        design.setMarines(marines);

        for (TurretEntry entry : turretWeapons) {
            design.addTurretWeapon(entry.getWeapon(), entry.getTurretSize());
        }
        for (BayEntry entry : bayWeapons) {
            design.addBayWeapon(entry.getWeapon(), entry.getBaySize());
        }

        design.setConstructionCost(constructionCost);
        return design;
    }

    /**
     * Builder for creating ShipTemplate instances.
     */
    public static class Builder {
        private int id;
        private String name;
        private ShipClass shipClass;
        private int totalTonnage;
        private int maneuverDrive;
        private int jumpDrive;
        private int energyPoints;
        private HighGuardArmor armor;
        private int nuclearDampers;
        private int mesonScreens;
        private int crew;
        private int marines;
        private final List<TurretEntry> turretWeapons = new ArrayList<>();
        private final List<BayEntry> bayWeapons = new ArrayList<>();
        private long constructionCost;
        private String description;

        public Builder id(int id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder shipClass(ShipClass shipClass) { this.shipClass = shipClass; return this; }
        public Builder totalTonnage(int totalTonnage) { this.totalTonnage = totalTonnage; return this; }
        public Builder maneuverDrive(int maneuverDrive) { this.maneuverDrive = maneuverDrive; return this; }
        public Builder jumpDrive(int jumpDrive) { this.jumpDrive = jumpDrive; return this; }
        public Builder energyPoints(int energyPoints) { this.energyPoints = energyPoints; return this; }
        public Builder armor(HighGuardArmor armor) { this.armor = armor; return this; }
        public Builder nuclearDampers(int nuclearDampers) { this.nuclearDampers = nuclearDampers; return this; }
        public Builder mesonScreens(int mesonScreens) { this.mesonScreens = mesonScreens; return this; }
        public Builder crew(int crew) { this.crew = crew; return this; }
        public Builder marines(int marines) { this.marines = marines; return this; }
        public Builder turretWeapon(HighGuardWeapon weapon, HighGuardWeapon.TurretSize size) {
            this.turretWeapons.add(new TurretEntry(weapon, size));
            return this;
        }
        public Builder bayWeapon(HighGuardWeapon weapon, HighGuardWeapon.BaySize size) {
            this.bayWeapons.add(new BayEntry(weapon, size));
            return this;
        }
        public Builder constructionCost(long cost) { this.constructionCost = cost; return this; }
        public Builder description(String description) { this.description = description; return this; }

        public ShipTemplate build() {
            return new ShipTemplate(this);
        }
    }

    // ============================================================
    // S09 Pre-designed Templates
    // ============================================================

    /**
     * Returns all standard S09 naval ship templates.
     *
     * @return immutable list of all pre-designed ship templates
     */
    public static List<ShipTemplate> getAllTemplates() {
        return Collections.unmodifiableList(List.of(
                scoutCourier(),
                destroyer(),
                lightCruiser(),
                heavyCruiser(),
                battlecruiser(),
                battleship(),
                carrier(),
                dreadnought()
        ));
    }

    /**
     * Returns templates filtered by ship class.
     *
     * @param shipClass the class to filter by
     * @return immutable list of templates matching the class
     */
    public static List<ShipTemplate> getByClass(ShipClass shipClass) {
        return getAllTemplates().stream()
                .filter(t -> t.shipClass == shipClass)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Scout/Courier class — 2,000 tons.
     * Fast, lightly armed reconnaissance ship.
     */
    public static ShipTemplate scoutCourier() {
        return new Builder()
                .id(1)
                .name("Scout/Courier")
                .shipClass(ShipClass.SCOUT)
                .totalTonnage(2000)
                .maneuverDrive(4)
                .jumpDrive(3)
                .energyPoints(20)
                .armor(HighGuardArmor.NONE)
                .nuclearDampers(0)
                .mesonScreens(0)
                .crew(12)
                .marines(0)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE)
                .constructionCost(10_000_000)
                .description("Lightly armed fast scout for reconnaissance and courier duty. Standard Imperial Navy patrol vessel.")
                .build();
    }

    /**
     * Destroyer class — 8,000 tons.
     * Light combat ship for screening and patrol.
     */
    public static ShipTemplate destroyer() {
        return new Builder()
                .id(2)
                .name("Destroyer")
                .shipClass(ShipClass.DESTROYER)
                .totalTonnage(8000)
                .maneuverDrive(3)
                .jumpDrive(3)
                .energyPoints(40)
                .armor(HighGuardArmor.TITANIUM_STEEL_1)
                .nuclearDampers(1)
                .mesonScreens(0)
                .crew(30)
                .marines(10)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.SMALL)
                .constructionCost(50_000_000)
                .description("Light combat ship for fleet screening and system patrol. Equipped with pulse lasers and missile bays.")
                .build();
    }

    /**
     * Light Cruiser class — 16,000 tons.
     * Medium combat ship for independent operations.
     */
    public static ShipTemplate lightCruiser() {
        return new Builder()
                .id(3)
                .name("Light Cruiser")
                .shipClass(ShipClass.LIGHT_CRUISER)
                .totalTonnage(16000)
                .maneuverDrive(3)
                .jumpDrive(3)
                .energyPoints(60)
                .armor(HighGuardArmor.TITANIUM_STEEL_2)
                .nuclearDampers(2)
                .mesonScreens(0)
                .crew(60)
                .marines(20)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.LARGE)
                .constructionCost(150_000_000)
                .description("Medium combat ship capable of independent operations. Beam lasers provide long-range firepower.")
                .build();
    }

    /**
     * Heavy Cruiser class — 32,000 tons.
     * Heavy combat ship for fleet actions.
     */
    public static ShipTemplate heavyCruiser() {
        return new Builder()
                .id(4)
                .name("Heavy Cruiser")
                .shipClass(ShipClass.HEAVY_CRUISER)
                .totalTonnage(32000)
                .maneuverDrive(2)
                .jumpDrive(3)
                .energyPoints(100)
                .armor(HighGuardArmor.TITANIUM_STEEL_3)
                .nuclearDampers(3)
                .mesonScreens(1)
                .crew(120)
                .marines(40)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.LARGE)
                .constructionCost(400_000_000)
                .description("Heavy combat ship for fleet actions. Meson screens provide protection against meson weapons.")
                .build();
    }

    /**
     * Battlecruiser class — 60,000 tons.
     * Fast capital ship with heavy armament.
     */
    public static ShipTemplate battlecruiser() {
        return new Builder()
                .id(5)
                .name("Battlecruiser")
                .shipClass(ShipClass.BATTLECRUISER)
                .totalTonnage(60000)
                .maneuverDrive(3)
                .jumpDrive(3)
                .energyPoints(160)
                .armor(HighGuardArmor.CRYSTALIRON_2)
                .nuclearDampers(4)
                .mesonScreens(2)
                .crew(200)
                .marines(80)
                .turretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE)
                .constructionCost(1_000_000_000L)
                .description("Fast capital ship with particle beam armament. Crystaliron armor and meson screens for heavy defense.")
                .build();
    }

    /**
     * Battleship class — 100,000 tons.
     * Line-of-battle capital ship.
     */
    public static ShipTemplate battleship() {
        return new Builder()
                .id(6)
                .name("Battleship")
                .shipClass(ShipClass.BATTLESHIP)
                .totalTonnage(100000)
                .maneuverDrive(2)
                .jumpDrive(2)
                .energyPoints(240)
                .armor(HighGuardArmor.CRYSTALIRON_3)
                .nuclearDampers(6)
                .mesonScreens(3)
                .crew(400)
                .marines(160)
                .turretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE)
                .constructionCost(2_500_000_000L)
                .description("Line-of-battle capital ship. Heavily armored with crystaliron and equipped with particle beams.")
                .build();
    }

    /**
     * Carrier class — 80,000 tons.
     * Ship designed to project fighter strength.
     */
    public static ShipTemplate carrier() {
        return new Builder()
                .id(7)
                .name("Carrier")
                .shipClass(ShipClass.CARRIER)
                .totalTonnage(80000)
                .maneuverDrive(2)
                .jumpDrive(3)
                .energyPoints(200)
                .armor(HighGuardArmor.TITANIUM_STEEL_3)
                .nuclearDampers(4)
                .mesonScreens(2)
                .crew(300)
                .marines(120)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.DOUBLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.BaySize.LARGE)
                .constructionCost(2_000_000_000L)
                .description("Carrier designed to project fighter strength. Carries fighter wings for extended combat reach.")
                .build();
    }

    /**
     * Dreadnought class — 1,000,000 tons.
     * Largest and most powerful warship.
     */
    public static ShipTemplate dreadnought() {
        return new Builder()
                .id(8)
                .name("Dreadnought")
                .shipClass(ShipClass.DREADNOUGHT)
                .totalTonnage(1000000)
                .maneuverDrive(1)
                .jumpDrive(2)
                .energyPoints(500)
                .armor(HighGuardArmor.BONDED_SUPERDENSE_3)
                .nuclearDampers(10)
                .mesonScreens(5)
                .crew(2000)
                .marines(800)
                .turretWeapon(HighGuardWeapon.MESON_GUN, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.MESON_GUN, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .turretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE)
                .bayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE)
                .bayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE)
                .constructionCost(50_000_000_000L)
                .description("Largest and most powerful warship. Bonded superdense armor and meson guns make it nearly invulnerable.")
                .build();
    }
}
