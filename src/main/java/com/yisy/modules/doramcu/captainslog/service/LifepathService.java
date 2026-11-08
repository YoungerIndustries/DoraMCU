package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.*;
import com.yisy.modules.doramcu.captainslog.model.*;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements the Lifepath Character Creation system from the Captain's Log Solo RPG rulebook.
 * <p>
 * The lifepath guides the player through 8 narrative steps that build the character:
 * <ol>
 *   <li><b>Name & Species</b> — Who are you?</li>
 *   <li><b>Background</b> — Where did you grow up? (Environment)</li>
 *   <li><b>Outlook</b> — How do you see the world?</li>
 *   <li><b>Education</b> — What training did you receive?</li>
 *   <li><b>Attributes</b> — Roll and assign your core stats.</li>
 *   <li><b>Disciplines</b> — Choose your area of expertise.</li>
 *   <li><b>Focuses</b> — Specialize within your disciplines.</li>
 *   <li><b>Values</b> — What principles guide you?</li>
 * </ol>
 * </p>
 * <p>
 * Each step presents narrative choices that affect the character's stats,
 * background traits, and personality. The system maintains state in a session map
 * until the character is finalized and saved to the database.
 * </p>
 */
@Component
public class LifepathService {

    private static final Logger log = LoggerFactory.getLogger(LifepathService.class);
    private static final int TOTAL_STEPS = 8;

    private final CaptainLogCharacterRepository characterRepository;
    private final AtomicLong sessionCounter = new AtomicLong(0);

    /**
     * In-memory session storage for ongoing lifepath creation.
     * Key: session ID, Value: the in-progress character data.
     */
    private final Map<Long, LifepathSession> sessions = new ConcurrentHashMap<>();

    /**
     * Holds the intermediate state of a lifepath creation session.
     */
    static class LifepathSession {
        long sessionId;
        String name;
        String species;
        String pronouns;
        int currentStep = 1;
        CaptainLogCharacter.Environment environment;
        CaptainLogCharacter.Outlook outlook;
        CaptainLogCharacter.Education education;
        CaptainLogCharacter.Assignment assignment;
        CaptainLogCharacter.Rank rank = CaptainLogCharacter.Rank.ENSIGN;
        Map<CaptainLogCharacter.Attribute, Integer> attributes = new EnumMap<>(CaptainLogCharacter.Attribute.class);
        Map<CaptainLogCharacter.Discipline, Integer> disciplines = new EnumMap<>(CaptainLogCharacter.Discipline.class);
        List<String> focuses = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<String> traits = new ArrayList<>();
    }

    public LifepathService(CaptainLogCharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    // ==================== Session Management ====================

    /**
     * Starts a new lifepath session and returns the first step.
     */
    public LifepathStepResponse startLifepath(LifepathStartRequest request) {
        long sessionId = sessionCounter.incrementAndGet();
        LifepathSession session = new LifepathSession();
        session.sessionId = sessionId;
        session.name = request.getName();
        session.species = request.getSpecies();
        session.pronouns = request.getPronouns();

        // Initialize attributes and disciplines to 0
        for (CaptainLogCharacter.Attribute attr : CaptainLogCharacter.Attribute.values()) {
            session.attributes.put(attr, 0);
        }
        for (CaptainLogCharacter.Discipline disc : CaptainLogCharacter.Discipline.values()) {
            session.disciplines.put(disc, 0);
        }

        sessions.put(sessionId, session);
        log.info("Started lifepath session {} for character: {}", sessionId, request.getName());

        LifepathStepResponse response = buildStep1(session);
        response.setSessionId(sessionId);
        return response;
    }

    /**
     * Processes a choice and returns the next step, or the final character if complete.
     */
    @Transactional
    public Object makeChoice(long sessionId, LifepathChoiceRequest request) {
        LifepathSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Lifepath session not found: " + sessionId);
        }

        int step = session.currentStep;
        String choiceId = request.getChoiceId();

        // Apply the choice based on the current step
        switch (step) {
            case 1 -> applyStep1Choice(session, choiceId);
            case 2 -> applyStep2Choice(session, choiceId);
            case 3 -> applyStep3Choice(session, choiceId);
            case 4 -> applyStep4Choice(session, choiceId);
            case 5 -> applyStep5Choice(session, choiceId);
            case 6 -> applyStep6Choice(session, choiceId);
            case 7 -> applyStep7Choice(session, choiceId);
            case 8 -> {
                applyStep8Choice(session, choiceId);
                // Finalize character
                CaptainLogCharacter character = finalizeCharacter(session);
                character = characterRepository.save(character);
                sessions.remove(sessionId);
                log.info("Lifepath session {} completed, character {} saved", sessionId, character.getName());
                return character;
            }
            default -> throw new IllegalStateException("Unexpected lifepath step: " + step);
        }

        // Advance to next step
        session.currentStep++;

        // Build and return the next step
        return buildNextStep(session);
    }

    // ==================== Step 1: Name & Species ====================

