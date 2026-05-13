package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.dto.ChatRequest;
import com.vitaai.dto.PageResponse;
import com.vitaai.entity.DiagnosisRecord;
import com.vitaai.service.AIService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@Valid @RequestBody ChatRequest request,
                                                   HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ApiResponse.success(aiService.chat(userId, request));
    }

    @GetMapping("/diagnoses")
    public ApiResponse<PageResponse<DiagnosisRecord>> getDiagnoses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Page<DiagnosisRecord> records = aiService.getHistory(userId, page, pageSize);
        return ApiResponse.success(PageResponse.of(
                records.getContent(), page, pageSize, records.getTotalElements()));
    }

    @GetMapping("/diagnoses/{id}")
    public ApiResponse<Map<String, Object>> getDiagnosis(@PathVariable Long id) {
        return ApiResponse.success(aiService.getDiagnosisDetail(id));
    }

    @PostMapping("/diagnoses/{id}/feedback")
    public ApiResponse<Void> submitFeedback(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        aiService.submitFeedback(id, body.get("accuracy"), body.get("comments"));
        return ApiResponse.success("反馈提交成功", null);
    }
}
