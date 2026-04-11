package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.request.MarketEventIngestRequest;
import com.nihilent.mutualfund.advisor.dto.request.SectorImpactDto;
import com.nihilent.mutualfund.advisor.dto.response.MarketEventIngestResponse;
import com.nihilent.mutualfund.advisor.entity.MarketEvent;
import com.nihilent.mutualfund.advisor.entity.MarketEventSector;
import com.nihilent.mutualfund.advisor.entity.Recommendation;
import com.nihilent.mutualfund.advisor.entity.Sector;
import com.nihilent.mutualfund.advisor.repository.MarketEventRepository;
import com.nihilent.mutualfund.advisor.repository.MarketEventSectorRepository;
import com.nihilent.mutualfund.advisor.repository.RecommendationRepository;
import com.nihilent.mutualfund.advisor.repository.SectorRepository;
import com.nihilent.mutualfund.advisor.repository.UserAlertRepository;
import com.nihilent.mutualfund.advisor.service.MarketScoringService;
import com.nihilent.mutualfund.advisor.service.MarketEventIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketEventIngestionServiceImpl implements MarketEventIngestionService {

    private final MarketEventRepository marketEventRepository;
    private final MarketEventSectorRepository marketEventSectorRepository;
    private final SectorRepository sectorRepository;
    private final MarketScoringService marketScoringService;
    private final RecommendationRepository recommendationRepository;
    private final UserAlertRepository userAlertRepository;

    @Override
    @Transactional
    public MarketEventIngestResponse ingestMarketEvent(MarketEventIngestRequest request) {
        LocalDateTime ingestionStartTime = LocalDateTime.now();

        // STEP 1: Build and save MarketEvent
        // ⚠ MarketEvent has NO setImpactSeverity() — do NOT add it
        MarketEvent event = new MarketEvent();
        event.setEventTitle(request.getEventTitle());
        event.setEventDescription(request.getEventDescription());
        event.setImpactType(request.getImpactType());
        event.setImpactDuration(request.getImpactDuration()); // String field
        event.setCredibilityScore(request.getCredibilityScore());
        event.setSourceUrl(request.getSourceUrl());
        event.setEventDate(request.getEventDate());
        event.setExpiryDate(request.getExpiryDate());
        event.setDataQualityFlag("MCP_INGESTED");
        event.setCreatedAt(ingestionStartTime);
        MarketEvent savedEvent = marketEventRepository.save(event);
        log.info("MarketEvent saved: eventId={}, title={}",
            savedEvent.getEventId(), savedEvent.getEventTitle());

        // STEP 2: Resolve sectors, save MarketEventSector rows
        // ⚠ MarketEventSector.setImpactSeverity() — NOT setImpactScore()
        List<Long> resolvedSectorIds = new ArrayList<>();
        for (SectorImpactDto dto : request.getAffectedSectors()) {
            Sector sector = sectorRepository
                .findBySectorName(dto.getSectorName()).orElse(null);
            if (sector == null) {
                log.warn("Sector not found, skipping: {}", dto.getSectorName());
                continue;
            }
            MarketEventSector mes = new MarketEventSector();
            mes.setMarketEvent(savedEvent);
            mes.setSector(sector);
            mes.setImpactSeverity(dto.getImpactSeverity()); // ← correct field
            marketEventSectorRepository.save(mes);
            resolvedSectorIds.add(sector.getSectorId());
        }
        log.info("Sectors resolved: {}/{}", resolvedSectorIds.size(),
            request.getAffectedSectors().size());

        // STEP 3: Trigger scoring engine — handles alert generation internally
        marketScoringService.runFullMarketScoringCycle();
        log.info("Scoring engine triggered for event: {}", savedEvent.getEventTitle());

        // STEP 4: Count distinct affected funds
        int affectedFundsCount = 0;
        if (!resolvedSectorIds.isEmpty()) {
            List<Recommendation> affected = recommendationRepository
                .findActiveRecommendationsBySectorIds(resolvedSectorIds);
            affectedFundsCount = (int) affected.stream()
                .map(r -> r.getFund().getFundId())
                .distinct().count();
        }

        // STEP 5: Count alerts generated after ingestion started
        int alertsGeneratedCount = userAlertRepository
            .findByCreatedAtAfter(ingestionStartTime).size();

        log.info("Ingestion complete: eventId={}, funds={}, alerts={}",
            savedEvent.getEventId(), affectedFundsCount, alertsGeneratedCount);

        return MarketEventIngestResponse.builder()
            .eventId(savedEvent.getEventId())
            .eventTitle(savedEvent.getEventTitle())
            .impactType(savedEvent.getImpactType())
            .credibilityScore(savedEvent.getCredibilityScore())
            .affectedSectorsCount(resolvedSectorIds.size())
            .affectedFundsCount(affectedFundsCount)
            .alertsGeneratedCount(alertsGeneratedCount)
            .status("INGESTED_AND_SCORED")
            .ingestedAt(LocalDateTime.now())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketEventIngestResponse> getActiveMarketEvents() {
        return marketEventRepository
            .findByExpiryDateAfter(LocalDateTime.now())
            .stream()
            .map(e -> MarketEventIngestResponse.builder()
                .eventId(e.getEventId())
                .eventTitle(e.getEventTitle())
                .impactType(e.getImpactType())
                .credibilityScore(e.getCredibilityScore())
                .affectedSectorsCount(
                    marketEventSectorRepository
                        .findByMarketEventEventId(e.getEventId()).size())
                .affectedFundsCount(0)
                .alertsGeneratedCount(0)
                .status("ACTIVE")
                .ingestedAt(e.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MarketEventIngestResponse getMarketEventById(Long eventId) {
        MarketEvent event = marketEventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException(
                "MarketEvent not found: " + eventId));
        return MarketEventIngestResponse.builder()
            .eventId(event.getEventId())
            .eventTitle(event.getEventTitle())
            .impactType(event.getImpactType())
            .credibilityScore(event.getCredibilityScore())
            .affectedSectorsCount(
                marketEventSectorRepository
                    .findByMarketEventEventId(eventId).size())
            .affectedFundsCount(0)
            .alertsGeneratedCount(0)
            .status(LocalDateTime.now().isBefore(event.getExpiryDate())
                ? "ACTIVE" : "EXPIRED")
            .ingestedAt(event.getCreatedAt())
            .build();
    }
}
