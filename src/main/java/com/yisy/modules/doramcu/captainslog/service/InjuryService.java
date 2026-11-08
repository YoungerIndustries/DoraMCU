package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages character injury and recovery in the Captain's Log system.
 * <p>
 * In the Captain's Log / Star Trek Adventures system, characters have a Constitution track
 * that represents their ability to withstand harm. The injury system tracks:
 * <ul>
 *   <li>Constitution value — the character's hit points (from the character sheet)</li>
 *   <li>Injury state — HEALTHY → STUNNED → INJURED → DOWN</li>
 *   <li>Current damage — how many hits the character has taken</li>
 * </ul>
 * </p>
 * <p>
 * Injury progression:
 * <ul>
 *   <li>When damage reaches Constitution / 2 → STUNNED (adds 1 Threat to task attempts)</li>
 *   <li>When damage reaches Constitution → INJURED (adds 2 Threats to task attempts)</li>
 *   <li>When damage exceeds Constitution → DOWN (character is incapacitated)</li>
 * </ul>
 * </p>
 * <p>
 * Recovery:
 * <ul>
 *   <li>Between missions, a character can recover by spending time in medical care</li>
 *   <li>A successful Medicine + Fitness task attempt heals 1 damage</li>
 *   <li>Full recovery resets injury state to HEALTHY</li>
 * </ul>
 * </p>
 */
@Service
public class InjuryService {

    private static final Logger log = LoggerFactory.getLogger(InjuryService.class);

    private final CaptainLogCharacterRepository characterRepository;

    public InjuryService(CaptainLogCharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    /**
     * Applies damage to a character and updates their injury state.
     *
     * @param characterId the character to damage
     * @param damage the amount of damage to apply
     * @return a map with the updated injury state, current damage, and constitution
     */
    @Transactional
    public Map<String, Object> applyDamage(Long characterId, int damage) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        int constitution = character.getConstitution();
        if (constitution <= 0) {
            // If no constitution is set, default to 3 (standard Captain's Log value)
            constitution = 3;
            character.setConstitution(3);
        }

        // Calculate current damage from injury state
        int currentDamage = damageFromInjuryState(character.getInjuryState(), constitution);

        // Apply new damage
        currentDamage += damage;
        log.info("Applying {} damage to {} (constitution={}, current damage={})",
                damage, character.getName(), constitution, currentDamage);

        // Update injury state based on damage
        CaptainLogCharacter.InjuryState newState;
        if (currentDamage <= 0) {
            newState = CaptainLogCharacter.InjuryState.HEALTHY;
        } else if (currentDamage <= constitution / 2) {
            newState = CaptainLogCharacter.InjuryState.STUNNED;
        } else if (currentDamage <= constitution) {
            newState = CaptainLogCharacter.InjuryState.INJURED;
        } else {
            newState = CaptainLogCharacter.InjuryState.DOWN;
        }

        character.setInjuryState(newState);
        characterRepository.save(character);

        log.info("{} injury state changed to {}", character.getName(), newState);

        Map<String, Object> result = new HashMap<>();
        result.put("characterId", characterId);
        result.put("characterName", character.getName());
        result.put("injuryState", newState.name());
        result.put("damageApplied", damage);
        result.put("constitution", constitution);
        result.put("injuryPenalty", getInjuryPenalty(newState));
        result.put("description", getInjuryDescription(newState, character.getName()));
        return result;
    }

    /**
     * Heals a character, reducing their damage and potentially improving injury state.
     *
     * @param characterId the character to heal
     * @param healing the amount of healing to apply
     * @return a map with the updated injury state
     */
    @Transactional
    public Map<String, Object> healCharacter(Long characterId, int healing) {
        return applyDamage(characterId, -healing);
    }

    /**
     * Performs a full recovery between missions, resetting injury state to HEALTHY.
     *
     * @param characterId the character to recover
     * @return a map with the recovery result
     */
    @Transactional
    public Map<String, Object> fullRecovery(Long characterId) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        CaptainLogCharacter.InjuryState previousState = character.getInjuryState();
        character.setInjuryState(CaptainLogCharacter.InjuryState.HEALTHY);
        characterRepository.save(character);

        log.info("{} recovered from {} to HEALTHY", character.getName(), previousState);

        Map<String, Object> result = new HashMap<>();
        result.put("characterId", characterId);
        result.put("characterName", character.getName());
        result.put("previousState", previousState.name());
        result.put("injuryState", "HEALTHY");
        result.put("description", character.getName() + " has fully recovered and is ready for the next mission.");
        return result;
    }

    /**
     * Returns the threat penalty imposed by the current injury state.
     * STUNNED adds 1 Threat, INJURED adds 2 Threats, DOWN cannot act.
     */
    public int getInjuryPenalty(CaptainLogCharacter.InjuryState state) {
        return switch (state) {
            case HEALTHY -> 0;
            case STUNNED -> 1;
            case INJURED -> 2;
            case DOWN -> -1; // Cannot act
        };
    }

    /**
     * Returns a human-readable description of the injury state.
     */
    public String getInjuryDescription(CaptainLogCharacter.InjuryState state, String characterName) {
        return switch (state) {
            case HEALTHY -> characterName + " is in good condition and ready for action.";
            case STUNNED -> characterName + " is stunned and operating at a disadvantage. " +
                    "Task attempts suffer 1 additional Threat.";
            case INJURED -> characterName + " is injured and struggling. " +
                    "Task attempts suffer 2 additional Threats.";
            case DOWN -> characterName + " is down and incapacitated. " +
                    "The character cannot perform task attempts until healed.";
        };
    }

    /**
     * Estimates the current damage based on injury state and constitution.
     * Used to reconstruct damage from the injury state when needed.
     */
    private int damageFromInjuryState(CaptainLogCharacter.InjuryState state, int constitution) {
        return switch (state) {
            case HEALTHY -> 0;
            case STUNNED -> constitution / 4; // Midpoint of HEALTHY and INJURED
            case INJURED -> constitution / 2 + constitution / 4; // Midpoint of STUNNED and DOWN
            case DOWN -> constitution + 1;
        };
    }
}
