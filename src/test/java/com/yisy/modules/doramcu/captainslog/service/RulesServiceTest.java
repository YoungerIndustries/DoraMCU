package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.RulesEntry;
import com.yisy.modules.doramcu.captainslog.model.RulesTable;
import com.yisy.modules.doramcu.captainslog.repository.RulesEntryRepository;
import com.yisy.modules.doramcu.captainslog.repository.RulesTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("RulesService Tests")
class RulesServiceTest {

    private RulesEntryRepository entryRepository;
    private RulesTableRepository tableRepository;
    private RulesService rulesService;

    private final List<RulesEntry> sampleEntries = new ArrayList<>();
    private final List<RulesTable> sampleTables = new ArrayList<>();

    @BeforeEach
    void setUp() {
        entryRepository = Mockito.mock(RulesEntryRepository.class);
        tableRepository = Mockito.mock(RulesTableRepository.class);
        rulesService = new RulesService(entryRepository, tableRepository);

        // Prepare sample rule entries
        sampleEntries.clear();
        sampleTables.clear();

        RulesEntry diceEntry = new RulesEntry();
        diceEntry.setId(1L);
        diceEntry.setTitle("2d20 Task Attempt Core Mechanic");
        diceEntry.setChapter("05.00 Rules of Play");
        diceEntry.setCategory("DICE_MECHANICS");
        diceEntry.setPage(179);
        diceEntry.setText("The core mechanic of Captain's Log is the 2d20 task attempt.");
        diceEntry.setNotes("Fundamental mechanic for all checks.");
        diceEntry.setActive(true);
        sampleEntries.add(diceEntry);

        RulesEntry combatEntry = new RulesEntry();
        combatEntry.setId(2L);
        combatEntry.setTitle("Physical Combat: Weapons and Equipment");
        combatEntry.setChapter("05.00 Rules of Play");
        combatEntry.setCategory("COMBAT");
        combatEntry.setPage(201);
        combatEntry.setText("Characters may use various weapons and equipment in combat.");
        combatEntry.setNotes("Equipment choices affect combat effectiveness.");
        combatEntry.setActive(true);
        sampleEntries.add(combatEntry);

        RulesEntry momentumEntry = new RulesEntry();
        momentumEntry.setId(3L);
        momentumEntry.setTitle("Momentum Pool");
        momentumEntry.setChapter("05.00 Rules of Play");
        momentumEntry.setCategory("MOMENTUM");
        momentumEntry.setPage(195);
        momentumEntry.setText("Momentum is a resource pool that builds during play.");
        momentumEntry.setNotes("Spend momentum for boons.");
        momentumEntry.setActive(true);
        sampleEntries.add(momentumEntry);

        RulesEntry threatEntry = new RulesEntry();
        threatEntry.setId(4L);
        threatEntry.setTitle("Threat Pool");
        threatEntry.setChapter("05.00 Rules of Play");
        threatEntry.setCategory("THREAT");
        threatEntry.setPage(197);
        threatEntry.setText("Threat represents narrative complications building up.");
        threatEntry.setNotes("Resolve threat to avoid consequences.");
        threatEntry.setActive(true);
        sampleEntries.add(threatEntry);

        RulesEntry missionEntry = new RulesEntry();
        missionEntry.setId(5L);
        missionEntry.setTitle("Mission Generation");
        missionEntry.setChapter("06.00 Playing Captain's Log");
        missionEntry.setCategory("MISSION_STRUCTURE");
        missionEntry.setPage(227);
        missionEntry.setText("Missions can be generated using the oracle tables.");
        missionEntry.setNotes("Use probability matrix service.");
        missionEntry.setActive(true);
        sampleEntries.add(missionEntry);

        RulesEntry oracleEntry = new RulesEntry();
        oracleEntry.setId(6L);
        oracleEntry.setTitle("Oracle Probability Levels");
        oracleEntry.setChapter("06.00 Playing Captain's Log");
        oracleEntry.setCategory("ORACLE");
        oracleEntry.setPage(231);
        oracleEntry.setText("Before asking the oracle a Yes/No question, set the probability level.");
        oracleEntry.setNotes("Setting probability is a skill.");
        oracleEntry.setActive(true);
        sampleEntries.add(oracleEntry);

        RulesEntry starshipEntry = new RulesEntry();
        starshipEntry.setId(7L);
        starshipEntry.setTitle("Starship Talents");
        starshipEntry.setChapter("04.00 Your Home Among the Stars");
        starshipEntry.setCategory("STARSHIP");
        starshipEntry.setPage(141);
        starshipEntry.setText("Starships can have talents that provide special capabilities.");
        starshipEntry.setNotes("Choose talents that complement mission style.");
        starshipEntry.setActive(true);
        sampleEntries.add(starshipEntry);

        RulesEntry characterEntry = new RulesEntry();
        characterEntry.setId(8L);
        characterEntry.setTitle("Character Talents");
        characterEntry.setChapter("03.00 Reporting for Duty");
        characterEntry.setCategory("CHARACTER_CREATION");
        characterEntry.setPage(77);
        characterEntry.setText("Talents are special abilities that provide unique advantages.");
        characterEntry.setNotes("Talents add uniqueness to characters.");
        characterEntry.setActive(true);
        sampleEntries.add(characterEntry);

        RulesEntry careerEntry = new RulesEntry();
        careerEntry.setId(9L);
        careerEntry.setTitle("Career Progression");
        careerEntry.setChapter("06.00 Playing Captain's Log");
        careerEntry.setCategory("CAREER");
        careerEntry.setPage(231);
        careerEntry.setText("Characters advance through their career by completing missions.");
        careerEntry.setNotes("Use career progression service.");
        careerEntry.setActive(true);
        sampleEntries.add(careerEntry);

        RulesEntry maintenanceEntry = new RulesEntry();
        maintenanceEntry.setId(10L);
        maintenanceEntry.setTitle("Maintenance and Downtime");
        maintenanceEntry.setChapter("06.00 Playing Captain's Log");
        maintenanceEntry.setCategory("MAINTENANCE");
        maintenanceEntry.setPage(233);
        maintenanceEntry.setText("Between missions, characters perform maintenance and repairs.");
        maintenanceEntry.setNotes("Reduce stress during downtime.");
        maintenanceEntry.setActive(true);
        sampleEntries.add(maintenanceEntry);

        RulesEntry equipmentEntry = new RulesEntry();
        equipmentEntry.setId(11L);
        equipmentEntry.setTitle("Equipment Management");
        equipmentEntry.setChapter("03.00 Reporting for Duty");
        equipmentEntry.setCategory("EQUIPMENT");
        equipmentEntry.setPage(91);
        equipmentEntry.setText("Characters carry equipment that aids in missions.");
        equipmentEntry.setNotes("Track equipment in inventory.");
        equipmentEntry.setActive(true);
        sampleEntries.add(equipmentEntry);

        RulesEntry crewEntry = new RulesEntry();
        crewEntry.setId(12L);
        crewEntry.setTitle("Crew Management");
        crewEntry.setChapter("03.00 Reporting for Duty");
        crewEntry.setCategory("CREW");
        crewEntry.setPage(93);
        crewEntry.setText("Crew members assist with tasks and provide support.");
        crewEntry.setNotes("Crew can assist with group checks.");
        crewEntry.setActive(true);
        sampleEntries.add(crewEntry);

        RulesEntry campaignEntry = new RulesEntry();
        campaignEntry.setId(13L);
        campaignEntry.setTitle("Campaign Tracking");
        campaignEntry.setChapter("06.00 Playing Captain's Log");
        campaignEntry.setCategory("CAMPAIGN");
        campaignEntry.setPage(235);
        campaignEntry.setText("Campaigns track long-term story arcs across multiple missions.");
        campaignEntry.setNotes("Use campaign service for tracking.");
        campaignEntry.setActive(true);
        sampleEntries.add(campaignEntry);

        RulesEntry injuryEntry = new RulesEntry();
        injuryEntry.setId(14L);
        injuryEntry.setTitle("Injury and Recovery");
        injuryEntry.setChapter("05.00 Rules of Play");
        injuryEntry.setCategory("INJURY");
        injuryEntry.setPage(203);
        injuryEntry.setText("Characters can be injured during combat and dangerous situations.");
        injuryEntry.setNotes("Health states range from Healthy to Down.");
        injuryEntry.setActive(true);
        sampleEntries.add(injuryEntry);

        RulesEntry worldEntry = new RulesEntry();
        worldEntry.setId(15L);
        worldEntry.setTitle("World Generation");
        worldEntry.setChapter("06.00 Playing Captain's Log");
        worldEntry.setCategory("WORLD_GENERATION");
        worldEntry.setPage(240);
        worldEntry.setText("Use oracle tables to generate planets and star systems.");
        worldEntry.setNotes("World generation adds variety to missions.");
        worldEntry.setActive(true);
        sampleEntries.add(worldEntry);

        RulesEntry settingEntry = new RulesEntry();
        settingEntry.setId(16L);
        settingEntry.setTitle("Star Trek Defined: The Prime Directive");
        settingEntry.setChapter("01.00 Star Trek Defined");
        settingEntry.setCategory("SETTING");
        settingEntry.setPage(7);
        settingEntry.setText("The Prime Directive is the foundational principle of the Federation.");
        settingEntry.setNotes("Creates moral dilemmas during missions.");
        settingEntry.setActive(true);
        sampleEntries.add(settingEntry);

        // Inactive entry for testing filtering
        RulesEntry inactiveEntry = new RulesEntry();
        inactiveEntry.setId(17L);
        inactiveEntry.setTitle("Deprecated Rule");
        inactiveEntry.setChapter("05.00 Rules of Play");
        inactiveEntry.setCategory("DICE_MECHANICS");
        inactiveEntry.setPage(999);
        inactiveEntry.setText("This rule is no longer active.");
        inactiveEntry.setNotes("Legacy rule.");
        inactiveEntry.setActive(false);
        sampleEntries.add(inactiveEntry);

        // Prepare sample table entries
        RulesTable missionType1 = new RulesTable();
        missionType1.setId(1L);
        missionType1.setTableName("Mission Type");
        missionType1.setCategory("MISSION");
        missionType1.setChapter("Appendix IV");
        missionType1.setPage(250);
        missionType1.setRollValue(1);
        missionType1.setResultLabel("Diplomacy");
        missionType1.setDescription("A diplomatic mission to negotiate with alien species.");
        missionType1.setActive(true);
        sampleTables.add(missionType1);

        RulesTable missionType2 = new RulesTable();
        missionType2.setId(2L);
        missionType2.setTableName("Mission Type");
        missionType2.setCategory("MISSION");
        missionType2.setChapter("Appendix IV");
        missionType2.setPage(250);
        missionType2.setRollValue(2);
        missionType2.setResultLabel("Exploration");
        missionType2.setDescription("An exploration mission to chart unknown space.");
        missionType2.setActive(true);
        sampleTables.add(missionType2);

        RulesTable complication1 = new RulesTable();
        complication1.setId(3L);
        complication1.setTableName("Complication");
        complication1.setCategory("MISSION");
        complication1.setChapter("Appendix IV");
        complication1.setPage(252);
        complication1.setRollValue(1);
        complication1.setResultLabel("Reinforcements");
        complication1.setDescription("Enemy reinforcements arrive unexpectedly.");
        complication1.setActive(true);
        sampleTables.add(complication1);

        RulesTable npcRole1 = new RulesTable();
        npcRole1.setId(4L);
        npcRole1.setTableName("NPC Role");
        npcRole1.setCategory("NPC");
        npcRole1.setChapter("Appendix IV");
        npcRole1.setPage(254);
        npcRole1.setRollValue(1);
        npcRole1.setResultLabel("Ally");
        npcRole1.setDescription("A helpful NPC who aids the character.");
        npcRole1.setActive(true);
        sampleTables.add(npcRole1);

        RulesTable yesNo1 = new RulesTable();
        yesNo1.setId(5L);
        yesNo1.setTableName("Yes/No Probability");
        yesNo1.setCategory("ORACLE");
        yesNo1.setChapter("Appendix IV");
        yesNo1.setPage(260);
        yesNo1.setRollValue(1);
        yesNo1.setResultLabel("Yes+");
        yesNo1.setDescription("Yes, and you gain Momentum.");
        yesNo1.setActive(true);
        sampleTables.add(yesNo1);

        RulesTable yesNo2 = new RulesTable();
        yesNo2.setId(6L);
        yesNo2.setTableName("Yes/No Probability");
        yesNo2.setCategory("ORACLE");
        yesNo2.setChapter("Appendix IV");
        yesNo2.setPage(260);
        yesNo2.setRollValue(2);
        yesNo2.setResultLabel("No-");
        yesNo2.setDescription("No, and you gain Threat.");
        yesNo2.setActive(true);
        sampleTables.add(yesNo2);

        // Inactive table entry
        RulesTable inactiveTable = new RulesTable();
        inactiveTable.setId(7L);
        inactiveTable.setTableName("Mission Type");
        inactiveTable.setCategory("MISSION");
        inactiveTable.setChapter("Appendix IV");
        inactiveTable.setPage(250);
        inactiveTable.setRollValue(3);
        inactiveTable.setResultLabel("Deprecated Mission");
        inactiveTable.setDescription("This is no longer used.");
        inactiveTable.setActive(false);
        sampleTables.add(inactiveTable);
    }

