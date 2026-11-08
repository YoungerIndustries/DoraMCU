package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides Mission Development events for the Captain's Log Solo RPG.
 * <p>
 * Mission developments are narrative twists that occur between beats during a mission.
 * They add unexpected complications, opportunities, or changes to the situation.
 * The rulebook describes these as events that keep the mission dynamic and unpredictable.
 * </p>
 * <p>
 * Developments are organized by category:
 * <ul>
 *   <li>COMPLICATION — something goes wrong or gets worse</li>
 *   <li>OPPORTUNITY — something unexpected helps the situation</li>
 *   <li>REVELATION — new information comes to light</li>
 *   <li>CHARACTER — the character's personal life is affected</li>
 *   <li>STARSHIP — the ship experiences an issue or discovery</li>
 *   <li>ENVIRONMENT — the setting changes in an unexpected way</li>
 * </ul>
 * </p>
 * <p>
 * The service also provides mission-type-aware development selection,
 * choosing developments that fit the current mission context.
 * </p>
 */
@Component
public class MissionDevelopmentService {

    private static final Logger log = LoggerFactory.getLogger(MissionDevelopmentService.class);
    private final Random random = new Random();

    // ==================== Development Categories ====================

    /**
     * Categories of mission developments.
     */
    public enum Category {
        COMPLICATION("Something goes wrong or gets worse."),
        OPPORTUNITY("Something unexpected helps the situation."),
        REVELATION("New information comes to light."),
        CHARACTER("The character's personal life is affected."),
        STARSHIP("The ship experiences an issue or discovery."),
        ENVIRONMENT("The setting changes in an unexpected way.");

        private final String description;

