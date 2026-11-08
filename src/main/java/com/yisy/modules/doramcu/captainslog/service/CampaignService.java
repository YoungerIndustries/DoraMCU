package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.CreateCampaignRequest;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCampaignRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogMissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CampaignService {

    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);

    private final CaptainLogCampaignRepository campaignRepository;
    private final CaptainLogCharacterRepository characterRepository;
    private final CaptainLogMissionRepository missionRepository;

    public CampaignService(CaptainLogCampaignRepository campaignRepository,
                           CaptainLogCharacterRepository characterRepository,
                           CaptainLogMissionRepository missionRepository) {
        this.campaignRepository = campaignRepository;
        this.characterRepository = characterRepository;
        this.missionRepository = missionRepository;
    }

    @Transactional
    public CaptainLogCampaign createCampaign(Long characterId, CreateCampaignRequest request) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        CaptainLogCampaign campaign = new CaptainLogCampaign();
        campaign.setCharacter(character);
        campaign.setTitle(request.getTitle());
        campaign.setDescription(request.getDescription());
        campaign.setOngoingNpcs(request.getOngoingNpcs());
        campaign.setKeyLocations(request.getKeyLocations());
        campaign.setPlotThreads(request.getPlotThreads());
        campaign.setStatus(CaptainLogCampaign.CampaignStatus.ACTIVE);
        campaign.setCreatedAt(LocalDateTime.now());

        campaign = campaignRepository.save(campaign);
        log.info("Created campaign '{}' for character {}", campaign.getTitle(), character.getName());
        return campaign;
    }

    public Optional<CaptainLogCampaign> getCampaign(Long id) {
        return campaignRepository.findById(id);
    }

    public List<CaptainLogCampaign> listCampaigns(Long characterId) {
        return campaignRepository.findByCharacterId(characterId);
    }

    public List<CaptainLogCampaign> listActiveCampaigns(Long characterId) {
        return campaignRepository.findByCharacterIdAndStatus(characterId, CaptainLogCampaign.CampaignStatus.ACTIVE);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        campaignRepository.deleteById(id);
        log.info("Deleted campaign {}", id);
    }

    @Transactional
    public CaptainLogCampaign concludeCampaign(Long id) {
        CaptainLogCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + id));
        campaign.setStatus(CaptainLogCampaign.CampaignStatus.CONCLUDED);
        campaign.setConcludedAt(LocalDateTime.now());
        campaign = campaignRepository.save(campaign);
        log.info("Concluded campaign '{}'", campaign.getTitle());
        return campaign;
    }

    /**
     * Adds a mission to a campaign.
     */
    @Transactional
    public CaptainLogMission addMissionToCampaign(Long campaignId, Long missionId) {
        CaptainLogCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + campaignId));
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        mission.setCampaign(campaign);
        return missionRepository.save(mission);
    }

    /**
     * Removes a mission from its campaign.
     */
    @Transactional
    public CaptainLogMission removeMissionFromCampaign(Long missionId) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        mission.setCampaign(null);
        return missionRepository.save(mission);
    }

    /**
     * Performs a mission debrief — post-mission reflection that guides the player
     * through reflection questions and updates the mission's values and development fields.
     */
    @Transactional
    public Map<String, Object> debriefMission(Long missionId, Map<String, String> reflections) {
        CaptainLogMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

        // Extract reflection answers
        String valuesUsed = reflections.getOrDefault("valuesUsed", "");
        String valuesChallenged = reflections.getOrDefault("valuesChallenged", "");
        String lessonsLearned = reflections.getOrDefault("lessonsLearned", "");
        String characterGrowth = reflections.getOrDefault("characterGrowth", "");
        String retireValue = reflections.getOrDefault("retireValue", "");
        String newValue = reflections.getOrDefault("newValue", "");

        // Update mission with reflection data
        if (!valuesUsed.isEmpty()) {
            mission.setValuesUsed(valuesUsed);
        }
        if (!valuesChallenged.isEmpty()) {
            mission.setValuesChallenged(valuesChallenged);
        }

        // Build development note from reflections
        StringBuilder development = new StringBuilder();
        if (mission.getDevelopment() != null && !mission.getDevelopment().isEmpty()) {
            development.append(mission.getDevelopment()).append("\n\n");
        }
        if (!lessonsLearned.isEmpty()) {
            development.append("Lessons learned: ").append(lessonsLearned).append("\n");
        }
        if (!characterGrowth.isEmpty()) {
            development.append("Character growth: ").append(characterGrowth).append("\n");
        }
        if (!retireValue.isEmpty()) {
            development.append("Retired value: ").append(retireValue).append("\n");
        }
        if (!newValue.isEmpty()) {
            development.append("New value adopted: ").append(newValue).append("\n");
        }
        mission.setDevelopment(development.toString().trim());

        mission = missionRepository.save(mission);

        log.info("Debriefed mission '{}' for character {}", mission.getName(), mission.getCharacter().getName());

        Map<String, Object> result = new HashMap<>();
        result.put("missionId", missionId);
        result.put("missionName", mission.getName());
        result.put("valuesUsed", mission.getValuesUsed());
        result.put("valuesChallenged", mission.getValuesChallenged());
        result.put("development", mission.getDevelopment());
        result.put("description", "Mission debrief complete. Character reflection has been recorded.");
        return result;
    }
}