    private LifepathStepResponse buildStep1(LifepathSession session) {
        String narrative = String.format(
            "Before your story begins, you must define who you are. %s hails from the stars, " +
            "ready to serve among the great explorers of the galaxy. What kind of being are you?",
            session.name != null ? session.name : "You"
        );

        List<LifepathChoice> choices = new ArrayList<>();

        choices.add(new LifepathChoice("human", "Human",
                "From Earth or a human colony. Versatile and adaptable, humans excel in many fields.",
                Map.of(CaptainLogCharacter.Attribute.PRESENCE, 1, CaptainLogCharacter.Attribute.REASON, 1)));

        choices.add(new LifepathChoice("vulcan", "Vulcan",
                "Logical and disciplined. Vulcans have a rich tradition of scientific and scholarly pursuit.",
                Map.of(CaptainLogCharacter.Attribute.REASON, 2)));

        choices.add(new LifepathChoice("klingon", "Klingon",
                "Fierce warriors who value honor above all. A Klingon's strength is legendary.",
                Map.of(CaptainLogCharacter.Attribute.STRENGTH, 2)));

        choices.add(new LifepathChoice("andorian", "Andorian",
                "Honor-bound warriors from a cold world. Andorians are tough and resilient.",
                Map.of(CaptainLogCharacter.Attribute.FITNESS, 1, CaptainLogCharacter.Attribute.STRENGTH, 1)));

        choices.add(new LifepathChoice("betazoid", "Betazoid",
                "Empathic communicators with a deep understanding of others. Natural diplomats.",
                Map.of(CaptainLogCharacter.Attribute.PRESENCE, 2)));

        choices.add(new LifepathChoice("denobulan", "Denobulan",
                "Master healers and scientists. Denobulans are among the greatest medical minds.",
                Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 1, CaptainLogCharacter.Attribute.INSIGHT, 1)));

