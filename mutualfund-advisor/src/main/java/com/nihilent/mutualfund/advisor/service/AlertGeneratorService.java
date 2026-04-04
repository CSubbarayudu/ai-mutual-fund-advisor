package com.nihilent.mutualfund.advisor.service;

import java.math.BigDecimal;

public interface AlertGeneratorService {

    void generateAlert(Long userId, Long recommendationId, Long eventId, BigDecimal scoreDrop);
}
