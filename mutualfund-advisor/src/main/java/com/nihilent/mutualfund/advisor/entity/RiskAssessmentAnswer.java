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
    private Long answerId;

    @ManyToOne
    @JoinColumn(name = "assessment_id")
    private RiskAssessment assessment;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private RiskQuestion question;

    private String selectedOption;

    private Integer optionScore;

    private LocalDateTime answeredAt;
}
