package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.MarketEventSector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketEventSectorRepository extends JpaRepository<MarketEventSector, Long> {

    List<MarketEventSector> findBySector_SectorId(Long sectorId);

    List<MarketEventSector> findByMarketEventEventId(Long eventId);
}
