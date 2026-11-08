package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Encumbrance rules from Classic Traveller Book 1: Characters & Combat (1981).
 * <p>
 * Encumbrance determines how much weight a character can carry and how
 * it affects their movement and combat abilities. The system uses the
 * character's Strength ability to determine carrying capacity.
 * </p>
 * <p>
 * Encumbrance levels:
 * </p>
 * <ul>
 *   <li><b>None</b> — carrying less than 1/3 of capacity: no penalty</li>
 *   <li><b>Light</b> — carrying 1/3 to 2/3 of capacity: -1 to movement</li>
 *   <li><b>Medium</b> — carrying 2/3 to full capacity: -2 to movement, -1 to combat</li>
 *   <li><b>Heavy</b> — carrying more than full capacity: -3 to movement, -2 to combat</li>
 * </ul>
 * <p>
 * Derived from Classic Traveller Book 1, Chapter 4: Combat.
 * </p>
 */
public class Encumbrance {

    /**
     * Encumbrance levels based on carried weight.
     */
    public enum Level {
        /**
         * No encumbrance — carrying less than 1/3 of capacity.
         */
        NONE("None", 0, 0, "No penalty"),

        /**
         * Light encumbrance — carrying 1/3 to 2/3 of capacity.
         */
        LIGHT("Light", -1, 0, "-1 to movement"),

        /**
         * Medium encumbrance — carrying 2/3 to full capacity.
         */
        MEDIUM("Medium", -2, -1, "-2 to movement, -1 to combat"),

        /**
         * Heavy encumbrance — carrying more than full capacity.
         */
        HEAVY("Heavy", -3, -2, "-3 to movement, -2 to combat");

        private final String name;
        private final int movementPenalty;
        private final int combatPenalty;
        private final String description;

