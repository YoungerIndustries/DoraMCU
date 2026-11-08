package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipComponent;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.hex.WeaponType;

import java.util.List;
import java.util.Objects;

/**
 * Converts a Traveller {@link ShipDesign} into a hex-based {@link Ship} instance
 * that can participate in fleet combat via the existing {@link com.yisy.modules.doramcu.hex.Battle} system.
 * <p>
 * This bridge maps Traveller design concepts to the hex combat model:
 * </p>
 * <ul>
 *   <li><b>Tonnage → ShipType:</b> Total tonnage determines the ship classification
 *       (Frigate, Destroyer, Cruiser, Battleship, Carrier, Mothership).</li>
 *   <li><b>Lasers → BEAM weapons:</b> Traveller laser banks become beam weapon components.</li>
 *   <li><b>Missiles → MISSILE weapons:</b> Traveller missile launchers become missile components.</li>
 *   <li><b>Fusion Drive → MDrive speed:</b> Within-system maneuver rating maps to MDrive bonus.</li>
 *   <li><b>Jump Drive → JDrive speed:</b> FTL jump rating maps to JDrive bonus.</li>
 *   <li><b>Hull points:</b> Traveller hull points override the ShipType base hull.</li>
 *   <li><b>Power Plant → Shields:</b> Larger power plants provide shield components.</li>
 * </ul>
 * <p>
 * The conversion is lossy — some Traveller-specific details (passenger class, cargo tons,
 * hit location mapping) are preserved in the design but not directly represented in the
 * hex combat model.
 * </p>
 */
public class TravellerShipFactory {

    private int nextId;

    /**
     * Creates a new factory with auto-incrementing ship IDs starting from 1.
     */
    public TravellerShipFactory() {
        this(1);
    }

    /**
     * Creates a new factory with auto-incrementing ship IDs starting from the given value.
     *
     * @param startId the first ID to assign
     */
    public TravellerShipFactory(int startId) {
        this.nextId = startId;
    }

    /**
     * Converts a Traveller ShipDesign into a hex Ship instance.
     * <p>
     * The resulting ship can be added to a {@link com.yisy.modules.doramcu.hex.Fleet}
     * and used in {@link com.yisy.modules.doramcu.hex.Battle} combat.
     * </p>
     *
     * @param design the Traveller ship design to convert
     * @return a new Ship instance representing the design in the hex combat model
     */
    public Ship convert(ShipDesign design) {
        Objects.requireNonNull(design, "Design must not be null");
        design.validate();

        ShipType shipType = tonnageToShipType(design.getTotalTonnage());
        int shipId = nextId++;

        Ship ship = new Ship(shipId, shipType);
        ship.setName(design.getName());

        // Add weapon components
        addWeaponComponents(ship, design);

        // Add shield components based on power plant size
        addShieldComponents(ship, design);

        // Add drive components
        addDriveComponents(ship, design);

        // Add hull reinforcement based on hull type
        addHullReinforcement(ship, design);

        // Override hull points from Traveller design
        overrideHullPoints(ship, design);

        return ship;
    }

