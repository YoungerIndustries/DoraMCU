package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.Moon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoonRepository extends JpaRepository<Moon, Long> {

    List<Moon> findBySolarSystemId(Long solarSystemId);

    List<Moon> findByParentPlanetId(Long planetId);
}
