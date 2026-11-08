package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacterTalent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CaptainLogCharacterTalent entities.
 */
@Repository
public interface CaptainLogCharacterTalentRepository extends JpaRepository<CaptainLogCharacterTalent, Long> {

    /**
     * Finds all talents for a character.
     */
    List<CaptainLogCharacterTalent> findByCharacterId(Long characterId);

    /**
     * Finds all active talents for a character.
     */
    List<CaptainLogCharacterTalent> findByCharacterIdAndActiveTrue(Long characterId);
}
