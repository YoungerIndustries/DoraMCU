package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Survival rules from Classic Traveller Book 1: Characters & Combat (1981).
 * <p>
 * Survival tracks a character's vital needs: food, water, shelter, and health.
 * The system models environmental hazards such as extreme temperatures,
 * altitude, radiation, and disease.
 * </p>
 * <p>
 * Key mechanics:
 * </p>
 * <ul>
 *   <li><b>Food</b> — a character needs 1 ration per day. Starvation causes damage.</li>
 *   <li><b>Water</b> — a character needs 1 liter per day. Dehydration causes damage.</li>
 *   <li><b>Shelter</b> — protection from environmental hazards.</li>
 *   <li><b>Temperature</b> — extreme heat or cold causes damage without proper gear.</li>
 *   <li><b>Disease</b> — unsanitary conditions can cause illness.</li>
 * </ul>
 * <p>
 * Derived from Classic Traveller Book 1, Chapter 4: Combat.
 * </p>
 */
public class Survival {

    /**
     * Environmental temperature categories.
     */
    public enum Temperature {
        /**
         * Extreme cold — below -40°C. Severe damage without protection.
         */
        EXTREME_COLD("Extreme Cold", -40, -20, -3, "Severe cold — heavy damage without protection"),

        /**
         * Very cold — -20°C to -10°C. Damage without protection.
         */
        VERY_COLD("Very Cold", -20, -10, -2, "Very cold — damage without protection"),

        /**
         * Cold — -10°C to 0°C. Minor damage without protection.
         */
        COLD("Cold", -10, 0, -1, "Cold — minor damage without protection"),

        /**
         * Cool — 0°C to 15°C. Comfortable with light clothing.
         */
        COOL("Cool", 0, 15, 0, "Cool — comfortable with light clothing"),

        /**
         * Temperate — 15°C to 25°C. Ideal conditions.
         */
        TEMPERATE("Temperate", 15, 25, 0, "Temperate — ideal conditions"),

        /**
         * Warm — 25°C to 35°C. Slightly uncomfortable.
         */
        WARM("Warm", 25, 35, 0, "Warm — slightly uncomfortable"),

        /**
         * Hot — 35°C to 45°C. Minor damage without protection.
         */
        HOT("Hot", 35, 45, -1, "Hot — minor damage without protection"),

        /**
         * Very hot — 45°C to 55°C. Damage without protection.
         */
        VERY_HOT("Very Hot", 45, 55, -2, "Very hot — damage without protection"),

        /**
         * Extreme heat — above 55°C. Severe damage without protection.
         */
        EXTREME_HEAT("Extreme Heat", 55, 100, -3, "Extreme heat — severe damage without protection");

        private final String name;
        private final int minTemp;
        private final int maxTemp;
        private final int damagePerDay;
        private final String description;

