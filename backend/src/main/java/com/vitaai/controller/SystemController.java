package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.entity.Feedback;
import com.vitaai.repository.FeedbackRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final FeedbackRepository feedbackRepository;

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("version", "1.0.0");
        data.put("uptime", System.currentTimeMillis());

        Map<String, String> components = new LinkedHashMap<>();
        components.put("database", "UP");
        components.put("aiService", "UP");
        data.put("components", components);

        return ApiResponse.success(data);
    }

    @GetMapping("/hot-searches")
    public ApiResponse<List<String>> hotSearches() {
        return ApiResponse.success(List.of("高血压", "糖尿病", "头痛", "失眠", "胃炎", "感冒", "哮喘", "湿疹"));
    }

    @GetMapping("/dicts/{type}")
    public ApiResponse<List<Map<String, String>>> getDict(@PathVariable String type) {
        List<Map<String, String>> dicts = new ArrayList<>();
        switch (type) {
            case "body_system" -> {
                dicts.addAll(buildDictItems(
                    new String[][]{{"RESPIRATORY","呼吸系统"},{"DIGESTIVE","消化系统"},{"CARDIOVASCULAR","循环系统"},
                    {"NERVOUS","神经系统"},{"MUSCULOSKELETAL","运动系统"},{"DERMATOLOGICAL","皮肤系统"},
                    {"ENT","五官系统"},{"URINARY","泌尿系统"}}));
            }
            case "drug_type" -> {
                dicts.addAll(buildDictItems(
                    new String[][]{{"PRESCRIPTION","处方药"},{"OTC","非处方药"},{"HERBAL","中药"},{"BIOLOGIC","生物制品"}}));
            }
            case "disease_classification" -> {
                dicts.addAll(buildDictItems(
                    new String[][]{{"RESPIRATORY","呼吸系统疾病"},{"DIGESTIVE","消化系统疾病"},{"CARDIOVASCULAR","心血管疾病"},
                    {"NERVOUS","神经系统疾病"},{"ENDOCRINE","内分泌疾病"},{"DERMATOLOGICAL","皮肤疾病"},
                    {"MUSCULOSKELETAL","骨科与风湿"},{"URINARY","泌尿系统疾病"},{"HEMATOLOGICAL","血液系统疾病"},
                    {"INFECTIOUS","感染性疾病"}}));
            }
            case "severity" -> {
                dicts.addAll(buildDictItems(
                    new String[][]{{"MILD","轻微"},{"MODERATE","中等"},{"SEVERE","严重"}}));
            }
            default -> {}
        }
        return ApiResponse.success(dicts);
    }

    private List<Map<String, String>> buildDictItems(String[][] items) {
        List<Map<String, String>> list = new ArrayList<>();
        for (String[] item : items) list.add(Map.of("code", item[0], "name", item[1]));
        return list;
    }

    @PostMapping("/feedback")
    public ApiResponse<Feedback> submitFeedback(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Feedback feedback = Feedback.builder()
                .subject((String) body.getOrDefault("subject", ""))
                .content((String) body.getOrDefault("content", ""))
                .contactEmail((String) body.getOrDefault("contactEmail", ""))
                .type(parseType((String) body.getOrDefault("type", "OTHER")))
                .build();

        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) {
            feedback.setUser(com.vitaai.entity.User.builder().id(userId).build());
        }

        return ApiResponse.success(feedbackRepository.save(feedback));
    }

    private Feedback.FeedbackType parseType(String type) {
        try { return Feedback.FeedbackType.valueOf(type); }
        catch (IllegalArgumentException e) { return Feedback.FeedbackType.OTHER; }
    }
}
