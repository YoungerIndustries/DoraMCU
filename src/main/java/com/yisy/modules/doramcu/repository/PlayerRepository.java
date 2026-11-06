package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByEmail(String email);

    Optional<Player> findByLoginPin(String loginPin);

    /**
     * Find a player by their linked User entity.
     */
    Optional<Player> findByUser(User user);

    boolean existsByEmail(String email);

    boolean existsByLoginPin(String loginPin);

    /**
     * Fetch all players with their associated User entity in a single query.
     * Avoids N+1 problem when accessing player.getUser().
     */
    @EntityGraph(attributePaths = {"user"})
    @Override
    List<Player> findAll();

    /**
     * Fetch a player with their associated User entity in a single query.
     * Avoids N+1 problem when accessing player.getUser().
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<Player> findById(Long id);
}
