package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.traveller.*;
import com.yisy.modules.doramcu.service.SurvivalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for survival tracking.
 * <p>
 * Provides endpoints for managing character survival states,
 * advancing days, and simulating survival scenarios.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/survival}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/survival")
public class SurvivalController {

    private static final Logger log = LoggerFactory.getLogger(SurvivalController.class);

    private final SurvivalService survivalService;

    public SurvivalController(SurvivalService survivalService) {
        this.survivalService = survivalService;
    }

    /**
     * Gets the current survival state for a character.
     */
    @GetMapping("/{characterId}")
    public ResponseEntity<Map<String, Object>> getSurvival(@PathVariable int characterId) {
        log.debug("GET /api/v1/traveller/survival/{}", characterId);

        try {
            Survival surv = survivalService.getSurvival(characterId);

            Map<String, Object> result = new HashMap<>();
            result.put("characterId", characterId);
            result.put("characterName", surv.getCharacter().getName());
            result.put("healthPoints", surv.getHealthPoints());
            result.put("maxHealthPoints", surv.getMaxHealthPoints());
            result.put("foodRations", surv.getFoodRations());
            result.put("waterLiters", surv.getWaterLiters());
            result.put("daysSinceLastMeal", surv.getDaysSinceLastMeal());
            result.put("daysSinceLastDrink", surv.getDaysSinceLastDrink());
            result.put("temperature", surv.getCurrentTemperature().getName());
            result.put("shelter", surv.getCurrentShelter().getName());
            result.put("diseasePenalty", surv.getDiseasePenalty());
            result.put("isAlive", surv.isAlive());

            // Diseases
            List<Map<String, Object>> diseases = new ArrayList<>();
            for (Survival.DiseaseInstance disease : surv.getDiseases()) {
                Map<String, Object> d = new HashMap<>();
                d.put("name", disease.getDisease().getName());
                d.put("daysRemaining", disease.getDaysRemaining());
                d.put("daysTotal", disease.getDaysTotal());
                d.put("penalty", disease.getDisease().getPenalty());
                diseases.add(d);
            }
            result.put("diseases", diseases);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Advances the survival state by one day.
     */
    @PostMapping("/{characterId}/advance")
    public ResponseEntity<Map<String, String>> advanceDay(@PathVariable int characterId) {
        log.debug("POST /api/v1/traveller/survival/{}/advance", characterId);

        try {
            String events = survivalService.advanceDay(characterId);
            Map<String, String> result = new HashMap<>();
            result.put("events", events);
            result.put("summary", survivalService.getSummary(characterId));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Advances the survival state by multiple days.
     */
    @PostMapping("/{characterId}/advance-days")
    public ResponseEntity<Map<String, String>> advanceDays(
            @PathVariable int characterId,
            @RequestParam(defaultValue = "1") int days) {
        log.debug("POST /api/v1/traveller/survival/{}/advance-days — {} days", characterId, days);

        try {
            String events = survivalService.advanceDays(characterId, days);
            Map<String, String> result = new HashMap<>();
            result.put("events", events);
            result.put("summary", survivalService.getSummary(characterId));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Adds food rations to a character.
     */
    @PostMapping("/{characterId}/food")
    public ResponseEntity<Map<String, Object>> addFood(
            @PathVariable int characterId,
            @RequestParam(defaultValue = "1") int rations) {
        log.debug("POST /api/v1/traveller/survival/{}/food — {} rations", characterId, rations);

        try {
            survivalService.addFood(characterId, rations);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("summary", survivalService.getSummary(characterId));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Adds water to a character.
     */
    @PostMapping("/{characterId}/water")
    public ResponseEntity<Map<String, Object>> addWater(
            @PathVariable int characterId,
            @RequestParam(defaultValue = "1") int liters) {
        log.debug("POST /api/v1/traveller/survival/{}/water — {} liters", characterId, liters);

        try {
            survivalService.addWater(characterId, liters);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("summary", survivalService.getSummary(characterId));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Sets the temperature for a character.
     */
    @PostMapping("/{characterId}/temperature")
    public ResponseEntity<Map<String, Object>> setTemperature(
            @PathVariable int characterId,
            @RequestParam String temperature) {
        log.debug("POST /api/v1/traveller/survival/{}/temperature — {}", characterId, temperature);

        try {
            survivalService.setTemperature(characterId, temperature);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("summary", survivalService.getSummary(characterId));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Sets the temperature from Celsius.
     */
    @PostMapping("/{characterId}/temperature-celsius")
    public ResponseEntity<Map<String, Object>> setTemperatureCelsius(
            @PathVariable int characterId,
            @RequestParam int celsius) {
        log.debug("POST /api/v1/traveller/survival/{}/temperature-celsius — {}°C", characterId, celsius);

        try {
            survivalService.setTemperatureCelsius(characterId, celsius);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("summary", survivalService.getSummary(characterId));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Sets the shelter for a character.
     */
    @PostMapping("/{characterId}/shelter")
    public ResponseEntity<Map<String, Object>> setShelter(
            @PathVariable int characterId,
            @RequestParam String shelter) {
        log.debug("POST /api/v1/traveller/survival/{}/shelter — {}", characterId, shelter);

        try {
            survivalService.setShelter(characterId, shelter);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("summary", survivalService.getSummary(characterId));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Adds a disease to a character.
     */
    @PostMapping("/{characterId}/disease")
    public ResponseEntity<Map<String, Object>> addDisease(
            @PathVariable int characterId,
            @RequestParam String disease) {
        log.debug("POST /api/v1/traveller/survival/{}/disease — {}", characterId, disease);

        try {
            survivalService.addDisease(characterId, disease);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("summary", survivalService.getSummary(characterId));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Gets a text summary of a character's survival state.
     */
    @GetMapping("/{characterId}/summary")
    public ResponseEntity<Map<String, String>> getSummary(@PathVariable int characterId) {
        log.debug("GET /api/v1/traveller/survival/{}/summary", characterId);

        try {
            String summary = survivalService.getSummary(characterId);
            Map<String, String> result = new HashMap<>();
            result.put("summary", summary);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Simulates a survival scenario.
     */
    @PostMapping("/{characterId}/simulate")
    public ResponseEntity<Map<String, String>> simulateSurvival(
            @PathVariable int characterId,
            @RequestParam(defaultValue = "10") int days,
            @RequestParam String temperature,
            @RequestParam String shelter,
            @RequestParam(defaultValue = "5") int food,
            @RequestParam(defaultValue = "5") int water) {
        log.debug("POST /api/v1/traveller/survival/{}/simulate — {} days, {}, {}",
                characterId, days, temperature, shelter);

        try {
            Survival.Temperature temp = Survival.Temperature.valueOf(temperature.toUpperCase());
            Survival.ShelterType sh = Survival.ShelterType.valueOf(shelter.toUpperCase());

            String events = survivalService.simulateSurvival(
                    characterId, days, temp, sh, food, water);

            Map<String, String> result = new HashMap<>();
            result.put("events", events);
            result.put("summary", survivalService.getSummary(characterId));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, String> result = new HashMap<>();
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Lists all tracked characters.
     */
    @GetMapping
    public ResponseEntity<Set<Integer>> listTracked() {
        log.debug("GET /api/v1/traveller/survival");
        return ResponseEntity.ok(survivalService.listTrackedCharacters());
    }

    /**
     * Lists all available temperatures.
     */
    @GetMapping("/temperatures")
    public ResponseEntity<List<Map<String, Object>>> listTemperatures() {
        log.debug("GET /api/v1/traveller/survival/temperatures");

        List<Map<String, Object>> temps = new ArrayList<>();
        for (Survival.Temperature temp : Survival.Temperature.values()) {
            Map<String, Object> t = new HashMap<>();
            t.put("name", temp.getName());
            t.put("minTemp", temp.getMinTemp());
            t.put("maxTemp", temp.getMaxTemp());
            t.put("damagePerDay", temp.getDamagePerDay());
            t.put("description", temp.getDescription());
            temps.add(t);
        }

        return ResponseEntity.ok(temps);
    }

    /**
     * Lists all available shelter types.
     */
    @GetMapping("/shelters")
    public ResponseEntity<List<Map<String, Object>>> listShelters() {
        log.debug("GET /api/v1/traveller/survival/shelters");

        List<Map<String, Object>> shelters = new ArrayList<>();
        for (Survival.ShelterType shelter : Survival.ShelterType.values()) {
            Map<String, Object> s = new HashMap<>();
            s.put("name", shelter.getName());
            s.put("protectionLevel", shelter.getProtectionLevel());
            s.put("description", shelter.getDescription());
            shelters.add(s);
        }

        return ResponseEntity.ok(shelters);
    }

    /**
     * Lists all available diseases.
     */
    @GetMapping("/diseases")
    public ResponseEntity<List<Map<String, Object>>> listDiseases() {
        log.debug("GET /api/v1/traveller/survival/diseases");

        List<Map<String, Object>> diseases = new ArrayList<>();
        for (Survival.Disease disease : Survival.Disease.values()) {
            Map<String, Object> d = new HashMap<>();
            d.put("name", disease.getName());
            d.put("severity", disease.getSeverity());
            d.put("durationDays", disease.getDurationDays());
            d.put("penalty", disease.getPenalty());
            d.put("description", disease.getDescription());
            diseases.add(d);
        }

        return ResponseEntity.ok(diseases);
    }
}
