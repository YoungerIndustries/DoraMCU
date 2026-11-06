package com.yisy.modules.doramcu.hex;

/**
 * Represents the population attribute categories that affect planet output.
 * <p>
 * Population stats follow the Galactic Civilizations IV model where each attribute
 * influences a different aspect of planetary production:
 * </p>
 * <ul>
 *   <li><b>Social</b> — Influence and approval output</li>
 *   <li><b>Intelligence</b> — Research output</li>
 *   <li><b>Diligence</b> — Manufacturing and production output</li>
 *   <li><b>Resolve</b> — Military damage and ship hit points</li>
 * </ul>
 */
public enum PopulationAttribute {
    /**
     * Social — affects influence and approval output.
     */
    SOCIAL,

    /**
     * Intelligence — affects research output.
     */
    INTELLIGENCE,

    /**
     * Diligence — affects manufacturing and production output.
     */
    DILIGENCE,

    /**
     * Resolve — affects military damage and ship hit points.
     */
    RESOLVE
}
