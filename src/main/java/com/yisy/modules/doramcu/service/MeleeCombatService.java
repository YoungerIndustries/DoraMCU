package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.traveller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service layer for melee combat encounters.
 * <p>
 * Manages combat encounters, combatant setup, and combat resolution.
 * Integrates with {@link CharacterService} to look up characters by ID
 * and create combatants from them.
 * </p>
 * <p>
 * Provides methods to:
 * </p>
 * <ul>
 *   <li>Start a new combat encounter between two characters</li>
 *   <li>Execute combat round by round or to completion</li>
 *   <li>Retrieve combat results and logs</li>
 *   <li>List available weapons and armor types</li>
 * </ul>
 */
@Service
public class MeleeCombatService {

    private static final Logger log = LoggerFactory.getLogger(MeleeCombatService.class);

    private final CharacterService characterService;
    private final Map<String, MeleeCombat> activeCombats = new ConcurrentHashMap<>();
    private final AtomicInteger combatIdCounter = new AtomicInteger(1);

    /**
     * Creates the service with the character service for character lookup.
     *
     * @param characterService the character service
     */
    public MeleeCombatService(CharacterService characterService) {
        this.characterService = Objects.requireNonNull(characterService,
                "CharacterService must not be null");
    }

    /**
     * Starts a new melee combat encounter between two characters.
     * <p>
     * Both characters are looked up by ID. Combatants are created with
     * the specified weapons and armor. If weapon or armor is not specified,
     * defaults to unarmed and no armor.
     * </p>
     *
     * @param attackerId the attacker's character ID
     * @param defenderId the defender's character ID
     * @param attackerWeapon the attacker's weapon (or null for unarmed)
     * @param attackerArmor the attacker's armor (or null for no armor)
     * @param defenderWeapon the defender's weapon (or null for unarmed)
     * @param defenderArmor the defender's armor (or null for no armor)
     * @return the created combat encounter
     */
    public MeleeCombat startCombat(int attackerId, int defenderId,
                                    String attackerWeapon, String attackerArmor,
                                    String defenderWeapon, String defenderArmor) {
        TravellerCharacter attackerChar = characterService.getCharacter(attackerId);
        if (attackerChar == null) {
            throw new IllegalArgumentException("Attacker character not found: " + attackerId);
        }
        TravellerCharacter defenderChar = characterService.getCharacter(defenderId);
        if (defenderChar == null) {
            throw new IllegalArgumentException("Defender character not found: " + defenderId);
        }

        // Create combatants
        String combatId = "combat-" + combatIdCounter.getAndIncrement();
        MeleeCombatant attacker = new MeleeCombatant(attackerChar, combatId + "-attacker");
        MeleeCombatant defender = new MeleeCombatant(defenderChar, combatId + "-defender");

        // Equip weapons
        if (attackerWeapon != null && !attackerWeapon.isEmpty()) {
            attacker.setWeapon(parseWeapon(attackerWeapon));
        }
        if (defenderWeapon != null && !defenderWeapon.isEmpty()) {
            defender.setWeapon(parseWeapon(defenderWeapon));
        }

        // Equip armor
        if (attackerArmor != null && !attackerArmor.isEmpty()) {
            attacker.setArmor(parseArmor(attackerArmor));
        }
        if (defenderArmor != null && !defenderArmor.isEmpty()) {
            defender.setArmor(parseArmor(defenderArmor));
        }

        // Create and store combat
        MeleeCombat combat = new MeleeCombat(attacker, defender);
        activeCombats.put(combatId, combat);

        log.info("Combat started: {} vs {} ({})",
                attacker.getName(), defender.getName(), combatId);

        return combat;
    }

    /**
     * Starts a simple combat with default equipment.
     *
     * @param attackerId the attacker's character ID
     * @param defenderId the defender's character ID
     * @return the created combat encounter
     */
    public MeleeCombat startCombat(int attackerId, int defenderId) {
        return startCombat(attackerId, defenderId, null, null, null, null);
    }

