package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Query("""
            SELECT r FROM Recommendation r
            JOIN FETCH r.fund f
            JOIN FETCH f.sectorAllocations sa
            JOIN FETCH sa.sector s
            WHERE r.recommendationStatus = 'ACTIVE'
            """)
    List<Recommendation> findAllActiveWithFundAndSectors();

    Optional<Recommendation> findByInvestor_InvestorIdAndFund_FundId(Long investorId, Long fundId);

    List<Recommendation> findByInvestor_InvestorIdAndRecommendationStatus(Long investorId, String status);
}
