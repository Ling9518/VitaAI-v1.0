package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> updates,
                                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success("更新成功", userService.updateProfile(userId, updates));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId,
                body.get("oldPassword"),
                body.get("newPassword"),
                body.get("confirmPassword"));
        return ApiResponse.success("密码修改成功", null);
    }

    @DeleteMapping("/account")
    public ApiResponse<Void> deleteAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.deleteAccount(userId);
        return ApiResponse.success("账号已注销", null);
    }
}
