package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogFocus;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogValue;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogFocusRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Implements the "Creation in Play" character creation method from the Captain's Log Solo RPG.
 * <p>
 * Unlike the full Lifepath system (which uses 8 narrative steps), Creation in Play builds
 * the character through quick narrative choices during the first mission. The player makes
 * choices that determine the character's attributes, disciplines, focuses, and values on the fly.
 * </p>
 * <p>
 * The process has 5 steps:
 * <ol>
 *   <li><b>Role</b> — Choose the character's primary role (Captain, Science Officer, Security, etc.)</li>
 *   <li><b>Background</b> — Choose a background trait that shapes the character's past</li>
 *   <li><b>Attributes</b> — Allocate attribute points based on the role</li>
 *   <li><b>Disciplines</b> — Choose primary and secondary disciplines</li>
 *   <li><b>Values</b> — Define the character's core values</li>
 * </ol>
 * </p>
 * <p>
 * Each step presents themed choices. The final step saves the character to the database.
 * </p>
 */
@Service
public class CreationInPlayService {

    private static final Logger log = LoggerFactory.getLogger(CreationInPlayService.class);

    private final CaptainLogCharacterRepository characterRepository;
    private final CaptainLogFocusRepository focusRepository;
    private final CaptainLogValueRepository valueRepository;
    private final Map<String, CreationSession> activeSessions = new HashMap<>();

    public CreationInPlayService(CaptainLogCharacterRepository characterRepository,
                                  CaptainLogFocusRepository focusRepository,
                                  CaptainLogValueRepository valueRepository) {
        this.characterRepository = characterRepository;
        this.focusRepository = focusRepository;
        this.valueRepository = valueRepository;
    }

    // ==================== Character Roles ====================

