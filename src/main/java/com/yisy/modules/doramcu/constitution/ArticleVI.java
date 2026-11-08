package com.yisy.modules.doramcu.constitution;

import java.util.List;

/**
 * Article VI — Debts, Supremacy, and Oaths.
 * <p>
 * Establishes the Constitution as the supreme law of the land, addresses
 * debts incurred under the Articles of Confederation, and requires all
 * government officials to take an oath supporting the Constitution.
 * </p>
 * <p>
 * Contains 3 sections covering: debts, supremacy clause, and oaths of office.
 * </p>
 */
public class ArticleVI extends Article {

    /**
     * Provisions regarding debts under Section 1.
     */
    public enum DebtProvision {
        CONFEDERATION_DEBTS("All Debts contracted and Engagements entered into, before the Adoption of this Constitution, shall be as valid against the United States under this Constitution, as under the Confederation");

        private final String description;

        DebtProvision(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Supremacy Clause provisions under Section 2.
     */
    public enum SupremacyClause {
        CONSTITUTION_SUPREME("This Constitution, and the Laws of the United States which shall be made in Pursuance thereof; and all Treaties made, or which shall be made, under the Authority of the United States, shall be the supreme Law of the Land"),
        STATE_OFFICERS_BOUND("the Judges in every State shall be bound thereby, any Thing in the Constitution or Laws of any State to the Contrary notwithstanding"),
        NO_RELIGIOUS_TEST("Senators and Representatives, and the Members of the several State Legislatures, and all executive and judicial Officers, shall be bound by Oath or Affirmation, to support this Constitution");

        private final String description;

        SupremacyClause(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Oath requirements under Section 3.
     */
    public enum OathRequirement {
        SUPPORT_CONSTITUTION("All legislative and executive, as well as judicial Officers, of the United States, as well as of the several States, shall be bound by Oath or Affirmation, to support this Constitution"),
        NO_RELIGIOUS_TEST("No religious Test shall ever be required as a Qualification to any Office or public Trust under the United States");

        private final String description;

        OathRequirement(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final int SECTION_COUNT = 3;

    public ArticleVI() {
        super(ArticleNumber.ARTICLE_VI, "Debts, Supremacy, Oaths",
                "Establishes the Constitution as the supreme law of the land, "
                + "addresses debts from the Articles of Confederation, "
                + "and requires government officials to take an oath supporting the Constitution.");
    }

    @Override
    public int getSectionCount() {
        return SECTION_COUNT;
    }

    /**
     * Returns debt provisions under Section 1.
     */
    public List<DebtProvision> getDebtProvisions() {
        return List.of(DebtProvision.values());
    }

    /**
     * Returns supremacy clause provisions under Section 2.
     */
    public List<SupremacyClause> getSupremacyClauses() {
        return List.of(SupremacyClause.values());
    }

    /**
     * Returns oath requirements under Section 3.
     */
    public List<OathRequirement> getOathRequirements() {
        return List.of(OathRequirement.values());
    }
}
