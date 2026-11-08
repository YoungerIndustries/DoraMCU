package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating {@link UnifiedShip} instances from both DoraMCU and
 * Traveller design paths.
 * <p>
 * This factory provides:
 * </p>
 * <ul>
 *   <li><b>DoraMCU path:</b> Create ships from ShipType with optional Traveller drive configuration</li>
 *   <li><b>Traveller path:</b> Create ships from HighGuardShipDesign with full hull module integration</li>
 *   <li><b>Preset ships:</b> Common ship configurations for quick prototyping</li>
 * </ul>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 *   // DoraMCU path with Traveller drives
 *   UnifiedShip frigate = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
 *       .withFusionDrive(FusionDrive.M3)
 *       .withJumpDrive(JumpDrive.J3)
 *       .withArmor(HighGuardArmor.TITANIUM_STEEL_2)
 *       .build();
 *
 *   // Traveller path
 *   HighGuardShipDesign design = new HighGuardShipDesign(2, "Explorer", 100000);
 *   design.setManeuverDrive(3);
 *   design.setJumpDrive(3);
 *   design.setArmor(HighGuardArmor.CRYSTALIRON_2);
 *   UnifiedShip ship = UnifiedShipFactory.fromDesign(design).build();
 * </pre>
 */
public class UnifiedShipFactory {

    private UnifiedShipFactory() {
        // Utility class
    }

    // ---- DoraMCU Path ----

    /**
     * Starts building a UnifiedShip from a DoraMCU ShipType.
     * <p>
     * The returned builder allows configuring Traveller drives, armor, and components
     * before building the final ship.
     * </p>
     *
     * @param id the unique ship identifier
     * @param shipType the DoraMCU ship classification
     * @return a new builder
     */
    public static DoraMCUBuilder createDoraMCUShip(int id, ShipType shipType) {
        return new DoraMCUBuilder(id, shipType);
    }

    /**
     * Builder for creating UnifiedShip from DoraMCU ShipType.
     */
    public static class DoraMCUBuilder {
        private final int id;
        private final ShipType shipType;
        private String name;
        private FusionDrive fusionDrive = FusionDrive.NONE;
        private JumpDrive jumpDrive = JumpDrive.NONE;
        private HighGuardArmor armor = HighGuardArmor.NONE;
        private final List<ShipComponent> components = new ArrayList<>();
        private int nuclearDampers = 0;
        private int mesonScreens = 0;
        private int computerModel = 0;

        DoraMCUBuilder(int id, ShipType shipType) {
            this.id = id;
            this.shipType = shipType;
            this.name = shipType.name() + "-" + id;
        }

        /**
         * Sets the ship's name.
         */
        public DoraMCUBuilder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the FusionDrive rating.
         */
        public DoraMCUBuilder withFusionDrive(FusionDrive fusionDrive) {
            this.fusionDrive = fusionDrive;
            return this;
        }

        /**
         * Sets the JumpDrive rating.
         */
        public DoraMCUBuilder withJumpDrive(JumpDrive jumpDrive) {
            this.jumpDrive = jumpDrive;
            return this;
        }

        /**
         * Sets the armor type.
         */
        public DoraMCUBuilder withArmor(HighGuardArmor armor) {
            this.armor = armor;
            return this;
        }

        /**
         * Adds a component to the ship.
         */
        public DoraMCUBuilder withComponent(ShipComponent component) {
            components.add(component);
            return this;
        }

        /**
         * Adds an MDrive component.
         */
        public DoraMCUBuilder withMDrive(int quantity) {
            components.add(ShipComponent.mDrive(quantity));
            return this;
        }

        /**
         * Adds a JDrive component.
         */
        public DoraMCUBuilder withJDrive(int quantity) {
            components.add(ShipComponent.jDrive(quantity));
            return this;
        }

        /**
         * Adds a shield component.
         */
        public DoraMCUBuilder withShield(int quantity) {
            components.add(ShipComponent.shield(quantity));
            return this;
        }

        /**
         * Adds a weapon component.
         */
        public DoraMCUBuilder withWeapon(WeaponType weaponType, int quantity) {
            components.add(new ShipComponent(weaponType, quantity));
            return this;
        }

        /**
         * Adds hull reinforcement.
         */
        public DoraMCUBuilder withHullReinforcement(int quantity) {
            components.add(ShipComponent.hullReinforcement(quantity));
            return this;
        }

        /**
         * Sets the nuclear dampers count.
         */
        public DoraMCUBuilder withNuclearDampers(int dampers) {
            this.nuclearDampers = dampers;
            return this;
        }

        /**
         * Sets the meson screens count.
         */
        public DoraMCUBuilder withMesonScreens(int screens) {
            this.mesonScreens = screens;
            return this;
        }

        /**
         * Sets the computer model.
         */
        public DoraMCUBuilder withComputerModel(int model) {
            this.computerModel = model;
            return this;
        }

        /**
         * Builds the UnifiedShip.
         *
         * @return the configured UnifiedShip
         */
        public UnifiedShip build() {
            UnifiedShip ship = new UnifiedShip(id, shipType);
            ship.setName(name);
            ship.setFusionDrive(fusionDrive);
            ship.setJumpDrive(jumpDrive);
            ship.setArmor(armor);

            // Add components
            for (ShipComponent component : components) {
                ship.addComponent(component);
            }

            return ship;
        }
    }

    // ---- Traveller Path ----

    /**
     * Starts building a UnifiedShip from a HighGuardShipDesign.
     *
     * @param design the High Guard ship design
     * @return a new builder
     */
    public static DesignBuilder fromDesign(HighGuardShipDesign design) {
        return new DesignBuilder(design);
    }

