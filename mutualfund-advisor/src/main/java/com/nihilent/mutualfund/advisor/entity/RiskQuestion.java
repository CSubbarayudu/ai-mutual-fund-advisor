package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_question")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(name = "question_text")
    private String questionText;

    private String category;
    private Integer weight;

    @Column(name = "active_flag")
    private Boolean activeFlag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}