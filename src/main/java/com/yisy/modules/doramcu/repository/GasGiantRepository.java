package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.GasGiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GasGiantRepository extends JpaRepository<GasGiant, Long> {

    List<GasGiant> findBySolarSystemId(Long solarSystemId);
}
