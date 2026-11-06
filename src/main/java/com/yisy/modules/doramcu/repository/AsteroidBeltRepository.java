package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.AsteroidBelt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsteroidBeltRepository extends JpaRepository<AsteroidBelt, Long> {

    List<AsteroidBelt> findBySolarSystemId(Long solarSystemId);
}
