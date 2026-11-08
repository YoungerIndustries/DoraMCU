package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CharacterLogManager Tests")
class CharacterLogManagerTest {

    private GameContext makeContext() {
        return new GameContext() {
            @Override public String getName() { return "Test Game"; }
            @Override public List<Player> getPlayers() { return Collections.emptyList(); }
            @Override public int getCurrentTurn() { return 0; }
            @Override public long getCurrentTime() { return 0; }
            @Override public long getTurnDuration() { return 60; }
            @Override public int getCurrentPlayerIndex() { return 0; }
            @Override public void setCurrentPlayerIndex(int index) {}
            @Override public Player getCurrentPlayer() { return null; }
            @Override public boolean isGameEnded() { return false; }
            @Override public void setGameEnded(boolean ended) {}
            @Override public void incrementTurn() {}
            @Override public Random getRandom() { return new Random(42); }
            @Override public int getActivePlayerCount() { return 0; }
            @Override public Player addPlayer(Player player) { return player; }
            @Override public Player getPlayer(int playerId) { return null; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Planet> getPlanets() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Planet getPlanet(int planetId) { return null; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Fleet> getFleets() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Fleet getFleet(int fleetId) { return null; }
            @Override public com.yisy.modules.doramcu.hex.Fleet addFleet(com.yisy.modules.doramcu.hex.Fleet fleet, Player owner) { return fleet; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Ship> getShips() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Ship getShip(int shipId) { return null; }
            @Override public com.yisy.modules.doramcu.hex.Ship addShip(com.yisy.modules.doramcu.hex.Ship ship) { return ship; }
            @Override public int getNextShipIdAndIncrement() { return 1; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Galaxy> getGalaxies() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Galaxy getGalaxy(int galaxyId) { return null; }
            @Override public com.yisy.modules.doramcu.hex.Galaxy addGalaxy(com.yisy.modules.doramcu.hex.Galaxy galaxy) { return galaxy; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.SolarSystem> getSolarSystems() { return new HashMap<>(); }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Battle> getBattles() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Battle getBattle(int battleId) { return null; }
            @Override public com.yisy.modules.doramcu.hex.Battle addBattle(com.yisy.modules.doramcu.hex.Battle battle) { return battle; }
            @Override public int getNextBattleIdAndIncrement() { return 1; }
            @Override public int getNextPlanetIdAndIncrement() { return 1; }
            @Override public int getNextFleetIdAndIncrement() { return 1; }
            @Override public int getNextSolarSystemIdAndIncrement() { return 1; }
            @Override public List<com.yisy.modules.doramcu.hex.SpaceStation> getSpaceStations() { return Collections.emptyList(); }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Empire> getEmpires() { return new HashMap<>(); }
        };
    }

    // ---- Character creation with logging ----

    @Test
    @DisplayName("Should create character and log creation")
    void testCreateCharacter() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex Starborn", "Sol-3 (Earth)");

        assertNotNull(character);
        assertEquals("Alex Starborn", character.getName());

        CharacterLog log = manager.getLog(character);
        assertNotNull(log);
        assertEquals(1, log.getExperienceCount());
        assertEquals(ExperienceType.CHARACTER_CREATION, log.getExperiences().get(0).getType());
    }

    @Test
    @DisplayName("Should create character with specified abilities")
    void testCreateCharacterWithAbilities() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        Map<AbilityScore, Integer> abilities = new HashMap<>();
        abilities.put(AbilityScore.DEX, 10);
        abilities.put(AbilityScore.ST, 8);
        abilities.put(AbilityScore.END, 12);
        abilities.put(AbilityScore.INT, 9);
        abilities.put(AbilityScore.EDU, 7);
        abilities.put(AbilityScore.SOC, 6);
        abilities.put(AbilityScore.APP, 11);

        TravellerCharacter character = manager.createCharacter("Alex", abilities, "Earth", null);

        assertEquals(10, character.getAbility(AbilityScore.DEX));
        assertEquals(12, character.getAbility(AbilityScore.END));
        assertEquals(11, character.getAbility(AbilityScore.APP));

        CharacterLog log = manager.getLog(character);
        assertNotNull(log);
        assertEquals(1, log.getExperienceCount());
    }

    // ---- Generate full character ----

    @Test
    @DisplayName("Should generate full character with career and log events")
    void testGenerateFullCharacter() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.generateFullCharacter("Alex", "Sol-3");

        assertNotNull(character);
        CharacterLog log = manager.getLog(character);
        assertNotNull(log);

        // Should have at least creation + career start
        assertTrue(log.getExperienceCount() > 1);
        assertTrue(log.getByType(ExperienceType.CHARACTER_CREATION).size() > 0);
        assertTrue(log.getByType(ExperienceType.CAREER_START).size() > 0);
    }

    @Test
    @DisplayName("Should generate full character with specified career")
    void testGenerateFullCharacterWithCareer() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.generateFullCharacter("Alex", "Sol-3", Career.SOLDIER);

        CharacterLog log = manager.getLog(character);
        assertNotNull(log);

        // Should have Soldier career start
        List<CharacterExperience> careerStarts = log.getByType(ExperienceType.CAREER_START);
        assertTrue(careerStarts.size() > 0);
        assertTrue(careerStarts.get(0).getTitle().contains("Soldier"));
    }

    // ---- Career with logging ----

    @Test
    @DisplayName("Should log career events when serving career")
    void testServeCareerWithLogging() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.serveCareerWithLogging(character, Career.SOLDIER);

        CharacterLog log = manager.getLog(character);
        assertNotNull(log);

        // Should have career start
        assertTrue(log.getByType(ExperienceType.CAREER_START).size() > 0);
        // Should have career completions
        assertTrue(log.getByType(ExperienceType.CAREER_COMPLETION).size() > 0);
        // Should have skill gains
        assertTrue(log.getByType(ExperienceType.SKILL_GAIN).size() > 0);
    }

