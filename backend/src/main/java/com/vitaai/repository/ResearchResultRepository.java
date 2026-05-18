package com.vitaai.repository;

import com.vitaai.entity.ResearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ResearchResultRepository extends JpaRepository<ResearchResult, Long> {
    Page<ResearchResult> findByStatusOrderByCreatedAtDesc(ResearchResult.ReviewStatus status, Pageable pageable);
    @Query("SELECT r FROM ResearchResult r WHERE r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    Page<ResearchResult> findAllApproved(Pageable pageable);
}
