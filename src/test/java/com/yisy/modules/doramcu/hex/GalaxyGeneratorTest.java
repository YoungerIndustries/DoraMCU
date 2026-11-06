package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GalaxyGenerator Tests")
class GalaxyGeneratorTest {

    @Nested
    @DisplayName("System Distribution Across Sectors")
    class SystemDistribution {

        @Test
        @DisplayName("should distribute systems across all 100 sectors")
        void shouldDistributeSystemsAcrossAllSectors() {
            GalaxyGenerator generator = new GalaxyGenerator(42L);
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(500)
                    .build();
            generator.setConfig(config);

            Galaxy galaxy = generator.generateGalaxy(1, "Test Galaxy");

            // Count systems per sector
            Map<Integer, Integer> sectorSystemCounts = new HashMap<>();
            for (Galaxy.Sector sector : galaxy.getAllSectors()) {
                int count = sector.size();
                sectorSystemCounts.put(sector.getId(), count);
                if (count > 0) {
                    // Verify each sector has at least 1 system
                    assertTrue(count >= 1,
                            "Sector " + sector.getId() + " (" + sector.getName() + ") should have at least 1 system");
                }
            }

            // All 100 sectors should have systems
            long sectorsWithSystems = sectorSystemCounts.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .count();
            assertEquals(Galaxy.TOTAL_SECTORS, sectorsWithSystems,
                    "All 100 sectors should have at least 1 system");
        }

        @Test
        @DisplayName("should distribute systems evenly - no sector should have 0 systems")
        void shouldNotHaveEmptySectors() {
            GalaxyGenerator generator = new GalaxyGenerator(123L);
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(300)
                    .build();
            generator.setConfig(config);

            Galaxy galaxy = generator.generateGalaxy(1, "Even Distribution");

            for (Galaxy.Sector sector : galaxy.getAllSectors()) {
                assertTrue(sector.size() > 0,
                        "Sector " + sector.getId() + " at (" + sector.getRow() + "," + sector.getCol() +
                        ") should not be empty");
            }
        }

        @Test
        @DisplayName("should populate corner sectors, not just center")
        void shouldPopulateCornerSectors() {
            GalaxyGenerator generator = new GalaxyGenerator(42L);
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(500)
                    .build();
            generator.setConfig(config);

            Galaxy galaxy = generator.generateGalaxy(1, "Corner Test");

            // Check all 4 corners
            Galaxy.Sector topLeft = galaxy.getSector(0, 0);
            Galaxy.Sector topRight = galaxy.getSector(0, 9);
            Galaxy.Sector bottomLeft = galaxy.getSector(9, 0);
            Galaxy.Sector bottomRight = galaxy.getSector(9, 9);

            assertTrue(topLeft.size() > 0, "Top-left corner sector (0,0) should have systems");
            assertTrue(topRight.size() > 0, "Top-right corner sector (0,9) should have systems");
            assertTrue(bottomLeft.size() > 0, "Bottom-left corner sector (9,0) should have systems");
            assertTrue(bottomRight.size() > 0, "Bottom-right corner sector (9,9) should have systems");
        }

        @Test
        @DisplayName("should generate hyperlanes connecting distributed systems")
        void shouldGenerateHyperlanesForDistributedSystems() {
            GalaxyGenerator generator = new GalaxyGenerator(42L);
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(500)
                    .hyperlaneDensity(1.0)
                    .build();
            generator.setConfig(config);

            Galaxy galaxy = generator.generateGalaxy(1, "Hyperlane Test");

            assertTrue(galaxy.getHyperlaneCount() > 0,
                    "Galaxy should have hyperlanes connecting distributed systems");
            // With 500 systems, we should have at least a spanning tree (499 hyperlanes)
            assertTrue(galaxy.getHyperlaneCount() >= galaxy.size() - 1,
                    "Should have at least a spanning tree of hyperlanes");
        }

        @Test
        @DisplayName("should generate total systems close to configured count")
        void shouldGenerateCloseToConfiguredCount() {
            GalaxyGenerator generator = new GalaxyGenerator(42L);
            int configuredCount = 500;
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(configuredCount)
                    .build();
            generator.setConfig(config);

            Galaxy galaxy = generator.generateGalaxy(1, "Count Test");

            // Each sector gets configuredCount/100 systems, but each subsector generates 1-3 systems randomly.
            // So the actual count can vary: some sectors may generate slightly fewer or more.
            // With 100 sectors and ~5 systems/sector, we expect roughly 100-300 systems.
            int actualCount = galaxy.size();
            assertTrue(actualCount > configuredCount * 0.3,
                    "Actual system count (" + actualCount + ") should be at least 30% of configured (" + configuredCount + ")");
            assertTrue(actualCount <= configuredCount * 2,
                    "Actual system count (" + actualCount + ") should not exceed 2x configured (" + configuredCount + ")");
        }

        @Test
        @DisplayName("should use minimum 1 system per sector when count is less than 100")
        void shouldUseMinimumOneSystemPerSector() {
            GalaxyGenerator generator = new GalaxyGenerator(42L);
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(50)
                    .build();
            generator.setConfig(config);

            Galaxy galaxy = generator.generateGalaxy(1, "Small Count");

            // With 50 systems and 100 sectors, each sector gets max(1, 50/100) = 1 system
            for (Galaxy.Sector sector : galaxy.getAllSectors()) {
                assertTrue(sector.size() >= 1,
                        "Sector " + sector.getId() + " should have at least 1 system even with small count");
            }
        }
    }

    @Nested
    @DisplayName("Reproducibility")
    class Reproducibility {

        @Test
        @DisplayName("same seed should produce same distribution")
        void sameSeedShouldProduceSameDistribution() {
            long seed = 99999L;

            GalaxyGenerator gen1 = new GalaxyGenerator(seed);
            GalaxyGenerator gen2 = new GalaxyGenerator(seed);

            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(200)
                    .build();
            gen1.setConfig(config);
            gen2.setConfig(config);

            Galaxy galaxy1 = gen1.generateGalaxy(1, "Repro Test");
            Galaxy galaxy2 = gen2.generateGalaxy(1, "Repro Test");

            assertEquals(galaxy1.size(), galaxy2.size(),
                    "Same seed should produce same total system count");

            // Check per-sector distribution matches
            for (int i = 0; i < Galaxy.TOTAL_SECTORS; i++) {
                Galaxy.Sector s1 = galaxy1.getSector(i);
                Galaxy.Sector s2 = galaxy2.getSector(i);
                assertEquals(s1.size(), s2.size(),
                        "Sector " + i + " should have same system count with same seed");
            }
        }
    }
}
