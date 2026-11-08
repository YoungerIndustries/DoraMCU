package com.yisy.modules.doramcu.ai;

/**
 * Defines the management areas of an empire that can be delegated to AI.
 * <p>
 * When a player enables AI delegation at the empire level, all areas are delegated.
 * The player can then fine-tune by toggling individual areas on/off.
 * </p>
 * <p>
 * Each area corresponds to a set of game actions and UI modules:
 * </p>
 * <ul>
 *   <li>FLEET_MANAGEMENT — Fleet movement, positioning, patrol routes</li>
 *   <li>COMBAT — Engaging enemies, battle tactics, retreat decisions</li>
 *   <li>CONSTRUCTION — Building ships, stations, facilities</li>
 *   <li>RESOURCE_MANAGEMENT — Mining, processing, resource allocation</li>
 *   <li>TRADE — Buying/selling commodities, trade route optimization</li>
 *   <li>EXPANSION — Colonizing planets, claiming systems</li>
 *   <li>RESEARCH — Technology research priorities</li>
 *   <li>DIPLOMACY — Relations with other empires</li>
 *   <li>COMMANDER_ASSIGNMENT — Assigning Traveller characters as commanders</li>
 * </ul>
 */
public enum AiDelegationArea {

    /** Fleet movement, positioning, patrol routes, fleet creation. */
    FLEET_MANAGEMENT("Fleet Management", "Automatically manage fleet movements and positioning"),

    /** Engaging enemies, battle tactics, retreat decisions, battle reinforcement. */
    COMBAT("Combat", "Automatically engage enemies and make tactical combat decisions"),

    /** Building ships, stations, and planetary facilities. */
    CONSTRUCTION("Construction", "Automatically build ships, stations, and facilities"),

    /** Mining operations, resource processing, pipeline optimization. */
    RESOURCE_MANAGEMENT("Resource Management", "Automatically manage mining and resource processing"),

    /** Buying/selling commodities, trade route optimization. */
    TRADE("Trade", "Automatically execute trades and optimize trade routes"),

    /** Colonizing planets, claiming new systems for expansion. */
    EXPANSION("Expansion", "Automatically colonize planets and expand territory"),

    /** Technology research priorities and allocation. */
    RESEARCH("Research", "Automatically prioritize and allocate research"),

    /** Relations with other empires, diplomatic stances. */
    DIPLOMACY("Diplomacy", "Automatically manage diplomatic relations"),

    /** Assigning Traveller characters as empire commanders. */
    COMMANDER_ASSIGNMENT("Commander Assignment", "Automatically assign commanders to optimize bonuses");

    private final String displayName;
    private final String description;

    AiDelegationArea(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Returns the human-readable display name for this area.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a short description of what this delegation area controls.
     */
    public String getDescription() {
        return description;
    }
}
