package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.dto.LoginRequest;
import com.vitaai.dto.RegisterRequest;
import com.vitaai.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestBody Map<String, String> request) {
        return ApiResponse.success(authService.refreshToken(request.get("refreshToken")));
    }

    @PostMapping("/captcha/send")
    public ApiResponse<Void> sendCaptcha(@RequestBody Map<String, String> request) {
        authService.sendVerificationCode(request.get("email"),
                request.getOrDefault("type", "REGISTER"));
        return ApiResponse.success("验证码已发送", null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success("已退出登录", null);
    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@RequestBody Map<String, String> request) {
        authService.verifyEmail(request.get("email"), request.get("code"));
        return ApiResponse.success("邮箱验证成功", null);
    }

    @PostMapping("/password/forgot")
    public ApiResponse<Void> forgotPassword(@RequestBody Map<String, String> request) {
        authService.sendVerificationCode(request.get("email"), "RESET");
        return ApiResponse.success("验证码已发送", null);
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody Map<String, String> request) {
        authService.resetPassword(
                request.get("email"),
                request.get("code"),
                request.get("newPassword"),
                request.get("confirmPassword"));
        return ApiResponse.success("密码重置成功", null);
    }
}
