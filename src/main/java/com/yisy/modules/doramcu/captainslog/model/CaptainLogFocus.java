package com.yisy.modules.doramcu.captainslog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * JPA entity representing a focus (specialty) within a discipline.
 * <p>
 * In the 2d20 system, focuses provide a +1 bonus to specific types of checks
 * within a discipline (e.g., "Starship Tactics" within Command).
 * </p>
 */
@Entity
@Table(name = "cl_focuses", indexes = {
        @Index(name = "idx_cl_focus_character", columnList = "character_id")
})
public class CaptainLogFocus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private CaptainLogCharacter character;

    /** The discipline this focus belongs to. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Discipline discipline;

    /** Name of the focus (e.g., "Starship Tactics", "Xenobiology"). */
    @Column(nullable = false, length = 100)
    private String name;

    /** Disciplines available in the 2d20 system. */
    public enum Discipline {
        COMMAND, LEADERSHIP, SCIENCE, SECURITY, MEDICINE, OPERATIONS
    }

    public CaptainLogFocus() {
    }

    public CaptainLogFocus(Discipline discipline, String name) {
        this.discipline = discipline;
        this.name = name;
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

    public Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CaptainLogFocus{discipline=" + discipline + ", name='" + name + "'}";
    }
}
