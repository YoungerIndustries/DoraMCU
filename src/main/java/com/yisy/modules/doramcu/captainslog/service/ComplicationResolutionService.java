package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides context-aware complication and advantage resolution for the Captain's Log Solo RPG.
 * <p>
 * Instead of a single generic complication table, this service organizes complications
 * and advantages by category so the system can suggest narrative outcomes that match
 * the current mission context. Categories include:
 * <ul>
 *   <li>COMBAT — complications during physical or starship combat</li>
 *   <li>EXPLORATION — complications during planetary exploration or discovery</li>
 *   <li>DIPLOMACY — complications during negotiations or political situations</li>
 *   <li>SCIENCE — complications during research or technical tasks</li>
 *   <li>PERSONAL — complications affecting the character personally</li>
 *   <li>CRISIS — complications during emergency or crisis situations</li>
 *   <li>GENERAL — general-purpose complications for any situation</li>
 * </ul>
 * </p>
 * <p>
 * The service also provides advantage resolution for when Momentum is spent
 * or excess successes generate favorable outcomes.
 * </p>
 * <p>
 * When no category is specified, the service infers the category from the
 * mission type: CRISIS → CRISIS, COMBAT → COMBAT, EXPLORATION → EXPLORATION,
 * DIPLOMACY → DIPLOMACY, SCIENCE → SCIENCE, and all others → GENERAL.
 * </p>
 */
@Component
public class ComplicationResolutionService {

    private static final Logger log = LoggerFactory.getLogger(ComplicationResolutionService.class);
    private final Random random = new Random();

    // ==================== Complication Categories ====================

    /**
     * Categories of complications and advantages.
     */
    public enum Category {
        COMBAT("Combat — physical or starship conflict."),
        EXPLORATION("Exploration — planetary surface or space discovery."),
        DIPLOMACY("Diplomacy — negotiation, politics, and social interaction."),
        SCIENCE("Science — research, analysis, and technical work."),
        PERSONAL("Personal — affecting the character's life and relationships."),
        CRISIS("Crisis — emergency situations with high stakes."),
        GENERAL("General — applicable to any situation.");

        private final String description;

