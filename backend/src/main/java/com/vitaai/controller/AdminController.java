package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.dto.PageResponse;
import com.vitaai.entity.*;
import com.vitaai.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    // ========== Disease Management (Admin) ==========

    @GetMapping("/diseases")
    public ApiResponse<Map<String, Object>> listDiseases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<Disease> diseases;
        if (status != null && !status.isEmpty()) {
            Disease.Status s = Disease.Status.valueOf(status.toUpperCase());
            diseases = diseaseRepository.findByStatus(s, PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
        } else if (keyword != null && !keyword.isEmpty()) {
            diseases = diseaseRepository.search(keyword, PageRequest.of(page - 1, pageSize));
        } else {
            diseases = diseaseRepository.findAll(PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
        }
        return ApiResponse.success(Map.of(
                "list", diseases.getContent(),
                "pagination", Map.of("total", diseases.getTotalElements(), "page", page, "pageSize", pageSize)
        ));
    }

    @PostMapping("/diseases")
    public ApiResponse<Disease> createDisease(@RequestBody Disease disease, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        disease.setCreatedBy(user);
        disease.setStatus(Disease.Status.APPROVED);
        disease.setPublishedAt(java.time.LocalDateTime.now());
        return ApiResponse.success("创建成功", diseaseRepository.save(disease));
    }

    @PutMapping("/diseases/{id}")
    public ApiResponse<Void> updateDisease(@PathVariable Long id, @RequestBody Disease updates) {
        Disease existing = diseaseRepository.findById(id).orElseThrow(() -> new RuntimeException("疾病不存在"));
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getAlias() != null) existing.setAlias(updates.getAlias());
        if (updates.getIcdCode() != null) existing.setIcdCode(updates.getIcdCode());
        if (updates.getCause() != null) existing.setCause(updates.getCause());
        if (updates.getSymptoms() != null) existing.setSymptoms(updates.getSymptoms());
        if (updates.getDiagnosis() != null) existing.setDiagnosis(updates.getDiagnosis());
        if (updates.getTreatment() != null) existing.setTreatment(updates.getTreatment());
        if (updates.getPrevention() != null) existing.setPrevention(updates.getPrevention());
        if (updates.getComplications() != null) existing.setComplications(updates.getComplications());
        if (updates.getClassification() != null) existing.setClassification(updates.getClassification());
        if (updates.getBodySystem() != null) existing.setBodySystem(updates.getBodySystem());
        if (updates.getSeverity() != null) existing.setSeverity(updates.getSeverity());
        if (updates.getIsInfectious() != null) existing.setIsInfectious(updates.getIsInfectious());
        if (updates.getIsChronic() != null) existing.setIsChronic(updates.getIsChronic());
        // Admin edit keeps current status
        diseaseRepository.save(existing);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/diseases/{id}")
    public ApiResponse<Void> deleteDisease(@PathVariable Long id) {
        diseaseRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    // ========== Drug Management (Admin) ==========

    @GetMapping("/drugs")
    public ApiResponse<Map<String, Object>> listDrugs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<Drug> drugs;
        if (status != null && !status.isEmpty()) {
            Drug.Status s = Drug.Status.valueOf(status.toUpperCase());
            drugs = drugRepository.findByStatus(s, PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
        } else if (keyword != null && !keyword.isEmpty()) {
            drugs = drugRepository.search(keyword, PageRequest.of(page - 1, pageSize));
        } else {
            drugs = drugRepository.findAll(PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
        }
        return ApiResponse.success(Map.of(
                "list", drugs.getContent(),
                "pagination", Map.of("total", drugs.getTotalElements(), "page", page, "pageSize", pageSize)
        ));
    }

    @PostMapping("/drugs")
    public ApiResponse<Drug> createDrug(@RequestBody Drug drug, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ApiResponse.success("创建成功", createDrugStatic(drug, userId));
    }

    @PutMapping("/drugs/{id}")
    public ApiResponse<Void> updateDrug(@PathVariable Long id, @RequestBody Drug updates) {
        Drug existing = drugRepository.findById(id).orElseThrow(() -> new RuntimeException("药品不存在"));
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getGenericName() != null) existing.setGenericName(updates.getGenericName());
        if (updates.getBrandName() != null) existing.setBrandName(updates.getBrandName());
        if (updates.getEfficacy() != null) existing.setEfficacy(updates.getEfficacy());
        if (updates.getUsage2() != null) existing.setUsage2(updates.getUsage2());
        if (updates.getDosage() != null) existing.setDosage(updates.getDosage());
        if (updates.getSideEffect() != null) existing.setSideEffect(updates.getSideEffect());
        if (updates.getContraindication() != null) existing.setContraindication(updates.getContraindication());
        if (updates.getDrugType() != null) existing.setDrugType(updates.getDrugType());
        if (updates.getForm() != null) existing.setForm(updates.getForm());
        if (updates.getSpecification() != null) existing.setSpecification(updates.getSpecification());
        if (updates.getStorage() != null) existing.setStorage(updates.getStorage());
        if (updates.getManufacturer() != null) existing.setManufacturer(updates.getManufacturer());
        if (updates.getApprovalNo() != null) existing.setApprovalNo(updates.getApprovalNo());
        if (updates.getPrice() != null) existing.setPrice(updates.getPrice());
        // Admin edit keeps current status
        drugRepository.save(existing);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/drugs/{id}")
    public ApiResponse<Void> deleteDrug(@PathVariable Long id) {
        drugRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    private Drug createDrugStatic(Drug drug, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        drug.setCreatedBy(user);
        drug.setStatus(User.Role.ADMIN.equals(user.getRole()) ? Drug.Status.APPROVED : Drug.Status.PENDING);
        if (drug.getStatus() == Drug.Status.APPROVED) {
            drug.setPublishedAt(java.time.LocalDateTime.now());
        }
        return drugRepository.save(drug);
    }
}
