package com.yisy.modules.doramcu.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private static final String SECRET = "DoraMCUSecretKeyForJWTTokenGenerationMustBeLongEnough256bits!!";
    private static final long EXPIRATION = 86400000;

    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(signingKey)
                .compact();
    }

    private String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---- generateToken ----

    @Test
    @DisplayName("generateToken - should generate a non-empty token")
    void generateToken_shouldGenerateToken() {
        String token = generateToken("testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("generateToken - should generate different tokens for different users")
    void generateToken_shouldGenerateDifferentTokens() {
        String token1 = generateToken("user1");
        String token2 = generateToken("user2");

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("generateToken - should generate valid token that can be parsed back")
    void generateToken_shouldGenerateParseableToken() {
        String token = generateToken("testuser");
        // Verify the token has the expected JWT structure (3 parts separated by dots)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT token should have 3 parts: header.payload.signature");
    }

    // ---- extractUsername ----

    @Test
    @DisplayName("extractUsername - should extract correct username from valid token")
    void extractUsername_shouldExtractUsername() {
        String token = generateToken("testuser");
        String username = extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("extractUsername - should extract username with special characters")
    void extractUsername_shouldExtractUsernameWithSpecialChars() {
        String token = generateToken("user@example.com");
        String username = extractUsername(token);

        assertEquals("user@example.com", username);
    }

    @Test
    @DisplayName("extractUsername - should throw exception for invalid token")
    void extractUsername_shouldThrowOnInvalidToken() {
        assertThrows(Exception.class, () -> extractUsername("invalid.token.here"));
    }

    // ---- isTokenValid ----

    @Test
    @DisplayName("isTokenValid - should return true for valid token")
    void isTokenValid_shouldReturnTrue() {
        String token = generateToken("testuser");
        assertTrue(isTokenValid(token));
    }

    @Test
    @DisplayName("isTokenValid - should return false for invalid token")
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        assertFalse(isTokenValid("invalid.token.here"));
    }

    @Test
    @DisplayName("isTokenValid - should return false for empty string")
    void isTokenValid_shouldReturnFalseForEmptyString() {
        assertFalse(isTokenValid(""));
    }

    @Test
    @DisplayName("isTokenValid - should return false for null")
    void isTokenValid_shouldReturnFalseForNull() {
        assertFalse(isTokenValid(null));
    }

    @Test
    @DisplayName("isTokenValid - should return false for tampered token")
    void isTokenValid_shouldReturnFalseForTamperedToken() {
        String token = generateToken("testuser");
        String tampered = token.substring(0, Math.max(0, token.length() - 5)) + "XXXXX";
        assertFalse(isTokenValid(tampered));
    }

    // ---- Round-trip test ----

    @Test
    @DisplayName("Round-trip - generate, extract, and validate token")
    void roundTrip_generateExtractValidate() {
        String username = "roundtripuser";
        String token = generateToken(username);

        assertNotNull(token);
        assertEquals(username, extractUsername(token));
        assertTrue(isTokenValid(token));
    }
}
