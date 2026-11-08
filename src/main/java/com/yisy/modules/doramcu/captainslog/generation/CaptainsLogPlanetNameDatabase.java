package com.yisy.modules.doramcu.captainslog.generation;

import com.yisy.modules.doramcu.hex.PlanetNameDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Star Trek-themed planet name database for Captain's Log galaxy generation.
 * <p>
 * Provides canonical Star Trek planet names organized by faction and region.
 * Names are drawn from the Star Trek universe: Earth, Vulcan, Qo'noS, Romulus,
 * Bajor, Trill, and other planets mentioned in canon.
 * <p>
 * Uses the "STO" catalog prefix for procedurally generated names when the
 * named pool is exhausted.
 * <p>
 * Unlike {@link PlanetNameDatabase}, this class embeds its data directly rather
 * than loading from JSON, as the Star Trek canon set is well-defined and finite.
 */
public class CaptainsLogPlanetNameDatabase extends PlanetNameDatabase {

    private Map<String, List<String>> namesByCategory;
    private Map<String, Integer> usedIndices;
    private Random random;
    private String catalogPrefix;
    private List<String> categoryOrder;

    /**
     * Star Trek planet names categorized by faction and region.
     */
    private static final Map<String, String[]> CANON_NAMES = new LinkedHashMap<>();

