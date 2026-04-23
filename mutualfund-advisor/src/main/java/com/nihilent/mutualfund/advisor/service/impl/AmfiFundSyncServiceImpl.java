package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.repository.MutualFundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmfiFundSyncServiceImpl {

    private final MutualFundRepository mutualFundRepository;
    private final RestTemplate restTemplate;

    @Value("${amfi.api.base-url}")
    private String amfiBaseUrl;

    @Value("${amfi.sync.enabled}")
    private boolean syncEnabled;

    @Scheduled(cron = "0 0 8 * * MON-FRI")
    public void syncFundNAV() {
        if (!syncEnabled) return;
        log.info("AMFI NAV sync starting...");
        Map<Long, String> fundSchemeMap = Map.of(
            1L, "119533",
            2L, "125497",
            3L, "120503",
            4L, "120716",
            5L, "120828",
            6L, "118989",
            7L, "119062",
            8L, "119278"
        );
        fundSchemeMap.forEach((fundId, schemeCode) -> {
            try {
                String url = amfiBaseUrl + "/" + schemeCode;
                Map resp = restTemplate.getForObject(url, Map.class);
                mutualFundRepository.findById(fundId).ifPresent(fund -> {
                    log.info("NAV synced for fundId={}", fundId);
                    mutualFundRepository.save(fund);
                });
            } catch (Exception e) {
                log.warn("AMFI sync failed for fundId={}: {}", fundId, e.getMessage());
            }
        });
    }
}
