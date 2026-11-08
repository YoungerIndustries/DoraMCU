# Traveller RPG — Next Implementation Priorities

## Executive Summary

After analyzing the complete codebase (118 Traveller Java classes, 27 controllers, 116 rules DB entries from 12 PDFs), this document identifies **what needs to be implemented next** to make the Traveller RPG module fully playable and integrated with the DoraMCU 4X game.

**Current State:**
- ✅ Rules database: 116 entries, 137 tables from 12 PDFs — **complete**
- ✅ Trade system backend: TradeManager, TradeSimulator, WorldEconomy — **complete**
- ✅ Credit exchange system: bidirectional credit ↔ resource conversion — **complete**
- ✅ Trade career integration: Merchant career bonuses — **complete**
- ✅ Trade route network: sector-wide routes with economic events — **complete**
- ✅ Adventure system: character travel, ship purchase, event witnessing — **complete**
- ✅ Trade UI: 5-tab panel in game.html — **complete**
- ❌ **Game loop integration**: Trade NOT processed each turn — **CRITICAL GAP**
- ❌ **Mercenary income**: NOT processed each turn — **CRITICAL GAP**
- ❌ **Character → Commander link**: No connection between Traveller characters and 4X commanders — **HIGH GAP**
- ❌ **Trade mission execution from UI**: UI shows data but can't execute missions — **HIGH GAP**
- ❌ **Adventure trade integration**: Adventures don't connect to trade system — **MEDIUM GAP**

---

## Critical Gaps (Block Playability)

### 1. Game Turn Loop — Trade Processing (CRITICAL)

**File:** `TurnManager.java` (lines 142-150)

**Problem:** `processTradeMarkets()` is an empty stub. It iterates over planets with UWP but does nothing:
```java
private void processTradeMarkets() {
    for (Planet planet : context.getPlanets().values()) {
        if (planet.hasUWP()) {
            // TradeManager will be accessed via Game if needed
            // For now, market fluctuation is handled by TradeManager directly
        }
    }
}
```

**Impact:** Trade markets never fluctuate. No auto-trade happens. No economic events are processed. The entire trade integration (CreditExchange, TradeCareer, TradeRouteNetwork) is dead code from the game loop's perspective.

**Fix Required:**
1. In `TurnManager.processTradeMarkets()`, cast `GameContext` to `Game` and call `game.getTradeManager().processEndOfTurn(pipelineManagers)`
2. This will: fluctuate markets, process economic events, build trade route network, and auto-trade for each empire

**Estimated Effort:** 15 lines of code in TurnManager.java

---

### 2. Game Turn Loop — Mercenary Income (CRITICAL)

**File:** `TurnManager.java` (lines 159-162)

**Problem:** `processMercenaryIncome()` is an empty stub:
```java
private void processMercenaryIncome() {
    // MercenaryIncomeManager is accessed via Game if needed
    // The manager processes contracts, resolves missions, and tracks income
}
```

**Impact:** Mercenary contracts never generate income. The MercenaryIncomeManager exists but is never called.

**Fix Required:**
1. In `TurnManager.processMercenaryIncome()`, cast `GameContext` to `Game` and call `game.getMercenaryIncomeManager().processEndOfTurn()`
2. Need to check what MercenaryIncomeManager.processEndOfTurn() does and ensure it's implemented

**Estimated Effort:** 10 lines of code + verify MercenaryIncomeManager

---

### 3. Character → Commander Link (HIGH)

**Problem:** Traveller characters exist independently from 4X game commanders. When a player creates a Traveller character and completes careers, that character has no connection to the Player/Commander in the 4X game.

**Current Flow:**
1. Player creates Traveller character via CharacterController
2. Character goes through careers via TourOfDutyService
3. Character starts adventure via TravellerAdventureService
4. **BUT**: The 4X Player has no link to the Traveller character
5. Trade career bonuses use `player.getCommanderBonus()` — but the bonus is never set from the character's Merchant career

**Impact:** Trade career bonuses are always 0%. The character's skills, age, and wealth don't affect the 4X gameplay.

**Fix Required:**
1. Add `travellerCharacterId` field to `Player` class
2. When character is assigned as commander, copy character's trade skills to player bonuses
3. In `TradeCareerService`, check if player has linked character and use character's Merchant tours for bonuses
4. Add endpoint to assign character as commander

