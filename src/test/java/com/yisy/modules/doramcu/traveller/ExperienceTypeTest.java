package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExperienceType Tests")
class ExperienceTypeTest {

    @Test
    @DisplayName("Should have 23 experience types")
    void testEnumSize() {
        assertEquals(23, ExperienceType.values().length);
    }

    @Test
    @DisplayName("Each type should have a non-empty display name")
    void testDisplayNamesNotEmpty() {
        for (ExperienceType type : ExperienceType.values()) {
            assertNotNull(type.getDisplayName(), type + " should have a display name");
            assertFalse(type.getDisplayName().isBlank(), type + " display name should not be blank");
        }
    }

    @Test
    @DisplayName("Each type should have a non-empty subtitle")
    void testSubtitlesNotEmpty() {
        for (ExperienceType type : ExperienceType.values()) {
            assertNotNull(type.getSubtitle(), type + " should have a subtitle");
            assertFalse(type.getSubtitle().isBlank(), type + " subtitle should not be blank");
        }
    }

    @Test
    @DisplayName("CHARACTER_CREATION should have correct display name and subtitle")
    void testCharacterCreation() {
        assertEquals("Character Creation", ExperienceType.CHARACTER_CREATION.getDisplayName());
        assertEquals("The beginning of a life", ExperienceType.CHARACTER_CREATION.getSubtitle());
    }

    @Test
    @DisplayName("BATTLE should have correct display name and subtitle")
    void testBattle() {
        assertEquals("Battle", ExperienceType.BATTLE.getDisplayName());
        assertEquals("The clash of arms", ExperienceType.BATTLE.getSubtitle());
    }

    @Test
    @DisplayName("MISSION_SUCCESS should have correct display name and subtitle")
    void testMissionSuccess() {
        assertEquals("Mission Success", ExperienceType.MISSION_SUCCESS.getDisplayName());
        assertEquals("Victory achieved", ExperienceType.MISSION_SUCCESS.getSubtitle());
    }

    @Test
    @DisplayName("MISSION_FAILURE should have correct display name and subtitle")
    void testMissionFailure() {
        assertEquals("Mission Failure", ExperienceType.MISSION_FAILURE.getDisplayName());
        assertEquals("Defeat and its consequences", ExperienceType.MISSION_FAILURE.getSubtitle());
    }

    @Test
    @DisplayName("EXPLORATION should have correct display name and subtitle")
    void testExploration() {
        assertEquals("Exploration", ExperienceType.EXPLORATION.getDisplayName());
        assertEquals("Discovering the unknown", ExperienceType.EXPLORATION.getSubtitle());
    }

    @Test
    @DisplayName("DEATH should have correct display name and subtitle")
    void testDeath() {
        assertEquals("Death", ExperienceType.DEATH.getDisplayName());
        assertEquals("The end of the road", ExperienceType.DEATH.getSubtitle());
    }

    @Test
    @DisplayName("HEROISM should have correct display name and subtitle")
    void testHeroism() {
        assertEquals("Heroism", ExperienceType.HEROISM.getDisplayName());
        assertEquals("Going beyond the call", ExperienceType.HEROISM.getSubtitle());
    }

    @Test
    @DisplayName("PROMOTION should have correct display name and subtitle")
    void testPromotion() {
        assertEquals("Promotion", ExperienceType.PROMOTION.getDisplayName());
        assertEquals("Rising through the ranks", ExperienceType.PROMOTION.getSubtitle());
    }

    @Test
    @DisplayName("RETIREMENT should have correct display name and subtitle")
    void testRetirement() {
        assertEquals("Retirement", ExperienceType.RETIREMENT.getDisplayName());
        assertEquals("Leaving service behind", ExperienceType.RETIREMENT.getSubtitle());
    }

