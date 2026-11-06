package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides ship names organized by {@link ShipType} for the 4X game.
 * <p>
 * Loads names from {@code /data/ship-names.json} on the classpath.
 * Names are organized by ship type: FRIGATE, DESTROYER, CRUISER, BATTLESHIP, CARRIER, MOTHERSHIP.
 * Falls back to procedural catalog-style names when named ships are exhausted.
 */
public class ShipNameDatabase {

    private static final String RESOURCE_PATH = "/data/ship-names.json";

    private final Map<ShipType, List<String>> namesByType;
    private final Map<ShipType, Integer> nextIndex;
    private final Map<ShipType, Set<String>> usedNames;
    private final Random random;
    private final String proceduralPrefix;
    private int nextProceduralId;

    /**
     * Creates a new ship name database with default names for all ship types.
     */
    public ShipNameDatabase() {
        this.namesByType = new EnumMap<>(ShipType.class);
        this.nextIndex = new EnumMap<>(ShipType.class);
        this.usedNames = new EnumMap<>(ShipType.class);
        this.random = new Random();
        this.proceduralPrefix = "DS";
        this.nextProceduralId = 1;

        loadNames();
    }

    /**
     * Creates a new ship name database with a custom procedural prefix.
     *
     * @param proceduralPrefix the prefix for procedurally generated names
     */
    public ShipNameDatabase(String proceduralPrefix) {
        this.namesByType = new EnumMap<>(ShipType.class);
        this.nextIndex = new EnumMap<>(ShipType.class);
        this.usedNames = new EnumMap<>(ShipType.class);
        this.random = new Random();
        this.proceduralPrefix = proceduralPrefix != null ? proceduralPrefix : "DS";
        this.nextProceduralId = 1;

        loadNames();
    }

    private void loadNames() {
        Map<String, List<String>> data = NameDatabaseLoader.loadCategorized(RESOURCE_PATH);
        for (ShipType type : ShipType.values()) {
            List<String> names = data.get(type.name());
            if (names != null) {
                namesByType.put(type, Collections.unmodifiableList(new ArrayList<>(names)));
            } else {
                namesByType.put(type, Collections.emptyList());
            }
            nextIndex.put(type, 0);
            usedNames.put(type, ConcurrentHashMap.newKeySet());
        }
    }

    // ---- Name retrieval ----

    /**
     * Returns the next sequential name for the given ship type.
     * <p>
     * When all named ships are exhausted, falls back to procedural naming
     * using the format {@code PREFIX-XXXXX} where XXXXX is a zero-padded 5-digit number.
     * </p>
     *
     * @param shipType the ship type to get a name for
     * @return the next available name
     */
    public String getNextName(ShipType shipType) {
        if (shipType == null) {
            throw new IllegalArgumentException("Ship type must not be null");
        }

        List<String> names = namesByType.get(shipType);
        int index = nextIndex.get(shipType);

        if (index < names.size()) {
            String name = names.get(index);
            nextIndex.put(shipType, index + 1);
            usedNames.get(shipType).add(name);
            return name;
        }

        // Fall back to procedural naming
        return generateProceduralName();
    }

    /**
     * Returns a random name for the given ship type.
     * <p>
     * Selects from unused names first; when exhausted, falls back to procedural naming.
     * </p>
     *
     * @param shipType the ship type to get a name for
     * @return a random available name
     */
    public String getRandomName(ShipType shipType) {
        if (shipType == null) {
            throw new IllegalArgumentException("Ship type must not be null");
        }

        List<String> names = namesByType.get(shipType);
        Set<String> used = usedNames.get(shipType);

        // Find unused names
        List<String> available = new ArrayList<>();
        for (String name : names) {
            if (!used.contains(name)) {
                available.add(name);
            }
        }

        if (!available.isEmpty()) {
            String name = available.get(random.nextInt(available.size()));
            used.add(name);
            return name;
        }

        // Fall back to procedural naming
        return generateProceduralName();
    }

    /**
     * Assigns the next sequential name to the given ship based on its type.
     *
     * @param ship the ship to name
     */
    public void assignName(Ship ship) {
        if (ship == null) {
            return;
        }
        ship.setName(getNextName(ship.getShipType()));
    }

    /**
     * Assigns a random name to the given ship based on its type.
     *
     * @param ship the ship to name
     */
    public void assignRandomName(Ship ship) {
        if (ship == null) {
            return;
        }
        ship.setName(getRandomName(ship.getShipType()));
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
     * Returns the number of named ships available for the given type.
     *
     * @param shipType the ship type to query
     * @return the number of named ships
     */
    public int getNameCount(ShipType shipType) {
        if (shipType == null) {
            throw new IllegalArgumentException("Ship type must not be null");
        }
        return namesByType.get(shipType).size();
    }

    /**
     * Returns the number of used names for the given ship type.
     *
     * @param shipType the ship type to query
     * @return the number of used names
     */
    public int getUsedCount(ShipType shipType) {
        if (shipType == null) {
            throw new IllegalArgumentException("Ship type must not be null");
        }
        return usedNames.get(shipType).size();
    }

    /**
     * Returns whether all named ships for the given type have been used.
     *
     * @param shipType the ship type to check
     * @return true if all names are exhausted
     */
    public boolean isExhausted(ShipType shipType) {
        if (shipType == null) {
            throw new IllegalArgumentException("Ship type must not be null");
        }
        return getUsedCount(shipType) >= getNameCount(shipType);
    }

    /**
     * Returns an unmodifiable list of all names for the given ship type.
     *
     * @param shipType the ship type to query
     * @return unmodifiable list of names
     */
    public List<String> getAllNames(ShipType shipType) {
        if (shipType == null) {
            throw new IllegalArgumentException("Ship type must not be null");
        }
        return namesByType.get(shipType);
    }

    /**
     * Resets the name database, allowing names to be reused.
     */
    public void reset() {
        for (ShipType type : ShipType.values()) {
            nextIndex.put(type, 0);
            usedNames.get(type).clear();
        }
        nextProceduralId = 1;
    }

    /**
     * Resets the name database for a specific ship type.
     *
     * @param shipType the ship type to reset
     */
    public void reset(ShipType shipType) {
        if (shipType == null) {
            throw new IllegalArgumentException("Ship type must not be null");
        }
        nextIndex.put(shipType, 0);
        usedNames.get(shipType).clear();
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
        StringBuilder sb = new StringBuilder();
        sb.append("ShipNameDatabase{");
        for (ShipType type : ShipType.values()) {
            if (sb.length() > 18) {
                sb.append(", ");
            }
            sb.append(type.name()).append("=").append(getNameCount(type));
        }
        sb.append("}");
        return sb.toString();
    }
}
