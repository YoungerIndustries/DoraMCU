# Resource Pipeline System Design

## Current State

The current resource system is a flat pool model:
- **Planets** produce abstract `ResourceType` values (MINERALS, ENERGY, FOOD, RESEARCH, INFLUENCE) each turn
- **TurnManager** sums all planet production and adds it directly to the player's resource pool
- **Ships** cost minerals and energy based on hull modules and components
- **No intermediate steps**: resources appear in player pools without mining, transport, or processing

## Proposed Resource Pipeline

### Pipeline Stages

```
Celestial Body → Mining → Transport → Processing → Modules → Construction
```

### 1. Raw Resources (Mined at Celestial Bodies)

Raw resources are extracted from celestial bodies by mining operations.

**RawResourceType enum:**
- `ORE` — Basic metal ore from planets and asteroids
- `CRYSTAL` — Energy crystals from comets and certain planets
- `GAS` — Volatile gases from gas giants
- `DUST` — Cosmic dust from nebulae and asteroid belts
- `BIO_MASS` — Organic material from habitable planets

**Mining operations:**
- Each celestial body has a `miningCapacity` based on size and mineral quality
- Mining requires a `MiningFacility` (SpaceStation with MINING type) or planet-based mining infrastructure
- Raw resources are stored in the celestial body's local storage until transported

### 2. Transport (Moving Raw Resources)

Raw resources are transported between celestial bodies by transport ships.

**Transport mechanics:**
- Transport ships have cargo capacity (measured in resource units)
- Transport routes connect celestial bodies within the same solar system or across hyperlanes
- Transport takes time (turns) based on distance
- Resources are loaded at source and unloaded at destination

### 3. Processing (Converting Raw to Refined)

Raw resources are processed into refined resources at processing facilities.

**RefinedResourceType enum:**
- `METAL_ALLOY` — Processed from ORE + ENERGY
- `POWER_CELL` — Processed from CRYSTAL + ORE
- `FUEL` — Processed from GAS + CRYSTAL
- `COMPOSITE` — Processed from DUST + ORE + ENERGY
- `NUTRIENT` — Processed from BIO_MASS + ENERGY

**Processing facilities:**
- SpaceStations with RESEARCH or TRADE types can process resources
- Planets with industrial facilities can process resources
- Each facility has a processing rate (units per turn)

### 4. Modules (Construction Components)

Refined resources are assembled into modules for ship and facility construction.

**ModuleType enum:**
- `HULL_MODULE` — Metal alloy + composite for ship hulls
- `WEAPON_MODULE` — Metal alloy + power cell for weapons
- `SHIELD_MODULE` — Power cell + composite for shields
- `ENGINE_MODULE` — Fuel + power cell for propulsion
- `FACILITY_MODULE` — Metal alloy + nutrient for station construction

**Module assembly:**
- Requires a shipyard (SpaceStation with SHIPYARD type or planet shipyard facility)
- Assembly takes time (turns) based on module complexity
- Modules are stored in the shipyard's inventory until used

### 5. Construction (Building Ships and Facilities)

Modules are consumed to build ships and facilities.

**Construction mechanics:**
- Ship designs specify required modules (type and quantity)
- Shipyard checks module inventory before starting construction
- Construction takes time based on ship complexity
- Completed ships are added to the player's fleet

## Data Model

### New Classes

1. **RawResourceType** — Enum of raw resource types
2. **RefinedResourceType** — Enum of refined resource types
3. **ModuleType** — Enum of module types with recipes
4. **ResourceStorage** — Generic storage for any resource type with capacity
5. **MiningOperation** — Mining activity at a celestial body
6. **TransportOrder** — Order to transport resources between locations
7. **ProcessingOperation** — Processing activity at a facility
8. **ModuleAssembly** — Assembly of refined resources into modules
9. **ResourcePipelineManager** — Manages the entire pipeline for a player

### Modified Classes

1. **Planet** — Add mining capacity and local resource storage
2. **SpaceStation** — Add processing and assembly capabilities
3. **Fleet** — Add transport ship support with cargo capacity
4. **TurnManager** — Process mining, transport, processing, and assembly each turn
5. **ResourceManager** — Integrate with pipeline for construction costs

## Turn Processing Order

Each turn, the pipeline processes in this order:

1. **Mining**: Extract raw resources from celestial bodies
2. **Transport**: Move resources along transport routes
3. **Processing**: Convert raw to refined resources
4. **Assembly**: Build modules from refined resources
5. **Construction**: Complete ship/facility building from modules

## Backward Compatibility

The existing flat resource system remains as a fallback:
- Players without mining/processing infrastructure continue to receive direct resource production
- The pipeline is an advanced system that players can opt into by building facilities
- ResourceManager.canAffordShip() checks both flat resources and module inventory

## Implementation Phases

### Phase 1: Core Resource Types
- Create RawResourceType, RefinedResourceType, ModuleType enums
- Create ResourceStorage class
- Create ModuleType with recipes (refined resource requirements)

### Phase 2: Mining Operations
- Create MiningOperation class
- Add mining capacity to Planet and other celestial bodies
- Integrate mining into TurnManager

### Phase 3: Transport System
- Create TransportOrder class
- Add cargo capacity to Fleet/Ship
- Integrate transport into TurnManager

### Phase 4: Processing Facilities
- Create ProcessingOperation class
- Add processing to SpaceStation and Planet
- Integrate processing into TurnManager

### Phase 5: Module Assembly
- Create ModuleAssembly class
- Add assembly to shipyards
- Integrate assembly into TurnManager

### Phase 6: Construction Integration
- Update ResourceManager to check module inventory
- Update ship building to consume modules
- Add pipeline status to HUD
