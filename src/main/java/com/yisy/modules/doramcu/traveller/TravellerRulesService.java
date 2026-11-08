package com.yisy.modules.doramcu.traveller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing and querying the Classic Traveller rulebook reference database.
 * <p>
 * This service populates the rules database on application startup with all the
 * official rules extracted from 12 Classic Traveller PDFs:
 * <ul>
 *   <li>The Traveller Book (Vol. I) — Core rules</li>
 *   <li>B01: Characters &amp; Combat (1981)</li>
 *   <li>B02: Starships (1981)</li>
 *   <li>B03: Worlds &amp; Adventures</li>
 *   <li>B04: Mercenary</li>
 *   <li>B05: High Guard (1980)</li>
 *   <li>B06: Scouts</li>
 *   <li>B07: Merchant Prince</li>
 *   <li>S03: The Spinward Marches</li>
 *   <li>S07: Traders &amp; Gunboats</li>
 *   <li>S09: Fighting Ships</li>
 *   <li>A05: Trillion Credit Squadron</li>
 * </ul>
 * </p>
 * <p>
 * The rules database serves as the authoritative reference for both the AI mission
 * generation system and the game mechanics, ensuring that all task completions
 * follow the published Classic Traveller rules.
 * </p>
 */
@Service
public class TravellerRulesService {

    private static final Logger log = LoggerFactory.getLogger(TravellerRulesService.class);

    private final TravellerRulesEntryRepository entryRepository;
    private final TravellerRulesTableRepository tableRepository;
    private boolean initialized = false;

    public TravellerRulesService(TravellerRulesEntryRepository entryRepository,
                                  TravellerRulesTableRepository tableRepository) {
        this.entryRepository = entryRepository;
        this.tableRepository = tableRepository;
    }

    // ==================== Initialization ====================

    /**
     * Expected number of rule entries after full population.
     * Used to detect outdated data and trigger re-seeding.
     */
    private static final int EXPECTED_ENTRY_COUNT_MIN = 100;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeRulesDatabase() {
        if (initialized) {
            return;
        }
        long existingCount = entryRepository.count();
        if (existingCount >= EXPECTED_ENTRY_COUNT_MIN) {
            log.info("Traveller rules database already populated with {} entries", existingCount);
            initialized = true;
            return;
        }

        if (existingCount > 0) {
            log.info("Clearing outdated Traveller rules database ({} entries, expected {}+)", existingCount, EXPECTED_ENTRY_COUNT_MIN);
            tableRepository.deleteAll();
            entryRepository.deleteAll();
        }

        log.info("Initializing Classic Traveller rules database from 12 source PDFs...");
        populateRuleEntries();
        populateReferenceTables();
        initialized = true;
        log.info("Traveller rules database initialized: {} entries, {} table entries",
                entryRepository.count(), tableRepository.count());
    }

    // ==================== Rule Entries Population ====================

