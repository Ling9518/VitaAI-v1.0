package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
public class HealthRecordController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<Map<String, Object>> get(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(userService.getHealthRecord(userId));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> data,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success("创建成功", userService.createHealthRecord(userId, data));
    }

    @PutMapping
    public ApiResponse<Map<String, Object>> update(@RequestBody Map<String, Object> data,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success("更新成功", userService.updateHealthRecord(userId, data));
    }
}
