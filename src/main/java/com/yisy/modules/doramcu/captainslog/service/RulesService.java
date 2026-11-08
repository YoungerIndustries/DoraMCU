package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.RulesEntry;
import com.yisy.modules.doramcu.captainslog.model.RulesTable;
import com.yisy.modules.doramcu.captainslog.repository.RulesEntryRepository;
import com.yisy.modules.doramcu.captainslog.repository.RulesTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing and querying the Captain's Log rulebook reference database.
 * <p>
 * This service populates the rules database on application startup with all the
 * official rules extracted from the Captain's Log Solo RPG rulebook. It provides
 * query methods for searching rules by category, chapter, or keyword.
 * </p>
 * <p>
 * The rules database serves as the authoritative reference for both the AI mission
 * generation system and the game mechanics, ensuring that all task completions
 * follow the published rules.
 * </p>
 */
@Service
public class RulesService {

    private static final Logger log = LoggerFactory.getLogger(RulesService.class);

    private final RulesEntryRepository entryRepository;
    private final RulesTableRepository tableRepository;
    private boolean initialized = false;

    public RulesService(RulesEntryRepository entryRepository, RulesTableRepository tableRepository) {
        this.entryRepository = entryRepository;
        this.tableRepository = tableRepository;
    }

    // ==================== Initialization ====================

