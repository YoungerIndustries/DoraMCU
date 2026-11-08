package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.*;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogShipTalent;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogStarshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements the Starship Creation system from the Captain's Log Solo RPG rulebook.
 * <p>
 * The starship creation guides the player through 6 narrative steps that build the starship:
 * <ol>
 *   <li><b>Name & Registry</b> — What is your ship called?</li>
 *   <li><b>Ship Class</b> — What class of vessel do you command?</li>
 *   <li><b>Systems</b> — Rate your ship's core systems.</li>
 *   <li><b>Departments</b> — Rate your ship's departments.</li>
 *   <li><b>Traits</b> — Choose your ship's personality traits.</li>
 *   <li><b>Talents</b> — Select your ship's special abilities.</li>
 * </ol>
 * </p>
 * <p>
 * Each step presents choices that affect the starship's stats and capabilities.
 * The system maintains state in a session map until the starship is finalized
 * and saved to the database.
 * </p>
 */
@Service
public class StarshipCreationService {

    private static final Logger log = LoggerFactory.getLogger(StarshipCreationService.class);
    private static final int TOTAL_STEPS = 6;

    private final CaptainLogStarshipRepository starshipRepository;
    private final CaptainLogShipTalentService shipTalentService;
    private final AtomicLong sessionCounter = new AtomicLong(0);

    /**
     * In-memory session storage for ongoing starship creation.
     */
    private final Map<Long, StarshipSession> sessions = new ConcurrentHashMap<>();

    /**
     * Holds the intermediate state of a starship creation session.
     */
    static class StarshipSession {
        long sessionId;
        String name;
        String registry;
        String shipClass;
        CaptainLogStarship.Scale scale = CaptainLogStarship.Scale.LARGE;
        boolean isStation = false;
        int currentStep = 1;
        Map<CaptainLogStarship.StarshipSystem, Integer> systems = new EnumMap<>(CaptainLogStarship.StarshipSystem.class);
        Map<CaptainLogStarship.StarshipDepartment, Integer> departments = new EnumMap<>(CaptainLogStarship.StarshipDepartment.class);
        String trait1;
        String trait2;
        String trait3;
        List<String> talentNames = new ArrayList<>();
    }

    public StarshipCreationService(CaptainLogStarshipRepository starshipRepository,
                                    CaptainLogShipTalentService shipTalentService) {
        this.starshipRepository = starshipRepository;
        this.shipTalentService = shipTalentService;
    }

    // ==================== Ship Class Definitions ====================

    /**
     * Predefined ship classes with default system and department ratings.
     */
    public static class ShipClassDefinition {
        private final String name;
        private final String description;
        private final CaptainLogStarship.Scale scale;
        private final Map<CaptainLogStarship.StarshipSystem, Integer> defaultSystems;
        private final Map<CaptainLogStarship.StarshipDepartment, Integer> defaultDepartments;

        ShipClassDefinition(String name, String description, CaptainLogStarship.Scale scale,
                             Map<CaptainLogStarship.StarshipSystem, Integer> defaultSystems,
                             Map<CaptainLogStarship.StarshipDepartment, Integer> defaultDepartments) {
            this.name = name;
            this.description = description;
            this.scale = scale;
            this.defaultSystems = defaultSystems;
            this.defaultDepartments = defaultDepartments;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public CaptainLogStarship.Scale getScale() { return scale; }
        public Map<CaptainLogStarship.StarshipSystem, Integer> getDefaultSystems() { return defaultSystems; }
        public Map<CaptainLogStarship.StarshipDepartment, Integer> getDefaultDepartments() { return defaultDepartments; }
    }

    private static final Map<String, ShipClassDefinition> SHIP_CLASSES = new LinkedHashMap<>();

