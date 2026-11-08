package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipComponent;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.hex.WeaponType;

import java.util.List;
import java.util.Objects;

/**
 * Converts a High Guard {@link HighGuardShipDesign} into a hex-based {@link Ship} instance.
 * <p>
 * This bridge maps High Guard design concepts to the hex combat model:
 * </p>
 * <ul>
 *   <li><b>Armored Hulls</b> → Hull reinforcement components scaled by armor type</li>
 *   <li><b>Hull Points</b> → Override ship's hull points from design</li>
 *   <li><b>Thrust</b> → MDrive component quantity</li>
 *   <li><b>Agility</b> → Evasion bonus stored in ship name metadata</li>
 *   <li><b>Energy Points</b> → Shield component quantity</li>
 *   <li><b>Turret Weapons</b> → BEAM/MISSILE weapon components scaled by turret size</li>
 *   <li><b>Bay Weapons</b> → MISSILE weapon components scaled by bay size</li>
 *   <li><b>Nuclear Dampers</b> → Additional hull reinforcement</li>
 *   <li><b>Meson Screens</b> → Additional shield components</li>
 * </ul>
 * <p>
 * High Guard weapons are mapped to hex weapon types:
 * </p>
 * <ul>
 *   <li>Pulse Laser, Beam Laser, Particle Beam, Fusion Gun, Meson Gun → BEAM</li>
 *   <li>Sandcaster → PULSION (special weapon)</li>
 *   <li>Missiles → MISSILE</li>
 *   <li>Torpedoes → TORPEDO</li>
 * </ul>
 */
public class HighGuardShipFactory {

    private int nextId;

    /**
     * Creates a new factory with auto-incrementing ship IDs starting from 1.
     */
    public HighGuardShipFactory() {
        this(1);
    }

    /**
     * Creates a new factory with auto-incrementing ship IDs starting from the given value.
     *
     * @param startId the first ID to assign
     */
    public HighGuardShipFactory(int startId) {
        this.nextId = startId;
    }

    /**
     * Converts a High Guard ShipDesign into a hex Ship instance.
     * <p>
     * The resulting ship can be added to a {@link com.yisy.modules.doramcu.hex.Fleet}
     * and used in {@link com.yisy.modules.doramcu.hex.Battle} combat.
     * </p>
     *
     * @param design the High Guard ship design to convert
     * @return a new Ship instance representing the design in the hex combat model
     */
    public Ship convert(HighGuardShipDesign design) {
        Objects.requireNonNull(design, "Design must not be null");

        ShipType shipType = tonnageToShipType(design.getTotalTonnage());
        int shipId = nextId++;

        Ship ship = new Ship(shipId, shipType);
        ship.setName(design.getName());

        // Add turret weapon components
        addTurretWeaponComponents(ship, design);

        // Add bay weapon components
        addBayWeaponComponents(ship, design);

        // Add shield components based on energy points and meson screens
        addShieldComponents(ship, design);

        // Add drive components based on thrust and jump drive
        addDriveComponents(ship, design);

        // Add hull reinforcement based on armor type
        addArmorComponents(ship, design);

        // Add nuclear damper reinforcement
        addNuclearDamperComponents(ship, design);

        // Override hull points from High Guard design
        overrideHullPoints(ship, design);

        return ship;
    }

    /**
     * Maps High Guard tonnage to the closest hex ShipType.
     * <p>
     * High Guard ships range from 100 tons (small fighters) to 1,000,000 tons
     * (Dreadnought-class capital ships).
     * </p>
     *
     * @param tonnage the total ship tonnage
     * @return the matching ShipType
     */
    static ShipType tonnageToShipType(int tonnage) {
        if (tonnage < 5000) {
            return ShipType.FRIGATE;
        } else if (tonnage < 10000) {
            return ShipType.DESTROYER;
        } else if (tonnage < 20000) {
            return ShipType.CRUISER;
        } else if (tonnage < 40000) {
            return ShipType.BATTLESHIP;
        } else if (tonnage < 60000) {
            return ShipType.CARRIER;
        } else {
            return ShipType.MOTHERSHIP;
        }
    }

