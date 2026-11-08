package com.yisy.modules.doramcu.captainslog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Implements the Probability Matrixes from the Captain's Log Solo RPG rulebook.
 * <p>
 * The rulebook contains over 100 random tables organized into categories:
 * <ul>
 *   <li>Yes/No Probability Matrix — core oracle with probability levels</li>
 *   <li>Mission Type tables — what kind of mission is this?</li>
 *   <li>Theme tables — the underlying narrative theme</li>
 *   <li>Advantage/Complication tables — narrative twists</li>
 *   <li>Encounter tables — who or what appears</li>
 *   <li>NPC tables — allies and adversaries (Appendix II)</li>
 *   <li>Location tables — where events take place (Appendix III)</li>
 *   <li>Alien Civilization tables — species and polities</li>
 *   <li>Career Event tables — what happens during downtime</li>
 * </ul>
 * </p>
 * <p>
 * Each table uses a d20 roll (1-20) to select from 20 entries.
 * Some tables use d6 or d10 and are mapped accordingly.
 * </p>
 */
@Component
public class ProbabilityMatrixService {

    private static final Logger log = LoggerFactory.getLogger(ProbabilityMatrixService.class);
    private final Random random = new Random();

    // ==================== Yes/No Probability Matrix ====================

    /**
     * Probability levels for Yes/No oracle queries.
     * The player sets the probability based on narrative context.
     */
    public enum ProbabilityLevel {
        VERY_UNLIKELY,   // 10% Yes
        UNLIKELY,        // 30% Yes
        FIFTY_FIFTY,     // 50% Yes
        LIKELY,          // 70% Yes
        VERY_LIKELY,     // 90% Yes
        CERTAIN          // 100% Yes (but may have complications)
    }

    /**
     * The Yes/No oracle result.
     */
    public enum YesNoResult {
        YES("Yes — the answer is affirmative."),
        YES_PLUS("Yes, and — the answer is affirmative with a bonus or advantage."),
        YES_MINUS("Yes, but — the answer is affirmative with a complication or cost."),
        NO("No — the answer is negative."),
        NO_PLUS("No, but — the answer is negative with a silver lining."),
        NO_MINUS("No, and — the answer is negative with additional bad news."),
        BOTH("Both/Neither — the situation is more complex than a simple yes or no."),
        CRITICAL_YES("Critical Yes — an extraordinary and highly favorable outcome."),
        CRITICAL_NO("Critical No — an extraordinary and highly unfavorable outcome.");

        private final String description;

        YesNoResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Queries the Yes/No oracle with a probability level.
     * <p>
     * The probability level determines the likelihood of a Yes result.
     * A d20 is rolled, and the result is compared to the probability threshold.
     * </p>
     *
     * @param probability the estimated probability level
     * @return the oracle result
     */
    public YesNoResult queryYesNo(ProbabilityLevel probability) {
        int roll = random.nextInt(20) + 1;
        log.debug("Yes/No oracle: probability={}, roll={}", probability, roll);

        return switch (probability) {
            case VERY_UNLIKELY -> queryVeryUnlikely(roll);
            case UNLIKELY -> queryUnlikely(roll);
            case FIFTY_FIFTY -> queryFiftyFifty(roll);
            case LIKELY -> queryLikely(roll);
            case VERY_LIKELY -> queryVeryLikely(roll);
            case CERTAIN -> queryCertain(roll);
        };
    }

    private YesNoResult queryVeryUnlikely(int roll) {
        // 10% Yes: only 20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 19) return YesNoResult.YES_MINUS;
        if (roll >= 17) return YesNoResult.NO_PLUS;
        if (roll >= 15) return YesNoResult.NO;
        return YesNoResult.NO_MINUS;
    }

    private YesNoResult queryUnlikely(int roll) {
        // 30% Yes: 16-20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 16) return YesNoResult.YES_MINUS;
        if (roll >= 14) return YesNoResult.YES;
        if (roll >= 11) return YesNoResult.BOTH;
        if (roll >= 6) return YesNoResult.NO;
        if (roll <= 2) return YesNoResult.CRITICAL_NO;
        return YesNoResult.NO_MINUS;
    }

    private YesNoResult queryFiftyFifty(int roll) {
        // 50% Yes: 11-20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 14) return YesNoResult.YES_PLUS;
        if (roll >= 11) return YesNoResult.YES;
        if (roll >= 10) return YesNoResult.BOTH;
        if (roll >= 7) return YesNoResult.NO;
        if (roll <= 2) return YesNoResult.CRITICAL_NO;
        return YesNoResult.NO_MINUS;
    }

    private YesNoResult queryLikely(int roll) {
        // 70% Yes: 4-20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 16) return YesNoResult.YES_PLUS;
        if (roll >= 4) return YesNoResult.YES;
        if (roll >= 2) return YesNoResult.YES_MINUS;
        return YesNoResult.CRITICAL_NO;
    }

    private YesNoResult queryVeryLikely(int roll) {
        // 90% Yes: 2-20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 16) return YesNoResult.YES_PLUS;
        if (roll >= 2) return YesNoResult.YES;
        if (roll == 1) return YesNoResult.CRITICAL_NO;
        return YesNoResult.NO_MINUS;
    }

    private YesNoResult queryCertain(int roll) {
        // 100% Yes, but may have complications
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 16) return YesNoResult.YES_PLUS;
        if (roll >= 6) return YesNoResult.YES;
        return YesNoResult.YES_MINUS;
    }

    // ==================== Mission Type Table ====================

    /**
     * Mission types from the Captain's Log rules.
     */
    public enum MissionType {
        DIPLOMACY("Diplomacy — Negotiations, treaties, and political maneuvering."),
        EXPLORATION("Exploration — Charting unknown space and discovering new worlds."),
        COMBAT("Combat — Engaging hostile forces in battle."),
        SCIENCE("Science — Research, analysis, and scientific discovery."),
        RESCUE("Rescue — Saving those in distress."),
        INVESTIGATION("Investigation — Uncovering secrets and solving mysteries."),
        TRADE("Trade — Commerce, resource acquisition, and economic dealings."),
        SURVEILLANCE("Surveillance — Espionage, reconnaissance, and covert operations."),
        CRISIS("Crisis — Responding to emergencies and disasters."),
        FIRST_CONTACT("First Contact — Encountering unknown species or civilizations."),
        MAINTENANCE("Maintenance — Ship repairs, upgrades, and downtime activities."),
        PATROL("Patrol — Monitoring borders and protecting territory."),
        TRANSPORT("Transport — Delivering personnel, cargo, or messages."),
        MEDICAL("Medical — Treating the sick and injured, epidemiological research."),
        SEARCH("Search — Finding lost ships, colonies, or artifacts."),
        ESCORT("Escort — Protecting another vessel or convoy."),
        RECONNAISSANCE("Reconnaissance — Gathering intelligence on a target."),
        NEGOTIATION("Negotiation — Brokerage between conflicting parties."),
        SURVIVAL("Survival — Enduring hostile environments or situations."),
        CONSPIRACY("Conspiracy — Uncovering plots and hidden agendas.");