    static {
        // Constitution-class: Balanced cruiser
        Map<CaptainLogStarship.StarshipSystem, Integer> constSystems = new EnumMap<>(CaptainLogStarship.StarshipSystem.class);
        constSystems.put(CaptainLogStarship.StarshipSystem.COMMS, 2);
        constSystems.put(CaptainLogStarship.StarshipSystem.COMPUTERS, 2);
        constSystems.put(CaptainLogStarship.StarshipSystem.ENGINES, 2);
        constSystems.put(CaptainLogStarship.StarshipSystem.SENSORS, 2);
        constSystems.put(CaptainLogStarship.StarshipSystem.STRUCTURE, 3);
        constSystems.put(CaptainLogStarship.StarshipSystem.WEAPONS, 2);
        Map<CaptainLogStarship.StarshipDepartment, Integer> constDepts = new EnumMap<>(CaptainLogStarship.StarshipDepartment.class);
        constDepts.put(CaptainLogStarship.StarshipDepartment.COMMAND, 2);
        constDepts.put(CaptainLogStarship.StarshipDepartment.SCIENCE, 2);
        constDepts.put(CaptainLogStarship.StarshipDepartment.SECURITY, 2);
        constDepts.put(CaptainLogStarship.StarshipDepartment.ENGINEERING, 2);
        constDepts.put(CaptainLogStarship.StarshipDepartment.MEDICAL, 1);
        constDepts.put(CaptainLogStarship.StarshipDepartment.OPERATIONS, 2);
        SHIP_CLASSES.put("CONSTITUTION", new ShipClassDefinition(
                "Constitution-class", "A balanced cruiser designed for exploration and defense.",
                CaptainLogStarship.Scale.LARGE, constSystems, constDepts));

        // Galaxy-class: Exploration flagship
        Map<CaptainLogStarship.StarshipSystem, Integer> galSystems = new EnumMap<>(CaptainLogStarship.StarshipSystem.class);
        galSystems.put(CaptainLogStarship.StarshipSystem.COMMS, 3);
        galSystems.put(CaptainLogStarship.StarshipSystem.COMPUTERS, 3);
        galSystems.put(CaptainLogStarship.StarshipSystem.ENGINES, 2);
        galSystems.put(CaptainLogStarship.StarshipSystem.SENSORS, 3);
        galSystems.put(CaptainLogStarship.StarshipSystem.STRUCTURE, 3);
        galSystems.put(CaptainLogStarship.StarshipSystem.WEAPONS, 2);
        Map<CaptainLogStarship.StarshipDepartment, Integer> galDepts = new EnumMap<>(CaptainLogStarship.StarshipDepartment.class);
        galDepts.put(CaptainLogStarship.StarshipDepartment.COMMAND, 3);
        galDepts.put(CaptainLogStarship.StarshipDepartment.SCIENCE, 3);
        galDepts.put(CaptainLogStarship.StarshipDepartment.SECURITY, 2);
        galDepts.put(CaptainLogStarship.StarshipDepartment.ENGINEERING, 2);
        galDepts.put(CaptainLogStarship.StarshipDepartment.MEDICAL, 2);
        galDepts.put(CaptainLogStarship.StarshipDepartment.OPERATIONS, 2);
        SHIP_CLASSES.put("GALAXY", new ShipClassDefinition(
                "Galaxy-class", "The Federation's premier exploration vessel.",
                CaptainLogStarship.Scale.CAPITAL, galSystems, galDepts));

        // Sovereign-class: Advanced explorer
        Map<CaptainLogStarship.StarshipSystem, Integer> sovSystems = new EnumMap<>(CaptainLogStarship.StarshipSystem.class);
        sovSystems.put(CaptainLogStarship.StarshipSystem.COMMS, 3);
        sovSystems.put(CaptainLogStarship.StarshipSystem.COMPUTERS, 3);
        sovSystems.put(CaptainLogStarship.StarshipSystem.ENGINES, 3);
        sovSystems.put(CaptainLogStarship.StarshipSystem.SENSORS, 3);
        sovSystems.put(CaptainLogStarship.StarshipSystem.STRUCTURE, 3);
        sovSystems.put(CaptainLogStarship.StarshipSystem.WEAPONS, 3);
        Map<CaptainLogStarship.StarshipDepartment, Integer> sovDepts = new EnumMap<>(CaptainLogStarship.StarshipDepartment.class);
        sovDepts.put(CaptainLogStarship.StarshipDepartment.COMMAND, 3);
        sovDepts.put(CaptainLogStarship.StarshipDepartment.SCIENCE, 3);
        sovDepts.put(CaptainLogStarship.StarshipDepartment.SECURITY, 3);
        sovDepts.put(CaptainLogStarship.StarshipDepartment.ENGINEERING, 2);
        sovDepts.put(CaptainLogStarship.StarshipDepartment.MEDICAL, 2);
        sovDepts.put(CaptainLogStarship.StarshipDepartment.OPERATIONS, 2);
        SHIP_CLASSES.put("SOVEREIGN", new ShipClassDefinition(
                "Sovereign-class", "An advanced explorer with superior speed and firepower.",
                CaptainLogStarship.Scale.CAPITAL, sovSystems, sovDepts));

        // Nebula-class: Science vessel
        Map<CaptainLogStarship.StarshipSystem, Integer> nebSystems = new EnumMap<>(CaptainLogStarship.StarshipSystem.class);
        nebSystems.put(CaptainLogStarship.StarshipSystem.COMMS, 3);
        nebSystems.put(CaptainLogStarship.StarshipSystem.COMPUTERS, 3);
        nebSystems.put(CaptainLogStarship.StarshipSystem.ENGINES, 2);
        nebSystems.put(CaptainLogStarship.StarshipSystem.SENSORS, 3);
        nebSystems.put(CaptainLogStarship.StarshipSystem.STRUCTURE, 2);
        nebSystems.put(CaptainLogStarship.StarshipSystem.WEAPONS, 1);
        Map<CaptainLogStarship.StarshipDepartment, Integer> nebDepts = new EnumMap<>(CaptainLogStarship.StarshipDepartment.class);
        nebDepts.put(CaptainLogStarship.StarshipDepartment.COMMAND, 2);
        nebDepts.put(CaptainLogStarship.StarshipDepartment.SCIENCE, 3);
        nebDepts.put(CaptainLogStarship.StarshipDepartment.SECURITY, 1);
        nebDepts.put(CaptainLogStarship.StarshipDepartment.ENGINEERING, 2);
        nebDepts.put(CaptainLogStarship.StarshipDepartment.MEDICAL, 3);
        nebDepts.put(CaptainLogStarship.StarshipDepartment.OPERATIONS, 2);
        SHIP_CLASSES.put("NEBULA", new ShipClassDefinition(
                "Nebula-class", "A science vessel with advanced medical and research facilities.",
                CaptainLogStarship.Scale.CAPITAL, nebSystems, nebDepts));

        // Intrepid-class: Deep space explorer
        Map<CaptainLogStarship.StarshipSystem, Integer> intSystems = new EnumMap<>(CaptainLogStarship.StarshipSystem.class);
        intSystems.put(CaptainLogStarship.StarshipSystem.COMMS, 2);
        intSystems.put(CaptainLogStarship.StarshipSystem.COMPUTERS, 2);
        intSystems.put(CaptainLogStarship.StarshipSystem.ENGINES, 3);
        intSystems.put(CaptainLogStarship.StarshipSystem.SENSORS, 3);
        intSystems.put(CaptainLogStarship.StarshipSystem.STRUCTURE, 1);
        intSystems.put(CaptainLogStarship.StarshipSystem.WEAPONS, 1);
        Map<CaptainLogStarship.StarshipDepartment, Integer> intDepts = new EnumMap<>(CaptainLogStarship.StarshipDepartment.class);
        intDepts.put(CaptainLogStarship.StarshipDepartment.COMMAND, 2);
        intDepts.put(CaptainLogStarship.StarshipDepartment.SCIENCE, 3);
        intDepts.put(CaptainLogStarship.StarshipDepartment.SECURITY, 1);
        intDepts.put(CaptainLogStarship.StarshipDepartment.ENGINEERING, 2);
        intDepts.put(CaptainLogStarship.StarshipDepartment.MEDICAL, 1);
        intDepts.put(CaptainLogStarship.StarshipDepartment.OPERATIONS, 2);
        SHIP_CLASSES.put("INTREPID", new ShipClassDefinition(
                "Intrepid-class", "A fast deep space explorer with advanced sensors.",
                CaptainLogStarship.Scale.MEDIUM, intSystems, intDepts));

        // Defiant-class: Combat vessel
        Map<CaptainLogStarship.StarshipSystem, Integer> defSystems = new EnumMap<>(CaptainLogStarship.StarshipSystem.class);
        defSystems.put(CaptainLogStarship.StarshipSystem.COMMS, 1);
        defSystems.put(CaptainLogStarship.StarshipSystem.COMPUTERS, 1);
        defSystems.put(CaptainLogStarship.StarshipSystem.ENGINES, 2);
        defSystems.put(CaptainLogStarship.StarshipSystem.SENSORS, 2);
        defSystems.put(CaptainLogStarship.StarshipSystem.STRUCTURE, 3);
        defSystems.put(CaptainLogStarship.StarshipSystem.WEAPONS, 3);
        Map<CaptainLogStarship.StarshipDepartment, Integer> defDepts = new EnumMap<>(CaptainLogStarship.StarshipDepartment.class);
        defDepts.put(CaptainLogStarship.StarshipDepartment.COMMAND, 2);
        defDepts.put(CaptainLogStarship.StarshipDepartment.SCIENCE, 1);
        defDepts.put(CaptainLogStarship.StarshipDepartment.SECURITY, 3);
        defDepts.put(CaptainLogStarship.StarshipDepartment.ENGINEERING, 2);
        defDepts.put(CaptainLogStarship.StarshipDepartment.MEDICAL, 1);
        defDepts.put(CaptainLogStarship.StarshipDepartment.OPERATIONS, 2);
        SHIP_CLASSES.put("DEFIANT", new ShipClassDefinition(
                "Defiant-class", "A small but heavily armed combat vessel.",
                CaptainLogStarship.Scale.SMALL, defSystems, defDepts));

        // D7-class (Klingon)
        Map<CaptainLogStarship.StarshipSystem, Integer> d7Systems = new EnumMap<>(CaptainLogStarship.StarshipSystem.class);
        d7Systems.put(CaptainLogStarship.StarshipSystem.COMMS, 1);
        d7Systems.put(CaptainLogStarship.StarshipSystem.COMPUTERS, 1);
        d7Systems.put(CaptainLogStarship.StarshipSystem.ENGINES, 2);
        d7Systems.put(CaptainLogStarship.StarshipSystem.SENSORS, 2);
        d7Systems.put(CaptainLogStarship.StarshipSystem.STRUCTURE, 3);
        d7Systems.put(CaptainLogStarship.StarshipSystem.WEAPONS, 3);
        Map<CaptainLogStarship.StarshipDepartment, Integer> d7Depts = new EnumMap<>(CaptainLogStarship.StarshipDepartment.class);
        d7Depts.put(CaptainLogStarship.StarshipDepartment.COMMAND, 2);
        d7Depts.put(CaptainLogStarship.StarshipDepartment.SCIENCE, 1);
        d7Depts.put(CaptainLogStarship.StarshipDepartment.SECURITY, 3);
        d7Depts.put(CaptainLogStarship.StarshipDepartment.ENGINEERING, 2);
        d7Depts.put(CaptainLogStarship.StarshipDepartment.MEDICAL, 1);
        d7Depts.put(CaptainLogStarship.StarshipDepartment.OPERATIONS, 2);
        SHIP_CLASSES.put("D7", new ShipClassDefinition(
                "D7-class", "A Klingon warbird built for battle.",
                CaptainLogStarship.Scale.LARGE, d7Systems, d7Depts));

        // Roma-class (Romulan)
        Map<CaptainLogStarship.StarshipSystem, Integer> romaSystems = new EnumMap<>(CaptainLogStarship.StarshipSystem.class);
        romaSystems.put(CaptainLogStarship.StarshipSystem.COMMS, 2);
        romaSystems.put(CaptainLogStarship.StarshipSystem.COMPUTERS, 2);
        romaSystems.put(CaptainLogStarship.StarshipSystem.ENGINES, 2);
        romaSystems.put(CaptainLogStarship.StarshipSystem.SENSORS, 3);
        romaSystems.put(CaptainLogStarship.StarshipSystem.STRUCTURE, 2);
        romaSystems.put(CaptainLogStarship.StarshipSystem.WEAPONS, 2);
        Map<CaptainLogStarship.StarshipDepartment, Integer> romaDepts = new EnumMap<>(CaptainLogStarship.StarshipDepartment.class);
        romaDepts.put(CaptainLogStarship.StarshipDepartment.COMMAND, 2);
        romaDepts.put(CaptainLogStarship.StarshipDepartment.SCIENCE, 2);
        romaDepts.put(CaptainLogStarship.StarshipDepartment.SECURITY, 3);
        romaDepts.put(CaptainLogStarship.StarshipDepartment.ENGINEERING, 2);
        romaDepts.put(CaptainLogStarship.StarshipDepartment.MEDICAL, 1);
        romaDepts.put(CaptainLogStarship.StarshipDepartment.OPERATIONS, 2);
        SHIP_CLASSES.put("ROMA", new ShipClassDefinition(
                "Roma-class", "A Romulan warship with cloaking capabilities.",
                CaptainLogStarship.Scale.LARGE, romaSystems, romaDepts));
    }

