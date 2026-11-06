package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.ConstructionModuleType;
import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.HullShape;
import com.yisy.modules.doramcu.hex.Material;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.ResourcePipelineManager;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipComponent;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.hex.StrategicResource;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.ShipOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages resource-related operations: ship affordability checks,
 * strategic resource validation, and ship build queuing.
 * <p>
 * Includes Dora AI integration for AI-assisted resource management advice.
 * </p>
 */
public class ResourceManager {

    private final GameContext context;

    /**
     * Creates a new ResourceManager.
     *
     * @param context the shared game context
     */
    public ResourceManager(GameContext context) {
        this.context = context;
    }

    /**
     * Generates resource management advice for a player.
     *
     * @param player the player requesting advice
     * @return the resource advice string
     */
    public String getResourceAdvice(Player player) {
        return String.format(
            "Player owns %d planets and has %d ships in queue.",
            player.getOwnedPlanetIds().size(),
            player.getShipQueue().size()
        );
    }

    // Flat resource methods (canAffordShip, canAffordHull) removed – use pipeline modules instead

    /**
     * Creates a hull module for a ship with the specified shape and materials.
     *
     * @param shipType the ship type name (e.g., "FRIGATE")
     * @param shape the hull shape name (e.g., "CUBE", "WEDGE"), or null for CUBE
     * @param exteriorMaterial the exterior material value (0-5), or null for Aluminum (1)
     * @param interiorMaterial the interior material value (0-5), or null for Empty (0)
     * @return a configured hull module
     */
    public HullModule createHullModule(String shipType, String shape, Integer exteriorMaterial, Integer interiorMaterial) {
        HullModule module = new HullModule(shipType);
        configureHullModule(module, shape, exteriorMaterial, interiorMaterial);
        return module;
    }

    /**
     * Creates all hull modules for a ship based on its ship type's cubic meters.
     * The number of modules is determined by the ship type's cubic meters divided by 1000.
     *
     * @param shipType the ship type (e.g., ShipType.FRIGATE)
     * @param shape the hull shape name (e.g., "CUBE", "WEDGE"), or null for CUBE
     * @param exteriorMaterial the exterior material value (0-5), or null for Aluminum (1)
     * @param interiorMaterial the interior material value (0-5), or null for Empty (0)
     * @return a list of configured hull modules
     */
    public List<HullModule> createHullModules(ShipType shipType, String shape, Integer exteriorMaterial, Integer interiorMaterial) {
        if (shipType == null) {
            throw new IllegalArgumentException("Ship type must not be null");
        }
        int moduleCount = shipType.getHullModuleCount();
        List<HullModule> modules = new ArrayList<>(moduleCount);
        for (int i = 0; i < moduleCount; i++) {
            HullModule module = new HullModule(shipType.name() + "-Module-" + i);
            configureHullModule(module, shape, exteriorMaterial, interiorMaterial);
            modules.add(module);
        }
        return modules;
    }

    /**
     * Configures a hull module with the specified shape and materials.
     *
     * @param module the module to configure
     * @param shape the hull shape name, or null for CUBE
     * @param exteriorMaterial the exterior material value, or null for Aluminum
     * @param interiorMaterial the interior material value, or null for Empty
     */
    private void configureHullModule(HullModule module, String shape, Integer exteriorMaterial, Integer interiorMaterial) {
        // Set shape
        if (shape != null && !shape.isEmpty()) {
            try {
                module.setShape(HullShape.valueOf(shape.toUpperCase()));
            } catch (IllegalArgumentException e) {
                module.setShape(HullShape.CUBE);
            }
        }

        // Set exterior material
        Material extMat = Material.Aluminum;
        if (exteriorMaterial != null) {
            try {
                extMat = Material.fromValue(exteriorMaterial);
            } catch (IllegalArgumentException e) {
                extMat = Material.Aluminum;
            }
        }
        module.setExteriorMaterial(extMat);

        // Set interior material
        Material intMat = Material.Empty;
        if (interiorMaterial != null) {
            try {
                intMat = Material.fromValue(interiorMaterial);
            } catch (IllegalArgumentException e) {
                intMat = Material.Empty;
            }
        }
        module.setInteriorMaterial(intMat);
    }

