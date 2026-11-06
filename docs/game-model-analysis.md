# DoraMCU Game Model Analysis

## Executive Summary

The DoraMCU game model consists of **~110 Java files totaling ~28,000 lines** across the `hex` package. While the core architecture (Game → Managers → Entities) is well-designed, there are significant issues with code duplication, class bloat, split responsibilities, and unused code that degrade maintainability.

---

## Architecture Overview

### Package Structure

| Package | Files | Lines | Purpose |
|---------|-------|-------|---------|
| `hex` (root) | ~60 | ~18,000 | Core domain entities, enums, name databases |
| `hex.gui` | ~43 | ~8,300 | Java desktop UI (deprecated) |
| `hex.manager` | ~7 | ~1,400 | Game managers (Turn, Resource, Fleet, Combat, Galaxy, Planet) |
| `hex.player` | ~2 | ~540 | Player game state, ship orders |

### Core Architecture

```
Game (959 lines) — Central coordinator
├── Implements GameContext interface (148 lines)
├── Holds all entity collections (players, planets, fleets, ships, galaxies, battles)
├── Delegates to 6 managers:
│   ├── TurnManager (210 lines) — Turn progression, resource production, ship construction
│   ├── ResourceManager (213 lines) — Ship affordability, build queuing
│   ├── FleetManager (191 lines) — Fleet creation, ship factories
│   ├── CombatResolver (203 lines) — Battle creation and resolution
│   ├── GalaxyManager (196 lines) — Galaxy and solar system management
│   └── PlanetManager (242 lines) — Planet creation, home system setup
└── Provides ID generation, serialization, empire integrity
```

### Entity Hierarchy

```
Galaxy (1,754 lines)
├── Sector (inner class, 207 lines) — 10x10 grid
│   └── SubSector (inner class) — 10x10 hex grid
│       └── SolarSystem (446 lines) — 3D hexmap
│           └── SolarSystemEntity (1,247 lines) — Celestial body container
│               ├── Star (1,005 lines)
│               ├── Planet (1,264 lines) — 3D hex sphere + 4X stats
│               ├── Asteroid (524 lines)
│               ├── AsteroidBelt (389 lines)
│               ├── GasGiant (460 lines)
│               ├── Moon (602 lines)
│               ├── Comet (528 lines)
│               ├── BlackHole (610 lines)
│               ├── Nebula (580 lines)
│               └── SpaceStation (1,467 lines)
└── Hyperlanes connecting systems

Player (TWO CLASSES — see Issue #1)
├── hex.Player (328 lines) — Identity: email, PIN, government, ethos
└── hex.player.Player (455 lines) — Game state: resources, planets, fleets

Ship (1,234 lines)
├── ShipType enum (202 lines) — FRIGATE, DESTROYER, CRUISER, etc.
├── ShipComponent (231 lines) — Weapons, shields, hull reinforcement
├── HullModule (784 lines) — 3D M3space grid
└── HullShape (171 lines) — CUBE, SPHERE, CYLINDER, etc.

Fleet (858 lines) — Group of ships with combat simulation
Battle (756 lines) — Structured battle lifecycle with state machine
```

---

## Top 15 Issues

### Issue 1: Duplicate Player Classes
**Severity: Critical**

Two unrelated `Player` classes exist:
- `hex.Player` (328 lines) — Identity: email, login PIN, government type, ethos
- `hex.player.Player` (455 lines) — Game state: resources, planets, fleets, ship queue

These classes are never linked. `Game` uses `hex.player.Player`, while `Galaxy` and web services use `hex.Player`. A player's identity and game state are completely disconnected.

**Impact:** Data inconsistency, confusion, impossible to correlate a logged-in user with their game state.

### Issue 2: Galaxy.java is a Duplicate Game Controller
**Severity: Critical**

`Galaxy.java` (1,754 lines) contains its own player management, turn tracking, planet/fleet/ship/battle collections, and resource stockpiles — duplicating everything in `Game.java`. The Javadoc even says "This class also serves as the central game controller."

**Impact:** Two competing centers of truth. Code that uses Galaxy directly bypasses Game's managers, leading to inconsistent state.

### Issue 3: Planet.java SRP Violation (1,264 lines)
**Severity: High**

Planet.java mixes 5 distinct responsibilities:
1. **3D hex geometry** — Hex3DMap sphere generation, layers (core, mantle, crust, atmosphere, moon)
2. **4X game statistics** — Resource production, population stats, strategic deposits
3. **Empire ownership** — Owner empire ID, economic level, capital flag
4. **Fleet management** — Stationed fleets, defense bonuses, repair
5. **Starting facilities** — Facility bonuses, ship build time reduction

