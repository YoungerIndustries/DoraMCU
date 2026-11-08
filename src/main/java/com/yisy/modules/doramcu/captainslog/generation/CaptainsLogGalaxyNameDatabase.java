package com.yisy.modules.doramcu.captainslog.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Star Trek-themed galaxy name database for Captain's Log galaxy generation.
 * <p>
 * Provides canonical Star Trek galaxy names and procedurally generated
 * designations. Names are drawn from the Star Trek universe: Milky Way,
 * Barzan, Romulan Galaxy, and other galaxies mentioned in canon.
 * <p>
 * Uses the "NGC" catalog prefix for procedurally generated names when the
 * named pool is exhausted (kept as "NGC" since it is an astronomical convention).
 * <p>
 * Unlike {@link GalaxyNameDatabase}, this class embeds its data directly rather
 * than loading from JSON, as the Star Trek canon set is well-defined and finite.
 */
public class CaptainsLogGalaxyNameDatabase {

    /**
     * Star Trek and astronomical galaxy names.
     * Includes canonical Star Trek galaxies and real galaxies referenced in canon.
     */
    private static final String[] CANON_NAMES = {
            // Canonical Star Trek galaxies
            "Milky Way", "Barzan", "Romulan Galaxy", "Andromeda",
            "Triangulum", "Magellanic Cloud", "Sombrero Galaxy",
            "Whirlpool Galaxy", "Pinwheel Galaxy", "Black Eye Galaxy",
            "Sombrero", "Centaurus A", "Messier 87", "Messier 31",
            "Messier 33", "Messier 81", "Messier 82", "Messier 101",
            "Messier 104", "Messier 51", "Messier 63", "Messier 77",
            "Messier 96", "Messier 105", "Messier 109", "Messier 110",
            // Star Trek referenced galaxies
            "Galaxy 314", "Galaxy 47", "Galaxy 718", "Galaxy 876",
            "Galaxy 1061", "Galaxy 1214", "Galaxy 1460", "Galaxy 1525",
            "Galaxy 179", "Galaxy 273", "Galaxy 3323", "Galaxy 3470",
            "Galaxy 3929", "Galaxy 4129", "Galaxy 504", "Galaxy 536",
            "Galaxy 625", "Galaxy 687", "Galaxy 832", "Galaxy 1002",
            // Astronomical galaxy names used in Star Trek
            "NGC 4412", "NGC 4438", "NGC 4449", "NGC 4461",
            "NGC 4536", "NGC 4559", "NGC 4565", "NGC 4567",
            "NGC 4568", "NGC 4594", "NGC 4631", "NGC 4656",
            "NGC 4657", "NGC 4676", "NGC 4699", "NGC 4725",
            "NGC 4753", "NGC 4755", "NGC 4762", "NGC 4763",
            "NGC 4765", "NGC 4773", "NGC 4789", "NGC 4826",
            "NGC 4845", "NGC 4866", "NGC 4881", "NGC 4889",
            "NGC 4893", "NGC 4900", "NGC 4911", "NGC 4921",
            // Quadrant-themed galaxy names
            "Alpha Quadrant Galaxy", "Beta Quadrant Galaxy",
            "Gamma Quadrant Galaxy", "Delta Quadrant Galaxy",
            "Federation Galaxy", "Klingon Galaxy", "Romulan Galaxy",
            "Cardassian Galaxy", "Ferengi Galaxy", "Dominion Galaxy",
            "Borg Galaxy", "Founders Galaxy", "Jem'Hadar Galaxy",
            "Vorta Galaxy", "Hirogen Galaxy", "Obsidian Order Galaxy",
            // Constellation-themed galaxy names
            "Cygnus Galaxy", "Orion Galaxy", "Lyra Galaxy",
            "Andromeda Galaxy", "Cassiopeia Galaxy", "Draco Galaxy",
            "Phoenix Galaxy", "Aquila Galaxy", "Pegasus Galaxy",
            "Perseus Galaxy", "Hercules Galaxy", "Sagittarius Galaxy",
            "Centaurus Galaxy", "Gemini Galaxy", "Leo Galaxy",
            "Scorpius Galaxy", "Ophiuchus Galaxy", "Corona Galaxy",
            "Taurus Galaxy", "Ursa Galaxy", "Boötes Galaxy",
            // Procedural-style names for variety
            "Sector 001 Galaxy", "Sector 007 Galaxy", "Sector 23 Galaxy",
            "Sector 30 Galaxy", "Sector 31 Galaxy", "Sector 2520 Galaxy",
            "Sector 21503 Galaxy", "Warp Core Galaxy", "Red Matter Galaxy",
            "Subspace Galaxy", "Transwarp Galaxy", "Quantum Galaxy",
            "Temporal Galaxy", "Dimensional Galaxy", "Nexus Galaxy",
            "CareTaker Galaxy", "Ocampa Galaxy", "Naaru Galaxy",
            "Karendi Galaxy", "Aurelar Galaxy", "Malcor Galaxy",
            "Socath Galaxy", "Tkon Galaxy", "Vaskan Galaxy",
            "Yridian Galaxy", "Zhat Vash Galaxy"
    };

