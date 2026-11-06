package com.yisy.modules.doramcu.hex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads name data from JSON files on the classpath.
 * <p>
 * Supports two JSON formats:
 * <ul>
 *   <li><b>Categorized</b> — {"catalogPrefix": "...", "categories": {"CAT1": [...], "CAT2": [...]}}</li>
 *   <li><b>Flat</b> — {"proceduralPrefix": "...", "names": [...]}</li>
 * </ul>
 * <p>
 * Used by {@link PlanetNameDatabase}, {@link StarNameDatabase}, {@link ShipNameDatabase},
 * {@link FleetNameDatabase}, and {@link GalaxyNameDatabase} to load names from external
 * data files instead of hardcoded Java arrays.
 */
public class NameDatabaseLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Loads a categorized name database JSON file.
     * Expected format: {"catalogPrefix": "...", "categories": {"CAT1": [...], "CAT2": [...]}}
     *
     * @param resourcePath the classpath resource path (e.g., "/data/planet-names.json")
     * @return a map from category name to list of names, or empty map on error
     */
    public static Map<String, List<String>> loadCategorized(String resourcePath) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        try (InputStream is = NameDatabaseLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return result;
            }
            JsonNode root = MAPPER.readTree(is);
            JsonNode categories = root.get("categories");
            if (categories == null || !categories.isObject()) {
                return result;
            }
            categories.fields().forEachRemaining(entry -> {
                List<String> names = new ArrayList<>();
                entry.getValue().forEach(el -> names.add(el.asText()));
                result.put(entry.getKey(), names);
            });
        } catch (IOException e) {
            // Return empty map; caller should handle missing data
        }
        return result;
    }

    /**
     * Loads a flat name list JSON file.
     * Expected format: {"proceduralPrefix": "...", "names": [...]}
     *
     * @param resourcePath the classpath resource path (e.g., "/data/fleet-names.json")
     * @return a list of names, or empty list on error
     */
    public static List<String> loadFlat(String resourcePath) {
        List<String> result = new ArrayList<>();
        try (InputStream is = NameDatabaseLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return result;
            }
            JsonNode root = MAPPER.readTree(is);
            JsonNode names = root.get("names");
            if (names == null || !names.isArray()) {
                return result;
            }
            names.forEach(el -> result.add(el.asText()));
        } catch (IOException e) {
            // Return empty list; caller should handle missing data
        }
        return result;
    }

    /**
     * Reads the catalog/procedural prefix from a JSON file.
     *
     * @param resourcePath the classpath resource path
     * @param defaultValue the default prefix if not found
     * @return the prefix string from the JSON file, or defaultValue
     */
    public static String loadPrefix(String resourcePath, String defaultValue) {
        try (InputStream is = NameDatabaseLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return defaultValue;
            }
            JsonNode root = MAPPER.readTree(is);
            JsonNode prefix = root.get("catalogPrefix");
            if (prefix != null) {
                return prefix.asText(defaultValue);
            }
            prefix = root.get("proceduralPrefix");
            if (prefix != null) {
                return prefix.asText(defaultValue);
            }
        } catch (IOException e) {
            // Fall through to default
        }
        return defaultValue;
    }
}