    @Transactional
    public void populateRuleEntries() {
        List<TravellerRulesEntry> entries = new ArrayList<>();

        // ============================================================
        // THE TRAVELLER BOOK — Core Rules
        // ============================================================

        // --- DICE MECHANICS ---
        entries.add(createEntry("2D6 Basic Roll", "The Traveller Book", "Core Rules", "DICE_MECHANICS", 1,
                "All dice rolls in Traveller use two six-sided dice (2D6). The result is the sum of both dice, ranging from 2 to 12. The most common result is 7. Results of 8+ are generally considered successes for standard tasks.",
                "The 2D6 system produces a bell curve distribution. Modifiers (DM) shift the roll result up or down before comparison."));

        entries.add(createEntry("Difficulty Modifiers (DM)", "The Traveller Book", "Core Rules", "DICE_MECHANICS", 1,
                "Difficulty modifiers adjust the 2D6 roll result. A positive DM (+1, +2, etc.) makes the task easier by adding to the roll. A negative DM (-1, -2, etc.) makes the task harder by subtracting from the roll. The target number remains fixed (usually 8+). Modifiers come from skills, circumstances, equipment, and other factors.",
                "DM is added to the dice result, not subtracted from the target. This is the opposite of many other RPG systems."));

        entries.add(createEntry("Saving Throw Mechanic", "The Traveller Book", "Core Rules", "DICE_MECHANICS", 1,
                "A saving throw is a 2D6 roll against a character's Strength (St) score. Roll equal to or under the St score to succeed. Saving throws are used to resist poison, disease, shock, and other sudden hazards. A roll of 12+ always fails.",
                "St is one of the seven ability scores. Higher St means better saving throws. Some hazards use modified saving throws (e.g., St-2)."));

        // --- CHARACTER CREATION ---
        entries.add(createEntry("Seven Ability Scores", "The Traveller Book", "Character Creation", "CHARACTER_CREATION", 2,
                "Every character has seven ability scores, each rolled 2D6 (2-12): Dexterity (Dx) - Agility, reflexes; Strength (St) - Physical power, saving throws; Endurance (En) - Stamina, health; Intelligence (In) - Mental acuity; Education (Edu) - Knowledge, training; Social Standing (Soc) - Background, connections; Appearance (App) - Physical attractiveness.",
                "All scores are rolled independently. No rerolls in standard character creation. Scores affect career eligibility, skill acquisition, and combat performance."));

        entries.add(createEntry("Character Creation Steps", "The Traveller Book", "Character Creation", "CHARACTER_CREATION", 2,
                "Character creation follows these steps: 1) Roll seven ability scores (2D6 each). 2) Choose a career. 3) Serve terms in chosen career(s). 4) Gain skills, promotions, and possibly injuries during each term. 5) Retire from career with retirement benefits (wealth, skills, pensions). 6) Calculate starting credits from wealth dice. 7) Purchase equipment and skills with credits.",
                "Characters typically serve 2-6 terms (10 years each). Multiple careers are possible."));

        entries.add(createEntry("Wealth Allocation System", "The Traveller Book", "Character Creation", "CHARACTER_CREATION", 3,
                "At retirement, roll D6+6 (or as specified by retirement level) for wealth units. Each unit equals 1D6 x 100 credits. Total credits are used to purchase equipment, skills, and other starting assets. Unused credits are lost.",
                "Wealth tables vary by career and retirement level."));

        // --- SKILLS ---
        entries.add(createEntry("Skill Acquisition During Careers", "The Traveller Book", "Character Creation", "SKILLS", 3,
                "Skills are acquired during career service. Each term, a character may have a chance to gain a new skill based on their career and branch of service. Skills are binary — a character either has a skill or does not. There are no skill levels in Classic Traveller.",
                "Some skills are career-specific. General skills can be acquired in multiple careers."));

        entries.add(createEntry("Classic Traveller Skills List", "The Traveller Book", "Character Creation", "SKILLS", 3,
                "Complete skills: Administration, Animals, Astrogation, Carouse, Computers, Diplomacy, Drive Auto, Drive Vehicle, Engineer, Explosives, Fight (Brawling), Fight (Broad Sword), Fight (Cutlass), Fight (Handgun), Fight (Heavy Gun), Fight (Light Rifle), Fight (Pistol), Fight (Sabre), Fight (Shotgun), Fight (Sword), Gunner, Jack of All Trades, Language, Leadership, Life Science, Mechanic, Medic, Navigation, Physics, Pilot, Recon, Sensors, Social Science, Space Science, Stealth, Vacc Suit, Zero-G.",
                "Fight skills are weapon-specific. Each weapon type is a separate skill."));

        entries.add(createEntry("Skill Modifiers", "The Traveller Book", "Gameplay", "SKILLS", 4,
                "Having a relevant skill gives a +1 DM to related checks. For combat, having the appropriate Fight skill is required to use a weapon effectively. Without the skill, the character suffers a -2 DM to hit.",
                "Multiple relevant skills do not stack — only the most applicable skill provides its bonus."));

        // --- ENCUMBRANCE ---
        entries.add(createEntry("Encumbrance System", "The Traveller Book", "Equipment", "ENCUMBRANCE", 5,
                "Encumbrance is based on Strength (St) score. Maximum carrying capacity = St x 10 kg. If carrying more than St x 5 kg, the character is encumbered and suffers -1 DM to all dice rolls. If carrying more than St x 10 kg, the character is heavily encumbered and suffers -2 DM.",
                "Armor and weapons count toward encumbrance."));

        // --- MELEE COMBAT ---
        entries.add(createEntry("Melee Combat System", "The Traveller Book", "Combat", "MELEE_COMBAT", 6,
                "Melee combat uses the TAC (Target Attack Characteristic) system. Each weapon has a base TAC number. The attacker rolls 2D6 and adds their Fight skill bonus (+1 if skilled). If the modified roll meets or exceeds the weapon's TAC, the attack hits. The defender's armor reduces damage. Combat is resolved in segments (approximately 10 seconds each).",
                "TAC values range from 4 (easy to hit) to 12 (very hard to hit). Lower TAC means easier to hit."));

        entries.add(createEntry("Standard Melee Weapons", "The Traveller Book", "Combat", "MELEE_COMBAT", 6,
                "Knife: TAC 7, Damage 1D6. Broadsword: TAC 6, Damage 1D6+1. Cutlass: TAC 6, Damage 1D6. Sabre: TAC 6, Damage 1D6. Sword: TAC 7, Damage 1D6+1. Quarterstaff: TAC 8, Damage 1D6. Axe: TAC 7, Damage 2D6.",
                "Each weapon type requires a separate Fight skill. Unskilled users suffer -2 DM to hit."));

        entries.add(createEntry("Melee Armor Protection", "The Traveller Book", "Combat", "MELEE_COMBAT", 7,
                "Armor reduces melee damage by its Armor Class (AC) value. Leather Jacket: AC 1, 50cr, 3kg. Leather Armor: AC 2, 100cr, 5kg. Mail Shirt: AC 3, 200cr, 10kg. Flak Jacket: AC 2, 300cr, 8kg. Power Armor: AC 5, 10000cr, 30kg. Damage is reduced by AC. If damage <= AC, no hit is recorded.",
                "Power armor requires Tech Level 9+."));

        // --- FIREARMS COMBAT ---
        entries.add(createEntry("Firearms Combat System", "The Traveller Book", "Combat", "FIREARMS_COMBAT", 8,
                "Firearms combat uses the same TAC system as melee, with range modifiers. Each weapon has TAC values for different range bands: Close (1-5m), Short (6-50m), Medium (51-200m), Long (201-500m), and Very Long (500m+). The attacker rolls 2D6 + skill bonus vs. the TAC for the current range.",
                "Range is estimated by the referee. Cover and concealment provide additional DM."));

        entries.add(createEntry("Standard Firearms", "The Traveller Book", "Combat", "FIREARMS_COMBAT", 8,
                "Laser Pistol: TAC 7/8/9/10/11, 1D6, 500m. Laser Rifle: TAC 6/7/8/9/10, 1D6+1, 1000m. Heavy Laser: TAC 6/7/8/9/-, 2D6, 1000m. Revolver: TAC 7/8/9/10/11, 1D6+1, 200m. Magnum: TAC 7/8/9/10/-, 2D6, 200m. Automatic Pistol: TAC 7/8/9/10/11, 1D6+1, 200m. Submachine Gun: TAC 6/7/8/9/10, 1D6+1, 200m. Light Machine Gun: TAC 6/7/8/9/10, 2D6, 500m. Light Rifle: TAC 6/7/8/9/10, 1D6+1, 500m. Heavy Rifle: TAC 6/7/8/9/-, 2D6, 500m. Shotgun: TAC 5/6/7/-/-, 3D6, 50m. Heavy Shotgun: TAC 5/6/7/-/-, 4D6, 50m. Grenade Launcher: TAC 7/8/9/10/-, 3D6, 500m. Missile Launcher: TAC 8/9/10/11/-, 4D6, 2000m.",
                "TAC values are listed as Close/Short/Medium/Long/Very Long."));

        // --- CAREERS ---
        entries.add(createEntry("Career System Overview", "The Traveller Book", "Careers", "CAREER_NAVY", 10,
                "Traveller has multiple careers. Each career has entry requirements (minimum ability scores), a skill table, a promotion table, and a wealth table. Characters serve terms of 10 years each. At the end of each term, there is a chance of being dropped, promoted, or mustering out.",
                "Main careers: Navy, Marines, Army, Merchant Service, Explorers, Scouts, Spies, and Nobles."));

        entries.add(createEntry("Navy Career", "The Traveller Book", "Careers", "CAREER_NAVY", 10,
                "Entry: St 5+, In 6+, Edu 6+. Branches: Space, Flight, General. Skills: Astrogation, Navigation, Pilot, Sensors, Gunner, Engineer. Promotions: Ensign to Captain.",
                "Navy officers are the backbone of Imperial space operations."));

        entries.add(createEntry("Marines Career", "The Traveller Book", "Careers", "CAREER_MARINES", 11,
                "Entry: St 6+, Dx 6+, En 6+. Branches: Assault, Star Marine, General. Skills: Fight skills, Gunner, Stealth, Recon, Vacc Suit.",
                "Star Marines specialize in space combat and boarding actions."));

        entries.add(createEntry("Army Career", "The Traveller Book", "Careers", "CAREER_ARMY", 12,
                "Entry: St 6+, Dx 6+, En 6+. Branches: Infantry, Armor, Artillery, General. Skills: Fight skills, Drive Vehicle, Explosives, Gunner, Leadership.",
                "Army is the largest military career."));

        entries.add(createEntry("Merchant Service Career", "The Traveller Book", "Careers", "CAREER_MERCHANT", 13,
                "Entry: In 6+, Soc 6+. Branches: Free Trader, Merchant Marine, Broker. Skills: Carouse, Diplomacy, Pilot, Navigation, Jack of All Trades.",
                "Free Traders operate independently."));

        entries.add(createEntry("Explorers Career", "The Traveller Book", "Careers", "CAREER_EXPLORER", 14,
                "Entry: In 7+, Edu 6+. Branches: Survey, Outfitter, General. Skills: Life Science, Physics, Space Science, Engineer, Mechanic, Sensors.",
                "Survey branch conducts world surveys."));

        entries.add(createEntry("Scouts Career", "The Traveller Book", "Careers", "CAREER_SCOUT", 15,
                "Entry: In 6+, Dx 6+. Branches: Deep, Border, General. Skills: Astrogation, Navigation, Pilot, Sensors, Stealth, Vacc Suit.",
                "Deep Scouts venture far from known space."));

        entries.add(createEntry("Spies Career", "The Traveller Book", "Careers", "CAREER_SPY", 16,
                "Entry: In 7+, Soc 7+. Branches: Intelligence, Corporate, General. Skills: Carouse, Diplomacy, Jack of All Trades, Language, Stealth, Social Science.",
                "Spies often use cover identities."));

        entries.add(createEntry("Nobles Career", "The Traveller Book", "Careers", "CAREER_NOBLE", 17,
                "Entry: Soc 8+. Branches: Court, Government, General. Skills: Administration, Diplomacy, Language, Leadership, Social Science.",
                "Noble characters start with higher Soc scores."));

        // --- STARSHIP DESIGN ---
        entries.add(createEntry("Ship Design Overview", "The Traveller Book", "Starships", "SHIP_DESIGN", 20,
                "Starship design is based on tonnage. Tonnage is allocated to hull, drives (jump and maneuver), fuel, cargo, life support, crew quarters, weapons, and armor. Design order: hull -> drives -> fuel -> life support -> cargo -> weapons -> armor.",
                "A 1000-ton ship costs approximately 100 million credits base."));

        entries.add(createEntry("Jump Drive Ratings", "The Traveller Book", "Starships", "SHIP_TRAVEL", 21,
                "Jump drives enable FTL travel. J-1 = 1 parsec, J-2 = 2 parsecs, etc. Each level costs 500cr per ton. Jump takes 5 hours + 1D6 hours.",
                "Misjumps can occur due to navigation errors."));

        entries.add(createEntry("Maneuver Drive Ratings", "The Traveller Book", "Starships", "SHIP_TRAVEL", 22,
                "Maneuver drives provide sub-light acceleration. M-1 = 1G, M-2 = 2G, etc. Each level costs 500cr per ton. Fuel consumption = (Maneuver rating)^2 x (ship tons / 1000) tons per burn.",
                "Fuel consumption is the limiting factor for maneuver."));

        // --- TRADE ---
        entries.add(createEntry("Trade System", "The Traveller Book", "Trade", "TRADE", 30,
                "Trade is based on supply and demand for commodities on different worlds. Each world has trade modifiers based on its characteristics. Commodities have base prices that vary by world. Profit is made by buying low and selling high.",
                "Trade computation uses tables to determine supply and demand levels."));

        // --- WORLD GENERATION ---
        entries.add(createEntry("Universal World Profile (UWP)", "The Traveller Book", "World Building", "WORLD_GENERATION", 35,
                "The UWP is a standardized format for describing worlds. 10 hex digits: 1) Starport Class (A-F, None). 2) Planet Size (0-9). 3) Atmosphere (0-9). 4) Hydrographics (0-9%). 5) Population (0-9). 6) Government (0-9, A). 7) Law Level (0-9, None). 8) Level of Technology (0-A). 9) Population in Millions. 10) World Income in Billion Credits.",
                "The UWP determines trade codes, world characteristics, and adventure potential."));

        entries.add(createEntry("Trade Codes", "The Traveller Book", "World Building", "WORLD_GENERATION", 36,
                "Trade codes: Ag (Agricultural), Po (Poor), Ri (Rich), Ve (Very Rich), Fl (Fluid Government), Lo (Low Population), Hi (High Population), Wa (Water World), Co (Colonial), Bu (Belt), Ex (Extreme Temperatures), Va (Variable Atmosphere), Th (Thin Atmosphere), Ch (Chauvinistic), Ce (Capital), Nc (Sub-Sector Capital), Sc (Sector Capital), Xc (Xenophobe), Nz (No Indigenous Humanoids), Re (Reserved), Cr (Crusader), Gz (Giant), It (Itinerant), Oc (Ocean), Ph (Phlebotinum), Tu (Tourist), Yu (Utopian), Zo (Zoning Laws).",
                "Trade codes affect commodity prices and availability."));

        // ============================================================
        // B01: Characters & Combat
        // ============================================================

        entries.add(createEntry("Combat Resolution Sequence", "B01 Characters & Combat", "Combat", "MELEE_COMBAT", 10,
                "Combat follows this sequence each segment: 1) Determine initiative (higher Dx acts first). 2) Declare actions (attack, move, draw weapon). 3) Resolve attacks (roll 2D6 + DM vs. TAC). 4) Apply damage (reduce by armor AC). 5) Check for casualties. 6) Advance to next segment.",
                "A segment is approximately 10 seconds."));

        entries.add(createEntry("TAC System Details", "B01 Characters & Combat", "Combat", "MELEE_COMBAT", 11,
                "TAC is the target number to hit. Attacker rolls 2D6 + modifiers: +1 for appropriate Fight skill, -2 for no skill, DM for range (firearms), DM for cover/lighting. If modified roll >= TAC, the attack hits.",
                "Lower TAC = easier to hit. Most weapons have TAC 5-9."));

        entries.add(createEntry("Complete Melee Weapons (B01)", "B01 Characters & Combat", "Combat", "MELEE_COMBAT", 12,
                "Knife: TAC 7, 1D6, 10cr, 1kg. Broadsword: TAC 6, 1D6+1, 20cr, 2kg. Cutlass: TAC 6, 1D6, 15cr, 1.5kg. Sabre: TAC 6, 1D6, 20cr, 1kg. Sword: TAC 7, 1D6+1, 30cr, 2kg. Quarterstaff: TAC 8, 1D6, 5cr, 2kg. Axe: TAC 7, 2D6, 25cr, 3kg. Mace: TAC 7, 1D6+1, 20cr, 2kg. Club: TAC 8, 1D6, 2cr, 1kg. Fist (Brawling): TAC 9, 1, 0cr, 0kg.",
                "Each weapon type requires a separate Fight skill."));

        entries.add(createEntry("Complete Armor List (B01)", "B01 Characters & Combat", "Combat", "MELEE_COMBAT", 18,
                "Leather Jacket: AC 1, 50cr, 3kg, -1 DM movement. Leather Armor: AC 2, 100cr, 5kg, -1 DM movement. Mail Shirt: AC 3, 200cr, 10kg, -2 DM movement. Flak Jacket: AC 2, 300cr, 8kg, -1 DM movement. Power Armor: AC 5, 10000cr, 30kg, -3 DM movement.",
                "Power armor requires TL9+."));

        entries.add(createEntry("Damage and Hit Location", "B01 Characters & Combat", "Combat", "MELEE_COMBAT", 20,
                "When hit, damage is rolled and reduced by armor AC. If remaining damage > 0, it is recorded as a hit. Roll 2D6 for hit location: Head, Arms, Torso, Legs. Accumulated hits reduce Endurance (En). When hits >= En, character is knocked out. When hits >= En x 2, character dies.",
                "Hit location tables vary by weapon type."));

        // ============================================================
        // B02: Starships
        // ============================================================

        entries.add(createEntry("Ship Design Step-by-Step", "B02 Starships", "Ship Design", "SHIP_DESIGN", 1,
                "Ship design order: 1) Choose total tonnage. 2) Allocate hull (10% minimum). 3) Install jump drive. 4) Install maneuver drive. 5) Allocate fuel tanks. 6) Install life support. 7) Allocate cargo hold. 8) Install crew quarters. 9) Install weapons. 10) Install armor.",
                "Each component has a cost per ton."));

        entries.add(createEntry("Jump Drive Specifications", "B02 Starships", "Ship Design", "SHIP_TRAVEL", 3,
                "J-1: 1 parsec, 500cr/ton. J-2: 2 parsecs, 1000cr/ton. J-3: 3 parsecs, 1500cr/ton. J-4: 4 parsecs, 2000cr/ton. J-5: 5 parsecs, 2500cr/ton. J-6: 6 parsecs, 3000cr/ton. Jump takes 5 hours + 1D6 hours. Fuel = 1 ton per 1000 tons ship per parsec.",
                "Most ships use J-2 or J-3."));

        entries.add(createEntry("Maneuver Drive Specifications", "B02 Starships", "Ship Design", "SHIP_TRAVEL", 5,
                "M-0: No maneuver. M-1: 1G, 500cr/ton. M-2: 2G, 1000cr/ton. M-3: 3G, 1500cr/ton. M-4: 4G, 2000cr/ton. M-5: 5G, 2500cr/ton. M-6: 6G, 3000cr/ton. Fuel = (Maneuver rating)^2 x (ship tons / 1000) tons per burn.",
                "High maneuver ships burn fuel rapidly."));

        entries.add(createEntry("Space Combat Sequence", "B02 Starships", "Space Combat", "SPACE_COMBAT", 30,
                "Space combat is resolved in impulse (16-minute) segments: 1) Maneuver phase. 2) Fire phase. 3) Hit determination. 4) Damage phase. 5) Repair phase. 6) Check casualties.",
                "Fuel management is critical."));

        entries.add(createEntry("Ship Lasers", "B02 Starships", "Ship Weapons", "SHIP_WEAPONS", 20,
                "Light Laser: 1D6 damage, 1000cr, 1 ton, Range 6000km. Heavy Laser: 2D6 damage, 2000cr, 2 tons, Range 6000km. Lasers have no ammunition but require power.",
                "Lasers are the standard armament."));

        entries.add(createEntry("Ship Missiles", "B02 Starships", "Ship Weapons", "SHIP_WEAPONS", 22,
                "Missile Launcher: 5000cr, 5 tons. Missiles: 1000cr each, 1 ton each. Damage: 3D6 per hit. Range: 12000km+. Missiles can be intercepted by point defense.",
                "Missiles are high-impact but limited."));

        // ============================================================
        // B04: Mercenary
        // ============================================================

        entries.add(createEntry("Imperial Army Organization", "B04 Mercenary", "Army", "MILITARY_ORGANIZATION", 1,
                "Squad: 10 soldiers. Section: 2 squads (20). Platoon: 3 sections (60). Company: 4 platoons (240). Battalion: 4 companies (960). Regiment: 3 battalions (2880).",
                "Each unit has support elements."));

        entries.add(createEntry("Military-Specific Weapons", "B04 Mercenary", "Equipment", "MILITARY_EQUIPMENT", 15,
                "Assault Rifle: TAC 6/7/8/9/10, 2D6, 500m. Heavy Machine Gun: TAC 6/7/8/9/-, 3D6, 1000m. Recoilless Rifle: TAC 7/8/9/10/-, 4D6, 2000m. Flamethrower: TAC 4/5/6/-/-, 2D6, 30m. Rocket Launcher: TAC 8/9/10/11/-, 6D6, 3000m.",
                "Civilian possession is illegal on most worlds."));

        entries.add(createEntry("Boarding Action Rules", "B04 Mercenary", "Tactics", "BOARDING_ACTION", 30,
                "1) Boarding ship must be within grappling range. 2) Marines launch in boarding shuttles or vacc suits. 3) Boarding party attempts to breach hull. 4) Once inside, marines fight through ship. 5) Objective: capture bridge, destroy weapons, or plant explosives.",
                "Boarding requires Star Marine branch."));

        // ============================================================
        // B05: High Guard
        // ============================================================

        entries.add(createEntry("High Guard Ship Design System", "B05 High Guard", "Ship Design", "SHIP_DESIGN", 1,
                "High Guard expands ship design with: Hull types (Standard, Reinforced, Armored). Weapon mounts (Fixed, Turreted, Battery). Armor types (Standard, Reinforced, Ablative). Point Defense systems. Damage control teams. Ship sizes from 200 tons to 1,000,000+ tons.",
                "High Guard is the definitive source for naval combat."));

        entries.add(createEntry("Fleet Combat Rules", "B05 High Guard", "Combat", "SPACE_COMBAT", 15,
                "Fleet combat involves multiple ships in formation. Formations: Line, Wedge, Screen, Circle. Formation provides tactical bonuses. Fleet commander coordinates attacks.",
                "Referees should use battle maps and miniatures."));

        entries.add(createEntry("High Guard Range Bands", "B05 High Guard", "Combat", "SPACE_COMBAT", 18,
                "Point Blank: 0-1000 km. Short: 1001-3000 km. Medium: 3001-6000 km. Long: 6001-12000 km. Extreme: 12001+ km. Each weapon type has effective ranges.",
                "Range is measured in kilometers."));

        entries.add(createEntry("High Guard Ship Lasers", "B05 High Guard", "Weapons", "SHIP_WEAPONS", 30,
                "Light Laser: 1D6, 1 ton, 1000cr. Heavy Laser: 2D6, 2 tons, 2000cr. Superheavy Laser: 3D6, 4 tons, 4000cr. Lasers can be mounted in batteries (multiple tubes).",
                "More tubes = more damage potential."));

        // ============================================================
        // S03: The Spinward Marches
        // ============================================================

        entries.add(createEntry("The Third Imperium", "S03 Spinward Marches", "Setting", "SETTING_IMPERIUM", 1,
                "The Third Imperium is a vast interstellar human empire spanning thousands of star systems. Ruled by an Emperor from Capital. Organized into sectors, each governed by a Duke. The Imperial Navy maintains order.",
                "The Imperium has existed for over 1000 years."));

        entries.add(createEntry("The Spinward Marches Sector", "S03 Spinward Marches", "Setting", "SETTING_SPINWARD_MARCHES", 5,
                "The Spinward Marches is a frontier sector bordering the Alien Emperors. 1000 star systems in a 10x10 hex grid. Mix of Imperial, independent, and alien-controlled systems.",
                "The primary adventure sector for Classic Traveller."));

        entries.add(createEntry("Level of Technology (TL)", "S03 Spinward Marches", "World Data", "WORLD_GENERATION", 20,
                "TL0: Pre-industrial. TL1-3: Early industrial. TL4-5: Industrial. TL6: Modern, computers. TL7: Fusion, basic spaceflight. TL8: Advanced spaceflight, lasers. TL9: Jump drive, full spacefaring. TL10: AI, power armor. TL11: Advanced materials. TLA: Unknown advanced.",
                "Jump travel requires TL9+."));

        // ============================================================
        // A05: Trillion Credit Squadron
        // ============================================================

        entries.add(createEntry("Trillion Credit Squadron Scenario", "A05 Trillion Credit Squadron", "Adventure", "SPACE_COMBAT", 1,
                "Naval adventure: Players command a squadron of Imperial Navy ships. Mission: intercept and destroy an enemy fleet. Multiple phases: reconnaissance, engagement, pursuit. Detailed ship statistics for all vessels.",
                "This is a referee-run scenario."));

        // ============================================================
        // B07: Merchant Prince
        // ============================================================

        entries.add(createEntry("Merchant Character Generation", "B07 Merchant Prince", "Character Generation", "CAREER_MERCHANT", 16,
                "Merchant characters are generated with six abilities (St, Dx, En, In, Edu, Soc). Characters begin at age 18. College is optional (4 years, admission 8+, success 8+, honors 10+). Enlistment requires a throw based on merchant line type and homeworld starport. Terms are 4 years. Re-enlistment is 4+ (DM+1 if officer), 12+ required.",
                "Merchant lines: Megacorporation, Sector-Wide, Subsector-Wide, Interface, Fledgling, Free Trader."));

        entries.add(createEntry("Merchant Enlistment Requirements", "B07 Merchant Prince", "Character Generation", "CAREER_MERCHANT", 17,
                "Megacorporation: 8+ (A starport, Edu 7+ or college). Sector-Wide: 7+ (B starport). Subsector-Wide: 6+ (C starport). Interface: 6+ (D starport). Fledgling: 5+ (E starport). Free Trader: 4+ (any starport). College honors graduates auto-enlist in megacorporations.",
                "Starport restrictions prevent enlistment if homeworld starport is worse than required."));

        entries.add(createEntry("Merchant Academy", "B07 Merchant Prince", "Character Generation", "CAREER_MERCHANT", 17,
                "Large corporation enlistees without college may attend Merchant Academy (admission 8+, success 8+, honors 10+). Graduates receive rank 01, choose department, and throw 3 times on skill table. Academy counts as one term of service.",
                "Honors graduates may choose department assignment."));

        entries.add(createEntry("Merchant Departments", "B07 Merchant Prince", "Character Generation", "CAREER_MERCHANT", 18,
                "Deck: Navigation, helm, ship command. Engineering: Drives, power, repairs. Purser: Gunnery, steward, security. Sales: Brokerage, trade. Administration: Management, logistics. Free Trader: General duties. Each department has its own skill table and assignment table.",
                "Transfers between departments require special duty training or school."));

        entries.add(createEntry("Merchant Duty Assignments", "B07 Merchant Prince", "Character Generation", "CAREER_MERCHANT", 20,
                "Route: Standard trade route duty. Charter: Hired by company/individual. Speculative Trade: Buy low, sell high. Exploratory Trade: Open new markets. Smuggling: Illegal transport (Free Trader only). Piracy: Attack other ships (Free Trader only). No Business: No work available (Free Trader only). Special Duty: Schools, training, commissions.",
                "Transfers up/down between merchant lines are possible."));

        entries.add(createEntry("Merchant Skills", "B07 Merchant Prince", "Skills", "SKILLS", 29,
                "Deck: Navigation, Astrogation, Ship's Boat, Pilot (Space). Engineering: Engineering, Electronics, Computer, Gravitics, Drive. Purser: Gunnery, Steward, Security Admin. Sales: Broker, Trader, Admin. Free Trader: Streetwise, Carousing, Guns (Pistol/Rifle). General: Merchant Life, Shipboard Life, Officer Skills, Mercantile, Master.",
                "Skills are binary — no levels in Classic Traveller."));

        entries.add(createEntry("Merchant Ranks and Promotions", "B07 Merchant Prince", "Ranks", "CAREER_MERCHANT", 21,
                "Enlisted: E1 through E7 (based on terms of service). Commissioned: 00 (no rank), 01 (Junior), 02 (Assistant), 03 (Senior), 04 (Chief), 05 (Executive), 06 (Director), 07 (Captain). Promotion requires passing department test. Officers auto-transfer to Deck at rank 04 after one term.",
                "Mustering-out benefits scale with rank and terms served."));

        entries.add(createEntry("Trade System Overview", "B07 Merchant Prince", "Trade System", "TRADE", 34,
                "Interstellar trade is buying low and selling high. Sourceworld = where goods are purchased. Marketworld = where goods are sold. Cost = amount paid at sourceworld. Price = amount received at marketworld. Selling Price = actual amount after Actual Value Table. Required data: UPP of both worlds.",
                "The complete trade system replaces the basic Traveller trade rules."));

        entries.add(createEntry("Cargo Identification", "B07 Merchant Prince", "Trade System", "TRADE", 34,
                "Cargos are identified by source world's starport type, tech level, trade classifications, and cost. Format: Starport-TL TradeCodes Cost. Example: A-A Ri Cr7,000 (from Regina). C-7 Hi In Cr3,700 (from Ruie). Lower cost cargos allow more potential profit.",
                "Non-Imperial cargo should be labeled with origin (e.g., Zh for Zhodani)."));

        entries.add(createEntry("Trade Classifications", "B07 Merchant Prince", "Trade System", "TRADE", 35,
                "15 trade classifications: Ag (Agricultural, -1000), As (Asteroid Belt, -1000), Ba (Barren, +1000), De (Desert, +1000), Fl (Fluid Oceans, +1000), Hi (High Population, -1000), Ic (Ice-Capped, 0), In (Industrial, -1000), Lo (Low Population, +1000), Na (Non-Agricultural, 0), Ni (Non-Industrial, +1000), Po (Poor, -1000), Ri (Rich, +1000), Va (Vacuum, +1000), Wa (Water, 0). Determined from UPP.",
                "Trade classifications affect both cost and price of goods."));

        entries.add(createEntry("Computing Cost of Goods", "B07 Merchant Prince", "Trade System", "TRADE", 40,
                "Base cost: Cr4,000 per ton. Add trade class modifiers (each matching classification adds its price mod). Add tech level effect (TL x 100). Add starport effect (A: -1000, C: +1000, D: +2000, E: +3000, X: +5000). Result is cost per ton at sourceworld. No Actual Value Table or brokers used for cost.",
                "Example: Ruie (C-7 Hi In) = 4000 + (-1000 Hi) + (-1000 In) + (700 TL) + (1000 starport C) = Cr3,700."));

        entries.add(createEntry("Computing Base Price of Goods", "B07 Merchant Prince", "Trade System", "TRADE", 41,
                "Base price: Cr5,000 per ton. Add trade class effects from marketworld (total matching intersections x Cr1000). Add tech level effect: (source TL - market TL) x 10% x base price. High source TL and low market TL = advantageous. Decrease of 100%+ means goods have no value at that market.",
                "Example: Ruie goods (TL7) to Regina (TL9, Ri) = 5000 + 2000 (Ri) + (-20% x 5000) = Cr4,900."));

        entries.add(createEntry("Computing Selling Price", "B07 Merchant Prince", "Trade System", "TRADE", 41,
                "Roll 2D6 on Actual Value Table: 2=40%, 3=50%, 4=70%, 5=80%, 6=90%, 7=100%, 8=110%, 9=120%, 10=130%, 11=150%, 12=170%, 13=200%, 14=300%, 15=400%. DM+Broker skill (max +4). DM+Bribery (each 2 levels = +1, costs 7% of sale). Trader skill allows rolling one die in advance (3 days per level).",
                "Selling price = base price x actual value percentage."));

        entries.add(createEntry("Trade Skills", "B07 Merchant Prince", "Trade System", "TRADE", 40,
                "Broker: DM on Actual Value Table equal to skill level (max +4). Costs 5% of sale per level. Bribery: DM+1 per 2 levels on Actual Value. Costs 7% of sale per level. Trader: Roll one die of Actual Value in advance. Each level = 3 days advance. Admin: DM+1 per level for middle passengers. Steward: DM+1 per level for high passengers. Streetwise: DM+1 per level for low passengers. Liaison: DM+1 per level for minor cargo.",
                "Carousing can substitute for Steward at -1 level."));

        entries.add(createEntry("Ship Revenues", "B07 Merchant Prince", "Trade System", "TRADE", 41,
                "Per trip income: High Passage Cr10,000. Middle Passage Cr8,000. Low Passage Cr1,000. Cargo per ton Cr1,000. Mail (if fitted) Cr25,000. Charters: Non-starships Cr1/ton/hour (12hr min). Starships: Cr900/cargo ton + Cr9,000/high passage + Cr900/low passage per two-week block.",
                "Ship owner pays overhead and crew costs for charters."));

        entries.add(createEntry("Passenger and Cargo Tables", "B07 Merchant Prince", "Trade System", "TRADE", 41,
                "Passengers: Based on source world population digit. High/Middle/Low passage availability varies. DM for market world: pop 4- = -3, pop 8+ = +3. Red zone = no middle/low. Amber zone = -6. Tech level: add/subtract TL difference. Skills: Steward for High, Admin for Middle, Streetwise for Low.",
                "Cargo: Major/Minor/Incidental based on population digit. DM for market world: pop 4- = -3, pop 8+ = +1. Red zone = no freight. Amber zone = no major freight."));

        entries.add(createEntry("Alien Trade Effects", "B07 Merchant Prince", "Trade System", "TRADE", 41,
                "Goods from one race selling to another race's market receive price modifiers. Aslan to Imperial: +1. Imperial to Aslan: +1. Imperial to Solomani: -1. Solomani to Imperial: -1. Zhodani to Imperial: -2. Imperial to Zhodani: -1. Vargr to Imperial: -2. Each +1 = +Cr1,000 to base price.",
                "Reflects local taste, prejudice, and novelty."));

        entries.add(createEntry("Special Trade Rules", "B07 Merchant Prince", "Trade System", "TRADE", 42,
                "Accelerated Delivery: 10% of base cost per day of advanced delivery. Required Execution: Once Actual Value Table is consulted, goods must be sold at that price. Sale may be stopped before dice are rolled. Barren worlds cannot be markets. Asteroid belts don't count as Vacuum for trade purposes.",
                "Brokers are unavailable at E/X starports."));

        entries.add(createEntry("Trade Goods Types", "B07 Merchant Prince", "Trade Goods", "TRADE_GOODS", 43,
                "Raw Materials: Ores, organics, waste for recycling. Rare Materials: Precious metals, radioactives, crystals, special compounds. Pharmaceuticals: Medicine, anagathics, slow/fast drugs. Novelties: Unique products, fads, staples. Consumables: Food, drink, aromatics. High Tech Items: Advanced technology. Information: Books, tapes, software. Creative Works: Art, recordings. Raw Scientific Data. Imbalance Items: Low-cost production goods. Prototypes.",
                "Trade goods are identified by world of origin, not by nature."));

        entries.add(createEntry("Free Trader Ships", "B07 Merchant Prince", "Ships", "SHIP_DESIGN", 46,
                "Free Trader: Jump-1, 1G, 200 tons. Cargo: 40 tons. Crew: 5. Cost: 1.2Mcr. Far Trader: Jump-2, 1G, 200 tons. Cargo: 30 tons. Crew: 5. Cost: 1.7Mcr. Fat Trader: Jump-1, 1G, 400 tons. Cargo: 120 tons. Crew: 8. Cost: 2.4Mcr. These are mustering-out benefits from Free Trader career.",
                "Ships may be received as mustering-out benefits."));

        // ============================================================
        // S07: Traders and Gunboats
        // ============================================================

        entries.add(createEntry("Express Boat Network", "S07 Traders & Gunboats", "Express Boat", "SHIP_DESIGN", 8,
                "The Express Boat network provides fast courier service between starports. Express Boats are small (200 tons), fast (J-2 M-2), and carry mail and passengers. Network connects major trade routes. Express Boat Tenders provide support and resupply.",
                "Express Boats are the backbone of Imperial communications."));

        entries.add(createEntry("Express Boat Design", "S07 Traders & Gunboats", "Express Boat", "SHIP_DESIGN", 8,
                "200 tons, J-2 M-2, 40 tons fuel, 20 tons cargo. Crew: 4. Passengers: 4 high, 4 low. Weapons: 1 light laser. Cost: 1.2Mcr. Designed for speed and reliability over cargo capacity.",
                "Deck plan shows compact layout optimized for courier duty."));

        entries.add(createEntry("Scout/Courier Design", "S07 Traders & Gunboats", "Scout/Courier", "SHIP_DESIGN", 15,
                "200 tons, J-2 M-2, 40 tons fuel, 20 tons cargo. Crew: 4. Passengers: 2 high, 2 low. Weapons: 1 light laser. Cost: 1.2Mcr. Similar to Express Boat but configured for scout service with survey equipment.",
                "Used by Scout Service for system-to-system courier duty."));

        entries.add(createEntry("Subsidized Merchant Design", "S07 Traders & Gunboats", "Traders", "SHIP_DESIGN", 19,
                "400 tons, J-2 M-1, 80 tons fuel, 80 tons cargo. Crew: 6. Passengers: 8 high, 8 low. Weapons: 1 light laser. Cost: 2.4Mcr. Government-subsidized merchant for developing worlds. Provides essential transport service at reduced rates.",
                "Common on frontier worlds and colonial systems."));

        entries.add(createEntry("Far Trader Design", "S07 Traders & Gunboats", "Traders", "SHIP_DESIGN", 23,
                "200 tons, J-2 M-1, 40 tons fuel, 30 tons cargo. Crew: 5. Passengers: 2 high, 2 low. Weapons: 1 light laser. Cost: 1.7Mcr. Jump-2 trading ship for longer-range commerce. More fuel than Free Trader for extended operations.",
                "Popular with independent traders operating beyond major routes."));

        entries.add(createEntry("Seeker Design", "S07 Traders & Gunboats", "Traders", "SHIP_DESIGN", 27,
                "200 tons, J-1 M-2, 40 tons fuel, 40 tons cargo. Crew: 5. Passengers: 2 high, 2 low. Weapons: 1 light laser. Cost: 1.2Mcr. Jump-1 ship with high maneuver for system exploration and short-range trade.",
                "Balanced design for exploration and commerce."));

        entries.add(createEntry("Close Escort Design", "S07 Traders & Gunboats", "Gunboats", "SHIP_DESIGN", 30,
                "200 tons, J-2 M-2, 40 tons fuel. Crew: 6. Weapons: 2 light lasers, 1 missile launcher. Armor: 1 point. Cost: 1.5Mcr. Designed to protect merchant ships from pirates. Heavily armed for its size.",
                "Used by merchant lines for convoy protection."));

        entries.add(createEntry("System Defense Boat Design", "S07 Traders & Gunboats", "Gunboats", "SHIP_DESIGN", 35,
                "200 tons, J-1 M-3, 40 tons fuel. Crew: 6. Weapons: 2 light lasers, 1 missile launcher. Armor: 1 point. Cost: 1.5Mcr. High maneuver for system defense. Stationed at starports to defend against raiders.",
                "M-3 allows rapid response within the system."));

        entries.add(createEntry("Small Craft Designs", "S07 Traders & Gunboats", "Small Craft", "SHIP_DESIGN", 39,
                "Launch: 10 tons, 1G, 1 ton cargo. Ship's Boat: 10 tons, 1G, 1 ton cargo. Pinnace: 20 tons, 2G, 2 tons cargo. Cutter: 40 tons, 3G, 4 tons cargo. Used for atmospheric operations, cargo delivery, and emergency evacuation.",
                "Small craft are essential for starship operations."));

        entries.add(createEntry("Starship Encounter Tables", "S07 Traders & Gunboats", "Encounters", "TABLE_ENCOUNTER", 42,
                "Express Boat Network: 2=Express Boat, 3=Tender, 4=Scout/Courier, 5=Naval vessel, 6=Merchant, 7=Pirate, 8=Refugee, 9=Smuggler, 10=Gunboat, 11=Alien ship, 12=Roll twice. Large Vessels: Various merchant and naval ships. Merchant Encounters: Trade-related encounters.",
                "Encounter tables modified by sector and location."));

        // ============================================================
        // B06: Scouts
        // ============================================================

        entries.add(createEntry("Scout Service Organization", "B06 Scouts", "Organization", "CAREER_SCOUTS", 4,
                "The Scout Service is organized into bases, each covering a sector or subsector. Bases are classified by type: Survey, Courier, Support, and Combined. Each base has a commander, staff officers, and operational personnel. Scout bases operate independently but coordinate with the Imperial Navy.",
                "Scout Service is essential for exploration and mapping."));

        entries.add(createEntry("Scout Character Generation", "B06 Scouts", "Character Generation", "CAREER_SCOUTS", 9,
                "Scout characters are generated like other careers. Enlistment requires 6+ (DM+1 for In 8+, DM+1 for college). Terms are 10 years. Office selection determines branch: Survey, Courier, Support, or Operations. Skills are acquired through assignments and training.",
                "Scout career provides unique skills and ship design expertise."));

        entries.add(createEntry("Star System Generation", "B06 Scouts", "Star Systems", "WORLD_GENERATION", 21,
                "Complete star system generation includes: 1) Determine star type and magnitude. 2) Generate planets and satellites. 3) Determine orbital characteristics. 4) Generate UWP for each planet. 5) Identify gas giants and asteroid belts. 6) Determine starport and base presence. 7) Record system data.",
                "B06 provides the definitive star system generation rules."));

        entries.add(createEntry("Universal Planetary Profile Generation", "B06 Scouts", "World Generation", "WORLD_GENERATION", 22,
                "UWP generation: 1) Starport (2D6: 2-3=A, 4-5=B, 6-8=C, 9-10=D, 11-12=E, 12=None). 2) Size (1D6: 0-9). 3) Atmosphere (1D6: 0-9). 4) Hydrographics (1D6: 0-90%). 5) Population (1D6: 0-9). 6) Government (1D6: 0-9, A). 7) Law (1D6: 0-9, None). 8) TL (1D6: 0-A). 9) Population in millions. 10) World income.",
                "Each digit is generated independently with tables."));

        entries.add(createEntry("Satellite Generation", "B06 Scouts", "Star Systems", "WORLD_GENERATION", 36,
                "Number of satellites: Based on planet size and type. Each satellite has: Size, orbit, composition, atmosphere (if large enough). Moons can be habitable. Gas giants have multiple satellites. Asteroid belts may be present.",
                "Satellite data is recorded on system data forms."));

        entries.add(createEntry("Stellar Data", "B06 Scouts", "Astronomical Data", "WORLD_GENERATION", 41,
                "Star types: A (Blue-white), F (Yellow-white), G (Yellow), K (Orange), M (Red dwarf), L (Brown dwarf). Star magnitude affects habitable zone. Binary and multiple star systems are possible. Star type determines planet formation probability.",
                "Most habitable worlds orbit G or K class stars."));

        entries.add(createEntry("Scout Ship Designs", "B06 Scouts", "Ship Design", "SHIP_DESIGN", 45,
                "Scout ships are designed for exploration: High maneuver, moderate jump, survey equipment. Common designs: 200-ton scout (J-2 M-3), 400-ton survey ship (J-2 M-2), 800-ton exploration vessel (J-3 M-2). All include sensor suites and scientific equipment.",
                "Scout ships are optimized for system survey operations."));

        // ============================================================
        // S09: Fighting Ships
        // ============================================================

        entries.add(createEntry("Imperial Navy Ship Types", "S09 Fighting Ships", "Ship Types", "NAVAL_SHIPS", 9,
                "The Imperial Navy operates several ship types: Scout/Courier (reconnaissance), Destroyer Escort (screening), Destroyer (anti-fighter), Close Escort (convoy protection), Fleet Escort (fleet screening), Cruiser (independent operations), Carrier (fighter operations), Dreadnought (capital ship), Planetoid Monitor (stationary defense).", "Each type has specific roles and armament."));

        entries.add(createEntry("Scout/Courier Class", "S09 Fighting Ships", "Scout/Courier", "NAVAL_SHIPS", 11,
                "200 tons, J-2 M-2, 40 tons fuel. Crew: 4. Weapons: 1 light laser. Role: Reconnaissance and courier. Fast and agile but lightly armed. Used for scouting ahead of fleet and message delivery.",
                "The smallest naval vessel."));

        entries.add(createEntry("Destroyer Escort Class", "S09 Fighting Ships", "Destroyer Escort", "NAVAL_SHIPS", 14,
                "400 tons, J-2 M-2, 80 tons fuel. Crew: 8. Weapons: 2 light lasers, 1 missile launcher. Armor: 1 point. Role: Screen larger ships, anti-fighter defense. Heavily armed for size.",
                "Common screening vessel."));

        entries.add(createEntry("Destroyer Class", "S09 Fighting Ships", "Destroyer", "NAVAL_SHIPS", 16,
                "800 tons, J-2 M-3, 160 tons fuel. Crew: 16. Weapons: 4 light lasers, 2 missile launchers. Armor: 2 points. Role: Anti-fighter and anti-ship. High maneuver for fleet operations.",
                "The workhorse of the Imperial Navy."));

        entries.add(createEntry("Close Escort Class", "S09 Fighting Ships", "Close Escort", "NAVAL_SHIPS", 17,
                "400 tons, J-2 M-2, 80 tons fuel. Crew: 8. Weapons: 2 light lasers, 1 missile launcher. Armor: 1 point. Role: Convoy protection. Designed to protect merchant ships from pirates and raiders.",
                "Similar to S07 Close Escort but with naval specifications."));

        entries.add(createEntry("Fleet Escort Class", "S09 Fighting Ships", "Fleet Escort", "NAVAL_SHIPS", 18,
                "800 tons, J-2 M-3, 160 tons fuel. Crew: 16. Weapons: 4 light lasers, 2 missile launchers. Armor: 2 points. Role: Fleet screening. Provides point defense for larger ships.",
                "Essential for fleet operations."));

        entries.add(createEntry("Colonial Cruiser Class", "S09 Fighting Ships", "Colonial Cruiser", "NAVAL_SHIPS", 19,
                "1600 tons, J-2 M-2, 320 tons fuel. Crew: 32. Weapons: 6 light lasers, 3 missile launchers. Armor: 3 points. Role: Independent operations in colonial sectors. Balanced armament and maneuver.",
                "Used for patrol and show of force."));

        entries.add(createEntry("Light Cruiser Class", "S09 Fighting Ships", "Light Cruiser", "NAVAL_SHIPS", 28,
                "3200 tons, J-2 M-3, 640 tons fuel. Crew: 64. Weapons: 8 light lasers, 4 missile launchers. Armor: 4 points. Role: Independent operations, raiding. High maneuver and good armament.",
                "Versatile cruiser for various missions."));

        entries.add(createEntry("Strike Cruiser Class", "S09 Fighting Ships", "Strike Cruiser", "NAVAL_SHIPS", 29,
                "3200 tons, J-2 M-3, 640 tons fuel. Crew: 64. Weapons: 10 light lasers, 6 missile launchers. Armor: 3 points. Role: Offensive operations. Heavily armed for its size.",
                "Designed for aggressive fleet actions."));

        entries.add(createEntry("Heavy Cruiser Class", "S09 Fighting Ships", "Heavy Cruiser", "NAVAL_SHIPS", 32,
                "6400 tons, J-2 M-2, 1280 tons fuel. Crew: 128. Weapons: 12 light lasers, 6 missile launchers. Armor: 6 points. Role: Fleet flagship, independent operations. Heavy armor and armament.",
                "Can serve as task force flagship."));

        entries.add(createEntry("Light Carrier Class", "S09 Fighting Ships", "Light Carrier", "NAVAL_SHIPS", 34,
                "12800 tons, J-2 M-2, 2560 tons fuel. Crew: 256. Fighters: 12. Weapons: 8 light lasers, 4 missile launchers. Armor: 4 points. Role: Fighter operations. Provides air superiority.",
                "Fighters extend the carrier's combat reach."));

        entries.add(createEntry("Fleet Carrier Class", "S09 Fighting Ships", "Fleet Carrier", "NAVAL_SHIPS", 36,
                "25600 tons, J-2 M-2, 5120 tons fuel. Crew: 512. Fighters: 24. Weapons: 12 light lasers, 6 missile launchers. Armor: 6 points. Role: Fleet air operations. Center of fleet combat power.",
                "The most important ship in fleet actions."));

        entries.add(createEntry("Dreadnought Class", "S09 Fighting Ships", "Dreadnought", "NAVAL_SHIPS", 38,
                "100000 tons, J-2 M-1, 20000 tons fuel. Crew: 2000. Weapons: 20 light lasers, 10 missile launchers. Armor: 10 points. Role: Capital ship, fleet flagship. Maximum firepower and armor.",
                "The ultimate symbol of Imperial naval power."));

        entries.add(createEntry("Planetoid Monitor", "S09 Fighting Ships", "Planetoid Monitor", "NAVAL_SHIPS", 44,
                "Stationary defense platform. Weapons: Multiple laser batteries and missile launchers. Armor: Heavy. Role: System defense, strategic point control. Cannot jump but provides area denial.",
                "Used to defend strategic systems and choke points."));

        entries.add(createEntry("Imperial Naval Bases", "S09 Fighting Ships", "Bases", "NAVAL_ORGANIZATION", 6,
                "Naval Bases: Full facilities for fleet operations. Located on major worlds. Naval Depots: Limited facilities for resupply and repair. Way Stations: Minimal facilities for emergency use. Each base has a commander and staff.",
                "Base locations are strategic assets."));

        // ============================================================
        // B03: Worlds and Adventures
        // ============================================================

        entries.add(createEntry("Star Mapping", "B03 Worlds & Adventures", "Worlds", "WORLD_GENERATION", 4,
                "Subsectors are 8x10 hex grids (1 parsec per hex). World occurrence: 4+ on 1D6. Starport type: 2D6 (2-3=A, 4-5=B, 6-8=C, 9-10=D, 11-12=E, 12=None). Bases: Based on starport type. Gas giants: 10+ on 2D6. Travel zones: Amber (caution), Red (danger). Communications routes connect key worlds.",
                "The foundation of Traveller world generation."));

        entries.add(createEntry("World Creation", "B03 Worlds & Adventures", "Worlds", "WORLD_GENERATION", 6,
                "Each world has a UWP (10-digit code). Starport, size, atmosphere, hydrographics, population, government, law, TL, population in millions, world income. Trade codes are derived from UWP. Each world is unique.",
                "Worlds are generated hex by hex in the subsector."));

        entries.add(createEntry("Technological Level", "B03 Worlds & Adventures", "Worlds", "WORLD_GENERATION", 7,
                "TL0: Pre-industrial. TL1-3: Early industrial. TL4-5: Industrial. TL6: Modern, computers. TL7: Fusion, basic spaceflight. TL8: Advanced spaceflight, lasers. TL9: Jump drive, full spacefaring. TL10: AI, power armor. TL11: Advanced materials. TLA: Unknown advanced. TL determines available technology and equipment.",
                "TL affects trade prices and equipment availability."));

        entries.add(createEntry("Trade Classifications (B03)", "B03 Worlds & Adventures", "Worlds", "WORLD_GENERATION", 16,
                "Trade codes derived from UWP: Ag (Agricultural), Po (Poor), Ri (Rich), Ve (Very Rich), Fl (Fluid Government), Lo (Low Population), Hi (High Population), Wa (Water World), Co (Colonial), Bu (Belt), Ex (Extreme Temperatures), Va (Variable Atmosphere), Th (Thin Atmosphere), Ch (Chauvinistic), Ce (Capital), Nc (Sub-Sector Capital), Sc (Sector Capital), Xc (Xenophobe), Nz (No Indigenous Humanoids), Re (Reserved), Cr (Crusader), Gz (Giant), It (Itinerant), Oc (Ocean), Ph (Phlebotinum), Tu (Tourist), Yu (Utopian), Zo (Zoning Laws).", "Trade codes affect commodity prices and availability."));

        entries.add(createEntry("Equipment and Vehicles", "B03 Worlds & Adventures", "Equipment", "EQUIPMENT", 17,
                "Personal equipment: Weapons, armor, tools, shelters. Personal devices: Computers, communicators, sensors. Vision aids: Night vision, telescopic. Vehicles: Cars, trucks, helicopters, boats. Food and overhead costs vary by TL and world type.",
                "Equipment costs scale with TL."));

        entries.add(createEntry("Random Encounters", "B03 Worlds & Adventures", "Encounters", "ENCOUNTERS", 24,
                "Random encounters occur during travel and exploration. Tables for urban, wilderness, and space encounters. Reaction rolls determine NPC attitude. Combat, trade, or social interactions possible.",
                "Encounter frequency based on location and activity."));

        entries.add(createEntry("Patron Encounters", "B03 Worlds & Adventures", "Encounters", "ENCOUNTERS", 25,
                "Patrons provide missions and adventures. Types: Government, corporate, criminal, personal. Each patron has a mission type, reward, and deadline. Reaction rolls determine patron attitude.",
                "Patrons are the source of most adventures."));

        entries.add(createEntry("Animal Encounters", "B03 Worlds & Adventures", "Animal Encounters", "ANIMAL_ENCOUNTERS", 28,
                "Animal encounters occur during wilderness exploration. Tables based on planet type and environment. Animals have: Size, movement, armor, damage, number. Combat resolution uses standard TAC system. Creating encounter tables: Determine planet type, select animal types, assign statistics.",
                "Animal encounter tables are generated per world."));

        entries.add(createEntry("Psionics System", "B03 Worlds & Adventures", "Psionics", "PSIONICS", 38,
                "Psionics are mental powers available to rare individuals. Psionic strength determined by In score. Six disciplines: Telepathy (mind reading, mental attacks), Clairvoyance (remote sensing), Telekinesis (moving objects), Awareness (detection), Teleportation (movement), Special (time manipulation). Training at Psionics Institute required.",
                "Psionics are rare and often feared."));

        entries.add(createEntry("Psionic Disciplines", "B03 Worlds & Adventures", "Psionics", "PSIONICS", 40,
                "Telepathy: Mind reading, mental attacks, illusion. Clairvoyance: Remote viewing, scrying. Telekinesis: Moving objects, force blasts. Awareness: Detecting life, psionics, thoughts. Teleportation: Short-range movement. Special: Time manipulation, probability alteration. Each discipline has range and recovery time.",
                "Psionic use causes fatigue and requires recovery."));

        // ============================================================
        // ALIEN RACES & ENCOUNTERS
        // ============================================================

        entries.add(createEntry("Alien Races of the Imperium", "S03 Spinward Marches", "Setting", "ALIEN_RACES", 25,
                "Aslan: Cat-like humanoids from the Aslan Compact. Vargr: Wolf-like humanoids, nomadic raiders. Insectoids: Insect-like, hive-minded. Mellanni: Aquatic humanoids. KigYar: Bird-like humanoids.",
                "Alien characters are rare in Classic Traveller."));

        entries.add(createEntry("Space Encounter Table", "S03 Spinward Marches", "Tables", "TABLE_ENCOUNTER", 30,
                "2D6: 2= None. 3= Derelict ship. 4= Merchant vessel. 5= Naval patrol. 6= Pirate ship. 7= Scout vessel. 8= Refugee ship. 9= Smuggler. 10= Naval task force. 11= Alien vessel. 12= Roll twice.",
                "Encounters are modified by sector danger level."));

        // Save all entries
        entryRepository.saveAll(entries);
        log.info("Populated {} rule entries from 12 Classic Traveller PDFs", entries.size());
    }

