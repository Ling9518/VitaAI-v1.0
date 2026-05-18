package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "disease_drug_rel")
@IdClass(DiseaseDrugRel.DiseaseDrugRelId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseDrugRel {
    @Id
    @Column(name = "disease_id")
    private Long diseaseId;

    @Id
    @Column(name = "drug_id")
    private Long drugId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('RECOMMENDED','CONTRAINDICATED','CAUTION') DEFAULT 'RECOMMENDED'")
    @Builder.Default
    private Relationship relationship = Relationship.RECOMMENDED;

    @Column(length = 200)
    private String description;

    @Column(length = 20)
    private String evidenceLevel;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Relationship { RECOMMENDED, CONTRAINDICATED, CAUTION }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiseaseDrugRelId implements Serializable {
        private Long diseaseId;
        private Long drugId;
    }
}
