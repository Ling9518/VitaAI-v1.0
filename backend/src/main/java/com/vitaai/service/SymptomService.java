package com.vitaai.service;

import com.vitaai.entity.*;
import com.vitaai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SymptomService {

    private final SymptomCategoryRepository categoryRepository;
    private final SymptomAssessmentRepository assessmentRepository;
    private final UserRepository userRepository;

    public List<Map<String, Object>> getCategoryTree() {
        List<SymptomCategory> roots = categoryRepository.findByParentIsNullAndIsActiveTrueOrderBySortOrder();
        List<Map<String, Object>> result = new ArrayList<>();
        for (SymptomCategory root : roots) {
            Map<String, Object> node = buildCategoryNode(root);
            result.add(node);
        }
        return result;
    }

    private Map<String, Object> buildCategoryNode(SymptomCategory cat) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", cat.getId());
        node.put("name", cat.getName());
        node.put("code", cat.getCode());
        node.put("icon", cat.getIcon());
        List<SymptomCategory> children = categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrder(cat.getId());
        if (!children.isEmpty()) {
            List<Map<String, Object>> childNodes = new ArrayList<>();
            for (SymptomCategory child : children) {
                childNodes.add(buildCategoryNode(child));
            }
            node.put("children", childNodes);
        }
        return node;
    }

    @Transactional
    public SymptomAssessment createAssessment(Long userId, Map<String, Object> request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        SymptomAssessment assessment = SymptomAssessment.builder()
                .user(user)
                .assessmentData(toJson(request))
                .symptomCategories(toJson(request.get("symptomCategories")))
                .selectedSymptoms(toJson(request.get("selectedSymptoms")))
                .duration((String) request.get("duration"))
                .severity(SymptomAssessment.Severity.MODERATE)
                .build();

        return assessmentRepository.save(assessment);
    }

    public SymptomAssessment getAssessment(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("自测记录不存在"));
    }

    @Transactional
    public SymptomAssessment completeAssessment(Long id, Map<String, Object> result) {
        SymptomAssessment assessment = getAssessment(id);
        assessment.setPreliminaryResult(toJson(result.get("preliminaryResult")));
        assessment.setSuggestedActions(toJson(result.get("suggestedActions")));
        if (result.get("severity") != null) {
            assessment.setSeverity(SymptomAssessment.Severity.valueOf(result.get("severity").toString()));
        }
        assessment.setStatus(SymptomAssessment.AssessmentStatus.COMPLETED);
        assessment.setCompletedAt(LocalDateTime.now());
        return assessmentRepository.save(assessment);
    }

    @Transactional
    public SymptomAssessment submitFeedback(Long id, String accuracy, String comments) {
        SymptomAssessment assessment = getAssessment(id);
        Map<String, String> feedback = new LinkedHashMap<>();
        feedback.put("accuracy", accuracy);
        feedback.put("comments", comments);
        assessment.setPreliminaryResult(assessment.getPreliminaryResult() + "|FEEDBACK:" + toJson(feedback));
        return assessmentRepository.save(assessment);
    }

    @SuppressWarnings("unchecked")
    private String toJson(Object obj) {
        if (obj == null) return "{}";
        if (obj instanceof String) return "{\"value\":\"" + obj + "\"}";
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