        private final String description;

        MissionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random mission type.
     * @return a randomly selected mission type
     */
    public MissionType rollMissionType() {
        MissionType[] types = MissionType.values();
        int index = random.nextInt(types.length);
        log.debug("Rolled mission type: {}", types[index]);
        return types[index];
    }

    // ==================== Mission Theme Table ====================

    /**
     * Narrative themes that color a mission.
     */
    public enum MissionTheme {
        BETRAYAL("Betrayal — Someone close cannot be trusted."),
        SACRIFICE("Sacrifice — Something valuable must be given up."),
        REDEMPTION("Redemption — A chance to make amends for past failures."),
        DISCOVERY("Discovery — Uncovering something that changes everything."),
        SURVIVAL("Survival — Enduring against overwhelming odds."),
        HONOR("Honor — Upholding principles in the face of temptation."),
        REVENGE("Revenge — Settling old scores."),
        HOPE("Hope — Finding light in the darkest circumstances."),
        FEAR("Fear — Confronting what lies in the shadows."),
        LOYALTY("Loyalty — Standing by allies when it matters most."),
        DECEPTION("Deception — Nothing is as it seems."),
        LEGACY("Legacy — The weight of the past shapes the future."),
        ISOLATION("Isolation — Cut off from help, alone with the problem."),
        ALLIANCE("Alliance — Forming unexpected partnerships."),
        CORRUPTION("Corruption — Power's corrupting influence."),
        INNOVATION("Innovation — Breaking new ground with bold ideas."),
        JUSTICE("Justice — Righting wrongs and holding the guilty accountable."),
        MYSTERY("Mystery — Solving an enigma with far-reaching consequences."),
        WAR("War — The brutal reality of conflict."),
        PEACE("Peace — Building bridges where walls once stood.");

        private final String description;

