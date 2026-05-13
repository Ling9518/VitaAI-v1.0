package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemController {

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

    @GetMapping("/dicts/{type}")
    public ApiResponse<List<Map<String, String>>> getDict(@PathVariable String type) {
        List<Map<String, String>> dicts = new ArrayList<>();
        switch (type) {
            case "body_system" -> {
                String[][] items = {
                    {"RESPIRATORY", "呼吸系统"}, {"DIGESTIVE", "消化系统"},
                    {"CARDIOVASCULAR", "循环系统"}, {"NERVOUS", "神经系统"},
                    {"MUSCULOSKELETAL", "运动系统"}, {"DERMATOLOGICAL", "皮肤系统"},
                    {"ENT", "五官系统"}, {"URINARY", "泌尿系统"}
                };
                for (String[] item : items) dicts.add(Map.of("code", item[0], "name", item[1]));
            }
            case "drug_type" -> {
                String[][] items = {
                    {"PRESCRIPTION", "处方药"}, {"OTC", "非处方药"},
                    {"HERBAL", "中药"}, {"BIOLOGIC", "生物制品"}
                };
                for (String[] item : items) dicts.add(Map.of("code", item[0], "name", item[1]));
            }
            case "disease_classification" -> {
                String[][] items = {
                    {"RESPIRATORY", "呼吸系统疾病"}, {"DIGESTIVE", "消化系统疾病"},
                    {"CARDIOVASCULAR", "心血管疾病"}, {"NERVOUS", "神经系统疾病"},
                    {"ENDOCRINE", "内分泌疾病"}, {"DERMATOLOGICAL", "皮肤疾病"},
                    {"MUSCULOSKELETAL", "骨科与风湿"}, {"URINARY", "泌尿系统疾病"},
                    {"HEMATOLOGICAL", "血液系统疾病"}, {"INFECTIOUS", "感染性疾病"}
                };
                for (String[] item : items) dicts.add(Map.of("code", item[0], "name", item[1]));
            }
        }
        return ApiResponse.success(dicts);
    }
}
