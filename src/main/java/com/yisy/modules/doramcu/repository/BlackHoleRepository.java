package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.BlackHole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlackHoleRepository extends JpaRepository<BlackHole, Long> {

    List<BlackHole> findBySolarSystemId(Long solarSystemId);
}
