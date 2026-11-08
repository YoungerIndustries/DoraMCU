package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogShipTalent;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogShipTalentRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogStarshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing starship talents in the Captain's Log Solo RPG.
 * <p>
 * Ship talents provide special capabilities that modify starship gameplay.
 * This service provides CRUD operations and talent suggestions based on
 * the starship's class, role, and configuration.
 * </p>
 */
@Service
public class CaptainLogShipTalentService {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogShipTalentService.class);
    private final CaptainLogShipTalentRepository talentRepository;
    private final CaptainLogStarshipRepository starshipRepository;

    public CaptainLogShipTalentService(CaptainLogShipTalentRepository talentRepository,
                                        CaptainLogStarshipRepository starshipRepository) {
        this.talentRepository = talentRepository;
        this.starshipRepository = starshipRepository;
    }

    // ==================== Predefined Talent Pools ====================

    /**
     * Ship class-specific talents — innate to the design.
     */
    private static final Map<String, List<ShipTalentDefinition>> CLASS_TALENTS = new LinkedHashMap<>();

    static {
        // Constitution-class talents
        CLASS_TALENTS.put("CONSTITUTION", Arrays.asList(
                new ShipTalentDefinition("Constitution Durability", "Constitution-class ships are rugged and reliable.",
                        "Gain a boon on Structure-based task attempts."),
                new ShipTalentDefinition("Standard Armament", "Well-armed for their size.",
                        "Gain a boon on Weapons + Security checks."),
                new ShipTalentDefinition("Shuttle Bay", "Carries multiple shuttles for away missions.",
                        "Gain a boon on Operations-based checks for shuttle operations.")
        ));

        // Galaxy-class talents
        CLASS_TALENTS.put("GALAXY", Arrays.asList(
                new ShipTalentDefinition("Exploration Suite", "Galaxy-class ships are built for deep exploration.",
                        "Gain a boon on Sensors + Science checks."),
                new ShipTalentDefinition("Advanced Laboratories", "State-of-the-art research facilities.",
                        "Gain a boon on Computers + Science checks."),
                new ShipTalentDefinition("Massive Shuttle Bay", "One of the largest shuttle bays in the fleet.",
                        "Gain a boon on Operations-based checks for away team deployment."),
                new ShipTalentDefinition("Cultural Database", "Extensive library of alien cultures.",
                        "Gain a boon on Comms + Leadership checks for diplomatic missions.")
        ));

        // Sovereign-class talents
        CLASS_TALENTS.put("SOVEREIGN", Arrays.asList(
                new ShipTalentDefinition("Enhanced Speed", "Sovereign-class ships are faster than most capital ships.",
                        "Gain a boon on Engines + Operations checks for speed maneuvers."),
                new ShipTalentDefinition("Heavy Armament", "More powerful weapons than Galaxy-class.",
                        "Gain a boon on Weapons + Security checks in starship combat."),
                new ShipTalentDefinition("Advanced Sensors", "Longer-range sensors than standard.",
                        "Gain a boon on Sensors + Science checks for detection.")
        ));

        // Nebula-class talents
        CLASS_TALENTS.put("NEBULA", Arrays.asList(
                new ShipTalentDefinition("Medical Facility", "Nebula-class ships have the best medical facilities.",
                        "Gain a boon on Computers + Medicine checks for medical operations."),
                new ShipTalentDefinition("Science Laboratory", "Dedicated science labs for research.",
                        "Gain a boon on Sensors + Science checks for analysis."),
                new ShipTalentDefinition("Diplomatic Suite", "Designed for diplomatic missions.",
                        "Gain a boon on Comms + Leadership checks during negotiations.")
        ));

        // Akira-class talents
        CLASS_TALENTS.put("AKIRA", Arrays.asList(
                new ShipTalentDefinition("Heavy Shielding", "Akira-class ships have powerful shields.",
                        "Gain a boon on Structure-based checks to resist damage."),
                new ShipTalentDefinition("Torpedo Spread", "Can fire multiple torpedoes at once.",
                        "Gain a boon on Weapons + Security checks for torpedo attacks."),
                new ShipTalentDefinition("Reinforced Hull", "Built for combat endurance.",
                        "Start with 1 additional hit in starship combat.")
        ));

        // Intrepid-class talents
        CLASS_TALENTS.put("INTREPID", Arrays.asList(
                new ShipTalentDefinition("Warp Core Instability", "Intrepid warp cores can push beyond normal limits.",
                        "Once per mission, gain a boon on an Engines check, but generate 1 Threat."),
                new ShipTalentDefinition("Stealth Systems", "Advanced cloaking device integration.",
                        "Gain a boon on Sensors + Operations checks for stealth maneuvers."),
                new ShipTalentDefinition("Rapid Deployment", "Quick shuttle and probe deployment.",
                        "Gain a boon on Operations-based checks for probe operations.")
        ));

        // Defiant-class talents
        CLASS_TALENTS.put("DEFIANT", Arrays.asList(
                new ShipTalentDefinition("Point-Defense Mastery", "Defiant-class excels at point defense.",
                        "Gain a boon on Weapons + Security checks against incoming fire."),
                new ShipTalentDefinition("Armor Plating", "Heavy armor makes Defiants hard to destroy.",
                        "Gain a boon on Structure-based checks to resist damage."),
                new ShipTalentDefinition("Agile Maneuvering", "Small size allows quick maneuvers.",
                        "Gain a boon on Engines + Operations checks for evasive maneuvers.")
        ));

        // D7-class (Klingon) talents
        CLASS_TALENTS.put("D7", Arrays.asList(
                new ShipTalentDefinition("Klingon Durability", "D7-class ships are built for battle.",
                        "Gain a boon on Structure-based checks to resist damage."),
                new ShipTalentDefinition("Quantum Torpedoes", "Powerful quantum torpedo armament.",
                        "Gain a boon on Weapons + Security checks for torpedo attacks."),
                new ShipTalentDefinition("Warrior Culture", "Klingon crews fight with honor.",
                        "Gain a boon on all combat-related task attempts.")
        ));

        // Roma-class (Romulan) talents
        CLASS_TALENTS.put("ROMA", Arrays.asList(
                new ShipTalentDefinition("Cloaking Device", "Romulan ships can go invisible.",
                        "Once per mission, automatically succeed on a stealth check."),
                new ShipTalentDefinition("Plasma Torpedoes", "Romulan plasma weapons are devastating.",
                        "Gain a boon on Weapons + Security checks for torpedo attacks."),
                new ShipTalentDefinition("Tactical Stealth", "Romulan ships excel at ambush tactics.",
                        "Gain a boon on Sensors + Operations checks for ambush setups.")
        ));

        // Cardassian Galor-class talents
        CLASS_TALENTS.put("GALOR", Arrays.asList(
                new ShipTalentDefinition("Heavy Firepower", "Galor-class ships are heavily armed.",
                        "Gain a boon on Weapons + Security checks in starship combat."),
                new ShipTalentDefinition("Industrial Capacity", "Can produce supplies and repairs at sea.",
                        "Gain a boon on Computers + Operations checks for repairs."),
                new ShipTalentDefinition("Occupation Suite", "Designed for planetary occupation.",
                        "Gain a boon on Comms + Security checks for occupation operations.")
        ));
    }

    /**
     * Role-based talents — gained through mission specialization.
     */
    private static final List<ShipTalentDefinition> ROLE_TALENTS = Arrays.asList(
            new ShipTalentDefinition("Diplomatic Mission", "Ship configured for diplomatic operations.",
                    "Gain a boon on Comms + Leadership checks during diplomatic missions."),
            new ShipTalentDefinition("Science Mission", "Ship configured for scientific research.",
                    "Gain a boon on Sensors + Science checks during exploration."),
            new ShipTalentDefinition("Combat Mission", "Ship configured for combat operations.",
                    "Gain a boon on Weapons + Security checks in starship combat."),
            new ShipTalentDefinition("Rescue Mission", "Ship configured for rescue operations.",
                    "Gain a boon on Engines + Operations checks for rescue maneuvers."),
            new ShipTalentDefinition("Patrol Mission", "Ship configured for patrol duties.",
                    "Gain a boon on Sensors + Security checks for detection."),
            new ShipTalentDefinition("Escort Mission", "Ship configured to protect other vessels.",
                    "Gain a boon on Structure-based checks when protecting another ship."),
            new ShipTalentDefinition("Reconnaissance", "Ship configured for stealth reconnaissance.",
                    "Gain a boon on Sensors + Operations checks for stealth operations."),
            new ShipTalentDefinition("Deep Space", "Ship configured for extended deep space operations.",
                    "Gain a boon on Computers + Operations checks for long-range navigation.")
    );

    /**
     * Upgrade talents — earned through refit and upgrades.
     */
    private static final List<ShipTalentDefinition> UPGRADE_TALENTS = Arrays.asList(
            new ShipTalentDefinition("Shield Upgrade", "Enhanced shield generators installed.",
                    "Gain a boon on Structure-based checks to resist damage."),
            new ShipTalentDefinition("Weapon Upgrade", "More powerful weapons installed.",
                    "Gain a boon on Weapons + Security checks for attacks."),
            new ShipTalentDefinition("Sensor Upgrade", "Advanced sensor array installed.",
                    "Gain a boon on Sensors + Science checks for detection."),
            new ShipTalentDefinition("Engine Upgrade", "Improved warp core installed.",
                    "Gain a boon on Engines + Operations checks for speed."),
            new ShipTalentDefinition("Computer Upgrade", "Enhanced computer systems installed.",
                    "Gain a boon on Computers-based checks for all operations."),
            new ShipTalentDefinition("Medical Upgrade", "Advanced medical facilities installed.",
                    "Gain a boon on Computers + Medicine checks for medical operations."),
            new ShipTalentDefinition("Communications Upgrade", "Long-range comms array installed.",
                    "Gain a boon on Comms + Science checks for communications."),
            new ShipTalentDefinition("Hull Repair", "Structural repairs and reinforcements.",
                    "Recover 1 hit point during downtime.")
    );

    /**
     * Ship talent definition for easy creation.
     */
    public static class ShipTalentDefinition {
        private final String name;
        private final String description;
        private final String mechanicalEffect;

        public ShipTalentDefinition(String name, String description, String mechanicalEffect) {
            this.name = name;
            this.description = description;
            this.mechanicalEffect = mechanicalEffect;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getMechanicalEffect() { return mechanicalEffect; }
    }

    // ==================== CRUD Operations ====================

    /**
     * Creates a new talent for a starship.
     */
    @Transactional
    public CaptainLogShipTalent createTalent(Long starshipId, String name, String description) {
        CaptainLogStarship starship = starshipRepository.findById(starshipId)
                .orElseThrow(() -> new NoSuchElementException("Starship not found: " + starshipId));

        CaptainLogShipTalent talent = new CaptainLogShipTalent(name, description);
        talent.setStarship(starship);
        CaptainLogShipTalent saved = talentRepository.save(talent);
        log.info("Created ship talent '{}' for starship '{}'", name, starship.getName());
        return saved;
    }

    /**
     * Gets a talent by ID.
     */
    public Optional<CaptainLogShipTalent> getTalent(Long talentId) {
        return talentRepository.findById(talentId);
    }

    /**
     * Lists all talents for a starship.
     */
    public List<CaptainLogShipTalent> listTalents(Long starshipId) {
        return talentRepository.findByStarshipId(starshipId);
    }

    /**
     * Deletes a talent.
     */
    @Transactional
    public void deleteTalent(Long talentId) {
        if (!talentRepository.existsById(talentId)) {
            throw new NoSuchElementException("Ship talent not found: " + talentId);
        }
        talentRepository.deleteById(talentId);
        log.info("Deleted ship talent {}", talentId);
    }

    // ==================== Talent Suggestions ====================

    /**
     * Suggests talents based on a starship's class.
     */
    public List<ShipTalentDefinition> suggestClassTalents(String shipClass) {
        String key = shipClass.toUpperCase();
        return CLASS_TALENTS.getOrDefault(key, Collections.emptyList());
    }

    /**
     * Suggests talents based on mission role.
     */
    public List<ShipTalentDefinition> suggestRoleTalents() {
        return new ArrayList<>(ROLE_TALENTS);
    }

    /**
     * Suggests talents from upgrades and refits.
     */
    public List<ShipTalentDefinition> suggestUpgradeTalents() {
        return new ArrayList<>(UPGRADE_TALENTS);
    }

    /**
     * Suggests all available talents for a starship, excluding those it already has.
     */
    public List<ShipTalentDefinition> suggestTalentsForStarship(CaptainLogStarship starship) {
        Set<String> existingNames = listTalents(starship.getId()).stream()
                .map(CaptainLogShipTalent::getName)
                .collect(Collectors.toSet());

        List<ShipTalentDefinition> suggestions = new ArrayList<>();

        // Add class talents
        String shipClass = starship.getShipClass();
        if (shipClass != null) {
            suggestClassTalents(shipClass).forEach(t -> {
                if (!existingNames.contains(t.getName())) {
                    suggestions.add(t);
                }
            });
        }

        // Add role talents
        ROLE_TALENTS.forEach(t -> {
            if (!existingNames.contains(t.getName())) {
                suggestions.add(t);
            }
        });

        // Add upgrade talents
        UPGRADE_TALENTS.forEach(t -> {
            if (!existingNames.contains(t.getName())) {
                suggestions.add(t);
            }
        });

        return suggestions;
    }

    /**
     * Gets all available talent definitions by category.
     */
    public Map<String, List<ShipTalentDefinition>> getAllTalentPools() {
        Map<String, List<ShipTalentDefinition>> pools = new LinkedHashMap<>();
        pools.put("Class", new ArrayList<>());
        pools.put("Role", new ArrayList<>(ROLE_TALENTS));
        pools.put("Upgrade", new ArrayList<>(UPGRADE_TALENTS));

        // Flatten class talents
        CLASS_TALENTS.values().forEach(pools.get("Class")::addAll);

        return pools;
    }

    /**
     * Gets all available ship class names for talent lookup.
     */
    public List<String> getAvailableShipClasses() {
        return new ArrayList<>(CLASS_TALENTS.keySet());
    }
}
