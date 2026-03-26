package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assessmentId;

    @ManyToOne
    @JoinColumn(name = "investor_id")
    private InvestorProfile investor;

    private Integer totalScore;

    private String riskLevel;

    private LocalDateTime assessedAt;

    private String remarks;

    private String modelVersion;
}
