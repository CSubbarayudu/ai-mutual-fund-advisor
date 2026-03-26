package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.InvestorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorProfileRepository extends JpaRepository<InvestorProfile, Long> {
}
