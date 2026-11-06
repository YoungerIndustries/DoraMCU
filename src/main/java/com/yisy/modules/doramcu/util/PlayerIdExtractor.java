package com.yisy.modules.doramcu.util;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Utility component that extracts a stable player ID from an authenticated user.
 * <p>
 * If the username is numeric, it is parsed directly. Otherwise, a stable hash
 * is computed so the same user always receives the same player ID across requests.
 * </p>
 */
@Component
public class PlayerIdExtractor {

    /**
     * Extract a player ID from the current authentication.
     *
     * @param authentication the Spring Security authentication object
     * @return a stable player ID derived from the username
     */
    public Long extract(Authentication authentication) {
        String username = authentication.getName();
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            // For non-numeric usernames (like "admin"), use a stable hash
            // so the same user always gets the same player ID across requests
            return Math.abs((long) username.hashCode()) % 100000L;
        }
    }
}
