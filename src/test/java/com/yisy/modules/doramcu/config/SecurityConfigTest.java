package com.yisy.modules.doramcu.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        // SecurityConfig requires JwtAuthenticationFilter, mock it
        securityConfig = new SecurityConfig(org.mockito.Mockito.mock(JwtAuthenticationFilter.class));
    }

    // ---- passwordEncoder ----

    @Test
    @DisplayName("passwordEncoder - should return BCryptPasswordEncoder")
    void passwordEncoder_shouldReturnBCrypt() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder instanceof org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder);
    }

    @Test
    @DisplayName("passwordEncoder - should encode passwords correctly")
    void passwordEncoder_shouldEncodePassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "test123";
        String encoded = encoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$") || encoded.startsWith("$2y$"));
    }

    @Test
    @DisplayName("passwordEncoder - should match encoded passwords")
    void passwordEncoder_shouldMatchPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "test123";
        String encoded = encoder.encode(rawPassword);

        assertTrue(encoder.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("passwordEncoder - should not match wrong passwords")
    void passwordEncoder_shouldNotMatchWrongPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "test123";
        String encoded = encoder.encode(rawPassword);

        assertFalse(encoder.matches("wrongPassword", encoded));
    }

    @Test
    @DisplayName("passwordEncoder - should produce different encodings each time")
    void passwordEncoder_shouldProduceDifferentEncodings() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "test123";
        String encoded1 = encoder.encode(rawPassword);
        String encoded2 = encoder.encode(rawPassword);

        // BCrypt uses salt, so encodings should differ
        assertNotEquals(encoded1, encoded2);
        // But both should match the raw password
        assertTrue(encoder.matches(rawPassword, encoded1));
        assertTrue(encoder.matches(rawPassword, encoded2));
    }
}