**Impact:** Hard to test, modify, or reuse any one aspect without affecting others.

### Issue 4: Ship.java SRP Violation (1,234 lines)
**Severity: High**

Ship.java mixes 5 distinct responsibilities:
1. **Component management** — Add/remove ShipComponents, slot tracking
2. **3D hull module grid** — HullModule list, M3space component placement
3. **Combat stats** — Damage, shields, hull, speed, evasion calculations
4. **Resource costs** — Mineral/energy cost, strategic resource requirements
5. **Damage and repair** — Take damage, repair hull, recharge shields

**Impact:** Same as Planet — hard to modify one aspect without risk to others.

### Issue 5: SpaceStation.java is Massive (1,467 lines)
**Severity: High**

SpaceStation.java is the second-largest file after Galaxy. It likely combines station structure, defense systems, production capabilities, and docking management.

**Impact:** Same SRP issues as Planet and Ship.

### Issue 6: SolarSystemEntity.java is Massive (1,247 lines)
**Severity: High**

SolarSystemEntity.java is the third-largest file. It likely combines celestial body container, orbital mechanics, resource generation, and interaction logic.

**Impact:** Same SRP issues.

### Issue 7: Fleet.engage() Duplicates Battle Logic
**Severity: Medium**

`Fleet.engage(Fleet)` (lines 720-766) implements its own combat simulation with fleet copies, damage distribution, and winner determination. This duplicates the logic in `Battle.fightRound()` and `Battle.fightToCompletion()`.

**Impact:** Two combat systems with potentially different results. Bug fixes must be applied in two places.

### Issue 8: GalaxyGenerator.java is Large (992 lines)
**Severity: Medium**

GalaxyGenerator handles galaxy generation with AI empires, planet placement, system creation, and hyperlane generation. At 992 lines, it likely mixes generation strategy, randomization, and data population.

**Impact:** Hard to test different generation strategies independently.

### Issue 9: HullModule.java is Large (784 lines)
**Severity: Medium**

HullModule manages a 10x10x10 3D grid of M3space cells, shape generation, material assignment, and component placement. At 784 lines, it likely mixes grid management, shape algorithms, and material logic.

**Impact:** Hard to test shape algorithms independently from grid management.

### Issue 10: Name Database Files are Bloated
**Severity: Medium**

| File | Lines | Content |
|------|-------|---------|
| FleetNameDatabase.java | 765 | Static fleet name lists |
| PlanetNameDatabase.java | 582 | Static planet name lists |
| ShipNameDatabase.java | 582 | Static ship name lists |
| StarNameDatabase.java | 470 | Static star name lists |
| GalaxyNameDatabase.java | 341 | Static galaxy name lists |

Total: **2,740 lines** of static string arrays that could be externalized to data files.

**Impact:** Wasted code lines, recompilation needed for name changes, no way to customize without code changes.

### Issue 11: Celestial Body Classes are Large and Similar
**Severity: Medium**

| File | Lines |
|------|-------|
| Star.java | 1,005 |
| Moon.java | 602 |
| BlackHole.java | 610 |
| Nebula.java | 580 |
| Comet.java | 528 |
| Asteroid.java | 524 |
| AsteroidBelt.java | 389 |
| GasGiant.java | 460 |

Total: **4,698 lines** for celestial bodies that likely share significant structure (position, mass, temperature, effects). They could share a common base class or interface.

**Impact:** Code duplication, inconsistent APIs, hard to add new celestial body types.

### Issue 12: ShipDesignLibrary.java is Large (574 lines)
**Severity: Medium**

ShipDesignLibrary likely contains pre-defined ship designs with components, costs, and stats. At 574 lines, it mixes design data with library management logic.

**Impact:** Hard to add new designs without modifying the class.

### Issue 13: SectorBuilder.java is Large (510 lines)
**Severity: Low**

SectorBuilder handles sector name generation, subsector naming, and grid coordinate conversion. At 510 lines, it likely contains large lookup tables for sector names.

**Impact:** Could be simplified with algorithmic name generation.

### Issue 14: StartingFacility.java is Large (198 lines)
**Severity: Low**

StartingFacility is an enum with 198 lines, suggesting complex bonus calculations and per-facility logic embedded in the enum.

**Impact:** Adding new facilities requires modifying the enum.

### Issue 15: Player Home Location Fields are Scattered
**Severity: Low**

`hex.player.Player` has 6 separate fields for home location:
- `homePlanetId`
- `homeSystemId`
- `homeSectorRow`, `homeSectorCol`
- `homeSubSectorRow`, `homeSubSectorCol`

