package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.traveller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service layer for firearms combat encounters.
 * <p>
 * Manages firearms combat encounters, combatant setup, and combat resolution.
 * Integrates with {@link CharacterService} to look up characters by ID
 * and create combatants from them.
 * </p>
 * <p>
 * Provides methods to:
 * </p>
 * <ul>
 *   <li>Start a new firearms combat encounter between two characters</li>
 *   <li>Execute combat round by round or to completion</li>
 *   <li>Retrieve combat results and logs</li>
 *   <li>List available firearms and cover types</li>
 * </ul>
 */
@Service
public class FirearmsCombatService {

    private static final Logger log = LoggerFactory.getLogger(FirearmsCombatService.class);

    private final CharacterService characterService;
    private final Map<String, FirearmsCombat> activeCombats = new ConcurrentHashMap<>();
    private final AtomicInteger combatIdCounter = new AtomicInteger(1);

    /**
     * Creates the service with the character service for character lookup.
     */
    public FirearmsCombatService(CharacterService characterService) {
        this.characterService = Objects.requireNonNull(characterService,
                "CharacterService must not be null");
    }

    /**
     * Starts a new firearms combat encounter between two characters.
     *
     * @param shooter1Id the first shooter's character ID
     * @param shooter2Id the second shooter's character ID
     * @param shooter1Weapon the first shooter's weapon (or null for default)
     * @param shooter1Cover the first shooter's cover (or null for no cover)
     * @param shooter2Weapon the second shooter's weapon (or null for default)
     * @param shooter2Cover the second shooter's cover (or null for no cover)
     * @param distance the distance between combatants in yards
     * @return the created combat encounter
     */
    public FirearmsCombat startCombat(int shooter1Id, int shooter2Id,
                                       String shooter1Weapon, String shooter1Cover,
                                       String shooter2Weapon, String shooter2Cover,
                                       int distance) {
        TravellerCharacter char1 = characterService.getCharacter(shooter1Id);
        if (char1 == null) {
            throw new IllegalArgumentException("Shooter 1 character not found: " + shooter1Id);
        }
        TravellerCharacter char2 = characterService.getCharacter(shooter2Id);
        if (char2 == null) {
            throw new IllegalArgumentException("Shooter 2 character not found: " + shooter2Id);
        }

        String combatId = "firearms-" + combatIdCounter.getAndIncrement();
        FirearmsCombatant shooter1 = new FirearmsCombatant(char1, combatId + "-shooter1");
        FirearmsCombatant shooter2 = new FirearmsCombatant(char2, combatId + "-shooter2");

        // Equip weapons
        if (shooter1Weapon != null && !shooter1Weapon.isEmpty()) {
            shooter1.setWeapon(parseWeapon(shooter1Weapon));
        }
        if (shooter2Weapon != null && !shooter2Weapon.isEmpty()) {
            shooter2.setWeapon(parseWeapon(shooter2Weapon));
        }

        // Set cover
        if (shooter1Cover != null && !shooter1Cover.isEmpty()) {
            shooter1.setCover(parseCover(shooter1Cover));
        }
        if (shooter2Cover != null && !shooter2Cover.isEmpty()) {
            shooter2.setCover(parseCover(shooter2Cover));
        }

        // Create and store combat
        FirearmsCombat combat = new FirearmsCombat(shooter1, shooter2, distance);
        activeCombats.put(combatId, combat);

        log.info("Firearms combat started: {} vs {} ({})",
                shooter1.getName(), shooter2.getName(), combatId);

        return combat;
    }

    /**
     * Starts a simple combat with default equipment.
     */
    public FirearmsCombat startCombat(int shooter1Id, int shooter2Id) {
        return startCombat(shooter1Id, shooter2Id, null, null, null, null, 50);
    }

    /**
     * Executes a single round of an active combat.
     */
    public FirearmsCombat.CombatRound executeRound(String combatId) {
        FirearmsCombat combat = activeCombats.get(combatId);
        if (combat == null) {
            throw new IllegalArgumentException("Combat not found: " + combatId);
        }
        if (!combat.isActive()) {
            throw new IllegalStateException("Combat is not active: " + combatId);
        }

        return combat.executeRound();
    }

    /**
     * Runs a combat to completion.
     */
    public FirearmsCombat.CombatOutcome fightToCompletion(String combatId) {
        FirearmsCombat combat = activeCombats.get(combatId);
        if (combat == null) {
            throw new IllegalArgumentException("Combat not found: " + combatId);
        }
        if (!combat.isActive()) {
            throw new IllegalStateException("Combat is not active: " + combatId);
        }

        log.info("Fighting firearms combat {} to completion", combatId);
        return combat.fightToCompletion();
    }

    /**
     * Gets the current state of a combat encounter.
     */
    public FirearmsCombat getCombat(String combatId) {
        return activeCombats.get(combatId);
    }

    /**
     * Returns a summary of a combat encounter.
     */
    public String getCombatSummary(String combatId) {
        FirearmsCombat combat = activeCombats.get(combatId);
        if (combat == null) {
            return null;
        }
        return combat.getSummary();
    }

    /**
     * Returns all available firearms.
     */
    public FirearmWeapon[] getAllWeapons() {
        return FirearmWeapon.values();
    }

    /**
     * Returns all available cover levels.
     */
    public FirearmsCombatant.CoverLevel[] getAllCoverLevels() {
        return FirearmsCombatant.CoverLevel.values();
    }

    /**
     * Lists all active combat encounters.
     */
    public Set<String> listActiveCombats() {
        return new HashSet<>(activeCombats.keySet());
    }

    /**
     * Creates a combat encounter with randomly generated characters.
     */
    public FirearmsCombat randomGunfight(String name1, String name2) {
        TravellerCharacter char1 = characterService.generateFullCharacter(name1);
        TravellerCharacter char2 = characterService.generateFullCharacter(name2);

        FirearmWeapon[] weapons = FirearmWeapon.values();
        Random random = new Random();
        FirearmWeapon weapon1 = weapons[random.nextInt(weapons.length)];
        FirearmWeapon weapon2 = weapons[random.nextInt(weapons.length)];

        FirearmsCombatant.CoverLevel[] covers = FirearmsCombatant.CoverLevel.values();
        FirearmsCombatant.CoverLevel cover1 = covers[random.nextInt(covers.length)];
        FirearmsCombatant.CoverLevel cover2 = covers[random.nextInt(covers.length)];

        FirearmsCombat combat = startCombat(
                char1.getId(), char2.getId(),
                weapon1.name(), cover1.name(),
                weapon2.name(), cover2.name(),
                random.nextInt(200) + 10);

        combat.fightToCompletion();

        return combat;
    }

    /**
     * Parses a weapon name string into a FirearmWeapon enum.
     */
    private FirearmWeapon parseWeapon(String weaponName) {
        try {
            return FirearmWeapon.valueOf(weaponName.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown weapon: " + weaponName +
                    ". Available: " + Arrays.toString(FirearmWeapon.values()));
        }
    }

    /**
     * Parses a cover name string into a CoverLevel enum.
     */
    private FirearmsCombatant.CoverLevel parseCover(String coverName) {
        try {
            return FirearmsCombatant.CoverLevel.valueOf(coverName.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown cover: " + coverName +
                    ". Available: " + Arrays.toString(FirearmsCombatant.CoverLevel.values()));
        }
    }
}
