package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

import java.util.List;
import java.util.Map;

/**
 * Response from a career progression action.
 * <p>
 * Contains the character's updated experience points, available upgrades,
 * and the result of the progression action.
 * </p>
 */
public class CareerProgressionResponse {

    /** Character's current experience points. */
    private int experiencePoints;

    /** Experience points needed for the next rank advancement. */
    private int xpForNextRank;

    /** Whether the character can advance rank. */
    private boolean canAdvanceRank;

    /** Whether the character can increase a discipline. */
    private boolean canIncreaseDiscipline;

    /** Whether the character can add a new focus. */
    private boolean canAddFocus;

    /** List of available upgrades the character can afford. */
    private List<CareerUpgrade> availableUpgrades;

    /** Description of the result. */
    private String description;

    /**
     * Represents a career upgrade option.
     */
    public static class CareerUpgrade {
        /** Type of upgrade. */
        private UpgradeType type;

        /** Human-readable title. */
        private String title;

        /** Description of what the upgrade does. */
        private String description;

        /** Experience point cost. */
        private int xpCost;

        /** Target discipline (for discipline/focus upgrades). */
        private CaptainLogCharacter.Discipline discipline;

        /** Focus name (for focus upgrades). */
        private String focusName;

        /** Next rank (for rank advancement). */
        private CaptainLogCharacter.Rank nextRank;

        public enum UpgradeType {
            RANK_ADVANCEMENT, DISCIPLINE_INCREASE, ADD_FOCUS, ADD_VALUE, RETIRE_VALUE
        }

        public UpgradeType getType() {
            return type;
        }

        public void setType(UpgradeType type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getXpCost() {
            return xpCost;
        }

        public void setXpCost(int xpCost) {
            this.xpCost = xpCost;
        }

        public CaptainLogCharacter.Discipline getDiscipline() {
            return discipline;
        }

        public void setDiscipline(CaptainLogCharacter.Discipline discipline) {
            this.discipline = discipline;
        }

        public String getFocusName() {
            return focusName;
        }

        public void setFocusName(String focusName) {
            this.focusName = focusName;
        }

        public CaptainLogCharacter.Rank getNextRank() {
            return nextRank;
        }

        public void setNextRank(CaptainLogCharacter.Rank nextRank) {
            this.nextRank = nextRank;
        }
    }

    // --- Getters and Setters ---

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public int getXpForNextRank() {
        return xpForNextRank;
    }

    public void setXpForNextRank(int xpForNextRank) {
        this.xpForNextRank = xpForNextRank;
    }

    public boolean isCanAdvanceRank() {
        return canAdvanceRank;
    }

    public void setCanAdvanceRank(boolean canAdvanceRank) {
        this.canAdvanceRank = canAdvanceRank;
    }

    public boolean isCanIncreaseDiscipline() {
        return canIncreaseDiscipline;
    }

    public void setCanIncreaseDiscipline(boolean canIncreaseDiscipline) {
        this.canIncreaseDiscipline = canIncreaseDiscipline;
    }

    public boolean isCanAddFocus() {
        return canAddFocus;
    }

    public void setCanAddFocus(boolean canAddFocus) {
        this.canAddFocus = canAddFocus;
    }

    public List<CareerUpgrade> getAvailableUpgrades() {
        return availableUpgrades;
    }

    public void setAvailableUpgrades(List<CareerUpgrade> availableUpgrades) {
        this.availableUpgrades = availableUpgrades;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
