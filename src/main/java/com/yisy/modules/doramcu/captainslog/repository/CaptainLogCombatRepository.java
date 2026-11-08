package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCombat;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCombat.CombatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptainLogCombatRepository extends JpaRepository<CaptainLogCombat, Long> {

    List<CaptainLogCombat> findByMissionIdOrderByCreatedAtDesc(Long missionId);

    List<CaptainLogCombat> findByMissionIdAndStatusOrderByCreatedAtDesc(Long missionId, CombatStatus status);
}
