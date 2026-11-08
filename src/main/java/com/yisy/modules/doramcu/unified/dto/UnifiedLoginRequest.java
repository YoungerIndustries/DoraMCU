package com.yisy.modules.doramcu.unified.dto;

import com.yisy.modules.doramcu.unified.GameType;

/**
 * Request to log in using the unified character system.
 * <p>
 * The player can provide any combination of:
 * </p>
 * <ul>
 *   <li>First and last name of a character</li>
 *   <li>Email address to list all characters for that player</li>
 * </ul>
 * <p>
 * If no search criteria are provided, the response will include a prompt
 * for the player to enter their information.
 * </p>
 */
public class UnifiedLoginRequest {

    private String firstName;
    private String lastName;
    private String email;

    public UnifiedLoginRequest() {
    }

    public UnifiedLoginRequest(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns true if any search criteria are provided.
     */
    public boolean hasSearchCriteria() {
        return (firstName != null && !firstName.isBlank()) ||
               (lastName != null && !lastName.isBlank()) ||
               (email != null && !email.isBlank());
    }

    /**
     * Returns the full name if both first and last are provided.
     */
    public String getFullName() {
        if (firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank()) {
            return firstName.trim() + " " + lastName.trim();
        }
        if (firstName != null && !firstName.isBlank()) {
            return firstName.trim();
        }
        if (lastName != null && !lastName.isBlank()) {
            return lastName.trim();
        }
        return null;
    }
}
