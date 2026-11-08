/**
 * JPA entity models for database persistence.
 * <p>
 * This package contains the persistent data layer — JPA entities mapped to database tables
 * via Hibernate. These objects represent the stored state of galaxies, solar systems,
 * planets, users, players, and game sessions.
 * </p>
 *
 * <h3>Relationship with {@code com.yisy.modules.doramcu.hex}</h3>
 * <p>
 * The {@code hex/} package contains in-memory domain objects used for live game logic.
 * The {@code model/} package contains only the persistence layer. They share similar
 * class names (e.g., {@link Planet} vs {@code hex.Planet}) but serve different purposes:
 * </p>
 * <ul>
 *   <li>{@code model.Planet} — JPA entity persisted to the {@code planets} table</li>
 *   <li>{@code hex.Planet} — In-memory domain object with game logic (658 lines)</li>
 * </ul>
 * <p>
 * <b>Conversion direction:</b>
 * </p>
 * <ul>
 *   <li><b>model → hex:</b> {@link com.yisy.modules.doramcu.service.GameInitializationService}
 *       converts JPA entities to hex domain objects on session load.</li>
 *   <li><b>hex → model:</b> Game state is serialized as JSON and stored in
 *       {@code PersistedGameSession.gameStateJson} on session save.</li>
 * </ul>
 * <p>
 * <b>Rule:</b> Never reference {@code hex.*} classes directly from {@code model.*} classes.
 * All conversion happens in the service layer.
 * </p>
 */
package com.yisy.modules.doramcu.model;
