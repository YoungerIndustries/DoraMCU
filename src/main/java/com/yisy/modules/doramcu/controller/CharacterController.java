package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.Career;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import com.yisy.modules.doramcu.service.CharacterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Traveller character creation and management.
 * <p>
 * Provides endpoints for creating characters, serving career tours,
 * and generating full characters with random careers.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/characters}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/characters")
public class CharacterController {

    private static final Logger log = LoggerFactory.getLogger(CharacterController.class);

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    /**
     * Creates a new character with randomly rolled abilities.
     *
     * @param name the character's name (query parameter)
     * @return the created character
     */
    @PostMapping
    public ResponseEntity<TravellerCharacter> createCharacter(@RequestParam String name) {
        log.debug("POST /api/v1/traveller/characters?name={} - creating character", name);
        TravellerCharacter character = characterService.createCharacter(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(character);
    }

    /**
     * Creates a character with manually set abilities.
     *
     * @param name the character's name
     * @param abilities map of ability scores (e.g., {"DEX": 8, "ST": 6, ...})
     * @return the created character
     */
    @PostMapping("/manual")
    public ResponseEntity<TravellerCharacter> createCharacterManual(
            @RequestParam String name,
            @RequestBody Map<String, Integer> abilities) {
        log.debug("POST /api/v1/traveller/characters/manual?name={} - creating with manual abilities", name);
        Map<AbilityScore, Integer> abilityMap = new java.util.EnumMap<>(AbilityScore.class);
        for (Map.Entry<String, Integer> entry : abilities.entrySet()) {
            try {
                AbilityScore ability = AbilityScore.valueOf(entry.getKey().toUpperCase());
                abilityMap.put(ability, entry.getValue());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown ability: {}", entry.getKey());
            }
        }
        TravellerCharacter character = characterService.createCharacter(name, abilityMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(character);
    }

    /**
     * Retrieves a character by ID.
     *
     * @param id the character ID
     * @return the character, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TravellerCharacter> getCharacter(@PathVariable int id) {
        log.debug("GET /api/v1/traveller/characters/{}", id);
        TravellerCharacter character = characterService.getCharacter(id);
        if (character == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(character);
    }

    /**
     * Returns all characters.
     *
     * @return list of all characters
     */
    @GetMapping
    public ResponseEntity<List<TravellerCharacter>> getAllCharacters() {
        log.debug("GET /api/v1/traveller/characters - listing all characters");
        List<TravellerCharacter> characters = characterService.listCharacters();
        return ResponseEntity.ok(characters);
    }

    /**
     * Simulates a career tour for a character.
     *
     * @param id the character ID
     * @param career the career to serve (e.g., SOLDIER, EXPLORER, GUNNER, PILOT, MERCHANT, SCOUT, SPY, NOBLE)
     * @return the career tour result
     */
    @PostMapping("/{id}/career")
    public ResponseEntity<TravellerCharacter.CareerTour> serveCareer(
            @PathVariable int id,
            @RequestParam String career) {
        log.debug("POST /api/v1/traveller/characters/{}/career?career={}", id, career);
        try {
            Career careerEnum = Career.valueOf(career.toUpperCase());
            TravellerCharacter.CareerTour tour = characterService.serveCareerTour(id, careerEnum);
            return ResponseEntity.ok(tour);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Generates a full character with random careers.
     * <p>
     * The character is created with random abilities and serves 2–4
     * random careers before retiring to adventure.
     * </p>
     *
     * @param name the character's name
     * @return the fully generated character
     */
    @PostMapping("/generate")
    public ResponseEntity<TravellerCharacter> generateCharacter(@RequestParam String name) {
        log.debug("POST /api/v1/traveller/characters/generate?name={} - full character generation", name);
        TravellerCharacter character = characterService.generateFullCharacter(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(character);
    }
}
