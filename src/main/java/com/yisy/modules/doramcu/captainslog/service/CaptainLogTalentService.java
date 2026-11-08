package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacterTalent;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterTalentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing character talents in the Captain's Log Solo RPG.
 * <p>
 * Talents are special abilities that provide unique mechanical benefits.
 * This service provides CRUD operations and talent suggestions based on
 * the character's species, background, education, and career path.
 * </p>
 */
@Service
public class CaptainLogTalentService {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogTalentService.class);
    private final CaptainLogCharacterTalentRepository talentRepository;
    private final CaptainLogCharacterRepository characterRepository;

    public CaptainLogTalentService(CaptainLogCharacterTalentRepository talentRepository,
                                    CaptainLogCharacterRepository characterRepository) {
        this.talentRepository = talentRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Predefined Talent Pools ====================

    /**
     * Species-specific talents — innate biological abilities.
     */
    private static final Map<String, List<TalentDefinition>> SPECIES_TALENTS = new LinkedHashMap<>();

    static {
        // Human talents
        SPECIES_TALENTS.put("HUMAN", Arrays.asList(
                new TalentDefinition("Adaptability", "Humans can adapt to almost any situation.",
                        "Gain a boon on one task attempt per mission when facing unfamiliar circumstances.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Persistence", "Humans never give up.",
                        "Once per mission, you may reroll a failed task attempt.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Diplomatic Immunity", "Humans are skilled negotiators.",
                        "Gain a boon on Presence + Leadership checks during diplomacy.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));

        // Vulcan talents
        SPECIES_TALENTS.put("VULCAN", Arrays.asList(
                new TalentDefinition("Vulcan Nerve Pinch", "A precise strike can render an opponent unconscious.",
                        "On a successful Fitness + Security check in combat, you can stun an opponent for one round.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Logical Analysis", "Vulcans see patterns others miss.",
                        "Gain a boon on Reason + Science checks.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Mental Discipline", "Vulcans can shield their minds.",
                        "Gain a boon on Insight-based checks against psychological effects.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));

        // Klingon talents
        SPECIES_TALENTS.put("KLINGON", Arrays.asList(
                new TalentDefinition("Klingon Combat Training", "Klingons are born warriors.",
                        "Gain a boon on Fitness + Security checks in combat.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Regenerative Healing", "Klingons heal faster than most species.",
                        "Recover from injury one beat earlier than normal.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Honor Bound", "Klingons keep their word.",
                        "When you make a promise and keep it, gain 1 Momentum.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));

        // Betazoid talents
        SPECIES_TALENTS.put("BETAZOID", Arrays.asList(
                new TalentDefinition("Empathy", "Betazoids can sense emotions.",
                        "Gain a boon on Presence + Leadership checks when reading others.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Telepathic Sensitivity", "Some Betazoids can read thoughts.",
                        "Once per mission, automatically succeed on a Presence + Insight check to detect deception.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));

        // Andorian talents
        SPECIES_TALENTS.put("ANDORIAN", Arrays.asList(
                new TalentDefinition("Cold Resistance", "Andorians thrive in cold environments.",
                        "Gain a boon on Fitness checks in cold or freezing environments.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Antenna Senses", "Andorian antennae detect subtle changes.",
                        "Gain a boon on Insight + Science checks to detect anomalies.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));

        // Trill talents
        SPECIES_TALENTS.put("TRILL", Arrays.asList(
                new TalentDefinition("Symbiont Wisdom", "Trill have the knowledge of previous hosts.",
                        "Gain a boon on Reason + Science checks for academic knowledge.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Extended Lifespan", "Trill live much longer than most species.",
                        "Gain a boon on Insight + Leadership checks involving long-term strategy.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));

        // Ferengi talents
        SPECIES_TALENTS.put("FERENGI", Arrays.asList(
                new TalentDefinition("Profit Motive", "Ferengi are driven by gain.",
                        "Gain a boon on Presence + Command checks during negotiations involving trade.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Resourcefulness", "Ferengi can find value anywhere.",
                        "Once per mission, treat a failed Technical check as a success with 0 successes.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));

        // Denobulan talents
        SPECIES_TALENTS.put("DENOBULAN", Arrays.asList(
                new TalentDefinition("Medical Mastery", "Denobulans are the finest healers.",
                        "Gain a boon on all Medicine-related task attempts.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Enhanced Physiology", "Denobulan bodies are highly resilient.",
                        "Start missions with 1 additional hit point.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));

        // Cardassian talents
        SPECIES_TALENTS.put("CARDASSIAN", Arrays.asList(
                new TalentDefinition("Tactical Cunning", "Cardassians are master strategists.",
                        "Gain a boon on Reason + Command checks for tactical planning.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Interrogation Expert", "Cardassians are skilled at extracting information.",
                        "Gain a boon on Presence + Security checks during interrogation.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));

        // Romulan talents
        SPECIES_TALENTS.put("ROMULAN", Arrays.asList(
                new TalentDefinition("Stealth Training", "Romulans are masters of secrecy.",
                        "Gain a boon on Fitness + Operations checks for stealth tasks.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES),
                new TalentDefinition("Political Intrigue", "Romulans navigate complex politics.",
                        "Gain a boon on Presence + Leadership checks in political situations.",
                        CaptainLogCharacterTalent.TalentSource.SPECIES)
        ));
    }

    /**
     * Lifepath talents — gained through life experiences.
     */
    private static final List<TalentDefinition> LIFEPATH_TALENTS = Arrays.asList(
            new TalentDefinition("Street Smart", "You learned to survive on your own.",
                    "Gain a boon on Insight + Security checks in urban environments.",
                    CaptainLogCharacterTalent.TalentSource.LIFEPATH),
            new TalentDefinition("Natural Leader", "People follow your lead instinctively.",
                    "Gain a boon on Presence + Leadership checks.",
                    CaptainLogCharacterTalent.TalentSource.LIFEPATH),
            new TalentDefinition("Survivalist", "You know how to endure harsh conditions.",
                    "Gain a boon on Fitness + Operations checks in wilderness environments.",
                    CaptainLogCharacterTalent.TalentSource.LIFEPATH),
            new TalentDefinition("Tech Whiz", "Technology comes naturally to you.",
                    "Gain a boon on Technical + Operations checks.",
                    CaptainLogCharacterTalent.TalentSource.LIFEPATH),
            new TalentDefinition("People Person", "You connect with others easily.",
                    "Gain a boon on Presence + Medicine checks when comforting others.",
                    CaptainLogCharacterTalent.TalentSource.LIFEPATH),
            new TalentDefinition("Quick Thinker", "You process information rapidly.",
                    "Once per mission, gain a boon on a Reason-based check.",
                    CaptainLogCharacterTalent.TalentSource.LIFEPATH),
            new TalentDefinition("Iron Will", "You resist pressure and intimidation.",
                    "Gain a boon on Insight + Security checks against fear or intimidation.",
                    CaptainLogCharacterTalent.TalentSource.LIFEPATH),
            new TalentDefinition("Explorer's Eye", "You notice details others miss.",
                    "Gain a boon on Insight + Science checks during exploration.",
                    CaptainLogCharacterTalent.TalentSource.LIFEPATH)
    );

    /**
     * Career talents — earned through advancement.
     */
    private static final List<TalentDefinition> CAREER_TALENTS = Arrays.asList(
            new TalentDefinition("Command Authority", "Your rank carries weight.",
                    "Gain a boon on Presence + Command checks when giving orders.",
                    CaptainLogCharacterTalent.TalentSource.CAREER),
            new TalentDefinition("Tactical Expert", "You've studied combat extensively.",
                    "Gain a boon on Reason + Security checks for tactical analysis.",
                    CaptainLogCharacterTalent.TalentSource.CAREER),
            new TalentDefinition("Medical Expert", "Years of medical training pay off.",
                    "Gain a boon on Technical + Medicine checks for complex procedures.",
                    CaptainLogCharacterTalent.TalentSource.CAREER),
            new TalentDefinition("Engineering Specialist", "You understand ship systems deeply.",
                    "Gain a boon on Technical + Operations checks for repairs.",
                    CaptainLogCharacterTalent.TalentSource.CAREER),
            new TalentDefinition("Science Authority", "Your scientific reputation precedes you.",
                    "Gain a boon on Reason + Science checks for research.",
                    CaptainLogCharacterTalent.TalentSource.CAREER),
            new TalentDefinition("Security Clearance", "You have access to classified information.",
                    "Gain a boon on Insight + Security checks for intelligence gathering.",
                    CaptainLogCharacterTalent.TalentSource.CAREER),
            new TalentDefinition("Diplomatic Training", "You're skilled in formal diplomacy.",
                    "Gain a boon on Presence + Leadership checks during formal negotiations.",
                    CaptainLogCharacterTalent.TalentSource.CAREER),
            new TalentDefinition("Field Experience", "You've been in the thick of it.",
                    "Once per mission, spend 1 Momentum to gain a boon on any check.",
                    CaptainLogCharacterTalent.TalentSource.CAREER)
    );

    /**
     * Training talents — learned through formal education.
     */
    private static final List<TalentDefinition> TRAINING_TALENTS = Arrays.asList(
            new TalentDefinition("Academic Excellence", "Your education was top-tier.",
                    "Gain a boon on Reason-based checks for academic subjects.",
                    CaptainLogCharacterTalent.TalentSource.TRAINING),
            new TalentDefinition("Combat Training", "You've been trained in hand-to-hand combat.",
                    "Gain a boon on Fitness + Security checks in unarmed combat.",
                    CaptainLogCharacterTalent.TalentSource.TRAINING),
            new TalentDefinition("Starship Operations", "You know how to run a ship.",
                    "Gain a boon on Technical + Operations checks for ship systems.",
                    CaptainLogCharacterTalent.TalentSource.TRAINING),
            new TalentDefinition("First Aid", "You can provide emergency medical care.",
                    "Gain a boon on Fitness + Medicine checks for first aid.",
                    CaptainLogCharacterTalent.TalentSource.TRAINING),
            new TalentDefinition("Navigation", "You can plot courses through space.",
                    "Gain a boon on Reason + Operations checks for navigation.",
                    CaptainLogCharacterTalent.TalentSource.TRAINING),
            new TalentDefinition("Communications", "You're skilled with comms equipment.",
                    "Gain a boon on Technical + Science checks for communications.",
                    CaptainLogCharacterTalent.TalentSource.TRAINING)
    );

    /**
     * Talent definition for easy creation.
     */
    public static class TalentDefinition {
        private final String name;
        private final String description;
        private final String mechanicalEffect;
        private final CaptainLogCharacterTalent.TalentSource source;

        public TalentDefinition(String name, String description, String mechanicalEffect, CaptainLogCharacterTalent.TalentSource source) {
            this.name = name;
            this.description = description;
            this.mechanicalEffect = mechanicalEffect;
            this.source = source;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getMechanicalEffect() { return mechanicalEffect; }
        public CaptainLogCharacterTalent.TalentSource getSource() { return source; }
    }

    // ==================== CRUD Operations ====================

    /**
     * Creates a new talent for a character.
     */
    @Transactional
    public CaptainLogCharacterTalent createTalent(Long characterId, String name, String description,
                                                   String mechanicalEffect, CaptainLogCharacterTalent.TalentSource source) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new NoSuchElementException("Character not found: " + characterId));

        CaptainLogCharacterTalent talent = new CaptainLogCharacterTalent(name, description, mechanicalEffect, source);
        talent.setCharacter(character);
        CaptainLogCharacterTalent saved = talentRepository.save(talent);
        log.info("Created talent '{}' for character '{}'", name, character.getName());
        return saved;
    }

    /**
     * Gets a talent by ID.
     */
    public Optional<CaptainLogCharacterTalent> getTalent(Long talentId) {
        return talentRepository.findById(talentId);
    }

    /**
     * Lists all talents for a character.
     */
    public List<CaptainLogCharacterTalent> listTalents(Long characterId) {
        return talentRepository.findByCharacterId(characterId);
    }

    /**
     * Lists all active talents for a character.
     */
    public List<CaptainLogCharacterTalent> listActiveTalents(Long characterId) {
        return talentRepository.findByCharacterIdAndActiveTrue(characterId);
    }

    /**
     * Deletes a talent.
     */
    @Transactional
    public void deleteTalent(Long talentId) {
        if (!talentRepository.existsById(talentId)) {
            throw new NoSuchElementException("Talent not found: " + talentId);
        }
        talentRepository.deleteById(talentId);
        log.info("Deleted talent {}", talentId);
    }

    /**
     * Toggles a talent's active status.
     */
    @Transactional
    public CaptainLogCharacterTalent toggleActive(Long talentId) {
        CaptainLogCharacterTalent talent = talentRepository.findById(talentId)
                .orElseThrow(() -> new NoSuchElementException("Talent not found: " + talentId));
        talent.setActive(!talent.isActive());
        return talentRepository.save(talent);
    }

    // ==================== Talent Suggestions ====================

    /**
     * Suggests talents based on a character's species.
     */
    public List<TalentDefinition> suggestSpeciesTalents(String species) {
        String key = species.toUpperCase();
        return SPECIES_TALENTS.getOrDefault(key, Collections.emptyList());
    }

    /**
     * Suggests talents based on a character's lifepath.
     */
    public List<TalentDefinition> suggestLifepathTalents() {
        return new ArrayList<>(LIFEPATH_TALENTS);
    }

    /**
     * Suggests talents based on career progression.
     */
    public List<TalentDefinition> suggestCareerTalents() {
        return new ArrayList<>(CAREER_TALENTS);
    }

    /**
     * Suggests talents based on training background.
     */
    public List<TalentDefinition> suggestTrainingTalents() {
        return new ArrayList<>(TRAINING_TALENTS);
    }

    /**
     * Suggests all available talents for a character, excluding those they already have.
     */
    public List<TalentDefinition> suggestTalentsForCharacter(CaptainLogCharacter character) {
        Set<String> existingNames = listTalents(character.getId()).stream()
                .map(CaptainLogCharacterTalent::getName)
                .collect(Collectors.toSet());

        List<TalentDefinition> suggestions = new ArrayList<>();

        // Add species talents
        String species = character.getTrait1(); // species stored in trait1 from lifepath
        if (species != null) {
            suggestSpeciesTalents(species).forEach(t -> {
                if (!existingNames.contains(t.getName())) {
                    suggestions.add(t);
                }
            });
        }

        // Add lifepath talents
        LIFEPATH_TALENTS.forEach(t -> {
            if (!existingNames.contains(t.getName())) {
                suggestions.add(t);
            }
        });

        // Add career talents
        CAREER_TALENTS.forEach(t -> {
            if (!existingNames.contains(t.getName())) {
                suggestions.add(t);
            }
        });

        // Add training talents
        TRAINING_TALENTS.forEach(t -> {
            if (!existingNames.contains(t.getName())) {
                suggestions.add(t);
            }
        });

        return suggestions;
    }

    /**
     * Gets all available talent definitions by source.
     */
    public Map<String, List<TalentDefinition>> getAllTalentPools() {
        Map<String, List<TalentDefinition>> pools = new LinkedHashMap<>();
        pools.put("Species", new ArrayList<>());
        pools.put("Lifepath", new ArrayList<>(LIFEPATH_TALENTS));
        pools.put("Career", new ArrayList<>(CAREER_TALENTS));
        pools.put("Training", new ArrayList<>(TRAINING_TALENTS));

        // Flatten species talents
        SPECIES_TALENTS.values().forEach(pools.get("Species")::addAll);

        return pools;
    }
}
