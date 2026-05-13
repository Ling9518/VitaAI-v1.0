package com.vitaai.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('VISITOR','USER','DOCTOR','ADMIN') DEFAULT 'USER'")
    private Role role;

    @Column(length = 100)
    private String realName;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('MALE','FEMALE','UNKNOWN') DEFAULT 'UNKNOWN'")
    private Gender gender;

    private LocalDate birthday;

    @Column(length = 100)
    private String doctorLicense;

    @Column(length = 50)
    private String doctorTitle;

    @Column(length = 50)
    private String doctorDept;

    @Builder.Default
    private Boolean isVerified = false;

    @Builder.Default
    private Boolean isDisabled = false;

    private LocalDateTime lastLoginAt;

    @Builder.Default
    private Integer loginAttempts = 0;

    private LocalDateTime lockedUntil;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum Role {
        VISITOR, USER, DOCTOR, ADMIN
    }

    public enum Gender {
        MALE, FEMALE, UNKNOWN
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