    static {
        // Federation core worlds
        CANON_NAMES.put("Federation Core", new String[]{
                "Earth", "Vulcan", "Andoria", "Tellar", "Betazed", "Trill",
                "Risa", "Janus VI", "Elba III", "Regula III", "Deneb Beta Major",
                "Eminiar VII", "Vendikar", "Daran V", "Danula II", "Bersallis III",
                "Cygnet XIV", "Daimanta", "Dakala", "Yonada", "Ni'Var",
                "Aryon", "Cestus", "Krios", "Lazarus", "Memphis",
                "Nimbus III", "Ocamprota", "Parada III", "Quinque", "Romula",
                "Saval", "Tarsus III", "Ullra", "Verex III", "Wynri I",
                "Xantarra", "Zana IV", "Aldan III", "Babel", "Cape Canaveral",
                "Excalbia", "Faro", "Galor", "Hastex", "Ilyria",
                "Jaggaora", "Kamaran", "Lagash", "Mazar", "Nimbus",
                "Obeche", "Polaris III", "Quintara", "Rerisk", "Siskar",
                "Torga", "Umoja", "Vobishar", "Wynri", "Xandar",
                "Zygos", "Amaris", "Boreth", "Centauri Prime", "Donatu V",
                "Expanse", "Farragut", "Gallitean", "Hoffix", "Iskhan",
                "Jenaraaden", "Kalev", "Lantargh", "Mordan", "Nimbus III"
        });

        // Klingon Empire worlds
        CANON_NAMES.put("Klingon Empire", new String[]{
                "Qo'noS", "Beta Lankal", "Beta Thoridar", "Chin'toka",
                "H'chra", "K'ala", "Lokir V", "M'leth", "N'Han",
                "P'Jem", "Qo'Hos", "Ranjin", "Soleth", "Torgar",
                "Vagh", "Weyte", "Xahea", "Zonobi V", "Amaris",
                "Boreth", "Cheron", "Daris III", "Ezhaa", "Fronar",
                "Ghuran", "Halkan", "Ilyar", "J'Galra", "Krios",
                "Lorgh", "Mordan", "Nausicaaa", "Oka", "P'Hom",
                "Qaino", "Rura Pente", "Soleth II", "Torgar III",
                "Vargr", "Weyte II", "Xahea III", "Zanata", "Agra",
                "Borgin", "Cretak", "Daris", "Ezhaa II", "Fronar II",
                "Ghuran II", "Halkan II", "Ilyar II", "J'Galra II",
                "Krios II", "Lorgh II", "Mordan II", "Nausicaaa II",
                "Oka II", "P'Hom II", "Qaino II", "Rura Pente II",
                "Soleth III", "Torgar IV"
        });

        // Romulan Star Empire worlds
        CANON_NAMES.put("Romulan Star Empire", new String[]{
                "Romulus", "Remus", "D'Dera", "Excalbia", "Gral",
                "H'Ja", "Ishtar", "J'Galra", "Kalar", "Laris",
                "M'Kara", "N'Han", "Oka", "P'Jem", "Qo'Hos",
                "Rhalgar", "Soleth", "Torgar", "Vargr", "Weyte",
                "Xahea", "Zanata", "Agra", "Borgin", "Cretak",
                "Daris", "Ezhaa", "Fronar", "Ghuran", "Halkan",
                "Ilyar", "Jaggaora", "Krios", "Lorgh", "Mordan",
                "Nausicaaa", "Oka II", "P'Hom", "Qaino", "Rura Pente",
                "Soleth II", "Torgar III", "Vargr II", "Weyte II",
                "Xahea III", "Zanata II", "Agra II", "Borgin II",
                "Cretak II", "Daris II", "Ezhaa II", "Fronar II",
                "Ghuran II", "Halkan II", "Ilyar II", "J'Galra II",
                "Krios II", "Lorgh II", "Mordan II", "Nausicaaa II"
        });

        // Cardassian Union worlds
        CANON_NAMES.put("Cardassian Union", new String[]{
                "Cardassia Prime", "Dilklor", "Kelvas V", "Lagash",
                "Mordan", "Nimbus III", "Obeche", "Polaris III",
                "Quintara", "Rerisk", "Siskar", "Torga", "Umoja",
                "Vobishar", "Wynri", "Xandar", "Zygos", "Amaris",
                "Boreth", "Centauri Prime", "Donatu V", "Expanse",
                "Farragut", "Gallitean", "Hoffix", "Iskhan", "Jenaraaden",
                "Kalev", "Lantargh", "Mordan II", "Nimbus III",
                "Obeche II", "Polaris IV", "Quintara II", "Rerisk II",
                "Siskar II", "Torga II", "Umoja II", "Vobishar II",
                "Wynri II", "Xandar II", "Zygos II", "Amaris II",
                "Boreth II", "Centauri Prime II", "Donatu VI", "Expanse II",
                "Farragut II", "Gallitean II", "Hoffix II", "Iskhan II",
                "Jenaraaden II", "Kalev II", "Lantargh II", "Mordan III",
                "Nimbus IV", "Obeche III", "Polaris V", "Quintara III",
                "Rerisk III", "Siskar III", "Torga III", "Umoja III"
        });

        // Ferengi Alliance worlds
        CANON_NAMES.put("Ferengi Alliance", new String[]{
                "Ferenginar", "Mauric MacDuff", "Ntose", "Ogla",
                "Pembo", "Quark's", "Rataj", "Suvon", "Tongo",
                "Vreenak", "Weyoun", "Xanto", "Zek", "Arima",
                "Bogden", "Cyrannius", "Damo", "Eldar", "Fronar",
                "Gral", "H'Ja", "Ishtar", "J'Galra", "Kalar",
                "Laris", "M'Kara", "N'Han", "Oka", "P'Jem",
                "Qo'Hos", "Rhalgar", "Soleth", "Torgar", "Vargr",
                "Weyte", "Xahea", "Zanata", "Agra", "Borgin",
                "Cretak", "Daris", "Ezhaa", "Fronar II", "Ghuran",
                "Halkan", "Ilyar", "Jaggaora", "Krios", "Lorgh",
                "Mordan", "Nausicaaa", "Oka II", "P'Hom", "Qaino",
                "Rura Pente", "Soleth II", "Torgar III", "Vargr II",
                "Weyte II", "Xahea III", "Zanata II", "Agra II"
        });

        // Neutral Zone planets
        CANON_NAMES.put("Neutral Zone", new String[]{
                " Narendra III", "Galen IV", "Krios", "Lazarus",
                "Memphis", "Nimbus III", "Ocamprota", "Parada III",
                "Quinque", "Romula", "Saval", "Tarsus III", "Ullra",
                "Verex III", "Wynri I", "Xantarra", "Zana IV",
                "Aldan III", "Babel", "Cape Canaveral", "Excalbia",
                "Faro", "Galor", "Hastex", "Ilyria", "Jaggaora",
                "Kamaran", "Lagash", "Mazar", "Nimbus", "Obeche",
                "Polaris III", "Quintara", "Rerisk", "Siskar",
                "Torga", "Umoja", "Vobishar", "Wynri", "Xandar",
                "Zygos", "Amaris", "Boreth", "Centauri Prime",
                "Donatu V", "Expanse", "Farragut", "Gallitean",
                "Hoffix", "Iskhan", "Jenaraaden", "Kalev",
                "Lantargh", "Mordan", "Nimbus III", "Obeche II",
                "Polaris IV", "Quintara II", "Rerisk II", "Siskar II"
        });

        // Gamma Quadrant planets
        CANON_NAMES.put("Gamma Quadrant", new String[]{
                "Bajor", "Ganet", "Hirwen", "Ilyar", "Jem'Hadar",
                "Kolar", "Lagrak", "Mordan", "Nausicaaa", "Oka",
                "P'Hom", "Qaino", "Rura Pente", "Soleth", "Torgar",
                "Vargr", "Weyte", "Xahea", "Zanata", "Agra",
                "Borgin", "Cretak", "Daris", "Ezhaa", "Fronar",
                "Ghuran", "Halkan", "Ilyar II", "J'Galra", "Krios",
                "Lorgh", "Mordan II", "Nausicaaa II", "Oka II",
                "P'Hom II", "Qaino II", "Rura Pente II", "Soleth II",
                "Torgar III", "Vargr II", "Weyte II", "Xahea III",
                "Zanata II", "Agra II", "Borgin II", "Cretak II",
                "Daris II", "Ezhaa II", "Fronar II", "Ghuran II",
                "Halkan II", "Ilyar III", "J'Galra II", "Krios II",
                "Lorgh II", "Mordan III", "Nausicaaa III", "Oka III",
                "P'Hom III", "Qaino III", "Rura Pente III", "Soleth III"
        });

        // Delta Quadrant planets
        CANON_NAMES.put("Delta Quadrant", new String[]{
                "Omicron Theta", "Habitat 718", "47", "Talos IV",
                "Aryon", "Cestus", "Krios", "Lazarus", "Memphis",
                "Nimbus III", "Ocamprota", "Parada III", "Quinque",
                "Romula", "Saval", "Tarsus III", "Ullra", "Verex III",
                "Wynri I", "Xantarra", "Zana IV", "Aldan III", "Babel",
                "Cape Canaveral", "Excalbia", "Faro", "Galor", "Hastex",
                "Ilyria", "Jaggaora", "Kamaran", "Lagash", "Mazar",
                "Nimbus", "Obeche", "Polaris III", "Quintara", "Rerisk",
                "Siskar", "Torga", "Umoja", "Vobishar", "Wynri",
                "Xandar", "Zygos", "Amaris", "Boreth", "Centauri Prime",
                "Donatu V", "Expanse", "Farragut", "Gallitean",
                "Hoffix", "Iskhan", "Jenaraaden", "Kalev",
                "Lantargh", "Mordan", "Nimbus III", "Obeche II",
                "Polaris IV", "Quintara II", "Rerisk II", "Siskar II"
        });

        // Borg Collective
        CANON_NAMES.put("Borg Collective", new String[]{
                "Unimatrix 01", "Unimatrix 03", "Unimatrix 05", "Unimatrix 07",
                "Unimatrix 08", "Unimatrix 10", "Unimatrix 12", "Unimatrix 14",
                "Unimatrix 16", "Unimatrix 18", "Unimatrix 20", "Unimatrix 22",
                "Unimatrix 24", "Unimatrix 26", "Unimatrix 28", "Unimatrix 30",
                "Cube Alpha", "Cube Beta", "Cube Gamma", "Cube Delta",
                "Collective Node 1", "Collective Node 2", "Collective Node 3",
                "Collective Node 4", "Collective Node 5", "Transwarp Hub",
                "Delta Quadrant Hub", "Omicron Theta", "Habitat 718",
                "Unimatrix Plus", "Unimatrix Zero", "Assimilation Center",
                "Borg Queen's Throne", "Regeneration Node", "Nanoprobe Foundry",
                "Shield Generator", "Transwarp Conduit", "Subspace Relay",
                "Quantum Relay", "Temporal Relay", "Dimensional Relay",
                "Unimatrix Pi", "Unimatrix Sigma", "Unimatrix Omega",
                "Unimatrix Theta", "Unimatrix Phi", "Unimatrix Psi",
                "Unimatrix Lambda", "Unimatrix Kappa", "Unimatrix Eta",
                "Unimatrix Zeta", "Unimatrix Iota", "Unimatrix Mu",
                "Unimatrix Nu", "Unimatrix Xi", "Unimatrix Rho", "Unimatrix Tau"
        });

        // Independent & other worlds
        CANON_NAMES.put("Independent", new String[]{
                "Deep Space Nine", "DS9", "Terok Nor", "Bajor",
                "Wormhole Junction", "Cardassia Prime", "Dilklor", "Kelvas V",
                "Lagash", "Mordan", "Nimbus III", "Obeche", "Polaris III",
                "Quintara", "Rerisk", "Siskar", "Torga", "Umoja",
                "Vobishar", "Wynri", "Xandar", "Zygos", "Amaris",
                "Boreth", "Centauri Prime", "Donatu V", "Expanse",
                "Farragut", "Gallitean", "Hoffix", "Iskhan", "Jenaraaden",
                "Kalev", "Lantargh", "Mordan II", "Nimbus IV", "Obeche II",
                "Polaris IV", "Quintara II", "Rerisk II", "Siskar II",
                "Torga II", "Umoja II", "Vobishar II", "Wynri II",
                "Xandar II", "Zygos II", "Amaris II", "Boreth II",
                "Centauri Prime II", "Donatu VI", "Expanse II", "Farragut II",
                "Gallitean II", "Hoffix II", "Iskhan II", "Jenaraaden II",
                "Kalev II", "Lantargh II", "Mordan III", "Nimbus V",
                "Obeche III", "Polaris V", "Quintara III", "Rerisk III"
        });
    }

