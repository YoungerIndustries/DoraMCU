package com.yisy.modules.doramcu.captainslog.generation;

import com.yisy.modules.doramcu.hex.Star;
import com.yisy.modules.doramcu.hex.StarNameDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Star Trek-themed star name database for Captain's Log galaxy generation.
 * <p>
 * Provides canonical Star Trek star names organized by {@link Star.StarType}.
 * Names are drawn from the Star Trek universe: Wolf 359, 40 Eridani, Beta Orionis,
 * 61 Cygni, Procyon, and other stars mentioned in canon.
 * <p>
 * Uses the "STO" catalog prefix for procedurally generated names when the
 * named pool is exhausted.
 * <p>
 * Unlike {@link StarNameDatabase}, this class embeds its data directly rather
 * than loading from JSON, as the Star Trek canon set is well-defined and finite.
 */
public class CaptainsLogStarNameDatabase extends StarNameDatabase {

    private Map<Star.StarType, List<String>> namesByType;
    private Map<Star.StarType, Integer> usedIndices;
    private Random random;
    private String catalogPrefix;

    /**
     * Star Trek star names categorized by spectral type.
     * <p>
     * RED — Red dwarfs, M-type stars (cool, dim, long-lived)
     * YELLOW — G-type stars like Sol (Sun-like)
     * BLUE — O/B-type stars (hot, massive, short-lived)
     * PURPLE — Exotic stars (neutron stars, pulsars, brown dwarfs)
     * </p>
     */
    private static final Map<Star.StarType, String[]> CANON_NAMES = new EnumMap<>(Star.StarType.class);

    static {
        // RED dwarfs — M-type stars
        CANON_NAMES.put(Star.StarType.RED, new String[]{
                "Barnard's Star", "Proxima Centauri", "Lalande 21185", "Sirius B",
                "Wolf 359", "Lacaille 9352", "Ross 154", "Ross 248",
                "EZ Aquarii", "UV Ceti", "GR Virginis", "YZ Cassiopeiae",
                "Luyten 726-8 A", "Luyten 726-8 B", "Procyon B", "40 Eridani B",
                "40 Eridani C", "61 Cygni B", "61 Cygni C", "Sigma Draconis",
                "Epsilon Eridani", "Ross 128", "Epsilon Indi A", "Epsilon Indi B",
                "Lacaille 9352", "DEN 1048-3956", "WISE 0855−0714",
                "Teegarden's Star", "WASP-12", "GJ 1214",
                "GJ 436", "GJ 570", "GJ 667 C", "GJ 832",
                "GJ 1002", "GJ 1151", "GJ 1214 b", "GJ 1460",
                "GJ 1525", "GJ 179", "GJ 273", "GJ 3323",
                "GJ 3470", "GJ 3929", "GJ 4129", "GJ 436 b",
                "GJ 504", "GJ 536", "GJ 625", "GJ 687",
                "GJ 876", "GJ 1061", "GJ 1115", "GJ 1151 b"
        });

        // YELLOW — G-type stars (Sun-like)
        CANON_NAMES.put(Star.StarType.YELLOW, new String[]{
                "Sol", "Alpha Centauri A", "Alpha Centauri B", "Tau Ceti",
                "Epsilon Eridani", "61 Cygni A", "Sigma Draconis", "Procyon",
                "Toliman", "Achernar", "Capella", "Castor",
                "Pollux", "Fomalhaut", "Hamal", "Mirach",
                "Deneb", "Altair", "Vega", "Arcturus",
                "Aldebaran", "Betelgeuse", "Rigel", "Sirius",
                "Spica", "Regulus", "Alnilam", "Mintaka",
                "Saiph", "Bellatrix", "Alcyone", "Electra",
                "Maia", "Merope", "Taygeta", "Celaeno",
                "Asterope", "Atlas", "Pleione", "36 Tauri",
                "Theta² Orionis", "Zeta Orionis", "Mu Orionis",
                "Pi³ Orionis", "Lambda Orionis", "Iota Orionis",
                "Nu Orionis", "Omicron Orionis", "Kappa Orionis",
                "Epsilon Orionis", "Gamma Orionis", "Delta Orionis",
                "Beta Orionis", "Alpha Orionis", "WC Orionis",
                "AI Orionis", "HD 38283", "HD 38291", "HD 38590"
        });

        // BLUE — O/B-type stars (hot, massive)
        CANON_NAMES.put(Star.StarType.BLUE, new String[]{
                "Rigel", "Spica", "Regulus", "Alnilam", "Mintaka",
                "Saiph", "Bellatrix", "Alcyone", "Electra", "Maia",
                "Merope", "Taygeta", "Celaeno", "Atlas", "Pleione",
                "36 Tauri", "Theta² Orionis A", "Theta² Orionis B",
                "Theta² Orionis C", "Theta² Orionis D", "Zeta Orionis",
                "Mu Orionis", "Pi³ Orionis", "Lambda Orionis",
                "Iota Orionis", "Nu Orionis", "Omicron Orionis",
                "Kappa Orionis", "Epsilon Orionis", "Gamma Orionis",
                "Delta Orionis", "Beta Orionis", "Alpha Orionis",
                "WC Orionis", "Acrux", "Becrux", "Gacrux",
                "Delta Crucis", "Theta Crucis", "Mimosa", "Atria",
                "Albireo A", "Albireo B", "Wezen", "Adhara",
                "Muliphein", "Sirrah", "Heka", "Phact",
                "Aludra", "Tajin", "Furud", "Hajara",
                "Meissa", "Naoth", "Waz", "Aludran",
                "Azha", "Sarin", "Tereus", "Algorab",
                "Peacock", "Grumium", "Kaus Australis", "Sceptrum"
        });

        // PURPLE — Exotic stars (neutron stars, pulsars, brown dwarfs)
        CANON_NAMES.put(Star.StarType.PURPLE, new String[]{
                "Vela Pulsar", "Crab Pulsar", "Gemini Pulsar",
                "PSR B1919+21", "PSR B0531+21", "PSR J0437-4715",
                "PSR B1257+12", "PSR J0737-3039", "PSR J1640-4631",
                "PSR B1509-58", "PSR J0205+6449", "PSR J1012+5307",
                "Xamidimura", "Wolf 359", "Teegarden's Star",
                "WISE 0855−0714", "DEN 1048-3956", "Luhman 16 A",
                "Luhman 16 B", "WISE 1049-5319", "WISE 1147-2000",
                "2MASS J09393547-2924171", "SIMP J013656.5+093347",
                "LHS 288", "Luhman 16", "WASP-12 b",
                "TrES-2b", "HD 189733 b", "WASP-12b",
                "KELT-9b", "Epsilon Indi Ab", "Ross 458 b",
                "Lacaille 9352 b", "GJ 1061 c", "GJ 1061 d",
                "GJ 1151 c", "GJ 1214 b", "GJ 436 b",
                "GJ 570 b", "GJ 667 Cb", "GJ 667 Cc", "GJ 667 Cd",
                "GJ 832 b", "GJ 1002 b", "GJ 1151 b",
                "GJ 1460 b", "GJ 1525 b", "GJ 179 b",
                "GJ 273 b", "GJ 3323 b", "GJ 3470 b",
                "GJ 3929 b", "GJ 4129 b", "GJ 504 b",
                "GJ 536 b", "GJ 625 b", "GJ 687 b",
                "GJ 876 b", "GJ 876 c", "GJ 876 d", "GJ 876 e"
        });
    }

