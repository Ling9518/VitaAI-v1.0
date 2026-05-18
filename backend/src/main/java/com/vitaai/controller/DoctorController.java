package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.dto.PageResponse;
import com.vitaai.entity.DiagnosisRecord;
import com.vitaai.entity.Disease;
import com.vitaai.entity.Drug;
import com.vitaai.entity.User;
import com.vitaai.repository.DiagnosisRecordRepository;
import com.vitaai.repository.UserRepository;
import com.vitaai.service.DiseaseService;
import com.vitaai.service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final UserRepository userRepository;
    private final DiagnosisRecordRepository diagnosisRecordRepository;
    private final DiseaseService diseaseService;
    private final DrugService drugService;

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(HttpServletRequest req) {
        Long doctorId = (Long) req.getAttribute("userId");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalPatients", userRepository.countByRole(User.Role.USER));
        data.put("totalDiagnoses", diagnosisRecordRepository.count());
        data.put("pendingReviews", 0);
        return ApiResponse.success(data);
    }

    @GetMapping("/patients")
    public ApiResponse<PageResponse<Map<String, Object>>> patients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<User> users = userRepository.findByRole(User.Role.USER,
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<Map<String, Object>> list = users.getContent().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("realName", u.getRealName());
            m.put("email", u.getEmail());
            m.put("lastLoginAt", u.getLastLoginAt());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();

        return ApiResponse.success(PageResponse.of(list, page, pageSize, users.getTotalElements()));
    }

    @GetMapping("/diagnoses")
    public ApiResponse<PageResponse<DiagnosisRecord>> diagnoses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<DiagnosisRecord> records = diagnosisRecordRepository.findAll(
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.success(PageResponse.of(records.getContent(), page, pageSize, records.getTotalElements()));
    }

    // ========== Disease Feedback ==========

    @GetMapping("/diseases")
    public ApiResponse<Map<String, Object>> listDiseases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Disease> diseases = diseaseService.getDiseases(page, pageSize, keyword, null);
        return ApiResponse.success(Map.of(
                "list", diseases.getContent(),
                "pagination", Map.of("total", diseases.getTotalElements(), "page", page, "pageSize", pageSize)
        ));
    }

    @PostMapping("/diseases")
    public ApiResponse<Disease> createDisease(@RequestBody Disease disease, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ApiResponse.success("提交成功，待管理员审核", diseaseService.createDisease(disease, userId));
    }

    @PutMapping("/diseases/{id}")
    public ApiResponse<Disease> updateDisease(@PathVariable Long id, @RequestBody Disease disease) {
        return ApiResponse.success("修改建议已提交，待管理员审核", diseaseService.updateDisease(id, disease));
    }

    // ========== Drug Feedback ==========

    @GetMapping("/drugs")
    public ApiResponse<Map<String, Object>> listDrugs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Drug> drugs = drugService.getDrugs(page, pageSize, keyword, null);
        return ApiResponse.success(Map.of(
                "list", drugs.getContent(),
                "pagination", Map.of("total", drugs.getTotalElements(), "page", page, "pageSize", pageSize)
        ));
    }

    @PostMapping("/drugs")
    public ApiResponse<Drug> createDrug(@RequestBody Drug drug, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ApiResponse.success("提交成功，待管理员审核", drugService.createDrug(drug, userId));
    }

    @PutMapping("/drugs/{id}")
    public ApiResponse<Drug> updateDrug(@PathVariable Long id, @RequestBody Drug drug) {
        return ApiResponse.success("修改建议已提交，待管理员审核", drugService.updateDrug(id, drug));
    }
}