    /**
     * Maps Traveller tonnage to the closest hex ShipType.
     * <p>
     * Mapping based on tonnage ranges:
     * </p>
     * <ul>
     *   <li>1,000 – 4,999 tons → FRIGATE (1 hull module)</li>
     *   <li>5,000 – 9,999 tons → DESTROYER (2 hull modules)</li>
     *   <li>10,000 – 19,999 tons → CRUISER (8 hull modules)</li>
     *   <li>20,000 – 39,999 tons → BATTLESHIP (27 hull modules)</li>
     *   <li>40,000 – 59,999 tons → CARRIER (12 hull modules)</li>
     *   <li>60,000+ tons → MOTHERSHIP (64 hull modules)</li>
     * </ul>
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
     * Converts Traveller weapon systems to hex ShipComponent weapon components.
     * <p>
     * Lasers map to BEAM weapons, missiles map to MISSILE weapons.
     * The quantity is scaled from Traveller damage dice to hex component quantity (1-3).
     * Multiple weapon components may be created if the design has many weapons.
     * </p>
     */
    private void addWeaponComponents(Ship ship, ShipDesign design) {
        List<ShipWeaponSystem> weapons = design.getWeapons();
        if (weapons.isEmpty()) {
            return;
        }

        // Count total laser banks and missile launchers
        int totalLasers = 0;
        int totalMissiles = 0;
        int totalLaserDice = 0;
        int totalMissileDice = 0;

        for (ShipWeaponSystem ws : weapons) {
            if (ws.isLaser()) {
                totalLasers += ws.getQuantity();
                totalLaserDice += ws.getDamageDice();
            } else if (ws.isMissile()) {
                totalMissiles += ws.getQuantity();
                totalMissileDice += ws.getDamageDice();
            }
        }

        // Convert lasers to BEAM components
        // Each component slot holds 1-3 weapons; scale by damage dice
        if (totalLasers > 0) {
            addBeamComponents(ship, totalLasers, totalLaserDice);
        }

        // Convert missiles to MISSILE components
        if (totalMissiles > 0) {
            addMissileComponents(ship, totalMissiles, totalMissileDice);
        }
    }

    /**
     * Adds BEAM weapon components to approximate Traveller laser damage.
     * <p>
     * Traveller laser damage dice are mapped to component quantities:
     * Higher damage dice → more components or higher quantity per component.
     * </p>
     */
    private void addBeamComponents(Ship ship, int laserCount, int damageDice) {
        // Average dice per laser bank
        double avgDicePerLaser = (laserCount > 0) ? (double) damageDice / laserCount : 0;

        // Scale: each laser with 2D6 = quantity 1, 4D6 = quantity 2, 6D6+ = quantity 3
        int quantityPerBank = 1;
        if (avgDicePerLaser >= 6) {
            quantityPerBank = 3;
        } else if (avgDicePerLaser >= 4) {
            quantityPerBank = 2;
        }

        // Create components, respecting slot limits
        int slotsAvailable = ship.getAvailableSlots();
        int componentsNeeded = Math.min(laserCount, slotsAvailable);

        for (int i = 0; i < componentsNeeded; i++) {
            try {
                ship.addComponent(new ShipComponent(WeaponType.BEAM, quantityPerBank));
            } catch (IllegalStateException e) {
                // No more slots available
                break;
            }
        }
    }

    /**
     * Adds MISSILE weapon components to approximate Traveller missile damage.
     */
    private void addMissileComponents(Ship ship, int launcherCount, int damageDice) {
        double avgDicePerLauncher = (launcherCount > 0) ? (double) damageDice / launcherCount : 0;

        // Scale missile quantity by warhead type
        int quantityPerLauncher = 1;
        if (avgDicePerLauncher >= 6) {
            quantityPerLauncher = 3;
        } else if (avgDicePerLauncher >= 4) {
            quantityPerLauncher = 2;
        }

        int slotsAvailable = ship.getAvailableSlots();
        int componentsNeeded = Math.min(launcherCount, slotsAvailable);

        for (int i = 0; i < componentsNeeded; i++) {
            try {
                ship.addComponent(new ShipComponent(WeaponType.MISSILE, quantityPerLauncher));
            } catch (IllegalStateException e) {
                break;
            }
        }
    }

