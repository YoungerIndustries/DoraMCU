package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Generates random Traveller sectors using Classic Traveller world generation algorithms.
 * <p>
 * This class implements the star generation procedure from Classic Traveller,
 * creating sectors with random star systems, each with procedurally generated
 * UWP values, trade codes, and hyperlane connections.
 * </p>
 * <p>
 * The generation process follows these steps:
 * </p>
 * <ol>
 *   <li>Determine the number of stars in the sector</li>
 *   <li>For each star, generate random sector map coordinates</li>
 *   <li>Generate a random UWP for each star's primary world</li>
 *   <li>Derive trade codes from the UWP</li>
 *   <li>Create hyperlane connections between nearby stars</li>
 * </ol>
 * <p>
 * Usage example:
 * </p>
 * <pre>
 *   SectorGenerator generator = new SectorGenerator(42);
 *   TravellerSector sector = generator.generate("Spinward Marches", 1);
 *
 *   System.out.println(sector.getWorldCount() + " worlds generated");
 *   for (TravellerWorld world : sector.getAllWorlds()) {
 *       System.out.println("  " + world.getName() + ": " + world.getUwp().getUwpString());
 *   }
 * </pre>
 */
public class SectorGenerator {

    /**
     * Default number of stars per sector (2D6 roll range).
     */
    private static final int MIN_STARS = 2;
    private static final int MAX_STARS = 12;

    /**
     * Default maximum hyperlane distance (parsecs).
     */
    private static final int DEFAULT_MAX_HYPERLANE_DISTANCE = 3;

    /**
     * Probability that a world is Imperium-aligned (0-1).
     */
    private static final double IMPERIUM_ALIGNMENT_CHANCE = 0.6;

    /**
     * Random number generator for procedural generation.
     */
    private final Random random;

    /**
     * Maximum distance for hyperlane connections.
     */
    private int maxHyperlaneDistance;

    /**
     * Name database for generating world names.
     */
    private final WorldNameGenerator nameGenerator;

    /**
     * Creates a new sector generator with a random seed.
     */
    public SectorGenerator() {
        this(new Random());
    }

    /**
     * Creates a new sector generator with the given seed for reproducible generation.
     *
     * @param seed the random seed
     */
    public SectorGenerator(long seed) {
        this(new Random(seed));
    }

    /**
     * Creates a new sector generator with the given random instance.
     *
     * @param random the random number generator
     */
    public SectorGenerator(Random random) {
        this.random = random;
        this.maxHyperlaneDistance = DEFAULT_MAX_HYPERLANE_DISTANCE;
        this.nameGenerator = new WorldNameGenerator(random);
    }

    /**
     * Sets the maximum distance for hyperlane connections.
     *
     * @param distance the maximum hyperlane distance in parsecs
     */
    public void setMaxHyperlaneDistance(int distance) {
        this.maxHyperlaneDistance = distance;
    }

    /**
     * Generates a complete Traveller sector with random star systems.
     *
     * @param sectorName the name of the sector
     * @param sectorId the unique sector identifier
     * @return the generated sector
     */
    public TravellerSector generate(String sectorName, int sectorId) {
        return generate(sectorName, sectorId, TravellerSector.DEFAULT_SIZE, TravellerSector.DEFAULT_SIZE);
    }

    /**
     * Generates a complete Traveller sector with random star systems and custom size.
     *
     * @param sectorName the name of the sector
     * @param sectorId the unique sector identifier
     * @param width the sector map width
     * @param height the sector map height
     * @return the generated sector
     */
    public TravellerSector generate(String sectorName, int sectorId, int width, int height) {
        TravellerSector sector = new TravellerSector(sectorId, sectorName, width, height);

        // Step 1: Determine number of stars
        int numStars = DiceUtil.rollNdX(2, 6) + MIN_STARS - 2; // 2D6 range: 2-12

        // Step 2: Generate stars with random coordinates
        Set<String> coordinateSet = new HashSet<>();
        List<TravellerWorld> worlds = new ArrayList<>();
        int nextWorldId = 1;

        for (int i = 0; i < numStars; i++) {
            // Find unique coordinates
            int x, y;
            do {
                x = DiceUtil.rollIndex(width);
                y = DiceUtil.rollIndex(height);
            } while (coordinateSet.contains(x + "," + y));
            coordinateSet.add(x + "," + y);

            // Step 3: Generate random UWP
            UniversalWorldProfile uwp = generateRandomUwp();

            // Generate world name
            String name = nameGenerator.generateName();

            // Create world
            TravellerWorld world = new TravellerWorld(nextWorldId++, name, uwp, x, y);

            // Random Imperium alignment
            if (DiceUtil.rollPercentile() / 100.0 < IMPERIUM_ALIGNMENT_CHANCE) {
                world.setImperiumAligned(true);
            }

            worlds.add(world);
            sector.addWorld(world);
        }

        // Step 4: Set capital world (highest population or first habitable)
        TravellerWorld capital = worlds.stream()
                .filter(TravellerWorld::isHabitable)
                .max((w1, w2) -> Long.compare(w1.getPopulationMillions(), w2.getPopulationMillions()))
                .orElse(null);
        if (capital != null) {
            sector.setCapitalWorld(capital);
        }

        // Step 5: Create hyperlanes between nearby stars
        for (int i = 0; i < worlds.size(); i++) {
            for (int j = i + 1; j < worlds.size(); j++) {
                TravellerWorld w1 = worlds.get(i);
                TravellerWorld w2 = worlds.get(j);

                int distance = w1.getJumpDistanceTo(w2);
                if (distance <= maxHyperlaneDistance) {
                    sector.addHyperlane(w1, w2);
                }
            }
        }

        return sector;
    }

