package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database of galaxy names for 4X space games.
 * <p>
 * Loads names from {@code /data/galaxy-names.json} on the classpath.
 * Falls back to procedural catalog-style designations when named galaxies are exhausted.
 */
public class GalaxyNameDatabase {

    private static final String RESOURCE_PATH = "/data/galaxy-names.json";

    private final List<String> names;
    private final ConcurrentHashMap<String, Boolean> usedNames;
    private final Random random;
    private String catalogPrefix;
    private int proceduralId;

    /**
     * Creates a new database with default catalog prefix "NGC".
     */
    public GalaxyNameDatabase() {
        this("NGC");
    }

    /**
     * Creates a new database with custom catalog prefix.
     *
     * @param catalogPrefix the prefix for procedural names (e.g., "NGC", "IC", "GAL")
     */
    public GalaxyNameDatabase(String catalogPrefix) {
        this.random = new Random();
        this.catalogPrefix = catalogPrefix != null ? catalogPrefix : "NGC";
        this.usedNames = new ConcurrentHashMap<>();
        this.proceduralId = 1;

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

    // ---- Core Methods ----

    /**
     * Returns the next sequential galaxy name.
     * <p>
     * Once all named galaxies are exhausted, procedurally generated names are returned.
     *
     * @return the next available galaxy name
     */
    public String getNextName() {
        for (String name : names) {
            if (usedNames.putIfAbsent(name, Boolean.TRUE) == null) {
                return name;
            }
        }
        return getProceduralName();
    }

    /**
     * Returns a random unused galaxy name.
     * <p>
     * Once all named galaxies are exhausted, procedurally generated names are returned.
     *
     * @return a random available galaxy name
     */
    public String getRandomName() {
        List<String> available = new ArrayList<>();
        for (String name : names) {
            if (!usedNames.containsKey(name)) {
                available.add(name);
            }
        }
        if (available.isEmpty()) {
            return getProceduralName();
        }
        String selected = available.get(random.nextInt(available.size()));
        usedNames.put(selected, Boolean.TRUE);
        return selected;
    }

    /**
     * Returns a procedurally generated catalog-style name.
     * Format: "{prefix}-{number}" (e.g., "NGC-00001").
     *
     * @return a procedural galaxy name
     */
    public String getProceduralName() {
        String name = catalogPrefix + "-" + String.format("%05d", proceduralId++);
        return name;
    }

    /**
     * Returns all galaxy names.
     *
     * @return unmodifiable list of all names
     */
    public List<String> getAllNames() {
        return Collections.unmodifiableList(names);
    }

    /**
     * Returns the total number of named galaxies.
     *
     * @return the name count
     */
    public int getNameCount() {
        return names.size();
    }

    /**
     * Returns the number of names used so far.
     *
     * @return the number of names used
     */
    public int getUsedCount() {
        return usedNames.size();
    }

    /**
     * Checks if all named galaxies have been used.
     *
     * @return true if all names are exhausted
     */
    public boolean isExhausted() {
        return usedNames.size() >= names.size();
    }

    /**
     * Resets the used names, allowing names to be reused.
     */
    public void reset() {
        usedNames.clear();
        proceduralId = 1;
    }

    /**
     * Returns the catalog prefix used for procedural names.
     *
     * @return the catalog prefix
     */
    public String getProceduralPrefix() {
        return catalogPrefix;
    }

    /**
     * Sets the catalog prefix for procedural names.
     *
     * @param prefix the new prefix
     */
    public void setProceduralPrefix(String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            this.catalogPrefix = prefix;
        }
    }

    /**
     * Assigns the next sequential name to the given galaxy.
     *
     * @param galaxy the galaxy to name
     */
    public void assignName(Galaxy galaxy) {
        if (galaxy != null) {
            // Galaxy doesn't have a name field yet, but we provide this for future use
        }
    }

    /**
     * Assigns a random name to the given galaxy.
     *
     * @param galaxy the galaxy to name
     */
    public void assignRandomName(Galaxy galaxy) {
        if (galaxy != null) {
            // Galaxy doesn't have a name field yet, but we provide this for future use
        }
    }

    @Override
    public String toString() {
        return String.format("GalaxyNameDatabase{total=%d, used=%d, prefix='%s'}",
                names.size(), usedNames.size(), catalogPrefix);
    }
}
