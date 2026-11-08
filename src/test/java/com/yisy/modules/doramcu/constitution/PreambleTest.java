package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PreambleTest {

    // ---- Static text tests ----

    @Test
    @DisplayName("Static TEXT constant should start with 'We the People'")
    void testStaticText() {
        assertTrue(Preamble.TEXT.startsWith("We the People"));
        assertTrue(Preamble.TEXT.contains("form a more perfect Union"));
        assertTrue(Preamble.TEXT.contains("establish Justice"));
        assertTrue(Preamble.TEXT.contains("insure domestic Tranquility"));
        assertTrue(Preamble.TEXT.contains("provide for the common defence"));
        assertTrue(Preamble.TEXT.contains("promote the general Welfare"));
        assertTrue(Preamble.TEXT.contains("secure the Blessings of Liberty"));
    }

    // ---- Constructor tests ----

    @Test
    @DisplayName("Default constructor should initialize text from constant")
    void testDefaultConstructorText() {
        Preamble preamble = new Preamble();
        assertEquals(Preamble.TEXT, preamble.getText());
    }

    @Test
    @DisplayName("Default constructor should set adoption date to September 17, 1787")
    void testDefaultConstructorDate() {
        Preamble preamble = new Preamble();
        assertEquals(LocalDate.of(1787, 9, 17), preamble.getAdoptionDate());
    }

    // ---- Getter and setter tests ----

    @Test
    @DisplayName("Should allow setting custom text")
    void testSetText() {
        Preamble preamble = new Preamble();
        String customText = "Custom preamble text";
        preamble.setText(customText);
        assertEquals(customText, preamble.getText());
    }

    @Test
    @DisplayName("Should allow setting custom adoption date")
    void testSetAdoptionDate() {
        Preamble preamble = new Preamble();
        LocalDate customDate = LocalDate.of(1789, 3, 4);
        preamble.setAdoptionDate(customDate);
        assertEquals(customDate, preamble.getAdoptionDate());
    }

    // ---- Purposes enum tests ----

    @Test
    @DisplayName("Should have 6 purposes")
    void testPurposesCount() {
        Preamble preamble = new Preamble();
        assertEquals(6, preamble.getPurposes().length);
    }

    @Test
    @DisplayName("Purposes should include all expected values")
    void testPurposesValues() {
        Preamble.Purpose[] purposes = new Preamble().getPurposes();
        assertArrayEquals(Preamble.Purpose.values(), purposes);

        assertTrue(purposes.length > 0);
        for (Preamble.Purpose purpose : purposes) {
            assertNotNull(purpose.getDescription());
            assertFalse(purpose.getDescription().isEmpty());
        }
    }

    @Test
    @DisplayName("Each purpose should have a meaningful description")
    void testPurposeDescriptions() {
        assertEquals("Form a more perfect Union", Preamble.Purpose.FORM_MORE_PERFECT_UNION.getDescription());
        assertEquals("Establish Justice", Preamble.Purpose.ESTABLISH_JUSTICE.getDescription());
        assertEquals("Insure domestic Tranquility", Preamble.Purpose.INSURE_DOMESTIC_TRANQUILITY.getDescription());
        assertEquals("Provide for the common defence", Preamble.Purpose.PROVIDE_FOR_COMMON_DEFENCE.getDescription());
        assertEquals("Promote the general Welfare", Preamble.Purpose.PROMOTE_GENERAL_WELFARE.getDescription());
        assertEquals("Secure the Blessings of Liberty to ourselves and our Posterity",
                Preamble.Purpose.SECURE_BLESSINGS_OF_LIBERTY.getDescription());
    }

    // ---- toString tests ----

    @Test
    @DisplayName("toString should contain adoption date")
    void testToString() {
        Preamble preamble = new Preamble();
        String str = preamble.toString();
        assertTrue(str.contains("Preamble{"));
        assertTrue(str.contains("adoptionDate="));
        assertTrue(str.contains("1787"));
    }
}