    // ==================== Trait Options ====================

    private static final List<String> SHIP_TRAITS = Arrays.asList(
            "Agile", "Well-Armed", "Advanced Sensors", "Heavy Shields",
            "Diplomatic Suite", "Medical Facility", "Science Laboratory",
            "Shuttle Bay", "Cargo Hold", "Engineering Marvel",
            "Veteran Crew", "Newly Commissioned", "Refitted",
            "Experimental Systems", "Stealth Capable", "Long Range",
            "Point Defense", "Torpedo Heavy", "Armor Plated",
            "Fast Warp Core", "Standard Issue", "Custom Build"
    );

    // ==================== Session Management ====================

    /**
     * Starts a new starship creation session.
     */
    public StarshipCreationStepResponse startCreation(StarshipCreationStartRequest request) {
        long sessionId = sessionCounter.incrementAndGet();
        StarshipSession session = new StarshipSession();
        session.sessionId = sessionId;
        session.name = request.getName();
        session.registry = request.getRegistry();

        // Initialize systems and departments to 0
        for (CaptainLogStarship.StarshipSystem sys : CaptainLogStarship.StarshipSystem.values()) {
            session.systems.put(sys, 0);
        }
        for (CaptainLogStarship.StarshipDepartment dept : CaptainLogStarship.StarshipDepartment.values()) {
            session.departments.put(dept, 0);
        }

        sessions.put(sessionId, session);
        log.info("Started starship creation session {} for: {}", sessionId, request.getName());

        StarshipCreationStepResponse response = buildStep1(session);
        response.setSessionId(sessionId);
        return response;
    }

