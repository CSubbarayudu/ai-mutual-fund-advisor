package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Maps to {@code investor_holding} in PostgreSQL (holding_status + audit columns per schema.sql).
 */
@Entity
@Table(name = "investor_holding")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestorHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private InvestorProfile investor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private MutualFund fund;

    @Column(name = "holding_status", length = 20)
    private String holdingStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
