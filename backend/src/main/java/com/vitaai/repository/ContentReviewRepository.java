package com.vitaai.repository;

import com.vitaai.entity.ContentReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContentReviewRepository extends JpaRepository<ContentReview, Long> {
    Page<ContentReview> findByStatusOrderBySubmittedAtDesc(ContentReview.ReviewStatus status, Pageable pageable);
    Page<ContentReview> findByContentTypeAndStatusOrderBySubmittedAtDesc(ContentReview.ContentType type, ContentReview.ReviewStatus status, Pageable pageable);
    List<ContentReview> findBySubmitterIdOrderBySubmittedAtDesc(Long submitterId);
}
