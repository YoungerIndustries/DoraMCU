package com.yisy.modules.doramcu.captainslog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * JPA entity representing a character value (moral compass).
 * <p>
 * Characters have up to 3 core values that guide their decisions.
 * Using or challenging these values during missions generates narrative tension.
 * </p>
 */
@Entity
@Table(name = "cl_values", indexes = {
        @Index(name = "idx_cl_value_character", columnList = "character_id")
})
public class CaptainLogValue {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private CaptainLogCharacter character;

    /** The value description (e.g., "Protect the innocent", "Seek knowledge"). */
    @Column(nullable = false, length = 200)
    private String description;

    /** Whether this value was used during a mission. */
    @Column(nullable = false)
    private boolean used = false;

    /** Whether this value was challenged during a mission. */
    @Column(nullable = false)
    private boolean challenged = false;

    public CaptainLogValue() {
    }

    public CaptainLogValue(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CaptainLogCharacter getCharacter() {
        return character;
    }

    public void setCharacter(CaptainLogCharacter character) {
        this.character = character;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isChallenged() {
        return challenged;
    }

    public void setChallenged(boolean challenged) {
        this.challenged = challenged;
    }

    @Override
    public String toString() {
        return "CaptainLogValue{description='" + description + "'}";
    }
}
