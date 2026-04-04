package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mutual_fund")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutualFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fund_id")
    private Long fundId;

    @Column(name = "fund_name", nullable = false, length = 200)
    private String fundName;

    @Column(name = "amc_name", nullable = false, length = 150)
    private String amcName;

    @Column(name = "category", length = 80)
    private String category;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "expense_ratio", precision = 5, scale = 3)
    private BigDecimal expenseRatio;

    @Column(name = "return_1y", precision = 6, scale = 2)
    private BigDecimal return1y;

    @Column(name = "return_3y", precision = 6, scale = 2)
    private BigDecimal return3y;

    @Column(name = "minimum_investment", precision = 12, scale = 2)
    private BigDecimal minimumInvestment;

    @Column(name = "investment_horizon", length = 30)
    private String investmentHorizon;

    @Column(name = "fund_status", nullable = false, length = 20)
    private String fundStatus;

    @Column(name = "volatility_score", precision = 4, scale = 2)
    private BigDecimal volatilityScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "fund", fetch = FetchType.LAZY)
    private List<FundSectorAllocation> sectorAllocations = new ArrayList<>();
}
