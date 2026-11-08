# B02: Starships (1981) - Rule Extractions

## Source: Classic_Traveller_CT__B02_Book_02_Starships_1981.pdf (52 pages)

---

## 1. STARSHIP DESIGN

### 1.1 Ship Design Process
**Title:** Ship Design Step-by-Step
**Category:** SHIP_DESIGN
**Chapter:** Ship Design
**Page:** 1
**Text:** Ship design follows a strict order:
1. Choose total tonnage (determines size and cost)
2. Allocate tonnage to hull (10% minimum)
3. Install jump drive (choose rating J-1 to J-6+)
4. Install maneuver drive (choose rating M-0 to M-6+)
5. Allocate fuel tanks (determines range)
6. Install life support (based on crew and passenger count)
7. Allocate cargo hold space
8. Install crew quarters (based on crew count)
9. Install weapons (if combat-capable)
10. Install armor (optional, reduces cargo space)
Remaining tonnage is available for optional systems.
**Notes:** Each component has a cost per ton. Total ship cost = sum of all component costs.

### 1.2 Jump Drive Details
**Title:** Jump Drive Specifications
**Category:** SHIP_TRAVEL
**Chapter:** Ship Design
**Page:** 3
**Text:** Jump drives enable faster-than-light travel:
- J-1: 1 parsec max range, 500cr/ton
- J-2: 2 parsecs max range, 1000cr/ton
- J-3: 3 parsecs max range, 1500cr/ton
- J-4: 4 parsecs max range, 2000cr/ton
- J-5: 5 parsecs max range, 2500cr/ton
- J-6: 6 parsecs max range, 3000cr/ton
Jump takes 5 hours + 1D6 hours. Fuel consumption = 1 ton per 1000 tons ship per parsec.
**Notes:** Higher jump ratings are exponentially more expensive. Most ships use J-2 or J-3.

### 1.3 Maneuver Drive Details
**Title:** Maneuver Drive Specifications
**Category:** SHIP_TRAVEL
**Chapter:** Ship Design
**Page:** 5
**Text:** Maneuver drives provide sub-light acceleration:
- M-0: No maneuver capability (hopper)
- M-1: 1G acceleration, 500cr/ton
- M-2: 2G acceleration, 1000cr/ton
- M-3: 3G acceleration, 1500cr/ton
- M-4: 4G acceleration, 2000cr/ton
- M-5: 5G acceleration, 2500cr/ton
- M-6: 6G acceleration, 3000cr/ton
Fuel consumption = (Maneuver rating)^2 x (ship tons / 1000) tons per burn.
**Notes:** Fuel consumption is the limiting factor for maneuver. High maneuver ships burn fuel rapidly.

### 1.4 Fuel Calculation
**Title:** Fuel Requirements
**Category:** SHIP_DESIGN
**Chapter:** Ship Design
**Page:** 7
**Text:** Fuel is needed for both jump and maneuver:
- Jump fuel: 1 ton per 1000 tons ship per parsec jumped
- Maneuver fuel: (Maneuver rating)^2 x (ship tons / 1000) tons per impulse of full burn
- Fuel tanks cost 50cr/ton
- Ships typically carry 10-30% of total tonnage as fuel
**Notes:** Insufficient fuel is a common cause of mission failure. Refueling is only possible at starports or gas giants.

---

## 2. SHIP SYSTEMS

### 2.1 Life Support
**Title:** Life Support Systems
**Category:** SHIP_SYSTEMS
**Chapter:** Ship Systems
**Page:** 10
**Text:** Life support provides breathable atmosphere and temperature control:
- Cost: 100cr per ton of protected space
- Required for crew, passengers, and cargo that needs atmosphere
- Failure causes suffocation within hours
- Redundant systems are recommended
**Notes:** Life support is one of the most critical ship systems. Failure is often catastrophic.

### 2.2 Cargo Holds
**Title:** Cargo Hold Capacity
**Category:** SHIP_SYSTEMS
**Chapter:** Ship Systems
**Page:** 12
**Text:** Cargo holds store trade goods and supplies:
- Cost: 50cr per ton of capacity
- Standard cargo: 1 ton per 1000cr value
- Bulk cargo: 10+ tons per unit
- Precious cargo: 0.1 tons or less per unit
- Cargo security varies by hold type
**Notes:** Cargo capacity is the primary profit driver for merchant ships.

### 2.3 Crew Quarters
**Title:** Crew Quarters Requirements
**Category:** SHIP_CREW
**Chapter:** Ship Systems
**Page:** 14
**Text:** Crew quarters provide living space for ship personnel:
- Standard: 1 ton per crew member
- Luxury: 2 tons per crew member
- Cramped: 0.5 tons per crew member
- Cost: 100cr per ton
- Insufficient quarters cause morale problems
**Notes:** Officer quarters are typically larger. Enlisted crew share compartments.

---

## 3. SHIP WEAPONS

### 3.1 Ship-Mounted Lasers
**Title:** Ship Lasers
**Category:** SHIP_WEAPONS
**Chapter:** Ship Weapons
**Page:** 20
**Text:** Ship-mounted lasers are the most common ship weapons:
- Light Laser: 1D6 damage, 1000cr, 1 ton, Range 6000km
- Heavy Laser: 2D6 damage, 2000cr, 2 tons, Range 6000km
- Lasers have no ammunition but require power
- Multiple lasers can be fired simultaneously
- Damage is reduced by target armor
**Notes:** Lasers are effective at all combat ranges. They are the standard armament for most ships.

