package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_doctor_rel")
@IdClass(PatientDoctorRel.PatientDoctorRelId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDoctorRel {
    @Id
    @Column(name = "patient_id")
    private Long patientId;

    @Id
    @Column(name = "doctor_id")
    private Long doctorId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PENDING','APPROVED','REVOKED') DEFAULT 'PENDING'")
    @Builder.Default
    private Authorization authorization = Authorization.PENDING;

    private LocalDateTime authorizedAt;

    @Column(length = 200)
    private String revokeReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Authorization { PENDING, APPROVED, REVOKED }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientDoctorRelId implements Serializable {
        private Long patientId;
        private Long doctorId;
    }
}
