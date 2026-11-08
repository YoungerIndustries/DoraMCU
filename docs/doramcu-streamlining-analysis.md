# DoraMCU 4X Game — Comprehensive Streamlining Analysis

**Date:** 2026-11-07
**Scope:** Full codebase analysis using Traveller RPB database as reference point
**Total Java Files:** ~567 across 17 packages

---

## Executive Summary

The DoraMCU project contains **three parallel game systems** that share significant conceptual overlap but maintain separate implementations:

| System | Package | Files | Purpose |
|--------|---------|-------|---------|
| **Traveller RPB** | `traveller` | 128 | Classic Traveller rules database + character/career/combat systems |
| **Captain's Log** | `captainslog` | 80 | Star Trek solo RPG with 2d20 mechanics, missions, lifepath |
| **4X Game Engine** | `hex` | 139 | Turn-based empire strategy with galaxies, fleets, resources |
| **Core Models** | `model` | 21 | JPA entities for persistence (duplicates hex entities) |
| **Integration** | `unified` | 18 | Adapters bridging the three systems |
| **Shared** | `shared` | 8 | Universal character identity across systems |
| **Services** | `service` | 26 | Top-level orchestration services |

**Key Finding:** The project has ~400 lines of near-duplicate infrastructure code, 8 duplicate entity types, and scattered utilities that could be consolidated into shared abstractions.

---

## 1. Rules Database Duplication (High Impact)

### Problem

Two nearly identical RPB (Rules Per Book) database systems exist:

| Feature | Traveller | Captain's Log |
|---------|-----------|---------------|
| Entry Entity | `TravellerRulesEntry` (149 lines) | `RulesEntry` (134 lines) |
| Table Entity | `TravellerRulesTable` (159 lines) | `RulesTable` (147 lines) |
| Repository | `TravellerRulesEntryRepository` | `RulesEntryRepository` |
| Service | `TravellerRulesService` | `RulesService` |
| Controller | `TravellerRulesController` | (embedded in CaptainLogController) |
| DB Table | `ct_rules_entries` | `cl_rules_entries` |
| DB Table | `ct_rules_tables` | `cl_rules_tables` |

The only real difference is that `TravellerRulesEntry` has a `source` field (for multi-book sourcing) and uses different index names. The schemas are otherwise identical.

### Opportunity

**Consolidate into a single generic rules database:**

```
RulesEntry (shared)
  - id, title, source, chapter, category, page, text, notes, active
  - gameSystem enum: TRAVELLER, CAPTAINS_LOG, UNIVERSAL
```

**Estimated savings:** ~250 lines of duplicate entity/repository code + simplified service layer.

---

## 2. Entity Model Duplication Across Packages (Critical Impact)

### Problem

The same domain entities exist in 2-3 different packages with separate implementations:

| Entity | `model` | `hex` | `traveller` | Notes |
|--------|---------|-------|-------------|-------|
| Galaxy | ✓ (15 lines) | ✓ (1,754 lines) | — | hex.Galaxy is a game controller, not just a model |
| Planet | ✓ (20 lines) | ✓ (1,264 lines) | — | hex.Planet has 5 responsibilities |
| Star | ✓ (16 lines) | ✓ (1,005 lines) | — | hex.Star is massive |
| Sector | ✓ (15 lines) | ✓ (inner class) | — | hex.Sector is inner class of Galaxy |
| SolarSystem | ✓ (20 lines) | ✓ (446 lines) | — | — |
| SpaceStation | ✓ (10 lines) | ✓ (1,467 lines) | ✓ (34 lines) | 3 versions! |
| Player | ✓ (32 lines) | ✓ (328 lines) | — | hex.Player is identity only |
| Ship | — | ✓ (1,234 lines) | ✓ (ShipDesign, ShipTemplate) | Different abstractions |
| Asteroid | ✓ | ✓ (524 lines) | — | — |
| Moon | ✓ | ✓ (602 lines) | — | — |
| Comet | ✓ | ✓ (528 lines) | — | — |
| BlackHole | ✓ | ✓ (610 lines) | — | — |
| GasGiant | ✓ | ✓ (460 lines) | — | — |
| Nebula | ✓ | ✓ (580 lines) | — | — |

### Root Cause

The `model` package contains thin JPA entities for database persistence, while `hex` contains rich domain objects with game logic. They were never unified — the `model` entities are barely used, and `hex` entities are not JPA-persisted.

### Opportunity

