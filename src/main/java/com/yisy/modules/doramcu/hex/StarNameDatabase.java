package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database of IAU official star names, organized by {@link Star.StarType}.
 * <p>
 * Loads names from {@code /data/star-names.json} on the classpath.
 * Names are categorized by spectral type: RED, YELLOW, BLUE, PURPLE.
 */
public class StarNameDatabase {

    private static final String RESOURCE_PATH = "/data/star-names.json";

    private final Map<Star.StarType, List<String>> namesByType;
    private final Map<Star.StarType, Integer> usedIndices;
    private final Random random;
    private final String catalogPrefix;

    /**
     * Creates a new database with IAU official star names and default catalog prefix "HIP".
     */
    public StarNameDatabase() {
        this("HIP");
    }

    /**
     * Creates a new database with IAU official star names and custom catalog prefix.
     *
     * @param catalogPrefix the prefix for procedural names (e.g., "HIP", "HD", "SD")
     */
    public StarNameDatabase(String catalogPrefix) {
        this.random = new Random();
        this.catalogPrefix = catalogPrefix != null ? catalogPrefix : "HIP";
        this.namesByType = new ConcurrentHashMap<>();
        this.usedIndices = new ConcurrentHashMap<>();

        loadNames();
    }

    private void loadNames() {
        Map<String, List<String>> data = NameDatabaseLoader.loadCategorized(RESOURCE_PATH);
        for (Star.StarType type : Star.StarType.values()) {
            List<String> names = data.get(type.name());
            if (names != null) {
                namesByType.put(type, Collections.unmodifiableList(new ArrayList<>(names)));
            } else {
                namesByType.put(type, Collections.emptyList());
            }
            usedIndices.put(type, 0);
        }
    }

    // ---- Name Retrieval ----

    /**
     * Returns the next sequential name for the given star type.
     * Cycles through the list, then falls back to procedural names.
     *
     * @param starType the star type classification
     * @return a star name
     */
    public String getName(Star.StarType starType) {
        List<String> names = namesByType.get(starType);
        if (names == null || names.isEmpty()) {
            return getProceduralName(usedIndices.merge(starType, 1, Integer::sum));
        }

        int index = usedIndices.getOrDefault(starType, 0);
        if (index < names.size()) {
            usedIndices.put(starType, index + 1);
            return names.get(index);
        }

        return getProceduralName(usedIndices.merge(starType, 1, Integer::sum));
    }

    /**
     * Returns a random name for the given star type.
     *
     * @param starType the star type classification
     * @return a random star name
     */
    public String getRandomName(Star.StarType starType) {
        List<String> names = namesByType.get(starType);
        if (names == null || names.isEmpty()) {
            return getProceduralName(random.nextInt(100000));
        }
        return names.get(random.nextInt(names.size()));
    }

    /**
     * Returns a procedurally generated catalog-style name.
     * Format: "{prefix}-{number}" (e.g., "HIP-12345").
     *
     * @param number the sequential number
     * @return a procedural star name
     */
    public String getProceduralName(int number) {
        return catalogPrefix + "-" + String.format("%05d", number);
    }

    /**
     * Returns all names for the given star type.
     *
     * @param starType the star type classification
     * @return unmodifiable list of names
     */
    public List<String> getAllNames(Star.StarType starType) {
        List<String> names = namesByType.get(starType);
        return names != null ? Collections.unmodifiableList(names) : Collections.emptyList();
    }

    /**
     * Returns the number of names available for the given star type.
     *
     * @param starType the star type classification
     * @return the name count
     */
    public int getNameCount(Star.StarType starType) {
        List<String> names = namesByType.get(starType);
        return names != null ? names.size() : 0;
    }

    /**
     * Checks if all names for the given star type have been used.
     *
     * @param starType the star type classification
     * @return true if all names are exhausted
     */
    public boolean isExhausted(Star.StarType starType) {
        int used = usedIndices.getOrDefault(starType, 0);
        int total = getNameCount(starType);
        return used >= total;
    }

    /**
     * Returns the number of names used for the given star type.
     *
     * @param starType the star type classification
     * @return the number of names used
     */
    public int getUsedCount(Star.StarType starType) {
        return usedIndices.getOrDefault(starType, 0);
    }

    /**
     * Resets the used index for the given star type.
     *
     * @param starType the star type classification
     */
    public void reset(Star.StarType starType) {
        usedIndices.put(starType, 0);
    }

    /**
     * Resets all used indices.
     */
    public void resetAll() {
        for (Star.StarType type : Star.StarType.values()) {
            usedIndices.put(type, 0);
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
     * Returns the total number of names across all star types.
     *
     * @return total name count
     */
    public int getTotalNameCount() {
        return namesByType.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    // ---- Star Integration ----

    /**
     * Assigns the next sequential name to the given star, based on its star type.
     *
     * @param star the star to name
     */
    public void assignName(Star star) {
        if (star != null) {
            star.setName(getName(star.getStarType()));
        }
    }

    /**
     * Assigns a random name to the given star, based on its star type.
     *
     * @param star the star to name
     */
    public void assignRandomName(Star star) {
        if (star != null) {
            star.setName(getRandomName(star.getStarType()));
        }
    }

    @Override
    public String toString() {
        return String.format("StarNameDatabase{prefix='%s', totalNames=%d}",
                catalogPrefix, getTotalNameCount());
    }
}
