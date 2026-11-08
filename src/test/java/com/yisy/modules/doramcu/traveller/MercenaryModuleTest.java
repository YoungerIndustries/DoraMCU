package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mercenary Module Tests")
class MercenaryModuleTest {

    // ---- MercenarySkill tests ----

    @Test
    @DisplayName("Should have all expected mercenary skills")
    void testMercenarySkills() {
        assertTrue(MercenarySkill.values().length >= 30);
        assertNotNull(MercenarySkill.COMBAT_RIFLEMAN);
        assertNotNull(MercenarySkill.DEMOLITIONS);
        assertNotNull(MercenarySkill.BATTLE_DRESS);
    }

    @Test
    @DisplayName("Should categorize skills correctly")
    void testSkillCategories() {
        assertEquals(MercenarySkill.MercenarySkillCategory.GUN_COMBAT,
                MercenarySkill.COMBAT_RIFLEMAN.getCategory());
        assertEquals(MercenarySkill.MercenarySkillCategory.COMBAT,
                MercenarySkill.DEMOLITIONS.getCategory());
        assertEquals(MercenarySkill.MercenarySkillCategory.EQUIPMENT,
                MercenarySkill.BATTLE_DRESS.getCategory());
    }

    // ---- MercenaryRank tests ----

    @Test
    @DisplayName("Should have all expected ranks")
    void testMercenaryRanks() {
        assertTrue(MercenaryRank.values().length >= 15);
    }

    @Test
    @DisplayName("Should identify officers correctly")
    void testOfficerRanks() {
        assertFalse(MercenaryRank.PRIVATE.isOfficer());
        assertFalse(MercenaryRank.SERGEANT.isOfficer());
        assertTrue(MercenaryRank.SECOND_LIEUTENANT.isOfficer());
        assertTrue(MercenaryRank.COLONEL.isOfficer());
    }

    @Test
    @DisplayName("Should identify NCOs correctly")
    void testNCOs() {
        assertFalse(MercenaryRank.PRIVATE.isNCO());
        assertTrue(MercenaryRank.CORPORAL.isNCO());
        assertTrue(MercenaryRank.SERGEANT_MAJOR.isNCO());
        assertFalse(MercenaryRank.CAPTAIN.isNCO());
    }

    @Test
    @DisplayName("Should navigate rank hierarchy")
    void testRankNavigation() {
        assertEquals(MercenaryRank.LANCE_CORPORAL, MercenaryRank.PRIVATE.nextHigher());
        assertEquals(MercenaryRank.PRIVATE, MercenaryRank.LANCE_CORPORAL.nextLower());
        assertNull(MercenaryRank.GENERAL.nextHigher());
        assertNull(MercenaryRank.PRIVATE.nextLower());
    }

    @Test
    @DisplayName("Should have correct salary and shares")
    void testSalaryAndShares() {
        assertEquals(300, MercenaryRank.PRIVATE.getMonthlySalary());
        assertEquals(1, MercenaryRank.PRIVATE.getShares());
        assertEquals(2000, MercenaryRank.COLONEL.getMonthlySalary());
        assertEquals(10, MercenaryRank.COLONEL.getShares());
    }

    // ---- ArmOfService tests ----

    @Test
    @DisplayName("Should have all expected arms")
    void testArmsOfService() {
        assertEquals(6, ArmOfService.values().length);
    }

    @Test
    @DisplayName("Should not allow direct entry to Commando")
    void testCommandoNotEnterable() {
        assertFalse(ArmOfService.COMMANDO.isEnterableDirectly());
        assertTrue(ArmOfService.INFANTRY.isEnterableDirectly());
    }

    @Test
    @DisplayName("Should have MOS tables")
    void testMosTables() {
        assertFalse(ArmOfService.INFANTRY.getMosTable().isEmpty());
        assertEquals(8, ArmOfService.INFANTRY.getMosTable().size());
    }

    // ---- MercenaryAssignment tests ----

    @Test
    @DisplayName("Should identify combat assignments")
    void testCombatAssignments() {
        assertTrue(MercenaryAssignment.RAID.isCombat());
        assertTrue(MercenaryAssignment.POLICE_ACTION.isCombat());
        assertFalse(MercenaryAssignment.TRAINING.isCombat());
        assertFalse(MercenaryAssignment.GARRISON.isCombat());
    }

    @Test
    @DisplayName("Should have correct survival thresholds")
    void testSurvivalThresholds() {
        // Training is auto-survive for most arms
        assertEquals(Integer.MAX_VALUE, MercenaryAssignment.TRAINING.getSurvivalThreshold(ArmOfService.INFANTRY));
        // Commando training has a threshold
        assertEquals(3, MercenaryAssignment.TRAINING.getSurvivalThreshold(ArmOfService.COMMANDO));
    }

