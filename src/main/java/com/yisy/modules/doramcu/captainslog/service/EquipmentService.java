package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.CreateEquipmentRequest;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogEquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentService.class);

    private final CaptainLogEquipmentRepository equipmentRepository;
    private final CaptainLogCharacterRepository characterRepository;

    public EquipmentService(CaptainLogEquipmentRepository equipmentRepository,
                            CaptainLogCharacterRepository characterRepository) {
        this.equipmentRepository = equipmentRepository;
        this.characterRepository = characterRepository;
    }

    @Transactional
    public CaptainLogEquipment createEquipment(Long characterId, CreateEquipmentRequest request) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        CaptainLogEquipment equipment = new CaptainLogEquipment();
        equipment.setCharacter(character);
        equipment.setName(request.getName());
        equipment.setDescription(request.getDescription());
        equipment.setCategory(request.getCategory());
        equipment.setBoonOn(request.getBoonOn());
        equipment.setActive(true);

        equipment = equipmentRepository.save(equipment);
        log.info("Created equipment '{}' for character {}", equipment.getName(), character.getName());
        return equipment;
    }

    public Optional<CaptainLogEquipment> getEquipment(Long id) {
        return equipmentRepository.findById(id);
    }

    public List<CaptainLogEquipment> listEquipment(Long characterId) {
        return equipmentRepository.findByCharacterId(characterId);
    }

    public List<CaptainLogEquipment> listActiveEquipment(Long characterId) {
        return equipmentRepository.findByCharacterIdAndActiveTrue(characterId);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
        log.info("Deleted equipment {}", id);
    }

    @Transactional
    public CaptainLogEquipment toggleActive(Long id) {
        CaptainLogEquipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + id));
        equipment.setActive(!equipment.isActive());
        equipment = equipmentRepository.save(equipment);
        log.info("Toggled equipment {} active status to {}", equipment.getName(), equipment.isActive());
        return equipment;
    }
}
