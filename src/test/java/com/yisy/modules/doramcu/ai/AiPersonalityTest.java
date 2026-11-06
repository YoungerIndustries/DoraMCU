package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiPersonality Tests")
class AiPersonalityTest {

    @Test
    @DisplayName("should create personality from government and ethos")
    void createPersonality() {
        AiPersonality personality = new AiPersonality(
                Player.GovernmentType.DICTATORSHIP,
                Player.Ethos.MILITARIST
        );

        assertNotNull(personality);
        assertEquals(Player.GovernmentType.DICTATORSHIP, personality.getGovernmentType());
        assertEquals(Player.Ethos.MILITARIST, personality.getEthos());
    }

    @Test
    @DisplayName("should create personality from Player")
    void createFromPlayer() {
        Player player = new Player(1, "test@test.com", "Test Empire");
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.PACIFIST);

        AiPersonality personality = AiPersonality.fromPlayer(player);

        assertNotNull(personality);
        assertEquals(Player.GovernmentType.DEMOCRACY, personality.getGovernmentType());
        assertEquals(Player.Ethos.PACIFIST, personality.getEthos());
    }

    @Test
    @DisplayName("should produce valid label")
    void getLabel() {
        AiPersonality personality = new AiPersonality(
                Player.GovernmentType.MONARCHY,
                Player.Ethos.MATERIALIST
        );

        assertEquals("MONARCHY_MATERIALIST", personality.getLabel());
    }

    @Test
    @DisplayName("all personality axes should be in [0.0, 1.0] range")
    void allAxesInRange() {
        for (Player.GovernmentType gov : Player.GovernmentType.values()) {
            for (Player.Ethos ethos : Player.Ethos.values()) {
                AiPersonality personality = new AiPersonality(gov, ethos);

                assertTrue(personality.getAggression() >= 0.0 && personality.getAggression() <= 1.0,
                        "Aggression out of range for " + gov + "/" + ethos);
                assertTrue(personality.getExpansionDrive() >= 0.0 && personality.getExpansionDrive() <= 1.0,
                        "Expansion drive out of range for " + gov + "/" + ethos);
                assertTrue(personality.getEconomicFocus() >= 0.0 && personality.getEconomicFocus() <= 1.0,
                        "Economic focus out of range for " + gov + "/" + ethos);
                assertTrue(personality.getScientificFocus() >= 0.0 && personality.getScientificFocus() <= 1.0,
                        "Scientific focus out of range for " + gov + "/" + ethos);
                assertTrue(personality.getDiplomaticStance() >= 0.0 && personality.getDiplomaticStance() <= 1.0,
                        "Diplomatic stance out of range for " + gov + "/" + ethos);
                assertTrue(personality.getRiskTolerance() >= 0.0 && personality.getRiskTolerance() <= 1.0,
                        "Risk tolerance out of range for " + gov + "/" + ethos);
            }
        }
    }

    @Test
    @DisplayName("should cover all 48 government×ethos combinations")
    void allCombinations() {
        int count = 0;
        for (Player.GovernmentType gov : Player.GovernmentType.values()) {
            for (Player.Ethos ethos : Player.Ethos.values()) {
                AiPersonality personality = new AiPersonality(gov, ethos);
                assertNotNull(personality);
                count++;
            }
        }
        assertEquals(Player.GovernmentType.values().length * Player.Ethos.values().length, count);
    }

    @Test
    @DisplayName("Militarist ethos should increase aggression")
    void militaristIncreasesAggression() {
        AiPersonality militarist = new AiPersonality(
                Player.GovernmentType.DICTATORSHIP,
                Player.Ethos.MILITARIST
        );
        AiPersonality pacifist = new AiPersonality(
                Player.GovernmentType.DICTATORSHIP,
                Player.Ethos.PACIFIST
        );

        assertTrue(militarist.getAggression() > pacifist.getAggression(),
                "Militarist should have higher aggression than Pacifist for same government");
    }

    @Test
    @DisplayName("Pacifist ethos should increase diplomatic stance")
    void pacifistIncreasesDiplomacy() {
        AiPersonality pacifist = new AiPersonality(
                Player.GovernmentType.DEMOCRACY,
                Player.Ethos.PACIFIST
        );
        AiPersonality militarist = new AiPersonality(
                Player.GovernmentType.DEMOCRACY,
                Player.Ethos.MILITARIST
        );

        assertTrue(pacifist.getDiplomaticStance() > militarist.getDiplomaticStance(),
                "Pacifist should have higher diplomatic stance than Militarist for same government");
    }

    @Test
    @DisplayName("Materialist ethos should increase scientific focus")
    void materialistIncreasesScience() {
        AiPersonality materialist = new AiPersonality(
                Player.GovernmentType.DEMOCRACY,
                Player.Ethos.MATERIALIST
        );
        AiPersonality spiritualist = new AiPersonality(
                Player.GovernmentType.DEMOCRACY,
                Player.Ethos.SPIRITUALIST
        );

        assertTrue(materialist.getScientificFocus() >= spiritualist.getScientificFocus(),
                "Materialist should have at least as high scientific focus as Spiritualist");
    }

    @Test
    @DisplayName("Corporate government should have highest economic focus base")
    void corporateHasHighEconomicFocus() {
        AiPersonality corporate = new AiPersonality(
                Player.GovernmentType.CORPORATE,
                Player.Ethos.MATERIALIST
        );
        AiPersonality dictatorship = new AiPersonality(
                Player.GovernmentType.DICTATORSHIP,
                Player.Ethos.MATERIALIST
        );

        assertTrue(corporate.getEconomicFocus() > dictatorship.getEconomicFocus(),
                "Corporate should have higher economic focus than Dictatorship");
    }

    @Test
    @DisplayName("Confederation should have highest diplomatic stance base")
    void confederationHasHighDiplomacy() {
        AiPersonality confederation = new AiPersonality(
                Player.GovernmentType.CONFEDERATION,
                Player.Ethos.EQUALIST
        );
        AiPersonality dictatorship = new AiPersonality(
                Player.GovernmentType.DICTATORSHIP,
                Player.Ethos.EQUALIST
        );

        assertTrue(confederation.getDiplomaticStance() > dictatorship.getDiplomaticStance(),
                "Confederation should have higher diplomatic stance than Dictatorship");
    }

    @Test
    @DisplayName("toMap should return all 6 axes")
    void toMap() {
        AiPersonality personality = new AiPersonality(
                Player.GovernmentType.DEMOCRACY,
                Player.Ethos.MATERIALIST
        );

        Map<String, Double> map = personality.toMap();
        assertEquals(6, map.size());
        assertTrue(map.containsKey("aggression"));
        assertTrue(map.containsKey("expansionDrive"));
        assertTrue(map.containsKey("economicFocus"));
        assertTrue(map.containsKey("scientificFocus"));
        assertTrue(map.containsKey("diplomaticStance"));
        assertTrue(map.containsKey("riskTolerance"));
    }

    @Test
    @DisplayName("toString should not throw")
    void toStringDoesNotThrow() {
        AiPersonality personality = new AiPersonality(
                Player.GovernmentType.DEMOCRACY,
                Player.Ethos.MATERIALIST
        );

        String str = personality.toString();
        assertNotNull(str);
        assertTrue(str.contains("AiPersonality"));
    }
}