    /**
     * Converts High Guard turret weapons to hex ShipComponent weapon components.
     * <p>
     * Turret size (Single/Double/Triple) scales the component quantity.
     * Weapon category determines the hex weapon type.
     * </p>
     */
    private void addTurretWeaponComponents(Ship ship, HighGuardShipDesign design) {
        List<HighGuardShipDesign.WeaponMount> turrets = design.getTurretWeapons();
        if (turrets.isEmpty()) {
            return;
        }

        for (HighGuardShipDesign.WeaponMount mount : turrets) {
            if (ship.getAvailableSlots() <= 0) {
                break;
            }

            HighGuardWeapon weapon = mount.getWeapon();
            int quantity = mount.getTurretSize() != null ? mount.getTurretSize().getMultiplier() : 1;

            WeaponType weaponType = mapHighGuardWeaponType(weapon);
            if (weaponType == null) {
                continue;
            }

            try {
                ship.addComponent(new ShipComponent(weaponType, quantity));
            } catch (IllegalStateException e) {
                // No more slots available
                break;
            }
        }
    }

    /**
     * Converts High Guard bay weapons to hex ShipComponent weapon components.
     * <p>
     * Bay size (50/100 tons) determines the component quantity.
     * Larger bays produce more missiles/torpedoes.
     * </p>
     */
    private void addBayWeaponComponents(Ship ship, HighGuardShipDesign design) {
        List<HighGuardShipDesign.WeaponMount> bays = design.getBayWeapons();
        if (bays.isEmpty()) {
            return;
        }

        for (HighGuardShipDesign.WeaponMount mount : bays) {
            if (ship.getAvailableSlots() <= 0) {
                break;
            }

            HighGuardWeapon weapon = mount.getWeapon();
            int quantity = mount.getBaySize() != null ? mount.getBaySize().getTons() / 50 : 1;

            WeaponType weaponType = mapHighGuardWeaponType(weapon);
            if (weaponType == null) {
                continue;
            }

            try {
                ship.addComponent(new ShipComponent(weaponType, quantity));
            } catch (IllegalStateException e) {
                // No more slots available
                break;
            }
        }
    }

    /**
     * Maps a High Guard weapon to a hex WeaponType.
     *
     * @param weapon the High Guard weapon
     * @return the corresponding hex WeaponType, or null if unmappable
     */
    static WeaponType mapHighGuardWeaponType(HighGuardWeapon weapon) {
        if (weapon == null) {
            return null;
        }

        HighGuardWeapon.WeaponCategory category = weapon.getCategory();
        switch (category) {
            case BEAM:
                return WeaponType.BEAM;
            case MISSILE:
                return WeaponType.MISSILE;
            case TORPEDO:
                return WeaponType.TORPEDO;
            case SPECIAL:
                // Sandcasters map to PULSION as a special weapon type
                return WeaponType.PULSION;
            default:
                return null;
        }
    }

    /**
     * Adds shield components based on energy points and meson screens.
     * <p>
     * Energy points determine base shield capacity.
     * Meson screens provide additional defensive layers.
     * </p>
     */
    private void addShieldComponents(Ship ship, HighGuardShipDesign design) {
        int energyPoints = design.getEnergyPoints();
        int mesonScreens = design.getMesonScreens();

        if (energyPoints <= 0 && mesonScreens <= 0) {
            return;
        }

        if (ship.getAvailableSlots() <= 0) {
            return;
        }

        try {
            // Energy points determine shield quantity: 10 EP = quantity 1, 20 EP = quantity 2, etc.
            int shieldQty = Math.max(1, energyPoints / 10);
            shieldQty = Math.min(3, shieldQty); // Cap at 3 per component

            if (mesonScreens > 0) {
                // Meson screens add an extra shield component
                ship.addComponent(ShipComponent.shield(shieldQty));
                if (ship.getAvailableSlots() > 0) {
                    ship.addComponent(ShipComponent.shield(Math.min(2, mesonScreens)));
                }
            } else {
                ship.addComponent(ShipComponent.shield(shieldQty));
            }
        } catch (IllegalStateException e) {
            // No slots available
        }
    }

