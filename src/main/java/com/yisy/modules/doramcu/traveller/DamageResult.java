package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Result of a single weapon attack in Traveller ship-to-ship combat.
 * <p>
 * Each attack produces a hit or miss, and if it hits, a specific hit location
 * is determined along with the damage dealt.
 * </p>
 */
public class DamageResult {

    private final ShipCombatStatus attacker;
    private final ShipCombatStatus target;
    private final boolean isHit;
    private final HitLocation hitLocation;
    private final int damageDice;
    private final int damageRolled;
    private final String weaponType;
    private final String description;

    private DamageResult(Builder builder) {
        this.attacker = builder.attacker;
        this.target = builder.target;
        this.isHit = builder.isHit;
        this.hitLocation = builder.hitLocation;
        this.damageDice = builder.damageDice;
        this.damageRolled = builder.damageRolled;
        this.weaponType = builder.weaponType;
        this.description = builder.description;
    }

    /** Returns the attacking ship. */
    public ShipCombatStatus getAttacker() {
        return attacker;
    }

    /** Returns the target ship. */
    public ShipCombatStatus getTarget() {
        return target;
    }

    /** Returns whether the attack hit. */
    public boolean isHit() {
        return isHit;
    }

    /** Returns the hit location, or null if the attack missed. */
    public HitLocation getHitLocation() {
        return hitLocation;
    }

    /** Returns the number of damage dice rolled. */
    public int getDamageDice() {
        return damageDice;
    }

    /** Returns the actual damage rolled (sum of dice). */
    public int getDamageRolled() {
        return damageRolled;
    }

    /** Returns the weapon type description. */
    public String getWeaponType() {
        return weaponType;
    }

    /** Returns a human-readable description of the attack. */
    public String getDescription() {
        return description;
    }

    /**
     * Creates a new builder for DamageResult.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for DamageResult instances.
     */
    public static class Builder {
        private ShipCombatStatus attacker;
        private ShipCombatStatus target;
        private boolean isHit;
        private HitLocation hitLocation;
        private int damageDice;
        private int damageRolled;
        private String weaponType;
        private String description;

        public Builder attacker(ShipCombatStatus attacker) {
            this.attacker = attacker;
            return this;
        }

        public Builder target(ShipCombatStatus target) {
            this.target = target;
            return this;
        }

        public Builder isHit(boolean isHit) {
            this.isHit = isHit;
            return this;
        }

        public Builder hitLocation(HitLocation hitLocation) {
            this.hitLocation = hitLocation;
            return this;
        }

        public Builder damageDice(int damageDice) {
            this.damageDice = damageDice;
            return this;
        }

        public Builder damageRolled(int damageRolled) {
            this.damageRolled = damageRolled;
            return this;
        }

        public Builder weaponType(String weaponType) {
            this.weaponType = weaponType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public DamageResult build() {
            return new DamageResult(this);
        }
    }

    @Override
    public String toString() {
        if (isHit) {
            return String.format("DamageResult{attacker='%s', target='%s', HIT at %s, %dD6=%d, weapon=%s}",
                    attacker.getName(), target.getName(), hitLocation.getLabel(),
                    damageDice, damageRolled, weaponType);
        } else {
            return String.format("DamageResult{attacker='%s', target='%s', MISS, weapon=%s}",
                    attacker.getName(), target.getName(), weaponType);
        }
    }
}