**Option A — Make `hex` entities JPA-persisted directly:**
Eliminate the `model` package entirely by adding JPA annotations to the `hex` entities. The `model` entities are too thin to justify a separate layer.

**Option B — Use `hex` as DTOs, `model` as JPA:**
Keep `model` as the persistence layer and make `hex` entities in-memory game state that maps to/from `model`.

**Estimated savings:** 21 files in `model` package + repository layer (~15 files) can be merged or eliminated.

---

## 3. Player Class Split (Critical — Already Identified)

### Problem

Three Player-related classes exist:
- `model.Player` (32 lines) — thin JPA entity
- `hex.Player` (328 lines) — identity: email, PIN, government, ethos
- `hex.player.Player` (455 lines) — game state: resources, planets, fleets

These are never properly linked. A logged-in user cannot be correlated with their game state.

### Opportunity

Consolidate into a single Player class that combines identity with game state, as recommended in the prior `game-model-analysis.md`.

---

## 4. Galaxy.java as Duplicate Game Controller (Critical — Already Identified)

### Problem

`hex.Galaxy` (1,754 lines) contains its own player management, turn tracking, planet/fleet/ship/battle collections, and resource stockpiles — duplicating everything in `hex.Game` (1,193 lines).

### Opportunity

Remove all game controller logic from Galaxy. Galaxy should only manage the galaxy hierarchy (sectors, subsectors, solar systems, celestial bodies, hyperlanes).

---

## 5. Dice Rolling Scattered Across 15+ Classes (Medium Impact)

### Problem

Dice rolling logic is duplicated across the codebase:

| Location | Method | Usage |
|----------|--------|-------|
| `captainslog/service/DiceService` | Full service class | Only used by Captain's Log |
| `traveller/AbstractBattle` | `rollD6()` | Mercenary combat |
| `traveller/CustomsCheck` | `roll2D6()` | Customs inspections |
| `traveller/FirearmsCombat` | `roll2D6()` | Firearms combat |
| `traveller/HighGuardShipCombat` | `roll2D6()` | Naval combat |
| `traveller/MeleeCombat` | `roll2D6()` | Melee combat |
| `traveller/MercenaryCareer` | `rollD6()`, `roll2D6()` | Career progression |
| `traveller/NavalBattle` | `roll2D6()` | Naval battles |
| `traveller/ShipToShipBattle` | `roll2D6()` | Ship combat |
| `traveller/TravellerCharacter` | `roll2D6()` | Ability rolling |
| `traveller/WorldGenerationService` | `roll2D6()` | World generation |
| `shared/service/SharedCharacterService` | `roll2D6()` | Character creation |

### Opportunity

Create a single `DiceUtil` class in `shared` or `util`:

```java
public final class DiceUtil {
    public static int rollD6() { ... }
    public static int roll2D6() { ... }
    public static int rollD20() { ... }
    public static int rollNdX(int count, int sides) { ... }
    public static List<Integer> rollMultiple(int count, int sides) { ... }
}
```

**Estimated savings:** ~50 lines of duplicated utility code + consistent behavior.

---

## 6. Name Database Externalization (Medium Impact — Already Identified)

### Problem

5 name database files contain 2,740 lines of static string arrays:

| File | Lines |
|------|-------|
| FleetNameDatabase | 765 |
| PlanetNameDatabase | 582 |
| ShipNameDatabase | 582 |
| StarNameDatabase | 470 |
| GalaxyNameDatabase | 341 |

### Opportunity

Move to JSON/CSV data files loaded at runtime. This also applies to Captain's Log name databases (`CaptainsLogGalaxyNameDatabase`, `CaptainsLogPlanetNameDatabase`, `CaptainsLogStarNameDatabase`).

**Estimated savings:** ~3,500+ lines of code replaced with data files.

---

## 7. Celestial Body Hierarchy (Medium Impact — Already Identified)

### Problem

8 celestial body classes (4,698 lines total) share significant structure but lack a common base:

| Class | Lines | Shared with others |
|-------|-------|--------------------|
| Star | 1,005 | Position, mass, temperature, effects |
| Moon | 602 | Position, mass, orbital mechanics |
| BlackHole | 610 | Position, mass, gravitational effects |
| Nebula | 580 | Position, volume, effects |
| Comet | 528 | Position, orbit, composition |
| Asteroid | 524 | Position, mass, composition |
| AsteroidBelt | 389 | Position, range, composition |
| GasGiant | 460 | Position, mass, atmosphere |

### Opportunity

Create `CelestialBody` abstract base class with common fields (position, mass, temperature, effects, name). Each specific type adds only its unique behavior.

