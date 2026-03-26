package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mutual_fund")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutualFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fundId;

    private String fundName;
    private String amcName;
    private String category;
    private String riskLevel;

    private BigDecimal expenseRatio;

    @Column(name = "return_1y")
    private BigDecimal return1y;

    @Column(name = "return_3y")
    private BigDecimal return3y;

    private BigDecimal minimumInvestment;
    private String investmentHorizon;

    private String fundStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