    /**
     * Processes a choice and returns the next step, or the final starship if complete.
     */
    @Transactional
    public Object makeChoice(long sessionId, StarshipCreationChoiceRequest request) {
        StarshipSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Starship creation session not found: " + sessionId);
        }

        switch (session.currentStep) {
            case 1 -> processStep1(session, request);
            case 2 -> processStep2(session, request);
            case 3 -> processStep3(session, request);
            case 4 -> processStep4(session, request);
            case 5 -> processStep5(session, request);
            case 6 -> {
                CaptainLogStarship result = finalizeStarship(session);
                sessions.remove(sessionId);
                return result;
            }
            default -> throw new IllegalStateException("Unexpected step: " + session.currentStep);
        }

        StarshipCreationStepResponse response;
        switch (session.currentStep) {
            case 2 -> response = buildStep2(session);
            case 3 -> response = buildStep3(session);
            case 4 -> response = buildStep4(session);
            case 5 -> response = buildStep5(session);
            case 6 -> response = buildStep6(session);
            default -> throw new IllegalStateException("Unexpected step: " + session.currentStep);
        }
        response.setSessionId(sessionId);
        return response;
    }

    // ==================== Step Processors ====================

    private void processStep1(StarshipSession session, StarshipCreationChoiceRequest request) {
        // Step 1: Confirm name and registry (already set at start)
        if (request.getName() != null) session.name = request.getName();
        if (request.getRegistry() != null) session.registry = request.getRegistry();
        session.currentStep = 2;
    }

