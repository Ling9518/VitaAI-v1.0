package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.entity.SymptomAssessment;
import com.vitaai.service.SymptomService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomService symptomService;

    @GetMapping("/categories")
    public ApiResponse<List<Map<String, Object>>> categories() {
        return ApiResponse.success(symptomService.getCategoryTree());
    }

    @PostMapping("/assessments")
    public ApiResponse<SymptomAssessment> createAssessment(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null && body.get("userId") != null) {
            userId = Long.parseLong(body.get("userId").toString());
        }
        if (userId == null) throw new RuntimeException("请先登录");
        return ApiResponse.success(symptomService.createAssessment(userId, body));
    }

    @GetMapping("/assessments/{id}")
    public ApiResponse<SymptomAssessment> getAssessment(@PathVariable Long id) {
        return ApiResponse.success(symptomService.getAssessment(id));
    }

    @PostMapping("/assessments/{id}/feedback")
    public ApiResponse<SymptomAssessment> submitFeedback(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String accuracy = body.getOrDefault("accuracy", "ACCURATE");
        String comments = body.getOrDefault("comments", "");
        return ApiResponse.success(symptomService.submitFeedback(id, accuracy, comments));
    }
}