        Level(String name, int movementPenalty, int combatPenalty, String description) {
            this.name = name;
            this.movementPenalty = movementPenalty;
            this.combatPenalty = combatPenalty;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public int getMovementPenalty() {
            return movementPenalty;
        }

        public int getCombatPenalty() {
            return combatPenalty;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * A carried item with weight.
     */
    public static class Item {
        private final String name;
        private final double weight;
        private final int quantity;
        private final Category category;

        /**
         * Categories of carried items.
         */
        public enum Category {
            WEAPON("Weapon"),
            AMMUNITION("Ammunition"),
            ARMOR("Armor"),
            CLOTHING("Clothing"),
            FOOD("Food"),
            WATER("Water"),
            MEDICAL("Medical"),
            ELECTRONICS("Electronics"),
            CARGO("Cargo"),
            PERSONAL("Personal"),
            OTHER("Other");

            private final String name;

            Category(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }

        /**
         * Creates a new item.
         *
         * @param name the item name
         * @param weight weight in kg
         * @param quantity number of items
         * @param category item category
         */
        public Item(String name, double weight, int quantity, Category category) {
            this.name = Objects.requireNonNull(name, "Name must not be null");
            this.weight = weight;
            this.quantity = quantity;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public double getWeight() {
            return weight;
        }

        public int getQuantity() {
            return quantity;
        }

        public Category getCategory() {
            return category;
        }

        public double getTotalWeight() {
            return weight * quantity;
        }

        @Override
        public String toString() {
            return String.format("%s x%d (%.1f kg)", name, quantity, getTotalWeight());
        }
    }

    private final TravellerCharacter character;
    private final List<Item> inventory;
    private final double baseMovement;

    /**
     * Creates a new encumbrance tracker for a character.
     *
     * @param character the traveller character
     */
    public Encumbrance(TravellerCharacter character) {
        this.character = Objects.requireNonNull(character, "Character must not be null");
        this.inventory = new ArrayList<>();
        double movement = 4.0 + (character.getAbility(AbilityScore.DEX) - 4) * 0.5;
        this.baseMovement = Math.max(2.0, Math.min(8.0, movement));
    }

    /**
     * Returns the character's carrying capacity in kg.
     * <p>
     * Capacity = Strength × 10 kg.
     * </p>
     */
    public double getCarryingCapacity() {
        return character.getAbility(AbilityScore.ST) * 10.0;
    }

    /**
     * Returns the current total weight carried in kg.
     */
    public double getCurrentWeight() {
        double total = 0;
        for (Item item : inventory) {
            total += item.getTotalWeight();
        }
        return total;
    }

    /**
     * Returns the remaining carrying capacity in kg.
     */
    public double getRemainingCapacity() {
        return getCarryingCapacity() - getCurrentWeight();
    }

    /**
     * Returns the current encumbrance level.
     */
    public Level getEncumbranceLevel() {
        double capacity = getCarryingCapacity();
        double current = getCurrentWeight();

        if (capacity <= 0) {
            return Level.HEAVY;
        }

        double ratio = current / capacity;

        if (ratio < 1.0 / 3.0) {
            return Level.NONE;
        } else if (ratio < 2.0 / 3.0) {
            return Level.LIGHT;
        } else if (ratio <= 1.0) {
            return Level.MEDIUM;
        } else {
            return Level.HEAVY;
        }
    }

    /**
     * Returns the movement penalty from encumbrance.
     */
    public int getMovementPenalty() {
        return getEncumbranceLevel().getMovementPenalty();
    }

    /**
     * Returns the combat penalty from encumbrance.
     */
    public int getCombatPenalty() {
        return getEncumbranceLevel().getCombatPenalty();
    }

    /**
     * Returns the effective movement factor.
     */
    public double getEffectiveMovement() {
        return Math.max(1.0, baseMovement + getMovementPenalty());
    }

    /**
     * Adds an item to the inventory.
     *
     * @return true if the item was added, false if it would exceed capacity by more than 50%
     */
    public boolean addItem(Item item) {
        if (getRemainingCapacity() - item.getTotalWeight() < -getCarryingCapacity() * 0.5) {
            return false;
        }

        // Check if item already exists
        for (Item existing : inventory) {
            if (existing.getName().equals(item.getName()) &&
                    existing.getCategory() == item.getCategory()) {
                // Update quantity instead of adding duplicate
                inventory.remove(existing);
                inventory.add(new Item(
                        existing.getName(),
                        existing.getWeight(),
                        existing.getQuantity() + item.getQuantity(),
                        existing.getCategory()));
                return true;
            }
        }

        inventory.add(item);
        return true;
    }

    /**
     * Removes an item from the inventory.
     *
     * @param name the item name
     * @param quantity number to remove
     * @return true if any items were removed
     */
    public boolean removeItem(String name, int quantity) {
        Iterator<Item> iterator = inventory.iterator();
        int remaining = quantity;

        while (iterator.hasNext() && remaining > 0) {
            Item item = iterator.next();
            if (item.getName().equalsIgnoreCase(name)) {
                if (item.getQuantity() <= remaining) {
                    remaining -= item.getQuantity();
                    iterator.remove();
                } else {
                    inventory.remove(item);
                    inventory.add(new Item(
                            item.getName(),
                            item.getWeight(),
                            item.getQuantity() - remaining,
                            item.getCategory()));
                    remaining = 0;
                }
            }
        }

        return quantity > remaining;
    }

    /**
     * Removes all items of a given name.
     */
    public void removeItem(String name) {
        inventory.removeIf(item -> item.getName().equalsIgnoreCase(name));
    }

    /**
     * Returns the inventory list (immutable).
     */
    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
    }

    /**
     * Returns items by category.
     */
    public Map<Item.Category, List<Item>> getItemsByCategory() {
        Map<Item.Category, List<Item>> byCategory = new EnumMap<>(Item.Category.class);
        for (Item.Category category : Item.Category.values()) {
            byCategory.put(category, new ArrayList<>());
        }

        for (Item item : inventory) {
            byCategory.get(item.getCategory()).add(item);
        }

        return byCategory;
    }

    /**
     * Returns the character.
     */
    public TravellerCharacter getCharacter() {
        return character;
    }

    /**
     * Returns the base movement factor.
     */
    public double getBaseMovement() {
        return baseMovement;
    }

    /**
     * Checks if the character is overloaded.
     */
    public boolean isOverloaded() {
        return getCurrentWeight() > getCarryingCapacity();
    }

    /**
     * Returns a summary of the encumbrance state.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(character.getName()).append(" Encumbrance:\n");
        sb.append("  Carrying Capacity: ").append(String.format("%.1f", getCarryingCapacity())).append(" kg\n");
        sb.append("  Current Weight: ").append(String.format("%.1f", getCurrentWeight())).append(" kg\n");
        sb.append("  Remaining: ").append(String.format("%.1f", getRemainingCapacity())).append(" kg\n");
        sb.append("  Level: ").append(getEncumbranceLevel().getName())
                .append(" (").append(getEncumbranceLevel().getDescription()).append(")\n");
        sb.append("  Movement: ").append(String.format("%.1f", getEffectiveMovement()))
                .append(" (base: ").append(String.format("%.1f", baseMovement)).append(")\n");

        if (!inventory.isEmpty()) {
            sb.append("  Inventory:\n");
            for (Item item : inventory) {
                sb.append("    - ").append(item).append("\n");
            }
        }

        return sb.toString();
    }
}
