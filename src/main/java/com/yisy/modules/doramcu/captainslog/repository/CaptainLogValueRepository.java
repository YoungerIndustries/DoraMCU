package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptainLogValueRepository extends JpaRepository<CaptainLogValue, Long> {

    List<CaptainLogValue> findByCharacterId(Long characterId);

    void deleteByCharacterId(Long characterId);
}
