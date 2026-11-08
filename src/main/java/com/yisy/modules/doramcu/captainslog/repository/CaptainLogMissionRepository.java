package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptainLogMissionRepository extends JpaRepository<CaptainLogMission, Long> {

    List<CaptainLogMission> findByCharacterIdOrderByMissionNumberAsc(Long characterId);

    List<CaptainLogMission> findByCharacterIdAndStatusOrderByMissionNumberAsc(Long characterId, MissionStatus status);
}
