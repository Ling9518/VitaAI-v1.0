package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.dto.PageResponse;
import com.vitaai.entity.*;
import com.vitaai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final DiseaseRepository diseaseRepository;
    private final DrugRepository drugRepository;
    private final DiagnosisRecordRepository diagnosisRecordRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("totalUsers", userRepository.count());
        data.put("totalDoctors", userRepository.findByRole(User.Role.DOCTOR, PageRequest.of(0, 1)).getTotalElements());
        data.put("totalDiseases", diseaseRepository.count());
        data.put("totalDrugs", drugRepository.count());
        data.put("totalDiagnoses", diagnosisRecordRepository.count());

        long pendingDiseases = diseaseRepository.findByStatus(Disease.Status.PENDING, PageRequest.of(0, 1)).getTotalElements();
        long pendingDrugs = drugRepository.findByStatus(Drug.Status.PENDING, PageRequest.of(0, 1)).getTotalElements();
        data.put("pendingReviews", pendingDiseases + pendingDrugs);

        return ApiResponse.success(data);
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<Map<String, Object>>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String role) {
        Page<User> users;
        if (role != null && !role.isEmpty()) {
            users = userRepository.findByRole(User.Role.valueOf(role.toUpperCase()),
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        } else {
            users = userRepository.findAll(
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        }

        List<Map<String, Object>> list = users.getContent().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("role", u.getRole().name());
            m.put("realName", u.getRealName());
            m.put("isDisabled", u.getIsDisabled());
            m.put("isVerified", u.getIsVerified());
            m.put("lastLoginAt", u.getLastLoginAt());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();

        return ApiResponse.success(PageResponse.of(list, page, pageSize, users.getTotalElements()));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (body.containsKey("isDisabled")) user.setIsDisabled((Boolean) body.get("isDisabled"));
        if (body.containsKey("role")) user.setRole(User.Role.valueOf((String) body.get("role")));
        if (body.containsKey("isVerified")) user.setIsVerified((Boolean) body.get("isVerified"));
        userRepository.save(user);
        return ApiResponse.success("更新成功", null);
    }

    @GetMapping("/content/pending")
    public ApiResponse<Map<String, Object>> pendingContent(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Disease> diseases = diseaseRepository.findByStatusIn(
                List.of(Disease.Status.PENDING), PageRequest.of(page - 1, pageSize));
        Page<Drug> drugs = drugRepository.findByStatus(
                Drug.Status.PENDING, PageRequest.of(page - 1, pageSize));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("diseases", PageResponse.of(diseases.getContent(), page, pageSize, diseases.getTotalElements()));
        data.put("drugs", PageResponse.of(drugs.getContent(), page, pageSize, drugs.getTotalElements()));
        return ApiResponse.success(data);
    }

    @PutMapping("/content/diseases/{id}/review")
    public ApiResponse<Void> reviewDisease(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Disease disease = diseaseRepository.findById(id).orElseThrow(() -> new RuntimeException("疾病不存在"));
        String action = (String) body.get("action");
        if ("APPROVED".equals(action)) {
            disease.setStatus(Disease.Status.APPROVED);
        } else if ("REJECTED".equals(action)) {
            disease.setStatus(Disease.Status.REJECTED);
            disease.setRejectReason((String) body.getOrDefault("reason", ""));
        }
        diseaseRepository.save(disease);
        return ApiResponse.success("审核完成", null);
    }

    @PutMapping("/content/drugs/{id}/review")
    public ApiResponse<Void> reviewDrug(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Drug drug = drugRepository.findById(id).orElseThrow(() -> new RuntimeException("药品不存在"));
        String action = (String) body.get("action");
        if ("APPROVED".equals(action)) {
            drug.setStatus(Drug.Status.APPROVED);
        } else if ("REJECTED".equals(action)) {
            drug.setStatus(Drug.Status.REJECTED);
            drug.setRejectReason((String) body.getOrDefault("reason", ""));
        }
        drugRepository.save(drug);
        return ApiResponse.success("审核完成", null);
    }

    @GetMapping("/audit-logs")
    public ApiResponse<PageResponse<AuditLog>> auditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        Page<AuditLog> logs = auditLogRepository.findAll(
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.success(PageResponse.of(logs.getContent(), page, pageSize, logs.getTotalElements()));
    }
}
