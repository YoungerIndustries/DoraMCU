package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides fleet names for the 4X game.
 * <p>
 * Loads names from {@code /data/fleet-names.json} on the classpath.
 * Falls back to procedural catalog-style names when named fleets are exhausted.
 */
public class FleetNameDatabase {

    private static final String RESOURCE_PATH = "/data/fleet-names.json";

    private final List<String> names;
    private final Map<String, Boolean> usedNames;
    private final Random random;
    private final String proceduralPrefix;
    private int nextProceduralId;
    private int nextSequentialIndex;

    /**
     * Creates a new fleet name database with default names.
     */
    public FleetNameDatabase() {
        this.usedNames = new ConcurrentHashMap<>();
        this.random = new Random();
        this.proceduralPrefix = "FLT";
        this.nextProceduralId = 1;
        this.nextSequentialIndex = 0;

        this.names = loadNames();
    }

    /**
     * Creates a new fleet name database with a custom procedural prefix.
     *
     * @param proceduralPrefix the prefix for procedurally generated names
     */
    public FleetNameDatabase(String proceduralPrefix) {
        this.usedNames = new ConcurrentHashMap<>();
        this.random = new Random();
        this.proceduralPrefix = proceduralPrefix != null ? proceduralPrefix : "FLT";
        this.nextProceduralId = 1;
        this.nextSequentialIndex = 0;

        this.names = loadNames();
    }

    private List<String> loadNames() {
        List<String> loaded = NameDatabaseLoader.loadFlat(RESOURCE_PATH);
        // Remove duplicates while preserving order
        Set<String> seen = new LinkedHashSet<>();
        List<String> unique = new ArrayList<>();
        for (String name : loaded) {
            if (seen.add(name)) {
                unique.add(name);
            }
        }
        return unique;
    }

    /**
     * Returns the next sequential name from the database.
     * <p>
     * After exhausting all named fleets, falls back to procedural naming.
     * </p>
     *
     * @return the next available name
     */
    public String getNextName() {
        // Try to find an unused name
        while (nextSequentialIndex < names.size()) {
            String name = names.get(nextSequentialIndex++);
            if (!usedNames.containsKey(name)) {
                usedNames.put(name, true);
                return name;
            }
        }

        // Fall back to procedural naming
        return generateProceduralName();
    }

    /**
     * Returns a random name from the database.
     * <p>
     * Selects from unused names first; when exhausted, falls back to procedural naming.
     * </p>
     *
     * @return a random available name
     */
    public String getRandomName() {
        // Find unused names
        List<String> available = new ArrayList<>();
        for (String name : names) {
            if (!usedNames.containsKey(name)) {
                available.add(name);
            }
        }

        if (!available.isEmpty()) {
            String name = available.get(random.nextInt(available.size()));
            usedNames.put(name, true);
            return name;
        }

        // Fall back to procedural naming
        return generateProceduralName();
    }

    /**
     * Assigns the next sequential name to the given fleet.
     *
     * @param fleet the fleet to name
     */
    public void assignName(Fleet fleet) {
        if (fleet == null) {
            return;
        }
        fleet.setName(getNextName());
    }

    /**
     * Assigns a random name to the given fleet.
     *
     * @param fleet the fleet to name
     */
    public void assignRandomName(Fleet fleet) {
        if (fleet == null) {
            return;
        }
        fleet.setName(getRandomName());
    }

    /**
     * Generates a procedural name using the configured prefix.
     *
     * @return a procedurally generated name
     */
    private String generateProceduralName() {
        String name = String.format("%s-%05d", proceduralPrefix, nextProceduralId++);
        return name;
    }

    // ---- Query methods ----

    /**
     * Returns the total number of named fleets in the database.
     *
     * @return the number of named fleets
     */
    public int getNameCount() {
        return names.size();
    }

    /**
     * Returns the number of used names.
     *
     * @return the number of used names
     */
    public int getUsedCount() {
        return usedNames.size();
    }

    /**
     * Returns whether all named fleets have been used.
     *
     * @return true if all names are exhausted
     */
    public boolean isExhausted() {
        return getUsedCount() >= getNameCount();
    }

    /**
     * Returns an unmodifiable list of all names in the database.
     *
     * @return unmodifiable list of names
     */
    public List<String> getAllNames() {
        return Collections.unmodifiableList(names);
    }

    /**
     * Resets the name database, allowing names to be reused.
     */
    public void reset() {
        usedNames.clear();
        nextSequentialIndex = 0;
        nextProceduralId = 1;
    }

    /**
     * Returns the procedural prefix used for generated names.
     *
     * @return the procedural prefix
     */
    public String getProceduralPrefix() {
        return proceduralPrefix;
    }

    @Override
    public String toString() {
        return String.format("FleetNameDatabase{total=%d, used=%d, prefix='%s'}",
                getNameCount(), getUsedCount(), proceduralPrefix);
    }
}
