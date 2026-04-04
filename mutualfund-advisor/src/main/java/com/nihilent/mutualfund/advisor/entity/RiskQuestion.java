package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_question")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "active_flag", nullable = false)
    private Boolean activeFlag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
