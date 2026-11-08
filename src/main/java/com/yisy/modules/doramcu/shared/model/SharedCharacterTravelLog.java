package com.yisy.modules.doramcu.shared.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a travel log entry when a character
 * travels between DoraMCU (Traveller) and Captain's Log systems.
 */
@Entity
@Table(name = "shared_character_travel_log", indexes = {
        @Index(name = "idx_travel_log_character", columnList = "character_id")
})
public class SharedCharacterTravelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private SharedCharacter character;

    @Column(name = "from_system", nullable = false, length = 20)
    private String fromSystem;

    @Column(name = "to_system", nullable = false, length = 20)
    private String toSystem;

    @Column(name = "travel_notes", columnDefinition = "TEXT")
    private String travelNotes;

    @Column(name = "traveled_at", nullable = false)
    private LocalDateTime traveledAt;

    public SharedCharacterTravelLog() {
    }

    public SharedCharacterTravelLog(SharedCharacter character, String fromSystem, String toSystem) {
        this.character = character;
        this.fromSystem = fromSystem;
        this.toSystem = toSystem;
        this.traveledAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SharedCharacter getCharacter() {
        return character;
    }

    public void setCharacter(SharedCharacter character) {
        this.character = character;
    }

    public String getFromSystem() {
        return fromSystem;
    }

    public void setFromSystem(String fromSystem) {
        this.fromSystem = fromSystem;
    }

    public String getToSystem() {
        return toSystem;
    }

    public void setToSystem(String toSystem) {
        this.toSystem = toSystem;
    }

    public String getTravelNotes() {
        return travelNotes;
    }

    public void setTravelNotes(String travelNotes) {
        this.travelNotes = travelNotes;
    }

    public LocalDateTime getTraveledAt() {
        return traveledAt;
    }

    public void setTraveledAt(LocalDateTime traveledAt) {
        this.traveledAt = traveledAt;
    }
}
