package com.nihilent.mutualfund.advisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fund_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private MutualFund fund;

    @Column(name = "doc_type", length = 50)
    private String docType;

    @Column(name = "content_summary", columnDefinition = "TEXT")
    private String contentSummary;
}