**Estimated savings:** ~500 lines through shared base class.

---

## 8. SRP Violations in Large Classes (High Impact — Already Identified)

### Problem

Several classes violate Single Responsibility Principle:

| Class | Lines | Responsibilities |
|-------|-------|-----------------|
| `hex.Planet` | 1,264 | 3D geometry, 4X stats, ownership, fleet mgmt, facilities |
| `hex.Ship` | 1,234 | Components, hull grid, combat, costs, damage |
| `hex.SpaceStation` | 1,467 | Structure, defense, production, docking |
| `hex.SolarSystemEntity` | 1,247 | Container, orbital mechanics, resources, interactions |
| `hex.Galaxy` | 1,754 | Hierarchy + game controller |

### Partial Progress

The hex package has already started splitting some entities:
- `PlanetGeometry`, `PlanetDefense`, `PlanetFacility` (extracted from Planet)
- `ShipHull`, `ShipCombatStats`, `ShipResourceCosts` (extracted from Ship)
- `SpaceStationCombatStats`, `SpaceStationDefense`, `SpaceStationEconomy` (extracted from SpaceStation)
- `StarGeometry` (extracted from Star)

### Opportunity

Complete the partial splits. The extracted classes exist but the parent classes still contain the original logic. The extracted classes need to be integrated as delegates.

---

## 9. Combat System Duplication (Medium Impact)

### Problem

Multiple combat systems exist with overlapping functionality:

| System | Package | Scope |
|--------|---------|-------|
| `traveller.FirearmsCombat` | traveller | Character firearms (2D6) |
| `traveller.MeleeCombat` | traveller | Character melee (2D6) |
| `traveller.ShipToShipBattle` | traveller | Classic Traveller ship combat |
| `traveller.HighGuardShipCombat` | traveller | High Guard advanced ship combat |
| `traveller.NavalBattle` | traveller | Fleet-level naval combat |
| `traveller.AbstractBattle` | traveller | Mercenary ground combat |
| `captainslog/service/CaptainLogCombatService` | captainslog | 2d20 task-based combat |
| `captainslog/service/StarshipCombatService` | captainslog | 2d20 ship combat |
| `hex.Battle` | hex | 4X fleet battles |
| `hex.Fleet.engage()` | hex | Inline fleet combat (duplicates Battle) |
| `hex.manager.CombatResolver` | hex | Battle orchestration |
| `hex.UnifiedBattle` | hex | Cross-system battle adapter |
| `service.FirearmsCombatService` | service | Wrapper for traveller firearms |
| `service.MeleeCombatService` | service | Wrapper for traveller melee |
| `service.BattleService` | service | Wrapper for hex battles |

### Opportunity

- Remove `Fleet.engage()` — all combat should go through `Battle` class
- The `service` wrappers (FirearmsCombatService, MeleeCombatService, BattleService) are thin delegators — consider inlining
- `UnifiedBattle` in hex is an attempt to bridge systems — evaluate if it's actually used

---

## 10. Service Layer Duplication (Medium Impact)

### Problem

Multiple services handle similar concerns:

| Concern | Services |
|---------|----------|
| Galaxy | `GalaxyService`, `GalaxyCreationService`, `GalaxyProgressService`, `hex.manager.GalaxyManager` |
| Planet | `hex.manager.PlanetManager` |
| Trade | `TradeService`, `hex.manager.TradeManager`, `hex.manager.TradeRouteNetwork` |
| Character | `CharacterService`, `hex.manager.CharacterManager`, `SharedCharacterService` |
| Fleet | `hex.manager.FleetManager` |
| Combat | `BattleService`, `FirearmsCombatService`, `MeleeCombatService`, `hex.manager.CombatResolver` |

### Opportunity

Clarify the service layer boundaries:
- `service` package = Spring-managed web-facing services
- `hex.manager` = in-memory game state managers
- Eliminate thin wrapper services that only delegate

---

## 11. Integration Layer Complexity (Low-Medium Impact)

### Current Architecture

```
Traveller (128 files) ←→ unified (18 files) ←→ hex 4X (139 files)
                              ↑
                    Captain's Log (80 files)
                              ↑
                      shared (8 files)
```

The `unified` package contains:
- `TravellerTo4XIntegrationService` (510 lines) — bridges Traveller characters to 4X
- `TravellerToCaptainsLogConverter` (371 lines) — maps Traveller concepts to Captain's Log
- `TravellerMissionAdapter` — adapts Traveller missions
- `WorldProfileAdapter` — adapts world profiles
- `UnifiedCharacterCreationService` — creates characters across systems
- `UnifiedCharacterLoginService` — login across systems
- `GlobalClockService` — shared game time
- `GameEngine` interface — abstracts game operations

