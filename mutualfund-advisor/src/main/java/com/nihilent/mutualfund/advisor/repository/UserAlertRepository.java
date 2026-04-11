package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.UserAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserAlertRepository extends JpaRepository<UserAlert, Long> {

    Optional<UserAlert> findTopByUser_UserIdAndRecommendation_RecommendationIdOrderByCreatedAtDesc(
            Long userId, Long recommendationId);

    List<UserAlert> findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    List<UserAlert> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<UserAlert> findByUser_UserIdAndIsReadFalse(Long userId);

    boolean existsByUser_UserIdAndRecommendation_RecommendationIdAndIsReadFalse(
            Long userId, Long recommendationId);

    Optional<UserAlert> findByAlertIdAndUser_UserId(Long alertId, Long userId);

    List<UserAlert> findByCreatedAtAfter(LocalDateTime since);
}
