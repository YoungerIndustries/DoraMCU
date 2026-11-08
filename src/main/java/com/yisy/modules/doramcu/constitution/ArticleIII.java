package com.yisy.modules.doramcu.constitution;

import java.util.List;

/**
 * Article III — The Judicial Branch.
 * <p>
 * Establishes the judicial branch of the federal government, creates the
 * Supreme Court, and defines the scope of federal judicial power,
 * including jurisdiction over cases and controversies, trial by jury,
 * and the definition of treason.
 * </p>
 * <p>
 * Contains 3 sections covering: judicial powers, jurisdiction and trial by jury,
 * and the definition of treason.
 * </p>
 */
public class ArticleIII extends Article {

    /**
     * Types of cases within federal judicial jurisdiction under Section 2.
     */
    public enum JurisdictionType {
        CONSTITUTION("Cases arising under this Constitution"),
        FEDERAL_LAWS("Cases arising under the Laws of the United States"),
        TREATIES("Cases arising under Treaties made, or which shall be made, under their Authority"),
        AMBASSADORS("Cases affecting Ambassadors, other public Ministers and Consuls"),
        ADMIRALTY("Cases of admiralty and maritime Jurisdiction"),
        FEDERAL_DISPUTES("Controversies to which the United States shall be a Party"),
        INTERSTATE_DISPUTES("Controversies between two or more States"),
        CITIZEN_DISPUTES("Controversies between Citizens of different States"),
        LAND_GRANTS("Controversies between Citizens claiming Lands under Grants of different States"),
        STATE_VS_CITIZEN("Controversies between a State and Citizens of another State");

        private final String description;

        JurisdictionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Types of crimes where trial by jury is required under Section 3.
     */
    public enum TrialByJuryRequirement {
        CRIMINAL_TRIALS("The Trial of all Crimes, except in Cases of Impeachment, shall be by Jury"),
        CRIME_LOCATION("shall be held in the State where the said Crimes shall have been committed");

        private final String description;

        TrialByJuryRequirement(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Elements of treason as defined under Section 3.
     */
    public enum TreasonElement {
        LEAGUE_WITH_ENEMY("levying War against [the United States]"),
        ADHERING_TO_ENEMIES("adhering to their Enemies, giving them Aid and Comfort"),
        TWO_WITNESSES("no Person shall be convicted of Treason unless on the Testimony of two Witnesses to the same overt Act"),
        OVERT_ACT("or on Confession in open Court");

        private final String description;

        TreasonElement(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final int SECTION_COUNT = 3;

    public ArticleIII() {
        super(ArticleNumber.ARTICLE_III, "The Judicial Branch",
                "Establishes the Supreme Court, defines federal judicial power and jurisdiction, "
                + "guarantees trial by jury in criminal cases, and defines treason.");
    }

    @Override
    public int getSectionCount() {
        return SECTION_COUNT;
    }

    /**
     * Returns all types of federal judicial jurisdiction under Section 2.
     */
    public List<JurisdictionType> getJurisdictionTypes() {
        return List.of(JurisdictionType.values());
    }

    /**
     * Returns trial by jury requirements under Section 3.
     */
    public List<TrialByJuryRequirement> getTrialByJuryRequirements() {
        return List.of(TrialByJuryRequirement.values());
    }

    /**
     * Returns elements of treason under Section 3.
     */
    public List<TreasonElement> getTreasonElements() {
        return List.of(TreasonElement.values());
    }

    /**
     * Judges hold office during good behavior (life tenure).
     */
    public boolean hasLifeTenure() {
        return true;
    }
}