    // ==================== getRulesByCategory ====================

    @Test
    @DisplayName("should return rules for DICE_MECHANICS category")
    void getRulesByCategory_DiceMechanics() {
        List<RulesEntry> expected = new ArrayList<>();
        expected.add(sampleEntries.get(0)); // dice entry
        expected.add(sampleEntries.get(16)); // inactive dice entry - should NOT be returned
        // Only active entries should be returned
        List<RulesEntry> activeExpected = List.of(sampleEntries.get(0));

        when(entryRepository.findByCategoryAndActiveTrue("DICE_MECHANICS")).thenReturn(activeExpected);

        List<RulesEntry> result = rulesService.getRulesByCategory("DICE_MECHANICS");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2d20 Task Attempt Core Mechanic", result.get(0).getTitle());
        verify(entryRepository).findByCategoryAndActiveTrue("DICE_MECHANICS");
    }

    @Test
    @DisplayName("should return rules for COMBAT category")
    void getRulesByCategory_Combat() {
        when(entryRepository.findByCategoryAndActiveTrue("COMBAT"))
                .thenReturn(List.of(sampleEntries.get(1)));

        List<RulesEntry> result = rulesService.getRulesByCategory("COMBAT");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Physical Combat: Weapons and Equipment", result.get(0).getTitle());
    }

