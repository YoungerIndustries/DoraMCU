package com.yisy.modules.doramcu.traveller;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * High Guard ship-to-ship combat engine from Classic Traveller Book 5: High Guard.
 * <p>
 * Combat proceeds in rounds, each with three phases:
 * </p>
 * <ol>
 *   <li><b>Maneuver Phase</b> — ships spend thrust to change range bands or aid gunnery</li>
 *   <li><b>Combat Phase</b> — ships fire weapons (missiles first, then beams)</li>
 *   <li><b>Ship Action Phase</b> — repairs, sensor locks, electronic warfare</li>
 * </ol>
 * <p>
 * Key mechanics:
 * </p>
 * <ul>
 *   <li><b>Range Bands</b> — Adjacent, Close, Short, Medium, Long, Very Long, Distant</li>
 *   <li><b>Thrust</b> — spent to change range bands; combined when both charge</li>
 *   <li><b>Agility</b> — used for dogfighting at Adjacent/Close range</li>
 *   <li><b>Gunnery</b> — 2D6 + skill + weapon modifier + computer bonus vs. 8+</li>
 *   <li><b>Damage</b> — beam: 2D6 + effect; missile: 1D6 × effect; reduced by armor</li>
 *   <li><b>Missile Flight Time</b> — missiles arrive after 1-10 turns depending on range</li>
 * </ul>
 * <p>
 * Initiative is determined by: 2D6 + Fleet Tactics skill + computer rating.
 * Higher initiative acts first in each phase.
 * </p>
 */
public class HighGuardShipCombat {

    /**
     * The current phase of combat within a round.
     */
    public enum CombatPhase {
        NOT_STARTED,
        MANEUVER,
        MISSILE_FIRING,
        BEAM_FIRING,
        SHIP_ACTION,
        COMPLETED
    }

    /**
     * The outcome of a completed battle.
     */
    public enum CombatOutcome {
        SIDE_0_VICTORY,
        SIDE_1_VICTORY,
        MUTUAL_DESTRUCTION,
        RETREAT,
        STALEMATE
    }

    /**
     * Represents a single event during the battle.
     */
    public static class CombatEvent {
        private final int round;
        private final CombatPhase phase;
        private final EventType type;
        private final String message;
        private final int side;

        public enum EventType {
            BATTLE_STARTED,
            RANGE_CHANGED,
            THRUST_SPENT,
            MISSILE_FIRED,
            MISSILE_HIT,
            MISSILE_MISS,
            BEAM_FIRED,
            BEAM_HIT,
            BEAM_MISS,
            ARMOR_ABSORBED,
            HULL_DAMAGE,
            SYSTEM_OFFLINE,
            CREW_CASUALTIES,
            SHIP_DESTROYED,
            SHIP_RETREATED,
            REPAIR_ATTEMPT,
            ROUND_COMPLETED,
            BATTLE_ENDED
        }

        CombatEvent(int round, CombatPhase phase, EventType type, String message, int side) {
            this.round = round;
            this.phase = phase;
            this.type = type;
            this.message = message;
            this.side = side;
        }

        public int getRound() { return round; }
        public CombatPhase getPhase() { return phase; }
        public EventType getType() { return type; }
        public String getMessage() { return message; }
        public int getSide() { return side; }
    }

    /**
     * Represents a ship's combat status.
     */
    public static class CombatShip {
        private final HighGuardShipDesign design;
        private final int side;
        private int hullPoints;
        private int currentThrust;
        private int currentAgility;
        private final List<HighGuardShipDesign.WeaponMount> operationalTurrets;
        private final List<HighGuardShipDesign.WeaponMount> operationalBays;
        private boolean destroyed;
        private boolean retreated;
        private int missilePods;

        CombatShip(HighGuardShipDesign design, int side) {
            this.design = design;
            this.side = side;
            this.hullPoints = design.getHullPoints();
            this.currentThrust = design.getThrust();
            this.currentAgility = design.getAgility();
            this.operationalTurrets = new ArrayList<>(design.getTurretWeapons());
            this.operationalBays = new ArrayList<>(design.getBayWeapons());
            this.missilePods = calculateMissilePods(design);
        }

        private int calculateMissilePods(HighGuardShipDesign design) {
            int pods = 0;
            for (HighGuardShipDesign.WeaponMount mount : design.getBayWeapons()) {
                pods += mount.getBaySize().getTons() / 10; // 1 pod per 10 tons of bay
            }
            return Math.max(pods, design.getTotalTonnage() / 1000); // Minimum pods by size
        }

