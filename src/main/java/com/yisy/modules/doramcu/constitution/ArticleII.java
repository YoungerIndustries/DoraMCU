package com.yisy.modules.doramcu.constitution;

import java.util.List;

/**
 * Article II — The Executive Branch.
 * <p>
 * Establishes the executive branch, vests executive power in the President,
 * and defines the powers, duties, and qualifications of the Presidency,
 * including the Vice President, the Cabinet, and the Electoral College.
 * </p>
 * <p>
 * Contains 4 sections covering: the President, executive powers,
 * duties and obligations, and disqualification from office.
 * </p>
 */
public class ArticleII extends Article {

    /**
     * Executive powers granted to the President under Article II, Section 2.
     */
    public enum ExecutivePower {
        COMMANDER_IN_CHIEF("Commander in Chief of the Army and Navy of the United States"),
        REPRIEVES("Grant Reprieves and Pardons for Offenses against the United States, except in Cases of Impeachment"),
        TREATIES("make Treaties, provided two thirds of the Senators present concur"),
        APPOINT_AMBASSADORS("nominate, and by and with the Advice and Consent of the Senate, shall appoint Ambassadors"),
        APPOINT_JUDGES("nominate, and by and with the Advice and Consent of the Senate, shall appoint Judges of the supreme Court"),
        APPOINT_OFFICERS("appoint all other Officers of the United States"),
        OPINION("he may require the Opinion, in writing, of the principal Officer in each of the executive Departments"),
        REPRIMAND("he shall have Power to Reprimand all Officers of the Law, throughout the executive Department");

        private final String description;

        ExecutivePower(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Presidential duties under Article II, Section 3.
     */
    public enum PresidentialDuty {
        STATE_OF_UNION("inform the Congress of the State of the Union"),
        RECOMMEND_MEASURES("recommend to their Consideration such Measures as he shall judge necessary and expedient"),
        CONVENE_CONGRESS("convoke both Houses, or either of them, on extraordinary Occasions"),
        ADJOURNMENT("adjourn them to such Time as he shall think necessary"),
        RECEIVE_AMBASSADORS("receive Ambassadors and other public Ministers"),
        FAITHFUL_EXECUTION("take Care that the Laws be faithfully executed"),
        COMMISSION_OFFICERS("commission all the Officers of the United States");

        private final String description;

        PresidentialDuty(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final int SECTION_COUNT = 4;

    public ArticleII() {
        super(ArticleNumber.ARTICLE_II, "The Executive Branch",
                "Establishes the Presidency, defines executive powers, qualifications, "
                + "the Electoral College, and the duties of the President.");
    }

    @Override
    public int getSectionCount() {
        return SECTION_COUNT;
    }

    /**
     * Returns all executive powers under Section 2.
     */
    public List<ExecutivePower> getExecutivePowers() {
        return List.of(ExecutivePower.values());
    }

    /**
     * Returns all presidential duties under Section 3.
     */
    public List<PresidentialDuty> getPresidentialDuties() {
        return List.of(PresidentialDuty.values());
    }

    /**
     * Minimum age requirement for the President: 35 years.
     */
    public int getPresidentMinimumAge() {
        return 35;
    }

    /**
     * Minimum citizenship requirement for the President: natural-born citizen.
     */
    public boolean requiresNaturalBornCitizen() {
        return true;
    }

    /**
     * Minimum residency requirement for the President: 14 years.
     */
    public int getPresidentResidencyYears() {
        return 14;
    }

    /**
     * Presidential term length in years.
     */
    public int getPresidentialTermYears() {
        return 4;
    }
}
