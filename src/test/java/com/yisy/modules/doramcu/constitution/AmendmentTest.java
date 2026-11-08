package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmendmentTest {

    // ---- RatifiedAmendment enum tests ----

    @Test
    @DisplayName("Should have 27 ratified amendments")
    void testRatifiedAmendmentCount() {
        assertEquals(27, Amendment.RatifiedAmendment.values().length);
    }

    @Test
    @DisplayName("First amendment should have correct number and title")
    void testFirstAmendment() {
        Amendment.RatifiedAmendment first = Amendment.RatifiedAmendment.FIRST;
        assertEquals(1, first.getNumber());
        assertEquals("Freedom of Religion, Speech, Press, Assembly, and Petition", first.getTitle());
    }

    @Test
    @DisplayName("First amendment should be ratified on December 15, 1791")
    void testFirstAmendmentDate() {
        assertEquals(LocalDate.of(1791, 12, 15), Amendment.RatifiedAmendment.FIRST.getRatificationDate());
    }

    @Test
    @DisplayName("First amendment should have 5 subjects")
    void testFirstAmendmentSubjects() {
        List<String> subjects = Amendment.RatifiedAmendment.FIRST.getSubjects();
        assertEquals(5, subjects.size());
        assertTrue(subjects.contains("Freedom of Religion"));
        assertTrue(subjects.contains("Freedom of Speech"));
        assertTrue(subjects.contains("Freedom of the Press"));
        assertTrue(subjects.contains("Freedom of Assembly"));
        assertTrue(subjects.contains("Right to Petition"));
    }

    @Test
    @DisplayName("Second amendment should be about bearing arms")
    void testSecondAmendment() {
        Amendment.RatifiedAmendment second = Amendment.RatifiedAmendment.SECOND;
        assertEquals(2, second.getNumber());
        assertEquals("Right to Bear Arms", second.getTitle());
    }

    @Test
    @DisplayName("Thirteenth amendment should abolish slavery")
    void testThirteenthAmendment() {
        Amendment.RatifiedAmendment thirteenth = Amendment.RatifiedAmendment.THIRTEENTH;
        assertEquals(13, thirteenth.getNumber());
        assertEquals("Slavery Abolished", thirteenth.getTitle());
        assertEquals(LocalDate.of(1865, 12, 6), thirteenth.getRatificationDate());
    }

    @Test
    @DisplayName("Fourteenth amendment should cover citizenship and equal protection")
    void testFourteenthAmendment() {
        Amendment.RatifiedAmendment fourteenth = Amendment.RatifiedAmendment.FOURTEENTH;
        assertEquals(14, fourteenth.getNumber());
        assertEquals("Citizenship Rights, Equal Protection", fourteenth.getTitle());
        assertTrue(fourteenth.getSubjects().contains("Equal Protection Clause"));
        assertTrue(fourteenth.getSubjects().contains("Due Process Clause"));
    }

    @Test
    @DisplayName("Twenty seventh amendment should be about congressional compensation")
    void testTwentySeventhAmendment() {
        Amendment.RatifiedAmendment twentySeventh = Amendment.RatifiedAmendment.TWENTY_SEVENTH;
        assertEquals(27, twentySeventh.getNumber());
        assertEquals("Congressional Compensation", twentySeventh.getTitle());
        assertEquals(LocalDate.of(1992, 5, 7), twentySeventh.getRatificationDate());
    }

    @Test
    @DisplayName("All amendments should have non-empty subjects")
    void testAllAmendmentsHaveSubjects() {
        for (Amendment.RatifiedAmendment ra : Amendment.RatifiedAmendment.values()) {
            assertFalse(ra.getSubjects().isEmpty(), "Amendment " + ra.getNumber() + " should have subjects");
        }
    }

    @Test
    @DisplayName("All amendments should have valid ratification dates")
    void testAllAmendmentsHaveDates() {
        for (Amendment.RatifiedAmendment ra : Amendment.RatifiedAmendment.values()) {
            assertNotNull(ra.getRatificationDate(), "Amendment " + ra.getNumber() + " should have a date");
        }
    }

    @Test
    @DisplayName("Amendment numbers should be sequential from 1 to 27")
    void testSequentialNumbers() {
        for (int i = 0; i < Amendment.RatifiedAmendment.values().length; i++) {
            assertEquals(i + 1, Amendment.RatifiedAmendment.values()[i].getNumber());
        }
    }

    // ---- AmendmentGroup tests ----

    @Test
    @DisplayName("Should have 7 amendment groups")
    void testAmendmentGroupCount() {
        assertEquals(7, Amendment.AmendmentGroup.values().length);
    }

    @Test
    @DisplayName("Bill of Rights group should cover amendments 1-10")
    void testBillOfRightsGroup() {
        Amendment.AmendmentGroup group = Amendment.AmendmentGroup.BILL_OF_RIGHTS;
        assertEquals("Bill of Rights", group.getName());
        assertEquals(1, group.getStartNumber());
        assertEquals(10, group.getEndNumber());
        assertTrue(group.contains(1));
        assertTrue(group.contains(10));
        assertFalse(group.contains(0));
        assertFalse(group.contains(11));
    }

    @Test
    @DisplayName("Reconstruction group should cover amendments 13-15")
    void testReconstructionGroup() {
        Amendment.AmendmentGroup group = Amendment.AmendmentGroup.RECONSTRUCTION;
        assertEquals("Reconstruction", group.getName());
        assertEquals(13, group.getStartNumber());
        assertEquals(15, group.getEndNumber());
        assertTrue(group.contains(13));
        assertTrue(group.contains(15));
        assertFalse(group.contains(12));
        assertFalse(group.contains(16));
    }

    @Test
    @DisplayName("Each group should have a description")
    void testGroupDescriptions() {
        for (Amendment.AmendmentGroup group : Amendment.AmendmentGroup.values()) {
            assertNotNull(group.getDescription());
            assertFalse(group.getDescription().isEmpty());
        }
    }

    // ---- Amendment wrapper class tests ----

    @Test
    @DisplayName("Amendment wrapper should delegate number correctly")
    void testWrapperNumber() {
        Amendment amendment = new Amendment(Amendment.RatifiedAmendment.FIRST);
        assertEquals(1, amendment.getNumber());
    }

    @Test
    @DisplayName("Amendment wrapper should delegate title correctly")
    void testWrapperTitle() {
        Amendment amendment = new Amendment(Amendment.RatifiedAmendment.SECOND);
        assertEquals("Right to Bear Arms", amendment.getTitle());
    }

    @Test
    @DisplayName("Amendment wrapper should delegate date correctly")
    void testWrapperDate() {
        Amendment amendment = new Amendment(Amendment.RatifiedAmendment.THIRTEENTH);
        assertEquals(LocalDate.of(1865, 12, 6), amendment.getRatificationDate());
    }

    @Test
    @DisplayName("Amendment wrapper should delegate subjects correctly")
    void testWrapperSubjects() {
        Amendment amendment = new Amendment(Amendment.RatifiedAmendment.FIRST);
        List<String> subjects = amendment.getSubjects();
        assertEquals(5, subjects.size());
        assertTrue(subjects.contains("Freedom of Speech"));
    }

    @Test
    @DisplayName("Amendment wrapper should delegate group correctly")
    void testWrapperGroup() {
        Amendment amendment = new Amendment(Amendment.RatifiedAmendment.FIRST);
        assertEquals(Amendment.AmendmentGroup.BILL_OF_RIGHTS, amendment.getGroup());

        Amendment reconstruction = new Amendment(Amendment.RatifiedAmendment.THIRTEENTH);
        assertEquals(Amendment.AmendmentGroup.RECONSTRUCTION, reconstruction.getGroup());
    }

    @Test
    @DisplayName("Amendment wrapper should return underlying enum")
    void testWrapperGetAmendment() {
        Amendment.RatifiedAmendment ra = Amendment.RatifiedAmendment.FIFTH;
        Amendment amendment = new Amendment(ra);
        assertSame(ra, amendment.getAmendment());
    }

    // ---- Group assignment tests ----

    @Test
    @DisplayName("First amendment should belong to Bill of Rights")
    void testFirstAmendmentGroup() {
        assertEquals(Amendment.AmendmentGroup.BILL_OF_RIGHTS,
                Amendment.RatifiedAmendment.FIRST.getGroup());
    }

    @Test
    @DisplayName("Eleventh amendment should belong to Early Republic")
    void testEleventhAmendmentGroup() {
        assertEquals(Amendment.AmendmentGroup.EARLY_REPUBLIC,
                Amendment.RatifiedAmendment.ELEVENTH.getGroup());
    }

    @Test
    @DisplayName("Nineteenth amendment should belong to Civil Rights")
    void testNineteenthAmendmentGroup() {
        assertEquals(Amendment.AmendmentGroup.CIVIL_RIGHTS,
                Amendment.RatifiedAmendment.NINETEENTH.getGroup());
    }

    @Test
    @DisplayName("Twenty first amendment group should contain amendment 21")
    void testTwentyFirstAmendmentGroup() {
        // Amendment 21 falls in multiple overlapping groups; getGroup() returns the first match
        Amendment.AmendmentGroup group = Amendment.RatifiedAmendment.TWENTY_FIRST.getGroup();
        assertNotNull(group);
        assertTrue(group.contains(21));
    }

    // ---- toString tests ----

    @Test
    @DisplayName("RatifiedAmendment toString should contain number and title")
    void testRatifiedAmendmentToString() {
        String str = Amendment.RatifiedAmendment.FIRST.toString();
        assertTrue(str.contains("Amendment{"));
        assertTrue(str.contains("1"));
        assertTrue(str.contains("Freedom"));
    }

    @Test
    @DisplayName("Amendment wrapper toString should delegate to enum")
    void testWrapperToString() {
        Amendment amendment = new Amendment(Amendment.RatifiedAmendment.FIRST);
        String str = amendment.toString();
        assertTrue(str.contains("Amendment{"));
        assertTrue(str.contains("1"));
    }

    // ---- Historical ordering tests ----

    @Test
    @DisplayName("Bill of Rights amendments should all share the same ratification date")
    void testBillOfRightsSameDate() {
        LocalDate date = Amendment.RatifiedAmendment.FIRST.getRatificationDate();
        for (int i = 1; i <= 10; i++) {
            Amendment.RatifiedAmendment ra = Amendment.RatifiedAmendment.values()[i - 1];
            assertEquals(date, ra.getRatificationDate(),
                    "Amendment " + i + " should share Bill of Rights ratification date");
        }
    }

    @Test
    @DisplayName("Amendments should be in chronological order by ratification date")
    void testChronologicalOrder() {
        Amendment.RatifiedAmendment[] values = Amendment.RatifiedAmendment.values();
        for (int i = 1; i < values.length; i++) {
            assertTrue(values[i - 1].getRatificationDate().isBefore(values[i].getRatificationDate())
                    || values[i - 1].getRatificationDate().isEqual(values[i].getRatificationDate()),
                    "Amendment " + values[i - 1].getNumber() + " should not be after Amendment " + values[i].getNumber());
        }
    }
}
