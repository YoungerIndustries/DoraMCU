package com.yisy.modules.doramcu.hex;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Generic storage for resources with capacity limits.
 * <p>
 * Used to store raw resources at celestial bodies, refined resources at
 * processing facilities, and modules at shipyards. Each storage has a
 * maximum capacity per resource type.
 * </p>
 *
 * @param <T> the resource type stored (enum)
 */
public class ResourceStorage<T extends Enum<T>> {

    private final Map<T, Integer> inventory;
    private final Map<T, Integer> capacity;

    /**
     * Creates a new resource storage with the given capacity per resource type.
     *
     * @param resourceClass the enum class of the resource type
     * @param defaultCapacity the default capacity for each resource type
     */
    public ResourceStorage(Class<T> resourceClass, int defaultCapacity) {
        this.inventory = new EnumMap<>(resourceClass);
        this.capacity = new EnumMap<>(resourceClass);
        for (T type : resourceClass.getEnumConstants()) {
            inventory.put(type, 0);
            capacity.put(type, defaultCapacity);
        }
    }

    /**
     * Creates a new resource storage with custom capacity per resource type.
     *
     * @param resourceClass the enum class of the resource type
     * @param capacities map of resource types to their capacities
     */
    @SafeVarargs
    public ResourceStorage(Class<T> resourceClass, Map.Entry<T, Integer>... capacities) {
        this.inventory = new EnumMap<>(resourceClass);
        this.capacity = new EnumMap<>(resourceClass);
        for (T type : resourceClass.getEnumConstants()) {
            inventory.put(type, 0);
            capacity.put(type, 0);
        }
        for (Map.Entry<T, Integer> entry : capacities) {
            capacity.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns the current amount of the given resource type.
     *
     * @param type the resource type
     * @return the current amount (0 if not present)
     */
    public int getAmount(T type) {
        return inventory.getOrDefault(type, 0);
    }

    /**
     * Returns the maximum capacity for the given resource type.
     *
     * @param type the resource type
     * @return the maximum capacity
     */
    public int getCapacity(T type) {
        return capacity.getOrDefault(type, 0);
    }

    /**
     * Returns the remaining free space for the given resource type.
     *
     * @param type the resource type
     * @return the remaining capacity
     */
    public int getRemainingCapacity(T type) {
        return getCapacity(type) - getAmount(type);
    }

    /**
     * Adds resources to the storage, up to capacity.
     *
     * @param type the resource type
     * @param amount the amount to add
     * @return the actual amount added (may be less than requested if capacity is reached)
     */
    public int add(T type, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int remaining = getRemainingCapacity(type);
        int actual = Math.min(amount, remaining);
        inventory.merge(type, actual, Integer::sum);
        return actual;
    }

    /**
     * Removes resources from the storage.
     *
     * @param type the resource type
     * @param amount the amount to remove
     * @return the actual amount removed (may be less than requested if insufficient resources)
     */
    public int remove(T type, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int available = getAmount(type);
        int actual = Math.min(amount, available);
        inventory.put(type, available - actual);
        return actual;
    }

    /**
     * Checks if the storage has enough of the given resource type.
     *
     * @param type the resource type
     * @param amount the required amount
     * @return true if the storage has at least the required amount
     */
    public boolean hasEnough(T type, int amount) {
        return getAmount(type) >= amount;
    }

    /**
     * Returns a copy of the current inventory.
     *
     * @return unmodifiable map of resource types to amounts
     */
    public Map<T, Integer> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

    /**
     * Returns a copy of the capacity configuration.
     *
     * @return unmodifiable map of resource types to capacities
     */
    public Map<T, Integer> getCapacities() {
        return Collections.unmodifiableMap(capacity);
    }

    /**
     * Sets the capacity for a specific resource type.
     *
     * @param type the resource type
     * @param newCapacity the new capacity
     */
    public void setCapacity(T type, int newCapacity) {
        if (newCapacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative: " + newCapacity);
        }
        capacity.put(type, newCapacity);
        // Trim inventory if it exceeds new capacity
        int current = getAmount(type);
        if (current > newCapacity) {
            inventory.put(type, newCapacity);
        }
    }

    /**
     * Returns the total amount of all resources in the storage.
     */
    public int getTotalAmount() {
        return inventory.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Returns the total capacity of all resource types.
     */
    public int getTotalCapacity() {
        return capacity.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public String toString() {
        return "ResourceStorage{inventory=" + inventory + ", capacity=" + capacity + "}";
    }
}