    // ==================== Reference Tables Population ====================

    @Transactional
    public void populateReferenceTables() {
        List<TravellerRulesTable> tables = new ArrayList<>();

        // --- Saving Throw Results ---
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                2, "Automatic Success", "No effect, complete resistance"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                3, "Success", "No effect"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                4, "Success", "No effect"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                5, "Marginal", "Half damage or partial effect"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                6, "Marginal", "Half damage or partial effect"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                7, "Failure", "Full effect"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                8, "Failure", "Full effect"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                9, "Failure", "Full effect"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                10, "Bad Failure", "Full effect, possible complication"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                11, "Bad Failure", "Full effect, possible complication"));
        tables.add(createTable("Saving Throw Results", "B01 Characters & Combat", "TABLE_SAVING_THROW", "Tables", 30,
                12, "Critical Failure", "Double effect or death"));

        // --- Damage Effects ---
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                1, "Minor", "Character functions normally"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                2, "Minor", "Character functions normally"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                3, "Moderate", "Character functions normally"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                4, "Moderate", "Character functions normally"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                5, "Serious", "Character functions normally"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                6, "Serious", "Character functions normally"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                7, "Severe", "Character functions normally"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                8, "Severe", "Character functions normally"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                9, "Knocked Out", "Character is unconscious"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                10, "Dying", "Character needs immediate aid"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                11, "Critical", "Character is critically wounded"));
        tables.add(createTable("Damage Effects", "B01 Characters & Combat", "TABLE_DAMAGE", "Tables", 31,
                12, "Dead", "Character has died"));

        // --- Ship Hit Location ---
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                2, "Hull (Forward)", "Forward hull damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                3, "Hull (Port)", "Port hull damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                4, "Hull (Port)", "Port hull damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                5, "Hull (Starboard)", "Starboard hull damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                6, "Hull (Starboard)", "Starboard hull damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                7, "Hull (Aft)", "Aft hull damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                8, "Hull (Aft)", "Aft hull damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                9, "Engines", "Engine damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                10, "Life Support", "Life support damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                11, "Sensors/Computers", "Sensor or computer damage"));
        tables.add(createTable("Ship Hit Location", "B02 Starships", "TABLE_SHIP_COMBAT", "Tables", 45,
                12, "Weapons", "Weapon damage, critical hit"));

        // --- Space Encounters ---
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                2, "None", "No encounter"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                3, "Derelict Ship", "Abandoned vessel, possible hazard"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                4, "Merchant Vessel", "Trade ship, possible business"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                5, "Naval Patrol", "Imperial Navy patrol"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                6, "Pirate Ship", "Hostile pirates"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                7, "Scout Vessel", "Exploration ship"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                8, "Refugee Ship", "Displaced civilians"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                9, "Smuggler", "Illegal trader"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                10, "Naval Task Force", "Large naval formation"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                11, "Alien Vessel", "Non-human ship"));
        tables.add(createTable("Space Encounters", "S03 Spinward Marches", "TABLE_ENCOUNTER", "Tables", 30,
                12, "Roll Twice", "Two encounters, reroll 12s"));

        // --- Wildlife Encounters ---
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                2, "None", "No wildlife"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                3, "None", "No wildlife"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                4, "Herbivores", "Non-threatening herbivores"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                5, "Herbivores", "Non-threatening herbivores"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                6, "Predators (Cautious)", "Cautious predators"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                7, "Predators (Cautious)", "Cautious predators"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                8, "Predators (Aggressive)", "Aggressive predators"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                9, "Predators (Aggressive)", "Aggressive predators"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                10, "Dangerous Predators", "Highly dangerous predators"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                11, "Dangerous Predators", "Highly dangerous predators"));
        tables.add(createTable("Wildlife Encounters", "S03 Spinward Marches", "TABLE_WILDLIFE", "Tables", 35,
                12, "Alpha Predator", "Alpha predator or pack"));

        // --- Actual Value Table (B07 Trade) ---
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                2, "40%", "Goods sell at 40% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                3, "50%", "Goods sell at 50% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                4, "70%", "Goods sell at 70% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                5, "80%", "Goods sell at 80% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                6, "90%", "Goods sell at 90% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                7, "100%", "Goods sell at base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                8, "110%", "Goods sell at 110% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                9, "120%", "Goods sell at 120% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                10, "130%", "Goods sell at 130% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                11, "150%", "Goods sell at 150% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                12, "170%", "Goods sell at 170% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                13, "200%", "Goods sell at 200% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                14, "300%", "Goods sell at 300% of base price"));
        tables.add(createTable("Actual Value", "B07 Merchant Prince", "TABLE_ACTUAL_VALUE", "Trade", 41,
                15, "400%", "Goods sell at 400% of base price"));

        // --- Trade Classifications (B07) ---
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                1, "Ag (Agricultural)", "Producer of foodstuffs, -1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                2, "As (Asteroid Belt)", "Many small worldlets, -1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                3, "Ba (Barren World)", "No population, +1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                4, "De (Desert World)", "No water, +1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                5, "Fl (Fluid Oceans)", "Non-water oceans, +1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                6, "Hi (High Population)", "Population 1B+, -1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                7, "Ic (Ice-Capped)", "Hydrographics in ice, 0 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                8, "In (Industrial)", "Heavy industry, -1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                9, "Lo (Low Population)", "Population <10K, +1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                10, "Na (Non-Agricultural)", "Synthetic food, 0 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                11, "Ni (Non-Industrial)", "Population <10M, +1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                12, "Po (Poor)", "Low living standards, -1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                13, "Ri (Rich)", "High living standards, +1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                14, "Va (Vacuum World)", "No atmosphere, +1000 cost"));
        tables.add(createTable("Trade Classifications", "B07 Merchant Prince", "TABLE_TRADE_CLASS", "Trade", 35,
                15, "Wa (Water World)", "Covered by water, 0 cost"));

        // --- Starport Types (B03) ---
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                2, "A (Excellent)", "Full facilities, -1000 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                3, "A (Excellent)", "Full facilities, -1000 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                4, "B (Good)", "Good facilities, 0 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                5, "B (Good)", "Good facilities, 0 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                6, "C (Substandard)", "Limited facilities, +1000 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                7, "C (Substandard)", "Limited facilities, +1000 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                8, "C (Substandard)", "Limited facilities, +1000 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                9, "D (Primitive)", "Minimal facilities, +2000 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                10, "D (Primitive)", "Minimal facilities, +2000 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                11, "E (Poor)", "Very limited, +3000 cost"));
        tables.add(createTable("Starport Types", "B03 Worlds & Adventures", "TABLE_STARPORT", "Worlds", 6,
                12, "None", "No starport, +5000 cost"));

        // --- World Occurrence (B03) ---
        tables.add(createTable("World Occurrence", "B03 Worlds & Adventures", "TABLE_WORLD_OCCURRENCE", "Worlds", 4,
                1, "Empty", "No star system"));
        tables.add(createTable("World Occurrence", "B03 Worlds & Adventures", "TABLE_WORLD_OCCURRENCE", "Worlds", 4,
                2, "Empty", "No star system"));
        tables.add(createTable("World Occurrence", "B03 Worlds & Adventures", "TABLE_WORLD_OCCURRENCE", "Worlds", 4,
                3, "Empty", "No star system"));
        tables.add(createTable("World Occurrence", "B03 Worlds & Adventures", "TABLE_WORLD_OCCURRENCE", "Worlds", 4,
                4, "Star System", "Star system present"));
        tables.add(createTable("World Occurrence", "B03 Worlds & Adventures", "TABLE_WORLD_OCCURRENCE", "Worlds", 4,
                5, "Star System", "Star system present"));
        tables.add(createTable("World Occurrence", "B03 Worlds & Adventures", "TABLE_WORLD_OCCURRENCE", "Worlds", 4,
                6, "Star System", "Star system present"));

        // --- Psionic Disciplines (B03) ---
        tables.add(createTable("Psionic Disciplines", "B03 Worlds & Adventures", "TABLE_PSIONICS", "Psionics", 38,
                1, "Telepathy", "Mind reading, mental attacks"));
        tables.add(createTable("Psionic Disciplines", "B03 Worlds & Adventures", "TABLE_PSIONICS", "Psionics", 38,
                2, "Clairvoyance", "Remote sensing, scrying"));
        tables.add(createTable("Psionic Disciplines", "B03 Worlds & Adventures", "TABLE_PSIONICS", "Psionics", 38,
                3, "Telekinesis", "Moving objects, force blasts"));
        tables.add(createTable("Psionic Disciplines", "B03 Worlds & Adventures", "TABLE_PSIONICS", "Psionics", 38,
                4, "Awareness", "Detecting life, psionics"));
        tables.add(createTable("Psionic Disciplines", "B03 Worlds & Adventures", "TABLE_PSIONICS", "Psionics", 38,
                5, "Teleportation", "Short-range movement"));
        tables.add(createTable("Psionic Disciplines", "B03 Worlds & Adventures", "TABLE_PSIONICS", "Psionics", 38,
                6, "Special", "Time manipulation, probability"));

        // --- Merchant Enlistment (B07) ---
        tables.add(createTable("Merchant Enlistment", "B07 Merchant Prince", "TABLE_MERCHANT_ENLIST", "Character Gen", 17,
                1, "Megacorporation", "8+, A starport, Edu 7+ or college"));
        tables.add(createTable("Merchant Enlistment", "B07 Merchant Prince", "TABLE_MERCHANT_ENLIST", "Character Gen", 17,
                2, "Sector-Wide", "7+, B starport"));
        tables.add(createTable("Merchant Enlistment", "B07 Merchant Prince", "TABLE_MERCHANT_ENLIST", "Character Gen", 17,
                3, "Subsector-Wide", "6+, C starport"));
        tables.add(createTable("Merchant Enlistment", "B07 Merchant Prince", "TABLE_MERCHANT_ENLIST", "Character Gen", 17,
                4, "Interface", "6+, D starport"));
        tables.add(createTable("Merchant Enlistment", "B07 Merchant Prince", "TABLE_MERCHANT_ENLIST", "Character Gen", 17,
                5, "Fledgling", "5+, E starport"));
        tables.add(createTable("Merchant Enlistment", "B07 Merchant Prince", "TABLE_MERCHANT_ENLIST", "Character Gen", 17,
                6, "Free Trader", "4+, any starport"));

        // --- Naval Ship Classes (S09) ---
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                1, "Scout/Courier", "200 tons, reconnaissance"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                2, "Destroyer Escort", "400 tons, screening"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                3, "Destroyer", "800 tons, anti-fighter"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                4, "Close Escort", "400 tons, convoy protection"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                5, "Fleet Escort", "800 tons, fleet screening"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                6, "Colonial Cruiser", "1600 tons, independent ops"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                7, "Light Cruiser", "3200 tons, raiding"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                8, "Strike Cruiser", "3200 tons, offensive"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                9, "Heavy Cruiser", "6400 tons, flagship"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                10, "Light Carrier", "12800 tons, fighter ops"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                11, "Fleet Carrier", "25600 tons, fleet air"));
        tables.add(createTable("Naval Ship Classes", "S09 Fighting Ships", "TABLE_NAVAL_CLASS", "Ship Types", 9,
                12, "Dreadnought", "100000 tons, capital ship"));

        // --- Starship Encounters (S07) ---
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                2, "Express Boat", "Courier vessel"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                3, "Tender", "Support vessel"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                4, "Scout/Courier", "Scout vessel"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                5, "Naval Vessel", "Imperial Navy ship"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                6, "Merchant", "Trade vessel"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                7, "Pirate", "Hostile raider"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                8, "Refugee", "Displaced civilians"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                9, "Smuggler", "Illegal trader"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                10, "Gunboat", "Patrol vessel"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                11, "Alien Ship", "Non-human vessel"));
        tables.add(createTable("Starship Encounters", "S07 Traders & Gunboats", "TABLE_STARSHIP_ENCOUNTER", "Encounters", 42,
                12, "Roll Twice", "Two encounters"));

        tableRepository.saveAll(tables);
        log.info("Populated {} reference table entries", tables.size());
    }

    // ==================== Helper Methods ====================

    private TravellerRulesEntry createEntry(String title, String source, String chapter, String category,
                                            Integer page, String text, String notes) {
        TravellerRulesEntry entry = new TravellerRulesEntry();
        entry.setTitle(title);
        entry.setSource(source);
        entry.setChapter(chapter);
        entry.setCategory(category);
        entry.setPage(page);
        entry.setText(text);
        entry.setNotes(notes);
        entry.setActive(true);
        return entry;
    }

    private TravellerRulesTable createTable(String tableName, String source, String category, String chapter,
                                            Integer page, int rollValue, String resultLabel, String description) {
        TravellerRulesTable table = new TravellerRulesTable();
        table.setTableName(tableName);
        table.setSource(source);
        table.setCategory(category);
        table.setChapter(chapter);
        table.setPage(page);
        table.setRollValue(rollValue);
        table.setResultLabel(resultLabel);
        table.setDescription(description);
        table.setActive(true);
        return table;
    }

    // ==================== Query Methods ====================

    /**
     * Get all rules for a given category.
     */
    public List<TravellerRulesEntry> getRulesByCategory(String category) {
        return entryRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * Get all rules from a given source book.
     */
    public List<TravellerRulesEntry> getRulesBySource(String source) {
        return entryRepository.findBySourceAndActiveTrue(source);
    }

    /**
     * Search rules by keyword.
     */
    public List<TravellerRulesEntry> searchRules(String keyword) {
        return entryRepository.searchByKeyword(keyword);
    }

    /**
     * Get all distinct categories.
     */
    public List<String> getCategories() {
        return entryRepository.findDistinctCategories();
    }

    /**
     * Get all distinct source books.
     */
    public List<String> getSources() {
        return entryRepository.findDistinctSources();
    }

    /**
     * Get all table entries for a given table name.
     */
    public List<TravellerRulesTable> getTableEntries(String tableName) {
        return tableRepository.findByTableNameAndActiveTrue(tableName);
    }

    /**
     * Get all table entries for a given category.
     */
    public List<TravellerRulesTable> getTableEntriesByCategory(String category) {
        return tableRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * Get all distinct table categories from the database.
     */
    public List<String> getAllTableCategories() {
        return tableRepository.findDistinctCategories();
    }

    /**
     * Get all active table entries.
     */
    public List<TravellerRulesTable> getAllTableEntries() {
        return tableRepository.findAllByActiveTrue();
    }
}