    /**
     * Generates a random Universal World Profile using Traveller dice rolls.
     * <p>
     * Each field is generated using the probability distributions from
     * Classic Traveller's world generation tables.
     * </p>
     *
     * @return a randomly generated UWP
     */
    public UniversalWorldProfile generateRandomUwp() {
        int planetSize = rollPlanetSize();
        int atmosphere = rollAtmosphere(planetSize);
        int gravity = rollGravity(planetSize, atmosphere);
        int starport = rollStarport();
        int population = rollPopulation(atmosphere, gravity);
        int government = rollGovernment(population);
        int lawLevel = rollLawLevel(government);

        // Build UWP string
        StringBuilder sb = new StringBuilder(7);
        sb.append(valueToHex(planetSize));
        sb.append(valueToHex(atmosphere));
        sb.append(valueToHex(gravity));
        sb.append(valueToHex(starport));
        sb.append(valueToHex(population));
        sb.append(valueToHex(government));
        sb.append(valueToHex(lawLevel));
        String uwpString = sb.toString();

        return new UniversalWorldProfile(
                planetSize, atmosphere, gravity,
                starport, population, government,
                lawLevel, null, uwpString
        );
    }

    /**
     * Rolls for planet size (0-A).
     * <p>
     * Distribution favors smaller worlds (3-5 range).
     * </p>
     */
    private int rollPlanetSize() {
        // 2D6-2, clamped to 0-10
        int roll = DiceUtil.rollNdX(2, 6) - 2;
        return Math.max(0, Math.min(10, roll));
    }

    /**
     * Rolls for atmosphere based on planet size.
     * <p>
     * Gas giants (7+) have thick atmospheres. Small worlds may have none.
     * </p>
     */
    private int rollAtmosphere(int planetSize) {
        if (planetSize >= 7) {
            // Gas giant: thick atmosphere (5-7)
            return 5 + DiceUtil.rollDSides(3);
        } else if (planetSize <= 1) {
            // Small world: likely no atmosphere
            if (DiceUtil.rollPercentile() / 100.0 < 0.5) {
                return 0;
            }
            return DiceUtil.rollDSides(4);
        } else {
            // Normal world: varied atmosphere
            return DiceUtil.rollDSides(8); // 1-8, mapped to 0-7 below
        }
    }

    /**
     * Rolls for gravity based on planet size and atmosphere.
     * <p>
     * UWP gravity = actual gravity + atmosphere modifier.
     * </p>
     */
    private int rollGravity(int planetSize, int atmosphere) {
        // Base gravity from planet size
        int baseGravity = Math.max(0, planetSize - 3); // Size 3 = 0G (low), Size 6 = 3G (high)
        baseGravity = Math.max(0, Math.min(6, baseGravity));

        // Atmosphere modifier
        int modifier = 0;
        if (atmosphere >= 1 && atmosphere <= 7) {
            modifier = 1; // Breathable atmosphere adds +1 to UWP gravity
        }

        int gravity = baseGravity + modifier;
        return Math.max(0, Math.min(10, gravity));
    }

    /**
     * Rolls for starport class (0-4, where 0 is best).
     */
    private int rollStarport() {
        // 1D6-1, clamped to 0-4
        int roll = DiceUtil.rollD6() - 1;
        return Math.max(0, Math.min(4, roll));
    }

