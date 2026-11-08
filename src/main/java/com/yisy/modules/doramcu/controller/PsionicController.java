package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.traveller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for psionic operations from Classic Traveller B03: Worlds & Adventures.
 * <p>
 * Provides endpoints for psionic detection, power checks, encounters,
 * and discipline management.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/psionics}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/psionics")
public class PsionicController {

    private static final Logger log = LoggerFactory.getLogger(PsionicController.class);

    private final CharacterService characterService;

    public PsionicController(CharacterService characterService) {
        this.characterService = characterService;
    }

    /**
     * Returns all psionic disciplines with descriptions.
     *
     * @return list of all 6 psionic disciplines
     */
    @GetMapping("/disciplines")
    public ResponseEntity<List<Map<String, Object>>> getDisciplines() {
        log.debug("GET /api/v1/traveller/psionics/disciplines");
        List<Map<String, Object>> disciplines = new ArrayList<>();
        for (PsionicDiscipline d : PsionicDiscipline.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", d.name());
            map.put("displayName", d.getDisplayName());
            map.put("description", d.getDescription());
            map.put("tableValue", d.getTableValue());
            disciplines.add(map);
        }
        return ResponseEntity.ok(disciplines);
    }

    /**
     * Returns all psionic difficulty levels with modifiers.
     *
     * @return list of all 5 difficulty levels
     */
    @GetMapping("/difficulties")
    public ResponseEntity<List<Map<String, Object>>> getDifficulties() {
        log.debug("GET /api/v1/traveller/psionics/difficulties");
        List<Map<String, Object>> difficulties = new ArrayList<>();
        for (PsionicCheck.Difficulty d : PsionicCheck.Difficulty.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", d.name());
            map.put("displayName", d.getDisplayName());
            map.put("modifier", d.getModifier());
            difficulties.add(map);
        }
        return ResponseEntity.ok(difficulties);
    }

    /**
     * Returns all psionic encounter types.
     *
     * @return list of all 8 encounter types
     */
    @GetMapping("/encounter-types")
    public ResponseEntity<List<Map<String, Object>>> getEncounterTypes() {
        log.debug("GET /api/v1/traveller/psionics/encounter-types");
        List<Map<String, Object>> types = new ArrayList<>();
        for (PsionicEncounter.EncounterType t : PsionicEncounter.EncounterType.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", t.name());
            map.put("displayName", t.getDisplayName());
            map.put("description", t.getDescription());
            types.add(map);
        }
        return ResponseEntity.ok(types);
    }