        MissionTheme(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random mission theme.
     * @return a randomly selected mission theme
     */
    public MissionTheme rollMissionTheme() {
        MissionTheme[] themes = MissionTheme.values();
        int index = random.nextInt(themes.length);
        log.debug("Rolled mission theme: {}", themes[index]);
        return themes[index];
    }

    // ==================== Complication Table ====================

    /**
     * Complications that arise during a mission.
     * These are triggered by Threat, failed checks, or narrative choices.
     */
    public enum Complication {
        REINFORCEMENTS("Reinforcements — The enemy gets backup."),
        SYSTEM_FAILURE("System Failure — A critical system goes offline."),
        BETRAYAL("Betrayal — An ally reveals hidden motives."),
        TIME_PRESSURE("Time Pressure — The clock starts ticking faster."),
        CASUALTIES("Casualties — Someone is hurt or lost."),
        ENVIRONMENTAL_HAZARD("Environmental Hazard — The environment turns hostile."),
        COMMUNICATIONS_BLACKOUT("Communications Blackout — Cut off from support."),
        UNEXPECTED_ALLY("Unexpected Ally — A stranger offers help, but at what cost?"),
        MORAL_DILEMMA("Moral Dilemma — A choice with no good options."),
        EQUIPMENT_LOSS("Equipment Loss — Vital gear is destroyed or stolen."),
        AMBUSH("Ambush — Attacked from an unexpected direction."),
        POLITICAL_INTERFERENCE("Political Interference — Orders from above complicate things."),
        TREACHEROUS_TERRAIN("Treachorous Terrain — The environment works against you."),
        DOUBLE_AGENT("Double Agent — Someone is playing both sides."),
        POWER_FAILURE("Power Failure — Lights out, systems down."),
        HOSTAGE_SITUATION("Hostage Situation — Innocents are held at gunpoint."),
        WEAPON_OF_MASS_DESTRUCTION("Weapon of Mass Destruction — A doomsday device is activated."),
        MUTINY("Mutiny — Crew members turn against the captain."),
        TEMPORAL_ANOMALY("Temporal Anomaly — Time behaves strangely."),
        COSMIC_HORROR("Cosmic Horror — Something beyond comprehension appears.");

        private final String description;

        Complication(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random complication.
     * @return a randomly selected complication
     */
    public Complication rollComplication() {
        Complication[] complications = Complication.values();
        int index = random.nextInt(complications.length);
        log.debug("Rolled complication: {}", complications[index]);
        return complications[index];
    }

    // ==================== Advantage Table ====================

    /**
     * Advantages that help during a mission.
     * These are triggered by Momentum, successful checks, or narrative choices.
     */
    public enum Advantage {
        INTEL("Intel — You learn something the enemy doesn't expect you to know."),
        REINFORCEMENTS("Reinforcements — Friendly forces arrive to help."),
        EQUIPMENT_BOOST("Equipment Boost — A piece of gear performs better than expected."),
        ALLY_ASSISTANCE("Ally Assistance — An ally steps in at the crucial moment."),
        ENVIRONMENTAL_EDGE("Environmental Edge — The environment works in your favor."),
        SURPRISE_ATTACK("Surprise Attack — You catch the enemy off guard."),
        MORALE_BOOST("Morale Boost — The crew's spirits are lifted."),
        TECHNOLOGY_EDGE("Technology Edge — Your tech outclasses the opposition."),
        INSIDER_INFORMATION("Insider Information — A mole provides critical details."),
        SAFE_HAVEN("Safe Haven — A place to rest and regroup."),
        WEAPON_UPGRADE("Weapon Upgrade — Armaments are more effective than planned."),
        DIPLOMATIC_OPENING("Diplomatic Opening — A path to negotiation appears."),
        MEDICAL_SUPPLY("Medical Supply — Healing resources are found."),
        TRANSPORT_ASSISTANCE("Transport Assistance — A ride or shuttle becomes available."),
        SENSOR_ADVANTAGE("Sensor Advantage — You see what others miss."),
        SHIELD_BOOST("Shield Boost — Defenses hold stronger than expected."),
        CREW_EXPERTISE("Crew Expertise — A crew member's skill shines."),
        LUCKY_BREAK("Lucky Break — Fortune smiles upon you."),
        ELEMENT_OF_SURPRISE("Element of Surprise — The enemy isn't ready."),
        COMMAND_PRESENCE("Command Presence — Your leadership inspires excellence.");

        private final String description;

        Advantage(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random advantage.
     * @return a randomly selected advantage
     */
    public Advantage rollAdvantage() {
        Advantage[] advantages = Advantage.values();
        int index = random.nextInt(advantages.length);
        log.debug("Rolled advantage: {}", advantages[index]);
        return advantages[index];
    }

    // ==================== Encounter Table ====================

    /**
     * Types of encounters that can occur during a mission.
     */
    public enum EncounterType {
        HOSTILE_SHIP("Hostile Ship — An enemy vessel hails or attacks."),
        DISTRESS_SIGNAL("Distress Signal — Someone is calling for help."),
        ANOMALOUS_READING("Anomalous Reading — Sensors detect something unusual."),
        NEUTRAL_PARTY("Neutral Party — A third faction enters the scene."),
        CIVILIAN_VESSEL("Civilian Vessel — Non-combatants are in the area."),
        DERELICT("Derelict — An abandoned ship or station drifts nearby."),
        ASTEROID_FIELD("Asteroid Field — Navigational hazard ahead."),
        PLANETARY_DEFENSE("Planetary Defense — Automated defenses activate."),
        PIRATE_ATTACK("Pirate Attack — Raiders strike without warning."),
        DIPLOMATIC_MEETING("Diplomatic Meeting — A formal encounter with another faction."),
        SCIENTIFIC_PHENOMENON("Scientific Phenomenon — A natural event demands study."),
        BLACK_MARKET("Black Market — An opportunity for illicit trade."),
        SEARCH_AND_RESCUE("Search and Rescue — Survivors need extraction."),
        TERRITORIAL_DISPUTE("Territorial Dispute — Two factions clash over space."),
        MYSTERIOUS_SIGNAL("Mysterious Signal — An encoded transmission is detected."),
        WEATHER_STORM("Weather Storm — A plasma storm or radiation belt."),
        GHOST_SHIP("Ghost Ship — A vessel with no life signs approaches."),
        REFUGEE_CRISIS("Refugee Crisis — Displaced civilians need aid."),
        MILITARY_BLOCKADE("Military Blockade — Passage is denied."),
        ARTIFACT_DISCOVERY("Artifact Discovery — An ancient object is found.");

        private final String description;

        EncounterType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random encounter.
     * @return a randomly selected encounter type
     */
    public EncounterType rollEncounter() {
        EncounterType[] encounters = EncounterType.values();
        int index = random.nextInt(encounters.length);
        log.debug("Rolled encounter: {}", encounters[index]);
        return encounters[index];
    }

    // ==================== NPC Generation Tables (Appendix II) ====================

    /**
     * NPC roles from the Captain's Log rulebook.
     */
    public enum NpcRole {
        COMMANDING_OFFICER("Commanding Officer — A leader making tough decisions."),
        FIRST_OFFICER("First Officer — The right hand, executing orders."),
        SCIENCE_OFFICER("Science Officer — Analyzing data and making discoveries."),
        SECURITY_OFFICER("Security Officer — Enforcing order and handling threats."),
        ENGINEER("Engineer — Keeping the ship running and fixing problems."),
        MEDICAL_OFFICER("Medical Officer — Healing the wounded and treating disease."),
        PILOT("Pilot — Flying the ship through danger."),
        TACTICAL_OFFICER("Tactical Officer — Directing weapons and shields."),
        COMMUNICATIONS_OFFICER("Communications Officer — Managing all transmissions."),
        NAVIGATOR("Navigator — Plotting courses through space."),
        DIPLOMAT("Diplomat — Negotiating with other factions."),
        SCOUT("Scout — Reconnaissance and first contact."),
        MERCHANT("Merchant — Trading goods and services."),
        SMUGGLER("Smuggler — Operating outside the law."),
        SPY("Spy — Gathering intelligence covertly."),
        MERCENARY("Mercenary — Fighting for profit."),
        ACADEMIC("Academic — Researching and teaching."),
        RELIGIOUS_LEADER("Religious Leader — Guiding the faithful."),
        CRIMINAL("Criminal — Operating in the shadows."),
        CIVILIAN("Civilian — An ordinary person caught in extraordinary events.");

        private final String description;

        NpcRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * NPC alignment — how they relate to the player.
     */
    public enum NpcAlignment {
        ALLY("Ally — Works with the player character."),
        NEUTRAL("Neutral — Has their own agenda."),
        RIVAL("Rival — Competes with the player."),
        ENEMY("Enemy — Actively opposes the player."),
        WILD_CARD("Wild Card — Unpredictable, could go either way.");

        private final String description;

        NpcAlignment(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Alien species from the Star Trek universe and beyond.
     */
    public enum AlienSpecies {
        HUMAN("Human — Versatile and adaptable."),
        ANDORIAN("Andorian — Honor-bound warriors from a cold world."),
        BAJORAN("Bajoran — Spiritual and resilient."),
        BETAZOID("Betazoid — Empathic communicators."),
        BOLIAN("Bolian — Artistic and philosophical."),
        CARDASSIAN("Cardassian — Ambitious and militaristic."),
        DENOBULAN("Denobulan — Master healers and scientists."),
        FERENGI("Ferengi — Profit-driven traders."),
        HYLEAN("Hylean — Proud and traditional."),
        KLINGON("Klingon — Fierce warriors who value honor."),
        ROMULAN("Romulan — Cunning and secretive."),
        VULCAN("Vulcan — Logical and disciplined."),
        TRILL("Trill — Symbiotic beings with combined wisdom."),
        ORION("Orion — Raiders and traders."),
        THOLIAN("Tholian — Isolationist and technologically advanced."),
        ROMULAN_ELITE("Romulan Elite — Nobility of the Romulan Star Empire."),
        FOUNDLING("Foundling — A mysterious and ancient species."),
        XINDI("Xindi — A diverse species with multiple primate forms."),
        DELTA_RACE("Delta Race — Advanced beings from the Delta Quadrant."),
        UNKNOWN("Unknown — An unclassified species.");

        private final String description;

        AlienSpecies(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * NPC name prefixes for generating character names.
     */
    private static final String[] NPC_FIRST_NAMES = {
        "James", "Kathryn", "Jean-Luc", "Benjamin", "Jonathan", "Chakotay",
        "Worf", "Deanna", "Data", "Geordi", "Picard", "Sisko",
        "Janeway", "Archer", "Kirk", "Spock", "McCoy", "Scott",
        "Uhura", "Chekov", "Sulu", "Riker", "Troi", "La Forge",
        "Tuvok", "Harry", "Paris", "Tom", "Voyager", "B'Elanna",
        "Torres", "Neelix", "Seven", "Kes", "Tuvok", "Guinan",
        "Q", "Zefram", "Zhang", "Abe", "Pike", "Number One",
        "Philippa", "George", "Muiri", "Raana", "Hikaru", "Carol",
        "Saru", "Tilly", "Stamets", "Burnham", "Georgiou", "Vash"
    };

    /**
     * NPC name suffixes for generating character names.
     */
    private static final String[] NPC_LAST_NAMES = {
        "Kirk", "Picard", "Sisko", "Janeway", "Archer", "Scott",
        "McCoy", "Spock", "Chekov", "Sulu", "Riker", "Troi",
        "La Forge", "Tuvok", "Paris", "Tom", "Torres", "Neelix",
        "Guinan", "Zefram", "Zhang", "Pike", "Saru", "Tilly",
        "Stamets", "Burnham", "Georgiou", "Vash", "Crusher", "Worf",
        "Dax", "Ezri", "Weyoun", "Dukat", "O'Brien", "Kira",
        "Sisko", "Quark", "Garak", "Odo", "Dax", "Rom",
        "Leeta", " Nog", "Jadzia", "Curzon", "Emony", "Gowron"
    };

    /**
     * Generates a random NPC name.
     * @return a randomly generated NPC name
     */
    public String generateNpcName() {
        String first = NPC_FIRST_NAMES[random.nextInt(NPC_FIRST_NAMES.length)];
        String last = NPC_LAST_NAMES[random.nextInt(NPC_LAST_NAMES.length)];
        return first + " " + last;
    }

    /**
     * Rolls for a random NPC role.
     * @return a randomly selected NPC role
     */
    public NpcRole rollNpcRole() {
        NpcRole[] roles = NpcRole.values();
        int index = random.nextInt(roles.length);
        log.debug("Rolled NPC role: {}", roles[index]);
        return roles[index];
    }

    /**
     * Rolls for a random NPC alignment.
     * @return a randomly selected NPC alignment
     */
    public NpcAlignment rollNpcAlignment() {
        NpcAlignment[] alignments = NpcAlignment.values();
        int index = random.nextInt(alignments.length);
        log.debug("Rolled NPC alignment: {}", alignments[index]);
        return alignments[index];
    }

    /**
     * Rolls for a random alien species.
     * @return a randomly selected alien species
     */
    public AlienSpecies rollAlienSpecies() {
        AlienSpecies[] species = AlienSpecies.values();
        int index = random.nextInt(species.length);
        log.debug("Rolled alien species: {}", species[index]);
        return species[index];
    }

    /**
     * Generates a complete NPC profile.
     * @return a map containing the NPC's name, role, alignment, and species
     */
    public Map<String, String> generateNpc() {
        String name = generateNpcName();
        NpcRole role = rollNpcRole();
        NpcAlignment alignment = rollNpcAlignment();
        AlienSpecies species = rollAlienSpecies();

        log.info("Generated NPC: {} ({}) - {} - {} - {}", name, species, role, alignment, role.getDescription());

        Map<String, String> npc = new LinkedHashMap<>();
        npc.put("name", name);
        npc.put("species", species.name());
        npc.put("speciesDescription", species.getDescription());
        npc.put("role", role.name());
        npc.put("roleDescription", role.getDescription());
        npc.put("alignment", alignment.name());
        return npc;
    }

    // ==================== Location Tables (Appendix III) ====================

    /**
     * Location types where events can take place.
     */
    public enum LocationType {
        STARBASE("Starbase — A large military or civilian space station."),
        COLONY("Colony — A settled world with infrastructure."),
        SPACE_STATION("Space Station — A smaller orbital facility."),
        PLANET_SURFACE("Planet Surface — On the ground of a habitable world."),
        DEEP_SPACE("Deep Space — Far from any star or colony."),
        WORMHOLE("Wormhole — A spatial anomaly connecting distant points."),
        NEBULA("Nebula — A cloud of gas and dust."),
        ASTEROID_BELT("Asteroid Belt — A field of rocky bodies."),
        STARSHIP("Starship — Aboard a vessel in transit."),
        MOON("Moon — A natural satellite with facilities."),
        OUTPOST("Outpost — A remote forward base."),
        MARKET("Market — A trading hub or bazaar."),
        DIPLOMATIC_ENCLAVE("Diplomatic Enclave — Neutral ground for negotiations."),
        RESEARCH_FACILITY("Research Facility — A laboratory or observatory."),
        MILITARY_FORTRESS("Military Fortress — A heavily defended installation."),
        PRISON("Prison — A detention facility."),
        HOSPITAL("Hospital — A medical center."),
        TEMPLE("Temple — A place of worship or meditation."),
        BLACK_SITE("Black Site — A covert, unacknowledged facility."),
        ANOMALOUS_ZONE("Anomalous Zone — An area where physics behaves strangely.");

        private final String description;

        LocationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Planet types for world generation.
     */
    public enum PlanetType {
        TERRAN("Terran — Earth-like, habitable world."),
        DESERT("Desert — Arid world with vast sand seas."),
        OCEAN("Ocean — Water world with minimal landmass."),
        ICE("Ice — Frozen world with glacial landscapes."),
        VOLCANIC("Volcanic — Active tectonics and lava flows."),
        JUNGLE("Jungle — Dense vegetation and high humidity."),
        GAS_GIANT("Gas Giant — No solid surface, atmospheric platforms."),
        BARREN("Barren — Lifeless rock with minimal atmosphere."),
        TOXIC("Toxic — Hazardous atmosphere requires protection."),
        URBAN("Urban — Heavily developed, city-covered world."),
        ARCTIC("Arctic — Polar world with ice caps."),
        SWAMP("Swamp — Wetlands and marshes."),
        MOUNTAIN("Mountain — Rugged terrain with high peaks."),
        SUBTERRANEAN("Subterranean — Underground civilization."),
        MOON_HABITAT("Moon Habitat — Artificial habitat on a moon."),
        DYSON_STRUCTURE("Dyson Structure — Artificial megastructure."),
        WARP_STORM("Warp Storm — Perpetual energy storm."),
        CRYSTAL("Crystal — World covered in crystalline formations."),
        MAGNETIC("Magnetic — Extreme magnetic fields affect technology."),
        UNKNOWN("Unknown — Classification pending further study.");

        private final String description;

        PlanetType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Location name prefixes for generating place names.
     */
    private static final String[] LOCATION_PREFIXES = {
        "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta",
        "Theta", "Iota", "Kappa", "Lambda", "Mu", "Nu",
        "Xi", "Omicron", "Pi", "Rho", "Sigma", "Tau",
        "Upsilon", "Phi", "Chi", "Psi", "Omega"
    };

    /**
     * Location name roots for generating place names.
     */
    private static final String[] LOCATION_ROOTS = {
        "Prime", "Major", "Minor", "Nova", "Aeterna", "Solis",
        "Luna", "Stellar", "Nebula", "Vega", "Rigel", "Altair",
        "Deneb", "Sirius", "Polaris", "Andromeda", "Centauri", "Pegasus",
        "Orion", "Lyra", "Cassiopeia", "Draco", "Hydra", "Phoenix",
        "Andoria", "Vulcan", "Qo'noS", "Romulus", "Cardasia", "Ferenginar",
        "Bajor", "Denobula", "Trill", "Betazed", "Risa", "Cestus"
    };

    /**
     * Generates a random location name.
     * @return a randomly generated location name
     */
    public String generateLocationName() {
        String prefix = LOCATION_PREFIXES[random.nextInt(LOCATION_PREFIXES.length)];
        String root = LOCATION_ROOTS[random.nextInt(LOCATION_ROOTS.length)];
        return prefix + " " + root;
    }

    /**
     * Rolls for a random location type.
     * @return a randomly selected location type
     */
    public LocationType rollLocationType() {
        LocationType[] types = LocationType.values();
        int index = random.nextInt(types.length);
        log.debug("Rolled location type: {}", types[index]);
        return types[index];
    }

    /**
     * Rolls for a random planet type.
     * @return a randomly selected planet type
     */
    public PlanetType rollPlanetType() {
        PlanetType[] types = PlanetType.values();
        int index = random.nextInt(types.length);
        log.debug("Rolled planet type: {}", types[index]);
        return types[index];
    }

    /**
     * Generates a complete location profile.
     * @return a map containing the location's name, type, and planet type
     */
    public Map<String, String> generateLocation() {
        String name = generateLocationName();
        LocationType locationType = rollLocationType();
        PlanetType planetType = rollPlanetType();

        log.info("Generated location: {} - {} - {}", name, locationType, planetType);

        Map<String, String> location = new LinkedHashMap<>();
        location.put("name", name);
        location.put("locationType", locationType.name());
        location.put("locationTypeDescription", locationType.getDescription());
        location.put("planetType", planetType.name());
        location.put("planetTypeDescription", planetType.getDescription());
        return location;
    }

    // ==================== Career Event Table ====================

    /**
     * Career events that occur during downtime between missions.
     */
    public enum CareerEvent {
        PROMOTION("Promotion — Your rank and responsibilities increase."),
        NEW_ASSIGNMENT("New Assignment — Transferred to a different post."),
        TRAINING("Training — Attended a course that improves your skills."),
        LEAVE("Leave — Time off to rest and recharge."),
        PERSONAL_CRISIS("Personal Crisis — A personal matter demands attention."),
        NEW_ALLY("New Ally — You gain a valuable contact or friend."),
        NEW_ENEMY("New Enemy — Someone holds a grudge against you."),
        EQUIPMENT_UPGRADE("Equipment Upgrade — Your gear is improved."),
        SHIP_TRANSFER("Ship Transfer — Reassigned to a different vessel."),
        MEDICAL_TREATMENT("Medical Treatment — Health issues are addressed."),
        POLITICAL_INTRIGUE("Political Intrigue — Office politics affect your career."),
        SCIENTIFIC_BREAKTHROUGH("Scientific Breakthrough — Your research yields results."),
        COURT_MARTIAL("Court Martial — You face disciplinary action."),
        AWARDS_CEREMONY("Awards Ceremony — Your achievements are recognized."),
        MENTORSHIP("Mentorship — You guide a junior officer."),
        INVESTIGATION("Investigation — You're questioned about a past mission."),
        BLACK_OPS("Black Ops — A covert assignment is offered."),
        RETIREMENT_CONSIDERATION("Retirement Consideration — Is it time to step down?"),
        FAMILY_MATTER("Family Matter — Relatives need your help."),
        RETIREMENT("Retirement — A chapter closes, a new one begins.");

        private final String description;

        CareerEvent(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random career event.
     * @return a randomly selected career event
     */
    public CareerEvent rollCareerEvent() {
        CareerEvent[] events = CareerEvent.values();
        int index = random.nextInt(events.length);
        log.debug("Rolled career event: {}", events[index]);
        return events[index];
    }

    // ==================== Expanded Prompt Cards ====================

    /**
     * Generates a random prompt card for mission inspiration.
     * This is an expanded version of the original 20 prompts, now with 60+ prompts
     * organized by category.
     */
    public String generatePromptCard() {
        String[] prompts = {
            // Combat prompts
            "A distress signal leads to an unexpected discovery.",
            "First contact with a species that defies classification.",
            "A political crisis threatens to escalate into war.",
            "An ancient artifact holds the key to a long-lost technology.",
            "A crew member's secret is revealed at the worst moment.",
            "A spatial anomaly creates a time dilation effect.",
            "A rival faction competes for the same objective.",
            "A moral dilemma challenges the crew's values.",
            "A seemingly routine mission hides a deeper conspiracy.",
            "A natural disaster threatens a nearby colony.",
            // Exploration prompts
            "An old enemy resurfaces with a new threat.",
            "A diplomatic mission requires unusual tactics.",
            "A scientific discovery has dangerous implications.",
            "A lost starship is found with its crew intact.",
            "A planet's ecosystem is collapsing — can it be saved?",
            "A cyber attack targets the ship's computer systems.",
            "A refugee fleet seeks asylum in Federation space.",
            "A wormhole opens to an uncharted region.",
            "A saboteur is among the crew.",
            "A race against time to prevent a catastrophe.",
            // Personal prompts
            "A former colleague asks for your help in secret.",
            "You receive a message from someone you thought was dead.",
            "A vision or dream hints at a future threat.",
            "Your ship's AI begins behaving strangely.",
            "A crew member goes missing during a routine EVA.",
            "An old wound reopens — literally or figuratively.",
            "You must choose between duty and personal loyalty.",
            "A child's life depends on your decision.",
            "Your past catches up with you in an unexpected way.",
            "A gift from an unknown sender arrives aboard.",
            // Crisis prompts
            "A supernova threatens nearby colonies.",
            "The ship's warp core goes critical.",
            "A plague spreads through the crew.",
            "Hostile forces blockade a vital trade route.",
            "A diplomatic incident could spark a war.",
            "An ancient weapon is discovered and activated.",
            "The crew mutinies over a controversial order.",
            "A black hole destabilizes the sector.",
            "A temporal rift threatens to erase the timeline.",
            "An AI achieves sentience and demands rights.",
            // Diplomacy prompts
            "Two factions negotiate a fragile peace.",
            "A trade agreement hides a hidden clause.",
            "An ambassador is assassinated on your watch.",
            "A cultural misunderstanding nearly causes conflict.",
            "A neutral zone is violated by an unknown party.",
            "A hostage exchange goes wrong.",
            "A secret treaty is exposed to the public.",
            "An alien delegation requests asylum.",
            "A border dispute escalates into skirmishes.",
            "A peace conference is sabotaged.",
            // Science prompts
            "A new form of matter is discovered.",
            "An experiment yields unexpected results.",
            "A disease with no known cure spreads.",
            "A signal from deep space contains a message.",
            "A planet's core is artificially generated.",
            "A biological weapon is unleashed.",
            "A star's behavior defies known physics.",
            "An archaeological dig uncovers a pre-warp civilization.",
            "A genetic anomaly is found in the crew.",
            "A parallel universe is detected.",
            // Mystery prompts
            "A series of murders points to an inside job.",
            "Stolen data could compromise national security.",
            "A ghost ship drifts through space, crew unaccounted for.",
            "A coded message leads to a hidden base.",
            "A witness disappears before testifying.",
            "A frame-up targets an innocent officer.",
            "A conspiracy reaches the highest levels of command.",
            "A missing person's trail leads to a black site.",
            "A forged document alters the course of events.",
            "A mysterious illness affects only certain crew members."
        };
        return prompts[random.nextInt(prompts.length)];
    }

    // ==================== Maintenance / Downtime Tables (Appendix IV) ====================

    /**
     * Maintenance events that occur during downtime between missions.
     */
    public enum MaintenanceEvent {
        SYSTEM_REPAIR("System Repair — Fixing damage to ship systems."),
        WEAPON_CALIBRATION("Weapon Calibration — Tuning weapons for optimal performance."),
        SHIELD_UPGRADE("Shield Upgrade — Improving defensive capabilities."),
        ENGINE_OVERHAUL("Engine Overhaul — Boosting warp and impulse performance."),
        SENSOR_ENHANCEMENT("Sensor Enhancement — Extending detection range."),
        COMPUTER_UPDATE("Computer Update — Installing new software and patches."),
        CREW_TRAINING("Crew Training — Improving crew skills and coordination."),
        MEDICAL_SUPPLY_RESTOCK("Medical Supply Restock — Replenishing healing resources."),
        HULL_PATCHING("Hull Patching — Repairing structural damage."),
        COMMS_REPAIR("Comms Repair — Restoring communication systems."),
        LIFE_SUPPORT_CHECK("Life Support Check — Ensuring environmental systems work."),
        CARGO_ORGANIZATION("Cargo Organization — Sorting and securing supplies."),
        CREW_LEAVE("Crew Leave — Allowing crew members time off."),
        DIPLOMATIC_VISIT("Diplomatic Visit — Hosting or attending a diplomatic event."),
        SCIENTIFIC_RESEARCH("Scientific Research — Conducting experiments and studies."),
        PERSONAL_TIME("Personal Time — Reflecting on values and goals."),
        INTEL_ANALYSIS("Intel Analysis — Reviewing intelligence reports."),
        CREW_EVALUATION("Crew Evaluation — Assessing crew performance."),
        EMERGENCY_DRILL("Emergency Drill — Practicing for worst-case scenarios."),
        SHIP_CUSTOMIZATION("Ship Customization — Personalizing the vessel.");

        private final String description;

        MaintenanceEvent(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random maintenance event.
     * @return a randomly selected maintenance event
     */
    public MaintenanceEvent rollMaintenanceEvent() {
        MaintenanceEvent[] events = MaintenanceEvent.values();
        int index = random.nextInt(events.length);
        log.debug("Rolled maintenance event: {}", events[index]);
        return events[index];
    }

    /**
     * Alien polities and factions from the Star Trek universe.
     */
    public enum AlienPolity {
        UNITED_FEDERATION("United Federation of Planets — Democratic alliance of worlds."),
        KLINGON_EMPIRE("Klingon Empire — Warrior culture valoring honor and strength."),
        ROMULAN_STAR_EMPIRE("Romulan Star Empire — Secretive and cunning rivals."),
        FERENGI_ALLIANCE("Ferengi Alliance — Profit-driven merchant society."),
        CARDASSIAN_UNION("Cardassian Union — Authoritarian militaristic state."),
        BAJORAN_RESISTANCE("Bajoran Resistance — Freedom fighters seeking independence."),
        ORION_SYNDICATE("Orion Syndicate — Interstellar criminal organization."),
        THOLIAN_COLLECTIVE("Tholian Collective — Isolationist defensive alliance."),
        ANDORIAN_EMPIRE("Andorian Empire — Honor-bound warrior culture."),
        BETAZOI_COLLECTIVE("Betazoi Collective — Empathic telepathic society."),
        DENOBULAN_FEDERATION("Denobulan Federation — Healing-focused civilization."),
        TRILL_UNITY("Trill Unity — Symbiotic beings with combined wisdom."),
        ROMULAN_SENSITATIVE_OFFICE("Romulan Sensitive Office — Secret intelligence agency."),
        FERENGI_COMMERCE_AUTHORITY("Ferengi Commerce Authority — Corporate overlords."),
        KLINGON_HIGH_COUNCIL("Klingon High Council — Governing body of the Empire."),
        FEDERATION_COUNCIL("Federation Council — Legislative body of the Federation."),
        BAJORAN_RELICTION_AUTHORITY("Bajoran Religion Authority — Spiritual leaders."),
        CARDASSIA_CENTRAL("Cardassia Central — Ruling council of Cardassia."),
        MAUDITH("Mau'Dor — Ancient and mysterious beings."),
        UNKNOWN_FACTION("Unknown Faction — An unclassified political entity.");

        private final String description;

        AlienPolity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random alien polity.
     * @return a randomly selected alien polity
     */
    public AlienPolity rollAlienPolity() {
        AlienPolity[] polities = AlienPolity.values();
        int index = random.nextInt(polities.length);
        log.debug("Rolled alien polity: {}", polities[index]);
        return polities[index];
    }

    /**
     * Generates a complete downtime profile.
     * @return a map containing the maintenance event and any related polity
     */
    public Map<String, String> generateDowntimeProfile() {
        MaintenanceEvent event = rollMaintenanceEvent();
        AlienPolity polity = rollAlienPolity();

        log.info("Generated downtime profile: {} — {}", event, polity);

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("maintenanceEvent", event.name());
        profile.put("maintenanceDescription", event.getDescription());
        profile.put("relatedPolity", polity.name());
        profile.put("polityDescription", polity.getDescription());
        return profile;
    }

    // ==================== Appendix II: Allies and Adversaries ====================

    /**
     * Pre-built adversary types from the Captain's Log rulebook (Appendix II).
     * These represent recurring threats and enemies.
     */
    public enum Adversary {
        KLINGON_WARRIOR("Klingon Warrior — Honorable but deadly opponent."),
        ROMULAN_OPERATIVE("Romulan Operative — Cunning agent of the Tal Shiar."),
        FERENGI_RAIDER("Ferengi Raider — Profit-driven pirate."),
        CARDASSIAN_OFFICER("Cardassian Officer — Ruthless enforcer of the Union."),
        ORION_SLAVE_TRADER("Orion Slave Trader — Criminal dealing in lives."),
        BORG_DRONE("Borg Drone — Relentless assimilator."),
        ROMULAN_SNIPER("Romulan Sniper — Patient and precise assassin."),
        KLINGON_BOARDING_PARTY("Klingon Boarding Party — Aggressive assault team."),
        FERENGI_MERCENARY("Ferengi Mercenary — Will fight for the right price."),
        CARDASSIAN_JAGHRAAF("Cardassian Jaghraf — Elite special forces operative."),
        THOLIAN_GUARDIAN("Tholian Guardian — Defensive but dangerous."),
        BAJORAN_INSURGENT("Bajoran Insurgent — Desperate freedom fighter."),
        ORION_WARRIOR("Orion Warrior — Brutal and strong combatant."),
        ROMULAN_TACTICIAN("Romulan Tactician — Master of deception and strategy."),
        KLINGON_CHAMPION("Klingon Champion — The best warrior the Empire has to offer."),
        UNKNOWN_ENTITIES("Unknown Entities — Mysterious beings with unknown motives."),
        CYBORG_ASSASSIN("Cyborg Assassin — Enhanced killer with no mercy."),
        PIRATE_CAPTAIN("Pirate Captain — Leader of a rogue crew."),
        CORRUPT_OFFICIAL("Corrupt Official — Abuses power for personal gain."),
        RIVAL_CAPTAIN("Rival Captain — Competing officer with conflicting goals.");

        private final String description;

        Adversary(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random adversary.
     * @return a randomly selected adversary
     */
    public Adversary rollAdversary() {
        Adversary[] adversaries = Adversary.values();
        int index = random.nextInt(adversaries.length);
        log.debug("Rolled adversary: {}", adversaries[index]);
        return adversaries[index];
    }

    /**
     * Pre-built ally types from the Captain's Log rulebook (Appendix II).
     * These represent helpful contacts and allies.
     */
    public enum Ally {
        FEDERATION_LIAISON("Federation Liaison — Official contact within Starfleet."),
        INFORMANT("Informant — Source of intelligence from within enemy ranks."),
        LOCAL_GUIDE("Local Guide — Knows the area and its secrets."),
        ALLIED_CAPTAIN("Allied Captain — Friendly officer who can provide assistance."),
        SCIENTIFIC_CONTACT("Scientific Contact — Expert who can analyze anomalies."),
        DIPLOMATIC_CONTACT("Diplomatic Contact — Can open doors through protocol."),
        RESISTANCE_LEADER("Resistance Leader — Commands local freedom fighters."),
        MEDICAL_EXPERT("Medical Expert — Can heal injuries and treat diseases."),
        TECH_SPECIALIST("Tech Specialist — Can repair or hack systems."),
        NAVIGATOR("Navigator — Knows the routes through dangerous space."),
        TRANSLATOR("Translator — Can communicate with alien species."),
        PILOT("Pilot — Expert ship or shuttle operator."),
        SECURITY_CONSULTANT("Security Consultant — Can assess and improve defenses."),
        SUPPLIER("Supplier — Provides equipment and resources."),
        LEGAL_ADVISOR("Legal Advisor — Knows the regulations and loopholes."),
        HISTORIAN("Historian — Knows the ancient secrets and prophecies."),
        PSYCHIC_ALLY("Psychic Ally — Can sense danger and read minds."),
        ENGINEER_CONTACT("Engineer Contact — Can modify or build equipment."),
        DIPLOMAT("Diplomat — Can negotiate peace or alliances."),
        MENTOR("Mentor — Wise advisor who guides your character.");

        private final String description;

        Ally(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random ally.
     * @return a randomly selected ally
     */
    public Ally rollAlly() {
        Ally[] allies = Ally.values();
        int index = random.nextInt(allies.length);
        log.debug("Rolled ally: {}", allies[index]);
        return allies[index];
    }

    /**
     * Generates an adversary profile with name, type, and motivation.
     * @return a map containing adversary details
     */
    public Map<String, String> generateAdversaryProfile() {
        Adversary adversary = rollAdversary();
        AlienSpecies species = rollAlienSpecies();
        AlienPolity polity = rollAlienPolity();

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("adversary", adversary.name());
        profile.put("adversaryDescription", adversary.getDescription());
        profile.put("species", species.name());
        profile.put("speciesDescription", species.getDescription());
        profile.put("affiliation", polity.name());
        profile.put("affiliationDescription", polity.getDescription());
        return profile;
    }

    /**
     * Generates an ally profile with name, type, and specialty.
     * @return a map containing ally details
     */
    public Map<String, String> generateAllyProfile() {
        Ally ally = rollAlly();
        AlienSpecies species = rollAlienSpecies();

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("ally", ally.name());
        profile.put("allyDescription", ally.getDescription());
        profile.put("species", species.name());
        profile.put("speciesDescription", species.getDescription());
        return profile;
    }

    // ==================== Appendix III: Locations and Starships ====================

    /**
     * Starship classes and types from the Star Trek universe (Appendix III).
     */
    public enum StarshipClass {
        CONSTITUTION("Constitution-Class — Versatile exploration vessel (2 ships)."),
        GALAXY("Galaxy-Class — Long-range exploration flagship (5 ships)."),
        SOVEREIGN("Sovereign-Class — Diplomatic and combat vessel (3 ships)."),
        VOYAGER("Voyager-Class — Deep space exploration (2 ships)."),
        DEFiant("Defiant-Class — Heavy combat vessel (1 ship)."),
        AKIRA("Akira-Class — Heavy exploration and combat (3 ships)."),
        NEBUULA("Nebula-Class — Science and exploration (2 ships)."),
        PROMETHEUS("Prometheus-Class — Stealth and reconnaissance (1 ship)."),
        D7("D7-Class — Klingon battle cruiser (1 ship)."),
        DREADNOUGHT("Dreadnought-Class — Klingon heavy warship (2 ships)."),
        WARSHIP("Warship-Class — Romulan heavy combat vessel (1 ship)."),
        CRUISER("Cruiser-Class — Romulan long-range vessel (2 ships)."),
        RAIDER("Raider-Class — Ferengi fast attack ship (1 ship)."),
        MERCHANT("Merchant-Class — Ferengi cargo and trade vessel (2 ships)."),
        ORBITAL("Orbital Station — Large space station (6 ships)."),
        SHUTTLE("Shuttlecraft — Small transport vessel (0 ships)."),
        RUNABOUT("Runabout — Small multi-role vessel (0 ships)."),
        EXPLORER("Explorer-Class — Deep space probe ship (1 ship)."),
        CUSTOM("Custom-Class — Unique vessel with custom capabilities."),
        UNKNOWN("Unknown-Class — Unidentified vessel type.");

        private final String description;

        StarshipClass(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random starship class.
     * @return a randomly selected starship class
     */
    public StarshipClass rollStarshipClass() {
        StarshipClass[] classes = StarshipClass.values();
        int index = random.nextInt(classes.length);
        log.debug("Rolled starship class: {}", classes[index]);
        return classes[index];
    }

    /**
     * Terrain types for planetary surfaces (Appendix III).
     */
    public enum TerrainType {
        MOUNTAINS("Mountains — Rugged peaks and valleys."),
        PLAINS("Plains — Flat, open grasslands."),
        FOREST("Forest — Dense woodland with diverse flora."),
        JUNGLE("Jungle — Thick, humid tropical growth."),
        DESERT("Desert — Arid sands and rock formations."),
        TUNDRA("Tundra — Frozen, barren landscape."),
        SWAMP("Swamp — Wet, marshy terrain with standing water."),
        VOLCANIC("Volcanic — Active lava flows and ash fields."),
        OCEANIC("Oceanic — Water-covered world with islands."),
        URBAN("Urban — Heavily developed cityscape."),
        CANYON("Canyon — Deep gorges and cliffs."),
        CAVE("Cave — Underground network of tunnels."),
        GLACIER("Glacier — Massive ice formations."),
        RAIN_FOREST("Rain Forest — Lush, biodiverse ecosystem."),
        STEPPE("Steppe — Semi-arid grassland."),
        BADLANDS("Badlands — Eroded, rocky terrain."),
        CRATER("Crater — Impact site with unique geology."),
        RIFT("Rift — Tectonic fissure with unusual activity."),
        ARTIFICIAL("Artificial — Constructed or modified terrain."),
        UNKNOWN("Unknown — Terrain type not yet identified.");

        private final String description;

        TerrainType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random terrain type.
     * @return a randomly selected terrain type
     */
    public TerrainType rollTerrainType() {
        TerrainType[] terrains = TerrainType.values();
        int index = random.nextInt(terrains.length);
        log.debug("Rolled terrain type: {}", terrains[index]);
        return terrains[index];
    }

    /**
     * Atmosphere types for planetary classification (Appendix III).
     */
    public enum AtmosphereType {
        BREATHABLE("Breathable — Humans can breathe without assistance."),
        TOXIC("Toxic — Requires environmental suit."),
        THIN("Thin — Low oxygen, requires breathing apparatus."),
        DENSE("Dense — High pressure, requires pressure suit."),
        NONE("None — Vacuum or near-vacuum environment."),
        WATER_VAPOR("Water Vapor — Steam-filled atmosphere."),
        METHANE("Methane — Natural gas atmosphere."),
        AMMONIA("Ammonia — Toxic ammonia-based atmosphere."),
        CARBON_DIOXIDE("Carbon Dioxide — CO2-rich atmosphere."),
        NITROGEN("Nitrogen — Nitrogen-dominant atmosphere."),
        OXYGEN_RICH("Oxygen Rich — Highly flammable, oxygen-saturated."),
        SULFURIC("Sulfuric — Acidic atmosphere with sulfur compounds."),
        RADIATION("Radiation — High radiation levels, dangerous to life."),
        PLASMA("Plasma — Superheated ionized gas."),
        CRYOGENIC("Cryogenic — Extremely cold, frozen gases."),
        STORM("Storm — Perpetual storms and lightning."),
        MAGNETIC("Magnetic — Strong electromagnetic field in atmosphere."),
        BIOLOGICAL("Biological — Living, organic atmosphere."),
        ARTIFICIAL("Artificial — Maintained by technology."),
        UNKNOWN("Unknown — Atmosphere composition not yet determined.");

        private final String description;

        AtmosphereType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random atmosphere type.
     * @return a randomly selected atmosphere type
     */
    public AtmosphereType rollAtmosphereType() {
        AtmosphereType[] atmospheres = AtmosphereType.values();
        int index = random.nextInt(atmospheres.length);
        log.debug("Rolled atmosphere type: {}", atmospheres[index]);
        return atmospheres[index];
    }

    /**
     * Population levels for world generation (Appendix III).
     */
    public enum PopulationLevel {
        UNINHABITED("Uninhabited — No sentient life."),
        WILDLIFE_ONLY("Wildlife Only — Non-sentient animal life."),
        PRIMITIVE("Primitive — Pre-industrial civilization."),
        MEDIEVAL("Medieval — Early industrial technology."),
        INDUSTRIAL("Industrial — Steam and early electricity."),
        ATOMIC("Atomic — Nuclear age technology."),
        INTERPLANETARY("Interplanetary — Colonizing nearby planets."),
        PRE_WARP("Pre-Warp — Approaching faster-than-light travel."),
        WARP_CAPABLE("Warp Capable — Has achieved FTL travel."),
        FEDERATION_LEVEL("Federation Level — Advanced warp civilization."),
        POST_WARP("Post-Warp — Mature warp civilization."),
        TYPE_II("Type II — Harnesses stellar energy (Kardashev scale)."),
        TYPE_III("Type III — Controls galactic energy."),
        ANCIENT("Ancient — Vastly advanced, possibly precursors."),
        DYING("Dying — Civilization in decline or collapse."),
        REBUILT("Rebuilt — Recovering from past destruction."),
        HIDDEN("Hidden — Deliberately concealing their presence."),
        NOMADIC("Nomadic — Moving civilization without fixed homes."),
        SUBTERRANEAN("Subterranean — Lives underground or beneath surface."),
        UNKNOWN("Unknown — Population level not yet determined.");

        private final String description;

        PopulationLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random population level.
     * @return a randomly selected population level
     */
    public PopulationLevel rollPopulationLevel() {
        PopulationLevel[] levels = PopulationLevel.values();
        int index = random.nextInt(levels.length);
        log.debug("Rolled population level: {}", levels[index]);
        return levels[index];
    }

    /**
     * Generates a complete world profile combining planet type, terrain, atmosphere, and population.
     * @return a map containing all world details
     */
    public Map<String, String> generateWorldProfile() {
        PlanetType planetType = rollPlanetType();
        TerrainType terrain = rollTerrainType();
        AtmosphereType atmosphere = rollAtmosphereType();
        PopulationLevel population = rollPopulationLevel();

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("planetType", planetType.name());
        profile.put("planetTypeDescription", planetType.getDescription());
        profile.put("terrain", terrain.name());
        profile.put("terrainDescription", terrain.getDescription());
        profile.put("atmosphere", atmosphere.name());
        profile.put("atmosphereDescription", atmosphere.getDescription());
        profile.put("population", population.name());
        profile.put("populationDescription", population.getDescription());
        return profile;
    }

    /**
     * Generates a complete starship encounter profile.
     * @return a map containing starship class and polity affiliation
     */
    public Map<String, String> generateStarshipEncounter() {
        StarshipClass shipClass = rollStarshipClass();
        AlienPolity polity = rollAlienPolity();

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("starshipClass", shipClass.name());
        profile.put("starshipDescription", shipClass.getDescription());
        profile.put("affiliation", polity.name());
        profile.put("affiliationDescription", polity.getDescription());
        return profile;
    }

    // ==================== Combined Mission Generation ====================

    /**
     * Generates a complete mission profile by rolling on multiple tables.
     * @return a map containing the mission type, theme, complication, encounter, NPC, and location
     */
    public Map<String, Object> generateMissionProfile() {
        MissionType missionType = rollMissionType();
        MissionTheme theme = rollMissionTheme();
        EncounterType encounter = rollEncounter();
        Map<String, String> npc = generateNpc();
        Map<String, String> location = generateLocation();
        String prompt = generatePromptCard();

        log.info("Generated mission profile: {} / {} at {}", missionType, theme, location.get("name"));

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("missionType", missionType.name());
        profile.put("missionTypeDescription", missionType.getDescription());
        profile.put("theme", theme.name());
        profile.put("themeDescription", theme.getDescription());
        profile.put("encounter", encounter.name());
        profile.put("encounterDescription", encounter.getDescription());
        profile.put("npc", npc);
        profile.put("location", location);
        profile.put("prompt", prompt);
        return profile;
    }
}
