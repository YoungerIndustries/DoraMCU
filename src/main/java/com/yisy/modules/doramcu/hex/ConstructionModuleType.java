package com.yisy.modules.doramcu.hex;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents module types assembled from refined resources for construction.
 * <p>
 * Each module type has a recipe that specifies the refined resources required
 * to produce one unit of that module. Modules are consumed when building ships
 * and facilities.
 * </p>
 */
public enum ConstructionModuleType {

    /**
     * Hull module for ship structure.
     * Recipe: 2 metal alloy + 1 composite
     */
    HULL_MODULE("Hull Module", Map.of(
            RefinedResourceType.METAL_ALLOY, 2,
            RefinedResourceType.COMPOSITE, 1
    ), 2),

    /**
     * Weapon module for ship armament.
     * Recipe: 1 metal alloy + 1 power cell
     */
    WEAPON_MODULE("Weapon Module", Map.of(
            RefinedResourceType.METAL_ALLOY, 1,
            RefinedResourceType.POWER_CELL, 1
    ), 1),

    /**
     * Shield module for ship defense.
     * Recipe: 1 power cell + 1 composite
     */
    SHIELD_MODULE("Shield Module", Map.of(
            RefinedResourceType.POWER_CELL, 1,
            RefinedResourceType.COMPOSITE, 1
    ), 1),

    /**
     * Engine module for ship propulsion.
     * Recipe: 1 fuel + 1 power cell
     */
    ENGINE_MODULE("Engine Module", Map.of(
            RefinedResourceType.FUEL, 1,
            RefinedResourceType.POWER_CELL, 1
    ), 1),

    /**
     * Facility module for station construction.
     * Recipe: 2 metal alloy + 1 nutrient
     */
    FACILITY_MODULE("Facility Module", Map.of(
            RefinedResourceType.METAL_ALLOY, 2,
            RefinedResourceType.NUTRIENT, 1
    ), 3);

    private final String displayName;
    private final Map<RefinedResourceType, Integer> recipe;
    private final int assemblyTimeTurns;

    ConstructionModuleType(String displayName, Map<RefinedResourceType, Integer> recipe, int assemblyTimeTurns) {
        this.displayName = displayName;
        this.recipe = Map.copyOf(recipe);
        this.assemblyTimeTurns = assemblyTimeTurns;
    }

    /** @return the human-readable display name */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the recipe for this module type.
     *
     * @return unmodifiable map of refined resource types to required quantities
     */
    public Map<RefinedResourceType, Integer> getRecipe() {
        return recipe;
    }

    /**
     * Returns the number of turns required to assemble one unit of this module.
     */
    public int getAssemblyTimeTurns() {
        return assemblyTimeTurns;
    }

    /**
     * Checks if the given inventory can produce one unit of this module.
     *
     * @param inventory the current refined resource inventory
     * @return true if all required resources are available in sufficient quantity
     */
    public boolean canProduce(Map<RefinedResourceType, Integer> inventory) {
        for (Map.Entry<RefinedResourceType, Integer> entry : recipe.entrySet()) {
            int available = inventory.getOrDefault(entry.getKey(), 0);
            if (available < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the total refined resource cost for producing the specified quantity.
     *
     * @param quantity the number of modules to produce
     * @return map of refined resource types to total required quantities
     */
    public Map<RefinedResourceType, Integer> getCostForQuantity(int quantity) {
        Map<RefinedResourceType, Integer> cost = new EnumMap<>(RefinedResourceType.class);
        for (Map.Entry<RefinedResourceType, Integer> entry : recipe.entrySet()) {
            cost.put(entry.getKey(), entry.getValue() * quantity);
        }
        return cost;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
