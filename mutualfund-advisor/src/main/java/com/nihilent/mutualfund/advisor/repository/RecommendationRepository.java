package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT r FROM Recommendation r " +
           "JOIN FETCH r.fund f " +
           "JOIN FETCH r.investor i " +
           "WHERE i.investorId = :investorId " +
           "ORDER BY r.marketAdjustedScore DESC")
    List<Recommendation> findByInvestor_InvestorIdOrderByMarketAdjustedScoreDesc(
            @Param("investorId") Long investorId);

    @Query("SELECT DISTINCT r FROM Recommendation r " +
           "JOIN r.fund f " +
           "JOIN f.sectorAllocations sa " +
           "WHERE r.recommendationStatus = 'ACTIVE' " +
           "AND sa.sector.sectorId IN :sectorIds")
    List<Recommendation> findActiveRecommendationsBySectorIds(
            @Param("sectorIds") List<Long> sectorIds);
}
