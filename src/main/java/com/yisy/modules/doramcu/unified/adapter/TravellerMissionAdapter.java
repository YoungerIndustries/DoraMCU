package com.yisy.modules.doramcu.unified.adapter;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogEntry;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.Career;
import com.yisy.modules.doramcu.traveller.ExplorationActivity;
import com.yisy.modules.doramcu.traveller.MercenaryTicket;
import com.yisy.modules.doramcu.traveller.MissionType;
import com.yisy.modules.doramcu.traveller.Skill;
import com.yisy.modules.doramcu.traveller.TradeMission;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import com.yisy.modules.doramcu.traveller.TravellerWorld;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Adapter that integrates Traveller gameplay with the Captain's Log system.
 * <p>
 * When a Traveller character plays through careers, mercenary contracts, trade missions,
 * or exploration activities, this adapter records each event as a Captain's Log mission
 * with a proper 3-act structure and detailed log entries.
 * </p>
 * <p>
 * The adapter works in two modes:
 * </p>
 * <ul>
 *   <li><b>Direct mode</b> — builds Captain's Log mission objects in memory (no persistence)</li>
 *   <li><b>Service mode</b> — uses a callback to persist missions via CaptainLogService</li>
 * </ul>
 * <p>
 * Usage example (direct mode):
 * </p>
 * <pre>
 * TravellerMissionAdapter adapter = new TravellerMissionAdapter();
 *
 * // Convert a career term to a mission
 * CaptainLogMission mission = adapter.adaptCareerTerm(character, tour, 1);
 *
 * // Convert a mercenary contract result to a mission
 * CaptainLogMission mission = adapter.adaptMercenaryContract(character, contract, result);
 *
 * // Convert a trade mission to a mission
 * CaptainLogMission mission = adapter.adaptTradeMission(character, tradeMission);
 * </pre>
 * <p>
 * Usage example (service mode with persistence):
 * </p>
 * <pre>
 * TravellerMissionAdapter adapter = new TravellerMissionAdapter(clCharacter.getId(),
 *     (mission) -> captainLogService.createMission(...),
 *     (missionId, act, text) -> captainLogService.updateMissionLog(missionId, act, text),
 *     (missionId, summary, dev) -> captainLogService.completeMission(missionId, summary, dev)
 * );
 * </pre>
 */
public class TravellerMissionAdapter {

    private final Long clCharacterId;
    private final Consumer<CaptainLogMission> missionCreator;
    private final BiConsumer<Long, String> missionLogUpdater;
    private final TriConsumer<Long, String, String> missionCompleter;
    private final boolean useService;