### Opportunity

The integration layer is well-designed but could be simplified:
- `TravellerToCaptainsLogConverter` is a large mapping class (371 lines) — consider using MapStruct or a generic mapping framework
- The `shared` package's `SharedCharacter` is a good start at universal identity — expand it to cover ships and worlds

---

## 12. Captain's Log Galaxy Generators in hex Package (Low Impact)

### Problem

The `hex` package contains Captain's Log-specific galaxy generators:
- `CaptainsLogGalaxyGenerator`
- `CaptainsLogGalaxyNameDatabase`
- `CaptainsLogPlanetNameDatabase`
- `CaptainsLogSectorBuilder`
- `CaptainsLogStarNameDatabase`
- `CaptainsLogTravelManager`

These are Captain's Log-specific but live in the hex (4X) package.

### Opportunity

Move these to the `captainslog` package or create a `shared/generation` package for cross-system galaxy generation.

---

## 13. GUI Package (Deprecated) — Cleanup Opportunity

### Problem

The `hex.gui` subpackage contains ~43 files (~8,300 lines) of Java desktop UI code that is deprecated.

### Opportunity

Remove or archive the GUI package. This is ~8,300 lines of dead code.

---

## Consolidation Priority Matrix

| Priority | Change | Effort | Lines Saved | Risk |
|----------|--------|--------|-------------|------|
| **P0** | Merge Player classes | High | ~100 | Critical bug fix |
| **P0** | Remove Galaxy game controller | High | ~500 | Critical architecture fix |
| **P1** | Consolidate RPB databases | Medium | ~250 | Medium — schema migration |
| **P1** | Complete partial SRP splits | High | ~300 | Medium — existing extracted classes |
| **P1** | Remove hex.gui (deprecated) | Low | ~8,300 | Low — dead code |
| **P2** | Shared DiceUtil | Low | ~50 | Low — pure utility |
| **P2** | Externalize name databases | Medium | ~3,500 | Low — data externalization |
| **P2** | CelestialBody base class | Medium | ~500 | Medium — inheritance change |
| **P2** | Remove Fleet.engage() | Low | ~50 | Low — single combat path |
| **P3** | Eliminate model package duplication | High | ~35 | High — persistence refactor |
| **P3** | Move CL generators from hex | Low | ~0 | Low — reorganization |
| **P3** | Thin service wrapper cleanup | Medium | ~100 | Medium — API changes |

### Total Estimated Lines Saved: ~13,000+ (from ~567 files)

---

## Recommended Phased Approach

### Phase 1: Quick Wins (Low Risk)
1. Create shared `DiceUtil` utility
2. Remove `Fleet.engage()` duplicate combat
3. Archive `hex.gui` deprecated code
4. Move Captain's Log generators out of `hex`

### Phase 2: Infrastructure Consolidation (Medium Risk)
5. Consolidate RPB databases into single generic system
6. Externalize all name databases to data files
7. Create `CelestialBody` base class
8. Complete partial SRP splits (Planet, Ship, SpaceStation)

### Phase 3: Architecture Fixes (High Risk)
9. Merge Player classes
10. Remove game controller from Galaxy
11. Resolve model/hex entity duplication
12. Clean up thin service wrappers

---

## Traveller RPB Database — Specific Observations

The Traveller RPB database (`traveller` package, 128 files) is the most mature and well-structured system:

**Strengths:**
- Clean separation of rules data (`TravellerRulesEntry`, `TravellerRulesTable`) from game logic
- Comprehensive coverage of Classic Traveller rules (7 source books)
- Well-organized domain model (combat, careers, ship design, trade, world generation)
- Good use of enums for domain concepts (Career, Skill, TradeCode, etc.)

**Areas for Improvement:**
- The RPB schema should be the template for a unified rules database
- `TravellerRulesService` could be generalized to serve any game system
- The rules extraction docs (`docs/traveller-rules-extraction/`) are well-organized and should be the model for Captain's Log rules extraction
- Consider adding `Mongoose Traveller 2E` rules as a second "source" in the same database, using the `source` field to distinguish editions

**Cross-System Value:**
The Traveller RPB database's `category` field system (DICE_MECHANICS, MELEE_COMBAT, SHIP_DESIGN, etc.) could serve as the universal taxonomy for all game rules, making it easier to query rules across systems.