        public HighGuardShipDesign getDesign() { return design; }
        public int getSide() { return side; }
        public int getHullPoints() { return hullPoints; }
        public int getMaxHullPoints() { return design.getHullPoints(); }
        public int getCurrentThrust() { return currentThrust; }
        public int getCurrentAgility() { return currentAgility; }
        public List<HighGuardShipDesign.WeaponMount> getOperationalTurrets() {
            return Collections.unmodifiableList(operationalTurrets);
        }
        public List<HighGuardShipDesign.WeaponMount> getOperationalBays() {
            return Collections.unmodifiableList(operationalBays);
        }
        public boolean isDestroyed() { return destroyed; }
        public boolean isRetreated() { return retreated; }
        public int getMissilePods() { return missilePods; }

        public void setHullPoints(int hp) {
            this.hullPoints = Math.max(0, hp);
            if (this.hullPoints <= 0) {
                this.destroyed = true;
            }
        }

        public void damageTurret(int index) {
            if (index >= 0 && index < operationalTurrets.size()) {
                operationalTurrets.get(index).setOperational(false);
            }
        }

        public void damageBay(int index) {
            if (index >= 0 && index < operationalBays.size()) {
                operationalBays.get(index).setOperational(false);
            }
        }

        public void reduceThrust(int amount) {
            this.currentThrust = Math.max(0, this.currentThrust - amount);
        }

        public void reduceAgility(int amount) {
            this.currentAgility = Math.max(0, this.currentAgility - amount);
        }

        public void useMissilePod() {
            this.missilePods = Math.max(0, this.missilePods - 1);
        }

        public void setRetreated(boolean retreated) {
            this.retreated = retreated;
        }

        public void setDestroyed(boolean destroyed) {
            this.destroyed = destroyed;
        }

        public double getHullRatio() {
            int max = design.getHullPoints();
            return max > 0 ? (double) hullPoints / max : 0;
        }
    }

    // ---- Combat State ----

    private final CombatShip side0;
    private final CombatShip side1;
    private HighGuardRangeBand currentRange;
    private CombatPhase currentPhase;
    private int currentRound;
    private final List<CombatEvent> eventLog;
    private final List<PendingMissile> pendingMissiles;
    private int side0Initiative;
    private int side1Initiative;
    private boolean completed;

    /**
     * A missile in flight that will impact after a delay.
     */
    private static class PendingMissile {
        final int impactRound;
        final int attackingSide;
        final HighGuardWeapon weapon;
        final int effect;

        PendingMissile(int impactRound, int attackingSide, HighGuardWeapon weapon, int effect) {
            this.impactRound = impactRound;
            this.attackingSide = attackingSide;
            this.weapon = weapon;
            this.effect = effect;
        }
    }

    /**
     * Creates a new High Guard combat between two ships.
     *
     * @param ship0 the first ship (side 0)
     * @param ship1 the second ship (side 1)
     * @param initialRange the starting range band
     */
    public HighGuardShipCombat(HighGuardShipDesign ship0, HighGuardShipDesign ship1,
                                HighGuardRangeBand initialRange) {
        this.side0 = new CombatShip(ship0, 0);
        this.side1 = new CombatShip(ship1, 1);
        this.currentRange = initialRange != null ? initialRange : HighGuardRangeBand.LONG;
        this.currentPhase = CombatPhase.NOT_STARTED;
        this.eventLog = new ArrayList<>();
        this.pendingMissiles = new ArrayList<>();
        this.completed = false;

        // Calculate initiative
        this.side0Initiative = DiceUtil.roll2D6() + ship0.getComputerModel();
        this.side1Initiative = DiceUtil.roll2D6() + ship1.getComputerModel();

        eventLog.add(new CombatEvent(0, CombatPhase.NOT_STARTED, CombatEvent.EventType.BATTLE_STARTED,
                String.format("%s vs %s at %s range", ship0.getName(), ship1.getName(), currentRange.getName()), -1));
    }

    // ---- Accessors ----

    public CombatShip getSide0() { return side0; }
    public CombatShip getSide1() { return side1; }
    public HighGuardRangeBand getCurrentRange() { return currentRange; }
    public CombatPhase getCurrentPhase() { return currentPhase; }
    public int getCurrentRound() { return currentRound; }
    public List<CombatEvent> getEventLog() { return Collections.unmodifiableList(eventLog); }
    public boolean isCompleted() { return completed; }
    public int getSide0Initiative() { return side0Initiative; }
    public int getSide1Initiative() { return side1Initiative; }