        Category(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== Development Tables by Category ====================

    /**
     * Complication developments — things that go wrong.
     */
    private static final List<DevelopmentDefinition> COMPLICATIONS = new ArrayList<>();

    static {
        COMPLICATIONS.addAll(Arrays.asList(
                new DevelopmentDefinition("Reinforcements Arrive", "The enemy gets backup, escalating the conflict.",
                        "The opposition gains an advantage. Spend 1 Momentum to negate, or accept a threat on the next check.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Equipment Failure", "Critical gear malfunctions at a bad time.",
                        "The next task attempt using that equipment suffers a threat.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Communication Lost", "Contact with support is severed.",
                        "You cannot call for help until the next beat. Gain 1 Threat.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Time Pressure", "The clock is ticking — you have less time than expected.",
                        "The next two task attempts suffer a threat.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Casualties", "An ally is injured or killed.",
                        "The mission takes a personal toll. Gain 1 Threat and reflect on your values.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Betrayal", "Someone you trusted was working against you.",
                        "A key ally reveals hidden motives. The situation worsens significantly.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Environmental Hazard", "The environment turns hostile.",
                        "All task attempts in the current scene suffer a threat.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Power Failure", "Systems go offline unexpectedly.",
                        "Starship systems take 1 stress. The next check using that system suffers a threat.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Ambush", "You're attacked from an unexpected direction.",
                        "The next combat check suffers 2 threats.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Resource Depletion", "Supplies or fuel run low.",
                        "You must adapt your plan. Gain 1 Threat.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Interference", "A third party disrupts your plans.",
                        "An unexpected force enters the scene. Roll on the Encounter table.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Structural Damage", "The ship or facility sustains damage.",
                        "Starship Structure takes 1 stress. A task attempt is required to assess the damage.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Moral Dilemma", "You face a difficult ethical choice.",
                        "Reflect on your values. Choosing against your values generates 1 Threat.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Escape Attempt", "A prisoner or enemy tries to flee.",
                        "A Security check is required to prevent the escape.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("System Overload", "Overuse pushes systems beyond their limits.",
                        "A starship system takes 2 stress. A task attempt is required to stabilize.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Political Fallout", "Your actions have diplomatic consequences.",
                        "A higher authority takes notice. Gain 1 Threat and note the political complication.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Injury", "You or an ally is wounded.",
                        "Take 1 damage. If you're already injured, gain 1 Threat.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Sabotage", "Someone has tampered with critical systems.",
                        "A system check is required to identify and fix the sabotage.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Unexpected Alliance", "An enemy offers a temporary truce.",
                        "The offer is suspicious. Accepting generates 1 Threat; refusing may escalate conflict.",
                        Category.COMPLICATION),
                new DevelopmentDefinition("Chain Reaction", "One failure triggers a cascade of problems.",
                        "Gain 2 Threats. The next task attempt in a different area also suffers a threat.",
                        Category.COMPLICATION)
        ));
    }

    /**
     * Opportunity developments — things that help.
     */
    private static final List<DevelopmentDefinition> OPPORTUNITIES = new ArrayList<>();

    static {
        OPPORTUNITIES.addAll(Arrays.asList(
                new DevelopmentDefinition("Reinforcements Arrive", "Help arrives just in time.",
                        "Gain 1 Momentum. The next task attempt gains a boon.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Lucky Break", "Fortune smiles on you.",
                        "Spend 1 Threat, or gain 1 Momentum.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Local Ally", "A local offers assistance.",
                        "Gain a boon on the next task attempt in this area.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Enemy Weakness", "You discover a vulnerability.",
                        "The next combat check gains a boon. The enemy is more vulnerable than expected.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Resource Discovery", "You find useful supplies or information.",
                        "Gain 1 Momentum. The resource can be used later in the mission.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("System Upgrade", "A system performs better than expected.",
                        "The next starship system check gains a boon.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Insider Information", "Someone leaks critical intel.",
                        "Gain a boon on the next Intelligence or Insight-based check.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Diplomatic Opening", "A path to negotiation appears.",
                        "The next Presence + Leadership check gains a boon.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Technological Advantage", "New tech gives you an edge.",
                        "The next Technical check gains a boon.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Morale Boost", "Your team's spirits are lifted.",
                        "Gain 1 Momentum. All crew members are more effective.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Shortcut", "A faster route or method is discovered.",
                        "Skip the next beat's task attempt, or gain a boon on it.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Enemy Retreat", "The opposition withdraws.",
                        "The immediate threat is reduced. Gain 1 Momentum.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Ancient Technology", "You discover advanced alien tech.",
                        "Gain a boon on the next Science check. The technology may have lasting value.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Natural Phenomenon", "A natural event aids your cause.",
                        "The environment shifts in your favor. Gain 1 Momentum.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Crew Expertise", "A crew member's hidden skill is revealed.",
                        "Gain a boon on the next task attempt in their area of expertise.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Favor Called In", "A past favor is repaid.",
                        "Gain 1 Momentum. A contact provides assistance.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Enemy Conflict", "Your enemies turn on each other.",
                        "The opposition is distracted. Gain a boon on the next check against them.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Clear Skies", "Conditions improve dramatically.",
                        "All threats on the current scene are reduced by 1 (minimum 0).",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Unexpected Success", "A previous action had a delayed positive effect.",
                        "Gain 1 Momentum. The earlier success pays off now.",
                        Category.OPPORTUNITY),
                new DevelopmentDefinition("Divine Intervention", "Something inexplicable helps you.",
                        "Automatically succeed on the next task attempt, or gain 2 Momentum.",
                        Category.OPPORTUNITY)
        ));
    }

    /**
     * Revelation developments — new information.
     */
    private static final List<DevelopmentDefinition> REVELATIONS = new ArrayList<>();

    static {
        REVELATIONS.addAll(Arrays.asList(
                new DevelopmentDefinition("Hidden Agenda", "Someone has been hiding their true motives.",
                        "An NPC's alignment changes. Reassess your strategy.",
                        Category.REVELATION),
                new DevelopmentDefinition("Ancient Secret", "A long-buried truth is uncovered.",
                        "The mission's context changes. Roll on the Complication table for the consequence.",
                        Category.REVELATION),
                new DevelopmentDefinition("Double Cross", "An ally was working for the enemy all along.",
                        "Trust is broken. Gain 1 Threat and lose the ally's assistance.",
                        Category.REVELATION),
                new DevelopmentDefinition("Bigger Picture", "The mission is part of a larger conspiracy.",
                        "The stakes are higher than expected. Note this for future missions.",
                        Category.REVELATION),
                new DevelopmentDefinition("Lost History", "You discover something about the past.",
                        "The revelation changes how you view the current situation. Gain a boon on the next Insight check.",
                        Category.REVELATION),
                new DevelopmentDefinition("Identity Revealed", "An NPC's true identity is exposed.",
                        "The NPC is someone important. This changes the diplomatic landscape.",
                        Category.REVELATION),
                new DevelopmentDefinition("Trap Discovered", "You realize you've been led into a trap.",
                        "Gain 1 Threat. A task attempt is required to escape the trap.",
                        Category.REVELATION),
                new DevelopmentDefinition("Signal Intercepted", "You overhear enemy communications.",
                        "Gain a boon on the next check against this enemy. You know their plans.",
                        Category.REVELATION),
                new DevelopmentDefinition("Artifact Decoded", "An ancient message is deciphered.",
                        "The message reveals a location or warning. Note this for future use.",
                        Category.REVELATION),
                new DevelopmentDefinition("Connection Found", "Two seemingly unrelated events are connected.",
                        "The mission's scope expands. A new objective emerges.",
                        Category.REVELATION),
                new DevelopmentDefinition("Prophecy", "An ancient prediction comes to light.",
                        "The prophecy may or may not come true. Note it for future missions.",
                        Category.REVELATION),
                new DevelopmentDefinition("Cover Blown", "Your true mission is exposed.",
                        "Gain 2 Threats. The element of surprise is lost.",
                        Category.REVELATION),
                new DevelopmentDefinition("Scientific Breakthrough", "A discovery changes everything.",
                        "Gain 1 Momentum. The discovery may have mission-ending implications.",
                        Category.REVELATION),
                new DevelopmentDefinition("Family Ties", "An NPC is related to someone you know.",
                        "This personal connection changes the dynamic. Reflect on your values.",
                        Category.REVELATION),
                new DevelopmentDefinition("Time Limit", "You discover a deadline you didn't know about.",
                        "The mission must be completed faster. All remaining checks suffer a threat.",
                        Category.REVELATION),
                new DevelopmentDefinition("Ghost Signal", "A mysterious transmission is detected.",
                        "The signal's origin is unknown. Roll on the Encounter table to determine the source.",
                        Category.REVELATION),
                new DevelopmentDefinition("Previous Mission", "Evidence of a previous mission is found.",
                        "Someone was here before. Their fate may inform your actions.",
                        Category.REVELATION),
                new DevelopmentDefinition("Political Intrigue", "The mission has political ramifications.",
                        "Higher powers are involved. Note this for career events.",
                        Category.REVELATION),
                new DevelopmentDefinition("Alien Origin", "The threat or discovery is of alien origin.",
                        "The scope of the mission expands beyond Federation space. Gain 1 Threat.",
                        Category.REVELATION),
                new DevelopmentDefinition("The Truth", "The real nature of the situation is revealed.",
                        "Everything you knew was wrong. The mission's objective changes.",
                        Category.REVELATION)
        ));
    }

    /**
     * Character developments — personal life affected.
     */
    private static final List<DevelopmentDefinition> CHARACTERS = new ArrayList<>();

    static {
        CHARACTERS.addAll(Arrays.asList(
                new DevelopmentDefinition("Personal Message", "A message from home arrives.",
                        "The message affects your mood. Gain a boon or threat on the next Presence check.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Old Friend", "Someone from your past appears.",
                        "The encounter stirs memories. Reflect on your values and background.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Moral Test", "Your values are directly challenged.",
                        "Make a choice that tests your core values. The consequence affects the mission.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Haunted", "A past failure resurfaces.",
                        "Gain 1 Threat. The memory affects your performance until addressed.",
                        Category.CHARACTER),
                new DevelopmentDefinition("New Resolve", "You find renewed determination.",
                        "Gain 1 Momentum. Your purpose is clarified.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Relationship Strain", "A relationship is tested.",
                        "A crew member or ally questions your decisions. A Presence + Leadership check may be needed.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Inheritance", "You receive something from your past.",
                        "An item, piece of information, or responsibility is passed to you.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Dream or Vision", "A vivid dream provides insight.",
                        "The dream may be prophetic. Gain a boon on the next Insight check.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Loyalty Tested", "Your loyalty is questioned.",
                        "Someone doubts your commitment. A Presence + Leadership check is required to restore trust.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Personal Loss", "You lose something important.",
                        "The loss affects you deeply. Gain 1 Threat and reflect on your values.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Recognition", "Your actions are recognized.",
                        "You receive praise or a commendation. Gain 1 Momentum.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Doubt", "You question your decisions.",
                        "Self-doubt creeps in. The next check suffers a threat until you regain confidence.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Mentor's Advice", "Wisdom from a mentor surfaces.",
                        "A remembered lesson applies to the current situation. Gain a boon on the next check.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Family Crisis", "Something happens to your family.",
                        "You must balance duty and personal life. Gain 1 Threat.",
                        Category.CHARACTER),
                new DevelopmentDefinition("New Purpose", "You discover a new goal.",
                        "This may affect your career path. Note this for career events.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Guilt", "A past action haunts you.",
                        "The guilt affects your performance. Gain 1 Threat until resolved.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Inspiration", "Something inspires you.",
                        "Gain 1 Momentum. Your creativity and determination are heightened.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Rival Appears", "A rival crosses your path.",
                        "The rivalry adds tension. The next check against the rival gains a boon.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Secret Kept", "You must keep a secret.",
                        "The burden of secrecy affects you. Gain 1 Threat.",
                        Category.CHARACTER),
                new DevelopmentDefinition("Character Growth", "You evolve as a person.",
                        "Consider adding a new value or retiring an old one. This is a moment of growth.",
                        Category.CHARACTER)
        ));
    }

    /**
     * Starship developments — ship-related events.
     */
    private static final List<DevelopmentDefinition> STARSHIPS = new ArrayList<>();

    static {
        STARSHIPS.addAll(Arrays.asList(
                new DevelopmentDefinition("System Malfunction", "A critical system fails.",
                        "A starship system takes 1 stress. A task attempt is required to repair.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Sensor Ghost", "Sensors detect something unexplained.",
                        "Roll on the Encounter table to determine what the sensors found.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Engine Surge", "Engines perform beyond expectations.",
                        "The next Engines check gains a boon. The ship can outmaneuver threats.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Hull Breach", "The ship's hull is compromised.",
                        "Structure takes 2 stress. Emergency repairs are needed.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Power Reroute", "Power is diverted to critical systems.",
                        "One system gains a boon; another suffers a threat. Choose which.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Computer Glitch", "The ship's computer malfunctions.",
                        "The next Computers check suffers a threat. Systems are unreliable.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Weapon Overcharge", "Weapons are charged beyond normal limits.",
                        "The next Weapons check gains a boon, but the system takes 1 stress.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Shield Fluctuation", "Shields are unstable.",
                        "The next Structure check suffers a threat. Shields may fail at a critical moment.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Navigation Error", "The ship is off course.",
                        "A navigation check is required. Time is lost correcting the course.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Life Support Issue", "Environmental controls fail.",
                        "Crew are affected. A task attempt is required to restore life support.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Warp Core Instability", "The warp core is fluctuating.",
                        "Gain 1 Threat. A Technical + Operations check is required to stabilize.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Cloak Activation", "The ship's cloaking device activates (if available).",
                        "The ship is temporarily invisible. The next stealth check gains a boon.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Transporter Accident", "A transporter malfunction occurs.",
                        "A crew member is affected. A Medicine check is required to assess the damage.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Phaser Array Online", "Additional weapons come online.",
                        "The next Weapons check gains a boon. The ship is more heavily armed.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Torpedo Launch", "A torpedo is fired.",
                        "Deal 2 hits to the target instead of 1. The torpedo may miss — roll a Weapons check.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Red Alert", "The ship goes to red alert.",
                        "All systems are at maximum readiness. Gain 1 Momentum.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Computer Override", "The ship's AI acts independently.",
                        "The computer makes a decision you didn't authorize. The consequence is unpredictable.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Docking Issue", "The ship has trouble docking or landing.",
                        "A task attempt is required to complete the docking safely.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Repair Bay Overload", "Too many systems need repair.",
                        "Choose one system to repair; the others wait. The un-repaired systems suffer a threat.",
                        Category.STARSHIP),
                new DevelopmentDefinition("Ship's Log", "An entry in the ship's log reveals something.",
                        "A previous captain's log entry provides insight. Gain a boon on the next check.",
                        Category.STARSHIP)
        ));
    }

    /**
     * Environment developments — setting changes.
     */
    private static final List<DevelopmentDefinition> ENVIRONMENTS = new ArrayList<>();

    static {
        ENVIRONMENTS.addAll(Arrays.asList(
                new DevelopmentDefinition("Weather Change", "The weather shifts dramatically.",
                        "All outdoor checks suffer a threat. The environment is now hostile.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Asteroid Field", "You enter an asteroid field.",
                        "Navigation becomes difficult. The next Engines check suffers a threat.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Nebula", "A nebula obscures sensors.",
                        "The next Sensors check suffers a threat. Visibility is reduced.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Gravity Well", "A gravitational anomaly is detected.",
                        "The ship is pulled off course. A navigation check is required.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Solar Flare", "A solar flare disrupts systems.",
                        "All starship systems take 1 stress. Communications are disrupted.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Wormhole", "A spatial anomaly appears.",
                        "The wormhole may lead anywhere. A Science check is required to analyze it.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Planetary Quake", "The ground shakes violently.",
                        "All checks on the planetary surface suffer a threat. Structural damage may occur.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Radiation Storm", "A radiation storm sweeps through.",
                        "Shielding is required. Characters without protection take 1 damage.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Magnetic Anomaly", "Magnetic fields interfere with electronics.",
                        "All Technical checks suffer a threat. Electronics are unreliable.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Time Dilation", "Time passes differently here.",
                        "Hours here may be days elsewhere. Note this for the mission timeline.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Atmospheric Shift", "The atmosphere changes composition.",
                        "Breathing apparatus may be needed. All checks suffer a threat until adapted.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Darkness", "Light sources fail or are blocked.",
                        "All checks in the dark suffer a threat. Vision is impaired.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Terrain Change", "The terrain shifts or becomes impassable.",
                        "The route changes. A navigation or exploration check is required.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Energy Drain", "Something is draining power.",
                        "Starship systems take 1 stress. The source must be identified.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Spatial Rift", "A tear in space-time appears.",
                        "The rift may contain something dangerous. A Science check is required to study it.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Electromagnetic Pulse", "An EMP disrupts all electronics.",
                        "All systems go offline. A task attempt is required to restore power.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Ice Age", "Temperatures plummet.",
                        "Cold-weather gear is needed. All checks suffer a threat until protected.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Volcanic Eruption", "A volcano erupts nearby.",
                        "The landscape changes. Evacuation may be necessary. Gain 1 Threat.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Tidal Wave", "A massive wave or flood occurs.",
                        "Evacuation is needed. A Fitness check is required to survive.",
                        Category.ENVIRONMENT),
                new DevelopmentDefinition("Silent Zone", "All communication is blocked.",
                        "No signals can be sent or received. The mission is isolated.",
                        Category.ENVIRONMENT)
        ));
    }

    /**
     * Development definition for easy creation.
     */
    public static class DevelopmentDefinition {
        private final String title;
        private final String narrative;
        private final String mechanical;
        private final Category category;

        public DevelopmentDefinition(String title, String narrative, String mechanical, Category category) {
            this.title = title;
            this.narrative = narrative;
            this.mechanical = mechanical;
            this.category = category;
        }

        public String getTitle() { return title; }
        public String getNarrative() { return narrative; }
        public String getMechanical() { return mechanical; }
        public Category getCategory() { return category; }
    }

    // ==================== Development Generation ====================

    /**
     * Rolls a random development from all categories.
     */
    public DevelopmentDefinition rollDevelopment() {
        List<DevelopmentDefinition> all = new ArrayList<>();
        all.addAll(COMPLICATIONS);
        all.addAll(OPPORTUNITIES);
        all.addAll(REVELATIONS);
        all.addAll(CHARACTERS);
        all.addAll(STARSHIPS);
        all.addAll(ENVIRONMENTS);

        int index = random.nextInt(all.size());
        log.debug("Rolled development: {}", all.get(index).getTitle());
        return all.get(index);
    }

    /**
     * Rolls a development from a specific category.
     */
    public DevelopmentDefinition rollDevelopment(Category category) {
        List<DevelopmentDefinition> developments = getDevelopmentsByCategory(category);
        if (developments.isEmpty()) {
            return rollDevelopment();
        }
        int index = random.nextInt(developments.size());
        log.debug("Rolled {} development: {}", category, developments.get(index).getTitle());
        return developments.get(index);
    }

    /**
     * Gets all developments for a specific category.
     */
    public List<DevelopmentDefinition> getDevelopmentsByCategory(Category category) {
        switch (category) {
            case COMPLICATION: return new ArrayList<>(COMPLICATIONS);
            case OPPORTUNITY: return new ArrayList<>(OPPORTUNITIES);
            case REVELATION: return new ArrayList<>(REVELATIONS);
            case CHARACTER: return new ArrayList<>(CHARACTERS);
            case STARSHIP: return new ArrayList<>(STARSHIPS);
            case ENVIRONMENT: return new ArrayList<>(ENVIRONMENTS);
            default: return Collections.emptyList();
        }
    }

    /**
     * Suggests a development based on the mission type.
     * <p>
     * Different mission types tend to generate different kinds of developments.
     * </p>
     */
    public DevelopmentDefinition rollDevelopmentForMission(CaptainLogMission.MissionType missionType) {
        // Map mission types to likely development categories
        Category category = switch (missionType) {
            case CRISIS, COMBAT -> Category.COMPLICATION;
            case EXPLORATION, SCIENCE -> Category.REVELATION;
            case DIPLOMACY -> Category.CHARACTER;
            case TRADE, INVESTIGATION -> Category.OPPORTUNITY;
            default -> {
                // Random category for other mission types
                Category[] categories = Category.values();
                yield categories[random.nextInt(categories.length)];
            }
        };
        return rollDevelopment(category);
    }

    /**
     * Gets all available developments across all categories.
     */
    public Map<Category, List<DevelopmentDefinition>> getAllDevelopments() {
        Map<Category, List<DevelopmentDefinition>> all = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            all.put(category, getDevelopmentsByCategory(category));
        }
        return all;
    }

    /**
     * Lists all development categories.
     */
    public List<Map<String, String>> listCategories() {
        List<Map<String, String>> categories = new ArrayList<>();
        for (Category category : Category.values()) {
            Map<String, String> cat = new LinkedHashMap<>();
            cat.put("name", category.name());
            cat.put("description", category.getDescription());
            cat.put("count", String.valueOf(getDevelopmentsByCategory(category).size()));
            categories.add(cat);
        }
        return categories;
    }
}
