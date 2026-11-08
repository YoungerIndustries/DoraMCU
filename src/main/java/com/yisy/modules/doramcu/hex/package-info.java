/**
 * In-memory game domain objects (hex grid model).
 * <p>
 * This package contains the core game logic domain — planets, stars, fleets, ships,
 * galaxies, sectors, subsectors, and all entities that participate in gameplay.
 * These objects are <b>not</b> JPA entities; they exist only in memory during a game session.
 * </p>
 *
 * <h3>Relationship with {@code com.yisy.modules.doramcu.model}</h3>
 * <p>
 * The {@code model/} package contains JPA entities that persist data to the database.
 * The {@code hex/} package contains domain objects that represent the live game state.
 * They are intentionally separate to keep persistence concerns out of game logic.
 * </p>
 * <p>
 * <b>Conversion direction:</b>
 * </p>
 * <ul>
 *   <li><b>model → hex:</b> On session load, {@link com.yisy.modules.doramcu.service.GameInitializationService}
 *       converts persisted JPA entities into in-memory hex domain objects.</li>
 *   <li><b>hex → model:</b> On session save, game state is serialized as JSON and stored
 *       in {@code PersistedGameSession.gameStateJson}.</li>
 * </ul>
 * <p>
 * <b>Rule:</b> Never reference {@code model.*} classes directly from {@code hex.*} classes.
 * All conversion happens in the service layer.
 * </p>
 */
package com.yisy.modules.doramcu.hex;
