package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptainLogStarshipRepository extends JpaRepository<CaptainLogStarship, Long> {

    List<CaptainLogStarship> findByNameContainingIgnoreCase(String name);
}
