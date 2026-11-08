package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.traveller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer for survival tracking.
 * <p>
 * Manages character survival states including food, water, temperature,
 * shelter, and disease. Integrates with {@link CharacterService} to
 * look up characters by ID.
 * </p>
 */
@Service
public class SurvivalService {

    private static final Logger log = LoggerFactory.getLogger(SurvivalService.class);

    private final CharacterService characterService;
    private final Map<Integer, Survival> survivalMap = new ConcurrentHashMap<>();

    /**
     * Creates the service with the character service for character lookup.
     */
    public SurvivalService(CharacterService characterService) {
        this.characterService = Objects.requireNonNull(characterService,
                "CharacterService must not be null");
    }

    /**
     * Gets or creates a survival tracker for a character.
     */
    public Survival getSurvival(int characterId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }

        return survivalMap.computeIfAbsent(characterId, id -> {
            Survival surv = new Survival(character);
            log.info("Created survival tracker for {}", character.getName());
            return surv;
        });
    }

    /**
     * Advances the survival state by one day.
     */
    public String advanceDay(int characterId) {
        Survival surv = getSurvival(characterId);
        String events = surv.advanceDay();
        log.info("Advanced day for {}: {}", surv.getCharacter().getName(),
                surv.isAlive() ? "alive" : "DEAD");
        return events;
    }

    /**
     * Advances the survival state by multiple days.
     */
    public String advanceDays(int characterId, int days) {
        Survival surv = getSurvival(characterId);
        StringBuilder allEvents = new StringBuilder();

        for (int i = 0; i < days && surv.isAlive(); i++) {
            allEvents.append(surv.advanceDay()).append("\n");
        }

        if (!surv.isAlive()) {
            log.warn("{} died during survival simulation", surv.getCharacter().getName());
        } else {
            log.info("Advanced {} days for {}", days, surv.getCharacter().getName());
        }

        return allEvents.toString();
    }

    /**
     * Adds food rations to a character.
     */
    public void addFood(int characterId, int rations) {
        Survival surv = getSurvival(characterId);
        surv.addFood(rations);
        log.info("Added {} rations to {}", rations, surv.getCharacter().getName());
    }

    /**
     * Adds water to a character.
     */
    public void addWater(int characterId, int liters) {
        Survival surv = getSurvival(characterId);
        surv.addWater(liters);
        log.info("Added {} liters of water to {}", liters, surv.getCharacter().getName());
    }

    /**
     * Sets the temperature for a character.
     */
    public void setTemperature(int characterId, String temperature) {
        Survival surv = getSurvival(characterId);
        try {
            Survival.Temperature temp = Survival.Temperature.valueOf(temperature.toUpperCase());
            surv.setTemperature(temp);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown temperature: " + temperature);
        }
    }

    /**
     * Sets the temperature from Celsius.
     */
    public void setTemperatureCelsius(int characterId, int celsius) {
        Survival surv = getSurvival(characterId);
        surv.setTemperatureCelsius(celsius);
        log.info("Set temperature for {} to {}°C", surv.getCharacter().getName(), celsius);
    }

    /**
     * Sets the shelter for a character.
     */
    public void setShelter(int characterId, String shelter) {
        Survival surv = getSurvival(characterId);
        try {
            Survival.ShelterType sh = Survival.ShelterType.valueOf(shelter.toUpperCase());
            surv.setShelter(sh);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown shelter: " + shelter);
        }
    }

    /**
     * Adds a disease to a character.
     */
    public void addDisease(int characterId, String disease) {
        Survival surv = getSurvival(characterId);
        try {
            Survival.Disease dis = Survival.Disease.valueOf(disease.toUpperCase());
            surv.addDisease(dis);
            log.info("Added {} to {}", dis.getName(), surv.getCharacter().getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown disease: " + disease);
        }
    }

    /**
     * Returns a summary of a character's survival state.
     */
    public String getSummary(int characterId) {
        Survival surv = getSurvival(characterId);
        return surv.getSummary();
    }

    /**
     * Lists all survival trackers.
     */
    public Set<Integer> listTrackedCharacters() {
        return new HashSet<>(survivalMap.keySet());
    }

    /**
     * Simulates a survival scenario for a character.
     * <p>
     * Places the character in harsh conditions and advances days.
     * </p>
     */
    public String simulateSurvival(int characterId, int days,
                                    Survival.Temperature temperature,
                                    Survival.ShelterType shelter,
                                    int initialFood, int initialWater) {
        Survival surv = getSurvival(characterId);
        surv.setTemperature(temperature);
        surv.setShelter(shelter);

        // Reset supplies
        surv.addFood(initialFood);
        surv.addWater(initialWater);

        log.info("Survival simulation for {}: {} days, {}, {}, {} food, {} water",
                surv.getCharacter().getName(), days,
                temperature.getName(), shelter.getName(),
                initialFood, initialWater);

        return advanceDays(characterId, days);
    }
}
