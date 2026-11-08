---
sessionId: session-261107-061252-1bqi
---

# Requirements

### Overview & Goals

Integrate the Traveller RPG system features into the existing DoraMCU web UI as a **unified hybrid interface**. The UI currently supports the 4X empire strategy game (login → sessions → empire panel → galaxy map → fleet actions → combat). The Traveller backend integration (characters, ship designs, UWP worlds, trade, mercenaries, unified combat) is complete. This plan adds UI surfaces for all Traveller features without creating a separate "mode" — everything blends seamlessly.

### User Stories

- As a player, I want to see my TravellerCharacter commander in the empire panel so I understand my bonuses.
- As a player, I want to create and develop my character (careers, skills) so I can improve my empire.
- As a player, I want to design ships using the Traveller tonnage-based system so I have more customization.
- As a player, I want to see UWP world profiles when exploring systems so I understand planet properties.
- As a player, I want to trade commodities between worlds so I can profit from market differences.
- As a player, I want to see mercenary units and ground combat results so I can manage planetary defense.

### Scope

**In Scope:**
1. **Character & Commander UI** — Character creation, career progression, skill display, commander bonuses in empire panel
2. **Ship Design Editor** — Traveller-style tonnage-based ship design as an alternative to ShipType building
3. **World Details & Trade** — UWP profiles, trade codes, commodity trading in system/planet views
4. **Combat & Mercenary UI** — Ground combat display, mercenary unit management, unified combat results

**Out of Scope:**
- Separate "Traveller mode" toggle
- Mobile UI
- AI chat integration with Traveller features
- Voice or desktop client changes

### Functional Requirements

1. **Character Panel**: Display commander avatar, abilities (ST/DX/EN/IN/ED/SO), career, skills, and active bonuses
2. **Character Creation Modal**: Career selection, term simulation, skill acquisition, life events
3. **Ship Design Modal**: Tonnage allocation, drive selection (Jump/Fusion), cargo/crew, weapons, cost calculation
4. **System Detail Enhancement**: UWP display (Atmosphere, Gravity, Population, Starport, Government, Law), trade codes
5. **Trade Panel**: Commodity list, buy/sell interface, market conditions, profit calculation
6. **Mercenary Panel**: Unit list (Fire Team → Regiment), ticket contracts, ground combat results
7. **Unified Combat Display**: Show combat type (fleet/ship-to-ship/High Guard/ground) and appropriate results

### Non-Functional Requirements

- UI must remain performant with large galaxy states (summary mode already used)
- New features must work with existing REST + SSE communication pattern
- CSS must follow existing dark space theme (variables.css)
- JavaScript must follow existing IIFE module pattern (game-ui-core.js, game-ui-main.js)


# Technical Design

### Current Implementation

**UI Architecture:**
- `game.html` (1024 lines) — Single-page app with login, session selection, empire panel, galaxy map (4 views: sector cards → subsector cards → hex grid → system detail), action panel, event log, and modals (turn phases, build station/facility/fleet, battles)
- `game-ui-main.js` (5678 lines) — Monolithic IIFE handling all UI logic: auth, game state, galaxy map rendering, turn phases, fleet operations, combat, pipeline
- `game-ui-core.js` — Core utilities, state management (`GameUIState` object), DOM helpers
- `game-api.js` (372 lines) — REST API client (auth, sessions, game state, actions)
- `game-sse.js` — SSE event handler for real-time updates
- CSS: `game.css` (main styles), `variables.css` (theme variables), `login.css`

**Backend Architecture:**
- `GameController.java` — REST endpoints for game actions (move, build, combat, turn, stations, facilities, fleets, battles, systems)
- `GameSessionService.java` — Core service managing sessions, game state, and actions
- `GameStateResponse.java` — DTO with sessionId, turn, players, subSectors, pipeline, fleets, empire summary, battles
- Traveller controllers: `CharacterController`, `ShipDesignController`, `TradeController`, `NavalController`, `HighGuardController`, `SurvivalController`, `MeleeCombatController`, `FirearmsCombatController`, `EncumbranceController`, `JumpTravelController`

**Traveller Backend (already integrated):**
- `CharacterManager` — Character creation, careers, skills, bonuses
- `TravellerShipFactory` / `HighGuardShipFactory` — Ship design conversion
- `TradeManager` — Commodity trading, market conditions
- `GroundBattleResolver` — Mercenary/abstract battle
- `MercenaryIncomeManager` — Ticket contracts
- `WorldProfileAdapter` — UWP → Planet conversion

### Key Decisions

