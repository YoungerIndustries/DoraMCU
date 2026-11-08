package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptainLogCrewMemberRepository extends JpaRepository<CaptainLogCrewMember, Long> {

    /**
     * Finds all crew members for a given character.
     */
    List<CaptainLogCrewMember> findByCharacterId(Long characterId);

    /**
     * Finds active crew members for a given character.
     */
    List<CaptainLogCrewMember> findByCharacterIdAndActiveTrue(Long characterId);
}
