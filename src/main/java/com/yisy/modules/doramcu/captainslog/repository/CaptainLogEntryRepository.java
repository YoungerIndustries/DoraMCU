package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptainLogEntryRepository extends JpaRepository<CaptainLogEntry, Long> {

    List<CaptainLogEntry> findByMissionIdOrderByCreatedAtAsc(Long missionId);
}
