package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('REGISTER','LOGIN','PASSWORD_RESET')")
    private VerifyType type;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    private Boolean isUsed = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }

    public enum VerifyType { REGISTER, LOGIN, PASSWORD_RESET }
}
