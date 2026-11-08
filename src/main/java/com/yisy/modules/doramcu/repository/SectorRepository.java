package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findByGalaxyId(Long galaxyId);

    Sector findByGalaxyIdAndSectorRowAndSectorCol(Long galaxyId, int sectorRow, int sectorCol);
}