    private final List<String> names;
    private final ConcurrentHashMap<String, Boolean> usedNames;
    private final Random random;
    private String catalogPrefix;
    private int proceduralId;

    /**
     * Creates a new database with default catalog prefix "NGC".
     */
    public CaptainsLogGalaxyNameDatabase() {
        this("NGC");
    }

    /**
     * Creates a new database with custom catalog prefix.
     *
     * @param catalogPrefix the prefix for procedural names (e.g., "NGC", "IC", "GAL")
     */
    public CaptainsLogGalaxyNameDatabase(String catalogPrefix) {
        this.random = new Random();
        this.catalogPrefix = catalogPrefix != null ? catalogPrefix : "NGC";
        this.usedNames = new ConcurrentHashMap<>();
        this.proceduralId = 1;

        this.names = loadNames();
    }

    private List<String> loadNames() {
        // Remove duplicates while preserving order
        Set<String> seen = new LinkedHashSet<>();
        List<String> unique = new ArrayList<>();
        for (String name : CANON_NAMES) {
            if (seen.add(name)) {
                unique.add(name);
            }
        }
        return Collections.unmodifiableList(unique);
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
     * Checks if a galaxy name has been used.
     *
     * @param name the galaxy name to check
     * @return true if the name has been used
     */
    public boolean isUsed(String name) {
        return usedNames.containsKey(name);
    }

    /**
     * Marks a galaxy name as used.
     *
     * @param name the galaxy name to mark as used
     */
    public void markUsed(String name) {
        usedNames.put(name, Boolean.TRUE);
    }

    /**
     * Unmarks a galaxy name, making it available again.
     *
     * @param name the galaxy name to unmark
     */
    public void unmarkUsed(String name) {
        usedNames.remove(name);
    }

    /**
     * Returns all available (unused) galaxy names.
     *
     * @return a list of unused galaxy names
     */
    public List<String> getAvailableNames() {
        List<String> available = new ArrayList<>();
        for (String name : names) {
            if (!usedNames.containsKey(name)) {
                available.add(name);
            }
        }
        return available;
    }

    /**
     * Returns all galaxy names (both used and unused).
     *
     * @return a list of all galaxy names
     */
    public List<String> getAllNames() {
        return Collections.unmodifiableList(names);
    }

    /**
     * Returns the number of available (unused) galaxy names.
     *
     * @return the count of available names
     */
    public int getAvailableCount() {
        int count = 0;
        for (String name : names) {
            if (!usedNames.containsKey(name)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the total number of galaxy names in the database.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return names.size();
    }

    /**
     * Returns the number of used galaxy names.
     *
     * @return the count of used names
     */
    public int getUsedCount() {
        return usedNames.size();
    }

    /**
     * Resets all used names, making all names available again.
     */
    public void reset() {
        usedNames.clear();
        proceduralId = 1;
    }

    /**
     * Sets the catalog prefix for procedural names.
     *
     * @param catalogPrefix the new catalog prefix
     */
    public void setCatalogPrefix(String catalogPrefix) {
        this.catalogPrefix = catalogPrefix != null ? catalogPrefix : "NGC";
    }

    /**
     * Returns the catalog prefix used for procedural names.
     *
     * @return the catalog prefix
     */
    public String getCatalogPrefix() {
        return catalogPrefix;
    }

    @Override
    public String toString() {
        return String.format("CaptainsLogGalaxyNameDatabase{prefix=%s, total=%d, used=%d, available=%d}",
                catalogPrefix, getTotalCount(), getUsedCount(), getAvailableCount());
    }
}
