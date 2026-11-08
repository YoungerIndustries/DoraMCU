package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaptainLogCharacterRepository extends JpaRepository<CaptainLogCharacter, Long> {

    List<CaptainLogCharacter> findByNameContainingIgnoreCase(String name);

    Optional<CaptainLogCharacter> findById(Long id);

    @Query("SELECT c FROM CaptainLogCharacter c LEFT JOIN FETCH c.focuses LEFT JOIN FETCH c.values WHERE c.id = :id")
    Optional<CaptainLogCharacter> findByIdWithEagerCollections(Long id);
}
