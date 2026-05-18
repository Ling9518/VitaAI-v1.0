package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "research_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String abstractText;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('PAPER','CASE_STUDY','GUIDELINE','REVIEW')")
    private ResearchType researchType;

    @Column(columnDefinition = "TEXT")
    private String diseaseIds;

    @Column(length = 500)
    private String keywords;

    @Column(columnDefinition = "TEXT")
    private String authors;

    private LocalDate publicationDate;

    @Column(length = 200)
    private String journal;

    @Column(length = 100)
    private String doi;

    @Column(length = 500)
    private String attachmentUrl;

    @Builder.Default
    private Integer viewsCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING'")
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(length = 500)
    private String rejectReason;

    private LocalDateTime publishedAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum ResearchType { PAPER, CASE_STUDY, GUIDELINE, REVIEW }
    public enum ReviewStatus { PENDING, APPROVED, REJECTED }
}
