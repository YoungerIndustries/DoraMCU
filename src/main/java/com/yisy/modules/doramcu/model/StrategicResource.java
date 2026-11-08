package com.yisy.modules.doramcu.model;

/**
 * Shared strategic resource enum for all celestial body JPA entities.
 * <p>
 * Strategic resources are rare deposits that create meaningful trade-offs in ship design.
 * This enum is shared across Planet, Moon, Asteroid, AsteroidBelt, and Comet entities
 * to avoid duplication. It mirrors the domain model {@code hex.StrategicResource}.
 * </p>
 */
public enum StrategicResource {
    /** Elerium — powers beam weapons and shields. */
    ELERIUM,

    /** Anti-Matter — powers missile weapons. */
    ANTI_MATTER,

    /** Thulium — powers mass driver weapons. */
    THULIUM,

    /** Durantium — required for elite manufacturing efforts. */
    DURANTIUM,

    /** Promethion — required for elite research efforts. */
    PROMETHION
}