    // ---- SpecialAssignment tests ----

    @Test
    @DisplayName("Should have correct tier availability")
    void testSpecialAssignmentTiers() {
        assertTrue(SpecialAssignment.OCS.isAvailableToEnlisted());
        assertFalse(SpecialAssignment.OCS.isAvailableToOfficers());
        assertTrue(SpecialAssignment.INTELLIGENCE_SCHOOL.isAvailableToOfficers());
        assertFalse(SpecialAssignment.INTELLIGENCE_SCHOOL.isAvailableToEnlisted());
        assertTrue(SpecialAssignment.COMMANDO_SCHOOL.isAvailableToEnlisted());
        assertTrue(SpecialAssignment.COMMANDO_SCHOOL.isAvailableToOfficers());
    }

    // ---- MissionType tests ----

    @Test
    @DisplayName("Should have all mission types")
    void testMissionTypes() {
        assertEquals(4, MissionType.values().length);
    }

    // ---- UnitType tests ----

    @Test
    @DisplayName("Should have correct unit hierarchy")
    void testUnitHierarchy() {
        assertEquals(8, UnitType.values().length);
        assertEquals(4, UnitType.FIRE_TEAM.getTypicalStrength());
        assertEquals(1500, UnitType.BRIGADE.getTypicalStrength());
    }

    @Test
    @DisplayName("Should calculate preservation levels")
    void testPreservationLevels() {
        assertEquals(180, UnitType.BATTALION.getPreservationLevel(452));
        assertEquals(1, UnitType.FIRE_TEAM.getPreservationLevel(4)); // 40% of 4 = 1.6, truncated to 1
    }

    @Test
    @DisplayName("Should map die rolls to unit types")
    void testDieRollMapping() {
        assertEquals(UnitType.FIRE_TEAM, UnitType.fromDieRoll(1));
        assertEquals(UnitType.SQUAD, UnitType.fromDieRoll(3));
        assertEquals(UnitType.BATTALION, UnitType.fromDieRoll(8));
        assertEquals(UnitType.BRIGADE, UnitType.fromDieRoll(12));
    }

    @Test
    @DisplayName("Should go down levels correctly")
    void testDownLevels() {
        assertEquals(UnitType.SQUAD, UnitType.PLATOON.downLevels(2));
        assertEquals(UnitType.FIRE_TEAM, UnitType.FIRE_TEAM.downLevels(5));
    }

    // ---- MercenaryDecoration tests ----

    @Test
    @DisplayName("Should have correct experience points")
    void testDecorationExperience() {
        assertEquals(1, MercenaryDecoration.MCUF.getExperiencePoints());
        assertEquals(2, MercenaryDecoration.MCG.getExperiencePoints());
        assertEquals(3, MercenaryDecoration.SEH.getExperiencePoints());
        assertEquals(1, MercenaryDecoration.PURPLE_HEART.getExperiencePoints());
    }

    // ---- MercenaryCareer tests ----

    @Test
    @DisplayName("Should create career with valid parameters")
    void testCreateCareer() {
        MercenaryCareer career = new MercenaryCareer(
                BranchOfService.ARMY, ArmOfService.INFANTRY, 8, 3);
        assertEquals(BranchOfService.ARMY, career.getBranch());
        assertEquals(ArmOfService.INFANTRY, career.getArm());
        assertEquals(8, career.getTechLevel());
        assertTrue(career.isAlive());
    }

    @Test
    @DisplayName("Should reject Commando as initial arm")
    void testRejectCommando() {
        assertThrows(IllegalArgumentException.class,
                () -> new MercenaryCareer(BranchOfService.ARMY, ArmOfService.COMMANDO, 8, 3));
    }

    @Test
    @DisplayName("Should reject Marine arm for Army")
    void testRejectMarineForArmy() {
        assertThrows(IllegalArgumentException.class,
                () -> new MercenaryCareer(BranchOfService.ARMY, ArmOfService.MARINE, 8, 3));
    }

    @Test
    @DisplayName("Should run career and gain skills")
    void testRunCareer() {
        MercenaryCareer career = new MercenaryCareer(
                BranchOfService.ARMY, ArmOfService.INFANTRY, 8, 2);
        career.runCareer();

        // Should have at least basic training skills
        Map<MercenarySkill, Integer> skills = career.getMercenarySkills();
        assertTrue(skills.containsKey(MercenarySkill.COMBAT_RIFLEMAN));
        assertTrue(skills.get(MercenarySkill.COMBAT_RIFLEMAN) >= 1);
    }

