package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.MarketEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketEventRepository extends JpaRepository<MarketEvent, Long> {

    List<MarketEvent> findByExpiryDateAfter(LocalDateTime now);

    List<MarketEvent> findByImpactTypeAndExpiryDateAfter(String impactType, LocalDateTime now);
}
