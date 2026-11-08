package com.yisy.modules.doramcu.traveller;

import java.util.*;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Detailed mercenary career generation from Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * This class implements the full mercenary career system, including:
 * </p>
 * <ul>
 *   <li>Terms of service (4 years each, subdivided into 4 one-year assignments)</li>
 *   <li>Arms of service (Infantry, Cavalry, Artillery, Support, Commando, Marine)</li>
 *   <li>Basic and advanced training</li>
 *   <li>General and specific assignments</li>
 *   <li>Special assignments (schools, cross-training, OCS)</li>
 *   <li>Survival, decorations, promotions, and skills</li>
 *   <li>Reenlistment and mustering out</li>
 *   <li>Resumes and hiring on</li>
 * </ul>
 * <p>
 * Each term consists of 4 assignments. After each assignment, the character
 * rolls for survival, decorations, promotion, and skills.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public class MercenaryCareer {

    private final BranchOfService branch;
    private ArmOfService arm;
    private MercenaryRank rank;
    private final int techLevel;
    private final int termsPlanned;
    private int currentTerm;
    private int currentAssignment;
    private boolean alive;
    private boolean isCommissioned;

    // Skills
    private final Map<MercenarySkill, Integer> mercenarySkills;
    private final Map<AbilityScore, Integer> abilities;

    // Decorations
    private final Map<MercenaryDecoration, Integer> decorations;
    private int mcufCount;
    private int mcgCount;
    private int sehCount;
    private int purpleHeartCount;
    private int csrCount;
    private int combatCommandCount;

    // Cross-training
    private final Set<ArmOfService> crossTrainedArms;

    // Special assignments
    private final List<SpecialAssignment> specialAssignments;

    // Term records
    private final List<TermRecord> termRecords;

    /**
     * Record of a single term of service.
     */
    public static class TermRecord {
        private final int termNumber;
        private final ArmOfService arm;
        private final MercenaryRank startingRank;
        private final MercenaryRank endingRank;
        private final List<AssignmentRecord> assignments;
        private final List<MercenarySkill> skillsGained;
        private final List<MercenaryDecoration> decorationsEarned;

        public TermRecord(int termNumber, ArmOfService arm, MercenaryRank startingRank,
                          MercenaryRank endingRank, List<AssignmentRecord> assignments,
                          List<MercenarySkill> skillsGained, List<MercenaryDecoration> decorationsEarned) {
            this.termNumber = termNumber;
            this.arm = arm;
            this.startingRank = startingRank;
            this.endingRank = endingRank;
            this.assignments = assignments;
            this.skillsGained = skillsGained;
            this.decorationsEarned = decorationsEarned;
        }

        public int getTermNumber() { return termNumber; }
        public ArmOfService getArm() { return arm; }
        public MercenaryRank getStartingRank() { return startingRank; }
        public MercenaryRank getEndingRank() { return endingRank; }
        public List<AssignmentRecord> getAssignments() { return Collections.unmodifiableList(assignments); }
        public List<MercenarySkill> getSkillsGained() { return Collections.unmodifiableList(skillsGained); }
        public List<MercenaryDecoration> getDecorationsEarned() { return Collections.unmodifiableList(decorationsEarned); }
    }

    /**
     * Record of a single assignment within a term.
     */
    public static class AssignmentRecord {
        private final MercenaryAssignment assignment;
        private final SpecialAssignment specialAssignment;
        private final boolean survived;
        private final boolean wounded;
        private final boolean promoted;
        private final MercenarySkill skillGained;
        private final MercenaryDecoration decoration;

        public AssignmentRecord(MercenaryAssignment assignment, SpecialAssignment specialAssignment,
                                boolean survived, boolean wounded, boolean promoted,
                                MercenarySkill skillGained, MercenaryDecoration decoration) {
            this.assignment = assignment;
            this.specialAssignment = specialAssignment;
            this.survived = survived;
            this.wounded = wounded;
            this.promoted = promoted;
            this.skillGained = skillGained;
            this.decoration = decoration;
        }

        public MercenaryAssignment getAssignment() { return assignment; }
        public SpecialAssignment getSpecialAssignment() { return specialAssignment; }
        public boolean isSurvived() { return survived; }
        public boolean isWounded() { return wounded; }
        public boolean isPromoted() { return promoted; }
        public MercenarySkill getSkillGained() { return skillGained; }
        public MercenaryDecoration getDecoration() { return decoration; }
    }

    /**
     * Creates a new mercenary career.
     *
     * @param branch the branch of service (Army or Marines)
     * @param arm the initial arm of service
     * @param techLevel the tech level of the world (affects skill rolls)
     * @param termsPlanned the number of terms to serve
     */
    public MercenaryCareer(BranchOfService branch, ArmOfService arm, int techLevel, int termsPlanned) {
        this.branch = Objects.requireNonNull(branch, "Branch must not be null");
        this.arm = Objects.requireNonNull(arm, "Arm must not be null");
        if (!arm.isEnterableDirectly()) {
            throw new IllegalArgumentException("Commando cannot be entered directly");
        }
        if (branch == BranchOfService.MARINES && arm != ArmOfService.MARINE && arm != ArmOfService.SUPPORT) {
            throw new IllegalArgumentException("Marines can only enter Marine or Support arms");
        }
        if (branch == BranchOfService.ARMY && arm == ArmOfService.MARINE) {
            throw new IllegalArgumentException("Army cannot enter Marine arm");
        }
        this.techLevel = Math.max(0, Math.min(15, techLevel));
        this.termsPlanned = termsPlanned;
        this.currentTerm = 0;
        this.currentAssignment = 0;
        this.alive = true;
        this.isCommissioned = false;
        this.rank = MercenaryRank.PRIVATE;
        this.mercenarySkills = new HashMap<>();
        this.abilities = new HashMap<>();
        this.decorations = new HashMap<>();
        this.crossTrainedArms = new HashSet<>();
        this.specialAssignments = new ArrayList<>();
        this.termRecords = new ArrayList<>();
    }

    /**
     * Runs the complete career generation for all planned terms.
     *
     * @return true if the character survived all terms
     */
    public boolean runCareer() {
        // Basic training: level 1 Gun Combat
        addMercenarySkill(MercenarySkill.COMBAT_RIFLEMAN, 1);

        // Advanced training: roll one MOS skill
        MercenarySkill mosSkill = rollMosSkill();
        addMercenarySkill(mosSkill, 1);

        for (int term = 1; term <= termsPlanned && alive; term++) {
            currentTerm = term;
            TermRecord record = processTerm();
            termRecords.add(record);

            // Attempt reenlistment after each term except the last
            if (term < termsPlanned && !reenlist()) {
                break;
            }
        }
        return alive;
    }

    /**
     * Processes a single term of service (4 assignments).
     */
    private TermRecord processTerm() {
        MercenaryRank startRank = rank;
        List<AssignmentRecord> assignments = new ArrayList<>();
        List<MercenarySkill> skillsGained = new ArrayList<>();
        List<MercenaryDecoration> decorationsEarned = new ArrayList<>();
        int officerPromotionsThisTerm = 0;

        for (int assignment = 0; assignment < 4 && alive; assignment++) {
            currentAssignment = assignment;

            // Determine general assignment
            int genRoll = DiceUtil.rollD6();
            if (getAbility(AbilityScore.INT) >= 8) {
                genRoll = Math.min(12, genRoll + 1); // may add 1
            }
            if (isCommissioned) {
                genRoll = Math.max(2, genRoll - 1); // buck for command
            }

            GeneralAssignmentType genType = getGeneralAssignment(genRoll);

            if (genType == GeneralAssignmentType.SPECIAL) {
                // Special assignment
                SpecialAssignment special = rollSpecialAssignment();
                processSpecialAssignment(special);
                specialAssignments.add(special);
                assignments.add(new AssignmentRecord(null, special, true, false, false, null, null));
            } else {
                // Unit assignment
                MercenaryAssignment unitAssignment = rollUnitAssignment();
                AssignmentResult result = processUnitAssignment(unitAssignment, genType, officerPromotionsThisTerm);
                if (result.skillGained != null) {
                    skillsGained.add(result.skillGained);
                }
                if (result.decoration != null) {
                    decorationsEarned.add(result.decoration);
                }
                if (result.promoted) {
                    officerPromotionsThisTerm++;
                }
                assignments.add(new AssignmentRecord(
                        unitAssignment, null, result.survived, result.wounded,
                        result.promoted, result.skillGained, result.decoration));
            }
        }

        return new TermRecord(currentTerm, arm, startRank, rank, assignments, skillsGained, decorationsEarned);
    }

    /**
     * General assignment types from the die roll.
     */
    private enum GeneralAssignmentType {
        COMMAND, STAFF, SPECIAL
    }

    private GeneralAssignmentType getGeneralAssignment(int roll) {
        if (roll <= 2) return GeneralAssignmentType.COMMAND;
        if (roll <= 4) return GeneralAssignmentType.STAFF;
        return GeneralAssignmentType.SPECIAL;
    }

    private MercenaryAssignment rollUnitAssignment() {
        int roll = DiceUtil.roll2D6();
        // Use Infantry table as default
        switch (roll) {
            case 2: case 3: return MercenaryAssignment.RAID;
            case 4: case 5: return MercenaryAssignment.COUNTER_INSURGENCY;
            case 6: case 7: case 8: return MercenaryAssignment.GARRISON;
            case 9: case 10: return MercenaryAssignment.TRAINING;
            case 11: return MercenaryAssignment.POLICE_ACTION;
            case 12: return MercenaryAssignment.INTERNAL_SECURITY;
            default: return MercenaryAssignment.TRAINING;
        }
    }

    private SpecialAssignment rollSpecialAssignment() {
        if (isCommissioned) {
            // Officer special assignments
            int roll = DiceUtil.rollD6();
            SpecialAssignment[] officerAssignments = {
                    SpecialAssignment.INTELLIGENCE_SCHOOL,
                    SpecialAssignment.COMMAND_COLLEGE,
                    SpecialAssignment.STAFF_COLLEGE,
                    SpecialAssignment.COMMANDO_SCHOOL,
                    SpecialAssignment.RECRUITING_DUTY,
                    SpecialAssignment.MILITARY_ATTACH
            };
            return officerAssignments[Math.min(roll - 1, officerAssignments.length - 1)];
        } else {
            // Enlisted/NCO special assignments
            int roll = DiceUtil.rollD6();
            if (branch == BranchOfService.MARINES && getAbility(AbilityScore.EDU) >= 7) {
                roll = Math.min(12, roll + 1);
            }
            if (branch == BranchOfService.ARMY && getAbility(AbilityScore.END) >= 7) {
                roll = Math.min(12, roll + 1);
            }
            SpecialAssignment[] enlistedAssignments = {
                    SpecialAssignment.CROSS_TRAINING,
                    SpecialAssignment.SPECIALIST_SCHOOL,
                    SpecialAssignment.COMMANDO_SCHOOL,
                    SpecialAssignment.PROTECTED_FORCES,
                    SpecialAssignment.RECRUITING_DUTY,
                    SpecialAssignment.OCS
            };
            return enlistedAssignments[Math.min(roll - 1, enlistedAssignments.length - 1)];
        }
    }

    private void processSpecialAssignment(SpecialAssignment special) {
        switch (special) {
            case CROSS_TRAINING:
                // Cross-train in another arm
                ArmOfService[] otherArms = ArmOfService.values();
                ArmOfService crossArm;
                do {
                    crossArm = otherArms[DiceUtil.rollIndex(otherArms.length)];
                } while (crossArm == arm || crossArm == ArmOfService.COMMANDO ||
                        (branch == BranchOfService.ARMY && crossArm == ArmOfService.MARINE));
                crossTrainedArms.add(crossArm);
                break;
            case SPECIALIST_SCHOOL:
                // Roll for specialist skill
                MercenarySkill[] specialistSkills = {
                        MercenarySkill.ADMIN, MercenarySkill.MEDIC, MercenarySkill.ELECTRONICS_MERC,
                        MercenarySkill.COMPUTER_MERC, MercenarySkill.MECHANICAL, MercenarySkill.ELECTRONICS_MERC
                };
                addMercenarySkill(specialistSkills[DiceUtil.rollIndex(specialistSkills.length)], 1);
                break;
            case COMMANDO_SCHOOL:
                // Commando school skills (roll 5+ for each)
                MercenarySkill[] commandoSkills = {
                        MercenarySkill.BRAWLING, MercenarySkill.COMBAT_RIFLEMAN, MercenarySkill.DEMOLITIONS,
                        MercenarySkill.WILDERNESS_SURVIVAL, MercenarySkill.RECON, MercenarySkill.VACC_SUIT,
                        MercenarySkill.BLADE_COMBAT, MercenarySkill.INSTRUCTION
                };
                for (MercenarySkill skill : commandoSkills) {
                    if (DiceUtil.rollD6() >= 5) {
                        addMercenarySkill(skill, 1);
                    }
                }
                break;
            case PROTECTED_FORCES:
                // Roll 3+ for Vacc Suit and Zero-G Combat
                if (DiceUtil.rollD6() >= 3) addMercenarySkill(MercenarySkill.VACC_SUIT, 1);
                if (DiceUtil.rollD6() >= 3) addMercenarySkill(MercenarySkill.ZERO_G_COMBAT, 1);
                break;
            case RECRUITING_DUTY:
                addMercenarySkill(MercenarySkill.RECRUITING, 1);
                break;
            case OCS:
                // Commission as Second Lieutenant
                isCommissioned = true;
                rank = MercenaryRank.SECOND_LIEUTENANT;
                // One command, one staff, one MOS skill
                addMercenarySkill(MercenarySkill.TACTICS_MERC, 1);
                addMercenarySkill(MercenarySkill.ADMIN, 1);
                addMercenarySkill(rollMosSkill(), 1);
                break;
            case INTELLIGENCE_SCHOOL:
                // Roll 4+ for each
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.FORGERY_MERC, 1);
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.BRIBERY, 1);
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.STREETWISE, 1);
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.INTERROGATION, 1);
                break;
            case COMMAND_COLLEGE:
                // Roll 4+ for each
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.TACTICS_MERC, 1);
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.LEADER, 1);
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.RECON, 1);
                break;
            case STAFF_COLLEGE:
                // Roll 4+ for each
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.ADMIN, 1);
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.COMBAT_ENGINEERING, 1);
                if (DiceUtil.rollD6() >= 4) addMercenarySkill(MercenarySkill.COMPUTER_MERC, 1);
                break;
            case MILITARY_ATTACH:
                // 1-4: attache (promotion +1 Soc), 5-6: aide (+1 Soc, choose next assignment)
                if (DiceUtil.rollD6() <= 4) {
                    promote();
                }
                break;
        }
    }

    private static class AssignmentResult {
        boolean survived;
        boolean wounded;
        boolean promoted;
        MercenarySkill skillGained;
        MercenaryDecoration decoration;
    }

    private AssignmentResult processUnitAssignment(MercenaryAssignment assignment,
                                                   GeneralAssignmentType genType,
                                                   int officerPromotionsThisTerm) {
        AssignmentResult result = new AssignmentResult();

        // Survival
        int survivalThreshold = assignment.getSurvivalThreshold(arm);
        if (survivalThreshold == Integer.MAX_VALUE) {
            result.survived = true;
        } else {
            int survivalRoll = DiceUtil.roll2D6();
            // DM +1 if any MOS skill >= 2
            if (hasMosSkill2OrGreater()) {
                survivalRoll++;
            }
            if (survivalRoll < survivalThreshold) {
                result.survived = false;
                alive = false;
                return result;
            }
            result.survived = true;
            result.wounded = survivalRoll == survivalThreshold;
            if (result.wounded && assignment.isCombat()) {
                addDecoration(MercenaryDecoration.PURPLE_HEART);
            }
        }

        // Combat service ribbon
        if (assignment.isCombat()) {
            addDecoration(MercenaryDecoration.COMBAT_SERVICE_RIBBON);
        }

        // Combat command for officers
        if (isCommissioned && genType == GeneralAssignmentType.COMMAND && assignment.isCombat()) {
            addDecoration(MercenaryDecoration.COMBAT_COMMAND);
        }

        // Decoration
        int decThreshold = assignment.getDecorationThreshold(arm);
        if (decThreshold != Integer.MAX_VALUE) {
            int decRoll = DiceUtil.roll2D6();
            if (decRoll >= decThreshold + 6) {
                result.decoration = MercenaryDecoration.SEH;
                addDecoration(MercenaryDecoration.SEH);
            } else if (decRoll >= decThreshold + 3) {
                result.decoration = MercenaryDecoration.MCG;
                addDecoration(MercenaryDecoration.MCG);
            } else if (decRoll >= decThreshold) {
                result.decoration = MercenaryDecoration.MCUF;
                addDecoration(MercenaryDecoration.MCUF);
            }
        }

        // Promotion (officers: max 1 per term; enlisted/NCO: each assignment)
        if (genType != GeneralAssignmentType.SPECIAL) {
            int promoThreshold = assignment.getPromotionThreshold(arm);
            if (promoThreshold != Integer.MAX_VALUE) {
                boolean canPromote = true;
                if (isCommissioned && officerPromotionsThisTerm >= 1) {
                    canPromote = false;
                }
                if (canPromote) {
                    int promoRoll = DiceUtil.roll2D6();
                    // DM +1 if Education 7+
                    if (getAbility(AbilityScore.EDU) >= 7) {
                        promoRoll++;
                    }
                    if (promoRoll >= promoThreshold) {
                        result.promoted = promote();
                    }
                }
            }
        }

        // Skills
        int skillThreshold = assignment.getSkillThreshold(arm);
        if (skillThreshold != Integer.MAX_VALUE) {
            int skillRoll = DiceUtil.roll2D6();
            if (skillRoll >= skillThreshold) {
                // Roll for skill based on assignment type
                MercenarySkill skill;
                if (genType == GeneralAssignmentType.COMMAND && isCommissioned) {
                    skill = rollCommandSkill();
                } else if (genType == GeneralAssignmentType.STAFF && isCommissioned) {
                    skill = rollStaffSkill();
                } else {
                    skill = rollMosSkill();
                }
                result.skillGained = skill;
                addMercenarySkill(skill, 1);
            }
        }

        return result;
    }

    private MercenarySkill rollMosSkill() {
        List<MercenarySkill> table = arm.getMosTable();
        int index = Math.min(DiceUtil.rollD6(), table.size() - 1);
        // TL 12+ adds 1 to the roll
        if (techLevel >= 12) {
            index = Math.min(index + 1, table.size() - 1);
        }
        return table.get(index);
    }

    private MercenarySkill rollCommandSkill() {
        MercenarySkill[] commandSkills = {MercenarySkill.TACTICS_MERC, MercenarySkill.LEADER};
        return commandSkills[DiceUtil.rollIndex(commandSkills.length)];
    }

    private MercenarySkill rollStaffSkill() {
        MercenarySkill[] staffSkills = {MercenarySkill.ADMIN, MercenarySkill.COMPUTER_MERC};
        return staffSkills[DiceUtil.rollIndex(staffSkills.length)];
    }

    private boolean hasMosSkill2OrGreater() {
        for (Map.Entry<MercenarySkill, Integer> entry : mercenarySkills.entrySet()) {
            if (entry.getValue() >= 2) return true;
        }
        return false;
    }

    private boolean promote() {
        if (rank == null) return false;
        MercenaryRank next = rank.nextHigher();
        if (next != null) {
            rank = next;
            if (rank.isOfficer()) {
                isCommissioned = true;
            }
            return true;
        }
        return false;
    }

    private void addMercenarySkill(MercenarySkill skill, int level) {
        mercenarySkills.merge(skill, level, Integer::sum);
    }

    private void addDecoration(MercenaryDecoration dec) {
        decorations.merge(dec, 1, Integer::sum);
        switch (dec) {
            case MCUF: mcufCount++; break;
            case MCG: mcgCount++; break;
            case SEH: sehCount++; break;
            case PURPLE_HEART: purpleHeartCount++; break;
            case COMBAT_SERVICE_RIBBON: csrCount++; break;
            case COMBAT_COMMAND: combatCommandCount++; break;
        }
    }

    private boolean reenlist() {
        int roll = DiceUtil.roll2D6();
        // Army enlisted/NCO: +2
        if (branch == BranchOfService.ARMY && !isCommissioned) {
            roll += 2;
        }
        // Marines cross-trained: +1
        if (branch == BranchOfService.MARINES && !crossTrainedArms.isEmpty()) {
            roll += 1;
        }
        return roll >= 8;
    }

    private int getAbility(AbilityScore ability) {
        return abilities.getOrDefault(ability, 0);
    }

    // ---- Setters for ability scores ----

    /**
     * Sets the ability scores from a Traveller character.
     *
     * @param abilities the ability scores
     */
    public void setAbilities(Map<AbilityScore, Integer> abilities) {
        this.abilities.putAll(abilities);
    }

    // ---- Getters ----

    public BranchOfService getBranch() { return branch; }
    public ArmOfService getArm() { return arm; }
    public MercenaryRank getRank() { return rank; }
    public int getTechLevel() { return techLevel; }
    public boolean isAlive() { return alive; }
    public boolean isCommissioned() { return isCommissioned; }
    public int getTermsServed() { return termRecords.size(); }

    public Map<MercenarySkill, Integer> getMercenarySkills() {
        return Collections.unmodifiableMap(mercenarySkills);
    }

    public Map<MercenaryDecoration, Integer> getDecorations() {
        return Collections.unmodifiableMap(decorations);
    }

    public int getMcufCount() { return mcufCount; }
    public int getMcgCount() { return mcgCount; }
    public int getSehCount() { return sehCount; }
    public int getPurpleHeartCount() { return purpleHeartCount; }
    public int getCsrCount() { return csrCount; }
    public int getCombatCommandCount() { return combatCommandCount; }

    public Set<ArmOfService> getCrossTrainedArms() {
        return Collections.unmodifiableSet(crossTrainedArms);
    }

    public List<SpecialAssignment> getSpecialAssignments() {
        return Collections.unmodifiableList(specialAssignments);
    }

    public List<TermRecord> getTermRecords() {
        return Collections.unmodifiableList(termRecords);
    }

    /**
     * Calculates the total experience points for morale.
     *
     * @return the total experience points
     */
    public int calculateExperiencePoints() {
        int total = 0;
        // Combat service ribbons
        total += csrCount;
        // Purple hearts
        total += purpleHeartCount;
        // Tactics skill levels
        total += mercenarySkills.getOrDefault(MercenarySkill.TACTICS_MERC, 0);
        // MCUF
        total += mcufCount;
        // MCG (2 each)
        total += mcgCount * 2;
        // SEH (3 each)
        total += sehCount * 3;
        return total;
    }

    /**
     * Calculates the morale rating (experience points + 1D6).
     *
     * @return the morale rating (number or less on 2D6 to pass)
     */
    public int calculateMorale() {
        return calculateExperiencePoints() + DiceUtil.rollD6();
    }

    /**
     * Generates a resume string for this career.
     *
     * @return the resume text
     */
    public String generateResume() {
        StringBuilder sb = new StringBuilder();
        sb.append("Branch: ").append(branch.getName()).append("\n");
        sb.append("Terms: ").append(getTermsServed()).append("\n");
        sb.append("Arm: ").append(arm.getName()).append("\n");
        sb.append("Final Rank: ").append(rank.getName()).append("\n");

        if (!specialAssignments.isEmpty()) {
            sb.append("Special Assignments: ");
            sb.append(specialAssignments.stream().map(SpecialAssignment::getName).collect(java.util.stream.Collectors.joining(", ")));
            sb.append("\n");
        }

        sb.append("Decorations: ");
        sb.append("MCUF=").append(mcufCount).append(", ");
        sb.append("MCG=").append(mcgCount).append(", ");
        sb.append("SEH=").append(sehCount).append(", ");
        sb.append("PH=").append(purpleHeartCount).append(", ");
        sb.append("CSR=").append(csrCount).append(", ");
        sb.append("CC=").append(combatCommandCount).append("\n");

        sb.append("Skills: ");
        sb.append(mercenarySkills.entrySet().stream()
                .map(e -> e.getKey().getName() + "-" + e.getValue())
                .collect(java.util.stream.Collectors.joining(", ")));

        return sb.toString();
    }
}
