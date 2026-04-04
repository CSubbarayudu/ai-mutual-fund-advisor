package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investor_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investor_id")
    private Long investorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "annual_income", precision = 15, scale = 2)
    private BigDecimal annualIncome;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "investment_goal", length = 50)
    private String investmentGoal;

    @Column(name = "investment_horizon", length = 30)
    private String investmentHorizon;

    @Column(name = "liquidity_preference", length = 30)
    private String liquidityPreference;

    @Column(name = "investment_experience", length = 30)
    private String investmentExperience;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
