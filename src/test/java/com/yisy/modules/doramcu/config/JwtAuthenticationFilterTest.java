package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    // ---- No Authorization header ----

    @Test
    @DisplayName("doFilterInternal - should pass through when no Authorization header")
    void doFilterInternal_shouldPassThroughWithoutAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("doFilterInternal - should pass through when Authorization header is not Bearer")
    void doFilterInternal_shouldPassThroughWithoutBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");
        when(request.getRequestURI()).thenReturn("/api/test");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
    }

    // ---- Valid JWT token ----

    @Test
    @DisplayName("doFilterInternal - should set authentication for valid token")
    void doFilterInternal_shouldSetAuthentication() throws ServletException, IOException {
        String token = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(jwtService.isTokenValid(token)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("testuser", auth.getName());
    }

    // ---- Invalid JWT token ----

    @Test
    @DisplayName("doFilterInternal - should not set authentication for invalid token")
    void doFilterInternal_shouldNotSetAuthenticationForInvalidToken() throws ServletException, IOException {
        String token = "invalid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ---- Exception handling ----

    @Test
    @DisplayName("doFilterInternal - should pass through on exception")
    void doFilterInternal_shouldPassThroughOnException() throws ServletException, IOException {
        String token = "bad-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Token parsing failed"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - should pass through when userDetailsService throws")
    void doFilterInternal_shouldPassThroughOnUserDetailsServiceError() throws ServletException, IOException {
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenThrow(new RuntimeException("DB error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // ---- Already authenticated ----

    @Test
    @DisplayName("doFilterInternal - should not override existing authentication")
    void doFilterInternal_shouldNotOverrideExistingAuth() throws ServletException, IOException {
        // Pre-set authentication
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("existing", "password");
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("newuser");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Should keep the existing authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("existing", auth.getName());
        verify(jwtService, never()).isTokenValid(anyString());
    }
}
