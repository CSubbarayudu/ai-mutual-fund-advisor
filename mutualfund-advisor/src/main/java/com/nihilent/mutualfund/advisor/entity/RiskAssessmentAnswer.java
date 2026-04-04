package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessment_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private RiskAssessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private RiskQuestion question;

    @Column(name = "selected_option", length = 255)
    private String selectedOption;

    @Column(name = "option_score", nullable = false)
    private Integer optionScore;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
}
