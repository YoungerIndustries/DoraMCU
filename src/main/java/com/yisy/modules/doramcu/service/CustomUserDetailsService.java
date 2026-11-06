package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    public CustomUserDetailsService(UserRepository userRepository, PlayerRepository playerRepository) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for: {}", username);

        // First, try to find as a regular User
        UserDetails userDetails = userRepository.findByUsername(username)
                .map(user -> {
                    log.debug("User found: {}", username);
                    return org.springframework.security.core.userdetails.User
                            .withUsername(user.getUsername())
                            .password(user.getPassword())
                            .roles("USER")
                            .build();
                })
                .orElse(null);

        if (userDetails != null) {
            return userDetails;
        }

        // If not found as User, try to find as a Player (by email)
        return playerRepository.findByEmail(username)
                .map(player -> {
                    log.debug("Player found: {} (empire: {})", player.getEmail(), player.getEmpireName());
                    return org.springframework.security.core.userdetails.User
                            .withUsername(player.getEmail())
                            .password(player.getLoginPin())
                            .roles("PLAYER")
                            .build();
                })
                .orElseGet(() -> {
                    log.warn("User/Player not found in database: {}", username);
                    throw new UsernameNotFoundException("User not found: " + username);
                });
    }
}
