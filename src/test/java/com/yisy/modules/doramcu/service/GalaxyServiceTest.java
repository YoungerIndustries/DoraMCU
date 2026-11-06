package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationProgress;
import com.yisy.modules.doramcu.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GalaxyService Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class GalaxyServiceTest {

    @Mock
    private GalaxyRepository galaxyRepository;
    @Mock
    private SolarSystemRepository solarSystemRepository;
    @Mock
    private StarRepository starRepository;
    @Mock
    private PlanetRepository planetRepository;
    @Mock
    private MoonRepository moonRepository;
    @Mock
    private AsteroidBeltRepository asteroidBeltRepository;
    @Mock
    private GasGiantRepository gasGiantRepository;
    @Mock
    private NebulaRepository nebulaRepository;
    @Mock
    private BlackHoleRepository blackHoleRepository;
    @Mock
    private CometRepository cometRepository;
    @Mock
    private SpaceStationRepository spaceStationRepository;
    @Mock
    private AsteroidRepository asteroidRepository;

    private GalaxyService galaxyService;

    @BeforeEach
    void setUp() {
        galaxyService = new GalaxyService(galaxyRepository, solarSystemRepository, starRepository,
                planetRepository, moonRepository, asteroidBeltRepository, gasGiantRepository,
                nebulaRepository, blackHoleRepository, cometRepository, spaceStationRepository, asteroidRepository);
    }

    // ---- hasGalaxies ----

    @Test
    @DisplayName("hasGalaxies - should return false when no galaxies exist")
    void hasGalaxies_shouldReturnFalseWhenEmpty() {
        when(galaxyRepository.count()).thenReturn(0L);

        assertFalse(galaxyService.hasGalaxies());
    }

    @Test
    @DisplayName("hasGalaxies - should return true when galaxies exist")
    void hasGalaxies_shouldReturnTrueWhenNotEmpty() {
        when(galaxyRepository.count()).thenReturn(3L);

        assertTrue(galaxyService.hasGalaxies());
    }

    // ---- getAllGalaxies ----

    @Test
    @DisplayName("getAllGalaxies - should return empty list when no galaxies")
    void getAllGalaxies_shouldReturnEmptyList() {
        when(galaxyRepository.findAll()).thenReturn(new ArrayList<>());

        List<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getAllGalaxies();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllGalaxies - should return all galaxies")
    void getAllGalaxies_shouldReturnAllGalaxies() {
        com.yisy.modules.doramcu.model.Galaxy galaxy1 = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy1.setId(1L);
        galaxy1.setName("Milky Way");
        com.yisy.modules.doramcu.model.Galaxy galaxy2 = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy2.setId(2L);
        galaxy2.setName("Andromeda");
        when(galaxyRepository.findAll()).thenReturn(List.of(galaxy1, galaxy2));

        List<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getAllGalaxies();

        assertEquals(2, result.size());
        assertEquals("Milky Way", result.get(0).getName());
        assertEquals("Andromeda", result.get(1).getName());
    }

    // ---- generateGalaxyInMemory ----

    @Test
    @DisplayName("generateGalaxyInMemory - should generate a galaxy with given parameters")
    void generateGalaxyInMemory_shouldGenerateGalaxy() {
        Galaxy result = galaxyService.generateGalaxyInMemory(
                "TestGalaxy", 10, 3,
                0.5, 0.3, 0.2,
                0.1, 0.05, 0.1,
                0.05, 42L
        );

        assertNotNull(result);
        assertEquals("TestGalaxy", result.getName());
    }

    @Test
    @DisplayName("generateGalaxyInMemory - should use seed for deterministic generation")
    void generateGalaxyInMemory_shouldUseSeed() {
        Galaxy result1 = galaxyService.generateGalaxyInMemory(
                "Galaxy", 5, 2,
                0.5, 0.3, 0.2, 0.1, 0.05, 0.1, 0.05, 123L
        );
        Galaxy result2 = galaxyService.generateGalaxyInMemory(
                "Galaxy", 5, 2,
                0.5, 0.3, 0.2, 0.1, 0.05, 0.1, 0.05, 123L
        );

        // Same seed should produce same number of sectors
        assertEquals(result1.getAllSectors().size(), result2.getAllSectors().size());
    }

    @Test
    @DisplayName("generateGalaxyInMemory - should fire progress callbacks when listener set")
    void generateGalaxyInMemory_shouldFireProgressCallbacks() {
        GalaxyGenerationProgress listener = mock(GalaxyGenerationProgress.class);
        galaxyService.setProgressListener(listener);

        galaxyService.generateGalaxyInMemory(
                "Galaxy", 5, 2,
                0.5, 0.3, 0.2, 0.1, 0.05, 0.1, 0.05, 42L
        );

        verify(listener, atLeastOnce()).onPrepareStep(anyString());
        verify(listener).onReady();
    }

    // ---- getGalaxyById ----

    @Test
    @DisplayName("getGalaxyById - should return galaxy when found")
    void getGalaxyById_shouldReturnGalaxy() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setId(1L);
        galaxy.setName("Milky Way");
        when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));

        Optional<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getGalaxyById(1L);

        assertTrue(result.isPresent());
        assertEquals("Milky Way", result.get().getName());
    }

    @Test
    @DisplayName("getGalaxyById - should return empty when not found")
    void getGalaxyById_shouldReturnEmpty() {
        when(galaxyRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getGalaxyById(999L);

        assertTrue(result.isEmpty());
    }

    // ---- deleteAllGalaxies ----

    @Test
    @DisplayName("deleteAllGalaxies - should call repository deleteAll")
    void deleteAllGalaxies_shouldCallRepository() {
        galaxyService.deleteAllGalaxies();

        verify(galaxyRepository).deleteAll();
    }

    // ---- GalaxyGenerationResult ----

    @Test
    @DisplayName("GalaxyGenerationResult - should store both galaxy types")
    void galaxyGenerationResult_shouldStoreBothTypes() {
        com.yisy.modules.doramcu.model.Galaxy dbGalaxy = new com.yisy.modules.doramcu.model.Galaxy();
        dbGalaxy.setName("DB Galaxy");
        Galaxy hexGalaxy = new Galaxy(1, "Hex Galaxy");

        GalaxyService.GalaxyGenerationResult result =
                new GalaxyService.GalaxyGenerationResult(dbGalaxy, hexGalaxy);

        assertEquals("DB Galaxy", result.getDatabaseGalaxy().getName());
        assertEquals("Hex Galaxy", result.getInMemoryGalaxy().getName());
    }

    // ---- setProgressListener ----

    @Test
    @DisplayName("setProgressListener - should set and clear listener")
    void setProgressListener_shouldSetListener() {
        GalaxyGenerationProgress listener = mock(GalaxyGenerationProgress.class);
        galaxyService.setProgressListener(listener);

        // Generate should trigger the listener
        galaxyService.generateGalaxyInMemory(
                "Galaxy", 5, 2,
                0.5, 0.3, 0.2, 0.1, 0.05, 0.1, 0.05, 42L
        );

        verify(listener, atLeastOnce()).onPrepareStep(anyString());

        // Clear listener
        galaxyService.setProgressListener(null);
    }
}
