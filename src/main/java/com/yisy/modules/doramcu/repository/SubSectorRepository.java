package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.SubSector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubSectorRepository extends JpaRepository<SubSector, Long> {

    List<SubSector> findBySectorId(Long sectorId);

    @org.springframework.data.jpa.repository.Query("SELECT s.sectorRow, s.sectorCol, ss.subSectorRow, ss.subSectorCol, ss.name, s.tradeType " +
            "FROM SubSector ss JOIN ss.sector s WHERE s.galaxy.id = :galaxyId")
    java.util.List<Object[]> findNamesByGalaxyId(Long galaxyId);

    SubSector findBySectorIdAndSubSectorRowAndSubSectorCol(Long sectorId, int subSectorRow, int subSectorCol);
}