    /**
     * Performs psionic detection for a character.
     * <p>
     * Uses the character's In (Intuition) score to determine if they have
     * psionic potential. Characters with In 8+ may have psionic ability.
     * </p>
     *
     * @param id the character ID
     * @return the detection result with the character's psionic status
     */
    @PostMapping("/characters/{id}/detect")
    public ResponseEntity<Map<String, Object>> detectPsionic(@PathVariable int id) {
        log.debug("POST /api/v1/traveller/psionics/characters/{}/detect", id);
        TravellerCharacter character = characterService.getCharacter(id);
        if (character == null) {
            return ResponseEntity.notFound().build();
        }
        int inScore = character.getAbility(AbilityScore.ST);
        boolean detected = character.detectPsionic();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("characterId", id);
        result.put("characterName", character.getName());
        result.put("inScore", inScore);
        result.put("detected", detected);
        if (detected && character.getPsionicAbility() != null) {
            result.put("detectionLevel", character.getPsionicAbility().getDetectionLevel().name());
            result.put("detectionLevelDisplay", character.getPsionicAbility().getDetectionLevel().getDisplayName());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Performs a psionic power check.
     * <p>
     * Target = 12 - (In / 2) - disciplineLevel + difficulty
     * Success = roll <= target
     * </p>
     *
     * @param inScore the character's In (Intuition) score
     * @param disciplineLevel the character's level in the relevant discipline (1-5)
     * @param difficulty the difficulty of the power attempt (EASY, NORMAL, HARD, VERY_HARD, NEAR_IMPOSSIBLE)
     * @return the check result
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> performCheck(
            @RequestParam int inScore,
            @RequestParam int disciplineLevel,
            @RequestParam String difficulty) {
        log.debug("POST /api/v1/traveller/psionics/check?inScore={},disciplineLevel={},difficulty={}",
                inScore, disciplineLevel, difficulty);
        try {
            PsionicCheck.Difficulty diff = PsionicCheck.Difficulty.valueOf(difficulty.toUpperCase());
            PsionicCheck checker = new PsionicCheck();
            PsionicCheck.PsionicResult result = checker.performCheck(inScore, disciplineLevel, diff);
            int fatigueCost = checker.getFatigueCost(result.isSuccess(), diff);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("success", result.isSuccess());
            map.put("roll", result.getRoll());
            map.put("target", result.getTarget());
            map.put("message", result.getMessage());
            map.put("fatigueCost", fatigueCost);
            map.put("inScore", inScore);
            map.put("disciplineLevel", disciplineLevel);
            map.put("difficulty", diff.getDisplayName());
            return ResponseEntity.ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generates a random psionic encounter.
     *
     * @param detectionLevel the character's psionic detection level (LATENT, WEAK, AVERAGE, STRONG, MASTER)
     * @return the encounter result with narrative
     */
    @PostMapping("/encounter")
    public ResponseEntity<Map<String, Object>> generateEncounter(
            @RequestParam String detectionLevel) {
        log.debug("POST /api/v1/traveller/psionics/encounter?detectionLevel={}", detectionLevel);
        try {
            PsionicAbility.DetectionLevel level = PsionicAbility.DetectionLevel.valueOf(detectionLevel.toUpperCase());
            PsionicEncounter encounter = new PsionicEncounter();
            PsionicEncounter.PsionicEncounterResult result = encounter.generateEncounter(level);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", result.getType().name());
            map.put("typeDisplay", result.getType().getDisplayName());
            map.put("narrative", result.getNarrative());
            map.put("roll", result.getRoll());
            return ResponseEntity.ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Adds a psionic discipline to a character.
     *
     * @param id the character ID
     * @param discipline the discipline name (TELEPATHY, CLAIRVOYANCE, TELEKINESIS, AWARENESS, TELEPORTATION, SPECIAL)
     * @param level the proficiency level (1-5)
     * @return the updated character
     */
    @PostMapping("/characters/{id}/discipline")
    public ResponseEntity<TravellerCharacter> addDiscipline(
            @PathVariable int id,
            @RequestParam String discipline,
            @RequestParam int level) {
        log.debug("POST /api/v1/traveller/psionics/characters/{}/discipline?discipline={},level={}",
                id, discipline, level);
        TravellerCharacter character = characterService.getCharacter(id);
        if (character == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            PsionicDiscipline disc = PsionicDiscipline.valueOf(discipline.toUpperCase());
            PsionicAbility ability = character.getPsionicAbility();
            if (ability == null) {
                int inScore = character.getAbility(AbilityScore.ST);
                ability = new PsionicAbility(inScore);
                ability.setDetectionLevel(PsionicAbility.DetectionLevel.WEAK);
                character.setPsionicAbility(ability);
            }
            ability.addDiscipline(disc, level);
            return ResponseEntity.ok(character);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Restores a character's psionic fatigue by resting.
     *
     * @param id the character ID
     * @param hours the number of hours of rest (1 hour = 1 fatigue point removed)
     * @return the updated character
     */
    @PostMapping("/characters/{id}/rest")
    public ResponseEntity<TravellerCharacter> restPsionic(
            @PathVariable int id,
            @RequestParam int hours) {
        log.debug("POST /api/v1/traveller/psionics/characters/{}/rest?hours={}", id, hours);
        TravellerCharacter character = characterService.getCharacter(id);
        if (character == null) {
            return ResponseEntity.notFound().build();
        }
        PsionicAbility ability = character.getPsionicAbility();
        if (ability != null) {
            ability.rest(hours);
        }
        return ResponseEntity.ok(character);
    }

    /**
     * Returns the psionic profile for a character.
     *
     * @param id the character ID
     * @return the psionic profile data
     */
    @GetMapping("/characters/{id}")
    public ResponseEntity<Map<String, Object>> getPsionicProfile(@PathVariable int id) {
        log.debug("GET /api/v1/traveller/psionics/characters/{}", id);
        TravellerCharacter character = characterService.getCharacter(id);
        if (character == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("characterId", id);
        profile.put("characterName", character.getName());
        profile.put("inScore", character.getAbility(AbilityScore.ST));
        profile.put("isPsionic", character.isPsionic());
        PsionicAbility ability = character.getPsionicAbility();
        if (ability != null && ability.getDetectionLevel() != PsionicAbility.DetectionLevel.NONE) {
            profile.put("detectionLevel", ability.getDetectionLevel().name());
            profile.put("detectionLevelDisplay", ability.getDetectionLevel().getDisplayName());
            profile.put("fatigue", ability.getFatigue());
            profile.put("maxFatigue", ability.getMaxFatigue());
            profile.put("canUsePowers", ability.canUsePowers());
            List<Map<String, Object>> disciplines = new ArrayList<>();
            for (Map.Entry<PsionicDiscipline, Integer> entry : ability.getDisciplines().entrySet()) {
                Map<String, Object> disc = new LinkedHashMap<>();
                disc.put("name", entry.getKey().name());
                disc.put("displayName", entry.getKey().getDisplayName());
                disc.put("level", entry.getValue());
                disciplines.add(disc);
            }
            profile.put("disciplines", disciplines);
        } else {
            profile.put("detectionLevel", "NONE");
            profile.put("detectionLevelDisplay", "None");
            profile.put("fatigue", 0);
            profile.put("maxFatigue", 0);
            profile.put("canUsePowers", false);
            profile.put("disciplines", new ArrayList<>());
        }
        return ResponseEntity.ok(profile);
    }
}
