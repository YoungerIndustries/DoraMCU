package com.yisy.modules.doramcu.hex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic registry for managing a single type of body in a solar system.
 * <p>
 * Extracted from SolarSystemEntity to eliminate code duplication across 11 body types.
 * Each body type (Star, Planet, Asteroid, etc.) now uses one instance of this class
 * instead of duplicating add/get/remove/getAll/count methods.
 * </p>
 */
public class SolarSystemBodyRegistry<T> {

    private final Map<Integer, T> bodies = new HashMap<>();

    /**
     * Returns the number of bodies in this registry.
     *
     * @return the count
     */
    public int count() {
        return bodies.size();
    }

    /**
     * Adds a body to the registry.
     *
     * @param body the body to add
     * @return the previous body with this ID, or null
     */
    public T add(T body) {
        if (body == null) {
            throw new IllegalArgumentException("Body must not be null");
        }
        // Extract ID from common interface
        int id;
        if (body instanceof CelestialBody cb) {
            id = cb.getId();
        } else if (body instanceof Fleet f) {
            id = f.getId();
        } else if (body instanceof SpaceStation s) {
            id = s.getId();
        } else {
            throw new IllegalArgumentException("Unsupported body type: " + body.getClass().getName());
        }
        return bodies.put(id, body);
    }

    /**
     * Retrieves a body by its ID.
     *
     * @param id the unique identifier
     * @return the body, or null if not present
     */
    public T get(int id) {
        return bodies.get(id);
    }

    /**
     * Removes a body by its ID.
     *
     * @param id the unique identifier
     * @return the removed body, or null if not present
     */
    public T remove(int id) {
        return bodies.remove(id);
    }

    /**
     * Returns all bodies in the registry.
     *
     * @return unmodifiable map of ID to body
     */
    public Map<Integer, T> getAll() {
        return Collections.unmodifiableMap(bodies);
    }

    /**
     * Returns all body values.
     *
     * @return collection of all bodies
     */
    public java.util.Collection<T> values() {
        return bodies.values();
    }
}
