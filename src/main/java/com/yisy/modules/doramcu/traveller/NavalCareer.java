package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Naval career progression from Classic Traveller Book 5: High Guard.
 * <p>
 * A naval career consists of multiple tours of duty, each lasting several years.
 * During each tour, an officer gains skills, experience, and may be promoted.
 * Careers can end in retirement, death, or dismissal.
 * </p>
 * <p>
 * Career structure:
 * </p>
 * <ul>
 *   <li>Each tour lasts 4 years (one term)</li>
 *   <li>Officers must pass duty rolls to continue serving</li>
 *   <li>Skills are gained based on assignments and performance</li>
 *   <li>Promotions are based on rank, experience, and performance</li>
 * </ul>
 */
public class NavalCareer {

    /**
     * Possible assignments during a naval career.
     */
    public enum Assignment {
        /**
         * Navigation — piloting and jump navigation.
         */
        NAVIGATION("Navigation", "Stellar Navigation", "Jump Navigation", "Astrogation"),

        /**
         * Engineering — ship systems and maintenance.
         */
        ENGINEERING("Engineering", "Fusion Drive", "Power Plant", "Ship Systems"),

        /**
         * Gunnery — weapons operation and tactics.
         */
        GUNNERY("Gunnery", "Laser Gunnery", "Missile Gunnery", "Targeting"),

        /**
         * Operations — fleet tactics and command.
         */
        OPERATIONS("Operations", "Fleet Tactics", "Command", "Strategy"),

        /**
         * Intelligence — espionage and reconnaissance.
         */
        INTELLIGENCE("Intelligence", "Reconnaissance", "Signals", "Cryptanalysis"),

        /**
         * Medical — ship's medical officer.
         */
        MEDICAL("Medical", "Ship's Doctor", "Surgery", "Medicine"),

        /**
         * Administration — logistics and supply.
         */
        ADMINISTRATION("Administration", "Logistics", "Supply", "Personnel"),

        /**
         * Training — instructor and trainer.
         */
        TRAINING("Training", "Instructor", "Drill", "Tactics");

        private final String name;
        private final String[] skills;

        Assignment(String name, String... skills) {
            this.name = name;
            this.skills = skills;
        }

        public String getName() {
            return name;
        }

        public String[] getSkills() {
            return skills;
        }
    }

    /**
     * Possible career endings.
     */
    public enum CareerEnd {
        /**
         * Normal retirement after completing tours.
         */
        RETIREMENT("Retirement"),

        /**
         * Death in service.
         */
        DEATH("Death in Service"),

        /**
         * Dismissal for misconduct.
         */
        DISMISSAL("Dismissal"),

        /**
         * Medical retirement due to injury or illness.
         */
        MEDICAL_RETIREMENT("Medical Retirement"),

        /**
         * Still serving.
         */
        ACTIVE("Active Duty");

        private final String description;

