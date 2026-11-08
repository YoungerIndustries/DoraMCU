package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogShipTalent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CaptainLogShipTalent entities.
 */
@Repository
public interface CaptainLogShipTalentRepository extends JpaRepository<CaptainLogShipTalent, Long> {

    /**
     * Finds all talents for a starship.
     */
    List<CaptainLogShipTalent> findByStarshipId(Long starshipId);

    /**
     * Finds all talents for a starship by name pattern.
     */
    List<CaptainLogShipTalent> findByStarshipIdAndNameContainingIgnoreCase(Long starshipId, String namePattern);
}
