# Traveller 2.0 (Mongoose Traveller 2E) Analysis and Improvements

## Executive Summary

This document analyzes the differences between **Classic Traveller** (currently implemented in DoraMCU) and **Mongoose Traveller 2nd Edition** (Update 2022, referred to as "Traveller 2.0"), and identifies specific improvements that can be made to the DoraMCU codebase.

**Key Finding:** Mongoose Traveller 2E is a modernization of Classic Traveller that retains the core 2D6 system while adding significant improvements to character creation, combat, skills, and gameplay flow.

---

## 1. Character Creation Improvements

### 1.1. Characteristics (Abilities)

**Classic Traveller (Current):**
- 7 abilities: Dex, St, End, Int, Edu, Soc, App
- All rolled 2D6 (2-12)
- Appearance (App) and Saving Throw (St) are separate stats

**Mongoose Traveller 2E:**
- **6 characteristics:** Strength (ST), Dexterity (DX), Endurance (EN), Intelligence (IN), Education (ED), Social Standing (SO)
- 3 of the 6 rolled with **boon** (3D6, keep best 2)
- No Appearance as a separate stat
- Saving Throw replaced by Endurance-based checks

**Improvement Recommendations:**
1. **Add Boon/Bane system** to ability rolling
   - Implement `roll3D6KeepBest2()` and `roll3D6KeepWorst2()` methods
   - Allow character creation to use boon for 3 abilities
2. **Consider removing Appearance** or making it a derived stat
3. **Merge Saving Throw into Endurance** for skill checks

### 1.2. Species Support

**Classic Traveller (Current):**
- Human-only character creation

**Mongoose Traveller 2E:**
- Full support for multiple species: Human, Aslan, Bwap, Vargr, Mellanni, etc.
- Species-specific modifiers to characteristics
- Species-specific career restrictions and bonuses

**Improvement Recommendations:**
1. Add `Species` enum with modifiers
2. Update `TravellerCharacter` to support species selection
3. Add species-specific career restrictions

### 1.3. Career System