        choices.add(new LifepathChoice("ferengi", "Ferengi",
                "Profit-driven traders with sharp minds for business and technology.",
                Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 2)));

        choices.add(new LifepathChoice("trill", "Trill",
                "Symbiotic beings with combined wisdom of past hosts. Incredibly knowledgeable.",
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 2)));

        choices.add(new LifepathChoice("bajoran", "Bajoran",
                "Spiritual and resilient. Bajorans have endured much and emerged stronger.",
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1, CaptainLogCharacter.Attribute.PRESENCE, 1)));

        choices.add(new LifepathChoice("other", "Other Species",
                "You are from a species not listed here. Your unique background shapes your path.",
                Map.of(CaptainLogCharacter.Attribute.FITNESS, 1)));

        return new LifepathStepResponse(1, TOTAL_STEPS, "Species", narrative, choices, session.name);
    }

    private void applyStep1Choice(LifepathSession session, String choiceId) {
        Map<String, Map<CaptainLogCharacter.Attribute, Integer>> bonuses = new HashMap<>();
        bonuses.put("human", Map.of(CaptainLogCharacter.Attribute.PRESENCE, 1, CaptainLogCharacter.Attribute.REASON, 1));
        bonuses.put("vulcan", Map.of(CaptainLogCharacter.Attribute.REASON, 2));
        bonuses.put("klingon", Map.of(CaptainLogCharacter.Attribute.STRENGTH, 2));
        bonuses.put("andorian", Map.of(CaptainLogCharacter.Attribute.FITNESS, 1, CaptainLogCharacter.Attribute.STRENGTH, 1));
        bonuses.put("betazoid", Map.of(CaptainLogCharacter.Attribute.PRESENCE, 2));
        bonuses.put("denobulan", Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 1, CaptainLogCharacter.Attribute.INSIGHT, 1));
        bonuses.put("ferengi", Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 2));
        bonuses.put("trill", Map.of(CaptainLogCharacter.Attribute.INSIGHT, 2));
        bonuses.put("bajoran", Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1, CaptainLogCharacter.Attribute.PRESENCE, 1));
        bonuses.put("other", Map.of(CaptainLogCharacter.Attribute.FITNESS, 1));

        Map<CaptainLogCharacter.Attribute, Integer> bonus = bonuses.getOrDefault(choiceId, Map.of());
        for (Map.Entry<CaptainLogCharacter.Attribute, Integer> entry : bonus.entrySet()) {
            session.attributes.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        // Set species trait
        String speciesName = switch (choiceId) {
            case "human" -> "Human";
            case "vulcan" -> "Vulcan";
            case "klingon" -> "Klingon";
            case "andorian" -> "Andorian";
            case "betazoid" -> "Betazoid";
            case "denobulan" -> "Denobulan";
            case "ferengi" -> "Ferengi";
            case "trill" -> "Trill";
            case "bajoran" -> "Bajoran";
            default -> "Unknown Species";
        };
        session.traits.add(speciesName);
    }

    // ==================== Step 2: Background (Environment) ====================

    private LifepathStepResponse buildStep2(LifepathSession session) {
        String narrative = String.format(
            "%s grew up in a world that shaped your early years. Where did your childhood take place?",
            session.name
        );

        List<LifepathChoice> choices = new ArrayList<>();

        choices.add(buildChoice("city", "Urban Center",
                "You grew up in a bustling city, surrounded by technology and diverse cultures.",
                CaptainLogCharacter.Environment.CITY,
                Map.of(CaptainLogCharacter.Attribute.PRESENCE, 1),
                "Urbanite"));

        choices.add(buildChoice("rural", "Rural Homestead",
                "The quiet countryside taught you self-reliance and connection to nature.",
                CaptainLogCharacter.Environment.RURAL,
                Map.of(CaptainLogCharacter.Attribute.FITNESS, 1),
                "Country Raised"));

        choices.add(buildChoice("space", "Space Station",
                "Born among the stars, you never knew a world with gravity and sky.",
                CaptainLogCharacter.Environment.SPACE,
                Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 1),
                "Space Born"));

        choices.add(buildChoice("wild", "Frontier World",
                "A harsh, untamed planet forged you into a survivor.",
                CaptainLogCharacter.Environment.WILD,
                Map.of(CaptainLogCharacter.Attribute.STRENGTH, 1),
                "Frontier Survivor"));

        choices.add(buildChoice("colony", "New Colony",
                "You helped build a new settlement from scratch, learning to adapt to anything.",
                CaptainLogCharacter.Environment.COLONY,
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1),
                "Colonist"));

        choices.add(buildChoice("federation", "Federation World",
                "Growing up in the heart of Federation space, you were raised on ideals of peace and exploration.",
                CaptainLogCharacter.Environment.FEDERATION_WORLD,
                Map.of(CaptainLogCharacter.Attribute.REASON, 1),
                "Federation Citizen"));

        return new LifepathStepResponse(2, TOTAL_STEPS, "Background", narrative, choices, session.name);
    }

    private LifepathChoice buildChoice(String id, String title, String desc,
                                        CaptainLogCharacter.Environment env,
                                        Map<CaptainLogCharacter.Attribute, Integer> bonuses,
                                        String trait) {
        LifepathChoice choice = new LifepathChoice(id, title, desc);
        choice.setEnvironment(env);
        choice.setAttributeBonuses(bonuses);
        choice.setBackgroundTrait(trait);
        return choice;
    }

    private void applyStep2Choice(LifepathSession session, String choiceId) {
        Map<String, CaptainLogCharacter.Environment> envs = Map.of(
                "city", CaptainLogCharacter.Environment.CITY,
                "rural", CaptainLogCharacter.Environment.RURAL,
                "space", CaptainLogCharacter.Environment.SPACE,
                "wild", CaptainLogCharacter.Environment.WILD,
                "colony", CaptainLogCharacter.Environment.COLONY,
                "federation", CaptainLogCharacter.Environment.FEDERATION_WORLD
        );
        session.environment = envs.getOrDefault(choiceId, CaptainLogCharacter.Environment.CITY);

        Map<String, Map<CaptainLogCharacter.Attribute, Integer>> bonuses = Map.of(
                "city", Map.of(CaptainLogCharacter.Attribute.PRESENCE, 1),
                "rural", Map.of(CaptainLogCharacter.Attribute.FITNESS, 1),
                "space", Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 1),
                "wild", Map.of(CaptainLogCharacter.Attribute.STRENGTH, 1),
                "colony", Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1),
                "federation", Map.of(CaptainLogCharacter.Attribute.REASON, 1)
        );
        Map<CaptainLogCharacter.Attribute, Integer> bonus = bonuses.getOrDefault(choiceId, Map.of());
        for (Map.Entry<CaptainLogCharacter.Attribute, Integer> entry : bonus.entrySet()) {
            session.attributes.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        Map<String, String> traits = Map.of(
                "city", "Urbanite",
                "rural", "Country Raised",
                "space", "Space Born",
                "wild", "Frontier Survivor",
                "colony", "Colonist",
                "federation", "Federation Citizen"
        );
        session.traits.add(traits.getOrDefault(choiceId, "Unknown Origin"));
    }

    // ==================== Step 3: Outlook ====================

    private LifepathStepResponse buildStep3(LifepathSession session) {
        String narrative = String.format(
            "As %s grew older, a worldview began to form. How do you see the universe around you?",
            session.name
        );

        List<LifepathChoice> choices = new ArrayList<>();

        choices.add(buildOutlookChoice("optimistic", "Optimistic",
                "You believe the future holds promise and that people are fundamentally good.",
                CaptainLogCharacter.Outlook.OPTIMISTIC,
                Map.of(CaptainLogCharacter.Attribute.PRESENCE, 1),
                "Hopeful"));

        choices.add(buildOutlookChoice("pessimistic", "Pessimistic",
                "You've seen too much darkness to trust in easy answers. Caution is your guide.",
                CaptainLogCharacter.Outlook.PESSIMISTIC,
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1),
                "Cautious"));

        choices.add(buildOutlookChoice("pragmatic", "Pragmatic",
                "You deal with things as they are, not as you wish them to be. Results matter most.",
                CaptainLogCharacter.Outlook.PRAGMATIC,
                Map.of(CaptainLogCharacter.Attribute.REASON, 1),
                "Practical"));

        choices.add(buildOutlookChoice("idealistic", "Idealistic",
                "You dream of a better galaxy and work tirelessly toward that vision.",
                CaptainLogCharacter.Outlook.IDEALISTIC,
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1, CaptainLogCharacter.Attribute.PRESENCE, 1),
                "Visionary"));

        choices.add(buildOutlookChoice("cynical", "Cynical",
                "Trust is earned, not given. You question motives and see through facades.",
                CaptainLogCharacter.Outlook.CYNICAL,
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 2),
                "Skeptic"));

        choices.add(buildOutlookChoice("adventurous", "Adventurous",
                "The unknown calls to you. Every day is a chance for a new discovery.",
                CaptainLogCharacter.Outlook.ADVENTUROUS,
                Map.of(CaptainLogCharacter.Attribute.FITNESS, 1),
                "Explorer"));

        return new LifepathStepResponse(3, TOTAL_STEPS, "Outlook", narrative, choices, session.name);
    }

    private LifepathChoice buildOutlookChoice(String id, String title, String desc,
                                               CaptainLogCharacter.Outlook outlook,
                                               Map<CaptainLogCharacter.Attribute, Integer> bonuses,
                                               String trait) {
        LifepathChoice choice = new LifepathChoice(id, title, desc);
        choice.setOutlook(outlook);
        choice.setAttributeBonuses(bonuses);
        choice.setBackgroundTrait(trait);
        return choice;
    }

    private void applyStep3Choice(LifepathSession session, String choiceId) {
        Map<String, CaptainLogCharacter.Outlook> outlooks = Map.of(
                "optimistic", CaptainLogCharacter.Outlook.OPTIMISTIC,
                "pessimistic", CaptainLogCharacter.Outlook.PESSIMISTIC,
                "pragmatic", CaptainLogCharacter.Outlook.PRAGMATIC,
                "idealistic", CaptainLogCharacter.Outlook.IDEALISTIC,
                "cynical", CaptainLogCharacter.Outlook.CYNICAL,
                "adventurous", CaptainLogCharacter.Outlook.ADVENTUROUS
        );
        session.outlook = outlooks.getOrDefault(choiceId, CaptainLogCharacter.Outlook.OPTIMISTIC);

        Map<String, Map<CaptainLogCharacter.Attribute, Integer>> bonuses = Map.of(
                "optimistic", Map.of(CaptainLogCharacter.Attribute.PRESENCE, 1),
                "pessimistic", Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1),
                "pragmatic", Map.of(CaptainLogCharacter.Attribute.REASON, 1),
                "idealistic", Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1, CaptainLogCharacter.Attribute.PRESENCE, 1),
                "cynical", Map.of(CaptainLogCharacter.Attribute.INSIGHT, 2),
                "adventurous", Map.of(CaptainLogCharacter.Attribute.FITNESS, 1)
        );
        Map<CaptainLogCharacter.Attribute, Integer> bonus = bonuses.getOrDefault(choiceId, Map.of());
        for (Map.Entry<CaptainLogCharacter.Attribute, Integer> entry : bonus.entrySet()) {
            session.attributes.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        Map<String, String> traits = Map.of(
                "optimistic", "Hopeful",
                "pessimistic", "Cautious",
                "pragmatic", "Practical",
                "idealistic", "Visionary",
                "cynical", "Skeptic",
                "adventurous", "Explorer"
        );
        session.traits.add(traits.getOrDefault(choiceId, "Unknown Outlook"));
    }

    // ==================== Step 4: Education ====================

    private LifepathStepResponse buildStep4(LifepathSession session) {
        String narrative = String.format(
            "%s needed training to prepare for a career among the stars. What path did you choose?",
            session.name
        );

        List<LifepathChoice> choices = new ArrayList<>();

        choices.add(buildEducationChoice("academy", "Starfleet Academy",
                "The premier training institution. Rigorous academics and physical conditioning.",
                CaptainLogCharacter.Education.ACADEMY,
                Map.of(CaptainLogCharacter.Attribute.REASON, 1, CaptainLogCharacter.Attribute.FITNESS, 1),
                "Academy Graduate"));

        choices.add(buildEducationChoice("military", "Military Training",
                "Combat-focused preparation. You learned to fight and lead under pressure.",
                CaptainLogCharacter.Education.MILITARY,
                Map.of(CaptainLogCharacter.Attribute.STRENGTH, 1, CaptainLogCharacter.Attribute.FITNESS, 1),
                "Combat Veteran"));

        choices.add(buildEducationChoice("academic", "University Education",
                "Years of scholarly study deepened your knowledge in a specific field.",
                CaptainLogCharacter.Education.ACADEMIC,
                Map.of(CaptainLogCharacter.Attribute.REASON, 2),
                "Scholar"));

        choices.add(buildEducationChoice("self_taught", "Self-Taught",
                "You learned through experience and determination, not formal schooling.",
                CaptainLogCharacter.Education.SELF_TAUGHT,
                Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1, CaptainLogCharacter.Attribute.TECHNICAL, 1),
                "Self-Made"));

        choices.add(buildEducationChoice("apprentice", "Apprenticeship",
                "A mentor guided your training, passing on practical skills and wisdom.",
                CaptainLogCharacter.Education.APPRENTICE,
                Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 2),
                "Trained Apprentice"));

        choices.add(buildEducationChoice("field", "Field Training",
                "You learned on the job, gaining experience through real missions and challenges.",
                CaptainLogCharacter.Education.FIELD_TRAINING,
                Map.of(CaptainLogCharacter.Attribute.FITNESS, 1, CaptainLogCharacter.Attribute.PRESENCE, 1),
                "Battle-Hardened"));

        return new LifepathStepResponse(4, TOTAL_STEPS, "Education", narrative, choices, session.name);
    }

    private LifepathChoice buildEducationChoice(String id, String title, String desc,
                                                  CaptainLogCharacter.Education education,
                                                  Map<CaptainLogCharacter.Attribute, Integer> bonuses,
                                                  String trait) {
        LifepathChoice choice = new LifepathChoice(id, title, desc);
        choice.setEducation(education);
        choice.setAttributeBonuses(bonuses);
        choice.setBackgroundTrait(trait);
        return choice;
    }

    private void applyStep4Choice(LifepathSession session, String choiceId) {
        Map<String, CaptainLogCharacter.Education> educations = Map.of(
                "academy", CaptainLogCharacter.Education.ACADEMY,
                "military", CaptainLogCharacter.Education.MILITARY,
                "academic", CaptainLogCharacter.Education.ACADEMIC,
                "self_taught", CaptainLogCharacter.Education.SELF_TAUGHT,
                "apprentice", CaptainLogCharacter.Education.APPRENTICE,
                "field", CaptainLogCharacter.Education.FIELD_TRAINING
        );
        session.education = educations.getOrDefault(choiceId, CaptainLogCharacter.Education.ACADEMY);

        Map<String, Map<CaptainLogCharacter.Attribute, Integer>> bonuses = Map.of(
                "academy", Map.of(CaptainLogCharacter.Attribute.REASON, 1, CaptainLogCharacter.Attribute.FITNESS, 1),
                "military", Map.of(CaptainLogCharacter.Attribute.STRENGTH, 1, CaptainLogCharacter.Attribute.FITNESS, 1),
                "academic", Map.of(CaptainLogCharacter.Attribute.REASON, 2),
                "self_taught", Map.of(CaptainLogCharacter.Attribute.INSIGHT, 1, CaptainLogCharacter.Attribute.TECHNICAL, 1),
                "apprentice", Map.of(CaptainLogCharacter.Attribute.TECHNICAL, 2),
                "field", Map.of(CaptainLogCharacter.Attribute.FITNESS, 1, CaptainLogCharacter.Attribute.PRESENCE, 1)
        );
        Map<CaptainLogCharacter.Attribute, Integer> bonus = bonuses.getOrDefault(choiceId, Map.of());
        for (Map.Entry<CaptainLogCharacter.Attribute, Integer> entry : bonus.entrySet()) {
            session.attributes.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        Map<String, String> traits = Map.of(
                "academy", "Academy Graduate",
                "military", "Combat Veteran",
                "academic", "Scholar",
                "self_taught", "Self-Made",
                "apprentice", "Trained Apprentice",
                "field", "Battle-Hardened"
        );
        session.traits.add(traits.getOrDefault(choiceId, "Unknown Education"));
    }

    // ==================== Step 5: Attributes Roll ====================

    private LifepathStepResponse buildStep5(LifepathSession session) {
        String narrative = String.format(
            "Now it's time to determine %s's core abilities. Roll 2d6 for each attribute, " +
            "then add the bonuses from your previous choices.", session.name
        );

        // Roll 2d6 for each attribute
        Random random = new Random();
        List<LifepathChoice> choices = new ArrayList<>();

        for (CaptainLogCharacter.Attribute attr : CaptainLogCharacter.Attribute.values()) {
            int roll = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
            int bonus = session.attributes.getOrDefault(attr, 0);
            int total = roll + bonus;

            String bonusText = bonus > 0 ? " + " + bonus : "";
            String title = attr.name() + ": " + total + " (rolled " + roll + bonusText + ")";
            String desc = "Roll result: " + roll + bonusText + (bonus > 0 ? " bonus" : "") + " = " + total;

            LifepathChoice choice = new LifepathChoice(attr.name().toLowerCase(), title, desc);
            choice.setAttributeBonuses(Map.of(attr, total));
            choices.add(choice);
        }

        return new LifepathStepResponse(5, TOTAL_STEPS, "Attributes", narrative, choices, session.name);
    }

    private void applyStep5Choice(LifepathSession session, String choiceId) {
        // In step 5, the player accepts all rolled attributes at once.
        // The choiceId is just a confirmation; we apply all the rolled values.
        // The values are already set in the session from buildStep5.
    }

    // ==================== Step 6: Disciplines ====================

    private LifepathStepResponse buildStep6(LifepathSession session) {
        String narrative = String.format(
            "%s must choose a career path. Your assignment determines your primary and secondary disciplines. " +
            "Your primary discipline gets a rating of 2, your secondary gets 1.", session.name
        );

        List<LifepathChoice> choices = new ArrayList<>();

        choices.add(buildDisciplineChoice("command", "Command Division",
                "Leading starships and making the tough decisions. You are the captain of your fate.",
                CaptainLogCharacter.Assignment.COMMAND,
                Map.of(CaptainLogCharacter.Discipline.COMMAND, 2, CaptainLogCharacter.Discipline.LEADERSHIP, 1),
                CaptainLogCharacter.Rank.LIEUTENANT));

        choices.add(buildDisciplineChoice("science", "Science Division",
                "Exploring the unknown through research and analysis. Discovery is your mission.",
                CaptainLogCharacter.Assignment.SCIENCE,
                Map.of(CaptainLogCharacter.Discipline.SCIENCE, 2, CaptainLogCharacter.Discipline.OPERATIONS, 1),
                CaptainLogCharacter.Rank.LIEUTENANT));

        choices.add(buildDisciplineChoice("security", "Security Division",
                "Protecting the ship and crew from threats. Vigilance is your watchword.",
                CaptainLogCharacter.Assignment.SECURITY,
                Map.of(CaptainLogCharacter.Discipline.SECURITY, 2, CaptainLogCharacter.Discipline.COMMAND, 1),
                CaptainLogCharacter.Rank.LIEUTENANT));

        choices.add(buildDisciplineChoice("engineering", "Engineering Division",
                "Keeping the ship running and pushing technology forward. Innovation is your tool.",
                CaptainLogCharacter.Assignment.ENGINEERING,
                Map.of(CaptainLogCharacter.Discipline.OPERATIONS, 2, CaptainLogCharacter.Discipline.SCIENCE, 1),
                CaptainLogCharacter.Rank.LIEUTENANT));

        choices.add(buildDisciplineChoice("medical", "Medical Division",
                "Healing the sick and injured. Compassion drives your work.",
                CaptainLogCharacter.Assignment.MEDICAL,
                Map.of(CaptainLogCharacter.Discipline.MEDICINE, 2, CaptainLogCharacter.Discipline.SCIENCE, 1),
                CaptainLogCharacter.Rank.LIEUTENANT));

        choices.add(buildDisciplineChoice("operations", "Operations Division",
                "Coordinating ship systems and managing resources. Efficiency is your strength.",
                CaptainLogCharacter.Assignment.OPERATIONS,
                Map.of(CaptainLogCharacter.Discipline.OPERATIONS, 2, CaptainLogCharacter.Discipline.LEADERSHIP, 1),
                CaptainLogCharacter.Rank.LIEUTENANT));

        return new LifepathStepResponse(6, TOTAL_STEPS, "Disciplines", narrative, choices, session.name);
    }

    private LifepathChoice buildDisciplineChoice(String id, String title, String desc,
                                                   CaptainLogCharacter.Assignment assignment,
                                                   Map<CaptainLogCharacter.Discipline, Integer> bonuses,
                                                   CaptainLogCharacter.Rank rank) {
        LifepathChoice choice = new LifepathChoice(id, title, desc);
        choice.setAssignment(assignment);
        choice.setDisciplineBonuses(bonuses);
        choice.setRank(rank);
        return choice;
    }

    private void applyStep6Choice(LifepathSession session, String choiceId) {
        Map<String, CaptainLogCharacter.Assignment> assignments = Map.of(
                "command", CaptainLogCharacter.Assignment.COMMAND,
                "science", CaptainLogCharacter.Assignment.SCIENCE,
                "security", CaptainLogCharacter.Assignment.SECURITY,
                "engineering", CaptainLogCharacter.Assignment.ENGINEERING,
                "medical", CaptainLogCharacter.Assignment.MEDICAL,
                "operations", CaptainLogCharacter.Assignment.OPERATIONS
        );
        session.assignment = assignments.getOrDefault(choiceId, CaptainLogCharacter.Assignment.COMMAND);

        Map<String, Map<CaptainLogCharacter.Discipline, Integer>> bonuses = Map.of(
                "command", Map.of(CaptainLogCharacter.Discipline.COMMAND, 2, CaptainLogCharacter.Discipline.LEADERSHIP, 1),
                "science", Map.of(CaptainLogCharacter.Discipline.SCIENCE, 2, CaptainLogCharacter.Discipline.OPERATIONS, 1),
                "security", Map.of(CaptainLogCharacter.Discipline.SECURITY, 2, CaptainLogCharacter.Discipline.COMMAND, 1),
                "engineering", Map.of(CaptainLogCharacter.Discipline.OPERATIONS, 2, CaptainLogCharacter.Discipline.SCIENCE, 1),
                "medical", Map.of(CaptainLogCharacter.Discipline.MEDICINE, 2, CaptainLogCharacter.Discipline.SCIENCE, 1),
                "operations", Map.of(CaptainLogCharacter.Discipline.OPERATIONS, 2, CaptainLogCharacter.Discipline.LEADERSHIP, 1)
        );
        Map<CaptainLogCharacter.Discipline, Integer> bonus = bonuses.getOrDefault(choiceId, Map.of());
        for (Map.Entry<CaptainLogCharacter.Discipline, Integer> entry : bonus.entrySet()) {
            session.disciplines.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        session.rank = CaptainLogCharacter.Rank.LIEUTENANT;
    }

    // ==================== Step 7: Focuses ====================

    private LifepathStepResponse buildStep7(LifepathSession session) {
        String narrative = String.format(
            "Within your disciplines, %s developed specializations called focuses. " +
            "Choose a focus that reflects your area of expertise.", session.name
        );

        List<LifepathChoice> choices = new ArrayList<>();

        // Generate focus options based on the character's primary discipline
        CaptainLogCharacter.Discipline primaryDiscipline = findPrimaryDiscipline(session);

        Map<CaptainLogCharacter.Discipline, List<String>> focusOptions = new EnumMap<>(CaptainLogCharacter.Discipline.class);
        focusOptions.put(CaptainLogCharacter.Discipline.COMMAND, List.of(
                "Starship Tactics", "Diplomatic Protocol", "Crisis Management", "Fleet Operations"
        ));
        focusOptions.put(CaptainLogCharacter.Discipline.LEADERSHIP, List.of(
                "Motivation", "Conflict Resolution", "Public Relations", "Team Building"
        ));
        focusOptions.put(CaptainLogCharacter.Discipline.SCIENCE, List.of(
                "Astrophysics", "Biology", "Chemistry", "Geology", "Xenobiology"
        ));
        focusOptions.put(CaptainLogCharacter.Discipline.SECURITY, List.of(
                "Hand-to-Hand Combat", "Firearms", "Investigation", "Interrogation"
        ));
        focusOptions.put(CaptainLogCharacter.Discipline.MEDICINE, List.of(
                "Surgery", "Pharmacology", "Psychiatry", "Epidemiology"
        ));
        focusOptions.put(CaptainLogCharacter.Discipline.OPERATIONS, List.of(
                "Engineering", "Navigation", "Communications", "Sensor Analysis"
        ));

        List<String> availableFocuses = focusOptions.getOrDefault(primaryDiscipline,
                focusOptions.get(CaptainLogCharacter.Discipline.COMMAND));

        for (int i = 0; i < availableFocuses.size(); i++) {
            String focusName = availableFocuses.get(i);
            LifepathChoice choice = new LifepathChoice(
                    "focus_" + i,
                    focusName,
                    "Specialize in " + focusName.toLowerCase() + " within " + primaryDiscipline.name().toLowerCase()
            );
            choice.setFocusName(focusName);
            choice.setFocusDiscipline(primaryDiscipline);
            choices.add(choice);
        }

        // Add a generic option
        LifepathChoice generic = new LifepathChoice(
                "focus_custom",
                "Custom Focus",
                "Define your own area of specialization."
        );
        generic.setFocusName("General Specialist");
        generic.setFocusDiscipline(primaryDiscipline);
        choices.add(generic);

        return new LifepathStepResponse(7, TOTAL_STEPS, "Focuses", narrative, choices, session.name);
    }

    private CaptainLogCharacter.Discipline findPrimaryDiscipline(LifepathSession session) {
        CaptainLogCharacter.Discipline primary = null;
        int max = 0;
        for (Map.Entry<CaptainLogCharacter.Discipline, Integer> entry : session.disciplines.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                primary = entry.getKey();
            }
        }
        return primary != null ? primary : CaptainLogCharacter.Discipline.COMMAND;
    }

    private void applyStep7Choice(LifepathSession session, String choiceId) {
        // The focus is stored in the choice; we'll apply it during finalization
        // For now, just mark that a focus was chosen
    }

    // ==================== Step 8: Values ====================

    private LifepathStepResponse buildStep8(LifepathSession session) {
        String narrative = String.format(
            "Finally, %s must define the core values that guide your actions. " +
            "Choose one value — these principles will shape your decisions throughout your career.", session.name
        );

        List<LifepathChoice> choices = new ArrayList<>();

        choices.add(new LifepathChoice("duty", "Duty",
                "You serve others above yourself. The mission comes first.",
                null, null, null, null, null, null, null, null, null, null, "Duty above all"));

        choices.add(new LifepathChoice("exploration", "Exploration",
                "The unknown calls to you. Discovery and understanding are your driving forces.",
                null, null, null, null, null, null, null, null, null, null, "The drive to explore"));

        choices.add(new LifepathChoice("justice", "Justice",
                "You stand for what is right, even when it's difficult. Fairness is paramount.",
                null, null, null, null, null, null, null, null, null, null, "Justice for all"));

        choices.add(new LifepathChoice("compassion", "Compassion",
                "You care deeply for others. Helping those in need defines who you are.",
                null, null, null, null, null, null, null, null, null, null, "Compassion guides me"));

        choices.add(new LifepathChoice("honor", "Honor",
                "Your word is your bond. Integrity and honor are non-negotiable.",
                null, null, null, null, null, null, null, null, null, null, "Honor is everything"));

        choices.add(new LifepathChoice("knowledge", "Knowledge",
                "Understanding the universe is your highest pursuit. Wisdom is power.",
                null, null, null, null, null, null, null, null, null, null, "Knowledge is power"));

        choices.add(new LifepathChoice("family", "Family",
                "Those you love are your anchor. Protecting them is your greatest motivation.",
                null, null, null, null, null, null, null, null, null, null, "Family first"));

        choices.add(new LifepathChoice("freedom", "Freedom",
                "You value liberty above all. No one should be oppressed or controlled.",
                null, null, null, null, null, null, null, null, null, null, "Freedom for all"));

        return new LifepathStepResponse(8, TOTAL_STEPS, "Values", narrative, choices, session.name);
    }

    private void applyStep8Choice(LifepathSession session, String choiceId) {
        Map<String, String> values = Map.of(
                "duty", "Duty above all",
                "exploration", "The drive to explore",
                "justice", "Justice for all",
                "compassion", "Compassion guides me",
                "honor", "Honor is everything",
                "knowledge", "Knowledge is power",
                "family", "Family first",
                "freedom", "Freedom for all"
        );
        session.values.add(values.getOrDefault(choiceId, "Undefined value"));
    }

    // ==================== Step Navigation ====================

    private LifepathStepResponse buildNextStep(LifepathSession session) {
        return switch (session.currentStep) {
            case 2 -> buildStep2(session);
            case 3 -> buildStep3(session);
            case 4 -> buildStep4(session);
            case 5 -> buildStep5(session);
            case 6 -> buildStep6(session);
            case 7 -> buildStep7(session);
            case 8 -> buildStep8(session);
            default -> null;
        };
    }

    // ==================== Finalization ====================

    private CaptainLogCharacter finalizeCharacter(LifepathSession session) {
        CaptainLogCharacter character = new CaptainLogCharacter();
        character.setName(session.name);
        character.setPronouns(session.pronouns);

        // Set traits from lifepath
        if (!session.traits.isEmpty()) character.setTrait1(session.traits.get(0));
        if (session.traits.size() > 1) character.setTrait2(session.traits.get(1));
        if (session.traits.size() > 2) character.setTrait3(session.traits.get(2));

        // Set background
        character.setEnvironment(session.environment != null ? session.environment : CaptainLogCharacter.Environment.CITY);
        character.setOutlook(session.outlook != null ? session.outlook : CaptainLogCharacter.Outlook.OPTIMISTIC);
        character.setEducation(session.education != null ? session.education : CaptainLogCharacter.Education.ACADEMY);
        character.setAssignment(session.assignment != null ? session.assignment : CaptainLogCharacter.Assignment.COMMAND);
        character.setRank(session.rank != null ? session.rank : CaptainLogCharacter.Rank.ENSIGN);

        // Set attributes (apply rolled values, capping at reasonable max)
        character.setFitness(Math.min(session.attributes.getOrDefault(CaptainLogCharacter.Attribute.FITNESS, 0), 6));
        character.setTechnical(Math.min(session.attributes.getOrDefault(CaptainLogCharacter.Attribute.TECHNICAL, 0), 6));
        character.setStrength(Math.min(session.attributes.getOrDefault(CaptainLogCharacter.Attribute.STRENGTH, 0), 6));
        character.setReason(Math.min(session.attributes.getOrDefault(CaptainLogCharacter.Attribute.REASON, 0), 6));
        character.setInsight(Math.min(session.attributes.getOrDefault(CaptainLogCharacter.Attribute.INSIGHT, 0), 6));
        character.setPresence(Math.min(session.attributes.getOrDefault(CaptainLogCharacter.Attribute.PRESENCE, 0), 6));

        // Constitution = average of STR and INS
        character.setConstitution((character.getStrength() + character.getInsight()) / 2);

        // Set disciplines
        character.setCommand(session.disciplines.getOrDefault(CaptainLogCharacter.Discipline.COMMAND, 0));
        character.setLeadership(session.disciplines.getOrDefault(CaptainLogCharacter.Discipline.LEADERSHIP, 0));
        character.setScience(session.disciplines.getOrDefault(CaptainLogCharacter.Discipline.SCIENCE, 0));
        character.setSecurity(session.disciplines.getOrDefault(CaptainLogCharacter.Discipline.SECURITY, 0));
        character.setMedicine(session.disciplines.getOrDefault(CaptainLogCharacter.Discipline.MEDICINE, 0));
        character.setOperations(session.disciplines.getOrDefault(CaptainLogCharacter.Discipline.OPERATIONS, 0));

        // Add values
        for (String valueDesc : session.values) {
            if (character.getValues().size() < 3) {
                CaptainLogValue value = new CaptainLogValue(valueDesc);
                value.setCharacter(character);
                character.getValues().add(value);
            }
        }

        return character;
    }
}