### 3.2 Ship-Mounted Missiles
**Title:** Ship Missiles
**Category:** SHIP_WEAPONS
**Chapter:** Ship Weapons
**Page:** 22
**Text:** Ship-mounted missiles provide long-range strike capability:
- Missile Launcher: 5000cr, 5 tons
- Missiles: 1000cr each, 1 ton each
- Damage: 3D6 per hit
- Range: 12000km+
- Limited number of missiles per ship
- Missiles can be intercepted by point defense
**Notes:** Missiles are high-impact but limited in number. They are often decisive in ship combat.

### 3.3 Ship Armor
**Title:** Ship Armor Systems
**Category:** SHIP_ARMOR
**Chapter:** Ship Weapons
**Page:** 25
**Text:** Ship armor reduces incoming damage:
- Cost: 1000cr per point of armor per 1000 tons
- Armor points reduce laser and missile damage
- Point Defense (PD) rating intercepts incoming missiles
- PD roll: 2D6 + Gunnery skill vs. PD target number
- Higher PD = better missile interception
**Notes:** Armor is expensive and reduces cargo capacity. It is essential for combat ships.

---

## 4. SPACE COMBAT

### 4.1 Combat Phases
**Title:** Space Combat Sequence
**Category:** SPACE_COMBAT
**Chapter:** Space Combat
**Page:** 30
**Text:** Space combat is resolved in impulse (16-minute) segments:
1. Maneuver phase — ships declare burns and change position
2. Fire phase — ships declare targets and fire weapons
3. Hit determination — roll to hit based on range, size, and skill
4. Damage phase — apply damage to ship systems
5. Repair phase — attempt emergency repairs
6. Check for casualties and mission status
**Notes:** Combat can last many impulses. Fuel management is critical.

### 4.2 Hit Determination
**Title:** Space Combat Hit Tables
**Category:** SPACE_COMBAT
**Chapter:** Space Combat
**Page:** 32
**Text:** Hit determination uses ship size and range:
- Ship size categories: Small (100-500t), Medium (500-2000t), Large (2000-10000t), Capital (10000t+)
- Range categories: Point Blank (<1000km), Short (1000-3000km), Medium (3000-6000km), Long (6000-12000km)
- Roll 2D6 + Gunnery skill vs. target number from table
- Larger ships are easier to hit
- Closer range makes hitting easier
**Notes:** The hit table is the core of space combat resolution. Referees should have it readily available.

### 4.3 Damage Allocation
**Title:** Ship Damage Resolution
**Category:** SPACE_COMBAT
**Chapter:** Space Combat
**Page:** 35
**Text:** When a ship is hit, damage is allocated to systems:
- Roll on hit location table for affected system
- Common hits: Hull, Engines, Weapons, Sensors, Life Support
- Critical hits can disable systems
- Hull damage reduces ship integrity
- Multiple hits to same system cause cumulative damage
- Ship is destroyed when hull integrity reaches 0
**Notes:** Damage to critical systems can be more devastating than hull damage.

---

## 5. SHIP CREW

### 5.1 Crew Requirements
**Title:** Minimum Crew Requirements
**Category:** SHIP_CREW
**Chapter:** Ship Crew
**Page:** 40
**Text:** Every ship requires a minimum crew:
- Captain: 1 (required)
- Navigation Officer: 1 per jump drive level
- Engineering Officer: 1 per maneuver drive level
- Gunner: 1 per weapon bank
- Sensors Operator: 1
- Life Support Technician: 1
- Crew per 1000 tons: 2-5 depending on ship type
- Marines: Optional, 1 per 2000 tons recommended
**Notes:** Insufficient crew reduces ship effectiveness. Automated systems can reduce crew requirements.

### 5.2 Officer Skills
**Title:** Required Officer Skills
**Category:** SHIP_CREW
**Chapter:** Ship Crew
**Page:** 42
**Text:** Key officer skills:
- Captain: Leadership, Navigation, Pilot
- Navigation Officer: Astrogation, Navigation
- Engineering Officer: Engineer, Mechanic
- Gunner: Gunner, Sensors
- Sensors Operator: Sensors, Computers
- Marine Officer: Fight skills, Gunner, Leadership
**Notes:** Officers without required skills suffer -2 DM to related checks.

---

## 6. SHIP COMBAT TABLES

### 6.1 Hit Location Table
**Title:** Ship Hit Location Table (2D6)
**Category:** TABLE_SHIP_COMBAT
**Chapter:** Tables
**Page:** 45
**Text:** Ship hit location results:
- 2: Hull (Forward)
- 3-4: Hull (Port)
- 5-6: Hull (Starboard)
- 7-8: Hull (Aft)
- 9: Engines
- 10: Life Support
- 11: Sensors/Computers
- 12: Weapons
- 12+: Critical Hit (roll again)
**Notes:** Different ship types may have modified hit location tables.

### 6.2 Damage Effects Table
**Title:** Ship Damage Effects
**Category:** TABLE_SHIP_COMBAT
**Chapter:** Tables
**Page:** 46
**Text:** Ship damage effects:
- 1-2 hits: Minor damage, no effect
- 3-5 hits: System degraded, -1 DM
- 6-8 hits: System damaged, -2 DM
- 9-10 hits: System destroyed
- 11+ hits: Catastrophic failure, possible explosion
**Notes:** Repairing systems requires engineering skill and time.
