package com.vitaai.service;

import com.vitaai.entity.*;
import com.vitaai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ContentReviewRepository reviewRepository;
    private final DiseaseRepository diseaseRepository;
    private final DrugRepository drugRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public Page<ContentReview> getPendingReviews(ContentReview.ContentType contentType, int page, int size) {
        if (contentType != null) {
            return reviewRepository.findByContentTypeAndStatusOrderBySubmittedAtDesc(
                    contentType, ContentReview.ReviewStatus.PENDING, PageRequest.of(page, size));
        }
        return reviewRepository.findByStatusOrderBySubmittedAtDesc(
                ContentReview.ReviewStatus.PENDING, PageRequest.of(page, size));
    }

    public ContentReview getReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审核记录不存在"));
    }

    @Transactional
    public ContentReview approveReview(Long id, Long reviewerId, String comments) {
        ContentReview review = getReview(id);
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审核人不存在"));

        review.setReviewer(reviewer);
        review.setAction(ContentReview.ReviewAction.APPROVE);
        review.setReviewComments(comments);
        review.setStatus(ContentReview.ReviewStatus.APPROVED);
        review.setReviewedAt(LocalDateTime.now());
        review.setCompletedAt(LocalDateTime.now());

        updateContentStatus(review.getContentType(), review.getContentId(), "APPROVED");
        return reviewRepository.save(review);
    }

    @Transactional
    public ContentReview rejectReview(Long id, Long reviewerId, String reason, String comments) {
        ContentReview review = getReview(id);
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审核人不存在"));

        review.setReviewer(reviewer);
        review.setAction(ContentReview.ReviewAction.REJECT);
        review.setRejectReason(reason);
        review.setReviewComments(comments);
        review.setStatus(ContentReview.ReviewStatus.REJECTED);
        review.setReviewedAt(LocalDateTime.now());
        review.setCompletedAt(LocalDateTime.now());

        updateContentStatus(review.getContentType(), review.getContentId(), "REJECTED");
        return reviewRepository.save(review);
    }

    @Transactional
    public List<ContentReview> batchReview(List<Long> ids, String action, Long reviewerId, String reason, String comments) {
        List<ContentReview> results = new ArrayList<>();
        for (Long id : ids) {
            if ("APPROVE".equals(action)) {
                results.add(approveReview(id, reviewerId, comments));
            } else {
                results.add(rejectReview(id, reviewerId, reason, comments));
            }
        }
        return results;
    }

    @Transactional
    public ContentReview submitForReview(ContentReview.ContentType type, Long contentId, Long submitterId) {
        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        ContentReview review = ContentReview.builder()
                .contentType(type)
                .contentId(contentId)
                .submitter(submitter)
                .build();
        return reviewRepository.save(review);
    }

    private void updateContentStatus(ContentReview.ContentType type, Long contentId, String status) {
        switch (type) {
            case DISEASE -> diseaseRepository.findById(contentId).ifPresent(d -> {
                d.setStatus(Disease.Status.valueOf(status));
                diseaseRepository.save(d);
            });
            case DRUG -> drugRepository.findById(contentId).ifPresent(d -> {
                d.setStatus(Drug.Status.valueOf(status));
                drugRepository.save(d);
            });
            case SKILL -> skillRepository.findById(contentId).ifPresent(s -> {
                s.setStatus("APPROVED".equals(status) ? Skill.Status.APPROVED : Skill.Status.DRAFT);
                skillRepository.save(s);
            });
        }
    }
}