**Classic Traveller (Current):**
- 8 careers: Soldier, Explorer, Gunner, Pilot, Merchant, Scout, Spy, Noble
- Binary skill acquisition (have/don't have)
- No specialization

**Mongoose Traveller 2E:**
- More careers with sub-specializations:
  - Navy (Space/Flight)
  - Marines (Star Marine/Assault)
  - Scouts (Deep/Border)
  - Soldiers (Infantry/Armor/Artillery)
  - Merchant Service (Free Trader/Broker/Merchant Marine)
  - Explorers (Survey/Outfitter)
  - Spies (Corporate/Intelligence)
  - Nobles (Court/Government)
  - Citizens (Scholar/Agent/Entertainer)
  - Drifters (Rogue/Thief/Enforcer/Pirate)
- **Skill levels** (0-4+)
- **Skill specialization**
- **Life events** during career terms
- **Contacts and enemies** tracking

**Improvement Recommendations:**
1. Add career sub-specializations
2. Implement skill levels (0-4+)
3. Add skill specialization system
4. Add life events during career terms
5. Track contacts and enemies

---

## 2. Skills System Improvements

### 2.1. Skill Levels

**Classic Traveller (Current):**
- Binary skills: have or don't have
- No progression

**Mongoose Traveller 2E:**
- Skills have levels: 0 (trained), 1, 2, 3, 4+
- Skill cap: **Int + Edu** (total skill levels cannot exceed this)
- Maximum skill level during character creation: 4 (unless specialization)

**Improvement Recommendations:**
1. Change `Set<Skill>` to `Map<Skill, Integer>` in `TravellerCharacter`
2. Add skill level cap based on Int + Edu
3. Implement skill progression during careers

### 2.2. Expanded Skills List

**Classic Traveller (Current):**
- 24 skills across 8 categories

**Mongoose Traveller 2E:**
- **50+ skills** including:
  - Admin, Advocate, Animals, Athletics, Art
  - Astrogation, Broker, Carouse, Comms
  - Computers, Deception, Diplomat, Drive
  - Engineer, Explosives, Flyer, Gambler
  - Gunner, Gun Combat, Heavy Weapons
  - Investigate, Jack of All Trades, Language
  - Leadership, Life Sciences, Mechanic, Medic
  - Melee, Navigation, Persuade, Pilot
  - Physical Sciences, Recon, Remote Operations
  - Science, Seafarer, Sensors, Social Sciences
  - Space Sciences, Stealth, Steward, Streetwise
  - Survival, Tactics, Trade, Vacc Suit, Zero-G

**Improvement Recommendations:**
1. Add missing skills to `Skill` enum
2. Reorganize skill categories to match Mongoose Traveller 2E
3. Add skill specializations

### 2.3. Skill Specialization

**Mongoose Traveller 2E:**
- Skills can have specializations (e.g., Gun Combat: Pistol, Rifle, SMG)
- Specializations provide +1 DM to the specialized area
- Characters can specialize in skills they have at level 1+

**Improvement Recommendations:**
1. Add `SkillSpecialization` enum
2. Link specializations to parent skills
3. Apply +1 DM for specialized skill checks

---

## 3. Combat System Improvements

### 3.1. Boon and Bane System

**Classic Traveller (Current):**
- Standard 2D6 rolls with modifiers

**Mongoose Traveller 2E:**
- **Boon:** Roll 3D6, keep best 2 (advantageous circumstances)
- **Bane:** Roll 3D6, keep worst 2 (disadvantageous circumstances)
- Boon and bane cancel each other out

**Improvement Recommendations:**
1. Add `BoonBane` enum: NONE, BOON, BANE
2. Update all dice rolling methods to support boon/bane
3. Apply boon/bane to combat, skills, and survival checks

### 3.2. Cover System

**Classic Traveller (Current):**
- No cover system

**Mongoose Traveller 2E:**
- Cover provides DM to attack rolls against covered target:
  - 1/4 cover (undergrowth, small rock): -0 DM
  - 1/2 cover (thick forest, low wall): -1 DM
  - 3/4 cover (jungle, trench): -2 DM
  - Full cover (pillbox): -4 DM
- Diving for cover: -2 DM to attacks, +1 to armor

**Improvement Recommendations:**
1. Add `CoverType` enum with DM values
2. Add cover tracking to combatants
3. Apply cover DM to ranged attack rolls

### 3.3. Effect-Based Resolution

**Classic Traveller (Current):**
- Pass/fail with margin of success

**Mongoose Traveller 2E:**
- **Effect** = roll result - target number
- Effect determines degree of success/failure:
  - Effect 6+: Exceptional success
  - Effect 1-5: Average success
  - Effect 0: Marginal success (with complications)
  - Effect -1 to -5: Marginal failure
  - Effect -6+: Catastrophic failure

**Improvement Recommendations:**
1. Add `Effect` calculation to all skill checks
2. Use effect to determine degree of success
3. Add complication system for marginal successes

### 3.4. Range Bands

**Classic Traveller (Current):**
- Short/Medium/Long range bands

**Mongoose Traveller 2E:**
- More detailed range bands:
  - Personal (<1.5m): 0 squares
  - Close (1.5-3m): 1-2 squares
  - Short (3-12m): 3-8 squares
  - Medium (12-50m): 9-34 squares
  - Long (51-250m): 35-166 squares
  - Very Long (251-500m): 167-334 squares
  - Distant (501m+): 334+ squares

**Improvement Recommendations:**
1. Update range bands in firearms combat
2. Add square-based movement tracking
3. Apply range DM to attack rolls

### 3.5. Stance System

**Mongoose Traveller 2E:**
- Standing: Normal combat
- Prone: -2 DM to ranged attacks, +1 to melee attacks
- Kneeling: -1 DM to ranged attacks, +0 to melee attacks

**Improvement Recommendations:**
1. Add `Stance` enum
2. Apply stance DM to attack rolls
3. Allow stance changes as actions

### 3.6. Armor System

**Classic Traveller (Current):**
- Armor reduces damage by fixed amount

**Mongoose Traveller 2E:**
- Armor has protection value that reduces damage
- **Armor piercing (AP)** weapons ignore some armor
- Effect 6+ always deals at least 1 damage regardless of armor
- Layered armor system (outer/inner layers)

**Improvement Recommendations:**
1. Add AP (armor piercing) to weapons
2. Implement layered armor system
3. Ensure effect 6+ always deals at least 1 damage

---

## 4. Vehicle Combat Improvements

### 4.1. Vehicle Combat System

**Classic Traveller (Current):**
- Basic vehicle combat

**Mongoose Traveller 2E:**
- Detailed vehicle combat with:
  - Vehicle types (light, medium, heavy)
  - Fire arcs (front/rear)
  - Vehicular actions (evasive, maneuvering, ram, stunt, weave)
  - Vehicle damage table with single/double/triple hits
  - Location-based damage

**Improvement Recommendations:**
1. Add `VehicleCombat` class
2. Implement fire arcs
3. Add vehicular actions
4. Implement vehicle damage table

---

## 5. Ship Design Improvements

### 5.1. Simplified Ship Design

**Classic Traveller (Current):**
- Complex tonnage-based design

**Mongoose Traveller 2E:**
- Simplified ship design process
- Pre-generated ship templates
- Deck plans for common ships

**Improvement Recommendations:**
1. Add pre-generated ship templates
2. Simplify ship design process
3. Add deck plan support

---

## 6. Trade and Economy Improvements

### 6.1. Trade System

**Classic Traveller (Current):**
- Basic commodity trading

**Mongoose Traveller 2E:**
- More detailed trade system with:
  - Supply and demand levels
  - Market conditions
  - Price fluctuations
  - Trade routes and logistics

**Improvement Recommendations:**
1. Add supply/demand levels to commodities
2. Implement market conditions
3. Add price fluctuation system

---

## 7. World Generation Improvements

### 7.1. Universal World Profile (UWP)

**Classic Traveller (Current):**
- Basic UWP implementation

**Mongoose Traveller 2E:**
- Enhanced UWP with:
  - More trade codes
  - World modifiers
  - Population and government types
  - Law level effects

**Improvement Recommendations:**
1. Add more trade codes
2. Implement world modifiers
3. Add law level effects

---

## 8. Implementation Priority

### High Priority (Core Gameplay)
1. **Boon/Bane system** - affects all dice rolls
2. **Skill levels** - fundamental to character progression
3. **Cover system** - important for tactical combat
4. **Effect-based resolution** - changes how success/failure works

### Medium Priority (Enhanced Features)
5. **Expanded skills list** - more character options
6. **Career sub-specializations** - more career depth
7. **Species support** - non-human characters
8. **Vehicle combat** - new combat type

### Low Priority (Nice to Have)
9. **Skill specialization** - fine-tuned character building
10. **Life events** - richer character backgrounds
11. **Contacts and enemies** - campaign hooks
12. **Stance system** - tactical combat options

---

## 9. Code Changes Summary

### New Classes/Enums
1. `BoonBane` enum
2. `Species` enum
3. `CoverType` enum
4. `Stance` enum
5. `SkillSpecialization` enum
6. `VehicleCombat` class
7. `LifeEvent` class
8. `Contact` class

### Modified Classes
1. `TravellerCharacter` - add species, skill levels, boon/bane
2. `Skill` - add levels and specializations
3. `Career` - add sub-specializations
4. `MeleeCombat` - add cover, stance, boon/bane
5. `FirearmsCombat` - add cover, stance, boon/bane, range bands
6. `Armor` - add AP and layered armor
7. `FirearmWeapon` - add AP values

### New Methods
1. `roll3D6KeepBest2()` - boon rolling
2. `roll3D6KeepWorst2()` - bane rolling
3. `calculateEffect()` - effect calculation
4. `applyCoverDM()` - cover modifier
5. `applyStanceDM()` - stance modifier

---

## 10. Testing Strategy

### Unit Tests
1. Test boon/bane dice rolling
2. Test skill level progression
3. Test cover DM application
4. Test effect calculation
5. Test species modifiers
6. Test career sub-specializations

### Integration Tests
1. Test character creation with boon/bane
2. Test combat with cover and stance
3. Test skill checks with effect resolution
4. Test vehicle combat scenarios

---

## 11. Conclusion

Mongoose Traveller 2E offers significant improvements over Classic Traveller while maintaining the core 2D6 system. The key improvements are:

1. **Boon/Bane system** for advantage/disadvantage
2. **Skill levels** for character progression
3. **Cover system** for tactical combat
4. **Effect-based resolution** for nuanced outcomes
5. **Expanded skills and careers** for more character options
6. **Species support** for non-human characters
7. **Vehicle combat** for ground vehicle battles

These improvements can be implemented incrementally, starting with the high-priority core gameplay changes and progressing to the enhanced features and nice-to-have additions.