    @Test
    @DisplayName("Should serve career with specified number of terms")
    void testServeCareerWithLoggingFixedTerms() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.serveCareerWithLogging(character, Career.SOLDIER, 2);

        CharacterLog log = manager.getLog(character);
        assertNotNull(log);

        // Should have career start
        assertTrue(log.getByType(ExperienceType.CAREER_START).size() > 0);
        // Should have at least 1 career completion (may retire early)
        assertTrue(log.getByType(ExperienceType.CAREER_COMPLETION).size() >= 1);
    }

    @Test
    @DisplayName("Should log retirement at end of career")
    void testCareerRetirementLogging() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.serveCareerWithLogging(character, Career.SOLDIER);

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> retirements = log.getByType(ExperienceType.RETIREMENT);
        assertTrue(retirements.size() > 0, "Should have at least one retirement entry");
    }

    @Test
    @DisplayName("Should handle single term career")
    void testServeCareerSingleTerm() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.serveCareerWithLogging(character, Career.NOBLE, 1);

        CharacterLog log = manager.getLog(character);
        assertNotNull(log);
        assertTrue(log.getByType(ExperienceType.CAREER_START).size() > 0);
        assertTrue(log.getByType(ExperienceType.CAREER_COMPLETION).size() >= 1);
    }

    // ---- Mission and adventure logging ----

    @Test
    @DisplayName("Should log battle")
    void testLogBattle() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logBattle(character, "Battle of Tarn", "Tarn VI",
                CharacterExperience.Outcome.SUCCESS, "They won the day.");

        CharacterLog log = manager.getLog(character);
        assertEquals(2, log.getExperienceCount()); // creation + battle
        CharacterExperience battle = log.getByType(ExperienceType.BATTLE).get(0);
        assertEquals("Tarn VI", battle.getLocation());
        assertEquals(CharacterExperience.Outcome.SUCCESS, battle.getOutcome());
    }

    @Test
    @DisplayName("Should log successful mission")
    void testLogSuccessfulMission() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logMission(character, "Rescue", "Lone Mountain", true, "Saved them all.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> missions = log.getByType(ExperienceType.MISSION_SUCCESS);
        assertEquals(1, missions.size());
        assertEquals("Lone Mountain", missions.get(0).getLocation());
    }

    @Test
    @DisplayName("Should log failed mission")
    void testLogFailedMission() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logMission(character, "Rescue", "Lone Mountain", false, "It went wrong.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> missions = log.getByType(ExperienceType.MISSION_FAILURE);
        assertEquals(1, missions.size());
    }

    @Test
    @DisplayName("Should log exploration")
    void testLogExploration() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExploration(character, "New World", "Stranger Moon", "A strange place.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> explorations = log.getByType(ExperienceType.EXPLORATION);
        assertEquals(1, explorations.size());
        assertEquals("Stranger Moon", explorations.get(0).getLocation());
    }

    @Test
    @DisplayName("Should log custom experience")
    void testLogExperience() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExperience(character, ExperienceType.HEROISM, "Heroic Act",
                "Saved the crew", "A true hero emerged.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> heroisms = log.getByType(ExperienceType.HEROISM);
        assertEquals(1, heroisms.size());
        assertEquals("Heroic Act", heroisms.get(0).getTitle());
    }

    // ---- Log retrieval ----

    @Test
    @DisplayName("Should retrieve log by character")
    void testGetLogByCharacter() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        CharacterLog log = manager.getLog(character);
        assertNotNull(log);
        assertSame(character, log.getCharacter());
    }

    @Test
    @DisplayName("Should retrieve log by ID")
    void testGetLogById() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        CharacterLog log = manager.getLog(character.getId());
        assertNotNull(log);
        assertSame(character, log.getCharacter());
    }

    @Test
    @DisplayName("Should return null for unknown character ID")
    void testGetLogUnknownId() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        assertNull(manager.getLog(99999));
    }

    @Test
    @DisplayName("Should return all logs")
    void testGetAllLogs() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        manager.createCharacter("Alex", "Earth");
        manager.createCharacter("Bob", "Mars");

        Map<Integer, CharacterLog> allLogs = manager.getAllLogs();
        assertEquals(2, allLogs.size());

        // Verify immutability
        assertThrows(UnsupportedOperationException.class, () -> allLogs.put(999, new CharacterLog(
                new TravellerCharacter(999, "Test"))));
    }

    @Test
    @DisplayName("Should get or create log for existing character")
    void testGetOrCreateLogExisting() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        CharacterLog log1 = manager.getOrCreateLog(character);
        CharacterLog log2 = manager.getOrCreateLog(character);
        assertSame(log1, log2);
    }

    @Test
    @DisplayName("Should create log for character not created via manager")
    void testGetOrCreateLogNew() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = new TravellerCharacter(42, "Manual Char");
        character.rollAbilities();

        CharacterLog log = manager.getOrCreateLog(character);
        assertNotNull(log);
        assertSame(character, log.getCharacter());
    }

    // ---- Character manager access ----

    @Test
    @DisplayName("Should provide access to underlying character manager")
    void testGetCharacterManager() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        assertNotNull(manager.getCharacterManager());
    }

    // ---- Integration: full lifecycle ----

    @Test
    @DisplayName("Full lifecycle: create, career, missions, book")
    void testFullLifecycle() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());

        // Create character
        TravellerCharacter character = manager.createCharacter("Alex", "Sol-3");

        // Serve career
        manager.serveCareerWithLogging(character, Career.SOLDIER);

        // Log adventures
        manager.logBattle(character, "Battle of Tarn", "Tarn VI",
                CharacterExperience.Outcome.SUCCESS, "Victory!");
        manager.logMission(character, "Rescue", "Lone Mountain", true, "Saved them.");
        manager.logExploration(character, "New World", "Stranger Moon", "Beautiful.");

        // Generate book
        CharacterLog log = manager.getLog(character);
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);

        // Verify book contains all parts
        assertTrue(book.contains("Alex"));
        assertTrue(book.contains("THE CHRONICLES OF"));
        assertTrue(book.contains("TABLE OF CONTENTS"));
        assertTrue(book.contains("EPILOGUE"));
        assertTrue(book.contains("END OF CHRONICLE"));
    }

    // ---- Multiple careers ----

    @Test
    @DisplayName("Should handle multiple careers for same character")
    void testMultipleCareers() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.serveCareerWithLogging(character, Career.SOLDIER, 1);
        manager.serveCareerWithLogging(character, Career.MERCHANT, 1);

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> careerStarts = log.getByType(ExperienceType.CAREER_START);
        assertEquals(2, careerStarts.size());

        boolean hasSoldier = careerStarts.stream().anyMatch(e -> e.getTitle().contains("Soldier"));
        boolean hasMerchant = careerStarts.stream().anyMatch(e -> e.getTitle().contains("Merchant"));
        assertTrue(hasSoldier);
        assertTrue(hasMerchant);
    }

    // ---- Multiple characters ----

    @Test
    @DisplayName("Should maintain separate logs for multiple characters")
    void testMultipleCharacterLogs() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter char1 = manager.createCharacter("Alex", "Earth");
        TravellerCharacter char2 = manager.createCharacter("Bob", "Mars");

        manager.logBattle(char1, "Battle 1", "World A",
                CharacterExperience.Outcome.SUCCESS, "Won");
        manager.logBattle(char2, "Battle 2", "World B",
                CharacterExperience.Outcome.FAILURE, "Lost");

        CharacterLog log1 = manager.getLog(char1);
        CharacterLog log2 = manager.getLog(char2);

        // Alex's log should have creation + battle
        assertEquals(2, log1.getExperienceCount());
        assertEquals(1, log1.getByType(ExperienceType.BATTLE).size());
        assertEquals("World A", log1.getByType(ExperienceType.BATTLE).get(0).getLocation());

        // Bob's log should have creation + battle
        assertEquals(2, log2.getExperienceCount());
        assertEquals(1, log2.getByType(ExperienceType.BATTLE).size());
        assertEquals("World B", log2.getByType(ExperienceType.BATTLE).get(0).getLocation());
    }

    // ---- Log experience with various types ----

    @Test
    @DisplayName("Should log injury experience")
    void testLogInjuryExperience() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExperience(character, ExperienceType.INJURY, "Wounded in Combat",
                "Lost an arm", "The blast took everything.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> injuries = log.getByType(ExperienceType.INJURY);
        assertEquals(1, injuries.size());
    }

    @Test
    @DisplayName("Should log discovery experience")
    void testLogDiscoveryExperience() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExperience(character, ExperienceType.DISCOVERY, "Ancient Ruins",
                "Found alien ruins", "The ruins predated all known civilizations.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> discoveries = log.getByType(ExperienceType.DISCOVERY);
        assertEquals(1, discoveries.size());
    }

    @Test
    @DisplayName("Should log betrayal experience")
    void testLogBetrayalExperience() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExperience(character, ExperienceType.BETRAYAL, "Stabbed in the Back",
                "Partner betrayed us", "Trust was shattered.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> betrayals = log.getByType(ExperienceType.BETRAYAL);
        assertEquals(1, betrayals.size());
    }

    @Test
    @DisplayName("Should log relationship experience")
    void testLogRelationshipExperience() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExperience(character, ExperienceType.RELATIONSHIP, "New Ally",
                "Met a trusted friend", "A bond formed in the heat of battle.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> relationships = log.getByType(ExperienceType.RELATIONSHIP);
        assertEquals(1, relationships.size());
    }

    @Test
    @DisplayName("Should log acquisition experience")
    void testLogAcquisitionExperience() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExperience(character, ExperienceType.ACQUISITION, "New Ship",
                "Purchased a Starliner", "The ship was fast and reliable.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> acquisitions = log.getByType(ExperienceType.ACQUISITION);
        assertEquals(1, acquisitions.size());
    }

    @Test
    @DisplayName("Should log loss experience")
    void testLogLossExperience() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExperience(character, ExperienceType.LOSS, "Ship Destroyed",
                "Lost everything", "The flames consumed all.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> losses = log.getByType(ExperienceType.LOSS);
        assertEquals(1, losses.size());
    }

    @Test
    @DisplayName("Should log reflection experience")
    void testLogReflectionExperience() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExperience(character, ExperienceType.REFLECTION, "Quiet Moment",
                "Reflected on the journey", "The stars seemed to whisper.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> reflections = log.getByType(ExperienceType.REFLECTION);
        assertEquals(1, reflections.size());
    }

    @Test
    @DisplayName("Should log narrow escape experience")
    void testLogNarrowEscapeExperience() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = manager.createCharacter("Alex", "Earth");

        manager.logExperience(character, ExperienceType.NARROW_ESCAPE, "Barely Survived",
                "Escaped a collapsing station", "Seconds from death.");

        CharacterLog log = manager.getLog(character);
        List<CharacterExperience> escapes = log.getByType(ExperienceType.NARROW_ESCAPE);
        assertEquals(1, escapes.size());
    }

    // ---- getAllLogs empty ----

    @Test
    @DisplayName("getAllLogs should return empty map when no characters")
    void testGetAllLogsEmpty() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        Map<Integer, CharacterLog> allLogs = manager.getAllLogs();
        assertNotNull(allLogs);
        assertTrue(allLogs.isEmpty());
    }

    // ---- getLog returns null for unknown character ----

    @Test
    @DisplayName("getLog should return null for character not in manager")
    void testGetLogUnknownCharacter() {
        CharacterLogManager manager = new CharacterLogManager(makeContext());
        TravellerCharacter character = new TravellerCharacter(999, "Unknown");
        character.rollAbilities();
        assertNull(manager.getLog(character));
    }
}
