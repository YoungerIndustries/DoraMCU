package com.yisy.modules.doramcu.constitution;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents an Amendment to the United States Constitution.
 * <p>
 * Amendments are appended to the Constitution and modify, expand, or clarify
 * its provisions. There are 27 ratified amendments, grouped into categories
 * such as the Bill of Rights, Reconstruction Amendments, and 20th-century amendments.
 * </p>
 */
public class Amendment {

    /**
     * Groups of amendments by historical era and purpose.
     */
    public enum AmendmentGroup {
        BILL_OF_RIGHTS("Bill of Rights", 1, 10, "Fundamental individual liberties and limitations on government power"),
        EARLY_REPUBLIC("Early Republic", 11, 12, "Procedural amendments addressing judicial limits and presidential elections"),
        RECONSTRUCTION("Reconstruction", 13, 15, "Abolition of slavery, citizenship rights, and voting rights after the Civil War"),
        PROGRESSIVE_ERA("Progressive Era", 16, 18, "Income tax, direct election of senators, and prohibition"),
        CIVIL_RIGHTS("Civil Rights", 19, 26, "Women's suffrage, voting age reduction, and other civil rights"),
        GOVERNMENT_PROCEDURES("Government Procedures", 20, 25, "Presidential terms, succession, and congressional sessions"),
        REPEALS("Repeals", 21, 21, "Repeal of previous amendments (Prohibition)");

        private final String name;
        private final int startNumber;
        private final int endNumber;
        private final String description;

        AmendmentGroup(String name, int startNumber, int endNumber, String description) {
            this.name = name;
            this.startNumber = startNumber;
            this.endNumber = endNumber;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public int getStartNumber() {
            return startNumber;
        }

        public int getEndNumber() {
            return endNumber;
        }

        public String getDescription() {
            return description;
        }

        public boolean contains(int amendmentNumber) {
            return amendmentNumber >= startNumber && amendmentNumber <= endNumber;
        }
    }

