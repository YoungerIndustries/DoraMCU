package com.yisy.modules.doramcu.constitution;

import java.util.List;

/**
 * Article V — Amendment Process.
 * <p>
 * Describes the procedure for amending the Constitution, providing two methods
 * for proposing amendments and two methods for ratification. This article
 * ensures the Constitution can evolve while maintaining stability.
 * </p>
 * <p>
 * Contains 1 section covering the amendment process.
 * </p>
 */
public class ArticleV extends Article {

    /**
     * Methods for proposing amendments.
     */
    public enum ProposalMethod {
        CONGRESSIONAL_VOTE("Two thirds of both Houses of Congress propose an amendment"),
        CONVENTION_REQUEST("Two thirds of state legislatures call for a convention to propose amendments");

        private final String description;

        ProposalMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Methods for ratifying proposed amendments.
     */
    public enum RatificationMethod {
        STATE_LEGISLATURES("Three fourths of state legislatures ratify the amendment"),
        STATE_CONVENTIONS("Three fourths of state conventions ratify the amendment");

        private final String description;

        RatificationMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Constraints on the amendment process.
     */
    public enum AmendmentConstraint {
        SLAVE_TRADE_PROTECTION("No Amendment shall prevent the Migration or Importation of Persons which any State may think proper to admit prior to the Year 1808"),
        DIRECT_TAX_PROTECTION("No State, without its Consent, shall be deprived of its equal Suffrage in the Senate");

        private final String description;

        AmendmentConstraint(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final int SECTION_COUNT = 1;

    public ArticleV() {
        super(ArticleNumber.ARTICLE_V, "Amendment Process",
                "Describes the procedure for amending the Constitution, including proposal "
                + "and ratification methods, ensuring the document can evolve while maintaining stability.");
    }

    @Override
    public int getSectionCount() {
        return SECTION_COUNT;
    }

    /**
     * Returns methods for proposing amendments.
     */
    public List<ProposalMethod> getProposalMethods() {
        return List.of(ProposalMethod.values());
    }

    /**
     * Returns methods for ratifying amendments.
     */
    public List<RatificationMethod> getRatificationMethods() {
        return List.of(RatificationMethod.values());
    }

    /**
     * Returns constraints on the amendment process.
     */
    public List<AmendmentConstraint> getAmendmentConstraints() {
        return List.of(AmendmentConstraint.values());
    }

    /**
     * Proportion of Congress required to propose an amendment: 2/3.
     */
    public double getCongressProposalFraction() {
        return 2.0 / 3.0;
    }

    /**
     * Proportion of states required to ratify an amendment: 3/4.
     */
    public double getStateRatificationFraction() {
        return 3.0 / 4.0;
    }
}