    private void processStep2(StarshipSession session, StarshipCreationChoiceRequest request) {
        // Step 2: Choose ship class
        String chosenClass = request.getChoice();
        ShipClassDefinition def = SHIP_CLASSES.get(chosenClass);
        if (def == null) {
            throw new IllegalArgumentException("Unknown ship class: " + chosenClass);
        }
        session.shipClass = def.getName();
        session.scale = def.getScale();
        session.systems.putAll(def.getDefaultSystems());
        session.departments.putAll(def.getDefaultDepartments());
        session.currentStep = 3;
    }

    private void processStep3(StarshipSession session, StarshipCreationChoiceRequest request) {
        // Step 3: Adjust systems
        if (request.getSystems() != null) {
            for (Map.Entry<String, Integer> entry : request.getSystems().entrySet()) {
                try {
                    CaptainLogStarship.StarshipSystem sys = CaptainLogStarship.StarshipSystem.valueOf(entry.getKey().toUpperCase());
                    session.systems.put(sys, entry.getValue());
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown system: {}", entry.getKey());
                }
            }
        }
        session.currentStep = 4;
    }

    private void processStep4(StarshipSession session, StarshipCreationChoiceRequest request) {
        // Step 4: Adjust departments
        if (request.getDepartments() != null) {
            for (Map.Entry<String, Integer> entry : request.getDepartments().entrySet()) {
                try {
                    CaptainLogStarship.StarshipDepartment dept = CaptainLogStarship.StarshipDepartment.valueOf(entry.getKey().toUpperCase());
                    session.departments.put(dept, entry.getValue());
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown department: {}", entry.getKey());
                }
            }
        }
        session.currentStep = 5;
    }

