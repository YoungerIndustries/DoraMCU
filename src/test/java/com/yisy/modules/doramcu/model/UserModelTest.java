package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Model Tests")
class UserModelTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Default constructor - should create empty user")
    void defaultConstructor_shouldCreateEmptyUser() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    @DisplayName("Parameterized constructor - should set all fields")
    void parameterizedConstructor_shouldSetAllFields() {
        User user = new User(1L, "admin", "encodedPassword");

        assertEquals(1L, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
    }

    // ---- Getters and Setters ----

    @Test
    @DisplayName("setId - should update id")
    void setId_shouldUpdateId() {
        User user = new User();
        user.setId(42L);

        assertEquals(42L, user.getId());
    }

    @Test
    @DisplayName("setUsername - should update username")
    void setUsername_shouldUpdateUsername() {
        User user = new User();
        user.setUsername("player1");

        assertEquals("player1", user.getUsername());
    }

    @Test
    @DisplayName("setPassword - should update password")
    void setPassword_shouldUpdatePassword() {
        User user = new User();
        user.setPassword("$2a$10$encoded");

        assertEquals("$2a$10$encoded", user.getPassword());
    }

    @Test
    @DisplayName("setUsername - should allow empty string")
    void setUsername_shouldAllowEmpty() {
        User user = new User();
        user.setUsername("");

        assertEquals("", user.getUsername());
    }

    // ---- Multiple updates ----

    @Test
    @DisplayName("Multiple setters - should update all fields")
    void multipleSetters_shouldUpdateAllFields() {
        User user = new User(1L, "old", "oldPass");
        user.setUsername("new");
        user.setPassword("newPass");

        assertEquals(1L, user.getId());
        assertEquals("new", user.getUsername());
        assertEquals("newPass", user.getPassword());
    }
}
