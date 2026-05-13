package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.dto.PageResponse;
import com.vitaai.entity.DiagnosisRecord;
import com.vitaai.entity.User;
import com.vitaai.repository.DiagnosisRecordRepository;
import com.vitaai.repository.UserRepository;
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

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(HttpServletRequest req) {
        Long doctorId = (Long) req.getAttribute("userId");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalPatients", userRepository.count());
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
}
