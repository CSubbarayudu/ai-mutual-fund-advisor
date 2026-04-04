package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    Optional<RiskAssessment> findTopByInvestor_InvestorIdOrderByAssessedAtDesc(Long investorId);
}
