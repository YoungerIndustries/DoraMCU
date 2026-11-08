package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A naval officer in the Imperial Navy from Classic Traveller Book 5: High Guard.
 * <p>
 * Each officer has a rank, a set of skills, and a current assignment.
 * Officers progress through the ranks by completing tours of duty and
 * demonstrating competence.
 * </p>
 * <p>
 * Officers can serve as:
 * </p>
 * <ul>
 *   <li>Commanding officers of individual ships</li>
 *   <li>Executive officers (XO) under a commanding officer</li>
 *   <li>Departmental heads (navigation, engineering, gunnery, etc.)</li>
 *   <li>Squadron or fleet commanders (flag officers)</li>
 * </ul>
 */
public class NavalOfficer {

    private final int id;
    private String name;
    private NavalRank rank;
    private final List<String> skills;
    private String currentAssignment;
    private String currentShip;
    private int toursOfDuty;
    private int combatExperience;
    private final List<String> serviceHistory;
    private boolean isRetired;

    /**
     * Creates a new naval officer with the given ID, name, and starting rank.
     *
     * @param id the unique officer identifier
     * @param name the officer's name
     * @param rank the starting rank
     */
    public NavalOfficer(int id, String name, NavalRank rank) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.rank = Objects.requireNonNull(rank, "Rank must not be null");
        this.skills = new ArrayList<>();
        this.currentAssignment = "Unassigned";
        this.currentShip = null;
        this.toursOfDuty = 0;
        this.combatExperience = 0;
        this.serviceHistory = new ArrayList<>();
        this.isRetired = false;
    }

    // ---- Accessors ----

    /**
     * Returns the officer's unique ID.
     *
     * @return the officer ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the officer's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the officer's name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
    }

    /**
     * Returns the officer's current rank.
     *
     * @return the rank
     */
    public NavalRank getRank() {
        return rank;
    }

    /**
     * Promotes the officer to the next rank.
     *
     * @return true if promotion was successful, false if already at the top
     */
    public boolean promote() {
        if (isRetired) {
            return false;
        }
        NavalRank next = rank.nextRank();
        if (next != null) {
            rank = next;
            serviceHistory.add("Promoted to " + rank.getTitle());
            return true;
        }
        return false;
    }

    /**
     * Demotes the officer to the previous rank.
     *
     * @return true if demotion was successful, false if already at the bottom
     */
    public boolean demote() {
        if (isRetired) {
            return false;
        }
        NavalRank prev = rank.previousRank();
        if (prev != null) {
            rank = prev;
            serviceHistory.add("Demoted to " + rank.getTitle());
            return true;
        }
        return false;
    }

    /**
     * Sets the officer's rank directly.
     *
     * @param rank the new rank
     */
    public void setRank(NavalRank rank) {
        this.rank = Objects.requireNonNull(rank, "Rank must not be null");
        serviceHistory.add("Rank changed to " + rank.getTitle());
    }

    /**
     * Returns the officer's skills (immutable list).
     *
     * @return the skills list
     */
    public List<String> getSkills() {
        return Collections.unmodifiableList(skills);
    }

    /**
     * Adds a skill to the officer's skill set.
     *
     * @param skill the skill to add
     * @return true if the skill was added, false if already present
     */
    public boolean addSkill(String skill) {
        Objects.requireNonNull(skill, "Skill must not be null");
        if (skills.contains(skill)) {
            return false;
        }
        skills.add(skill);
        return true;
    }

    /**
     * Checks if the officer has the given skill.
     *
     * @param skill the skill to check
     * @return true if the officer has the skill
     */
    public boolean hasSkill(String skill) {
        return skills.contains(skill);
    }

    /**
     * Returns the officer's current assignment.
     *
     * @return the assignment description
     */
    public String getCurrentAssignment() {
        return currentAssignment;
    }

    /**
     * Sets the officer's current assignment.
     *
     * @param assignment the new assignment
     */
    public void setCurrentAssignment(String assignment) {
        this.currentAssignment = assignment;
        if (assignment != null) {
            serviceHistory.add("Assigned to: " + assignment);
        }
    }

    /**
     * Returns the ship the officer is currently assigned to, or null if unassigned.
     *
     * @return the ship name or null
     */
    public String getCurrentShip() {
        return currentShip;
    }

    /**
     * Assigns the officer to a ship.
     *
     * @param ship the ship name
     */
    public void assignToShip(String ship) {
        this.currentShip = Objects.requireNonNull(ship, "Ship must not be null");
        serviceHistory.add("Assigned to ship: " + ship);
    }

    /**
     * Removes the officer from their current ship assignment.
     */
    public void removeFromShip() {
        if (currentShip != null) {
            serviceHistory.add("Removed from ship: " + currentShip);
            currentShip = null;
        }
    }

    /**
     * Returns the number of tours of duty completed.
     *
     * @return the number of tours
     */
    public int getToursOfDuty() {
        return toursOfDuty;
    }

    /**
     * Records a completed tour of duty.
     */
    public void completeTour() {
        if (!isRetired) {
            toursOfDuty++;
            serviceHistory.add("Completed tour " + toursOfDuty);
        }
    }

    /**
     * Returns the officer's combat experience rating.
     * <p>
     * Combat experience increases with each battle participated in.
     * Higher combat experience improves the officer's effectiveness
     * in combat situations.
     * </p>
     *
     * @return the combat experience rating
     */
    public int getCombatExperience() {
        return combatExperience;
    }

    /**
     * Records participation in a battle.
     *
     * @param victory true if the battle was won
     */
    public void recordBattle(boolean victory) {
        if (!isRetired) {
            combatExperience++;
            serviceHistory.add("Battle " + combatExperience + ": " + (victory ? "Victory" : "Defeat"));
        }
    }

    /**
     * Returns the officer's service history (immutable list).
     *
     * @return the service history
     */
    public List<String> getServiceHistory() {
        return Collections.unmodifiableList(serviceHistory);
    }

    /**
     * Checks if the officer is retired.
     *
     * @return true if retired
     */
    public boolean isRetired() {
        return isRetired;
    }

    /**
     * Retires the officer.
     */
    public void retire() {
        isRetired = true;
        serviceHistory.add("Retired from service");
    }

    /**
     * Checks if the officer can command a ship of the given tonnage.
     *
     * @param tonnage the ship's tonnage
     * @return true if the officer can command the ship
     */
    public boolean canCommandShip(int tonnage) {
        return rank.canCommandShip(tonnage);
    }

    /**
     * Checks if the officer is a flag officer (can command squadrons or fleets).
     *
     * @return true if flag officer
     */
    public boolean isFlagOfficer() {
        return rank.isFlagOfficer();
    }

    /**
     * Returns the officer's competence rating.
     * <p>
     * Competence is derived from rank, skills, and combat experience.
     * Higher competence improves the officer's effectiveness in all roles.
     * </p>
     *
     * @return the competence rating (0–12)
     */
    public int getCompetenceRating() {
        int rating = rank.getLevel() / 2;
        rating += Math.min(skills.size(), 4);
        rating += Math.min(combatExperience / 3, 2);
        return Math.min(rating, 12);
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rank.getTitle()).append(" ").append(name);
        if (currentShip != null) {
            sb.append(" (").append(currentShip).append(")");
        }
        if (isRetired) {
            sb.append(" [Retired]");
        }
        return sb.toString();
    }
}
