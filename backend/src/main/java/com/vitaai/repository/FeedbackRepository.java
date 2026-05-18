package com.vitaai.repository;

import com.vitaai.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Page<Feedback> findByStatusOrderByCreatedAtDesc(Feedback.FeedbackStatus status, Pageable pageable);
    Page<Feedback> findByTypeOrderByCreatedAtDesc(Feedback.FeedbackType type, Pageable pageable);
}
