package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false, length = 50)
    private String module;

    @Column(nullable = false, length = 100)
    private String operation;

    @Column(length = 500)
    private String operationDesc;

    @Column(length = 10)
    private String requestMethod;

    @Column(length = 500)
    private String requestUrl;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    private Integer responseCode;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Integer executionTime;

    @Column(length = 100)
    private String traceId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
