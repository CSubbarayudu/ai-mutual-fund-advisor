package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.MutualFund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MutualFundRepository extends JpaRepository<MutualFund, Long> {
}
