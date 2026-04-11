package com.nihilent.mutualfund.advisor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketEventIngestResponse {
    private Long eventId;
    private String eventTitle;
    private String impactType;
    private BigDecimal credibilityScore;
    private Integer affectedSectorsCount;
    private Integer affectedFundsCount;
    private Integer alertsGeneratedCount;
    private String status;            // INGESTED_AND_SCORED | ACTIVE | EXPIRED
    private LocalDateTime ingestedAt;
}
