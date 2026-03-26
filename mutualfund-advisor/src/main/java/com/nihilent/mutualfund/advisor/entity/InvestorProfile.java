package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.*;
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
    private Long investorId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer age;

    @Column(name = "annual_income")
    private BigDecimal annualIncome;

    private String occupation;

    @Column(name = "investment_goal")
    private String investmentGoal;

    @Column(name = "investment_horizon")
    private String investmentHorizon;

    @Column(name = "liquidity_preference")
    private String liquidityPreference;

    @Column(name = "investment_experience")
    private String investmentExperience;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}