    @Test
    @DisplayName("Display names should be unique")
    void testDisplayNamesUnique() {
        ExperienceType[] values = ExperienceType.values();
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getDisplayName(), values[j].getDisplayName(),
                        "Display names should be unique: " + values[i] + " and " + values[j]);
            }
        }
    }

    @Test
    @DisplayName("Subtitles should be unique")
    void testSubtitlesUnique() {
        ExperienceType[] values = ExperienceType.values();
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getSubtitle(), values[j].getSubtitle(),
                        "Subtitles should be unique: " + values[i] + " and " + values[j]);
            }
        }
    }

    @Test
    @DisplayName("Should include all career-related types")
    void testCareerTypesExist() {
        assertNotNull(ExperienceType.CAREER_START);
        assertNotNull(ExperienceType.CAREER_COMPLETION);
        assertNotNull(ExperienceType.SKILL_GAIN);
        assertNotNull(ExperienceType.PROMOTION);
        assertNotNull(ExperienceType.RETIREMENT);
    }

    @Test
    @DisplayName("Should include all adventure-related types")
    void testAdventureTypesExist() {
        assertNotNull(ExperienceType.BATTLE);
        assertNotNull(ExperienceType.MISSION_SUCCESS);
        assertNotNull(ExperienceType.MISSION_FAILURE);
        assertNotNull(ExperienceType.EXPLORATION);
        assertNotNull(ExperienceType.TRADE);
        assertNotNull(ExperienceType.INJURY);
        assertNotNull(ExperienceType.NARROW_ESCAPE);
        assertNotNull(ExperienceType.DISCOVERY);
        assertNotNull(ExperienceType.HEROISM);
    }

    @Test
    @DisplayName("Should include social and personal types")
    void testSocialTypesExist() {
        assertNotNull(ExperienceType.SOCIAL);
        assertNotNull(ExperienceType.BETRAYAL);
        assertNotNull(ExperienceType.REFLECTION);
        assertNotNull(ExperienceType.RELATIONSHIP);
        assertNotNull(ExperienceType.ACQUISITION);
        assertNotNull(ExperienceType.LOSS);
    }

    // ---- Remaining enum values individually tested ----

    @Test
    @DisplayName("TRADE should have correct display name and subtitle")
    void testTrade() {
        assertEquals("Trade", ExperienceType.TRADE.getDisplayName());
        assertEquals("Commerce and profit", ExperienceType.TRADE.getSubtitle());
    }

    @Test
    @DisplayName("SOCIAL should have correct display name and subtitle")
    void testSocial() {
        assertEquals("Social Encounter", ExperienceType.SOCIAL.getDisplayName());
        assertEquals("Dealing with others", ExperienceType.SOCIAL.getSubtitle());
    }

    @Test
    @DisplayName("INJURY should have correct display name and subtitle")
    void testInjury() {
        assertEquals("Injury", ExperienceType.INJURY.getDisplayName());
        assertEquals("The cost of conflict", ExperienceType.INJURY.getSubtitle());
    }

    @Test
    @DisplayName("NARROW_ESCAPE should have correct display name and subtitle")
    void testNarrowEscape() {
        assertEquals("Narrow Escape", ExperienceType.NARROW_ESCAPE.getDisplayName());
        assertEquals("Surviving against the odds", ExperienceType.NARROW_ESCAPE.getSubtitle());
    }

    @Test
    @DisplayName("DISCOVERY should have correct display name and subtitle")
    void testDiscovery() {
        assertEquals("Discovery", ExperienceType.DISCOVERY.getDisplayName());
        assertEquals("Uncovering hidden truths", ExperienceType.DISCOVERY.getSubtitle());
    }

    @Test
    @DisplayName("BETRAYAL should have correct display name and subtitle")
    void testBetrayal() {
        assertEquals("Betrayal", ExperienceType.BETRAYAL.getDisplayName());
        assertEquals("Trust broken", ExperienceType.BETRAYAL.getSubtitle());
    }

    @Test
    @DisplayName("REFLECTION should have correct display name and subtitle")
    void testReflection() {
        assertEquals("Reflection", ExperienceType.REFLECTION.getDisplayName());
        assertEquals("Moments of introspection", ExperienceType.REFLECTION.getSubtitle());
    }

    @Test
    @DisplayName("RELATIONSHIP should have correct display name and subtitle")
    void testRelationship() {
        assertEquals("Relationship", ExperienceType.RELATIONSHIP.getDisplayName());
        assertEquals("Bonds with others", ExperienceType.RELATIONSHIP.getSubtitle());
    }

    @Test
    @DisplayName("ACQUISITION should have correct display name and subtitle")
    void testAcquisition() {
        assertEquals("Acquisition", ExperienceType.ACQUISITION.getDisplayName());
        assertEquals("Gaining something valuable", ExperienceType.ACQUISITION.getSubtitle());
    }

    @Test
    @DisplayName("LOSS should have correct display name and subtitle")
    void testLoss() {
        assertEquals("Loss", ExperienceType.LOSS.getDisplayName());
        assertEquals("What was taken away", ExperienceType.LOSS.getSubtitle());
    }

    @Test
    @DisplayName("OTHER should have correct display name and subtitle")
    void testOther() {
        assertEquals("Other", ExperienceType.OTHER.getDisplayName());
        assertEquals("An event beyond categorization", ExperienceType.OTHER.getSubtitle());
    }

    @Test
    @DisplayName("SKILL_GAIN should have correct display name and subtitle")
    void testSkillGain() {
        assertEquals("Skill Gain", ExperienceType.SKILL_GAIN.getDisplayName());
        assertEquals("Learning something new", ExperienceType.SKILL_GAIN.getSubtitle());
    }

    @Test
    @DisplayName("CAREER_START should have correct display name and subtitle")
    void testCareerStart() {
        assertEquals("Career Start", ExperienceType.CAREER_START.getDisplayName());
        assertEquals("Entering a new profession", ExperienceType.CAREER_START.getSubtitle());
    }

    @Test
    @DisplayName("CAREER_COMPLETION should have correct display name and subtitle")
    void testCareerCompletion() {
        assertEquals("Career Completion", ExperienceType.CAREER_COMPLETION.getDisplayName());
        assertEquals("Finishing a term of service", ExperienceType.CAREER_COMPLETION.getSubtitle());
    }

    @Test
    @DisplayName("Should include OTHER type")
    void testOtherTypeExists() {
        assertNotNull(ExperienceType.OTHER);
    }
}