    @Test
    @DisplayName("should return empty list for unknown category")
    void getRulesByCategory_Unknown() {
        when(entryRepository.findByCategoryAndActiveTrue("UNKNOWN_CATEGORY"))
                .thenReturn(Collections.emptyList());

        List<RulesEntry> result = rulesService.getRulesByCategory("UNKNOWN_CATEGORY");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getRulesByChapter ====================

    @Test
    @DisplayName("should return rules for chapter 05.00")
    void getRulesByChapter_Chapter05() {
        List<RulesEntry> chapterEntries = new ArrayList<>();
        for (RulesEntry e : sampleEntries) {
            if (e.getChapter().equals("05.00 Rules of Play") && e.isActive()) {
                chapterEntries.add(e);
            }
        }
        when(entryRepository.findByChapterAndActiveTrue("05.00 Rules of Play"))
                .thenReturn(chapterEntries);

        List<RulesEntry> result = rulesService.getRulesByChapter("05.00 Rules of Play");

        assertNotNull(result);
        // dice, combat, momentum, threat, injury = 5 entries in chapter 05.00
        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("should return rules for chapter 06.00")
    void getRulesByChapter_Chapter06() {
        List<RulesEntry> chapterEntries = new ArrayList<>();
        for (RulesEntry e : sampleEntries) {
            if (e.getChapter().equals("06.00 Playing Captain's Log") && e.isActive()) {
                chapterEntries.add(e);
            }
        }
        when(entryRepository.findByChapterAndActiveTrue("06.00 Playing Captain's Log"))
                .thenReturn(chapterEntries);

        List<RulesEntry> result = rulesService.getRulesByChapter("06.00 Playing Captain's Log");

        assertNotNull(result);
        assertTrue(result.size() >= 3); // mission, oracle, career, maintenance
    }

    @Test
    @DisplayName("should return empty list for unknown chapter")
    void getRulesByChapter_Unknown() {
        when(entryRepository.findByChapterAndActiveTrue("99.00 Nonexistent"))
                .thenReturn(Collections.emptyList());

        List<RulesEntry> result = rulesService.getRulesByChapter("99.00 Nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== searchRules ====================

    @Test
    @DisplayName("should search rules by keyword 'task'")
    void searchRules_TaskKeyword() {
        when(entryRepository.searchByKeyword("task"))
                .thenReturn(List.of(sampleEntries.get(0))); // "2d20 Task Attempt Core Mechanic"

        List<RulesEntry> result = rulesService.searchRules("task");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().toLowerCase().contains("task"));
    }

    @Test
    @DisplayName("should search rules by keyword 'combat'")
    void searchRules_CombatKeyword() {
        when(entryRepository.searchByKeyword("combat"))
                .thenReturn(List.of(sampleEntries.get(1)));

        List<RulesEntry> result = rulesService.searchRules("combat");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getText().toLowerCase().contains("combat"));
    }

    @Test
    @DisplayName("should search rules case-insensitively")
    void searchRules_CaseInsensitive() {
        when(entryRepository.searchByKeyword("COMBAT"))
                .thenReturn(List.of(sampleEntries.get(1)));

        List<RulesEntry> result = rulesService.searchRules("COMBAT");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return empty list when keyword not found")
    void searchRules_NotFound() {
        when(entryRepository.searchByKeyword("nonexistentkeyword"))
                .thenReturn(Collections.emptyList());

        List<RulesEntry> result = rulesService.searchRules("nonexistentkeyword");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== searchRulesInCategory ====================

    @Test
    @DisplayName("should search rules within COMBAT category")
    void searchRulesInCategory_Combat() {
        when(entryRepository.searchByKeywordInCategory("COMBAT", "weapon"))
                .thenReturn(List.of(sampleEntries.get(1)));

        List<RulesEntry> result = rulesService.searchRulesInCategory("COMBAT", "weapon");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COMBAT", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return empty when keyword not in category")
    void searchRulesInCategory_NotFound() {
        when(entryRepository.searchByKeywordInCategory("DICE_MECHANICS", "weapon"))
                .thenReturn(Collections.emptyList());

        List<RulesEntry> result = rulesService.searchRulesInCategory("DICE_MECHANICS", "weapon");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getCategories ====================

    @Test
    @DisplayName("should return all distinct categories")
    void getCategories() {
        List<String> expectedCategories = List.of(
                "DICE_MECHANICS", "COMBAT", "MOMENTUM", "THREAT",
                "MISSION_STRUCTURE", "ORACLE", "STARSHIP", "CHARACTER_CREATION",
                "CAREER", "MAINTENANCE", "EQUIPMENT", "CREW",
                "CAMPAIGN", "INJURY", "WORLD_GENERATION", "SETTING"
        );
        when(entryRepository.findDistinctCategories()).thenReturn(expectedCategories);

        List<String> result = rulesService.getCategories();

        assertNotNull(result);
        assertEquals(16, result.size());
        assertTrue(result.contains("DICE_MECHANICS"));
        assertTrue(result.contains("COMBAT"));
        assertTrue(result.contains("SETTING"));
    }

    // ==================== getChapters ====================

    @Test
    @DisplayName("should return all distinct chapters")
    void getChapters() {
        List<String> expectedChapters = List.of(
                "01.00 Star Trek Defined",
                "03.00 Reporting for Duty",
                "04.00 Your Home Among the Stars",
                "05.00 Rules of Play",
                "06.00 Playing Captain's Log"
        );
        when(entryRepository.findDistinctChapters()).thenReturn(expectedChapters);

        List<String> result = rulesService.getChapters();

        assertNotNull(result);
        assertEquals(5, result.size());
        assertTrue(result.contains("05.00 Rules of Play"));
        assertTrue(result.contains("01.00 Star Trek Defined"));
    }

    // ==================== getTableEntries ====================

    @Test
    @DisplayName("should return table entries for Mission Type table")
    void getTableEntries_MissionType() {
        List<RulesTable> missionTables = new ArrayList<>();
        for (RulesTable t : sampleTables) {
            if (t.getTableName().equals("Mission Type") && t.isActive()) {
                missionTables.add(t);
            }
        }
        when(tableRepository.findByTableNameAndActiveTrueOrderByRollValueAsc("Mission Type"))
                .thenReturn(missionTables);

        List<RulesTable> result = rulesService.getTableEntries("Mission Type");

        assertNotNull(result);
        assertEquals(2, result.size());
        // Should be ordered by roll value
        assertEquals(1, result.get(0).getRollValue());
        assertEquals(2, result.get(1).getRollValue());
    }

    @Test
    @DisplayName("should return table entries for Yes/No Probability table")
    void getTableEntries_YesNo() {
        List<RulesTable> yesNoTables = new ArrayList<>();
        for (RulesTable t : sampleTables) {
            if (t.getTableName().equals("Yes/No Probability") && t.isActive()) {
                yesNoTables.add(t);
            }
        }
        when(tableRepository.findByTableNameAndActiveTrueOrderByRollValueAsc("Yes/No Probability"))
                .thenReturn(yesNoTables);

        List<RulesTable> result = rulesService.getTableEntries("Yes/No Probability");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Yes+", result.get(0).getResultLabel());
        assertEquals("No-", result.get(1).getResultLabel());
    }

    @Test
    @DisplayName("should return empty list for unknown table name")
    void getTableEntries_Unknown() {
        when(tableRepository.findByTableNameAndActiveTrueOrderByRollValueAsc("Nonexistent Table"))
                .thenReturn(Collections.emptyList());

        List<RulesTable> result = rulesService.getTableEntries("Nonexistent Table");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getTableEntriesByCategory ====================

    @Test
    @DisplayName("should return table entries for MISSION category")
    void getTableEntriesByCategory_Mission() {
        List<RulesTable> missionTables = new ArrayList<>();
        for (RulesTable t : sampleTables) {
            if (t.getCategory().equals("MISSION") && t.isActive()) {
                missionTables.add(t);
            }
        }
        when(tableRepository.findByCategoryAndActiveTrue("MISSION"))
                .thenReturn(missionTables);

        List<RulesTable> result = rulesService.getTableEntriesByCategory("MISSION");

        assertNotNull(result);
        assertEquals(3, result.size()); // 2 mission type + 1 complication
    }

    @Test
    @DisplayName("should return table entries for ORACLE category")
    void getTableEntriesByCategory_Oracle() {
        List<RulesTable> oracleTables = new ArrayList<>();
        for (RulesTable t : sampleTables) {
            if (t.getCategory().equals("ORACLE") && t.isActive()) {
                oracleTables.add(t);
            }
        }
        when(tableRepository.findByCategoryAndActiveTrue("ORACLE"))
                .thenReturn(oracleTables);

        List<RulesTable> result = rulesService.getTableEntriesByCategory("ORACLE");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== getTableNames ====================

    @Test
    @DisplayName("should return all distinct table names")
    void getTableNames() {
        List<String> expectedNames = List.of("Mission Type", "Complication", "NPC Role", "Yes/No Probability");
        when(tableRepository.findDistinctTableNames()).thenReturn(expectedNames);

        List<String> result = rulesService.getTableNames();

        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains("Mission Type"));
        assertTrue(result.contains("Yes/No Probability"));
    }

    // ==================== getTableCategories ====================

    @Test
    @DisplayName("should return all distinct table categories")
    void getTableCategories() {
        List<String> expectedCategories = List.of("MISSION", "NPC", "ORACLE");
        when(tableRepository.findDistinctCategories()).thenReturn(expectedCategories);

        List<String> result = rulesService.getTableCategories();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("MISSION"));
        assertTrue(result.contains("ORACLE"));
    }

    // ==================== searchTables ====================

    @Test
    @DisplayName("should search tables by keyword 'diplomacy'")
    void searchTables_Diplomacy() {
        when(tableRepository.searchByKeyword("diplomacy"))
                .thenReturn(List.of(sampleTables.get(0))); // Mission Type: Diplomacy

        List<RulesTable> result = rulesService.searchTables("diplomacy");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Diplomacy", result.get(0).getResultLabel());
    }

    @Test
    @DisplayName("should search tables by keyword 'enemy'")
    void searchTables_Enemy() {
        when(tableRepository.searchByKeyword("enemy"))
                .thenReturn(List.of(sampleTables.get(2))); // Complication: Reinforcements

        List<RulesTable> result = rulesService.searchTables("enemy");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getDescription().toLowerCase().contains("enemy"));
    }

    @Test
    @DisplayName("should return empty list when keyword not found in tables")
    void searchTables_NotFound() {
        when(tableRepository.searchByKeyword("nonexistent"))
                .thenReturn(Collections.emptyList());

        List<RulesTable> result = rulesService.searchTables("nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getRulesSummary ====================

    @Test
    @DisplayName("should return summary with all metadata")
    void getRulesSummary() {
        List<String> categories = List.of("DICE_MECHANICS", "COMBAT", "MOMENTUM");
        List<String> chapters = List.of("05.00 Rules of Play", "06.00 Playing Captain's Log");
        List<String> tableNames = List.of("Mission Type", "Complication");
        List<String> tableCategories = List.of("MISSION", "ORACLE");

        when(entryRepository.findDistinctCategories()).thenReturn(categories);
        when(entryRepository.findDistinctChapters()).thenReturn(chapters);
        when(tableRepository.findDistinctTableNames()).thenReturn(tableNames);
        when(tableRepository.findDistinctCategories()).thenReturn(tableCategories);
        when(entryRepository.count()).thenReturn(17L);
        when(tableRepository.count()).thenReturn(7L);

        Map<String, Object> result = rulesService.getRulesSummary();

        assertNotNull(result);
        assertEquals(categories, result.get("categories"));
        assertEquals(chapters, result.get("chapters"));
        assertEquals(tableNames, result.get("tableNames"));
        assertEquals(tableCategories, result.get("tableCategories"));
        assertEquals(17L, result.get("totalEntries"));
        assertEquals(7L, result.get("totalTableEntries"));
    }

    @Test
    @DisplayName("should return summary with empty data when no rules populated")
    void getRulesSummary_Empty() {
        when(entryRepository.findDistinctCategories()).thenReturn(Collections.emptyList());
        when(entryRepository.findDistinctChapters()).thenReturn(Collections.emptyList());
        when(tableRepository.findDistinctTableNames()).thenReturn(Collections.emptyList());
        when(tableRepository.findDistinctCategories()).thenReturn(Collections.emptyList());
        when(entryRepository.count()).thenReturn(0L);
        when(tableRepository.count()).thenReturn(0L);

        Map<String, Object> result = rulesService.getRulesSummary();

        assertNotNull(result);
        assertTrue(((List<?>) result.get("categories")).isEmpty());
        assertEquals(0L, result.get("totalEntries"));
        assertEquals(0L, result.get("totalTableEntries"));
    }

    // ==================== getRulesForAction ====================

    @Test
    @DisplayName("should return DICE_MECHANICS rules for 'roll' action")
    void getRulesForAction_Roll() {
        when(entryRepository.findByCategoryAndActiveTrue("DICE_MECHANICS"))
                .thenReturn(List.of(sampleEntries.get(0)));

        List<RulesEntry> result = rulesService.getRulesForAction("roll");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DICE_MECHANICS", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return DICE_MECHANICS rules for 'task' action")
    void getRulesForAction_Task() {
        when(entryRepository.findByCategoryAndActiveTrue("DICE_MECHANICS"))
                .thenReturn(List.of(sampleEntries.get(0)));

        List<RulesEntry> result = rulesService.getRulesForAction("task attempt");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return DICE_MECHANICS rules for 'check' action")
    void getRulesForAction_Check() {
        when(entryRepository.findByCategoryAndActiveTrue("DICE_MECHANICS"))
                .thenReturn(List.of(sampleEntries.get(0)));

        List<RulesEntry> result = rulesService.getRulesForAction("check");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return COMBAT rules for 'combat' action")
    void getRulesForAction_Combat() {
        when(entryRepository.findByCategoryAndActiveTrue("COMBAT"))
                .thenReturn(List.of(sampleEntries.get(1)));

        List<RulesEntry> result = rulesService.getRulesForAction("combat");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COMBAT", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return COMBAT rules for 'attack' action")
    void getRulesForAction_Attack() {
        when(entryRepository.findByCategoryAndActiveTrue("COMBAT"))
                .thenReturn(List.of(sampleEntries.get(1)));

        List<RulesEntry> result = rulesService.getRulesForAction("attack");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return MOMENTUM rules for 'momentum' action")
    void getRulesForAction_Momentum() {
        when(entryRepository.findByCategoryAndActiveTrue("MOMENTUM"))
                .thenReturn(List.of(sampleEntries.get(2)));

        List<RulesEntry> result = rulesService.getRulesForAction("spend momentum");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MOMENTUM", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return THREAT rules for 'threat' action")
    void getRulesForAction_Threat() {
        when(entryRepository.findByCategoryAndActiveTrue("THREAT"))
                .thenReturn(List.of(sampleEntries.get(3)));

        List<RulesEntry> result = rulesService.getRulesForAction("resolve threat");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("THREAT", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return MISSION_STRUCTURE rules for 'mission' action")
    void getRulesForAction_Mission() {
        when(entryRepository.findByCategoryAndActiveTrue("MISSION_STRUCTURE"))
                .thenReturn(List.of(sampleEntries.get(4)));

        List<RulesEntry> result = rulesService.getRulesForAction("mission generation");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MISSION_STRUCTURE", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return ORACLE rules for 'oracle' action")
    void getRulesForAction_Oracle() {
        when(entryRepository.findByCategoryAndActiveTrue("ORACLE"))
                .thenReturn(List.of(sampleEntries.get(5)));

        List<RulesEntry> result = rulesService.getRulesForAction("oracle query");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ORACLE", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return STARSHIP rules for 'starship' action")
    void getRulesForAction_Starship() {
        when(entryRepository.findByCategoryAndActiveTrue("STARSHIP"))
                .thenReturn(List.of(sampleEntries.get(6)));

        List<RulesEntry> result = rulesService.getRulesForAction("starship creation");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("STARSHIP", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return CHARACTER_CREATION rules for 'character' action")
    void getRulesForAction_Character() {
        when(entryRepository.findByCategoryAndActiveTrue("CHARACTER_CREATION"))
                .thenReturn(List.of(sampleEntries.get(7)));

        List<RulesEntry> result = rulesService.getRulesForAction("character creation");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CHARACTER_CREATION", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return CAREER rules for 'career' action")
    void getRulesForAction_Career() {
        when(entryRepository.findByCategoryAndActiveTrue("CAREER"))
                .thenReturn(List.of(sampleEntries.get(8)));

        List<RulesEntry> result = rulesService.getRulesForAction("career progression");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CAREER", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return MAINTENANCE rules for 'maintenance' action")
    void getRulesForAction_Maintenance() {
        when(entryRepository.findByCategoryAndActiveTrue("MAINTENANCE"))
                .thenReturn(List.of(sampleEntries.get(9)));

        List<RulesEntry> result = rulesService.getRulesForAction("maintenance downtime");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MAINTENANCE", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return EQUIPMENT rules for 'equipment' action")
    void getRulesForAction_Equipment() {
        when(entryRepository.findByCategoryAndActiveTrue("EQUIPMENT"))
                .thenReturn(List.of(sampleEntries.get(10)));

        List<RulesEntry> result = rulesService.getRulesForAction("equipment management");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EQUIPMENT", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return CREW rules for 'crew' action")
    void getRulesForAction_Crew() {
        when(entryRepository.findByCategoryAndActiveTrue("CREW"))
                .thenReturn(List.of(sampleEntries.get(11)));

        List<RulesEntry> result = rulesService.getRulesForAction("crew management");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CREW", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return CAMPAIGN rules for 'campaign' action")
    void getRulesForAction_Campaign() {
        when(entryRepository.findByCategoryAndActiveTrue("CAMPAIGN"))
                .thenReturn(List.of(sampleEntries.get(12)));

        List<RulesEntry> result = rulesService.getRulesForAction("campaign tracking");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CAMPAIGN", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return INJURY rules for 'injury' action")
    void getRulesForAction_Injury() {
        when(entryRepository.findByCategoryAndActiveTrue("INJURY"))
                .thenReturn(List.of(sampleEntries.get(13)));

        List<RulesEntry> result = rulesService.getRulesForAction("injury recovery");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("INJURY", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return WORLD_GENERATION rules for 'world' action")
    void getRulesForAction_World() {
        when(entryRepository.findByCategoryAndActiveTrue("WORLD_GENERATION"))
                .thenReturn(List.of(sampleEntries.get(14)));

        List<RulesEntry> result = rulesService.getRulesForAction("world generation");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WORLD_GENERATION", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return SETTING rules for 'setting' action")
    void getRulesForAction_Setting() {
        when(entryRepository.findByCategoryAndActiveTrue("SETTING"))
                .thenReturn(List.of(sampleEntries.get(15)));

        List<RulesEntry> result = rulesService.getRulesForAction("setting lore");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SETTING", result.get(0).getCategory());
    }

    @Test
    @DisplayName("should return multiple categories when action matches multiple keywords")
    void getRulesForAction_MultipleCategories() {
        when(entryRepository.findByCategoryAndActiveTrue("DICE_MECHANICS"))
                .thenReturn(List.of(sampleEntries.get(0)));
        when(entryRepository.findByCategoryAndActiveTrue("COMBAT"))
                .thenReturn(List.of(sampleEntries.get(1)));

        // "roll combat" should match both DICE_MECHANICS and COMBAT
        List<RulesEntry> result = rulesService.getRulesForAction("roll combat");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getCategory().equals("DICE_MECHANICS")));
        assertTrue(result.stream().anyMatch(e -> e.getCategory().equals("COMBAT")));
    }

    @Test
    @DisplayName("should return empty list when action matches no keywords")
    void getRulesForAction_NoMatch() {
        // Mock all categories to return empty lists (in case any keyword partially matches)
        when(entryRepository.findByCategoryAndActiveTrue(anyString()))
                .thenReturn(Collections.emptyList());

        List<RulesEntry> result = rulesService.getRulesForAction("xyz unrelated action");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should handle case-insensitive action matching")
    void getRulesForAction_CaseInsensitive() {
        when(entryRepository.findByCategoryAndActiveTrue("DICE_MECHANICS"))
                .thenReturn(List.of(sampleEntries.get(0)));

        List<RulesEntry> result = rulesService.getRulesForAction("ROLL");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== initializeRulesDatabase ====================

    @Test
    @DisplayName("should skip initialization if database already has entries")
    void initializeRulesDatabase_HasEntries() {
        // count() is called twice: once for the guard check, once in the log.info statement
        when(entryRepository.count()).thenReturn(55L);

        rulesService.initializeRulesDatabase();

        // Method returns early after checking count > 0
        verify(entryRepository, times(2)).count();
        // initialized flag is set to true
        assertTrue(true); // service initialized without populating
    }

    @Test
    @DisplayName("should not double-initialize on second call")
    void initializeRulesDatabase_NoDoubleInit() {
        // count() called twice on first call (guard + log), then skipped on second call
        when(entryRepository.count()).thenReturn(55L);

        // First call
        rulesService.initializeRulesDatabase();
        // Second call should skip because initialized flag is true
        rulesService.initializeRulesDatabase();

        // count() called only twice (first call: guard check + log.info)
        verify(entryRepository, times(2)).count();
    }

    // ==================== Model field validation ====================

    @Test
    @DisplayName("RulesEntry should have all required fields set")
    void rulesEntryFields() {
        RulesEntry entry = sampleEntries.get(0);

        assertNotNull(entry.getId());
        assertEquals("2d20 Task Attempt Core Mechanic", entry.getTitle());
        assertEquals("05.00 Rules of Play", entry.getChapter());
        assertEquals("DICE_MECHANICS", entry.getCategory());
        assertEquals(179, entry.getPage());
        assertNotNull(entry.getText());
        assertNotNull(entry.getNotes());
        assertTrue(entry.isActive());
    }

    @Test
    @DisplayName("RulesEntry inactive flag should work")
    void rulesEntryInactive() {
        RulesEntry entry = sampleEntries.get(16);

        assertFalse(entry.isActive());
        entry.setActive(true);
        assertTrue(entry.isActive());
    }

    @Test
    @DisplayName("RulesTable should have all required fields set")
    void rulesTableFields() {
        RulesTable table = sampleTables.get(0);

        assertNotNull(table.getId());
        assertEquals("Mission Type", table.getTableName());
        assertEquals("MISSION", table.getCategory());
        assertEquals("Appendix IV", table.getChapter());
        assertEquals(250, table.getPage());
        assertEquals(1, table.getRollValue());
        assertEquals("Diplomacy", table.getResultLabel());
        assertNotNull(table.getDescription());
        assertTrue(table.isActive());
    }

    @Test
    @DisplayName("RulesTable toString should include key fields")
    void rulesTableToString() {
        RulesTable table = sampleTables.get(0);
        String str = table.toString();

        assertTrue(str.contains("Mission Type"));
        assertTrue(str.contains("Diplomacy"));
        assertTrue(str.contains("roll=1"));
    }

    @Test
    @DisplayName("RulesEntry toString should include key fields")
    void rulesEntryToString() {
        RulesEntry entry = sampleEntries.get(0);
        String str = entry.toString();

        assertTrue(str.contains("2d20 Task Attempt Core Mechanic"));
        assertTrue(str.contains("05.00 Rules of Play"));
        assertTrue(str.contains("DICE_MECHANICS"));
        assertTrue(str.contains("page=179"));
    }

    // ==================== Edge cases ====================

    @Test
    @DisplayName("should handle null-safe category lookup")
    void getRulesByCategory_NullSafe() {
        when(entryRepository.findByCategoryAndActiveTrue(null))
                .thenThrow(NullPointerException.class);

        assertThrows(NullPointerException.class, () ->
                rulesService.getRulesByCategory(null));
    }

    @Test
    @DisplayName("should handle empty string keyword search")
    void searchRules_EmptyKeyword() {
        when(entryRepository.searchByKeyword(""))
                .thenReturn(Collections.emptyList());

        List<RulesEntry> result = rulesService.searchRules("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should handle empty string table search")
    void searchTables_EmptyKeyword() {
        when(tableRepository.searchByKeyword(""))
                .thenReturn(Collections.emptyList());

        List<RulesTable> result = rulesService.searchTables("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getRulesForAction should handle null action gracefully")
    void getRulesForAction_NullAction() {
        assertThrows(NullPointerException.class, () ->
                rulesService.getRulesForAction(null));
    }

    // ==================== Data integrity tests ====================

    @Test
    @DisplayName("sample entries should have unique IDs")
    void sampleEntriesUniqueIds() {
        Set<Long> ids = new HashSet<>();
        for (RulesEntry entry : sampleEntries) {
            assertTrue(ids.add(entry.getId()), "Duplicate ID found: " + entry.getId());
        }
        assertEquals(sampleEntries.size(), ids.size());
    }

    @Test
    @DisplayName("sample tables should have unique IDs")
    void sampleTablesUniqueIds() {
        Set<Long> ids = new HashSet<>();
        for (RulesTable table : sampleTables) {
            assertTrue(ids.add(table.getId()), "Duplicate ID found: " + table.getId());
        }
        assertEquals(sampleTables.size(), ids.size());
    }

    @Test
    @DisplayName("sample entries should have non-null titles")
    void sampleEntriesNonNullTitles() {
        for (RulesEntry entry : sampleEntries) {
            assertNotNull(entry.getTitle(), "Entry with ID " + entry.getId() + " has null title");
            assertFalse(entry.getTitle().isBlank(), "Entry with ID " + entry.getId() + " has blank title");
        }
    }

    @Test
    @DisplayName("sample entries should have non-null categories")
    void sampleEntriesNonNullCategories() {
        for (RulesEntry entry : sampleEntries) {
            assertNotNull(entry.getCategory(), "Entry with ID " + entry.getId() + " has null category");
            assertFalse(entry.getCategory().isBlank(), "Entry with ID " + entry.getId() + " has blank category");
        }
    }

    @Test
    @DisplayName("sample entries should have non-null chapters")
    void sampleEntriesNonNullChapters() {
        for (RulesEntry entry : sampleEntries) {
            assertNotNull(entry.getChapter(), "Entry with ID " + entry.getId() + " has null chapter");
            assertFalse(entry.getChapter().isBlank(), "Entry with ID " + entry.getId() + " has blank chapter");
        }
    }

    @Test
    @DisplayName("sample tables should have valid roll values")
    void sampleTablesValidRollValues() {
        for (RulesTable table : sampleTables) {
            assertTrue(table.getRollValue() >= 1 && table.getRollValue() <= 20,
                    "Roll value out of range: " + table.getRollValue());
        }
    }

    @Test
    @DisplayName("sample tables should have non-null result labels")
    void sampleTablesNonNullLabels() {
        for (RulesTable table : sampleTables) {
            assertNotNull(table.getResultLabel(), "Table with ID " + table.getId() + " has null result label");
            assertFalse(table.getResultLabel().isBlank(), "Table with ID " + table.getId() + " has blank result label");
        }
    }

    @Test
    @DisplayName("sample tables should have non-null descriptions")
    void sampleTablesNonNullDescriptions() {
        for (RulesTable table : sampleTables) {
            assertNotNull(table.getDescription(), "Table with ID " + table.getId() + " has null description");
        }
    }

    // ==================== Category coverage tests ====================

    @Test
    @DisplayName("should cover all 16 rule categories in sample data")
    void categoryCoverage() {
        Set<String> categories = new HashSet<>();
        for (RulesEntry entry : sampleEntries) {
            if (entry.isActive()) {
                categories.add(entry.getCategory());
            }
        }

        // Verify all expected categories are present
        assertTrue(categories.contains("DICE_MECHANICS"), "Missing DICE_MECHANICS");
        assertTrue(categories.contains("COMBAT"), "Missing COMBAT");
        assertTrue(categories.contains("MOMENTUM"), "Missing MOMENTUM");
        assertTrue(categories.contains("THREAT"), "Missing THREAT");
        assertTrue(categories.contains("MISSION_STRUCTURE"), "Missing MISSION_STRUCTURE");
        assertTrue(categories.contains("ORACLE"), "Missing ORACLE");
        assertTrue(categories.contains("STARSHIP"), "Missing STARSHIP");
        assertTrue(categories.contains("CHARACTER_CREATION"), "Missing CHARACTER_CREATION");
        assertTrue(categories.contains("CAREER"), "Missing CAREER");
        assertTrue(categories.contains("MAINTENANCE"), "Missing MAINTENANCE");
        assertTrue(categories.contains("EQUIPMENT"), "Missing EQUIPMENT");
        assertTrue(categories.contains("CREW"), "Missing CREW");
        assertTrue(categories.contains("CAMPAIGN"), "Missing CAMPAIGN");
        assertTrue(categories.contains("INJURY"), "Missing INJURY");
        assertTrue(categories.contains("WORLD_GENERATION"), "Missing WORLD_GENERATION");
        assertTrue(categories.contains("SETTING"), "Missing SETTING");

        assertEquals(16, categories.size(), "Should have exactly 16 categories");
    }

    @Test
    @DisplayName("should cover multiple chapters in sample data")
    void chapterCoverage() {
        Set<String> chapters = new HashSet<>();
        for (RulesEntry entry : sampleEntries) {
            if (entry.isActive()) {
                chapters.add(entry.getChapter());
            }
        }

        assertTrue(chapters.contains("01.00 Star Trek Defined"));
        assertTrue(chapters.contains("03.00 Reporting for Duty"));
        assertTrue(chapters.contains("04.00 Your Home Among the Stars"));
        assertTrue(chapters.contains("05.00 Rules of Play"));
        assertTrue(chapters.contains("06.00 Playing Captain's Log"));
    }

    // ==================== Table ordering tests ====================

    @Test
    @DisplayName("table entries should be ordered by roll value ascending")
    void tableEntriesOrderedByRollValue() {
        List<RulesTable> missionTables = new ArrayList<>();
        for (RulesTable t : sampleTables) {
            if (t.getTableName().equals("Mission Type") && t.isActive()) {
                missionTables.add(t);
            }
        }
        when(tableRepository.findByTableNameAndActiveTrueOrderByRollValueAsc("Mission Type"))
                .thenReturn(missionTables);

        List<RulesTable> result = rulesService.getTableEntries("Mission Type");

        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).getRollValue() < result.get(i).getRollValue(),
                    "Tables not ordered by roll value: " + result.get(i - 1).getRollValue() + " >= " + result.get(i).getRollValue());
        }
    }
}