1. **Unified hybrid interface** — No separate mode toggle. Traveller features blend into existing panels: character in empire info, ship design in build modal, UWP in system detail, trade in a new right-panel section.
2. **Incremental modal-based UI** — Use the existing modal pattern (build station, build facility, create fleet) for new Traveller features (character creation, ship design, trade). This minimizes HTML changes.
3. **Extend GameStateResponse** — Add Traveller-specific fields (commander, trade opportunities, mercenary units) to the existing DTO rather than creating parallel endpoints.
4. **Split game-ui-main.js** — The 5678-line monolith will be extended with focused sub-modules (character-ui.js, ship-design-ui.js, trade-ui.js) following the existing pattern of game-ui-core.js and game-ui-galaxy-progress.js.
5. **Reuse existing CSS patterns** — New UI elements follow existing glass-morphism, dark theme, and icon-based design from game.css.

### Architecture Diagram

``` mermaid
graph LR
    U[game.html] -->|REST| GA[game-api.js]
    U -->|SSE| GS[game-sse.js]
    U -->|UI Logic| GM[game-ui-main.js]
    U -->|Character UI| CU[character-ui.js NEW]
    U -->|Ship Design UI| SU[ship-design-ui.js NEW]
    U -->|Trade UI| TU[trade-ui.js NEW]
    GA -->|REST| GC[GameController]
    GA -->|REST| CC[CharacterController]
    GA -->|REST| TC[TradeController]
    GA -->|REST| SDC[ShipDesignController]
    GC -->|Service| GSS[GameSessionService]
    CC -->|Service| CM[CharacterManager]
    TC -->|Service| TRM[TradeManager]
    SDC -->|Service| TSF[TravellerShipFactory]
    GSS -->|Domain| G[Game]
    G -->|Managers| EM[EmpireManager]
    G -->|Managers| FM[FleetManager]
    G -->|Managers| CR[CombatResolver]
```

### Proposed Changes

**Phase 1: Character & Commander UI**
- Add commander section to empire panel in `game.html` (abilities, career, skills, bonuses)
- Create character creation modal with career selection and term simulation
- Extend `game-api.js` with character endpoints (`getCharacter`, `createCharacter`, `advanceCareer`)
- Create `character-ui.js` module for character display and creation logic
- Extend `GameStateResponse` with `commander` field (TravellerCharacter summary)
- Update `GameSessionService.getGameState()` to include commander data

**Phase 2: Ship Design Editor**
- Replace/enhance build ship modal with Traveller ship design interface
- Add tonnage allocation, drive selection, cargo/crew, weapons panels
- Extend `game-api.js` with ship design endpoints (`createDesign`, `convertToShip`)
- Create `ship-design-ui.js` module
- Add `ShipDesignDto` to DTO layer
- Update `GameController` with design-based ship building endpoint

**Phase 3: World Details & Trade**
- Enhance system detail view with UWP display (atmosphere, gravity, population, starport, government, law)
- Add trade codes to planet cards
- Create trade modal with commodity list, buy/sell, market conditions
- Extend `game-api.js` with trade endpoints (`getMarket`, `buyCommodity`, `sellCommodity`)
- Create `trade-ui.js` module
- Add trade data to `GameStateResponse` or system detail endpoint

**Phase 4: Combat & Mercenary UI**
- Add mercenary unit section to empire panel (units, tickets, ground strength)
- Enhance battle status modal to show combat type and ground combat results
- Extend `game-api.js` with mercenary endpoints (`getUnits`, `createUnit`, `startTicket`)
- Update battle display to differentiate fleet vs ground combat
- Add mercenary data to `EmpireSummary` in `GameStateResponse`

### Data Models / Contracts

**New DTOs:**
- `CommanderSummaryDto` — character name, career, skills, abilities, active bonuses
- `ShipDesignDto` — tonnage, drives, cargo, crew, weapons, cost
- `TradeOpportunityDto` — commodity, buy price, sell price, market condition
- `MercenarySummaryDto` — unit list, ticket status, ground strength

**Extended GameStateResponse fields:**
- `CommanderSummaryDto commander`
- `List<TradeOpportunityDto> tradeOpportunities`
- `MercenarySummaryDto mercenarySummary`

**New API Endpoints:**
- `GET /api/v1/characters/{playerId}` — Get character
- `POST /api/v1/characters` — Create character
- `POST /api/v1/characters/{id}/career` — Advance career term
- `POST /api/v1/ship-designs` — Create ship design
- `POST /api/v1/ship-designs/{id}/build` — Convert design to ship
- `GET /api/v1/sessions/{sessionId}/trade/market` — Get market for system
- `POST /api/v1/sessions/{sessionId}/trade/buy` — Buy commodity
- `POST /api/v1/sessions/{sessionId}/trade/sell` — Sell commodity
- `GET /api/v1/mercenaries/{playerId}` — Get mercenary units
- `POST /api/v1/mercenaries` — Create mercenary unit
- `POST /api/v1/mercenaries/ticket` — Start mercenary ticket

### File Structure