    /**
     * All 27 ratified amendments with their titles and key subjects.
     */
    public enum RatifiedAmendment {
        FIRST(1, "Freedom of Religion, Speech, Press, Assembly, and Petition",
                LocalDate.of(1791, 12, 15),
                List.of("Freedom of Religion", "Freedom of Speech", "Freedom of the Press",
                        "Freedom of Assembly", "Right to Petition")),
        SECOND(2, "Right to Bear Arms",
                LocalDate.of(1791, 12, 15),
                List.of("Right to Keep and Bear Arms", "Well-regulated Militia")),
        THIRD(3, "Quartering of Soldiers",
                LocalDate.of(1791, 12, 15),
                List.of("Protection against Forced Quartering of Soldiers")),
        FOURTH(4, "Search and Seizure",
                LocalDate.of(1791, 12, 15),
                List.of("Protection against Unreasonable Searches and Seizures", "Warrants")),
        FIFTH(5, "Grand Jury, Double Jeopardy, Self Incrimination, Due Process, Takings",
                LocalDate.of(1791, 12, 15),
                List.of("Grand Jury Indictment", "Double Jeopardy Protection",
                        "Protection against Self-Incrimination", "Due Process", "Just Compensation for Takings")),
        SIXTH(6, "Right to Speedy Trial, Confrontation of Witnesses",
                LocalDate.of(1791, 12, 15),
                List.of("Speedy and Public Trial", "Impartial Jury", "Confrontation of Witnesses",
                        "Compulsory Process", "Assistance of Counsel")),
        SEVENTH(7, "Trial by Jury in Civil Cases",
                LocalDate.of(1791, 12, 15),
                List.of("Right to Jury Trial in Civil Cases", "Re-examination of Facts")),
        EIGHTH(8, "Cruel and Unusual Punishment",
                LocalDate.of(1791, 12, 15),
                List.of("Excessive Bail Prohibition", "Excessive Fines Prohibition",
                        "Cruel and Unusual Punishment Prohibition")),
        NINTH(9, "Rights Retained by the People",
                LocalDate.of(1791, 12, 15),
                List.of("Enumeration of Rights shall not be construed to deny other Rights retained by the People")),
        TENTH(10, "Powers of the States and People",
                LocalDate.of(1791, 12, 15),
                List.of("Powers not delegated to the United States are reserved to the States or the People")),
        ELEVENTH(11, "Judicial Limits — State Sovereign Immunity",
                LocalDate.of(1795, 2, 7),
                List.of("State Sovereign Immunity from Suits by Citizens of another State")),
        TWELFTH(12, "Choosing the President, Vice President",
                LocalDate.of(1804, 6, 15),
                List.of("Separate Electoral Votes for President and Vice President")),
        THIRTEENTH(13, "Slavery Abolished",
                LocalDate.of(1865, 12, 6),
                List.of("Abolition of Slavery", "Involuntary Servitude except as Punishment for Crime")),
        FOURTEENTH(14, "Citizenship Rights, Equal Protection",
                LocalDate.of(1868, 7, 9),
                List.of("Citizenship Clause", "Due Process Clause", "Equal Protection Clause",
                        "Privileges or Immunities Clause")),
        FIFTEENTH(15, "Race No Bar to Vote",
                LocalDate.of(1870, 3, 30),
                List.of("Voting Rights shall not be denied on account of Race, Color, or previous Condition of Servitude")),
        SIXTEENTH(16, "Income Tax",
                LocalDate.of(1913, 2, 3),
                List.of("Congress may levy Income Tax without Apportionment among the States")),
        SEVENTEENTH(17, "Popular Election of Senators",
                LocalDate.of(1913, 4, 8),
                List.of("Senators Elected by the People of each State")),
        EIGHTEENTH(18, "Prohibition of Liquor",
                LocalDate.of(1919, 1, 16),
                List.of("Prohibition of Manufacturing, Sale, and Transportation of Intoxicating Liquors")),
        NINETEENTH(19, "Women's Right to Vote",
                LocalDate.of(1920, 8, 18),
                List.of("Voting Rights shall not be denied on account of Sex")),
        TWENTIETH(20, "Presidential Term and Succession, Assembly of Congress",
                LocalDate.of(1933, 1, 23),
                List.of("Lame Duck Amendment", "Presidential Inauguration Date", "Congressional Session Start")),
        TWENTY_FIRST(21, "Repeal of Prohibition",
                LocalDate.of(1933, 12, 5),
                List.of("Repeal of the Eighteenth Amendment", "State Regulation of Alcohol")),
        TWENTY_SECOND(22, "Two-Term Limit on Presidency",
                LocalDate.of(1951, 2, 27),
                List.of("Presidential Term Limits", "Maximum of Two Elected Terms")),
        TWENTY_THIRD(23, "Presidential Vote for D.C.",
                LocalDate.of(1961, 3, 29),
                List.of("District of Columbia Electoral College Votes")),
        TWENTY_FOURTH(24, "Abolition of Poll Taxes",
                LocalDate.of(1964, 1, 23),
                List.of("Voting Rights shall not be denied for Failure to pay any Poll Tax")),
        TWENTY_FIFTH(25, "Presidential Disability and Succession",
                LocalDate.of(1967, 2, 10),
                List.of("Presidential Succession", "Vice Presidential Vacancy",
                        "Presidential Disability", "Congressional Determination")),
        TWENTY_SIXTH(26, "Right to Vote at Age 18",
                LocalDate.of(1971, 7, 1),
                List.of("Voting Age set to 18 Years")),
        TWENTY_SEVENTH(27, "Congressional Compensation",
                LocalDate.of(1992, 5, 7),
                List.of("Changes in Congressional Compensation shall not take effect until after a subsequent Election"));

        private final int number;
        private final String title;
        private final LocalDate ratificationDate;
        private final List<String> subjects;

        RatifiedAmendment(int number, String title, LocalDate ratificationDate, List<String> subjects) {
            this.number = number;
            this.title = title;
            this.ratificationDate = ratificationDate;
            this.subjects = subjects;
        }

        public int getNumber() {
            return number;
        }

        public String getTitle() {
            return title;
        }

        public LocalDate getRatificationDate() {
            return ratificationDate;
        }

        public List<String> getSubjects() {
            return subjects;
        }

        public AmendmentGroup getGroup() {
            for (AmendmentGroup group : AmendmentGroup.values()) {
                if (group.contains(number)) {
                    return group;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "Amendment{" + number + ": " + title + ", ratified=" + ratificationDate + "}";
        }
    }

    private final RatifiedAmendment amendment;

    public Amendment(RatifiedAmendment amendment) {
        this.amendment = amendment;
    }

    public int getNumber() {
        return amendment.getNumber();
    }

    public String getTitle() {
        return amendment.getTitle();
    }

    public LocalDate getRatificationDate() {
        return amendment.getRatificationDate();
    }

    public List<String> getSubjects() {
        return amendment.getSubjects();
    }

    public AmendmentGroup getGroup() {
        return amendment.getGroup();
    }

    public RatifiedAmendment getAmendment() {
        return amendment;
    }

    @Override
    public String toString() {
        return amendment.toString();
    }
}