    /**
     * Creates a new database with Star Trek star names and default catalog prefix "STO".
     */
    public CaptainsLogStarNameDatabase() {
        this("STO");
    }

    /**
     * Creates a new database with Star Trek star names and custom catalog prefix.
     *
     * @param catalogPrefix the prefix for procedural names (e.g., "STO", "ST", "TOS")
     */
    public CaptainsLogStarNameDatabase(String catalogPrefix) {
        super(catalogPrefix != null ? catalogPrefix : "STO");
        this.random = new Random();
        this.catalogPrefix = catalogPrefix != null ? catalogPrefix : "STO";
        this.namesByType = new ConcurrentHashMap<>();
        this.usedIndices = new ConcurrentHashMap<>();

        loadNames();
    }

    private void loadNames() {
        for (Star.StarType type : Star.StarType.values()) {
            String[] names = CANON_NAMES.get(type);
            if (names != null) {
                namesByType.put(type, Collections.unmodifiableList(new ArrayList<>(List.of(names))));
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
     * Format: "{prefix}-{number}" (e.g., "STO-01234").
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
     * @return an unmodifiable list of names
     */
    public List<String> getAllNames(Star.StarType starType) {
        return namesByType.getOrDefault(starType, Collections.emptyList());
    }

    /**
     * Returns all names across all star types.
     *
     * @return a list of all star names
     */
    public List<String> getAllNames() {
        List<String> all = new ArrayList<>();
        for (List<String> names : namesByType.values()) {
            all.addAll(names);
        }
        return all;
    }

    /**
     * Returns the number of names for the given star type.
     *
     * @param starType the star type classification
     * @return the count of names
     */
    public int getCount(Star.StarType starType) {
        List<String> names = namesByType.get(starType);
        return names != null ? names.size() : 0;
    }

    /**
     * Returns the total number of names across all types.
     *
     * @return the total count
     */
    public int getTotalCount() {
        int total = 0;
        for (List<String> names : namesByType.values()) {
            total += names.size();
        }
        return total;
    }

    /**
     * Returns the number of names used for the given star type.
     *
     * @param starType the star type classification
     * @return the count of used names
     */
    public int getUsedCount(Star.StarType starType) {
        return usedIndices.getOrDefault(starType, 0);
    }

    /**
     * Returns the total number of names used across all types.
     *
     * @return the total used count
     */
    public int getTotalUsedCount() {
        int total = 0;
        for (int count : usedIndices.values()) {
            total += count;
        }
        return total;
    }

    /**
     * Resets the used indices for all star types.
     */
    public void reset() {
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

    @Override
    public String toString() {
        return String.format("CaptainsLogStarNameDatabase{prefix=%s, total=%d, used=%d}",
                catalogPrefix, getTotalCount(), getTotalUsedCount());
    }
}
