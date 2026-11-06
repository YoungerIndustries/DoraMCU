package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database of planet names for 4X space games, organized by category.
 * <p>
 * Loads names from {@code /data/planet-names.json} on the classpath.
 * Provides names from various cultural and historical sources:
 * Roman/Greek/Norse mythology, historical figures, celestial bodies, and cultural names.
 * Falls back to procedural catalog-style designations when named planets are exhausted.
 */
public class PlanetNameDatabase {

    private static final String RESOURCE_PATH = "/data/planet-names.json";

    private final Map<String, List<String>> namesByCategory;
    private final Map<String, Integer> usedIndices;
    private final Random random;
    private final String catalogPrefix;
    private final List<String> categoryOrder;

    /**
     * Creates a new database with default catalog prefix "PDX".
     */
    public PlanetNameDatabase() {
        this("PDX");
    }

    /**
     * Creates a new database with custom catalog prefix.
     *
     * @param catalogPrefix the prefix for procedural names (e.g., "PDX", "EXO", "PLT")
     */
    public PlanetNameDatabase(String catalogPrefix) {
        this.random = new Random();
        this.catalogPrefix = catalogPrefix != null ? catalogPrefix : "PDX";
        this.namesByCategory = new ConcurrentHashMap<>();
        this.usedIndices = new ConcurrentHashMap<>();
        this.categoryOrder = new ArrayList<>();

        loadNames();
    }

    private void loadNames() {
        Map<String, List<String>> data = NameDatabaseLoader.loadCategorized(RESOURCE_PATH);
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            namesByCategory.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
            usedIndices.put(entry.getKey(), 0);
            categoryOrder.add(entry.getKey());
        }
    }

    // ---- Name Retrieval ----

    /**
     * Returns the next sequential name, cycling through all categories.
     *
     * @return a planet name
     */
    public String getName() {
        for (String category : categoryOrder) {
            List<String> names = namesByCategory.get(category);
            if (names == null || names.isEmpty()) {
                continue;
            }

            int index = usedIndices.getOrDefault(category, 0);
            if (index < names.size()) {
                usedIndices.put(category, index + 1);
                return names.get(index);
            }
        }

        // All categories exhausted, fall back to procedural names
        int totalUsed = getTotalUsedCount();
        return getProceduralName(totalUsed);
    }

    /**
     * Returns a random name from any category.
     *
     * @return a random planet name
     */
    public String getRandomName() {
        List<String> allNames = getAllNames();
        if (allNames.isEmpty()) {
            return getProceduralName(random.nextInt(100000));
        }
        return allNames.get(random.nextInt(allNames.size()));
    }

    /**
     * Returns the next sequential name from a specific category.
     *
     * @param category the category name (e.g., "Roman Mythology", "Greek Mythology")
     * @return a planet name from the specified category
     */
    public String getName(String category) {
        List<String> names = namesByCategory.get(category);
        if (names == null || names.isEmpty()) {
            return getProceduralName(usedIndices.merge(category, 1, Integer::sum));
        }

        int index = usedIndices.getOrDefault(category, 0);
        if (index < names.size()) {
            usedIndices.put(category, index + 1);
            return names.get(index);
        }

        return getProceduralName(usedIndices.merge(category, 1, Integer::sum));
    }

    /**
     * Returns a random name from a specific category.
     *
     * @param category the category name
     * @return a random planet name from the specified category
     */
    public String getRandomName(String category) {
        List<String> names = namesByCategory.get(category);
        if (names == null || names.isEmpty()) {
            return getProceduralName(random.nextInt(100000));
        }
        return names.get(random.nextInt(names.size()));
    }

    /**
     * Returns a procedurally generated catalog-style name.
     * Format: "{prefix}-{number}" (e.g., "PDX-00123").
     *
     * @param number the sequential number
     * @return a procedural planet name
     */
    public String getProceduralName(int number) {
        return catalogPrefix + "-" + String.format("%05d", number);
    }

    /**
     * Returns all names from all categories.
     *
     * @return unmodifiable list of all names
     */
    public List<String> getAllNames() {
        List<String> all = new ArrayList<>();
        for (List<String> names : namesByCategory.values()) {
            all.addAll(names);
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Returns all names for a specific category.
     *
     * @param category the category name
     * @return unmodifiable list of names
     */
    public List<String> getAllNames(String category) {
        List<String> names = namesByCategory.get(category);
        return names != null ? Collections.unmodifiableList(names) : Collections.emptyList();
    }

    /**
     * Returns the number of names available for a specific category.
     *
     * @param category the category name
     * @return the name count
     */
    public int getNameCount(String category) {
        List<String> names = namesByCategory.get(category);
        return names != null ? names.size() : 0;
    }

    /**
     * Returns the total number of names across all categories.
     *
     * @return total name count
     */
    public int getTotalNameCount() {
        return namesByCategory.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Returns the number of names used for a specific category.
     *
     * @param category the category name
     * @return the number of names used
     */
    public int getUsedCount(String category) {
        return usedIndices.getOrDefault(category, 0);
    }

    /**
     * Returns the total number of names used across all categories.
     *
     * @return total used count
     */
    public int getTotalUsedCount() {
        return usedIndices.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Checks if all names for a specific category have been used.
     *
     * @param category the category name
     * @return true if all names are exhausted
     */
    public boolean isExhausted(String category) {
        int used = usedIndices.getOrDefault(category, 0);
        int total = getNameCount(category);
        return used >= total;
    }

    /**
     * Checks if all names across all categories have been used.
     *
     * @return true if all names are exhausted
     */
    public boolean isExhausted() {
        for (String category : categoryOrder) {
            if (!isExhausted(category)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resets the used index for a specific category.
     *
     * @param category the category name
     */
    public void reset(String category) {
        usedIndices.put(category, 0);
    }

    /**
     * Resets all used indices.
     */
    public void resetAll() {
        for (String category : categoryOrder) {
            usedIndices.put(category, 0);
        }
    }

    /**
     * Returns the catalog prefix used for procedural names.
     *
     * @return the catalog prefix
     */
    public String getCatalogPrefix() {
        return catalogPrefix;
    }

    /**
     * Returns the list of available categories.
     *
     * @return unmodifiable list of category names
     */
    public List<String> getCategories() {
        return Collections.unmodifiableList(categoryOrder);
    }

    /**
     * Assigns a name to the given planet using the next available name.
     *
     * @param planet the planet to name
     */
    public void assignName(Planet planet) {
        if (planet != null) {
            planet.setName(getName());
        }
    }

    /**
     * Assigns a random name to the given planet.
     *
     * @param planet the planet to name
     */
    public void assignRandomName(Planet planet) {
        if (planet != null) {
            planet.setName(getRandomName());
        }
    }

    @Override
    public String toString() {
        return String.format("PlanetNameDatabase{prefix='%s', totalNames=%d, categories=%d}",
                catalogPrefix, getTotalNameCount(), categoryOrder.size());
    }
}
