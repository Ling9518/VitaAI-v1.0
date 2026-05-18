package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "symptom_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String assessmentData;

    @Column(columnDefinition = "TEXT")
    private String symptomCategories;

    @Column(columnDefinition = "TEXT")
    private String selectedSymptoms;

    @Column(length = 50)
    private String duration;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('MILD','MODERATE','SEVERE','CRITICAL') DEFAULT 'MODERATE'")
    private Severity severity;

    @Column(columnDefinition = "TEXT")
    private String preliminaryResult;

    @Column(columnDefinition = "TEXT")
    private String suggestedActions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_diagnosis_id")
    private DiagnosisRecord linkedDiagnosis;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('IN_PROGRESS','COMPLETED','ABANDONED') DEFAULT 'IN_PROGRESS'")
    @Builder.Default
    private AssessmentStatus status = AssessmentStatus.IN_PROGRESS;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    public enum Severity { MILD, MODERATE, SEVERE, CRITICAL }
    public enum AssessmentStatus { IN_PROGRESS, COMPLETED, ABANDONED }
}
