package com.yisy.modules.doramcu.traveller;

import java.util.*;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Customs check system from Classic Traveller B07: Merchant Prince.
 * <p>
 * When arriving at a world with contraband, customs may inspect the ship.
 * The smuggler can choose to bribe, run, or fight. The law level of the
 * destination world affects the frequency and severity of inspections.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 7: Merchant Prince (1985).
 * </p>
 */
public class CustomsCheck {

    /**
     * Outcomes of a customs inspection.
     */
    public enum Outcome {
        /**
         * No inspection — the ship passes through freely.
         */
        NO_INSPECTION("No Inspection", "Customs clears your ship without issue."),

        /**
         * Inspection passed — the smuggler evaded detection.
         */
        EVADDED("Evaded", "You successfully hide the contraband from customs."),

        /**
         * Bribe accepted — customs looks the other way.
         */
        BRIBED("Bribed", "Customs accepts your bribe and waves you through."),

        /**
         * Caught — contraband confiscated, fine imposed.
         */
        CAUGHT("Caught", "Customs discovers the contraband. Goods confiscated and fine imposed."),

        /**
         * Caught and imprisoned — severe penalty.
         */
        IMPRISONED("Imprisoned", "Customs arrests the crew. Ship and cargo seized."),

        /**
         * Combat — the smuggler runs and engages customs forces.
         */
        COMBAT("Combat", "You attempt to flee. Customs forces engage your ship.");

        private final String displayName;
        private final String description;

