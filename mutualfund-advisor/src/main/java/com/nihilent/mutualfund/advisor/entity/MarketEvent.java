package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps to {@code market_event} in PostgreSQL. {@code impact_duration} is stored as VARCHAR per schema
 * (e.g. MEDIUM_TERM); {@code credibility_score} is DECIMAL(3,1).
 */
@Entity
@Table(name = "market_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_title", nullable = false, length = 255)
    private String eventTitle;

    @Column(name = "event_description", columnDefinition = "TEXT")
    private String eventDescription;

    @Column(name = "impact_type", nullable = false, length = 20)
    private String impactType;

    @Column(name = "credibility_score", precision = 3, scale = 1)
    private BigDecimal credibilityScore;

    @Column(name = "impact_duration", length = 20)
    private String impactDuration;

    @Column(name = "data_quality_flag", length = 20)
    private String dataQualityFlag;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
