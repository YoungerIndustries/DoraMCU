package com.yisy.modules.doramcu.constitution;

import java.util.List;

/**
 * Article I — The Legislative Branch.
 * <p>
 * Establishes Congress as a bicameral legislature consisting of the Senate
 * and the House of Representatives. Defines the powers of Congress,
 * qualifications for members, and limits on both federal and state authority.
 * </p>
 * <p>
 * Contains 10 sections covering: the Legislature, the House, the Senate,
 * elections, membership rules, compensation, revenue bills and legislative process,
 * powers of Congress, limits on Congress, and powers prohibited to states.
 * </p>
 */
public class ArticleI extends Article {

    /**
     * Enumerated powers granted to Congress under Article I, Section 8.
     */
    public enum CongressionalPower {
        TAXATION("Lay and collect Taxes, Duties, Imposts and Excises"),
        BORROW_MONEY("Borrow money on the credit of the United States"),
        REGULATE_COMMERCE("Regulate Commerce with foreign Nations and among the several States"),
        NATURALIZATION("Establish an uniform Rule of Naturalization"),
        COINAGE("Coin Money, regulate the Value thereof"),
        POST_OFFICES("Establish Post Offices and post Roads"),
        PATENTS("Promote the Progress of Science and useful Arts"),
        COURTS("Constitute Tribunals inferior to the supreme Court"),
        MARITIME_LAWS("Define and punish Piracies and Felonies committed on the high Seas"),
        DECLARE_WAR("Declare War, grant Letters of Marque and Reprisal"),
        RAISE_ARMY("Raise and support Armies"),
        PROVIDE_NAVY("Provide and maintain a Navy"),
        MILITIA("Provide for calling forth the Militia"),
        DISTRICT_OF_COLUMBIA("Exercise exclusive Legislation over the District for the Seat of Government"),
        NECESSARY_AND_PROPER("Make all Laws which shall be necessary and proper for carrying into Execution the foregoing Powers");

        private final String description;

        CongressionalPower(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Limits on Congressional power under Article I, Section 9.
     */
    public enum CongressionalLimit {
        SLAVE_TRADE("The Migration or Importation of Persons which any of the States now existing shall think proper to admit, shall not be prohibited prior to 1808"),
        HABEAS_CORPUS("The Privilege of the Writ of Habeas Corpus shall not be suspended, unless when in Cases of Rebellion or Invasion the public Safety may require it"),
        BILL_OF_ATTAINDER("No Bill of Attainder or ex post facto Law shall be passed"),
        CAPITATION_TAXES("No Capitation, or other direct, Tax shall be laid, unless in Proportion to the Census"),
        EXPORT_TAXES("No Tax or Duty shall be laid on Articles exported from any State"),
        PORT_PREFERENCES("No Preference shall be given to the Ports of one State over those of another"),
        APPROPRIATIONS("No Money shall be drawn from the Treasury, but in Consequence of Appropriations made by Law"),
        TITLES_OF_NOBILITY("No Title of Nobility shall be granted by the United States"),
        RELIGIOUS_TEST("No religious Test shall ever be required as a Qualification to any Office or public Trust");

        private final String description;

        CongressionalLimit(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Powers prohibited to states under Article I, Section 10.
     */
    public enum StateProhibition {
        TREATIES("No State shall enter into any Treaty, Alliance, or Confederation"),
        LETTERS_OF_MARQUE("Grant Letters of Marque and Reprisal"),
        COIN_MONEY("Coin Money; emit Bills of Credit"),
        BILL_OF_ATTAINDER("Pass any Bill of Attainder, ex post facto Law, or Law impairing the Obligation of Contracts"),
        TITLES_OF_NOBILITY("Grant any Title of Nobility"),
        WITHOUT_CONSENT_TAXES("Lay any Imposts or Duties on Imports or Exports, without the Consent of the Congress"),
        KEEP_TROOPS("Keep Troops or Ships of War in Time of Peace"),
        MUTUAL_COMPACTS("Enter into any Agreement or Compact with another State, or with a foreign Power");

        private final String description;

        StateProhibition(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final int SECTION_COUNT = 10;

    public ArticleI() {
        super(ArticleNumber.ARTICLE_I, "The Legislative Branch",
                "Establishes Congress as a bicameral legislature with the Senate and House of Representatives, "
                + "defines Congressional powers, qualifications, and limits on federal and state authority.");
    }

    @Override
    public int getSectionCount() {
        return SECTION_COUNT;
    }

    /**
     * Returns all enumerated powers of Congress under Section 8.
     */
    public List<CongressionalPower> getCongressionalPowers() {
        return List.of(CongressionalPower.values());
    }

    /**
     * Returns all limits on Congressional power under Section 9.
     */
    public List<CongressionalLimit> getCongressionalLimits() {
        return List.of(CongressionalLimit.values());
    }

    /**
     * Returns all powers prohibited to states under Section 10.
     */
    public List<StateProhibition> getStateProhibitions() {
        return List.of(StateProhibition.values());
    }

    /**
     * Minimum age requirement for Representatives: 25 years.
     */
    public int getRepresentativeMinimumAge() {
        return 25;
    }

    /**
     * Minimum citizenship requirement for Representatives: 7 years.
     */
    public int getRepresentativeCitizenshipYears() {
        return 7;
    }

    /**
     * Minimum age requirement for Senators: 30 years.
     */
    public int getSenatorMinimumAge() {
        return 30;
    }

    /**
     * Minimum citizenship requirement for Senators: 9 years.
     */
    public int getSenatorCitizenshipYears() {
        return 9;
    }

    /**
     * Senator term length in years.
     */
    public int getSenatorTermYears() {
        return 6;
    }

    /**
     * House Representative term length in years.
     */
    public int getRepresentativeTermYears() {
        return 2;
    }
}
