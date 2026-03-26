package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}
