package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.InvestorHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestorHoldingRepository extends JpaRepository<InvestorHolding, Long> {

    List<InvestorHolding> findByInvestor_InvestorId(Long investorId);
}