    /**
     * Builder for creating UnifiedShip from HighGuardShipDesign.
     */
    public static class DesignBuilder {
        private final HighGuardShipDesign design;
        private final List<ShipComponent> additionalComponents = new ArrayList<>();

        DesignBuilder(HighGuardShipDesign design) {
            if (design == null) {
                throw new IllegalArgumentException("Design must not be null");
            }
            this.design = design;
        }

        /**
         * Adds an additional DoraMCU component to the ship.
         * <p>
         * This allows mixing Traveller design with DoraMCU components.
         * </p>
         */
        public DesignBuilder withAdditionalComponent(ShipComponent component) {
            additionalComponents.add(component);
            return this;
        }

        /**
         * Adds a shield component.
         */
        public DesignBuilder withShield(int quantity) {
            additionalComponents.add(ShipComponent.shield(quantity));
            return this;
        }

        /**
         * Adds a weapon component.
         */
        public DesignBuilder withWeapon(WeaponType weaponType, int quantity) {
            additionalComponents.add(new ShipComponent(weaponType, quantity));
            return this;
        }

        /**
         * Builds the UnifiedShip.
         *
         * @return the configured UnifiedShip
         */
        public UnifiedShip build() {
            UnifiedShip ship = new UnifiedShip(design);

            // Add additional components
            for (ShipComponent component : additionalComponents) {
                ship.addComponent(component);
            }

            return ship;
        }
    }

    // ---- Preset Ships ----

    /**
     * Creates a preset frigate with balanced drives.
     * <p>
     * Specs: 1000 tons, 3M Fusion, J-3 Jump, Titanium Steel armor.
     * </p>
     *
     * @param id the unique ship identifier
     * @return the configured UnifiedShip
     */
    public static UnifiedShip createFrigate(int id) {
        return createDoraMCUShip(id, ShipType.FRIGATE)
                .withName("Frigate-" + id)
                .withFusionDrive(FusionDrive.M3)
                .withJumpDrive(JumpDrive.J3)
                .withArmor(HighGuardArmor.TITANIUM_STEEL_1)
                .withShield(2)
                .build();
    }

    /**
     * Creates a preset destroyer with strong drives.
     * <p>
     * Specs: 2000 tons, 4M Fusion, J-4 Jump, Titanium Steel armor.
     * </p>
     *
     * @param id the unique ship identifier
     * @return the configured UnifiedShip
     */
    public static UnifiedShip createDestroyer(int id) {
        return createDoraMCUShip(id, ShipType.DESTROYER)
                .withName("Destroyer-" + id)
                .withFusionDrive(FusionDrive.M4)
                .withJumpDrive(JumpDrive.J4)
                .withArmor(HighGuardArmor.TITANIUM_STEEL_2)
                .withShield(2)
                .build();
    }

    /**
     * Creates a preset cruiser with heavy drives.
     * <p>
     * Specs: 8000 tons, 5M Fusion, J-5 Jump, Crystaliron armor.
     * </p>
     *
     * @param id the unique ship identifier
     * @return the configured UnifiedShip
     */
    public static UnifiedShip createCruiser(int id) {
        return createDoraMCUShip(id, ShipType.CRUISER)
                .withName("Cruiser-" + id)
                .withFusionDrive(FusionDrive.M5)
                .withJumpDrive(JumpDrive.J5)
                .withArmor(HighGuardArmor.CRYSTALIRON_2)
                .withShield(3)
                .build();
    }

    /**
     * Creates a preset battleship with maximum firepower.
     * <p>
     * Specs: 27000 tons, 6M Fusion, J-6 Jump, Crystaliron armor.
     * </p>
     *
     * @param id the unique ship identifier
     * @return the configured UnifiedShip
     */
    public static UnifiedShip createBattleship(int id) {
        return createDoraMCUShip(id, ShipType.BATTLESHIP)
                .withName("Battleship-" + id)
                .withFusionDrive(FusionDrive.M6)
                .withJumpDrive(JumpDrive.J6)
                .withArmor(HighGuardArmor.CRYSTALIRON_3)
                .withShield(3)
                .build();
    }

    /**
     * Creates a preset Traveller-style 100kt ship.
     * <p>
     * Specs: 100,000 tons, 3M Fusion, J-3 Jump, Titanium Steel armor.
     * </p>
     *
     * @param id the unique ship identifier
     * @param name the ship's name
     * @return the configured UnifiedShip
     */
    public static UnifiedShip createTravellerShip(int id, String name) {
        HighGuardShipDesign design = new HighGuardShipDesign(id, name, 100000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);
        design.setArmor(HighGuardArmor.TITANIUM_STEEL_2);
        design.setPowerPlantRating(3);
        design.setCrew(100);

        return fromDesign(design).build();
    }

    /**
     * Creates a preset Traveller-style 400kt capital ship.
     * <p>
     * Specs: 400,000 tons, 4M Fusion, J-4 Jump, Crystaliron armor.
     * </p>
     *
     * @param id the unique ship identifier
     * @param name the ship's name
     * @return the configured UnifiedShip
     */
    public static UnifiedShip createTravellerCapitalShip(int id, String name) {
        HighGuardShipDesign design = new HighGuardShipDesign(id, name, 400000);
        design.setManeuverDrive(4);
        design.setJumpDrive(4);
        design.setArmor(HighGuardArmor.CRYSTALIRON_3);
        design.setPowerPlantRating(5);
        design.setCrew(500);

        return fromDesign(design).build();
    }
}