        Temperature(String name, int minTemp, int maxTemp, int damagePerDay, String description) {
            this.name = name;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.damagePerDay = damagePerDay;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public int getMinTemp() {
            return minTemp;
        }

        public int getMaxTemp() {
            return maxTemp;
        }

        public int getDamagePerDay() {
            return damagePerDay;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Determines the temperature category for a given temperature.
         */
        public static Temperature fromCelsius(int celsius) {
            for (Temperature temp : values()) {
                if (celsius >= temp.minTemp && celsius < temp.maxTemp) {
                    return temp;
                }
            }
            if (celsius >= 100) {
                return EXTREME_HEAT;
            }
            return EXTREME_COLD;
        }
    }

    /**
     * Types of shelter available.
     */
    public enum ShelterType {
        /**
         * No shelter — fully exposed to elements.
         */
        NONE("No Shelter", 0, "Fully exposed to elements"),

        /**
         * Tarp or light cover — minimal protection.
         */
        TARP("Tarp", 1, "Minimal protection from rain/sun"),

        /**
         * Tent — basic shelter.
         */
        TENT("Tent", 2, "Basic shelter from elements"),

        /**
         * Building — standard shelter.
         */
        BUILDING("Building", 3, "Standard shelter"),

        /**
         * Fortified building — good protection.
         */
        FORTIFIED("Fortified Building", 4, "Good protection from elements"),

        /**
         * Space habitat — full environmental control.
         */
        SPACE_HABITAT("Space Habitat", 5, "Full environmental control");

        private final String name;
        private final int protectionLevel;
        private final String description;

        ShelterType(String name, int protectionLevel, String description) {
            this.name = name;
            this.protectionLevel = protectionLevel;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public int getProtectionLevel() {
            return protectionLevel;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Disease types that can affect characters.
     */
    public enum Disease {
        /**
         * Common cold — minor illness.
         */
        COMMON_COLD("Common Cold", 1, 3, -1, "Minor illness — -1 to all rolls"),

        /**
         * Fever — moderate illness.
         */
        FEVER("Fever", 2, 7, -2, "Moderate illness — -2 to all rolls"),

        /**
         * Dysentery — gastrointestinal illness.
         */
        DYSENTERY("Dysentery", 3, 10, -2, "Gastrointestinal — -2 to all rolls, extra water loss"),

        /**
         * Plague — severe illness.
         */
        PLAGUE("Plague", 4, 14, -3, "Severe illness — -3 to all rolls, daily damage"),

        /**
         * Radiation sickness — from radiation exposure.
         */
        RADIATION_SICKNESS("Radiation Sickness", 5, 21, -4, "Radiation — -4 to all rolls, daily damage");

        private final String name;
        private final int severity;
        private final int durationDays;
        private final int penalty;
        private final String description;

        Disease(String name, int severity, int durationDays, int penalty, String description) {
            this.name = name;
            this.severity = severity;
            this.durationDays = durationDays;
            this.penalty = penalty;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public int getSeverity() {
            return severity;
        }

        public int getDurationDays() {
            return durationDays;
        }

        public int getPenalty() {
            return penalty;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * A disease instance affecting a character.
     */
    public static class DiseaseInstance {
        private final Disease disease;
        private final int daysRemaining;
        private final int daysTotal;

        DiseaseInstance(Disease disease, int daysRemaining, int daysTotal) {
            this.disease = disease;
            this.daysRemaining = daysRemaining;
            this.daysTotal = daysTotal;
        }

        public Disease getDisease() {
            return disease;
        }

        public int getDaysRemaining() {
            return daysRemaining;
        }

        public int getDaysTotal() {
            return daysTotal;
        }

        public String getDescription() {
            return String.format("%s (%d/%d days)", disease.getName(), daysRemaining, daysTotal);
        }
    }

    private final TravellerCharacter character;
    private int foodRations;
    private int waterLiters;
    private int daysSinceLastMeal;
    private int daysSinceLastDrink;
    private Temperature currentTemperature;
    private ShelterType currentShelter;
    private final List<DiseaseInstance> diseases;
    private int healthPoints;
    private int maxHealthPoints;
    private final List<String> eventLog;

    /**
     * Creates a new survival tracker for a character.
     *
     * @param character the traveller character
     */
    public Survival(TravellerCharacter character) {
        this.character = Objects.requireNonNull(character, "Character must not be null");
        this.foodRations = 5;
        this.waterLiters = 5;
        this.daysSinceLastMeal = 0;
        this.daysSinceLastDrink = 0;
        this.currentTemperature = Temperature.TEMPERATE;
        this.currentShelter = ShelterType.BUILDING;
        this.diseases = new ArrayList<>();
        this.maxHealthPoints = character.getAbility(AbilityScore.END) * 2;
        this.healthPoints = maxHealthPoints;
        this.eventLog = new ArrayList<>();

        eventLog.add("Survival tracking started for " + character.getName());
    }

    /**
     * Advances the survival state by one day.
     * <p>
     * Checks food, water, temperature, and disease effects.
     * </p>
     *
     * @return a summary of what happened during the day
     */
    public String advanceDay() {
        StringBuilder events = new StringBuilder();
        events.append("Day ").append(daysSinceLastMeal).append(" for ").append(character.getName()).append(":\n");

        // Food check
        if (foodRations > 0) {
            foodRations--;
            daysSinceLastMeal = 0;
            events.append("  Ate a ration. Remaining: ").append(foodRations).append("\n");
        } else {
            daysSinceLastMeal++;
            if (daysSinceLastMeal >= 3) {
                int starvationDamage = daysSinceLastMeal - 2;
                healthPoints -= starvationDamage;
                events.append("  STARVATION! Day ").append(daysSinceLastMeal)
                        .append(" without food. Damage: ").append(starvationDamage).append("\n");
            } else if (daysSinceLastMeal >= 2) {
                events.append("  Hungry — no food for ").append(daysSinceLastMeal).append(" days\n");
            }
        }

        // Water check
        if (waterLiters > 0) {
            waterLiters--;
            daysSinceLastDrink = 0;
            events.append("  Drank water. Remaining: ").append(waterLiters).append(" liters\n");
        } else {
            daysSinceLastDrink++;
            if (daysSinceLastDrink >= 3) {
                int dehydrationDamage = daysSinceLastDrink - 2;
                healthPoints -= dehydrationDamage;
                events.append("  DEHYDRATION! Day ").append(daysSinceLastDrink)
                        .append(" without water. Damage: ").append(dehydrationDamage).append("\n");
            } else if (daysSinceLastDrink >= 2) {
                events.append("  Thirsty — no water for ").append(daysSinceLastDrink).append(" days\n");
            }
        }

        // Temperature check
        int tempDamage = currentTemperature.getDamagePerDay();
        if (tempDamage < 0) {
            int shelterProtection = currentShelter.getProtectionLevel();
            int reducedDamage = Math.max(0, tempDamage + shelterProtection);
            if (reducedDamage > 0) {
                healthPoints -= reducedDamage;
                events.append("  ").append(currentTemperature.getName())
                        .append(" damage: ").append(reducedDamage)
                        .append(" (shelter: ").append(currentShelter.getName()).append(")\n");
            } else {
                events.append("  ").append(currentTemperature.getName())
                        .append(" — shelter provides full protection\n");
            }
        }

        // Disease progression
        Random random = new Random();
        Iterator<DiseaseInstance> iterator = diseases.iterator();
        while (iterator.hasNext()) {
            DiseaseInstance disease = iterator.next();
            int newDays = disease.getDaysRemaining() - 1;
            if (newDays <= 0) {
                iterator.remove();
                events.append("  Recovered from ").append(disease.getDisease().getName()).append("\n");
            } else {
                // Replace with updated instance
                iterator.remove();
                diseases.add(new DiseaseInstance(disease.getDisease(), newDays, disease.getDaysTotal()));
                Disease d = disease.getDisease();
                if (d == Disease.PLAGUE || d == Disease.RADIATION_SICKNESS) {
                    healthPoints -= 1;
                    events.append("  ").append(disease.getDescription())
                            .append(" — daily damage\n");
                } else {
                    events.append("  ").append(disease.getDescription()).append("\n");
                }
            }
        }

        // Check for new disease
        if (currentShelter == ShelterType.NONE || currentShelter == ShelterType.TARP) {
            int diseaseChance = 10; // 10% chance per day in poor conditions
            if (random.nextInt(100) < diseaseChance) {
                Disease newDisease = randomDisease();
                diseases.add(new DiseaseInstance(newDisease, newDisease.getDurationDays(), newDisease.getDurationDays()));
                events.append("  Contracted ").append(newDisease.getName())
                        .append(" — ").append(newDisease.getDescription()).append("\n");
            }
        }

        // Check health
        if (healthPoints <= 0) {
            events.append("  ").append(character.getName()).append(" has died!\n");
        }

        eventLog.add(events.toString());
        return events.toString();
    }

    /**
     * Adds food rations.
     */
    public void addFood(int rations) {
        foodRations += rations;
        eventLog.add("Added " + rations + " food rations. Total: " + foodRations);
    }

    /**
     * Adds water.
     */
    public void addWater(int liters) {
        waterLiters += liters;
        eventLog.add("Added " + liters + " liters of water. Total: " + waterLiters);
    }

    /**
     * Sets the current temperature.
     */
    public void setTemperature(Temperature temperature) {
        this.currentTemperature = temperature;
        eventLog.add("Temperature changed to " + temperature.getName());
    }

    /**
     * Sets the current temperature from Celsius.
     */
    public void setTemperatureCelsius(int celsius) {
        this.currentTemperature = Temperature.fromCelsius(celsius);
        eventLog.add("Temperature set to " + celsius + "°C (" + currentTemperature.getName() + ")");
    }

    /**
     * Sets the current shelter.
     */
    public void setShelter(ShelterType shelter) {
        this.currentShelter = shelter;
        eventLog.add("Shelter changed to " + shelter.getName());
    }

    /**
     * Adds a disease to the character.
     */
    public void addDisease(Disease disease) {
        diseases.add(new DiseaseInstance(disease, disease.getDurationDays(), disease.getDurationDays()));
        eventLog.add(character.getName() + " contracted " + disease.getName());
    }

    /**
     * Returns the current health points.
     */
    public int getHealthPoints() {
        return healthPoints;
    }

    /**
     * Returns the maximum health points.
     */
    public int getMaxHealthPoints() {
        return maxHealthPoints;
    }

    /**
     * Returns the remaining food rations.
     */
    public int getFoodRations() {
        return foodRations;
    }

    /**
     * Returns the remaining water in liters.
     */
    public int getWaterLiters() {
        return waterLiters;
    }

    /**
     * Returns the days since last meal.
     */
    public int getDaysSinceLastMeal() {
        return daysSinceLastMeal;
    }

    /**
     * Returns the days since last drink.
     */
    public int getDaysSinceLastDrink() {
        return daysSinceLastDrink;
    }

    /**
     * Returns the current temperature.
     */
    public Temperature getCurrentTemperature() {
        return currentTemperature;
    }

    /**
     * Returns the current shelter.
     */
    public ShelterType getCurrentShelter() {
        return currentShelter;
    }

    /**
     * Returns the active diseases (immutable).
     */
    public List<DiseaseInstance> getDiseases() {
        return Collections.unmodifiableList(diseases);
    }

    /**
     * Returns the total penalty from diseases.
     */
    public int getDiseasePenalty() {
        int total = 0;
        for (DiseaseInstance disease : diseases) {
            total += disease.getDisease().getPenalty();
        }
        return total;
    }

    /**
     * Checks if the character is alive.
     */
    public boolean isAlive() {
        return healthPoints > 0;
    }

    /**
     * Returns the character.
     */
    public TravellerCharacter getCharacter() {
        return character;
    }

    /**
     * Returns the event log (immutable).
     */
    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    /**
     * Returns a summary of the survival state.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(character.getName()).append(" Survival Status:\n");
        sb.append("  Health: ").append(healthPoints).append("/").append(maxHealthPoints).append("\n");
        sb.append("  Food: ").append(foodRations).append(" rations\n");
        sb.append("  Water: ").append(waterLiters).append(" liters\n");
        sb.append("  Days since meal: ").append(daysSinceLastMeal).append("\n");
        sb.append("  Days since drink: ").append(daysSinceLastDrink).append("\n");
        sb.append("  Temperature: ").append(currentTemperature.getName()).append("\n");
        sb.append("  Shelter: ").append(currentShelter.getName()).append("\n");

        if (!diseases.isEmpty()) {
            sb.append("  Diseases:\n");
            for (DiseaseInstance disease : diseases) {
                sb.append("    - ").append(disease.getDescription()).append("\n");
            }
            sb.append("  Total disease penalty: ").append(getDiseasePenalty()).append("\n");
        }

        sb.append("  Status: ").append(isAlive() ? "ALIVE" : "DEAD");

        return sb.toString();
    }

    /**
     * Picks a random disease based on severity.
     */
    private Disease randomDisease() {
        Disease[] diseases = Disease.values();
        Random random = new Random();
        // Weight toward less severe diseases
        int roll = random.nextInt(100);
        if (roll < 40) {
            return Disease.COMMON_COLD;
        } else if (roll < 70) {
            return Disease.FEVER;
        } else if (roll < 85) {
            return Disease.DYSENTERY;
        } else if (roll < 95) {
            return Disease.PLAGUE;
        } else {
            return Disease.RADIATION_SICKNESS;
        }
    }
}