    /**
     * Populates the rules database on application startup if empty.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeRulesDatabase() {
        if (initialized) {
            return;
        }
        if (entryRepository.count() > 0) {
            log.info("Rules database already populated with {} entries", entryRepository.count());
            initialized = true;
            return;
        }

        log.info("Initializing rules database from Captain's Log rulebook...");
        populateRuleEntries();
        populateOracleTables();
        initialized = true;
        log.info("Rules database initialized: {} entries, {} table entries",
                entryRepository.count(), tableRepository.count());
    }

    // ==================== Rule Entries Population ====================

    @Transactional
    public void populateRuleEntries() {
        List<RulesEntry> entries = new ArrayList<>();

        // --- Chapter 05.00: Rules of Play ---

        // 2d20 Core Mechanic
        entries.add(createEntry(
                "2d20 Task Attempt Core Mechanic",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                179,
                "The core mechanic of Captain's Log is the 2d20 task attempt. To perform a task attempt, roll 2d20 and compare each die individually to the Target Number. Each die that rolls equal to or below the Target Number counts as 1 success. A roll of 1 on any die counts as 2 successes. A roll of 20 on any die is a complication and does not count as a success. If the total successes meet or exceed the Difficulty, the task succeeds.",
                "This is the fundamental mechanic used for all checks in the game. Target Number = Attribute + Discipline for characters, or System + Department for starships."
        ));

        entries.add(createEntry(
                "Target Number Calculation",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                180,
                "The Target Number for a task attempt is calculated by adding one Attribute value and one Discipline value. For character task attempts, this is the character's Attribute rating plus their Discipline rating. For starship task attempts, this is the starship's System rating plus the Department rating. The Target Number determines how easy it is to succeed on a roll — higher Target Numbers mean more dice results count as successes.",
                "Example: A character with Fitness 3 and Security 2 has a Target Number of 5 for Fitness+Security checks."
        ));

        entries.add(createEntry(
                "Difficulty Levels",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                181,
                "Difficulty represents the number of successes needed for a task attempt to succeed. The default Difficulty is 1. Harder tasks require more successes: Difficulty 2 requires 2 successes, Difficulty 3 requires 3 successes. The GM (or the solo player using the oracle) sets Difficulty based on the narrative context, urgency, and stakes of the task.",
                "In Captain's Log, Difficulty rarely exceeds 3. Use Difficulty 2 for challenging tasks and Difficulty 3 for extremely difficult or urgent tasks."
        ));

        entries.add(createEntry(
                "Focus Bonus",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                182,
                "If a character has a Focus (specialty) that applies to the task attempt, any die that rolls equal to or below the Discipline value counts as 2 successes instead of 1. Without a Focus, only a natural 1 counts as 2 successes. Focuses represent specialized training in a narrow area within a Discipline.",
                "Example: A character with Security 3 and a 'Hand-to-Hand Combat' Focus — any die rolling 3 or below counts as 2 successes during combat checks."
        ));

        entries.add(createEntry(
                "Natural 20 Complications",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                183,
                "A natural 20 on any die is a complication. It does not count as a success. Instead, it generates 1 Threat that is added to the mission's Threat pool. Complications represent unexpected problems that arise during the task attempt. The player should narrate a complication that makes the situation more challenging.",
                "Complications are a core part of the 2d20 system and add narrative tension. In Captain's Log, they are especially important for solo play."
        ));

        entries.add(createEntry(
                "Boons and Threats (Dice Modifiers)",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                184,
                "Boons and Threats are additional dice rolled alongside the base 2d20. Boons represent advantages and helpful circumstances; Threats represent disadvantages and complications. After rolling all dice (2d20 + boons + threats), sort them in descending order and keep only the best 2 dice. Boons and Threats cancel each other out: 1 Boon and 1 Threat have no net effect.",
                "In Captain's Log, you cannot spend Momentum to add extra dice. Boons and Threats come from narrative circumstances, crew assistance, equipment, and spending resources."
        ));

        // Momentum System
        entries.add(createEntry(
                "Momentum System Overview",
                "05.00 Rules of Play",
                "MOMENTUM",
                190,
                "Momentum is a resource that represents narrative advantage. In Captain's Log, the maximum Momentum is 1. Momentum is generated when a task attempt produces excess successes beyond the Difficulty. Momentum can be spent to: reroll one die on a task attempt, remove a Threat from the Threat pool, or gain a narrative advantage (roll on the Advantage table).",
                "Momentum is capped at 1 in Captain's Log, unlike the full STA system where it can accumulate higher."
        ));

        entries.add(createEntry(
                "Spending Momentum",
                "05.00 Rules of Play",
                "MOMENTUM",
                191,
                "Momentum can be spent before or after a task attempt. Before a roll: reroll one die (reroll the worst die kept). After a roll: remove a Threat from the Threat pool, or gain a narrative advantage. When spending Momentum to reroll, the Momentum is consumed regardless of the outcome. When spending Momentum to remove a Threat, both the Momentum and one Threat are consumed.",
                "Strategic use of Momentum is key to Captain's Log gameplay. Save it for critical moments or use it proactively to improve odds."
        ));

        // Threat System
        entries.add(createEntry(
                "Threat System Overview",
                "05.00 Rules of Play",
                "THREAT",
                193,
                "Threat represents narrative complications and adversity. Threat is generated by natural 20s (complications), failed task attempts, and narrative choices. Threat can be spent by the player to gain a Boon on a task attempt. Alternatively, Threat can be resolved by rolling on the Complication table, which introduces a narrative complication. Momentum can be spent to remove Threat.",
                "Threat adds tension to solo play. When Threat accumulates, the player must decide whether to spend it for advantage or face the consequences."
        ));

        entries.add(createEntry(
                "Spending Threat",
                "05.00 Rules of Play",
                "THREAT",
                194,
                "When you have Threat in the Threat pool, you can spend 1 Threat to gain 1 Boon on a task attempt. This represents using the complications in the narrative to your advantage. Alternatively, you can resolve Threat by rolling on the Complication table, which introduces a new complication to the mission.",
                "Spending Threat for a Boon is a powerful option — use it when a task attempt is critical to the mission."
        ));

        // Attributes
        entries.add(createEntry(
                "Character Attributes",
                "03.00 Reporting for Duty",
                "CHARACTER_CREATION",
                71,
                "Characters have six Attributes that define their capabilities: Fitness (physical ability and endurance), Presence (force of personality and social skills), Strength (raw physical power), Reason (analytical thinking and problem-solving), Technical (mechanical and technological aptitude), and Insight (intuition and perception). Each Attribute has a rating from 1 to 5, with 3 being the average.",
                "Attributes are combined with Disciplines to calculate Target Numbers for task attempts."
        ));

        entries.add(createEntry(
                "Character Disciplines",
                "03.00 Reporting for Duty",
                "CHARACTER_CREATION",
                75,
                "Disciplines represent a character's professional training. The six Disciplines are: Command (leadership and decision-making), Engineering (ship systems and technology), Leadership (inspiring and directing others), Medicine (healing and biological science), Operations (navigation and piloting), Science (research and analysis), and Security (combat and defense). Each Discipline has a rating from 0 to 5.",
                "Disciplines are combined with Attributes to calculate Target Numbers. A character's primary Discipline typically has the highest rating."
        ));

        // --- Combat Rules ---

        entries.add(createEntry(
                "Physical Combat Overview",
                "05.00 Rules of Play",
                "COMBAT",
                200,
                "Physical combat in Captain's Log is simplified compared to the full STA system. Characters and opponents have hits (health). If no initiative is used, both sides start with 1 hit. Each round, the character performs a task attempt using an appropriate Attribute and Discipline combination. Success deals 1 hit to the opponent. Failure means the character takes 1 hit. Extra successes generate Momentum. Natural 20s generate Threat and complications.",
                "Combat continues until one side is defeated (0 hits remaining). The character can attempt to retreat if the situation becomes too dangerous."
        ));

        entries.add(createEntry(
                "Starship Combat Overview",
                "05.00 Rules of Play",
                "COMBAT",
                205,
                "Starship combat follows the same basic structure as physical combat but on a larger scale. The ship's hit points are determined by its scale: Small=2, Medium=3, Large=4, Capital=5, Station=6. Task attempts use the starship's System ratings combined with Department ratings. Two extra hits represent critical damage to systems.",
                "Starship combat uses the same 2d20 mechanic. Failed attempts can add stress to ship systems."
        ));

        entries.add(createEntry(
                "Combat Scale and Hit Points",
                "05.00 Rules of Play",
                "COMBAT",
                206,
                "The scale of a vessel or opponent determines its hit points in combat. Small targets (individuals, small craft) have 2 hits. Medium targets (standard starships) have 3 hits. Large targets (cruisers, large vessels) have 4 hits. Capital targets (battleships, large stations) have 5 hits. Station targets have 6 hits. Two extra hits beyond the normal amount represent critical damage.",
                "When designing opponents, choose a scale that matches the narrative threat level."
        ));

        // --- Mission Structure ---

        entries.add(createEntry(
                "Three-Act Mission Structure",
                "06.00 Playing Captain's Log",
                "MISSION_STRUCTURE",
                225,
                "Each mission in Captain's Log follows a three-act structure with 15 narrative beats total (5 beats per act). Act 1 (Opening Log) establishes the situation and introduces the conflict. Act 2 (Supplementary Logs) develops the conflict and raises the stakes. Act 3 (Closing Log) resolves the mission and shows the consequences. Each beat involves narration, a task attempt (if needed), and resolution.",
                "The three-act structure provides a framework for solo play. Use the oracle tables to generate content for each act."
        ));

        entries.add(createEntry(
                "Narrative Beats",
                "06.00 Playing Captain's Log",
                "MISSION_STRUCTURE",
                226,
                "Each act contains 5 narrative beats. A beat is a scene or moment in the mission. At each beat, the player narrates what happens, performs a task attempt if the situation calls for it, and resolves the outcome. After 5 beats, the act concludes and the next act begins. The player writes an in-character log entry at the transition between acts.",
                "Beats help pace the mission. Not every beat requires a dice roll — some are purely narrative."
        ));

        entries.add(createEntry(
                "Mission Types",
                "06.00 Playing Captain's Log",
                "MISSION_STRUCTURE",
                228,
                "Missions can be of various types, including Diplomacy, Exploration, Combat, Science, Rescue, Investigation, Trade, Surveillance, Crisis, First Contact, Maintenance, Patrol, Transport, Medical, Search, Escort, Reconnaissance, Negotiation, Survival, and Conspiracy. The mission type influences the tasks, challenges, and narrative tone of the mission.",
                "Use the Mission Type probability matrix to randomly generate mission types."
        ));

        // --- Starship Systems ---

        entries.add(createEntry(
                "Starship Systems",
                "04.00 Your Home Among the Stars",
                "STARSHIP",
                139,
                "Starships have six Systems: Comms (communications and hailing), Computers (data processing and automation), Engines (warp and impulse propulsion), Sensors (detection and scanning), Structure (hull integrity and shields), and Weapons (offensive armaments). Each System has a rating that is combined with a Department rating for starship task attempts.",
                "Starship systems can take stress during operations. At maximum stress, systems fail and require repair."
        ));

        entries.add(createEntry(
                "Starship Departments",
                "04.00 Your Home Among the Stars",
                "STARSHIP",
                140,
                "Starships have six Departments: Command (ship-wide leadership), Engineering (power and propulsion), Medical (health and biology), Operations (navigation and flight), Science (research and analysis), and Security (defense and internal order). Department ratings are combined with System ratings for starship task attempts.",
                "Department ratings reflect the crew's capability in that area."
        ));

        // --- Oracle System ---

        entries.add(createEntry(
                "Yes/No Oracle",
                "06.00 Playing Captain's Log",
                "ORACLE",
                230,
                "The Yes/No oracle is the core tool for solo play. The player sets a probability level (Very Unlikely, Unlikely, 50/50, Likely, Very Likely, Certain) based on the narrative context, then rolls to determine the answer. The probability level determines the likelihood of a Yes result. Results can be Yes, Yes+, Yes-, No, No+, No-, Both/Neither, Critical Yes, or Critical No.",
                "The oracle replaces the GM's judgment in solo play. Use it to answer questions about the world and situation."
        ));

        entries.add(createEntry(
                "Oracle Probability Levels",
                "06.00 Playing Captain's Log",
                "ORACLE",
                231,
                "Before asking the oracle a Yes/No question, set the probability level based on the narrative context. Very Unlikely means the answer is almost certainly No. Unlikely means the answer is probably No. 50/50 means the answer is equally likely to be Yes or No. Likely means the answer is probably Yes. Very Likely means the answer is almost certainly Yes. Certain means the answer is definitely Yes, but the oracle may still produce complications.",
                "Setting the probability level is a skill. Practice improves your ability to judge the odds accurately."
        ));

        entries.add(createEntry(
                "Oracle Results Interpretation",
                "06.00 Playing Captain's Log",
                "ORACLE",
                232,
                "The oracle produces more than simple Yes/No answers. Yes+ means Yes, and you gain Momentum. Yes- means Yes, but with a complication or cost. No+ means No, but something helpful happens. No- means No, and you gain Threat. Both/Neither means both outcomes occur or neither occurs. Critical Yes means an overwhelming Yes with major consequences. Critical No means an overwhelming No with major consequences.",
                "The + and - modifiers add narrative depth to oracle results. Use them to drive the story forward."
        ));

        // --- Chapter 01.00: Star Trek Defined ---

        entries.add(createEntry(
                "Star Trek Defined: The Prime Directive",
                "01.00 Star Trek Defined",
                "SETTING",
                7,
                "The Prime Directive is the foundational principle of the United Federation of Planets: non-interference with the natural development of pre-warp civilizations. It is a moral and legal constraint that shapes every decision in Star Trek. Violating the Prime Directive carries serious consequences, but the gray areas and exceptions are where the drama lies.",
                "In Captain's Log, the Prime Directive often creates moral dilemmas during missions."
        ));

        entries.add(createEntry(
                "Star Trek Defined: Exploration and Discovery",
                "01.00 Star Trek Defined",
                "SETTING",
                9,
                "Exploration is the core mission of Starfleet. The mandate to go where no one has gone before drives every character, every ship, and every mission. Discovery of new worlds, new life, and new ideas is the purpose of the Federation. Exploration involves risk, curiosity, and the courage to face the unknown.",
                "Missions in Captain's Log should always reflect the spirit of exploration, even when the mission type is combat or diplomacy."
        ));

        entries.add(createEntry(
                "Star Trek Defined: Diplomacy and Peace",
                "01.00 Star Trek Defined",
                "SETTING",
                11,
                "Diplomacy is the preferred method of conflict resolution in the Star Trek universe. The Federation seeks peaceful coexistence with all sentient species. Diplomacy involves negotiation, cultural understanding, and the willingness to find common ground. When diplomacy fails, other measures may be necessary, but they are always a last resort.",
                "Diplomacy missions in Captain's Log test the character's Presence and Command attributes."
        ));

        entries.add(createEntry(
                "Star Trek Defined: The Future of Humanity",
                "01.00 Star Trek Defined",
                "SETTING",
                13,
                "Star Trek envisions a future where humanity has overcome poverty, disease, and prejudice. Post-scarcity society means that material needs are met, and people work for personal growth and the betterment of others. This utopian vision is not perfect — conflicts still arise from ideology, culture, and ambition — but the baseline is hope and progress.",
                "This optimistic vision shapes the tone of Captain's Log missions. Even in dark situations, hope persists."
        ));

        // --- Chapter 02.00: The Star Trek Universe ---

        entries.add(createEntry(
                "The United Federation of Planets",
                "02.00 The Star Trek Universe",
                "SETTING",
                17,
                "The United Federation of Planets is a democratic alliance of over 150 member worlds, with Earth as its capital. The Federation is governed by the Federation Council, which sets policy and represents member worlds. Starfleet is the exploration and defense arm of the Federation. The Federation's core values are peace, exploration, scientific discovery, and mutual aid.",
                "The Federation is the primary political entity in Captain's Log. Most characters serve the Federation in some capacity."
        ));

        entries.add(createEntry(
                "Starfleet Organization",
                "02.00 The Star Trek Universe",
                "SETTING",
                19,
                "Starfleet is the exploration, scientific research, and defense service of the Federation. It operates starships, space stations, and planetary facilities. Starfleet ranks from Ensign (junior officer) to Admiral (fleet commander). The chain of command is strict, but individual initiative is valued. Starfleet Academy trains officers in various disciplines.",
                "Character ranks in Captain's Log range from Ensign to Captain, with promotions earned through career events."
        ));

        entries.add(createEntry(
                "The Klingon Empire",
                "02.00 The Star Trek Universe",
                "SETTING",
                21,
                "The Klingon Empire is a warrior culture that values honor, strength, and martial prowess. Klingons are fierce opponents but can be allies when honor dictates. The Empire is governed by the Klingon High Council. Klingon society is hierarchical, with warriors at the top. Despite their aggression, Klingons have a deep code of honor that they expect others to respect.",
                "Klingon adversaries in Captain's Log are honorable but deadly. They respect strength and courage."
        ));

        entries.add(createEntry(
                "The Romulan Star Empire",
                "02.00 The Star Trek Universe",
                "SETTING",
                23,
                "The Romulan Star Empire is a secretive and cunning rival of the Federation. Romulans value subtlety, deception, and strategic advantage. The Tal Shiar is the intelligence and security agency that permeates Romulan society. Romulan technology includes cloaking devices and advanced weapons. Despite their rivalry with the Federation, Romulans can be negotiated with.",
                "Romulan adversaries in Captain's Log are cunning and patient. They prefer indirect approaches."
        ));

        entries.add(createEntry(
                "The Ferengi Alliance",
                "02.00 The Star Trek Universe",
                "SETTING",
                25,
                "The Ferengi Alliance is a profit-driven society governed by the Rules of Acquisition. Ferengi are skilled traders, merchants, and entrepreneurs. They value wealth above all else, but family and honor also matter. The Ferengi Commerce Authority regulates trade. Despite their greed, Ferengi can be reliable allies when the deal is right.",
                "Ferengi in Captain's Log are motivated by profit. Negotiations with them involve finding mutual benefit."
        ));

        entries.add(createEntry(
                "The Cardassian Union",
                "02.00 The Star Trek Universe",
                "SETTING",
                27,
                "The Cardassian Union is an authoritarian militaristic state. Cardassians value order, discipline, and strength. The Union has a history of expansionism and occupation. The Obsidian Order is the secret police and intelligence agency. Cardassian society is rigidly hierarchical. Despite their authoritarian nature, Cardassians can be pragmatic allies.",
                "Cardassian adversaries in Captain's Log are ruthless and calculating."
        ));

        entries.add(createEntry(
                "The Borg Collective",
                "02.00 The Star Trek Universe",
                "SETTING",
                29,
                "The Borg are a cybernetic collective that assimilates other species to achieve perfection. Individuality is erased; all members share a collective consciousness. The Borg are relentless and technologically advanced. Resistance to the Borg is nearly impossible through conventional means. The Borg represent the ultimate threat to individuality and freedom.",
                "Borg encounters in Captain's Log are among the most dangerous. Survival requires creativity and sacrifice."
        ));

        // --- Chapter 03.00: Character Creation (expanded) ---

        entries.add(createEntry(
                "Character Talents",
                "03.00 Reporting for Duty",
                "CHARACTER_CREATION",
                77,
                "Talents are special abilities that provide unique advantages. Each talent has a specific effect, such as rerolling a die once per mission, gaining an automatic success on a specific type of check, or providing a narrative advantage. Talents are gained through lifepath choices or career events. A character typically has 1-3 talents.",
                "Talents add uniqueness to characters. Choose talents that reflect your character's background and personality."
        ));

        entries.add(createEntry(
                "Character Values",
                "03.00 Reporting for Duty",
                "CHARACTER_CREATION",
                79,
                "Values represent what a character holds dear. Common values include Honor, Duty, Family, Freedom, Knowledge, Justice, Loyalty, Courage, Compassion, and Curiosity. Values guide a character's decisions and create dramatic tension when they conflict. During debriefing, values may be tested, challenged, or even changed.",
                "Values are the heart of a character. They drive the narrative and create compelling stories."
        ));

        entries.add(createEntry(
                "Lifepath Character Creation",
                "03.00 Reporting for Duty",
                "CHARACTER_CREATION",
                81,
                "Lifepath creation is a step-by-step narrative process that builds a character through a series of choices. Each choice affects the character's attributes, disciplines, focuses, talents, and values. The lifepath covers childhood, education, early career, and key life events. The result is a character with a rich backstory and clear motivations.",
                "Use the Lifepath service for detailed character creation. Each step presents narrative choices that shape the character."
        ));

        entries.add(createEntry(
                "Creation in Play",
                "03.00 Reporting for Duty",
                "CHARACTER_CREATION",
                83,
                "Creation in Play is a faster method of character creation. The character starts with average stats and develops through the first few missions. As the character faces challenges, the player makes choices that define the character's abilities and personality. This method is ideal for quick starts and experimental characters.",
                "Use Creation in Play when you want to jump straight into the action and develop the character organically."
        ));

        entries.add(createEntry(
                "Character Constitution and Health",
                "03.00 Reporting for Duty",
                "CHARACTER_CREATION",
                85,
                "Constitution represents a character's physical resilience. It determines how much damage a character can take before being incapacitated. Constitution is derived from the Fitness attribute. A character with high Constitution can endure more punishment. Health states range from Healthy to Stunned to Injured to Down.",
                "Managing health is important during combat and dangerous missions. Use the Injury service to track health states."
        ));

        // --- Chapter 04.00: Starships (expanded) ---

        entries.add(createEntry(
                "Starship Talents",
                "04.00 Your Home Among the Stars",
                "STARSHIP",
                141,
                "Starships can have talents that provide special capabilities. Examples include Enhanced Sensors (improved detection range), Stealth Systems (reduced detectability), Advanced Weapons (increased damage), Reinforced Hull (extra hit points), and Medical Bay (improved healing). Talents are chosen during starship creation and reflect the ship's design and modifications.",
                "Starship talents add variety to different vessel types. Choose talents that complement your mission style."
        ));

        entries.add(createEntry(
                "Starship Stress System",
                "04.00 Your Home Among the Stars",
                "STARSHIP",
                143,
                "Each starship system can accumulate stress during operations. Stress represents wear and tear on the system. When a system reaches maximum stress, it fails and requires repair. Stress is generated by failed task attempts, combat damage, and environmental hazards. Stress can be reduced during maintenance periods.",
                "Managing system stress is critical for long missions. Plan maintenance downtime to keep systems operational."
        ));

        entries.add(createEntry(
                "Starship Scale",
                "04.00 Your Home Among the Stars",
                "STARSHIP",
                145,
                "Starship scale determines the vessel's size and capabilities. Small ships (shuttles, runabouts) have limited systems and crew. Medium ships (Constitution-class) are standard exploration vessels. Large ships (Galaxy-class) have extensive systems and large crews. Capital ships (Sovereign-class) are flagships with maximum capabilities. Stations are fixed installations with specialized functions.",
                "Scale affects hit points in combat and the number of systems available. Choose a scale that matches your mission needs."
        ));

        entries.add(createEntry(
                "Starship Creation Process",
                "04.00 Your Home Among the Stars",
                "STARSHIP",
                147,
                "Creating a starship involves choosing a class, assigning system ratings, assigning department ratings, choosing talents, and naming the vessel. System ratings determine the ship's technical capabilities. Department ratings reflect the crew's proficiency. The total points available for ratings depends on the ship's scale. A balanced ship performs well in most situations; a specialized ship excels in specific areas.",
                "Use the Starship Creation service to build a vessel step by step."
        ));

        // --- Chapter 05.00: Rules of Play (expanded) ---

        entries.add(createEntry(
                "Task Attempt Variations",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                185,
                "Task attempts can be modified by various factors. Contested checks involve two parties rolling against each other; the one with more successes wins. Timed checks require a certain number of successes before time runs out. Extended checks involve multiple rolls over time, accumulating successes and failures. Group checks involve multiple characters contributing to a single task.",
                "Different task types add variety to gameplay. Use the appropriate variation for the narrative situation."
        ));

        entries.add(createEntry(
                "Contested Checks",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                186,
                "In a contested check, both parties perform task attempts using relevant attributes and disciplines. The party with more successes wins. If the successes are tied, the status quo remains. Contested checks are used for social confrontations, races, hacking attempts, and any situation where two parties are directly opposed.",
                "Contested checks add tension to social and competitive situations."
        ));

        entries.add(createEntry(
                "Extended Checks",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                187,
                "Extended checks involve multiple task attempts over time. Each success moves the task forward; each failure sets it back. The task requires a certain number of total successes to complete. Failures may generate complications or consume time. Extended checks are used for repairs, research, construction, and any task that takes multiple steps.",
                "Extended checks simulate long-term efforts. Track cumulative successes and failures."
        ));

        entries.add(createEntry(
                "Group Checks",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                188,
                "In a group check, multiple characters contribute to a single task. Each character rolls their own task attempt. The total successes are combined. If the combined successes meet the difficulty, the group succeeds. Individual failures may still generate complications for those characters. Group checks represent teamwork and coordination.",
                "Crew members can assist with group checks, providing boons or additional successes."
        ));

        entries.add(createEntry(
                "Automatic Successes and Failures",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                189,
                "Some tasks are so easy or so difficult that no roll is needed. If a character's Target Number is significantly higher than the difficulty, the task succeeds automatically. If the task is far beyond the character's capabilities, it fails automatically. The GM (or the player in solo play) decides when a roll is unnecessary.",
                "Use automatic results to pace the game. Not every action needs a dice roll."
        ));

        // --- Combat (expanded) ---

        entries.add(createEntry(
                "Physical Combat: Weapons and Equipment",
                "05.00 Rules of Play",
                "COMBAT",
                201,
                "Characters may use various weapons and equipment in combat. Phasers are the standard energy weapon, with settings from 1 (stun) to 10 (lethal). Handheld weapons deal 1 hit per success. Heavy weapons may deal 2 hits per success but require a task attempt to use effectively. Armor and shields can reduce incoming damage. Equipment is tracked in the character's inventory.",
                "Equipment choices affect combat effectiveness. Choose gear that matches your character's role."
        ));

        entries.add(createEntry(
                "Physical Combat: Retreat and Evasion",
                "05.00 Rules of Play",
                "COMBAT",
                202,
                "A character can attempt to retreat from combat by performing a task attempt using Fitness and Operations (or another relevant combination). Success means the character escapes; failure means the character takes damage while trying to flee. Evasive maneuvers can be attempted during combat to reduce incoming damage, but they require spending Momentum or accepting complications.",
                "Retreat is a valid tactical option. Not every fight needs to be won."
        ));

        entries.add(createEntry(
                "Starship Combat: Range Bands",
                "05.00 Rules of Play",
                "COMBAT",
                207,
                "Starship combat uses range bands to determine effectiveness. Close range allows torpedoes and point-defense weapons. Medium range is optimal for phaser banks. Long range limits weapons to long-range phasers and torpedoes. Extreme range allows only sensor scanning and communication. Maneuvering changes range bands. Each side's position affects weapon effectiveness.",
                "Range management is key to starship combat. Use maneuvers to control engagement distance."
        ));

        entries.add(createEntry(
                "Starship Combat: Actions",
                "05.00 Rules of Play",
                "COMBAT",
                208,
                "During starship combat, each round a ship can perform one action: ATTACK (fire weapons), TORPEDO (launch torpedoes), MANEUVER (change range band), REPAIR (reduce system stress), EVASIVE (reduce incoming damage), or SHIELD_RECHARGE (restore shield capacity). The action chosen affects the task attempt's target number and difficulty. Choosing the right action is a tactical decision.",
                "Starship combat actions provide tactical depth. Plan your actions carefully."
        ));

        entries.add(createEntry(
                "Starship Combat: Retreat",
                "05.00 Rules of Play",
                "COMBAT",
                209,
                "A starship can attempt to retreat from combat by performing a task attempt using Engines and Operations. Success means the ship escapes; failure means the ship takes damage while trying to flee. Warp drive can be used for an emergency retreat, but it requires a successful task attempt and may generate complications.",
                "Retreat is a valid tactical option in starship combat. Preserve the ship and crew."
        ));

        // --- Mission Structure (expanded) ---

        entries.add(createEntry(
                "Mission Generation",
                "06.00 Playing Captain's Log",
                "MISSION_STRUCTURE",
                227,
                "Missions can be generated using the oracle tables or created manually. To generate a mission, roll on the Mission Type table, then use the Complication table to add a twist. The mission's theme, location, and key NPCs can also be generated using the appropriate tables. The three-act structure provides the framework for the generated content.",
                "Use the Probability Matrix service to generate mission elements randomly."
        ));

        entries.add(createEntry(
                "Mission Debriefing",
                "06.00 Playing Captain's Log",
                "MISSION_STRUCTURE",
                229,
                "After each mission, the character writes a debriefing log entry. This is a chance to reflect on the mission's events, assess what went well and what went wrong, and consider how the experience has changed the character. Debriefing may trigger career events, value changes, and character growth. It is an essential part of the Captain's Log experience.",
                "Debriefing adds depth to the narrative. Use it to develop your character's arc."
        ));

        entries.add(createEntry(
                "Career Progression",
                "06.00 Playing Captain's Log",
                "CAREER",
                231,
                "Characters advance through their career by completing missions and reflecting on their experiences. Every few missions, a career event occurs that may result in a promotion, new assignment, training, or personal development. Career progression includes rank advancement, discipline increases, new focuses, and talent acquisition. The character's values may also evolve over time.",
                "Use the Career Progression service to track and manage character advancement."
        ));

        entries.add(createEntry(
                "Maintenance and Downtime",
                "06.00 Playing Captain's Log",
                "MAINTENANCE",
                233,
                "Between missions, the character and ship undergo maintenance and downtime activities. The Maintenance Event table determines what happens during downtime. Activities include system repairs, crew training, personal time, and scientific research. Downtime is essential for restoring ship systems, healing injuries, and developing the character.",
                "Plan downtime activities strategically to prepare for future missions."
        ));

        // --- Equipment ---

        entries.add(createEntry(
                "Equipment Management",
                "06.00 Playing Captain's Log",
                "EQUIPMENT",
                235,
                "Characters carry equipment that provides advantages in specific situations. Equipment includes weapons, tools, medical supplies, communication devices, and personal items. Each piece of equipment may provide a boon on relevant task attempts or enable actions that would otherwise be impossible. Equipment can be lost, damaged, or upgraded during missions.",
                "Use the Equipment service to manage the character's inventory."
        ));

        // --- Crew Management ---

        entries.add(createEntry(
                "Crew Management",
                "06.00 Playing Captain's Log",
                "CREW",
                237,
                "The character's crew members are essential to mission success. Each crew member has a role, key skills, and a relationship with the character. Crew members can assist with task attempts, providing boons or additional successes. They can also be affected by mission outcomes, including injuries and casualties. Managing the crew involves balancing their needs with mission requirements.",
                "Use the Crew service to manage crew members and their contributions."
        ));

        // --- Campaign Tracking ---

        entries.add(createEntry(
                "Campaign Tracking",
                "06.00 Playing Captain's Log",
                "CAMPAIGN",
                239,
                "A campaign is a series of connected missions that form an ongoing narrative arc. Campaigns track recurring characters, factions, and storylines. Missions within a campaign build on each other, with consequences carrying over from one mission to the next. Campaigns provide long-term goals and a sense of continuity. The campaign debrief system helps track progress and unresolved threads.",
                "Use the Campaign service to manage ongoing story arcs and connected missions."
        ));

        // --- Injury and Recovery ---

        entries.add(createEntry(
                "Injury and Recovery System",
                "06.00 Playing Captain's Log",
                "INJURY",
                241,
                "Characters can be injured during missions. Health states range from HEALTHY to STUNNED to INJURED to DOWN. Taking damage moves a character to a worse state. Recovery moves a character back toward health. Medical treatment accelerates recovery. Severe injuries may require extended downtime. The injury system adds stakes to combat and dangerous situations.",
                "Use the Injury service to track character health and recovery."
        ));

        // --- Difficulty Scaling ---

        entries.add(createEntry(
                "Difficulty Scaling Guidelines",
                "05.00 Rules of Play",
                "DICE_MECHANICS",
                192,
                "Difficulty should scale with the narrative context. Routine tasks use Difficulty 1. Challenging tasks use Difficulty 2. Extremely difficult tasks use Difficulty 3. The urgency, stakes, and consequences of the task should influence the difficulty. A task that seems routine may become difficult under time pressure or in adverse conditions. Adjust difficulty dynamically based on the situation.",
                "Difficulty scaling keeps the game balanced and engaging. Don't be afraid to adjust on the fly."
        ));

        // --- World Generation ---

        entries.add(createEntry(
                "World Generation",
                "Appendix III",
                "WORLD_GENERATION",
                293,
                "Generating a new world involves rolling on multiple tables: Terrain Type, Atmosphere Type, and Population Level. The combination of these rolls creates a unique world with specific characteristics. The terrain determines the landscape, the atmosphere determines habitability, and the population level determines the civilization's technological advancement. Additional rolls can determine the world's political situation, key features, and potential conflicts.",
                "Use the world generation tables to create unique planets for exploration missions."
        ));

        // --- Star Trek Eras ---

        entries.add(createEntry(
                "Star Trek Eras and Settings",
                "02.00 The Star Trek Universe",
                "SETTING",
                31,
                "Captain's Log can be set in different eras of the Star Trek timeline. The TOS era (2260s) features early exploration and the Constitution-class starships. The TNG era (2360s) features the Federation at its height with Galaxy-class starships. The DS9 era (2370s) features the Dominion War and political intrigue. The VOY era (2370s) features deep space exploration beyond Federation territory. The era affects available technology, political situations, and adversaries.",
                "Choose an era that matches your preferred tone and setting."
        ));

        entryRepository.saveAll(entries);
        log.info("Populated {} rule entries", entries.size());
    }

    // ==================== Oracle Tables Population ====================

    @Transactional
    public void populateOracleTables() {
        List<RulesTable> tables = new ArrayList<>();

        // Yes/No Probability Matrix — the core oracle tool
        // This table encodes the probability levels and their d20 thresholds
        // Very Unlikely: Yes on 1-2, No on 3-20
        // Unlikely: Yes on 1-5, No on 6-20
        // 50/50: Yes on 1-10, No on 11-20
        // Likely: Yes on 1-15, No on 16-20
        // Very Likely: Yes on 1-18, No on 19-20
        // Certain: Yes on 1-20 (but may have complications)
        String[] yesNoResults = {
                "CRITICAL_YES", "YES_PLUS", "YES", "YES_MINUS", "BOTH_NEITHER",
                "NO_MINUS", "NO", "NO_PLUS", "CRITICAL_NO", "NO_MINUS",
                "NO", "NO_PLUS", "NO", "YES_MINUS", "BOTH_NEITHER",
                "YES_PLUS", "YES", "YES_MINUS", "NO_MINUS", "NO"
        };
        String[] yesNoDescs = {
                "Critical Yes — an overwhelming, dramatic success with major consequences.",
                "Yes+ — Yes, and you gain Momentum.",
                "Yes — a straightforward affirmative answer.",
                "Yes- — Yes, but with a complication or cost.",
                "Both/Neither — both outcomes occur, or neither occurs.",
                "No- — No, and you gain Threat.",
                "No — a straightforward negative answer.",
                "No+ — No, but something helpful happens.",
                "Critical No — an overwhelming, dramatic failure with major consequences.",
                "No- — No, and the situation worsens.",
                "No — the answer is clearly negative.",
                "No+ — No, but a silver lining appears.",
                "No — the answer is negative.",
                "Yes- — Yes, but at a price.",
                "Both/Neither — an unexpected combination of outcomes.",
                "Yes+ — Yes, and things go better than expected.",
                "Yes — a clear affirmative answer.",
                "Yes- — Yes, but complications follow.",
                "No- — No, and the consequences are severe.",
                "No — the answer is negative."
        };
        for (int i = 0; i < yesNoResults.length; i++) {
            tables.add(createTableEntry("Yes/No Probability (50/50)", "ORACLE", "Appendix I", 253,
                    i + 1, yesNoResults[i], yesNoDescs[i]));
        }

        // Very Unlikely probability curve
        String[] veryUnlikelyResults = {
                "YES_PLUS", "YES", "NO_MINUS", "NO", "NO",
                "NO", "NO", "NO_PLUS", "NO", "NO",
                "NO", "NO", "NO", "NO", "NO",
                "NO", "NO", "NO", "NO", "CRITICAL_NO"
        };
        String[] veryUnlikelyDescs = {
                "Yes+ — almost impossibly, things go your way.",
                "Yes — against all odds, the answer is yes.",
                "No- — no, and the situation is worse than expected.",
                "No — the expected negative answer.",
                "No — clearly not.",
                "No — the answer is no.",
                "No — no.",
                "No+ — no, but a small mercy.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "Critical No — an overwhelming and dramatic no."
        };
        for (int i = 0; i < veryUnlikelyResults.length; i++) {
            tables.add(createTableEntry("Yes/No Probability (Very Unlikely)", "ORACLE", "Appendix I", 253,
                    i + 1, veryUnlikelyResults[i], veryUnlikelyDescs[i]));
        }

        // Unlikely probability curve
        String[] unlikelyResults = {
                "YES_PLUS", "YES", "YES_MINUS", "NO_MINUS", "NO",
                "NO", "NO", "NO_PLUS", "NO", "NO",
                "NO", "NO", "NO", "NO", "NO",
                "NO", "NO", "NO", "NO_PLUS", "CRITICAL_NO"
        };
        String[] unlikelyDescs = {
                "Yes+ — surprisingly, things go your way.",
                "Yes — unlikely but possible.",
                "Yes- — yes, but barely.",
                "No- — no, and it's worse than expected.",
                "No — the expected negative answer.",
                "No — no.",
                "No — no.",
                "No+ — no, but a small mercy.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No — no.",
                "No+ — no, but a silver lining.",
                "Critical No — a dramatic and overwhelming no."
        };
        for (int i = 0; i < unlikelyResults.length; i++) {
            tables.add(createTableEntry("Yes/No Probability (Unlikely)", "ORACLE", "Appendix I", 253,
                    i + 1, unlikelyResults[i], unlikelyDescs[i]));
        }

        // Likely probability curve
        String[] likelyResults = {
                "CRITICAL_YES", "YES_PLUS", "YES", "YES_MINUS", "YES",
                "YES_MINUS", "BOTH_NEITHER", "NO_MINUS", "NO", "NO",
                "NO_PLUS", "NO", "YES_MINUS", "YES", "YES_PLUS",
                "YES", "YES_MINUS", "BOTH_NEITHER", "NO_MINUS", "NO_PLUS"
        };
        String[] likelyDescs = {
                "Critical Yes — an overwhelming and dramatic yes.",
                "Yes+ — yes, and things go better than expected.",
                "Yes — a clear affirmative answer.",
                "Yes- — yes, but with complications.",
                "Yes — yes.",
                "Yes- — yes, but barely.",
                "Both/Neither — an unexpected combination.",
                "No- — no, and the situation worsens.",
                "No — no.",
                "No — no.",
                "No+ — no, but a silver lining.",
                "No — no.",
                "Yes- — yes, but at a cost.",
                "Yes — yes.",
                "Yes+ — yes, with a bonus.",
                "Yes — yes.",
                "Yes- — yes, but complications follow.",
                "Both/Neither — both or neither.",
                "No- — no, and it's worse.",
                "No+ — no, but something helpful."
        };
        for (int i = 0; i < likelyResults.length; i++) {
            tables.add(createTableEntry("Yes/No Probability (Likely)", "ORACLE", "Appendix I", 253,
                    i + 1, likelyResults[i], likelyDescs[i]));
        }

        // Very Likely probability curve
        String[] veryLikelyResults = {
                "CRITICAL_YES", "YES_PLUS", "YES", "YES_MINUS", "YES",
                "YES", "YES_MINUS", "BOTH_NEITHER", "YES", "YES",
                "YES_MINUS", "YES", "YES", "YES", "YES",
                "YES", "YES_PLUS", "YES", "YES_MINUS", "NO_MINUS"
        };
        String[] veryLikelyDescs = {
                "Critical Yes — an overwhelming and dramatic yes.",
                "Yes+ — yes, and things go better than expected.",
                "Yes — a clear affirmative answer.",
                "Yes- — yes, but with complications.",
                "Yes — yes.",
                "Yes — yes.",
                "Yes- — yes, but barely.",
                "Both/Neither — an unexpected combination.",
                "Yes — yes.",
                "Yes — yes.",
                "Yes- — yes, but at a cost.",
                "Yes — yes.",
                "Yes — yes.",
                "Yes — yes.",
                "Yes — yes.",
                "Yes — yes.",
                "Yes+ — yes, with a bonus.",
                "Yes — yes.",
                "Yes- — yes, but complications follow.",
                "No- — almost impossibly, no."
        };
        for (int i = 0; i < veryLikelyResults.length; i++) {
            tables.add(createTableEntry("Yes/No Probability (Very Likely)", "ORACLE", "Appendix I", 253,
                    i + 1, veryLikelyResults[i], veryLikelyDescs[i]));
        }

        // Mission Type Table
        String[] missionTypes = {
                "DIPLOMACY", "EXPLORATION", "COMBAT", "SCIENCE", "RESCUE",
                "INVESTIGATION", "TRADE", "SURVEILLANCE", "CRISIS", "FIRST_CONTACT",
                "MAINTENANCE", "PATROL", "TRANSPORT", "MEDICAL", "SEARCH",
                "ESCORT", "RECONNAISSANCE", "NEGOTIATION", "SURVIVAL", "CONSPIRACY"
        };
        String[] missionTypeDescs = {
                "Negotiations, treaties, and political maneuvering.",
                "Charting unknown space and discovering new worlds.",
                "Engaging hostile forces in battle.",
                "Research, analysis, and scientific discovery.",
                "Saving those in distress.",
                "Uncovering secrets and solving mysteries.",
                "Commerce, resource acquisition, and economic dealings.",
                "Espionage, reconnaissance, and covert operations.",
                "Responding to emergencies and disasters.",
                "Encountering unknown species or civilizations.",
                "Ship repairs, upgrades, and downtime activities.",
                "Monitoring borders and protecting territory.",
                "Delivering personnel, cargo, or messages.",
                "Treating the sick and injured, epidemiological research.",
                "Finding lost ships, colonies, or artifacts.",
                "Protecting another vessel or convoy.",
                "Gathering intelligence on a target.",
                "Brokerage between conflicting parties.",
                "Enduring hostile environments or situations.",
                "Uncovering plots and hidden agendas."
        };
        for (int i = 0; i < missionTypes.length; i++) {
            tables.add(createTableEntry("Mission Type", "MISSION", "Appendix I", 253,
                    i + 1, missionTypes[i], missionTypeDescs[i]));
        }

        // Complication Table
        String[] complications = {
                "REINFORCEMENTS", "SYSTEM_FAILURE", "BETRAYAL", "TIME_PRESSURE", "CASUALTIES",
                "ENVIRONMENTAL_HAZARD", "COMMUNICATIONS_BLACKOUT", "UNEXPECTED_ALLY", "MORAL_DILEMMA", "EQUIPMENT_LOSS",
                "AMBUSH", "POLITICAL_INTERFERENCE", "TREACHEROUS_TERRAIN", "DOUBLE_AGENT", "POWER_FAILURE",
                "HOSTAGE_SITUATION", "WEAPON_OF_MASS_DESTRUCTION", "MUTINY", "TEMPORAL_ANOMALY", "COSMIC_HORROR"
        };
        String[] complicationDescs = {
                "The enemy gets backup.",
                "A critical system goes offline.",
                "An ally reveals hidden motives.",
                "The clock starts ticking faster.",
                "Someone is hurt or lost.",
                "The environment turns hostile.",
                "Cut off from support.",
                "A stranger offers help, but at what cost?",
                "A choice with no good options.",
                "Vital gear is destroyed or stolen.",
                "Attacked from an unexpected direction.",
                "Orders from above complicate things.",
                "The environment works against you.",
                "Someone is playing both sides.",
                "Lights out, systems down.",
                "Innocents are held at gunpoint.",
                "A doomsday device is activated.",
                "Crew members turn against the captain.",
                "Time behaves strangely.",
                "Something beyond comprehension appears."
        };
        for (int i = 0; i < complications.length; i++) {
            tables.add(createTableEntry("Complication", "COMBAT", "Appendix I", 260,
                    i + 1, complications[i], complicationDescs[i]));
        }

        // Advantage Table
        String[] advantages = {
                "INTEL", "REINFORCEMENTS", "EQUIPMENT_BOOST", "ALLY_ASSISTANCE", "ENVIRONMENTAL_EDGE",
                "SURPRISE_ATTACK", "MORALE_BOOST", "TECHNOLOGY_EDGE", "INSIDER_INFORMATION", "SAFE_HAVEN",
                "WEAPON_UPGRADE", "DIPLOMATIC_OPENING", "MEDICAL_SUPPLY", "TRANSPORT_ASSISTANCE", "SENSOR_ADVANTAGE",
                "SHIELD_BOOST", "CREW_EXPERTISE", "LUCKY_BREAK", "ELEMENT_OF_SURPRISE", "COMMAND_PRESENCE"
        };
        String[] advantageDescs = {
                "You learn something the enemy doesn't expect you to know.",
                "Friendly forces arrive to help.",
                "A piece of gear performs better than expected.",
                "An ally steps in at the crucial moment.",
                "The environment works in your favor.",
                "You catch the enemy off guard.",
                "The crew's spirits are lifted.",
                "Your tech outclasses the opposition.",
                "A mole provides critical details.",
                "A place to rest and regroup.",
                "Armaments are more effective than planned.",
                "A path to negotiation appears.",
                "Healing resources are found.",
                "A ride or shuttle becomes available.",
                "You see what others miss.",
                "Defenses hold stronger than expected.",
                "A crew member's skill shines.",
                "Fortune smiles upon you.",
                "The enemy isn't ready.",
                "Your leadership inspires excellence."
        };
        for (int i = 0; i < advantages.length; i++) {
            tables.add(createTableEntry("Advantage", "MISSION", "Appendix I", 265,
                    i + 1, advantages[i], advantageDescs[i]));
        }

        // Encounter Table
        String[] encounters = {
                "HOSTILE_SHIP", "DISTRESS_SIGNAL", "ANOMALOUS_READING", "NEUTRAL_PARTY", "CIVILIAN_VESSEL",
                "DERELICT", "ASTEROID_FIELD", "PLANETARY_DEFENSE", "PIRATE_ATTACK", "DIPLOMATIC_MEETING",
                "SCIENTIFIC_PHENOMENON", "BLACK_MARKET", "SEARCH_AND_RESCUE", "TERRITORIAL_DISPUTE", "MYSTERIOUS_SIGNAL",
                "WEATHER_STORM", "GHOST_SHIP", "REFUGEE_CRISIS", "MILITARY_BLOCKADE", "ARTIFACT_DISCOVERY"
        };
        String[] encounterDescs = {
                "An enemy vessel hails or attacks.",
                "Someone is calling for help.",
                "Sensors detect something unusual.",
                "A third faction enters the scene.",
                "Non-combatants are in the area.",
                "An abandoned ship or station drifts nearby.",
                "Navigational hazard ahead.",
                "Automated defenses activate.",
                "Raiders strike without warning.",
                "A formal encounter with another faction.",
                "A natural event demands study.",
                "An opportunity for illicit trade.",
                "Survivors need extraction.",
                "Two factions clash over space.",
                "An encoded transmission is detected.",
                "A plasma storm or radiation belt.",
                "A vessel with no life signs approaches.",
                "Displaced civilians need aid.",
                "Passage is denied.",
                "An ancient object is found."
        };
        for (int i = 0; i < encounters.length; i++) {
            tables.add(createTableEntry("Encounter", "MISSION", "Appendix I", 270,
                    i + 1, encounters[i], encounterDescs[i]));
        }

        // NPC Role Table
        String[] npcRoles = {
                "COMMANDING_OFFICER", "FIRST_OFFICER", "SCIENCE_OFFICER", "SECURITY_OFFICER", "ENGINEER",
                "MEDICAL_OFFICER", "PILOT", "TACTICAL_OFFICER", "COMMUNICATIONS_OFFICER", "NAVIGATOR",
                "DIPLOMAT", "SCOUT", "MERCHANT", "SMUGGLER", "SPY",
                "MERCENARY", "ACADEMIC", "RELIGIOUS_LEADER", "CRIMINAL", "CIVILIAN"
        };
        String[] npcRoleDescs = {
                "A leader making tough decisions.",
                "The right hand, executing orders.",
                "Analyzing data and making discoveries.",
                "Enforcing order and handling threats.",
                "Keeping the ship running and fixing problems.",
                "Healing the wounded and treating disease.",
                "Flying the ship through danger.",
                "Directing weapons and shields.",
                "Managing all transmissions.",
                "Plotting courses through space.",
                "Negotiating with other factions.",
                "Reconnaissance and first contact.",
                "Trading goods and services.",
                "Operating outside the law.",
                "Gathering intelligence covertly.",
                "Fighting for profit.",
                "Researching and teaching.",
                "Guiding the faithful.",
                "Operating in the shadows.",
                "An ordinary person caught in extraordinary events."
        };
        for (int i = 0; i < npcRoles.length; i++) {
            tables.add(createTableEntry("NPC Role", "NPC", "Appendix II", 281,
                    i + 1, npcRoles[i], npcRoleDescs[i]));
        }

        // Alien Species Table
        String[] species = {
                "HUMAN", "ANDORIAN", "BAJORAN", "BETAZOID", "BOLIAN",
                "CARDASSIAN", "DENOBULAN", "FERENGI", "HYLEAN", "KLINGON",
                "ROMULAN", "VULCAN", "TRILL", "ORION", "THOLIAN",
                "ROMULAN_ELITE", "FOUNDLING", "XINDI", "DELTA_RACE", "UNKNOWN"
        };
        String[] speciesDescs = {
                "Versatile and adaptable.",
                "Honor-bound warriors from a cold world.",
                "Spiritual and resilient.",
                "Empathic communicators.",
                "Artistic and philosophical.",
                "Ambitious and militaristic.",
                "Master healers and scientists.",
                "Profit-driven traders.",
                "Proud and traditional.",
                "Fierce warriors who value honor.",
                "Cunning and secretive.",
                "Logical and disciplined.",
                "Symbiotic beings with combined wisdom.",
                "Raiders and traders.",
                "Isolationist and technologically advanced.",
                "Nobility of the Romulan Star Empire.",
                "A mysterious and ancient species.",
                "A diverse species with multiple primate forms.",
                "Advanced beings from the Delta Quadrant.",
                "An unclassified species."
        };
        for (int i = 0; i < species.length; i++) {
            tables.add(createTableEntry("Alien Species", "NPC", "Appendix II", 285,
                    i + 1, species[i], speciesDescs[i]));
        }

        // Location Type Table
        String[] locationTypes = {
                "STARBASE", "COLONY", "SPACE_STATION", "PLANET_SURFACE", "DEEP_SPACE",
                "WORMHOLE", "NEBULA", "ASTEROID_BELT", "STARSHIP", "MOON",
                "OUTPOST", "MARKET", "DIPLOMATIC_ENCLAVE", "RESEARCH_FACILITY", "MILITARY_FORTRESS",
                "PRISON", "HOSPITAL", "TEMPLE", "BLACK_SITE", "ANOMALOUS_ZONE"
        };
        String[] locationTypeDescs = {
                "A large military or civilian space station.",
                "A settled world with infrastructure.",
                "A smaller orbital facility.",
                "On the ground of a habitable world.",
                "Far from any star or colony.",
                "A spatial anomaly connecting distant points.",
                "A cloud of gas and dust.",
                "A field of rocky bodies.",
                "Aboard a vessel in transit.",
                "A natural satellite with facilities.",
                "A remote forward base.",
                "A trading hub or bazaar.",
                "Neutral ground for negotiations.",
                "A laboratory or observatory.",
                "A heavily defended installation.",
                "A detention facility.",
                "A medical center.",
                "A place of worship or meditation.",
                "A covert, unacknowledged facility.",
                "An area where physics behaves strangely."
        };
        for (int i = 0; i < locationTypes.length; i++) {
            tables.add(createTableEntry("Location Type", "LOCATION", "Appendix III", 293,
                    i + 1, locationTypes[i], locationTypeDescs[i]));
        }

        // Career Event Table
        String[] careerEvents = {
                "PROMOTION", "NEW_ASSIGNMENT", "TRAINING", "LEAVE", "PERSONAL_CRISIS",
                "NEW_ALLY", "NEW_ENEMY", "EQUIPMENT_UPGRADE", "SHIP_TRANSFER", "MEDICAL_TREATMENT",
                "POLITICAL_INTRIGUE", "SCIENTIFIC_BREAKTHROUGH", "COURT_MARTIAL", "AWARDS_CEREMONY", "MENTORSHIP",
                "INVESTIGATION", "BLACK_OPS", "RETIREMENT_CONSIDERATION", "FAMILY_MATTER", "RETIREMENT"
        };
        String[] careerEventDescs = {
                "Your rank and responsibilities increase.",
                "Transferred to a different post.",
                "Attended a course that improves your skills.",
                "Time off to rest and recharge.",
                "A personal matter demands attention.",
                "You gain a valuable contact or friend.",
                "Someone holds a grudge against you.",
                "Your gear is improved.",
                "Reassigned to a different vessel.",
                "Health issues are addressed.",
                "Office politics affect your career.",
                "Your research yields results.",
                "You face disciplinary action.",
                "Your achievements are recognized.",
                "You guide a junior officer.",
                "You're questioned about a past mission.",
                "A covert assignment is offered.",
                "Is it time to step down?",
                "Relatives need your help.",
                "A chapter closes, a new one begins."
        };
        for (int i = 0; i < careerEvents.length; i++) {
            tables.add(createTableEntry("Career Event", "CAREER", "Appendix I", 275,
                    i + 1, careerEvents[i], careerEventDescs[i]));
        }

        // Maintenance Event Table (Appendix IV)
        String[] maintenanceEvents = {
                "SYSTEM_REPAIR", "WEAPON_CALIBRATION", "SHIELD_UPGRADE", "ENGINE_OVERHAUL", "SENSOR_ENHANCEMENT",
                "COMPUTER_UPDATE", "CREW_TRAINING", "MEDICAL_SUPPLY_RESTOCK", "HULL_PATCHING", "COMMS_REPAIR",
                "LIFE_SUPPORT_CHECK", "CARGO_ORGANIZATION", "CREW_LEAVE", "DIPLOMATIC_VISIT", "SCIENTIFIC_RESEARCH",
                "PERSONAL_TIME", "INTEL_ANALYSIS", "CREW_EVALUATION", "EMERGENCY_DRILL", "SHIP_CUSTOMIZATION"
        };
        String[] maintenanceDescs = {
                "Fixing damage to ship systems.",
                "Tuning weapons for optimal performance.",
                "Improving defensive capabilities.",
                "Boosting warp and impulse performance.",
                "Extending detection range.",
                "Installing new software and patches.",
                "Improving crew skills and coordination.",
                "Replenishing healing resources.",
                "Repairing structural damage.",
                "Restoring communication systems.",
                "Ensuring environmental systems work.",
                "Sorting and securing supplies.",
                "Allowing crew members time off.",
                "Hosting or attending a diplomatic event.",
                "Conducting experiments and studies.",
                "Reflecting on values and goals.",
                "Reviewing intelligence reports.",
                "Assessing crew performance.",
                "Practicing for worst-case scenarios.",
                "Personalizing the vessel."
        };
        for (int i = 0; i < maintenanceEvents.length; i++) {
            tables.add(createTableEntry("Maintenance Event", "MAINTENANCE", "Appendix IV", 311,
                    i + 1, maintenanceEvents[i], maintenanceDescs[i]));
        }

        // Alien Polity Table (Appendix IV)
        String[] polities = {
                "UNITED_FEDERATION", "KLINGON_EMPIRE", "ROMULAN_STAR_EMPIRE", "FERENGI_ALLIANCE", "CARDASSIAN_UNION",
                "BAJORAN_RESISTANCE", "ORION_SYNDICATE", "THOLIAN_COLLECTIVE", "ANDORIAN_EMPIRE", "BETAZOI_COLLECTIVE",
                "DENOBULAN_FEDERATION", "TRILL_UNITY", "ROMULAN_SENSITATIVE_OFFICE", "FERENGI_COMMERCE_AUTHORITY", "KLINGON_HIGH_COUNCIL",
                "FEDERATION_COUNCIL", "BAJORAN_RELIGION_AUTHORITY", "CARDASSIA_CENTRAL", "MAUDOR", "UNKNOWN_FACTION"
        };
        String[] polityDescs = {
                "Democratic alliance of worlds.",
                "Warrior culture valoring honor and strength.",
                "Secretive and cunning rivals.",
                "Profit-driven merchant society.",
                "Authoritarian militaristic state.",
                "Freedom fighters seeking independence.",
                "Interstellar criminal organization.",
                "Isolationist defensive alliance.",
                "Honor-bound warrior culture.",
                "Empathic telepathic society.",
                "Healing-focused civilization.",
                "Symbiotic beings with combined wisdom.",
                "Secret intelligence agency.",
                "Corporate overlords.",
                "Governing body of the Empire.",
                "Legislative body of the Federation.",
                "Spiritual leaders.",
                "Ruling council of Cardassia.",
                "Ancient and mysterious beings.",
                "An unclassified political entity."
        };
        for (int i = 0; i < polities.length; i++) {
            tables.add(createTableEntry("Alien Polity", "POLITY", "Appendix IV", 315,
                    i + 1, polities[i], polityDescs[i]));
        }

        // ==================== Appendix II: Allies and Adversaries ====================

        // Adversary Table (Appendix II)
        String[] adversaries = {
                "KLINGON_WARRIOR", "ROMULAN_OPERATIVE", "FERENGI_RAIDER", "CARDASSIAN_OFFICER",
                "ORION_SLAVE_TRADER", "BORG_DRONE", "ROMULAN_SNIPER", "KLINGON_BOARDING_PARTY",
                "FERENGI_MERCENARY", "CARDASSIAN_JAGHRAAF", "THOLIAN_GUARDIAN", "BAJORAN_INSURGENT",
                "ORION_WARRIOR", "ROMULAN_TACTICIAN", "KLINGON_CHAMPION", "UNKNOWN_ENTITIES",
                "CYBORG_ASSASSIN", "PIRATE_CAPTAIN", "CORRUPT_OFFICIAL", "RIVAL_CAPTAIN"
        };
        String[] adversaryDescs = {
                "Honorable but deadly opponent.",
                "Cunning agent of the Tal Shiar.",
                "Profit-driven pirate.",
                "Ruthless enforcer of the Union.",
                "Criminal dealing in lives.",
                "Relentless assimilator.",
                "Patient and precise assassin.",
                "Aggressive assault team.",
                "Will fight for the right price.",
                "Elite special forces operative.",
                "Defensive but dangerous.",
                "Desperate freedom fighter.",
                "Brutal and strong combatant.",
                "Master of deception and strategy.",
                "The best warrior the Empire has to offer.",
                "Mysterious beings with unknown motives.",
                "Enhanced killer with no mercy.",
                "Leader of a rogue crew.",
                "Abuses power for personal gain.",
                "Competing officer with conflicting goals."
        };
        for (int i = 0; i < adversaries.length; i++) {
            tables.add(createTableEntry("Adversary", "ADVERSARY", "Appendix II", 281,
                    i + 1, adversaries[i], adversaryDescs[i]));
        }

        // Ally Table (Appendix II)
        String[] allies = {
                "FEDERATION_LIAISON", "INFORMANT", "LOCAL_GUIDE", "ALLIED_CAPTAIN",
                "SCIENTIFIC_CONTACT", "DIPLOMATIC_CONTACT", "RESISTANCE_LEADER", "MEDICAL_EXPERT",
                "TECH_SPECIALIST", "NAVIGATOR", "TRANSLATOR", "PILOT",
                "SECURITY_CONSULTANT", "SUPPLIER", "LEGAL_ADVISOR", "HISTORIAN",
                "PSYCHIC_ALLY", "ENGINEER_CONTACT", "DIPLOMAT", "MENTOR"
        };
        String[] allyDescs = {
                "Official contact within Starfleet.",
                "Source of intelligence from within enemy ranks.",
                "Knows the area and its secrets.",
                "Friendly officer who can provide assistance.",
                "Expert who can analyze anomalies.",
                "Can open doors through protocol.",
                "Commands local freedom fighters.",
                "Can heal injuries and treat diseases.",
                "Can repair or hack systems.",
                "Knows the routes through dangerous space.",
                "Can communicate with alien species.",
                "Expert ship or shuttle operator.",
                "Can assess and improve defenses.",
                "Provides equipment and resources.",
                "Knows the regulations and loopholes.",
                "Knows the ancient secrets and prophecies.",
                "Can sense danger and read minds.",
                "Can modify or build equipment.",
                "Can negotiate peace or alliances.",
                "Wise advisor who guides your character."
        };
        for (int i = 0; i < allies.length; i++) {
            tables.add(createTableEntry("Ally", "ALLY", "Appendix II", 285,
                    i + 1, allies[i], allyDescs[i]));
        }

        // ==================== Appendix III: Locations and Starships ====================

        // Starship Class Table (Appendix III)
        String[] starshipClasses = {
                "CONSTITUTION", "GALAXY", "SOVEREIGN", "VOYAGER", "DEFIANT",
                "AKIRA", "NEBULA", "PROMETHEUS", "D7", "DREADNOUGHT",
                "WARSHIP", "CRUISER", "RAIDER", "MERCHANT", "ORBITAL",
                "SHUTTLE", "RUNABOUT", "EXPLORER", "CUSTOM", "UNKNOWN"
        };
        String[] starshipDescs = {
                "Versatile exploration vessel.",
                "Long-range exploration flagship.",
                "Diplomatic and combat vessel.",
                "Deep space exploration.",
                "Heavy combat vessel.",
                "Heavy exploration and combat.",
                "Science and exploration.",
                "Stealth and reconnaissance.",
                "Klingon battle cruiser.",
                "Klingon heavy warship.",
                "Romulan heavy combat vessel.",
                "Romulan long-range vessel.",
                "Ferengi fast attack ship.",
                "Ferengi cargo and trade vessel.",
                "Large space station.",
                "Small transport vessel.",
                "Small multi-role vessel.",
                "Deep space probe ship.",
                "Unique vessel with custom capabilities.",
                "Unidentified vessel type."
        };
        for (int i = 0; i < starshipClasses.length; i++) {
            tables.add(createTableEntry("Starship Class", "STARSHIP", "Appendix III", 293,
                    i + 1, starshipClasses[i], starshipDescs[i]));
        }

        // Terrain Type Table (Appendix III)
        String[] terrains = {
                "MOUNTAINS", "PLAINS", "FOREST", "JUNGLE", "DESERT",
                "TUNDRA", "SWAMP", "VOLCANIC", "OCEANIC", "URBAN",
                "CANYON", "CAVE", "GLACIER", "RAIN_FOREST", "STEPPE",
                "BADLANDS", "CRATER", "RIFT", "ARTIFICIAL", "UNKNOWN"
        };
        String[] terrainDescs = {
                "Rugged peaks and valleys.",
                "Flat, open grasslands.",
                "Dense woodland with diverse flora.",
                "Thick, humid tropical growth.",
                "Arid sands and rock formations.",
                "Frozen, barren landscape.",
                "Wet, marshy terrain with standing water.",
                "Active lava flows and ash fields.",
                "Water-covered world with islands.",
                "Heavily developed cityscape.",
                "Deep gorges and cliffs.",
                "Underground network of tunnels.",
                "Massive ice formations.",
                "Lush, biodiverse ecosystem.",
                "Semi-arid grassland.",
                "Eroded, rocky terrain.",
                "Impact site with unique geology.",
                "Tectonic fissure with unusual activity.",
                "Constructed or modified terrain.",
                "Terrain type not yet identified."
        };
        for (int i = 0; i < terrains.length; i++) {
            tables.add(createTableEntry("Terrain Type", "TERRAIN", "Appendix III", 295,
                    i + 1, terrains[i], terrainDescs[i]));
        }

        // Atmosphere Type Table (Appendix III)
        String[] atmospheres = {
                "BREATHABLE", "TOXIC", "THIN", "DENSE", "NONE",
                "WATER_VAPOR", "METHANE", "AMMONIA", "CARBON_DIOXIDE", "NITROGEN",
                "OXYGEN_RICH", "SULFURIC", "RADIATION", "PLASMA", "CRYOGENIC",
                "STORM", "MAGNETIC", "BIOLOGICAL", "ARTIFICIAL", "UNKNOWN"
        };
        String[] atmosphereDescs = {
                "Humans can breathe without assistance.",
                "Requires environmental suit.",
                "Low oxygen, requires breathing apparatus.",
                "High pressure, requires pressure suit.",
                "Vacuum or near-vacuum environment.",
                "Steam-filled atmosphere.",
                "Natural gas atmosphere.",
                "Toxic ammonia-based atmosphere.",
                "CO2-rich atmosphere.",
                "Nitrogen-dominant atmosphere.",
                "Highly flammable, oxygen-saturated.",
                "Acidic atmosphere with sulfur compounds.",
                "High radiation levels, dangerous to life.",
                "Superheated ionized gas.",
                "Extremely cold, frozen gases.",
                "Perpetual storms and lightning.",
                "Strong electromagnetic field in atmosphere.",
                "Living, organic atmosphere.",
                "Maintained by technology.",
                "Atmosphere composition not yet determined."
        };
        for (int i = 0; i < atmospheres.length; i++) {
            tables.add(createTableEntry("Atmosphere Type", "ATMOSPHERE", "Appendix III", 297,
                    i + 1, atmospheres[i], atmosphereDescs[i]));
        }

        // Population Level Table (Appendix III)
        String[] populations = {
                "UNINHABITED", "WILDLIFE_ONLY", "PRIMITIVE", "MEDIEVAL", "INDUSTRIAL",
                "ATOMIC", "INTERPLANETARY", "PRE_WARP", "WARP_CAPABLE", "FEDERATION_LEVEL",
                "POST_WARP", "TYPE_II", "TYPE_III", "ANCIENT", "DYING",
                "REBUILT", "HIDDEN", "NOMADIC", "SUBTERRANEAN", "UNKNOWN"
        };
        String[] populationDescs = {
                "No sentient life.",
                "Non-sentient animal life.",
                "Pre-industrial civilization.",
                "Early industrial technology.",
                "Steam and early electricity.",
                "Nuclear age technology.",
                "Colonizing nearby planets.",
                "Approaching faster-than-light travel.",
                "Has achieved FTL travel.",
                "Advanced warp civilization.",
                "Mature warp civilization.",
                "Harnesses stellar energy (Kardashev scale).",
                "Controls galactic energy.",
                "Vastly advanced, possibly precursors.",
                "Civilization in decline or collapse.",
                "Recovering from past destruction.",
                "Deliberately concealing their presence.",
                "Moving civilization without fixed homes.",
                "Lives underground or beneath surface.",
                "Population level not yet determined."
        };
        for (int i = 0; i < populations.length; i++) {
            tables.add(createTableEntry("Population Level", "POPULATION", "Appendix III", 299,
                    i + 1, populations[i], populationDescs[i]));
        }

        // ==================== Appendix I: Mission Theme ====================

        // Mission Theme Table
        String[] themes = {
                "FIRST_CONTACT", "WAR", "PIRACY", "SCIENTIFIC_DISCOVERY", "POLITICAL_INTRIGUE",
                "SURVIVAL", "RESCUE", "BETRAYAL", "SACRIFICE", "REDEMPTION",
                "EXPLORATION", "DIPLOMACY", "INVESTIGATION", "CONSPIRACY", "TIME_CRISIS",
                "ETHICAL_DILEMMA", "CULTURAL_CLASH", "TECHNOLOGICAL_THREAT", "PERSONAL_DEMONS", "COSMIC_MYSTERY"
        };
        String[] themeDescs = {
                "Encountering a species never before seen.",
                "Open conflict between factions.",
                "Raiders and thieves threaten the mission.",
                "A groundbreaking scientific finding.",
                "Secret agendas and hidden motives.",
                "Fighting to stay alive against the odds.",
                "Saving those in peril.",
                "An ally reveals hidden motives.",
                "Giving up something precious for the greater good.",
                "A fallen character seeks to make amends.",
                "Charting the unknown frontier.",
                "Negotiating peace between enemies.",
                "Uncovering a hidden truth.",
                "A web of deception threatens everything.",
                "A temporal anomaly threatens the timeline.",
                "A choice with no right answer.",
                "Two cultures collide with devastating results.",
                "A technology threatens to change everything.",
                "The character's past catches up with them.",
                "A mystery that defies explanation."
        };
        for (int i = 0; i < themes.length; i++) {
            tables.add(createTableEntry("Mission Theme", "MISSION", "Appendix I", 255,
                    i + 1, themes[i], themeDescs[i]));
        }

        // ==================== Appendix II: NPC Alignment ====================

        // NPC Alignment Table
        String[] npcAlignments = {
                "ALLY", "NEUTRAL", "ADVERSARY", "UNPREDICTABLE", "SECRET_ALLY",
                "SECRET_ENEMY", "MERCENARY", "INFORMANT", "MENTOR", "RIVAL",
                "LOVE_INTEREST", "MENTEE", "TRAITOR", "HERO", "VILLAIN",
                "ANTIHERO", "BYSTANDER", "CIVILIAN", "AUTHORITY", "OUTLAW"
        };
        String[] npcAlignmentDescs = {
                "A friend who will help the character.",
                "Neither friend nor foe; motivated by self-interest.",
                "An opponent who actively works against the character.",
                "Unpredictable; may help or hinder at any time.",
                "Appears neutral but secretly aids the character.",
                "Appears friendly but secretly works against the character.",
                "Will help for the right price.",
                "Provides information and intelligence.",
                "Guides and teaches the character.",
                "Competes with the character for the same goal.",
                "A romantic connection that complicates the mission.",
                "A junior character the character is responsible for.",
                "Betrays trust at a critical moment.",
                "A noble figure who inspires others.",
                "A malevolent force driving the conflict.",
                "A morally gray character with mixed motives.",
                "Caught in the middle of events.",
                "An innocent who needs protection.",
                "A figure of power who can help or hinder.",
                "Operating outside the law."
        };
        for (int i = 0; i < npcAlignments.length; i++) {
            tables.add(createTableEntry("NPC Alignment", "NPC", "Appendix II", 283,
                    i + 1, npcAlignments[i], npcAlignmentDescs[i]));
        }

        // ==================== Appendix III: Planet Type ====================

        // Planet Type Table
        String[] planetTypes = {
                "TERRESTRIAL", "GAS_GIANT", "ICE_WORLD", "LAVA_WORLD", "OCEAN_WORLD",
                "DESERT_WORLD", "JUNGLE_WORLD", "TUNDRA_WORLD", "VOLCANIC_WORLD", "TOXIC_WORLD",
                "DYING_WORLD", "PARADISE_WORLD", "WAR_TORN_WORLD", "COLONY_WORLD", "MINING_WORLD",
                "AGRICULTURAL_WORLD", "INDUSTRIAL_WORLD", "CIVILIAN_WORLD", "MILITARY_WORLD", "UNKNOWN_WORLD"
        };
        String[] planetTypeDescs = {
                "A rocky world with land and atmosphere.",
                "A massive gas planet with no solid surface.",
                "A frozen world with ice-covered surfaces.",
                "A world with active lava flows and extreme heat.",
                "A world covered mostly by water.",
                "A dry, arid world with vast deserts.",
                "A lush world covered in dense vegetation.",
                "A frozen world with permanent ice and snow.",
                "A world with active volcanoes and geothermal activity.",
                "A world with a toxic, uninhabitable atmosphere.",
                "A world whose biosphere is collapsing.",
                "A beautiful, pristine world of natural wonders.",
                "A world devastated by ongoing conflict.",
                "A settled world with established colonies.",
                "A world exploited for its mineral resources.",
                "A world dedicated to food production.",
                "A world with heavy manufacturing and industry.",
                "A peaceful world of non-military civilians.",
                "A world under military control.",
                "A world whose characteristics are not yet known."
        };
        for (int i = 0; i < planetTypes.length; i++) {
            tables.add(createTableEntry("Planet Type", "WORLD_GENERATION", "Appendix III", 301,
                    i + 1, planetTypes[i], planetTypeDescs[i]));
        }

        // ==================== Appendix IV: Additional Matrixes ====================

        // Starship Damage Table
        String[] starshipDamages = {
                "HULL_BREACH", "SHIELD_FAILURE", "ENGINE_DAMAGE", "WEAPON_OFFLINE", "SENSOR_BLIND",
                "COMPUTER_MALFUNCTION", "COMMS_DOWN", "LIFE_SUPPORT_FAILURE", "POWER_ROUTING_ERROR", "CARGO_LOSS",
                "CREW_CASUALTIES", "MEDICAL_BAY_DAMAGE", "TORPEDO_BAY_DAMAGE", "SHUTTLE_BAY_DAMAGE", "BRIDGE_DAMAGE",
                "WARP_CORE_BREACH", "INERTIAL_DAMPENER_FAILURE", "NAVIGATION_ERROR", "CLOAKING_FAILURE", "UNKNOWN_DAMAGE"
        };
        String[] starshipDamageDescs = {
                "A section of the hull is breached; atmosphere is venting.",
                "Shields drop to zero; the ship is vulnerable.",
                "Engines are damaged; speed is reduced.",
                "Weapons systems are offline.",
                "Sensors are blind; the ship cannot detect anything.",
                "Computer systems are malfunctioning.",
                "Communications are down.",
                "Life support fails in a section of the ship.",
                "Power is misrouted; some systems lose power.",
                "Cargo is lost or damaged.",
                "Crew members are killed or injured.",
                "The medical bay is damaged; treatment is impaired.",
                "The torpedo bay is damaged; torpedoes are unavailable.",
                "The shuttle bay is damaged; shuttles are unavailable.",
                "The bridge is damaged; command is impaired.",
                "The warp core is breaching; the ship is in critical danger.",
                "Inertial dampeners fail; movement causes injury.",
                "Navigation systems fail; the ship cannot plot courses.",
                "Cloaking device fails; the ship is visible.",
                "The nature of the damage is unknown."
        };
        for (int i = 0; i < starshipDamages.length; i++) {
            tables.add(createTableEntry("Starship Damage", "STARSHIP", "Appendix IV", 313,
                    i + 1, starshipDamages[i], starshipDamageDescs[i]));
        }

        // Personal Development Table
        String[] personalDevelopments = {
                "NEW_SKILL", "NEW_FOCUS", "NEW_TALENT", "VALUE_CHANGE", "VALUE_CHALLENGE",
                "NEW_ALLY", "NEW_ENEMY", "PERSONAL_CRISIS", "ROMANTIC_DEVELOPMENT", "FAMILY_MATTER",
                "CAREER_ADVANCEMENT", "DISCIPLINE_INCREASE", "ATTRIBUTE_INCREASE", "NEW_EQUIPMENT", "NEW_STARSHIP",
                "MENTORSHIP", "RETIREMENT_CONSIDERATION", "BLACK_OPS_ASSIGNMENT", "AWARD", "DISCIPLINARY_ACTION"
        };
        String[] personalDevDescs = {
                "The character learns a new skill.",
                "The character gains a new focus (specialty).",
                "The character develops a new talent.",
                "A core value is replaced or modified.",
                "A core value is tested and may change.",
                "The character gains a new ally.",
                "The character gains a new enemy.",
                "A personal crisis demands attention.",
                "A romantic relationship develops.",
                "A family matter requires resolution.",
                "The character is promoted or reassigned.",
                "A discipline rating increases.",
                "An attribute rating increases.",
                "The character acquires new equipment.",
                "The character is assigned a new starship.",
                "The character takes on a mentee or gains a mentor.",
                "The character considers retirement.",
                "A covert assignment is offered.",
                "The character receives an award or recognition.",
                "The character faces disciplinary action."
        };
        for (int i = 0; i < personalDevelopments.length; i++) {
            tables.add(createTableEntry("Personal Development", "CAREER", "Appendix IV", 317,
                    i + 1, personalDevelopments[i], personalDevDescs[i]));
        }

        // Equipment Table
        String[] equipments = {
                "PHASER_PISTOL", "PHASER_RIFLE", "TRICORDER", "HYPODERMIC_MEDICAL_INJECTOR", "COMMS_BADGE",
                "TRANSPORTER_MODULATOR", "INERTIAL_DAMPENER", "PHOTON_TORPEDO", "QUANTUM_TORPEDO", "SHIELD_GENERATOR",
                "WARP_DRIVE_COMPONENT", "SENSOR_ARRAY", "CLOAKING_DEVICE", "PROBE", "SCIENTIFIC_INSTRUMENT",
                "MEDICAL_SCANNER", "BIOBEAM", "GRAVITON_BOMB", "TEMPORAL_DEVICE", "ARTIFACT"
        };
        String[] equipmentDescs = {
                "Standard handheld energy weapon.",
                "Long-range energy weapon.",
                "Multi-purpose scanning and analysis device.",
                "Medical device for administering treatments.",
                "Personal communication device.",
                "Device for modifying transporter beams.",
                "Device for reducing inertial forces.",
                "Standard antimatter projectile weapon.",
                "Advanced antimatter projectile weapon.",
                "Defensive energy barrier generator.",
                "Component of the warp drive system.",
                "Advanced sensor detection array.",
                "Device for rendering the ship invisible.",
                "Deployable reconnaissance probe.",
                "Specialized scientific measurement tool.",
                "Device for scanning biological signatures.",
                "Device for beaming biological matter.",
                "Experimental weapon using graviton particles.",
                "Device that manipulates time.",
                "An object of unknown origin and purpose."
        };
        for (int i = 0; i < equipments.length; i++) {
            tables.add(createTableEntry("Equipment", "EQUIPMENT", "Appendix IV", 319,
                    i + 1, equipments[i], equipmentDescs[i]));
        }

        tableRepository.saveAll(tables);
        log.info("Populated {} table entries", tables.size());
    }

    // ==================== Query Methods ====================

    /**
     * Gets all rule entries for a specific category.
     */
    public List<RulesEntry> getRulesByCategory(String category) {
        return entryRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * Gets all rule entries for a specific chapter.
     */
    public List<RulesEntry> getRulesByChapter(String chapter) {
        return entryRepository.findByChapterAndActiveTrue(chapter);
    }

    /**
     * Searches rules by keyword across all categories.
     */
    public List<RulesEntry> searchRules(String keyword) {
        return entryRepository.searchByKeyword(keyword);
    }

    /**
     * Searches rules by keyword within a specific category.
     */
    public List<RulesEntry> searchRulesInCategory(String category, String keyword) {
        return entryRepository.searchByKeywordInCategory(category, keyword);
    }

    /**
     * Gets all available rule categories.
     */
    public List<String> getCategories() {
        return entryRepository.findDistinctCategories();
    }

    /**
     * Gets all available chapters.
     */
    public List<String> getChapters() {
        return entryRepository.findDistinctChapters();
    }

    /**
     * Gets all entries for a specific oracle table.
     */
    public List<RulesTable> getTableEntries(String tableName) {
        return tableRepository.findByTableNameAndActiveTrueOrderByRollValueAsc(tableName);
    }

    /**
     * Gets all table entries for a specific category.
     */
    public List<RulesTable> getTableEntriesByCategory(String category) {
        return tableRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * Gets all available table names.
     */
    public List<String> getTableNames() {
        return tableRepository.findDistinctTableNames();
    }

    /**
     * Gets all available table categories.
     */
    public List<String> getTableCategories() {
        return tableRepository.findDistinctCategories();
    }

    /**
     * Searches table entries by keyword.
     */
    public List<RulesTable> searchTables(String keyword) {
        return tableRepository.searchByKeyword(keyword);
    }

    /**
     * Gets a summary of all rules and tables for AI context.
     * Returns a compact representation suitable for AI prompts.
     */
    public Map<String, Object> getRulesSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("categories", getCategories());
        summary.put("chapters", getChapters());
        summary.put("tableNames", getTableNames());
        summary.put("tableCategories", getTableCategories());
        summary.put("totalEntries", entryRepository.count());
        summary.put("totalTableEntries", tableRepository.count());
        return summary;
    }

    /**
     * Gets rules relevant to a specific game action for AI context.
     * Returns the most relevant rules based on the action type.
     */
    public List<RulesEntry> getRulesForAction(String action) {
        List<RulesEntry> results = new ArrayList<>();

        String actionLower = action.toLowerCase();
        if (actionLower.contains("roll") || actionLower.contains("check") || actionLower.contains("task")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("DICE_MECHANICS"));
        }
        if (actionLower.contains("combat") || actionLower.contains("fight") || actionLower.contains("attack")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("COMBAT"));
        }
        if (actionLower.contains("momentum")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("MOMENTUM"));
        }
        if (actionLower.contains("threat")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("THREAT"));
        }
        if (actionLower.contains("mission") || actionLower.contains("act") || actionLower.contains("beat")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("MISSION_STRUCTURE"));
        }
        if (actionLower.contains("oracle") || actionLower.contains("yes") || actionLower.contains("no")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("ORACLE"));
        }
        if (actionLower.contains("ship") || actionLower.contains("starship")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("STARSHIP"));
        }
        if (actionLower.contains("character") || actionLower.contains("create") || actionLower.contains("lifepath")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("CHARACTER_CREATION"));
        }
        if (actionLower.contains("career") || actionLower.contains("promotion") || actionLower.contains("rank")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("CAREER"));
        }
        if (actionLower.contains("maintenance") || actionLower.contains("downtime") || actionLower.contains("repair")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("MAINTENANCE"));
        }
        if (actionLower.contains("equipment") || actionLower.contains("weapon") || actionLower.contains("gear")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("EQUIPMENT"));
        }
        if (actionLower.contains("crew") || actionLower.contains("npc") || actionLower.contains("team")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("CREW"));
        }
        if (actionLower.contains("campaign") || actionLower.contains("arc") || actionLower.contains("story")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("CAMPAIGN"));
        }
        if (actionLower.contains("injury") || actionLower.contains("health") || actionLower.contains("wound")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("INJURY"));
        }
        if (actionLower.contains("world") || actionLower.contains("planet") || actionLower.contains("generate")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("WORLD_GENERATION"));
        }
        if (actionLower.contains("setting") || actionLower.contains("universe") || actionLower.contains("trek")) {
            results.addAll(entryRepository.findByCategoryAndActiveTrue("SETTING"));
        }

        return results;
    }

    // ==================== Helper Methods ====================

    private RulesEntry createEntry(String title, String chapter, String category, Integer page, String text, String notes) {
        RulesEntry entry = new RulesEntry();
        entry.setTitle(title);
        entry.setChapter(chapter);
        entry.setCategory(category);
        entry.setPage(page);
        entry.setText(text);
        entry.setNotes(notes);
        entry.setActive(true);
        return entry;
    }

    private RulesTable createTableEntry(String tableName, String category, String chapter, Integer page,
                                         int rollValue, String resultLabel, String description) {
        RulesTable entry = new RulesTable();
        entry.setTableName(tableName);
        entry.setCategory(category);
        entry.setChapter(chapter);
        entry.setPage(page);
        entry.setRollValue(rollValue);
        entry.setResultLabel(resultLabel);
        entry.setDescription(description);
        entry.setActive(true);
        return entry;
    }
}
