package com.yisy.modules.doramcu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * JPA entity representing a player in the 4X empire game.
 * <p>
 * Extends the basic User entity with 4X game-specific characteristics:
 * email for account management, login PIN for authentication, and empire
 * attributes (name, government type, ethos) for gameplay.
 * </p>
 */
@Entity
@Table(name = "players",
        indexes = {
                @Index(name = "idx_player_email", columnList = "email"),
                @Index(name = "idx_player_login_pin", columnList = "loginPin"),
                @Index(name = "idx_player_user_id", columnList = "userId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_player_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_player_login_pin", columnNames = "loginPin")
        })
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 6)
    private String loginPin;

    @Column(nullable = false)
    private String empireName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GovernmentType governmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ethos ethos;

    @ManyToOne
    private User user;

    /**
     * Whether this player has administrative rights to access the admin panel
     * and manage server configuration, galaxies, and users.
     */
    @Column(nullable = false)
    private boolean isAdmin = false;

    /**
     * Government types available for an Empire.
     * Based on Stellaris government mechanics.
     */
    public enum GovernmentType {
        DEMOCRACY,
        AUTHORITARIAN,
        MONARCHY,
        OLIGARCHY,
        THEOCRACY,
        DICTATORSHIP,
        CONFEDERATION,
        CORPORATE
    }

    /**
     * Ethos types that define the core values of an Empire.
     * Based on Stellaris ethos system.
     */
    public enum Ethos {
        MATERIALIST,
        SPIRITUALIST,
        LIBERTARIAN,
        EQUALIST,
        MILITARIST,
        PACIFIST
    }

    public Player() {
    }

    public Player(Long id, String email, String loginPin, String empireName,
                  GovernmentType governmentType, Ethos ethos) {
        this.id = id;
        this.email = email;
        this.loginPin = loginPin;
        this.empireName = empireName;
        this.governmentType = governmentType;
        this.ethos = ethos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginPin() {
        return loginPin;
    }

    public void setLoginPin(String loginPin) {
        this.loginPin = loginPin;
    }

    public String getEmpireName() {
        return empireName;
    }

    public void setEmpireName(String empireName) {
        this.empireName = empireName;
    }

    public GovernmentType getGovernmentType() {
        return governmentType;
    }

    public void setGovernmentType(GovernmentType governmentType) {
        this.governmentType = governmentType;
    }

    public Ethos getEthos() {
        return ethos;
    }

    public void setEthos(Ethos ethos) {
        this.ethos = ethos;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public String toString() {
        return "Player{id=" + id + ", email='" + email + "', empire='" + empireName +
                "', government=" + governmentType + ", ethos=" + ethos + ", admin=" + isAdmin + "}";
    }
}