    /**
     * Executes a full combat round.
     *
     * @return true if the combat is still ongoing after this round
     */
    public boolean executeRound() {
        if (completed) {
            return false;
        }

        currentRound++;

        // Maneuver phase
        currentPhase = CombatPhase.MANEUVER;
        executeManeuverPhase();

        if (isCombatEnded()) {
            return false;
        }

        // Missile firing phase
        currentPhase = CombatPhase.MISSILE_FIRING;
        executeMissileFiringPhase();

        if (isCombatEnded()) {
            return false;
        }

        // Resolve pending missiles
        resolvePendingMissiles();

        if (isCombatEnded()) {
            return false;
        }

        // Beam firing phase
        currentPhase = CombatPhase.BEAM_FIRING;
        executeBeamFiringPhase();

        if (isCombatEnded()) {
            return false;
        }

        // Ship action phase
        currentPhase = CombatPhase.SHIP_ACTION;
        executeShipActionPhase();

        eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.ROUND_COMPLETED,
                String.format("Round %d completed — range: %s", currentRound, currentRange.getName()), -1));

        // Check if combat is over
        if (isCombatEnded()) {
            endCombat();
            return false;
        }

        currentPhase = CombatPhase.NOT_STARTED;
        return true;
    }

    /**
     * Fights the combat to completion.
     *
     * @return the combat outcome
     */
    public CombatOutcome fightToCompletion() {
        while (!completed && currentRound < 100) {
            executeRound();
        }
        if (!completed) {
            endCombat();
        }
        return getOutcome();
    }

    // ---- Phase Execution ----

    /**
     * Maneuver phase — ships spend thrust to change range.
     */
    private void executeManeuverPhase() {
        // Determine who acts first by initiative
        boolean side0First = side0Initiative >= side1Initiative;
        CombatShip first = side0First ? side0 : side1;
        CombatShip second = side0First ? side1 : side0;

        // Each ship decides: charge, flee, or hold
        int firstThrust = first.getCurrentThrust();
        int secondThrust = second.getCurrentThrust();

        // Simple AI: charge if hull > 50%, flee if hull < 30%, hold otherwise
        boolean firstCharging = first.getHullRatio() > 0.5;
        boolean secondCharging = second.getHullRatio() > 0.5;

        // Calculate new range
        HighGuardRangeBand newRange = HighGuardRangeBand.calculateNewRange(
                currentRange, firstThrust, secondThrust, firstCharging, secondCharging);

        if (newRange != currentRange) {
            eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.RANGE_CHANGED,
                    String.format("Range changed: %s → %s", currentRange.getName(), newRange.getName()), -1));
            currentRange = newRange;
        }

        eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.THRUST_SPENT,
                String.format("Side 0: %d thrust (%s), Side 1: %d thrust (%s)",
                        firstThrust, firstCharging ? "charge" : "flee",
                        secondThrust, secondCharging ? "charge" : "flee"), -1));
    }

    /**
     * Missile firing phase — ships fire missiles and torpedoes.
     */
    private void executeMissileFiringPhase() {
        fireMissilesForSide(side0, side1);
        fireMissilesForSide(side1, side0);
    }

    private void fireMissilesForSide(CombatShip attacker, CombatShip defender) {
        if (attacker.isDestroyed() || attacker.isRetreated()) {
            return;
        }

        // Fire from bays
        for (HighGuardShipDesign.WeaponMount mount : attacker.getOperationalBays()) {
            if (!mount.isOperational()) continue;
            HighGuardWeapon weapon = mount.getWeapon();
            if (!weapon.isInRange(currentRange)) continue;
            if (attacker.getMissilePods() <= 0) continue;

            // Gunnery roll
            int gunneryRoll = DiceUtil.roll2D6() + weapon.getHitModifier(currentRange) +
                    attacker.getDesign().getComputerModel();
            int effect = gunneryRoll - 8;

            if (gunneryRoll >= 8) {
                // Hit — missile will impact after flight time
                int flightTime = weapon.getFlightTime(currentRange);
                int impactRound = currentRound + flightTime;

                pendingMissiles.add(new PendingMissile(impactRound, attacker.getSide(), weapon, effect));
                attacker.useMissilePod();

                eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.MISSILE_FIRED,
                        String.format("Side %d fires %s (effect %d, impacts round %d)",
                                attacker.getSide(), weapon.getName(), effect, impactRound), attacker.getSide()));
            } else {
                eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.MISSILE_MISS,
                        String.format("Side %d's %s misses (roll %d)",
                                attacker.getSide(), weapon.getName(), gunneryRoll), attacker.getSide()));
            }
        }
    }

    /**
     * Resolve missiles that have reached their target.
     */
    private void resolvePendingMissiles() {
        List<PendingMissile> arrived = new ArrayList<>();
        List<PendingMissile> remaining = new ArrayList<>();

        for (PendingMissile missile : pendingMissiles) {
            if (missile.impactRound <= currentRound) {
                arrived.add(missile);
            } else {
                remaining.add(missile);
            }
        }

        for (PendingMissile missile : arrived) {
            CombatShip attacker = missile.attackingSide == 0 ? side0 : side1;
            CombatShip defender = missile.attackingSide == 0 ? side1 : side0;

            if (defender.isDestroyed()) continue;

            // Calculate damage
            int damage = rollMissileDamage(missile.weapon, missile.effect);
            int armorReduction = defender.getDesign().getArmorProtection();

            eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.MISSILE_HIT,
                    String.format("Side %d's %s hits! Raw damage: %d",
                            missile.attackingSide, missile.weapon.getName(), damage), missile.attackingSide));

            applyDamage(defender, damage, armorReduction);
        }

        pendingMissiles.clear();
        pendingMissiles.addAll(remaining);
    }

    /**
     * Beam firing phase — ships fire lasers and beam weapons.
     */
    private void executeBeamFiringPhase() {
        fireBeamsForSide(side0, side1);
        fireBeamsForSide(side1, side0);
    }

    private void fireBeamsForSide(CombatShip attacker, CombatShip defender) {
        if (attacker.isDestroyed() || attacker.isRetreated()) {
            return;
        }

        for (HighGuardShipDesign.WeaponMount mount : attacker.getOperationalTurrets()) {
            if (!mount.isOperational()) continue;
            HighGuardWeapon weapon = mount.getWeapon();
            if (!weapon.isBeam() && !weapon.isSpecial()) continue;
            if (!weapon.isInRange(currentRange)) continue;

            // Gunnery roll
            int gunneryRoll = DiceUtil.roll2D6() + weapon.getHitModifier(currentRange) +
                    attacker.getDesign().getComputerModel();
            int effect = gunneryRoll - 8;

            if (gunneryRoll >= 8) {
                // Hit
                int damage = rollBeamDamage(weapon, effect, mount.getWeaponCount());
                int armorReduction = defender.getDesign().getArmorProtection();

                eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.BEAM_HIT,
                        String.format("Side %d's %s hits! Raw damage: %d (effect %d)",
                                attacker.getSide(), weapon.getName(), damage, effect), attacker.getSide()));

                applyDamage(defender, damage, armorReduction);
            } else {
                eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.BEAM_MISS,
                        String.format("Side %d's %s misses (roll %d)",
                                attacker.getSide(), weapon.getName(), gunneryRoll), attacker.getSide()));
            }
        }
    }

    /**
     * Ship action phase — repairs and system recovery.
     */
    private void executeShipActionPhase() {
        // Simple repair: each ship can repair 1D6 hull points per round
        for (CombatShip ship : Arrays.asList(side0, side1)) {
            if (ship.isDestroyed() || ship.isRetreated()) continue;
            if (ship.getHullPoints() < ship.getMaxHullPoints()) {
                int repair = DiceUtil.rollD6();
                int oldHp = ship.getHullPoints();
                ship.setHullPoints(Math.min(ship.getMaxHullPoints(), ship.getHullPoints() + repair));
                int actualRepair = ship.getHullPoints() - oldHp;
                if (actualRepair > 0) {
                    eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.REPAIR_ATTEMPT,
                            String.format("Side %d repairs %d hull points", ship.getSide(), actualRepair), ship.getSide()));
                }
            }
        }
    }

    // ---- Damage Resolution ----

    /**
     * Applies damage to a ship, accounting for armor.
     */
    private void applyDamage(CombatShip defender, int rawDamage, int armorReduction) {
        int absorbed = Math.min(rawDamage, armorReduction);
        int hullDamage = rawDamage - absorbed;

        if (absorbed > 0) {
            eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.ARMOR_ABSORBED,
                    String.format("Side %d's armor absorbs %d damage", defender.getSide(), absorbed), defender.getSide()));
        }

        if (hullDamage > 0) {
            defender.setHullPoints(defender.getHullPoints() - hullDamage);
            eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.HULL_DAMAGE,
                    String.format("Side %d takes %d hull damage (HP: %d/%d)",
                            defender.getSide(), hullDamage,
                            defender.getHullPoints(), defender.getMaxHullPoints()), defender.getSide()));

            // Random system damage on high hits
            if (hullDamage >= 10 && DiceUtil.rollPercentile() <= 30) {
                damageRandomSystem(defender);
            }
        }

        if (defender.getHullPoints() <= 0) {
            defender.setDestroyed(true);
            eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.SHIP_DESTROYED,
                    String.format("Side %d's %s is destroyed!", defender.getSide(), defender.getDesign().getName()), defender.getSide()));
        }
    }

    /**
     * Damages a random system on the ship.
     */
    private void damageRandomSystem(CombatShip ship) {
        int roll = DiceUtil.rollPercentile();

        if (roll < 30 && !ship.getOperationalTurrets().isEmpty()) {
            // Damage a turret
            int index = ThreadLocalRandom.current().nextInt(ship.getOperationalTurrets().size());
            HighGuardShipDesign.WeaponMount mount = ship.getOperationalTurrets().get(index);
            if (mount.isOperational()) {
                mount.setOperational(false);
                eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.SYSTEM_OFFLINE,
                        String.format("Side %d's %s goes offline", ship.getSide(), mount), ship.getSide()));
            }
        } else if (roll < 50) {
            // Reduce thrust
            ship.reduceThrust(1);
            eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.SYSTEM_OFFLINE,
                    String.format("Side %d's thrust reduced to %d", ship.getSide(), ship.getCurrentThrust()), ship.getSide()));
        } else if (roll < 70) {
            // Crew casualties
            int casualties = DiceUtil.rollDSides(10);
            eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.CREW_CASUALTIES,
                    String.format("Side %d suffers %d crew casualties", ship.getSide(), casualties), ship.getSide()));
        }
    }

    private int rollBeamDamage(HighGuardWeapon weapon, int effect, int weaponCount) {
        // Beam damage: baseDice + effect, multiplied by weapon count
        int baseDamage = weapon.getBaseDamageDice();
        int damage = (baseDamage + effect) * weaponCount;
        return Math.max(1, damage);
    }

    private int rollMissileDamage(HighGuardWeapon weapon, int effect) {
        // Missile damage: 1D6 × effect × baseDice
        if (effect <= 0) return 1;
        int damage = 0;
        for (int i = 0; i < effect * weapon.getBaseDamageDice(); i++) {
            damage += DiceUtil.rollD6();
        }
        return Math.max(1, damage);
    }

    // ---- Combat End ----

    private boolean isCombatEnded() {
        return side0.isDestroyed() || side1.isDestroyed() ||
               side0.isRetreated() || side1.isRetreated() ||
               currentRound >= 100;
    }

    private void endCombat() {
        completed = true;
        currentPhase = CombatPhase.COMPLETED;

        CombatOutcome outcome = getOutcome();
        eventLog.add(new CombatEvent(currentRound, currentPhase, CombatEvent.EventType.BATTLE_ENDED,
                String.format("Battle ended: %s", outcome.name()), -1));
    }

    /**
     * Returns the outcome of the combat.
     */
    public CombatOutcome getOutcome() {
        if (side0.isDestroyed() && side1.isDestroyed()) {
            return CombatOutcome.MUTUAL_DESTRUCTION;
        } else if (side0.isDestroyed() || side0.isRetreated()) {
            return CombatOutcome.SIDE_1_VICTORY;
        } else if (side1.isDestroyed() || side1.isRetreated()) {
            return CombatOutcome.SIDE_0_VICTORY;
        } else if (currentRound >= 100) {
            return CombatOutcome.STALEMATE;
        }
        return CombatOutcome.STALEMATE;
    }

    /**
     * Returns a summary of the combat.
     */
    public String getSummary() {
        return String.format(
                "Combat: %s vs %s | Rounds: %d | Range: %s | Outcome: %s | " +
                "Side0 HP: %d/%d | Side1 HP: %d/%d",
                side0.getDesign().getName(), side1.getDesign().getName(),
                currentRound, currentRange.getName(),
                completed ? getOutcome().name() : "ongoing",
                side0.getHullPoints(), side0.getMaxHullPoints(),
                side1.getHullPoints(), side1.getMaxHullPoints());
    }
}
