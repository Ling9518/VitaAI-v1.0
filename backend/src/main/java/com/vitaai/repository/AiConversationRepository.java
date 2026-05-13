package com.vitaai.repository;

import com.vitaai.entity.AiConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByDiagnosisRecordIdOrderByCreatedAtAsc(Long diagnosisRecordId);

    Page<AiConversation> findByDiagnosisRecordIdOrderByCreatedAtAsc(Long diagnosisRecordId, Pageable pageable);

    void deleteByDiagnosisRecordId(Long diagnosisRecordId);
}
