package com.yisy.modules.doramcu.hex;

/**
 * Represents a body (celestial, fleet, or space station) positioned within a {@link SolarSystemEntity}.
 * <p>
 * This is a lightweight wrapper that associates any body object with a 3D hex position
 * and a {@link BodyType} enum value, enabling uniform spatial queries across all body types.
 */
public class SystemBody {

    private final Object body;
    private final Hex3D position;
    private final BodyType type;

    /**
     * The type of body contained in this entry.
     */
    public enum BodyType {
        STAR, PLANET, ASTEROID_BELT, GAS_GIANT, NEBULA, BLACK_HOLE, MOON, ASTEROID, COMET, FLEET, SPACE_STATION
    }

    /**
     * Creates a new system body entry.
     *
     * @param body the body object (must not be null)
     * @param position the 3D hex position in the solar system (must not be null)
     * @param type the type of the body
     * @throws IllegalArgumentException if body or position is null
     */
    public SystemBody(Object body, Hex3D position, BodyType type) {
        if (body == null) {
            throw new IllegalArgumentException("Body must not be null");
        }
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null");
        }
        this.body = body;
        this.position = position;
        this.type = type;
    }

    /**
     * @return the underlying body object
     */
    public Object getBody() {
        return body;
    }

    /**
     * Casts the body to the specified type.
     *
     * @param clazz the target class
     * @param <T> the type parameter
     * @return the body cast to the requested type
     * @throws ClassCastException if the body is not an instance of the requested type
     */
    @SuppressWarnings("unchecked")
    public <T> T getBodyAs(Class<T> clazz) {
        return clazz.cast(body);
    }

    /**
     * @return the 3D hex position of this body within the solar system
     */
    public Hex3D getPosition() {
        return position;
    }

    /**
     * @return the type of this body
     */
    public BodyType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("SystemBody{type=%s, position=%s, body=%s}", type, position, body);
    }
}
