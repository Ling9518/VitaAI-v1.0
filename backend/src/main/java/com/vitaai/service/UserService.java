package com.vitaai.service;

import com.vitaai.entity.DiagnosisRecord;
import com.vitaai.entity.HealthRecord;
import com.vitaai.entity.User;
import com.vitaai.repository.AiConversationRepository;
import com.vitaai.repository.DiagnosisRecordRepository;
import com.vitaai.repository.FavoriteRepository;
import com.vitaai.repository.HealthRecordRepository;
import com.vitaai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final DiagnosisRecordRepository diagnosisRecordRepository;
    private final AiConversationRepository aiConversationRepository;
    private final FavoriteRepository favoriteRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return buildUserProfile(user);
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (updates.containsKey("realName")) user.setRealName((String) updates.get("realName"));
        if (updates.containsKey("phone")) user.setPhone((String) updates.get("phone"));
        if (updates.containsKey("avatarUrl")) user.setAvatarUrl((String) updates.get("avatarUrl"));
        if (updates.containsKey("gender")) user.setGender(User.Gender.valueOf((String) updates.get("gender")));
        if (updates.containsKey("birthday")) user.setBirthday(java.time.LocalDate.parse((String) updates.get("birthday")));

        userRepository.save(user);
        return buildUserProfile(user);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("两次密码不一致");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        // Delete favorites
        favoriteRepository.deleteByUserId(userId);

        // Delete health record
        healthRecordRepository.findByUserId(userId).ifPresent(hr -> {
            healthRecordRepository.delete(hr);
        });

        // Delete diagnosis records and their conversations
        java.util.List<DiagnosisRecord> records = diagnosisRecordRepository.findAllByUserId(userId);
        for (DiagnosisRecord record : records) {
            aiConversationRepository.deleteByDiagnosisRecordId(record.getId());
            diagnosisRecordRepository.delete(record);
        }

        // Delete the user
        userRepository.deleteById(userId);
    }

    // 健康档案
    public Map<String, Object> getHealthRecord(Long userId) {
        HealthRecord hr = healthRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("健康档案不存在，请先创建"));
        return buildHealthRecordMap(hr);
    }

    @Transactional
    public Map<String, Object> createHealthRecord(Long userId, Map<String, Object> data) {
        if (healthRecordRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("健康档案已存在");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        HealthRecord hr = HealthRecord.builder()
                .user(user)
                .bloodType(HealthRecord.BloodType.valueOf((String) data.getOrDefault("bloodType", "UNKNOWN")))
                .height(new BigDecimal(data.getOrDefault("height", "0").toString()))
                .weight(new BigDecimal(data.getOrDefault("weight", "0").toString()))
                .medicalHistory((String) data.getOrDefault("medicalHistory", ""))
                .allergyHistory((String) data.getOrDefault("allergyHistory", ""))
                .medicationRecords((String) data.getOrDefault("medicationRecords", ""))
                .familyHistory((String) data.getOrDefault("familyHistory", ""))
                .surgeryHistory((String) data.getOrDefault("surgeryHistory", ""))
                .lifestyle((String) data.getOrDefault("lifestyle", ""))
                .lastCheckupDate(parseDate(data.get("lastCheckupDate")))
                .build();
        recalculateCompleteness(hr);
        healthRecordRepository.save(hr);
        return buildHealthRecordMap(hr);
    }

    @Transactional
    public Map<String, Object> updateHealthRecord(Long userId, Map<String, Object> data) {
        HealthRecord hr = healthRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("健康档案不存在"));

        if (data.containsKey("bloodType")) hr.setBloodType(HealthRecord.BloodType.valueOf((String) data.get("bloodType")));
        if (data.containsKey("height")) hr.setHeight(new BigDecimal(data.get("height").toString()));
        if (data.containsKey("weight")) hr.setWeight(new BigDecimal(data.get("weight").toString()));
        if (data.containsKey("medicalHistory")) hr.setMedicalHistory((String) data.get("medicalHistory"));
        if (data.containsKey("allergyHistory")) hr.setAllergyHistory((String) data.get("allergyHistory"));
        if (data.containsKey("medicationRecords")) hr.setMedicationRecords((String) data.get("medicationRecords"));
        if (data.containsKey("familyHistory")) hr.setFamilyHistory((String) data.get("familyHistory"));
        if (data.containsKey("surgeryHistory")) hr.setSurgeryHistory((String) data.get("surgeryHistory"));
        if (data.containsKey("lifestyle")) hr.setLifestyle((String) data.get("lifestyle"));
        if (data.containsKey("lastCheckupDate")) hr.setLastCheckupDate(parseDate(data.get("lastCheckupDate")));

        recalculateCompleteness(hr);
        healthRecordRepository.save(hr);
        return buildHealthRecordMap(hr);
    }

    private Map<String, Object> buildUserProfile(User user) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole().name());
        profile.put("realName", user.getRealName());
        profile.put("phone", user.getPhone());
        profile.put("avatarUrl", user.getAvatarUrl());
        profile.put("gender", user.getGender() != null ? user.getGender().name() : null);
        profile.put("birthday", user.getBirthday());
        profile.put("doctorLicense", user.getDoctorLicense());
        profile.put("doctorTitle", user.getDoctorTitle());
        profile.put("doctorDept", user.getDoctorDept());
        profile.put("isVerified", user.getIsVerified());
        profile.put("lastLoginAt", user.getLastLoginAt());
        profile.put("createdAt", user.getCreatedAt());
        return profile;
    }

    private void recalculateCompleteness(HealthRecord hr) {
        int total = 10;
        int filled = 0;

        if (hr.getBloodType() != null && hr.getBloodType() != HealthRecord.BloodType.UNKNOWN) filled++;
        if (hr.getHeight() != null && hr.getHeight().compareTo(BigDecimal.ZERO) > 0) filled++;
        if (hr.getWeight() != null && hr.getWeight().compareTo(BigDecimal.ZERO) > 0) filled++;
        if (hr.getLastCheckupDate() != null) filled++;
        if (!isEmptyField(hr.getMedicalHistory())) filled++;
        if (!isEmptyField(hr.getAllergyHistory())) filled++;
        if (!isEmptyField(hr.getMedicationRecords())) filled++;
        if (!isEmptyField(hr.getFamilyHistory())) filled++;
        if (!isEmptyField(hr.getSurgeryHistory())) filled++;
        if (!isEmptyField(hr.getLifestyle())) filled++;

        BigDecimal rate = new BigDecimal(filled * 100 / total);
        hr.setCompletenessRate(rate);
        hr.setIsComplete(filled == total);
    }

    private boolean isEmptyField(String value) {
        return value == null || value.isBlank() || "[]".equals(value.trim()) || "{}".equals(value.trim());
    }

    private java.time.LocalDate parseDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDate d) return d;
        String s = value.toString().trim();
        if (s.isEmpty()) return null;
        return java.time.LocalDate.parse(s);
    }

    private Map<String, Object> buildHealthRecordMap(HealthRecord hr) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", hr.getId());
        map.put("userId", hr.getUser().getId());
        map.put("bloodType", hr.getBloodType().name());
        map.put("height", hr.getHeight());
        map.put("weight", hr.getWeight());
        map.put("medicalHistory", hr.getMedicalHistory());
        map.put("allergyHistory", hr.getAllergyHistory());
        map.put("medicationRecords", hr.getMedicationRecords());
        map.put("familyHistory", hr.getFamilyHistory());
        map.put("surgeryHistory", hr.getSurgeryHistory());
        map.put("lifestyle", hr.getLifestyle());
        map.put("isComplete", hr.getIsComplete());
        map.put("completenessRate", hr.getCompletenessRate());
        map.put("lastCheckupDate", hr.getLastCheckupDate());
        map.put("createdAt", hr.getCreatedAt());
        map.put("updatedAt", hr.getUpdatedAt());
        return map;
    }
}
