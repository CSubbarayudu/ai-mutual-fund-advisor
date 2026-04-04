package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.entity.MarketEvent;
import com.nihilent.mutualfund.advisor.entity.Recommendation;
import com.nihilent.mutualfund.advisor.entity.UserAlert;
import com.nihilent.mutualfund.advisor.entity.Users;
import com.nihilent.mutualfund.advisor.repository.UserAlertRepository;
import com.nihilent.mutualfund.advisor.service.AlertGeneratorService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertGeneratorServiceImpl implements AlertGeneratorService {

    private final UserAlertRepository userAlertRepository;
    private final EntityManager entityManager;

    @Override
    public void generateAlert(Long userId, Long recommendationId, Long eventId, BigDecimal scoreDrop) {
        if (userAlertRepository.existsByUser_UserIdAndRecommendation_RecommendationIdAndIsReadFalse(
                userId, recommendationId)) {
            log.info("Duplicate alert skipped for userId={} recommendationId={}", userId, recommendationId);
            return;
        }

        if (eventId == null) {
            log.warn("Skipping alert: eventId is null for userId={} recommendationId={}", userId, recommendationId);
            return;
        }

        Users user = entityManager.getReference(Users.class, userId);
        Recommendation recommendation = entityManager.getReference(Recommendation.class, recommendationId);
        MarketEvent marketEvent = entityManager.getReference(MarketEvent.class, eventId);

        UserAlert alert = new UserAlert();
        alert.setUser(user);
        alert.setRecommendation(recommendation);
        alert.setEvent(marketEvent);
        alert.setAlertType("SCORE_DEGRADATION");
        alert.setAlertMessage(
                "Market events reduced fund score by "
                        + scoreDrop.setScale(1, RoundingMode.HALF_UP)
                        + " points.");

        String severity;
        if (scoreDrop.compareTo(BigDecimal.valueOf(25)) >= 0) {
            severity = "CRITICAL";
        } else if (scoreDrop.compareTo(BigDecimal.valueOf(15)) >= 0) {
            severity = "WARNING";
        } else {
            severity = "INFO";
        }
        alert.setSeverity(severity);
        alert.setIsRead(false);
        alert.setCreatedAt(LocalDateTime.now());

        userAlertRepository.save(alert);
        log.info(
                "Alert saved: userId={} recommendationId={} eventId={} severity={}",
                userId,
                recommendationId,
                eventId,
                severity);
    }
}
