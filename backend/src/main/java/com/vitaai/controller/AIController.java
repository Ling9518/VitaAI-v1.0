package com.vitaai.controller;

import com.vitaai.ai.MedicalSkillService;
import com.vitaai.dto.ApiResponse;
import com.vitaai.dto.ChatRequest;
import com.vitaai.dto.PageResponse;
import com.vitaai.entity.DiagnosisRecord;
import com.vitaai.entity.Skill;
import com.vitaai.service.AIService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final MedicalSkillService medicalSkillService;

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

    @DeleteMapping("/diagnoses/{id}")
    public ApiResponse<Void> deleteDiagnosis(@PathVariable Long id) {
        aiService.deleteDiagnosis(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody ChatRequest request,
                                  HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return aiService.streamChat(userId, request);
    }

    @GetMapping("/report/{diagnosisId}")
    public ApiResponse<Map<String, Object>> getReport(@PathVariable Long diagnosisId) {
        return ApiResponse.success(aiService.generateReport(diagnosisId));
    }

    // ========== Skills (active, for AI) ==========

    @GetMapping("/skills")
    public ApiResponse<List<Map<String, Object>>> getSkills() {
        return ApiResponse.success(aiService.getSkills());
    }

    // ========== Skills CRUD (admin) ==========

    @GetMapping("/skills/all")
    public ApiResponse<PageResponse<Skill>> getAllSkills(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Skill> skills = medicalSkillService.getAllSkills(page, pageSize);
        return ApiResponse.success(PageResponse.of(
                skills.getContent(), page, pageSize, skills.getTotalElements()));
    }

    @GetMapping("/skills/{id}")
    public ApiResponse<Skill> getSkill(@PathVariable Long id) {
        return ApiResponse.success(medicalSkillService.getSkill(id));
    }

    @PostMapping("/skills")
    public ApiResponse<Skill> createSkill(@RequestBody Skill skill, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ApiResponse.success("创建成功", medicalSkillService.createSkill(skill, userId));
    }

    @PutMapping("/skills/{id}")
    public ApiResponse<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        return ApiResponse.success("更新成功", medicalSkillService.updateSkill(id, skill));
    }

    @DeleteMapping("/skills/{id}")
    public ApiResponse<Void> deleteSkill(@PathVariable Long id) {
        medicalSkillService.deleteSkill(id);
        return ApiResponse.success("删除成功", null);
    }

    // ========== Vita-skills file operations ==========

    @PostMapping("/skills/upload")
    public ApiResponse<Void> uploadSkillFile(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String content = body.get("content");
        if (name == null || name.isBlank()) return ApiResponse.badRequest("技能名称不能为空");
        if (content == null || content.isBlank()) return ApiResponse.badRequest("技能内容不能为空");
        try {
            medicalSkillService.uploadSkillFile(name, content);
            return ApiResponse.success("上传成功", null);
        } catch (IOException e) {
            return ApiResponse.serverError("文件写入失败: " + e.getMessage());
        }
    }

    @PostMapping("/skills/sync")
    public ApiResponse<Map<String, Object>> syncSkills() {
        try {
            Map<String, Object> result = medicalSkillService.syncFromVitaSkills();
            return ApiResponse.success("同步完成", result);
        } catch (IOException e) {
            return ApiResponse.serverError("同步失败: " + e.getMessage());
        }
    }
}
