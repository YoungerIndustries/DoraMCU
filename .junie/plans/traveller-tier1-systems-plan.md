# Traveller RPG — Tier 1 Gameplay Systems Implementation Plan

## Overview

Implement the three missing gameplay systems that exist in the rules database but have no gameplay code:
1. **Psionics System** (B03 Worlds & Adventures) — Psychic abilities with 6 disciplines
2. **Smuggling Gameplay** (B07 Merchant Prince) — Illegal trade with customs evasion
3. **Alien Race Mechanics** (S03 Spinward Marches) — Species traits affecting gameplay

## Current State

- Rules DB has all three systems documented (116 entries, 137 tables from 12 PDFs)
- All P0-P3 integration gaps are closed (trade processing, mercenary income, character-commander link, trade UI)
- 119 Traveller Java classes covering combat, careers, trade, ships, naval, adventures, world generation

---

### ✓ Step 1: Implement Psionics System (B03)

**Scope:** ~300 lines across 4 new files + 1 service update

**New Classes:**
- `PsionicDiscipline.java` — Enum with 6 disciplines: Telepathy, Clairvoyance, Telekinesis, Awareness, Teleportation, Special
- `PsionicAbility.java` — Character's psionic profile: detected level, strength, disciplines, fatigue
- `PsionicCheck.java` — Dice roll resolver for psionic power attempts using In score + discipline level
- `PsionicEncounter.java` — Random psionic encounter generator for adventures

**Service Updates:**
- `TravellerAdventureService.java` — Add psionic detection during character creation, psionic power checks during adventures
- `TravellerCharacter.java` — Add psionicAbility field

**Rules Reference:**
- TABLE_PSIONICS (6 entries in rules DB)
- Psionic detection uses In score + random roll
- Power checks use discipline level vs. difficulty

---

### ✓ Step 2: Implement Smuggling Gameplay (B07)

**Scope:** ~200 lines across 3 new files + 2 service updates

**New Classes:**
- `SmugglingOperation.java` — Smuggling mission: contraband type, quantity, route, customs evasion roll
- `ContrabandCommodity.java` — Illegal goods: drugs, weapons, slaves, stolen art with higher risk/reward
- `CustomsCheck.java` — Customs evasion system: bribe vs. run vs. fight, law level effects

**Service Updates:**
- `TradeManager.java` — Add smuggling operations as a special trade type with higher profit and risk
- `TradeController.java` — Add `/api/v1/traveller/trade/smuggling` endpoints

**Rules Reference:**
- B07 Merchant Prince smuggling rules
- Law level affects customs frequency and severity
- Contraband sells for 2-5x legal commodity prices

---

### ✓ Step 3: Implement Alien Race Mechanics (S03)

**Scope:** ~250 lines across 3 new files + 1 service update

**New Classes:**
- `AlienRace.java` — Enum: Human, Aslan, Vargr, Insectoid, Melniki, Tiltean, Zhodani, Loxi
- `AlienRaceTrait.java` — Race-specific bonuses/penalties: combat modifiers, skill bonuses, trade effects
- `AlienEncounter.java` — Random alien encounter system for adventures

**Service Updates:**
- `TravellerCharacter.java` — Add alienRace field, apply race traits to abilities and skills
- `TravellerAdventureService.java` — Generate alien NPCs and encounters

**Rules Reference:**
- S03 Spinward Marches alien race descriptions
- Aslan: +1 melee combat, -1 social with humans
- Vargr: +1 navigation, pack tactics bonus
- Insectoid: hive mind communication, chitin armor bonus

---

### Step 4: Add UI for all three systems

**Scope:** ~200 lines in existing HTML/JS files

**Updates:**
- `game.html` — Add tabs for Psionics, Smuggling, and Alien Races in the trade modal or a new Traveller modal
- `trade-ui.js` or new `traveller-ui.js` — Display psionic abilities, smuggling operations, alien encounter logs
- `game-api.js` — Add API calls for new endpoints

---

### Step 5: Test all endpoints and UI

**Scope:** Verify all new endpoints return 200 OK, UI renders correctly

**Verification:**
- All new REST endpoints return valid JSON
- UI displays psionic abilities, smuggling ops, and alien encounters
- Build compiles successfully

---

## Estimated Total Effort

| Step | Files | Lines | Complexity |
|------|-------|-------|------------|
| Psionics | 4 new + 2 updated | ~300 | Medium |
| Smuggling | 3 new + 2 updated | ~200 | Medium |
| Alien Races | 3 new + 2 updated | ~250 | Medium |
| UI | 3 updated | ~200 | Low |
| Testing | — | — | Low |
| **Total** | **10 new + 6 updated** | **~950** | **Medium** |
