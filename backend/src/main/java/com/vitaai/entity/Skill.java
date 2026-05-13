package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String diseaseIds;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(length = 50)
    private String aiModel;

    @Builder.Default
    private Integer priority = 0;

    @Builder.Default
    private Integer usageCount = 0;

    @Builder.Default
    private Integer successCount = 0;

    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal accuracyRate = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 3, scale = 2)
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    @Column(length = 20)
    private String version = "1.0";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('DRAFT','PENDING','APPROVED','DEPRECATED') DEFAULT 'APPROVED'")
    private Status status;

    private LocalDateTime publishedAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum Status {
        DRAFT, PENDING, APPROVED, DEPRECATED
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
