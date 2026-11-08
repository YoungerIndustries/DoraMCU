# Classic Traveller Rules Database - Source Index

This directory contains structured rule extractions from 7 Classic Traveller PDFs, organized for import into the DoraMCU rules database.

## Source Books

| Code | Title | Pages | Focus |
|------|-------|-------|-------|
| Core | The Traveller Book (Vol. I) | 159 | Core rules, character creation, all careers, combat, starships, trade |
| B01 | Book 1: Characters & Combat (1981) | 51 | Updated character creation, detailed combat rules |
| B02 | Book 2: Starships (1981) | 52 | Ship design, space combat, ship systems |
| B04 | Book 4: Mercenary | 60 | Army, marines, military adventures, ground combat |
| B05 | Book 5: High Guard (1980) | 60 | Naval combat, advanced ship design, fleet actions |
| S03 | Supplement 3: The Spinward Marches | 48 | Sector map, world data, adventure hooks |
| A05 | Adventure 5: Trillion Credit Squadron | 52 | Naval adventure scenario, fleet tactics |

## Rule Categories

Each extraction file is organized into these categories that map to the `RulesEntry.category` field:

### Core Mechanics
- `DICE_MECHANICS` - 2D6 system, modifiers, saving throws
- `CHARACTER_CREATION` - Ability scores, career selection, retirement
- `SKILLS` - Skill lists, acquisition, proficiency levels
- `ENCUMBRANCE` - Weight limits, carrying capacity

### Combat
- `MELEE_COMBAT` - Blade weapons, armor, TAC system
- `FIREARMS_COMBAT` - Gun weapons, range bands, TAC system
- `VEHICLE_COMBAT` - Ground vehicle rules
- `SPACE_COMBAT` - Ship-to-ship combat
- `BOARDING_ACTION` - Boarding actions between ships

### Careers
- `CAREER_NAVY` - Navy career, skills, promotions
- `CAREER_MARINES` - Marines career
- `CAREER_ARMY` - Army career
- `CAREER_MERCHANT` - Merchant Service career
- `CAREER_EXPLORER` - Explorers career
- `CAREER_SCOUT` - Scouts career
- `CAREER_SPY` - Spies career
- `CAREER_NOBLE` - Nobles career
- `CAREER_CITIZEN` - Citizens career
- `CAREER_DRIFTER` - Drifters career

### Starships
- `SHIP_DESIGN` - Tonnage, drives, hull construction
- `SHIP_SYSTEMS` - Life support, cargo, fuel, power
- `SHIP_WEAPONS` - Ship-mounted weapons
- `SHIP_ARMOR` - Ship armor, point defense
- `SHIP_CREW` - Crew requirements, officers
- `SHIP_TRAVEL` - Jump travel, maneuver drives

### Trade & Economy
- `COMMODITIES` - Cargo types, prices, volumes
- `TRADE` - Trade computation, market conditions
- `WORLD_GENERATION` - UWP, world types, trade codes

### Military
- `MILITARY_ORGANIZATION` - Unit structure, formations
- `MILITARY_EQUIPMENT` - Military weapons, armor, vehicles
- `MILITARY_TACTICS` - Tactics, strategies

### Setting
- `SETTING_IMPERIUM` - Third Imperium background
- `SETTING_SPINWARD_MARCHES` - Spinward Marches sector
- `ALIEN_RACES` - Non-human species

### Reference Tables
- `TABLE_SAVING_THROW` - Saving throw tables
- `TABLE_DAMAGE` - Damage tables
- `TABLE_SHIP_COMBAT` - Ship combat tables
- `TABLE_WILDLIFE` - Wildlife encounter tables
- `TABLE_ENCOUNTER` - Random encounter tables
