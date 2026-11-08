package com.yisy.modules.doramcu.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Arrays;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private static final String DEV_PROFILE = "dev";

    @Value("${JWT_SECRET:}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private final Environment environment;

    private volatile SecretKey signingKey;

    public JwtService(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        boolean isDevOrTest = Arrays.asList(environment.getActiveProfiles()).contains(DEV_PROFILE)
                || Arrays.asList(environment.getActiveProfiles()).contains("test")
                || environment.getActiveProfiles().length == 0
                || "true".equalsIgnoreCase(environment.getProperty("doramcu.development"));

        if (secret == null || secret.isBlank()) {
            if (isDevOrTest) {
                // Use a fixed default secret in dev/test mode with a warning
                secret = "dev-default-secret-change-me-in-production-1234567890";
                log.warn("JWT_SECRET is not set! Using default development/test secret. Set JWT_SECRET in production!");
            } else {
                // Fail fast in production
                throw new IllegalStateException(
                        "JWT_SECRET must be configured for production! Set the JWT_SECRET environment variable or property.");
            }
        }

        // Validate secret key length for HMAC-SHA256 (minimum 32 bytes / 256 bits recommended)
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            if (isDevOrTest) {
                log.warn("JWT_SECRET is shorter than 32 bytes ({} bytes). Use at least 32 bytes for production security.",
                        secret.getBytes(StandardCharsets.UTF_8).length);
            } else {
                throw new IllegalStateException(
                        "JWT_SECRET must be at least 32 bytes for production security! Current length: "
                                + secret.getBytes(StandardCharsets.UTF_8).length + " bytes.");
            }
        }

        // Validate JWT expiration is between 1 hour and 7 days
        if (expiration < 3_600_000 || expiration > 604_800_000) {
            throw new IllegalStateException(
                    "jwt.expiration must be between 1 hour (3600000ms) and 7 days (604800000ms). Current value: " + expiration);
        }

        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT service initialized successfully with expiration: {}ms", expiration);
    }

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            synchronized (this) {
                if (signingKey == null) {
                    signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        return signingKey;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
