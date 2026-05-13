package com.vitaai.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "drugs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Drug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String genericName;

    @Column(length = 200)
    private String brandName;

    @Column(columnDefinition = "TEXT")
    private String searchKeywords;

    @Column(columnDefinition = "TEXT")
    private String efficacy;

    @Column(name = "`usage`", columnDefinition = "TEXT")
    private String usage2;

    @Column(columnDefinition = "TEXT")
    private String dosage;

    @Column(columnDefinition = "TEXT")
    private String sideEffect;

    @Column(columnDefinition = "TEXT")
    private String contraindication;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PRESCRIPTION','OTC','HERBAL','BIOLOGIC') DEFAULT 'OTC'")
    private DrugType drugType;

    @Column(length = 50)
    private String form;

    @Column(length = 100)
    private String specification;

    @Column(length = 200)
    private String storage;

    @Column(length = 200)
    private String manufacturer;

    @Column(length = 50)
    private String approvalNo;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

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

    public enum DrugType {
        PRESCRIPTION, OTC, HERBAL, BIOLOGIC
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
