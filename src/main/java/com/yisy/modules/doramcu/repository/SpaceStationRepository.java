package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.SpaceStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceStationRepository extends JpaRepository<SpaceStation, Long> {

    List<SpaceStation> findBySolarSystemId(Long solarSystemId);
}
