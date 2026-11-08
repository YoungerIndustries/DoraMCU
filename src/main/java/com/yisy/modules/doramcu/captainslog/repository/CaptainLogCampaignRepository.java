package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptainLogCampaignRepository extends JpaRepository<CaptainLogCampaign, Long> {
    List<CaptainLogCampaign> findByCharacterId(Long characterId);
    List<CaptainLogCampaign> findByCharacterIdAndStatus(Long characterId, CaptainLogCampaign.CampaignStatus status);
}
