package com.vitaai.repository;

import com.vitaai.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByEmailAndCodeAndIsUsedFalseOrderByCreatedAtDesc(String email, String code);
    void deleteByEmail(String email);
}
