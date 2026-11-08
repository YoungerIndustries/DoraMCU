/**
 * Classic Traveller game systems — Character creation, starship design, combat, navigation,
 * naval operations, and mercenary ground forces.
 * <p>
 * This package implements rules from Classic Traveller (1977-1981), including:
 * </p>
 * <ul>
 *   <li><b>Book 1: Characters &amp; Combat</b> — Character creation, careers, skills, melee and firearms combat</li>
 *   <li><b>Book 2: Starships</b> — Tonnage-based modular ship design, ship-to-ship combat, jump travel, and maneuver</li>
 *   <li><b>Book 4: Mercenary</b> — Army careers, unit organization, ground combat, recruiting, tickets, and equipment</li>
 *   <li><b>Book 5: High Guard</b> — Imperial Navy ranks, fleet organization, formations, and naval battles</li>
 *   <li><b>Supplement 3: Spinward Marches</b> — World generation with UWP profiles, trade codes, and sector maps</li>
 * </ul>
 * <p>
 * Core subsystems:
 * </p>
 * <ul>
 *   <li><b>Character Creation</b> — Ability scores, careers, skills, mercenary career generation</li>
 *   <li><b>Ship Design</b> — Hull, drives, power plant, life support, computers, weapons, passengers</li>
 *   <li><b>Ship-to-Ship Combat</b> — Hit locations, damage resolution, battle coordination</li>
 *   <li><b>Design-to-Combat Bridge</b> — Converts Traveller designs to hex combat ships</li>
 *   <li><b>World &amp; Sector</b> — UWP profiles, trade codes, hyperlanes, sector generation</li>
 *   <li><b>Jump Travel</b> — Route planning, fuel management, interstellar navigation</li>
 *   <li><b>Maneuver &amp; Exploration</b> — Within-system travel, planet approach, system survey</li>
 *   <li><b>Naval Operations</b> — Imperial Navy ranks, officers, squadrons, fleets, formations, battles</li>
 *   <li><b>Mercenary</b> — Army careers, arms of service, unit organization, recruiting, tickets, abstract battles, equipment</li>
 * </ul>
 * <p>
 * This system is a standalone RPG that complements the DoraMCU 4X game ({@code com.yisy.modules.doramcu.hex})
 * by providing Traveller-compatible character creation, ship design, and combat systems.
 * Integration between the two games is handled by the {@code com.yisy.modules.doramcu.unified} package.
 * </p>
 */
package com.yisy.modules.doramcu.traveller;
