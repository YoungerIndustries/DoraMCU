package com.yisy.modules.doramcu.constitution;

import java.util.List;

/**
 * Article VII — Ratification.
 * <p>
 * Specifies the conditions under which the Constitution would go into effect:
 * ratification by nine of the thirteen original states through conventions.
 * This was the final article and the shortest, containing only one section.
 * </p>
 * <p>
 * Contains 1 section covering the ratification requirement.
 * </p>
 */
public class ArticleVII extends Article {

    /**
     * Ratification requirements under the single section.
     */
    public enum RatificationRequirement {
        NINE_STATES("The Ratification of the Conventions of nine States, shall be sufficient for the Establishment of this Constitution between the States so ratifying the Same"),
        CONVENTION_METHOD("Ratification by state conventions rather than state legislatures");

        private final String description;

        RatificationRequirement(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final int SECTION_COUNT = 1;

    public ArticleVII() {
        super(ArticleNumber.ARTICLE_VII, "Ratification",
                "Specifies that ratification by nine of the thirteen original states "
                + "through conventions is sufficient for the Constitution to take effect.");
    }

    @Override
    public int getSectionCount() {
        return SECTION_COUNT;
    }

    /**
     * Returns ratification requirements.
     */
    public List<RatificationRequirement> getRatificationRequirements() {
        return List.of(RatificationRequirement.values());
    }

    /**
     * Number of states required for initial ratification: 9.
     */
    public int getRequiredStatesForRatification() {
        return 9;
    }

    /**
     * Total number of original states: 13.
     */
    public int getTotalOriginalStates() {
        return 13;
    }
}
