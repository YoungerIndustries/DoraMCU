package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Support both Authorization header and query parameter (for SSE EventSource which cannot set custom headers)
        String tokenFromHeader = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenFromHeader = authHeader.substring(7);
        }

        // Extract token from query parameter only - do NOT call getParameter() on POST/PUT requests
        // with JSON bodies, as it consumes the input stream and causes StreamReadException downstream.
        // Only parse the query string (URL), never the request body.
        String tokenFromQuery = null;
        String queryString = request.getQueryString();
        if (queryString != null && queryString.contains("token=")) {
            try {
                java.util.Map<String, String> params = parseQueryString(queryString);
                tokenFromQuery = params.get("token");
            } catch (Exception e) {
                log.debug("Failed to parse query string: {}", e.getMessage());
            }
        }

        if (tokenFromHeader == null && tokenFromQuery == null) {
            log.debug("No JWT token found in request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        jwt = tokenFromHeader != null ? tokenFromHeader : tokenFromQuery;
        try {
            username = jwtService.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt)) {
                    log.debug("JWT token validated for user: {}", username);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("JWT token is invalid for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private java.util.Map<String, String> parseQueryString(String queryString) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }
        for (String param : queryString.split("&")) {
            int eqIndex = param.indexOf('=');
            if (eqIndex > 0) {
                String key = java.net.URLDecoder.decode(param.substring(0, eqIndex), java.nio.charset.StandardCharsets.UTF_8);
                String value = java.net.URLDecoder.decode(param.substring(eqIndex + 1), java.nio.charset.StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }
}
