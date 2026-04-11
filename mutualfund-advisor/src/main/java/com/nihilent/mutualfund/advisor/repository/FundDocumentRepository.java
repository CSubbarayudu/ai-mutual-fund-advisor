package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.FundDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FundDocumentRepository extends JpaRepository<FundDocument, Long> {
    List<FundDocument> findByFund_FundId(Long fundId);
}
