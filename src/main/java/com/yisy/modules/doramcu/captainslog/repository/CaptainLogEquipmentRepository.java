package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptainLogEquipmentRepository extends JpaRepository<CaptainLogEquipment, Long> {
    List<CaptainLogEquipment> findByCharacterId(Long characterId);
    List<CaptainLogEquipment> findByCharacterIdAndActiveTrue(Long characterId);
    void deleteByCharacterId(Long characterId);
}