    private void processStep5(StarshipSession session, StarshipCreationChoiceRequest request) {
        // Step 5: Choose traits
        if (request.getTraits() != null && !request.getTraits().isEmpty()) {
            session.trait1 = request.getTraits().size() > 0 ? request.getTraits().get(0) : null;
            session.trait2 = request.getTraits().size() > 1 ? request.getTraits().get(1) : null;
            session.trait3 = request.getTraits().size() > 2 ? request.getTraits().get(2) : null;
        }
        session.currentStep = 6;
    }

    // ==================== Step Builders ====================

    private StarshipCreationStepResponse buildStep1(StarshipSession session) {
        StarshipCreationStepResponse response = new StarshipCreationStepResponse();
        response.setStep(session.currentStep);
        response.setTotalSteps(TOTAL_STEPS);
        response.setTitle("Name & Registry");
        response.setDescription("Confirm your ship's name and registry number.");
        response.setNarrative("Every starship begins with a name. What will yours be called?");

        List<StarshipCreationChoice> choices = new ArrayList<>();
        StarshipCreationChoice confirm = new StarshipCreationChoice();
        confirm.setId("CONFIRM");
        confirm.setTitle("Confirm");
        confirm.setDescription("Proceed with name: " + session.name + ", Registry: " + session.registry);
        choices.add(confirm);
        response.setChoices(choices);
        return response;
    }

    private StarshipCreationStepResponse buildStep2(StarshipSession session) {
        StarshipCreationStepResponse response = new StarshipCreationStepResponse();
        response.setStep(session.currentStep);
        response.setTotalSteps(TOTAL_STEPS);
        response.setTitle("Ship Class");
        response.setDescription("Choose your ship's class — this determines its default capabilities.");
        response.setNarrative("What class of vessel do you command? Each class has its own strengths and weaknesses.");

        List<StarshipCreationChoice> choices = new ArrayList<>();
        for (Map.Entry<String, ShipClassDefinition> entry : SHIP_CLASSES.entrySet()) {
            StarshipCreationChoice choice = new StarshipCreationChoice();
            choice.setId(entry.getKey());
            choice.setTitle(entry.getValue().getName());
            choice.setDescription(entry.getValue().getDescription());
            choices.add(choice);
        }
        response.setChoices(choices);
        return response;
    }

