package com.yisy.modules.doramcu.constitution;

import java.util.List;

/**
 * Article IV — The States, Citizenship, and New States.
 * <p>
 * Defines the relationship between the states and the federal government,
 * including full faith and credit, privileges and immunities, extradition,
 * admission of new states, and the guarantee of republican government.
 * </p>
 * <p>
 * Contains 4 sections covering: full faith and credit, state citizens and extradition,
 * new states, and republican government guarantee.
 * </p>
 */
public class ArticleIV extends Article {

    /**
     * Obligations among states under Section 1 (Full Faith and Credit).
     */
    public enum FullFaithAndCredit {
        PUBLIC_ACTS("Full Faith and Credit shall be given in each State to the public Acts, Records, and judicial Proceedings of every other State"),
        CONGRESS_REGULATION("Congress may by general Laws prescribe the Manner in which such Acts, Records and Proceedings shall be proved, and the Effect thereof");

        private final String description;

        FullFaithAndCredit(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Privileges and immunities and extradition rules under Section 2.
     */
    public enum PrivilegesAndImmunities {
        CITIZEN_PRIVILEGES("The Citizens of each State shall be entitled to all Privileges and Immunities of Citizens in the several States"),
        EXtradition("A Person charged in any State with Treason, Felony, or other Crime, who shall flee from Justice, and be found in another State, shall on Demand of the executive Authority of the State from which he fled, be delivered up"),
        FUGITIVE_SERVANTS("No Person held to Service or Labour in one State, under the Laws thereof, escaping into another, shall, in Consequence of any Law or Regulation therein, be discharged from such Service or Labour"),
        NO_RELIGIOUS_TEST("No religious Test shall ever be required as a Qualification to any Office or public Trust under the United States");

        private final String description;

        PrivilegesAndImmunities(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rules for admission of new states under Section 3.
     */
    public enum NewStateRules {
        NEW_STATES("New States may be admitted by the Congress into this Union"),
        NO_NEW_STATE_WITHIN_JURISDICTION("No new State shall be formed or erected within the Jurisdiction of any other State"),
        UNION_OF_STATES("No State shall be formed by the Junction of two or more States, or Parts of States, without the Consent of the Legislatures of the States concerned as well as of the Congress"),
        FEDERAL_PROPERTY("The Congress shall have Power to dispose of and make all needful Rules and Regulations respecting the Territory or other Property belonging to the United States");

        private final String description;

        NewStateRules(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Federal guarantees to states under Section 4.
     */
    public enum FederalGuarantee {
        REPUBLICAN_FORM("The United States shall guarantee to every State in this Union a Republican Form of Government"),
        PROTECTION_AGAINST_INVASION("protect each of them against Invasion"),
        SUPPRESSION_OF_DOMESTIC_VIOLENCE("on Application of the Legislature, or of the Executive (when the Legislature cannot be convened) against domestic Violence");

        private final String description;

        FederalGuarantee(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final int SECTION_COUNT = 4;

    public ArticleIV() {
        super(ArticleNumber.ARTICLE_IV, "The States, Citizenship, New States",
                "Defines the relationship between states, including full faith and credit, "
                + "privileges and immunities, extradition, admission of new states, "
                + "and the guarantee of republican government.");
    }

    @Override
    public int getSectionCount() {
        return SECTION_COUNT;
    }

    public List<FullFaithAndCredit> getFullFaithAndCreditRules() {
        return List.of(FullFaithAndCredit.values());
    }

    public List<PrivilegesAndImmunities> getPrivilegesAndImmunities() {
        return List.of(PrivilegesAndImmunities.values());
    }

    public List<NewStateRules> getNewStateRules() {
        return List.of(NewStateRules.values());
    }

    public List<FederalGuarantee> getFederalGuarantees() {
        return List.of(FederalGuarantee.values());
    }
}