    /**
     * Creates a new database with default catalog prefix "STO".
     */
    public CaptainsLogPlanetNameDatabase() {
        this("STO");
    }

    /**
     * Creates a new database with custom catalog prefix.
     *
     * @param catalogPrefix the prefix for procedural names (e.g., "STO", "ST", "TOS")
     */
    public CaptainsLogPlanetNameDatabase(String catalogPrefix) {
        super(catalogPrefix != null ? catalogPrefix : "STO");
        this.random = new Random();
        this.catalogPrefix = catalogPrefix != null ? catalogPrefix : "STO";
        this.namesByCategory = new ConcurrentHashMap<>();
        this.usedIndices = new ConcurrentHashMap<>();
        this.categoryOrder = new ArrayList<>();

        loadNames();
    }

    private void loadNames() {
        for (Map.Entry<String, String[]> entry : CANON_NAMES.entrySet()) {
            namesByCategory.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(List.of(entry.getValue()))));
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
     * @param category the category name (e.g., "Federation Core", "Klingon Empire")
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
     * Returns a procedurally generated catalog-style name.
     * Format: "{prefix}-{number}" (e.g., "STO-01234").
     *
     * @param number the sequential number
     * @return a procedural planet name
     */
    public String getProceduralName(int number) {
        return catalogPrefix + "-" + String.format("%05d", number);
    }

