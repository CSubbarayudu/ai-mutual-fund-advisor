package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long recommendationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private InvestorProfile investor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private MutualFund fund;

    @Column(name = "base_match_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal baseMatchScore;

    @Column(name = "market_adjusted_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal marketAdjustedScore;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
    private String recommendationReason;

    @Column(name = "explanation_data", columnDefinition = "jsonb")
    private String explanationData;

    @Column(name = "recommendation_status", nullable = false, length = 20)
    private String recommendationStatus;

    @Column(name = "model_version", nullable = false, length = 20)
    private String modelVersion;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}
