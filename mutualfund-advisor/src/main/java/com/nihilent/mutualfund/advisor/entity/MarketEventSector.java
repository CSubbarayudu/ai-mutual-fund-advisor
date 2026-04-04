package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Primary key column in PostgreSQL is {@code id} (see schema.sql).
 */
@Entity
@Table(name = "market_event_sector")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketEventSector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long marketEventSectorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private MarketEvent marketEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Column(name = "impact_severity", nullable = false, precision = 4, scale = 2)
    private BigDecimal impactSeverity;
}
