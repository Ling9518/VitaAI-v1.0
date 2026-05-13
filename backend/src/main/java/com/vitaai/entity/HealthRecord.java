package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('A','B','AB','O','UNKNOWN') DEFAULT 'UNKNOWN'")
    private BloodType bloodType;

    @Column(precision = 5, scale = 1)
    private BigDecimal height;

    @Column(precision = 5, scale = 1)
    private BigDecimal weight;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(columnDefinition = "TEXT")
    private String allergyHistory;

    @Column(columnDefinition = "TEXT")
    private String medicationRecords;

    @Column(columnDefinition = "TEXT")
    private String familyHistory;

    @Column(columnDefinition = "TEXT")
    private String surgeryHistory;

    @Column(columnDefinition = "TEXT")
    private String lifestyle;

    @Builder.Default
    private Boolean isComplete = false;

    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal completenessRate = BigDecimal.ZERO;

    private LocalDate lastCheckupDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum BloodType {
        A, B, AB, O, UNKNOWN
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
