package com.yisy.modules.doramcu.shared.repository;

import com.yisy.modules.doramcu.shared.model.SharedCharacter;
import com.yisy.modules.doramcu.unified.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for shared characters that can travel between game systems.
 */
@Repository
public interface SharedCharacterRepository extends JpaRepository<SharedCharacter, Long> {

    List<SharedCharacter> findByNameContainingIgnoreCase(String name);

    Optional<SharedCharacter> findById(Long id);

    List<SharedCharacter> findByOriginSystem(GameType gameType);

    Optional<SharedCharacter> findByClCharacterId(Long clCharacterId);

    Optional<SharedCharacter> findByTravellerCharacterId(Integer travellerCharacterId);

    Optional<SharedCharacter> findByDoramcu4xPlayerId(Long doramcu4xPlayerId);
}
