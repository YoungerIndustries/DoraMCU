package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.SolarSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolarSystemRepository extends JpaRepository<SolarSystem, Long> {

    List<SolarSystem> findByGalaxyId(Long galaxyId);

    List<SolarSystem> findByGalaxyIdOrderBySectorRowAscSectorColAsc(Long galaxyId);

    List<SolarSystem> findBySectorRowAndSectorCol(int sectorRow, int sectorCol);

    @org.springframework.data.jpa.repository.Query("SELECT ss.id as id, ss.subSectorRow as subSectorRow, ss.subSectorCol as subSectorCol, " +
            "CASE WHEN s.id IS NOT NULL THEN 1 ELSE 0 END as starCount, " +
            "COUNT(DISTINCT p.id) as planetCount, " +
            "COUNT(DISTINCT m.id) as moonCount, " +
            "COUNT(DISTINCT ab.id) as asteroidBeltCount, " +
            "COUNT(DISTINCT gg.id) as gasGiantCount, " +
            "COUNT(DISTINCT n.id) as nebulaCount, " +
            "COUNT(DISTINCT bh.id) as blackHoleCount, " +
            "COUNT(DISTINCT c.id) as cometCount, " +
            "COUNT(DISTINCT ss2.id) as spaceStationCount, " +
            "COUNT(DISTINCT a.id) as asteroidCount " +
            "FROM SolarSystem ss " +
            "LEFT JOIN ss.star s " +
            "LEFT JOIN ss.planets p " +
            "LEFT JOIN ss.moons m " +
            "LEFT JOIN ss.asteroidBelts ab " +
            "LEFT JOIN ss.gasGiants gg " +
            "LEFT JOIN ss.nebulas n " +
            "LEFT JOIN ss.blackHoles bh " +
            "LEFT JOIN ss.comets c " +
            "LEFT JOIN ss.spaceStations ss2 " +
            "LEFT JOIN ss.asteroids a " +
            "WHERE ss.sectorRow = :sectorRow AND ss.sectorCol = :sectorCol " +
            "GROUP BY ss.id, ss.subSectorRow, ss.subSectorCol, s.id")
    java.util.List<Object[]> findBySectorRowAndSectorColCounts(
            @org.springframework.data.repository.query.Param("sectorRow") int sectorRow,
            @org.springframework.data.repository.query.Param("sectorCol") int sectorCol);

    List<SolarSystem> findBySectorRowAndSectorColAndSubSectorRowAndSubSectorCol(
            int sectorRow, int sectorCol, int subSectorRow, int subSectorCol);

    long countByGalaxyId(Long galaxyId);

    long countBySectorRowAndSectorCol(int sectorRow, int sectorCol);

    long countBySectorRowAndSectorColAndSubSectorRowAndSubSectorCol(
            int sectorRow, int sectorCol, int subSectorRow, int subSectorCol);

    @org.springframework.data.jpa.repository.Query("SELECT ss.id as id, ss.sectorRow as sectorRow, ss.sectorCol as sectorCol, " +
            "ss.subSectorRow as subSectorRow, ss.subSectorCol as subSectorCol, " +
            "CASE WHEN s.id IS NOT NULL THEN 1 ELSE 0 END as starCount, " +
            "COUNT(DISTINCT p.id) as planetCount, " +
            "COUNT(DISTINCT m.id) as moonCount, " +
            "COUNT(DISTINCT ab.id) as asteroidBeltCount, " +
            "COUNT(DISTINCT gg.id) as gasGiantCount, " +
            "COUNT(DISTINCT n.id) as nebulaCount, " +
            "COUNT(DISTINCT bh.id) as blackHoleCount, " +
            "COUNT(DISTINCT c.id) as cometCount, " +
            "COUNT(DISTINCT ss2.id) as spaceStationCount, " +
            "COUNT(DISTINCT a.id) as asteroidCount " +
            "FROM SolarSystem ss " +
            "LEFT JOIN ss.star s " +
            "LEFT JOIN ss.planets p " +
            "LEFT JOIN ss.moons m " +
            "LEFT JOIN ss.asteroidBelts ab " +
            "LEFT JOIN ss.gasGiants gg " +
            "LEFT JOIN ss.nebulas n " +
            "LEFT JOIN ss.blackHoles bh " +
            "LEFT JOIN ss.comets c " +
            "LEFT JOIN ss.spaceStations ss2 " +
            "LEFT JOIN ss.asteroids a " +
            "GROUP BY ss.id, ss.sectorRow, ss.sectorCol, ss.subSectorRow, ss.subSectorCol, s.id")
    java.util.List<Object[]> findAllWithCounts();

    // Enhanced query that returns system names, positions, and planet ownership data
    @org.springframework.data.jpa.repository.Query("SELECT ss.id as id, ss.name as name, ss.sectorRow as sectorRow, ss.sectorCol as sectorCol, " +
            "ss.subSectorRow as subSectorRow, ss.subSectorCol as subSectorCol, ss.posX as posX, ss.posY as posY, ss.posZ as posZ, " +
            "s.name as starName, " +
            "CASE WHEN s.id IS NOT NULL THEN 1 ELSE 0 END as starCount, " +
            "COUNT(DISTINCT p.id) as planetCount, " +
            "COUNT(DISTINCT m.id) as moonCount, " +
            "COUNT(DISTINCT ab.id) as asteroidBeltCount, " +
            "COUNT(DISTINCT gg.id) as gasGiantCount, " +
            "COUNT(DISTINCT n.id) as nebulaCount, " +
            "COUNT(DISTINCT bh.id) as blackHoleCount, " +
            "COUNT(DISTINCT c.id) as cometCount, " +
            "COUNT(DISTINCT ss2.id) as spaceStationCount, " +
            "COUNT(DISTINCT a.id) as asteroidCount, " +
            "p.name as planetName, p.planetSize as planetSize, p.ownerEmpireId as ownerEmpireId " +
            "FROM SolarSystem ss " +
            "LEFT JOIN ss.star s " +
            "LEFT JOIN ss.planets p " +
            "LEFT JOIN ss.moons m " +
            "LEFT JOIN ss.asteroidBelts ab " +
            "LEFT JOIN ss.gasGiants gg " +
            "LEFT JOIN ss.nebulas n " +
            "LEFT JOIN ss.blackHoles bh " +
            "LEFT JOIN ss.comets c " +
            "LEFT JOIN ss.spaceStations ss2 " +
            "LEFT JOIN ss.asteroids a " +
            "GROUP BY ss.id, ss.name, ss.sectorRow, ss.sectorCol, ss.subSectorRow, ss.subSectorCol, " +
            "ss.posX, ss.posY, ss.posZ, s.name, p.name, p.planetSize, p.ownerEmpireId")
    java.util.List<Object[]> findAllWithRichData();

    // Query for a specific system by ID (same columns as findAllWithRichData)
    @org.springframework.data.jpa.repository.Query("SELECT ss.id as id, ss.name as name, ss.sectorRow as sectorRow, ss.sectorCol as sectorCol, " +
            "ss.subSectorRow as subSectorRow, ss.subSectorCol as subSectorCol, ss.posX as posX, ss.posY as posY, ss.posZ as posZ, " +
            "s.name as starName, " +
            "CASE WHEN s.id IS NOT NULL THEN 1 ELSE 0 END as starCount, " +
            "COUNT(DISTINCT p.id) as planetCount, " +
            "COUNT(DISTINCT m.id) as moonCount, " +
            "COUNT(DISTINCT ab.id) as asteroidBeltCount, " +
            "COUNT(DISTINCT gg.id) as gasGiantCount, " +
            "COUNT(DISTINCT n.id) as nebulaCount, " +
            "COUNT(DISTINCT bh.id) as blackHoleCount, " +
            "COUNT(DISTINCT c.id) as cometCount, " +
            "COUNT(DISTINCT ss2.id) as spaceStationCount, " +
            "COUNT(DISTINCT a.id) as asteroidCount, " +
            "p.name as planetName, p.planetSize as planetSize, p.ownerEmpireId as ownerEmpireId " +
            "FROM SolarSystem ss " +
            "LEFT JOIN ss.star s " +
            "LEFT JOIN ss.planets p " +
            "LEFT JOIN ss.moons m " +
            "LEFT JOIN ss.asteroidBelts ab " +
            "LEFT JOIN ss.gasGiants gg " +
            "LEFT JOIN ss.nebulas n " +
            "LEFT JOIN ss.blackHoles bh " +
            "LEFT JOIN ss.comets c " +
            "LEFT JOIN ss.spaceStations ss2 " +
            "LEFT JOIN ss.asteroids a " +
            "WHERE ss.id = :systemId " +
            "GROUP BY ss.id, ss.name, ss.sectorRow, ss.sectorCol, ss.subSectorRow, ss.subSectorCol, " +
            "ss.posX, ss.posY, ss.posZ, s.name, p.name, p.planetSize, p.ownerEmpireId")
    java.util.List<Object[]> findSystemDetailsById(@org.springframework.data.repository.query.Param("systemId") Long systemId);

    // Count systems per subsector for summary mode (avoids expensive rich data query)
    @org.springframework.data.jpa.repository.Query("SELECT CONCAT(ss.sectorRow, '-', ss.sectorCol, '-', ss.subSectorRow, '-', ss.subSectorCol) as key, COUNT(ss.id) as count " +
            "FROM SolarSystem ss WHERE ss.galaxy.id = :galaxyId " +
            "GROUP BY ss.sectorRow, ss.sectorCol, ss.subSectorRow, ss.subSectorCol")
    java.util.List<Object[]> countSystemsBySubSector(@org.springframework.data.repository.query.Param("galaxyId") Long galaxyId);
}
