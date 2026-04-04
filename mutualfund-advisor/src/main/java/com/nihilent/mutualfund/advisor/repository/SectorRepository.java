package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    Optional<Sector> findBySectorName(String sectorName);
}