    /**
     * Primary roles that determine attribute and discipline bonuses.
     */
    public enum CharacterRole {
        CAPTAIN("Captain", "Commanding officer of a starship.",
                Map.of(CaptainLogCharacter.Attribute.PRESENCE, 2, CaptainLogCharacter.Attribute.INSIGHT, 1),
                List.of(CaptainLogCharacter.Discipline.COMMAND, CaptainLogCharacter.Discipline.LEADERSHIP)),
        SCIENCE_OFFICER("Science Officer", "Chief scientist and researcher.",
                Map.of(CaptainLogCharacter.Attribute.REASON, 2, CaptainLogCharacter.Attribute.TECHNICAL, 1),
                List.of(CaptainLogCharacter.Discipline.SCIENCE, CaptainLogCharacter.Discipline.OPERATIONS)),
        SECURITY_OFFICER("Security Officer", "Head of security and tactical operations.",
                Map.of(CaptainLogCharacter.Attribute.FITNESS, 2, CaptainLogCharacter.Attribute.STRENGTH, 1),
                List.of(CaptainLogCharacter.Discipline.SECURITY, CaptainLogCharacter.Discipline.COMMAND)),
        CHIEF_ENGINEER("Chief Engineer", "Oversees all engineering operations.",
                Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 2, CaptainLogCharacter.Attribute.REASON, 1),
                List.of(CaptainLogCharacter.Discipline.OPERATIONS, CaptainLogCharacter.Discipline.SCIENCE)),
        CMO("Chief Medical Officer", "Responsible for crew health and bio-research.",
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 2, CaptainLogCharacter.Attribute.REASON, 1),
                List.of(CaptainLogCharacter.Discipline.MEDICINE, CaptainLogCharacter.Discipline.SCIENCE)),
        HELMSMAN("Helmsman", "Pilot and navigation specialist.",
                Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 2, CaptainLogCharacter.Attribute.FITNESS, 1),
                List.of(CaptainLogCharacter.Discipline.OPERATIONS, CaptainLogCharacter.Discipline.SECURITY)),
        TACTICAL_OFFICER("Tactical Officer", "Weapons and defense specialist.",
                Map.of(CaptainLogCharacter.Attribute.FITNESS, 2, CaptainLogCharacter.Attribute.TECHNICAL, 1),
                List.of(CaptainLogCharacter.Discipline.SECURITY, CaptainLogCharacter.Discipline.OPERATIONS)),
        DIPLOMAT("Diplomat", "Negotiator and first contact specialist.",
                Map.of(CaptainLogCharacter.Attribute.PRESENCE, 2, CaptainLogCharacter.Attribute.INSIGHT, 1),
                List.of(CaptainLogCharacter.Discipline.LEADERSHIP, CaptainLogCharacter.Discipline.SCIENCE));

        private final String name;
        private final String description;
        private final Map<CaptainLogCharacter.Attribute, Integer> attributeBonuses;
        private final List<CaptainLogCharacter.Discipline> recommendedDisciplines;

        CharacterRole(String name, String description,
                      Map<CaptainLogCharacter.Attribute, Integer> attributeBonuses,
                      List<CaptainLogCharacter.Discipline> recommendedDisciplines) {
            this.name = name;
            this.description = description;
            this.attributeBonuses = attributeBonuses;
            this.recommendedDisciplines = recommendedDisciplines;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public Map<CaptainLogCharacter.Attribute, Integer> getAttributeBonuses() { return attributeBonuses; }
        public List<CaptainLogCharacter.Discipline> getRecommendedDisciplines() { return recommendedDisciplines; }
    }

    // ==================== Background Traits ====================

    /**
     * Background traits that shape the character's past and provide narrative hooks.
     */
    public enum BackgroundTrait {
        STARFLEET_ACADEMY("Starfleet Academy Graduate",
                "You graduated from Starfleet Academy with honors.",
                "Gain a boon on your first Command check during the mission.",
                Map.of(), List.of("Academic")),
        FIELD_VETERAN("Field Veteran",
                "You've seen combat and survived.",
                "Gain a boon on your first Security check during the mission.",
                Map.of(CaptainLogCharacter.Attribute.FITNESS, 1), List.of("Battle-hardened")),
        EXPLORER("Born Explorer",
                "You've always been drawn to the unknown.",
                "Gain a boon on your first Science check during the mission.",
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1), List.of("Curious")),
        DIPLOMATIC_FAMILY("Diplomatic Heritage",
                "Your family has a long history of diplomatic service.",
                "Gain a boon on your first Leadership check during the mission.",
                Map.of(CaptainLogCharacter.Attribute.PRESENCE, 1), List.of("Charismatic")),
        COLONY_RAISED("Colony Raised",
                "You grew up on a frontier colony, resourceful and tough.",
                "Gain a boon on your first Operations check during the mission.",
                Map.of(CaptainLogCharacter.Attribute.STRENGTH, 1), List.of("Resourceful")),
        SCIENTIFIC_PRODIGY("Scientific Prodigy",
                "Your natural aptitude for science was recognized early.",
                "Gain a boon on your first Reason-based check during the mission.",
                Map.of(CaptainLogCharacter.Attribute.REASON, 1), List.of("Brilliant")),
        MEDICAL_TRAINING("Medical Training",
                "You received extensive medical training.",
                "Gain a boon on your first Medicine check during the mission.",
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1), List.of("Compassionate")),
        STREET_SMART("Street Smart",
                "You learned to survive on your own in the underworld.",
                "Gain a boon on your first Presence-based check during the mission.",
                Map.of(CaptainLogCharacter.Attribute.PRESENCE, 1), List.of("Cunning"));

        private final String name;
        private final String description;
        private final String mechanicalEffect;
        private final Map<CaptainLogCharacter.Attribute, Integer> attributeBonuses;
        private final List<String> personalityTraits;

        BackgroundTrait(String name, String description, String mechanicalEffect,
                        Map<CaptainLogCharacter.Attribute, Integer> attributeBonuses,
                        List<String> personalityTraits) {
            this.name = name;
            this.description = description;
            this.mechanicalEffect = mechanicalEffect;
            this.attributeBonuses = attributeBonuses;
            this.personalityTraits = personalityTraits;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getMechanicalEffect() { return mechanicalEffect; }
        public Map<CaptainLogCharacter.Attribute, Integer> getAttributeBonuses() { return attributeBonuses; }
        public List<String> getPersonalityTraits() { return personalityTraits; }
    }

    // ==================== Core Values ====================

    /**
     * Predefined core values for quick selection.
     */
    public enum CoreValue {
        DUTY("Duty", "You put the mission and your crew above all else."),
        HONOR("Honor", "You live by a strict personal code of conduct."),
        COMPASSION("Compassion", "You believe in helping those in need, no matter the cost."),
        CURIOSITY("Curiosity", "The drive to discover and understand is your guiding force."),
        BRAVERY("Bravery", "You face danger head-on, never backing down."),
        LOYALTY("Loyalty", "Your crew and friends are your world; you'd die for them."),
        JUSTICE("Justice", "You fight for what is right, even when it's unpopular."),
        INNOVATION("Innovation", "You believe progress comes from pushing boundaries."),
        TRADITION("Tradition", "The wisdom of the past guides your every decision."),
        FREEDOM("Freedom", "You value independence and the right to choose above all.");

        private final String name;
        private final String description;

        CoreValue(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    // ==================== Creation Session ====================

    /**
     * Tracks the state of a Creation in Play session.
     */
    public static class CreationSession {
        String sessionId;
        int currentStep = 0;
        String name;
        String species = "Human";
        String rank = "CAPTAIN";
        String assignment;
        CharacterRole role;
        BackgroundTrait background;
        Map<CaptainLogCharacter.Attribute, Integer> attributes = new HashMap<>();
        Map<CaptainLogCharacter.Discipline, Integer> disciplines = new HashMap<>();
        List<String> focusNames = new ArrayList<>();
        List<String> valueNames = new ArrayList<>();
        String backgroundTraitText;
        String mechanicalEffect;

        CreationSession(String sessionId) {
            this.sessionId = sessionId;
            // Initialize default attributes
            for (CaptainLogCharacter.Attribute attr : CaptainLogCharacter.Attribute.values()) {
                attributes.put(attr, 1);
            }
            // Initialize default disciplines
            for (CaptainLogCharacter.Discipline disc : CaptainLogCharacter.Discipline.values()) {
                disciplines.put(disc, 0);
            }
        }
    }

    // ==================== Session Management ====================

    /**
     * Starts a new Creation in Play session.
     */
    @Transactional
    public Map<String, Object> startCreation(String name, String species, String rank) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        CreationSession session = new CreationSession(sessionId);
        session.name = name;
        session.species = species != null ? species : "Human";
        session.rank = rank != null ? rank : "CAPTAIN";
        activeSessions.put(sessionId, session);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("step", 1);
        response.put("stepName", "Role");
        response.put("description", "Choose your character's primary role on the starship.");
        response.put("choices", getRoleChoices());

        log.info("Creation in Play started: {} (sessionId={})", name, sessionId);
        return response;
    }

    /**
     * Advances the creation session with the player's choice.
     */
    @Transactional
    public Map<String, Object> makeChoice(String sessionId, Map<String, String> choice) {
        CreationSession session = activeSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);

        switch (session.currentStep) {
            case 0 -> response.putAll(processRoleChoice(session, choice));
            case 1 -> response.putAll(processBackgroundChoice(session, choice));
            case 2 -> response.putAll(processAttributeChoice(session, choice));
            case 3 -> response.putAll(processDisciplineChoice(session, choice));
            case 4 -> response.putAll(processValueChoice(session, choice));
            case 5 -> {
                // Final step: save character
                CaptainLogCharacter character = saveCharacter(session);
                activeSessions.remove(sessionId);
                response.put("character", character);
                response.put("complete", true);
                response.put("message", "Character " + character.getName() + " created successfully!");
                return response;
            }
            default -> throw new IllegalStateException("Invalid step: " + session.currentStep);
        }

        session.currentStep++;
        response.put("step", session.currentStep + 1);

        return response;
    }

    // ==================== Step Processors ====================

    private Map<String, Object> processRoleChoice(CreationSession session, Map<String, String> choice) {
        Map<String, Object> result = new HashMap<>();
        String roleName = choice.get("choice");
        session.role = CharacterRole.valueOf(roleName);

        // Apply attribute bonuses
        session.role.getAttributeBonuses().forEach((attr, bonus) ->
                session.attributes.put(attr, session.attributes.get(attr) + bonus));

        result.put("choice", roleName);
        result.put("role", session.role.getName());
        result.put("description", session.role.getDescription());
        result.put("attributeBonuses", session.role.getAttributeBonuses());
        result.put("nextStepName", "Background");
        result.put("nextDescription", "Choose a background trait that shapes your character's past.");
        result.put("nextChoices", getBackgroundChoices());

        return result;
    }

    private Map<String, Object> processBackgroundChoice(CreationSession session, Map<String, String> choice) {
        Map<String, Object> result = new HashMap<>();
        String bgName = choice.get("choice");
        session.background = BackgroundTrait.valueOf(bgName);
        session.backgroundTraitText = session.background.getName();
        session.mechanicalEffect = session.background.getMechanicalEffect();

        // Apply attribute bonuses
        session.background.getAttributeBonuses().forEach((attr, bonus) ->
                session.attributes.put(attr, session.attributes.get(attr) + bonus));

        result.put("choice", bgName);
        result.put("background", session.background.getName());
        result.put("description", session.background.getDescription());
        result.put("mechanicalEffect", session.mechanicalEffect);
        result.put("nextStepName", "Attributes");
        result.put("nextDescription", "Review your attributes. You have 3 bonus points to distribute.");
        result.put("nextChoices", getAttributeChoices(session));

        return result;
    }

    private Map<String, Object> processAttributeChoice(CreationSession session, Map<String, String> choice) {
        Map<String, Object> result = new HashMap<>();

        // Parse attribute distribution: "FITNESS=2,REASON=1" format
        String distribution = choice.get("distribution");
        if (distribution != null && !distribution.isEmpty()) {
            String[] pairs = distribution.split(",");
            for (String pair : pairs) {
                String[] parts = pair.trim().split("=");
                if (parts.length == 2) {
                    CaptainLogCharacter.Attribute attr = CaptainLogCharacter.Attribute.valueOf(parts[0].trim());
                    int bonus = Integer.parseInt(parts[1].trim());
                    session.attributes.put(attr, session.attributes.get(attr) + bonus);
                }
            }
        }

        result.put("attributes", session.attributes);
        result.put("nextStepName", "Disciplines");
        result.put("nextDescription", "Choose your primary and secondary disciplines.");
        result.put("nextChoices", getDisciplineChoices(session));

        return result;
    }

    private Map<String, Object> processDisciplineChoice(CreationSession session, Map<String, String> choice) {
        Map<String, Object> result = new HashMap<>();

        String primary = choice.get("primary");
        String secondary = choice.get("secondary");

        if (primary != null) {
            CaptainLogCharacter.Discipline disc = CaptainLogCharacter.Discipline.valueOf(primary);
            session.disciplines.put(disc, 2);
        }
        if (secondary != null) {
            CaptainLogCharacter.Discipline disc = CaptainLogCharacter.Discipline.valueOf(secondary);
            session.disciplines.put(disc, 1);
        }

        // Set focus based on primary discipline
        if (primary != null) {
            session.focusNames.add(getSuggestedFocus(CaptainLogCharacter.Discipline.valueOf(primary)));
        }

        result.put("primaryDiscipline", primary);
        result.put("secondaryDiscipline", secondary);
        result.put("disciplines", session.disciplines);
        result.put("focus", session.focusNames);
        result.put("nextStepName", "Values");
        result.put("nextDescription", "Choose 2 core values that define your character.");
        result.put("nextChoices", getValueChoices());

        return result;
    }

    private Map<String, Object> processValueChoice(CreationSession session, Map<String, String> choice) {
        Map<String, Object> result = new HashMap<>();

        String value1 = choice.get("value1");
        String value2 = choice.get("value2");

        if (value1 != null) session.valueNames.add(value1);
        if (value2 != null) session.valueNames.add(value2);

        result.put("values", session.valueNames);
        result.put("nextStepName", "Complete");
        result.put("nextDescription", "Your character is ready! Confirm to create.");
        result.put("nextChoices", List.of(Map.of("choice", "CONFIRM", "title", "Create Character",
                "description", "Finalize and save " + session.name)));

        return result;
    }

    // ==================== Character Saving ====================

    @Transactional
    CaptainLogCharacter saveCharacter(CreationSession session) {
        CaptainLogCharacter character = new CaptainLogCharacter();
        character.setName(session.name);
        try {
            character.setRank(CaptainLogCharacter.Rank.valueOf(session.rank));
        } catch (IllegalArgumentException e) {
            character.setRank(CaptainLogCharacter.Rank.CAPTAIN);
        }
        character.setAssignment(null);
        character.setConstitution(10);
        character.setExperiencePoints(0);

        // Set attributes
        character.setFitness(session.attributes.get(CaptainLogCharacter.Attribute.FITNESS));
        character.setTechnical(session.attributes.get(CaptainLogCharacter.Attribute.TECHNICAL));
        character.setStrength(session.attributes.get(CaptainLogCharacter.Attribute.STRENGTH));
        character.setReason(session.attributes.get(CaptainLogCharacter.Attribute.REASON));
        character.setInsight(session.attributes.get(CaptainLogCharacter.Attribute.INSIGHT));
        character.setPresence(session.attributes.get(CaptainLogCharacter.Attribute.PRESENCE));

        // Set traits from background
        if (!session.valueNames.isEmpty()) {
            character.setTrait1(session.valueNames.get(0));
        }
        if (session.valueNames.size() > 1) {
            character.setTrait2(session.valueNames.get(1));
        }
        if (session.background != null && !session.background.getPersonalityTraits().isEmpty()) {
            character.setTrait3(session.background.getPersonalityTraits().get(0));
        }

        // Save character first
        character = characterRepository.save(character);

        // Save focuses
        for (String focusName : session.focusNames) {
            CaptainLogFocus focus = new CaptainLogFocus();
            focus.setCharacter(character);
            focus.setName(focusName);
            if (session.role != null && !session.role.getRecommendedDisciplines().isEmpty()) {
                CaptainLogCharacter.Discipline primaryDisc = session.role.getRecommendedDisciplines().get(0);
                focus.setDiscipline(CaptainLogFocus.Discipline.valueOf(primaryDisc.name()));
            }
            focus = focusRepository.save(focus);
            log.info("Added focus '{}' to {}", focusName, character.getName());
        }

        // Save values
        for (String valueName : session.valueNames) {
            CaptainLogValue value = new CaptainLogValue();
            value.setCharacter(character);
            value.setDescription(valueName);
            valueRepository.save(value);
            log.info("Added value '{}' to {}", valueName, character.getName());
        }

        log.info("Creation in Play completed: {} (role={}, background={})",
                character.getName(), session.role != null ? session.role.getName() : "none",
                session.background != null ? session.background.getName() : "none");
        return character;
    }

    // ==================== Choice Generators ====================

    private List<Map<String, String>> getRoleChoices() {
        List<Map<String, String>> choices = new ArrayList<>();
        for (CharacterRole role : CharacterRole.values()) {
            Map<String, String> choice = new HashMap<>();
            choice.put("choice", role.name());
            choice.put("title", role.getName());
            choice.put("description", role.getDescription());
            choices.add(choice);
        }
        return choices;
    }

    private List<Map<String, String>> getBackgroundChoices() {
        List<Map<String, String>> choices = new ArrayList<>();
        for (BackgroundTrait bg : BackgroundTrait.values()) {
            Map<String, String> choice = new HashMap<>();
            choice.put("choice", bg.name());
            choice.put("title", bg.getName());
            choice.put("description", bg.getDescription());
            choice.put("mechanicalEffect", bg.getMechanicalEffect());
            choices.add(choice);
        }
        return choices;
    }

    private List<Map<String, Object>> getAttributeChoices(CreationSession session) {
        List<Map<String, Object>> choices = new ArrayList<>();
        Map<String, Object> choice = new HashMap<>();
        choice.put("choice", "DISTRIBUTE");
        choice.put("title", "Distribute 3 Bonus Points");
        choice.put("description", "Add up to 3 points across your attributes. Each attribute can be increased by 1.");
        choice.put("currentAttributes", session.attributes);
        choice.put("bonusPoints", 3);
        choices.add(choice);
        return choices;
    }

    private List<Map<String, Object>> getDisciplineChoices(CreationSession session) {
        List<Map<String, Object>> choices = new ArrayList<>();
        Map<String, Object> choice = new HashMap<>();
        choice.put("choice", "SELECT");
        choice.put("title", "Choose Primary and Secondary Disciplines");
        choice.put("description", "Primary discipline gets rating 2, secondary gets rating 1.");
        choice.put("recommended", session.role.getRecommendedDisciplines().stream()
                .map(Enum::name).toList());
        choice.put("allDisciplines", Arrays.stream(CaptainLogCharacter.Discipline.values())
                .map(Enum::name).toList());
        choices.add(choice);
        return choices;
    }

    private List<Map<String, String>> getValueChoices() {
        List<Map<String, String>> choices = new ArrayList<>();
        for (CoreValue value : CoreValue.values()) {
            Map<String, String> choice = new HashMap<>();
            choice.put("choice", value.name());
            choice.put("title", value.getName());
            choice.put("description", value.getDescription());
            choices.add(choice);
        }
        return choices;
    }

    private String getSuggestedFocus(CaptainLogCharacter.Discipline discipline) {
        return switch (discipline) {
            case COMMAND -> "Starfleet Command";
            case LEADERSHIP -> "Crew Management";
            case SCIENCE -> "Astrophysics";
            case SECURITY -> "Hand-to-Hand Combat";
            case MEDICINE -> "Trauma Surgery";
            case OPERATIONS -> "Starship Navigation";
        };
    }

    /**
     * Lists all available roles.
     */
    public List<Map<String, String>> listRoles() {
        List<Map<String, String>> roles = new ArrayList<>();
        for (CharacterRole role : CharacterRole.values()) {
            Map<String, String> roleMap = new HashMap<>();
            roleMap.put("name", role.getName());
            roleMap.put("description", role.getDescription());
            roleMap.put("attributeBonuses", role.getAttributeBonuses().toString());
            roleMap.put("recommendedDisciplines", role.getRecommendedDisciplines().toString());
            roles.add(roleMap);
        }
        return roles;
    }

    /**
     * Lists all available background traits.
     */
    public List<Map<String, String>> listBackgrounds() {
        List<Map<String, String>> backgrounds = new ArrayList<>();
        for (BackgroundTrait bg : BackgroundTrait.values()) {
            Map<String, String> bgMap = new HashMap<>();
            bgMap.put("name", bg.getName());
            bgMap.put("description", bg.getDescription());
            bgMap.put("mechanicalEffect", bg.getMechanicalEffect());
            backgrounds.add(bgMap);
        }
        return backgrounds;
    }

    /**
     * Lists all available core values.
     */
    public List<Map<String, String>> listValues() {
        List<Map<String, String>> values = new ArrayList<>();
        for (CoreValue value : CoreValue.values()) {
            Map<String, String> valueMap = new HashMap<>();
            valueMap.put("name", value.getName());
            valueMap.put("description", value.getDescription());
            values.add(valueMap);
        }
        return values;
    }
}
