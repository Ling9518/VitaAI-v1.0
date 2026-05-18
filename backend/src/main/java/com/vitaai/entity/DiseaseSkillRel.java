package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "disease_skill_rel")
@IdClass(DiseaseSkillRel.DiseaseSkillRelId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseSkillRel {
    @Id
    @Column(name = "disease_id")
    private Long diseaseId;

    @Id
    @Column(name = "skill_id")
    private Long skillId;

    @Builder.Default
    @Column(precision = 3, scale = 2)
    private java.math.BigDecimal weight = new java.math.BigDecimal("1.00");

    @Column(columnDefinition = "TEXT")
    private String matchKeywords;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiseaseSkillRelId implements Serializable {
        private Long diseaseId;
        private Long skillId;
    }
}