    /**
     * Functional interface for creating a mission.
     */
    @FunctionalInterface
    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }

    /**
     * Functional interface for completing a mission.
     */
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    /**
     * Creates a direct-mode adapter that builds mission objects in memory.
     * <p>
     * Use this mode for testing or when you want to collect missions
     * and persist them yourself.
     * </p>
     */
    public TravellerMissionAdapter() {
        this.clCharacterId = null;
        this.missionCreator = null;
        this.missionLogUpdater = null;
        this.missionCompleter = null;
        this.useService = false;
    }

    /**
     * Creates a service-mode adapter that persists missions via callbacks.
     *
     * @param clCharacterId the Captain's Log character ID
     * @param missionCreator callback to create a mission (receives the mission object)
     * @param missionLogUpdater callback to update act logs (receives mission ID and act text)
     * @param missionCompleter callback to complete a mission (receives mission ID, summary, development)
     */
    public TravellerMissionAdapter(Long clCharacterId,
                                    Consumer<CaptainLogMission> missionCreator,
                                    BiConsumer<Long, String> missionLogUpdater,
                                    TriConsumer<Long, String, String> missionCompleter) {
        this.clCharacterId = clCharacterId;
        this.missionCreator = missionCreator;
        this.missionLogUpdater = missionLogUpdater;
        this.missionCompleter = missionCompleter;
        this.useService = true;
    }

    // ============================================================
    // Career Term Adaptation
    // ============================================================

    /**
     * Converts a Traveller career term into a Captain's Log mission.
     * <p>
     * Each career term becomes a mission with:
     * </p>
     * <ul>
     *   <li>Act 1: The character begins the term, describing the assignment</li>
     *   <li>Act 2: Skills learned, promotions, and key events during the term</li>
     *   <li>Act 3: Term completion, retirement decision, and wealth earned</li>
     * </ul>
     *
     * @param character the Traveller character
     * @param tour the career tour to adapt
     * @param termNumber the 1-based term number in the career
     * @return a fully populated Captain's Log mission
     */
    public CaptainLogMission adaptCareerTerm(TravellerCharacter character,
                                              TravellerCharacter.CareerTour tour,
                                              int termNumber) {
        String characterName = character.getName();
        Career career = tour.getCareer();
        String rank = tour.getRank();
        List<Skill> skills = tour.getSkillsLearned();
        boolean retired = tour.isRetired();
        TravellerCharacter.RetirementType retirementType = tour.getRetirementType();
        long wealth = tour.getWealthEarned();

        // Build mission
        CaptainLogMission mission = new CaptainLogMission();
        mission.setMissionNumber(termNumber);
        mission.setName(TravellerToCaptainsLogConverter.generateCareerMissionName(career, rank, termNumber));
        mission.setType(mapCareerToMissionType(career));
        mission.setStatus(TravellerToCaptainsLogConverter.mapRetirementToStatus(retirementType));
        mission.setCurrentAct(3);
        mission.setCreatedAt(LocalDateTime.now());

        // Act 1 — Opening Log
        String act1 = TravellerToCaptainsLogConverter.generateCareerAct1(
                characterName, career, rank, termNumber);
        mission.setAct1Log(act1);

        // Act 2 — Supplementary Logs
        String act2 = TravellerToCaptainsLogConverter.generateCareerAct2(
                skills, false, null);
        mission.setAct2Log(act2);

        // Act 3 — Closing Log
        String act3 = TravellerToCaptainsLogConverter.generateCareerAct3(
                characterName, retired, retirementType, wealth);
        mission.setAct3Log(act3);

        // Summary and development
        mission.setSummary(String.format(
            "%s completed term %d of %s service at rank %s, "
            + "earning %d credits and learning %d new skills.",
            characterName, termNumber, career.getName(), rank, wealth, skills.size()
        ));
        mission.setDevelopment(TravellerToCaptainsLogConverter.generateCareerDevelopment(skills, false));

        // Values used — based on career
        mission.setValuesUsed(mapCareerToValues(career));

        // Career event flag for every 3rd term
        mission.setCareerEvent(termNumber % 3 == 0);
        if (mission.isCareerEvent()) {
            mission.setCareerEventDescription(generateCareerEventDescription(termNumber, career, retired));
        }

        // Log entries
        List<CaptainLogEntry> entries = new ArrayList<>();

        // Act 1 entry — narrative
        entries.add(new CaptainLogEntry(1, CaptainLogEntry.EntryType.NARRATIVE,
                String.format("Assignment to %s — Term %d begins.", career.getName(), termNumber)));

        // Act 2 entries — skill checks for each skill learned
        for (Skill skill : skills) {
            CaptainLogEntry entry = new CaptainLogEntry(2, CaptainLogEntry.EntryType.SKILL_CHECK,
                    String.format("Training in %s completed successfully.", skill.getName()));
            entry.setDiscipline(skill.getName());
            entry.setSuccess(true);
            entries.add(entry);
        }

        // Act 3 entry — narrative conclusion
        entries.add(new CaptainLogEntry(3, CaptainLogEntry.EntryType.NARRATIVE,
                retired
                        ? String.format("Service concluded with %s retirement.",
                                retirementType != null ? retirementType.getName() : "normal")
                        : "Term completed — preparing for the next assignment."));

        mission.setLogEntries(entries);

        if (retired) {
            mission.setCompletedAt(LocalDateTime.now());
        }

        // Persist if using service mode
        if (useService && missionCreator != null) {
            missionCreator.accept(mission);
        }

        return mission;
    }

    /**
     * Converts a full career (all terms) into multiple Captain's Log missions.
     *
     * @param character the Traveller character
     * @return list of missions, one per career term
     */
    public List<CaptainLogMission> adaptFullCareer(TravellerCharacter character) {
        List<CaptainLogMission> missions = new ArrayList<>();
        List<TravellerCharacter.CareerTour> tours = character.getCareerTours();

        int termNumber = 0;
        for (TravellerCharacter.CareerTour tour : tours) {
            termNumber++;
            CaptainLogMission mission = adaptCareerTerm(character, tour, termNumber);
            missions.add(mission);
        }

        return missions;
    }

    // ============================================================
    // Mercenary Contract Adaptation
    // ============================================================

    /**
     * Converts a mercenary contract result into a Captain's Log mission.
     * <p>
     * The mission captures the full arc of the mercenary operation:
     * </p>
     * <ul>
     *   <li>Act 1: Contract acceptance and deployment</li>
     *   <li>Act 2: Combat and mission execution</li>
     *   <li>Act 3: Resolution, profit/casualties, and aftermath</li>
     * </ul>
     *
     * @param character the Traveller character leading the mission
     * @param ticket the mercenary ticket
     * @param success whether the mission succeeded
     * @param profit the profit earned
     * @param casualties the number of casualties
     * @param strength the strength committed
     * @return a fully populated Captain's Log mission
     */
    public CaptainLogMission adaptMercenaryContract(TravellerCharacter character,
                                                     MercenaryTicket ticket,
                                                     boolean success,
                                                     long profit,
                                                     int casualties,
                                                     int strength) {
        String characterName = character.getName();
        MissionType missionType = ticket.getMissionType();
        String world = ticket.getWorld();
        String patron = ticket.getPatron();

        CaptainLogMission mission = new CaptainLogMission();
        mission.setName(TravellerToCaptainsLogConverter.generateMercenaryMissionName(missionType, world));
        mission.setType(TravellerToCaptainsLogConverter.mapMissionType(missionType));
        mission.setStatus(success ? CaptainLogMission.MissionStatus.COMPLETED
                : CaptainLogMission.MissionStatus.ABANDONED);
        mission.setCurrentAct(3);
        mission.setCreatedAt(LocalDateTime.now());
        mission.setCompletedAt(LocalDateTime.now());

        // Act 1 — Contract acceptance
        mission.setAct1Log(String.format(
            "%s accepts a %s contract from %s for operations on %s. "
            + "A force of %d troops is committed to the mission. "
            + "The base payment is %d credits with a success bonus of %d credits.",
            characterName, missionType.getName().toLowerCase(), patron, world,
            strength, ticket.getBasePayment(), ticket.getSuccessBonus()
        ));

        // Act 2 — Combat
        String combatDescription;
        if (success) {
            combatDescription = String.format(
                "The mission proceeds with determination. %s leads the force through intense combat, "
                + "overcoming the enemy's defenses. Despite suffering %d casualties, "
                + "the objective is secured. The force demonstrates exceptional discipline and courage.",
                characterName, casualties
            );
        } else {
            combatDescription = String.format(
                "The mission encounters fierce resistance. %s fights valiantly, "
                + "but the enemy proves too strong. %d casualties are sustained "
                + "before a tactical withdrawal is ordered. The lessons learned will inform future operations.",
                characterName, casualties
            );
        }
        mission.setAct2Log(combatDescription);

        // Act 3 — Resolution
        String resolution;
        if (success) {
            resolution = String.format(
                "Mission success! The contract is fulfilled, earning %d credits in profit. "
                + "%s's leadership is praised by the patron. The force returns to base "
                + "to regroup and recover from the %d casualties sustained.",
                profit, characterName, casualties
            );
        } else {
            resolution = String.format(
                "The mission ends in failure. The contract yields a net result of %d credits. "
                + "%d casualties are a heavy price. %s reflects on what went wrong "
                + "and what can be improved for the next engagement.",
                profit, casualties, characterName
            );
        }
        mission.setAct3Log(resolution);

        // Summary
        mission.setSummary(String.format(
            "%s %s contract on %s: %s. Profit: %d cr. Casualties: %d.",
            characterName, missionType.getName(), world,
            success ? "SUCCESS" : "FAILURE", profit, casualties
        ));

        // Development
        mission.setDevelopment(String.format(
            "%s combat experience. %d casualties sustained. "
            + "Leadership under fire tested.",
            success ? "Proven" : "Hard-earned", casualties
        ));

        // Values
        mission.setValuesUsed(success ? "Duty, Courage, Honor" : "Survival, Resilience");

        // Log entries
        List<CaptainLogEntry> entries = new ArrayList<>();

        entries.add(new CaptainLogEntry(1, CaptainLogEntry.EntryType.NARRATIVE,
                String.format("Contract accepted: %s on %s for patron %s.",
                        missionType.getName(), world, patron)));

        // Combat entry in Act 2
        CaptainLogEntry combatEntry = new CaptainLogEntry(2, CaptainLogEntry.EntryType.COMBAT,
                String.format("Engagement on %s — %s with %d casualties.",
                        world, success ? "victory achieved" : "defeat suffered", casualties));
        combatEntry.setSuccess(success);
        entries.add(combatEntry);

        entries.add(new CaptainLogEntry(3, CaptainLogEntry.EntryType.NARRATIVE,
                String.format("Mission concluded — %s: %d credits, %d casualties.",
                        success ? "profit earned" : "loss incurred", profit, casualties)));

        mission.setLogEntries(entries);

        if (useService && missionCreator != null) {
            missionCreator.accept(mission);
        }

        return mission;
    }

    // ============================================================
    // Trade Mission Adaptation
    // ============================================================

    /**
     * Converts a completed TradeMission into a Captain's Log mission.
     * <p>
     * The mission captures the trade run:
     * </p>
     * <ul>
     *   <li>Act 1: Departure and cargo loading at origin</li>
     *   <li>Act 2: Travel and events en route</li>
     *   <li>Act 3: Arrival, unloading, and profit calculation</li>
     * </ul>
     *
     * @param character the Traveller character conducting the trade
     * @param tradeMission the completed trade mission
     * @return a fully populated Captain's Log mission
     */
    public CaptainLogMission adaptTradeMission(TravellerCharacter character, TradeMission tradeMission) {
        String characterName = character.getName();
        String origin = tradeMission.getOrigin().getName();
        String destination = tradeMission.getDestination().getName();
        long profit = tradeMission.getNetProfit();
        boolean profitable = profit >= 0;
        double roi = tradeMission.getReturnOnInvestment();
        List<String> events = tradeMission.getEvents();

        CaptainLogMission mission = new CaptainLogMission();
        mission.setName(TravellerToCaptainsLogConverter.generateTradeMissionName(origin, destination));
        mission.setType(CaptainLogMission.MissionType.TRADE);
        mission.setStatus(mapTradeStatus(tradeMission.getStatus()));
        mission.setCurrentAct(3);
        mission.setCreatedAt(LocalDateTime.now());
        mission.setCompletedAt(LocalDateTime.now());

        // Act 1 — Departure
        StringBuilder act1 = new StringBuilder();
        act1.append(characterName).append(" prepares for a trade run from ")
            .append(origin).append(" to ").append(destination).append(". ");
        act1.append("The ship's cargo hold is loaded with the most profitable commodities available. ");

        // Add loading events
        for (String event : events) {
            if (event.contains("Bought") || event.contains("Cargo loaded") || event.contains("Arrived at")) {
                if (act1.length() < 500) {
                    act1.append(event).append(" ");
                }
            }
        }
        mission.setAct1Log(act1.toString());

        // Act 2 — Travel
        StringBuilder act2 = new StringBuilder();
        act2.append("The journey from ").append(origin).append(" to ").append(destination).append(" begins. ");
        for (String event : events) {
            if (event.contains("Departing") || event.contains("Travel cost") || event.contains("Arrived")) {
                act2.append(event).append(" ");
            }
        }
        mission.setAct2Log(act2.toString());

        // Act 3 — Arrival and profit
        StringBuilder act3 = new StringBuilder();
        act3.append("Arriving at ").append(destination).append(", cargo is unloaded and sold. ");
        for (String event : events) {
            if (event.contains("Sold") || event.contains("revenue") || event.contains("profit")
                    || event.contains("Discarded")) {
                act3.append(event).append(" ");
            }
        }
        act3.append(String.format("Net result: %d credits (%.1f%% ROI).", profit, roi));
        mission.setAct3Log(act3.toString());

        // Summary
        mission.setSummary(String.format(
            "Trade run %s → %s: %s. Net profit: %d cr (%.1f%% ROI).",
            origin, destination,
            profitable ? "profitable" : "at a loss", profit, roi
        ));

        // Development
        mission.setDevelopment(String.format(
            "Trade experience gained. %s. Commercial acumen %s.",
            profitable ? "Profitable venture builds capital" : "Losses teach valuable lessons",
            profitable ? "improves" : "needs refinement"
        ));

        // Values
        mission.setValuesUsed(profitable ? "Enterprise, Prudence" : "Resilience, Adaptability");

        // Log entries
        List<CaptainLogEntry> entries = new ArrayList<>();

        entries.add(new CaptainLogEntry(1, CaptainLogEntry.EntryType.NARRATIVE,
                String.format("Departing %s bound for %s with loaded cargo.", origin, destination)));

        entries.add(new CaptainLogEntry(2, CaptainLogEntry.EntryType.NARRATIVE,
                String.format("Transit complete — arrived at %s.", destination)));

        // Discovery entry if profitable
        if (profitable) {
            entries.add(new CaptainLogEntry(3, CaptainLogEntry.EntryType.DISCOVERY,
                    String.format("Trade successful — %d credits profit.", profit)));
        } else {
            entries.add(new CaptainLogEntry(3, CaptainLogEntry.EntryType.NARRATIVE,
                    String.format("Trade completed at a loss — %d credits.", Math.abs(profit))));
        }

        mission.setLogEntries(entries);

        if (useService && missionCreator != null) {
            missionCreator.accept(mission);
        }

        return mission;
    }

    // ============================================================
    // Exploration Activity Adaptation
    // ============================================================

    /**
     * Converts an exploration activity into a Captain's Log mission.
     * <p>
     * The mission captures the exploration:
     * </p>
     * <ul>
     *   <li>Act 1: Arrival at the system and survey preparation</li>
     *   <li>Act 2: The exploration activity and any discoveries</li>
     *   <li>Act 3: Results analysis and recommendations</li>
     * </ul>
     *
     * @param character the Traveller character conducting the exploration
     * @param activity the completed exploration activity
     * @return a fully populated Captain's Log mission
     */
    public CaptainLogMission adaptExplorationActivity(TravellerCharacter character,
                                                       ExplorationActivity activity) {
        String characterName = character.getName();
        String target = activity.getTarget();
        String system = activity.getStarSystem().getName();
        boolean success = activity.isSuccessful();
        ExplorationActivity.Discovery discovery = activity.getDiscovery();
        String notes = activity.getNotes();

        CaptainLogMission mission = new CaptainLogMission();
        mission.setName(TravellerToCaptainsLogConverter.generateExplorationMissionName(
                activity.getType(), target));
        mission.setType(TravellerToCaptainsLogConverter.mapExplorationType(activity.getType()));
        mission.setStatus(success ? CaptainLogMission.MissionStatus.COMPLETED
                : CaptainLogMission.MissionStatus.ABANDONED);
        mission.setCurrentAct(3);
        mission.setCreatedAt(LocalDateTime.now());
        mission.setCompletedAt(LocalDateTime.now());

        // Act 1 — Arrival
        mission.setAct1Log(String.format(
            "%s arrives at the %s system to conduct %s on %s. "
            + "The ship's sensors are calibrated and the crew prepares for %d weeks of work.",
            characterName, system,
            activity.getType().name().replace("_", " ").toLowerCase(),
            target, activity.getWeeksRequired()
        ));

        // Act 2 — Exploration
        String act2;
        if (success) {
            act2 = String.format(
                "The exploration proceeds successfully. %s leads the team through careful analysis. "
                + "%s %s",
                characterName,
                discovery != ExplorationActivity.Discovery.NONE
                        ? String.format("A significant discovery is made: %s.", discovery.name().replace("_", " ").toLowerCase())
                        : "The data is collected and analyzed.",
                notes != null && !notes.isEmpty() ? notes : ""
            );
        } else {
            act2 = String.format(
                "The exploration encounters difficulties. Despite %s's best efforts, "
                + "the data collected is insufficient for definitive conclusions. "
                + "A return mission may be necessary.",
                characterName
            );
        }
        mission.setAct2Log(act2);

        // Act 3 — Results
        String act3;
        if (success && discovery != ExplorationActivity.Discovery.NONE) {
            act3 = String.format(
                "Mission complete. The discovery of %s in the %s system "
                + "opens new possibilities for future exploration and development. "
                + "%s's report is filed for review.",
                discovery.name().replace("_", " ").toLowerCase(), system, characterName
            );
        } else if (success) {
            act3 = String.format(
                "The exploration concludes without major findings, but the data "
                + "contributes to the overall understanding of the %s system.",
                system
            );
        } else {
            act3 = String.format(
                "The mission ends without achieving its objectives. "
                + "%s recommends further study with improved equipment or a larger team.",
                characterName
            );
        }
        mission.setAct3Log(act3);

        // Summary
        mission.setSummary(String.format(
            "%s of %s in %s system: %s. Discovery: %s.",
            activity.getType().name().replace("_", " "),
            target, system,
            success ? "SUCCESS" : "FAILED",
            discovery != ExplorationActivity.Discovery.NONE ? discovery.name().replace("_", " ") : "None"
        ));

        // Development
        mission.setDevelopment(String.format(
            "Exploration experience gained. %s",
            discovery != ExplorationActivity.Discovery.NONE
                    ? "Scientific knowledge expanded."
                    : "Field methods refined."
        ));

        // Values
        mission.setValuesUsed("Curiosity, Knowledge, Discovery");

        // Log entries
        List<CaptainLogEntry> entries = new ArrayList<>();

        entries.add(new CaptainLogEntry(1, CaptainLogEntry.EntryType.NARRATIVE,
                String.format("Arrived at %s system — %s begins.",
                        system, activity.getType().name().replace("_", " "))));

        if (success && discovery != ExplorationActivity.Discovery.NONE) {
            CaptainLogEntry discoveryEntry = new CaptainLogEntry(2, CaptainLogEntry.EntryType.DISCOVERY,
                    String.format("Discovered: %s — %s",
                            discovery.name().replace("_", " "), notes));
            entries.add(discoveryEntry);
        } else {
            entries.add(new CaptainLogEntry(2, CaptainLogEntry.EntryType.NARRATIVE,
                    success ? "Exploration completed — no significant findings."
                            : "Exploration failed — insufficient data."));
        }

        entries.add(new CaptainLogEntry(3, CaptainLogEntry.EntryType.NARRATIVE,
                String.format("Mission concluded — %s.",
                        success ? "data collected" : "mission aborted")));

        mission.setLogEntries(entries);

        if (useService && missionCreator != null) {
            missionCreator.accept(mission);
        }

        return mission;
    }

    // ============================================================
    // Character Creation Adaptation
    // ============================================================

    /**
     * Creates a Captain's Log character from a Traveller character.
     * <p>
     * Maps all abilities, careers, and skills to the Captain's Log format.
     * </p>
     *
     * @param travellerCharacter the Traveller character
     * @return a new Captain's Log character (not persisted)
     */
    public CaptainLogCharacter adaptCharacterCreation(TravellerCharacter travellerCharacter) {
        CaptainLogCharacter clChar = new CaptainLogCharacter();
        clChar.setName(travellerCharacter.getName());

        // Map abilities to attributes
        for (Map.Entry<AbilityScore, Integer> entry : travellerCharacter.getAbilities().entrySet()) {
            String attrName = TravellerToCaptainsLogConverter.mapAbilityToAttribute(entry.getKey());
            int value = entry.getValue();
            setAttribute(clChar, attrName, value);
        }

        // Map primary career to assignment
        Career primaryCareer = travellerCharacter.getPrimaryCareer();
        if (primaryCareer != null) {
            clChar.setAssignment(TravellerToCaptainsLogConverter.mapCareerToAssignment(primaryCareer));
        } else {
            clChar.setAssignment(CaptainLogCharacter.Assignment.COMMAND);
        }

        // Map highest rank achieved
        String highestRank = "Enlisted";
        for (TravellerCharacter.CareerTour tour : travellerCharacter.getCareerTours()) {
            highestRank = tour.getRank();
        }
        clChar.setRank(TravellerToCaptainsLogConverter.mapRank(highestRank));

        // Set constitution from END and INT average (matching CaptainLogService logic)
        int end = travellerCharacter.getAbility(AbilityScore.END);
        int intScore = travellerCharacter.getAbility(AbilityScore.INT);
        if (end > 0 && intScore > 0) {
            clChar.setConstitution((end + intScore) / 2);
        }

        // Map skills to disciplines
        assignDisciplinesFromSkills(clChar, travellerCharacter.getSkills());

        // Set career events and missions from tours
        clChar.setCareerEvents((int) travellerCharacter.getCareerTours().stream()
                .filter(TravellerCharacter.CareerTour::isRetired).count());
        clChar.setMissionsCompleted(travellerCharacter.getCareerTours().size());

        return clChar;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private CaptainLogMission.MissionType mapCareerToMissionType(Career career) {
        return TravellerToCaptainsLogConverter.mapCareerToMissionType(career);
    }

    private CaptainLogMission.MissionStatus mapTradeStatus(TradeMission.Status status) {
        return switch (status) {
            case COMPLETED -> CaptainLogMission.MissionStatus.COMPLETED;
            case COMPLETED_WITH_LOSS -> CaptainLogMission.MissionStatus.COMPLETED;
            case FAILED -> CaptainLogMission.MissionStatus.ABANDONED;
            default -> CaptainLogMission.MissionStatus.IN_PROGRESS;
        };
    }

    private String mapCareerToValues(Career career) {
        return switch (career) {
            case SOLDIER -> "Duty, Courage, Discipline";
            case PILOT -> "Freedom, Exploration, Skill";
            case GUNNER -> "Precision, Vigilance, Honor";
            case EXPLORER -> "Curiosity, Knowledge, Discovery";
            case MERCHANT -> "Enterprise, Prudence, Prosperity";
            case NOBLE -> "Honor, Leadership, Tradition";
            case SCOUT -> "Independence, Discovery, Resilience";
            case SPY -> "Secrecy, Cunning, Adaptability";
        };
    }

    private String generateCareerEventDescription(int termNumber, Career career, boolean retired) {
        String[] events = {
            "Promotion: Exceptional service recognized with advancement.",
            "New Assignment: Transferred to a challenging new post.",
            "Field Experience: Combat or crisis testing met with resolve.",
            "Mentorship: A senior officer offers guidance and wisdom.",
            "Reputation: Actions have made a name known across the sector.",
            "Award: Commendation for outstanding performance.",
            "Scar: A physical or emotional mark from past service remains.",
            "Rival: A fellow officer challenges methods and decisions.",
            "New Technology: Assigned to test experimental equipment.",
            "Crew Bond: The team has become like family."
        };
        return events[(termNumber - 1) % events.length];
    }

    private void setAttribute(CaptainLogCharacter clChar, String attrName, int value) {
        switch (attrName) {
            case "strength" -> clChar.setStrength(value);
            case "dexterity" -> clChar.setFitness(value);
            case "constitution" -> clChar.setConstitution(value);
            case "intelligence" -> clChar.setReason(value);
            case "wisdom" -> clChar.setInsight(value);
            case "charisma" -> clChar.setPresence(value);
            case "technical" -> clChar.setTechnical(value);
        }
    }

    private void assignDisciplinesFromSkills(CaptainLogCharacter clChar, java.util.Set<Skill> skills) {
        int command = 0, leadership = 0, science = 0, security = 0, medicine = 0, operations = 0;

        for (Skill skill : skills) {
            String name = skill.getName().toLowerCase();
            if (name.contains("combat") || name.contains("firearms") || name.contains("melee")
                    || name.contains("gunnery") || name.contains("pistol") || name.contains("rifle")) {
                security++;
            } else if (name.contains("science") || name.contains("astro") || name.contains("survey")
                    || name.contains("biology") || name.contains("physics")) {
                science++;
            } else if (name.contains("admin") || name.contains("law") || name.contains("politics")
                    || name.contains("diplomacy")) {
                leadership++;
            } else if (name.contains("medic") || name.contains("doctor") || name.contains("surgery")) {
                medicine++;
            } else if (name.contains("pilot") || name.contains("drive") || name.contains("nav")
                    || name.contains("electronic") || name.contains("computer")) {
                operations++;
            } else if (name.contains("carpentry") || name.contains("mechanic") || name.contains("electronics")) {
                operations++;
            } else {
                command++;
            }
        }

        // Normalize to reasonable discipline values (max 3)
        clChar.setCommand(Math.min(3, command));
        clChar.setLeadership(Math.min(3, leadership));
        clChar.setScience(Math.min(3, science));
        clChar.setSecurity(Math.min(3, security));
        clChar.setMedicine(Math.min(3, medicine));
        clChar.setOperations(Math.min(3, operations));
    }
}
