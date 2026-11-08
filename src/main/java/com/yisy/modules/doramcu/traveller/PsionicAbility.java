package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * A character's psionic profile from Classic Traveller B03: Worlds & Adventures.
 * <p>
 * Psionic ability is detected during character creation using the In (Intuition) score.
 * Characters with In 8+ may have psionic potential. The detection roll determines
 * the level of psionic strength, from latent to master.
 * </p>
 * <p>
 * Psionic powers cause fatigue when used. Each use adds fatigue points; when
 * fatigue equals or exceeds the character's In score, they cannot use powers
 * until they rest (1 hour of rest removes 1 fatigue point).
 * </p>
 * <p>
 * Derived from Classic Traveller Book 3: Worlds & Adventures (1977).
 * </p>
 */
public class PsionicAbility {

    /**
     * Levels of psionic detection strength.
     */
    public enum DetectionLevel {
        /**
         * No psionic ability detected.
         */
        NONE(0, "None"),

        /**
         * Latent psionic ability — very weak, may develop later.
         */
        LATENT(1, "Latent"),

        /**
         * Weak psionic ability — basic powers available.
         */
        WEAK(2, "Weak"),

        /**
         * Average psionic ability — reliable powers.
         */
        AVERAGE(3, "Average"),

        /**
         * Strong psionic ability — powerful effects.
         */
        STRONG(4, "Strong"),

        /**
         * Master psionic — rare and extremely powerful.
         */
        MASTER(5, "Master");

        private final int level;
        private final String displayName;

        DetectionLevel(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }

        public int getLevel() {
            return level;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private DetectionLevel detectionLevel;
    private final Map<PsionicDiscipline, Integer> disciplineLevels;
    private int fatigue;
    private final int maxFatigue;

    /**
     * Creates a new psionic ability profile.
     *
     * @param inScore the character's In (Intuition) score, used for max fatigue
     */
    public PsionicAbility(int inScore) {
        this.detectionLevel = DetectionLevel.NONE;
        this.disciplineLevels = new EnumMap<>(PsionicDiscipline.class);
        this.fatigue = 0;
        this.maxFatigue = Math.max(inScore, 2);
    }

    /**
     * Performs psionic detection for a character during character creation.
     * <p>
     * Roll 2D6 and compare against the character's In score:
     * - Roll &lt;= In: Psionic detected (level based on how much lower)
     * - Roll &gt; In: No psionic ability
     * </p>
     *
     * @param inScore the character's In (Intuition) score
     * @param roll the 2D6 detection roll result
     * @return true if psionic ability was detected
     */
    public boolean detectPsionic(int inScore, int roll) {
        if (inScore < 8) {
            this.detectionLevel = DetectionLevel.NONE;
            return false;
        }
        if (roll <= inScore - 4) {
            this.detectionLevel = DetectionLevel.MASTER;
        } else if (roll <= inScore - 2) {
            this.detectionLevel = DetectionLevel.STRONG;
        } else if (roll <= inScore) {
            this.detectionLevel = DetectionLevel.AVERAGE;
        } else if (roll <= inScore + 2) {
            this.detectionLevel = DetectionLevel.WEAK;
        } else if (roll <= inScore + 4) {
            this.detectionLevel = DetectionLevel.LATENT;
        } else {
            this.detectionLevel = DetectionLevel.NONE;
        }
        return this.detectionLevel != DetectionLevel.NONE;
    }

    /**
     * Adds a psionic discipline to the character's abilities.
     *
     * @param discipline the discipline
     * @param level the proficiency level (1-5)
     */
    public void addDiscipline(PsionicDiscipline discipline, int level) {
        if (discipline == null) {
            throw new IllegalArgumentException("Discipline must not be null");
        }
        if (level < 1 || level > 5) {
            throw new IllegalArgumentException("Discipline level must be 1-5, got: " + level);
        }
        this.disciplineLevels.put(discipline, level);
    }

    /**
     * Returns the level of a specific discipline.
     *
     * @param discipline the discipline
     * @return the level (0 if not learned)
     */
    public int getDisciplineLevel(PsionicDiscipline discipline) {
        return disciplineLevels.getOrDefault(discipline, 0);
    }

    /**
     * Returns all learned disciplines with their levels.
     *
     * @return unmodifiable map of discipline to level
     */
    public Map<PsionicDiscipline, Integer> getDisciplines() {
        return Collections.unmodifiableMap(disciplineLevels);
    }

    /**
     * Returns the detection level of this character's psionic ability.
     */
    public DetectionLevel getDetectionLevel() {
        return detectionLevel;
    }

    /**
     * Sets the detection level (used when loading saved characters).
     */
    public void setDetectionLevel(DetectionLevel level) {
        this.detectionLevel = Objects.requireNonNull(level, "Level must not be null");
    }

    /**
     * Returns the current fatigue points.
     */
    public int getFatigue() {
        return fatigue;
    }

    /**
     * Returns the maximum fatigue before powers are blocked.
     */
    public int getMaxFatigue() {
        return maxFatigue;
    }

    /**
     * Adds fatigue from using a psionic power.
     *
     * @param amount the fatigue points to add
     * @return true if the character can still use powers after this
     */
    public boolean addFatigue(int amount) {
        fatigue += amount;
        return fatigue < maxFatigue;
    }

    /**
     * Removes fatigue from resting.
     *
     * @param amount the fatigue points to remove
     */
    public void rest(int amount) {
        fatigue = Math.max(0, fatigue - amount);
    }

    /**
     * Checks if the character can currently use psionic powers.
     *
     * @return true if not fatigued and has detected ability
     */
    public boolean canUsePowers() {
        return detectionLevel != DetectionLevel.NONE && fatigue < maxFatigue;
    }

    /**
     * Returns a summary of the psionic ability.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Psionic: ").append(detectionLevel.getDisplayName());
        if (!disciplineLevels.isEmpty()) {
            sb.append(" | Disciplines: ");
            boolean first = true;
            for (Map.Entry<PsionicDiscipline, Integer> entry : disciplineLevels.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey().getDisplayName()).append("=").append(entry.getValue());
                first = false;
            }
        }
        sb.append(" | Fatigue: ").append(fatigue).append("/").append(maxFatigue);
        return sb.toString();
    }
}