        CareerEnd(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final NavalOfficer officer;
    private final List<Assignment> assignments;
    private int currentTour;
    private int maxTours;
    private CareerEnd careerEnd;
    private final List<String> careerEvents;

    /**
     * Creates a new naval career for the given officer.
     *
     * @param officer the officer whose career this is
     */
    public NavalCareer(NavalOfficer officer) {
        this.officer = Objects.requireNonNull(officer, "Officer must not be null");
        this.assignments = new ArrayList<>();
        this.currentTour = 0;
        this.maxTours = 6;
        this.careerEnd = CareerEnd.ACTIVE;
        this.careerEvents = new ArrayList<>();
    }

    // ---- Accessors ----

    /**
     * Returns the officer whose career this is.
     *
     * @return the officer
     */
    public NavalOfficer getOfficer() {
        return officer;
    }

    /**
     * Returns the list of assignments (immutable).
     *
     * @return the assignments list
     */
    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    /**
     * Returns the current tour number.
     *
     * @return the current tour
     */
    public int getCurrentTour() {
        return currentTour;
    }

    /**
     * Returns the maximum number of tours.
     *
     * @return the max tours
     */
    public int getMaxTours() {
        return maxTours;
    }

    /**
     * Sets the maximum number of tours.
     *
     * @param maxTours the max tours
     */
    public void setMaxTours(int maxTours) {
        this.maxTours = maxTours;
    }

    /**
     * Returns the career end status.
     *
     * @return the career end
     */
    public CareerEnd getCareerEnd() {
        return careerEnd;
    }

    /**
     * Returns the career events log (immutable).
     *
     * @return the career events
     */
    public List<String> getCareerEvents() {
        return Collections.unmodifiableList(careerEvents);
    }

    // ---- Career Progression ----

    /**
     * Starts a new tour of duty with the given assignment.
     *
     * @param assignment the assignment for this tour
     * @return true if the tour was started, false if career has ended
     */
    public boolean startTour(Assignment assignment) {
        if (careerEnd != CareerEnd.ACTIVE) {
            return false;
        }
        if (currentTour >= maxTours) {
            careerEnd = CareerEnd.RETIREMENT;
            careerEvents.add("Career ended: " + careerEnd.getDescription());
            officer.retire();
            return false;
        }
        currentTour++;
        assignments.add(assignment);
        // Gain skills from assignment
        for (String skill : assignment.getSkills()) {
            officer.addSkill(skill);
        }
        careerEvents.add("Tour " + currentTour + ": " + assignment.getName());
        return true;
    }

    /**
     * Completes the current tour and processes results.
     *
     * @return true if the tour was completed successfully
     */
    public boolean completeTour() {
        if (careerEnd != CareerEnd.ACTIVE) {
            return false;
        }
        officer.completeTour();
        // Check for promotion
        if (shouldPromote()) {
            officer.promote();
            careerEvents.add("Promotion after tour " + currentTour);
        }
        // Check if career should end
        if (currentTour >= maxTours) {
            careerEnd = CareerEnd.RETIREMENT;
            careerEvents.add("Career ended: " + careerEnd.getDescription());
            officer.retire();
        }
        return true;
    }

    /**
     * Determines if the officer should be promoted.
     * <p>
     * Promotion is based on rank, tours completed, and combat experience.
     * </p>
     *
     * @return true if the officer should be promoted
     */
    private boolean shouldPromote() {
        // Higher ranks need more tours for promotion
        int rankLevel = officer.getRank().getLevel();
        int toursNeeded = (rankLevel / 2) + 1;
        return currentTour % toursNeeded == 0 && currentTour > 0;
    }

    /**
     * Records a battle during the career.
     *
     * @param victory true if the battle was won
     */
    public void recordBattle(boolean victory) {
        if (careerEnd == CareerEnd.ACTIVE) {
            officer.recordBattle(victory);
            careerEvents.add("Battle: " + (victory ? "Victory" : "Defeat"));
            // Death check — 1 in 20 chance per battle lost
            if (!victory && Math.random() < 0.05) {
                careerEnd = CareerEnd.DEATH;
                careerEvents.add("Career ended: " + careerEnd.getDescription());
            }
        }
    }

    /**
     * Returns the total years of service.
     * <p>
     * Each tour is 4 years.
     * </p>
     *
     * @return the total years
     */
    public int getTotalYears() {
        return currentTour * 4;
    }

    /**
     * Returns the officer's final rank.
     *
     * @return the final rank
     */
    public NavalRank getFinalRank() {
        return officer.getRank();
    }

    /**
     * Returns a summary of the career.
     *
     * @return the career summary
     */
    public String getCareerSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(officer.getName()).append(": ");
        sb.append(currentTour).append(" tours, ");
        sb.append(getTotalYears()).append(" years, ");
        sb.append("Final rank: ").append(officer.getRank().getTitle());
        sb.append(", Status: ").append(careerEnd.getDescription());
        return sb.toString();
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return getCareerSummary();
    }
}
