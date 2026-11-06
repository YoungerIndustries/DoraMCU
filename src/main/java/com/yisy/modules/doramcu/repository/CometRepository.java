package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.Comet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CometRepository extends JpaRepository<Comet, Long> {

    List<Comet> findBySolarSystemId(Long solarSystemId);
}
