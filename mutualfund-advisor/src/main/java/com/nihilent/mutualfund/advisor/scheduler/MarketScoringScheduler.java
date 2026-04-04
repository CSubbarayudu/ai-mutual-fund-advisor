package com.nihilent.mutualfund.advisor.scheduler;

import com.nihilent.mutualfund.advisor.service.MarketScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketScoringScheduler {

    private final MarketScoringService marketScoringService;

    @Scheduled(fixedRateString = "${scoring.scheduler.rate:300000}")
    public void scheduledMarketScoringRun() {
        log.info("Scheduled scoring triggered at {}", LocalDateTime.now());
        marketScoringService.runFullMarketScoringCycle();
    }
}
