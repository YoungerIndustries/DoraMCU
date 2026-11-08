package com.yisy.modules.doramcu.captainslog.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a starship talent (special ability).
 * <p>
 * Ship talents provide special capabilities, similar to character talents in the 2d20 system.
 * </p>
 */
@Entity
@Table(name = "cl_ship_talents", indexes = {
        @Index(name = "idx_cl_ship_talent_starship", columnList = "starship_id")
})
public class CaptainLogShipTalent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "starship_id", nullable = false)
    private CaptainLogStarship starship;

    /** Name of the talent (e.g., "Advanced Sensors", "Shield Harmonics"). */
    @Column(nullable = false, length = 100)
    private String name;

    /** Description of what the talent does. */
    @Column(columnDefinition = "TEXT")
    private String description;

    public CaptainLogShipTalent() {
    }

    public CaptainLogShipTalent(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CaptainLogStarship getStarship() {
        return starship;
    }

    public void setStarship(CaptainLogStarship starship) {
        this.starship = starship;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CaptainLogShipTalent{name='" + name + "'}";
    }
}
