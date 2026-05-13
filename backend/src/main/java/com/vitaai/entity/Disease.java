package com.vitaai.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diseases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Disease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String alias;

    @Column(length = 20)
    private String icdCode;

    @Column(columnDefinition = "TEXT")
    private String cause;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    private String prevention;

    @Column(columnDefinition = "TEXT")
    private String complications;

    @Column(length = 100)
    private String classification;

    @Column(length = 50)
    private String bodySystem;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('MILD','MODERATE','SEVERE') DEFAULT 'MODERATE'")
    private Severity severity;

    @Builder.Default
    private Boolean isInfectious = false;

    @Builder.Default
    private Boolean isChronic = false;

    @Builder.Default
    private Integer viewsCount = 0;

    @Builder.Default
    private Integer favoritesCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    private LocalDateTime reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'APPROVED'")
    private Status status;

    @Column(length = 500)
    private String rejectReason;

    private LocalDateTime publishedAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum Severity {
        MILD, MODERATE, SEVERE
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
