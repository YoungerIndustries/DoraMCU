package com.yisy.modules.doramcu.constitution;

/**
 * Represents the Preamble of the United States Constitution.
 * <p>
 * The Preamble introduces the Constitution with the famous opening words
 * "We the People" and states the purposes of the government:
 * forming a more perfect Union, establishing justice, insuring domestic tranquility,
 * providing for the common defence, promoting the general welfare, and securing
 * the blessings of liberty.
 * </p>
 */
public class Preamble {

    public static final String TEXT =
            "We the People of the United States, in Order to form a more perfect Union, "
            + "establish Justice, insure domestic Tranquility, provide for the common defence, "
            + "promote the general Welfare, and secure the Blessings of Liberty to ourselves "
            + "and our Posterity, do ordain and establish this Constitution for the United States of America.";

    /**
     * The stated purposes of the Constitution as enumerated in the Preamble.
     */
    public enum Purpose {
        FORM_MORE_PERFECT_UNION("Form a more perfect Union"),
        ESTABLISH_JUSTICE("Establish Justice"),
        INSURE_DOMESTIC_TRANQUILITY("Insure domestic Tranquility"),
        PROVIDE_FOR_COMMON_DEFENCE("Provide for the common defence"),
        PROMOTE_GENERAL_WELFARE("Promote the general Welfare"),
        SECURE_BLESSINGS_OF_LIBERTY("Secure the Blessings of Liberty to ourselves and our Posterity");

        private final String description;

        Purpose(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private String text;
    private java.time.LocalDate adoptionDate;

    public Preamble() {
        this.text = TEXT;
        this.adoptionDate = java.time.LocalDate.of(1787, 9, 17);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public java.time.LocalDate getAdoptionDate() {
        return adoptionDate;
    }

    public void setAdoptionDate(java.time.LocalDate adoptionDate) {
        this.adoptionDate = adoptionDate;
    }

    public Purpose[] getPurposes() {
        return Purpose.values();
    }

    @Override
    public String toString() {
        return "Preamble{adoptionDate=" + adoptionDate + "}";
    }
}
