package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.traveller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer for encumbrance management.
 * <p>
 * Manages character inventories, carrying capacity, and encumbrance levels.
 * Integrates with {@link CharacterService} to look up characters by ID.
 * </p>
 */
@Service
public class EncumbranceService {

    private static final Logger log = LoggerFactory.getLogger(EncumbranceService.class);

    private final CharacterService characterService;
    private final Map<Integer, Encumbrance> encumbranceMap = new ConcurrentHashMap<>();

    /**
     * Creates the service with the character service for character lookup.
     */
    public EncumbranceService(CharacterService characterService) {
        this.characterService = Objects.requireNonNull(characterService,
                "CharacterService must not be null");
    }

    /**
     * Gets or creates an encumbrance tracker for a character.
     */
    public Encumbrance getEncumbrance(int characterId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }

        return encumbranceMap.computeIfAbsent(characterId, id -> {
            Encumbrance enc = new Encumbrance(character);
            log.info("Created encumbrance tracker for {}", character.getName());
            return enc;
        });
    }

    /**
     * Adds an item to a character's inventory.
     */
    public boolean addItem(int characterId, String name, double weight,
                            int quantity, Encumbrance.Item.Category category) {
        Encumbrance enc = getEncumbrance(characterId);
        Encumbrance.Item item = new Encumbrance.Item(name, weight, quantity, category);
        boolean added = enc.addItem(item);

        if (added) {
            log.info("Added {} to {}'s inventory: {}",
                    category.getName(), enc.getCharacter().getName(), item);
        }

        return added;
    }

    /**
     * Removes an item from a character's inventory.
     */
    public boolean removeItem(int characterId, String name, int quantity) {
        Encumbrance enc = getEncumbrance(characterId);
        boolean removed = enc.removeItem(name, quantity);

        if (removed) {
            log.info("Removed {} x{} from {}'s inventory", name, quantity,
                    enc.getCharacter().getName());
        }

        return removed;
    }

    /**
     * Removes all items of a given name from a character's inventory.
     */
    public void removeAllItem(int characterId, String name) {
        Encumbrance enc = getEncumbrance(characterId);
        enc.removeItem(name);
        log.info("Removed all {} from {}'s inventory", name,
                enc.getCharacter().getName());
    }

    /**
     * Returns a summary of a character's encumbrance state.
     */
    public String getSummary(int characterId) {
        Encumbrance enc = getEncumbrance(characterId);
        return enc.getSummary();
    }

    /**
     * Lists all encumbrance trackers.
     */
    public Set<Integer> listTrackedCharacters() {
        return new HashSet<>(encumbranceMap.keySet());
    }

    /**
     * Presets common gear for a character based on their career.
     */
    public void presetGear(int characterId, Career career) {
        Encumbrance enc = getEncumbrance(characterId);

        switch (career) {
            case SOLDIER:
                enc.addItem(new Encumbrance.Item("Laser Rifle", 5.5, 1, Encumbrance.Item.Category.WEAPON));
                enc.addItem(new Encumbrance.Item("Laser Pistol", 3.0, 1, Encumbrance.Item.Category.WEAPON));
                enc.addItem(new Encumbrance.Item("Battle Dress", 12.0, 1, Encumbrance.Item.Category.ARMOR));
                enc.addItem(new Encumbrance.Item("Rations", 0.5, 5, Encumbrance.Item.Category.FOOD));
                enc.addItem(new Encumbrance.Item("Water", 1.0, 3, Encumbrance.Item.Category.WATER));
                enc.addItem(new Encumbrance.Item("Medikit", 2.0, 1, Encumbrance.Item.Category.MEDICAL));
                break;
            case EXPLORER:
                enc.addItem(new Encumbrance.Item("Laser Pistol", 3.0, 1, Encumbrance.Item.Category.WEAPON));
                enc.addItem(new Encumbrance.Item("Rations", 0.5, 10, Encumbrance.Item.Category.FOOD));
                enc.addItem(new Encumbrance.Item("Water", 1.0, 5, Encumbrance.Item.Category.WATER));
                enc.addItem(new Encumbrance.Item("Medikit", 2.0, 1, Encumbrance.Item.Category.MEDICAL));
                enc.addItem(new Encumbrance.Item("Scanner", 1.5, 1, Encumbrance.Item.Category.ELECTRONICS));
                break;
            case MERCHANT:
                enc.addItem(new Encumbrance.Item("Laser Pistol", 3.0, 1, Encumbrance.Item.Category.WEAPON));
                enc.addItem(new Encumbrance.Item("Rations", 0.5, 5, Encumbrance.Item.Category.FOOD));
                enc.addItem(new Encumbrance.Item("Water", 1.0, 3, Encumbrance.Item.Category.WATER));
                enc.addItem(new Encumbrance.Item("Credit Stick", 0.1, 1, Encumbrance.Item.Category.PERSONAL));
                break;
            case NOBLE:
                enc.addItem(new Encumbrance.Item("Small Sword", 1.5, 1, Encumbrance.Item.Category.WEAPON));
                enc.addItem(new Encumbrance.Item("Fine Clothing", 2.0, 1, Encumbrance.Item.Category.CLOTHING));
                enc.addItem(new Encumbrance.Item("Credit Stick", 0.1, 1, Encumbrance.Item.Category.PERSONAL));
                break;
            default:
                enc.addItem(new Encumbrance.Item("Laser Pistol", 3.0, 1, Encumbrance.Item.Category.WEAPON));
                enc.addItem(new Encumbrance.Item("Rations", 0.5, 3, Encumbrance.Item.Category.FOOD));
                enc.addItem(new Encumbrance.Item("Water", 1.0, 2, Encumbrance.Item.Category.WATER));
                break;
        }

        log.info("Preset gear for {} ({} career)", enc.getCharacter().getName(), career.getName());
    }
}