    /**
     * Checks if a player can build a ship given available strategic resources.
     *
     * @param player the player
     * @param ship the ship to build
     * @return true if all strategic resource requirements are met
     */
    public boolean canBuildShipWithResources(Player player, Ship ship) {
        if (player == null || ship == null) {
            return false;
        }
        Map<Integer, Planet> planets = context.getPlanets();
        Set<StrategicResource> available = player.getAvailableStrategicResources(planets);
        return ship.canBuildWith(available);
    }

    /**
     * Queues a ship for construction using the resource pipeline's module system.
     *
     * @param player the ordering player
     * @param ship the ship to build
     * @param targetFleetId the fleet to add the completed ship to
     * @param pipelineManager the resource pipeline manager
     * @return the ship order created, or null if modules are insufficient
     */
    public ShipOrder queueShipBuild(Player player, Ship ship, int targetFleetId,
                                     ResourcePipelineManager pipelineManager) {
        if (!canAffordShipWithModules(player, ship, pipelineManager)) {
            return null;
        }
        if (!canBuildShipWithResources(player, ship)) {
            return null;
        }

        // Consume modules from pipeline
        Map<ConstructionModuleType, Integer> requirements = calculateModuleRequirements(ship);
        pipelineManager.consumeModules(requirements);

        int buildTime = ship.getBuildTime();
        ShipOrder order = new ShipOrder(ship, buildTime, targetFleetId);
        player.addShipOrder(order);
        return order;
    }

    // ---- Module-Based Construction (Resource Pipeline) ----

    /**
     * Calculates the construction module requirements for a ship.
     * <p>
     * This method maps a ship's hull modules to construction module types
     * based on the components installed in each hull module.
     * </p>
     *
     * @param ship the ship to calculate module requirements for
     * @return map of construction module types to required quantities
     */
    public Map<ConstructionModuleType, Integer> calculateModuleRequirements(Ship ship) {
        Map<ConstructionModuleType, Integer> requirements = new java.util.EnumMap<>(ConstructionModuleType.class);

        if (ship == null || ship.getHullModules().isEmpty()) {
            return requirements;
        }

        // Count hull exterior cells for HULL_MODULE across all modules
        int hullCells = 0;
        for (HullModule module : ship.getHullModules()) {
            hullCells += module.getShapeCellCount();
        }
        requirements.put(ConstructionModuleType.HULL_MODULE, Math.max(1, hullCells / 5));

        // Count components for module requirements
        int weaponCount = 0;
        int shieldCount = 0;
        int hullReinforcementCount = 0;

        for (ShipComponent component : ship.getComponents()) {
            switch (component.getType()) {
                case WEAPON -> weaponCount++;
                case SHIELD -> shieldCount++;
                case HULL_REINFORCEMENT -> hullReinforcementCount++;
            }
        }

        if (weaponCount > 0) {
            requirements.put(ConstructionModuleType.WEAPON_MODULE, weaponCount);
        }
        if (shieldCount > 0) {
            requirements.put(ConstructionModuleType.SHIELD_MODULE, shieldCount);
        }
        // Hull reinforcements add to hull module count
        if (hullReinforcementCount > 0) {
            requirements.merge(ConstructionModuleType.HULL_MODULE, hullReinforcementCount, Integer::sum);
        }

        return requirements;
    }

    /**
     * Checks if a player can build a ship using the resource pipeline's module system.
     *
     * @param player the player
     * @param ship the ship to build
     * @param pipelineManager the resource pipeline manager
     * @return true if sufficient modules are available
     */
    public boolean canAffordShipWithModules(Player player, Ship ship, ResourcePipelineManager pipelineManager) {
        if (player == null || ship == null || pipelineManager == null) {
            return false;
        }
        Map<ConstructionModuleType, Integer> requirements = calculateModuleRequirements(ship);
        return pipelineManager.canBuildWithModules(requirements);
    }
}