    private StarshipCreationStepResponse buildStep3(StarshipSession session) {
        StarshipCreationStepResponse response = new StarshipCreationStepResponse();
        response.setStep(session.currentStep);
        response.setTotalSteps(TOTAL_STEPS);
        response.setTitle("Starship Systems");
        response.setDescription("Adjust your ship's system ratings. Higher ratings mean better performance in that area.");
        response.setNarrative("Your ship's systems determine how well it performs in various situations. You can adjust these from the defaults.");

        List<StarshipCreationChoice> choices = new ArrayList<>();
        StarshipCreationChoice confirm = new StarshipCreationChoice();
        confirm.setId("CONFIRM");
        confirm.setTitle("Confirm Systems");
        StringBuilder sb = new StringBuilder("Current ratings: ");
        session.systems.forEach((sys, val) -> sb.append(sys.name()).append("=").append(val).append(", "));
        confirm.setDescription(sb.toString());
        Map<String, Integer> sysMap = new HashMap<>();
        session.systems.forEach((sys, val) -> sysMap.put(sys.name(), val));
        confirm.setSystems(sysMap);
        choices.add(confirm);
        response.setChoices(choices);
        return response;
    }

    private StarshipCreationStepResponse buildStep4(StarshipSession session) {
        StarshipCreationStepResponse response = new StarshipCreationStepResponse();
        response.setStep(session.currentStep);
        response.setTotalSteps(TOTAL_STEPS);
        response.setTitle("Starship Departments");
        response.setDescription("Adjust your ship's department ratings. These represent the crew's capabilities.");
        response.setNarrative("Your ship's departments represent the crew's expertise. You can adjust these from the defaults.");

        List<StarshipCreationChoice> choices = new ArrayList<>();
        StarshipCreationChoice confirm = new StarshipCreationChoice();
        confirm.setId("CONFIRM");
        confirm.setTitle("Confirm Departments");
        StringBuilder sb = new StringBuilder("Current ratings: ");
        session.departments.forEach((dept, val) -> sb.append(dept.name()).append("=").append(val).append(", "));
        confirm.setDescription(sb.toString());
        Map<String, Integer> deptMap = new HashMap<>();
        session.departments.forEach((dept, val) -> deptMap.put(dept.name(), val));
        confirm.setDepartments(deptMap);
        choices.add(confirm);
        response.setChoices(choices);
        return response;
    }

    private StarshipCreationStepResponse buildStep5(StarshipSession session) {
        StarshipCreationStepResponse response = new StarshipCreationStepResponse();
        response.setStep(session.currentStep);
        response.setTotalSteps(TOTAL_STEPS);
        response.setTitle("Ship Traits");
        response.setDescription("Choose up to 3 traits that define your ship's character.");
        response.setNarrative("Every ship has a personality. Choose up to 3 traits that define yours.");

        List<StarshipCreationChoice> choices = new ArrayList<>();
        for (String trait : SHIP_TRAITS) {
            StarshipCreationChoice choice = new StarshipCreationChoice();
            choice.setId(trait);
            choice.setTitle(trait);
            choice.setDescription("Select trait: " + trait);
            choices.add(choice);
        }
        response.setChoices(choices);
        return response;
    }

