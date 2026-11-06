package com.yisy.modules.doramcu.hex;

/**
 * Represents strategic resources required for specialized ship components and elite units.
 * <p>
 * Strategic resources are rare deposits that create meaningful trade-offs in ship design.
 * A ship that excels in one area may be impossible to build without controlling specific
 * resource nodes. This follows the Galactic Civilizations IV model where strategic
 * resources gate advanced military capabilities.
 * </p>
 */
public enum StrategicResource {
    /**
     * Elerium — powers beam weapons and shields.
     */
    ELERIUM,

    /**
     * Anti-Matter — powers missile weapons.
     */
    ANTI_MATTER,

    /**
     * Thulium — powers mass driver weapons.
     */
    THULIUM,

    /**
     * Durantium — required for elite manufacturing efforts.
     */
    DURANTIUM,

    /**
     * Promethion — required for elite research efforts.
     */
    PROMETHION
}
