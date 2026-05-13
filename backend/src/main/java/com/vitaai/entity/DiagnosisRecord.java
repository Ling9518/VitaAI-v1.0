package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_record_id")
    private HealthRecord healthRecord;

    @Column(columnDefinition = "TEXT")
    private String symptomSummary;

    @Column(columnDefinition = "TEXT")
    private String symptomsDetail;

    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;

    @Column(columnDefinition = "TEXT")
    private String suggestedDiseases;

    @Column(columnDefinition = "TEXT")
    private String suggestedDrugs;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('LOW','MEDIUM','HIGH','CRITICAL')")
    private SeverityLevel severityLevel;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @Column(columnDefinition = "TEXT")
    private String warningText;

    @Builder.Default
    private Boolean needsHospital = false;

    @Column(length = 500)
    private String reportUrl;

    @Column(columnDefinition = "TEXT")
    private String conversationSummary;

    @Builder.Default
    private Integer messageCount = 0;

    @Builder.Default
    private Integer totalTokens = 0;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ACCURATE','MOSTLY_ACCURATE','INACCURATE','PENDING') DEFAULT 'PENDING'")
    private FeedbackAccuracy feedbackAccuracy;

    @Column(columnDefinition = "TEXT")
    private String feedbackDetail;

    private LocalDateTime feedbackAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum SeverityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum FeedbackAccuracy {
        ACCURATE, MOSTLY_ACCURATE, INACCURATE, PENDING
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