        Outcome(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Actions the smuggler can take when customs approaches.
     */
    public enum Action {
        /**
         * Attempt to evade detection with stealth.
         */
        EVADE("Evade", "Hide the contraband and hope for the best."),

        /**
         * Bribe the customs officials.
         */
        BRIBE("Bribe", "Offer credits to look the other way."),

        /**
         * Run and fight if pursued.
         */
        RUN("Run", "Attempt to flee the system. Fight if pursued.");

        private final String displayName;
        private final String description;

        Action(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Checks if customs inspects the ship on arrival.
     * <p>
     * Roll 2D6: inspection on 2-8 (58% chance). Higher law levels increase this.
     * Starport class also affects inspection frequency.
     * </p>
     *
     * @param lawLevel the law level of the destination world (0-9, A=anarchy)
     * @param starportClass the starport class (A=excellent, F=poor, Open=None)
     * @return true if customs inspects
     */
    public boolean checkInspection(int lawLevel, char starportClass) {
        int roll = DiceUtil.roll2D6();
        // Higher law = more inspections; better starport = more inspections
        int threshold = 8;
        if (lawLevel >= 8) threshold = 10; // Strict law
        else if (lawLevel >= 5) threshold = 8; // Normal
        else if (lawLevel >= 2) threshold = 6; // Lax
        else threshold = 4; // Anarchy

        if (starportClass == 'A' || starportClass == 'B') threshold += 1;
        if (starportClass == 'F' || starportClass == 'O') threshold -= 2;

        return roll <= threshold;
    }

    /**
     * Resolves a customs encounter based on the smuggler's action.
     *
     * @param action the smuggler's chosen action
     * @param contraband the type of contraband being smuggled
     * @param lawLevel the law level of the destination world
     * @param pilotSkill the pilot skill level (1-5, 0 = none)
     * @param credits the credits available for bribing
     * @return the outcome of the encounter
     */
    public CustomsResult resolve(Action action, ContrabandCommodity contraband,
                                  int lawLevel, int pilotSkill, long credits) {
        return switch (action) {
            case EVADE -> resolveEvade(contraband, lawLevel);
            case BRIBE -> resolveBribe(contraband, lawLevel, credits);
            case RUN -> resolveRun(pilotSkill, lawLevel);
        };
    }

    /**
     * Resolves an evasion attempt.
     */
    private CustomsResult resolveEvade(ContrabandCommodity contraband, int lawLevel) {
        int roll = DiceUtil.roll2D6();
        // Lower risk contraband is easier to hide; lower law = easier
        int target = 8 + (5 - contraband.getRiskLevel()) + (9 - lawLevel) / 2;
        boolean success = roll <= target;

        if (success) {
            return new CustomsResult(Outcome.EVADDED, roll, target,
                    "Contraband hidden successfully. No inspection detected.");
        } else {
            // Severity depends on law level and contraband risk
            if (lawLevel >= 8 && contraband.getRiskLevel() >= 4) {
                return new CustomsResult(Outcome.IMPRISONED, roll, target,
                        "Caught with high-risk contraband in a strict law system. Crew arrested.");
            }
            return new CustomsResult(Outcome.CAUGHT, roll, target,
                    "Customs discovers the contraband. Goods confiscated.");
        }
    }

    /**
     * Resolves a bribe attempt.
     */
    private CustomsResult resolveBribe(ContrabandCommodity contraband, int lawLevel, long credits) {
        // Bribe cost scales with risk and law level
        long bribeCost = contraband.getBasePrice() * (lawLevel + 1) / 2;
        int roll = DiceUtil.roll2D6();

        if (credits >= bribeCost && roll <= 8) {
            return new CustomsResult(Outcome.BRIBED, roll, 8,
                    "Bribe of " + bribeCost + " credits accepted. Customs waves you through.", bribeCost);
        } else if (credits >= bribeCost) {
            // Bribe refused — they take the money AND report you
            return new CustomsResult(Outcome.CAUGHT, roll, 8,
                    "Bribe refused. Customs reports you and confiscates goods.", bribeCost);
        } else {
            return new CustomsResult(Outcome.CAUGHT, roll, 8,
                    "Insufficient credits for bribe. Customs inspects and finds contraband.");
        }
    }

    /**
     * Resolves a run attempt.
     */
    private CustomsResult resolveRun(int pilotSkill, int lawLevel) {
        int roll = DiceUtil.roll2D6();
        // Pilot skill helps; higher law = more pursuit
        int target = 6 + pilotSkill - (lawLevel >= 8 ? 2 : lawLevel >= 5 ? 1 : 0);
        boolean success = roll <= target;

        if (success) {
            return new CustomsResult(Outcome.EVADDED, roll, target,
                    "Ship escapes customs pursuit. Contraband safe.");
        } else {
            return new CustomsResult(Outcome.COMBAT, roll, target,
                    "Customs forces engage your ship. Combat initiated.");
        }
    }

    /**
     * Calculates the fine for being caught with contraband.
     */
    public long calculateFine(ContrabandCommodity contraband, int quantity, int lawLevel) {
        long baseFine = contraband.getBasePrice() * quantity;
        // Law level multiplier: strict law = higher fines
        double multiplier = 1.0 + (lawLevel * 0.2);
        return (long) (baseFine * multiplier);
    }

    /**
     * Result of a customs check.
     */
    public static class CustomsResult {
        private final Outcome outcome;
        private final int roll;
        private final int target;
        private final String narrative;
        private final long creditsLost;

        CustomsResult(Outcome outcome, int roll, int target, String narrative) {
            this(outcome, roll, target, narrative, 0);
        }

        CustomsResult(Outcome outcome, int roll, int target, String narrative, long creditsLost) {
            this.outcome = outcome;
            this.roll = roll;
            this.target = target;
            this.narrative = narrative;
            this.creditsLost = creditsLost;
        }

        public Outcome getOutcome() {
            return outcome;
        }

        public int getRoll() {
            return roll;
        }

        public int getTarget() {
            return target;
        }

        public String getNarrative() {
            return narrative;
        }

        public long getCreditsLost() {
            return creditsLost;
        }

        public boolean isSuccess() {
            return outcome == Outcome.EVADDED || outcome == Outcome.BRIBED || outcome == Outcome.NO_INSPECTION;
        }

        @Override
        public String toString() {
            return String.format("Customs: %s — %s", outcome.getDisplayName(), narrative);
        }
    }
}
