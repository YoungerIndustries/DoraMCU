package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.Asteroid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsteroidRepository extends JpaRepository<Asteroid, Long> {

    List<Asteroid> findBySolarSystemId(Long solarSystemId);

    long countBySolarSystemId(Long solarSystemId);
}