    private StarshipCreationStepResponse buildStep6(StarshipSession session) {
        StarshipCreationStepResponse response = new StarshipCreationStepResponse();
        response.setStep(session.currentStep);
        response.setTotalSteps(TOTAL_STEPS);
        response.setTitle("Ship Talents");
        response.setDescription("Choose your ship's special abilities based on its class.");
        response.setNarrative("Your ship has unique capabilities. Choose from talents available to your ship's class.");

        List<StarshipCreationChoice> choices = new ArrayList<>();
        List<CaptainLogShipTalentService.ShipTalentDefinition> classTalents = shipTalentService.suggestClassTalents(session.shipClass);
        for (CaptainLogShipTalentService.ShipTalentDefinition talent : classTalents) {
            StarshipCreationChoice choice = new StarshipCreationChoice();
            choice.setId(talent.getName());
            choice.setTitle(talent.getName());
            choice.setDescription(talent.getDescription());
            choices.add(choice);
        }
        // Add a "Skip" option
        StarshipCreationChoice skip = new StarshipCreationChoice();
        skip.setId("SKIP");
        skip.setTitle("Skip Talents");
        skip.setDescription("Proceed without selecting talents now.");
        choices.add(skip);
        response.setChoices(choices);
        return response;
    }

    // ==================== Finalization ====================

    @Transactional
    private CaptainLogStarship finalizeStarship(StarshipSession session) {
        CaptainLogStarship starship = new CaptainLogStarship();
        starship.setName(session.name);
        starship.setRegistry(session.registry);
        starship.setShipClass(session.shipClass);
        starship.setScale(session.scale);
        starship.setStation(session.isStation);
        starship.setTrait1(session.trait1);
        starship.setTrait2(session.trait2);
        starship.setTrait3(session.trait3);

        // Set system ratings
        session.systems.forEach((sys, val) -> {
            switch (sys) {
                case COMMS -> starship.setComms(val);
                case COMPUTERS -> starship.setComputers(val);
                case ENGINES -> starship.setEngines(val);
                case SENSORS -> starship.setSensors(val);
                case STRUCTURE -> starship.setStructure(val);
                case WEAPONS -> starship.setWeapons(val);
            }
        });

        // Set department ratings
        session.departments.forEach((dept, val) -> {
            switch (dept) {
                case COMMAND -> starship.setCommand(val);
                case SCIENCE -> starship.setScience(val);
                case SECURITY -> starship.setSecurity(val);
                case ENGINEERING -> starship.setEngineering(val);
                case MEDICAL -> starship.setMedical(val);
                case OPERATIONS -> starship.setOperations(val);
            }
        });

        // Set attributes based on systems (simplified: average of related systems)
        starship.setFitness((session.systems.get(CaptainLogStarship.StarshipSystem.ENGINES) +
                             session.systems.get(CaptainLogStarship.StarshipSystem.STRUCTURE)) / 2);
        starship.setTechnical((session.systems.get(CaptainLogStarship.StarshipSystem.COMPUTERS) +
                               session.systems.get(CaptainLogStarship.StarshipSystem.COMMS)) / 2);
        starship.setStrength(session.systems.get(CaptainLogStarship.StarshipSystem.STRUCTURE));
        starship.setReason((session.systems.get(CaptainLogStarship.StarshipSystem.COMPUTERS) +
                            session.systems.get(CaptainLogStarship.StarshipSystem.SENSORS)) / 2);
        starship.setInsight(session.systems.get(CaptainLogStarship.StarshipSystem.SENSORS));
        starship.setPresence((session.systems.get(CaptainLogStarship.StarshipSystem.COMMS) +
                              session.systems.get(CaptainLogStarship.StarshipSystem.WEAPONS)) / 2);

        // Save and add talents
        CaptainLogStarship saved = starshipRepository.save(starship);
        for (String talentName : session.talentNames) {
            CaptainLogShipTalent talent = new CaptainLogShipTalent(talentName,
                    "Selected during starship creation.");
            talent.setStarship(saved);
            saved.getTalents().add(talent);
        }

        log.info("Created starship '{}' (class: {}) via creation wizard", saved.getName(), saved.getShipClass());
        return saved;
    }

    /**
     * Gets all available ship classes.
     */
    public List<ShipClassDefinition> getAvailableShipClasses() {
        return new ArrayList<>(SHIP_CLASSES.values());
    }

    /**
     * Gets all available ship traits.
     */
    public List<String> getAvailableTraits() {
        return new ArrayList<>(SHIP_TRAITS);
    }

    /**
     * Gets a session by ID (for status checks).
     */
    public Optional<StarshipSession> getSession(long sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