        Category(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== Complication Tables by Category ====================

    /**
     * Combat complications — things that go wrong during conflict.
     */
    private static final Map<String, String> COMBAT_COMPLICATIONS = new LinkedHashMap<>();
    static {
        COMBAT_COMPLICATIONS.put("Friendly Fire", "Your attack strikes an ally or civilian instead of the intended target.");
        COMBAT_COMPLICATIONS.put("Weapon Jam", "Your primary weapon malfunctions at a critical moment.");
        COMBAT_COMPLICATIONS.put("Reinforcements", "The enemy receives unexpected backup, shifting the balance of power.");
        COMBAT_COMPLICATIONS.put("Ambush", "You walk into a trap — the enemy was waiting for you.");
        COMBAT_COMPLICATIONS.put("Collateral Damage", "The battle damages critical infrastructure or endangers innocents.");
        COMBAT_COMPLICATIONS.put("Shield Failure", "Defensive systems drop, leaving you exposed to enemy fire.");
        COMBAT_COMPLICATIONS.put("Communication Breakdown", "You lose contact with your team during the fight.");
        COMBAT_COMPLICATIONS.put("Hostage Taken", "An innocent is captured and used as leverage.");
        COMBAT_COMPLICATIONS.put("Environmental Hazard", "The battlefield environment turns against you (fire, explosion, etc.).");
        COMBAT_COMPLICATIONS.put("Enemy Retreat", "The enemy withdraws, but leaves behind a dangerous situation (bomb, trap, etc.).");
        COMBAT_COMPLICATIONS.put("Moral Cost", "You achieve victory, but at a cost that haunts you.");
        COMBAT_COMPLICATIONS.put("Unexpected Weapon", "The enemy reveals a weapon or tactic you didn't anticipate.");
        COMBAT_COMPLICATIONS.put("Terrain Collapse", "The ground or structure beneath you gives way during combat.");
        COMBAT_COMPLICATIONS.put("Interference", "A third party intervenes, complicating the conflict.");
        COMBAT_COMPLICATIONS.put("Injury", "You or an ally takes a serious wound that will require attention.");
        COMBAT_COMPLICATIONS.put("Equipment Loss", "Vital gear is destroyed or lost in the heat of battle.");
        COMBAT_COMPLICATIONS.put("Time Pressure", "You must end the fight quickly before something worse happens.");
        COMBAT_COMPLICATIONS.put("Betrayal", "Someone on your side reveals they were working for the enemy.");
        COMBAT_COMPLICATIONS.put("Civilian Casualties", "Innocent lives are lost, and the blame falls on you.");
        COMBAT_COMPLICATIONS.put("Aftermath", "The battle is won, but the consequences are far worse than the conflict itself.");
    }

    /**
     * Exploration complications — things that go wrong during discovery.
     */
    private static final Map<String, String> EXPLORATION_COMPLICATIONS = new LinkedHashMap<>();
    static {
        EXPLORATION_COMPLICATIONS.put("Hostile Wildlife", "The planet's native creatures prove dangerous and territorial.");
        EXPLORATION_COMPLICATIONS.put("Environmental Shift", "Weather or terrain conditions change rapidly, trapping your team.");
        EXPLORATION_COMPLICATIONS.put("Lost Signal", "Communication with the ship is lost; you're on your own.");
        EXPLORATION_COMPLICATIONS.put("Ancient Trap", "The ruins or structure contains defenses that activate as you explore.");
        EXPLORATION_COMPLICATIONS.put("Contamination", "Something in the environment is harmful — spores, radiation, or toxin.");
        EXPLORATION_COMPLICATIONS.put("Rival Explorers", "Another group is already here, and they don't want company.");
        EXPLORATION_COMPLICATIONS.put("Structural Collapse", "The cave, ruin, or structure begins to collapse around you.");
        EXPLORATION_COMPLICATIONS.put("Time Dilation", "Time passes differently here — hours here could be days elsewhere.");
        EXPLORATION_COMPLICATIONS.put("Psychological Effect", "The environment affects your mind — hallucinations, fear, or confusion.");
        EXPLORATION_COMPLICATIONS.put("Discovery Gone Wrong", "What you find is far more dangerous than you expected.");
        EXPLORATION_COMPLICATIONS.put("Equipment Failure", "Critical exploration gear fails in the field.");
        EXPLORATION_COMPLICATIONS.put("Hidden Inhabitants", "The planet is inhabited, and the natives are not welcoming.");
        EXPLORATION_COMPLICATIONS.put("Anomaly Activation", "A spatial or temporal anomaly activates, altering the landscape.");
        EXPLORATION_COMPLICATIONS.put("Resource Shortage", "Supplies run low faster than expected; you must adapt.");
        EXPLORATION_COMPLICATIONS.put("Navigation Error", "Your coordinates were wrong — you're farther from the ship than planned.");
        EXPLORATION_COMPLICATIONS.put("Gravitational Shift", "Gravity fluctuates, making movement and balance difficult.");
        EXPLORATION_COMPLICATIONS.put("Artifact Curse", "An ancient artifact affects those who touch it in unexpected ways.");
        EXPLORATION_COMPLICATIONS.put("Ecosystem Collapse", "The local ecosystem is dying, and your presence may accelerate it.");
        EXPLORATION_COMPLICATIONS.put("First Contact Gone Wrong", "Initial contact with a new species leads to misunderstanding or conflict.");
        EXPLORATION_COMPLICATIONS.put("The Unknown", "Something you cannot explain or categorize appears before you.");
    }

    /**
     * Diplomacy complications — things that go wrong during negotiation.
     */
    private static final Map<String, String> DIPLOMACY_COMPLICATIONS = new LinkedHashMap<>();
    static {
        DIPLOMACY_COMPLICATIONS.put("Cultural Misunderstanding", "A gesture or phrase is deeply offensive to the other party.");
        DIPLOMACY_COMPLICATIONS.put("Hidden Agenda", "One side is negotiating in bad faith, hiding their true intentions.");
        DIPLOMACY_COMPLICATIONS.put("Third Party Interference", "An outside force disrupts the negotiations.");
        DIPLOMACY_COMPLICATIONS.put("Assassination Attempt", "Someone tries to kill a key negotiator during the talks.");
        DIPLOMACY_COMPLICATIONS.put("Public Outcry", "The terms of negotiation spark outrage among the populace.");
        DIPLOMACY_COMPLICATIONS.put("Betrayal", "A trusted ally reveals they were working against you all along.");
        DIPLOMACY_COMPLICATIONS.put("Deadline Pressure", "Time runs out before an agreement can be reached.");
        DIPLOMACY_COMPLICATIONS.put("Sabotage", "Someone sabotages the negotiation venue or process.");
        DIPLOMACY_COMPLICATIONS.put("Revealed Secret", "A damaging secret about one party is exposed during negotiations.");
        DIPLOMACY_COMPLICATIONS.put("Power Struggle", "Internal politics weaken the negotiating position of one side.");
        DIPLOMACY_COMPLICATIONS.put("Miscommunication", "A critical detail is lost in translation or misunderstood.");
        DIPLOMACY_COMPLICATIONS.put("Hostile Audience", "The public or press present at the negotiation are openly hostile.");
        DIPLOMACY_COMPLICATIONS.put("Unforeseen Consequence", "The agreement has unintended consequences that worsen the situation.");
        DIPLOMACY_COMPLICATIONS.put("Personal Attack", "The negotiation becomes personal, damaging trust and rapport.");
        DIPLOMACY_COMPLICATIONS.put("Espionage", "You discover that one side is spying on the other during talks.");
        DIPLOMACY_COMPLICATIONS.put("Blackmail", "One party attempts to blackmail the other to gain leverage.");
        DIPLOMACY_COMPLICATIONS.put("Broken Promise", "A previous agreement is revealed to have been violated.");
        DIPLOMACY_COMPLICATIONS.put("Ideological Clash", "Fundamental beliefs make compromise impossible without sacrifice.");
        DIPLOMACY_COMPLICATIONS.put("Unexpected Ally", "An unlikely party enters the negotiation, changing the dynamics.");
        DIPLOMACY_COMPLICATIONS.put("The Bigger Picture", "The negotiation is revealed to be part of a much larger conflict.");
    }

    /**
     * Science complications — things that go wrong during research.
     */
    private static final Map<String, String> SCIENCE_COMPLICATIONS = new LinkedHashMap<>();
    static {
        SCIENCE_COMPLICATIONS.put("Experimental Failure", "The experiment produces unexpected and dangerous results.");
        SCIENCE_COMPLICATIONS.put("Data Corruption", "Critical research data is lost or corrupted.");
        SCIENCE_COMPLICATIONS.put("Ethical Dilemma", "The research raises serious ethical questions that must be addressed.");
        SCIENCE_COMPLICATIONS.put("Equipment Malfunction", "A key piece of scientific equipment fails at a critical moment.");
        SCIENCE_COMPLICATIONS.put("Contamination", "The sample or experiment is contaminated, invalidating results.");
        SCIENCE_COMPLICATIONS.put("Rival Research", "A competing group has already made the discovery and is keeping it secret.");
        SCIENCE_COMPLICATIONS.put("Unforeseen Reaction", "The experiment triggers a chain reaction with unpredictable effects.");
        SCIENCE_COMPLICATIONS.put("Funding Cut", "Resources for the research are suddenly withdrawn.");
        SCIENCE_COMPLICATIONS.put("Theoretical Error", "The underlying theory is flawed; the research must start over.");
        SCIENCE_COMPLICATIONS.put("Biological Hazard", "A pathogen or organism escapes containment.");
        SCIENCE_COMPLICATIONS.put("AI Malfunction", "An AI or computer system used in research behaves unpredictably.");
        SCIENCE_COMPLICATIONS.put("Discovery Suppressed", "Someone with authority wants the discovery hidden or destroyed.");
        SCIENCE_COMPLICATIONS.put("Paradox", "The research reveals a paradox that challenges understanding of reality.");
        SCIENCE_COMPLICATIONS.put("Resource Conflict", "The research requires resources that are needed elsewhere.");
        SCIENCE_COMPLICATIONS.put("Peer Review Failure", "The scientific community rejects or criticizes the findings.");
        SCIENCE_COMPLICATIONS.put("Temporal Effect", "The experiment affects time in unexpected ways.");
        SCIENCE_COMPLICATIONS.put("Dimensional Breach", "The research opens a connection to another dimension or reality.");
        SCIENCE_COMPLICATIONS.put("Mental Toll", "The research takes a psychological toll on the scientist.");
        SCIENCE_COMPLICATIONS.put("Weaponization", "The discovery could be weaponized, and someone is trying to do so.");
        SCIENCE_COMPLICATIONS.put("The Unknown Variable", "Something in the data doesn't fit any known model or theory.");
    }

    /**
     * Personal complications — things that affect the character personally.
     */
    private static final Map<String, String> PERSONAL_COMPLICATIONS = new LinkedHashMap<>();
    static {
        PERSONAL_COMPLICATIONS.put("Past Resurfaces", "An old enemy or unresolved issue from your past catches up with you.");
        PERSONAL_COMPLICATIONS.put("Relationship Strain", "A personal relationship is tested by the current situation.");
        PERSONAL_COMPLICATIONS.put("Health Issue", "A physical or mental health problem flares up at a bad time.");
        PERSONAL_COMPLICATIONS.put("Moral Conflict", "Your values are challenged by a difficult choice.");
        PERSONAL_COMPLICATIONS.put("Secret Revealed", "A personal secret is exposed, affecting your reputation or relationships.");
        PERSONAL_COMPLICATIONS.put("Family Crisis", "A family member needs your help, but duty calls elsewhere.");
        PERSONAL_COMPLICATIONS.put("Identity Crisis", "You question who you are and what you stand for.");
        PERSONAL_COMPLICATIONS.put("Trust Broken", "Someone you trusted betrays you.");
        PERSONAL_COMPLICATIONS.put("Guilt", "A past decision haunts you, and the consequences are now apparent.");
        PERSONAL_COMPLICATIONS.put("Temptation", "An offer or opportunity tempts you to compromise your principles.");
        PERSONAL_COMPLICATIONS.put("Isolation", "You feel alone and unsupported in the face of the challenge.");
        PERSONAL_COMPLICATIONS.put("Fear", "A deep-seated fear is triggered by the current situation.");
        PERSONAL_COMPLICATIONS.put("Loss", "You lose something or someone important.");
        PERSONAL_COMPLICATIONS.put("Doubt", "You begin to doubt your abilities or decisions.");
        PERSONAL_COMPLICATIONS.put("Rivalry", "A personal rival appears, complicating your mission.");
        PERSONAL_COMPLICATIONS.put("Loyalty Conflict", "You must choose between loyalty to different people or causes.");
        PERSONAL_COMPLICATIONS.put("Burnout", "The stress of command takes its toll; you're running on empty.");
        PERSONAL_COMPLICATIONS.put("Unexpected Connection", "You form an unexpected bond that complicates your objectivity.");
        PERSONAL_COMPLICATIONS.put("Inheritance", "A legacy or responsibility from your past falls upon you.");
        PERSONAL_COMPLICATIONS.put("The Price of Command", "The weight of leadership forces you to make a sacrifice.");
    }

    /**
     * Crisis complications — things that go wrong during emergencies.
     */
    private static final Map<String, String> CRISIS_COMPLICATIONS = new LinkedHashMap<>();
    static {
        CRISIS_COMPLICATIONS.put("Catastrophic Failure", "A critical system fails completely, escalating the emergency.");
        CRISIS_COMPLICATIONS.put("Chain Reaction", "One failure triggers another, creating a cascade of problems.");
        CRISIS_COMPLICATIONS.put("Evacuation Needed", "The situation requires immediate evacuation, but escape routes are limited.");
        CRISIS_COMPLICATIONS.put("Command Breakdown", "The chain of command is disrupted; decisions must be made without guidance.");
        CRISIS_COMPLICATIONS.put("Resource Depletion", "Critical supplies (power, air, water) are running out fast.");
        CRISIS_COMPLICATIONS.put("Panic", "Fear and panic spread among the crew or population, making coordination difficult.");
        CRISIS_COMPLICATIONS.put("Secondary Threat", "While dealing with the crisis, a new threat emerges.");
        CRISIS_COMPLICATIONS.put("Communication Failure", "You cannot reach key personnel or external support.");
        CRISIS_COMPLICATIONS.put("Structural Damage", "The ship or facility sustains critical structural damage.");
        CRISIS_COMPLICATIONS.put("Time Running Out", "The window to resolve the crisis is closing rapidly.");
        CRISIS_COMPLICATIONS.put("Casualties", "People are being hurt or killed; the death toll rises.");
        CRISIS_COMPLICATIONS.put("Containment Breach", "Something dangerous escapes its containment (pathogen, energy, entity).");
        CRISIS_COMPLICATIONS.put("System Overload", "Systems are pushed beyond their limits and begin to fail.");
        CRISIS_COMPLICATIONS.put("External Interference", "An outside force exploits the crisis for their own purposes.");
        CRISIS_COMPLICATIONS.put("Moral Sacrifice", "Saving the many requires sacrificing the few.");
        CRISIS_COMPLICATIONS.put("False Alarm", "The crisis was a false alarm, but the response caused real damage.");
        CRISIS_COMPLICATIONS.put("Sabotage", "The crisis was caused by deliberate sabotage from within.");
        CRISIS_COMPLICATIONS.put("Natural Disaster", "A natural phenomenon (solar flare, asteroid, etc.) compounds the emergency.");
        CRISIS_COMPLICATIONS.put("Leadership Challenge", "Your authority is questioned during the crisis.");
        CRISIS_COMPLICATIONS.put("The Unthinkable", "The crisis reveals a threat that was previously considered impossible.");
    }

    /**
     * General complications — applicable to any situation.
     */
    private static final Map<String, String> GENERAL_COMPLICATIONS = new LinkedHashMap<>();
    static {
        GENERAL_COMPLICATIONS.put("Unexpected Delay", "Something causes an unexpected delay, putting you behind schedule.");
        GENERAL_COMPLICATIONS.put("Misinformation", "The information you relied on was wrong or deliberately misleading.");
        GENERAL_COMPLICATIONS.put("Resource Shortage", "You lack the resources needed to complete the task as planned.");
        GENERAL_COMPLICATIONS.put("Interference", "An outside force interferes with your plans.");
        GENERAL_COMPLICATIONS.put("Equipment Failure", "A piece of equipment fails at a critical moment.");
        GENERAL_COMPLICATIONS.put("Weather Event", "A sudden weather event disrupts your operations.");
        GENERAL_COMPLICATIONS.put("Political Complication", "Political forces complicate what should have been a straightforward task.");
        GENERAL_COMPLICATIONS.put("Communication Error", "A message is misinterpreted, leading to confusion or conflict.");
        GENERAL_COMPLICATIONS.put("Unexpected Arrival", "Someone arrives unannounced, changing the situation.");
        GENERAL_COMPLICATIONS.put("Hidden Danger", "A danger that was not apparent reveals itself.");
        GENERAL_COMPLICATIONS.put("Bureaucratic Hurdle", "Red tape or regulations slow you down.");
        GENERAL_COMPLICATIONS.put("Competing Priority", "Another urgent matter demands your attention.");
        GENERAL_COMPLICATIONS.put("Technical Glitch", "A minor technical issue snowballs into a major problem.");
        GENERAL_COMPLICATIONS.put("Personnel Issue", "A crew member's personal problem affects the mission.");
        GENERAL_COMPLICATIONS.put("Environmental Change", "The environment shifts in an unexpected way.");
        GENERAL_COMPLICATIONS.put("Information Leak", "Sensitive information is leaked to the wrong parties.");
        GENERAL_COMPLICATIONS.put("Timing Issue", "Everything happens at once, and you can't address it all.");
        GENERAL_COMPLICATIONS.put("Unforeseen Consequence", "A previous action has unexpected repercussions.");
        GENERAL_COMPLICATIONS.put("Lost Opportunity", "A chance to succeed slips away due to circumstances beyond your control.");
        GENERAL_COMPLICATIONS.put("The Twist", "The situation is not what it seems — everything changes.");
    }

    // ==================== Advantage Tables by Category ====================

    private static final Map<String, String> COMBAT_ADVANTAGES = new LinkedHashMap<>();
    static {
        COMBAT_ADVANTAGES.put("Element of Surprise", "You catch the enemy off guard, gaining the upper hand.");
        COMBAT_ADVANTAGES.put("Reinforcements Arrive", "Friendly forces arrive just in time to turn the tide.");
        COMBAT_ADVANTAGES.put("Enemy Weakness", "You discover a critical weakness in the enemy's defenses.");
        COMBAT_ADVANTAGES.put("Weapon Upgrade", "Your weapon performs better than expected, dealing more damage.");
        COMBAT_ADVANTAGES.put("Tactical Advantage", "The terrain or positioning favors your side.");
        COMBAT_ADVANTAGES.put("Morale Boost", "Your crew's fighting spirit soars; they fight with renewed determination.");
        COMBAT_ADVANTAGES.put("Enemy Retreat", "The enemy breaks and flees, ending the conflict in your favor.");
        COMBAT_ADVANTAGES.put("Shield Boost", "Defensive systems hold stronger than expected, absorbing more damage.");
        COMBAT_ADVANTAGES.put("Precision Strike", "Your attack hits a critical point, disabling the enemy effectively.");
        COMBAT_ADVANTAGES.put("Allied Support", "An ally provides crucial support at the right moment.");
        COMBAT_ADVANTAGES.put("Intelligence", "You learn the enemy's plans and can counter them.");
        COMBAT_ADVANTAGES.put("Environmental Edge", "The environment works in your favor during the fight.");
        COMBAT_ADVANTAGES.put("Quick Recovery", "You recover from damage faster than expected.");
        COMBAT_ADVANTAGES.put("Enemy Confusion", "The enemy is confused or disorganized, making them easier to defeat.");
        COMBAT_ADVANTAGES.put("Command Presence", "Your leadership inspires excellence in your crew.");
        COMBAT_ADVANTAGES.put("Lucky Break", "Fortune smiles upon you; a shot misses by inches or hits a weak point.");
        COMBAT_ADVANTAGES.put("Surrender", "The enemy surrenders, avoiding further bloodshed.");
        COMBAT_ADVANTAGES.put("Capture", "You capture a valuable prisoner or piece of intelligence.");
        COMBAT_ADVANTAGES.put("Minimal Damage", "The battle ends with minimal damage to your side.");
        COMBAT_ADVANTAGES.put("Victory Cry", "Your victory sends a message that boosts morale across the fleet.");
    }

    private static final Map<String, String> EXPLORATION_ADVANTAGES = new LinkedHashMap<>();
    static {
        EXPLORATION_ADVANTAGES.put("Rare Discovery", "You find something rare and valuable that advances knowledge.");
        EXPLORATION_ADVANTAGES.put("Safe Passage", "The terrain proves easier to navigate than expected.");
        EXPLORATION_ADVANTAGES.put("Friendly Inhabitants", "The local inhabitants are welcoming and helpful.");
        EXPLORATION_ADVANTAGES.put("Ancient Knowledge", "You uncover ancient records or technology that provides insight.");
        EXPLORATION_ADVANTAGES.put("Resource Bonanza", "The area is rich in resources that can be harvested.");
        EXPLORATION_ADVANTAGES.put("Clear Signal", "Communication with the ship is crystal clear throughout the mission.");
        EXPLORATION_ADVANTAGES.put("Shortcut", "You discover a faster route to your destination.");
        EXPLORATION_ADVANTAGES.put("Natural Phenomenon", "A beautiful or scientifically significant phenomenon occurs.");
        EXPLORATION_ADVANTAGES.put("Artifact Found", "You recover an artifact of great historical or scientific value.");
        EXPLORATION_ADVANTAGES.put("Ecosystem Insight", "You gain a deeper understanding of the local ecosystem.");
        EXPLORATION_ADVANTAGES.put("Weather Break", "A break in bad weather allows you to proceed safely.");
        EXPLORATION_ADVANTAGES.put("Local Guide", "A local offers to guide you through the area.");
        EXPLORATION_ADVANTAGES.put("Mapping Complete", "You complete a detailed map of the area, aiding future missions.");
        EXPLORATION_ADVANTAGES.put("First Contact Success", "Contact with a new species goes smoothly and opens new possibilities.");
        EXPLORATION_ADVANTAGES.put("Scientific Breakthrough", "Your exploration leads to a major scientific discovery.");
        EXPLORATION_ADVANTAGES.put("Hidden Shelter", "You find a safe place to rest and regroup.");
        EXPLORATION_ADVANTAGES.put("Unexpected Ally", "A stranger offers help and shares valuable local knowledge.");
        EXPLORATION_ADVANTAGES.put("Clear Path", "Obstacles that should have blocked your path are absent or easily bypassed.");
        EXPLORATION_ADVANTAGES.put("Preserved Ruins", "Ancient ruins are well-preserved, offering rich information.");
        EXPLORATION_ADVANTAGES.put("The View", "The landscape is breathtaking, boosting morale and providing inspiration.");
    }

    private static final Map<String, String> DIPLOMACY_ADVANTAGES = new LinkedHashMap<>();
    static {
        DIPLOMACY_ADVANTAGES.put("Common Ground", "You find shared interests that build trust and rapport.");
        DIPLOMACY_ADVANTAGES.put("Mediator", "A neutral third party helps facilitate the negotiation.");
        DIPLOMACY_ADVANTAGES.put("Cultural Insight", "Understanding of the other party's culture gives you an edge.");
        DIPLOMACY_ADVANTAGES.put("Personal Connection", "A personal bond forms between you and the other negotiator.");
        DIPLOMACY_ADVANTAGES.put("Favorable Terms", "The other party offers terms more favorable than you expected.");
        DIPLOMACY_ADVANTAGES.put("Public Support", "The public or press supports your position, strengthening your hand.");
        DIPLOMACY_ADVANTAGES.put("Revealed Truth", "A hidden truth comes to light that favors your position.");
        DIPLOMACY_ADVANTAGES.put("Allied Support", "An ally publicly supports your position, adding weight to your argument.");
        DIPLOMACY_ADVANTAGES.put("Diplomatic Immunity", "You are granted special status that protects you during negotiations.");
        DIPLOMACY_ADVANTAGES.put("Breakthrough", "A creative solution resolves a seemingly intractable issue.");
        DIPLOMACY_ADVANTAGES.put("Trust Built", "The other party reveals their true intentions, and they are peaceful.");
        DIPLOMACY_ADVANTAGES.put("Gift Exchange", "A meaningful gift strengthens the bond between parties.");
        DIPLOMACY_ADVANTAGES.put("Shared Enemy", "Both parties face a common threat, encouraging cooperation.");
        DIPLOMACY_ADVANTAGES.put("Historical Precedent", "A past agreement or event provides a framework for resolution.");
        DIPLOMACY_ADVANTAGES.put("Charismatic Appeal", "Your words resonate deeply with the other party.");
        DIPLOMACY_ADVANTAGES.put("Compromise Found", "Both sides find a middle ground that satisfies everyone.");
        DIPLOMACY_ADVANTAGES.put("New Alliance", "The negotiation leads to a formal alliance or partnership.");
        DIPLOMACY_ADVANTAGES.put("Information Shared", "The other party shares valuable intelligence willingly.");
        DIPLOMACY_ADVANTAGES.put("Peace Treaty", "A lasting peace agreement is reached, ending a long-standing conflict.");
        DIPLOMACY_ADVANTAGES.put("Respect Earned", "The other party gains deep respect for you and your people.");
    }

    private static final Map<String, String> SCIENCE_ADVANTAGES = new LinkedHashMap<>();
    static {
        SCIENCE_ADVANTAGES.put("Breakthrough", "The experiment yields results far beyond expectations.");
        SCIENCE_ADVANTAGES.put("Peer Recognition", "Your findings are immediately recognized as significant by the scientific community.");
        SCIENCE_ADVANTAGES.put("Funding Secured", "Additional resources are allocated to support the research.");
        SCIENCE_ADVANTAGES.put("Collaboration", "A fellow scientist offers to collaborate, accelerating progress.");
        SCIENCE_ADVANTAGES.put("Clean Data", "The data is unusually clean and easy to analyze.");
        SCIENCE_ADVANTAGES.put("Equipment Upgrade", "New or improved equipment becomes available for the research.");
        SCIENCE_ADVANTAGES.put("Pattern Recognition", "You spot a pattern in the data that others missed.");
        SCIENCE_ADVANTAGES.put("Publication", "The findings are published, bringing recognition and credibility.");
        SCIENCE_ADVANTAGES.put("Practical Application", "The research has an immediate and useful application.");
        SCIENCE_ADVANTAGES.put("Mentorship", "A senior scientist offers guidance that accelerates your work.");
        SCIENCE_ADVANTAGES.put("Theoretical Confirmation", "A long-held theory is confirmed by your research.");
        SCIENCE_ADVANTAGES.put("Resource Discovery", "The research reveals a new resource or energy source.");
        SCIENCE_ADVANTAGES.put("AI Assistance", "An AI or computer system provides unexpected insights.");
        SCIENCE_ADVANTAGES.put("Cross-Disciplinary Insight", "Knowledge from another field provides a key insight.");
        SCIENCE_ADVANTAGES.put("Replication Success", "Another lab successfully replicates your results, confirming their validity.");
        SCIENCE_ADVANTAGES.put("Ethical Clarity", "The ethical questions are resolved in a way that allows the research to proceed.");
        SCIENCE_ADVANTAGES.put("Unexpected Connection", "The research connects to another area of study in a meaningful way.");
        SCIENCE_ADVANTAGES.put("Grant Award", "A prestigious grant or award recognizes your work.");
        SCIENCE_ADVANTAGES.put("Technology Transfer", "The research leads to a new technology that benefits society.");
        SCIENCE_ADVANTAGES.put("The Eureka Moment", "A sudden insight solves a problem that has been plaguing you for weeks.");
    }

    private static final Map<String, String> PERSONAL_ADVANTAGES = new LinkedHashMap<>();
    static {
        PERSONAL_ADVANTAGES.put("Inner Strength", "You draw upon a reserve of strength you didn't know you had.");
        PERSONAL_ADVANTAGES.put("Supportive Friend", "A friend offers words of encouragement that restore your confidence.");
        PERSONAL_ADVANTAGES.put("Clarity", "A moment of clarity helps you see the path forward.");
        PERSONAL_ADVANTAGES.put("Reconciliation", "A strained relationship is healed, restoring trust and support.");
        PERSONAL_ADVANTAGES.put("Personal Growth", "The challenge helps you grow as a person and leader.");
        PERSONAL_ADVANTAGES.put("Good News", "Positive news from home lifts your spirits.");
        PERSONAL_ADVANTAGES.put("Mentor's Wisdom", "Advice from a mentor proves invaluable at a critical moment.");
        PERSONAL_ADVANTAGES.put("Self-Discovery", "You learn something new about yourself that strengthens your resolve.");
        PERSONAL_ADVANTAGES.put("Healing", "A physical or emotional wound begins to heal faster than expected.");
        PERSONAL_ADVANTAGES.put("Inspiration", "An unexpected source of inspiration fuels your creativity and determination.");
        PERSONAL_ADVANTAGES.put("Trust Restored", "Someone proves their loyalty, restoring your faith in them.");
        PERSONAL_ADVANTAGES.put("New Perspective", "A new perspective helps you see the situation in a more positive light.");
        PERSONAL_ADVANTAGES.put("Family Support", "Your family offers unwavering support from afar.");
        PERSONAL_ADVANTAGES.put("Overcoming Fear", "You confront and overcome a deep-seated fear.");
        PERSONAL_ADVANTAGES.put("Forgiveness", "You forgive yourself or others, releasing a burden that weighed you down.");
        PERSONAL_ADVANTAGES.put("Confidence Boost", "A small success builds confidence that carries over to larger challenges.");
        PERSONAL_ADVANTAGES.put("Meaningful Connection", "You form a deep bond with someone that enriches your life.");
        PERSONAL_ADVANTAGES.put("Purpose Renewed", "Your sense of purpose is renewed and strengthened.");
        PERSONAL_ADVANTAGES.put("Legacy", "You realize the positive impact you've had on others' lives.");
        PERSONAL_ADVANTAGES.put("Peace of Mind", "A period of calm and reflection restores your mental clarity.");
    }

    private static final Map<String, String> CRISIS_ADVANTAGES = new LinkedHashMap<>();
    static {
        CRISIS_ADVANTAGES.put("Quick Thinking", "A clever solution resolves the crisis faster than expected.");
        CRISIS_ADVANTAGES.put("Crew Heroism", "A crew member's bravery turns the tide of the emergency.");
        CRISIS_ADVANTAGES.put("System Recovery", "A failed system comes back online sooner than expected.");
        CRISIS_ADVANTAGES.put("External Help", "Help arrives from an unexpected source just in time.");
        CRISIS_ADVANTAGES.put("Containment Success", "The dangerous element is contained before it spreads.");
        CRISIS_ADVANTAGES.put("Evacuation Complete", "Everyone is evacuated safely before the situation worsens.");
        CRISIS_ADVANTAGES.put("Damage Contained", "The damage is limited and repairable; the worst is avoided.");
        CRISIS_ADVANTAGES.put("Leadership Recognized", "Your calm leadership during the crisis earns respect and loyalty.");
        CRISIS_ADVANTAGES.put("Resource Found", "A hidden reserve of critical resources is discovered.");
        CRISIS_ADVANTAGES.put("Communication Restored", "Contact is re-established with key personnel or support.");
        CRISIS_ADVANTAGES.put("Crisis Averted", "The crisis is resolved before it escalates to a catastrophe.");
        CRISIS_ADVANTAGES.put("Lesson Learned", "The crisis reveals a weakness that can now be addressed.");
        CRISIS_ADVANTAGES.put("Unity", "The crisis brings people together, strengthening bonds and cooperation.");
        CRISIS_ADVANTAGES.put("Innovation", "The crisis forces an innovative solution that could be applied elsewhere.");
        CRISIS_ADVANTAGES.put("Minimal Casualties", "The number of casualties is far lower than feared.");
        CRISIS_ADVANTAGES.put("Early Warning", "An early warning system detects the crisis before it becomes critical.");
        CRISIS_ADVANTAGES.put("Backup Plan", "A contingency plan proves effective, mitigating the impact.");
        CRISIS_ADVANTAGES.put("Public Support", "The public rallies behind you during the crisis, providing moral support.");
        CRISIS_ADVANTAGES.put("Threat Neutralized", "The source of the crisis is identified and neutralized.");
        CRISIS_ADVANTAGES.put("Recovery Begins", "Recovery efforts begin immediately, and progress is rapid.");
    }

    private static final Map<String, String> GENERAL_ADVANTAGES = new LinkedHashMap<>();
    static {
        GENERAL_ADVANTAGES.put("Lucky Break", "Fortune smiles upon you; things go better than expected.");
        GENERAL_ADVANTAGES.put("Helpful Stranger", "An unexpected ally offers assistance.");
        GENERAL_ADVANTAGES.put("Clear Path", "Obstacles that should have blocked your way are absent.");
        GENERAL_ADVANTAGES.put("Good Timing", "Everything aligns perfectly; the timing is ideal.");
        GENERAL_ADVANTAGES.put("Resource Found", "You find the resources you need when you need them.");
        GENERAL_ADVANTAGES.put("Information Gained", "You learn something that gives you an advantage.");
        GENERAL_ADVANTAGES.put("Smooth Operation", "Everything proceeds without a hitch.");
        GENERAL_ADVANTAGES.put("Momentum", "Success breeds success; things start falling into place.");
        GENERAL_ADVANTAGES.put("Unexpected Help", "Someone you didn't expect offers crucial support.");
        GENERAL_ADVANTAGES.put("Favorable Conditions", "The conditions are ideal for your task.");
        GENERAL_ADVANTAGES.put("Quick Resolution", "A problem that should have taken time is resolved quickly.");
        GENERAL_ADVANTAGES.put("Boosted Morale", "The team's spirits are high, improving performance.");
        GENERAL_ADVANTAGES.put("Efficient Use", "Resources are used more efficiently than planned.");
        GENERAL_ADVANTAGES.put("Positive Outcome", "The result is better than you hoped for.");
        GENERAL_ADVANTAGES.put("New Opportunity", "A new opportunity arises from the current situation.");
        GENERAL_ADVANTAGES.put("Recognition", "Your efforts are recognized and appreciated.");
        GENERAL_ADVANTAGES.put("Strengthened Position", "Your position is stronger than before.");
        GENERAL_ADVANTAGES.put("Avoided Problem", "A potential problem is avoided before it becomes an issue.");
        GENERAL_ADVANTAGES.put("Improved Relations", "Relationships with key parties improve.");
        GENERAL_ADVANTAGES.put("The Silver Lining", "Even in difficulty, a positive outcome emerges.");
    }

    // ==================== Category Mapping ====================

    /**
     * Maps mission types to complication categories.
     */
    private static final Map<CaptainLogMission.MissionType, Category> MISSION_TYPE_TO_CATEGORY = new HashMap<>();
    static {
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.CRISIS, Category.CRISIS);
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.COMBAT, Category.COMBAT);
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.EXPLORATION, Category.EXPLORATION);
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.DIPLOMACY, Category.DIPLOMACY);
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.SCIENCE, Category.SCIENCE);
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.TRADE, Category.GENERAL);
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.RESCUE, Category.CRISIS);
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.INVESTIGATION, Category.GENERAL);
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.FIRST_CONTACT, Category.DIPLOMACY);
        MISSION_TYPE_TO_CATEGORY.put(CaptainLogMission.MissionType.PERSONAL, Category.PERSONAL);
    }

    /**
     * Gets the complication category for a given mission type.
     *
     * @param missionType the mission type
     * @return the corresponding complication category
     */
    public Category getCategoryForMissionType(CaptainLogMission.MissionType missionType) {
        return MISSION_TYPE_TO_CATEGORY.getOrDefault(missionType, Category.GENERAL);
    }

    // ==================== Complication Resolution ====================

    /**
     * Resolves a complication by rolling on the appropriate category table.
     *
     * @param category the complication category
     * @return a map containing the complication name and description
     */
    public Map<String, String> resolveComplication(Category category) {
        Map<String, String> complications = getComplicationsForCategory(category);
        return rollFromTable(complications);
    }

    /**
     * Resolves a complication based on the mission type.
     *
     * @param missionType the mission type
     * @return a map containing the complication name and description
     */
    public Map<String, String> resolveComplication(CaptainLogMission.MissionType missionType) {
        Category category = getCategoryForMissionType(missionType);
        log.debug("Resolving complication for mission type {}: using category {}", missionType, category);
        return resolveComplication(category);
    }

    /**
     * Resolves a complication with a custom category override.
     * If no category is specified, uses GENERAL.
     *
     * @param category the optional category override
     * @return a map containing the complication name and description
     */
    public Map<String, String> resolveComplication(Category category, String context) {
        Map<String, String> result = resolveComplication(category);
        if (context != null && !context.isEmpty()) {
            result.put("context", context);
        }
        return result;
    }

    // ==================== Advantage Resolution ====================

    /**
     * Resolves an advantage by rolling on the appropriate category table.
     *
     * @param category the advantage category
     * @return a map containing the advantage name and description
     */
    public Map<String, String> resolveAdvantage(Category category) {
        Map<String, String> advantages = getAdvantagesForCategory(category);
        return rollFromTable(advantages);
    }

    /**
     * Resolves an advantage based on the mission type.
     *
     * @param missionType the mission type
     * @return a map containing the advantage name and description
     */
    public Map<String, String> resolveAdvantage(CaptainLogMission.MissionType missionType) {
        Category category = getCategoryForMissionType(missionType);
        log.debug("Resolving advantage for mission type {}: using category {}", missionType, category);
        return resolveAdvantage(category);
    }

    // ==================== Category Listing ====================

    /**
     * Lists all available complication categories.
     *
     * @return a list of category names and descriptions
     */
    public List<Map<String, String>> listCategories() {
        List<Map<String, String>> categories = new ArrayList<>();
        for (Category category : Category.values()) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("category", category.name());
            entry.put("description", category.getDescription());
            categories.add(entry);
        }
        return categories;
    }

    /**
     * Lists all complications for a given category.
     *
     * @param category the category
     * @return a list of complication name-description pairs
     */
    public List<Map<String, String>> listComplications(Category category) {
        return listFromMap(getComplicationsForCategory(category));
    }

    /**
     * Lists all advantages for a given category.
     *
     * @param category the category
     * @return a list of advantage name-description pairs
     */
    public List<Map<String, String>> listAdvantages(Category category) {
        return listFromMap(getAdvantagesForCategory(category));
    }

    // ==================== Internal Methods ====================

    private Map<String, String> getComplicationsForCategory(Category category) {
        return switch (category) {
            case COMBAT -> COMBAT_COMPLICATIONS;
            case EXPLORATION -> EXPLORATION_COMPLICATIONS;
            case DIPLOMACY -> DIPLOMACY_COMPLICATIONS;
            case SCIENCE -> SCIENCE_COMPLICATIONS;
            case PERSONAL -> PERSONAL_COMPLICATIONS;
            case CRISIS -> CRISIS_COMPLICATIONS;
            case GENERAL -> GENERAL_COMPLICATIONS;
        };
    }

    private Map<String, String> getAdvantagesForCategory(Category category) {
        return switch (category) {
            case COMBAT -> COMBAT_ADVANTAGES;
            case EXPLORATION -> EXPLORATION_ADVANTAGES;
            case DIPLOMACY -> DIPLOMACY_ADVANTAGES;
            case SCIENCE -> SCIENCE_ADVANTAGES;
            case PERSONAL -> PERSONAL_ADVANTAGES;
            case CRISIS -> CRISIS_ADVANTAGES;
            case GENERAL -> GENERAL_ADVANTAGES;
        };
    }

    private Map<String, String> rollFromTable(Map<String, String> table) {
        List<String> keys = new ArrayList<>(table.keySet());
        String key = keys.get(random.nextInt(keys.size()));
        Map<String, String> result = new LinkedHashMap<>();
        result.put("name", key);
        result.put("description", table.get(key));
        return result;
    }

    private List<Map<String, String>> listFromMap(Map<String, String> map) {
        List<Map<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("description", entry.getValue());
            list.add(item);
        }
        return list;
    }
}
