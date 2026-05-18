package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('DISEASE','DRUG','SKILL','RESEARCH','CORRECTION')")
    private ContentType contentType;

    @Column(nullable = false)
    private Long contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_id", nullable = false)
    private User submitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('APPROVE','REJECT','MODIFY','RETURN')")
    private ReviewAction action;

    @Column(columnDefinition = "TEXT")
    private String reviewComments;

    @Column(length = 500)
    private String rejectReason;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('LOW','NORMAL','HIGH','URGENT') DEFAULT 'NORMAL'")
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PENDING','IN_REVIEW','APPROVED','REJECTED','MODIFIED') DEFAULT 'PENDING'")
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() { if (submittedAt == null) submittedAt = LocalDateTime.now(); }

    public enum ContentType { DISEASE, DRUG, SKILL, RESEARCH, CORRECTION }
    public enum ReviewAction { APPROVE, REJECT, MODIFY, RETURN }
    public enum Priority { LOW, NORMAL, HIGH, URGENT }
    public enum ReviewStatus { PENDING, IN_REVIEW, APPROVED, REJECTED, MODIFIED }
}