**New Files:**
```
src/main/resources/static/js/
├── character-ui.js          (NEW) — Character display and creation
├── ship-design-ui.js        (NEW) — Ship design editor
└── trade-ui.js              (NEW) — Trade interface

src/main/java/com/yisy/modules/doramcu/dto/
├── CommanderSummaryDto.java  (NEW)
├── ShipDesignDto.java        (NEW)
├── TradeOpportunityDto.java  (NEW)
└── MercenarySummaryDto.java  (NEW)
```

**Modified Files:**
```
src/main/resources/static/
├── game.html                 (MODIFIED) — Add commander section, character modal, trade modal, ship design modal, mercenary section
├── css/game.css              (MODIFIED) — Add styles for new sections
├── js/game-api.js            (MODIFIED) — Add Traveller API methods
├── js/game-ui-main.js        (MODIFIED) — Wire new modules, update empire panel, system detail
└── js/game-ui-core.js        (MODIFIED) — Add Traveller state to GameUIState

src/main/java/com/yisy/modules/doramcu/
├── dto/GameStateResponse.java       (MODIFIED) — Add commander, trade, mercenary fields
├── dto/EmpireSummary.java           (MODIFIED) — Add mercenary summary
├── controller/GameController.java   (MODIFIED) — Add design-based build, trade endpoints
├── service/GameSessionService.java  (MODIFIED) — Populate new DTO fields
└── controller/TradeController.java  (MODIFIED) — Add session-scoped trade endpoints
```

### Risks

1. **game-ui-main.js bloat** — Adding more to the 5678-line monolith worsens maintainability. Mitigation: New features go into separate JS modules (character-ui.js, ship-design-ui.js, trade-ui.js) that register with game-ui-main.js.
2. **GameStateResponse size** — Adding Traveller fields increases response size. Mitigation: Use summary DTOs and lazy-load details via dedicated endpoints.
3. **CSS complexity** — New UI elements must match the existing dark space theme. Mitigation: Reuse existing CSS variables and component patterns.
4. **Backend API gaps** — Some Traveller controllers may not be session-aware. Mitigation: Add session-scoped endpoints to GameController or create a TravellerSessionController.


### ✓ Step 1: Create New DTOs

### ✓ Step 2: Extend GameStateResponse and EmpireSummary

### ✓ Step 3: Add Backend API Endpoints

### ✓ Step 4: Extend game-api.js with Traveller API methods

### ✓ Step 5: Add Commander Section to game.html

### ✓ Step 6: Create character-ui.js module

### ✓ Step 7: Wire Character UI in game-ui-main.js and game-ui-core.js

### ✓ Step 8: Add Ship Design Modal to game.html

### ✓ Step 9: Create ship-design-ui.js module

### ✓ Step 10: Wire Ship Design in game-ui-main.js

### ✓ Step 11: Add UWP and Trade UI to game.html

### ✓ Step 12: Create trade-ui.js module

### ✓ Step 13: Wire Trade UI in game-ui-main.js

### ✓ Step 14: Add Mercenary Section to game.html

### ✓ Step 15: Wire Mercenary UI in game-ui-main.js

### ✓ Step 16: Add CSS styles for all new components

### ✓ Step 17: Compile and verify

### ✓ Step 18: Connect UI triggers

### ✓ Step 19: Populate GameStateResponse fields

# Testing

### Validation Approach

After each phase, verify:
1. Compilation passes: `./gradlew.bat compileJava`
2. Test suite passes: `./gradlew.bat test`
3. Application runs: `./gradlew.bat bootRun --no-daemon`
4. Web UI loads and displays new Traveller features
5. No regression in existing 4X game features

### Key Scenarios

**Phase 1 (Character UI):**
- Login → empire panel shows commander section with abilities and career
- Character creation modal opens, career selection works, skills display correctly
- Commander bonuses appear in empire operations (combat DM, trade profit%)
- SSE updates reflect character changes

**Phase 2 (Ship Design):**
- Build ship button opens Traveller design editor
- Tonnage allocation, drive selection, and weapon placement work
- Converting design to ship adds it to fleet
- Ship appears in fleet list with correct stats

**Phase 3 (World & Trade):**
- System detail view shows UWP profile (atmosphere, gravity, etc.)
- Planet cards display trade codes
- Trade modal shows commodities with buy/sell prices
- Buying/selling updates resources and market conditions

**Phase 4 (Combat & Mercenary):**
- Empire panel shows mercenary units and ground strength
- Battle modal differentiates fleet vs ground combat
- Mercenary ticket contracts display income
- Ground combat results show unit casualties

### Edge Cases

- Player without a TravellerCharacter → show "Create Commander" prompt
- Empty trade market → show "No trade opportunities" state
- No mercenary units → show "Recruit Units" button
- Combat with mixed entity types → show appropriate combat type indicator
- Large galaxy with many worlds → UWP data loads lazily per system
