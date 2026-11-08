package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.CreateCrewMemberRequest;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCrewMember;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCrewMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing crew members in the Captain's Log Solo RPG system.
 * <p>
 * Crew members are NPCs who serve on the character's starship and can assist
 * with task attempts in their area of expertise.
 * </p>
 */
@Component
public class CaptainLogCrewService {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogCrewService.class);

    private final CaptainLogCrewMemberRepository crewMemberRepository;
    private final CaptainLogCharacterRepository characterRepository;

    public CaptainLogCrewService(CaptainLogCrewMemberRepository crewMemberRepository,
                                  CaptainLogCharacterRepository characterRepository) {
        this.crewMemberRepository = crewMemberRepository;
        this.characterRepository = characterRepository;
    }

    /**
     * Creates a new crew member for a character.
     */
    @Transactional
    public CaptainLogCrewMember createCrewMember(Long characterId, CreateCrewMemberRequest request) {
        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        CaptainLogCrewMember crewMember = new CaptainLogCrewMember();
        crewMember.setName(request.getName());
        crewMember.setSpecies(request.getSpecies());
        crewMember.setRole(request.getRole());
        crewMember.setPrimaryDiscipline(request.getPrimaryDiscipline());
        crewMember.setSecondaryDiscipline(request.getSecondaryDiscipline());
        crewMember.setExpertiseRating(request.getExpertiseRating() != null ? request.getExpertiseRating() : 1);
        crewMember.setRelationship(request.getRelationship());
        crewMember.setDescription(request.getDescription());
        crewMember.setCharacter(character);

        CaptainLogCrewMember saved = crewMemberRepository.save(crewMember);
        log.info("Created crew member {} for character {}", saved.getName(), characterId);
        return saved;
    }

    /**
     * Retrieves a crew member by ID.
     */
    public Optional<CaptainLogCrewMember> getCrewMember(Long id) {
        return crewMemberRepository.findById(id);
    }

    /**
     * Lists all crew members for a character.
     */
    public List<CaptainLogCrewMember> listCrewMembers(Long characterId) {
        return crewMemberRepository.findByCharacterId(characterId);
    }

    /**
     * Lists only active crew members for a character.
     */
    public List<CaptainLogCrewMember> listActiveCrewMembers(Long characterId) {
        return crewMemberRepository.findByCharacterIdAndActiveTrue(characterId);
    }

    /**
     * Deletes a crew member.
     */
    @Transactional
    public void deleteCrewMember(Long id) {
        crewMemberRepository.deleteById(id);
        log.info("Deleted crew member: {}", id);
    }

    /**
     * Toggles a crew member's active status.
     */
    @Transactional
    public CaptainLogCrewMember toggleActive(Long id) {
        CaptainLogCrewMember crewMember = crewMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Crew member not found: " + id));
        crewMember.setActive(!crewMember.isActive());
        return crewMemberRepository.save(crewMember);
    }
}