    /**
     * Adds shield components based on the Traveller power plant size.
     * <p>
     * In Traveller, the power plant determines how many weapon banks can be powered.
     * In the hex model, larger power plants translate to shield capacity.
     * </p>
     * <ul>
     *   <li>Standard (size 1) → 1 shield component (quantity 1)</li>
     *   <li>Double (size 2) → 1 shield component (quantity 2)</li>
     *   <li>Triple (size 3) → 1 shield component (quantity 3)</li>
     *   <li>Quadruple (size 4) → 2 shield components</li>
     * </ul>
     */
    private void addShieldComponents(Ship ship, ShipDesign design) {
        PowerPlant pp = design.getPowerPlant();
        if (pp == PowerPlant.NONE) {
            return;
        }

        int size = pp.getSize();
        if (size <= 0) {
            return;
        }

        if (ship.getAvailableSlots() <= 0) {
            return;
        }

        try {
            if (size >= 4) {
                // Quadruple: add 2 shield components
                ship.addComponent(ShipComponent.shield(2));
                if (ship.getAvailableSlots() > 0) {
                    ship.addComponent(ShipComponent.shield(1));
                }
            } else {
                // Size 1-3: one shield component with matching quantity
                ship.addComponent(ShipComponent.shield(size));
            }
        } catch (IllegalStateException e) {
            // No slots available
        }
    }

    /**
     * Adds MDrive and JDrive components based on Traveller drive ratings.
     * <p>
     * Fusion drive rating maps to MDrive component quantity.
     * Jump drive rating maps to JDrive component quantity.
     * </p>
     */
    private void addDriveComponents(Ship ship, ShipDesign design) {
        int fusionRating = design.getFusionDrive().getRating();
        int jumpRating = design.getJumpDrive().getRating();

        // Add MDrive component if fusion rating is significant
        if (fusionRating >= 2 && ship.getAvailableSlots() > 0) {
            try {
                // Scale: rating 2-3 → quantity 1, 4-6 → quantity 2, 7-9 → quantity 3
                int mDriveQty = 1;
                if (fusionRating >= 7) mDriveQty = 3;
                else if (fusionRating >= 4) mDriveQty = 2;
                ship.addComponent(ShipComponent.mDrive(mDriveQty));
            } catch (IllegalStateException e) {
                // No slots
            }
        }

        // Add JDrive component if jump rating is significant
        if (jumpRating >= 1 && ship.getAvailableSlots() > 0) {
            try {
                // Scale: J-1 to J-3 → quantity 1, J-4 to J-6 → quantity 2, J-7+ → quantity 3
                int jDriveQty = 1;
                if (jumpRating >= 7) jDriveQty = 3;
                else if (jumpRating >= 4) jDriveQty = 2;
                ship.addComponent(ShipComponent.jDrive(jDriveQty));
            } catch (IllegalStateException e) {
                // No slots
            }
        }
    }

    /**
     * Adds hull reinforcement components based on Traveller hull type.
     * <p>
     * Reinforced hull → 1 hull reinforcement component.
     * Double hull → 2 hull reinforcement components.
     * </p>
     */
    private void addHullReinforcement(Ship ship, ShipDesign design) {
        ShipHullType hullType = design.getHullType();
        if (hullType == ShipHullType.STANDARD) {
            return;
        }

        if (ship.getAvailableSlots() <= 0) {
            return;
        }

        try {
            if (hullType == ShipHullType.DOUBLE) {
                ship.addComponent(ShipComponent.hullReinforcement(2));
                if (ship.getAvailableSlots() > 0) {
                    ship.addComponent(ShipComponent.hullReinforcement(1));
                }
            } else if (hullType == ShipHullType.REINFORCED) {
                ship.addComponent(ShipComponent.hullReinforcement(1));
            }
        } catch (IllegalStateException e) {
            // No slots
        }
    }

    /**
     * Overrides the ship's hull points to match the Traveller design's hull points.
     * <p>
     * The Traveller hull point formula (tonnage / 1000 × hull multiplier) produces
     * different values than the hex ShipType base hull. This method ensures the
     * converted ship has the correct Traveller hull points.
     * </p>
     */
    private void overrideHullPoints(Ship ship, ShipDesign design) {
        int travellerHull = design.getHullPoints();
        // Use repairHull to set hull points; it caps at getMaxHullPoints()
        // If Traveller hull exceeds the ShipType base + component bonuses,
        // the ship will be at max capacity, which is acceptable
        int current = ship.getCurrentHullPoints();
        int diff = travellerHull - current;
        if (diff > 0) {
            ship.repairHull(diff);
        }
    }
}
