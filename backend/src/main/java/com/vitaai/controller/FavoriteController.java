package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public ApiResponse<Map<String, Object>> toggle(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        return ApiResponse.success(favoriteService.toggle(userId, targetType, targetId));
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam String targetType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(favoriteService.getUserFavorites(userId, targetType));
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> check(@RequestParam String targetType,
                                       @RequestParam Long targetId,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(favoriteService.isFavorited(userId, targetType, targetId));
    }
}