**Estimated Effort:** New field in Player.java, updates to TradeCareerService.java, new endpoint in CharacterController.java

---

### 4. Trade Mission Execution from UI (HIGH)

**Problem:** The trade UI in game.html shows market prices, routes, career info, exchange data, and events — but the "Execute Trade Mission" button calls `GameApi.executeTradeMission()` which sends to `/api/v1/traveller/trade/mission`. The TradeController has this endpoint, but TradeService.executeTradeMission() needs an active game session with worlds.

**Current State:**
- TradeController has `/mission` endpoint ✅
- TradeService.executeTradeMission() exists ✅
- BUT: The endpoint requires a TradeRequest with worldId, destinationWorldId, commodity, quantity
- The UI doesn't collect these inputs — it just shows data

**Impact:** Players can see trade data but can't actually execute trades from the UI.

**Fix Required:**
1. Add trade mission form to the trade UI (source world, dest world, commodity, quantity)
2. Add "Execute" button that sends TradeRequest to `/api/v1/traveller/trade/mission`
3. Display trade results (profit, cost, revenue) in the UI
4. Update cargo display after trade

**Estimated Effort:** ~100 lines in trade-ui.js, ~50 lines in game.html

---

## High Priority Gaps (Enhance Playability)

### 5. Adventure ↔ Trade Integration (MEDIUM)

**Problem:** TravellerAdventureService handles character travel and ship management, but doesn't connect to the trade system. Characters on adventures can't buy/sell commodities.

**Current State:**
- Adventures have credits ✅
- Adventures have fleets with ships ✅
- Adventures can travel between systems ✅
- BUT: No trade operations during adventures

**Impact:** Adventure characters can't engage in merchant activities — one of Traveller's core pillars.

**Fix Required:**
1. In TravellerAdventureService, add `tradeCommodity(characterId, commodity, quantity, action)` method
2. Use TradeManager to get prices at current system
3. Deduct/add credits from adventure credit balance
4. Track cargo in TravellerAdventure
5. Add trade tab to adventure UI

**Estimated Effort:** ~200 lines in TravellerAdventureService.java, new UI components

---

### 6. B07 Trade Rules — Actual Value Table Implementation (MEDIUM)

**Problem:** The rules database has the Actual Value table (TABLE_ACTUAL_VALUE with 14 entries), but the trade price calculation doesn't use it. The WorldEconomy uses simple market conditions (SURPLUS, NORMAL, SHORT) without the B07 Actual Value roll.

**Current State:**
- TABLE_ACTUAL_VALUE exists in DB ✅
- WorldEconomy has market conditions ✅
- BUT: No dice roll for actual value during trade

**Impact:** Trade prices are static based on market conditions. No randomness or dramatic price swings from the Actual Value table (which can give 40% to 400% of base price).

**Fix Required:**
1. In WorldEconomy.getBuyPrice()/getSellPrice(), add optional Actual Value roll
2. Roll 2D6 on TABLE_ACTUAL_VALUE to get percentage modifier
3. Apply to base price before market condition modifier
4. Make the roll optional (controlled by game settings)

**Estimated Effort:** ~50 lines in WorldEconomy.java

---

### 7. World Generation from Rules (MEDIUM)

**Problem:** The rules database has world generation rules (B03, B06) but the galaxy generation doesn't use them. Worlds are generated with random UWP values without following the Traveller generation tables.

**Current State:**
- GalaxyManager generates solar systems ✅
- Planets have UWP data ✅
- BUT: UWP values are random, not generated from Traveller tables

**Impact:** Worlds don't follow Traveller probability distributions. No trade codes derived from UWP.

**Fix Required:**
1. Create WorldGenerationService using TABLE_WORLD_OCCURRENCE, TABLE_STARPORT, etc.
2. Implement step-by-step UWP generation from B03/B06 rules
3. Derive trade codes from UWP automatically
4. Use in GalaxyManager when generating new systems

**Estimated Effort:** ~300 lines in new WorldGenerationService.java

---

## Medium Priority Gaps (Quality of Life)

### 8. Trade Controller — Session-Based Endpoints (MEDIUM)

**Problem:** The TradeController career/exchange/events/routes endpoints require a sessionId, but return 404 for non-existent sessions. The endpoints need a proper game session to work.

