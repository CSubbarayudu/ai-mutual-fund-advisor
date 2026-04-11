package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUser_UserIdOrderByAskedAtDesc(Long userId);
    List<ChatHistory> findByInvestor_InvestorIdOrderByAskedAtDesc(Long investorId);
}