Each has individual getters/setters, totaling ~120 lines of boilerplate.

**Impact:** Error-prone, hard to validate consistency, could be a single `HomeLocation` value object.

---

## File Size Distribution

| Size Range | File Count | Total Lines | Examples |
|------------|-----------|-------------|----------|
| 1,000+ lines | 8 | 10,584 | Galaxy, Planet, Ship, SpaceStation, SolarSystemEntity, Star |
| 500-999 lines | 12 | 6,890 | Fleet, Battle, HullModule, GalaxyGenerator, Moon, BlackHole |
| 200-499 lines | 30 | 6,200 | Managers, ViewProviders, HexMapPanel |
| <200 lines | 60 | 4,326 | Enums, utilities, small helpers |

**Top 6 files account for 38% of all model code.**

---

## Improvement Recommendations

### Priority 1: Merge Player Classes (Critical)

**Action:** Consolidate `hex.Player` and `hex.player.Player` into a single class that combines identity (email, PIN, government, ethos) with game state (resources, planets, fleets).

**Benefit:** Single source of truth for player data. Enables proper authentication-to-game-state linkage.

**Files affected:** `hex.Player`, `hex.player.Player`, all callers

### Priority 2: Remove Game Controller from Galaxy (Critical)

**Action:** Remove all player/turn/planet/fleet/ship/battle management from `Galaxy.java`. Galaxy should only manage the galaxy hierarchy (sectors, subsectors, solar systems, celestial bodies, hyperlanes). All game state management should go through `Game` and its managers.

**Benefit:** Single center of truth for game state. Galaxy becomes a pure domain model.

**Files affected:** `Galaxy.java`, all callers that use Galaxy for game state

### Priority 3: Split Planet.java (High)

**Action:** Extract into focused classes:
- `PlanetGeometry` — 3D hex sphere, layers, moon
- `Planet` — 4X stats, ownership, facilities (thin coordinator)
- `PlanetDefense` — Stationed fleets, defense bonuses

**Benefit:** Each class is independently testable and modifiable.

**Files affected:** `Planet.java`, new classes

### Priority 4: Split Ship.java (High)

**Action:** Extract into focused classes:
- `ShipHull` — Hull modules, M3space grid, component placement
- `Ship` — Components, combat stats, costs (thin coordinator)
- `ShipDamage` — Damage tracking, repair, shield recharge

**Benefit:** Same as Planet — independent testability.

**Files affected:** `Ship.java`, new classes

### Priority 5: Remove Fleet.engage() (Medium)

**Action:** Deprecate `Fleet.engage(Fleet)` and all its inner classes. All combat should go through `Battle` class and `CombatResolver` manager.

**Benefit:** Single combat system. Bug fixes applied once.

**Files affected:** `Fleet.java`, callers of `Fleet.engage()`

### Priority 6: Externalize Name Databases (Medium)

**Action:** Move name lists from Java classes to JSON/CSV data files. Load at runtime via a `NameDatabaseService`.

**Benefit:** ~2,740 lines of code replaced with data files. Customizable without recompilation.

**Files affected:** All 5 NameDatabase files, new service class

### Priority 7: Create CelestialBody Base Class (Medium)

**Action:** Extract common fields/methods from Star, Moon, BlackHole, Nebula, Comet, Asteroid, GasGiant into a `CelestialBody` abstract base class.

**Benefit:** Reduced duplication, consistent API, easier to add new body types.

**Files affected:** All celestial body classes, new base class

### Priority 8: Consolidate Player Home Location (Low)

**Action:** Create `HomeLocation` value object with planetId, systemId, sectorRow/Col, subSectorRow/Col. Replace 6 individual fields with 1 field.

**Benefit:** ~100 lines saved, atomic updates, easier validation.

**Files affected:** `hex.player.Player`, callers

---

## Summary of Impact

| Priority | Change | Effort | Impact | Lines Saved |
|----------|--------|--------|--------|-------------|
| 1 | Merge Player classes | High | Critical | ~100 |
| 2 | Remove Galaxy game controller | High | Critical | ~500 |
| 3 | Split Planet.java | High | High | ~300 |
| 4 | Split Ship.java | High | High | ~300 |
| 5 | Remove Fleet.engage() | Low | Medium | ~50 |
| 6 | Externalize name databases | Medium | Medium | ~2,740 |
| 7 | CelestialBody base class | Medium | Medium | ~500 |
| 8 | Consolidate home location | Low | Low | ~100 |

The highest-ROI improvements are **Priority 1** (fixes critical data inconsistency) and **Priority 6** (saves 2,740 lines with clear benefit). **Priority 2** is also critical for architectural correctness.