**Current State:**
- Endpoints exist ✅
- Endpoints return data when session exists ✅
- BUT: No way to create a session from the trade UI

**Impact:** Trade career, exchange, events, and routes data is only available during an active game session.

**Fix Required:**
1. Ensure GameSessionService creates default session on first access
2. Or add session creation to the trade UI flow
3. Make endpoints return meaningful empty states instead of 404

**Estimated Effort:** ~30 lines in TradeController.java or GameSessionService.java

---

### 9. Naval Combat — Pre-Designed Ships (LOW)

**Problem:** The rules database has 14 naval ship classes from S09 (Scout/Courier to Dreadnought), but no pre-designed ships are available in the game. Players must design ships from scratch.

**Current State:**
- HighGuardShipFactory exists ✅
- Ship design rules in DB ✅
- BUT: No pre-built ship templates

**Impact:** Players can't quickly deploy standard Imperial Navy ships.

**Fix Required:**
1. Create ShipTemplate class with pre-configured stats
2. Add S09 ship classes as templates
3. Add endpoint to create ship from template
4. Add template browser to UI

**Estimated Effort:** ~200 lines in new ShipTemplate.java, ~100 lines in controller

---

### 10. Psionics System (LOW)

**Problem:** The rules database has psionics rules from B03 (6 disciplines), but no psionics implementation exists in the game.

**Current State:**
- TABLE_PSIONICS in DB ✅
- Psionics rules extracted ✅
- BUT: No psionics gameplay

**Impact:** Characters can't use psychic abilities.

**Fix Required:**
1. Create PsionicAbility enum with 6 disciplines
2. Add psionic detection to character creation
3. Implement psionic power checks
4. Add psionic encounters

**Estimated Effort:** ~300 lines across multiple files

---

## Priority Implementation Order

| Priority | Gap | Estimated Lines | Impact |
|----------|-----|-----------------|--------|
| **P0** | TurnManager: Trade processing | ~15 | **Blocks all trade gameplay** |
| **P0** | TurnManager: Mercenary income | ~10 | **Blocks mercenary gameplay** |
| **P1** | Character → Commander link | ~100 | **Trade bonuses don't work** |
| **P1** | Trade mission UI execution | ~150 | **Can't trade from UI** |
| **P2** | Adventure ↔ Trade integration | ~200 | **Adventures lack trade** |
| **P2** | B07 Actual Value table | ~50 | **Static trade prices** |
| **P2** | World generation from rules | ~300 | **Random UWP, no trade codes** |
| **P3** | Session-based endpoints fix | ~30 | **404 errors** |
| **P4** | Naval pre-designed ships | ~300 | **No quick ship deployment** |
| **P4** | Psionics system | ~300 | **No psychic abilities** |

---

## Quick Wins (Can Be Done Immediately)

### Quick Win 1: Fix TurnManager Trade Processing (~15 lines)

```java
// In TurnManager.java, replace processTradeMarkets():
private void processTradeMarkets() {
    if (context instanceof Game game) {
        TradeManager tradeManager = game.getTradeManager();
        if (tradeManager != null) {
            tradeManager.processEndOfTurn(pipelineManagers);
        }
    }
}
```

### Quick Win 2: Fix TurnManager Mercenary Income (~10 lines)

```java
// In TurnManager.java, replace processMercenaryIncome():
private void processMercenaryIncome() {
    if (context instanceof Game game) {
        MercenaryIncomeManager mercManager = game.getMercenaryIncomeManager();
        if (mercManager != null) {
            mercManager.processEndOfTurn();
        }
    }
}
```

These two quick wins would immediately activate the entire trade integration system (CreditExchange, TradeCareer, TradeRouteNetwork, economic events) and the mercenary income system.

---

## Conclusion

The **single biggest gap** is that the game turn loop doesn't call the trade and mercenary processing methods. This is a ~25-line fix that would activate all the integration work already done (credit exchange, career bonuses, route networks, economic events).

After that, the **Character → Commander link** is the next most important fix, because without it, trade career bonuses are always 0% and the Traveller character system is disconnected from the 4X gameplay.

The **Trade UI mission execution** is the third priority — players need to be able to actually execute trades from the UI, not just view data.
