package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.FundSectorAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FundSectorAllocationRepository extends JpaRepository<FundSectorAllocation, Long> {

    List<FundSectorAllocation> findByFund_FundId(Long fundId);
}