    @Test
    @DisplayName("Should generate resume")
    void testGenerateResume() {
        MercenaryCareer career = new MercenaryCareer(
                BranchOfService.ARMY, ArmOfService.INFANTRY, 8, 1);
        career.runCareer();
        String resume = career.generateResume();
        assertNotNull(resume);
        assertTrue(resume.contains("Army"));
        assertTrue(resume.contains("Infantry"));
    }

    @Test
    @DisplayName("Should calculate experience points")
    void testExperiencePoints() {
        MercenaryCareer career = new MercenaryCareer(
                BranchOfService.ARMY, ArmOfService.INFANTRY, 8, 2);
        career.runCareer();
        int exp = career.calculateExperiencePoints();
        assertTrue(exp >= 0);
    }

    // ---- MercenaryTicket tests ----

    @Test
    @DisplayName("Should create ticket with defaults")
    void testCreateTicket() {
        MercenaryTicket ticket = new MercenaryTicket(
                1, "Test Ticket", "Patron", MissionType.STRIKER,
                MercenaryTicket.PaymentType.SUCCESS_ONLY, MercenaryTicket.TicketType.SHORT,
                60000, 0, 0, 10000, 3, 50, 8, "Test mission", "TestWorld");
        assertEquals(1, ticket.getId());
        assertEquals(MissionType.STRIKER, ticket.getMissionType());
        assertEquals(60000, ticket.getBasePayment());
    }

    @Test
    @DisplayName("Should calculate payroll costs")
    void testPayrollCosts() {
        MercenaryTicket ticket = new MercenaryTicket(
                1, "Test", "Patron", MissionType.STRIKER,
                MercenaryTicket.PaymentType.UPFRONT, MercenaryTicket.TicketType.SHORT,
                60000, 0, 0, 10000, 3, 50, 8, "Test", "World");
        long payroll = ticket.calculatePayrollCost(50);
        assertEquals(90000, payroll); // 50 * 600 * 3
    }

    // ---- MercenaryUnit tests ----

    @Test
    @DisplayName("Should create unit")
    void testCreateUnit() {
        MercenaryUnit unit = new MercenaryUnit(
                1, "Alpha Platoon", UnitType.PLATOON, MercenaryRank.FIRST_LIEUTENANT,
                52, 8, 5, 2, 5);
        assertEquals(52, unit.getStrength());
        assertEquals(0, unit.getCasualties());
        assertFalse(unit.isDestroyed());
    }

    @Test
    @DisplayName("Should track casualties")
    void testCasualties() {
        MercenaryUnit unit = new MercenaryUnit(
                1, "Alpha", UnitType.PLATOON, MercenaryRank.FIRST_LIEUTENANT,
                52, 8, 5, 2, 5);
        unit.addCasualties(10);
        assertEquals(42, unit.getStrength());
        assertEquals(10, unit.getCasualties());
    }

    @Test
    @DisplayName("Should detect destroyed units")
    void testDestroyed() {
        MercenaryUnit unit = new MercenaryUnit(
                1, "Alpha", UnitType.FIRE_TEAM, MercenaryRank.CORPORAL,
                4, 8, 1, 0, 0);
        unit.addCasualties(4);
        assertTrue(unit.isDestroyed());
    }

    @Test
    @DisplayName("Should add sub-units")
    void testSubUnits() {
        MercenaryUnit platoon = new MercenaryUnit(
                1, "Alpha Platoon", UnitType.PLATOON, MercenaryRank.FIRST_LIEUTENANT,
                52, 8, 5, 2, 5);
        MercenaryUnit squad = new MercenaryUnit(
                2, "Alpha Squad", UnitType.SQUAD, MercenaryRank.SERGEANT,
                9, 7, 3, 1, 0);
        platoon.addSubUnit(squad);
        assertEquals(1, platoon.getSubUnits().size());
    }

    // ---- MercenaryRecruiting tests ----

    @Test
    @DisplayName("Should not recruit on low TL worlds")
    void testCannotRecruitLowTL() {
        MercenaryRecruiting recruiting = new MercenaryRecruiting(6, 2, 5, 1);
        assertFalse(recruiting.canRecruit());
    }

    @Test
    @DisplayName("Should recruit on suitable worlds")
    void testCanRecruit() {
        MercenaryRecruiting recruiting = new MercenaryRecruiting(8, 8, 5, 2);
        assertTrue(recruiting.canRecruit());
    }