    /**
     * Adds MDrive and JDrive components based on thrust and jump drive ratings.
     */
    private void addDriveComponents(Ship ship, HighGuardShipDesign design) {
        int maneuverDrive = design.getManeuverDrive();
        int jumpDrive = design.getJumpDrive();

        // Add MDrive component based on maneuver drive
        if (maneuverDrive >= 1 && ship.getAvailableSlots() > 0) {
            try {
                // Scale: 1-2 → quantity 1, 3-4 → quantity 2, 5-6 → quantity 3
                int mDriveQty = 1;
                if (maneuverDrive >= 5) mDriveQty = 3;
                else if (maneuverDrive >= 3) mDriveQty = 2;
                ship.addComponent(ShipComponent.mDrive(mDriveQty));
            } catch (IllegalStateException e) {
                // No slots
            }
        }

        // Add JDrive component based on jump drive
        if (jumpDrive >= 1 && ship.getAvailableSlots() > 0) {
            try {
                int jDriveQty = 1;
                if (jumpDrive >= 5) jDriveQty = 3;
                else if (jumpDrive >= 3) jDriveQty = 2;
                ship.addComponent(ShipComponent.jDrive(jDriveQty));
            } catch (IllegalStateException e) {
                // No slots
            }
        }
    }

    /**
     * Adds hull reinforcement components based on High Guard armor type.
     * <p>
     * Armor types map to reinforcement levels:
     * </p>
     * <ul>
     *   <li>NONE → No reinforcement</li>
     *   <li>TITANIUM_STEEL → 1 hull reinforcement component</li>
     *   <li>CRYSTALIRON → 2 hull reinforcement components</li>
     *   <li>BONDED_SUPERDENSE → 3 hull reinforcement components</li>
     * </ul>
     */
    private void addArmorComponents(Ship ship, HighGuardShipDesign design) {
        HighGuardArmor armor = design.getArmor();
        if (armor == HighGuardArmor.NONE) {
            return;
        }

        if (ship.getAvailableSlots() <= 0) {
            return;
        }

        try {
            HighGuardArmor.ArmorMaterial material = armor.getMaterial();
            int layers = armor.getLayers();
            if (material == HighGuardArmor.ArmorMaterial.NONE) {
                return;
            }

            // Scale reinforcement by armor material and layers
            int baseReinforcement;
            switch (material) {
                case TITANIUM_STEEL:
                    baseReinforcement = 1;
                    break;
                case CRYSTALIRON:
                    baseReinforcement = 2;
                    break;
                case BONDED_SUPERDENSE:
                    baseReinforcement = 3;
                    break;
                default:
                    return;
            }

            // Add reinforcement components based on layers
            int qty = Math.min(3, baseReinforcement + layers - 1);
            ship.addComponent(ShipComponent.hullReinforcement(qty));
            if (layers >= 3 && ship.getAvailableSlots() > 0) {
                ship.addComponent(ShipComponent.hullReinforcement(Math.min(2, layers - 2)));
            }
        } catch (IllegalStateException e) {
            // No slots
        }
    }

    /**
     * Adds hull reinforcement for nuclear dampers.
     * <p>
     * Nuclear dampers reduce nuclear missile damage, represented as
     * additional hull reinforcement in the hex model.
     * </p>
     */
    private void addNuclearDamperComponents(Ship ship, HighGuardShipDesign design) {
        int dampers = design.getNuclearDampers();
        if (dampers <= 0) {
            return;
        }

        if (ship.getAvailableSlots() <= 0) {
            return;
        }

        try {
            int qty = Math.min(2, dampers);
            ship.addComponent(ShipComponent.hullReinforcement(qty));
        } catch (IllegalStateException e) {
            // No slots
        }
    }

    /**
     * Overrides the ship's hull points to match the High Guard design's hull points.
     * <p>
     * High Guard hull points are calculated as:
     * HullRate × (1 + ArmorFactor / 10), which can exceed the hex ShipType base.
     * </p>
     */
    private void overrideHullPoints(Ship ship, HighGuardShipDesign design) {
        int hgHull = design.getHullPoints();
        if (hgHull <= 0) {
            return;
        }

        int current = ship.getCurrentHullPoints();
        int diff = hgHull - current;
        if (diff > 0) {
            ship.repairHull(diff);
        }
    }
}
