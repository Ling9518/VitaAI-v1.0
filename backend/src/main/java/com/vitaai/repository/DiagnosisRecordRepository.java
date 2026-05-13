package com.vitaai.repository;

import com.vitaai.entity.DiagnosisRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosisRecordRepository extends JpaRepository<DiagnosisRecord, Long> {
    Page<DiagnosisRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<DiagnosisRecord> findByConversationId(String conversationId);

    List<DiagnosisRecord> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    long countByUserId(Long userId);

    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    void deleteByUserId(Long userId);

    List<DiagnosisRecord> findAllByUserId(Long userId);
}