    @Test
    @DisplayName("Should perform basic training")
    void testBasicTraining() {
        MercenaryRecruiting recruiting = new MercenaryRecruiting(8, 8, 5, 2);
        int trained = recruiting.basicTraining(20, 3);
        assertEquals(18, trained); // 6 * 3 = 18 capacity
    }

    @Test
    @DisplayName("Should reject training without instruction skill")
    void testNoTrainingWithoutSkill() {
        MercenaryRecruiting recruiting = new MercenaryRecruiting(8, 8, 5, 2);
        assertEquals(0, recruiting.basicTraining(20, 0));
    }

    // ---- AbstractBattle tests ----

    @Test
    @DisplayName("Should create battle forces")
    void testBattleForce() {
        AbstractBattle.BattleForce force = new AbstractBattle.BattleForce(
                MissionType.STRIKER, 8, UnitType.BATTALION, 5, 452, true);
        assertEquals(MissionType.STRIKER, force.getMission());
        assertEquals(452, force.getStrength());
        assertFalse(force.isDefeated());
    }

    @Test
    @DisplayName("Should resolve abstract battle")
    void testResolveBattle() {
        AbstractBattle.BattleForce own = new AbstractBattle.BattleForce(
                MissionType.STRIKER, 9, UnitType.BATTALION, 6, 452, true);
        AbstractBattle.BattleForce enemy = new AbstractBattle.BattleForce(
                MissionType.STRIKER, 7, UnitType.BATTALION, 4, 452, false);

        AbstractBattle battle = new AbstractBattle(own, enemy);
        boolean won = battle.resolve();

        // Either force should be defeated
        assertTrue(own.isDefeated() || enemy.isDefeated());
        assertFalse(battle.getCombatResults().isEmpty());
    }

    // ---- MercenaryWeapon tests ----

    @Test
    @DisplayName("Should have all expected weapons")
    void testWeapons() {
        assertTrue(MercenaryWeapon.values().length >= 15);
    }

    @Test
    @DisplayName("Should identify battle dress weapons")
    void testBattleDressWeapons() {
        assertTrue(MercenaryWeapon.PGMP_13.requiresBattleDress());
        assertTrue(MercenaryWeapon.FGMP_14.requiresBattleDress());
        assertFalse(MercenaryWeapon.ASSAULT_RIFLE.requiresBattleDress());
    }

    @Test
    @DisplayName("Should identify zero-G weapons")
    void testZeroGWeapons() {
        assertTrue(MercenaryWeapon.SNUB_PISTOL.isZeroGWeapon());
        assertTrue(MercenaryWeapon.ACCELERATOR_RIFLE.isZeroGWeapon());
        assertFalse(MercenaryWeapon.ASSAULT_RIFLE.isZeroGWeapon());
    }

    @Test
    @DisplayName("Should have correct tech levels")
    void testWeaponTechLevels() {
        assertEquals(7, MercenaryWeapon.ASSAULT_RIFLE.getTechLevel());
        assertEquals(10, MercenaryWeapon.ACR.getTechLevel());
        assertEquals(12, MercenaryWeapon.GAUSS_RIFLE.getTechLevel());
        assertEquals(15, MercenaryWeapon.FGMP_15.getTechLevel());
    }

    // ---- MercenaryArmor tests ----

    @Test
    @DisplayName("Should have all expected armor types")
    void testArmorTypes() {
        assertEquals(7, MercenaryArmor.values().length);
    }

    @Test
    @DisplayName("Should identify battle dress armor")
    void testBattleDressArmor() {
        assertTrue(MercenaryArmor.BATTLE_DRESS.requiresBattleDress());
        assertFalse(MercenaryArmor.BALLISTIC_CLOTH.requiresBattleDress());
    }

    @Test
    @DisplayName("Should have correct protection values")
    void testProtectionValues() {
        assertEquals(0, MercenaryArmor.COMBAT_UNIFORM.getProtection());
        assertEquals(8, MercenaryArmor.BATTLE_DRESS.getProtection());
        assertEquals(7, MercenaryArmor.COMPOSITE_ARMOR.getProtection());
    }

    @Test
    @DisplayName("Should calculate effective weight for battle dress")
    void testEffectiveWeight() {
        assertEquals(10000, MercenaryArmor.BATTLE_DRESS.getEffectiveWeightGrams());
        assertEquals(5000, MercenaryArmor.BALLISTIC_CLOTH.getEffectiveWeightGrams());
    }

    // ---- BranchOfService tests ----

    @Test
    @DisplayName("Should have correct reenlistment DMs")
    void testReenlistmentDm() {
        assertEquals(2, BranchOfService.ARMY.getReenlistmentDm());
        assertEquals(0, BranchOfService.MARINES.getReenlistmentDm());
    }
}
