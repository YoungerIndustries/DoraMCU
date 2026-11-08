package com.yisy.modules.doramcu.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiDelegationSettings Tests")
class AiDelegationSettingsTest {

    // ---- Builder Tests ----

    @Test
    @DisplayName("should create settings with all areas disabled by default")
    void defaultSettingsAllDisabled() {
        AiDelegationSettings settings = AiDelegationSettings.builder().build();
        assertFalse(settings.isEmpireAiEnabled());
        assertEquals(0, settings.getDelegatedCount());
        assertEquals(9, settings.getTotalAreas());
    }

    @Test
    @DisplayName("should enable empire AI switch")
    void enableEmpireAi() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .build();
        assertTrue(settings.isEmpireAiEnabled());
    }

    @Test
    @DisplayName("should enable specific area")
    void enableSpecificArea() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        assertTrue(settings.isAreaEnabled(AiDelegationArea.COMBAT));
        assertFalse(settings.isAreaEnabled(AiDelegationArea.TRADE));
    }

    @Test
    @DisplayName("should enable all areas")
    void enableAllAreas() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableAll()
                .build();
        assertEquals(9, settings.getDelegatedCount());
        for (AiDelegationArea area : AiDelegationArea.values()) {
            assertTrue(settings.isDelegated(area));
        }
    }

    @Test
    @DisplayName("should disable all areas")
    void disableAllAreas() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .disableAll()
                .build();
        assertEquals(0, settings.getDelegatedCount());
    }

    @Test
    @DisplayName("should set area with boolean value")
    void setAreaBoolean() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .setArea(AiDelegationArea.FLEET_MANAGEMENT, true)
                .setArea(AiDelegationArea.COMBAT, false)
                .build();
        assertTrue(settings.isAreaEnabled(AiDelegationArea.FLEET_MANAGEMENT));
        assertFalse(settings.isAreaEnabled(AiDelegationArea.COMBAT));
    }

    // ---- Factory Methods ----

    @Test
    @DisplayName("allEnabled should have all areas enabled but empire switch off")
    void allEnabledFactory() {
        AiDelegationSettings settings = AiDelegationSettings.allEnabled();
        assertFalse(settings.isEmpireAiEnabled());
        assertEquals(9, settings.getEnabledAreas().size());
        assertEquals(0, settings.getDelegatedCount()); // empire switch is off
    }

    @Test
    @DisplayName("allDisabled should have all areas disabled")
    void allDisabledFactory() {
        AiDelegationSettings settings = AiDelegationSettings.allDisabled();
        assertFalse(settings.isEmpireAiEnabled());
        assertEquals(0, settings.getEnabledAreas().size());
    }

    // ---- isDelegated vs isAreaEnabled ----

    @Test
    @DisplayName("isDelegated should be false when empire switch is off")
    void delegatedFalseWhenEmpireOff() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        assertTrue(settings.isAreaEnabled(AiDelegationArea.COMBAT));
        assertFalse(settings.isDelegated(AiDelegationArea.COMBAT));
    }

    @Test
    @DisplayName("isDelegated should be true when both empire and area are enabled")
    void delegatedTrueWhenBothEnabled() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        assertTrue(settings.isAreaEnabled(AiDelegationArea.COMBAT));
        assertTrue(settings.isDelegated(AiDelegationArea.COMBAT));
    }

    @Test
    @DisplayName("isDelegated should be false when area is disabled")
    void delegatedFalseWhenAreaDisabled() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        assertFalse(settings.isDelegated(AiDelegationArea.TRADE));
    }

    // ---- Immutable Copy Methods ----

    @Test
    @DisplayName("withEmpireAiEnabled should create a new instance")
    void withEmpireAiEnabled() {
        AiDelegationSettings original = AiDelegationSettings.builder()
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        AiDelegationSettings updated = original.withEmpireAiEnabled(true);

        assertFalse(original.isEmpireAiEnabled());
        assertTrue(updated.isEmpireAiEnabled());
        assertTrue(updated.isAreaEnabled(AiDelegationArea.COMBAT));
    }

    @Test
    @DisplayName("withAreaToggled should create a new instance")
    void withAreaToggled() {
        AiDelegationSettings original = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        AiDelegationSettings updated = original.withAreaToggled(AiDelegationArea.TRADE, true);

        assertFalse(original.isDelegated(AiDelegationArea.TRADE));
        assertTrue(updated.isDelegated(AiDelegationArea.TRADE));
        assertTrue(updated.isDelegated(AiDelegationArea.COMBAT));
    }

    @Test
    @DisplayName("withAreaToggled should throw on null area")
    void withAreaToggledNullThrows() {
        AiDelegationSettings settings = AiDelegationSettings.allDisabled();
        assertThrows(NullPointerException.class, () -> settings.withAreaToggled(null, true));
    }

    // ---- getDelegatedAreas and getEnabledAreas ----

    @Test
    @DisplayName("getDelegatedAreas should return empty when empire off")
    void delegatedAreasEmptyWhenEmpireOff() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .enableArea(AiDelegationArea.COMBAT)
                .enableArea(AiDelegationArea.TRADE)
                .build();
        assertTrue(settings.getDelegatedAreas().isEmpty());
        assertEquals(2, settings.getEnabledAreas().size());
    }

    @Test
    @DisplayName("getDelegatedAreas should return only enabled areas when empire on")
    void delegatedAreasWhenEmpireOn() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .enableArea(AiDelegationArea.TRADE)
                .build();
        assertEquals(2, settings.getDelegatedAreas().size());
        assertTrue(settings.getDelegatedAreas().contains(AiDelegationArea.COMBAT));
        assertTrue(settings.getDelegatedAreas().contains(AiDelegationArea.TRADE));
    }

    // ---- getAreaSettings ----

    @Test
    @DisplayName("getAreaSettings should return unmodifiable map")
    void areaSettingsUnmodifiable() {
        AiDelegationSettings settings = AiDelegationSettings.builder().build();
        Map<AiDelegationArea, Boolean> map = settings.getAreaSettings();
        assertThrows(UnsupportedOperationException.class, () -> map.put(AiDelegationArea.COMBAT, true));
    }

    @Test
    @DisplayName("getAreaSettings should contain all areas")
    void areaSettingsContainsAll() {
        AiDelegationSettings settings = AiDelegationSettings.builder().build();
        assertEquals(9, settings.getAreaSettings().size());
    }

    // ---- Null Safety ----

    @Test
    @DisplayName("isAreaEnabled should throw on null")
    void isAreaEnabledNullThrows() {
        AiDelegationSettings settings = AiDelegationSettings.allDisabled();
        assertThrows(NullPointerException.class, () -> settings.isAreaEnabled(null));
    }

    @Test
    @DisplayName("isDelegated should throw on null")
    void isDelegatedNullThrows() {
        AiDelegationSettings settings = AiDelegationSettings.allDisabled();
        assertThrows(NullPointerException.class, () -> settings.isDelegated(null));
    }

    // ---- Builder Method Chaining ----

    @Test
    @DisplayName("builder methods should chain")
    void builderChaining() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .enableArea(AiDelegationArea.FLEET_MANAGEMENT)
                .setArea(AiDelegationArea.TRADE, true)
                .build();

        assertTrue(settings.isEmpireAiEnabled());
        assertTrue(settings.isDelegated(AiDelegationArea.COMBAT));
        assertTrue(settings.isDelegated(AiDelegationArea.FLEET_MANAGEMENT));
        assertTrue(settings.isDelegated(AiDelegationArea.TRADE));
        assertEquals(3, settings.getDelegatedCount());
    }

    // ---- Equals and HashCode ----

    @Test
    @DisplayName("equal settings should be equal")
    void equalsAndHashCode() {
        AiDelegationSettings s1 = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        AiDelegationSettings s2 = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .build();

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    @DisplayName("different settings should not be equal")
    void notEqual() {
        AiDelegationSettings s1 = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        AiDelegationSettings s2 = AiDelegationSettings.builder()
                .empireAiEnabled(false)
                .enableArea(AiDelegationArea.COMBAT)
                .build();

        assertNotEquals(s1, s2);
    }

    // ---- toString ----

    @Test
    @DisplayName("toString should include key info")
    void toStringContainsInfo() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        String str = settings.toString();
        assertTrue(str.contains("empireAiEnabled=true"));
        assertTrue(str.contains("COMBAT"));
    }
}