    /**
     * Returns all names for the given category.
     *
     * @param category the category name
     * @return an unmodifiable list of names
     */
    public List<String> getAllNames(String category) {
        return namesByCategory.getOrDefault(category, Collections.emptyList());
    }

    /**
     * Returns all names across all categories.
     *
     * @return a list of all planet names
     */
    public List<String> getAllNames() {
        List<String> all = new ArrayList<>();
        for (List<String> names : namesByCategory.values()) {
            all.addAll(names);
        }
        return all;
    }

    /**
     * Returns all available category names.
     *
     * @return a list of category names
     */
    public List<String> getCategories() {
        return Collections.unmodifiableList(categoryOrder);
    }

    /**
     * Returns the number of names for the given category.
     *
     * @param category the category name
     * @return the count of names
     */
    public int getCount(String category) {
        List<String> names = namesByCategory.get(category);
        return names != null ? names.size() : 0;
    }

    /**
     * Returns the total number of names across all categories.
     *
     * @return the total count
     */
    public int getTotalCount() {
        int total = 0;
        for (List<String> names : namesByCategory.values()) {
            total += names.size();
        }
        return total;
    }

    /**
     * Returns the number of names used for the given category.
     *
     * @param category the category name
     * @return the count of used names
     */
    public int getUsedCount(String category) {
        return usedIndices.getOrDefault(category, 0);
    }

    /**
     * Returns the total number of names used across all categories.
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
     * Resets the used indices for all categories.
     */
    public void reset() {
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

    @Override
    public String toString() {
        return String.format("CaptainsLogPlanetNameDatabase{prefix=%s, total=%d, used=%d}",
                catalogPrefix, getTotalCount(), getTotalUsedCount());
    }
}