    /**
     * Executes a single round of an active combat.
     *
     * @param combatId the combat ID
     * @return the combat round result
     */
    public MeleeCombat.CombatRound executeRound(String combatId) {
        MeleeCombat combat = activeCombats.get(combatId);
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
     *
     * @param combatId the combat ID
     * @return the final combat outcome
     */
    public MeleeCombat.CombatOutcome fightToCompletion(String combatId) {
        MeleeCombat combat = activeCombats.get(combatId);
        if (combat == null) {
            throw new IllegalArgumentException("Combat not found: " + combatId);
        }
        if (!combat.isActive()) {
            throw new IllegalStateException("Combat is not active: " + combatId);
        }

        log.info("Fighting combat {} to completion", combatId);
        return combat.fightToCompletion();
    }

    /**
     * Gets the current state of a combat encounter.
     *
     * @param combatId the combat ID
     * @return the combat, or null if not found
     */
    public MeleeCombat getCombat(String combatId) {
        return activeCombats.get(combatId);
    }

    /**
     * Returns a summary of a combat encounter.
     *
     * @param combatId the combat ID
     * @return the summary string, or null if not found
     */
    public String getCombatSummary(String combatId) {
        MeleeCombat combat = activeCombats.get(combatId);
        if (combat == null) {
            return null;
        }
        return combat.getSummary();
    }

    /**
     * Returns all available melee weapons.
     *
     * @return array of all melee weapons
     */
    public MeleeWeapon[] getAllWeapons() {
        return MeleeWeapon.values();
    }

    /**
     * Returns all available armor types.
     *
     * @return array of all armor types
     */
    public Armor[] getAllArmor() {
        return Armor.values();
    }

    /**
     * Lists all active combat encounters.
     *
     * @return set of active combat IDs
     */
    public Set<String> listActiveCombats() {
        return new HashSet<>(activeCombats.keySet());
    }

    /**
     * Creates a combat encounter with randomly generated characters.
     * <p>
     * Two characters are generated with random abilities and careers,
     * then pitted against each other with random weapons.
     * </p>
     *
     * @param name1 the first character's name
     * @param name2 the second character's name
     * @return the combat encounter fought to completion
     */
    public MeleeCombat randomDuel(String name1, String name2) {
        // Generate characters
        TravellerCharacter char1 = characterService.generateFullCharacter(name1);
        TravellerCharacter char2 = characterService.generateFullCharacter(name2);

        // Random weapons
        MeleeWeapon[] weapons = MeleeWeapon.values();
        Random random = new Random();
        MeleeWeapon weapon1 = weapons[random.nextInt(weapons.length)];
        MeleeWeapon weapon2 = weapons[random.nextInt(weapons.length)];

        // Random armor
        Armor[] armors = Armor.values();
        Armor armor1 = armors[random.nextInt(armors.length)];
        Armor armor2 = armors[random.nextInt(armors.length)];

        // Start combat
        MeleeCombat combat = startCombat(
                char1.getId(), char2.getId(),
                weapon1.name(), armor1.name(),
                weapon2.name(), armor2.name());

        // Fight to completion
        combat.fightToCompletion();

        return combat;
    }

    /**
     * Parses a weapon name string into a MeleeWeapon enum.
     *
     * @param weaponName the weapon name (case-insensitive)
     * @return the weapon
     */
    private MeleeWeapon parseWeapon(String weaponName) {
        try {
            return MeleeWeapon.valueOf(weaponName.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown weapon: " + weaponName +
                    ". Available: " + Arrays.toString(MeleeWeapon.values()));
        }
    }

    /**
     * Parses an armor name string into an Armor enum.
     *
     * @param armorName the armor name (case-insensitive)
     * @return the armor
     */
    private Armor parseArmor(String armorName) {
        try {
            return Armor.valueOf(armorName.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown armor: " + armorName +
                    ". Available: " + Arrays.toString(Armor.values()));
        }
    }
}
