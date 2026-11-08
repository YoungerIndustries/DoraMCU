package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogFocus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptainLogFocusRepository extends JpaRepository<CaptainLogFocus, Long> {

    List<CaptainLogFocus> findByCharacterId(Long characterId);

    void deleteByCharacterId(Long characterId);
}
