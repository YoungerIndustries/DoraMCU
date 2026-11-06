package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.Nebula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NebulaRepository extends JpaRepository<Nebula, Long> {

    List<Nebula> findBySolarSystemId(Long solarSystemId);
}
