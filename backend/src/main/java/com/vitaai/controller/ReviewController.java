package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.entity.ContentReview;
import com.vitaai.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/pending")
    public ApiResponse<Page<ContentReview>> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String contentType) {
        ContentReview.ContentType ct = null;
        if (contentType != null) {
            try { ct = ContentReview.ContentType.valueOf(contentType); } catch (IllegalArgumentException ignored) {}
        }
        return ApiResponse.success(reviewService.getPendingReviews(ct, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ContentReview> getDetail(@PathVariable Long id) {
        return ApiResponse.success(reviewService.getReview(id));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<ContentReview> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long reviewerId = Long.parseLong(body.getOrDefault("reviewerId", "1"));
        String comments = body.getOrDefault("comments", "");
        return ApiResponse.success(reviewService.approveReview(id, reviewerId, comments));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<ContentReview> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long reviewerId = Long.parseLong(body.getOrDefault("reviewerId", "1"));
        String reason = body.getOrDefault("reason", "不符合要求");
        String comments = body.getOrDefault("comments", "");
        return ApiResponse.success(reviewService.rejectReview(id, reviewerId, reason, comments));
    }

    @PostMapping("/batch")
    public ApiResponse<?> batchReview(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<Integer> idInts = (java.util.List<Integer>) body.get("ids");
        java.util.List<Long> ids = idInts.stream().map(Integer::longValue).toList();
        String action = (String) body.get("action");
        Long reviewerId = Long.parseLong(body.getOrDefault("reviewerId", "1").toString());
        String reason = (String) body.getOrDefault("reason", "");
        String comments = (String) body.getOrDefault("comments", "");
        return ApiResponse.success(reviewService.batchReview(ids, action, reviewerId, reason, comments));
    }
}
