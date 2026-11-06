package com.yisy.modules.doramcu.hex;

/**
 * Callback interface for galaxy generation progress reporting.
 * <p>
 * Implementations receive progress updates during galaxy generation,
 * allowing UI components to display progress bars, status messages,
 * and detailed step-by-step build logs.
 * </p>
 */
public interface GalaxyGenerationProgress {
    /**
     * Called when preparation steps begin, before galaxy generation starts.
     *
     * @param stepDescription a human-readable description of the preparation step
     */
    default void onPrepareStep(String stepDescription) {
        // Default no-op; override to capture preparation steps
    }

    /**
     * Called when galaxy generation starts.
     *
     * @param totalSystems the total number of solar systems to generate
     */
    void onStart(int totalSystems);

    /**
     * Called after each solar system is generated.
     *
     * @param systemsGenerated the number of systems generated so far
     * @param totalSystems the total number of systems to generate
     */
    void onSystemGenerated(int systemsGenerated, int totalSystems);

    /**
     * Called when hyperlane generation starts.
     */
    void onHyperlanesStart();

    /**
     * Called when galaxy generation is complete.
     */
    void onComplete();

    /**
     * Called when a log message should be appended to the build log.
     * <p>
     * Implementations may display this in a text area, console, or other
     * logging surface. The message includes a step description and optional
     * detail information.
     * </p>
     *
     * @param message the log message text
     */
    default void onLog(String message) {
        // Default no-op; override to capture log messages
    }

    /**
     * Called when a sub-step within a solar system generation is completed.
     * <p>
     * For example: "Generating star...", "Adding 5 planets...", "Creating nebula...".
     * </p>
     *
     * @param systemIndex the 1-based index of the current system being generated
     * @param stepDescription a human-readable description of the sub-step
     */
    default void onSystemStep(int systemIndex, String stepDescription) {
        // Default no-op; override to capture sub-step details
    }

    /**
     * Called when database persistence starts.
     */
    default void onPersistStart() {
        // Default no-op; override to capture persistence start
    }

    /**
     * Called when database persistence completes.
     */
    default void onPersistComplete() {
        // Default no-op; override to capture persistence complete
    }

    /**
     * Called when galaxy generation is ready to begin.
     * <p>
     * This signals that all preparation steps are complete and the
     * actual generation is about to start.
     * </p>
     */
    default void onReady() {
        // Default no-op; override to capture ready state
    }

    /**
     * Called when a solar system has just been created, before the delay.
     * <p>
     * This allows UI components to display the just-built system
     * (e.g., in a 2D visualization panel) before moving on.
     * </p>
     *
     * @param system the newly created solar system
     */
    default void onSystemCreated(com.yisy.modules.doramcu.hex.SolarSystem system) {
        // Default no-op; override to display the system
    }
}