    /**
     * Rolls for population based on atmosphere and gravity.
     * <p>
     * Habitable worlds have higher population. Non-habitable worlds have 0.
     * </p>
     */
    private int rollPopulation(int atmosphere, int gravity) {
        // Check if habitable
        boolean breathable = (atmosphere >= 1 && atmosphere <= 7) || atmosphere == 10;
        boolean tolerableGravity = gravity >= 1 && gravity <= 8;

        if (!breathable || !tolerableGravity) {
            return 0;
        }

        // Habitable world: 2D6-2, clamped to 1-9
        int roll = DiceUtil.rollNdX(2, 6) - 2;
        return Math.max(1, Math.min(9, roll));
    }

    /**
     * Rolls for government type based on population.
     * <p>
     * No population = no government. Higher population = more likely to have government.
     * </p>
     */
    private int rollGovernment(int population) {
        if (population == 0) {
            return 0;
        }

        // 1D6, clamped to 0-9
        int roll = DiceUtil.rollD6() - 1;
        return Math.min(9, roll);
    }

    /**
     * Rolls for law level based on government.
     * <p>
     * No government = no law. Higher government = higher law level.
     * </p>
     */
    private int rollLawLevel(int government) {
        if (government == 0) {
            return 0;
        }

        // 1D6, clamped to 0-9
        int roll = DiceUtil.rollD6() - 1;
        return Math.min(9, roll);
    }

    /**
     * Converts a numeric value (0-10) to its hex character representation.
     *
     * @param value the value (0-10)
     * @return the hex character (0-9, A)
     */
    private static char valueToHex(int value) {
        if (value >= 0 && value <= 9) {
            return (char) ('0' + value);
        } else if (value == 10) {
            return 'A';
        } else {
            throw new IllegalArgumentException("Value must be 0-10, got " + value);
        }
    }

    /**
     * Generates names for Traveller worlds using syllable combinations.
     */
    private static class WorldNameGenerator {

        private static final String[] SYLLABLES_START = {
                "Al", "An", "Ar", "As", "Ba", "Be", "Bi", "Ca", "Ce", "Ch",
                "Da", "De", "Di", "Dr", "Du", "El", "En", "Er", "Es", "Ex",
                "Fa", "Fe", "Fi", "Fl", "Fo", "Ga", "Ge", "Gi", "Go", "Gr",
                "Gu", "Ha", "He", "Hi", "Ho", "Il", "In", "Ir", "Is", "Ka",
                "Ke", "Ki", "Ko", "Kr", "Ku", "La", "Le", "Li", "Lo", "Lu",
                "Ma", "Me", "Mi", "Mo", "Mu", "Na", "Ne", "Ni", "No", "Nu",
                "Oc", "Od", "Of", "On", "Or", "Os", "Pa", "Pe", "Pi", "Po",
                "Pr", "Pu", "Ra", "Re", "Ri", "Ro", "Ru", "Sa", "Se", "Si",
                "So", "Su", "Ta", "Te", "Ti", "To", "Tr", "Tu", "Ul", "Un",
                "Ur", "Us", "Va", "Ve", "Vi", "Vo", "Vu", "Wa", "We", "Xi",
                "Ya", "Ye", "Yo", "Za", "Ze", "Zi", "Zo"
        };

        private static final String[] SYLLABLES_END = {
                "a", "al", "an", "ar", "as", "ax", "en", "er", "es", "ex",
                "ia", "ic", "id", "il", "in", "is", "it", "ix", "or", "os",
                "on", "or", "os", "ox", "ra", "re", "ri", "ro", "rus", "us",
                "va", "ve", "vi", "vo", "ya", "ye", "yn", "yr", "ys", "yx"
        };

        private final Random random;

        WorldNameGenerator(Random random) {
            this.random = random;
        }

        /**
         * Generates a random world name by combining syllables.
         *
         * @return a generated world name
         */
        String generateName() {
            int syllableCount = DiceUtil.rollDSides(2) + 2; // 2-3 syllables
            StringBuilder name = new StringBuilder();

            // First syllable (start)
            name.append(SYLLABLES_START[DiceUtil.rollIndex(SYLLABLES_START.length)]);

            // Middle syllables
            for (int i = 1; i < syllableCount - 1; i++) {
                name.append(SYLLABLES_END[DiceUtil.rollIndex(SYLLABLES_END.length)]);
                if (DiceUtil.rollD6() >= 4) {
                    name.append(SYLLABLES_START[DiceUtil.rollIndex(SYLLABLES_START.length)]);
                }
            }

            // Last syllable (end)
            name.append(SYLLABLES_END[DiceUtil.rollIndex(SYLLABLES_END.length)]);

            return name.toString();
        }
    }
}
